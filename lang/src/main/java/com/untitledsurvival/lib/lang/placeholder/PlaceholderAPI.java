package com.untitledsurvival.lib.lang.placeholder;

import com.untitledsurvival.lib.lang.placeholder.builtin.PlayerPlaceholder;
import com.untitledsurvival.lib.lang.placeholder.builtin.TimePlaceholder;
import com.untitledsurvival.lib.lang.placeholder.builtin.wildcard.ServerPlaceholder;
import com.untitledsurvival.lib.lang.placeholder.shorthands.PlayerShorthand;
import com.untitledsurvival.lib.lang.placeholder.shorthands.ShorthandBase;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
public class PlaceholderAPI {
    public static final Pattern placeholderPattern = Pattern.compile("%((?<namespace>\\w+):)?(?<placeholder>\\w+)%");

    private static final ShorthandBase basePlaceholder = new ShorthandBase();

    @Getter private static final EventStack eventStack = new EventStack();
    @Getter private static final Map<String, Placeholder<?>> namespacePlaceholders = new HashMap<>();

    static {
        register(eventStack, basePlaceholder, new TimePlaceholder(), new PlayerPlaceholder(), new ServerPlaceholder());
    }

    public static void register(Placeholder<?>... additions) {
        for (Placeholder<?> placeholder : additions) {
            if (namespacePlaceholders.containsKey(placeholder.getNamespace())) {
                throw new IllegalArgumentException("Placeholder with namespace '%s' already registered".formatted(placeholder.getNamespace()));
            }

            // associate the placeholder with the namespace
            namespacePlaceholders.put(placeholder.getNamespace(), placeholder);
        }
    }

    public static void shorthand(String name, PlayerShorthand placeholder) {
        // add the standalone
        basePlaceholder.getStandalones().put(name, placeholder);
    }

    public static String apply(String format, Object... objs) {
        Matcher matcher = placeholderPattern.matcher(format);

        // the modified version of the string
        // that will be returned
        String output = format;

        while (matcher.find()) {
            // both properties of the placeholder
            String namespace = matcher.group("namespace");

            // find the matching placeholder
            Placeholder<?> placeholder = namespacePlaceholders.get(namespace);
            if (placeholder == null) {
                // there is no matching placeholder
                log.warning("Attempt at using placeholder with namespace '%s' which doesn't exist".formatted(namespace));
                continue;
            }

            String name = matcher.group("placeholder");
            String replaceResult;

            // if the placeholder allows null then pass it null and use that result
            if (placeholder.allowNull()) {
                replaceResult = placeholder.apply(null, name);
            }

            // for each object provided make an attempt to apply it
            // (if there's more than 1 of the same type it will apply the first one)
            else {
                // find any objects that are castable to the generic type of the placeholder
                Object matching = Arrays.stream(objs)
                        .filter(obj -> placeholder.getType().isAssignableFrom(obj.getClass()))
                        .findFirst().orElse(null);

                // if there was no matching type then placeholders
                // are attempting to be used
                if (matching == null) {
                    log.warning("Call to PlaceholderAPI.apply with matching namespace '%s' found but no matching type '%s'".formatted(namespace, placeholder.getType().getSimpleName()));
                    continue;
                }

                replaceResult = placeholderToType(placeholder, matching.getClass()).apply(matching, name);
            }

            // if the result is null then this namespace didn't
            // have the specified placeholder
            if (replaceResult == null) {
                log.warning("Attempt at using placeholder %%%s:%s%% which doesn't exist".formatted(namespace, name));
                continue;
            }

            // replace the matched placeholder with the result
            output = output.replace(matcher.group(), replaceResult);
        }

        // clear the event stack depending on its life count
        eventStack.lifeClear();
        return output;
    }

    public static <T> Placeholder<T> placeholderToType(Placeholder<?> placeholder, Class<?> type) {
        if (placeholder.getType().isAssignableFrom(type)) {
            //noinspection unchecked
            return (Placeholder<T>) placeholder;
        }

        return null;
    }
}

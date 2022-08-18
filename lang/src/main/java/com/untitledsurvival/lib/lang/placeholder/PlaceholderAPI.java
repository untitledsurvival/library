package com.untitledsurvival.lib.lang.placeholder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PlaceholderAPI {
    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%((\\w+):)?(\\w+)%");

    private static PlaceholderAPI instance = null;

    public static PlaceholderAPI get() {
        if (instance == null) {
            instance = new PlaceholderAPI();
            instance.isGlobalPlaceholder = true;
        }

        return instance;
    }

    // whether this is the global instance of the PlaceholderAPI
    private boolean isGlobalPlaceholder = false;

    private final Map<String, String> constants = new HashMap<>();

    public void addConstant(String name, String value) {
        // do not allow usage of constants on global placeholders
        if (isGlobalPlaceholder) {
            return;
        }

        constants.put(name, value);
    }

    public String apply(String format, Object... objs) {
        return PLACEHOLDER_PATTERN.matcher(format).replaceAll(match -> {
            String namespace = match.group(2);
            String target = match.group(3);

            if (namespace == null) {
                // fetch from the constants map or return the unchanged match
                return constants.getOrDefault(target, match.group());
            }

            for (Object obj : objs) {
                List<? extends Placeholder<?>> placeholders = PlaceholderRegistry.getPlaceholdersOfType(obj.getClass(), namespace);

                // find the matching placeholder
                for (Placeholder<?> placeholder : placeholders) {
                    String result = placeholderToType(placeholder, obj.getClass()).apply(obj, target);

                    // if the result is not null, return it
                    if (result != null) {
                        return result;
                    }
                }
            }

            // if there is no matching placeholder, return the unmodified placeholder
            return match.group();
        });
    }

    private static <T> Placeholder<T> placeholderToType(Placeholder<?> placeholder, Class<?> type) {
        if (placeholder.getType().isAssignableFrom(type)) {
            //noinspection unchecked
            return (Placeholder<T>) placeholder;
        }

        return null;
    }
}

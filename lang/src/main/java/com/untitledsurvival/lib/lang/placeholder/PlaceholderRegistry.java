package com.untitledsurvival.lib.lang.placeholder;

import lombok.Getter;

import java.util.*;

public class PlaceholderRegistry {
    @Getter private static final Map<String, List<Placeholder<?>>> NAMESPACES = new HashMap<>();

    public static void register(Placeholder<?>... placeholders) {
        for (Placeholder<?> placeholder : placeholders) {
            List<Placeholder<?>> placeholderList = NAMESPACES.getOrDefault(placeholder.getNamespace(), new ArrayList<>());

            // add the placeholder to the list
            placeholderList.add(placeholder);

            // store the list relative to the namespace
            NAMESPACES.put(placeholder.getNamespace(), placeholderList);
        }
    }

    public static List<Placeholder<?>> getPlaceholders(String namespace) {
        return NAMESPACES.getOrDefault(namespace, new ArrayList<>());
    }

    public static List<Placeholder<?>> getPlaceholdersOfType(Class<?> type) {
        return getPlaceholdersOfType(type, null);
    }

    public static List<Placeholder<?>> getPlaceholdersOfType(Class<?> type, String namespace) {
        List<Placeholder<?>> placeholders = new ArrayList<>();
        List<Placeholder<?>> toSearch = NAMESPACES.values().stream().flatMap(Collection::stream).toList();

        if (namespace != null) {
            toSearch = getPlaceholders(namespace);
        }

        toSearch.forEach(placeholder -> {
            if (type.isAssignableFrom(placeholder.getType()) || placeholder instanceof Placeholder.Wildcard) {
                placeholders.add(placeholder);
            }
        });

        return placeholders;
    }
}

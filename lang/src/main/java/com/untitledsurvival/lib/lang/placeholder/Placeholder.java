package com.untitledsurvival.lib.lang.placeholder;

import lombok.NonNull;

public interface Placeholder<T> {
    String apply(T object, @NonNull String placeholderName);

    Class<T> getType();

    default String getNamespace() {
        return null;
    }

    default boolean allowNull() {
        return false;
    }

    interface Wildcard extends Placeholder<Object> {
        @Override
        default String apply(Object object, @NonNull String placeholderName) {
            return apply(placeholderName);
        }

        String apply(@NonNull String placeholderName);

        @Override
        default Class<Object> getType() {
            return Object.class;
        }
    }
}

package com.untitledsurvival.lib.lang.utils;

import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public final class ScopedFormatter {
    @Getter
    private final String scope;
    @Getter
    private final PluginResource resource;

    ScopedFormatter(PluginResource resource) {
        this(resource, null);
    }

    ScopedFormatter(PluginResource resource, String scope) {
        this.scope = scope;
        this.resource = resource;
    }

    public MessageFormatter fmt(@NonNull String key) {
        String path = this.scope == null ? key : "%s.%s".formatted(this.scope, key);
        Object value = resource.get(path);

        if (value instanceof List<?>) {
            value = ((List<?>) value).stream().map(Object::toString).collect(Collectors.joining("\n"));
        }

        if (!(value instanceof String string)) {
            // send a formatter back for the value
            return new MessageFormatter(key);
        }

        return new MessageFormatter(string);
    }
}

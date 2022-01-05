package com.untitledsurvival.lib;

import lombok.AccessLevel;
import lombok.Getter;

public abstract class LibModule {
    @Getter(AccessLevel.PUBLIC)
    boolean initialized = false;

    abstract public void init();
    public void deinit() {}

    void initialize() {
        if (initialized) {
            return; // do not call init a second time
        }

        init();
        initialized = true;
    }

    void deinitialize() {
        if (!initialized) {
            return; // do not deinit if not initialized
        }

        deinit();
        initialized = false;
    }
}

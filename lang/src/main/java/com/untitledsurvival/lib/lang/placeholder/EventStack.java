package com.untitledsurvival.lib.lang.placeholder;

import java.util.HashMap;

public class EventStack extends HashMap<String, String> implements Placeholder.Wildcard {
    private int lives = 0;

    public EventStack push(String key, String value) {
        super.put(key, value);
        return this;
    }

    /**
     * @param x how many times clear can be called before it goes through (inclusive)
     */
    public void live(int x) {
        this.lives = x;
    }

    public void lifeClear() {
        if (--lives > 0) {
            return;
        }

        super.clear();
    }

    @Override
    public String apply(Object obj, String placeholderName) {
        return getOrDefault(placeholderName, null);
    }

    @Override
    public String getNamespace() {
        return "event";
    }
}

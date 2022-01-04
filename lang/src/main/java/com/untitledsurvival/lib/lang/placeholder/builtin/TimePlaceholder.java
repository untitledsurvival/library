package com.untitledsurvival.lib.lang.placeholder.builtin;

import com.untitledsurvival.lib.lang.placeholder.Placeholder;

public class TimePlaceholder implements Placeholder<TimePlaceholder.Time> {
    @Override
    public String apply(Time time, String placeholderName) {
        long format = switch (placeholderName) {
            case "days" -> time.days;
            case "hours" -> time.hours;
            case "minutes" -> time.minutes;
            case "seconds" -> time.seconds;

            default -> 69_420;
        };

        return "%,d".formatted(format);
    }

    @Override
    public Class<Time> getType() {
        return Time.class;
    }

    @Override
    public String getNamespace() {
        return "time";
    }

    public record Time(long days, long hours, long minutes, long seconds) {}
}

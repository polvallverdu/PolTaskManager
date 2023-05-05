package dev.polv.taskmanager.utils;

import java.time.Duration;

public class DurationUtils {

    /**
     *
     * @param ticks Minecraft Server Ticks (20 ticks per second)
     * @return Duration
     */
    public Duration fromTicks(long ticks) {
        return Duration.ofMillis(ticks * 50);
    }

}

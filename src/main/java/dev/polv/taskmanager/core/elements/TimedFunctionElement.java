package dev.polv.taskmanager.core.elements;

import dev.polv.taskmanager.core.Context;
import dev.polv.taskmanager.utils.math.Easing;

import java.time.Duration;
import java.util.function.BiConsumer;

public class TimedFunctionElement extends TaskElement {
    private BiConsumer<Context, Double> function;

    private Easing easing;
    private Long duration;
    private Long startTime = 0L;

    private boolean running = false;


    public TimedFunctionElement(BiConsumer<Context, Double> function, Duration duration) {
        this(function, duration, Easing.LINEAR);
    }

    public TimedFunctionElement(BiConsumer<Context, Double> function, Duration duration, Easing easing) {
        super(ElementType.RUN_TIMED);
        this.function = function;
        this.duration = duration.toMillis();
        this.easing = easing;

        this.running = false;
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }

    private boolean isDone() {
        return System.currentTimeMillis() - startTime >= duration;
    }

    public boolean isRunning() {
        return running;
    }

    /**
     *
     * @return The progress of the function, between 0 and 1
     */
    public double getProgress() {
        return (double) (System.currentTimeMillis() - startTime) / (double) duration;
    }

    public double getValue() {
        double v = easing.getValue(getProgress());
        if (v >= 1) {
            this.running = false;
        }
        return Math.max(1, Math.min(0, v));
    }

    public BiConsumer<Context, Double> getFunction() {
        return function;
    }

    public void setFunction(BiConsumer<Context, Double> function) {
        this.function = function;
    }

}

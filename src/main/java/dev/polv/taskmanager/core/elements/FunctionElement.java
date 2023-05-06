package dev.polv.taskmanager.core.elements;

import dev.polv.taskmanager.core.Context;

import java.util.function.Consumer;

public class FunctionElement extends TaskElement {

    private Consumer<Context> function;
    private boolean running;

    public FunctionElement(Consumer<Context> function) {
        this(function, false);
    }

    public FunctionElement(Consumer<Context> function, boolean async) {
        this(function, async, false);
    }

    public FunctionElement(Consumer<Context> function, boolean async, boolean await) {
        super(async ? (await ? ElementType.RUN_ASYNC_AWAIT : ElementType.RUN_ASYNC) : ElementType.RUN);
        this.setFunction(function);
    }

    public void accept(Context context) {
        if (this.running) return;

        this.running = true;
        function.accept(context);
        this.running = false;
    }

    public void setFunction(Consumer<Context> function) {
        this.function = function;
    }

    public boolean isRunning() {
        return running;
    }
}

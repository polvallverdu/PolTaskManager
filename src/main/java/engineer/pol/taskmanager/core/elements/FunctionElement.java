package engineer.pol.taskmanager.core.elements;

import engineer.pol.taskmanager.core.Context;

import java.util.function.Consumer;

public class FunctionElement extends TaskElement {

    private Consumer<Context> function;

    public FunctionElement(Consumer<Context> function) {
        this(function, false);
    }

    public FunctionElement(Consumer<Context> function, boolean async) {
        super(async ? ElementType.RUN_ASYNC : ElementType.RUN);
        this.function = function;
    }

    public Consumer<Context> getFunction() {
        return function;
    }

    public void setFunction(Consumer<Context> function) {
        this.function = function;
    }

}

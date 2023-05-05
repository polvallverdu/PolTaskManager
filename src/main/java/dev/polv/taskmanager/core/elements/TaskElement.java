package dev.polv.taskmanager.core.elements;

public class TaskElement {

    private ElementType type;

    public enum ElementType {
        WAIT,
        RUN,
        RUN_ASYNC,
        RUN_TIMED,
        LOOP,
    }

    protected TaskElement(ElementType type) {
        this.type = type;
    }

    public ElementType getType() {
        return type;
    }

    public void cancel() {
    }
}

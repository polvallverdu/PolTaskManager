package dev.polv.taskmanager.core.elements;

import dev.polv.taskmanager.core.TaskChain;

import java.util.concurrent.Callable;

@Deprecated(since = "Currently not implemented")
public class LoopElement extends TaskElement {

    private TaskChain loopChain;
    private Callable<Boolean> condition;

    public LoopElement(TaskChain loopChain, Callable<Boolean> condition) {
        super(ElementType.LOOP);
        this.loopChain = loopChain;
        this.condition = condition;
        // TODO
    }
}

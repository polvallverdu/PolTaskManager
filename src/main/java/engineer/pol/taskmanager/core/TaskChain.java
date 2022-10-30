package engineer.pol.taskmanager.core;

import engineer.pol.taskmanager.core.elements.FunctionElement;
import engineer.pol.taskmanager.core.elements.TaskElement;
import engineer.pol.taskmanager.core.elements.TimedFunctionElement;
import engineer.pol.taskmanager.core.elements.WaitElement;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TaskChain {

    private TaskStatus status;

    private Long nextAction = null;
    private int index = 0;

    private TaskManager manager = null;

    private final List<TaskElement> elements;

    private Context context;

    private TaskChain() {
        this(new ArrayList<>());
    }

    private TaskChain(List<TaskElement> elements) {
        this.status = TaskStatus.NONE;

        this.elements = elements;
        this.resetContext();
    }


    public static TaskChain create(TaskManager manager) {
        TaskChain chain = new TaskChain();
        chain.setManager(manager);
        return chain;
    }

    public static TaskChain copy(TaskChain taskChain) {
        TaskChain clone = new TaskChain(new ArrayList<>(taskChain.getElements()));
        clone.setManager(taskChain.manager);
        return clone;
    }

    private TaskElement getCurrentElement() {
        return elements.get(index);
    }

    private List<TaskElement> getElements() {
        return elements;
    }

    private void nextElement() {
        index++;
        if (index >= elements.size()) {
            _cancel();
            index = 0;
        }
    }

    protected void _start() {
        if (manager == null) {
            throw new IllegalStateException("TaskChain is not registered to a TaskManager");
        }
        index = 0;
        nextAction = System.currentTimeMillis();
        status = TaskStatus.RUNNING;
        tick();
    }

    protected void _schedule(Duration time) {
        if (manager == null) {
            throw new IllegalStateException("TaskChain is not registered to a TaskManager");
        }
        index = 0;
        nextAction = System.currentTimeMillis() + time.toMillis();
        status = TaskStatus.RUNNING;
        tick();
    }

    protected void _cancel() {
        this.getCurrentElement().cancel();
        nextAction = null;
        status = TaskStatus.FINISHED;
    }

    protected void tick() {
        if (status != TaskStatus.RUNNING || nextAction == null || this.manager == null) return;
        if (System.currentTimeMillis() < nextAction) return;

        TaskElement element = getCurrentElement();
        if (this.execute(element)) {
            this.nextElement();
        }
    }

    private boolean execute(TaskElement element) {
        boolean next = true;

        switch (element.getType()) {
            case WAIT -> {
                WaitElement waitElement = (WaitElement) element;
                nextAction = System.currentTimeMillis() + waitElement.getTime();
            }
            case RUN -> {
                FunctionElement functionElement = (FunctionElement) element;
                functionElement.getFunction().accept(context);
                nextAction = System.currentTimeMillis();
                tick();
            }
            case RUN_ASYNC -> {
                FunctionElement functionElement = (FunctionElement) element;
                manager.getExecutor().submit(() -> {
                    nextAction = null;
                    functionElement.getFunction().accept(context);
                    nextAction = System.currentTimeMillis();
                });
            }
            case RUN_TIMED -> {
                next = false;
                TimedFunctionElement timedFunctionElement = (TimedFunctionElement) element;
                if (!timedFunctionElement.isRunning()) {
                    timedFunctionElement.start();
                }

                double v = timedFunctionElement.getValue();
                if (v >= 1) next = true;

                timedFunctionElement.getFunction().accept(context, v);
            }
        }

        return next;
    }

    protected TaskChain setManager(@Nullable TaskManager manager) {
        if (this.manager != null) {
            this.manager.unregister(this);
        }
        this.manager = manager;
        return this;
    }

    private void resetContext() {
        this.context = new Context(this);
    }

    public boolean isFinished() {
        return status == TaskStatus.FINISHED;
    }

    public boolean isRunning() {
        return status == TaskStatus.RUNNING;
    }

    public Long getNextAction() {
        return nextAction;
    }

    public int getIndex() {
        return index;
    }

    public TaskChain wait(Duration time) {
        elements.add(new WaitElement(time));
        return this;
    }

    public TaskChain run(Consumer<Context> function) {
        elements.add(new FunctionElement(function, false));
        return this;
    }

    public TaskChain runAsync(Consumer<Context> function) {
        elements.add(new FunctionElement(function, true));
        return this;
    }
}

package engineer.pol.taskmanager.core;

import engineer.pol.taskmanager.core.elements.FunctionElement;
import engineer.pol.taskmanager.core.elements.TaskElement;
import engineer.pol.taskmanager.core.elements.WaitElement;

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


    public static TaskChain create() {
        return new TaskChain();
    }

    public static TaskChain copy(TaskChain taskChain) {
        return new TaskChain(new ArrayList<>(taskChain.getElements()));
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
        nextAction = null;
        status = TaskStatus.FINISHED;
    }

    protected void tick() {
        if (status != TaskStatus.RUNNING || nextAction == null) return;
        if (System.currentTimeMillis() < nextAction) return;

        TaskElement element = getCurrentElement();
        this.execute(element);
        this.nextElement();
    }

    private void execute(TaskElement element) {
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
        }
    }

    protected void setManager(TaskManager manager) {
        this.manager = manager;
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

package dev.polv.taskmanager.core;

import dev.polv.taskmanager.core.elements.WaitElement;
import dev.polv.taskmanager.core.elements.FunctionElement;
import dev.polv.taskmanager.core.elements.TaskElement;
import dev.polv.taskmanager.core.elements.TimedFunctionElement;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class TaskChain {

    private TaskStatus status;

    private Long nextAction = null;
    private int index = 0;
    private boolean blocked = false;

    private TaskManager manager = null;

    private final List<TaskElement> elements;

    private Context context;
    private final Lock tickLock = new ReentrantLock();
    private boolean awaiting = false;

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

    protected void _start() throws IllegalStateException {
        if (manager == null) {
            throw new IllegalStateException("TaskChain is not registered to a TaskManager");
        }
        index = 0;
        nextAction = System.currentTimeMillis();
        status = TaskStatus.RUNNING;
        // tick();
    }

    protected void _schedule(Duration time) throws IllegalStateException {
        if (manager == null) {
            throw new IllegalStateException("TaskChain is not registered to a TaskManager");
        }
        index = 0;
        nextAction = System.currentTimeMillis() + time.toMillis();
        status = TaskStatus.RUNNING;
        // tick();
    }

    protected void _cancel() {
        this.getCurrentElement().cancel();
        nextAction = null;
        status = TaskStatus.FINISHED;
    }

    protected void tick() {
        if (!tickLock.tryLock()) return;
        if (status != TaskStatus.RUNNING || nextAction == null || this.manager == null) return;
        if (System.currentTimeMillis() < nextAction) return;

        TaskElement element = getCurrentElement();
        if (this.execute(element)) {
            this.nextElement();
        }

        tickLock.unlock();
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
                functionElement.accept(context);
                nextAction = System.currentTimeMillis();
            }
            case RUN_ASYNC -> {
                FunctionElement functionElement = (FunctionElement) element;
                manager.getExecutor().submit(() -> functionElement.accept(context));
                nextAction = System.currentTimeMillis();
            }
            case RUN_ASYNC_AWAIT -> {
                FunctionElement functionElement = (FunctionElement) element;
                next = false;

                if (!functionElement.isRunning() && !this.awaiting) {
                    awaiting = true;
                    manager.getExecutor().submit(() -> functionElement.accept(context));
                } else if (!functionElement.isRunning() && this.awaiting) {
                    awaiting = false;
                    next = true;
                }
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

    public boolean isBlocked() {
        return blocked;
    }

    public Long getNextAction() {
        return nextAction;
    }

    public int getIndex() {
        return index;
    }

    /**
     * Will wait for a certain amount of time before executing next chain.
     *
     * @param time Time to wait
     * @return Same {@link TaskChain}
     */
    public TaskChain timeout(Duration time) {
        if (this.blocked) {
            throw new IllegalStateException("TaskChain is blocked");
        }

        elements.add(new WaitElement(time));
        return this;
    }

    /**
     * Will wait for a certain amount of time before executing next chain.
     * @param timeInMillis Time to wait in milliseconds
     * @return Same {@link TaskChain}
     */
    public TaskChain timeout(long timeInMillis) {
        if (this.blocked) {
            throw new IllegalStateException("TaskChain is blocked");
        }

        return timeout(Duration.ofMillis(timeInMillis));
    }

    /**
     * Runs a function synchronously. Will block current thread.
     *
     * @param function Function to run
     * @return Same {@link TaskChain}
     */
    public TaskChain run(Consumer<Context> function) {
        if (this.blocked) {
            throw new IllegalStateException("TaskChain is blocked");
        }

        elements.add(new FunctionElement(function, false));
        return this;
    }

    /**
     * Runs a function asynchronously (won't wait for it to finish).
     * @param function Function to run
     * @return Same {@link TaskChain}
     */
    public TaskChain runAsync(Consumer<Context> function) {
        if (this.blocked) {
            throw new IllegalStateException("TaskChain is blocked");
        }

        elements.add(new FunctionElement(function, true));
        return this;
    }

    /**
     * Runs a function asynchronously and waits for it to finish before continuing
     *
     * @param function Function to run
     * @return Same {@link TaskChain}
     */
    public TaskChain runAsyncAwait(Consumer<Context> function) {
        if (this.blocked) {
            throw new IllegalStateException("TaskChain is blocked");
        }

        elements.add(new FunctionElement(function, true, true));
        return this;
    }

    /**
     * Builds and runs the {@link TaskChain}.
     * <br><br>
     * * <b>This will lock the Chain. No more Chains can be attached.</b>
     */
    public void start() {
        this._start();
    }

    /**
     * Builds and schedules the {@link TaskChain}.
     * <br><br>
     * * <b>This will lock the Chain. No more Chains can be attached.</b>
     * @param time Time to wait before starting the Chain
     */
    public void schedule(Duration time) {
        this._schedule(time);
    }

    /**
     * Builds and schedules the {@link TaskChain}.
     * <br><br>
     * * <b>This will lock the Chain. No more Chains can be attached.</b>
     * @param timeInMillis Time to wait before starting the Chain in milliseconds
     */
    public void schedule(long timeInMillis) {
        this.schedule(Duration.ofMillis(timeInMillis));
    }

    /**
     * Builds and schedules the {@link TaskChain} with repetition.
     * It will run the delay first, and each time it finishes it will run the interval.
     * <br><br>
     * * <b>This will lock the Chain. No more Chains can be attached.</b>
     * @param delay Delay before starting the Chain
     * @param interval Interval between each repetition
     */
    public void repeat(Duration delay, Duration interval) {
        this.timeout(interval);
        this.run(ctx -> {
            this.index = -1; // Apaño
        });

        this.schedule(delay);
    }
}

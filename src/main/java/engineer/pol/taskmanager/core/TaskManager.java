package engineer.pol.taskmanager.core;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class TaskManager {

    private final List<TaskChain> tasks;
    private final ExecutorService executor;

    protected TaskManager() {
        this.tasks = new ArrayList<>();
        this.executor = Executors.newCachedThreadPool();
    }

    public void tick() {
        new ArrayList<>(tasks).forEach(chain -> {
            if (!chain.isRunning()) {
                return;
            }
            chain.tick();
        });
    }

    protected ExecutorService getExecutor() {
        return executor;
    }

    public static TaskManager create() {
        return new TaskManager();
    }

    public TaskChain run(Consumer<Context> function) {
        TaskChain task =TaskChain.create(this).run(function);
        this.tasks.add(task);
        return task;
    }

    public TaskChain runAsync(Consumer<Context> function) {
        TaskChain task = TaskChain.create(this).runAsync(function);
        this.tasks.add(task);
        return task;
    }

    public void register(TaskChain taskChain) {
        if (taskChain.isRunning()) {
            throw new IllegalArgumentException("Task is already running!");
        }
        if (taskChain.isFinished()) {
            throw new IllegalArgumentException("Task is already finished!");
        }
        taskChain.setManager(this);
        tasks.add(taskChain);
    }

    protected void unregister(TaskChain taskChain) {
        tasks.remove(taskChain);
    }
}

package dev.polv.taskmanager.core;


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

    /**
     * @return new {@link TaskManager} from this manager
     */
    public static TaskManager create() {
        return new TaskManager();
    }

    /**
     * Creates a {@link TaskChain} and runs {@link TaskChain#run(Consumer)}
     *
     * @param function Function to run
     * @return new {@link TaskChain}
     */
    public TaskChain run(Consumer<Context> function) {
        TaskChain task =TaskChain.create(this).run(function);
        this.tasks.add(task);
        return task;
    }

    /**
     * Creates a {@link TaskChain} and runs {@link TaskChain#runAsync(Consumer)}
     *
     * @param function Function to run
     * @return new {@link TaskChain}
     */
    public TaskChain runAsync(Consumer<Context> function) {
        TaskChain task = TaskChain.create(this).runAsync(function);
        this.tasks.add(task);
        return task;
    }

    /**
     * Registers an existing {@link TaskChain} to this manager.
     *
     * @param taskChain The {@link TaskChain} to register
     */
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

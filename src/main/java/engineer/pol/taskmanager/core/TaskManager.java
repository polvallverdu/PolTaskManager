package engineer.pol.taskmanager.core;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskManager {

    private final List<TaskChain> tasks;
    private final ExecutorService executor;

    public TaskManager() {
        this.tasks = new ArrayList<>();
        this.executor = Executors.newCachedThreadPool();
    }

    public void tick() {
        new ArrayList<>(tasks).forEach(chain -> {
            if (chain.isFinished()) {
                tasks.remove(chain);
                return;
            }
            if (chain.isRunning()) {
                chain.tick();
            }
        });
    }

    protected ExecutorService getExecutor() {
        return executor;
    }
}

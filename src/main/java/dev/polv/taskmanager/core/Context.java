package dev.polv.taskmanager.core;

import java.util.concurrent.ConcurrentHashMap;

public class Context {

    private final TaskChain chain;
    private final ConcurrentHashMap<String, Object> returnData;

    protected Context(TaskChain chain) {
        this.chain = chain;
        this.returnData = new ConcurrentHashMap<>();
    }

    /**
     * Cancel the current task
     */
    public void cancel() {
        chain._cancel();
    }

    /**
     * Get data saved of the {@link TaskChain}
     *
     * @param key The {@link String} key of the data
     * @return The data in {@link Object}
     */
    public Object getReturnData(String key) {
        return returnData.get(key);
    }

    /**
     * Save data in the {@link TaskChain}
     *
     * @param key The {@link String} key of the data
     * @param value The data in {@link Object}
     */
    public void putReturnData(String key, Object value) {
        this.returnData.put(key, value);
    }

    /**
     * Check if the {@link TaskChain} has data saved
     *
     * @param key The {@link String} key of the data
     * @return {@code true} if the {@link TaskChain} has data saved, otherwise {@code false}
     */
    public boolean hasReturnData(String key) {
        return this.returnData.containsKey(key);
    }

}

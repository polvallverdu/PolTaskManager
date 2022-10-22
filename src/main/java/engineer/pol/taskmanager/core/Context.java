package engineer.pol.taskmanager.core;

import java.util.concurrent.ConcurrentHashMap;

public class Context {

    private final TaskChain chain;
    private ConcurrentHashMap<String, Object> returnData;

    protected Context(TaskChain chain) {
        this.chain = chain;
        this.returnData = new ConcurrentHashMap<>();
    }

    public void cancel() {
        chain._cancel();
    }

    public Object getReturnData(String key) {
        return returnData.get(key);
    }

    public void putReturnData(String key, Object value) {
        this.returnData.put(key, value);
    }

}

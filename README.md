# PolTaskManager

## How to start
First instanciate a `TaskManager` object. Inside, there's a `TaskManager::tick()` method that will run the tasks.

### Fabric
```java
public class ExampleMod implements ModInitializer {
    @Override
    public void onInitialize() {
        TaskManager taskManager = TaskManager.create();
        ServerTickEvents.START_SERVER_TICK.register((xd) -> taskManager.tick());
    }
}
```
---
```java
@Environment(EnvType.CLIENT)
public class ExampleMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TaskManager taskManager = TaskManager.create();
        ClientTickEvents.START_CLIENT_TICK.register((xd) -> taskManager.tick());
    }
}
```

## How to use




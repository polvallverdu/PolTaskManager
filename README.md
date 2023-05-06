# PolTaskManager

A simple solution to creating tasks and managing them. Schedule a repeating task from anywhere, chain sync and async functions easily, cancel the task from anywhere (even inside or on another thread).

[![Release](https://jitpack.io/v/dev.polv/PolTaskManager.svg)](https://jitpack.io/#dev.polv/PolTaskManager)
## Add it to your project

### Gradle

1. Add jitpack to repo
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```
2. Add the dependency
```groovy
dependencies {
    implementation("dev.polv:PolTaskManager:{release or commit hash}") // Currently v1.0
}
```

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

It is recommended to execute the tick function from the Main App Thread.

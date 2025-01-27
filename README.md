# PooledCompletableFuture
A wrapper around CompletableFuture to make working with futures and asynchronous operation easier in Java.

## Concepts
### Pipelining
Pipelining is where you organise your code to execute in sequential chains of tasks, where each task
begins processing as soon as the previous one completes.

Suppose we have a scenario where we need to process data through multiple stages:
1. Retrieve raw data from a remote source
2. Perform computations or transformations on that fetched data
3. Save the processed data to a database
Using `PooledCompletableFuture`, we can *pipeline* these stages as follows:

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

Executor customExecutor = Executors.newFixedThreadPool(4);
CompletableFuture<String> dataFromRemoteSource = fetchDataFromRemoteSource();
PooledCompletableFuture.of(dataFromRemoteSource, customExecutor)
    .thenApply(rawData -> {
        PooledCompletableFuture<ProcessedData> processedData = processData(rawData);
        return processedData;
    })
    .thenCompose(x -> x)
    .thenApply(processedData -> {
        PooledCompletableFuture<Void> saveTask = saveInDatabase(processedData);
        return processedData;
    })
    .thenCompose(x -> x)
    .exceptionally(e -> {
        System.err.println("An error occurred: " + e.getMessage());
        return null;
    });
```
In this scenario, where:
- `fetchDataFromRemoteSource()` is a method from an external library which returns a normal `CompletableFuture<String>`
- `processData(String)` and `saveInDatabase(ProcessedData)` are owned by us, and use `PooledCompletableFuture`s
this shows how you can pipeline your execution into a linear sequence of tasks.
### Downwind asynchronous execution
Following on from the previous point, a benefit of this library is that when you specify an executor, it follows through downwind in your pipeline until otherwise specified.
This means if you specify your execution at the very beginning, any executions happening from instance methods on `PooledCompletableFuture` occur on the executor you specified originally.
Here's how it looks if you want to change what executor you want to use:
```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

Executor customExecutor = Executors.newFixedThreadPool(4);
Executor databaseExecutor = Executors.newSingleThreadExecutor();
CompletableFuture<String> dataFromRemoteSource = fetchDataFromRemoteSource();
PooledCompletableFuture.of(dataFromRemoteSource, customExecutor)
    .thenApply(rawData -> { // thenApply uses customExecutor
        PooledCompletableFuture<ProcessedData> processedData = processData(rawData);
        return processedData;
    })
    .async(databaseExecutor) // switching to use the databaseExecutor
    .thenCompose(x -> x) // this method (and onwards) uses databaseExecutor
    .thenApply(processedData -> { // this method uses databaseExecutor
        PooledCompletableFuture<Void> saveTask = saveInDatabase(processedData);
        return processedData;
    })
    .thenCompose(x -> x)
    .exceptionally(e -> { // if an error is thrown, this method uses databaseExecutor
        System.err.println("An error occurred: " + e.getMessage());
        return null;
    });
```
### Programs with a "Main Thread"
A main thread is a single thread (or a thread pool) where execution of a program primarily happens. In Minecraft, this would be the server thread.
The `PooledCompletableFuture` library allows you to specify the main thread and then use `sync()` in the pipeline to move back to the main thread seamlessly.

You can achieve this by doing:

```java
import me.emissions.concurrency.PooledExecutors;

PooledExecutors.setMainThread(<your executor>);
```
For example, with Bukkit:
```java
import me.emissions.concurrency.PooledExecutors;

public class SomePlugin extends JavaPlugin {
    [...]
    
    @Override
    public void onLoad() {
        PooledExecutors.setMainThread(Bukkit.getServer()
            .getScheduler()
            .getMainThreadExecutor(this)
        );
    }
    
    [...]
}
```

Switching to the main thread during pipelining:
```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

Executor customExecutor = Executors.newFixedThreadPool(4);
Executor databaseExecutor = Executors.newSingleThreadExecutor();
CompletableFuture<String> dataFromRemoteSource = fetchDataFromRemoteSource();
PooledCompletableFuture.of(dataFromRemoteSource, customExecutor)
    .thenApply(rawData -> { // thenApply uses customExecutor
        PooledCompletableFuture<ProcessedData> processedData = processData(rawData);
        return processedData;
    })
    .async(databaseExecutor) // switching to use the databaseExecutor
    .thenCompose(x -> x) // this method (and onwards) uses databaseExecutor
    .thenApply(processedData -> { // this method uses databaseExecutor
        PooledCompletableFuture<Void> saveTask = saveInDatabase(processedData);
        return processedData;
    })
    .thenCompose(x -> x)
    .exceptionally(e -> { // if an error is thrown, this method uses databaseExecutor
        System.err.println("An error occurred: " + e.getMessage());
        return null;
    })
    .sync() // switch to the main thread you specified
    .thenRun(() -> {
        // execution here occurs on the main thread!
        System.out.println("done task!");
    })
```

### Pooled Executors
Pooled executors are a concept unique to this library. As standard, this library ships with
executors laying dormant for use in asynchronous tasks. See the `PooledExecutors` class.

This class creates (by default) 50 single thread executors, which any asynchronously executed task will use
if not specified otherwise.

#### **NOTE:** the reason for generating 50 single thread executors (compared to a thread pool) is that this largely prevents deadlocks
with `.join()` and `.get()` -- you can deadlock a thread pool very easily!

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

CompletableFuture<String> dataFromRemoteSource = fetchDataFromRemoteSource();
PooledCompletableFuture.of(dataFromRemoteSource) // Immediately uses the pooled executors
    .thenApply(rawData -> { // executes on a random pooled executor
        PooledCompletableFuture<ProcessedData> processedData = processData(rawData);
        return processedData;
    })
    .thenCompose(x -> x) // executes on a random pooled executor
    .thenApply(processedData -> { // executes on a random pooled executor
        PooledCompletableFuture<Void> saveTask = saveInDatabase(processedData);
        return processedData;
    })
    .thenCompose(x -> x) // executes on a random pooled executor
    .exceptionally(e -> { // executes on a random pooled executor
        System.err.println("An error occurred: " + e.getMessage());
        return null;
    });
```
The above shows how pooled executors are used *by default*.

But how do I switch to using pooled executors mid-pipeline?
```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

Executor customExecutor = Executors.newFixedThreadPool(4);
CompletableFuture<String> dataFromRemoteSource = fetchDataFromRemoteSource();
PooledCompletableFuture.of(dataFromRemoteSource, customExecutor)
    .thenApply(rawData -> { // thenApply uses customExecutor
        PooledCompletableFuture<ProcessedData> processedData = processData(rawData);
        return processedData;
    })
    .async() // switching to use pooled executors <--! HERE !-->
    .thenCompose(x -> x) // this method (and onwards) uses a random pooled executor
    .thenApply(processedData -> { // this method uses uses a random pooled executor
        PooledCompletableFuture<Void> saveTask = saveInDatabase(processedData);
        return processedData;
    })
    .thenCompose(x -> x) // uses a random pooled executor
    .exceptionally(e -> { // if an error is thrown, this method uses uses a random pooled executor
        System.err.println("An error occurred: " + e.getMessage());
        return null;
    })
    .sync() // switch to the main thread you specified
    .thenRun(() -> {
        // execution here occurs on the main thread!
        System.out.println("done task!");
    })
```



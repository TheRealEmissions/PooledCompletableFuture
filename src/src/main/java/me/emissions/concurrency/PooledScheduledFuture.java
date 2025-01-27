package me.emissions.concurrency;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class PooledScheduledFuture<T> {
    private final ScheduledFuture<? extends T> future;

    private PooledScheduledFuture(final ScheduledFuture<? extends T> future) {
        this.future = future;
    }

    public static <T> PooledScheduledFuture<T> of(final ScheduledFuture<? extends T> future) {
        return new PooledScheduledFuture<>(future);
    }

    public static PooledScheduledFuture<?> schedule(
        final Runnable command,
        final long delay,
        final TimeUnit unit
    ) {
        return PooledScheduledFuture.of(PooledExecutors.getAnyExecutor()
            .schedule(command, delay, unit));
    }

    public static PooledScheduledFuture<?> schedule(
        final Runnable command,
        final long delay,
        final TimeUnit unit,
        final ScheduledExecutorService executor
    ) {
        return PooledScheduledFuture.of(executor.schedule(command, delay, unit));
    }

    public static <T> PooledCompletableFuture<T> schedule(
        final Supplier<? extends T> supplier,
        final long delay,
        final TimeUnit unit
    ) {
        final var completableFuture = new PooledCompletableFuture<T>();
        PooledScheduledFuture.schedule(
            () -> {
                completableFuture.complete(supplier);
            }, delay, unit
        );
        return completableFuture;
    }

    public static <T> PooledCompletableFuture<T> schedule(
        final Supplier<? extends T> supplier,
        final long delay,
        final TimeUnit unit,
        final ScheduledExecutorService executor
    ) {
        final var completableFuture = new PooledCompletableFuture<T>(executor);
        PooledScheduledFuture.schedule(
            () -> {
                completableFuture.complete(supplier);
            }, delay, unit, executor
        );
        return completableFuture;
    }

    public static PooledScheduledFuture<?> scheduleAtFixedRate(
        final Runnable command,
        final long initialDelay,
        final long period,
        final TimeUnit unit
    ) {
        return PooledScheduledFuture.of(PooledExecutors.getAnyExecutor()
            .scheduleAtFixedRate(command, initialDelay, period, unit));
    }

    public static PooledScheduledFuture<?> scheduleAtFixedRate(
        final Runnable command,
        final long initialDelay,
        final long period,
        final TimeUnit unit,
        final ScheduledExecutorService executor
    ) {
        return PooledScheduledFuture.of(executor.scheduleAtFixedRate(command, initialDelay, period, unit));
    }

    public static PooledScheduledFuture<?> scheduleWithFixedDelay(
        final Runnable command,
        final long initialDelay,
        final long delay,
        final TimeUnit unit
    ) {
        return PooledScheduledFuture.of(PooledExecutors.getAnyExecutor()
            .scheduleWithFixedDelay(command, initialDelay, delay, unit));
    }

    public static PooledScheduledFuture<?> scheduleWithFixedDelay(
        final Runnable command,
        final long initialDelay,
        final long delay,
        final TimeUnit unit,
        final ScheduledExecutorService executor
    ) {
        return PooledScheduledFuture.of(executor.scheduleWithFixedDelay(command, initialDelay, delay, unit));
    }

    public long getDelay(final TimeUnit unit) {
        return this.future.getDelay(unit);
    }

    public boolean cancel(final boolean mayInterruptIfRunning) {
        return this.future.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled() {
        return this.future.isCancelled();
    }

    public boolean isDone() {
        return this.future.isDone();
    }

    public T get() throws InterruptedException, ExecutionException {
        return this.future.get();
    }

    public T get(final long timeout, final TimeUnit unit) throws
        InterruptedException,
        ExecutionException,
        TimeoutException {
        return this.future.get(timeout, unit);
    }
}


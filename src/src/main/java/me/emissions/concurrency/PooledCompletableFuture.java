package me.emissions.concurrency;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.function.*;

public class PooledCompletableFuture<T> {
    private final CompletableFuture<T> future;
    private final Executor executor;

    private PooledCompletableFuture(final CompletableFuture<T> future) {
        this.future = future;
        this.executor = null;
    }

    /**
     * Create a new PooledCompletableFuture executing on the pooled executors
     */
    public PooledCompletableFuture() {
        this.future = new CompletableFuture<>();
        this.executor = null;
    }

    /**
     * Create a new PooledCompletableFuture with a provided executor
     *
     * @param executor the provided executor
     */
    public PooledCompletableFuture(final Executor executor) {
        this.future = new CompletableFuture<>();
        this.executor = executor;
    }

    /**
     * Wrap a CompletableFuture into a PooledCompletableFuture executing on the provided executor
     *
     * @param future   the future to wrap
     * @param executor the provided executor
     */
    public PooledCompletableFuture(final CompletableFuture<T> future, final Executor executor) {
        this.future = future;
        this.executor = executor;
    }

    public CompletableFuture<T> getFuture() {
        return this.future;
    }

    private boolean hasOwnExecutor() {
        return this.executor != null;
    }

    /**
     * Wrap a CompletableFuture into a PooledCompletableFuture executing on the pooled executors
     *
     * @param future the future to wrap
     * @param <T>    the return type of the CompletableFuture
     * @return a PooledCompletableFuture representing the wrapped CompletableFuture
     */
    public static <T> PooledCompletableFuture<T> of(final CompletableFuture<T> future) {
        return new PooledCompletableFuture<>(future);
    }

    /**
     * Wrap a CompletableFuture into a PooledCompletableFuture executing on the provided executor
     *
     * @param future   the future to wrap
     * @param executor the provided executor
     * @param <T>      the return type of the CompletableFuture
     * @return a PooledCompletableFuture representing the wrapped CompletableFuture
     */
    public static <T> PooledCompletableFuture<T> of(
        final CompletableFuture<T> future,
        final Executor executor
    ) {
        return new PooledCompletableFuture<>(future, executor);
    }

    /**
     * A new PooledCompletableFuture that completes when all the given CompletableFutures complete
     *
     * @param futures the CompletableFutures to wait for
     * @return a new PooledCompletableFuture that completes when all the given CompletableFutures complete
     * @see CompletableFuture#allOf(CompletableFuture...)
     */
    public static PooledCompletableFuture<Void> allOf(final CompletableFuture<?>... futures) {
        return PooledCompletableFuture.of(CompletableFuture.allOf(futures));
    }

    /**
     * A new PooledCompletableFuture that completes when all the given CompletableFutures complete. The provided executor applies
     * to downwind of the pipeline.
     *
     * @param futures  the CompletableFutures to wait for
     * @param executor the provided executor to apply downwind of the pipeline
     * @return a new PooledCompletableFuture that completes when all the given CompletableFutures complete
     * @see CompletableFuture#allOf(CompletableFuture...)
     */
    public static PooledCompletableFuture<Void> allOf(
        final CompletableFuture<?>[] futures,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(CompletableFuture.allOf(futures), executor);
    }

    /**
     * A new PooledCompletableFuture that completes when all the given CompletableFutures complete. The provided executor applies
     * to downwind of the pipeline.
     *
     * @param futures  the CompletableFutures to wait for
     * @param executor the provided executor to apply downwind of the pipeline
     * @return a new PooledCompletableFuture that completes when all the given CompletableFutures complete
     * @see CompletableFuture#allOf(CompletableFuture...)
     */
    public static PooledCompletableFuture<Void> allOf(
        final Executor executor,
        final CompletableFuture<?>... futures
    ) {
        return PooledCompletableFuture.of(CompletableFuture.allOf(futures), executor);
    }

    /**
     * Creates a new PooledCompletableFuture that completes when all the given PooledCompletableFutures complete.
     *
     * @param futures the array of PooledCompletableFutures to wait for
     * @return a new PooledCompletableFuture that completes when all the provided PooledCompletableFutures complete
     */
    public static PooledCompletableFuture<Void> allOf(final PooledCompletableFuture<?>... futures) {
        return PooledCompletableFuture.allOf(Arrays.stream(futures)
            .map(f -> f.future)
            .toArray(CompletableFuture[]::new)
        );
    }

    /**
     * Returns a new PooledCompletableFuture that completes when all the provided PooledCompletableFutures complete.
     * The provided executor applies downwind in the pipeline.
     *
     * @param futures  the array of PooledCompletableFutures to wait for
     * @param executor the executor that will apply downwind in the pipeline
     * @return a new PooledCompletableFuture that completes when all the provided PooledCompletableFutures complete
     */
    public static PooledCompletableFuture<Void> allOf(
        final PooledCompletableFuture<?>[] futures,
        final Executor executor
    ) {
        return PooledCompletableFuture.allOf(Arrays.stream(futures)
            .map(f -> f.future)
            .toArray(CompletableFuture[]::new), executor
        );
    }

    /**
     * Returns a new PooledCompletableFuture that completes when all the provided PooledCompletableFutures complete.
     * The provided executor is applied downwind in the pipeline.
     *
     * @param executor the executor that will apply downwind in the pipeline
     * @param futures  the array of PooledCompletableFutures to wait for
     * @return a new PooledCompletableFuture that completes when all the provided PooledCompletableFutures complete
     */
    public static PooledCompletableFuture<Void> allOf(
        final Executor executor,
        final PooledCompletableFuture<?>... futures
    ) {
        return PooledCompletableFuture.allOf(Arrays.stream(futures)
            .map(f -> f.future)
            .toArray(CompletableFuture[]::new), executor
        );
    }

    /**
     * Returns a new {@link PooledCompletableFuture} that completes when any of the given {@link CompletableFuture}s
     * complete.
     *
     * @param futures the CompletableFutures to wait for
     * @return a new PooledCompletableFuture that completes when any of the given CompletableFutures complete
     * @see CompletableFuture#anyOf(CompletableFuture...)
     */
    public static PooledCompletableFuture<Object> anyOf(final CompletableFuture<?>... futures) {
        return PooledCompletableFuture.of(CompletableFuture.anyOf(futures));
    }

    /**
     * Returns a new PooledCompletableFuture that completes when any of the given CompletableFutures complete.
     *
     * @param futures  the array of CompletableFutures to wait for
     * @param executor the executor that will apply downwind in the pipeline.
     * @return a new PooledCompletableFuture that completes when any of the given CompletableFutures complete
     */
    public static PooledCompletableFuture<Object> anyOf(
        final CompletableFuture<?>[] futures,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(CompletableFuture.anyOf(futures), executor);
    }

    /**
     * Returns a new PooledCompletableFuture that completes when any of the given CompletableFutures complete.
     *
     * @param executor the executor that will apply downwind in the pipeline.
     * @param futures  the array of CompletableFutures to wait for
     * @return a new PooledCompletableFuture that completes when any of the given CompletableFutures complete
     */
    public static PooledCompletableFuture<Object> anyOf(
        final Executor executor,
        final CompletableFuture<?>... futures
    ) {
        return PooledCompletableFuture.of(CompletableFuture.anyOf(futures), executor);
    }

    /**
     * Returns a new {@link PooledCompletableFuture} that completes when any of the given
     * {@link PooledCompletableFuture}s complete.
     *
     * @param futures the PooledCompletableFutures to wait for
     * @return a new PooledCompletableFuture that completes when any of the given PooledCompletableFutures complete
     * @see PooledCompletableFuture#anyOf(CompletableFuture...)
     */
    public static PooledCompletableFuture<Object> anyOf(final PooledCompletableFuture<?>... futures) {
        return PooledCompletableFuture.anyOf(Arrays.stream(futures)
            .map(f -> f.future)
            .toArray(CompletableFuture[]::new));
    }

    /**
     * Returns a new PooledCompletableFuture that completes when any of the given PooledCompletableFutures complete.
     *
     * @param futures  the array of PooledCompletableFutures to wait for
     * @param executor the executor that will apply downwind in the pipeline.
     * @return a new PooledCompletableFuture that completes when any of the given PooledCompletableFutures complete
     */
    public static PooledCompletableFuture<Object> anyOf(
        final PooledCompletableFuture<?>[] futures,
        final Executor executor
    ) {
        return PooledCompletableFuture.anyOf(
            Arrays.stream(futures)
                .map(f -> f.future)
                .toArray(CompletableFuture[]::new), executor
        );
    }

    /**
     * Returns a new PooledCompletableFuture that completes when any of the given PooledCompletableFutures complete.
     *
     * @param executor the executor that will apply downwind in the pipeline.
     * @param futures  the array of PooledCompletableFutures to wait for
     * @return a new PooledCompletableFuture that completes when any of the given PooledCompletableFutures complete
     */
    public static PooledCompletableFuture<Object> anyOf(
        final Executor executor,
        final PooledCompletableFuture<?>... futures
    ) {
        return PooledCompletableFuture.anyOf(
            Arrays.stream(futures)
                .map(f -> f.future)
                .toArray(CompletableFuture[]::new), executor
        );
    }

    /**
     * Creates a {@code PooledCompletableFuture} that is already completed with the given value.
     *
     * @param <U>   the type of the value
     * @param value the value to complete the future with
     * @return a {@code PooledCompletableFuture} representing a completed future with the specified value
     */
    public static <U> PooledCompletableFuture<U> completedFuture(final U value) {
        return PooledCompletableFuture.of(CompletableFuture.completedFuture(value));
    }

    /**
     * Creates a {@code PooledCompletableFuture} that is already completed with the given value
     * and uses the specified executor downwind in the pipeline.
     *
     * @param <U>      the type of the value
     * @param value    the value to complete the future with
     * @param executor the executor to use for asynchronous tasks
     * @return a {@code PooledCompletableFuture} representing a completed future with the specified value
     */
    public static <U> PooledCompletableFuture<U> completedFuture(final U value, final Executor executor) {
        return PooledCompletableFuture.of(CompletableFuture.completedFuture(value), executor);
    }

    /**
     * Creates a {@code PooledCompletableFuture} that is already completed exceptionally
     * with the provided {@code Throwable}.
     *
     * @param <U> the type parameter of the {@code PooledCompletableFuture}
     * @param ex  the exception used to complete the future exceptionally
     * @return a {@code PooledCompletableFuture} completed exceptionally with the given exception
     */
    public static <U> PooledCompletableFuture<U> failedFuture(final Throwable ex) {
        return PooledCompletableFuture.of(CompletableFuture.failedFuture(ex));
    }

    /**
     * Creates a {@code PooledCompletableFuture} that is already completed exceptionally
     * with the provided {@code Throwable}, using the specified {@code Executor} for downstream tasks.
     *
     * @param <U>      the type parameter of the {@code PooledCompletableFuture}
     * @param ex       the exception used to complete the future exceptionally
     * @param executor the executor to use for downstream operations
     * @return a {@code PooledCompletableFuture} completed exceptionally with the given exception
     */
    public static <U> PooledCompletableFuture<U> failedFuture(final Throwable ex, final Executor executor) {
        return PooledCompletableFuture.of(CompletableFuture.failedFuture(ex), executor);
    }

    /**
     * Creates a PooledCompletableFuture by supplying a value asynchronously using the provided supplier.
     *
     * @param <T>      the type of the result supplied
     * @param supplier the supplier function used to produce the result
     * @return a PooledCompletableFuture containing the result produced by the supplier
     * @implNote Executors on the pooled executors {@link PooledExecutors#getAnyExecutor()}
     */
    public static <T> PooledCompletableFuture<T> supply(final Supplier<T> supplier) {
        return PooledCompletableFuture.of(CompletableFuture.supplyAsync(supplier, PooledExecutors.getAnyExecutor()));
    }

    /**
     * Creates and returns a pooled {@link PooledCompletableFuture} by supplying a task asynchronously
     * using the provided {@link Supplier} and executing it with the specified {@link Executor}.
     *
     * @param <T>      the type of the result produced by the supplier
     * @param supplier the supplier function that provides the result asynchronously
     * @param executor the executor that will be used for task execution
     * @return a {@link PooledCompletableFuture} representing the asynchronous computation
     */
    public static <T> PooledCompletableFuture<T> supply(final Supplier<T> supplier, final Executor executor) {
        return PooledCompletableFuture.of(CompletableFuture.supplyAsync(supplier, executor), executor);
    }

    /**
     * Executes a given {@link Runnable} asynchronously using a specified executor and returns
     * a {@link PooledCompletableFuture} representing the asynchronous computation.
     *
     * @param runnable the {@link Runnable} task to be executed asynchronously
     * @return a {@link PooledCompletableFuture} representing the asynchronous computation
     * @implNote Executors on the pooled executors {@link PooledExecutors#getAnyExecutor()}
     */
    public static PooledCompletableFuture<Void> run(final Runnable runnable) {
        return PooledCompletableFuture.of(CompletableFuture.runAsync(runnable, PooledExecutors.getAnyExecutor()));
    }

    /**
     * Executes a given {@link Runnable} task asynchronously using a specified {@link Executor}.
     *
     * @param runnable the task to execute asynchronously
     * @param executor the executor used to execute the task
     * @return a {@link PooledCompletableFuture} representing the asynchronous computation
     */
    public static PooledCompletableFuture<Void> run(final Runnable runnable, final Executor executor) {
        return PooledCompletableFuture.of(CompletableFuture.runAsync(runnable, executor), executor);
    }

    /**
     * Sets the pipeline to use the pooled executors for downwind execution.
     *
     * @return a copied {@link PooledCompletableFuture} using the pooled executors.
     */
    public PooledCompletableFuture<T> async() {
        return PooledCompletableFuture.of(this.future);
    }

    /**
     * Sets the pipeline to use the provided executor for downwind execution.
     *
     * @param executor the executor to be used for the computation
     * @return a copied PooledCompletableFuture with the specified executor
     */
    public PooledCompletableFuture<T> async(final Executor executor) {
        return PooledCompletableFuture.of(this.future, executor);
    }

    public PooledCompletableFuture<T> sync() {
        var mainThread = PooledExecutors.getMainThread();
        if (mainThread == null) {
            throw new NullPointerException("Cannot use main thread as main thread does not exist. Use PooledExecutors.setMainThread(Executor) to set a main thread.");
        }
        return PooledCompletableFuture.of(
            this.future,
            mainThread
        );
    }

    public <U> PooledCompletableFuture<U> thenApply(final Function<? super T, ? extends U> fn) {
        return PooledCompletableFuture.of(
            this.future.thenApplyAsync(
                fn,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public <U> PooledCompletableFuture<U> thenApply(
        final Function<? super T, ? extends U> fn,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.thenApplyAsync(fn, executor), executor);
    }

    public PooledCompletableFuture<Void> thenAccept(final Consumer<? super T> action) {
        return PooledCompletableFuture.of(
            this.future.thenAcceptAsync(
                action,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public PooledCompletableFuture<Void> thenAccept(final Consumer<? super T> action, final Executor executor) {
        return PooledCompletableFuture.of(this.future.thenAcceptAsync(action, executor), executor);
    }

    public PooledCompletableFuture<Void> thenRun(final Runnable action) {
        return PooledCompletableFuture.of(
            this.future.thenRunAsync(
                action,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public PooledCompletableFuture<Void> thenRun(final Runnable action, final Executor executor) {
        return PooledCompletableFuture.of(this.future.thenRunAsync(action, executor), executor);
    }

    public <U, V> PooledCompletableFuture<V> thenCombine(
        final CompletionStage<? extends U> other,
        final BiFunction<? super T, ? super U, ? extends V> fn
    ) {
        return PooledCompletableFuture.of(
            this.future.thenCombineAsync(
                other,
                fn,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public <U, V> PooledCompletableFuture<V> thenCombine(
        final CompletionStage<? extends U> other,
        final BiFunction<? super T, ? super U, ? extends V> fn,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.thenCombineAsync(other, fn, executor), executor);
    }

    public <U, V> PooledCompletableFuture<V> thenCombine(
        final PooledCompletableFuture<? extends U> other,
        final BiFunction<? super T, ? super U, ? extends V> fn
    ) {
        return PooledCompletableFuture.of(
            this.future.thenCombineAsync(
                other.future,
                fn,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public <U, V> PooledCompletableFuture<V> thenCombine(
        final PooledCompletableFuture<? extends U> other,
        final BiFunction<? super T, ? super U, ? extends V> fn,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.thenCombineAsync(other.future, fn, executor), executor);
    }

    public <U> PooledCompletableFuture<Void> thenAcceptBoth(
        final CompletionStage<? extends U> other,
        final BiConsumer<? super T, ? super U> action
    ) {
        return PooledCompletableFuture.of(
            this.future.thenAcceptBothAsync(
                other,
                action,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public <U> PooledCompletableFuture<Void> thenAcceptBoth(
        final CompletionStage<? extends U> other,
        final BiConsumer<? super T, ? super U> action,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.thenAcceptBothAsync(other, action, executor), executor);
    }

    public <U> PooledCompletableFuture<Void> thenAcceptBoth(
        final PooledCompletableFuture<? extends U> other,
        final BiConsumer<? super T, ? super U> action
    ) {
        return PooledCompletableFuture.of(
            this.future.thenAcceptBothAsync(
                other.future,
                action,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public <U> PooledCompletableFuture<Void> thenAcceptBoth(
        final PooledCompletableFuture<? extends U> other,
        final BiConsumer<? super T, ? super U> action,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.thenAcceptBothAsync(other.future, action, executor), executor);
    }

    public PooledCompletableFuture<Void> runAfterBoth(final CompletionStage<?> other, final Runnable action) {
        return PooledCompletableFuture.of(
            this.future.runAfterBothAsync(
                other,
                action,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public PooledCompletableFuture<Void> runAfterBoth(
        final CompletionStage<?> other,
        final Runnable action,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.runAfterBothAsync(other, action, executor), executor);
    }

    public PooledCompletableFuture<Void> runAfterBoth(
        final PooledCompletableFuture<?> other,
        final Runnable action
    ) {
        return PooledCompletableFuture.of(
            this.future.runAfterBothAsync(
                other.future,
                action,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public PooledCompletableFuture<Void> runAfterBoth(
        final PooledCompletableFuture<?> other,
        final Runnable action,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.runAfterBothAsync(other.future, action, executor), executor);
    }

    public <U> PooledCompletableFuture<U> applyToEither(
        final CompletionStage<? extends T> other,
        final Function<? super T, U> fn
    ) {
        return PooledCompletableFuture.of(
            this.future.applyToEitherAsync(
                other,
                fn,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public <U> PooledCompletableFuture<U> applyToEither(
        final CompletionStage<? extends T> other,
        final Function<? super T, U> fn,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.applyToEitherAsync(other, fn, executor), executor);
    }

    public <U> PooledCompletableFuture<U> applyToEither(
        final PooledCompletableFuture<? extends T> other,
        final Function<? super T, U> fn
    ) {
        return PooledCompletableFuture.of(
            this.future.applyToEitherAsync(
                other.future,
                fn,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public <U> PooledCompletableFuture<U> applyToEither(
        final PooledCompletableFuture<? extends T> other,
        final Function<? super T, U> fn,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.applyToEitherAsync(other.future, fn, executor), executor);
    }

    public PooledCompletableFuture<Void> acceptEither(
        final CompletionStage<? extends T> other,
        final Consumer<? super T> action
    ) {
        return PooledCompletableFuture.of(
            this.future.acceptEitherAsync(
                other,
                action,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public PooledCompletableFuture<Void> acceptEither(
        final CompletionStage<? extends T> other,
        final Consumer<? super T> action,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.acceptEitherAsync(other, action, executor), executor);
    }

    public PooledCompletableFuture<Void> acceptEither(
        final PooledCompletableFuture<? extends T> other,
        final Consumer<? super T> action
    ) {
        return PooledCompletableFuture.of(
            this.future.acceptEitherAsync(
                other.future,
                action,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public PooledCompletableFuture<Void> acceptEither(
        final PooledCompletableFuture<? extends T> other,
        final Consumer<? super T> action,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.acceptEitherAsync(other.future, action, executor), executor);
    }

    public PooledCompletableFuture<Void> runAfterEither(final CompletionStage<?> other, final Runnable action) {
        return PooledCompletableFuture.of(
            this.future.runAfterEitherAsync(
                other,
                action,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public PooledCompletableFuture<Void> runAfterEither(
        final CompletionStage<?> other,
        final Runnable action,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.runAfterEitherAsync(other, action, executor), executor);
    }

    public PooledCompletableFuture<Void> runAfterEither(
        final PooledCompletableFuture<?> other,
        final Runnable action
    ) {
        return PooledCompletableFuture.of(
            this.future.runAfterEitherAsync(
                other.future,
                action,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public PooledCompletableFuture<Void> runAfterEither(
        final PooledCompletableFuture<?> other,
        final Runnable action,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.runAfterEitherAsync(other.future, action, executor), executor);
    }

    public <U> PooledCompletableFuture<U> thenCompose(final Function<? super T, ? extends PooledCompletableFuture<U>> fn) {
        return PooledCompletableFuture.of(
            this.future.thenComposeAsync(
                f -> fn.apply(f).getFuture(),
                getCurrentExecutor()
            ), this.executor
        );
    }

    public <U> PooledCompletableFuture<U> thenCompose(
        final Function<? super T, ? extends PooledCompletableFuture<U>> fn,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(
            this.future.thenComposeAsync(
                f -> fn.apply(f)
                    .getFuture(), executor
            ), executor
        );
    }

    public <U> PooledCompletableFuture<U> handle(final BiFunction<? super T, Throwable, ? extends U> fn) {
        return PooledCompletableFuture.of(
            this.future.handleAsync(
                fn,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public <U> PooledCompletableFuture<U> handle(
        final BiFunction<? super T, Throwable, ? extends U> fn,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.handleAsync(fn, executor), executor);
    }

    public PooledCompletableFuture<T> whenComplete(final BiConsumer<? super T, ? super Throwable> action) {
        return PooledCompletableFuture.of(
            this.future.whenCompleteAsync(
                action,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public PooledCompletableFuture<T> whenComplete(
        final BiConsumer<? super T, ? super Throwable> action,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.whenCompleteAsync(action, executor), executor);
    }

    public PooledCompletableFuture<T> exceptionally(final Function<Throwable, ? extends T> fn) {
        return PooledCompletableFuture.of(
            this.future.exceptionallyAsync(
                fn,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public PooledCompletableFuture<T> exceptionally(
        final Function<Throwable, ? extends T> fn,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(this.future.exceptionallyAsync(fn, executor), executor);
    }

    public PooledCompletableFuture<T> exceptionallyCompose(final Function<Throwable, ?
        extends PooledCompletableFuture<T>> fn) {
        return PooledCompletableFuture.of(
            this.future.exceptionallyComposeAsync(
                f -> fn.apply(f)
                    .getFuture(),
                getCurrentExecutor()
            ), this.executor
        );
    }

    public PooledCompletableFuture<T> exceptionallyCompose(
        final Function<Throwable, ? extends PooledCompletableFuture<T>> fn,
        final Executor executor
    ) {
        return PooledCompletableFuture.of(
            this.future.exceptionallyComposeAsync(
                f -> fn.apply(f)
                    .getFuture(), executor
            ), executor
        );
    }

    public PooledCompletableFuture<T> complete(final Supplier<? extends T> supplier) {
        return PooledCompletableFuture.of(
            this.future.completeAsync(
                supplier,
                getCurrentExecutor()
            ), this.executor
        );
    }

    public PooledCompletableFuture<T> complete(final Supplier<? extends T> supplier, final Executor executor) {
        return PooledCompletableFuture.of(this.future.completeAsync(supplier, executor), executor);
    }

    public PooledCompletableFuture<T> orTimeout(final long timeout, final TimeUnit unit) {
        return PooledCompletableFuture.of(this.future.orTimeout(timeout, unit));
    }

    public PooledCompletableFuture<T> completeOnTimeout(final T value, final long timeout, final TimeUnit unit) {
        return PooledCompletableFuture.of(this.future.completeOnTimeout(value, timeout, unit));
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

    public boolean isCompletedExceptionally() {
        return this.future.isCompletedExceptionally();
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

    public T getNow(final T valueIfAbsent) {
        return this.future.getNow(valueIfAbsent);
    }

    public T join() {
        return this.future.join();
    }

    public boolean completeExceptionally(final Throwable ex) {
        return this.future.completeExceptionally(ex);
    }

    public void obtrudeValue(final T value) {
        this.future.obtrudeValue(value);
    }

    public void obtrudeException(final Throwable ex) {
        this.future.obtrudeException(ex);
    }

    public int getNumberOfDependents() {
        return this.future.getNumberOfDependents();
    }

    @Override
    public String toString() {
        return this.future.toString();
    }

    public <U> PooledCompletableFuture<U> newIncompleteFuture() {
        return PooledCompletableFuture.of(this.future.newIncompleteFuture(), this.executor);
    }

    public PooledCompletableFuture<T> copy() {
        return PooledCompletableFuture.of(this.future.copy(), this.executor);
    }

    private Executor getCurrentExecutor() {
        return this.hasOwnExecutor()
            ? this.executor
            : PooledExecutors.getAnyExecutor();
    }
}

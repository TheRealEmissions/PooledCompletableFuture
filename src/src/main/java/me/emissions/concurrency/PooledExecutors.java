package me.emissions.concurrency;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public final class PooledExecutors {
    private PooledExecutors() {}

    private static final int NUMBER_POOLED_EXECUTORS = 50;

    private static final List<ScheduledExecutorService> executors = IntStream.range(0, NUMBER_POOLED_EXECUTORS + 1)
        .mapToObj(i -> Executors.newSingleThreadScheduledExecutor(Executors.defaultThreadFactory()))
        .toList();

    public static ScheduledExecutorService getAnyExecutor() {
        return PooledExecutors.executors.get(ThreadLocalRandom.current().nextInt(0, NUMBER_POOLED_EXECUTORS));
    }

    public static List<ScheduledExecutorService> getExecutors() {
        return PooledExecutors.executors;
    }
}

package com.hungvers.services.catalog.common.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrencyTest {
    private ConcurrencyTest() {
        /* This utility class should not be instantiated */
    }

    public static List<Throwable> fireConcurrentTasks(int threads, Runnable task) throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threads; i++) {
                pool.submit(() -> {
                    try {
                        ready.countDown();
                        start.await();
                        task.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (RuntimeException e) {
                        errors.add(e);
                    } finally {
                        done.countDown();
                    }
                });
            }

            ready.await();
            start.countDown();
            if (!done.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for tasks to finish");
            }
        }

        return errors;
    }

    public static <T> List<Output<T>> fireConcurrentDistinctTasks(Collection<Input<T>> tasks) throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(tasks.size());
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(tasks.size());

        List<Output<T>> outputs = Collections.synchronizedList(new ArrayList<>());
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var task: tasks) {
                pool.submit(() -> {
                    try {
                        ready.countDown();
                        start.await();
                        outputs.add(Output.success(task.op(), task.task().get()));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (RuntimeException e) {
                        outputs.add(Output.failure(task.op(), e));
                    } finally {
                        done.countDown();
                    }
                });
            }

            ready.await();
            start.countDown();
            if (!done.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for tasks to finish");
            }
        }

        return outputs;
    }
}

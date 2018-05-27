package ru.mail.colloquium.toolkit.concurrent;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.mail.colloquium.diagnostics.DebugUtils.safeThrow;

public class ExclusiveExecutor {
    private final int minDelay;
    private final ScheduledExecutorService executor;
    private final Runnable task;
    private final AtomicInteger sync = new AtomicInteger(0);

    private final Runnable internalTask = new Runnable() {
        @Override
        public void run() {
            try {
                ExclusiveExecutor.this.task.run();
            } catch (Exception e) {
                safeThrow(e);
            }
            executor.schedule(restartTask, minDelay, TimeUnit.MILLISECONDS);
        }
    };

    private final Runnable restartTask = () -> {
        if (sync.getAndSet(0) > 1)
            internalTask.run();
    };

    public ExclusiveExecutor(int delay, ScheduledExecutorService executor, Runnable task) {
        this.minDelay = delay;
        this.executor = executor;
        this.task = task;
    }

    public void execute(boolean forced) {
        if (sync.getAndIncrement() == 0 || forced) {
            executor.execute(internalTask);
        }
    }

    public boolean busy() {
        return sync.get() > 0;
    }
}

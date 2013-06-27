package de.etecture.opensource.genericimport.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ScheduleExpression;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;

/**
 * schedules a repeated execution of a work.
 *
 * @author rhk
 * @version ${project.version}
 * @since 1.1.1
 */
public class WorkScheduler {

    private static final Logger LOG =
            Logger.getLogger(WorkScheduler.class.getName());
    private final BootstrapContext bootCtx;
    private final ExecutionContext execCtx;
    private final Map<Work, Timer> scheduledWorks = new HashMap<>();

    /**
     * constructs a new WorkScheduler with the given {@link BootstrapContext}
     *
     * @param bootCtx the context to build the scheduler for.
     */
    public WorkScheduler(BootstrapContext bootCtx) throws UnavailableException {
        this(bootCtx, new ExecutionContext());
    }

    /**
     * constructs a new WorkScheduler with the given {@link BootstrapContext}
     * and {@link ExecutionContext}
     *
     * @param bootCtx the context to build the scheduler for.
     * @param execCtx the execution context within the work should be executed.
     */
    public WorkScheduler(BootstrapContext bootCtx, ExecutionContext execCtx)
            throws UnavailableException {
        this.bootCtx = bootCtx;
        this.execCtx = execCtx;
        // this is just to check, if timers can be created.
        bootCtx.createTimer().cancel();
    }

    /**
     * schedules the given work at a specific schedule expression.
     *
     * @param work the work to do
     * @param scheduleExpression tells when to start the work.
     */
    public void scheduleWork(Work work, ScheduleExpression scheduleExpression)
            throws WorkException, UnavailableException {
        throw new UnsupportedOperationException(
                "scheduleExpression is not implemented yet");
    }

    /**
     * schedules the given work at a specific schedule expression.
     *
     * @param work the work to do
     * @param scheduleExpression tells when to start the work.
     * @param listener the callback listener for the work execution
     */
    public void scheduleWork(Work work, ScheduleExpression scheduleExpression,
            WorkListener listener) {
        throw new UnsupportedOperationException(
                "scheduleExpression is not implemented yet");
    }

    /**
     * schedules the given work at the specific time.
     *
     * @param work the work to do
     * @param initialTimeoutInMs the initialTimeout in ms.
     * @param periodInMs the period to repeat the work in ms.
     */
    public void scheduleWork(final Work work, long initialTimeoutInMs,
            long periodInMs) {
        scheduleWorkStarter(new WorkStarter(work), initialTimeoutInMs,
                periodInMs);
    }

    /**
     * schedules the given work at a specific schedule expression.
     *
     * @param work the work to do
     * @param initialTimeoutInMs the initialTimeout in ms.
     * @param periodInMs the period to repeat the work in ms.
     * @param listener the callback listener for the work execution
     */
    public void scheduleWork(final Work work, long initialTimeoutInMs,
            long periodInMs,
            final WorkListener listener) {
        scheduleWorkStarter(new WorkStarter(work, listener), initialTimeoutInMs,
                periodInMs);
    }

    private void scheduleWorkStarter(WorkStarter starter,
            long initialTimeoutInMs, long period) {
        try {
            Timer timer = bootCtx.createTimer();
            timer.schedule(starter, initialTimeoutInMs, period);
            scheduledWorks.put(starter.work, timer);
        } catch (UnavailableException ex) {
            throw new IllegalStateException("timer is not available", ex);
        }
    }

    /**
     * cancels all scheduled works.
     */
    public void cancel() {
        for (Entry<Work, Timer> workEntry : scheduledWorks.entrySet()) {
            workEntry.getValue().cancel();
            workEntry.getKey().release();
        }
    }

    /**
     * cancels a specific work
     *
     * @param work a work that was scheduled.
     */
    public void cancel(Work work) {
        if (scheduledWorks.containsKey(work)) {
            scheduledWorks.get(work).cancel();
            scheduledWorks.remove(work);
            work.release();
        }
    }

    private class WorkStarter extends TimerTask {

        private final Work work;
        private final WorkListener listener;

        WorkStarter(Work work) {
            this(work, null);
        }

        WorkStarter(Work work, WorkListener listener) {
            this.work = work;
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                if (listener != null) {
                    bootCtx.getWorkManager().scheduleWork(work,
                            WorkManager.IMMEDIATE,
                            execCtx, listener);
                } else {
                    bootCtx.getWorkManager().scheduleWork(work);
                }
            } catch (WorkException ex) {
                LOG.log(Level.SEVERE, "cannot schedule work", ex);
            }
        }
    }
}

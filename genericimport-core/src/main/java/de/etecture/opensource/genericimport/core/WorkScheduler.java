/*
 * This file is part of the ETECTURE Open Source Community Projects.
 *
 * Copyright (c) 2013 by:
 *
 * ETECTURE GmbH
 * Darmstädter Landstraße 112
 * 60598 Frankfurt
 * Germany
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.etecture.opensource.genericimport.core;

import de.etecture.opensource.genericimport.cron.ScheduleExpression;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
import org.joda.time.DateTime;

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
        scheduleWorkStarter(new WorkStarter(work, null, scheduleExpression));
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
        scheduleWorkStarter(new WorkStarter(work, listener, scheduleExpression));
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

    private void scheduleWorkStarter(WorkStarter starter) {
        try {
            Timer timer = bootCtx.createTimer();
            long now = System.currentTimeMillis();
            long nextValidTime =
                    starter.se.getNextValidTime(now);
            timer.schedule(starter, nextValidTime - now);
            LOG.log(Level.INFO,
                    "scheduling the work \"{0}\" with schedule: {1} which is next at: {2}",
                    new Object[]{starter.work,
                starter.se,
                new DateTime(nextValidTime).toString("HH:mm:ss")});
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
        private final ScheduleExpression se;
        private boolean canceled = false;

        WorkStarter(Work work) {
            this(work, null);
        }

        WorkStarter(Work work, WorkListener listener) {
            this(work, listener, null);
        }

        WorkStarter(Work work, WorkListener listener, ScheduleExpression se) {
            this.work = work;
            this.listener = listener;
            this.se = se;
        }

        @Override
        public void run() {
            LOG.log(Level.INFO,
                    "its {0}, so start the work: \"{1}\"",
                    new Object[]{new DateTime().toString("HH:mm:ss"),
                work.toString()});
            try {
                if (listener != null) {
                    bootCtx.getWorkManager().doWork(work,
                            WorkManager.IMMEDIATE,
                            execCtx, listener);
                } else {
                    bootCtx.getWorkManager().doWork(work);
                }
            } catch (WorkException ex) {
                LOG.log(Level.SEVERE, "cannot schedule work", ex);
            }
            if (se != null && !canceled) {
                // reschedule
                cancel();
                scheduleWorkStarter(new WorkStarter(work, listener, se));
            }
        }

        @Override
        public boolean cancel() {
            canceled = true;
            return super.cancel();
        }
    }
}

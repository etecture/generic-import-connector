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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

/**
 * this is a basic implementation for an import connector.
 *
 * @author rhk
 * @version ${project.version}
 * @since 1.1.1
 */
@Connector(vendorName = "ETECTURE GmbH",
        version = "${project.version}",
        displayName = "Generic Import Connector",
        description =
        "reads files at a specific location and imports these files.",
        eisType = "FileSystem")
public class GenericImportConnector implements FileAgentCallback,
        ResourceAdapter {

    private static final Logger LOG = Logger.getLogger(
            GenericImportConnector.class.getName());
    private BootstrapContext ctx;
    private WorkScheduler scheduler;
    private final Map<ActivationSpec, FileAgentWork> works = new HashMap<>();

    @Override
    public final void start(BootstrapContext ctx) throws
            ResourceAdapterInternalException {
        LOG.info("starts the ImportConnector...");
        this.ctx = ctx;
        try {
            this.scheduler = new WorkScheduler(ctx);
        } catch (UnavailableException ex) {
            throw new ResourceAdapterInternalException(
                    "This resource-adapter needs an available timer to work!",
                    ex);
        }
    }

    protected final WorkManager getWorkManager() {
        return ctx.getWorkManager();
    }

    protected final Timer createTimer() throws UnavailableException {
        return ctx.createTimer();
    }

    @Override
    public final void endpointActivation(MessageEndpointFactory endpointFactory,
            ActivationSpec spec) throws ResourceException {
        LOG.log(Level.INFO, "register an endpoint with spec: {0}", spec);
        // an endpoint is deployed.
        if (spec instanceof GenericImportSpec) {
            GenericImportSpec importSpec = (GenericImportSpec) spec;
            // create a FileAgentWork
            FileAgentWork work = new FileAgentWork(
                    importSpec, endpointFactory, this);
            // remember the work
            this.works.put(spec, work);
            // schedule a FileAgentWork with this importerSpec.
            this.scheduler.scheduleWork(work,
                    importSpec.getStartDelay(),
                    importSpec.getPeriod());
        } else {
            throw new NotSupportedException(String.format(
                    "Endpoint for %s must be specified with an instance of %s instead of: %s",
                    getClass().getSimpleName(), GenericImportSpec.class
                    .getSimpleName(), spec.getClass().getName()));
        }
    }

    @Override
    public final void endpointDeactivation(
            MessageEndpointFactory endpointFactory,
            ActivationSpec spec) {
        LOG.log(Level.INFO, "deregister an endpoint with spec: {0}", spec);
        // an endpoint is undeployed
        if (this.works.containsKey(spec)) {
            FileAgentWork work = this.works.get(spec);
            this.scheduler.cancel(work);
            this.works.remove(spec);
        }
    }

    @Override
    public final void stop() {
        LOG.info("stop the ImportConnector...");
        this.scheduler.cancel();
        this.works.clear();
    }

    @Override
    public final XAResource[] getXAResources(ActivationSpec[] specs) throws
            ResourceException {
        return new XAResource[0];
    }

    @Override
    public final void onNewImportFile(GenericImportSpec spec,
            MessageEndpointFactory endpointFactory, File file) {
        try {
            LOG.log(Level.INFO, "process new importfile: {0} of type {1}",
                    new Object[]{file.getName(),
                spec.getMimeType()});
            this.getWorkManager().startWork(
                    new FileImportWork(spec.getMimeType(), file,
                    endpointFactory));
        } catch (WorkException ex) {
            LOG.log(Level.SEVERE, "cannot schedule an FileImportWork: ", ex);
        }
    }
}

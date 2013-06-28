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

import de.etecture.opensource.genericimport.api.ImportStatusListener;
import de.etecture.opensource.genericimport.spi.ImportFileProcessor;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;

/**
 * this is a work to do an import of a specific file.
 *
 * @author rhk
 * @version ${project.version}
 * @since 1.1.1
 */
public class FileImportWork implements Work, InvocationHandler {

    private static final Logger LOG = Logger.getLogger(FileImportWork.class
            .getName());
    private final String mimeType;
    private final File file;
    private final MessageEndpointFactory endpointFactory;

    FileImportWork(String mimeType, File file,
            MessageEndpointFactory endpointFactory) {
        this.mimeType = mimeType;
        this.file = file;
        this.endpointFactory = endpointFactory;
    }

    @Override
    public void release() {
    }

    @Override
    public void run() {
        LOG.log(Level.INFO,
                "searching a processor that supports mimetype: {0}",
                mimeType);
        ImportStatusListener proxy = (ImportStatusListener) Proxy
                .newProxyInstance(getClass()
                .getClassLoader(),
                new Class[]{ImportStatusListener.class}, this);
        // search for the desired processor.
        try {
            for (ImportFileProcessor processor : ServiceLoader.load(
                    ImportFileProcessor.class)) {
                LOG.log(Level.INFO,
                        "checking processor: {0} to support mimetype: {1}",
                        new Object[]{processor.getClass().getSimpleName(),
                    mimeType});
                if (processor.isResponsibleFor(mimeType)) {
                    // delegate the work to the processor.
                    LOG.log(Level.INFO,
                            "invoking processor: {0}",
                            processor.getClass().getSimpleName());
                    processor.processFile(mimeType, file, proxy);
                    return;
                }
            }
            LOG.log(Level.WARNING,
                    "No Processor registered to process the file: {1} with mimetype: {0}",
                    new Object[]{mimeType,
                file.getName()});
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, String.format(
                    "cannot search for Processors: %s",
                    t.getMessage()), t);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws
            Throwable {
        Object result = null;
        LOG.log(Level.INFO, "inform the ImportStatusListener");
        try {
            // create an Endpoint that is a ImportStatusListener too...
            MessageEndpoint endpoint = (MessageEndpoint) endpointFactory
                    .createEndpoint(null);
            try {
                // starts the transaction to send a message
                LOG.log(Level.INFO,
                        "open an endpoint to an ImportStatusListener");
                endpoint.beforeDelivery(method);
                // sends the message now...
                result = method.invoke(endpoint, args);
            } catch (NoSuchMethodException e) {
                LOG.log(Level.SEVERE,
                        "ResourceAdapter cannot find a method:", e);
            } catch (ResourceException e) {
                LOG.log(Level.SEVERE,
                        "ResourceAdapter cannot inform an ImportStatusListener:",
                        e);
            } finally {
                // transaction is completed...
                try {
                    // inform the endpoint that we're finished here
                    LOG.log(Level.INFO,
                            "close the endpoint to an ImportStatusListener");
                    endpoint.afterDelivery();
                } catch (ResourceException e) {
                    LOG.log(Level.SEVERE,
                            "ResourceAdapter cannot finish the transaction:",
                            e);
                }
            }
            endpoint.release();
        } catch (UnavailableException e) {
            LOG.log(Level.SEVERE,
                    "ResourceAdapter tried to send to a deactivated endpoint.",
                    e);
        }
        return result;
    }
}

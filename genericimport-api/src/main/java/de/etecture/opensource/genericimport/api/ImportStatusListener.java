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
package de.etecture.opensource.genericimport.api;

/**
 * Defines a Listener interface that observes the importer.
 *
 * @author rherschke
 * @version ${project.version}
 * @since 1.1.1
 */
public interface ImportStatusListener<T> {

    /**
     * called, when an import is started.
     *
     * @since 1.1.1
     */
    void onStart(String importId);

    /**
     * called, when an import is finished.
     *
     * @since 1.1.1
     */
    void onFinished(String importId);

    /**
     * called, when the importer has read a piece that can be imported.
     *
     * @param payload
     * @since 1.1.1
     */
    void onProgress(String importId, T payload);

    /**
     * called, when the importer likes to inform about an error.
     *
     * @param message the error message
     * @param args the arguments to build the message
     *
     * @return wether or not, the importer should go on (true) or stop (false)
     * @since 1.1.1
     */
    boolean onError(String importId, String message, Object... args);

    /**
     * called, when the importer likes to inform about a warning.
     *
     * @param message
     * @param args
     * @since 1.1.1
     */
    void onWarning(String importId, String message, Object... args);
}

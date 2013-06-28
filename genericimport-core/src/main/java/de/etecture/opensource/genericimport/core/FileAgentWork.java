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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;

/**
 * This class is a Work implementation that is responsible to watch the
 * specified path if there is a new File present, that's name matches a specific
 * regular expression.
 * <p>
 * If new import files were found, the given {@link FileAgentCallback} is
 * called.
 *
 * @author rhk
 * @since 1.1.1
 * @version ${project.version}
 */
public class FileAgentWork implements Work {

    private static final Logger LOG = Logger.getLogger(FileAgentWork.class
            .getName());
    private final FileAgeFilter fileFilter;
    private final GenericImportSpec spec;
    private final FileAgentCallback callback;
    private final MessageEndpointFactory endpointFactory;

    FileAgentWork(GenericImportSpec spec,
            MessageEndpointFactory endpointFactory,
            FileAgentCallback callback) {
        this.spec = spec;
        this.endpointFactory = endpointFactory;
        this.fileFilter = new FileAgeFilter(spec.getImportFilePattern());
        this.callback = callback;
    }

    FileAgeFilter getFileFilter() {
        return fileFilter;
    }

    /**
     * returns the ImporterSpec with wich this agent was created.
     *
     * @return
     */
    public GenericImportSpec getImporterSpec() {
        return spec;
    }

    /**
     * returns the endpointFactory for which this agent was created.
     *
     * @return
     */
    public MessageEndpointFactory getEndpointFactory() {
        return endpointFactory;
    }

    @Override
    public void release() {
    }

    @Override
    public void run() {
        LOG.log(Level.INFO,
                "FileAgent now starting examining the directory {0} for new files ...",
                spec.getImportPath());
        try {
            // check, if there is a new import-file at the specified location
            File[] newFiles = getNewFiles();
            if (newFiles != null && newFiles.length > 0) {
                processNewFiles(newFiles);
            } else {
                LOG.info("FileAgent did not yet find any new files...");
            }
        } catch (WorkException ex) {
            LOG.log(Level.SEVERE,
                    "FileAgent found an error while checking for new files: ",
                    ex);
        }
        // set the timestamp...
        this.fileFilter.reset();
        LOG.info("FileAgent done examining...");
    }

    /**
     * request an array of files. The files are filtered so that only files
     * newer then the last imort are returned.
     *
     * @return an array of files.
     */
    private File[] getNewFiles() {
        File path = new File(spec.getImportPath());
        return path.listFiles(fileFilter);
    }

    /**
     * processes each of the new files.
     *
     * @param newFiles the array of files to be processed.
     * @throws WorkException if the workmanager cannot handle works.
     */
    private void processNewFiles(File[] newFiles) throws WorkException {
        LOG.log(Level.INFO, "FileAgent found {0} new files...",
                newFiles.length);
        sortFileArrayByAge(newFiles);
        // and process each file...
        for (File newFile : newFiles) {
            callback.onNewImportFile(spec, endpointFactory, newFile);
        }
    }

    /**
     * sorts the files in the given array by it's age.
     *
     * @param newFiles the array of files to be sorted.
     */
    private void sortFileArrayByAge(File[] newFiles) {
        Arrays.sort(newFiles, new FileAgeComparator());
    }
}

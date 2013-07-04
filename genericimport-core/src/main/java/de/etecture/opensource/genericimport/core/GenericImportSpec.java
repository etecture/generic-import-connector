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
import java.io.Serializable;
import java.util.Arrays;
import javax.resource.ResourceException;
import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

/**
 * implements the basic information for the importer spec.
 *
 * @author rhk
 * @version ${project.version}
 * @since 1.1.1
 */
@Activation(messageListeners = {ImportStatusListener.class})
public class GenericImportSpec implements ActivationSpec, Serializable {

    private static final long serialVersionUID = -1;
    private ResourceAdapter ra;
    @ConfigProperty(type = String.class,
            description = "the path to look for new import files.")
    private String importPath;
    @ConfigProperty(type = String.class, defaultValue = ".*",
            description = "the file pattern to search for.")
    private String importFilePattern;
    @ConfigProperty(type = String.class,
            description = "the schedule expression to be used for the agent.")
    private String scheduleExpression;
    @ConfigProperty(type = String.class,
            description = "the mime-type of the import file.")
    private String mimeType;

    @Override
    public ResourceAdapter getResourceAdapter() {
        return ra;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        this.ra = ra;
    }

    @Override
    public void validate() throws InvalidPropertyException {
    }

    public String getImportPath() {
        return importPath;
    }

    public void setImportPath(String importPath) {
        this.importPath = importPath;
    }

    public String getImportFilePattern() {
        return importFilePattern;
    }

    public void setImportFilePattern(String importFilePattern) {
        this.importFilePattern = importFilePattern;
    }

    public String getScheduleExpression() {
        return scheduleExpression;
    }

    public void setScheduleExpression(String scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String ImportType) {
        this.mimeType = ImportType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GenericImportSpec other = (GenericImportSpec) obj;
        return Arrays.equals(
                new Object[]{
            this.importFilePattern,
            this.importPath,
            this.scheduleExpression},
                new Object[]{
            other.importFilePattern,
            other.importPath,
            other.scheduleExpression});
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{importFilePattern,
            importPath,
            scheduleExpression});
    }

    @Override
    public String toString() {
        return String.format(
                "GenericImportSpec{%n"
                + "\timportPath: %s,%n"
                + "\timportFilePattern: %s,%n"
                + "\tscheduleExpression: %s,%n"
                + "\tmimeType: %s%n}%n",
                importPath, importFilePattern, scheduleExpression, mimeType);
    }
}

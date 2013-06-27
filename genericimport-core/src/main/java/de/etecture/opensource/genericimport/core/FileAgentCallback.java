package de.etecture.opensource.genericimport.core;

import java.io.File;
import javax.resource.spi.endpoint.MessageEndpointFactory;

/**
 * processes new import files.
 * <p>
 * this processor is a callback, used by the {@link FileAgentWork} to inform a
 * connector that a new file can be processed.
 *
 * @author rhk
 * @version ${project.version}
 * @since 1.1.1
 */
public interface FileAgentCallback {

    /**
     * called by the fileagent to process the file.
     *
     * @param file the file to be processed.
     * @param endpointFactory the endpoint factory for which the file should be
     * processed.
     * @param spec the Importer Specification for which this file should be
     * processed.
     */
    void onNewImportFile(GenericImportSpec spec,
            MessageEndpointFactory endpointFactory, File file);
}

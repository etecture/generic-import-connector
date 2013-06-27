package de.etecture.opensource.genericimport.spi;

import de.etecture.opensource.genericimport.api.ImportStatusListener;
import java.io.File;

/**
 * this interface defines a processor for a file to be implemented by a service
 * provider.
 *
 * @author rhk
 * @version ${project.version}
 * @since 1.1.1
 */
public interface ImportFileProcessor {

    /**
     * called by the resource adapter to request the responsibility for a given
     * mime-type.
     * <p>
     * N.B. a service provider must implement this method to return true, if and
     * only if the given modelElementType is the type this processor is
     * responsible for.
     *
     * @param mimeType the mimeType to check
     * @return wether or not this processor is responsible for this element.
     */
    boolean isResponsibleFor(String mimeType);

    /**
     * called by the resource adapter to process this file.
     *
     * @param mimeType the type of element
     * @param file the file to process.
     * @param callback the callback to be used to fire
     * {@link ImportStatusEvent}s
     */
    void processFile(String mimeType, File file, ImportStatusListener callback);
}

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
    void onStart();

    /**
     * called, when an import is finished.
     *
     * @since 1.1.1
     */
    void onFinished();

    /**
     * called, when the importer has read a piece that can be imported.
     *
     * @param payload
     * @since 1.1.1
     */
    void onProgress(T payload);

    /**
     * called, when the importer likes to inform about an error.
     *
     * @param message the error message
     * @param args the arguments to build the message
     *
     * @return wether or not, the importer should go on (true) or stop (false)
     * @since 1.1.1
     */
    boolean onError(String message, Object... args);

    /**
     * called, when the importer likes to inform about a warning.
     *
     * @param message
     * @param args
     * @since 1.1.1
     */
    void onWarning(String message, Object... args);
}

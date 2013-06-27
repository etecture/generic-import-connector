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

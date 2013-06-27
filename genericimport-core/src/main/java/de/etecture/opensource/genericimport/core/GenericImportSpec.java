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

    long getStartDelay() {
        return 5000;
    }

    long getPeriod() {
        return 5000;
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

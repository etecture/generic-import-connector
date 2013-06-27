package de.etecture.opensource.genericimport.core;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * filters file that matches the given pattern and was last modified after this
 * file filters remembered timestamp.
 *
 * @author rhk
 * @version ${project.version}
 * @since 1.0.1
 */
class FileAgeFilter implements FileFilter {

    private final Pattern fileNamePattern;
    private long lastImportedTimestamp = 0;

    FileAgeFilter(String fileNamePattern) {
        this.fileNamePattern = Pattern.compile(fileNamePattern);
    }

    Pattern getFileNamePattern() {
        return fileNamePattern;
    }

    @Override
    public boolean accept(File pathname) {
        // check if the timestamp of the file is newer
        return fileNamePattern.matcher(pathname.getName()).matches() && (pathname.lastModified() > lastImportedTimestamp);
    }

    /**
     * resets this FileFilter to the current time.
     */
    public void reset() {
        this.lastImportedTimestamp = System.currentTimeMillis();
    }
}

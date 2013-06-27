package de.etecture.opensource.genericimport.core;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

/**
 * compares the last modified timestamp of two files.
 *
 * @author rhk
 * @version ${project.version}
 * @since 1.0.1
 * @see Comparator
 */
class FileAgeComparator implements Comparator<File>, Serializable {

    private static final long serialVersionUID = 1l;

    @Override
    public int compare(File f1, File f2) {
        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
    }
}

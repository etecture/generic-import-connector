package de.etecture.opensource.genericimport.core;

import de.etecture.opensource.genericimport.core.GenericImportSpec;

/**
 *
 * @author rhk
 */
public class TestImporterSpec extends GenericImportSpec {

    public TestImporterSpec(String importPath, String filePattern) {
        super.setImportFilePattern(filePattern);
        super.setImportPath(importPath);
    }
}

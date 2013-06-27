/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.etecture.opensource.genericimport.core;

import de.etecture.opensource.genericimport.core.FileAgeComparator;
import de.herschke.testhelper.PrettyPrintingRule;
import java.io.File;
import static org.fest.assertions.Assertions.assertThat;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests the {@link FileAgeComparator}
 *
 * @author rhk
 */
public class FileAgeComparatorTest {

    @Rule
    public PrettyPrintingRule out = new PrettyPrintingRule();

    /**
     * Test of {@link FileAgeComparator#compare()}.
     */
    @Test
    public void testCompareFilesByAge() throws Exception {
        File f1 = File.createTempFile("fileAgeTest", "tmp");
        f1.deleteOnExit();
        long time = DateTime.now().minusMillis(100).getMillis();
        assertThat(f1.setLastModified(time)).isTrue();
        File f2 = File.createTempFile("fileAgeTest", "tmp");
        f2.deleteOnExit();
        time = DateTime.now().minusSeconds(100).getMillis();
        assertThat(f2.setLastModified(time - 100)).isTrue();
        FileAgeComparator instance = new FileAgeComparator();
        assertThat(instance.compare(f1, f1)).isEqualTo(0);
        assertThat(instance.compare(f2, f2)).isEqualTo(0);
        assertThat(instance.compare(f1, f2)).isGreaterThan(0);
        assertThat(instance.compare(f2, f1)).isLessThan(0);
    }
}
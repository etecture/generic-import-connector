package de.etecture.opensource.genericimport.core;

import de.etecture.opensource.genericimport.core.FileAgeFilter;
import de.herschke.testhelper.ConsoleWriter.Color;
import de.herschke.testhelper.PrettyPrintingRule;
import java.io.File;
import static org.fest.assertions.Assertions.assertThat;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;

/**
 * tests the {@link FileAgeFilter}
 *
 * @author rhk
 * @version ${project.version}
 * @since 1.0.1
 */
public class FileAgeFilterTest {

    @Rule
    public PrettyPrintingRule out = new PrettyPrintingRule();

    /**
     * Test of {@link FileAgeFilter#accept(java.io.File)}.
     */
    @Test
    public void testNormalRun() throws Exception {
        File f1 = File.createTempFile("fileAgeTest", ".tmp");
        f1.deleteOnExit();
        final long time = DateTime.now().minusSeconds(100).getMillis();
        assertThat(f1.setLastModified(time)).isTrue();

        File f2 = File.createTempFile("fileAge", ".tmp");
        f2.deleteOnExit();
        assertThat(f2.setLastModified(time)).isTrue();

        FileAgeFilter filter = new FileAgeFilter("fileAgeTest.+\\.tmp");
        assertThat(filter.accept(f1)).isTrue();
        assertThat(filter.accept(f2)).isFalse();

        final long testTime = DateTime.now().plusSeconds(1).getMillis();
        // now test the reset...
        filter.reset();
        out.printLeft("--> wait a second!");
        // wait for time is testTime
        while (System.currentTimeMillis() < testTime) {
            // do nothing, just wait!;
        }
        out.printRight(Color.BLUE, "done.");

        File f3 = File.createTempFile("fileAgeTest", ".tmp");
        f3.deleteOnExit();
        assertThat(f3.setLastModified(testTime)).isTrue();
        assertThat(f2.setLastModified(testTime)).isTrue();

        // check again
        assertThat(filter.accept(f1)).isFalse();
        assertThat(filter.accept(f2)).isFalse();
        assertThat(filter.accept(f3)).isTrue();
    }
}
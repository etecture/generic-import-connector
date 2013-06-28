/*
 * This file is part of the ETECTURE Open Source Community Projects.
 *
 * Copyright (c) 2013 by:
 *
 * ETECTURE GmbH
 * Darmstädter Landstraße 112
 * 60598 Frankfurt
 * Germany
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.etecture.opensource.genericimport.core;

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
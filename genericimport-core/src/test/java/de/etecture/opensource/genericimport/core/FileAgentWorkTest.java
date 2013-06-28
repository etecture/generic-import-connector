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

import java.io.File;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import static org.fest.assertions.Assertions.assertThat;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * tests the {@link FileAgentWork}
 *
 * @author rhk
 * @version ${project.version}
 * @since 1.0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class FileAgentWorkTest {

    @Mock
    FileAgentCallback processor;

    private void switchOffLogger(Class<?>... classes) {
        Logger.getGlobal().setLevel(Level.OFF);
        Logger.getGlobal().setUseParentHandlers(false);
        Logger.getAnonymousLogger().setLevel(Level.OFF);
        Logger.getAnonymousLogger().setUseParentHandlers(false);
        for (Class<?> clazz : classes) {
            Logger.getLogger(clazz.getName()).setLevel(Level.OFF);
            Logger.getLogger(clazz.getName()).setUseParentHandlers(false);
        }
    }

    @Before
    public void init() throws Exception {
        reset(processor);
        switchOffLogger(FileAgentWork.class);
    }

    /**
     * Normal Test of {@link FileAgentWork#run()}.
     */
    @Test
    public void testNormalRun() throws Exception {

        File[] testFiles = new File[10];
        for (int i = 0; i < testFiles.length; i++) {
            testFiles[i] = File.createTempFile("fileAgent_", ".tmp");
            testFiles[i].deleteOnExit();
            assertThat(testFiles[i].setLastModified(0)).isTrue();
        }



        FileAgentWork fileAgent = new FileAgentWork(
                new TestImporterSpec(
                testFiles[0].getParent(),
                "fileAgent_.*\\.tmp"),
                null,
                processor);

        final long testTime = DateTime.now().plusSeconds(1).getMillis();
        System.out.println("wait a second!");
        // wait for time is testTime
        while (System.currentTimeMillis() < testTime) {
            // do nothing, just wait!;
        }
        System.out.println("done.");
        for (int i = 0; i < testFiles.length; i++) {
            assertThat(testFiles[i].setLastModified(testTime + (i * 1000l)))
                    .isTrue();
        }

        fileAgent.run();

        ArgumentCaptor<File> argument = ArgumentCaptor.forClass(
                File.class);
        verify(processor, times(10)).onNewImportFile(
                any(GenericImportSpec.class),
                any(MessageEndpointFactory.class),
                argument.capture());

        Arrays.sort(testFiles, new FileAgeComparator());
        for (ListIterator<File> it = argument.getAllValues()
                .listIterator(); it.hasNext();) {
            File file = testFiles[it.nextIndex()];
            assertThat(it.next().getName()).isEqualTo(file.getName());
        }
    }

    /**
     * Test of {@link FileAgentWork#run()} with no files found.
     */
    @Test
    public void testNoFilesRun() throws Exception {
        final String importDir =
                File.createTempFile("dummy", ".tmp").getParent();

        System.out.printf("working with importDir: %s%n", importDir);
        FileAgentWork fileAgent = new FileAgentWork(new TestImporterSpec(
                importDir,
                "fileAgent2_.*\\.tmp"), null, processor);
        fileAgent.run();
        verifyZeroInteractions(processor);
    }

    /**
     * Test of {@link FileAgentWork#run()} with no files found.
     */
    @Test
    public void testGetFilesReturnNullRun() throws Exception {

        FileAgentWork fileAgent = new FileAgentWork(
                new TestImporterSpec(
                "/some/crude/directory/that/does/not/exist/to/force/getfiles/returning/null",
                "fileAgent2_.*\\.tmp"), null, processor);
        fileAgent.run();
        verifyZeroInteractions(processor);
    }
}

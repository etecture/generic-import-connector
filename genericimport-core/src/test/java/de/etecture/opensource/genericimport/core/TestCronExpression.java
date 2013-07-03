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

import de.herschke.testhelper.PrettyPrintingRule;
import static junit.framework.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * test the method {@link  GenericImportSpec#getPeriod() } with different valit
 * expressions
 *
 * @author christoph knauf
 * @version ${project.version}
 * @since 1.0.1
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCronExpression {

    private String everyYear = "0 0 0 1 12 ?";
    private String daily = "0 0 0 * * ?";
    private String hourly = "2 5 * * * ?";
    private String minutely = "0 0/1 * * * ?";
    private String every5Seconds = "0/5 * * * * ?";
    private String every10Minutes = "0 */10 * * * ?";
    private String[] expressions = {
        minutely, every5Seconds, daily, hourly, every10Minutes, everyYear
    };
    private long[] periodsInMillis = {
        60000, 5000, 60000 * 60 * 24, 60000 * 60, 60000 * 10, 60000 * 60 * 24 * 365L
    };
    @Mock
    GenericImportSpec instance;
    @Rule
    public PrettyPrintingRule out = new PrettyPrintingRule();

    @Before
    public void init(){
        instance = new GenericImportSpec();
    }
    
    @Test
    public void testPerdiod() {
        for (int i = 0; i < expressions.length; i++) {
            String exprString = expressions[i];
            instance.setScheduleExpression(exprString);
            out.println(instance.getPeriod()+" == "+periodsInMillis[i]);
            assertTrue(instance.getPeriod() == periodsInMillis[i]);
        }
    }
}

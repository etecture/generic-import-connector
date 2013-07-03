/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

package de.etecture.opensource.genericimport.cron;

import de.herschke.testhelper.ConsoleWriter;
import de.herschke.testhelper.PrettyPrintingRule;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatterBuilder;
import org.junit.Rule;
import org.junit.Test;

/**
 *
 * @author rhk
 */
public class ScheduleExpressionTest {

    @Rule
    public PrettyPrintingRule out = new PrettyPrintingRule();
    DateTime time = new DateTime(2013, 5, 7, 1,
            15, 32, DateTimeZone.UTC);

    @Test
    public void testHourly() throws Exception {
        ScheduleExpression exp = new ScheduleExpression("@hourly");
        long diff = (new DateTime(2013, 5, 7, 2, 0, 0, 0,
                DateTimeZone.UTC)
                .getMillis())
                - time
                .getMillis();
        printResult(exp.toString(), time, diff, exp.getNextValidTime(time
                .getMillis()) - time.getMillis());
    }

    @Test
    public void testEveryMinute() throws Exception {
        ScheduleExpression exp = new ScheduleExpression("@every-minute");
        long diff = (new DateTime(2013, 5, 7, 1, 16, 0, 0,
                DateTimeZone.UTC)
                .getMillis())
                - time
                .getMillis();
        printResult(exp.toString(), time, diff, exp.getNextValidTime(time
                .getMillis()) - time.getMillis());
    }

    @Test
    public void testEverySecond() throws Exception {
        ScheduleExpression exp = new ScheduleExpression("@every-second");
        long diff = (new DateTime(2013, 5, 7, 1, 15, 33, 0,
                DateTimeZone.UTC)
                .getMillis())
                - time
                .getMillis();
        printResult(exp.toString(), time, diff, exp.getNextValidTime(time
                .getMillis()) - time.getMillis());
    }

    @Test
    public void testMidnight() throws Exception {
        ScheduleExpression exp = new ScheduleExpression("@midnight");
        long diff = (new DateTime(2013, 5, 8, 0, 0, 0, 0,
                DateTimeZone.UTC)
                .getMillis())
                - time
                .getMillis();
        printResult(exp.toString(), time, diff, exp.getNextValidTime(time
                .getMillis()) - time.getMillis());
    }

    @Test
    public void testComplexExpression() throws Exception {
        ScheduleExpression exp = new ScheduleExpression("*/5 3/7 5-7 FRI-SUN");
        DateTime next = new DateTime(2013, 5, 10, 5, 3, 0, DateTimeZone.UTC);
        long diff = (next.getMillis()) - time.getMillis();
        long nextTime = exp.getNextValidTime(time.getMillis());
        printResult(exp.toString(), time, diff, nextTime - time.getMillis());

        DateTime nnext = new DateTime(2013, 5, 10, 5, 3, 5, DateTimeZone.UTC);
        diff = (nnext.getMillis()) - next.getMillis();
        nextTime = exp.getNextValidTime(next.getMillis());
        printResult(exp.toString(), next, diff, nextTime - next.getMillis());

        nnext = nnext.plusSeconds(51);

        DateTime nnnext = new DateTime(2013, 5, 10, 5, 10, 0, DateTimeZone.UTC);
        diff = (nnnext.getMillis()) - nnext.getMillis();
        nextTime = exp.getNextValidTime(nnext.getMillis());
        printResult(exp.toString(), nnext, diff, nextTime - nnext.getMillis());
    }

    @Test
    public void testExactExpression() throws Exception {
        ScheduleExpression exp = new ScheduleExpression("0 3 9 WED");
        long diff = (new DateTime(time.getYear(), time.getMonthOfYear(), time
                .getDayOfMonth() + 1, 9, 3, 0,
                DateTimeZone.UTC)
                .getMillis())
                - time
                .getMillis();
        printResult(exp.toString(), time, diff, exp.getNextValidTime(time
                .getMillis()) - time.getMillis());
    }

    protected void printResult(String expression, DateTime time, long expected,
            long difference) throws AssertionError {
        out.println(expression);
        out.printLeft("%s + %s == %s",
                time.toString("E HH:mm:ss.SSS"),
                new Period(expected).toString(new PeriodFormatterBuilder()
                .printZeroRarelyFirst()
                .appendHours()
                .appendSuffix("h")
                .appendSeparator(" ")
                .printZeroRarelyLast()
                .appendMinutes()
                .appendSuffix("m")
                .appendSeparator(" ")
                .printZeroRarelyLast()
                .appendSeconds()
                .appendSuffix("s")
                .appendSeparator(" ")
                .appendMillis3Digit()
                .appendSuffix("ms")
                .toFormatter()),
                time.plusMillis((int) expected).
                toString("E HH:mm:ss.SSS"));
        if (difference == expected) {
            out.printRight(ConsoleWriter.Color.GREEN, " OK");
        } else {
            out.printRight(ConsoleWriter.Color.RED, " FAILED");
            String explain = String.format("  expected: %s, actual: %s", time
                    .plusMillis(
                    (int) expected).toString("E HH:mm:ss.SSS"), time.plusMillis(
                    (int) difference).toString("E HH:mm:ss.SSS"));
            out.println(explain);
            throw new AssertionError(explain);
        }
    }
}

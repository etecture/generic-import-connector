package de.etecture.opensource.genericimport.cron;

import java.text.ParseException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * represents a schedule expression.
 * <p>
 * the grammar is the following:
 * <br/><code>
 * &lt;expression&gt;&nbsp;=&nbsp;&lt;seconds:field&gt;&lt;space&gt;&lt;minutes:field&gt;&lt;space&gt;&lt;hours:field&gt;&lt;space&gt;&lt;day_in_week:field&gt;<br/>
 * &lt;field&gt;&nbsp;=&nbsp;&lt;group&gt;&nbsp;[&nbsp;&lt;comma&gt;&lt;field&gt;&nbsp;]<br/>
 * &lt;group&gt;&nbsp;=&nbsp;&lt;star&gt;&nbsp;|&nbsp;&lt;start&gt;&nbsp;[&nbsp;&lt;minus&gt;&lt;end&gt;&nbsp;]&nbsp;[&nbsp;&lt;slash&gt;&nbsp;&lt;divider&gt;&nbsp;]<br/></code>
 * <p>
 * the following values are allowed:
 * <br/>
 * <table border=1 cellpadding=5 cellspacing=0>
 * <tr><th>field</th><th>start</th><th>end</th><th>divider</th></tr>
 * <tr><th>seconds</th><td>0..59</td><td>1..59</td><td>1..59</td></tr>
 * <tr><th>minutes</th><td>0..59</td><td>1..59</td><td>1..59</td></tr>
 * <tr><th>hours</th><td>0..23</td><td>1..23</td><td>1..23</td></tr>
 * <tr><th>day_in_weak</th><td>1..7</td><td>2..7</td><td>1..6</td></tr>
 * </table>
 *
 * @author rhk
 */
public class ScheduleExpression {

    private final SortedSet<ScheduleExpressionField> fields = new TreeSet<>();
    private final String definition;

    private enum Template {

        HOURLY("(?i:@hourly)", "0 0 * *"),
        EVERY_MINUTE("(?i:@every\\-minute)", "0 * * *"),
        EVERY_SECOND("(?i:@every\\-second)", "* * * *"),
        MIDNIGHT("(?i:@midnight)", "0 0 0 *");
        private final String pattern;
        private final String replacement;

        private Template(String pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }

        public String replace(String in) {
            if (in.matches(pattern)) {
                return replacement;
            } else {
                return in;
            }
        }
    }

    public ScheduleExpression(String definition) throws ParseException {
        for (Template template : Template.values()) {
            definition = template.replace(definition);
        }
        this.definition = definition;
        parseDefinition();
    }

    private void parseDefinition() throws ParseException {
        fields.clear();
        String[] fieldDefinitions = definition.split("\\s+");
        if (fieldDefinitions.length
                < ScheduleExpressionField.Type.values().length) {
            throw new ParseException(
                    String.format(
                    "cannot parse schedule expression definition, due to missing field: %s",
                    ScheduleExpressionField.Type.values()[fieldDefinitions.length]
                    .name()), 0);
        } else if (fieldDefinitions.length > ScheduleExpressionField.Type
                .values().length) {
            throw new ParseException(
                    "cannot parse schedule expression due to unknown additional field",
                    0);
        }
        for (int i = 0; i < fieldDefinitions.length; i++) {
            fields.add(new ScheduleExpressionField(ScheduleExpressionField.Type
                    .values()[i],
                    fieldDefinitions[i]));
        }
    }

    public long getNextValidTime(long actualTime) {
        long nextTime = actualTime;
        // next full second
        nextTime += 1000 - (actualTime % 1000);
        for (ScheduleExpressionField field : fields) {
            nextTime += field.getDifferenceToNextValidTime(nextTime);
            SortedSet<ScheduleExpressionField> headSet = fields.headSet(field);
            ScheduleExpressionField[] headArray = headSet.toArray(
                    new ScheduleExpressionField[headSet.size()]);
            for (int i = headArray.length - 1; i >= 0; i--) {
                nextTime += headArray[i]
                        .getDifferenceToNextValidTime(nextTime);
            }
        }
        return nextTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ScheduleExpressionField field : fields) {
            sb.append(field.toString()).append(", ");
        }
        sb.delete(sb.lastIndexOf(", "), sb.length());
        return sb.toString();
    }
}

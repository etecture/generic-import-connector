package de.etecture.opensource.genericimport.cron;

import java.text.ParseException;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * represents a field in the schedule expression.
 * <p>
 * N.B. a field can be the seconds, the minutes a.s.o.
 *
 * @author rhk
 */
public class ScheduleExpressionField implements
        Comparable<ScheduleExpressionField> {

    public enum WEEKDAY {

        MON,
        TUE,
        WED,
        THU,
        FRI,
        SAT,
        SUN;

        public String replaceAll(String in) {
            return in.replaceAll("(?i:" + name() + ")", "" + ordinal());
        }
    }

    public enum Type {

        SECONDS(0, 60, 1000l),
        MINUTES(0, 60, 60l * 1000l),
        HOURS(0, 24, 60l * 60l * 1000l),
        DAY_IN_WEEK(0, 7, 24l * 60l * 60l * 1000l) {
            @Override
            public long getActual(long actualTime) {
                return new DateTime(actualTime, DateTimeZone.UTC).getDayOfWeek()
                        - 1;
            }
        };
        private final int min;
        private final int max;
        private final long multiplicator;

        private Type(int max, long multiplicator) {
            this(0, max, multiplicator);
        }

        private Type(int min, int max, long multiplicator) {
            this.min = min;
            this.max = max;
            this.multiplicator = multiplicator;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        public long getMultiplicator() {
            return multiplicator;
        }

        public long getRange() {
            return max - min;
        }

        public long getActual(long actualTime) {
            return (actualTime / multiplicator) % getRange();
        }
    }
    private static final Pattern GROUP_REGEX = Pattern.compile(
            "(?<start>\\d+)(?:\\-(?<end>\\d+))?(?:/(?<divider>\\d+))?");
    private final Type type;
    private final String definition;
    private final SortedSet<ScheduleExpressionFieldGroup> groups =
            new TreeSet<>();

    public ScheduleExpressionField(Type type, String definition) throws
            ParseException {
        this.type = type;
        this.definition = definition;
        parseDefinition();
    }

    private void parseDefinition() throws ParseException {
        groups.clear();
        int i = 0;
        for (String groupDefinition : definition.split(",")) {
            if (type == Type.DAY_IN_WEEK) {
                for (WEEKDAY w : WEEKDAY.values()) {
                    groupDefinition = w.replaceAll(groupDefinition);
                }
            }
            // replace the star '*'
            groupDefinition = groupDefinition.replaceAll("\\*",
                    String.format("%d-%d", type.min, type.max));
            Matcher m = GROUP_REGEX.matcher(groupDefinition);
            if (m.matches()) {
                int start = getGroupValue(m.group("start"), type.min);
                String div = m.group("divider");
                int divider = getGroupValue(div, 1);
                int end;
                if (div == null || div.length() == 0) {
                    end = getGroupValue(m.group("end"), start + 1);
                } else {
                    end = getGroupValue(m.group("end"), type.max);
                }
                groups.add(new ScheduleExpressionFieldGroup(start, end,
                        divider));
            } else {
                throw new ParseException(
                        "the group does not match the schedule expression specification.",
                        i);
            }
            i++;
        }
        if (groups.isEmpty()) {
            throw new ParseException("an empty expression is not allowed.", 0);
        }
    }

    public long getDifferenceToNextValidTime(long actualTime) {
        long actualFieldTime = type.getActual(actualTime);
        long frac = actualTime % type.multiplicator;
        Iterator<ScheduleExpressionFieldGroup> it = groups.iterator();
        while (it.hasNext()) {
            ScheduleExpressionFieldGroup group = it.next();
            if (group.getStart() <= actualFieldTime
                    && actualFieldTime < group.getEnd()) {
                long correctedActualFieldTime = actualFieldTime - group
                        .getStart();
                long dividerRest = correctedActualFieldTime % group.getDivider();
                if (dividerRest == 0) {
                    return 0;
                }
                long difference = group.getDivider() - dividerRest;
                return (difference * type.multiplicator) - frac;
            } else if (actualFieldTime < group.getStart()) {
                return ((group.getStart() - actualFieldTime)
                        * type.multiplicator) - frac;
            }
        }
        assert !groups.isEmpty(); // empty groups are checked when parsing the groups.
        return (((groups.first().getStart() - actualFieldTime)
                + type.getRange())
                * type.multiplicator) - frac;
    }

    @Override
    public String toString() {
        return type.name() + "(" + definition.replaceAll("\\*", String.format(
                "%d-%d", type.min, type.max)) + ")";
    }

    private static int getGroupValue(String groupValue, int defaultValue) {
        if (groupValue == null || groupValue.length() == 0) {
            return defaultValue;
        } else {
            return Integer.parseInt(groupValue);
        }
    }

    @Override
    public int compareTo(ScheduleExpressionField other) {
        return Integer.compare(this.type.ordinal(), other.type.ordinal());
    }
}

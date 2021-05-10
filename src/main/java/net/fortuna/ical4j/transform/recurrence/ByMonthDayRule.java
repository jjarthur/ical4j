package net.fortuna.ical4j.transform.recurrence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.YearMonth;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static net.fortuna.ical4j.transform.recurrence.Frequency.MONTHLY;
import static net.fortuna.ical4j.transform.recurrence.Frequency.YEARLY;

/**
 * Applies BYMONTHDAY rules specified in this Recur instance to the specified date list. If no BYMONTHDAY rules are
 * specified the date list is returned unmodified.
 *
 * NOTE: For now BYMONTHDAY is not compatible with Instant temporal type. This is because this rule requires a
 * year-month value which is not available from Instant temporal type.
 */
public class ByMonthDayRule<T extends Temporal> extends AbstractDateExpansionRule<T> {

    private transient Logger log = LoggerFactory.getLogger(ByMonthDayRule.class);

    private final List<Integer> monthDayList;

    public ByMonthDayRule(List<Integer> monthDayList, Frequency frequency) {
        super(frequency);
        this.monthDayList = monthDayList;
    }

    @Override
    public List<T> transform(List<T> dates) {
        if (monthDayList.isEmpty()) {
            return dates;
        }
        final List<T> monthDayDates = new ArrayList<>();
        for (final T date : dates) {
            if (EnumSet.of(MONTHLY, YEARLY).contains(getFrequency())) {
                monthDayDates.addAll(new ExpansionFilter().apply(date));
            } else {
                Optional<T> limit = new LimitFilter().apply(date);
                limit.ifPresent(monthDayDates::add);
            }
        }
        return monthDayDates;
    }

    private class LimitFilter implements Function<T, Optional<T>> {
        @Override
        public Optional<T> apply(T date) {
            if (monthDayList.contains(getDayOfMonth(date))) {
                return Optional.of(date);
            }
            return Optional.empty();
        }
    }

    private class ExpansionFilter implements Function<T, List<T>> {
        @Override
        public List<T> apply(T date) {
            List<T> retVal = new ArrayList<>();
            // construct a list of possible month days..
            final YearMonth yearMonth = YearMonth.of(getYear(date), getMonth(date));
            for (final int monthDay : monthDayList) {
                if (!yearMonth.isValidDay(Math.abs(monthDay))) {
                    if (log.isTraceEnabled()) {
                        log.trace("Invalid day of month: " + monthDay);
                    }
                    continue;
                }
                T candidate;
                if (monthDay > 0) {
                    candidate = withTemporalField(date, DAY_OF_MONTH, monthDay);
                } else {
                    candidate = withTemporalField(date, DAY_OF_MONTH, yearMonth.lengthOfMonth() + 1 + monthDay);
                }
                retVal.add(candidate);
            }
            return retVal;
        }
    }

    /**
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        log = LoggerFactory.getLogger(ByMonthDayRule.class);
    }
}

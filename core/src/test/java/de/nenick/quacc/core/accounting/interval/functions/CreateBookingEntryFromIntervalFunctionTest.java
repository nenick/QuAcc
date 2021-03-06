package de.nenick.quacc.core.accounting.interval.functions;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.tz.UTCProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import de.nenick.quacc.core.bookingentry.creation.CreateBookingIntervalEntryFunction;
import de.nenick.quacc.core.bookingentry.creation.CreateBookingEntryFromIntervalFunction;
import de.nenick.quacc.core.common.util.QuAccDateUtil;

import static de.nenick.quacc.core.bookinginterval.BookingIntervalOption.daily;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class CreateBookingEntryFromIntervalFunctionTest {

    @Test
    public void work() {
        fail("not ready");
    }
    /*
    public static final long intervalId = 42l;

    @Mock
    IntervalDb intervalDb;

    @Mock
    AccountingDb accountingDb;

    @Mock
    IntervalChangeDb intervalChangeDb;

    @Mock
    CreateBookingIntervalEntryFunction createBookingIntervalEntryFunction;

    @InjectMocks
    CreateBookingEntryFromIntervalFunction createBookingEntryFromIntervalFunction;

    @Before
    public void setup() {
        DateTimeZone.setProvider(new UTCProvider());
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCreateAccountingForRange_daily() {
        DateTime intervalStartDate = QuAccDateUtil.toDateTime(30, 12, 2014);
        DateTime updateUntil = QuAccDateUtil.toDateTime(2, 1, 2015);

        IntervalCursor cursor = MockIntervalCursor.create()
                .withDateStart(intervalStartDate.toDate())
                .withInterval(daily)
                .withUpdatedUntil(IntervalDb.NO_DATE_GIVEN)
                .withDateLast(IntervalDb.NO_DATE_GIVEN)
                .get();
        IntervalChangeCursor intervalChangeCursor = MockIntervalChangeCursor.create().get();
        given(intervalChangeDb.getAllForInterval(anyLong())).willReturn(intervalChangeCursor);
        given(intervalChangeDb.getAllForIntervalUntil(anyLong(), any(Date.class))).willReturn(intervalChangeCursor);

        createBookingEntryFromIntervalFunction.apply(cursor, new DateTime(updateUntil));

        verify(createBookingIntervalEntryFunction).apply(cursor, QuAccDateUtil.toDateTime(30, 12, 2014).toDate());
        verify(createBookingIntervalEntryFunction).apply(cursor, QuAccDateUtil.toDateTime(31, 12, 2014).toDate());
        verify(createBookingIntervalEntryFunction).apply(cursor, QuAccDateUtil.toDateTime(1, 1, 2015).toDate());
        verify(createBookingIntervalEntryFunction).apply(cursor, QuAccDateUtil.toDateTime(2, 1, 2015).toDate());
        verifyNoMoreInteractions(createBookingIntervalEntryFunction);
    }

    @Test
    public void shouldUpdateIntervalState_dateUpdateUntil() {
        DateTime updateUntil = QuAccDateUtil.toDateTime(1, 2, 2015);
        DateTime updatedUntil = updateUntil.minusDays(1);
        IntervalCursor cursor = MockIntervalCursor.create()
                .withId(intervalId)
                .withInterval(daily)
                .withUpdatedUntil(updatedUntil.toDate())
                .withDateLast(updatedUntil.toDate())
                .get();
        IntervalChangeCursor intervalChangeCursor = MockIntervalChangeCursor.create().get();
        given(intervalChangeDb.getAllForInterval(anyLong())).willReturn(intervalChangeCursor);
        given(intervalChangeDb.getAllForIntervalUntil(anyLong(), any(Date.class))).willReturn(intervalChangeCursor);

        createBookingEntryFromIntervalFunction.apply(cursor, updateUntil);
        verify(intervalDb).updatedUntil(intervalId, updateUntil.toDate(), updateUntil.toDate());
    }

    @Test
    public void shouldApplyIntervalChangeSingle() {
        DateTime intervalStartDate = QuAccDateUtil.toDateTime(30, 12, 2014);
        DateTime updateUntil = QuAccDateUtil.toDateTime(2, 1, 2015);

        IntervalCursor cursor = MockIntervalCursor.create()
                .withDateStart(intervalStartDate.toDate())
                .withInterval(daily)
                .withUpdatedUntil(IntervalDb.NO_DATE_GIVEN)
                .withDateLast(IntervalDb.NO_DATE_GIVEN)
                .get();

        IntervalChangeCursor intervalChangeCursor = MockIntervalChangeCursor.create()
                .withMoveToNext(1, 4)
                .withChange(IntervalChange.single)
                .withComment("blub")
                .withValue(450)
                .withDate(QuAccDateUtil.toDateTime(1, 1, 2015).toDate())
                .get();
        given(intervalChangeDb.getAllForInterval(anyLong())).willReturn(intervalChangeCursor);
        given(intervalChangeDb.getAllForIntervalUntil(anyLong(), any(Date.class))).willReturn(intervalChangeCursor);
        given(createBookingIntervalEntryFunction.apply(cursor, QuAccDateUtil.toDateTime(1, 1, 2015).toDate())).willReturn(11l);


        createBookingEntryFromIntervalFunction.apply(cursor, new DateTime(updateUntil));

        verify(createBookingIntervalEntryFunction).apply(cursor, QuAccDateUtil.toDateTime(30, 12, 2014).toDate());
        verify(createBookingIntervalEntryFunction).apply(cursor, QuAccDateUtil.toDateTime(31, 12, 2014).toDate());

        verify(createBookingIntervalEntryFunction).apply(cursor, QuAccDateUtil.toDateTime(1, 1, 2015).toDate());
        verify(accountingDb).updateComment(11, "blub");
        verify(accountingDb).updateValue(11, 450);
        verifyNoMoreInteractions(accountingDb);
        verify(createBookingIntervalEntryFunction).apply(cursor, QuAccDateUtil.toDateTime(2, 1, 2015).toDate());
        verifyNoMoreInteractions(createBookingIntervalEntryFunction);
    }

    @Test
    public void shouldApplyIntervalChangeFollowUp() {
        DateTime intervalStartDate = QuAccDateUtil.toDateTime(30, 12, 2014);
        DateTime updateUntil = QuAccDateUtil.toDateTime(2, 1, 2015);

        IntervalCursor cursor = MockIntervalCursor.create()
                .withId(11)
                .withDateStart(intervalStartDate.toDate())
                .withInterval(daily)
                .withUpdatedUntil(IntervalDb.NO_DATE_GIVEN)
                .withDateLast(IntervalDb.NO_DATE_GIVEN)
                .get();

        IntervalChangeCursor intervalChangeCursor = MockIntervalChangeCursor.create()
                .withMoveToNext(1, 4)
                .withChange(IntervalChange.followUp)
                .withComment("blub")
                .withValue(450)
                .withDate(QuAccDateUtil.toDateTime(1, 1, 2015).toDate())
                .get();

        given(intervalChangeDb.getAllForInterval(11)).willReturn(intervalChangeCursor);
        given(intervalChangeDb.getAllForIntervalUntil(anyLong(), any(Date.class))).willReturn(MockIntervalChangeCursor.create().get());
        given(intervalChangeDb.getAllForIntervalUntil(intervalId, QuAccDateUtil.toDateTime(1, 1, 2015).toDate())).willReturn(intervalChangeCursor);

        given(createBookingIntervalEntryFunction.apply(cursor, QuAccDateUtil.toDateTime(30, 12, 2014).toDate())).willReturn(9l);
        given(createBookingIntervalEntryFunction.apply(cursor, QuAccDateUtil.toDateTime(31, 12, 2014).toDate())).willReturn(10l);
        given(createBookingIntervalEntryFunction.apply(cursor, QuAccDateUtil.toDateTime(1, 1, 2015).toDate())).willReturn(11l);
        given(createBookingIntervalEntryFunction.apply(cursor, QuAccDateUtil.toDateTime(2, 1, 2015).toDate())).willReturn(12l);

        createBookingEntryFromIntervalFunction.apply(cursor, new DateTime(updateUntil));

        verify(createBookingIntervalEntryFunction).apply(cursor, QuAccDateUtil.toDateTime(30, 12, 2014).toDate());
        verify(createBookingIntervalEntryFunction).apply(cursor, QuAccDateUtil.toDateTime(31, 12, 2014).toDate());

        verify(createBookingIntervalEntryFunction).apply(cursor, QuAccDateUtil.toDateTime(1, 1, 2015).toDate());
        verify(accountingDb, times(2)).updateComment(11, "blub");
        verify(accountingDb, times(2)).updateValue(11, 450);

        verify(createBookingIntervalEntryFunction).apply(cursor, QuAccDateUtil.toDateTime(2, 1, 2015).toDate());
        verify(accountingDb).updateComment(12, "blub");
        verify(accountingDb).updateValue(12, 450);
        verifyNoMoreInteractions(accountingDb);

        verifyNoMoreInteractions(createBookingIntervalEntryFunction);
    }

    @Test
    public void shouldUseCurrentFollowUpChanges() {
        DateTime intervalStartDate = QuAccDateUtil.toDateTime(30, 12, 2014);
        DateTime updateUntil = QuAccDateUtil.toDateTime(2, 1, 2015);

        IntervalCursor cursor = MockIntervalCursor.create()
                .withId(11)
                .withDateStart(intervalStartDate.toDate())
                .withInterval(daily)
                .withUpdatedUntil(QuAccDateUtil.toDateTime(1, 1, 2015).toDate())
                .withDateLast(QuAccDateUtil.toDateTime(1, 1, 2015).toDate())
                .get();

        IntervalChangeCursor intervalChangeUntilCursor = MockIntervalChangeCursor.create()
                .withMoveToNext(2, 4)
                .withChange(IntervalChange.followUp, IntervalChange.followUp)
                .withComment("blub", null)
                .withValue(0, 450)
                .withDate(QuAccDateUtil.toDateTime(31, 12, 2014).toDate(), QuAccDateUtil.toDateTime(1, 1, 2015).toDate())
                .get();
        given(intervalChangeDb.getAllForIntervalUntil(11, QuAccDateUtil.toDateTime(1, 1, 2015).toDate())).willReturn(intervalChangeUntilCursor);

        IntervalChangeCursor intervalChangeCursor = MockIntervalChangeCursor.create()
                .withMoveToNext(1, 4)
                .withChange(IntervalChange.followUp)
                .withComment("blub")
                .withValue(450)
                .withDate(QuAccDateUtil.toDateTime(1, 1, 2015).toDate())
                .get();
        given(intervalChangeDb.getAllForInterval(11)).willReturn(intervalChangeCursor);

        given(createBookingIntervalEntryFunction.apply(cursor, QuAccDateUtil.toDateTime(2, 1, 2015).toDate())).willReturn(11l);

        createBookingEntryFromIntervalFunction.apply(cursor, new DateTime(updateUntil));

        verify(createBookingIntervalEntryFunction).apply(cursor, QuAccDateUtil.toDateTime(2, 1, 2015).toDate());
        verify(accountingDb).updateComment(11, "blub");
        verify(accountingDb).updateValue(11, 450);
        verifyNoMoreInteractions(accountingDb);

        verifyNoMoreInteractions(createBookingIntervalEntryFunction);
    }
    */
}
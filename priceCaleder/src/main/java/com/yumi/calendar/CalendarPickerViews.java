package com.yumi.calendar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.yumi.calendar.adapter.DefaultDayViewAdapter;
import com.yumi.calendar.decorator.CalendarCellDecorator;
import com.yumi.calendar.decorator.DayViewAdapter;
import com.yumi.calendar.descriptor.MonthCellDescriptor;
import com.yumi.calendar.descriptor.MonthCellDescriptor.RangeState;
import com.yumi.calendar.descriptor.MonthDescriptor;
import com.yumi.calendar.view.CalendarCellViews;
import com.yumi.calendar.view.MonthViews;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;

/**
 * Created by TechHunt Developers on 2019-11-22.
 */
public class CalendarPickerViews extends ListView {
    private Context context;

    public enum SelectionMode {

        SINGLE,

        MULTIPLE,
        RANGE,
        TWICE
    }

    private final MonthAdapter adapter;
    private final List<List<List<MonthCellDescriptor>>> cells = new ArrayList<>();
    final MonthViews.Listener listener = new CellClickedListener();
    final List<MonthDescriptor> months = new ArrayList<>();
    final List<MonthCellDescriptor> selectedCells = new ArrayList<>();
    final List<MonthCellDescriptor> highlightedCells = new ArrayList<>();
    final List<Calendar> selectedCals = new ArrayList<>();
    final List<Calendar> highlightedCals = new ArrayList<>();
    private Locale locale;
    private DateFormat monthNameFormat;  // 月份格式化
    private DateFormat weekdayNameFormat;  // 星期格式化
    private Calendar minCal;
    private Calendar maxCal;
    private Calendar monthCounter;
    private boolean displayOnly;
    SelectionMode selectionMode;
    Calendar today;
    private int dividerColor;  // 分割线
    private int dayBackgroundResId;  // 日期背景
    private int dayTextColorResId;  // 日期颜色
    private int titleTextColor;  // 月份颜色
    private boolean displayHeader;  // 是否显示星期header
    private int headerTextColor;  // 星期header颜色
    private Typeface titleTypeface;
    private Typeface dateTypeface;

    private OnDateSelectedListener dateListener;
    private DateSelectableFilter dateConfiguredListener;
    private OnInvalidDateSelectedListener invalidDateListener =
            new DefaultOnInvalidDateSelectedListener();
    private CellClickInterceptor cellClickInterceptor;
    private List<CalendarCellDecorator> decorators;
    private DayViewAdapter dayViewAdapter = new DefaultDayViewAdapter();

    public void setDecorators(List<CalendarCellDecorator> decorators) {
        this.decorators = decorators;
        if (null != adapter) {
            adapter.notifyDataSetChanged();
        }
    }

    public List<CalendarCellDecorator> getDecorators() {
        return decorators;
    }

    public CalendarPickerViews(Context context, boolean displayHeader) {
        super(context);
        this.context = context;
        Resources res = context.getResources();
        adapter = getMonthAdapter();
        this.dividerColor = res.getColor(R.color.calendar_divider);
        this.dayBackgroundResId = R.drawable.calendar_bg_selector;
        this.dayTextColorResId = R.drawable.calendar_text_selector;
        this.titleTextColor = res.getColor(R.color.calendar_text_title);
        this.displayHeader = displayHeader;
        this.headerTextColor = res.getColor(R.color.calendar_text_week_head);
        initView(res.getColor(R.color.calendar_bg), context);
    }

    public CalendarPickerViews(Context context, int bg, int dividerColor, int dayBackgroundResId, int dayTextColorResId,
                              int titleTextColor, boolean displayHeader, int headerTextColor) {
        super(context);
        this.context = context;
        adapter = getMonthAdapter();
        this.dividerColor = dividerColor;
        this.dayBackgroundResId = dayBackgroundResId;
        this.dayTextColorResId = dayTextColorResId;
        this.titleTextColor = titleTextColor;
        this.displayHeader = displayHeader;
        this.headerTextColor = headerTextColor;
        initView(bg, context);
    }

    public CalendarPickerViews(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        Resources res = context.getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarPickerView);
        final int bg = a.getColor(R.styleable.CalendarPickerView_android_background,
                res.getColor(R.color.calendar_bg));
        dividerColor = a.getColor(R.styleable.CalendarPickerView_tsquare_dividerColor,
                res.getColor(R.color.calendar_divider));
        dayBackgroundResId = a.getResourceId(R.styleable.CalendarPickerView_tsquare_dayBackground,
                R.drawable.calendar_bg_selector);
        dayTextColorResId = a.getResourceId(R.styleable.CalendarPickerView_tsquare_dayTextColor,
                R.drawable.calendar_text_selector);
        titleTextColor = a.getColor(R.styleable.CalendarPickerView_tsquare_titleTextColor,
                res.getColor(R.color.calendar_text_title));
        displayHeader = a.getBoolean(R.styleable.CalendarPickerView_tsquare_displayHeader, true);
        headerTextColor = a.getColor(R.styleable.CalendarPickerView_tsquare_headerTextColor,
                res.getColor(R.color.calendar_text_week_head));
        a.recycle();

        adapter = getMonthAdapter();
        initView(bg, context);
    }

    private void initView(int bg, Context context) {
        setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        setDivider(null);
        setDividerHeight(0);
        setBackgroundColor(bg);
        setCacheColorHint(bg);

        locale = Locale.getDefault();
        today = Calendar.getInstance(locale);
        minCal = Calendar.getInstance(locale);
        maxCal = Calendar.getInstance(locale);
        monthCounter = Calendar.getInstance(locale);
        monthNameFormat = new SimpleDateFormat(context.getString(R.string.month_name_format), locale);
        weekdayNameFormat = new SimpleDateFormat(context.getString(R.string.day_name_format), locale);

        if (isInEditMode()) {
            Calendar nextYear = Calendar.getInstance(locale);
            nextYear.add(Calendar.YEAR, 1);

            Calendar cal = Calendar.getInstance();
            init(cal.getTime(), nextYear.getTime()) //
                    .withSelectedDate(new Date());
        }
    }

    public MonthAdapter getMonthAdapter() {
        return new MonthAdapter();
    }

    public FluentInitializer init(Date minDate, Date maxDate, Locale locale) {
        if (minDate == null || maxDate == null) {
            throw new IllegalArgumentException(
                    "minDate and maxDate must be non-null.  " + dbg(minDate, maxDate));
        }
        if (minDate.after(maxDate)) {
            throw new IllegalArgumentException(
                    "minDate must be before maxDate.  " + dbg(minDate, maxDate));
        }
        if (locale == null) {
            throw new IllegalArgumentException("Locale is null.");
        }

        this.locale = locale;
        today = Calendar.getInstance(locale);
        minCal = Calendar.getInstance(locale);
        maxCal = Calendar.getInstance(locale);
        monthCounter = Calendar.getInstance(locale);
        monthNameFormat = new SimpleDateFormat(getContext().getString(R.string.month_name_format), locale);
        for (MonthDescriptor month : months) {
            month.setLabel(monthNameFormat.format(month.date));
        }
        weekdayNameFormat = new SimpleDateFormat(getContext().getString(R.string.day_name_format), locale);

        this.selectionMode = SelectionMode.SINGLE;
        selectedCals.clear();
        selectedCells.clear();
        highlightedCals.clear();
        highlightedCells.clear();

        cells.clear();
        months.clear();
        minCal.setTime(minDate);
        maxCal.setTime(maxDate);
        setMidnight(minCal);
        setMidnight(maxCal);
        displayOnly = false;

        maxCal.add(MINUTE, -1);

        monthCounter.setTime(minCal.getTime());
        final int maxMonth = maxCal.get(MONTH);
        final int maxYear = maxCal.get(YEAR);
        while ((monthCounter.get(MONTH) <= maxMonth // Up to, including the month.
                || monthCounter.get(YEAR) < maxYear) // Up to the year.
                && monthCounter.get(YEAR) < maxYear + 1) { // But not > next yr.
            Date date = monthCounter.getTime();
            MonthDescriptor month =
                    new MonthDescriptor(monthCounter.get(MONTH), monthCounter.get(YEAR), date,
                            monthNameFormat.format(date));
            cells.add(getMonthCells(month, monthCounter));
            Log.d("Adding month %s", month.getClass().getName());
            months.add(month);
            monthCounter.add(MONTH, 1);
        }

        validateAndUpdate();
        return new FluentInitializer();
    }


    public FluentInitializer init(Date minDate, Date maxDate) {
        return init(minDate, maxDate, Locale.getDefault());
    }

    public class FluentInitializer {

        public FluentInitializer inMode(SelectionMode mode) {
            selectionMode = mode;
            validateAndUpdate();
            return this;
        }


        public FluentInitializer withSelectedDate(Date selectedDates) {
            return withSelectedDates(Collections.singletonList(selectedDates));
        }


        public FluentInitializer withSelectedDates(Collection<Date> selectedDates) {
            if (selectionMode == SelectionMode.SINGLE && selectedDates.size() > 1) {
                throw new IllegalArgumentException("SINGLE mode can't be used with multiple selectedDates");
            }
            if (selectionMode == SelectionMode.RANGE && selectedDates.size() > 2) {
                throw new IllegalArgumentException(
                        "RANGE mode only allows two selectedDates.  You tried to pass " + selectedDates.size());
            }
            if (selectedDates != null) {
                for (Date date : selectedDates) {
                    selectDate(date);
                }
            }
            scrollToSelectedDates();

            validateAndUpdate();
            return this;
        }

        public FluentInitializer withHighlightedDates(Collection<Date> dates) {
            highlightDates(dates);
            return this;
        }

        public FluentInitializer withHighlightedDate(Date date) {
            return withHighlightedDates(Collections.singletonList(date));
        }

        @SuppressLint("SimpleDateFormat")
        public FluentInitializer setShortWeekdays(String[] newShortWeekdays) {
            DateFormatSymbols symbols = new DateFormatSymbols(locale);
            symbols.setShortWeekdays(newShortWeekdays);
            weekdayNameFormat =
                    new SimpleDateFormat(getContext().getString(R.string.day_name_format), symbols);
            return this;
        }


        public FluentInitializer displayOnly() {
            displayOnly = true;
            return this;
        }
    }

    private void validateAndUpdate() {
        if (getAdapter() == null) {
            setAdapter(adapter);
        }
        adapter.notifyDataSetChanged();
    }

    private void scrollToSelectedMonth(final int selectedIndex) {
        scrollToSelectedMonth(selectedIndex, false);
    }

    private void scrollToSelectedMonth(final int selectedIndex, final boolean smoothScroll) {
        post(new Runnable() {
            @Override
            public void run() {
                Log.d("Scroll to position %d", selectedIndex + "");

                if (smoothScroll) {
                    smoothScrollToPosition(selectedIndex);
                } else {
                    setSelection(selectedIndex);
                }
            }
        });
    }

    private void scrollToSelectedDates() {
        Integer selectedIndex = null;
        Integer todayIndex = null;
        Calendar today = Calendar.getInstance(locale);
        for (int c = 0; c < months.size(); c++) {
            MonthDescriptor month = months.get(c);
            if (selectedIndex == null) {
                for (Calendar selectedCal : selectedCals) {
                    if (sameMonth(selectedCal, month)) {
                        selectedIndex = c;
                        break;
                    }
                }
                if (selectedIndex == null && todayIndex == null && sameMonth(today, month)) {
                    todayIndex = c;
                }
            }
        }
        if (selectedIndex != null) {
            scrollToSelectedMonth(selectedIndex);
        } else if (todayIndex != null) {
            scrollToSelectedMonth(todayIndex);
        }
    }

    public boolean scrollToDate(Date date) {
        Integer selectedIndex = null;

        Calendar cal = Calendar.getInstance(locale);
        cal.setTime(date);
        for (int c = 0; c < months.size(); c++) {
            MonthDescriptor month = months.get(c);
            if (sameMonth(cal, month)) {
                selectedIndex = c;
                break;
            }
        }
        if (selectedIndex != null) {
            scrollToSelectedMonth(selectedIndex);
            return true;
        }
        return false;
    }


    public void fixDialogDimens() {
        Log.d("", "Fixing dimensions to h = %d / w = %d" + getMeasuredHeight() + "  " + getMeasuredWidth());
        // Fix the layout height/width after the dialog has been shown.
        getLayoutParams().height = getMeasuredHeight();
        getLayoutParams().width = getMeasuredWidth();
        // Post this runnable so it runs _after_ the dimen changes have been applied/re-measured.
        post(new Runnable() {
            @Override
            public void run() {
                Log.d("", "Dimens are fixed: now scroll to the selected date");
                scrollToSelectedDates();
            }
        });
    }


    public void setTitleTypeface(Typeface titleTypeface) {
        this.titleTypeface = titleTypeface;
        validateAndUpdate();
    }


    public void setDateTypeface(Typeface dateTypeface) {
        this.dateTypeface = dateTypeface;
        validateAndUpdate();
    }


    public void setTypeface(Typeface typeface) {
        setTitleTypeface(typeface);
        setDateTypeface(typeface);
    }


    public void unfixDialogDimens() {
        Log.d("", "Reset the fixed dimensions to allow for re-measurement");
        // Fix the layout height/width after the dialog has been shown.
        getLayoutParams().height = LayoutParams.MATCH_PARENT;
        getLayoutParams().width = LayoutParams.MATCH_PARENT;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (months.isEmpty()) {
            throw new IllegalStateException(
                    "Must have at least one month to display.  Did you forget to call init()?");
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public Date getSelectedDate() {
        return (selectedCals.size() > 0 ? selectedCals.get(0).getTime() : null);
    }

    public List<Date> getSelectedDates() {
        List<Date> selectedDates = new ArrayList<>();
        for (MonthCellDescriptor cal : selectedCells) {
            selectedDates.add(cal.date);
        }
        Collections.sort(selectedDates);
        return selectedDates;
    }

    public List<MonthCellDescriptor> getSelectedCells() {
        return selectedCells;
    }

    /**
     * Returns a string summarizing what the client sent us for init() params.
     */
    private static String dbg(Date minDate, Date maxDate) {
        return "minDate: " + minDate + "\nmaxDate: " + maxDate;
    }

    /**
     * Clears out the hours/minutes/seconds/millis of a Calendar.
     */
    static void setMidnight(Calendar cal) {
        cal.set(HOUR_OF_DAY, 0);
        cal.set(MINUTE, 0);
        cal.set(SECOND, 0);
        cal.set(MILLISECOND, 0);
    }

    private class CellClickedListener implements MonthViews.Listener {
        @Override
        public void handleClick(MonthCellDescriptor cell) {
            Date clickedDate = cell.date;

            if (cellClickInterceptor != null && cellClickInterceptor.onCellClicked(clickedDate, cell)) {
                return;
            }
            if (!betweenDates(clickedDate, minCal, maxCal) || !isDateSelectable(clickedDate) || !cell.isSelectable) {
                if (invalidDateListener != null) {
                    invalidDateListener.onInvalidDateSelected(clickedDate);
                }
            } else {
                boolean wasSelected = doSelectDate(clickedDate, cell);
                if (dateListener != null) {
                    if (wasSelected) {
                        dateListener.onDateSelected(clickedDate, cell);
                    } else {
                        dateListener.onDateUnselected(clickedDate);
                    }
                }
            }
        }
    }


    public boolean selectDate(Date date) {
        return selectDate(date, false);
    }


    public boolean selectDate(Date date, boolean smoothScroll) {
        validateDate(date);

        MonthCellWithMonthIndex monthCellWithMonthIndex = getMonthCellWithIndexByDate(date);
        if (monthCellWithMonthIndex == null || !isDateSelectable(date)) {
            return false;
        }
        boolean wasSelected = doSelectDate(date, monthCellWithMonthIndex.cell);
        if (wasSelected) {
            scrollToSelectedMonth(monthCellWithMonthIndex.monthIndex, smoothScroll);
        }
        return wasSelected;
    }

    private void validateDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Selected date must be non-null.");
        }
        if (date.before(minCal.getTime()) || date.after(maxCal.getTime())) {
            throw new IllegalArgumentException(String.format(
                    "SelectedDate must be between minDate and maxDate."
                            + "%nminDate: %s%nmaxDate: %s%nselectedDate: %s", minCal.getTime(), maxCal.getTime(),
                    date));
        }
    }

    private boolean doSelectDate(Date date, MonthCellDescriptor cell) {
        Calendar newlySelectedCal = Calendar.getInstance(locale);
        newlySelectedCal.setTime(date);
        // Sanitize input: clear out the hours/minutes/seconds/millis.
        setMidnight(newlySelectedCal);

        // Clear any remaining range state.
        for (MonthCellDescriptor selectedCell : selectedCells) {
            selectedCell.rangeState = RangeState.NONE;
        }

        switch (selectionMode) {
            case RANGE:
                if (selectedCals.size() > 1) {
                    clearOldSelections();
                } else if (selectedCals.size() == 1 && newlySelectedCal.before(selectedCals.get(0))) {
                    clearOldSelections();
                }
                break;

            case MULTIPLE:
                date = applyMultiSelect(date, newlySelectedCal);
                break;

            case SINGLE:
                clearOldSelections();
                break;

            case TWICE:
                if (selectedCals.size() > 1) {
                    clearOldSelections();
                }
                break;
            default:
                throw new IllegalStateException("Unknown selectionMode " + selectionMode);
        }

        if (date != null) {
            // Select a new cell.
            if (selectedCells.size() == 0 || !selectedCells.get(0).equals(cell)) {
                selectedCells.add(cell);
                cell.isSelected = true;
            }
            selectedCals.add(newlySelectedCal);

            if (selectionMode == SelectionMode.RANGE && selectedCells.size() > 1) {
                Date start = selectedCells.get(0).date;
                Date end = selectedCells.get(1).date;
                selectedCells.get(0).rangeState = MonthCellDescriptor.RangeState.FIRST;
                selectedCells.get(1).rangeState = MonthCellDescriptor.RangeState.LAST;

                for (List<List<MonthCellDescriptor>> month : cells) {
                    for (List<MonthCellDescriptor> week : month) {
                        for (MonthCellDescriptor singleCell : week) {
                            if (singleCell.date.after(start)
                                    && singleCell.date.before(end)
                                    && singleCell.isSelectable) {
                                singleCell.isSelected = true;
                                singleCell.rangeState = MonthCellDescriptor.RangeState.MIDDLE;
                                selectedCells.add(singleCell);
                            }
                        }
                    }
                }
            }
        }

        validateAndUpdate();
        return date != null;
    }

    public void clearSelections() {
        clearOldSelections();
        validateAndUpdate();
    }

    private void clearOldSelections() {
        for (MonthCellDescriptor selectedCell : selectedCells) {
            selectedCell.isSelected = false;

            if (dateListener != null) {
                Date selectedDate = selectedCell.date;

                if (selectionMode == SelectionMode.RANGE) {
                    int index = selectedCells.indexOf(selectedCell);
                    if (index == 0 || index == selectedCells.size() - 1) {
                        dateListener.onDateUnselected(selectedDate);
                    }
                } else {
                    dateListener.onDateUnselected(selectedDate);
                }
            }
        }
        selectedCells.clear();
        selectedCals.clear();
    }

    private Date applyMultiSelect(Date date, Calendar selectedCal) {
        for (MonthCellDescriptor selectedCell : selectedCells) {
            if (selectedCell.date.equals(date)) {
                selectedCell.isSelected = false;
                selectedCells.remove(selectedCell);
                date = null;
                break;
            }
        }
        for (Calendar cal : selectedCals) {
            if (sameDate(cal, selectedCal)) {
                selectedCals.remove(cal);
                break;
            }
        }
        return date;
    }

    public void highlightDates(Collection<Date> dates) {
        for (Date date : dates) {
            validateDate(date);

            MonthCellWithMonthIndex monthCellWithMonthIndex = getMonthCellWithIndexByDate(date);
            if (monthCellWithMonthIndex != null) {
                Calendar newlyHighlightedCal = Calendar.getInstance();
                newlyHighlightedCal.setTime(date);
                MonthCellDescriptor cell = monthCellWithMonthIndex.cell;

                highlightedCells.add(cell);
                highlightedCals.add(newlyHighlightedCal);
                cell.isHighlighted = true;
            }
        }

        validateAndUpdate();
    }

    public void clearHighlightedDates() {
        for (MonthCellDescriptor cal : highlightedCells) {
            cal.isHighlighted = false;
        }
        highlightedCells.clear();
        highlightedCals.clear();

        validateAndUpdate();
    }

    /**
     * Hold a cell with a month-index.
     */
    private static class MonthCellWithMonthIndex {
        public MonthCellDescriptor cell;
        public int monthIndex;

        public MonthCellWithMonthIndex(MonthCellDescriptor cell, int monthIndex) {
            this.cell = cell;
            this.monthIndex = monthIndex;
        }
    }

    private MonthCellWithMonthIndex getMonthCellWithIndexByDate(Date date) {
        int index = 0;
        Calendar searchCal = Calendar.getInstance(locale);
        searchCal.setTime(date);
        Calendar actCal = Calendar.getInstance(locale);

        for (List<List<MonthCellDescriptor>> monthCells : cells) {
            for (List<MonthCellDescriptor> weekCells : monthCells) {
                for (MonthCellDescriptor actCell : weekCells) {
                    actCal.setTime(actCell.date);
                    if (sameDate(actCal, searchCal) && actCell.isSelectable) {
                        return new MonthCellWithMonthIndex(actCell, index);
                    }
                }
            }
            index++;
        }
        return null;
    }

    protected class MonthAdapter extends BaseAdapter {
        private final LayoutInflater inflater;

        protected MonthAdapter() {
            inflater = LayoutInflater.from(getContext());
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public int getCount() {
            return months.size();
        }

        @Override
        public Object getItem(int position) {
            return months.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MonthViews monthView = (MonthViews) convertView;
            if (monthView == null
                    || !monthView.getTag(R.id.day_view_adapter_class).equals(dayViewAdapter.getClass())) {
                int resId = getMonthViewResId();
                monthView =
                        MonthViews.create(parent, inflater, resId, weekdayNameFormat, listener, today, dividerColor,
                                dayBackgroundResId, dayTextColorResId, titleTextColor, displayHeader,
                                headerTextColor, decorators, locale, dayViewAdapter);
                monthView.setTag(R.id.day_view_adapter_class, dayViewAdapter.getClass());
            } else {
                monthView.setDecorators(decorators);
            }
            monthView.init(months.get(position), cells.get(position), displayOnly, Typeface.createFromAsset(context.getAssets(), "fonts/GoogleSans-Bold.ttf"),
                    Typeface.createFromAsset(context.getAssets(), "fonts/GoogleSans-Regular.ttf"));
            return monthView;
        }

        protected int getMonthViewResId() {
            return R.layout.month;
        }
    }

    List<List<MonthCellDescriptor>> getMonthCells(MonthDescriptor month, Calendar startCal) {
        Calendar cal = Calendar.getInstance(locale);
        cal.setTime(startCal.getTime());
        List<List<MonthCellDescriptor>> cells = new ArrayList<>();
        cal.set(DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(DAY_OF_WEEK);
        int offset = cal.getFirstDayOfWeek() - firstDayOfWeek;
        if (offset > 0) {
            offset -= 7;
        }
        cal.add(Calendar.DATE, offset);

        Calendar minSelectedCal = minDate(selectedCals);
        Calendar maxSelectedCal = maxDate(selectedCals);

        while ((cal.get(MONTH) < month.month + 1 || cal.get(YEAR) < month.year) //
                && cal.get(YEAR) <= month.year) {
            Log.d("", "Building week row starting at %s" + cal.getTime() + "");
            List<MonthCellDescriptor> weekCells = new ArrayList<>();
            cells.add(weekCells);
            for (int c = 0; c < 7; c++) {
                Date date = cal.getTime();
                boolean isCurrentMonth = cal.get(MONTH) == month.month;
                boolean isSelected = isCurrentMonth && containsDate(selectedCals, cal);
                boolean isSelectable =
                        isCurrentMonth && betweenDates(cal, minCal, maxCal) && isDateSelectable(date);
                boolean isToday = sameDate(cal, today);
                boolean isHighlighted = containsDate(highlightedCals, cal);
                int value = cal.get(DAY_OF_MONTH);

                MonthCellDescriptor.RangeState rangeState = MonthCellDescriptor.RangeState.NONE;
                if (selectedCals.size() > 1) {
                    if (sameDate(minSelectedCal, cal)) {
                        rangeState = MonthCellDescriptor.RangeState.FIRST;
                    } else if (sameDate(maxDate(selectedCals), cal)) {
                        rangeState = MonthCellDescriptor.RangeState.LAST;
                    } else if (betweenDates(cal, minSelectedCal, maxSelectedCal)) {
                        rangeState = MonthCellDescriptor.RangeState.MIDDLE;
                    }
                }

                weekCells.add(getWeekCell(date, isCurrentMonth, isSelectable, isSelected, isToday,
                        isHighlighted, value, rangeState));
                cal.add(DATE, 1);
            }
        }
        return cells;
    }

    protected MonthCellDescriptor getWeekCell(Date date, boolean isCurrentMonth, boolean isSelectable, boolean isSelected,
                                              boolean isToday, boolean isHighlighted, int value, MonthCellDescriptor.RangeState rangeState) {
        return new MonthCellDescriptor(date, isCurrentMonth, isSelectable, isSelected, isToday,
                isHighlighted, value, rangeState);
    }

    private boolean containsDate(List<Calendar> selectedCals, Date date) {
        Calendar cal = Calendar.getInstance(locale);
        cal.setTime(date);
        return containsDate(selectedCals, cal);
    }

    private static boolean containsDate(List<Calendar> selectedCals, Calendar cal) {
        for (Calendar selectedCal : selectedCals) {
            if (sameDate(cal, selectedCal)) {
                return true;
            }
        }
        return false;
    }

    private static Calendar minDate(List<Calendar> selectedCals) {
        if (selectedCals == null || selectedCals.size() == 0) {
            return null;
        }
        Collections.sort(selectedCals);
        return selectedCals.get(0);
    }

    private static Calendar maxDate(List<Calendar> selectedCals) {
        if (selectedCals == null || selectedCals.size() == 0) {
            return null;
        }
        Collections.sort(selectedCals);
        return selectedCals.get(selectedCals.size() - 1);
    }

    private static boolean sameDate(Calendar cal, Calendar selectedDate) {
        return cal.get(MONTH) == selectedDate.get(MONTH)
                && cal.get(YEAR) == selectedDate.get(YEAR)
                && cal.get(DAY_OF_MONTH) == selectedDate.get(DAY_OF_MONTH);
    }

    private static boolean betweenDates(Calendar cal, Calendar minCal, Calendar maxCal) {
        final Date date = cal.getTime();
        return betweenDates(date, minCal, maxCal);
    }

    static boolean betweenDates(Date date, Calendar minCal, Calendar maxCal) {
        final Date min = minCal.getTime();
        return (date.equals(min) || date.after(min)) // >= minCal
                && date.before(maxCal.getTime()); // && < maxCal
    }

    private static boolean sameMonth(Calendar cal, MonthDescriptor month) {
        return (cal.get(MONTH) == month.month && cal.get(YEAR) == month.year);
    }

    private boolean isDateSelectable(Date date) {
        return dateConfiguredListener == null || dateConfiguredListener.isDateSelectable(date);
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        dateListener = listener;
    }

    public void setOnInvalidDateSelectedListener(OnInvalidDateSelectedListener listener) {
        invalidDateListener = listener;
    }

    /**
     * Set a listener used to discriminate between selectable and unselectable dates. Set this to
     * disable arbitrary dates as they are rendered.
     * <p/>
     * Important: set this before you call {@link #init(Date, Date)} methods.  If called afterwards,
     * it will not be consistently applied.
     */
    public void setDateSelectableFilter(DateSelectableFilter listener) {
        dateConfiguredListener = listener;
    }


    public void setCustomDayView(DayViewAdapter dayViewAdapter) {
        this.dayViewAdapter = dayViewAdapter;
        if (null != adapter) {
            adapter.notifyDataSetChanged();
        }
    }

    public void setCellClickInterceptor(CellClickInterceptor listener) {
        cellClickInterceptor = listener;
    }

    public interface OnDateSelectedListener {
        void onDateSelected(Date date, MonthCellDescriptor cell);

        void onDateUnselected(Date date);
    }
    public interface OnInvalidDateSelectedListener {
        void onInvalidDateSelected(Date date);
    }

    /**
     * Interface used for determining the selectability of a date cell when it is configured for
     * display on the calendar.
     *
     * @see #setDateSelectableFilter(DateSelectableFilter)
     */
    public interface DateSelectableFilter {
        boolean isDateSelectable(Date date);
    }

    /**
     * Interface to be notified when a cell is clicked and possibly intercept the click.  Return true
     * to intercept the click and prevent any selections from changing.
     *
     * @see #setCellClickInterceptor(CellClickInterceptor)
     */
    public interface CellClickInterceptor {
        boolean onCellClicked(Date date, MonthCellDescriptor cell);
    }

    private class DefaultOnInvalidDateSelectedListener implements OnInvalidDateSelectedListener {
        @Override
        public void onInvalidDateSelected(Date date) {
//            AppUtils.showToast(getContext(), R.string.calendar_date_can_not_be_selected);
        }
    }
}

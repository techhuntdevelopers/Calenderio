package com.yumi.calendar.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yumi.calendar.R;
import com.yumi.calendar.decorator.CalendarCellDecorator;
import com.yumi.calendar.decorator.DayViewAdapter;
import com.yumi.calendar.descriptor.MonthCellDescriptor;
import com.yumi.calendar.descriptor.MonthDescriptor;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by TechHunt Developers on 2019-11-22.
 */
public class MonthViews extends LinearLayout {
    TextView title;
    CalendarGridViews grid;
    private Listener listener;
    private List<CalendarCellDecorator> decorators;
    private boolean isRtl;
    private Locale locale;
    private NumberFormat numberFormatter;
    private boolean displayOnly;

    public MonthViews(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public static MonthViews create(ViewGroup parent, LayoutInflater inflater, @LayoutRes int resId,
                                    DateFormat weekdayNameFormat, Listener listener, Calendar today, int dividerColor,
                                    int dayBackgroundResId, int dayTextColorResId, int titleTextColor, boolean displayHeader,
                                    int headerTextColor, Locale locale, DayViewAdapter adapter) {
        return create(parent, inflater, resId, weekdayNameFormat, listener, today, dividerColor,
                dayBackgroundResId, dayTextColorResId, titleTextColor, displayHeader, headerTextColor, null,
                locale, adapter);
    }

    public static MonthViews create(ViewGroup parent, LayoutInflater inflater, @LayoutRes int resId,
                                    DateFormat weekdayNameFormat, Listener listener, Calendar today, int dividerColor,
                                    int dayBackgroundResId, int dayTextColorResId, int titleTextColor, boolean displayHeader,
                                    int headerTextColor, List<CalendarCellDecorator> decorators, Locale locale,
                                    DayViewAdapter adapter) {
        final MonthViews view = (MonthViews) inflater.inflate(resId, parent, false);

        view.setDayViewAdapter(adapter);
        view.setDividerColor(dividerColor);
        view.setDayTextColor(dayTextColorResId);
        view.setTitleTextColor(titleTextColor);
        view.setDisplayHeader(displayHeader);
        view.setHeaderTextColor(headerTextColor);

        if (dayBackgroundResId != 0) {
            view.setDayBackground(dayBackgroundResId);
        }

        final int originalDayOfWeek = today.get(Calendar.DAY_OF_WEEK);

        view.isRtl = isRtl(locale);
        view.locale = locale;
        int firstDayOfWeek = today.getFirstDayOfWeek();
        final CalendarRowViews headerRow = (CalendarRowViews) view.grid.getChildAt(0);
        for (int offset = 0; offset < 7; offset++) {
            today.set(Calendar.DAY_OF_WEEK, getDayOfWeek(firstDayOfWeek, offset, view.isRtl));
            final TextView textView = (TextView) headerRow.getChildAt(offset);
            textView.setText(weekdayNameFormat.format(today.getTime()));
        }
        today.set(Calendar.DAY_OF_WEEK, originalDayOfWeek);
        view.listener = listener;
        view.decorators = decorators;
        return view;
    }

    private static int getDayOfWeek(int firstDayOfWeek, int offset, boolean isRtl) {
        int dayOfWeek = firstDayOfWeek + offset;
        if (isRtl) {
            return 8 - dayOfWeek;
        }
        return dayOfWeek;
    }

    private static boolean isRtl(Locale locale) {
        // TODO convert the build to gradle and use getLayoutDirection instead of this (on 17+)?
        final int directionality = Character.getDirectionality(locale.getDisplayName(locale).charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT
                || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    public MonthViews getMonthView(LayoutInflater inflater, ViewGroup parent) {
        return (MonthViews) inflater.inflate(R.layout.month, parent, false);
    }

    public List<CalendarCellDecorator> getDecorators() {
        return decorators;
    }

    public void setDecorators(List<CalendarCellDecorator> decorators) {
        this.decorators = decorators;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        title = (TextView) findViewById(R.id.title);
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/GoogleSans-Bold.ttf");
        title.setTypeface(font);
        grid = (CalendarGridViews) findViewById(R.id.calendar_grid);
    }

    public void init(MonthDescriptor month, List<List<MonthCellDescriptor>> cells,
                     boolean displayOnly, Typeface titleTypeface, Typeface dateTypeface) {
        Log.d("Initializing MonthView", "");
        long start = System.currentTimeMillis();
        title.setText(month.label);
        numberFormatter = NumberFormat.getInstance(locale);
        this.displayOnly = displayOnly;

        final int numRows = cells.size();
        grid.setNumRows(numRows);
        for (int i = 0; i < 6; i++) {
            CalendarRowViews weekRow = (CalendarRowViews) grid.getChildAt(i + 1);
            weekRow.setListener(listener);
            if (i < numRows) {
                weekRow.setVisibility(VISIBLE);
                List<MonthCellDescriptor> week = cells.get(i);
                for (int c = 0; c < week.size(); c++) {
                    MonthCellDescriptor cell = week.get(isRtl ? 6 - c : c);
                    CalendarCellViews cellView = (CalendarCellViews) weekRow.getChildAt(c);

                    initCellView(cellView, cell);

                    if (null != decorators) {
                        for (CalendarCellDecorator decorator : decorators) {
                            decorator.decorate(cellView, cell.date);
                        }
                    }
                }
            } else {
                weekRow.setVisibility(GONE);
            }
        }

        if (titleTypeface != null) {
            title.setTypeface(titleTypeface);
        }
        if (dateTypeface != null) {
            grid.setTypeface(dateTypeface);
        }

        Log.d("MonthView.init", "MonthView.init took %d ms" + (System.currentTimeMillis() - start));
    }

    public void initCellView(CalendarCellViews cellView, MonthCellDescriptor cell) {
        String cellDate = numberFormatter.format(cell.value);
        cellView.setEnabled(cell.isCurrentMonth);
        cellView.setClickable(!displayOnly);
        cellView.setSelectable(cell.isSelectable);
        cellView.setSelected(cell.isSelected);
        cellView.setCurrentMonth(cell.isCurrentMonth);
        cellView.setToday(cell.isToday);
        cellView.setRangeState(cell.rangeState);
        cellView.setHighlighted(cell.isHighlighted);
        cellView.setTag(cell);
        if (!cellView.getDayOfMonthTextView().getText().equals(cellDate)) {
            cellView.getDayOfMonthTextView().setText(cellDate);
        }
        if (cell.isToday) {
//            cellView.getDayOfMonthTextView().setText(getContext().getString(R.string.calendar_today));
        }
        if (cell.isSelectable) {
            cellView.setClickable(true);
        } else {
            cellView.setClickable(false);
        }
    }

    public void setDividerColor(int color) {
        grid.setDividerColor(color);
    }

    public void setDayBackground(int resId) {
        grid.setDayBackground(resId);
    }

    public void setDayTextColor(int resId) {
        grid.setDayTextColor(resId);
    }

    public void setDayViewAdapter(DayViewAdapter adapter) {
        grid.setDayViewAdapter(adapter);
    }

    public void setTitleTextColor(int color) {
        title.setTextColor(color);
    }

    public void setDisplayHeader(boolean displayHeader) {
        grid.setDisplayHeader(displayHeader);
    }

    public void setHeaderTextColor(int color) {
        grid.setHeaderTextColor(color);
    }

    public interface Listener {
        void handleClick(MonthCellDescriptor cell);
    }
}

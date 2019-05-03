package com.yumi.calendar.adapter;

import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.widget.TextView;

import com.yumi.calendar.R;
import com.yumi.calendar.decorator.DayViewAdapter;
import com.yumi.calendar.view.CalendarCellViews;

/**
 * Created by TechHunt Developers on 2019-11-22.
 */
public class DefaultDayViewAdapter implements DayViewAdapter {

  @Override
  public void makeCellView(CalendarCellViews parent) {
      TextView textView = new TextView(
              new ContextThemeWrapper(parent.getContext(), R.style.CalendarCell_CalendarDate));
      textView.setDuplicateParentStateEnabled(true);
      parent.setGravity(Gravity.CENTER);
      parent.addView(textView);
      parent.setDayOfMonthTextView(textView);
  }
}

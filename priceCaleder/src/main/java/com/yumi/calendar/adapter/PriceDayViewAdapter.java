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
public class PriceDayViewAdapter implements DayViewAdapter {

  @Override
  public void makeCellView(CalendarCellViews parent) {
      TextView textView = new TextView(
              new ContextThemeWrapper(parent.getContext(), R.style.CalendarCell_CalendarDate));
      textView.setDuplicateParentStateEnabled(true);
      TextView tvPrice = new TextView(
              new ContextThemeWrapper(parent.getContext(), R.style.CalendarCell_CalendarPrice));
      tvPrice.setDuplicateParentStateEnabled(true);
      parent.setGravity(Gravity.CENTER);
      parent.addView(textView);
      parent.addView(tvPrice);
      parent.setDayOfMonthTextView(textView);
      parent.setPriceTextView(tvPrice);
  }
}

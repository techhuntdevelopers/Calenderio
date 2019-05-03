package com.yumi.calendar.decorator;

import com.yumi.calendar.view.CalendarCellViews;

import java.util.Date;

/**
 * Created by TechHunt Developers on 2019-11-22.
 */
public interface CalendarCellDecorator {
  void decorate(CalendarCellViews cellView, Date date);
}

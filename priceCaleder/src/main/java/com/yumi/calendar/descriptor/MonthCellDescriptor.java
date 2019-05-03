// Copyright 2012 Square, Inc.

package com.yumi.calendar.descriptor;


import com.yumi.calendar.view.MonthViews;

import java.util.Date;

/**
 * Created by TechHunt Developers on 2019-11-22.
 */
public class MonthCellDescriptor {
  public enum RangeState {
    NONE, FIRST, MIDDLE, LAST
  }

  /**
   * 日期
   */
  public Date date;
  public int value;
  /**
   * 是否是当月
   */
  public boolean isCurrentMonth;
  /**
   * 是否被选中
   */
  public boolean isSelected;
  /**
   * 是否是今天
   */
  public boolean isToday;
  /**
   * 是否可选
   */
  public boolean isSelectable;
  /**
   * 是否高亮
   */
  public boolean isHighlighted;
  /**
   * 日期区间
   */
  public RangeState rangeState;

  public MonthCellDescriptor(Date date, boolean currentMonth, boolean selectable, boolean selected,
                             boolean today, boolean highlighted, int value, RangeState rangeState) {
    this.date = date;
    isCurrentMonth = currentMonth;
    isSelectable = selectable;
    isHighlighted = highlighted;
    isSelected = selected;
    isToday = today;
    this.value = value;
    this.rangeState = rangeState;
  }

  @Override
  public String toString() {
    return "MonthCellDescriptor{"
        + "date="
        + date
        + ", value="
        + value
        + ", isCurrentMonth="
        + isCurrentMonth
        + ", isSelected="
        + isSelected
        + ", isToday="
        + isToday
        + ", isSelectable="
        + isSelectable
        + ", isHighlighted="
        + isHighlighted
        + ", rangeState="
        + rangeState
        + '}';
  }
}

package com.yumi.calendar.descriptor;

import java.util.Date;

/**
 * Created by TechHunt Developers on 2019-11-22.
 */
public class MonthDescriptor {
  public final int month;
  public final int year;
  public final Date date;
  /**
   * 月份title展示label
   */
  public String label;

  public MonthDescriptor(int month, int year, Date date, String label) {
    this.month = month;
    this.year = year;
    this.date = date;
    this.label = label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return "MonthDescriptor{"
        + "label='"
        + label
        + '\''
        + ", month="
        + month
        + ", year="
        + year
        + '}';
  }
}

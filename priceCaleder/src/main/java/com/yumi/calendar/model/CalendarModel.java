package com.yumi.calendar.model;

import java.util.Date;

/**
 * Created by TechHunt Developers on 2019-11-22.
 */
public class CalendarModel {
    public String mode;
    public Date minDate;
    public Date maxDate;

    public CalendarModel(){

    }

    public CalendarModel(String mode, Date minDate, Date maxDate) {
        this.mode = mode;
        this.minDate = minDate;
        this.maxDate = maxDate;
    }
}

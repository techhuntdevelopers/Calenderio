package com.yumi.calendar.model;

import java.util.Date;
import java.util.List;

/**
 * Created by TechHunt Developers on 2019-11-22.
 */
public class CalenderResultData {
    public List<Date> dates;

    public CalenderResultData(List<Date> dates) {
        this.dates = dates;
    }

    public CalenderResultData() {
    }
}

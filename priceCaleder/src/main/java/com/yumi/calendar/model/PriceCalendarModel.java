package com.yumi.calendar.model;

import java.util.Date;
import java.util.List;

/**
 * Created by TechHunt Developers on 2019-11-22.
 */
public class PriceCalendarModel {
    public String mode;
    public List<PriceModel> priceList;
    public Date minDate;
    public Date maxDate;

    public PriceCalendarModel() {

    }

    public PriceCalendarModel(String mode, List<PriceModel> priceList) {
        this.mode = mode;
        this.priceList = priceList;
        this.minDate = minDate;
        this.maxDate = maxDate;

    }
}

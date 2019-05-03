package com.yumi.calendar.model;

import java.util.Date;
import java.util.List;

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
    }
}

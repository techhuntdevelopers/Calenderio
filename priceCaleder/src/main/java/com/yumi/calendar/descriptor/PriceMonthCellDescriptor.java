package com.yumi.calendar.descriptor;

import com.yumi.calendar.PricePickerViews;

import java.util.Date;

/**
 * Created by TechHunt Developers on 2019-11-22.
 */
public class PriceMonthCellDescriptor extends MonthCellDescriptor {
    /**
     * 当天是否有价格信息
     */
    public final boolean hasPrice;

    /**
     * 当天价格信息
     */
    public final int price;

    public PriceMonthCellDescriptor(Date date, boolean currentMonth, boolean selectable, boolean selected, boolean today, boolean highlighted, int value, RangeState rangeState, boolean hasPrice, int price) {
        super(date, currentMonth, selectable, selected, today, highlighted, value, rangeState);
        this.price = price;
        this.hasPrice = (price > 0);
    }
}

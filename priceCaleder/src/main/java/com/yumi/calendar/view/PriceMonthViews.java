package com.yumi.calendar.view;

import android.content.Context;
import android.util.AttributeSet;

import com.yumi.calendar.descriptor.MonthCellDescriptor;
import com.yumi.calendar.descriptor.PriceMonthCellDescriptor;

/**
 * Created by TechHunt Developers on 2019-11-22.
 */
public class PriceMonthViews extends MonthViews {
    public PriceMonthViews(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initCellView(CalendarCellViews cellView, MonthCellDescriptor cell) {
        super.initCellView(cellView, cell);
        if (cell instanceof PriceMonthCellDescriptor) {
            PriceMonthCellDescriptor c = (PriceMonthCellDescriptor) cell;
            if (c.hasPrice) {
                cellView.getPriceTextView().setText("" + c.price);
            } else {
                c.isSelectable = false;
                cellView.setSelectable(false);
                cellView.setClickable(false);
                cellView.getPriceTextView().setText("");
            }
        }

    }
}

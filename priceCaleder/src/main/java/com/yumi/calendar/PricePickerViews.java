package com.yumi.calendar;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.yumi.calendar.descriptor.MonthCellDescriptor;
import com.yumi.calendar.descriptor.PriceMonthCellDescriptor;
import com.yumi.calendar.model.PriceModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by TechHunt Developers on 2019-11-22.
 */
public class PricePickerViews extends CalendarPickerViews {

    private ArrayMap<Date, PriceModel> maps;

    public PricePickerViews(Context context, ArrayMap<Date, PriceModel> maps) {
        super(context, true);
        this.maps = maps;
    }

    public PricePickerViews(Context context, ArrayMap<Date, PriceModel> maps, boolean displayHeader) {
        super(context, displayHeader);
        this.maps = maps;
    }


    @Override
    public MonthAdapter getMonthAdapter() {
        return new PriceMonthAdapter();
    }

    @Override
    protected MonthCellDescriptor getWeekCell(Date date, boolean isCurrentMonth, boolean isSelectable, boolean isSelected, boolean isToday, boolean isHighlighted, int value, MonthCellDescriptor.RangeState rangeState) {
        PriceModel model = new PriceModel();
        if (maps != null) {
            model = maps.get(date);
        }
        return new PriceMonthCellDescriptor(date, isCurrentMonth, isSelectable, isSelected,
                isToday, isHighlighted, value, rangeState, model != null && model.hasPrice, model == null ? -1 : model.price);
    }

    public List<PriceMonthCellDescriptor> getSelectedPriceCells() {
        List<PriceMonthCellDescriptor> cells = new ArrayList<>();
        for (MonthCellDescriptor descriptor : getSelectedCells()) {
            if (descriptor instanceof PriceMonthCellDescriptor) {
                cells.add((PriceMonthCellDescriptor) descriptor);
            }
        }
        return cells;
    }

    private class PriceMonthAdapter extends MonthAdapter {

        private PriceMonthAdapter() {
            super();
        }

        @Override
        protected int getMonthViewResId() {
            return R.layout.price_month;
        }
    }
}

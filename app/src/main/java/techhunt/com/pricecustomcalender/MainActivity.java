package techhunt.com.pricecustomcalender;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.yumi.calendar.CalendarPickerViews;
import com.yumi.calendar.PricePickerViews;
import com.yumi.calendar.adapter.DefaultDayViewAdapter;
import com.yumi.calendar.adapter.PriceDayViewAdapter;
import com.yumi.calendar.decorator.CalendarCellDecorator;
import com.yumi.calendar.model.PriceCalendarModel;
import com.yumi.calendar.model.PriceModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String SELECTION_MODE_SINGLE = "single";
    public static final String SELECTION_MODE_MULTIPLE = "multiple";
    public static final String SELECTION_MODE_RANGE = "range";
    public static final String SELECTION_MODE_TWICE = "twice";
    public static final String VIEW_DAY = "dayView";
    public static final String VIEW_PRICE = "priceView";

    private PricePickerViews pricePickerView;
    private CalendarPickerViews pickerView;
    private BottomSheetDialog dialog = null;

    private Button price;
    private Button single;
    private Button multiple;
    private Button range;
    private CalendarPickerViews.SelectionMode selectionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.price:
                calenderChooser(SELECTION_MODE_SINGLE, VIEW_PRICE);
                break;
            case R.id.single:
                calenderChooser(SELECTION_MODE_SINGLE, VIEW_DAY);
                break;
            case R.id.multiple:
                calenderChooser(SELECTION_MODE_MULTIPLE, VIEW_DAY);
                break;
            case R.id.range:
                calenderChooser(SELECTION_MODE_RANGE, VIEW_DAY);
                break;
        }
    }

    private void calenderChooser(String mode, String type) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.sheet, null);
        dialog = new BottomSheetDialog(this);

        dialog.setCancelable(false);
        dialog.setContentView(view);
        LinearLayout main = view.findViewById(R.id.main);
        Button doneBtn = view.findViewById(R.id.doneBtn);
        Button closeBtn = view.findViewById(R.id.closeBtn);

        if (type.equals(VIEW_PRICE)) {
            final Calendar calendar = Calendar.getInstance();
            GregorianCalendar gcal = new GregorianCalendar(calendar.get(Calendar.YEAR) + 1, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            Date date = gcal.getTime();
            PriceCalendarModel priceCalendarModel = getPriceCalendarModel();

            pricePickerView = new PricePickerViews(getApplicationContext(),
                    getPriceModelMaps(priceCalendarModel.priceList));
            pricePickerView.setCustomDayView(new PriceDayViewAdapter());
            pricePickerView.setDecorators(Collections.<CalendarCellDecorator>emptyList());
            pricePickerView.init(calendar.getTime(), date)
                    .withSelectedDate(calendar.getTime())
                    .inMode(getSelectionMode(mode));

            main.addView(pricePickerView);

            doneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Date date = pricePickerView.getSelectedDate();
                    List<Date> dateList = pricePickerView.getSelectedDates();
                    dialog.dismiss();
                }
            });
        } else if (type.equals(VIEW_DAY)) {
            Calendar calendar = Calendar.getInstance();
            Calendar nextYear = Calendar.getInstance();
            nextYear.add(Calendar.YEAR, 1);
            pickerView = new CalendarPickerViews(this, true);
            pickerView.setCustomDayView(new DefaultDayViewAdapter());
            pickerView.setDecorators(Collections.<CalendarCellDecorator>emptyList());
            pickerView.init(calendar.getTime(), nextYear.getTime())
                    .withSelectedDate(calendar.getTime())
                    .inMode(getSelectionMode(mode));
            main.addView(pickerView);

            doneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Date date = pricePickerView.getSelectedDate();
                    List<Date> dateList = pricePickerView.getSelectedDates();
                    dialog.dismiss();
                }
            });
        }

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (dialog != null) {
            if (!dialog.isShowing())
                dialog.show();
        }
    }

    private CalendarPickerViews.SelectionMode getSelectionMode(String mode) {
        if (mode.equals(SELECTION_MODE_MULTIPLE)) {
            selectionMode = CalendarPickerViews.SelectionMode.MULTIPLE;
        } else if (mode.equals(SELECTION_MODE_RANGE)) {
            selectionMode = CalendarPickerViews.SelectionMode.RANGE;
        } else if (mode.equals(SELECTION_MODE_TWICE)) {
            selectionMode = CalendarPickerViews.SelectionMode.TWICE;
        }
        return selectionMode;
    }

    private PriceCalendarModel getPriceCalendarModel() {
        List<PriceModel> list = new ArrayList<>();
        final Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 12; i++) {
            GregorianCalendar gcal = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) + i);
            Date date = gcal.getTime();
            list.add(new PriceModel(true, date, i + 500));
        }
        return new PriceCalendarModel("", list);
    }

    private void initView() {
        price = findViewById(R.id.price);
        single = findViewById(R.id.single);
        multiple = findViewById(R.id.multiple);
        range = findViewById(R.id.range);

        price.setOnClickListener(this);
        single.setOnClickListener(this);
        multiple.setOnClickListener(this);
        range.setOnClickListener(this);
    }

    private ArrayMap<Date, PriceModel> getPriceModelMaps(List<PriceModel> priceList) {
        ArrayMap<Date, PriceModel> maps = new ArrayMap<>();
        if (priceList == null || priceList.size() == 0) {
            return maps;
        }
        for (PriceModel model : priceList) {
            maps.put(model.date, model);
        }
        return maps;
    }
}

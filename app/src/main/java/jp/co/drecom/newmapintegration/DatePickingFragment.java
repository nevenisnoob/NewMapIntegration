package jp.co.drecom.newmapintegration;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.DateTimeKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.Date;
import java.util.GregorianCalendar;

import jp.co.drecom.newmapintegration.utils.NewLog;


/**
 * A simple {@link Fragment} subclass.
 */
//notice that never fix android.support.v4.app.Fragment with android.app.Fragment
public class DatePickingFragment extends Fragment implements View.OnClickListener {

    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private Button confirmBtn;
    private Button cancelBtn;
    private long startUnixTime;
    private long endUnixTime;

    OnDatePickListener datePickListener;

    public DatePickingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        datePickListener = (OnDatePickListener) activity;
    }

    //send data to Activity
    public interface OnDatePickListener {
        public void getDateData (long startTime, long endTime);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_date_picking, container, false);
        startDatePicker = (DatePicker) view.findViewById(R.id.start_date_picker);
        endDatePicker = (DatePicker) view.findViewById(R.id.end_date_picker);
        confirmBtn = (Button) view.findViewById(R.id.confirm_btn);
        cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        confirmBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_btn:
                getUnixTime();
                getFragmentManager().beginTransaction().remove(this).commit();
                break;
            case R.id.cancel_btn:
                getFragmentManager().beginTransaction().remove(this).commit();
                break;
        }
    }

    private void getUnixTime() {
        GregorianCalendar startTime = new GregorianCalendar(startDatePicker.getYear(),
                startDatePicker.getMonth(), startDatePicker.getDayOfMonth(),
                0, 0, 0);
        startUnixTime = startTime.getTimeInMillis() / 1000;

        GregorianCalendar endTime = new GregorianCalendar(endDatePicker.getYear(),
                endDatePicker.getMonth(), endDatePicker.getDayOfMonth(),
                23,59,59);
        endUnixTime = endTime.getTimeInMillis() / 1000;

        datePickListener.getDateData(startUnixTime, endUnixTime);

        NewLog.logD("the start time is " + startUnixTime + " and the end time is " + endUnixTime);
    }
}

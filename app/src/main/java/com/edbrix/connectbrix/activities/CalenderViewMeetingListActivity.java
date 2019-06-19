package com.edbrix.connectbrix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.edbrix.connectbrix.Application;
import com.edbrix.connectbrix.R;
import com.edbrix.connectbrix.adapters.SchoolListWithCalendarAdapter;
import com.edbrix.connectbrix.baseclass.BaseActivity;
import com.edbrix.connectbrix.data.GetMeetingsByMonthYearResposeData;
import com.edbrix.connectbrix.data.MeetingListData;
import com.edbrix.connectbrix.data.MyEventDay;
import com.edbrix.connectbrix.data.UserMeetingByDateParentData;
import com.edbrix.connectbrix.data.UserMeetingListResponseData;
import com.edbrix.connectbrix.utils.Constants;
import com.edbrix.connectbrix.utils.SessionManager;
import com.edbrix.connectbrix.volley.GsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalenderViewMeetingListActivity extends BaseActivity {

    private static final String TAG = CalenderViewMeetingListActivity.class.getName();
    private LinearLayout mLinearLayoutCal;
    private CalendarView mCalendarView;
    private ListView mMeetingListWithCalender;
    private TextView mTxtSelectedDate;
    private TextView txtDataFound;
    private FloatingActionButton mFloatingActionButtonFabWithListview;
    SchoolListWithCalendarAdapter schoolListWithCalendarAdapter;
    SessionManager sessionManager;
    ArrayList<String> daysForEvent = new ArrayList<>();
    private List<EventDay> mEventDays = new ArrayList<>();
    SimpleDateFormat finalSimpleDateFormat;
    private GetMeetingsByMonthYearResposeData meetingListData;
    private UserMeetingByDateParentData userMeetingByDateParentData;
    String dateForGetEvents;
    ArrayList<UserMeetingListResponseData> userMeetingListResponseData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender_view_meeting_list);
        getSupportActionBar().setTitle("Meeting List");

        sessionManager = new SessionManager(this);
        assignViews();
        meetingListData = new GetMeetingsByMonthYearResposeData();
        userMeetingListResponseData = new ArrayList<>();

        try {
            finalSimpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy");
            Date date = new Date();
            mTxtSelectedDate.setText(finalSimpleDateFormat.format(date));
            SimpleDateFormat tempSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date tempDate = null;
            Date getMonthYear = finalSimpleDateFormat.parse(mTxtSelectedDate.getText().toString());

            String getMonthYear_str_date = tempSimpleDateFormat.format(getMonthYear);
            String [] temp_date_str = getMonthYear_str_date.split("-");
            prepareMeetingByMonthYear(temp_date_str[0],temp_date_str[1]);


            tempDate = finalSimpleDateFormat.parse(mTxtSelectedDate.getText().toString());
            dateForGetEvents = tempSimpleDateFormat.format(tempDate);
            prepareMeetingListByDate(dateForGetEvents);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        /*SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        prepareMeetingListByDate(formatter.format(date));*/

        clickListner();
        // addEventToCalendar();

    }

    private void clickListner() {

        mMeetingListWithCalender.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CalenderViewMeetingListActivity.this, MeetingDetailsActivity.class);
                intent.putExtra("meetingDbId", userMeetingListResponseData.get(position).getId());
                intent.putExtra("IsHost", userMeetingListResponseData.get(position).getIsHost());
                intent.putExtra("IsCalenderActivity", "Y");
                startActivity(intent);
            }
        });

        mFloatingActionButtonFabWithListview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalenderViewMeetingListActivity.this, CreateMeetingActivity.class);
                intent.putExtra("comesFor", "new");
                startActivity(intent);
            }
        });

        mCalendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                try {
                    MyEventDay myEventDay = (MyEventDay) eventDay;
                    Calendar cal = myEventDay.getCalendar();
                    Date date = cal.getTime();
                    mTxtSelectedDate.setText(finalSimpleDateFormat.format(date));
                    SimpleDateFormat tempSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date tempDate = finalSimpleDateFormat.parse(mTxtSelectedDate.getText().toString());
                    dateForGetEvents = tempSimpleDateFormat.format(tempDate);
                    prepareMeetingListByDate(dateForGetEvents);

                } catch (Exception e) {
                    e.printStackTrace();
                    Calendar cal1 = mCalendarView.getSelectedDate();
                    Date date = cal1.getTime();
                    mTxtSelectedDate.setText(finalSimpleDateFormat.format(date));
                    /*SimpleDateFormat tempSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date tempDate = finalSimpleDateFormat.parse(mTxtSelectedDate.getText().toString());
                    dateForGetEvents = tempSimpleDateFormat.format(tempDate);*/
                    txtDataFound.setVisibility(View.VISIBLE);
                    mMeetingListWithCalender.setVisibility(View.GONE);
                }
            }
        });

        mCalendarView.setOnForwardPageChangeListener(new OnCalendarPageChangeListener() {
            @Override
            public void onChange() {
                Calendar calendar = mCalendarView.getCurrentPageDate();
                Date date = calendar.getTime();
                SimpleDateFormat tempSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String forwardPageDate = tempSimpleDateFormat.format(date);
                Log.e(TAG, forwardPageDate);
                String[] splitString = forwardPageDate.split("-");
                prepareMeetingByMonthYear(splitString[0], splitString[1]);
            }
        });
    }

    private void addEventToCalendar(String date) {
        Log.e(TAG, date);
        int count = 0;
        try {
            SimpleDateFormat convertDateTime = new SimpleDateFormat("MM/dd/yyyy");
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            Date dateTime = convertDateTime.parse(date);
            String tempDate = sdf.format(dateTime);

            Date dateForEvent = sdf.parse(tempDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateForEvent);
            MyEventDay myEventDay1 = new MyEventDay(cal, R.drawable.circle, "Note " + count++);
            mCalendarView.setDate(myEventDay1.getCalendar());
            mEventDays.add(myEventDay1);
            mCalendarView.setEvents(mEventDays);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*try {
            for (int i = 1; i <= 9; i++) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date dateForEvent = sdf.parse(i + "-06-2019");
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateForEvent);
                MyEventDay myEventDay1 = new MyEventDay(cal, R.drawable.circle, "Note " + i);
                mCalendarView.setDate(myEventDay1.getCalendar());
                mEventDays.add(myEventDay1);
                mCalendarView.setEvents(mEventDays);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private void assignViews() {
        mLinearLayoutCal = (LinearLayout) findViewById(R.id.linearLayoutCal);
        mCalendarView = (CalendarView) findViewById(R.id.calendarView);
        mMeetingListWithCalender = (ListView) findViewById(R.id.meetingListWithCalender);
        mFloatingActionButtonFabWithListview = (FloatingActionButton) findViewById(R.id.float_btn);
        mTxtSelectedDate = (TextView) findViewById(R.id.txtSelectedDate);
        txtDataFound = (TextView) findViewById(R.id.txtDataFound);
    }

    private void setSchoolListWithCalendarAdapter(ArrayList<String> date) {
        /*schoolListWithCalendarAdapter = new SchoolListWithCalendarAdapter(CalenderViewMeetingListActivity.this, date);
        mMeetingListWithCalender.setAdapter(schoolListWithCalendarAdapter);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
           /* case android.R.id.:
                startActivity(new Intent(this, UserProfileActivity.class));
                return true;*/
            case R.id.menuCalender:
                startActivity(new Intent(this, SchoolListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.calender_view_menu, menu);
        return true;

    }


    /*private void prepareListData() {
        try {
            showBusyProgress();
            JSONObject jo = new JSONObject();

            jo.put("UserId", sessionManager.getSessionUserId());
            jo.put("APIKEY", sessionManager.getPrefsOrganizationApiKey());
            jo.put("SECRETKEY", sessionManager.getPrefsOrganizationSecretKey());

            Log.i(SchoolListActivity.class.getName(), Constants.getMeetingList + "\n\n" + jo.toString());

            GsonRequest<MeetingListData> getAssignAvailabilityLearnersListRequest = new GsonRequest<>(Request.Method.POST, Constants.getMeetingList, jo.toString(), MeetingListData.class,
                    new Response.Listener<MeetingListData>() {
                        @Override
                        public void onResponse(@NonNull MeetingListData response) {
                            hideBusyProgress();
                            if (response.getError() != null) {
                                showToast(response.getError().getErrorMessage());
                            } else {
                                if (response.getSuccess() == 1) {

                                    meetingListData = response;
                                    if (meetingListData.getUserMeetingsDates() != null && meetingListData.getUserMeetingsDates().size() > 0) {
                                        txtDataFound.setVisibility(View.GONE);
                                        mMeetingListWithCalender.setVisibility(View.VISIBLE);

                                        for (int i = 0; i < meetingListData.getUserMeetingsDates().size(); i++) {
                                            *//*daysForEvent.add(meetingListData.getUserMeetingsDates().get(i).getDate());*//*
                                            addEventToCalendar(meetingListData.getUserMeetingsDates().get(i).getDate());
                                        }

                                    }
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideBusyProgress();

                }
            });
            getAssignAvailabilityLearnersListRequest.setRetryPolicy(Application.getDefaultRetryPolice());
            getAssignAvailabilityLearnersListRequest.setShouldCache(false);
            Application.getInstance().addToRequestQueue(getAssignAvailabilityLearnersListRequest, "MeetingListData");

        } catch (JSONException e) {
            hideBusyProgress();
            showToast("Something went wrong. Please try again later.");
        }

    }*/

    private void prepareMeetingListByDate(String dateForGetEvents) {

        try {

            showBusyProgress();
            JSONObject jo = new JSONObject();
            jo.put("APIKEY", sessionManager.getPrefsOrganizationApiKey());
            jo.put("SECRETKEY", sessionManager.getPrefsOrganizationSecretKey());
            jo.put("UserId", sessionManager.getSessionUserId());
            jo.put("MeetingDate", dateForGetEvents);

            GsonRequest<UserMeetingByDateParentData> getMeetingByDateRequest = new GsonRequest<>(Request.Method.POST, Constants.getMeetingByDate, jo.toString(), UserMeetingByDateParentData.class,
                    new Response.Listener<UserMeetingByDateParentData>() {
                        @Override
                        public void onResponse(@NonNull UserMeetingByDateParentData response) {
                            hideBusyProgress();
                            if (response.getError() != null) {
                                showToast(response.getError().getErrorMessage());
                            } else {
                                if (response.getSuccess() == 1) {

                                    userMeetingByDateParentData = response;
                                    if (userMeetingByDateParentData.getMeetings() != null && userMeetingByDateParentData.getMeetings().size() > 0) {
                                        txtDataFound.setVisibility(View.GONE);
                                        mMeetingListWithCalender.setVisibility(View.VISIBLE);
                                        userMeetingListResponseData = new ArrayList<>();

                                        userMeetingListResponseData.addAll(userMeetingByDateParentData.getMeetings());
                                        schoolListWithCalendarAdapter = new SchoolListWithCalendarAdapter(CalenderViewMeetingListActivity.this, userMeetingListResponseData);
                                        mMeetingListWithCalender.setAdapter(schoolListWithCalendarAdapter);
                                    } else {
                                        mMeetingListWithCalender.setVisibility(View.GONE);
                                        txtDataFound.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideBusyProgress();
                    Log.e(TAG, error.getMessage());
                }
            });
            getMeetingByDateRequest.setRetryPolicy(Application.getDefaultRetryPolice());
            getMeetingByDateRequest.setShouldCache(false);
            Application.getInstance().addToRequestQueue(getMeetingByDateRequest, "getMeetingByDateRequest");

        } catch (Exception e) {
            hideBusyProgress();
            showToast("Something went wrong. Please try again later.");
        }
    }


    private void prepareMeetingByMonthYear(String year, String month) {

        Log.e(TAG, year);
        Log.e(TAG, month);
        try {
            JSONObject jo = new JSONObject();
            jo.put("APIKEY", sessionManager.getPrefsOrganizationApiKey());
            jo.put("SECRETKEY", sessionManager.getPrefsOrganizationSecretKey());
            jo.put("UserId", sessionManager.getSessionUserId());
            jo.put("Year", year);
            jo.put("Month", month);

            GsonRequest<GetMeetingsByMonthYearResposeData> getAssignAvailabilityLearnersListRequest = new GsonRequest<>(Request.Method.POST, Constants.getAvailableMeetingDates, jo.toString(), GetMeetingsByMonthYearResposeData.class,
                    new Response.Listener<GetMeetingsByMonthYearResposeData>() {
                        @Override
                        public void onResponse(@NonNull GetMeetingsByMonthYearResposeData response) {
                            hideBusyProgress();
                            if (response.getError() != null) {
                                showToast(response.getError().getErrorMessage());
                            } else {
                                if (response.getSuccess() == 1) {

                                    meetingListData = response;
                                    if (meetingListData.getMeetingDates() != null && meetingListData.getMeetingDates().size() > 0) {

                                        for (int i = 0; i < meetingListData.getMeetingDates().size(); i++) {
                                            /*daysForEvent.add(meetingListData.getUserMeetingsDates().get(i).getDate());*/
                                            addEventToCalendar(meetingListData.getMeetingDates().get(i));
                                        }

                                    }
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideBusyProgress();
                    Log.e(TAG, error.getMessage());
                }
            });
            getAssignAvailabilityLearnersListRequest.setRetryPolicy(Application.getDefaultRetryPolice());
            getAssignAvailabilityLearnersListRequest.setShouldCache(false);
            Application.getInstance().addToRequestQueue(getAssignAvailabilityLearnersListRequest, "MeetingListData");

        } catch (Exception e) {
            hideBusyProgress();
            showToast("Something went wrong. Please try again later.");
        }
    }

}

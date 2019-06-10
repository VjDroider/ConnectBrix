package com.edbrix.connectbrix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.edbrix.connectbrix.Application;
import com.edbrix.connectbrix.R;
import com.edbrix.connectbrix.adapters.UserOptionsListAdapter;
import com.edbrix.connectbrix.baseclass.BaseActivity;
import com.edbrix.connectbrix.data.UserData;
import com.edbrix.connectbrix.utils.Constants;
import com.edbrix.connectbrix.utils.SessionManager;
import com.edbrix.connectbrix.volley.GsonRequest;
import com.edbrix.connectbrix.volley.SettingsMy;

import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends BaseActivity {

    private static final String TAG = UserProfileActivity.class.getName();
    private CircleImageView mImgProfile;
    private TextView mTextViewUserName;
    private TextView mTextViewEmail;
    private TextView mTextViewMobileNo;
    private TextView mTextViewOrgnization;
    private TextView mTextViewType;
    private ListView mUserOptionList;
    private RelativeLayout mUpdatePhotoLayout;
    LinearLayout mLinearLayoutUserInfo;

    UserOptionsListAdapter userOptionsListAdapter;
    ArrayList<String> userOptions = new ArrayList<>();
    ArrayList<Integer> userOptionsImages = new ArrayList<>();
    Intent intent;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getSupportActionBar().setTitle("User Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        assignViews();


        userOptions.add("Edit Profile");
        userOptions.add("Change Password");
        userOptions.add("App Tour");

        userOptionsImages.add(R.drawable.editprofile);
        userOptionsImages.add(R.drawable.resetpass);
        userOptionsImages.add(R.drawable.apptour);

        sessionManager = new SessionManager(this);
        GetUserPersonalData();

        setUserDetails();

        userOptionsListAdapter = new UserOptionsListAdapter(UserProfileActivity.this, userOptions, userOptionsImages);
        mUserOptionList.setAdapter(userOptionsListAdapter);

        clickListner();
    }

    private void setUserDetails() {

        mTextViewUserName.setText(sessionManager.getSessionUserFirstName() + " " + sessionManager.getSessionUserFirstLast());
        mTextViewEmail.setText(sessionManager.getSessionUserEmail());
        mTextViewOrgnization.setText(sessionManager.getPrefsSessionSchoolDispalyName());
        //mTextViewType.setText(sessionManager.getSessionUserType());

        if (sessionManager.getSessionUserType().toString().equals("T")) {
            mTextViewType.setText("Teacher");
        } else if (sessionManager.getSessionUserType().toString().equals("A")) {
            mTextViewType.setText("Admin");
        } else if (sessionManager.getSessionUserType().toString().equals("S")) {
            mTextViewType.setText("Student");
        } else if (sessionManager.getSessionUserType().toString().equals("P")) {
            mTextViewType.setText("Parent");
        } else if (sessionManager.getSessionUserType().toString().equals("G")) {
            mTextViewType.setText("Group");
        } else if (sessionManager.getSessionUserType().toString().equals("O")) {
            mTextViewType.setText("Other");
        }

        int randomNumber = generateRandomIntIntRange(0001,9999);
        String imageUrl = sessionManager.getSessionProfileImageUrl()+"?id="+randomNumber;


        Glide.with(this).load(imageUrl)
                .apply(RequestOptions.bitmapTransform(new FitCenter()))
                .into(mImgProfile);

    }

    private void clickListner() {

        mUserOptionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    intent = new Intent(UserProfileActivity.this, EditProfileActivity.class);
                    startActivity(intent);
                } else if (position == 1) {
                    intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
                    startActivity(intent);

                } else if (position == 2) {
                    PrefManager prefManager = new PrefManager(getApplicationContext());
                    prefManager.setFirstTimeLaunch(true);
                    intent = new Intent(UserProfileActivity.this, WelcomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        mLinearLayoutUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(UserProfileActivity.this, UpdateProfilePicActivity.class);
                startActivity(intent);
            }
        });
    }

    private void assignViews() {
        mImgProfile = (CircleImageView) findViewById(R.id.imgProfile);
        mTextViewUserName = (TextView) findViewById(R.id.textViewUserName);
        mTextViewEmail = (TextView) findViewById(R.id.textViewEmail);
        mTextViewMobileNo = (TextView) findViewById(R.id.textViewMobileNo);
        mTextViewOrgnization = (TextView) findViewById(R.id.textViewOrgnization);
        mTextViewType = (TextView) findViewById(R.id.textViewType);
        mUserOptionList = (ListView) findViewById(R.id.userOptionList);
        mUpdatePhotoLayout = (RelativeLayout) findViewById(R.id.updatePhotoLayout);
        mLinearLayoutUserInfo = (LinearLayout) findViewById(R.id.linearLayoutUserInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.user_profile_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menuLogout:
                startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
                logoutFromApp();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutFromApp() {
        finish();
    }

    private void GetUserPersonalData() {
        try {

            showBusyProgress();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("UserId", sessionManager.getSessionUserId());
            jsonObject.put("AccessToken", sessionManager.getPrefsSessionAccessToken());

            GsonRequest<UserData> userOrganizationListRequest = new GsonRequest<>(Request.Method.POST, Constants.getUserPersonalData, jsonObject.toString(), UserData.class,
                    new Response.Listener<UserData>() {
                        @Override
                        public void onResponse(@NonNull UserData response) {
                            hideBusyProgress();
                            if (response.getError() != null) {
                                String error = response.getError().getErrorMessage();
                                showToast(error);
                            } else {

                                if (response.getSuccess() == 1) {
                                    sessionManager.updateSessionProfileImageUrl(response.getUser().getImageUrl());
                                    setUserDetails();
                                }
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    hideBusyProgress();
                    showToast(SettingsMy.getErrorMessage(error));
                }
            });
            userOrganizationListRequest.setRetryPolicy(Application.getDefaultRetryPolice());
            userOrganizationListRequest.setShouldCache(false);
            Application.getInstance().addToRequestQueue(userOrganizationListRequest, "userOrganizationListRequest");

        } catch (Exception e) {

            hideBusyProgress();
            Log.e(TAG, e.getMessage());
        }

    }

    @Override
    public void onBackPressed() {
        setUserDetails();
        super.onBackPressed();
    }
}

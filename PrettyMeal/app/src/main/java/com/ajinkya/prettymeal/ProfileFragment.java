package com.ajinkya.prettymeal;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.helper.widget.MotionEffect;
import androidx.fragment.app.Fragment;

import com.adevinta.leku.LocationPickerActivity;
import com.ajinkya.prettymeal.activity.AboutUsPage;
import com.ajinkya.prettymeal.activity.AddressPicker;
import com.ajinkya.prettymeal.activity.ClientHistoryActivity;
import com.ajinkya.prettymeal.activity.EditProfileActivity;
import com.ajinkya.prettymeal.activity.LoginActivity;
import com.ajinkya.prettymeal.db.UserDBHelper;
import com.ajinkya.prettymeal.model.UserInfo;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    private TextView TvProfileName, TvProfileEmail, TvMobileNo, TvDetailsEmail, TvEditProfile, TvAboutUs, TvLogoutBtn;
    private CircleImageView profileImage;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private String UserUid;
    private UserInfo userInfo;
    private CardView HistoryBtn;
    private UserDBHelper mHelper; // 声明一个用户数据库的帮助器对象
    private SQLiteDatabase sql;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Initialize(view);
        LoadingData();
        Buttons();

        //registerForActivityResulta方法只能在onCreate()中注册。onstart()之后就不能注册了
        // 从后一个页面（EditProfileActivity）返回当前页面时触发
        //EditProfileActivity 需满足：1. 设置 ResultCode =RESULT_OK, 2.发送数据 locationPickerIntent
        @SuppressLint("ResourceAsColor")
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {

                        String LoginIn_Email = result.getData().getStringExtra("customer_email");
                        TvProfileEmail.setText(LoginIn_Email);
                        TvDetailsEmail.setText(LoginIn_Email);

                        sql = mHelper.openReadLink();
                        ArrayList<UserInfo> infoArrayList = mHelper.queryAll(UserDBHelper.TABLE_Customer);
                        for(int i=0;i<infoArrayList.size();i++){
                            if (infoArrayList.get(i).UserEmail.equals(LoginIn_Email)){
                                TvProfileName.setText(infoArrayList.get(i).UserName);
                                TvMobileNo.setText(infoArrayList.get(i).UserMobileNo);
                            }
                        }
                    }
                });

        TvEditProfile.setOnClickListener(View -> {
            Intent locationPickerIntent = new Intent(getContext(), EditProfileActivity.class);
            locationPickerIntent.putExtra("UserName",TvProfileName.getText().toString());
            locationPickerIntent.putExtra("UserEmail",TvProfileEmail.getText().toString());
            locationPickerIntent.putExtra("UserMobileNo",TvMobileNo.getText().toString());
            launcher.launch(locationPickerIntent);

        });

        return view;
    }

    private void Initialize(View view) {

        // TextView & Other View initialize
        profileImage = view.findViewById(R.id.CustomerProfile_image);
        TvProfileName = view.findViewById(R.id.CustomerProfileName);
        TvProfileEmail = view.findViewById(R.id.CustomerProfileEmail);
        TvMobileNo = view.findViewById(R.id.CustomerDetailNumber);
        TvDetailsEmail = view.findViewById(R.id.CustomerDetailEmail);
        TvEditProfile = view.findViewById(R.id.CustomerEditProfile);
        TvAboutUs = view.findViewById(R.id.CustomerAboutUs);
        TvLogoutBtn = view.findViewById(R.id.CustomerLogout);
        HistoryBtn = view.findViewById(R.id.ProfileHistoryCardView);

        // Firebase requirement initialize
        auth = FirebaseAuth.getInstance();

        mHelper = UserDBHelper.getInstance(getContext(), 1);

//        firebaseUser = auth.getCurrentUser();
//        assert firebaseUser != null;
//        UserUid = firebaseUser.getUid();
//        firebaseDatabase = FirebaseDatabase.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();
        //获得数据库帮助器的唯一实例，实例版本为1
        mHelper = UserDBHelper.getInstance(getContext(), 1);
        // 打开数据库帮助器的写连接
        sql = mHelper.openWriteLink();
        mHelper.setmDB(sql);//将数据库实例传到数据库帮助器里
    }

    @Override
    public void onStop() {
        super.onStop();
        // 关闭数据库连接
        mHelper.closeLink();
    }

    /**
    * 加载登录用户的部分信息
    **/
    private void LoadingData() {

        String LoginIn_Email = getActivity().getIntent().getStringExtra("LoginIn_Email");
        TvProfileEmail.setText(LoginIn_Email);
        TvDetailsEmail.setText(LoginIn_Email);

        sql = mHelper.openReadLink();
        ArrayList<UserInfo> infoArrayList = mHelper.query(UserDBHelper.TABLE_Customer,String.format("customer_email='%s'",LoginIn_Email));
        if(!infoArrayList.isEmpty()){
            for(int i=0;i<infoArrayList.size();i++) {
                TvProfileName.setText(infoArrayList.get(i).UserName);
                TvMobileNo.setText(infoArrayList.get(i).UserMobileNo);
            }
        }

        //        DatabaseReference RefUserInfo = firebaseDatabase.getReference().child("Client_Application").child("Users").child(UserUid).child("UserInfo");
//
//        // Read from the database
//        RefUserInfo.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                userInfo = new UserInfo();
//                userInfo.setUserProfileUrl(Objects.requireNonNull(dataSnapshot.child("ProfileImg").getValue()).toString());
//                userInfo.setUserName(Objects.requireNonNull(dataSnapshot.child("Name").getValue()).toString());
//                userInfo.setUserEmail(Objects.requireNonNull(dataSnapshot.child("Email").getValue()).toString());
//                userInfo.setUserMobileNo(Objects.requireNonNull(dataSnapshot.child("PhoneNo").getValue()).toString());
//                userInfo.setUserMembership(Objects.requireNonNull(dataSnapshot.child("Membership").getValue()).toString());
//
//                // Set data to Views
//                setValue(userInfo);
//            }
//
//            private void setValue(UserInfo userInfo) {
//                if (!userInfo.getUserName().isEmpty()) {
//                    if (!userInfo.getUserProfileUrl().isEmpty()) {
//                        Glide.with(requireContext()).load(userInfo.getUserProfileUrl()).into(profileImage);
//                    }
//                    TvProfileName.setText(userInfo.getUserName());
//                    TvProfileEmail.setText(userInfo.getUserEmail());
//                    TvMobileNo.setText(userInfo.getUserMobileNo());
//                    TvDetailsEmail.setText(userInfo.getUserEmail());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
//            }
//        });

    }

    private void Buttons() {
//        TvEditProfile.setOnClickListener(View -> {
//            Intent intent = new Intent(getContext(), EditProfileActivity.class);
//            intent.putExtra("UserName",TvProfileName.getText().toString());
//            intent.putExtra("UserEmail",TvProfileEmail.getText().toString());
//            intent.putExtra("UserMobileNo",TvMobileNo.getText().toString());
//            startActivity(intent);
//        });

        TvAboutUs.setOnClickListener(View -> {
            Intent intent = new Intent(getContext(), AboutUsPage.class);
            startActivity(intent);
        });

        TvLogoutBtn.setOnClickListener(View -> {
            LogoutUser();
        });

        HistoryBtn.setOnClickListener(View->{
            Intent intent = new Intent(getContext(), ClientHistoryActivity.class);
            startActivity(intent);
        });
    }

    /**
    * 退出登录
    **/
    private void LogoutUser() {
        new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setContentText("Do you really want to exit..?")
                .setConfirmText("YES, LOG OUT!")
                .setCancelButton("NO", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
//                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    }
                })
                .show();
    }


}
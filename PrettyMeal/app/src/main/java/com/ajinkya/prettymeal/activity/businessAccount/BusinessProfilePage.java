package com.ajinkya.prettymeal.activity.businessAccount;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ajinkya.prettymeal.R;
import com.ajinkya.prettymeal.activity.LoginActivity;
import com.ajinkya.prettymeal.db.UserDBHelper;
import com.ajinkya.prettymeal.model.UserInfo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BusinessProfilePage extends AppCompatActivity {
    private Button SaveBtn, LogoutBtn;
    private TextInputEditText EtName, EtEmail, EtMobileNo;
    private TextInputLayout layoutName, layoutEmail, layoutMobileNo;
    private String Name, Email, MobileNo, ProfileUrl;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private DatabaseReference UserInfoRef;
    private UserDBHelper mHelper; // 声明一个用户数据库的帮助器对象
    private SQLiteDatabase sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_profile_page);

        Toolbar toolbar = findViewById(R.id.ProfilePageToolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ExtractInfoFromIntent();
        Initialize();
        Buttons();

    }

    @Override
    public void onStart() {
        super.onStart();
        //获得数据库帮助器的唯一实例，实例版本为1
        mHelper = UserDBHelper.getInstance(this, 1);
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

    private void Initialize() {
//        initializing TextInputEditText
        EtName = findViewById(R.id.ProfileNameEditText);
        EtEmail = findViewById(R.id.ProfileEmailEditText);
        EtMobileNo = findViewById(R.id.ProfileMobileNoEditText);

//        initializing TextInputLayout
        layoutName = findViewById(R.id.ProfileNameEditTextLayout);
        layoutEmail = findViewById(R.id.ProfileEmailEditTextLayout);
        layoutMobileNo = findViewById(R.id.ProfileMobileNoEditTextLayout);

//        set values to editText
        EtName.setText(Name);
        EtEmail.setText(Email);
        EtMobileNo.setText(MobileNo);

//        initializing buttons/ hyperlinked text
        SaveBtn = findViewById(R.id.ProfileSaveBtn);
        LogoutBtn = findViewById(R.id.ProfileLogOutBtn);

        mHelper = UserDBHelper.getInstance(this, 1);

////        ProgressDialog creating...
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Validating Your Details...");
//        progressDialog.setMessage("Please wait.... ");
//        progressDialog.setCancelable(false);

//        firebase initialization
//        String Current_Uid = FirebaseAuth.getInstance().getUid();
//        assert Current_Uid != null;
//        UserInfoRef = FirebaseDatabase.getInstance().getReference().child("Business_Application").child("Users").child(Current_Uid).child("UserInfo");

    }

    /**
     * 接收 BusinessHomePage 传递的信息
     **/
    private void ExtractInfoFromIntent() {
        Intent intent = getIntent();
        Name = intent.getStringExtra("UserName");
        Email = intent.getStringExtra("UserEmail");
        MobileNo = intent.getStringExtra("UserMobileNo");
        ProfileUrl = intent.getStringExtra("UserProfileUrl");

    }

    private void Buttons() {
        SaveBtn.setOnClickListener(View -> {
            if(!EtName.getText().toString().isEmpty() && !EtEmail.getText().toString().isEmpty()
                    && !EtMobileNo.getText().toString().isEmpty()) {

                String idStr = null;
                sql = mHelper.openReadLink();
                ArrayList<UserInfo> infoArrayList = mHelper.query(UserDBHelper.TABLE_Business,
                        String.format("business_name ='%s'", Name));
                for (int i = 0; i < infoArrayList.size(); i++) {
//                    Log.i("111111111", infoArrayList.get(i).toString(UserDBHelper.TABLE_Business));
//                    Log.i("111111111", String.valueOf(infoArrayList.get(i).getUserId()));
                    idStr = String.valueOf(infoArrayList.get(i).getUserId());
                }

                sql = mHelper.openWriteLink();
                UserInfo updateduser = new UserInfo();
                updateduser.UserName = EtName.getText().toString();
                updateduser.UserEmail = EtEmail.getText().toString();
                updateduser.UserMobileNo = EtMobileNo.getText().toString();

                mHelper.updateKey(UserDBHelper.TABLE_Business, updateduser, "business_name", idStr);
                mHelper.updateKey(UserDBHelper.TABLE_Business, updateduser, "business_email", idStr);
                mHelper.updateKey(UserDBHelper.TABLE_Business, updateduser, "business_phone", idStr);

                Toast.makeText(this, "Data Saved SuccessFully", Toast.LENGTH_SHORT).show();

                // 设置 ResultCode，并传递数据
                Intent locationPickerIntent = new Intent(BusinessProfilePage.this, BusinessHomePage.class);

                locationPickerIntent.putExtra("business_name",EtName.getText().toString());
//                locationPickerIntent.putExtra("business_email",EtEmail.getText().toString());
//                locationPickerIntent.putExtra("business_phone",EtMobileNo.getText().toString());
                setResult(RESULT_OK, locationPickerIntent);
                finish();

            }else Toast.makeText(this, "Enter all Details", Toast.LENGTH_SHORT).show();

//            if (IsTextFieldValidate()) {
//                UserInfoRef.child("Name").setValue(Name);
//                UserInfoRef.child("Email").setValue(Email);
//                UserInfoRef.child("PhoneNo").setValue(MobileNo);
//                Toast.makeText(this, "Data Saved SuccessFully", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(BusinessProfilePage.this, BusinessHomePage.class);
//                startActivity(intent);
//                finish();
//            } else Toast.makeText(this, "Enter all Details", Toast.LENGTH_SHORT).show();

        });

        LogoutBtn.setOnClickListener(View -> {
            LogoutUser();
        });

    }

    private void LogoutUser() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
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
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(BusinessProfilePage.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }

    private boolean IsTextFieldValidate() {
        Name = Objects.requireNonNull(EtName.getText()).toString();
        Email = Objects.requireNonNull(EtEmail.getText()).toString().trim();
        MobileNo = Objects.requireNonNull(EtMobileNo.getText()).toString().trim();

        layoutName.setErrorEnabled(false);
        layoutEmail.setErrorEnabled(false);
        layoutMobileNo.setErrorEnabled(false);

        String EmailRegex = "^(.+)@(.+)$";
        Pattern EmailPattern = Pattern.compile(EmailRegex);
        Matcher EmailMatcher = EmailPattern.matcher(Email);

        if (Name.isEmpty()) {
            layoutName.setError("Please Provide Your Name");
            return false;
        } else if (Email.isEmpty() || !EmailMatcher.matches()) {
            layoutEmail.setError("Please Provide Validate Email");
            return false;
        } else if (MobileNo.length() < 10 || MobileNo.contains("+")) {
            layoutMobileNo.setError("Please Provide 10 digit MobileNo");
            return false;
        }

        return true;
    }

}
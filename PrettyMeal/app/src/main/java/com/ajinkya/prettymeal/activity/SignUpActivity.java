package com.ajinkya.prettymeal.activity;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ajinkya.prettymeal.R;
import com.ajinkya.prettymeal.activity.businessAccount.BusinessRegisterActivity;
import com.ajinkya.prettymeal.db.UserDBHelper;
import com.ajinkya.prettymeal.model.UserInfo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SignUpActivity extends AppCompatActivity {

    private Button RegisterBtn;
    private TextView LoginPageRedirect;
    private TextInputEditText EtName, EtEmail, EtMobileNo, EtPassword, EtConformPassword;
    private TextInputLayout layoutName, layoutEmail, layoutMobileNo, layoutPassword, layoutConformPassword;
    private String Name, Email, MobileNo, Password, ConformPassword;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private UserDBHelper mHelper; // 声明一个用户数据库的帮助器对象
    private SQLiteDatabase sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Initialize();
        Buttons();

    }

    private void Initialize() {

//        initializing TextInputEditText
        EtName = findViewById(R.id.customerNameReg);
        EtEmail = findViewById(R.id.customerEmailReg);
        EtMobileNo = findViewById(R.id.customerMobileReg);
        EtPassword = findViewById(R.id.customerPasswordReg);
        EtConformPassword = findViewById(R.id.customerPasswordConform);

//        initializing TextInputLayout
        layoutName = findViewById(R.id.customerNameRegLayout);
        layoutEmail = findViewById(R.id.customerEmailRegLayout);
        layoutMobileNo = findViewById(R.id.customerMobileRegLayout);
        layoutPassword = findViewById(R.id.customerPasswordRegLayout);
        layoutConformPassword = findViewById(R.id.customerPasswordConformLayout);

//        initializing buttons/ hyperlinked text
        RegisterBtn = findViewById(R.id.customerRegisterBtn);
        LoginPageRedirect = findViewById(R.id.LoginPageRedirect);

//        ProgressDialog creating...
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Registering Your Details...");
        progressDialog.setMessage("Please wait.... ");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();

        mHelper = UserDBHelper.getInstance(this, 1);

    }

    /**
     * 判断注册信息是否合理
     **/
    private boolean IsTextFieldValidate() {
        Name = Objects.requireNonNull(EtName.getText()).toString();
        Email = Objects.requireNonNull(EtEmail.getText()).toString().trim();
        MobileNo = Objects.requireNonNull(EtMobileNo.getText()).toString().trim();
        Password = Objects.requireNonNull(EtPassword.getText()).toString().trim();
        ConformPassword = Objects.requireNonNull(EtConformPassword.getText()).toString().trim();

        layoutName.setErrorEnabled(false);
        layoutEmail.setErrorEnabled(false);
        layoutMobileNo.setErrorEnabled(false);
        layoutPassword.setErrorEnabled(false);
        layoutConformPassword.setErrorEnabled(false);

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
        } else if (Password.isEmpty()) {
            layoutPassword.setError("Enter Password for Security");
            return false;
        } else if (!ConformPassword.equals(Password)) {
            layoutConformPassword.setError("Password does not match");
            EtConformPassword.setText("");
            ConformPassword = "";
            return false;
        }

        return true;
    }

    private void Buttons() {
        RegisterBtn.setOnClickListener(View -> {
//            if (IsTextFieldValidate()) {
//                progressDialog.show();
//                StartRegister();
//            }

            sql = mHelper.openReadLink();
            ArrayList<UserInfo> infoArrayList = mHelper.queryAll(UserDBHelper.TABLE_Customer);
            if (infoArrayList.size() != 0){
                for(int i=0;i<infoArrayList.size();i++){
//                    Log.i("kk","TABLE_Customer: "+infoArrayList.get(i).toString(UserDBHelper.TABLE_Customer));

                    if (infoArrayList.get(i).UserEmail.equals(Email)) {
                        Toast.makeText(SignUpActivity.this, "Email Id already used for another account", Toast.LENGTH_SHORT).show();
                    }else{
                        DatabaseRegister();
                    }
                }
            }else{
                DatabaseRegister();
            }
        });

        LoginPageRedirect.setOnClickListener(View -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //获得数据库帮助器的唯一实例，实例版本为1
        mHelper = UserDBHelper.getInstance(this, 1);
        // 打开数据库帮助器的写连接
        sql = mHelper.openWriteLink();
        mHelper.setmDB(sql);//将数据库实例传到数据库帮助器里
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 关闭数据库连接
        mHelper.closeLink();
    }

    /**
     * 数据库 创建新用户
     **/
    private void DatabaseRegister(){
        progressDialog.dismiss();

        UserInfo info1 = new UserInfo();
        info1.UserName = EtName.getText().toString();
        info1.UserEmail = EtEmail.getText().toString();
        info1.UserMobileNo = EtMobileNo.getText().toString();
        info1.UserPassword = EtPassword.getText().toString();

        sql = mHelper.openWriteLink();
        mHelper.insert(UserDBHelper.TABLE_Customer,info1);

        new SweetAlertDialog(SignUpActivity.this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("注册成功")
                .show();

        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

//        sql = mHelper.openReadLink();
//        ArrayList<UserInfo> infoArrayList = mHelper.queryAll(UserDBHelper.TABLE_Customer);
//        for(int i=0;i<infoArrayList.size();i++){
//            Log.i("kk","TABLE_Customer: "+infoArrayList.get(i).toString(UserDBHelper.TABLE_Customer));
//        }

    }

    /**
    * Firebase 创建新用户
    **/
    private void StartRegister() {
        //该方法接受电子邮件地址和密码，对其进行验证
        auth.createUserWithEmailAndPassword(Email, Password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Objects.requireNonNull(auth.getCurrentUser()).sendEmailVerification().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        RegisterUserInDatabase(Name, Email, MobileNo);
                    } else {
                        layoutEmail.setError("Provide correct Email");
                        new SweetAlertDialog(SignUpActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Error...")
                                .setContentText("Email id not exist!")
                                .show();
                        Log.e(TAG, "onComplete: Wrong email address");
                    }

                });

            } else {
                progressDialog.dismiss();
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthUserCollisionException existEmail) {
                    Log.e(TAG, "StartRegister: Email id already exist..., Please try with different Email");
                    layoutEmail.setError("Email already used for another user");
                    new SweetAlertDialog(SignUpActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Email is already Used...")
                            .setContentText("Please try with different Email")
                            .show();
                } catch (Exception e) {
                    Log.d(TAG, "onComplete: " + e.getMessage());
                    new SweetAlertDialog(SignUpActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Something went wrong!")
                            .show();
                }

            }

        });
    }

    /**
     * Firebase 用户数据库
     **/
    private void RegisterUserInDatabase(String name, String email, String mobileNo) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        String Current_Uid = currentUser.getUid();

        // Store user type in database
        DatabaseReference UserTypeReference = FirebaseDatabase.getInstance().getReference().child("UserType");
        UserTypeReference.child(Current_Uid).setValue("Customer");


        // Store user data in database
        DatabaseReference userInfoReference = FirebaseDatabase.getInstance().getReference().child("Client_Application").child("Users").child(Current_Uid).child("UserInfo");
        Log.e(TAG, "RegisterPhone: " + userInfoReference);
        HashMap<String, String> user = new HashMap<>();
        user.put("Name", name);
        user.put("Email", email);
        user.put("PhoneNo", mobileNo);
        user.put("ProfileImg", "");
        user.put("Membership", "NA");
        user.put("MealsLeft", "0");
        user.put("TotalMeals", "0");
        user.put("PlanStartDate", "NA");


        userInfoReference.setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.dismiss();
                new SweetAlertDialog(SignUpActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Successfully Registered\n Please Verify your Email")
                        .setContentText("Verification link sent on your email.\nIf mail not visible inside Inbox then check the Spam folder")
                        .setConfirmText("OK")
                        .setConfirmClickListener(sDialog -> {
                            sDialog.dismissWithAnimation();
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .show();
                Log.e(TAG, "onComplete: Registered Successfully....");
            } else {
                progressDialog.dismiss();
                new SweetAlertDialog(SignUpActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Something went wrong!")
                        .show();
                Log.e(TAG, "onComplete: Filed to register info");
            }
        });

    }


}
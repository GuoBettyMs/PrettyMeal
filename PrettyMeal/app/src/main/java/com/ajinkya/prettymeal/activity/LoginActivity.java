package com.ajinkya.prettymeal.activity;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.MotionEffect;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ajinkya.prettymeal.R;
import com.ajinkya.prettymeal.activity.businessAccount.BusinessLoginActivity;
import com.ajinkya.prettymeal.db.UserDBHelper;
import com.ajinkya.prettymeal.model.UserInfo;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity {

    private Button LoginBtn;
    private TextView PasswordReset, RegisterPageNavigate, LoginAsMessOwner;
    private TextInputEditText EtEmail, EtPassword;
    private TextInputLayout layoutEmail, layoutPassword;
    private String Email, Password;
    private ProgressDialog progressDialog, loadingBar;
    private FirebaseAuth mAuth;
    private UserDBHelper mHelper; // 声明一个用户数据库的帮助器对象
    private SQLiteDatabase sql;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Initialize();
//        Permissions();
        Buttons();

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

    private void Initialize() {
        // initializing TextInputEditText
        EtEmail = findViewById(R.id.customerEmailLogin);
        EtPassword = findViewById(R.id.customerPasswordLogin);

        // initializing TextInputLayout
        layoutEmail = findViewById(R.id.customerEmailLoginLayout);
        layoutPassword = findViewById(R.id.customerPasswordLoginLayout);

        // initializing buttons/ hyperlinked text
        PasswordReset = findViewById(R.id.customerForgotPassword);
        LoginBtn = findViewById(R.id.customerLoginBtn);
        RegisterPageNavigate = findViewById(R.id.RegisterPageRedirect);
        LoginAsMessOwner = findViewById(R.id.LoginAsMessOwner);

        // ProgressDialog creating...
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Validating...");
        progressDialog.setMessage("Please wait.... ");
        progressDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();

        mHelper = UserDBHelper.getInstance(this, 1);

        //location requirements
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }

    private boolean IsTextFieldValidate() {
        Email = Objects.requireNonNull(EtEmail.getText()).toString().trim();
        Password = Objects.requireNonNull(EtPassword.getText()).toString().trim();

        layoutEmail.setErrorEnabled(false);
        layoutPassword.setErrorEnabled(false);

        String EmailRegex = "^(.+)@(.+)$";
        Pattern EmailPattern = Pattern.compile(EmailRegex);
        Matcher EmailMatcher = EmailPattern.matcher(Email);

        if (Email.isEmpty() || !EmailMatcher.matches()) {
            layoutEmail.setError("Please Provide Validate Email");
            return false;
        } else if (Password.isEmpty()) {
            layoutPassword.setError("Enter Password");
            return false;
        }
        return true;
    }

    private void Buttons() {
        PasswordReset.setOnClickListener(View ->{
            AlertDialog.Builder builder=new AlertDialog.Builder(this)
                    .setTitle("Forgot Password...?")
                    .setCancelable(false)
                    .setMessage("Please provide your registered email.")
                    .setIcon(R.drawable.ic_profile);

            LinearLayout linearLayout=new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            final EditText editText = new EditText(this);
            // write the email using which you registered
            editText.setHint("Enter Registered Email");
            editText.setMinEms(16);
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            linearLayout.addView(editText);
            linearLayout.setPadding(10, 10, 10, 10);
            builder.setView(linearLayout);

            final EditText password = new EditText(this);
            password.setHint("Enter New Password");
            password.setMinEms(0);
            password.setInputType(InputType.TYPE_CLASS_TEXT);
            linearLayout.addView(password);
            linearLayout.setPadding(10, 10, 10, 10);
            builder.setView(linearLayout);

            // Click on Recover and a email will be sent to your registered email id
            builder.setPositiveButton("Reset Password", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String email = editText.getText().toString().trim();
                    String str = String.format("business_email='%s'",email);

                    if (!email.isEmpty()){
                        sql = mHelper.openWriteLink();
                        UserInfo info = new UserInfo();
                        info.UserPassword = password.getText().toString();
                        mHelper.updateKey(UserDBHelper.TABLE_Customer,info,"customer_password",str);
                    }
//                    String EmailRegex = "^(.+)@(.+)$";
//                    Pattern EmailPattern = Pattern.compile(EmailRegex);
//                    Matcher EmailMatcher = EmailPattern.matcher(email);
//                    if(!email.isEmpty() && EmailMatcher.matches()){
//                        beginRecovery(email);
//                    }else{
//                        new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
//                                .setTitleText("Invalid Email..!")
//                                .setContentText("Please provide Correct Email! ")
//                                .show();
//                        Log.e(TAG, "onClick: Sorry, You Not provided Email");
//                    }

                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();

        });

        LoginBtn.setOnClickListener(View -> {
//            if (IsTextFieldValidate()) {
//                progressDialog.show();
//                StartLogin(Email, Password);
//            }
            DatabaseLogin();
        });

        RegisterPageNavigate.setOnClickListener(View -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        LoginAsMessOwner.setOnClickListener(View -> {
            Intent intent = new Intent(LoginActivity.this, BusinessLoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * 数据库 登录
     **/
    private void DatabaseLogin(){

        String LoginIn_Email = EtEmail.getText().toString();
        String LoginIn_Password = EtPassword.getText().toString();

        sql = mHelper.openReadLink();
        ArrayList<UserInfo> infoArrayList = mHelper.queryAll(UserDBHelper.TABLE_Customer);
        for(int i=0;i<infoArrayList.size();i++){
            if (infoArrayList.get(i).UserEmail.equals(LoginIn_Email) && infoArrayList.get(i).UserPassword.equals(LoginIn_Password)){
//                Log.i("kk","TABLE_Customer: "+infoArrayList.get(i).toString(UserDBHelper.TABLE_Customer));
                progressDialog.dismiss();

                //LoginActivity 传递 数值到 AddressPicker,并跳转
                Intent intent = new Intent(LoginActivity.this,AddressPicker.class);
                intent.putExtra("CancelBtnEnable",false);
                intent.putExtra("LoginIn_Email", LoginIn_Email);
                startActivity(intent);
            }else{
                progressDialog.dismiss();
                new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Invalid username or password")
                        .setContentText("Please Enter Correct username or password")
                        .show();
                EtPassword.setText("");
                Log.e(TAG, "onComplete: Invalid username or password");
            }
        }
    }

    /**
     *  LoginActivity 跳转到 MainActivity
     *  接收 setResult 设置回调
     **/
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode==10 &&  resultCode==RESULT_OK){
//            if (data!=null){
//
//                // Get data form result ==>
//                double Latitude = data.getExtras().getDouble("Latitude",0.0);
//                double Longitude = data.getExtras().getDouble("Longitude",0.0);
//                String AddressLine1 = data.getExtras().getString("AddressLine1","India");
//                String AddressLine2 = data.getExtras().getString("AddressLine2","India");
//                String FullAddress = data.getExtras().getString("FullAddress","India");
//                String ShortAddress = data.getExtras().getString("ShortAddress","India");
//
//                // update firebase database ==>
////                String Current_Uid = FirebaseAuth.getInstance().getUid();
////                assert Current_Uid != null;
////                DatabaseReference userInfoRef = FirebaseDatabase.getInstance().getReference().child("Client_Application").child("Users").child(Current_Uid).child("UserAddress");
////
////                userInfoRef.child("Latitude").setValue(String.valueOf(Latitude));
////                userInfoRef.child("Longitude").setValue(String.valueOf(Longitude));
////                userInfoRef.child("AddressLine1").setValue(AddressLine1);
////                userInfoRef.child("AddressLine2").setValue(AddressLine2);
////                userInfoRef.child("FullAddress").setValue(FullAddress);
////                userInfoRef.child("ShortAddress").setValue(ShortAddress);
//
//                Intent in = new Intent(LoginActivity.this, MainActivity.class);
//                in.putExtra("User_Email","777789");
//                startActivity(in);
//                finish();
//
//            }
//        }
//    }

    private void beginRecovery(String email) {
        loadingBar=new ProgressDialog(this);
        loadingBar.setMessage("Sending Email....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();
                if(task.isSuccessful())
                {
                    new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Successfully sent mail")
                            .setContentText("Password reset link sent on your email.\nIf mail not visible inside Inbox then check the Spam folder")
                            .setConfirmText("OK")
                            .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                            .show();
                }
                else {
                    new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Invalid Email..!")
                            .setContentText("Please provide Registered Email! ")
                            .show();
                }
            }
        }).addOnFailureListener(e -> {
            loadingBar.dismiss();
            Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
        });
    }

    private void StartLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userUid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                    DatabaseReference UserTypeReference = FirebaseDatabase.getInstance().getReference().child("UserType").child(userUid);
                    UserTypeReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String userType = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                            Log.e(TAG, "Value is: " + userType);
                            if (userType.equals("Customer")){
                                Login();
                            }else{
                                progressDialog.dismiss();
                                new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Oops...")
                                        .setContentText("You can't able to use this account as a normal account")
                                        .show();
                                mAuth.signOut();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Failed to read value
                            progressDialog.dismiss();
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });

                } else {
                    progressDialog.dismiss();
                    new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Invalid username or password")
                            .setContentText("Please Enter Correct username or password")
                            .show();
                    EtPassword.setText("");
                    Log.e(TAG, "onComplete: Invalid username or password");

                }

            }

            private void Login() {
                if (Objects.requireNonNull(mAuth.getCurrentUser()).isEmailVerified()) {
                    progressDialog.dismiss();
                    GetAddress();

                } else {
                    progressDialog.dismiss();
                    new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Email Not Verified..!")
                            .setContentText("Please verify your email")
                            .setConfirmText("Resend")
                            .showCancelButton(true)
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    mAuth.getCurrentUser().sendEmailVerification();
                                    sDialog
                                            .setTitleText("Mail sent Successfully!")
                                            .setContentText("Please check your Inbox as well as Spam folder")
                                            .setConfirmText("OK")
                                            .setConfirmClickListener(null)
                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                }
                            })
                            .show();
                }
            }
        });
    }

    /**
     *  LoginActivity 传递 <"CancelBtnEnable" = false>数值到 AddressPicker,并跳转
     **/
    private void GetAddress() {
        Intent intent = new Intent(LoginActivity.this,AddressPicker.class);
        intent.putExtra("CancelBtnEnable",false);
        startActivityForResult(intent,10);

    }

//    private void Permissions() {
//        try {
//            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
//                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 44);
//            }else {
//                if (!CheckGpsStatus()){
//                    buttonSwitchGPS_ON();
//                }
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//    private boolean CheckGpsStatus(){
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            Log.e(MotionEffect.TAG, "CheckGpsStatus: Gps Is On" );
//            return true;
//        }
//        else {
//            Log.e(MotionEffect.TAG, "CheckGpsStatus: Gps Is OFF" );
//            return false;
//
//        }
//    }
//    public void buttonSwitchGPS_ON(){
//
//        LocationRequest locationRequest = LocationRequest.create();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(10000);
//        locationRequest.setFastestInterval(10000/2);
//
//        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();
//
//        locationSettingsRequestBuilder.addLocationRequest(locationRequest);
//        locationSettingsRequestBuilder.setAlwaysShow(true);
//
//        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
//        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());
//        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
//            @Override
//            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
//                Log.e(MotionEffect.TAG, "onSuccess: Location settings (GPS) is ON.");
//            }
//        });
//
//        task.addOnFailureListener(this, new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.e(MotionEffect.TAG, "onSuccess: Location settings (GPS) is OFF.");
//
//                if (e instanceof ResolvableApiException){
//                    try {
//                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
//                        resolvableApiException.startResolutionForResult(LoginActivity.this,
//                                REQUEST_CHECK_SETTINGS);
//                    } catch (IntentSender.SendIntentException sendIntentException) {
//                        sendIntentException.printStackTrace();
//                    }
//                }
//            }
//        });
//    }

}
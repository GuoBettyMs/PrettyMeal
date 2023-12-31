package com.ajinkya.prettymeal.activity.businessAccount;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.MotionEffect;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.adevinta.leku.LocationPickerActivity;
import com.ajinkya.prettymeal.R;
import com.ajinkya.prettymeal.activity.AddressPicker;
import com.ajinkya.prettymeal.activity.LoginActivity;
import com.ajinkya.prettymeal.activity.MainActivity;
import com.ajinkya.prettymeal.activity.SignUpActivity;
import com.ajinkya.prettymeal.db.UserDBHelper;
import com.ajinkya.prettymeal.model.UserInfo;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MessInfoRegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button RegisterBtn, AddressPickerBtn;
    private TextInputEditText EtMessName, EtSupportEmail, EtSupportMobileNo, EtMessDescription;
    private TextView MessAddressTextView;
    private TextInputLayout layoutMessName, layoutSupportEmail, layoutSupportMobileNo, layoutMessDescription;
    private Spinner MessTypeSpinner;
    private String UserName, UserEmail, UserMobileNo, UserPassword, MessName, SupportEmail, SupportMobileNo, MessDescription, MessType, MessFullAddress,MessShortAddress, MessLat, MessLong;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private LocationManager locationManager;
    private FirebaseUser currentUser;
    private UserDBHelper mHelper; // 声明一个用户数据库的帮助器对象
    private SQLiteDatabase sql;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mess_info_register);

        ExtractInfoFromIntent();
        Initialize();
//        Permissions();
        Buttons();

        //registerForActivityResulta方法只能在onCreate()中注册。onstart()之后就不能注册了
        // 从后一个页面（AddressPicker）携带地址参数返回当前页面时触发
        //查询地址
        @SuppressLint("ResourceAsColor")
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        MessShortAddress = result.getData().getStringExtra("business_AddressLine2");
                        MessAddressTextView.setVisibility(View.VISIBLE);
                        MessAddressTextView.setText(MessShortAddress);
                    }
                });

        AddressPickerBtn.setOnClickListener(View -> {
            Intent intent = new Intent(MessInfoRegisterActivity.this,AddressPicker.class);
            intent.putExtra("CancelBtnEnable",false);
            intent.putExtra("BusinessBool",true);
            launcher.launch(intent);
        });

    }

    /**
    * 接收 BusinessRegisterActivity 数据
    **/
    private void ExtractInfoFromIntent() {
        Intent intent = getIntent();
        UserName = intent.getStringExtra("UserName");
        UserEmail = intent.getStringExtra("UserEmail");
        UserMobileNo = intent.getStringExtra("UserMobileNo");
        UserPassword = intent.getStringExtra("Password");
    }

    private void Initialize() {
//        initializing TextInputEditText
        EtMessName = findViewById(R.id.RegMessName);
        EtSupportEmail = findViewById(R.id.RegMessEmail);
        EtSupportMobileNo = findViewById(R.id.RegMessPhoneNo);
        EtMessDescription = findViewById(R.id.RegMessDescription);
        MessTypeSpinner = findViewById(R.id.MessTypeSpinner);
        MessAddressTextView = findViewById(R.id.RegMessAddressTextView);

//        initializing TextInputLayout
        layoutMessName = findViewById(R.id.RegMessNameLayout);
        layoutSupportEmail = findViewById(R.id.RegMessEmailLayout);
        layoutSupportMobileNo = findViewById(R.id.RegMessPhoneNoLayout);
        layoutMessDescription = findViewById(R.id.RegMessDescriptionLayout);

//        initializing buttons/ hyperlinked text
        RegisterBtn = findViewById(R.id.businessRegisterBtn);
        AddressPickerBtn = findViewById(R.id.MessLocationPickerBtn);

//        ProgressDialog creating...
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Validating Your Details...");
        progressDialog.setMessage("Please wait.... ");
        progressDialog.setCancelable(false);

//        firebase initialization
        auth = FirebaseAuth.getInstance();

//        set data to spinner
        MessTypeSpinner.setOnItemSelectedListener(this);
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("PureVeg");
        categories.add("Veg-NonVeg");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        MessTypeSpinner.setAdapter(dataAdapter);

        //location requirements
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mHelper = UserDBHelper.getInstance(this, 1);

        EtMessName.setText(UserName);
        EtSupportEmail.setText(UserEmail);
        EtSupportMobileNo.setText(UserMobileNo);
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

    private void Buttons() {
        RegisterBtn.setOnClickListener(View -> {
//            if (IsTextFieldValidate()) {
//                progressDialog.show();
//                StartRegister();
//            }
            DatabaseRegister();
        });

//        AddressPickerBtn.setOnClickListener(View -> {
//            Intent intent = new Intent(MessInfoRegisterActivity.this,AddressPicker.class);
//            intent.putExtra("CancelBtnEnable",false);
//            intent.putExtra("BusinessBool",true);
//            startActivityForResult(intent,10);
//        });
    }

    /**
     * 数据库 创建新用户
     **/
    private void DatabaseRegister(){
        progressDialog.dismiss();

        UserInfo info1 = new UserInfo();
        info1.UserName = UserName;
        info1.UserEmail = UserEmail;
        info1.UserMobileNo = UserMobileNo;
        info1.UserPassword = UserPassword;
        info1.UserAddress = MessShortAddress;

        sql = mHelper.openWriteLink();
        mHelper.insert(UserDBHelper.TABLE_Business,info1);

//        sql = mHelper.openReadLink();
//        ArrayList<UserInfo> infoArrayList = mHelper.queryAll(UserDBHelper.TABLE_Business);
//        for(int i=0;i<infoArrayList.size();i++){
//            Log.i("kk","TABLE_Business: "+infoArrayList.get(i).toString(UserDBHelper.TABLE_Business));
//        }
//
//        new SweetAlertDialog(MessInfoRegisterActivity.this, SweetAlertDialog.ERROR_TYPE)
//                .setTitleText("注册成功")
//                .show();

        Intent intent = new Intent(MessInfoRegisterActivity.this, BusinessLoginActivity.class);
        startActivity(intent);
        finish();

    }

    private boolean IsTextFieldValidate() {
        MessName = Objects.requireNonNull(EtMessName.getText()).toString();
        SupportEmail = Objects.requireNonNull(EtSupportEmail.getText()).toString().trim();
        SupportMobileNo = Objects.requireNonNull(EtSupportMobileNo.getText()).toString().trim();
        MessDescription = Objects.requireNonNull(EtMessDescription.getText()).toString().trim();
        MessType = Objects.requireNonNull(MessTypeSpinner.getSelectedItem().toString());

        layoutMessName.setErrorEnabled(false);
        layoutSupportEmail.setErrorEnabled(false);
        layoutSupportMobileNo.setErrorEnabled(false);
        layoutMessDescription.setErrorEnabled(false);
//        layoutMessName.setErrorEnabled(false);

        String EmailRegex = "^(.+)@(.+)$";
        Pattern EmailPattern = Pattern.compile(EmailRegex);
        Matcher EmailMatcher = EmailPattern.matcher(SupportEmail);

        if (MessName.isEmpty()) {
            layoutMessName.setError("Please Provide Mess Name");
            return false;
        } else if (SupportEmail.isEmpty() || !EmailMatcher.matches()) {
            layoutSupportEmail.setError("Please Provide Validate Email");
            return false;
        } else if (SupportMobileNo.length() < 10 || SupportMobileNo.contains("+")) {
            layoutSupportMobileNo.setError("Please Provide 10 digit MobileNo");
            return false;
        } else if (MessDescription.isEmpty()) {
            layoutMessDescription.setError("Enter Your Mess Information");
            return false;
        } else if (MessType.isEmpty()) {
            Toast.makeText(this, "Select mess type", Toast.LENGTH_SHORT).show();
            return false;
        } else if (MessFullAddress.isEmpty()){
            Toast.makeText(this, "Pick mess location", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        MessType = adapterView.getItemAtPosition(i).toString();

        // Showing selected spinner item
        Log.e(TAG, "onItemSelected: "+MessType );
//        Toast.makeText(adapterView.getContext(), "Selected: " + MessType, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode==10 &&  resultCode==RESULT_OK){
//            if (data!=null){
//
//                // Get data form result ==>
//                double Latitude = data.getExtras().getDouble("Latitude",0.0);
//                double Longitude = data.getExtras().getDouble("Longitude",0.0);
////                String AddressLine1 = data.getExtras().getString("AddressLine1","India");
////                String AddressLine2 = data.getExtras().getString("AddressLine2","India");
//                MessFullAddress = data.getExtras().getString("FullAddress","India");
//                MessShortAddress = data.getExtras().getString("ShortAddress","India");
//
//
//                MessLat = String.valueOf(Latitude);
//                MessLong = String.valueOf(Longitude);
//                MessAddressTextView.setVisibility(View.VISIBLE);
//                MessAddressTextView.setText(MessFullAddress);
//            }
//        }
//    }
//
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
//
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
//                        resolvableApiException.startResolutionForResult(MessInfoRegisterActivity.this,
//                                REQUEST_CHECK_SETTINGS);
//                    } catch (IntentSender.SendIntentException sendIntentException) {
//                        sendIntentException.printStackTrace();
//                    }
//                }
//            }
//        });
//    }

    private void StartRegister() {
        auth.createUserWithEmailAndPassword(UserEmail, UserPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Objects.requireNonNull(auth.getCurrentUser()).sendEmailVerification().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        RegisterUserInDatabase();
                    } else {
//                        layoutEmail.setError("Provide correct Email");
                        new SweetAlertDialog(MessInfoRegisterActivity.this, SweetAlertDialog.ERROR_TYPE)
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
//                    layoutEmail.setError("Email already used for another user");
                    new SweetAlertDialog(MessInfoRegisterActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Email is already Used...")
                            .setContentText("Please try with different Email")
                            .show();
                } catch (Exception e) {
                    Log.d(TAG, "onComplete: " + e.getMessage());
                    new SweetAlertDialog(MessInfoRegisterActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Something went wrong!")
                            .show();
                }

            }

        });
    }

    private void RegisterUserInDatabase() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        String Current_Uid = currentUser.getUid();

        //Create another data childes
        DatabaseReference MessOwnerHistory = FirebaseDatabase.getInstance().getReference().child("Business_Application").child("Users").child(Current_Uid).child("History");
        DatabaseReference MessWalletRef = FirebaseDatabase.getInstance().getReference().child("Business_Application").child("Users").child(Current_Uid).child("WalletInfo");
        HashMap<String, String> data = new HashMap<>();
        data.put("WalletBalance", "0");
        data.put("TotalPlatesConsumed", "0");
        MessWalletRef.setValue(data);

        // Store user type in database
        DatabaseReference UserTypeReference = FirebaseDatabase.getInstance().getReference().child("UserType");
        UserTypeReference.child(Current_Uid).setValue("MessOwner");

        // Store user data in database
        DatabaseReference userInfoReference = FirebaseDatabase.getInstance().getReference().child("Business_Application").child("Users").child(Current_Uid).child("UserInfo");
        Log.e(TAG, "RegisterPhone: " + userInfoReference);
        HashMap<String, String> user = new HashMap<>();
        user.put("Name", UserName);
        user.put("Email", UserEmail);
        user.put("PhoneNo", UserMobileNo);
        user.put("ProfileImg", "");
        userInfoReference.setValue(user);


        // update firebase database ==>
        DatabaseReference MessInfoRef = FirebaseDatabase.getInstance().getReference().child("Business_Application").child("Users").child(Current_Uid).child("MessDetails");

        HashMap<String, String> MessDetails = new HashMap<>();
        MessDetails.put("Latitude", MessLat);
        MessDetails.put("Longitude", MessLong);
        MessDetails.put("FullAddress", MessFullAddress);
        MessDetails.put("ShortAddress", MessShortAddress);
        MessDetails.put("MessName", MessName);
        MessDetails.put("MessType", MessType);
        MessDetails.put("SupportEmail", SupportEmail);
        MessDetails.put("SupportPhoneNo", SupportMobileNo);
        MessDetails.put("MessDesc", MessDescription);
        MessDetails.put("VegMenu", "NA");
        MessDetails.put("NonVegMenu", "NA");
        MessDetails.put("Price", "60");
        MessDetails.put("MessUID", Current_Uid);

        MessInfoRef.setValue(MessDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressDialog.dismiss();
                new SweetAlertDialog(MessInfoRegisterActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText("Successfully Registered\n Please Verify your Email")
                        .setContentText("Verification link sent on your email.\nIf mail not visible inside Inbox then check the Spam folder")
                        .setConfirmText("OK")
                        .setConfirmClickListener(sDialog -> {
                            sDialog.dismissWithAnimation();
                            Intent intent = new Intent(MessInfoRegisterActivity.this, BusinessLoginActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .show();
                Log.e(TAG, "onComplete: Registered Successfully....");
            } else {
                progressDialog.dismiss();
                new SweetAlertDialog(MessInfoRegisterActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Something went wrong!")
                        .show();
                Log.e(TAG, "onComplete: Filed to register info");
            }
        });

    }

}
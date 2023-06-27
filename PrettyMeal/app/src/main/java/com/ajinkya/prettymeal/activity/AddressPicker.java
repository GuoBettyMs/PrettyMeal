package com.ajinkya.prettymeal.activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.adevinta.leku.LocationPickerActivity;
import com.ajinkya.prettymeal.BuildConfig;
import com.ajinkya.prettymeal.R;
import com.ajinkya.prettymeal.activity.businessAccount.MessInfoRegisterActivity;
import com.ajinkya.prettymeal.utils.GpsTracker;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AddressPicker extends AppCompatActivity {
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private final int MAP_BUTTON_REQUEST_CODE = 1;
    private Button LocationPickerBtn, UseLocationBtn, CancelBtn;
    private TextInputEditText AddressLine1, AddressLine2;
    private TextInputLayout AddressLine1Layout, AddressLine2Layout;
    private String AddressLine_1 = "", AddressLine_2 = "", FullAddress, ShortAddress;
    private double Latitude = 0.0;
    private double Longitude = 0.0;
    private double resultLat, resultLng;
    private boolean cancelBtnStatus,businessBool;
    private LocationManager locationManager;
    private GpsTracker gpsTracker;
    private String MAPS_API_KEY = "地图定位Key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_picker);

        ParseData();
        Initialize();
//        Permissions();
        GetLatLng();//获取定位成功的经纬度
        Buttons();

        //registerForActivityResulta方法只能在onCreate()中注册。onstart()之后就不能注册了
        // 从后一个页面（LocationPickerActivity）携带地址参数返回当前页面时触发
        //查询地址
        @SuppressLint("ResourceAsColor")
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {

                        UseLocationBtn.setVisibility(View.VISIBLE);
                        AddressLine1Layout.setVisibility(View.VISIBLE);
                        AddressLine2Layout.setVisibility(View.VISIBLE);
                        AddressLine1Layout.setBoxStrokeColor(R.color.orange);
                        AddressLine2Layout.setBoxStrokeColor(R.color.orange);

                        resultLat = result.getData().getDoubleExtra("latitude", 0.0);
                        resultLng = result.getData().getDoubleExtra("longitude", 0.0);

                        Parcelable fullAddress = result.getData().getParcelableExtra("address");
                        List<String> list = Arrays.asList(fullAddress.toString().split("\""));
                        Log.e(TAG, "fullAddress ==> " + list.get(1));

                        FullAddress = list.get(1);
                        ShortAddress = list.get(1);
                        AddressLine_1 = list.get(1);
                        AddressLine_2 = list.get(1);
                        AddressLine1.setText(AddressLine_1);
                        AddressLine2.setText(AddressLine_2);

                    }
                });

        LocationPickerBtn.setOnClickListener(View -> {
            Intent locationPickerIntent = new LocationPickerActivity.Builder()
                    .withLocation(Latitude, Longitude)
                    .withGeolocApiKey(MAPS_API_KEY)
                    .withGooglePlacesApiKey(MAPS_API_KEY)
                    .withSearchZone("hi_IN")
                    .withDefaultLocaleSearchZone()
                    .shouldReturnOkOnBackPressed()
                    .withUnnamedRoadHidden()
                    .withGooglePlacesEnabled()
                    .build(this);

//            startActivityForResult(locationPickerIntent, MAP_BUTTON_REQUEST_CODE);
            launcher.launch(locationPickerIntent);
        });

    }

    /**
     * 接收 LoginActivity 传递的 <"CancelBtnEnable" = false>数值
     * 或者 接收 MainActivity 传递的 <"CancelBtnEnable" = true>数值
     **/
    private void ParseData() {
        Intent intent = new Intent();
        cancelBtnStatus = intent.getBooleanExtra("CancelBtnEnable", true);
    }

    /**
     * 返回
     **/
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * 初始化控件
     **/
    private void Initialize() {

        //Other views -->
        LocationPickerBtn = findViewById(R.id.PickMyLocBtn);
        UseLocationBtn = findViewById(R.id.UseThisLocationBtn);
        CancelBtn = findViewById(R.id.CancelBtn);
        AddressLine1 = findViewById(R.id.AddressLine1);
        AddressLine2 = findViewById(R.id.AddressLine2);
        AddressLine1Layout = findViewById(R.id.AddressLine1Layout);
        AddressLine2Layout = findViewById(R.id.AddressLine2Layout);

        // back button visibility ==>
        if (!cancelBtnStatus){
            CancelBtn.setVisibility(View.GONE);
        }else{
            //Toolbar setup -->
            Toolbar toolbar = findViewById(R.id.AddressPickerToolbar);
            setSupportActionBar(toolbar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            toolbar.setNavigationOnClickListener(view -> onBackPressed());
        }

        //location requirements
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }

    /**
     * 按钮点击事件
     **/
    private void Buttons() {
//        LocationPickerBtn.setOnClickListener(View -> {
//            Intent locationPickerIntent = new LocationPickerActivity.Builder()
//                    .withLocation(Latitude, Longitude)
//                    .withGeolocApiKey(MAPS_API_KEY)
//                    .withGooglePlacesApiKey(MAPS_API_KEY)
//                    .withSearchZone("hi_IN")
//                    .withDefaultLocaleSearchZone()
//                    .shouldReturnOkOnBackPressed()
//                    .withUnnamedRoadHidden()
//                    .withGooglePlacesEnabled()
//                    .build(this);
//
//            startActivityForResult(locationPickerIntent, MAP_BUTTON_REQUEST_CODE);
//        });
//

        //发送数据到 MainActivity
        UseLocationBtn.setOnClickListener(View -> {
//            SubmitResult();

            businessBool = getIntent().getBooleanExtra("BusinessBool", false);
            if (!businessBool){
                String LoginIn_Email = getIntent().getStringExtra("LoginIn_Email");
                Intent data = new Intent(AddressPicker.this, MainActivity.class);
                data.putExtra("LoginIn_Email",LoginIn_Email);
                data.putExtra("Latitude", resultLat);
                data.putExtra("Longitude", resultLng);
                data.putExtra("AddressLine1", AddressLine1.getText());
                data.putExtra("AddressLine2", AddressLine_2);
                data.putExtra("FullAddress", FullAddress);
                data.putExtra("ShortAddress", ShortAddress);
                startActivity(data);
                finish();
            }else{
                Intent data = new Intent(AddressPicker.this, MessInfoRegisterActivity.class);
                data.putExtra("business_AddressLine1", AddressLine1.getText());
                data.putExtra("business_AddressLine2", AddressLine_2);
                setResult(RESULT_OK,data);
                finish();
            }

        });

        CancelBtn.setOnClickListener(View -> {
            onBackPressed();
        });
    }

    /**
     *检查Gps定位是否开启
     **/
    private boolean CheckGpsStatus() {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e(TAG, "CheckGpsStatus: Gps Is On");
            return true;
        } else {
            Log.e(TAG, "CheckGpsStatus: Gps Is OFF");
            return false;

        }
    }

    /**
     *更改位置信息设置
     * 应用不应直接启用服务GPS，而应指定所需的准确度/耗电量以及更新间隔，然后设备自动对系统设置进行相应的更改
     **/
    public void buttonSwitchGPS_ON() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();
        locationSettingsRequestBuilder.addLocationRequest(locationRequest);
        locationSettingsRequestBuilder.setAlwaysShow(true);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Toast.makeText(AddressPicker.this, "Location settings (GPS) is ON.", Toast.LENGTH_SHORT).show();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddressPicker.this, "Location settings (GPS) is OFF.", Toast.LENGTH_SHORT).show();

                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        resolvableApiException.startResolutionForResult(AddressPicker.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                }
            }
        });
    }

    /**
    * 获取定位成功的经纬度
    **/
    private void GetLatLng() {
        if (ActivityCompat.checkSelfPermission(AddressPicker.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (CheckGpsStatus()) {
                gpsTracker = new GpsTracker(AddressPicker.this);
                if (gpsTracker.canGetLocation()) {
                    Latitude = gpsTracker.getLatitude();
                    Longitude = gpsTracker.getLongitude();
                } else {
                    gpsTracker.showSettingsAlert();
                }
            } else {
                buttonSwitchGPS_ON();
            }

        } else {
            ActivityCompat.requestPermissions(AddressPicker.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    44);
        }
    }

    /**
     * 谷歌地图返回定位结果
     **/
//    @SuppressLint("ResourceAsColor")
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK && data != null) {
//            Log.d("RESULT****", "OK");
//            if (requestCode == 1) {
//
//                UseLocationBtn.setVisibility(View.VISIBLE);
//                AddressLine1Layout.setBoxStrokeColor(R.color.orange);
//                AddressLine2Layout.setBoxStrokeColor(R.color.orange);
//                AddressLine1Layout.setVisibility(View.VISIBLE);
//                AddressLine2Layout.setVisibility(View.VISIBLE);
//
//                resultLat = data.getDoubleExtra("latitude", 0.0);
////                Log.e("LATITUDE****", Double.toString(resultLat));
//                resultLng = data.getDoubleExtra("longitude", 0.0);
////                Log.e("LONGITUDE****", Double.toString(resultLng));
//
//                Parcelable fullAddress = data.getParcelableExtra("address");
//                List<String> list = Arrays.asList(fullAddress.toString().split("\""));
//                Log.e(TAG, "fullAddress ==> " + list.get(1));
//
//                FullAddress = list.get(1);
//                ShortAddress = list.get(1);
//                AddressLine_1 = list.get(1);
//                AddressLine_2 = list.get(1);
//                AddressLine1.setText(AddressLine_1);
//                AddressLine2.setText(AddressLine_2);
//
//            }
//        }
//        if (resultCode == Activity.RESULT_CANCELED) {
//            Log.e("RESULT****", "CANCELLED");
//        }
//    }

    /**
     * 发送 setResult 设置回调
     * AddressPicker 保存地址数据到关键字，并结束当前页面
     **/
    private void SubmitResult(){
        FullAddress = AddressLine1.getText()+" "+AddressLine_2;
        Intent data = new Intent();
        data.putExtra("Latitude", resultLat);
        data.putExtra("Longitude", resultLng);
        data.putExtra("AddressLine1", AddressLine1.getText());
        data.putExtra("AddressLine2", AddressLine_2);
        data.putExtra("FullAddress", FullAddress);
        data.putExtra("ShortAddress", ShortAddress);
        setResult(RESULT_OK, data);
        finish();
    }

}
package com.ajinkya.prettymeal.activity.businessAccount;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ajinkya.prettymeal.R;
import com.ajinkya.prettymeal.activity.AboutUsPage;
import com.ajinkya.prettymeal.activity.EditProfileActivity;
import com.ajinkya.prettymeal.db.UserDBHelper;
import com.ajinkya.prettymeal.model.UserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class BusinessHomePage extends AppCompatActivity {

    private TextView MessNameTV, MessLocationTV, NonVegMenuHeading;
    private ImageView bannerImg, veg_nonVegImgBanner, vegImgBanner;
    private TextView bannerMessName, BannerLocation, bannerPrice, BannerVegMenu, BannerNonVegMenu;
    private Button EditMenu;
    private RecyclerView vegMenuRecyclerview, NonVegMenuRecyclerView;
    private CardView messCard, Wallet, profile, EditMessInfo, History, AboutUs, Support;
    private CircleImageView profileImage;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference UserInfoReference, MessInfoReference;
    private String UserUid, UserName, UserEmail, UserPhoneNo, Membership, ProfileImageURL;
    private String MessName, MessShortAddress, MessFullAddress, SupportEmail, SupportPhoneNo, Latitude, Longitude, MessDetails, MessType, VegMenu, NonVegMenu, Price;
    private UserInfo userInfo;
    private BusinessHomePageVegMenuAdapter vegMenuAdapter, NonVegMenuAdapter;
    private String[] vegMenuList, nonVegMenuList;
    private UserDBHelper mHelper; // 声明一个用户数据库的帮助器对象
    private SQLiteDatabase sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_home_page);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(Color.WHITE);

        Initialize();
        initData();
//        FatchData();
        Buttons();

        //registerForActivityResulta方法只能在onCreate()中注册。onstart()之后就不能注册了
        // 从后一个页面（BusinessProfilePage）返回当前页面时触发
        //BusinessProfilePage 需满足：1. 设置 ResultCode = RESULT_OK, 2.发送数据 locationPickerIntent
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        String business_resetname = result.getData().getStringExtra("business_name");
                        MessNameTV.setText(business_resetname);
                        bannerMessName.setText(business_resetname);
                    }
                });

        profile.setOnClickListener(View -> {
            Intent intent = new Intent(BusinessHomePage.this, BusinessProfilePage.class);
            intent.putExtra("UserName", UserName);
            intent.putExtra("UserEmail", UserEmail);
            intent.putExtra("UserMobileNo", UserPhoneNo);
            intent.putExtra("UserProfileUrl", ProfileImageURL);
            launcher.launch(intent);

        });

        // 从后一个页面（EditMessInfo）返回当前页面时触发
        //EditMessInfo 需满足：1. 设置 ResultCode = RESULT_OK, 2.发送数据 locationPickerIntent
        ActivityResultLauncher<Intent> EditMessInfolauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        MessName = result.getData().getStringExtra("business_name");
                        MessType = result.getData().getStringExtra("business_messType");
                        Price = result.getData().getStringExtra("business_price");

                        SetDataToViews();
                    }
                });

        EditMessInfo.setOnClickListener(View -> {
            Intent intent = new Intent(BusinessHomePage.this, EditMessInfo.class);
            intent.putExtra("MessName", MessName);
            intent.putExtra("SupportMobileNo", SupportPhoneNo);
            intent.putExtra("SupportEmail", SupportEmail);
            intent.putExtra("MessType", MessType);
            intent.putExtra("MessDescription", MessDetails);
            intent.putExtra("Price", Price);
            EditMessInfolauncher.launch(intent);

        });

        // 从后一个页面（EditMessMenuActivity）返回当前页面时触发
        //EditMessMenuActivity 需满足：1. 设置 ResultCode = RESULT_OK, 2.发送数据 locationPickerIntent
        ActivityResultLauncher<Intent> EditMenulauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        VegMenu = result.getData().getStringExtra("VegMenu");
                        NonVegMenu = result.getData().getStringExtra("NonVegMenu");

                        SetDataToViews();
                    }
                });

        EditMenu.setOnClickListener(View -> {
            Intent intent = new Intent(BusinessHomePage.this, EditMessMenuActivity.class);
            intent.putExtra("MessType", MessType);
            intent.putExtra("VegMenu", VegMenu);
            intent.putExtra("NonVegMenu", NonVegMenu);
            EditMenulauncher.launch(intent);

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

    private void Initialize() {
        // TextView initialize
        MessNameTV = findViewById(R.id.MessNameHomePage);
        MessLocationTV = findViewById(R.id.LocationTextViewBHP);
        bannerMessName = findViewById(R.id.messNameMC);
        BannerLocation = findViewById(R.id.messAddressMC);
        BannerVegMenu = findViewById(R.id.todayVegMenuMC);
        BannerNonVegMenu = findViewById(R.id.todayNonVegMenuMC);
        bannerPrice = findViewById(R.id.PriceMC);
        NonVegMenuHeading = findViewById(R.id.NonVegMenuHeading);

//        Other View initialize
        bannerImg = findViewById(R.id.bannerImageMC);
        vegImgBanner = findViewById(R.id.vegImageMC);
        veg_nonVegImgBanner = findViewById(R.id.vegNonVegImageMC);
        EditMenu = findViewById(R.id.EditMenuBtn);
        vegMenuRecyclerview = findViewById(R.id.VegMenuRecyclerView);
        NonVegMenuRecyclerView = findViewById(R.id.NonVegMenuRecyclerView);

        messCard = findViewById(R.id.messCard);
        Wallet = findViewById(R.id.WalletIcon);
        profile = findViewById(R.id.ProfileIcon);
        EditMessInfo = findViewById(R.id.EditMessIcon);
        History = findViewById(R.id.HistoryIcon);
        AboutUs = findViewById(R.id.AboutUsIcon);
        Support = findViewById(R.id.SupportIcon);

        mHelper = UserDBHelper.getInstance(this, 1);

        //firebase ==>
//        String Current_Uid = FirebaseAuth.getInstance().getUid();
//        Log.e(TAG, "Initialize: Current_Uid: " + Current_Uid);
//        assert Current_Uid != null;
//        UserInfoReference = FirebaseDatabase.getInstance().getReference().child("Business_Application").child("Users").child(Current_Uid).child("UserInfo");
//        MessInfoReference = FirebaseDatabase.getInstance().getReference().child("Business_Application").child("Users").child(Current_Uid).child("MessDetails");

    }

    private void initData(){

        SupportEmail = getIntent().getStringExtra("LoginIn_Email");
        MessType = "PureVeg";
        MessDetails = "MessDescription";
        VegMenu = "Dal + Rice + Chapati + Aalu-bhaji + Shimla mirchi + Salad";
        NonVegMenu = "Chicken kari + Chapati + Salad";
        Price = "60";

        sql = mHelper.openReadLink();
        ArrayList<UserInfo> infoArrayList = mHelper.queryAll(UserDBHelper.TABLE_Business);
        for(int i=0;i<infoArrayList.size();i++){
            if (infoArrayList.get(i).UserEmail.equals(SupportEmail)){
                MessName = infoArrayList.get(i).UserName;
                SupportPhoneNo = infoArrayList.get(i).UserMobileNo;
                MessShortAddress = infoArrayList.get(i).UserAddress;
                MessFullAddress = infoArrayList.get(i).UserAddress;

                ProfileImageURL = infoArrayList.get(i).UserProfileUrl;//传送到下一界面
            }
        }

        SetDataToViews();
        UserName = MessName;
        UserEmail = SupportEmail;
        UserPhoneNo = SupportPhoneNo;

    }

    private void Buttons() {
        Wallet.setOnClickListener(View -> {
            Toast.makeText(this, "敬请期待", Toast.LENGTH_SHORT).show();
        });

//        profile.setOnClickListener(View -> {
//            Intent intent = new Intent(BusinessHomePage.this, BusinessProfilePage.class);
//            intent.putExtra("UserName", UserName);
//            intent.putExtra("UserEmail", UserEmail);
//            intent.putExtra("UserMobileNo", UserPhoneNo);
//            startActivity(intent);
//        });

//        EditMenu.setOnClickListener(View -> {
//            Intent intent = new Intent(BusinessHomePage.this, EditMessMenuActivity.class);
//            intent.putExtra("MessType", MessType);
//            intent.putExtra("VegMenu", VegMenu);
//            intent.putExtra("NonVegMenu", NonVegMenu);
//            startActivity(intent);
//        });

//        EditMessInfo.setOnClickListener(View -> {
//            Intent intent = new Intent(BusinessHomePage.this, EditMessInfo.class);
//            intent.putExtra("MessName", MessName);
//            intent.putExtra("SupportMobileNo", SupportPhoneNo);
//            intent.putExtra("SupportEmail", SupportEmail);
//            intent.putExtra("MessType", MessType);
//            intent.putExtra("MessDescription", MessDetails);
//            intent.putExtra("Price", Price);
//            startActivity(intent);
//        });

        History.setOnClickListener(View -> {
            Intent intent = new Intent(BusinessHomePage.this, BusinessHistoryActivity.class);
            startActivity(intent);
        });

        Support.setOnClickListener(View -> {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"kardileajinkya@gmail.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "I Need Support");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello Team PrettyMeal\n");
            startActivity(Intent.createChooser(emailIntent, "Choose an Email client :"));

        });

        AboutUs.setOnClickListener(View -> {
            Intent intent = new Intent(BusinessHomePage.this, AboutUsPage.class);
            startActivity(intent);
        });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void SetDataToViews() {
        MessNameTV.setText(MessName);
        MessLocationTV.setText(MessShortAddress);
        bannerMessName.setText(MessName);
        BannerLocation.setText(MessFullAddress);
        bannerPrice.setText("Rs " + Price + " For Each");

        if (MessType.equals("PureVeg")) {
            vegImgBanner.setVisibility(View.VISIBLE);
            veg_nonVegImgBanner.setVisibility(View.GONE);

            vegMenuList = VegMenu.split("\\+");
            BannerVegMenu.setText(VegMenu);

            BannerNonVegMenu.setVisibility(View.GONE);
            NonVegMenuHeading.setVisibility(View.GONE);
            NonVegMenuRecyclerView.setVisibility(View.GONE);

            vegMenuAdapter = new BusinessHomePageVegMenuAdapter(vegMenuList, this);
            vegMenuRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            vegMenuRecyclerview.setAdapter(vegMenuAdapter);
        }else{
            vegImgBanner.setVisibility(View.GONE);
            veg_nonVegImgBanner.setVisibility(View.VISIBLE);

            nonVegMenuList = NonVegMenu.split("\\+");
            BannerNonVegMenu.setText(NonVegMenu);
            BannerNonVegMenu.setVisibility(View.VISIBLE);

            NonVegMenuHeading.setVisibility(View.VISIBLE);
            NonVegMenuRecyclerView.setVisibility(View.VISIBLE);

            NonVegMenuAdapter = new BusinessHomePageVegMenuAdapter(nonVegMenuList, this);
            NonVegMenuRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            NonVegMenuRecyclerView.setAdapter(NonVegMenuAdapter);
        }

    }

    private void FatchData() {
        UserInfoReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e(TAG, "onDataChange: UserInfo " + snapshot);

                UserName = Objects.requireNonNull(snapshot.child("Name").getValue()).toString();
                UserEmail = Objects.requireNonNull(snapshot.child("Email").getValue()).toString();
                UserPhoneNo = Objects.requireNonNull(snapshot.child("PhoneNo").getValue()).toString();
                ProfileImageURL = Objects.requireNonNull(snapshot.child("ProfileImg").getValue()).toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        MessInfoReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e(TAG, "onDataChange: MessInfo " + snapshot);

                MessName = Objects.requireNonNull(snapshot.child("MessName").getValue()).toString();
                SupportEmail = Objects.requireNonNull(snapshot.child("SupportEmail").getValue()).toString();
                SupportPhoneNo = Objects.requireNonNull(snapshot.child("SupportPhoneNo").getValue()).toString();
                MessType = Objects.requireNonNull(snapshot.child("MessType").getValue()).toString();
                MessDetails = Objects.requireNonNull(snapshot.child("MessDesc").getValue()).toString();
                Latitude = Objects.requireNonNull(snapshot.child("Latitude").getValue()).toString();
                Longitude = Objects.requireNonNull(snapshot.child("Longitude").getValue()).toString();
                MessShortAddress = Objects.requireNonNull(snapshot.child("ShortAddress").getValue()).toString();
                MessFullAddress = Objects.requireNonNull(snapshot.child("FullAddress").getValue()).toString();
                VegMenu = Objects.requireNonNull(snapshot.child("VegMenu").getValue()).toString();
                NonVegMenu = Objects.requireNonNull(snapshot.child("NonVegMenu").getValue()).toString();
                Price = Objects.requireNonNull(snapshot.child("Price").getValue()).toString();
                SetDataToViews();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
package com.ajinkya.prettymeal.activity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ajinkya.prettymeal.R;
import com.ajinkya.prettymeal.adapter.MessMenuAdapter;
import com.ajinkya.prettymeal.db.UserDBHelper;
import com.ajinkya.prettymeal.model.UserInfo;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class MessDetailsActivity extends AppCompatActivity {
    private ArrayList<String> vegList, nonVegList;
    private ImageSlider imageSlider;
    private RecyclerView vegMenuRecyclerView, nonVegMenuRecyclerView;
    private LinearLayout nonVegMenuLayout;
    private ImageButton backButton;
    private String MessName, MessDesc, MessAddress, MessLat, MessLong, MessType, VegMenu, NonVegMenu, SupportMail, SupportPhoneNo, MessUID;
    private TextView MessNameTv, MessDetailsTv, MessAddressTv, SupportMailTv, SupportPhoneNoTv;
    private Button LocateOnMapBtn, CollectMealBtn;
    private DatabaseReference MessWalletRef, MessOwnerHistory, ClientMealHistory, UserInfoRef;
    private String MessWalletBalance, TotalPlateConsumed, MealLefts, UserName;
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;
    private UserDBHelper mHelper; // 声明一个用户数据库的帮助器对象
    private SQLiteDatabase sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mess_details);

        mHelper = UserDBHelper.getInstance(this, 1);

        GetDataFromIntent();//接收上一页面传递的数据
        Initialize();
        SetDataToViews();
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

    private void GetDataFromIntent() {
        Intent intent = getIntent();
        UserName = intent.getStringExtra("UserName");
        MessName = intent.getStringExtra("MessName");
        MessDesc = intent.getStringExtra("MessDesc");
        MessAddress = intent.getStringExtra("MessAddress");
        MessLat = intent.getStringExtra("MessLat");
        MessLong = intent.getStringExtra("MessLong");
        MessType = intent.getStringExtra("MessType");
        VegMenu = intent.getStringExtra("VegMenu");
        NonVegMenu = intent.getStringExtra("NonVegMenu");
        SupportMail = intent.getStringExtra("SupportMail");
        SupportPhoneNo = intent.getStringExtra("SupportPhoneNo");
        MessUID = intent.getStringExtra("MessUID");

    }

    private void Initialize() {

//        Initialize Views
        imageSlider = findViewById(R.id.MessImagesSlider);
        vegMenuRecyclerView = findViewById(R.id.RecyclerViewVegFoodMenu);
        nonVegMenuRecyclerView = findViewById(R.id.RecyclerViewNonVegFoodMenu);
        nonVegMenuLayout = findViewById(R.id.NonVegMenu);
        backButton = findViewById(R.id.profile_image);

        MessNameTv = findViewById(R.id.MessNameMD);
        MessDetailsTv = findViewById(R.id.MessDetailsMD);
        MessAddressTv = findViewById(R.id.MessAddressMD);
        SupportMailTv = findViewById(R.id.MessEmailAddress);
        SupportPhoneNoTv = findViewById(R.id.MessPhoneNo);

        //Buttons
        LocateOnMapBtn = findViewById(R.id.LocateOnMapBtn);
        CollectMealBtn = findViewById(R.id.CollectMealBtn);

//        Arraylists
        vegList = new ArrayList<>();
        nonVegList = new ArrayList<>();

        List<SlideModel> ImagesList = new ArrayList<>();
//        ImagesList.add(new SlideModel(R.drawable.banner1, ScaleTypes.FIT));
        ImagesList.add(new SlideModel("https://img.freepik.com/premium-photo/indian-hindu-veg-thali-also-known-as-food-platter-is-complete-lunch-dinner-meal-closeup-selective-focus_466689-9116.jpg?w=2000", ScaleTypes.FIT));
        ImagesList.add(new SlideModel("https://i.pinimg.com/736x/0f/13/7d/0f137d2a243f7b63e5716ab4c10c3ee3.jpg", ScaleTypes.FIT));
        ImagesList.add(new SlideModel("https://5.imimg.com/data5/HW/II/SH/SELLER-9770898/veg-thali-500x500.jpg", ScaleTypes.FIT));
        imageSlider.setImageList(ImagesList, ScaleTypes.FIT);

//        Set RecyclerViews
        vegMenuRecyclerView.setHasFixedSize(true);
        vegMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        nonVegMenuRecyclerView.setHasFixedSize(true);
        nonVegMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Firebase Reference
//        String Current_UID = FirebaseAuth.getInstance().getUid();
//        assert Current_UID != null;
//        UserInfoRef = FirebaseDatabase.getInstance().getReference().child("Client_Application").child("Users").child(Current_UID).child("UserInfo");
//        ClientMealHistory = FirebaseDatabase.getInstance().getReference().child("Client_Application").child("Users").child(Current_UID).child("History");
//        MessOwnerHistory = FirebaseDatabase.getInstance().getReference().child("Business_Application").child("Users").child(MessUID).child("History");
//        MessWalletRef = FirebaseDatabase.getInstance().getReference().child("Business_Application").child("Users").child(MessUID).child("WalletInfo");

//        MessWalletRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                MessWalletBalance = Objects.requireNonNull(snapshot.child("WalletBalance").getValue()).toString();
//                TotalPlateConsumed = Objects.requireNonNull(snapshot.child("TotalPlatesConsumed").getValue()).toString();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

//        UserInfoRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                MealLefts = Objects.requireNonNull(snapshot.child("MealsLeft").getValue()).toString();
//                UserName = Objects.requireNonNull(snapshot.child("Name").getValue()).toString();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

    }

    private void SetDataToViews() {

        MessNameTv.setText(MessName);
        MessDetailsTv.setText(MessDesc);
        MessAddressTv.setText(MessAddress);
        SupportMailTv.setText(SupportMail);
        SupportPhoneNoTv.setText(SupportPhoneNo);

        if (!VegMenu.isEmpty()) {
            String[] strSplit = VegMenu.split("\\+");
            vegList = new ArrayList<>(Arrays.asList(strSplit));
            MessMenuAdapter messVegMenuAdapter = new MessMenuAdapter(vegList);
            vegMenuRecyclerView.setAdapter(messVegMenuAdapter);
        }

        if (!MessType.equals("PureVeg")) {
            String[] strSplit = NonVegMenu.split("\\+");
            nonVegList = new ArrayList<>(Arrays.asList(strSplit));
            MessMenuAdapter messNonVegMenuAdapter = new MessMenuAdapter(nonVegList);
            nonVegMenuRecyclerView.setAdapter(messNonVegMenuAdapter);
        } else {
            nonVegMenuLayout.setVisibility(View.GONE);
        }
    }

    private void Buttons() {
        backButton.setOnClickListener(View -> {
            onBackPressed();
        });

        LocateOnMapBtn.setOnClickListener(View->{
            Intent intent = new Intent(MessDetailsActivity.this, MapActivity.class);
            intent.putExtra("MessLat", MessLat);
            intent.putExtra("MessLong", MessLong);
            intent.putExtra("MessName", MessName);
            startActivity(intent);
        });

        CollectMealBtn.setOnClickListener(View->{
            new SweetAlertDialog(this)
                    .setTitleText("Please Conform Your Meal")
                    .setContentText("You will be allowed to enjoy this meal once you conform.")
                    .setConfirmText("Conform")
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            ConformMeal();

                            sDialog
                                    .setTitleText("Successful!")
                                    .setContentText("Enjoy Your Meal!")
                                    .setConfirmText("OK")
                                    .setConfirmClickListener(null)
                                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        }
                    })
                    .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    })
                    .show();
        });

    }

    private void ConformMeal() {
        MessWalletBalance = "10";
        TotalPlateConsumed = "60";
        MealLefts = "40";

        Date c = Calendar.getInstance().getTime();
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault());
        String DateTime = df.format(c);
        Log.e(TAG, "DisplayPlanConformationDialog: "+DateTime );

        String TransactionNo = "TR"+DateTime.trim().replace("-","").replace(":","");

        //Update MessWallet
        String WalletBal = String.valueOf(Integer.parseInt(MessWalletBalance)+48);
        String PlatesConsumed = String.valueOf(Integer.parseInt(TotalPlateConsumed)+1);
//        MessWalletRef.child("WalletBalance").setValue(WalletBal);
//        MessWalletRef.child("TotalPlatesConsumed").setValue(PlatesConsumed);

        //Update UserInfo
        String MealBaki = String.valueOf(Integer.parseInt(MealLefts)-1);
//        UserInfoRef.child("MealsLeft").setValue(MealBaki);

        //Update MessOwnerHistory,business页面 历史记录
        HashMap<String, String> MessHistoryData = new HashMap<>();
        MessHistoryData.put("UserName", UserName);
        MessHistoryData.put("DateTime", DateTime);
        MessHistoryData.put("TransactionNo", TransactionNo);
//        MessOwnerHistory.child(TransactionNo).setValue(MessHistoryData);

        UserInfo info1 = new UserInfo();
        info1.UserName = UserName;
        info1.history_DateTime = DateTime;
        info1.history_TransactionNo = TransactionNo;
        sql = mHelper.openWriteLink();
        mHelper.insert(UserDBHelper.TABLE_BusinessHistory,info1);

        //Update Client History,customer页面 历史记录
        HashMap<String, String> ClientHistoryData = new HashMap<>();
        ClientHistoryData.put("MessName", MessName);
        ClientHistoryData.put("DateTime", DateTime);
        ClientHistoryData.put("TransactionNo", TransactionNo);
//        ClientMealHistory.child(TransactionNo).setValue(ClientHistoryData);

        UserInfo info2 = new UserInfo();
        info2.UserName = MessName;
        info2.history_DateTime = DateTime;
        info2.history_TransactionNo = TransactionNo;
        sql = mHelper.openWriteLink();
        mHelper.insert(UserDBHelper.TABLE_CustomerHistory,info2);

//        sql = mHelper.openReadLink();
//        ArrayList<UserInfo> infoArrayList = mHelper.queryAll(UserDBHelper.TABLE_BusinessHistory);
//        for(int i=0;i<infoArrayList.size();i++){
//            Log.i("kk","TABLE_BusinessHistory: "+infoArrayList.get(i).toString(UserDBHelper.TABLE_BusinessHistory));
//        }


    }
}
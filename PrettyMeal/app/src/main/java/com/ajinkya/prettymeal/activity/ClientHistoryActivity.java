package com.ajinkya.prettymeal.activity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ajinkya.prettymeal.R;
import com.ajinkya.prettymeal.activity.businessAccount.BusinessHistoryActivity;
import com.ajinkya.prettymeal.db.UserDBHelper;
import com.ajinkya.prettymeal.model.HistoryModel;
import com.ajinkya.prettymeal.model.UserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ClientHistoryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private HistoryRecyclerViewAdapter historyRecyclerViewAdapter;
    private String Name,TransactionNo,DateTime;
    private UserDBHelper mHelper; // 声明一个用户数据库的帮助器对象
    private SQLiteDatabase sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_history);

        Initialize();
        FetchData();
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
        toolbar = findViewById(R.id.ClientHistoryToolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle("History");

        recyclerView = findViewById(R.id.ClientHistoryRecyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        ArrayList historyModelArrayList = new ArrayList<>();
        historyRecyclerViewAdapter = new HistoryRecyclerViewAdapter(historyModelArrayList,this,"Client");
        recyclerView.setAdapter(historyRecyclerViewAdapter);

        mHelper = UserDBHelper.getInstance(this, 1);
    }

    /**
    * Customer 收集的历史菜单信息(含商家名字、日期)
    **/
    private void FetchData() {
        ArrayList<HistoryModel> historyModelArrayList = new ArrayList<>();

        sql = mHelper.openReadLink();
        ArrayList<UserInfo> infoArrayList = mHelper.queryAll(UserDBHelper.TABLE_CustomerHistory);
        if(!infoArrayList.isEmpty()){
            for(int i = 0; i<infoArrayList.size();i++){
//                Log.i("kk","TABLE_CustomerHistory: "+infoArrayList.get(i).toString(UserDBHelper.TABLE_CustomerHistory));
                Name = infoArrayList.get(i).UserName;
                TransactionNo = infoArrayList.get(i).history_TransactionNo;
                DateTime = infoArrayList.get(i).history_DateTime;
            }

            HistoryModel historyModel = new HistoryModel(Name, TransactionNo, DateTime);
            historyModelArrayList.add(historyModel);
            historyRecyclerViewAdapter = new HistoryRecyclerViewAdapter(historyModelArrayList, ClientHistoryActivity.this, "Client");
            recyclerView.setAdapter(historyRecyclerViewAdapter);
        }else{
            Toast.makeText(this, "Sorry Mess Not Available", Toast.LENGTH_SHORT).show();
        }

//        String Current_UID = FirebaseAuth.getInstance().getUid();
//        DatabaseReference ClientHistoryRef = FirebaseDatabase.getInstance().getReference().child("Client_Application").child("Users").child(Current_UID).child("History");

//        ClientHistoryRef.addValueEventListener(new ValueEventListener() {
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<HistoryModel> historyModelArrayList = new ArrayList<>();
//                if (snapshot.hasChildren()){
//                    for (DataSnapshot data : snapshot.getChildren()) {
//                        String key = data.getKey();
//                        Log.e(TAG, "onDataChange: "+key );
//                        assert key != null;
//                        if (!key.isEmpty()){
//                            String Name = Objects.requireNonNull(snapshot.child(key).child("MessName").getValue()).toString();
//                            String TransactionNo = Objects.requireNonNull(snapshot.child(key).child("TransactionNo").getValue()).toString();
//                            String DateTime = Objects.requireNonNull(snapshot.child(key).child("DateTime").getValue()).toString();
//                            HistoryModel historyModel = new HistoryModel(Name, TransactionNo, DateTime);
//                            historyModelArrayList.add(historyModel);
//                        }
//
//                    }
//
//                }
//                historyRecyclerViewAdapter = new HistoryRecyclerViewAdapter(historyModelArrayList, ClientHistoryActivity.this, "Client");
//                recyclerView.setAdapter(historyRecyclerViewAdapter);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }
}
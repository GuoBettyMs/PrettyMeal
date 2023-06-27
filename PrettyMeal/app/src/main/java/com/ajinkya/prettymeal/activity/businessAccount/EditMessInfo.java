package com.ajinkya.prettymeal.activity.businessAccount;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.ajinkya.prettymeal.R;
import com.ajinkya.prettymeal.db.UserDBHelper;
import com.ajinkya.prettymeal.model.UserInfo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditMessInfo extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private Toolbar toolbar;
    private TextInputEditText EtMessName, EtSupportEmail, EtSupportMobileNo, EtMessDescription, EtPrice;
    private TextInputLayout layoutMessName, layoutSupportEmail, layoutSupportMobileNo, layoutMessDescription, layoutPrice;
    private Spinner MessTypeSpinner;
    private String MessName, SupportEmail, SupportMobileNo, MessDescription, MessType, Price;
    private DatabaseReference MessInfoRef;
    private UserDBHelper mHelper; // 声明一个用户数据库的帮助器对象
    private SQLiteDatabase sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mess_info);

        ExtractInfoFromIntent();
        Initialize();

    }

    private void ExtractInfoFromIntent() {
        Intent intent = getIntent();
        MessName = intent.getStringExtra("MessName");
        SupportEmail = intent.getStringExtra("SupportEmail");
        SupportMobileNo = intent.getStringExtra("SupportMobileNo");
        MessType = intent.getStringExtra("MessType");
        MessDescription = intent.getStringExtra("MessDescription");
        Price = intent.getStringExtra("Price");

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
        toolbar = findViewById(R.id.EditMessInfoToolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setTitle("Edit Mess Information");
        toolbar.inflateMenu(R.menu.save_btn);
        toolbar.setOnMenuItemClickListener(item -> {
            saveDetails(); //保存数据
            return true;
        });

//        initializing TextInputEditText
        EtMessName = findViewById(R.id.EditMessName);
        EtSupportEmail = findViewById(R.id.EditMessEmail);
        EtSupportMobileNo = findViewById(R.id.EditMessPhoneNo);
        EtMessDescription = findViewById(R.id.EditMessDescription);
        MessTypeSpinner = findViewById(R.id.EditMessTypeSpinner);
        EtPrice = findViewById(R.id.EditMessPrice);

//        initializing TextInputLayout
        layoutMessName = findViewById(R.id.EditMessNameLayout);
        layoutSupportEmail = findViewById(R.id.EditMessEmailLayout);
        layoutSupportMobileNo = findViewById(R.id.EditMessPhoneNoLayout);
        layoutMessDescription = findViewById(R.id.EditMessDescriptionLayout);
        layoutPrice = findViewById(R.id.EditMessPriceLayout);

//        Set Values
        EtMessName.setText(MessName);
        EtSupportEmail.setText(SupportEmail);
        EtSupportMobileNo.setText(SupportMobileNo);
        EtMessDescription.setText(MessDescription);
        EtPrice.setText(Price);

//        firebase initialization
//        String Current_Uid = FirebaseAuth.getInstance().getUid();
//        assert Current_Uid != null;
//        MessInfoRef = FirebaseDatabase.getInstance().getReference().child("Business_Application").child("Users").child(Current_Uid).child("MessDetails");

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

        // set data to spinner
        if(MessType.equals("PureVeg")){
            MessTypeSpinner.setSelection(0,true);
        }else{
            MessTypeSpinner.setSelection(1,true);
        }
    }

    private boolean IsTextFieldValidate() {
        MessName = Objects.requireNonNull(EtMessName.getText()).toString();
        SupportEmail = Objects.requireNonNull(EtSupportEmail.getText()).toString().trim();
        SupportMobileNo = Objects.requireNonNull(EtSupportMobileNo.getText()).toString().trim();
        MessDescription = Objects.requireNonNull(EtMessDescription.getText()).toString().trim();
        MessType = Objects.requireNonNull(MessTypeSpinner.getSelectedItem().toString());
        Price = Objects.requireNonNull(EtPrice.getText()).toString().trim();

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
        } else if (Price.isEmpty()) {
            layoutPrice.setError("Enter Your Mess Price");
            return false;
        }

        return true;
    }

    private void saveDetails() {
//        if(IsTextFieldValidate()){
        if(!EtMessName.getText().toString().isEmpty()){
            String idStr = null;
            sql = mHelper.openReadLink();
            ArrayList<UserInfo> infoArrayList = mHelper.query(UserDBHelper.TABLE_Business,
                    String.format("business_name ='%s'", MessName));
            for (int i = 0; i < infoArrayList.size(); i++) {
//                    Log.i("111111111", infoArrayList.get(i).toString(UserDBHelper.TABLE_Business));
//                    Log.i("111111111", String.valueOf(infoArrayList.get(i).getUserId()));
                idStr = String.valueOf(infoArrayList.get(i).getUserId());
            }

            sql = mHelper.openWriteLink();
            UserInfo updateduser = new UserInfo();
            updateduser.UserName = EtMessName.getText().toString();
            updateduser.UserEmail = EtSupportEmail.getText().toString();
            updateduser.UserMobileNo = EtSupportMobileNo.getText().toString();

            mHelper.updateKey(UserDBHelper.TABLE_Business, updateduser, "business_name", idStr);
            mHelper.updateKey(UserDBHelper.TABLE_Business, updateduser, "business_email", idStr);
            mHelper.updateKey(UserDBHelper.TABLE_Business, updateduser, "business_phone", idStr);

            MessDescription = EtMessDescription.getText().toString();
            Price = EtPrice.getText().toString();

//            MessInfoRef.child("MessName").setValue(MessName);
//            MessInfoRef.child("MessType").setValue(MessType);
//            MessInfoRef.child("MessDesc").setValue(MessDescription);
//            MessInfoRef.child("Price").setValue(Price);
//            MessInfoRef.child("SupportEmail").setValue(SupportEmail);
//            MessInfoRef.child("SupportPhoneNo").setValue(SupportMobileNo);

            Intent intent = new Intent(EditMessInfo.this, BusinessHomePage.class);
            Toast.makeText(this, "Data Saved Successfully", Toast.LENGTH_SHORT).show();
            intent.putExtra("business_name",EtMessName.getText().toString());
            intent.putExtra("business_phone",EtSupportMobileNo.getText().toString());
            intent.putExtra("business_messType",MessTypeSpinner.getSelectedItem().toString());
            intent.putExtra("business_messDescription",MessDescription);
            intent.putExtra("business_price",Price);
            setResult(RESULT_OK, intent);
            finish();
        }else Toast.makeText(this, "Please Provide All Details", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        MessType = adapterView.getItemAtPosition(i).toString();

        // Showing selected spinner item
        Log.e(TAG, "onItemSelected: "+MessType );
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
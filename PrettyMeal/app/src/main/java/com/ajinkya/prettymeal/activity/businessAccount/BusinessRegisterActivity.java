package com.ajinkya.prettymeal.activity.businessAccount;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ajinkya.prettymeal.R;
import com.ajinkya.prettymeal.activity.LoginActivity;
import com.ajinkya.prettymeal.activity.SignUpActivity;
import com.ajinkya.prettymeal.db.UserDBHelper;
import com.ajinkya.prettymeal.model.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BusinessRegisterActivity extends AppCompatActivity {
    private Button NextBtn;
    private TextView LoginPageRedirect;
    private TextInputEditText EtName, EtEmail, EtMobileNo, EtPassword, EtConformPassword;
    private TextInputLayout layoutName, layoutEmail, layoutMobileNo, layoutPassword, layoutConformPassword;
    private String Name, Email, MobileNo, Password, ConformPassword;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private UserDBHelper mHelper; // 声明一个用户数据库的帮助器对象
    private SQLiteDatabase sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_register);

        Initialize();
        Buttons();
    }

    private void Initialize() {

//        initializing TextInputEditText
        EtName = findViewById(R.id.BUserNameEditText);
        EtEmail = findViewById(R.id.BUserEmailEditText);
        EtMobileNo = findViewById(R.id.BUserMobileNoEditText);
        EtPassword = findViewById(R.id.BUserPasswordText);
        EtConformPassword = findViewById(R.id.BUserConformPasswordText);

//        initializing TextInputLayout
        layoutName = findViewById(R.id.BUserNameEditTextLayout);
        layoutEmail = findViewById(R.id.BUserEmailEditTextLayout);
        layoutMobileNo = findViewById(R.id.BUserMobileNoEditTextLayout);
        layoutPassword = findViewById(R.id.BUserPasswordTextLayout);
        layoutConformPassword = findViewById(R.id.BUserConformPasswordTextLayout);

//        initializing buttons/ hyperlinked text
        NextBtn = findViewById(R.id.BRegNextBtn);
        LoginPageRedirect = findViewById(R.id.BusinessLoginPageRedirect);

//        ProgressDialog creating...
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Validating Your Details...");
        progressDialog.setMessage("Please wait.... ");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();

        mHelper = UserDBHelper.getInstance(this, 1);

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
        NextBtn.setOnClickListener(View -> {
//            if (IsTextFieldValidate()) {
//                progressDialog.show();
//                CheckEmailAlreadyRegistered();
//            }
            CheckEmailAlreadyRegistered_Database();
        });

        LoginPageRedirect.setOnClickListener(View -> {
            Intent intent = new Intent(BusinessRegisterActivity.this, BusinessLoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * 数据库检查新用户
     **/
    private void CheckEmailAlreadyRegistered_Database(){
        progressDialog.dismiss();

        Name = EtName.getText().toString();
        Email = EtEmail.getText().toString();
        MobileNo = EtMobileNo.getText().toString();
        Password = EtPassword.getText().toString();

        if(!Email.isEmpty()){
            sql = mHelper.openReadLink();
            ArrayList<UserInfo> infoArrayList = mHelper.query(UserDBHelper.TABLE_Business, String.format("business_email='%s'",Email));

            if(!infoArrayList.isEmpty() ){
                Toast.makeText(BusinessRegisterActivity.this,
                        "Email does not exsit", Toast.LENGTH_SHORT).show();
            }else{
                PassDataOnNextActivity();
            }
        }else{
            Toast.makeText(BusinessRegisterActivity.this,
                    "Email is empty", Toast.LENGTH_SHORT).show();
        }

    }

    private void PassDataOnNextActivity() {
        Log.e(TAG, "PassDataOnNextActivity: " );
        Intent intent = new Intent(BusinessRegisterActivity.this, MessInfoRegisterActivity.class);
        intent.putExtra("UserName",Name);
        intent.putExtra("UserEmail",Email);
        intent.putExtra("UserMobileNo",MobileNo);
        intent.putExtra("Password",Password);
        startActivity(intent);
    }

    private void CheckEmailAlreadyRegistered() {
        auth.fetchSignInMethodsForEmail(Email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            List<String> signInMethods = result.getSignInMethods();
                            assert signInMethods != null;
                            Log.e(TAG, "onComplete: SignIn Methods  ==  "+ signInMethods  );
                            if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                                // User can sign in with email/password
                                Log.e(TAG, "Email Id already used for another account", task.getException());
                                Toast.makeText(BusinessRegisterActivity.this, "Email Id already used for another account", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();

                            }else {
                                // Go to the next page and pass all the information
                                progressDialog.dismiss();
                                PassDataOnNextActivity();
                            }
                        }
                    }
                });
    }

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

}
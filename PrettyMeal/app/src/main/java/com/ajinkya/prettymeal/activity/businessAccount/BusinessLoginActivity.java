package com.ajinkya.prettymeal.activity.businessAccount;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ajinkya.prettymeal.R;
import com.ajinkya.prettymeal.activity.AddressPicker;
import com.ajinkya.prettymeal.activity.LoginActivity;
import com.ajinkya.prettymeal.db.UserDBHelper;
import com.ajinkya.prettymeal.model.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BusinessLoginActivity extends AppCompatActivity {
    private TextView businessRegPageNavigator, PasswordReset;
    private TextInputEditText EtEmail, EtPassword;
    private TextInputLayout layoutEmail, layoutPassword;
    private String Email, Password;
    private Button LoginBtn;
    private ProgressDialog progressDialog, loadingBar;
    private FirebaseAuth mAuth;
    private UserDBHelper mHelper; // 声明一个用户数据库的帮助器对象
    private SQLiteDatabase sql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_login);

        Initialize();
        Buttons();

        businessRegPageNavigator = findViewById(R.id.businessRegPageNavigator);
        LoginBtn = findViewById(R.id.BusinessLoginBtn);

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
        EtEmail = findViewById(R.id.EmailBL);
        EtPassword = findViewById(R.id.PasswordBL);

        // initializing TextInputLayout
        layoutEmail = findViewById(R.id.EmailBLLayout);
        layoutPassword = findViewById(R.id.PasswordBLLayout);

        // initializing buttons/ hyperlinked text
        PasswordReset = findViewById(R.id.BusinessForgotPassword);
        LoginBtn = findViewById(R.id.BusinessLoginBtn);
        businessRegPageNavigator = findViewById(R.id.businessRegPageNavigator);

        // ProgressDialog creating...
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Validating...");
        progressDialog.setMessage("Please wait.... ");
        progressDialog.setCancelable(false);

        mAuth = FirebaseAuth.getInstance();

        mHelper = UserDBHelper.getInstance(this, 1);
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
        PasswordReset.setOnClickListener(View -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Forgot Password...?")
                    .setCancelable(false)
                    .setMessage("Please provide your registered email.")
                    .setIcon(R.drawable.ic_profile);

            LinearLayout linearLayout = new LinearLayout(this);
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
                        mHelper.updateKey(UserDBHelper.TABLE_Business,info,"business_password",str);
                    }

//                    String EmailRegex = "^(.+)@(.+)$";
//                    Pattern EmailPattern = Pattern.compile(EmailRegex);
//                    Matcher EmailMatcher = EmailPattern.matcher(email);
//                    if (!email.isEmpty() && EmailMatcher.matches()) {
//                        beginRecovery(email);
//                    } else {
//                        new SweetAlertDialog(BusinessLoginActivity.this, SweetAlertDialog.ERROR_TYPE)
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

        businessRegPageNavigator.setOnClickListener(View -> {
            Intent intent = new Intent(BusinessLoginActivity.this, BusinessRegisterActivity.class);
            startActivity(intent);
            finish();
        });

    }

    /**
     * 数据库 登录
     **/
    private void DatabaseLogin(){
        progressDialog.dismiss();

        String LoginIn_Email = EtEmail.getText().toString();
        sql = mHelper.openReadLink();
        ArrayList<UserInfo> infoArrayList = mHelper.query(UserDBHelper.TABLE_Business,String.format("business_email='%s'",LoginIn_Email));
        if (!infoArrayList.isEmpty()){

            Intent in = new Intent(BusinessLoginActivity.this, BusinessHomePage.class);
            in.putExtra("LoginIn_Email",LoginIn_Email);
            startActivity(in);
            finish();
        }else{
            new SweetAlertDialog(BusinessLoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Invalid username or password")
                    .setContentText("Please Enter Correct username or password")
                    .show();
            EtPassword.setText("");
            Log.e(TAG, "onComplete: Invalid username or password");
        }

    }

    private void beginRecovery(String email) {
        loadingBar = new ProgressDialog(this);
        loadingBar.setMessage("Sending Email....");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();
                if (task.isSuccessful()) {
                    new SweetAlertDialog(BusinessLoginActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Successfully sent mail")
                            .setContentText("Password reset link sent on your email.\nIf mail not visible inside Inbox then check the Spam folder")
                            .setConfirmText("OK")
                            .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                            .show();
                } else {
                    new SweetAlertDialog(BusinessLoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Invalid Email..!")
                            .setContentText("Please provide Registered Email! ")
                            .show();
                }
            }
        }).addOnFailureListener(e -> {
            loadingBar.dismiss();
            Toast.makeText(BusinessLoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                            if (userType.equals("MessOwner")) {
                                Login();
                            } else {
                                progressDialog.dismiss();
                                new SweetAlertDialog(BusinessLoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Oops...")
                                        .setContentText("You can't able to use this account as a business account")
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
                    new SweetAlertDialog(BusinessLoginActivity.this, SweetAlertDialog.ERROR_TYPE)
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
//                    GetAddress();

                    Intent in = new Intent(BusinessLoginActivity.this, BusinessHomePage.class);
                    startActivity(in);
                    finish();

                } else {
                    progressDialog.dismiss();
                    new SweetAlertDialog(BusinessLoginActivity.this, SweetAlertDialog.WARNING_TYPE)
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

}
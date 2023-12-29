package my.edu.utar.moneyforest.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import my.edu.utar.moneyforest.R;


/*Done by Grace Lai Meng Huey
 For registration, user is required to enter their email address and password. User is also
 required to tick on the checkbox to agree with the privacy policy which they can view the
 details of the policy by clicking the blue colour text. After registration, user will be
 brought into Main Activity. There also will be a “Sign in” text for user to click to enter
 the Login Activity if he or she wishes to do so.  */


public class RegisterActivity extends AppCompatActivity {

    private Button registerBtn;
    private TextView loginTv, policy;
    private EditText nameInput, pwdInput, cnfPwdInput;
    private FirebaseAuth mAuth;
    private CheckBox policyCb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerBtn = findViewById(R.id.registerBtn);
        loginTv = findViewById(R.id.signin);
        nameInput = findViewById(R.id.nameRegister);
        pwdInput = findViewById(R.id.pwdRegister);
        cnfPwdInput = findViewById(R.id.confirmRegister);
        policyCb = findViewById(R.id.checkBoxPolicy);
        policy = findViewById(R.id.policy);
        mAuth = FirebaseAuth.getInstance();


        policy.setTextColor(Color.BLUE);
        policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.termsfeed.com/live/9e050bed-4efc-4efc-bdd3-8766ed02e826"));
                startActivity(intent);
            }
        });

        loginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userName = nameInput.getText().toString();
                String pwd = pwdInput.getText().toString();
                String cnfPwd = cnfPwdInput.getText().toString();


                if (!pwd.equals(cnfPwd)){
                    Toast.makeText(RegisterActivity.this, "Both passwords are not same",Toast.LENGTH_SHORT).show();

                }
                else if (TextUtils.isEmpty(userName) ||
                        TextUtils.isEmpty(pwd) ||
                        TextUtils.isEmpty(cnfPwd )){

                    Toast.makeText(RegisterActivity.this, "Please add your credentials",Toast.LENGTH_SHORT).show();

                }
                else if (!policyCb.isChecked()){
                    Toast.makeText(RegisterActivity.this, "Please tick to agree the privacy policy",Toast.LENGTH_SHORT).show();

                }
                else{
                    ProgressBar progressBar = findViewById(R.id.progressBar);
                    progressBar.setVisibility(View.VISIBLE);
                    v.setVisibility(View.GONE);
                    mAuth.createUserWithEmailAndPassword(userName,pwd)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        FirebaseUser newUser = mAuth.getCurrentUser();
                                        String userId = newUser.getUid(), email = newUser.getEmail();

                                        // Split the email address at the "@" symbol
                                        String[] parts = email.split("@");
                                        String name = parts[0];

                                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("user");
                                        DatabaseReference newUserRef = usersRef.child(userId);

                                        Date currentDate = new Date();

                                        //Create new user account
                                        User user = new User(userId, 0, "", 0, currentDate.toString(), name, null, 0, 0);
                                        newUserRef.setValue(user);

                                        DatabaseReference dbReference_challenge;
                                        DatabaseReference dbReference_course;
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        dbReference_course = database.getReference("course");

                                        dbReference_challenge = database.getReference("challenge");

                                        Handler handler = new Handler(Looper.getMainLooper()) {
                                            private int totalsucc = 0;
                                            @Override
                                            public void handleMessage(Message message) {
                                                if (message.what == 0) {
                                                    int taskId = message.arg1;
                                                    int result = message.arg2;

                                                    if (result == 0) {
                                                        totalsucc++;
                                                        if(totalsucc == 2){
                                                            Toast.makeText(RegisterActivity.this, "Registered successfully",Toast.LENGTH_SHORT).show();
                                                            Intent intent1 = new Intent(RegisterActivity.this, LoginActivity.class);
                                                            startActivity(intent1);
                                                            finish();
                                                        }
                                                    }}
                                            }
                                        };

                                        dbReference_challenge.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {
                                                ArrayList<Object> itemData = (ArrayList<Object> ) snapshot.getValue();

                                                DatabaseReference ref = database.getReference("user_challenge").child(userId);
                                                ref.setValue(itemData);

                                                Message message = handler.obtainMessage(0, 0, 0);
                                                handler.sendMessage(message);

                                                dbReference_course.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot snapshot) {
                                                        try {
                                                            ArrayList<Object> itemData2 = (ArrayList<Object>) snapshot.getValue();

                                                            DatabaseReference ref = database.getReference("user_course").child(userId);
                                                            ref.setValue(itemData2);

                                                            Message message = handler.obtainMessage(0, 0, 0);
                                                            handler.sendMessage(message);

                                                        } catch (Exception ex) {
                                                            ex.printStackTrace();
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(DatabaseError error) {
                                                        System.out.println(error);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError error) {
                                            }
                                        });

                                    }
                                    else{
                                        String errorMessage = task.getException().getMessage();

                                        Toast.makeText(RegisterActivity.this, errorMessage,Toast.LENGTH_SHORT).show();

                                        progressBar.setVisibility(View.GONE);
                                        v.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                }
            }
        });

    }


}
package my.edu.utar.moneyforest.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import my.edu.utar.moneyforest.R;
import my.edu.utar.moneyforest.SplashScreenActivity;

/*In this activity, it requires user to input their email address and password
in order to continue using the app by going to the Main Activity.
We also provide Google and Facebook authentication for user to login or sign up the app.
If the user does not have an account yet and wishes to register for it,
user can click on the “Sign up” text to go into Register Activity for sign up. */
/*Done by Grace Lai Meng Huey*/

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn;
    private EditText nameInput, pwdInput;
    private TextView registerTv;
    private ImageView google, fb;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        google = findViewById(R.id.google);
        fb = findViewById(R.id.fb);
        mAuth = FirebaseAuth.getInstance();
        loginBtn = findViewById(R.id.loginBtn);
        registerTv = findViewById(R.id.signup);
        nameInput = findViewById(R.id.nameLogin);
        pwdInput = findViewById(R.id.pwdLogin);


        //Google sign in configuration
        GoogleSignInOptions signInRequest = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this,signInRequest);


        google.setOnClickListener((View.OnClickListener) view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            startActivityForResult(intent,100);
        });


        registerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = nameInput.getText().toString();
                String pwd = pwdInput.getText().toString();

                if (TextUtils.isEmpty(userName) ||
                        TextUtils.isEmpty(pwd)){

                    Toast.makeText(LoginActivity.this, "Please enter your credentials",Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    ProgressBar progressBar = findViewById(R.id.progressBar);
                    progressBar.setVisibility(View.VISIBLE);
                    v.setVisibility(View.GONE);

                    mAuth.signInWithEmailAndPassword(userName,pwd)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(LoginActivity.this, "Login successfully",Toast.LENGTH_SHORT).show();
                                        Intent intent1 = new Intent(LoginActivity.this, SplashScreenActivity.class);
                                        startActivity(intent1);
                                        finish();
                                    }
                                    else{
                                        Toast.makeText(LoginActivity.this, "No such user",Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                        v.setVisibility(View.VISIBLE);

                                    }
                                }
                            });
                }
            }
        });

        //FB sign in
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, FBAuthentication.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }


    // check to see if the user is currently signed in.
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            Intent intent = new Intent(LoginActivity.this,SplashScreenActivity.class);
            startActivity(intent);
            this.finish();
        }
    }



    //Google sign in
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {

            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);

            if (signInAccountTask.isSuccessful()) {
                // When google sign in successful initialize string
                String s = "Google sign in successful";
                Toast.makeText(LoginActivity.this, s, Toast.LENGTH_SHORT).show();

                // Initialize sign in account
                try {
                    // Initialize sign in account
                    GoogleSignInAccount googleSignInAccount = signInAccountTask.getResult(ApiException.class);


                    if (googleSignInAccount != null) {
                        // When sign in account is not equal to null initialize auth credential
                        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                        // Check credential
                        mAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete( Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    FirebaseUser newUser = mAuth.getCurrentUser();
                                    String userId = newUser.getUid(), email = newUser.getEmail();

                                    // Split the email address at the "@" symbol
                                    String[] parts = email.split("@");
                                    String name = parts[0];

                                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("user");
                                    DatabaseReference newUserRef = usersRef.child(userId);

                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    String todayDate = dateFormat.format(new Date());

                                    User user = new User(userId, 0, "", 0, todayDate, name, null, 0, 0);
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
                                                        // When task is successful redirect to profile activity display Toast
                                                        startActivity(new Intent(LoginActivity.this, SplashScreenActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                                        Toast.makeText(LoginActivity.this, "Authentication success", Toast.LENGTH_SHORT).show();
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



                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed :" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }else {
                // Task failed
                Exception exception = signInAccountTask.getException();
                if (exception != null) {
                    Log.e("SignInError", "Error signing in", exception);
                }
            }
        }
    }

}
package my.edu.utar.moneyforest;

import android.content.Intent;
import android.os.Handler;

import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import my.edu.utar.moneyforest.user.LoginActivity;

/*Done by Grace Lai Meng Huey
Once the user enters the app, there will be a splash screen showcasing our brand logo and name.
The duration of the splash screen is carefully timed to balance brand exposure and user convenience.
In this app, we implement Firebase. When the app is launched, it will check whether the user
has logged in previously. If so, it will navigate user to Main Activity. Otherwise,
the user will be brought into Login Activity. . */

public class SplashScreenActivity extends AppCompatActivity {


    Animation move;
    ImageView logo;
    TextView name;
    private String userId;


    private ExecutorService executor;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash_screen);

        move = AnimationUtils.loadAnimation(this, R.anim.logo);

        logo = findViewById(R.id.logo);
        name = findViewById(R.id.name);

        logo.setAnimation(move);
        name.setAnimation(move);

        executor = Executors.newSingleThreadExecutor();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            try {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true); // must call first
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            mDatabase = FirebaseDatabase.getInstance().getReference();
            //==== Load Challenge Lists, if exists in SharedPreferences, load it, otherwise,
            //==== Retrieve from firebase.


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 1500);

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 1500);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

}
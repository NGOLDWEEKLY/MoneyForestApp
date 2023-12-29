package my.edu.utar.moneyforest;


import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import my.edu.utar.moneyforest.challenge.ChallengeFragment;
import my.edu.utar.moneyforest.course.HomeFragment;
import my.edu.utar.moneyforest.user.LeaderBoardFragment;
import my.edu.utar.moneyforest.user.NotificationFragment;
import my.edu.utar.moneyforest.user.ProfileFragment;

/* Done by Grace Lai Meng Huey
The activity will display the bottom navigation bar for user to choose
whether he or she wishes to view which content. It also contains a fragment
to display the content chosen by the user. */

public class MainActivity extends AppCompatActivity {

    // to force activity refresh
    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(bottomNavigationView.getSelectedItemId());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.trophy:
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment, new LeaderBoardFragment())
                                .commit();
                        return true;

                    case R.id.challenge:
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment, new ChallengeFragment())
                                .commit();
                        return true;

                    case R.id.home:
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment, new HomeFragment())
                                .commit();
                        return true;

                    case R.id.notification:
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment, new NotificationFragment())
                                .commit();
                        return true;

                    case R.id.profile:
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment, new ProfileFragment())
                                .commit();
                        return true;
                }
                return false;
            }
        });

        bottomNavigationView.setSelectedItemId(R.id.home);
        if (false) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, new HomeFragment())
                    .commit();
            bottomNavigationView.setVisibility(View.GONE);
        }


    }
}
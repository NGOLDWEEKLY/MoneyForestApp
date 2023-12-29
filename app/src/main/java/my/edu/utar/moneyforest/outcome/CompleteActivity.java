package my.edu.utar.moneyforest.outcome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import my.edu.utar.moneyforest.R;
import my.edu.utar.moneyforest.challenge.ChallengeOutcomeActivity;
import my.edu.utar.moneyforest.challenge.ChallengeResult;
import my.edu.utar.moneyforest.course.Course;

/*Done by Ng Jing Ying, Wai Jia Le*/
/*This is a helper class to bridge the challenge or course activity with the main activity.
 * In this class, it will display the congratulation page of course/challenge completion
 * Then, navigate to the respective activity.
 * */
public class CompleteActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private TextView headingTV;
    private TextView briefTV;
    private ImageView imageView;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete);

        headingTV = (TextView) findViewById(R.id.headingTV);
        briefTV = (TextView) findViewById(R.id.briefTV);
        imageView = (ImageView) findViewById(R.id.imageView);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            userId = "guest";
        }

        Intent prevIntent = getIntent();
        String type = prevIntent.getStringExtra("type");
        //==== SECTION 1.1 If it is a Challenge Completion===//
        if (type.equals("challenge")) {
            ChallengeResult chalRes = (ChallengeResult) prevIntent.getSerializableExtra("chalRes");
            // Reward XPs
            DatabaseReference userXPRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("xp");
            userXPRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        DatabaseReference userXPWriteRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("xp");
                        userXPWriteRef.setValue((Long) (snapshot.getValue()) + chalRes.getXP());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
            // Reward MT Coins
            DatabaseReference userMTCoinRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("coin");
            userMTCoinRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        DatabaseReference userMTCoinWriteRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("coin");
                        userMTCoinWriteRef.setValue((Long) (snapshot.getValue()) + (chalRes.getCoin()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
            if (chalRes.getOutcome() == ChallengeResult.WIN) {
                headingTV.setText("You Win");
                briefTV.setText("Congratulation! You have done a good job!");
                imageView.setImageResource(R.drawable.win);
            } else {
                headingTV.setText("You Lose");
                briefTV.setText("Uh oh... No worries, let's see how we can improve next round!");
                imageView.setImageResource(R.drawable.lose);
            }
        }
        //==== SECTION 1.2 If it is a Course Completion===//
        else if (type.equals("course")) {
            Course course = (Course) prevIntent.getSerializableExtra("course");
            long status = course.updateProgress(prevIntent.getIntExtra("section_id", 0));
            DatabaseReference userXPRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("xp");
            userXPRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        DatabaseReference userXPWriteRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("xp");
                        userXPWriteRef.setValue((Long) (snapshot.getValue()) + 20);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            if (status == 0) {
                databaseReference = database.getReference("user_course").child(userId).child("" + course.getCourse_id());
                databaseReference.setValue(course);
            } else if (status == Course.COURSE_DONE_FLAG) {
                databaseReference = database.getReference("user_course").child(userId).child("" + (course.getCourse_id() + 1)).child("course_sections")
                        .child("0").child("status");
                databaseReference.setValue(1);
            }
            headingTV.setText("Complete!");
            briefTV.setText("Congratulation! You have finished a chapter and gained 20 XP!");
            imageView.setImageResource(R.drawable.win);
        }
        //==== SECTION 1.3 If it is a Challenge-embedded Course Completion===//
        else {
            ChallengeResult chalRes = (ChallengeResult) prevIntent.getSerializableExtra("chalRes");
            // Reward XPs
            DatabaseReference userXPRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("xp");
            userXPRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        DatabaseReference userXPWriteRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("xp");
                        userXPWriteRef.setValue((Long) (snapshot.getValue()) + chalRes.getXP());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
            // Reward MT Coins
            DatabaseReference userMTCoinRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("coin");
            userMTCoinRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        DatabaseReference userMTCoinWriteRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("coin");
                        userMTCoinWriteRef.setValue((Long) (snapshot.getValue()) + (chalRes.getCoin()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
            if (chalRes.getOutcome() == ChallengeResult.WIN) {
                headingTV.setText("You Win");
                briefTV.setText("Congratulation! You have done a good job!");
                imageView.setImageResource(R.drawable.win);
            } else {
                headingTV.setText("You Lose");
                briefTV.setText("Uh oh... No worries, let's see how we can improve next round!");
                imageView.setImageResource(R.drawable.lose);
            }
            Course course = (Course) prevIntent.getSerializableExtra("course");
            long status = course.updateProgress(prevIntent.getIntExtra("section_id", 0));
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            databaseReference = database.getReference("user_course").child(userId).child("" + course.getCourse_id());
            databaseReference.setValue(course);
            if (status == Course.COURSE_DONE_FLAG) {
                databaseReference = database.getReference("user_course").child(userId).child("" + (course.getCourse_id() + 1)).child("course_sections")
                        .child("0").child("status");
                databaseReference.setValue(1);
            }
        }

        Button continueBtn = (Button) findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type.equals("course")) {
                    finish();
                } else {
                    Intent intent = new Intent(CompleteActivity.this, ChallengeOutcomeActivity.class);
                    ChallengeResult chalRes = (ChallengeResult) prevIntent.getSerializableExtra("chalRes");
                    intent.putExtra("chalRes", chalRes);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }
            }
        });
    }
}
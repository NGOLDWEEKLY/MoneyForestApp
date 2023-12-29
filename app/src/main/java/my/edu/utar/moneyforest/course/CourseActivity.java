package my.edu.utar.moneyforest.course;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import my.edu.utar.moneyforest.outcome.CompleteActivity;
import my.edu.utar.moneyforest.R;
import my.edu.utar.moneyforest.challenge.UserChallenge;

/*Done by Wai Jia Le*/
/*Show the Contents of the chapter and Quiz*/
//All the content and quiz are rendered from HTML using WebView. (It is convenient and time-saving)
public class CourseActivity extends AppCompatActivity {

    private Context context;
    private LinearLayout course_content_ll;
    private WebView course_content_wv;
    private LinearLayout previous_next_ll;
    private ImageButton backBtn;
    private Button prevBtn, nextBtn;
    private Intent this_page;
    private TextView section_title;


    private String userId;
    private DatabaseReference dbReference_course_section;
    private DatabaseReference dbReference_course_section1;
    private DatabaseReference getDbReference_setUnlock;
    private int curDispIndex;
    private boolean pageLocked = false;
    private int course_id;
    private int section_id;
    private String section_name;
    private int edit_ChildLocation;
    private int edit_LockLocation;
    private Course course;
    private int lastSection;

    private String quizQuestion;
    private String quizFeedBack;
    private int quizAnswer;

    private List<UserChallenge> chals = new ArrayList<>();
    private RecyclerView challenge_rv;
    private ArrayList<String> contentList;
    private Handler handler;
    private Handler getLocationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_course);
        context = this;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            userId = "guest";
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbReference_course_section = database.getReference("course_section");
        dbReference_course_section.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                //get Intent message from previous intent
                this_page = getIntent();
                course_id = Integer.parseInt(this_page.getStringExtra("Course_ID"));
                section_id = Integer.parseInt(this_page.getStringExtra("Section_ID"));
                section_name = this_page.getStringExtra("Section_Name");
                lastSection = this_page.getIntExtra("Last_Section", 0);
                course = (Course) this_page.getSerializableExtra("course");
                //get the course section data from the firebase, including the quiz ( The data is in HTML form)
                getCourse_Section_Data(this_page, snapshot);

                Message message = handler.obtainMessage(0, 0, 0);
                handler.sendMessage(message);


            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                curDispIndex = 0; //this variable is used to check the current index of the view, so can implement the previous and next function

                WebSettings webSettings = course_content_wv.getSettings();
                webSettings.setJavaScriptEnabled(true);
                String styledHtmlContent = "<style> @import url('https://fonts.googleapis.com/css2?family=DM+Sans:opsz@9..40&display=swap'); </style>" + contentList.get(0);
                course_content_wv.loadDataWithBaseURL(null, styledHtmlContent, "text/html", "utf-8", null);

                backBtn = (ImageButton) findViewById(R.id.courseBackBtn);

                section_title = findViewById(R.id.section_title);
                section_title.setText(section_name);

                prevBtn = findViewById(R.id.button_previous);
                nextBtn = findViewById(R.id.button_next);
                backBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (pageLocked) {
                        } // this will check whether the quiz is finished or not, this variable will be modified by the WebAppInterface Class
                        else if (curDispIndex > 0) {
                            curDispIndex--;
                            WebSettings webSettings = course_content_wv.getSettings();
                            webSettings.setJavaScriptEnabled(true);
                            String styledHtmlContent = "<style> @import url('https://fonts.googleapis.com/css2?family=DM+Sans:opsz@9..40&display=swap'); </style>" + contentList.get(curDispIndex);
                            // Attach the JavaScript interface
                            course_content_wv.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");
                            course_content_wv.loadDataWithBaseURL(null, styledHtmlContent, "text/html", "utf-8", null);
                        }
                    }
                });
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (pageLocked) {
                        } else if (curDispIndex < contentList.size() - 1) {
                            curDispIndex++;
                            WebSettings webSettings = course_content_wv.getSettings();
                            webSettings.setJavaScriptEnabled(true);
                            String styledHtmlContent = "<style> @import url('https://fonts.googleapis.com/css2?family=DM+Sans:opsz@9..40&display=swap'); </style>" + contentList.get(curDispIndex);
                            // Attach the JavaScript interface
                            course_content_wv.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");
                            course_content_wv.loadDataWithBaseURL(null, styledHtmlContent, "text/html", "utf-8", null);
                        } else {
                            Intent intent = new Intent(CourseActivity.this, CompleteActivity.class);
                            intent.putExtra("course", course);
                            intent.putExtra("type", "course");
                            intent.putExtra("section_id", section_id);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        };
        course_content_ll = findViewById(R.id.course_content);
        previous_next_ll = findViewById(R.id.button_ll);
        course_content_wv = findViewById(R.id.course_content_wv);

    }

    //get the related course section data
    public void getCourse_Section_Data(Intent this_page, DataSnapshot snapshot) {
        int section_id = Integer.parseInt(this_page.getStringExtra("Section_ID"));
        lastSection = this_page.getIntExtra("Last_Section", 0);

        quizQuestion = "";
        quizFeedBack = "";
        quizAnswer = 0;

        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {

            Map<String, Object> itemData = (Map<String, Object>) itemSnapshot.getValue();
            if (itemData != null) {
                if (itemData.get("course_section_id").toString().equals(String.valueOf(section_id))) {
                    contentList = (ArrayList<String>) itemData.get("content");
                }

            }

        }
    }

    //Called by javascript automatically
    public class WebAppInterface {

        @JavascriptInterface
        public void onValueReceived(String value) {
            // get the value from javascript and process it
            CourseActivity.this.handleValueFromWebView(value);
        }
    }

    //For certain page like Quiz, page will be unlocked by javascript
    //If user has answered the questions
    public void handleValueFromWebView(String value) {
        if (value.equals("0")) { //if no value received, locked it
            pageLocked = true;
        } else //otherwise
            pageLocked = false;
    }


}
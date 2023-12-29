package my.edu.utar.moneyforest.course;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import my.edu.utar.moneyforest.R;
import my.edu.utar.moneyforest.user.User;
import my.edu.utar.moneyforest.challenge.UserChallenge;
import my.edu.utar.moneyforest.challenge.Challenge;
import my.edu.utar.moneyforest.challenge.ChallengeActivity;

/*Done by Wai Jia Le*/
/*This fragment will display all the available courses */
//When a section(chapter) or challenge is clicked, it will navigate to the CourseActivity.class or ChallengeActivity.class

public class HomeFragment extends Fragment {

    public ExpandableListView expandableListViewExample;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableTitleList;
    LinkedHashMap<String, List<String>> expandableDetailList;
    private Context context;
    private View view;
    private Spinner curr_course_lvl;


    final String Beginner = "Beginner";
    final String Intermediate = "Intermediate";
    final String Advanced = "Advanced";
    private String lvl_choice = Beginner;


    private List<Challenge> chal_lst;
    private Handler handler;
    private String userId;
    private DatabaseReference dbReference_course;
    private DatabaseReference dbReference_challenge;


    private double beginnerProgressTotal = 0;
    private double intermediateProgressTotal = 0;
    private double advancedProgressTotal = 0;
    private double progressTotal[] = new double[3];
    private ProgressBar pb;
    private TextView pb_tv;

    private List<Course> course_list;
    private int course_num[] = new int[3];
    private List<Challenge> chals = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = container.getContext();
        view = inflater.inflate(R.layout.fragment_home, container, false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            userId = "guest";
        }

        //read the data that are stored in the variable
        handler = new Handler(Looper.getMainLooper()) {
            private int successCnt = 0;

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    int taskId = msg.arg1;
                    int result = msg.arg2;
                    if (result == 0) {

                        //reinitialize the progress
                        beginnerProgressTotal = 0;
                        intermediateProgressTotal = 0;
                        advancedProgressTotal = 0;

                        successCnt++;
                        if (successCnt == 2) {

                            //initialize the view
                            curr_course_lvl = view.findViewById(R.id.course_lvl);
                            List<String> levels = Arrays.asList(Beginner, Intermediate, Advanced);

                            //Spinner for choosing the level of courses
                            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, R.layout.curr_course_level_spinner_layout, levels);
                            curr_course_lvl.setAdapter(spinnerAdapter);
                            curr_course_lvl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view1, int i, long l) {
                                    boolean load = false;

                                    TextView course_title = (TextView) view.findViewById(R.id.course_title_tv);
                                    Message message = handler.obtainMessage(0, 0, 1);

                                    //This is the first level and never be locked
                                    if (i == 0) {
                                        course_title.setText("Courses");
                                        lvl_choice = Beginner;
                                        load = true;
                                    }

                                    //check the previous level is complete or not
                                    else if ((progressTotal[i - 1] == course_num[i - 1])) {
                                        if (i == 1) {
                                            course_title.setText("Courses");
                                            lvl_choice = Intermediate;
                                            load = true;
                                        } else if (i == 2) {
                                            course_title.setText("Unavailable");
                                            lvl_choice = Advanced;
                                        }

                                    } else { //if the previous level is not complete, hide everything
                                        course_title.setText("Unavailable");
                                        pb.setVisibility(View.INVISIBLE);
                                        pb_tv.setVisibility(View.INVISIBLE);
                                        expandableListViewExample.setVisibility(View.INVISIBLE);
                                        Toast.makeText(getContext(), "You must complete the current level of course to advance!", Toast.LENGTH_SHORT).show();
                                    }

                                    // if the previous level is complete, Reload the Course Data for the new level and Unlock the new level
                                    if (load == true) {
                                        pb.setVisibility(View.VISIBLE);
                                        pb_tv.setVisibility(View.VISIBLE);
                                        expandableListViewExample.setVisibility(View.VISIBLE);

                                        setCourseListData();
                                        showCourse();

                                        pb.setProgress((int) (progressTotal[i] / (0.0 + course_num[i]) * 100));
                                    }
                                }


                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });

                            setCourseListData();
                            showCourse();

                            pb_tv = (TextView) view.findViewById(R.id.progress_bar_tv);
                            //create hte progress bar to show the current progress of the level
                            pb = (ProgressBar) view.findViewById(R.id.levelPB);
                            pb.setProgress((int) (progressTotal[0] / (0.0 + course_num[0]) * 100));


                        }
                    }
                }
            }
        };

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbReference_course = database.getReference("user_course").child(userId);
        dbReference_challenge = database.getReference("user_challenge").child(userId);

        //get and store the Course data from the Firebase
        dbReference_course.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println("XXX");
                course_list = new ArrayList<>();

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {

                    Map<String, Object> itemData = (Map<String, Object>) itemSnapshot.getValue();
                    if (itemData != null) {
                        getDataFromFirebase(itemData);
                    }
                }
                Message message = handler.obtainMessage(0, 0, 0);
                handler.sendMessage(message);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        //get and store the Challenge data from the Firebase
        dbReference_challenge.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                chal_lst = new ArrayList<>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Map<String, Object> itemDataChal = (Map<String, Object>) itemSnapshot.getValue();
                    if (itemDataChal != null) {
                        try {
                            String chal_history = "{}";
                            if (itemDataChal.get("chalHistory") != null) {
                                chal_history = itemDataChal.get("chalHistory").toString();
                            }
                            Challenge itemChal = new Challenge(
                                    Integer.parseInt(itemDataChal.get("id").toString()),
                                    itemDataChal.get("name").toString(),
                                    itemDataChal.get("desc").toString(),
                                    itemDataChal.get("content").toString(),
                                    itemDataChal.get("brief").toString(),
                                    Integer.parseInt(itemDataChal.get("progress").toString()),
                                    itemDataChal.get("chalImg").toString(),
                                    chal_history,
                                    (boolean) itemDataChal.get("popular"));
                            if (itemDataChal.get("apikey") != null)
                                itemChal.setApikey(itemDataChal.get("apikey").toString());
                            chal_lst.add(itemChal);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                Message message = handler.obtainMessage(0, 0, 0);
                handler.sendMessage(message);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    public void getDataFromFirebase(Map<String, Object> data) {
        try {
            int course_id = Integer.parseInt(data.get("course_id").toString());
            int course_lvl = Integer.parseInt(data.get("course_lvl").toString());
            double course_progress = Double.parseDouble(data.get("course_progress").toString());
            if (course_lvl == 1) {
                beginnerProgressTotal += course_progress;
            } else if (course_lvl == 2) {
                intermediateProgressTotal += course_progress;
            } else if (course_lvl == 3) {
                advancedProgressTotal += course_progress;
            }
            course_num[course_lvl - 1]++;
            progressTotal[course_lvl - 1] += course_progress;
            String course_name = data.get("course_name").toString();
            List<Course_Section> course_sections = new ArrayList<Course_Section>();

            for (Map<String, Object> sectionData : ((ArrayList<Map<String, Object>>) (data.get("course_sections")))) {
                Course_Section section = new Course_Section();
                section.setId((String) sectionData.get("id"));
                section.setName((String) sectionData.get("name"));
                section.setIs_chal((boolean) sectionData.get("is_chal"));
                section.setStatus((long) sectionData.get("status"));
                course_sections.add(section);
            }

            Course course = new Course(course_id, course_name, course_lvl, 1, course_progress, "Sarah", course_sections);

            course_list.add(course);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setCourseListData() {

        int course_lvl = 0;
        if (lvl_choice.equals(Beginner))
            course_lvl = 1;
        else if (lvl_choice.equals(Intermediate))
            course_lvl = 2;
        else if (lvl_choice.equals(Advanced))
            course_lvl = 3;

        expandableDetailList = new LinkedHashMap<String, List<String>>();
        for (int i = 0; i < course_list.size(); i++) {
            if (course_list.get(i).getCourse_lvl() == course_lvl) {
                List<Course_Section> csLists = course_list.get(i).getCourse_sections();
                List<String> csStrLists = new ArrayList<>();
                for (int j = 0; j < chal_lst.size(); j++) {
                    if (Integer.parseInt(course_list.get(i).getCourse_sections().get(i).getId()) == chal_lst.get(j).getId()) {
                        course_list.get(i).setChal(chal_lst.get(j));
                    }
                }
                for (int j = 0; j < csLists.size(); j++) {
                    csStrLists.add(csLists.get(j).getName());
                }
                expandableDetailList.put(course_list.get(i).getCourse_name(), csStrLists);
            }
        }
        System.out.println("");
    }

    //show the Courses using expandablelist
    public void showCourse() {
        expandableListViewExample = (ExpandableListView) view.findViewById(R.id.expandableListViewSample);
        if (expandableDetailList.size() > 0) {
            expandableListViewExample.setVisibility(View.VISIBLE);
            expandableTitleList = new ArrayList<String>(expandableDetailList.keySet());
            expandableListAdapter = new CusExpandableListAdapter(view.getContext(), expandableTitleList, expandableDetailList, view, course_list);
            expandableListViewExample.setAdapter(expandableListAdapter);
            expandableListViewExample.expandGroup(0);
            expandableListViewExample.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {

                    Course_Section cs = course_list.get(i).getCourse_sections().get(i1);

                    if (cs.getStatus() != Course.SECTION_LOCKED) { //check the lock of each the section
                        if (cs.isIs_chal()) { //check whether is challenge module
                            UserChallenge itemUC = new UserChallenge();
                            itemUC.setUser(new User(userId));
                            itemUC.setChallenge(course_list.get(i).getChal());
                            Intent intent = new Intent(context, ChallengeActivity.class);
                            intent.putExtra("userChal", itemUC);
                            intent.putExtra("course", course_list.get(i));
                            intent.putExtra("type", "course");
                            context.startActivity(intent);

                        } else { //Section module, if the section is not locked, open a new intent for the chapter
                            Intent courseContent = new Intent(context, CourseActivity.class);
                            courseContent.putExtra("Course_ID", String.valueOf(course_list.get(i).getCourse_id()));
                            courseContent.putExtra("Section_ID", cs.getId());
                            courseContent.putExtra("Section_Name", cs.getName());
                            courseContent.putExtra("course", course_list.get(i));
                            context.startActivity(courseContent);
                        }
                    } else {
                        Toast.makeText(getContext(), "You must complete the preceding task to proceed!", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        } else {
            expandableListViewExample.setVisibility(View.INVISIBLE);
        }
    }
}

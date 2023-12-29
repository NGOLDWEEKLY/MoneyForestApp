package my.edu.utar.moneyforest.user;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import my.edu.utar.moneyforest.R;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/*This fragment is to display leaderboard of user rankings
* that will redisplay once there is change detected in the firebase*/

//done by Wong Tze-Qing, Sarah
public class LeaderBoardFragment extends Fragment {

    private DatabaseReference databaseReference;
    private Handler handler;
    private ArrayList<User> userArr = new ArrayList<>();
    private ValueEventListener databaseListener;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_leader_board, container, false);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("user");
        databaseReference.keepSynced(true);

        databaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userArr = new ArrayList<User>();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    Map<String, Object> itemData = (Map<String, Object>) itemSnapshot.getValue();
                    try {
                        String pfp;
                        String rank, streak;
                        if (itemData.get("pfp") == null) pfp = "";
                        else
                            pfp = itemData.get("pfp").toString();
                        if (itemData.get("rank") == null) rank = "0";
                        else
                            rank = itemData.get("rank").toString();
                        if (itemData.get("streak") == null) streak = "0";
                        else
                            streak = itemData.get("streak").toString();
                        User user = new User(
                                itemSnapshot.getKey(),
                                Integer.parseInt(itemData.get("xp").toString()),
                                "",
                                0,
                                "",
                                itemData.get("name").toString(),
                                pfp,
                                Integer.parseInt(streak),
                                Integer.parseInt(rank)
                        );
                        userArr.add(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    //sort user according to highest XP
                    Collections.sort(userArr, new Comparator<User>() {
                        @Override
                        public int compare(User user, User t1) {
                            return (t1.getXp() - user.getXp());
                        }
                    });

                    //update data in database after sort users
                    updateData(userArr);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Message message = handler.obtainMessage(0, 0, 0);
                handler.sendMessage(message);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Failed to read user data: " + error.getMessage());
            }
        };
        databaseReference.addValueEventListener(databaseListener);

        handler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    int taskId = msg.arg1;
                    int result = msg.arg2;
                    if (result == 0) {
                        LinearLayout mainll = (LinearLayout) view.findViewById(R.id.mainll);

                        int max;
                        //check total users
                        if (userArr.size() > 10)
                            max = 10; //if >10, display 10 only
                        else
                            max = userArr.size(); //if <10, display all

                        //display users
                        try {
                            mainll.removeAllViews(); //remove existing information before display
                            for (int i = 0; i < max; i++) {
                                User user = userArr.get(i);
                                int xp = user.getXp();
                                String name = user.getName();
                                String pfp = user.getPfp();
                                //display profile picture for top 3 users only
                                if (i < 3) {
                                    String llID = "person" + (i + 1);
                                    String xpID = "_xp" + (i + 1);
                                    String nameID = "name" + (i + 1);

                                    int xpName = getResources().getIdentifier(xpID, "id", getActivity().getPackageName());
                                    int nameName = getResources().getIdentifier(nameID, "id", getActivity().getPackageName());
                                    int llName = getResources().getIdentifier(llID, "id", getActivity().getPackageName());

                                    TextView tvXP = view.findViewById(xpName);
                                    TextView tvname = view.findViewById(nameName);
                                    LinearLayout ll = view.findViewById(llName);

                                    tvXP.setText(xp + "XP");
                                    tvname.setText(name);
                                    Bitmap decodedBitmap;
                                    if (pfp != null && !pfp.isEmpty()) {
                                        byte[] decodedBytes = Base64.decode(pfp, Base64.DEFAULT);
                                        decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                    } else {
                                        decodedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.moneytree);
                                    }

                                    CircleImageView pic = new CircleImageView(getContext());
                                    pic.setImageBitmap(decodedBitmap);
                                    pic.setClipToOutline(true);
                                    pic.setBorderColor(getResources().getColor(R.color.medium_sea_green));
                                    pic.setBorderWidth(1);

                                    LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.MATCH_PARENT
                                    );
                                    pic.setLayoutParams(lp1);
                                    pic.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    pic.setPadding(20, 20, 20, 20);
                                    ll.addView(pic);
                                }

                                //only display name and xp value for players not in top 3
                                if (i > 2) {
                                    LinearLayout newll = new LinearLayout(getContext());

                                    LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
                                    newll.setLayoutParams(lp2);
                                    newll.setOrientation(LinearLayout.HORIZONTAL);

                                    ImageView star = new ImageView(getContext());
                                    String starName = "star" + (i + 1);
                                    int starid = getResources().getIdentifier(starName, "drawable", getActivity().getPackageName());
                                    star.setImageResource(starid);
                                    LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(0,
                                            LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                                    lp3.gravity = Gravity.CENTER;
                                    star.setLayoutParams(lp3);
                                    star.setPadding(10, 10, 10, 10);

                                    TextView nametv = new TextView(getContext());
                                    LinearLayout.LayoutParams lp4 = new LinearLayout.LayoutParams(0,
                                            LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                                    lp4.gravity = Gravity.CENTER; //layout gravity
                                    nametv.setLayoutParams(lp4);
                                    nametv.setGravity(Gravity.CENTER); //gravity
                                    nametv.setTextColor(Color.BLACK);
                                    nametv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                                    nametv.setTypeface(null, Typeface.BOLD);
                                    nametv.setText(name);

                                    TextView xptv = new TextView(getContext());
                                    LinearLayout.LayoutParams lp5 = new LinearLayout.LayoutParams(0,
                                            LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                                    lp5.gravity = Gravity.CENTER; //layout gravity
                                    xptv.setLayoutParams(lp5);
                                    xptv.setGravity(Gravity.CENTER); //gravity
                                    xptv.setTextColor(Color.BLACK);
                                    xptv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                                    xptv.setTypeface(null, Typeface.BOLD);
                                    xptv.setText("" + xp + "XP");

                                    newll.addView(star);
                                    newll.addView(nametv);
                                    newll.addView(xptv);
                                    mainll.addView(newll);
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        };

        return view;
    }

    private void updateData(List<User> userArr) {
        for (User user : userArr) {
            DatabaseReference userRef = databaseReference.child(user.getId());
            userRef.child("rank").setValue(user.getRank());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(databaseListener);
    }
}

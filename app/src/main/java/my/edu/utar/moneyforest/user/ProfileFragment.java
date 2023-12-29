package my.edu.utar.moneyforest.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import de.hdodenhof.circleimageview.CircleImageView;
import my.edu.utar.moneyforest.R;
import my.edu.utar.moneyforest.SplashScreenActivity;

/*The profile fragment is to display the details of the user and
allow navigation to other fragment or activity*/

//done by Wong Tze-Qing, Sarah
public class ProfileFragment extends Fragment {

    private DatabaseReference databaseReference;
    private ValueEventListener databaseListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference("user")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        displayProfile(view);

        ImageButton signOut = view.findViewById(R.id.signOutButton);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(requireContext(), SplashScreenActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
                Toast.makeText(getContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show();
            }
        });

        Button lbButton = view.findViewById(R.id.lbButton);
        lbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callLeaderboardFragment();
            }
        });

        Button budget = view.findViewById(R.id.budget);
        budget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CalculatorActivity.class);
                intent.putExtra("url", "https://www.calculator.net/budget-calculator.html");
                intent.putExtra("msg", "Launching Budget Calculator...");
                startActivity(intent);
            }
        });

        Button debt = view.findViewById(R.id.debt);
        debt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CalculatorActivity.class);
                intent.putExtra("url", "https://www.calculator.net/debt-payoff-calculator.html");
                intent.putExtra("msg", "Launching Debt Calculator...");
                startActivity(intent);
            }
        });

        Button savings = view.findViewById(R.id.savings);
        savings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CalculatorActivity.class);
                intent.putExtra("url", "https://www.calculator.net/savings-calculator.html");
                intent.putExtra("msg", "Launching Savings Calculator...");
                startActivity(intent);
            }
        });

        Button mortgage = view.findViewById(R.id.mortgage);
        mortgage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CalculatorActivity.class);
                intent.putExtra("url", "https://www.calculator.net/mortgage-calculator.html");
                intent.putExtra("msg", "Launching Mortgage Calculator...");
                startActivity(intent);
            }
        });

        return view;
    }

    private void callLeaderboardFragment() {
        LeaderBoardFragment lb = new LeaderBoardFragment();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment, lb);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    private void displayProfile(View view) {
        databaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user == null)
                        return;

                    String name = user.getName();
                    String joinDate = user.getJoinDate();
                    int xp = user.getXp();
                    String badge = user.getBadge();
                    String pfp = user.getPfp();
                    int streak = user.getStreak();
                    int rank = user.getRank();
                    int coin = user.getCoin();

                    LinearLayout pfpll = (LinearLayout) view.findViewById(R.id.pfpll);
                    TextView nametv = (TextView) view.findViewById(R.id.nametv);
                    TextView datetv = (TextView) view.findViewById(R.id.datetv);
                    TextView xptv = view.findViewById(R.id.xp);
                    TextView cointv = view.findViewById(R.id.coin);

                    //set profile picture
                    Bitmap decodedBitmap;
                    if (pfp != null && !pfp.isEmpty()) {
                        byte[] decodedBytes = Base64.decode(pfp, Base64.DEFAULT);
                        decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                    } else {
                        decodedBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.moneytree);
                    }

                    CircleImageView picPfp = new CircleImageView(getContext());
                    picPfp.setImageBitmap(decodedBitmap);
                    picPfp.setClipToOutline(true);
                    picPfp.setBorderColor(getResources().getColor(R.color.medium_sea_green));
                    picPfp.setBorderWidth(1);

                    LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    picPfp.setLayoutParams(lp1);
                    picPfp.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    picPfp.setPadding(10, 10, 10, 10);
                    pfpll.addView(picPfp);

                    nametv.setText(name);
                    datetv.setText("Joined " + joinDate);

                    LinearLayout badgell = (LinearLayout) view.findViewById(R.id.badge);

                    LinearLayout picll = new LinearLayout(getContext());
                    picll.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    picll.setLayoutParams(lp);
                    badgell.addView(picll);

                    LinearLayout textll = new LinearLayout(getContext());
                    picll.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams lp12 = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    textll.setLayoutParams(lp12);
                    badgell.addView(textll);

                    if (badge != null) {
                        int badgeNo = 0;
                        String[] allBadge = new String[1];

                        //check if got >1 badge
                        if (badge.contains(",")) {
                            allBadge = badge.split(",");
                            badgeNo = 2;
                        } else {
                            allBadge[0] = badge;
                            badgeNo = 1;
                        }

                        //check what kind of badge the user has
                        for (int i = 0; i < badgeNo; i++) {
                            CircleImageView pic = new CircleImageView(getContext());
                            TextView tv = new TextView(getContext());
                            String picName = "";
                            String title = "";

                            switch (allBadge[i]) {
                                case "Streak Master":
                                    picName = "fire";
                                    title = "Streak\nMaster";
                                    break;
                                case "Crisis Resolver":
                                    picName = "baby";
                                    title = "Crisis\nResolver";
                                    break;
                                case "Critical Thinker":
                                    picName = "brain";
                                    title = "Critical\nThinker";
                                    break;
                                case "Budget Boss":
                                    picName = "boss";
                                    title = "Budget\nBoss";
                                    break;
                                case "Wealthy Wizard":
                                    picName = "wizard";
                                    title = "Wealthy\nWizard";
                                    break;
                                case "Smart Saver":
                                    picName = "piggy";
                                    title = "Smart\nSaver";
                                    break;
                                default:
                                    break;
                            }
                            int picId = getResources().getIdentifier(picName, "drawable", getActivity().getPackageName());
                            pic.setImageResource(picId);
                            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, 72, 1.0f);
                            pic.setLayoutParams(lp2);
                            pic.setPadding(10, 0, 10, 0);
                            pic.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            pic.setBorderColor(getResources().getColor(R.color.medium_sea_green));
                            pic.setBorderWidth(1);
                            picll.addView(pic);

                            tv.setText(title);
                            LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(0,
                                    LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                            lp3.gravity = Gravity.CENTER;
                            tv.setGravity(Gravity.CENTER);
                            tv.setLayoutParams(lp3);
                            tv.setTextColor(Color.BLACK);
                            tv.setPadding(10, 10, 10, 10);
                            textll.addView(tv);
                        }
                    }

                    TextView ldPos = (TextView) view.findViewById(R.id.ldPos);

                    if (rank == 0)
                        ldPos.setText("N/A");
                    else if (rank == 1)
                        ldPos.setText("1st");
                    else if (rank == 2)
                        ldPos.setText("2nd");
                    else if (rank == 3)
                        ldPos.setText("3rd");
                    else
                        ldPos.setText(rank + "th");

                    TextView streaktv = (TextView) view.findViewById(R.id.streak);
                    streaktv.setText("You have " + streak + " days streak!");

                    xptv.setText(String.valueOf(xp) + "XP");
                    cointv.setText(String.valueOf(coin) + "MT");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(databaseListener);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        databaseReference.removeEventListener(databaseListener);
    }
}

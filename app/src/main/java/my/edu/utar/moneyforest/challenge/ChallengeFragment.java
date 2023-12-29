package my.edu.utar.moneyforest.challenge;


import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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

import my.edu.utar.moneyforest.R;
import my.edu.utar.moneyforest.user.User;

/*Done by Ng Jing Ying*/
/*This is the fragment (page) for displaying a list of available challenges.
 * The layout of each challenge item is dynamically changed according to the status of the challenge.
 * which details of the elements UI is implemented in ChalGridAdapter.java.
 * */
public class ChallengeFragment extends Fragment {
    private List<UserChallenge> chals = new ArrayList<>();
    List<UserChallenge> popChals = new ArrayList<>();
    private String userId;
    private Handler handler;
    private RecyclerView challenge_rv, challenge_pop_rv;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_challenge, container, false);
        challenge_rv = (RecyclerView) view.findViewById(R.id.challenge_rv);
        challenge_pop_rv = (RecyclerView) view.findViewById(R.id.challenge_pop_rv);

        //==== SECTION 1 Get information from Firebase Auth====//
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            userId = "guest";
        }
        //==SECTION 2 Initialise handler for handling retrieved data==//
        handler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    int taskId = msg.arg1;
                    int result = msg.arg2;

                    if (result == 0) {
                        ChalGridAdapter adapter = new ChalGridAdapter(chals);
                        LinearLayout.LayoutParams rvParams = (LinearLayout.LayoutParams) challenge_rv.getLayoutParams();
                        rvParams.weight = chals.size() / 2 + chals.size() % 2; // height of the recycler view is adapt to the list size
                        challenge_rv.setLayoutParams(rvParams);

                        challenge_rv.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns
                        challenge_rv.setAdapter(adapter);
                        challenge_rv.setLayoutFrozen(true);

                        ChalGridAdapter adapter2 = new ChalGridAdapter(popChals);
                        rvParams = (LinearLayout.LayoutParams) challenge_pop_rv.getLayoutParams();
                        rvParams.weight = popChals.size() / 2 + popChals.size() % 2;
                        challenge_pop_rv.setLayoutParams(rvParams);

                        challenge_pop_rv.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns
                        challenge_pop_rv.setAdapter(adapter2);
                        challenge_pop_rv.setLayoutFrozen(true);
                    }
                }
            }
        };
        try {
            //==SECTION 3 Retrive challenge data from Firebase==//
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            databaseReference = database.getReference("user_challenge").child(userId);

            // Read data from Firebase
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                        Map<String, Object> itemData = (Map<String, Object>) itemSnapshot.getValue();
                        if (itemData != null) {
                            try {
                                String chal_history = "{}";
                                if (itemData.get("chalHistory") != null) {
                                    chal_history = itemData.get("chalHistory").toString();
                                }
                                User user = new User(userId);
                                UserChallenge item = new UserChallenge();
                                Challenge itemChal = new Challenge(
                                        Integer.parseInt(itemData.get("id").toString()),
                                        itemData.get("name").toString(),
                                        itemData.get("desc").toString(),
                                        itemData.get("content").toString(),
                                        itemData.get("brief").toString(),
                                        Integer.parseInt(itemData.get("progress").toString()),
                                        itemData.get("chalImg").toString(),
                                        chal_history,
                                        (boolean) itemData.get("popular"));
                                if (itemData.get("apikey") != null)
                                    itemChal.setApikey(itemData.get("apikey").toString());
                                item.setUser(user);
                                item.setChallenge(itemChal);
                                if ((boolean) itemData.get("popular") == true) {
                                    popChals.add(item);
                                } else {
                                    chals.add(item);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                    Message message = handler.obtainMessage(0, 0, 0);
                    handler.sendMessage(message);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return view;
    }

}
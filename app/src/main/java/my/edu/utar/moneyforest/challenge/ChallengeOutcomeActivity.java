package my.edu.utar.moneyforest.challenge;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import my.edu.utar.moneyforest.network.MTRESTTask;
import my.edu.utar.moneyforest.R;

/*Done by Ng Jing Ying*/
/*This is the activity (page) for displaying the challenge outcome.
 * The activity begins with initialise all the GUI component variables,
 * Then, it retrieves the challenge information from intent,
 * It will call chatGPT API using the summarised game record in ChallengeActivity.
 * Then display accordingly.
 * */
public class ChallengeOutcomeActivity extends AppCompatActivity {
    private TextView chalTitle;
    private Button co_continueBtn;
    private ProgressBar loadingBar;
    private LinearLayout chalReviewLL;
    private final int MT_SUCCESS = 0;
    private final int MT_ERROR = -1;
    private ExecutorService executor;
    private final Object lock = new Object();
    private Handler handler;

    private ChallengeResult chalRes;
    private ProgressBar stablvlPB;
    private ProgressBar happilvlPB;
    private TextView xpTV, coinTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_outcome);

        //======SECTION 1 Initialise UI variables===//
        chalTitle = (TextView) findViewById(R.id.chalTitle);
        co_continueBtn = (Button) findViewById(R.id.co_continueBtn);
        chalReviewLL = (LinearLayout) findViewById(R.id.chalReviewLL);
        loadingBar = (findViewById(R.id.loadingBar));
        stablvlPB = (ProgressBar) findViewById(R.id.stablvlPB);
        happilvlPB = (ProgressBar) findViewById(R.id.happilvlPB);
        xpTV = (TextView) findViewById(R.id.xpTV);
        coinTV = (TextView) findViewById(R.id.coinTV);

        //======SECTION 2 Retrieve info from intent===//
        Intent intent = getIntent();
        chalRes = (ChallengeResult) intent.getSerializableExtra("chalRes");
        chalTitle.setText(chalRes.getChalName());
        xpTV.setText("" + chalRes.getXP() + " XP");
        coinTV.setText("" + chalRes.getCoin() + " MT");

        stablvlPB.setProgress((int) (chalRes.getStableLevel() * 100));
        happilvlPB.setProgress((int) (chalRes.getHappiLevel() * 100));
        executor = Executors.newSingleThreadExecutor();

        //======SECTION 3 Prepare chatGPT API call===//
        boolean useGPT = true;  // because chatGPT is not free, there is test mode for UI testing
        if (useGPT) {
            try {
                JSONObject messageObject = new JSONObject();
                JSONArray messagesArray = new JSONArray();
                messageObject.put("content", "You are a financial coach. Explain why these actions are inappropriate:\n<p>" +
                        chalRes.getImproperActions().toString().replaceAll("=", "")
                        + "</p>>.\n Format your response in JSON array, i.e. [{\"action\":\"\",\"reason\":\"\"}]");
                messageObject.put("role", "system");
                messagesArray.put(messageObject);
                executeBackgroundTask(0, messagesArray);
                messageObject = new JSONObject();
                messagesArray = new JSONArray();
                messageObject.put("content", "You are a financial coach. Explain why these actions are appropriate:\n<p>" +
                        chalRes.getProperActions().toString().replaceAll("=", "")
                        + "</p>>.\n Format your response in JSON array, i.e. [{\"action\":\"\",\"reason\":\"\"}]");
                messageObject.put("role", "system");
                messagesArray.put(messageObject);
                executeBackgroundTask(1, messagesArray);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            addActionUI("What you have done properly", chalRes.getProperActions(), R.color.medium_sea_green);
            addActionUI("List of Inappropriate Actions", chalRes.getImproperActions(), R.color.orange_300);
        }

        //======SECTION 4 Handler to listne to chatGPT call results===//
        handler = new Handler(getMainLooper()) {
            private int totalCompleted = 0;

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    int taskId = msg.arg1;
                    int result = msg.arg2;
                    if (result == MT_SUCCESS) {
                        totalCompleted++;
                    } else if (result == MT_ERROR) {
                        Toast.makeText(ChallengeOutcomeActivity.this, "An error occured. We will fix it as soon as possible", Toast.LENGTH_SHORT).show();
                    }
                    if (totalCompleted == 2) {
                        //====Handle GUI =====//
                        addActionUI("What you have done properly", chalRes.getProperActions(), R.color.medium_sea_green);
                        addActionUI("List of Inappropriate Actions", chalRes.getImproperActions(), R.color.orange_300);
                    }
                }
            }
        };
        co_continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void addActionUI(String heading, ArrayList<Map<String, String>> actions, int color_id) {
        co_continueBtn.setVisibility(View.VISIBLE);
        loadingBar.setVisibility(View.GONE);
        LayoutInflater inflater2 = LayoutInflater.from(ChallengeOutcomeActivity.this);
        View customTextViewLayout = inflater2.inflate(R.layout.textview_game_bold, null);
        TextView tv = customTextViewLayout.findViewById(R.id.gameTextView);
        tv.setText(heading);
        tv.setTextColor(getResources().getColor(color_id));
        tv.setPadding((int) dpToPx(10), (int) dpToPx(10), (int) dpToPx(10), (int) dpToPx(10));
        tv.setTextSize(18);
        chalReviewLL.addView(tv);

        // Print the stored data
        for (Map<String, String> data : actions) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                LinearLayout actionLL = new LinearLayout(ChallengeOutcomeActivity.this);
                inflater2 = LayoutInflater.from(ChallengeOutcomeActivity.this);
                customTextViewLayout = inflater2.inflate(R.layout.textview_game_bold, null);
                TextView actionNameTV = customTextViewLayout.findViewById(R.id.gameTextView);
                LinearLayout.LayoutParams actionNameTVParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                actionLL.setLayoutParams(actionNameTVParams);
                actionLL.setOrientation(LinearLayout.HORIZONTAL);
                actionLL.setBackgroundColor(getResources().getColor(color_id));
                actionNameTVParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        9
                );
                actionNameTV.setLayoutParams(actionNameTVParams);
                actionNameTV.setText(entry.getKey());
                actionNameTV.setTextColor(Color.WHITE);
                inflater2 = LayoutInflater.from(ChallengeOutcomeActivity.this);
                customTextViewLayout = inflater2.inflate(R.layout.textview_game, null);
                TextView tv2 = customTextViewLayout.findViewById(R.id.gameTextView);
                actionNameTVParams = new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );
                tv2.setLayoutParams(actionNameTVParams);
                tv2.setText("+");
                tv2.setTypeface(null, Typeface.BOLD);
                tv2.setTextColor(Color.WHITE);
                tv2.setTextSize(dpToPx(6));
                actionLL.addView(actionNameTV);
                actionLL.addView(tv2);
                chalReviewLL.addView(actionLL);
                LinearLayout smll = new LinearLayout(ChallengeOutcomeActivity.this);
                inflater2 = LayoutInflater.from(ChallengeOutcomeActivity.this);
                customTextViewLayout = inflater2.inflate(R.layout.textview_game, null);
                TextView actionInfoTV = customTextViewLayout.findViewById(R.id.gameTextView);
                actionInfoTV.setGravity(Gravity.LEFT);
                actionInfoTV.setText(entry.getValue());
                actionInfoTV.setVisibility(View.GONE);
                // toggle show/ hide of payer details
                actionLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (actionInfoTV.getVisibility() == View.GONE) {
                            tv2.setText("-");
                            actionInfoTV.setVisibility(View.VISIBLE);
                        } else {
                            tv2.setText("+");
                            actionInfoTV.setVisibility(View.GONE);
                        }
                    }
                });
                smll.addView(actionInfoTV);

                chalReviewLL.addView(smll);
            }
        }

    }

    // Perform background task
    private void executeBackgroundTask(final int taskId, final JSONArray messagesArray) {
        executor.execute(() -> {
            int status = -1;
            String result;
            // prevent concurrently calling chatGPT API
            synchronized (lock) {
                result = MTRESTTask.performGPTTask(messagesArray, chalRes.getApikey());
            }
            try {
                JSONArray assistantContent = new JSONArray(
                        new JSONObject(result)
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content"));
                System.out.println("asds" + assistantContent);
                if (taskId == 0) {
                    for (int i = 0; i < chalRes.getImproperActions().size(); i++) {
                        Map<String, String> data = chalRes.getImproperActions().get(i);
                        String currAction = assistantContent.getJSONObject(i).getString("action");
                        String newValue = assistantContent.getJSONObject(i).getString("reason");
                        for (Map.Entry<String, String> entry : data.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();
                            if (currAction.equals(key)) {
                                entry.setValue(newValue);
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < chalRes.getProperActions().size(); i++) {
                        Map<String, String> data = chalRes.getProperActions().get(i);
                        String currAction = assistantContent.getJSONObject(i).getString("action");
                        String newValue = assistantContent.getJSONObject(i).getString("reason");
                        for (Map.Entry<String, String> entry : data.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();
                            if (currAction.equals(key)) {
                                entry.setValue(newValue);
                            }
                        }
                    }
                }

                status = MT_SUCCESS;
            } catch (JSONException e) {
                status = MT_ERROR;
                e.printStackTrace();
            } finally {
                // Inform main thread about task completion
                Message message = handler.obtainMessage(0, taskId, status);
                handler.sendMessage(message);
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private int dpToPx(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

}
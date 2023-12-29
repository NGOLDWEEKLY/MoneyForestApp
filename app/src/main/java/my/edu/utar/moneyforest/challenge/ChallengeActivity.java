package my.edu.utar.moneyforest.challenge;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import my.edu.utar.moneyforest.outcome.CompleteActivity;
import my.edu.utar.moneyforest.course.Course;
import my.edu.utar.moneyforest.network.MTRESTTask;
import my.edu.utar.moneyforest.R;
import my.edu.utar.moneyforest.user.User;

/*Done by Ng Jing Ying*/
/*This is the activity (page) for playing the chatGPT-powered challenges.
 * The activity begins with initialise all the GUI component variables,
 * Then, it retrieves the challenge information from intent,
 * reading the current challenge status (completed/ ongoing/ new) to initialise the game environment.
 * Lastly, a series of listeners is initialised to capture user interaction and chatGPT responses from time to time.
 * */
public class ChallengeActivity extends AppCompatActivity {

    // constant values as an indicator of each chatGPT response
    private final int MT_ERROR = -1;
    private final int MT_SUCCESS = 0;
    private final int MT_SWITCH = 2;

    // Constant strings that will be displayed when the page is loading
    private final String[] randomFinTips = {"Save money from daily boba purchases for 3 years and get yourself an iPhone!",
            "Prepare your emergency fund which should be lasting for at least 3-6 months.",
            "Did you know? You should prioritise your high interest debt repayment to prevent sabotage of the double interest!",
            "Having a tight budget? Why not try cooking yourself at home!",
            "Track your spending using Money Forest Budgeting tool now!"
    };
    //===Variables for backend services interactions===//
    private final Object lock = new Object();
    private ExecutorService executor;
    private Handler handler;
    private DatabaseReference databaseReference;

    private AlertDialog progressAlert;
    private Button startEndBtn;
    private ImageView imageChal;
    private ArrayList<Button> optionsBtn = new ArrayList<Button>();
    private EditText editTextAction;
    private TextView moneyInBank;
    private TextView finTipTV;
    private LinearLayout assistLayout, gameLL;
    private RecyclerView happilvlRV;
    private View customTextViewLayout, customButtonLayout;
    private ScrollView storySV, happilvlSV;
    private LayoutInflater inflater2;
    private ProgressBar gameProgressBar;

    //===Object of data models===//
    private User user;
    private Course course;
    private Challenge chal;
    private ChallengeResult chalRes = new ChallengeResult();
    private JSONArray messagesArray = new JSONArray(), messagesArrayForEvaluation = new JSONArray();
    private String chalType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        //==============SECTION 0 Setting up GUI Component=======//
        editTextAction = findViewById(R.id.editTextAction);
        moneyInBank = findViewById(R.id.moneyInBank);
        startEndBtn = findViewById(R.id.startEndBtn);
        imageChal = findViewById(R.id.imageChal);
        assistLayout = findViewById(R.id.assistLayout);
        happilvlRV = findViewById(R.id.happilvlRV);
        happilvlSV = findViewById(R.id.happilvlSV);
        gameLL = findViewById(R.id.gameLL);
        storySV = findViewById(R.id.storySV);
        gameProgressBar = findViewById(R.id.gameProgressBar);
        Button sendBtn = findViewById(R.id.sendBtn);
        ImageButton backBtn = findViewById(R.id.backBtn);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);
        finTipTV = dialogView.findViewById(R.id.finTipTV);
        inflater2 = LayoutInflater.from(ChallengeActivity.this);
        customTextViewLayout = inflater2.inflate(R.layout.textview_game, null);

        //=== UI Components====//
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setView(dialogView);
        alertBuilder.setCancelable(false);
        progressAlert = alertBuilder.create();

        //=========SECTION 1  Get Challenge Information from Intent========//
        Intent intent = getIntent();
        chalType = intent.getStringExtra("type");
        if (chalType.equals("course"))
            course = (Course) intent.getSerializableExtra("course");
        UserChallenge uc = (UserChallenge) getIntent().getSerializableExtra("userChal");
        user = uc.getUser();
        chal = uc.getChallenge();
        TextView chal_title = findViewById(R.id.chal_title);
        chal_title.setText(chal.getName());

        //=========SECTION 2 Get Challenge Status========//
        switch (chal.getProgress()) {
            //=========SECTION 2.1  Challenge Not Yet Started========//
            case Challenge.CHALLENGE_NEW:
                inflater2 = LayoutInflater.from(ChallengeActivity.this);
                customTextViewLayout = inflater2.inflate(R.layout.textview_game, null);
                TextView tempTV = customTextViewLayout.findViewById(R.id.gameTextView);
                tempTV.setText(chal.getBrief());
                String base64Image = chal.getChalImg();
                // Decode Base64 string into image
                if (!base64Image.isEmpty()) {
                    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    imageChal.setImageBitmap(decodedBitmap);
                }
                imageChal.setVisibility(View.VISIBLE);
                happilvlSV.setVisibility(View.GONE);
                assistLayout.addView(tempTV);
                break;
            //=========SECTION 2.2  Challenge Already Completed========//
            case Challenge.CHALLENGE_COMPLETE:
                try {
                    JSONObject chalHistory = new JSONObject(chal.getChalHistory());
                    if (chalHistory.has("messagesArray")) {
                        messagesArray = chalHistory.getJSONArray("messagesArray");
                        messagesArrayForEvaluation = chalHistory.getJSONArray("messagesArrayForEvaluation");
                        assistLayout.removeAllViews();
                        imageChal.setVisibility(View.GONE);
                        happilvlSV.setVisibility(View.GONE);
                        startEndBtn.setVisibility(View.GONE);
                        //=====Update UI=====//
                        displayChallengeProgress(messagesArray, messagesArrayForEvaluation);
                        updateChallengeUI();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            //=========SECTION 2.3  Challenge Ongoing========//
            default:
                //=========Load Previous Data for the Challenge==========//
                try {
                    JSONObject chalHistory = new JSONObject(chal.getChalHistory());
                    if (chalHistory.has("messagesArray")) {
                        messagesArray = chalHistory.getJSONArray("messagesArray");
                        messagesArrayForEvaluation = chalHistory.getJSONArray("messagesArrayForEvaluation");
                        assistLayout.removeAllViews();
                        imageChal.setVisibility(View.GONE);
                        gameLL.setVisibility(View.VISIBLE);
                        startEndBtn.setVisibility(View.GONE);
                        //=====Update UI=====//
                        displayChallengeProgress(messagesArray, messagesArrayForEvaluation);
                        updateChallengeUI();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        //=========SECTION 3 Setting up Event Handler==========//
        storySV.post(new Runnable() {
            @Override
            public void run() {
                storySV.fullScroll(ScrollView.FOCUS_DOWN); // scroll the scrollview to bottom
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inflater2 = LayoutInflater.from(ChallengeActivity.this);
                customTextViewLayout = inflater2.inflate(R.layout.textview_game, null);
                TextView tv = customTextViewLayout.findViewById(R.id.gameTextView);
                if (editTextAction.getText().toString().isEmpty()) {
                    Toast.makeText(ChallengeActivity.this, "Empty input is not accepted.", Toast.LENGTH_SHORT).show();
                    return;
                }
                tv.setAlpha(0.6f);
                tv.setText("You: " + editTextAction.getText().toString() + "\n");
                tv.setGravity(Gravity.CENTER);
                assistLayout.addView(tv);
                if (updateMessageArrays(editTextAction.getText().toString()) == MT_SUCCESS) {
                    editTextAction.setText("");
                    finTipTV.setText(randomFinTips[new Random().nextInt(randomFinTips.length)]);
                    progressAlert.show(); // show loader
                    executeBackgroundTask(0); // execute chatGPT API call
                }
            }
        });
        startEndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chal.getProgress() == Challenge.CHALLENGE_NEW) {
                    assistLayout.removeAllViews();
                    happilvlSV.setVisibility(View.VISIBLE);
                    imageChal.setVisibility(View.GONE);
                    gameLL.setVisibility(View.VISIBLE);
                    startEndBtn.setVisibility(View.GONE);
                }
                if (updateMessageArrays("") == MT_SUCCESS) {
                    finTipTV.setText(randomFinTips[new Random().nextInt(randomFinTips.length)]);
                    progressAlert.show();// show loader
                    executeBackgroundTask(0);// execute chatGPT API call
                }

            }
        });
        executor = Executors.newSingleThreadExecutor();
        handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    int result = msg.arg2;
                    progressAlert.dismiss();
                    switch (result) {
                        case MT_SUCCESS:
                            updateChallengeUI();
                            break;
                        case MT_SWITCH:
                            // Challenge ended, switch to complete activity
                            Intent intent = new Intent(ChallengeActivity.this, CompleteActivity.class);
                            if (chalType.equals("course")) {
                                intent.putExtra("course", course);
                                intent.putExtra("type", "course_challenge");
                            } else
                                intent.putExtra("type", "challenge");
                            intent.putExtra("chalRes", chalRes);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                            break;
                        case MT_ERROR:
                            Toast.makeText(ChallengeActivity.this, "An error occured. We will fix it as soon as possible", Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                    }
                }
            }
        };
    }

    private void updateChallengeUI() {
        inflater2 = LayoutInflater.from(ChallengeActivity.this);
        customTextViewLayout = inflater2.inflate(R.layout.textview_game, null);
        TextView tv = customTextViewLayout.findViewById(R.id.gameTextView);
        tv.setText(chal.getCurrQues() + "\n");
        assistLayout.addView(tv);
        storySV.post(new Runnable() {
            @Override
            public void run() {
                storySV.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        gameProgressBar.setProgress(chal.getProgress());
        moneyInBank.setText("" + String.format("%5.0f", chal.getMoneyInBank()));

        ArrayList<String> options = chal.getCurrOptions();
        try {
            ArrayList<Challenge.NPCHappiness> happinesses = chal.getNpcHappinesses();
            NpcGridAdapter adapter = new NpcGridAdapter(happinesses);
            happilvlRV.setAdapter(adapter);
            happilvlRV.setLayoutManager(new GridLayoutManager(ChallengeActivity.this, happinesses.size() % 5));

            // remove options button from current view
            for (int i = 0; i < optionsBtn.size(); i++) {
                assistLayout.removeView(optionsBtn.get(i));
            }
            optionsBtn.clear();

            if (chal.getProgress() == Challenge.CHALLENGE_COMPLETE) {
                gameLL.setVisibility(View.GONE);
                startEndBtn.setVisibility(View.VISIBLE);
                happilvlSV.setVisibility(View.GONE);
                startEndBtn.setText("Show Result"); // challenge completed
            } else {
                for (int i = 0; i < options.size(); i++) {
                    // prevent chatGPT generated CUSTOM options displayed out
                    if (options.get(i).toLowerCase(Locale.ROOT).contains("custom")) {
                        continue;
                    }
                    customButtonLayout = inflater2.inflate(R.layout.button_option, null);
                    Button btnTmp = (customButtonLayout.findViewById(R.id.gameButton));
                    btnTmp.setText(options.get(i));
                    LinearLayout.LayoutParams optionBtm = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    optionBtm.bottomMargin = (int) dpToPx(5);
                    btnTmp.setLayoutParams(optionBtm);
                    btnTmp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String userAction = ((Button) view).getText().toString();
                            inflater2 = LayoutInflater.from(ChallengeActivity.this);
                            customTextViewLayout = inflater2.inflate(R.layout.textview_game, null);
                            TextView tv = customTextViewLayout.findViewById(R.id.gameTextView);
                            tv.setAlpha(0.6f);
                            tv.setText("You: " + userAction + "\n");
                            tv.setGravity(Gravity.CENTER);
                            assistLayout.addView(tv);
                            if (updateMessageArrays(userAction) == MT_SUCCESS) {
                                finTipTV.setText(randomFinTips[new Random().nextInt(randomFinTips.length)]);
                                progressAlert.show();
                                executeBackgroundTask(0);
                            }
                        }
                    });
                    optionsBtn.add(btnTmp);
                    assistLayout.addView(btnTmp);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Two JSON message arrays is maintained all the time. First message array is for the chatGPT which handles game logic,
    // where second message array is for the chatGPT which gives evaluation and recommendations.
    private int updateMessageArrays(String userAction) {
        try {
            JSONObject gameSystemMessage = new JSONObject();
            JSONObject evalSystemMessage = new JSONObject();
            switch (chal.getProgress()) {
                case Challenge.CHALLENGE_COMPLETE:
                    evalSystemMessage.put("content", "Now, stop asking me questions. Evaluate the coach session. List down" +
                            "the proper and improper decision made by me. Please keep your answer in JSON format as follows:" +
                            "{\"score\":0.6, \"proper_decisions\":[{if any}], \"improper_decisions\":[{if any}]}");
                    evalSystemMessage.put("role", "user");
                    messagesArrayForEvaluation.put(evalSystemMessage);
                    break;
                case Challenge.CHALLENGE_NEW:
                    gameSystemMessage.put("content", chal.getContent());
                    gameSystemMessage.put("role", "system");
                    messagesArray.put(gameSystemMessage);
                    evalSystemMessage.put("content", "You are a financial coach. You constantly ask me to make decision.");
                    evalSystemMessage.put("role", "system");
                    messagesArrayForEvaluation.put(evalSystemMessage);
                    break;
                default:
                    gameSystemMessage.put("content", userAction);
                    gameSystemMessage.put("role", "user");
                    messagesArray.put(gameSystemMessage);
                    evalSystemMessage.put("content", userAction);
                    evalSystemMessage.put("role", "user");
                    messagesArrayForEvaluation.put(evalSystemMessage);
            }
        } catch (Exception ex) {
            return MT_ERROR;
        }
        return MT_SUCCESS;
    }

    public void displayChallengeProgress(JSONArray jsonArray, JSONArray messagesArrayForEvaluation) {
        try {
            int j = jsonArray.length() - 1;
            JSONObject lastChat = new JSONObject();
            JSONObject assistantContent = new JSONObject();
            // Retrieve the most recent assistant message (which is stored in JSON format)
            while (j >= 0) {
                lastChat = jsonArray.getJSONObject(j);
                if (lastChat.getString("role").equals("assistant")) {
                    assistantContent = new JSONObject(lastChat.getString("content"));
                    break;
                }
                j--;
            }

            for (int i = 1; i < messagesArrayForEvaluation.length() && i != j; i++) {
                inflater2 = LayoutInflater.from(ChallengeActivity.this);
                customTextViewLayout = inflater2.inflate(R.layout.textview_game, null);
                TextView tv = customTextViewLayout.findViewById(R.id.gameTextView);
                if (messagesArrayForEvaluation.getJSONObject(i).getString("role").equals("assistant")) {
                    tv.setText(messagesArrayForEvaluation.getJSONObject(i).getString("content") + "\n");
                    tv.setGravity(Gravity.CENTER);
                } else {
                    tv.setAlpha(0.6f);
                    tv.setText("You: " + messagesArrayForEvaluation.getJSONObject(i).getString("content") + "\n");
                    tv.setGravity(Gravity.CENTER);
                }
                assistLayout.addView(tv);
                storySV.post(new Runnable() {
                    @Override
                    public void run() {
                        storySV.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }

            //====== Handle data model====//
            if (assistantContent.has("question")) {
                chal.setCurrQues(assistantContent.getString("question"));
                chal.setProgress((int) (assistantContent.getDouble("game_progress") * 100));
                chal.setMoneyInBank((int) assistantContent.getDouble("money_in_bank"));
                JSONArray optionsJSON = assistantContent.getJSONArray("options");
                ArrayList<String> options = new ArrayList<String>();
                for (int i = 0; i < optionsJSON.length(); i++) {
                    options.add(optionsJSON.getString(i));
                }
                chal.setCurrOptions(options);
                JSONObject happiNPCsJSON = assistantContent.getJSONObject("happiness");
                int i = 0;
                chal.removeAllHappiness();
                // Returned happiness level of each NPCs is in a form of key-value pair.
                for (Iterator<String> it = happiNPCsJSON.keys(); it.hasNext(); i++) {
                    String key = it.next();
                    double value = happiNPCsJSON.getDouble(key);
                    chal.addNPCHappiness(key, value);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Perform call ChatGPT API Task
    private void executeBackgroundTask(final int taskId) {
        executor.execute(() -> {
            int status = -1;
            String result;
            // use lock to prevent concurrently calling chatGPT API
            synchronized (lock) {
                boolean useGPT = true;
                if (!useGPT) { // because chatGPT is not free, this is a test mode for UI testing
                    float selfset = chal.getProgress() / 100f + 0.1f;

                    if (chal.getProgress() == Challenge.CHALLENGE_COMPLETE)
                        result = "{\n" +
                                "  \"id\": \"chatcmpl-7rgivMyb8jmYupPZlAwNrU6hQ0qTw\",\n" +
                                "  \"object\": \"chat.completion\",\n" +
                                "  \"created\": 1693029969,\n" +
                                "  \"model\": \"gpt-3.5-turbo-0613\",\n" +
                                "  \"choices\": [\n" +
                                "    {\n" +
                                "      \"index\": 0,\n" +
                                "      \"message\": {\n" +
                                "        \"role\": \"assistant\",\n" +
                                "        \"content\": \"{\\\"question\\\": \\\"Sarah, a 30-year-old graphic designer in Penang, lost her career and stability after her husband's sudden death. Overwhelmed by grief, medical bills, and rent, she isolated herself, yearning to overcome depression's grip. Sarah's best friend, Anna, has been trying to help her get back on her feet. Anna suggests that Sarah should start looking for a new job to regain financial stability. What should Sarah do?\\\", \\\"proper_decisions\\\": [\\\"looking for a new job\\\", \\\"Ask Sarah for help\\\"],\\\"improper_decisions\\\": [\\\"Borrow money from loan shark\\\", \\\"Continue isolating herself\\\"], \\\"score\\\": 0.6, \\\"money_in_bank\\\": 0, \\\"happiness\\\":{\\\"Sarah\\\":0.2, \\\"Anna\\\":0.5}}\"\n" +
                                "      },\n" +
                                "      \"finish_reason\": \"stop\"\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"usage\": {\n" +
                                "    \"prompt_tokens\": 390,\n" +
                                "    \"completion_tokens\": 144,\n" +
                                "    \"total_tokens\": 534\n" +
                                "  }\n" +
                                "}";
                    else {
                        result = "{\n" +
                                "  \"id\": \"chatcmpl-7rgivMyb8jmYupPZlAwNrU6hQ0qTw\",\n" +
                                "  \"object\": \"chat.completion\",\n" +
                                "  \"created\": 1693029969,\n" +
                                "  \"model\": \"gpt-3.5-turbo-0613\",\n" +
                                "  \"choices\": [\n" +
                                "    {\n" +
                                "      \"index\": 0,\n" +
                                "      \"message\": {\n" +
                                "        \"role\": \"assistant\",\n" +
                                "        \"content\": \"{\\\"question\\\": \\\"Sarah, a 30-year-old graphic designer in Penang, lost her career and stability after her husband's sudden death. Overwhelmed by grief, medical bills, and rent, she isolated herself, yearning to overcome depression's grip. Sarah's best friend, Anna, has been trying to help her get back on her feet. Anna suggests that Sarah should start looking for a new job to regain financial stability. What should Sarah do?\\\", \\\"options\\\": [\\\"Start looking for a new job\\\", \\\"Continue isolating herself\\\"], \\\"game_progress\\\": " + selfset + ", \\\"money_in_bank\\\": 0, \\\"happiness\\\":{\\\"Sarah\\\":0.2, \\\"Anna\\\":0.5}}\"\n" +
                                "      },\n" +
                                "      \"finish_reason\": \"stop\"\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"usage\": {\n" +
                                "    \"prompt_tokens\": 390,\n" +
                                "    \"completion_tokens\": 144,\n" +
                                "    \"total_tokens\": 534\n" +
                                "  }\n" +
                                "}";
                    }
                    try {
                        Thread.sleep(100);
                    } catch (Exception ex) {
                    }

                } else {
                    if (chal.getProgress() == Challenge.CHALLENGE_COMPLETE)
                        result = MTRESTTask.performGPTTask(messagesArrayForEvaluation, chal.getApikey());
                    else
                        result = MTRESTTask.performGPTTask(messagesArray, chal.getApikey());
                }
            }
            try {
                JSONObject assistantContent = new JSONObject(
                        new JSONObject(result)
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content"));

                if (chal.getProgress() == Challenge.CHALLENGE_COMPLETE) {
                    JSONArray proper_str_arr = assistantContent.getJSONArray("proper_decisions");
                    JSONArray improper_str_arr = assistantContent.getJSONArray("improper_decisions");
                    ArrayList<Map<String, String>> properActions = new ArrayList<>();
                    ArrayList<Map<String, String>> improperActions = new ArrayList<>();
                    for (int i = 0; i < proper_str_arr.length(); i++) {
                        Map<String, String> data = new HashMap<>();
                        data.put(proper_str_arr.getString(i), "");
                        properActions.add(data);
                    }
                    for (int i = 0; i < improper_str_arr.length(); i++) {
                        Map<String, String> data = new HashMap<>();
                        data.put(improper_str_arr.getString(i), "");
                        improperActions.add(data);
                    }
                    //=== store the challenge results returned by chatGPT
                    chalRes.setProperActions(properActions);
                    chalRes.setImproperActions(improperActions);
                    chalRes.calculateStableLevel(chal.getMoneyInBank());
                    chalRes.setHappiLevel(chal.getNPCHappinessLevel(0));
                    chalRes.calculateOutcome();
                    chalRes.setChalName(chal.getName());
                    chalRes.setApikey(chal.getApikey());
                    status = MT_SWITCH;
                } else {
                    //====== Handle data model====//
                    chal.setCurrQues(assistantContent.getString("question"));
                    chal.setProgress((int) (assistantContent.getDouble("game_progress") * 100));
                    chal.setMoneyInBank((int) assistantContent.getDouble("money_in_bank"));

                    JSONArray optionsJSON = assistantContent.getJSONArray("options");
                    ArrayList<String> options = new ArrayList<>();

                    for (int i = 0; i < optionsJSON.length(); i++) {
                        options.add(optionsJSON.getString(i));
                    }
                    chal.setCurrOptions(options);

                    JSONObject happiNPCsJSON = assistantContent.getJSONObject("happiness");
                    int i = 0;
                    chal.removeAllHappiness();
                    for (Iterator<String> it = happiNPCsJSON.keys(); it.hasNext(); i++) {
                        String key = it.next();
                        double value = happiNPCsJSON.getDouble(key);
                        if (i == 0) { // first NPC is main player
                            chal.addNPCHappiness("You", value);
                        } else {
                            chal.addNPCHappiness(key, value);
                        }
                    }

                    JSONObject msgCoach = new JSONObject();
                    String coachQuestion = assistantContent.getString("question");
                    msgCoach.put("content", coachQuestion);
                    msgCoach.put("role", "assistant");
                    messagesArrayForEvaluation.put(msgCoach);

                    JSONObject msgAssistantAll = new JSONObject();
                    msgAssistantAll.put("content", assistantContent.toString());
                    msgAssistantAll.put("role", "assistant");
                    messagesArray.put(msgAssistantAll);
                    JSONObject chalHistoryJSON = new JSONObject();
                    chalHistoryJSON.put("messagesArray", messagesArray);
                    chalHistoryJSON.put("messagesArrayForEvaluation", messagesArrayForEvaluation);
                    chal.setChalHistory(chalHistoryJSON.toString());

                    //===STORE INTO Firebase==//
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    databaseReference = database.getReference("user_challenge").child(user.getId()).child("" + (chal.getId() - 1));
                    databaseReference.setValue(chal);

                    status = MT_SUCCESS;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                status = MT_ERROR; // return error signal, this may be caused by failure request
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
        executor.shutdown(); // close the executor when the activity is destroyed.
    }

    private int dpToPx(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
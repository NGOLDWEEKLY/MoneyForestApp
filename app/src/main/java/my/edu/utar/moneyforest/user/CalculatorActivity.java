package my.edu.utar.moneyforest.user;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import my.edu.utar.moneyforest.R;

//done by Wong Tze-Qing, Sarah
public class CalculatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        WebView webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        String url = getIntent().getStringExtra("url");
        String msg = getIntent().getStringExtra("msg");
        webView.loadUrl(url);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        //ends the activity to go back to profile fragment
        ImageButton backBtn = (ImageButton) findViewById(R.id.BackBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

}
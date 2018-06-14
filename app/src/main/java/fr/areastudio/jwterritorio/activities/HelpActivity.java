package fr.areastudio.jwterritorio.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import fr.areastudio.jwterritorio.R;

public class HelpActivity extends AppCompatActivity {



    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        settings = getSharedPreferences(
                MainActivity.PREFS, 0);
        WebView webview = findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setDefaultTextEncodingName("utf-8");
        webview.loadUrl("file:///android_asset/" + getString(R.string.help_webpage));
    }
}

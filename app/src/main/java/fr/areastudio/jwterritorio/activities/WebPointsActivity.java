package fr.areastudio.jwterritorio.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import fr.areastudio.jwterritorio.R;

public class WebPointsActivity extends AppCompatActivity {



    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        settings = getSharedPreferences(
                MainActivity.PREFS, 0);
        WebView webview = findViewById(R.id.webview);
        webview.setWebViewClient(new WebViewClient(){

        });
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.loadUrl("https://www.google.com/maps/d/u/2/embed?mid=1MgIDCyty_TVSiubKCbtRnh_r_tsCmW5F");
//        BottomNavigationView navigation = findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//        if (((MyApplication)getApplication()).getMe().type.equals("PUBLISHER")){
//            navigation.getMenu().removeItem(R.id.navigation_assign);
//        }
//        navigation.getMenu().findItem(R.id.navigation_web).setChecked(true);
    }
}

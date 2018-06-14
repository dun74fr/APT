package fr.areastudio.jwterritorio.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;

public class WebActivity extends AppCompatActivity {


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent;
            switch (item.getItemId()) {

                case R.id.navigation_main:
                    intent = new Intent(WebActivity.this,MainActivity.class);
                    WebActivity.this.startActivity(intent);
                    return true;
                case R.id.navigation_address_list:
                    intent = new Intent(WebActivity.this,MyAddressesActivity.class);
                    WebActivity.this.startActivity(intent);
                    return true;
                case R.id.navigation_assign:
                    intent = new Intent(WebActivity.this,AssignActivity.class);
                    WebActivity.this.startActivity(intent);
                    return true;

            }
            return false;
        }
    };
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        settings = getSharedPreferences(
                MainActivity.PREFS, 0);
        WebView webview = findViewById(R.id.webview);
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                handler.proceed(settings.getString("user",""), settings.getString("password",""));
            }
        });
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.loadUrl("https://www.ptbvl.org");
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        if (((MyApplication)getApplication()).getMe().type.equals("PUBLISHER")){
            navigation.getMenu().removeItem(R.id.navigation_assign);
        }
        navigation.getMenu().findItem(R.id.navigation_web).setChecked(true);
    }
}

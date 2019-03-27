package fr.areastudio.jwterritorio.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.activeandroid.query.Delete;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.areastudio.jwterritorio.MyApplication;
import fr.areastudio.jwterritorio.R;
import fr.areastudio.jwterritorio.model.Address;
import fr.areastudio.jwterritorio.model.Assignments;
import fr.areastudio.jwterritorio.model.Publisher;
import fr.areastudio.jwterritorio.model.Territory;
import fr.areastudio.jwterritorio.model.Visit;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mUrlView;
    private TextView help;
    private View mProgressView;
    private View mLoginFormView;
    private SharedPreferences settings;
    protected String serverUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        settings = getSharedPreferences(
                MainActivity.PREFS, 0);

        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        mUrlView = findViewById(R.id.url);
        help = findViewById(R.id.help_btn);
        help.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,HelpActivity.class));
            }
        });
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                        attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mUrlView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        serverUrl = mUrlView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
//        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
//            mPasswordView.setError(getString(R.string.error_invalid_password));
//            focusView = mPasswordView;
//            cancel = true;
//        }

        // Check for a valid email addressText.
        if (TextUtils.isEmpty(serverUrl)) {
            mUrlView.setError(getString(R.string.error_field_required));
            focusView = mUrlView;
            cancel = true;
       }
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
// else if (!isEmailValid(email)) {
//            mEmailView.setError(getString(R.string.error_invalid_email));
//            focusView = mEmailView;
//            cancel = true;
//        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(serverUrl + getString(R.string.login_url), email, password);
            mAuthTask.execute((Void) null);
        }
    }

//    private boolean isEmailValid(String email) {
//        //TODO: Replace this with your own logic
//        return email.contains("@");
//    }
//
//    private boolean isPasswordValid(String password) {
//        //TODO: Replace this with your own logic
//        return password.length() >=  4;
//    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private  String mUrl;

        UserLoginTask(String url, String email, String password) {
            mUrl = url;
            mEmail = email;
            mPassword = password;
            mUrl = mUrl.replace("\n","");
            mUrl = mUrl.replace("{email}", email);
            mUrl = mUrl.replace("{password}", password);

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL(mUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", System.getProperty("http.agent"));
                conn.setReadTimeout(60000 /* milliseconds */);
                conn.setConnectTimeout(90000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                conn.connect();
                if (conn.getResponseCode() == -1) {
                    return null;
                }
                InputStream input = new BufferedInputStream(conn.getInputStream());

                // OutputStream output = new FileOutputStream(out);

                byte data[] = new byte[1024];
                int count;
                while ((count = input.read(data)) != -1) {
                    out.write(data, 0, count);
                }
                out.flush();
                input.close();
                JSONObject mainJson = new JSONObject(out.toString());
//                mainJson = mainJson.getJSONObject("data");

                new Delete().from(Visit.class).execute();
                new Delete().from(Address.class).execute();
                new Delete().from(Assignments.class).execute();
                new Delete().from(Territory.class).execute();
                new Delete().from(Publisher.class).execute();
//                new Delete().from(Congregation.class).execute();
//                Congregation congregation = new Congregation(mainJson.getJSONObject("congregation").getString("name")) ;
//                congregation.uuid = mainJson.getJSONObject("congregation").getString("uuid");
//                congregation.address = mainJson.getJSONObject("congregation").getString("address");
//                congregation.lat = mainJson.getJSONObject("congregation").getString("lat");
//                congregation.lng = mainJson.getJSONObject("congregation").getString("lng");
//                congregation.number = mainJson.getJSONObject("congregation").getString("number");
//                congregation.city = mainJson.getJSONObject("congregation").optString("city");
//                congregation.save();
//                settings.edit().putString("congregation_uuid",congregation.uuid).apply();

                new Delete().from(Publisher.class).where("uuid = ?", mainJson.getString("_id")).execute();
                Publisher me = new Publisher(mainJson.getString("name"));
                me.email = mainJson.getString("email");
                me.uuid = mainJson.getString("_id");
                me.type = mainJson.getString("type");
                me.save();
                ((MyApplication)getApplication()).setMe(me);
                settings.edit().putString("user_id", mainJson.getString("_id"))
                        .putString("user",mainJson.getString("email"))
                        .putString("password",mPassword).apply();
                return true;
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                settings.edit().putString("user", mEmail).apply();
                settings.edit().putString("password", mPassword).apply();
                settings.edit().putString("serverUrl",serverUrl).apply();;
                Intent main = new Intent(LoginActivity.this,MainActivity.class);
                LoginActivity.this.startActivity(main);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}


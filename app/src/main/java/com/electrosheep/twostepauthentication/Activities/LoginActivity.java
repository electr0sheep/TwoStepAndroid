package com.electrosheep.twostepauthentication.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.electrosheep.twostepauthentication.Models.LoginResponse;
import com.electrosheep.twostepauthentication.Models.NewUserResponse;
import com.electrosheep.twostepauthentication.Parsers.HttpManager;
import com.electrosheep.twostepauthentication.Parsers.LoginResponseJSONParser;
import com.electrosheep.twostepauthentication.Parsers.NewUserResponseJSONParser;
import com.electrosheep.twostepauthentication.Parsers.RequestPackage;
import com.electrosheep.twostepauthentication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    public static final String SERVER_URL =
            "https://two-step-authentication.herokuapp.com/php";
    public SharedPreferences sharedPref;


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUsernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String fcm_token = sharedPref.getString("fcm_token", "ERROR");;

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid fcm_token.
        if (fcm_token.equals("ERROR")) {
            Toast.makeText(this, "ERROR: no fcm token", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "FCM token is: " + fcm_token, Toast.LENGTH_SHORT).show();
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password, fcm_token);
            mAuthTask.execute(SERVER_URL);
        }
    }

    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        //return username.contains("@");
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only username addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary username addresses first. Note that there won't be
                // a primary username address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> usernames = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            usernames.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addUsernamesToAutoComplete(usernames);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addUsernamesToAutoComplete(List<String> usernameAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, usernameAddressCollection);

        mUsernameView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, String> {

        private final String mUsername;
        private final String mPassword;
        private final String mFcmToken;

        UserLoginTask(String username, String password, String fcm_token) {
            mUsername = username;
            mPassword = password;
            mFcmToken = fcm_token;
        }

        @Override
        protected String doInBackground(String... params) {
            // encode the request package
            RequestPackage p = new RequestPackage();
            p.setMethod("POST");
            p.setParam("username", mUsername);
            p.setParam("password", mPassword);
            p.setParam("fcm_token", mFcmToken);
            p.setUri(params[0] + "/newfcmtoken.php");

            return HttpManager.getData(p);
        }

        @Override
        protected void onPostExecute(final String content) {
            mAuthTask = null;
            showProgress(false);

            if (content == null || content == ""){
                Toast.makeText(LoginActivity.this, "There was no server response", Toast.LENGTH_SHORT).show();
            }

            // parse the string response
            LoginResponse response = LoginResponseJSONParser.parseFeed(content);

            if (response == null){
                Toast.makeText(LoginActivity.this, "The following unparseable string was sent by the server: " + content, Toast.LENGTH_LONG).show();
                return;
            }

            // if the call was successful, launch main activity
            if (response.getResult() == true) {
                loginSuccess(mUsername, mPassword);
            }

            // if response was user not found, prompt to create new user
            if (response.getMessage().equals("User doesn't exist")){
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Create new user?");
                builder.setMessage("We couldn't find a user by that ID in our database. If you'd like, we can set up a new user with the username and password you just entered");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                        UserCreationTask newUserTask = new UserCreationTask(mUsername, mPassword);
                        newUserTask.execute(SERVER_URL);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
                builder.show();
            } else {
                Toast.makeText(LoginActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    public class UserCreationTask extends AsyncTask<String, Void, String> {

        private final String mUsername;
        private final String mPassword;

        UserCreationTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected String doInBackground(String... params) {
            // encode the request package
            RequestPackage p = new RequestPackage();
            p.setMethod("POST");
            p.setParam("username", mUsername);
            p.setParam("password", mPassword);
            p.setUri(params[0] + "/new.php");

            return HttpManager.getData(p);
        }

        @Override
        protected void onPostExecute(final String content) {
            // parse the string response
            NewUserResponse response = NewUserResponseJSONParser.parseFeed(content);

            // show the response
            Toast.makeText(LoginActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();

            // Proceed to the main activity
            if (response.getResult() == true) {
                loginSuccess(mUsername, mPassword);
            }
        }
    }

    private void loginSuccess(String username, String password){
        // don't do anything yet, just show a toast
        Toast.makeText(this, "Login success", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.putExtra("URL", SERVER_URL);
//        intent.putExtra("username", username);
//        intent.putExtra("password", password);
//        startActivity(intent);
    }
}
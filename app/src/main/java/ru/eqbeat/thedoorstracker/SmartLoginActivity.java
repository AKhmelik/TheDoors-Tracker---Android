package ru.eqbeat.thedoorstracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import studios.codelight.smartloginlibrary.SmartLoginBuilder;
import studios.codelight.smartloginlibrary.SmartLoginConfig;
import studios.codelight.smartloginlibrary.manager.UserSessionManager;
import studios.codelight.smartloginlibrary.users.SmartFacebookUser;
import studios.codelight.smartloginlibrary.users.SmartGoogleUser;
import studios.codelight.smartloginlibrary.users.SmartUser;
import studios.codelight.smartloginlibrary.util.DialogUtil;
import studios.codelight.smartloginlibrary.util.UserUtil;

public class SmartLoginActivity extends AppCompatActivity implements
        View.OnClickListener{

    CallbackManager callbackManager;
    SmartLoginConfig config;
    EditText usernameEditText, passwordEditText, usernameSignup, emailSignup, passwordSignup, repeatPasswordSignup;
    ProgressDialog progress;
    //LinearLayout signUpPanel;
    ViewGroup mContainer;
    LinearLayout signinContainer, signupContainer, signupStage2Container;
    ImageView appLogo;


    //Google Sign in related
    private static GoogleApiClient mGoogleApiClient;
    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;
    private UserApi currentUser;
    private String loginProviderIdentifier;
    private String oAuth;
    private String TAG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get the current user details
        currentUser = UserSessionManager.getCurrentUser(this);
        if(currentUser != null) {
            setResult(SmartLoginConfig.CUSTOM_LOGIN_REQUEST);
            finish();
        }

        //get the config object from the intent and unpack it
        Bundle bundle = getIntent().getExtras();
        config = SmartLoginConfig.unpack(bundle);

        //Set the facebook app id and initialize sdk
        FacebookSdk.setApplicationId(config.getFacebookAppId());
        FacebookSdk.sdkInitialize(getApplicationContext());

        //Attaching the view
        setContentView(R.layout.activity_smart_login);

        //Set the title and back button on the Action bar
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Login");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Get the containers required to inject the views
        mContainer = (ViewGroup) findViewById(R.id.main_container);
        signinContainer = (LinearLayout) findViewById(R.id.signin_container);
        signupStage2Container = (LinearLayout) findViewById(R.id.signup_stage2_container);
        signupContainer = (LinearLayout) findViewById(R.id.signup_container);

        //Inject the views in the respective containers
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        /**
         * init second step of social registration
         * @author a.khmelik 2016-03-28
         */
        {
            signupStage2Container.addView(layoutInflater.inflate(R.layout.fragment_stage2_login, mContainer, false));
            //listeners
            findViewById(R.id.custom_signin_button_stage2).setOnClickListener(this);

            //Hide necessary views
            signupStage2Container.setVisibility(View.GONE);
        }

        //include views based on user settings
        if(config.isCustomLoginEnabled()){
            signinContainer.addView(layoutInflater.inflate(R.layout.fragment_custom_login, mContainer, false));
            if(config.isFacebookEnabled() || config.isGoogleEnabled()) {
                signinContainer.addView(layoutInflater.inflate(R.layout.fragment_divider, mContainer, false));
            }
            signupContainer.addView(layoutInflater.inflate(R.layout.fragment_signup, mContainer, false));

            //listeners
            findViewById(R.id.custom_signin_button).setOnClickListener(this);
            findViewById(R.id.custom_signup_button).setOnClickListener(this);
            findViewById(R.id.user_signup_button).setOnClickListener(this);

            //Hide necessary views
            signupContainer.setVisibility(View.GONE);
        }

        if(config.isFacebookEnabled()){
            signinContainer.addView(layoutInflater.inflate(R.layout.fragment_facebook_login, mContainer, false));
            AppCompatButton facebookButton = (AppCompatButton) findViewById(R.id.login_fb_button);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                facebookButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.facebook_vector, 0, 0, 0);
            } else {
                facebookButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_facebook_white_36dp, 0, 0, 0);
            }
            facebookButton.setOnClickListener(this);
        }


        //bind the views
        appLogo = (ImageView) findViewById(R.id.applogo_imageView);
        usernameEditText = (EditText) findViewById(R.id.userNameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        usernameSignup = (EditText) findViewById(R.id.userNameSignUp);
        passwordSignup = (EditText) findViewById(R.id.passwordSignUp);
        repeatPasswordSignup = (EditText) findViewById(R.id.repeatPasswordSignUp);
        emailSignup = (EditText) findViewById(R.id.emailSignUp);

        //Set app logo
        if(config.getAppLogo() != 0) {
            appLogo.setImageResource(config.getAppLogo());
        } else {
            appLogo.setVisibility(View.GONE);
        }

        //Facebook login callback
        callbackManager = CallbackManager.Factory.create();


    }

    //Required for Facebook and google login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //for facebook login
        callbackManager.onActivityResult(requestCode, resultCode, data);

        //For google login
        if (requestCode == SmartLoginConfig.GOOGLE_LOGIN_REQUEST) {
            progress = ProgressDialog.show(this, "", getString(R.string.getting_data), true);
            // If the error resolution was not successful we should not resolve further.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }



    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.login_fb_button){
            //do facebook login
            doFacebookLogin();
        } else if(id == R.id.custom_signin_button){
            //custom signin implementation
            doCustomSignin();
        } else if(id == R.id.custom_signup_button){
            //custom signup implementation
            //AnimUtil.slideToTop(signinContainer);
            signinContainer.setVisibility(View.GONE);
            signupContainer.setVisibility(View.VISIBLE);
            findViewById(R.id.userNameSignUp).requestFocus();
        } else if(id == R.id.user_signup_button){
            doCustomSignup();
        }
        else if(id == R.id.custom_signin_button_stage2){
            doStage2Signup();
        }

    }



    public void doCustomSignup() {
        String username = usernameSignup.getText().toString();
        String password = passwordSignup.getText().toString();
        String repeatPassword = repeatPasswordSignup.getText().toString();
        String email = emailSignup.getText().toString();
        if (username.equals("")) {
            //DialogUtil.getErrorDialog(R.string.username_error, this).show();
            usernameSignup.setError(getResources().getText(R.string.username_error));
            usernameSignup.requestFocus();
        } else if (password.equals("")) {
            //DialogUtil.getErrorDialog(R.string.password_error, this).show();
            passwordSignup.setError(getResources().getText(R.string.password_error));
            passwordSignup.requestFocus();
        } else if (email.equals("")) {
            //DialogUtil.getErrorDialog(R.string.no_email_error, this).show();
            emailSignup.setError(getResources().getText(R.string.no_email_error));
            emailSignup.requestFocus();
        } else if (!password.equals(repeatPassword)) {
            //DialogUtil.getErrorDialog(R.string.password_mismatch, this).show();
            repeatPasswordSignup.setError(getResources().getText(R.string.password_mismatch));
            repeatPasswordSignup.requestFocus();
        } else {
            if (SmartLoginBuilder.smartCustomLoginListener != null) {


                /**
                 * custom AUTH works by username
                 * @author a.khmelik 2016-04-05
                 */
                UserUtil userUtil = new UserUtil();
                userUtil.context = this;
                userUtil.apiSignUp(username, password, email);
            }
        }
    }


    public void loginCallback(){
            UserApi userApi = UserUtil.userApi;
            if(userApi.is_reg == 1){
                setResult(SmartLoginConfig.CUSTOM_LOGIN_REQUEST);
                finishLogin(userApi);
            }
            else if(userApi.is_reg == 2){
                signupStage2Container.setVisibility(View.VISIBLE);
                signinContainer.setVisibility(View.GONE);
                signupContainer.setVisibility(View.GONE);
            }
            else{
                Toast.makeText(this, userApi.error_message, Toast.LENGTH_SHORT).show();
                //setResult(RESULT_CANCELED);
                //      finish();
            }
    }

    public void doStage2Signup() {

        EditText usernameSignup2 = (EditText) findViewById(R.id.userNameSignUpStep2);
        String username = usernameSignup2.getText().toString();

        EditText emailSignup2 = (EditText) findViewById(R.id.emailSignUpStep2);
        String email = emailSignup2.getText().toString();

            if(username.equals("")){
                //DialogUtil.getErrorDialog(R.string.username_error, this).show();
                usernameSignup2.setError(getResources().getText(R.string.username_error));
                usernameSignup2.requestFocus();
            }else if(email.equals("")){
                //DialogUtil.getErrorDialog(R.string.no_email_error, this).show();
                emailSignup2.setError(getResources().getText(R.string.no_email_error));
                emailSignup2.requestFocus();
            }
            else {
                if (SmartLoginBuilder.smartCustomLoginListener != null) {

                    UserUtil userUtil = new UserUtil();
                    userUtil.context =this;
                    userUtil.apiSignUpStage2(oAuth, loginProviderIdentifier, username, email);
                }
            }
    }

    public void doCustomSignin() {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            if(username.equals("")){
                //DialogUtil.getErrorDialog(R.string.username_error, this).show();
                if(config.getLoginType() == SmartLoginConfig.LoginType.withUsername) {
                    usernameEditText.setError(getResources().getText(R.string.username_error));
                } else {
                    usernameEditText.setError(getResources().getText(R.string.email_error));
                }
                usernameEditText.requestFocus();
            } else if(password.equals("")){
                //DialogUtil.getErrorDialog(R.string.password_error, this).show();
                passwordEditText.setError(getResources().getText(R.string.password_error));
                passwordEditText.requestFocus();
            } else {

                if (SmartLoginBuilder.smartCustomLoginListener != null) {
                    final ProgressDialog progress = ProgressDialog.show(this, "", getString(R.string.logging_holder), true);

                    /**
                     * custom AUTH works by username
                     * @author a.khmelik 2016-04-05
                     */
                    UserUtil userUtil = new UserUtil();
                    userUtil.context =this;
                    userUtil.apiSignIn(username, password);
                    progress.dismiss();
                }
            }
    }

    public void doFacebookLogin() {


            final SmartLoginActivity context = this;
            Toast.makeText(SmartLoginActivity.this, "Facebook login", Toast.LENGTH_SHORT).show();
            final ProgressDialog progress = ProgressDialog.show(this, "", getString(R.string.logging_holder), true);
            ArrayList<String> permissions = config.getFacebookPermissions();
            if (permissions == null){
                permissions = SmartLoginConfig.getDefaultFacebookPermissions();
            }
            LoginManager.getInstance().logInWithReadPermissions(SmartLoginActivity.this, permissions);
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

                @Override
                public void onSuccess(LoginResult loginResult) {
                    progress.setMessage(getString(R.string.getting_data));
                    GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            progress.dismiss();
                            UserUtil util = new UserUtil();
                            SmartFacebookUser facebookUser = util.populateFacebookUser(object);
                            if(facebookUser != null){
                                //oauth
                                //Facebook
                                util.context =context;
                                oAuth  = facebookUser.getUserId();
                                loginProviderIdentifier =  "Facebook";
                                util.oauthSignIn(oAuth, loginProviderIdentifier);

                                progress.dismiss();

                                                               // finishLogin(facebookUser);
                            } else {
                                finish();
                            }
                        }
                    });
                    request.executeAsync();
                }

                @Override
                public void onCancel() {
                    progress.dismiss();
                    finish();
                    Log.d("Facebook Login", "User cancelled the login process");
                }

                @Override
                public void onError(FacebookException e) {
                    progress.dismiss();
                    finish();
                    Toast.makeText(SmartLoginActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void finishLogin(UserApi userApi){


//        @todo  should add post auth from backend
        UserSessionManager sessionManager = new UserSessionManager();
        if(sessionManager.setUserSession(this, userApi)){
            Intent intent = new Intent();
            intent.putExtra(SmartLoginConfig.USER, userApi);
                signinContainer.setVisibility(View.GONE);
                signupContainer.setVisibility(View.GONE);
            setResult(SmartLoginConfig.CUSTOM_LOGIN_REQUEST, intent);

            finish();

        } else {
            DialogUtil.getErrorDialog(R.string.network_error, this);
        }
    }


    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(signupContainer.getVisibility() == View.VISIBLE && config.isCustomLoginEnabled()){
            signupContainer.setVisibility(View.GONE);
            signinContainer.setVisibility(View.VISIBLE);
        } else {
            finish();
        }
    }

    public static GoogleApiClient getGoogleApiClient(){
        return mGoogleApiClient;
    }
}

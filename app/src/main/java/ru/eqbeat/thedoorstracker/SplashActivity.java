package ru.eqbeat.thedoorstracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;

import studios.codelight.smartloginlibrary.SmartCustomLoginListener;
import studios.codelight.smartloginlibrary.SmartCustomLogoutListener;
import studios.codelight.smartloginlibrary.SmartLoginBuilder;
import studios.codelight.smartloginlibrary.SmartLoginConfig;
import studios.codelight.smartloginlibrary.users.SmartFacebookUser;
import studios.codelight.smartloginlibrary.users.SmartGoogleUser;
import studios.codelight.smartloginlibrary.users.SmartUser;

/**
 * Created by AKhmelik on 27.03.2016.
 */

public class SplashActivity extends AppCompatActivity implements SmartCustomLogoutListener, SmartCustomLoginListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SmartLoginBuilder loginBuilder = new SmartLoginBuilder();

        //Set facebook permissions
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add("public_profile");
        permissions.add("email");
        permissions.add("user_birthday");
        permissions.add("user_friends");
        Intent intent = loginBuilder.with(getApplicationContext())
                .setAppLogo(getlogo())
                .isFacebookLoginEnabled(true)
                .withFacebookAppId(getString(R.string.facebook_app_id)).withFacebookPermissions(permissions)
                .isGoogleLoginEnabled(true)
                .isCustomLoginEnabled(true, SmartLoginConfig.LoginType.withEmail)
                .setSmartCustomLoginHelper(SplashActivity.this)
                .build();

        startActivityForResult(intent, SmartLoginConfig.LOGIN_REQUEST);
//        finish();
    }

    private int getlogo() {
        return R.mipmap.ic_launcher;
//        return 0;
    }

    @Override
    public boolean customSignin(SmartUser user) {
        Toast.makeText(SplashActivity.this, user.getUsername() + " " + user.getPassword(), Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean customSignup(SmartUser newUser) {
        newUser=newUser;
        return false;
    }

    @Override
    public boolean customUserSignout(SmartUser smartUser) {
        smartUser=smartUser;
        return false;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String fail = "Login Failed";
        if(resultCode == SmartLoginConfig.FACEBOOK_LOGIN_REQUEST){
            SmartFacebookUser user;
            try {
                user = data.getParcelableExtra(SmartLoginConfig.USER);
                String userDetails = user.getProfileName() + " " + user.getEmail() + " " + user.getBirthday();
                Toast.makeText(SplashActivity.this, userDetails, Toast.LENGTH_SHORT).show();

            }catch (Exception e){
                Toast.makeText(SplashActivity.this, fail, Toast.LENGTH_SHORT).show();
            }
        }
        else if(resultCode == SmartLoginConfig.GOOGLE_LOGIN_REQUEST){
            SmartGoogleUser user = data.getParcelableExtra(SmartLoginConfig.USER);
            String userDetails = user.getEmail() + " " + user.getBirthday() + " " + user.getAboutMe();
            Toast.makeText(SplashActivity.this, userDetails, Toast.LENGTH_SHORT).show();
        }
        else if(resultCode == SmartLoginConfig.CUSTOM_LOGIN_REQUEST){
            SmartUser user = data.getParcelableExtra(SmartLoginConfig.USER);
            String userDetails = user.getUsername() + " (Custom User)";
            Toast.makeText(SplashActivity.this, userDetails, Toast.LENGTH_SHORT).show();
        }
        /*else if(resultCode == SmartLoginConfig.CUSTOM_SIGNUP_REQUEST){
            SmartUser user = data.getParcelableExtra(SmartLoginConfig.USER);
            String userDetails = user.getUsername() + " (Custom User)";
            loginResult.setText(userDetails);
        }*/
        else if(resultCode == RESULT_CANCELED){
            Toast.makeText(SplashActivity.this, fail, Toast.LENGTH_SHORT).show();
        }

    }
}
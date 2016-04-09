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
    public boolean customSignin(UserApi user) {
        Toast.makeText(SplashActivity.this, user.hash + " " + user.is_reg, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean customSignup(UserApi newUser) {
        newUser=newUser;
        return false;
    }

    @Override
    public boolean customUserSignout(UserApi smartUser) {
        smartUser=smartUser;
        return false;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String fail = "Login Failed";



        if(resultCode == SmartLoginConfig.CUSTOM_LOGIN_REQUEST){
            Intent intentTracker = new Intent(this, TrackerActivity.class);
            startActivity(intentTracker);
        }
        else if(resultCode == RESULT_CANCELED){
            finish();
        }

    }
}
package studios.codelight.smartloginlibrary.manager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.gson.Gson;

//import studios.codelight.smartloginlibrary.R;
import ru.eqbeat.thedoorstracker.R;
import ru.eqbeat.thedoorstracker.UserApi;
import studios.codelight.smartloginlibrary.SmartCustomLogoutListener;
import ru.eqbeat.thedoorstracker.SmartLoginActivity;
import studios.codelight.smartloginlibrary.SmartLoginConfig;
import studios.codelight.smartloginlibrary.util.DialogUtil;

/**
 * Created by Kalyan on 9/29/2015.
 */
public class UserSessionManager {

    static final String USER_SESSION = "user_session_key";
    static final String USER_PREFS = "codelight_studios_user_prefs";
    static final String DEFAULT_SESSION_VALUE = "No logged in user";

    /*
        This static method can be called to get the logged in user.
        It reads from the shared preferences and builds a SmartUser object and returns it.
        If no user is logged in it returns null
    */
    public static UserApi getCurrentUser(Context context){
        UserApi userApi = null;
        SharedPreferences preferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String sessionUser = preferences.getString(USER_SESSION, DEFAULT_SESSION_VALUE);
        if(!sessionUser.equals(DEFAULT_SESSION_VALUE)){

            userApi = gson.fromJson(sessionUser, UserApi.class);

            try {
                userApi = gson.fromJson(sessionUser, UserApi.class);

            }catch (Exception e){
                Log.e("GSON", e.getMessage());
            }
        }
        return userApi;
    }

    /*
        This method sets the session object for the current logged in user.
        This is called from inside the SmartLoginActivity to save the
        current logged in user to the shared preferences.
    */
    public boolean setUserSession(Context context, UserApi userApi){
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        try {
            preferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
            editor = preferences.edit();


            Gson gson = new Gson();
            String sessionUser = gson.toJson(userApi);
            Log.d("GSON", sessionUser);
            editor.putString(USER_SESSION, sessionUser);
            editor.apply();
            return true;
        } catch (Exception e){
            Log.e("Session Error", e.getMessage());
            return false;
        }
    }

    /*
        This static method logs out the user that is logged in.
        This implements facebook and google logout.
        Custom user logout is left to the user.
        It also removes the preference entries.
    */
    public static boolean logout(Activity context, UserApi userApi, SmartCustomLogoutListener smartCustomLogoutListener){
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        try {
            preferences = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE);
            editor = preferences.edit();

            try {
                String user_type = preferences.getString(SmartLoginConfig.USER_TYPE, SmartLoginConfig.CUSTOMUSERFLAG);
                switch (user_type) {
                    case SmartLoginConfig.FACEBOOKFLAG:
                        LoginManager.getInstance().logOut();
                        break;
                    case SmartLoginConfig.GOOGLEFLAG:
                        GoogleApiClient mGoogleApiClient = SmartLoginActivity.getGoogleApiClient();
                        if(mGoogleApiClient != null) {
                            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
                            mGoogleApiClient.disconnect();
                        }
                        break;
                    case SmartLoginConfig.CUSTOMUSERFLAG:
                        if(!smartCustomLogoutListener.customUserSignout(userApi)){
                            throw new Exception("User not logged out");
                        }
                        break;
                    default:
                        break;
                }

                editor.remove(SmartLoginConfig.USER_TYPE);
                editor.remove(USER_SESSION);
                editor.apply();
                return true;
            } catch (Exception e){
                Log.e("User Logout Error", e.getMessage());
                DialogUtil.getErrorDialog(R.string.network_error, context).show();
                return false;
            }

        } catch (Exception e){
            Log.e("User Logout Error", e.getMessage());
            return false;
        }
    }
}

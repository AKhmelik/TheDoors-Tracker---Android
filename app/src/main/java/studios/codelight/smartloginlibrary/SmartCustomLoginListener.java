package studios.codelight.smartloginlibrary;

import studios.codelight.smartloginlibrary.users.SmartUser;


public interface SmartCustomLoginListener {
    boolean customSignin(SmartUser user);
    boolean customSignup(SmartUser newUser);
}

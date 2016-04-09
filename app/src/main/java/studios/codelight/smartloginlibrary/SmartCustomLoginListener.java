package studios.codelight.smartloginlibrary;

import ru.eqbeat.thedoorstracker.UserApi;


public interface SmartCustomLoginListener {
    boolean customSignin(UserApi user);
    boolean customSignup(UserApi newUser);
}

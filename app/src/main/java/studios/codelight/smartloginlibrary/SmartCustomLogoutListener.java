package studios.codelight.smartloginlibrary;

import ru.eqbeat.thedoorstracker.UserApi;

public interface SmartCustomLogoutListener {
    boolean customUserSignout(UserApi smartUser);
}

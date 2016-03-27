package studios.codelight.smartloginlibrary;

import studios.codelight.smartloginlibrary.users.SmartUser;

public interface SmartCustomLogoutListener {
    boolean customUserSignout(SmartUser smartUser);
}

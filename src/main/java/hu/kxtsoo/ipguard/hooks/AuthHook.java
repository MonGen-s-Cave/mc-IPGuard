package hu.kxtsoo.ipguard.hooks;

import java.util.List;

public interface AuthHook {
    List<String> getPlayersByIP(String ipAddress);
}

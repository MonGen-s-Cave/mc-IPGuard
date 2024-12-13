package hu.kxtsoo.ipguard.hooks.impl;

import fr.xephi.authme.api.v3.AuthMeApi;
import hu.kxtsoo.ipguard.hooks.AuthHook;

import java.util.List;

public class AuthMeHook implements AuthHook {

    @Override
    public List<String> getPlayersByIP(String ipAddress) {
        AuthMeApi authMeApi = AuthMeApi.getInstance();
        return authMeApi.getNamesByIp(ipAddress);
    }
}
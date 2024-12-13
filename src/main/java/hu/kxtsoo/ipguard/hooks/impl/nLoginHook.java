package hu.kxtsoo.ipguard.hooks.impl;

import com.nickuc.login.api.nLoginAPI;
import com.nickuc.login.api.types.AccountData;
import hu.kxtsoo.ipguard.hooks.AuthHook;

import java.util.List;
import java.util.stream.Collectors;

public class nLoginHook implements AuthHook {

    @Override
    public List<String> getPlayersByIP(String ipAddress) {
        List<AccountData> accounts = nLoginAPI.getApi().getAccountsByIp(ipAddress);
        return accounts.stream().map(AccountData::getLastName).collect(Collectors.toList());
    }
}
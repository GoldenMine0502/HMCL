package org.jackhuang.hellominecraft.launcher.utils.auth.yggdrasil;

import org.jackhuang.hellominecraft.launcher.utils.auth.AuthenticationException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jackhuang.hellominecraft.C;
import org.jackhuang.hellominecraft.HMCLog;
import org.jackhuang.hellominecraft.utils.NetUtils;
import org.jackhuang.hellominecraft.utils.StrUtils;

public class YggdrasilAuthentication {

    public static final Gson GSON = new GsonBuilder()
    .registerTypeAdapter(GameProfile.class, new GameProfile.GameProfileSerializer())
    .registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer())
    .registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    protected static final String BASE_URL = "https://authserver.mojang.com/";
    protected static final URL ROUTE_AUTHENTICATE = NetUtils.constantURL(BASE_URL + "authenticate");
    protected static final URL ROUTE_REFRESH = NetUtils.constantURL(BASE_URL + "refresh");

    protected static final String STORAGE_KEY_ACCESS_TOKEN = "accessToken";
    protected static final String STORAGE_KEY_PROFILE_NAME = "displayName";
    protected static final String STORAGE_KEY_PROFILE_ID = "uuid";
    protected static final String STORAGE_KEY_PROFILE_PROPERTIES = "profileProperties";
    protected static final String STORAGE_KEY_USER_NAME = "username";
    protected static final String STORAGE_KEY_USER_ID = "userid";
    protected static final String STORAGE_KEY_USER_PROPERTIES = "userProperties";

    private final Proxy proxy;
    private final String clientToken;
    private final PropertyMap userProperties = new PropertyMap();

    private String userid, username, password, accessToken;
    private GameProfile selectedProfile;
    private GameProfile[] profiles;
    private boolean isOnline;

    public YggdrasilAuthentication(Proxy proxy, String clientToken) {
        this.proxy = proxy;
        this.clientToken = clientToken;
    }

    // <editor-fold defaultstate="collapsed" desc="Get/Set">
    public void setUsername(String username) {
        if ((isLoggedIn()) && (canPlayOnline()))
            throw new IllegalStateException("Cannot change username while logged in & online");

        this.username = username;
    }

    public void setPassword(String password) {
        if ((isLoggedIn()) && (canPlayOnline()) && (StrUtils.isNotBlank(password)))
            throw new IllegalStateException("Cannot set password while logged in & online");

        this.password = password;
    }

    public GameProfile getSelectedProfile() {
        return this.selectedProfile;
    }

    public String getUserId() {
        return this.userid;
    }

    public PropertyMap getUserProperties() {
        if (isLoggedIn())
            return (PropertyMap) userProperties.clone();
        return new PropertyMap();
    }

    public GameProfile[] getAvailableProfiles() {
        return profiles;
    }

    public String getAuthenticatedToken() {
        return this.accessToken;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Log In/Out">
    public boolean canPlayOnline() {
        return isLoggedIn() && getSelectedProfile() != null && this.isOnline;
    }

    public boolean canLogIn() {
        return !canPlayOnline() && StrUtils.isNotBlank(username) && (StrUtils.isNotBlank(password) || StrUtils.isNotBlank(getAuthenticatedToken()));
    }

    public boolean isLoggedIn() {
        return StrUtils.isNotBlank(this.accessToken);
    }

    public void logIn() throws AuthenticationException {
        if (StrUtils.isBlank(username))
            throw new AuthenticationException(C.i18n("login.invalid_username"));

        if (StrUtils.isNotBlank(getAuthenticatedToken())) {
            if (StrUtils.isBlank(getUserId()))
                if (StrUtils.isNotBlank(username))
                    userid = username;
                else
                    throw new AuthenticationException(C.i18n("login.invalid_uuid_and_username"));

            loggedIn(ROUTE_REFRESH, new RefreshRequest(getAuthenticatedToken(), clientToken));
        } else if (StrUtils.isNotBlank(password))
            loggedIn(ROUTE_AUTHENTICATE, new AuthenticationRequest(username, password, clientToken));
        else
            throw new AuthenticationException(C.i18n("login.invalid_password"));
    }
    
    private void loggedIn(URL url, Object input) throws AuthenticationException {
        try {
            String jsonResult = input == null ? NetUtils.get(url) : NetUtils.post(url, GSON.toJson(input), "application/json", proxy);
            Response response = (Response) GSON.fromJson(jsonResult, Response.class);

            if (StrUtils.isNotBlank(response.error)) {
                HMCLog.err("Failed to log in, the auth server returned an error: " + response.error + ", message: " + response.errorMessage + ", cause: " + response.cause);
                if (response.errorMessage.contains("Invalid token")) {
                    response.errorMessage = C.i18n("login.invalid_token");
                }
                throw new AuthenticationException("Request error: " + response.errorMessage);
            }

            if (!clientToken.equals(response.clientToken))
                throw new AuthenticationException(C.i18n("login.changed_client_token"));

            User user = response.user;
            userid = user != null && user.id != null ? user.id : username;

            isOnline = true;
            profiles = response.availableProfiles;
            selectedProfile = response.selectedProfile;
            userProperties.clear();
            this.accessToken = response.accessToken;

            if (user != null && user.properties != null)
                userProperties.putAll(user.properties);
        } catch (IOException | IllegalStateException | JsonParseException e) {
            throw new AuthenticationException(C.i18n("login.failed.connect_authentication_server"), e);
        }
    }

    public void logOut() {
        password = null;
        userid = null;
        selectedProfile = null;
        userProperties.clear();

        accessToken = null;
        profiles = null;
        isOnline = false;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Settings Storage">
    public void loadFromStorage(Map<String, Object> credentials) {
        logOut();

        setUsername((String) credentials.get(STORAGE_KEY_USER_NAME));

        if (credentials.containsKey(STORAGE_KEY_USER_ID))
            userid = (String) credentials.get(STORAGE_KEY_USER_ID);
        else
            userid = username;

        if (credentials.containsKey(STORAGE_KEY_USER_PROPERTIES))
            userProperties.fromList((List<Map<String, String>>) credentials.get(STORAGE_KEY_USER_PROPERTIES));

        if ((credentials.containsKey(STORAGE_KEY_PROFILE_NAME)) && (credentials.containsKey(STORAGE_KEY_PROFILE_ID))) {
            GameProfile profile = new GameProfile(UUIDTypeAdapter.fromString((String) credentials.get(STORAGE_KEY_PROFILE_ID)), (String) credentials.get(STORAGE_KEY_PROFILE_NAME));
            if (credentials.containsKey(STORAGE_KEY_PROFILE_PROPERTIES))
                profile.properties.fromList((List<Map<String, String>>) credentials.get(STORAGE_KEY_PROFILE_PROPERTIES));
            selectedProfile = profile;
        }

        this.accessToken = (String) credentials.get(STORAGE_KEY_ACCESS_TOKEN);
    }

    public Map<String, Object> saveForStorage() {
        Map<String, Object> result = new HashMap<>();

        if (username != null)
            result.put(STORAGE_KEY_USER_NAME, username);
        if (getUserId() != null)
            result.put(STORAGE_KEY_USER_ID, getUserId());

        if (!getUserProperties().isEmpty())
            result.put(STORAGE_KEY_USER_PROPERTIES, getUserProperties().list());

        GameProfile sel = getSelectedProfile();
        if (sel != null) {
            result.put(STORAGE_KEY_PROFILE_NAME, sel.name);
            result.put(STORAGE_KEY_PROFILE_ID, sel.id);
            if (!sel.properties.isEmpty())
                result.put(STORAGE_KEY_PROFILE_PROPERTIES, sel.properties.list());
        }

        if (StrUtils.isNotBlank(getAuthenticatedToken()))
            result.put(STORAGE_KEY_ACCESS_TOKEN, getAuthenticatedToken());

        return result;
    }

    // </editor-fold>
}

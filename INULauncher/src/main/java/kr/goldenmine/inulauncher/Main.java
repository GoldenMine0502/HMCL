package kr.goldenmine.inulauncher;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jackhuang.hmcl.auth.*;
import org.jackhuang.hmcl.auth.microsoft.MicrosoftAccount;
import org.jackhuang.hmcl.auth.microsoft.MicrosoftAccountFactory;
import org.jackhuang.hmcl.auth.microsoft.MicrosoftService;
import org.jackhuang.hmcl.auth.microsoft.MicrosoftSession;
import org.jackhuang.hmcl.auth.yggdrasil.GameProfile;
import org.jackhuang.hmcl.auth.yggdrasil.YggdrasilService;
import org.jackhuang.hmcl.game.OAuthServer;
import org.jackhuang.hmcl.setting.Accounts;
import org.jackhuang.hmcl.task.Schedulers;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.ui.WeakListenerHolder;
import org.jackhuang.hmcl.util.io.HttpRequest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.logging.Level;

import static org.jackhuang.hmcl.util.Logging.LOG;
import static org.jackhuang.hmcl.util.Pair.pair;

public class Main {
//    static GameProfile gameProfile;

    public static void main(String[] args) throws Exception {
//        Task.supplyAsync(account::logInWhenCredentialsExpired)
//                .whenComplete(Schedulers.javafx(), (authInfo, exception) -> {
//                    if (exception == null) {
////                        success.accept(authInfo);
////                        onSuccess();
//                    } else {
//                        LOG.log(Level.INFO, "Failed to login when credentials expired: " + account, exception);
////                        onFailure(Accounts.localizeErrorMessage(exception));
//                    }
//                }).start();
//        MICROSOFT.authenticate();

//        OAuthServer.Factory server = Accounts.OAUTH_CALLBACK;

//        server.grantDeviceCode();
//        OAuthServer session = (OAuthServer) server.startServer();
//        session.getIdToken();
//        session.getRedirectURI();
        //
//
        final String SCOPE = "XboxLive.signin offline_access";

        OAuthServer.Factory callback = Accounts.OAUTH_CALLBACK;
        callback.customClientId = LauncherProperties.MAIN.getClientId();

//        callback.startServer();
//        System.out.println(callback.getClientId());

        MicrosoftAccountFactory factory = Accounts.FACTORY_MICROSOFT;
        MicrosoftService service = factory.getService();
        OAuth.Result result = OAuth.MICROSOFT.authenticate(OAuth.GrantFlow.AUTHORIZATION_CODE, new OAuth.Options(SCOPE, callback));
        System.out.println(result.getAccessToken());
        System.out.println(result.getRefreshToken());
//        MicrosoftSession session = authenticateViaLiveAccessToken(result.getAccessToken(), result.getRefreshToken());
//        MicrosoftSession session = service.authenticate();

//        System.out.println(session.toStorage());
////        new CreateAccountPane.DialogCharacterSelector(), username, password, null, additionalData)
//        MicrosoftAccount account = factory.create(new NullDialogSelector(), id, password, null, null);
//        AuthInfo authInfo = account.logIn();
//
//        String accessToken = authInfo.getAccessToken();
//        String userName = authInfo.getUsername();
//        String uuid = authInfo.getUUID().toString();
//
//        System.out.println(accessToken);
//        System.out.println(userName);
//        System.out.println(uuid);
//
//        authInfo.close();
    }

    static class NullDialogSelector implements CharacterSelector {
        @Override
        public GameProfile select(YggdrasilService yggdrasilService, List<GameProfile> names) throws NoSelectedCharacterException {
            return null;
        }
    }
}

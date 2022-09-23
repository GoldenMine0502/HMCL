package kr.goldenmine.inulauncher;

import org.jackhuang.hmcl.auth.*;
import org.jackhuang.hmcl.auth.microsoft.MicrosoftAccount;
import org.jackhuang.hmcl.auth.microsoft.MicrosoftAccountFactory;
import org.jackhuang.hmcl.auth.microsoft.MicrosoftService;
import org.jackhuang.hmcl.auth.yggdrasil.GameProfile;
import org.jackhuang.hmcl.auth.yggdrasil.YggdrasilService;
import org.jackhuang.hmcl.game.OAuthServer;
import org.jackhuang.hmcl.setting.Accounts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class Main {
//    static GameProfile gameProfile;

    public static void main(String[] args) throws Exception {
//        gameProfile = new GameProfile(UUID.randomUUID(), "asdf");

        String id = null;
        String password = null;

        try(BufferedReader reader = new BufferedReader(new FileReader("account/account.txt"))) {
            id = reader.readLine();
            password = reader.readLine();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println(id);
        System.out.println(password);


        OAuthServer.Factory server = Accounts.OAUTH_CALLBACK;

        OAuthServer session = (OAuthServer) server.startServer();


        MicrosoftAccountFactory factory = Accounts.FACTORY_MICROSOFT;
//        new CreateAccountPane.DialogCharacterSelector(), username, password, null, additionalData)
        MicrosoftAccount account = factory.create(new NullDialogSelector(), id, password, null, null);
        AuthInfo authInfo = account.logIn();

        String accessToken = authInfo.getAccessToken();
        String userName = authInfo.getUsername();
        String uuid = authInfo.getUUID().toString();

        System.out.println(accessToken);
        System.out.println(userName);
        System.out.println(uuid);

        authInfo.close();
    }

    static class NullDialogSelector implements CharacterSelector {
        @Override
        public GameProfile select(YggdrasilService yggdrasilService, List<GameProfile> names) throws NoSelectedCharacterException {
            return null;
        }
    }
}

package kr.goldenmine.inulauncher;

import java.io.BufferedReader;
import java.io.FileReader;

public class LauncherProperties {
    public static final LauncherProperties MAIN;

    private String id;
    private String password;
    private String clientId;

    static {
        String clientId = null;
        String id = null;
        String password = null;

        try(BufferedReader reader = new BufferedReader(new FileReader("account/account.txt"))) {
            clientId = reader.readLine();
            id = reader.readLine();
            password = reader.readLine();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        MAIN = new LauncherProperties(clientId, id, password);
    }

    public LauncherProperties(String clientId, String id, String password) {
        this.clientId = clientId;
        this.id = id;
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }
}

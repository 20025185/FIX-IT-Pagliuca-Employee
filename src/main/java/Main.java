import GUI.dialogs.LoginWindow;

import java.io.IOException;

import static firebase.FirebaseAPI.initializeFirebase;

public class Main {
    public static void main(String[] args) throws IOException {
        new LoginWindow();
        initializeFirebase();
    }
}
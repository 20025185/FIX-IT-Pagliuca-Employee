import GUI.dialogs.LoginWindow;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        new LoginWindow();
        initializeFirebase();
    }

    public static void initializeFirebase() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("pagliu-db-firebase-adminsdk-ml5ap-f0eb21cf94.json");

        @SuppressWarnings("deprecation") FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://pagliu-db.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);
    }
}
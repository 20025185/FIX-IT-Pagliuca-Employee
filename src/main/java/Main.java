import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    private static FileInputStream refreshToken;
    private static FirebaseAuth fAuth;
    private static FirebaseDatabase rootNode;
    private static DatabaseReference dbr;
    private static FileInputStream serviceAccount;
    private static FirebaseOptions options;
    private static LoginWindow loginWindow;

    public static void main(String[] args) throws IOException {

        LoginWindow loginWindow = new LoginWindow();

        initializeFirebase();

    }

    public static void initializeFirebase() throws IOException {
        serviceAccount = new FileInputStream("pagliu-db-firebase-adminsdk-ml5ap-f0eb21cf94.json");

        options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://pagliu-db.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);
    }


}

import GUI.Board;
import GUI.dialogs.LoginWindow;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import utils.Employee;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        LoginWindow loginWindow = new LoginWindow();
        initializeFirebase();

        //  testing
        Employee employee = new Employee("asdfdas@test.it", "PGLMNKL", "Manuel", "Pagliuca");
        Board board = new Board(employee);
    }

    public static void initializeFirebase() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("pagliu-db-firebase-adminsdk-ml5ap-f0eb21cf94.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://pagliu-db.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);
    }
}
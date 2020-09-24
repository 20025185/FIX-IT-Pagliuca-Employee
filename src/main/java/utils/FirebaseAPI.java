package utils;

import com.google.firebase.database.*;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

@SuppressWarnings("JavaDoc")
public class FirebaseAPI {
    private final static String BASE_URL = "https://identitytoolkit.googleapis.com/v1/accounts:";
    private final static String firebaseKey = "AIzaSyAvOgNrXpFdMpNhi7KgyXq0Bav7WejwRk0";
    private final static String OPERATION_AUTH = "signInWithPassword";
    private Employee employee = null;

    /***
     * Metodo che permette l'autenticazione sul server di Firebase con FirebaseAuthentication, essendo non possibile effettuare l'autenticazione su
     * questa piattaforma da Desktop (le API sono strettamente confinate per Mobile) ho dovuto utilizzare le REST API.
     * Facendo una GET con all'interno Username e Password nella richiesta, il server se trova l'account risponde con un JSON contenente email, uid, tokenID,
     * in caso contrario darà una 400 Bad Request.
     * @param username
     * @param password
     * @return
     */
    public Employee signInWithPassword(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Non sono stante inserite le credenziali.",
                    "Credenziali non valide",
                    JOptionPane.ERROR_MESSAGE);
        }

        URL url;
        URLConnection con;
        HttpURLConnection urlRequest;

        String email, uid, token;

        try {
            url = new URL(BASE_URL + OPERATION_AUTH + "?key=" + firebaseKey);
            con = url.openConnection();
            urlRequest = (HttpURLConnection) con;
            urlRequest.setRequestMethod("POST");
            urlRequest.setDoOutput(true);
            urlRequest.setRequestProperty("Content-Type", "application/json");

            OutputStream os = urlRequest.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            osw.write("{\"email\":\"" + username + "\",\"password\":\"" + password + "\",\"returnSecureToken\":true}");
            osw.flush();
            osw.close();
            urlRequest.connect();

            JSONObject jsonObject = convertInputStreamToJSONObject((InputStream) urlRequest.getContent());

            email = jsonObject.get("email").toString();
            uid = jsonObject.get("localId").toString();
            token = jsonObject.get("idToken").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return retrieveOtherInfo(email, uid, token);
    }

    /***
     *  Le stringhe contenenti la risposta Json smistata vengono passate a questo metodo, il quale si occupa di cercare all'interno del
     *  Real Time Database la presenza dell'UID nella tabella degli impiegati "employee".
     *  Se la presenza ne è verificata, in tal caso si creerà un oggetto Employee contenente i dati precedentemente ottenuti dal Json e
     *  i dati ottenuti dal RTB di Firebase.
     *  In caso contrario si restituirà un valore nullo.
     * @param email
     * @param uid
     * @param token
     * @return
     */
    private Employee retrieveOtherInfo(String email, String uid, String token) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("employee");
        CountDownLatch countDownLatch = new CountDownLatch(1);

        databaseReference.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    employee = new Employee();
                    employee.setEmail(email);
                    employee.setUID(uid);
                    employee.setTokenID(token);
                    employee.setFiscalCode(dataSnapshot.child("fiscalCode").getValue().toString());
                    employee.setFullname(dataSnapshot.child("fullname").getValue().toString());
                    employee.setSurname(dataSnapshot.child("surname").getValue().toString());
                    employee.setImageURL(dataSnapshot.child("imageURL").getValue().toString());
                    employee.setBirthday(dataSnapshot.child("birthday").getValue().toString());
                }
                countDownLatch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return employee;
    }

    /***
     * Funzione ausiliaria, serve per convertire i dati che si ricevono sullo stream in ingresso in un oggetto Json.
     * @param inputStream
     * @return
     * @throws JSONException
     * @throws IOException
     */
    private static JSONObject convertInputStreamToJSONObject(InputStream inputStream)
            throws JSONException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null)
            result.append(line);

        inputStream.close();
        return new JSONObject(result.toString());
    }

}
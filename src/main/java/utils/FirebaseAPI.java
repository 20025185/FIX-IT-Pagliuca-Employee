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

public class FirebaseAPI {
    private final static String BASE_URL = "https://identitytoolkit.googleapis.com/v1/accounts:";
    private final static String firebaseKey = "AIzaSyAvOgNrXpFdMpNhi7KgyXq0Bav7WejwRk0";
    private final static String OPERATION_AUTH = "signInWithPassword";
    boolean isEmployee = false;

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
        Employee employee = new Employee();

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

            employee.setEmail(jsonObject.get("email").toString());
            employee.setUID(jsonObject.get("localId").toString());
            employee.setTokenID(jsonObject.get("idToken").toString());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        //  Chiamo la funzione per arrichire i dati dell'impiegato nel caso fosse un impiegato, nel caso contrario restituisco null.
        return retrieveOtherInfo(employee);
    }

    private Employee retrieveOtherInfo(Employee employee) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("employee");

        databaseReference.child(employee.getUID()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(employee.getUID()).getValue() != null) {
                    employee.setFiscalCode(dataSnapshot.child("fiscalCode").getValue().toString());
                    employee.setFullname(dataSnapshot.child("fullname").getValue().toString());
                    employee.setSurname(dataSnapshot.child("surname").getValue().toString());
                    employee.setImageURL(dataSnapshot.child("imageURL").getValue().toString());
                    employee.setBirthday(dataSnapshot.child("birthday").getValue().toString());
                    isEmployee = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //  Controllo se è un impiegato, nella LoginWindow nella riga 107, si mostrerà un dialog in caso contrario.
        if (isEmployee) {
            return employee;
        } else {
            return null;
        }
    }

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

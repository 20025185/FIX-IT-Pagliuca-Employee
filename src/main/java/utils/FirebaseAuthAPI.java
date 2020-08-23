package utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class FirebaseAuthAPI {
    private static final String BASE_URL = "https://identitytoolkit.googleapis.com/v1/accounts:";
    private static final String OPERATION_AUTH = "signInWithPassword";
    private static final String OPERATION_REFRESH_TOKEN = "token";
    private static final String OPERATION_ACCOUNT_INFO = "lookup";

    private String firebaseKey;
    private static FirebaseAuthAPI instance = null;

    public FirebaseAuthAPI() {
        firebaseKey = "AIzaSyAvOgNrXpFdMpNhi7KgyXq0Bav7WejwRk0";
    }

    public static FirebaseAuthAPI getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthAPI();
        }
        return instance;
    }

    public Employee auth(String username, String password) throws Exception {
        HttpURLConnection urlRequest = null;
        String uid = null;

        Employee employee = new Employee();

        try {
            URL url = new URL(BASE_URL + OPERATION_AUTH + "?key=" + firebaseKey);
            urlRequest = (HttpURLConnection) url.openConnection();
            urlRequest.setDoOutput(true);
            urlRequest.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStream os = urlRequest.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write("{\"email\":\"" + username + "\",\"password\":\"" + password + "\",\"returnSecureToken\":true}");
            osw.flush();
            osw.close();
            urlRequest.connect();
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) urlRequest.getContent())); //Convert the input stream to a json element
            JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.

            employee.setEmail(rootobj.get("email").getAsString());
            employee.setUID(rootobj.get("localId").getAsString());
            employee.setTokenID(rootobj.get("idToken").getAsString());

        } catch (Exception e) {
            return null;
        } finally {
            urlRequest.disconnect();
        }
        return employee;
    }

    public String getAccountInfo(String token) throws Exception {
        HttpURLConnection urlRequest = null;
        String email = null;

        try {
            URL url = new URL(BASE_URL + OPERATION_ACCOUNT_INFO + "?key=" + firebaseKey);
            urlRequest = (HttpURLConnection) url.openConnection();
            urlRequest.setDoOutput(true);
            urlRequest.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStream os = urlRequest.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write("{\"idToken\":\"" + token + "\"}");
            osw.flush();
            osw.close();
            urlRequest.connect();

            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) urlRequest.getContent())); //Convert the input stream to a json element
            JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.

            email = rootobj.get("users").getAsJsonArray().get(0).getAsJsonObject().get("email").getAsString();

        } catch (Exception e) {
            return null;
        } finally {
            urlRequest.disconnect();
        }
        return email;

    }
}

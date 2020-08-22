public class Employee {
    private static String uid;
    private String email;
    private String tokenID;

    Employee(){

    }

    Employee(String t_uid){
        uid = t_uid;
    }

    public static String getUid() {
        return uid;
    }

    public static void setUid(String uid) {
        Employee.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    public String getUID(){
        return uid;
    }

    public void setUID(String t_uid){
        uid = t_uid;
    }
}

package utils;

public class Employee {
    private static String uid;
    private static String tokenID;
    private String email;
    private String fiscalCode;
    private String fullname;
    private String surname;

    public Employee(){

    }

    public Employee(String email, String fiscalCode, String fullname, String surname) {
        this.email = email;
        this.fiscalCode = fiscalCode;
        this.fullname = fullname;
        this.surname = surname;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "email='" + email + '\'' +
                ", fiscalCode='" + fiscalCode + '\'' +
                ", fullname='" + fullname + '\'' +
                ", surname='" + surname + '\'' +
                '}';
    }

    Employee(String t_uid){
        uid = t_uid;
    }

    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
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

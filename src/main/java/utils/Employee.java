package utils;

public class Employee {
    private String uid;
    private String tokenID;
    private String email;
    private String fiscalCode;
    private String fullname;
    private String surname;
    private String imageURL;
    private String birthday;

    public Employee() {

    }

    public Employee(String uid, String tokenID, String email, String fiscalCode, String fullname, String surname, String imageURL) {
        this.uid = uid;
        this.tokenID = tokenID;
        this.email = email;
        this.fiscalCode = fiscalCode;
        this.fullname = fullname;
        this.surname = surname;
        this.imageURL = imageURL;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "uid='" + uid + '\'' +
                ", tokenID='" + tokenID + '\'' +
                ", email='" + email + '\'' +
                ", fiscalCode='" + fiscalCode + '\'' +
                ", fullname='" + fullname + '\'' +
                ", surname='" + surname + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", birthday='" + birthday + '\'' +
                '}';
    }

    public Employee(String uid, String tokenID, String email, String fiscalCode, String fullname, String surname, String imageURL, String birthday) {
        this.uid = uid;
        this.tokenID = tokenID;
        this.email = email;
        this.fiscalCode = fiscalCode;
        this.fullname = fullname;
        this.surname = surname;
        this.imageURL = imageURL;
        this.birthday = birthday;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Employee(String email, String fiscalCode, String fullname, String surname) {
        this.email = email;
        this.fiscalCode = fiscalCode;
        this.fullname = fullname;
        this.surname = surname;
    }

    Employee(String t_uid) {
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getUID() {
        return uid;
    }

    public void setUID(String t_uid) {
        uid = t_uid;
    }

}

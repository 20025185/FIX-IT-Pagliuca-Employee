package firebase;

/***
 * Classe che mappa un istanza impiegato del Real Time Database degli impiegati in un oggetto Java.
 */
@SuppressWarnings("ALL")
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String tokenID) {
        this.tokenID = tokenID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}

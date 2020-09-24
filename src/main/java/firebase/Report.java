package firebase;

/***
 * Classe che mappa una segnalazione presente sul Real Time Database di Firebase in un oggetto Java.
 */

@SuppressWarnings("ALL")
public class Report {
    private String uid;
    private String id;
    private String date;
    private String time;
    private String object;
    private String position;
    private String description;
    private String type;
    private String rating;
    private String dataChiusura;
    private String priority = "-1";
    private String status = "undefined";
    private boolean social;

    @Override
    public String toString() {
        return "Report{" +
                "uid='" + uid + '\'' +
                ", id='" + id + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", object='" + object + '\'' +
                ", position='" + position + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", rating='" + rating + '\'' +
                ", dataChiusura='" + dataChiusura + '\'' +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                ", social=" + social +
                '}';
    }

    public Report(String id, String uid, String object, String description,
                  String date, String time, String type, String position,
                  String priority, String status, boolean social) {
        this.id = id;
        this.uid = uid;
        this.object = object;
        this.description = description;
        this.date = date;
        this.time = time;
        this.type = type;
        this.position = position;
        this.priority = priority;
        this.status = status;
        this.social = social;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDataChiusura() {
        return dataChiusura;
    }

    public void setDataChiusura(String dataChiusura) {
        this.dataChiusura = dataChiusura;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSocial() {
        return social;
    }

    public void setSocial(boolean social) {
        this.social = social;
    }
}

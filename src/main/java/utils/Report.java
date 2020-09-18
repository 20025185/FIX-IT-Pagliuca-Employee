package utils;

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

    public void setDataChiusura(String dataChiusura) {
        this.dataChiusura = dataChiusura;
    }

    public String getDataChiusura() {
        return dataChiusura;
    }

    @Override
    public String toString() {
        return "Report{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", object='" + object + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", type='" + type + '\'' +
                ", position='" + position + '\'' +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                ", social='" + Boolean.toString(social) + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Report() {
        this.id = "";
        this.uid = "";
        this.object = "";
        this.description = "";
        this.date = "";
        this.time = "";
        this.type = "";
        this.position = "";
        this.priority = "";
        this.status = "";
        this.social = false;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
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

    public boolean getSocial() {
        return social;
    }

    public void setSocial(boolean social) {
        this.social = social;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

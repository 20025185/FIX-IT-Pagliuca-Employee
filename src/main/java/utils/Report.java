package utils;

public class Report {

    private String id;
    private String uid;
    private String object;
    private String description;
    private String date;
    private String time;
    private String type;
    private String position;
    private String priority;
    private String status;
    private String social;

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
                ", social='" + social + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Report(){
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
        this.social = "";
    }

    public Report(String id, String uid, String object, String description, String date, String time, String type, String position, String priority, String status, String social) {
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

    public String getSocial() {
        return social;
    }

    public void setSocial(String social) {
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

package hk.edu.cuhk.ie.iems5722.group33;

public class Msg {
    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SENT = 1;
    private int type;
    private int user_id;
    private String name;
    private String message;
    private String message_time;

    public Msg(int user_id, String name, String message, String message_time, int type) {
        this.user_id = user_id;
        this.name = "User: " + name;
        this.message = message;
        this.message_time = message_time;
        this.type = type;
    }

    public int getuserId() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getMessage_time() {
        return message_time;
    }

    public int getType() {
        return type;
    }
}


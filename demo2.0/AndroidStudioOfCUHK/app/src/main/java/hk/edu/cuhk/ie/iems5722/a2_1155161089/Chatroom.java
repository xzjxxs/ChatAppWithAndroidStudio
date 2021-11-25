package hk.edu.cuhk.ie.iems5722.a2_1155161089;

import java.io.Serializable;

public class Chatroom implements Serializable {
    private String id;
    private String name;

    public Chatroom(String id, String name) {
        this.name = name;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}

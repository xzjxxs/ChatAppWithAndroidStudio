package hk.edu.cuhk.ie.iems5722.a4_1155161089;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class ChatroomAdapter extends ArrayAdapter<Chatroom> {
    private int resourceId;

    public ChatroomAdapter(Context context, int textViewResourceId, List<Chatroom> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            convertView= LayoutInflater.from(getContext()).inflate(resourceId, null);
        }
        Chatroom chatRoom=getItem(position); //Gets an chatroom title instance of the current item
        TextView textViewRoom = (TextView) convertView.findViewById(R.id.room_title);
        textViewRoom.setText(chatRoom.getName());
        return  convertView;
    }
}

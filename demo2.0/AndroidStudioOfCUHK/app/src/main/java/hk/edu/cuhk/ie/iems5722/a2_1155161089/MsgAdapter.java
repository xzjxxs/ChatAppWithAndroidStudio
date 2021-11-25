package hk.edu.cuhk.ie.iems5722.a2_1155161089;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MsgAdapter extends ArrayAdapter<Msg> {
    private int resourceId;

    public MsgAdapter(Context context, int textViewResourceId, List<Msg> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Msg msg = getItem(position);
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
            viewHolder.rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
            viewHolder.leftUser = (TextView) view.findViewById(R.id.leftMsg_name);
            viewHolder.rightUser = (TextView) view.findViewById(R.id.rightMsg_name);
            viewHolder.leftMsg = (TextView) view.findViewById(R.id.leftMsg_msg);
            viewHolder.rightMsg = (TextView) view.findViewById(R.id.rightMsg_msg);
            viewHolder.leftMsgTime = (TextView) view.findViewById(R.id.leftMsg_time);
            viewHolder.rightMsgTime = (TextView) view.findViewById(R.id.rightMsg_time);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if (msg.getType() == Msg.TYPE_RECEIVED) { // if is received message, show the left layout
            viewHolder.leftLayout.setVisibility(View.VISIBLE);
            viewHolder.rightLayout.setVisibility(View.GONE);
            viewHolder.leftMsg.setText(msg.getMessage());
            viewHolder.leftUser.setText(msg.getName());
            viewHolder.leftMsgTime.setText(msg.getMessage_time());
        } else if (msg.getType() == Msg.TYPE_SENT) { // if is sent message, show the right layout
            viewHolder.rightLayout.setVisibility(View.VISIBLE);
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.rightMsg.setText(msg.getMessage());
            viewHolder.rightUser.setText(msg.getName());
            viewHolder.rightMsgTime.setText(msg.getMessage_time());
        }
        return view;
    }

    class ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        TextView leftUser;
        TextView rightUser;
        TextView leftMsgTime;
        TextView rightMsgTime;
    }

    // set the load direction
    @Override
    public Msg getItem(int position) {
        return super.getItem(super.getCount() - position - 1);
    }

}
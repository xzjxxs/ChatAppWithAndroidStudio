package hk.edu.cuhk.ie.iems5722.a1_1155161089;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ListView msgListView;
    private EditText inputText;
    private ImageButton send;
    private MsgAdapter adapter;
    private List<Msg> msgList = new ArrayList<Msg>();
    ImageButton button = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // set the time format
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

        adapter = new MsgAdapter(ChatActivity.this, R.layout.record, msgList);
        msgListView = (ListView) findViewById(R.id.msgListView);
        msgListView.setAdapter(adapter);

        inputText = (EditText) findViewById(R.id.messageInput);
        send = (ImageButton) findViewById(R.id.sendButton);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the current time
                Date curTime = new Date(System.currentTimeMillis());
                String timeMsg = simpleDateFormat.format(curTime);
                // get the input text with time
                String inputMsg = inputText.getText().toString();
                if (!"".equals(inputMsg)) {
                    String content = inputText.getText().append("\n\n" + timeMsg).toString();
                    Msg msg = new Msg(content, Msg.TYPE_SENT);
                    msgList.add(msg);
                    adapter.notifyDataSetChanged(); // refresh the list view
                    msgListView.setSelection(msgList.size());
                    inputText.setText("");
                } else { // check if there is no input
                    Toast.makeText(getApplicationContext(), "Empty Message", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    // back button of action bar
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}


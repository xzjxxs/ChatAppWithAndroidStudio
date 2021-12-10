package hk.edu.cuhk.ie.iems5722.a4_1155161089;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
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
    String chatroomId;

    public int total_pages;
    public int next_page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // action bar
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); // set the keyboard

        // extract the extras from MainActivity
        Bundle extras = getIntent().getExtras();
        chatroomId = extras.getString("chatroomId");
        String chatroomName = extras.getString("chatroomName");
        Log.e("Current in chatroom: ", String.valueOf(chatroomId));
        this.setTitle(chatroomName);

        // define the time format
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        // set the adapter for message list view
        adapter = new MsgAdapter(ChatActivity.this, R.layout.record, msgList);
        msgListView = (ListView) findViewById(R.id.msgListView);
        msgListView.setAdapter(adapter);

        // perform background operation
        String url = "http://18.116.63.13/api/a4/get_messages?chatroom_id=" + chatroomId + "&page=1";
        listMsgTask task = new listMsgTask();
        task.execute(url);

        // load the next page
        msgListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private boolean isFirst = true;

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (msgListView.getChildAt(firstVisibleItem) != null) {
                    isFirst = false;
                    int top = msgListView.getChildAt(firstVisibleItem).getTop();
                    if (firstVisibleItem == 0 && top == 0) {
                        isFirst = true;
                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (isFirst && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (next_page <= total_pages) {
                        // load the next page
                        Log.e("next_page", String.valueOf(next_page));
                        String newUrl = "http://18.116.63.13/api/a4/get_messages?chatroom_id=" + chatroomId + "&page=" + next_page;
                        Log.e("newUrl", newUrl);
                        listMsgTask task = new listMsgTask();
                        task.execute(newUrl);

                    }
                }
            }
        });

        inputText = (EditText) findViewById(R.id.messageInput);
        send = (ImageButton) findViewById(R.id.sendButton);
        send.setOnClickListener(v -> {
            // get the current time
            Date curTime = new Date(System.currentTimeMillis());
            String timeMsg = simpleDateFormat.format(curTime);

            // get the input text with time
            String sendMsg = inputText.getText().toString();
            if (!"".equals(sendMsg) && sendMsg.length() < 200) {
//                Msg msg = new Msg(1155161089, "Xu", sendMsg, timeMsg, 1);

                // perform background operation
                String sendUrl = "http://18.116.63.13/api/a4/send_message";
                sendMsgTask sendTask = new sendMsgTask();
                sendTask.execute(sendUrl, chatroomId, "1155161089", "XU", sendMsg);

                // refresh
                String newUrl = "http://18.116.63.13/api/a4/get_messages?chatroom_id=" + chatroomId + "&page=1";
                listMsgTask refreshTask = new listMsgTask();
                refreshTask.execute(newUrl);
            } else if ("".equals(sendMsg)) { // chek if there is no input
                Toast.makeText(getApplicationContext(), "Empty Message", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Too Long Message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // define asynchronous tasks in the background
    private class listMsgTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... urls) {
            String msgData = "";
            InputStream is = null;
            Log.e("urls", String.valueOf(urls));
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000); // 10,000 milliseconds
                conn.setConnectTimeout(15000); // 15,000 milliseconds
                conn.setRequestMethod("GET"); // use the GET method
                conn.setDoInput(true);
                // Start the query
                conn.connect();
                int response = conn.getResponseCode(); // This will be 200 if successful
                is = conn.getInputStream();
                if (response == HttpURLConnection.HTTP_OK) {
                    Log.e("response success", "success get url content");
                }

                // Convert the InputStream into a string
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    msgData += line;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close(); // Close the InputStream when done
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.e("chatroomData", msgData);
            return msgData;
        }

        @Override
        protected void onPostExecute(String msgResult) {
            // Extract data
            try {
                JSONObject json = new JSONObject(msgResult); //{"data", "status"}
                String status = json.getString("status");
                JSONObject dataArray = json.getJSONObject("data");//{"message", "total_pages", "current_page"}
                total_pages = dataArray.getInt("total_pages");
                next_page = dataArray.getInt("current_page") + 1;

                Msg msg;
                JSONArray messagesArray = dataArray.getJSONArray("messages");
                if (status.equals("OK")) {
                    for (int i = 0; i < messagesArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) messagesArray.get(i);
                        //extra data
                        int user_id = jsonObject.getInt("user_id");
                        String name = jsonObject.getString("name");
                        String message = jsonObject.getString("message");
                        String message_time = jsonObject.getString("message_time");
                        // check whether is message sent by myself
                        if (user_id == 1155161089) {
                            msg = new Msg(user_id, name, message, message_time, 1);
                        } else {
                            msg = new Msg(user_id, name, message, message_time, 0);
                        }
                        msgList.add(msg);
                        adapter.notifyDataSetChanged(); // refresh the list view
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    // define asynchronous tasks in the background
    private class sendMsgTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                Uri.Builder builder = new Uri.Builder();

                // build the parameters using ArrayList objects para_names and para_values
                builder.appendQueryParameter("chatroom_id", urls[1]);
                builder.appendQueryParameter("user_id", urls[2]);
                builder.appendQueryParameter("name", urls[3]);
                builder.appendQueryParameter("message", urls[4]);

                String query = builder.build().getEncodedQuery();
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                int response = conn.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    Log.d("Send Message:", "Success");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String sendResult) {
            msgList.clear();
            inputText.setText("");
        }
    }

    // set the refresh function
    public void refresh() {
        msgList.clear();
        String newUrl = "http://18.116.63.13/api/a4/get_messages?chatroom_id=" + chatroomId + "&page=1";
        listMsgTask refreshTask = new listMsgTask();
        refreshTask.execute(newUrl);
    }

    // set the actionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // back to the home page
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case R.id.action_refresh:
                refresh();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}


package hk.edu.cuhk.ie.iems5722.group33;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private final List<Chatroom> chatroomList = new ArrayList<>();
    private ChatroomAdapter roomAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.region_right_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {// back button
        switch (item.getItemId()) {
            case R.id.add_friends:
                //Toast.makeText(AddFriendsActivity, "Add Friends", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, AddFriendsActivity.class);
                startActivity(intent);
                break;
            case R.id.upload_files://Toast.makeText(AddFriendsActivity, "Add Friends", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MainActivity.this, FileActivity.class);
                startActivity(i);
                break;
            case R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isGooglePlayServiceAvailable(this)) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            this.setTitle(LoginActivity.uid);

            // set the adapter for chatroom list view
            roomAdapter = new ChatroomAdapter(this, R.layout.roomlistview, chatroomList);
            ListView chatroomListView = findViewById(R.id.room_list_view); // set the view position
            chatroomListView.setAdapter(roomAdapter);

            String url = "http://18.116.63.13/api/a4/get_chatrooms/" + LoginActivity.uid;
            listRoomTask task = new listRoomTask();
            task.execute(url);
            Log.e("chatroomList", String.valueOf(chatroomList));

            // set click listener to enter the chatroom
            chatroomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                    intent.putExtra("chatroomId", chatroomList.get(position).getId());
                    Log.d("chatroomId", chatroomList.get(position).getId());
                    intent.putExtra("chatroomName", chatroomList.get(position).getName());
                    Log.d("chatroomName", chatroomList.get(position).getName());
                    startActivity(intent);
                }
            });
        }
    }

    public boolean isGooglePlayServiceAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 9000).show();
            }
            return false;
        }
        return true;
    }

    // define asynchronous tasks in the background
    private class listRoomTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... urls) {
            String chatroomData = "";
            InputStream is = null;
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
                    Log.e("response success", "Success get url content");
                }

                // Convert the InputStream into a string
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    chatroomData += line;
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
            return chatroomData;
        }

        @Override
        protected void onPostExecute(String chatroomResult) {
            // Extract data
            try {
                JSONObject json = new JSONObject(chatroomResult);

                JSONArray jsonArray = json.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                    //get name
                    String name = jsonObject.getString("name");
                    String roomId = jsonObject.getString("id");
                    Chatroom chatroom = new Chatroom(roomId, name);
                    chatroomList.add(chatroom);
                    roomAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

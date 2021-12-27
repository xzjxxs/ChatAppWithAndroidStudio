package hk.edu.cuhk.ie.iems5722.group33;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class AddFriendsActivity extends AppCompatActivity {
    private static final String TAG = "AddFriendsActivity";
    private EditText addfriendsid;
    private EditText addfriendsname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfriends);
        addfriendsid = findViewById(R.id.addfriendsid);
        //password = findViewById(R.id.password);
        addfriendsname = findViewById(R.id.addfriendsname);
        this.setTitle("Add Friends");
    }

    public void add(View v) {
        switch (v.getId()) {
            case R.id.addfriendsBtn:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        //userinfo = (UserInfo) getApplicationContext();
                        String uafid = addfriendsid.getText().toString().trim();
//                        //String pwd = password.getText().toString().trim();
                        String uafn = addfriendsname.getText().toString().trim();
                        String addfriend_url = "http://18.116.63.13/api/a4/addfriend";/*change your server address in here*/
                        Log.d(LoginActivity.uid, uafid);
                        addFriend(addfriend_url, LoginActivity.uid, uafid, uafn);

                    }
                }).start();
        }

    }

    private void addFriend(String url, String userId, String friendId, String friendName) {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("friendName", friendName);
        formBuilder.add("friendId", friendId);
        formBuilder.add("userId", userId);
        formBuilder.add("userName", LoginActivity.un);
        Request request = new Request.Builder().url(url).post(formBuilder.build()).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("error", "server error");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String res = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (res.equals("no this friend")) {
                            Toast toast = Toast.makeText(AddFriendsActivity.this, "no this user", Toast.LENGTH_SHORT);
                            toast.show();
                            Log.d("error", "No this user");
                        } else if (res.equals("friend exist")) {
                            Toast toast = Toast.makeText(AddFriendsActivity.this, "friend exist", Toast.LENGTH_SHORT);
                            toast.show();
                            Log.d("error", "Friend exist");
                        } else {
                            Log.d("ok", res);
                            Toast toast = Toast.makeText(AddFriendsActivity.this, "add friend successfully", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                    }
                });
            }
        });

    }
}
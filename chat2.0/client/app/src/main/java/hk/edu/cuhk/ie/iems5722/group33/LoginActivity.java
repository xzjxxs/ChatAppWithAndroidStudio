package hk.edu.cuhk.ie.iems5722.group33;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText userid;
    private EditText password;
    private EditText username;
//    public static String pid;

    private Button login, register;
    public static String uid;
    String pwd;
    public static String un;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userid = findViewById(R.id.userid);
        password = findViewById(R.id.password);
        username = findViewById(R.id.username);
        this.setTitle("Login");
    }

    public void fun(View v) {
        uid = userid.getText().toString();
        pwd = password.getText().toString();
        un = username.getText().toString();

        if (uid.equals("") || pwd.equals("")) {
            Toast toast = Toast.makeText(LoginActivity.this, "Input can not be empty!", Toast.LENGTH_SHORT);
            toast.show();
            Log.d("error", "id and password can not be empty");
            return;
        }
        switch (v.getId()) {
            case R.id.login:
                String login_url = "http://18.116.63.13/api/a4/login";
                getCheckFromServer(login_url, uid, pwd, un);
                break;
            case R.id.register:
                String register_url = "http://18.116.63.13/api/a4/register";
                registeNameWordToServer(register_url, uid, pwd, un);
                break;
        }
    }

    private void getCheckFromServer(String url, String userId, String passWord, String userName) {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username", userName);
        formBuilder.add("password", passWord);
        formBuilder.add("userId", userId);
        Request request = new Request.Builder().url(url).post(formBuilder.build()).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("error", "network error");
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String res = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (res.equals("0")) {
                            Toast toast = Toast.makeText(LoginActivity.this, "No such account, please register first", Toast.LENGTH_SHORT);
                            toast.show();
                            Log.d("error", "No such account, please register first");
                        } else if (res.equals("1")) {
                            Toast toast = Toast.makeText(LoginActivity.this, "Wrong userId/password", Toast.LENGTH_SHORT);
                            toast.show();
                            Log.d("error", "The password is incorrect ");
                        } else {
                            Toast toast = Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT);
                            toast.show();
                            Log.d("error", res);
                            sharedPreferences = getSharedPreferences("UserIDAndPassword", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", userName);
                            editor.apply();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("userid", userId);
                            startActivity(intent);
                        }

                    }
                });
            }
        });

    }

    private void registeNameWordToServer(String url, String userId, String passWord, String userName) {
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username", userName);
        formBuilder.add("password", passWord);
        formBuilder.add("userId", userId);
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
                        if (res.equals("0")) {
                            Toast toast = Toast.makeText(LoginActivity.this, "Already registered", Toast.LENGTH_SHORT);
                            toast.show();
                            Log.d("error", "this username has been registered");
                        } else {
                            Log.d("error", res);
                            Toast toast = Toast.makeText(LoginActivity.this, "Register successfully!", Toast.LENGTH_SHORT);
                            toast.show();
                            Log.d("error", "Register successfully");
                            sharedPreferences = getSharedPreferences("UserIDAndPassword", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", userName);
                            editor.apply();
                        }

                    }
                });
            }
        });

    }
}


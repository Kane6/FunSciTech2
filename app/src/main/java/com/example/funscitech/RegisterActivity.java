package com.example.funscitech;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Callback;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int RESULT_CODE_REGISTER = 0;
    private Button btnRegister;
    private EditText etAccount,etPass,etPassConfirm;
//    private CheckBox cbAgree;

    Tools tools = new Tools();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle("注册");

        etAccount = findViewById(R.id.et_account);
        etPass = findViewById(R.id.et_password);
        etPassConfirm = findViewById(R.id.et_password_confirm);
//        cbAgree = findViewById(R.id.cb_agree);
        btnRegister = findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        String name = etAccount.getText().toString();
        String pass = etPass.getText().toString();
        String passConfirm = etPassConfirm.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(RegisterActivity.this, "用户名不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(RegisterActivity.this, "密码不能为空", Toast.LENGTH_LONG).show();
            return;
        }

        if (!TextUtils.equals(pass,passConfirm)) {
            Toast.makeText(RegisterActivity.this, "密码不一致", Toast.LENGTH_LONG).show();
            return;
        }

//        if (!cbAgree.isChecked()) {
//            Toast.makeText(RegisterActivity.this, "请同意用户协议", Toast.LENGTH_LONG).show();
//            return;
//        }

//        String url = "https://8.130.182.76/register";
//        String url = "http://8.130.182.76:60/register";
//        String url = "http://183.11.71.83/register";
        String url = "http://m.funsci-tech.com/register";
        registToSever(url, name, pass);

//        Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_LONG).show();
        tools.showToastInThread(RegisterActivity.this, "注册成功！");
        this.finish();
    }

    private void registToSever(String url, final String name, String pass)
    {
        Log.i("RegisterActivity", "qdq registtoserver begin ");
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("username", name);
        formBuilder.add("password", pass);
        Log.i("RegisterActivity", "qdq registtoserver name pass "+name+" "+pass);
        Request request = null;
        try {
             request = new Request.Builder().url(url).post(formBuilder.build()).build();
        } catch(Exception e) {
            e.printStackTrace();
        }
        Log.i("RegisterActivity", "qdq registtoserver name pass "+request);
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e)
            {
                Log.i("RegisterActivity", "qdq onResponse0");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tools.showToastInThread(RegisterActivity.this, "服务器错误");
                    }
                });
//                Log.i("RegisterActivity", e);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("RegisterActivity", "qdq onResponse");
                try {
                    Log.i("RegisterActivity", response.body().string());
                    JSONObject jsonobj = new JSONObject(response.body().string());
                    int rescode = jsonobj.getInt("rescode");
                    String resmsg = jsonobj.getString("message");
                    Log.i("RegisterActivity", "qdq res: "+resmsg);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (rescode == 204) {
                                tools.showToastInThread(RegisterActivity.this, "该用户名已被注册");
                            } else if (rescode == 201) {
                                tools.showToastInThread(RegisterActivity.this, resmsg);
                                // 存储注册的用户名 密码
                                SharedPreferences spf = getSharedPreferences("spfRecord", MODE_PRIVATE);
                                SharedPreferences.Editor edit = spf.edit();
                                edit.putString("account", name);
                                edit.putString("password", pass);
                                edit.apply();

                                // 数据回传
                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putString("account",name);
                                bundle.putString("password",pass);
                                intent.putExtras(bundle);
                                setResult(RESULT_CODE_REGISTER,intent);
                            }
                        }
                    });
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}


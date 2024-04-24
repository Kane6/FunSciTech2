package com.example.funscitech;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LoginSuccess extends AppCompatActivity implements View.OnClickListener {

    private Button cartoonTransBtn;
//    private Button yoloDetect;
    private Button exitLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_success);
        cartoonTransBtn = findViewById(R.id.cartoon_transfer);
        cartoonTransBtn.setOnClickListener(this);
        exitLogin = findViewById(R.id.exit_login);
        exitLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cartoon_transfer:
                cartoonTransfer();
                break;
            case R.id.yolo7_detect:
                break;
            case R.id.exit_login:
                logout();
        }
    }

    private void cartoonTransfer()
    {
        Intent intent = new Intent(LoginSuccess.this, CartoonTransfer.class);
        startActivity(intent);
//        LoginSuccess.this.finish();
    }

    public void logout() {
        SharedPreferences spf = getSharedPreferences("spfRecord", MODE_PRIVATE);
        SharedPreferences.Editor edit = spf.edit();
        edit.putBoolean("isLogin", false);
        edit.apply();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }
}
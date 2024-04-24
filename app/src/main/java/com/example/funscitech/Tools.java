package com.example.funscitech;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Tools extends AppCompatActivity {
    protected Toast toast;

    protected void showToastInThread(Context context, String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
}

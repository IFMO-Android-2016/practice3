package ru.ifmo.droid2016.vkdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.vk.sdk.VKAccessToken;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (VKAccessToken.tokenFromSharedPreferences(this, Constants.KEY_TOKEN) != null) {
            startVkDemo();
        }

        setContentView(R.layout.activity_main);
        findViewById(R.id.signin_vk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVkDemo();
            }
        });
    }

    private void startVkDemo() {
        finish();
        final Intent intent = new Intent(MainActivity.this, VkDemoActivity.class);
        startActivity(intent);
    }
}

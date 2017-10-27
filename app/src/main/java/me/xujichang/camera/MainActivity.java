package me.xujichang.camera;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import me.xujichang.xcamera.cameraV1.XCameraV1Activity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startPreview(View view) {
        Intent lIntent = new Intent(this, XCameraV1Activity.class);
        startActivity(lIntent);
    }
}

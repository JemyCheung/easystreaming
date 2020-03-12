package com.ijemy.easystreaming;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ijemy.easystreaming.util.PermissionChecker;
import com.ijemy.easystreaming.util.Util;

import androidx.annotation.Nullable;

public class MainActivity extends Activity {

    private PermissionChecker mPermissionChecker = new PermissionChecker(this);
    private Button mStart ;
    private EditText mUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUrl = findViewById(R.id.url);
        mStart = findViewById(R.id.start);

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,StreamingByCameraActivity.class);
                String url = mUrl.getText().toString();
                intent.putExtra("url",url);
                startActivity(intent);
            }
        });

        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || mPermissionChecker.checkPermission();
        if (!isPermissionOK) {
            Util.showToast(this, "Some permissions is not approved !!!");
            return;
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mPermissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

package com.supinfo.supsms.activity;

import android.os.Bundle;
import android.view.Menu;

import com.supinfo.supsms.R;


public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        checkUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}

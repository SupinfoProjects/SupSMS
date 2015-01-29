package com.supinfo.supsms.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;

import com.supinfo.supsms.tools.Properties;

public abstract class BaseActivity extends ActionBarActivity {
    protected void checkUser() {
        if (Properties.getInstance().getUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

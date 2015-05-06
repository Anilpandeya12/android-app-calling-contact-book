package com.sinch.apptoappcontactbook;

import android.app.Application;

import com.parse.Parse;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        Parse.initialize(this, "YOUR_APPLICATION_ID", "YOUR_CLIENT_KEY");
    }
}

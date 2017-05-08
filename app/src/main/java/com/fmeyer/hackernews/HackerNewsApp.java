package com.fmeyer.hackernews;

import com.google.firebase.FirebaseApp;
import com.orm.SugarApp;

public class HackerNewsApp extends SugarApp {

    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(getApplicationContext());
    }
}

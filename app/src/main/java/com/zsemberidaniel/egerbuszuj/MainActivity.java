package com.zsemberidaniel.egerbuszuj;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zsemberidaniel.egerbuszuj.realm.FileToRealm;

import net.danlew.android.joda.JodaTimeAndroid;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JodaTimeAndroid.init(this);
        Realm.init(this);
        FileToRealm.init(this);
    }
}

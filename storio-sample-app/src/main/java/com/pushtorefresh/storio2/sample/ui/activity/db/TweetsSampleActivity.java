package com.pushtorefresh.storio2.sample.ui.activity.db;

import android.os.Bundle;

import com.pushtorefresh.storio2.sample.R;
import com.pushtorefresh.storio2.sample.ui.activity.BaseActivity;

public class TweetsSampleActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweets_sample);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}

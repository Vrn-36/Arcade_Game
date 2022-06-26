package com.example.arcadegame;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class Activity_Animation extends Activity {
    Activity_Game_Layout game_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game_layout = new Activity_Game_Layout(this);
        setContentView(game_layout);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        game_layout.userTouched();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        game_layout.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        game_layout.resume();
    }
}

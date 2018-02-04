package com.xposed.hook;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * Created by lin on 2018/2/4.
 */

public class LuckMoneySetting extends Activity {

    private CheckBox cb;
    private CheckBox cb2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lucky_money_setting);
        setTitle("微信红包");
        cb = (CheckBox) findViewById(R.id.cb);
        cb2 = (CheckBox) findViewById(R.id.cb2);
        final SharedPreferences sp = getSharedPreferences("lucky_money", MODE_WORLD_READABLE);
        cb.setChecked(sp.getBoolean("quick_open", false));
        cb2.setChecked(sp.getBoolean("auto_receive", false));
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean("quick_open", isChecked).commit();
            }
        });
        cb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean("auto_receive", isChecked).commit();
            }
        });
    }
}

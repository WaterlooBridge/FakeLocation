package com.xposed.hook;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * Created by lin on 2018/2/4.
 */

public class LuckMoneySetting extends AppCompatActivity {

    private CompoundButton cb;
    private CompoundButton cb2;
    private CompoundButton cb3;
    private CompoundButton cb4;
    private EditText et_lucky_money_delay;

    private SharedPreferences sp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lucky_money_setting);
        setTitle("微信红包");
        cb = (CompoundButton) findViewById(R.id.cb);
        cb2 = (CompoundButton) findViewById(R.id.cb2);
        cb3 = (CompoundButton) findViewById(R.id.cb3);
        cb4 = (CompoundButton) findViewById(R.id.cb4);
        et_lucky_money_delay = findViewById(R.id.et_lucky_money_delay);
        sp = getSharedPreferences("lucky_money", MODE_WORLD_READABLE);
        cb.setChecked(sp.getBoolean("quick_open", true));
        cb2.setChecked(sp.getBoolean("auto_receive", true));
        cb3.setChecked(sp.getBoolean("recalled", true));
        cb4.setChecked(sp.getBoolean("3_days_Moments", false));
        et_lucky_money_delay.setText(String.valueOf(sp.getInt("lucky_money_delay", 0)));
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
        cb3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean("recalled", isChecked).commit();
            }
        });
        cb4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sp.edit().putBoolean("3_days_Moments", isChecked).commit();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        try {
            int delay = Integer.parseInt(et_lucky_money_delay.getText().toString());
            sp.edit().putInt("lucky_money_delay", delay).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

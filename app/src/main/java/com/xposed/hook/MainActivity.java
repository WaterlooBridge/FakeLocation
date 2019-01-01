package com.xposed.hook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sp;

    private EditText etLatitude;
    private EditText etLongitude;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etLatitude = (EditText) findViewById(R.id.et_latitude);
        etLongitude = (EditText) findViewById(R.id.et_longitude);
        lv = (ListView) findViewById(R.id.lv);
        sp = getSharedPreferences("location", MODE_WORLD_READABLE);
        etLatitude.setText(sp.getString("latitude", "34.752600"));
        etLongitude.setText(sp.getString("longitude", "113.662000"));
        findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp.edit().putString("latitude", etLatitude.getText().toString())
                        .putString("longitude", etLongitude.getText().toString()).commit();
            }
        });
        lv.setAdapter(new MyAdapter(sp));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox cb = ViewHolder.get(view, R.id.cb);
                boolean flag = !cb.isChecked();
                sp.edit().putBoolean(parent.getAdapter().getItem(position).toString(), flag).commit();
                cb.setChecked(flag);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_dingding) {
            startActivity(new Intent(this, RimetActivity.class));
            return true;
        } else if (id == R.id.item_luck_money) {
            startActivity(new Intent(this, LuckMoneySetting.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private static class MyAdapter extends BaseAdapter {

        private List<String> list = PkgConfig.packages;
        private SharedPreferences sp;

        public MyAdapter(SharedPreferences preferences) {
            sp = preferences;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public String getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_package, parent, false);
            TextView tv = ViewHolder.get(convertView, R.id.tv);
            CheckBox cb = ViewHolder.get(convertView, R.id.cb);
            String pkg = list.get(position);
            tv.setText(pkg);
            cb.setChecked(sp.getBoolean(pkg, false));
            return convertView;
        }
    }
}

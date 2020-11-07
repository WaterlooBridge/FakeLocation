package com.xposed.hook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.xposed.hook.entity.AppInfo;
import com.xposed.hook.utils.AppUtil;
import com.xposed.hook.utils.ViewHolder;

import java.io.Serializable;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.lv);
        lv.setAdapter(new MyAdapter(getApplicationContext()));
        lv.setOnItemClickListener((parent, view, position, id) -> {
            startActivity(new Intent(this, RimetActivity.class)
                    .putExtra("appInfo", (Serializable) lv.getAdapter().getItem(position)));
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
        if (id == R.id.item_luck_money) {
            startActivity(new Intent(this, LuckMoneySetting.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private static class MyAdapter extends BaseAdapter {

        private List<AppInfo> list;

        public MyAdapter(Context context) {
            list = AppUtil.getAppList(context);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public AppInfo getItem(int position) {
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
            ImageView iv_icon = ViewHolder.get(convertView, R.id.iv_icon);
            TextView tv_title = ViewHolder.get(convertView, R.id.tv_title);
            TextView tv_package = ViewHolder.get(convertView, R.id.tv_package);
            AppInfo pkg = list.get(position);
            iv_icon.setImageDrawable(pkg.icon);
            tv_title.setText(pkg.title);
            tv_package.setText(pkg.packageName);
            return convertView;
        }
    }
}

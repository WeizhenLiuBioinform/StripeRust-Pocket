package org.pytorch.imagesegmentation;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.PicBaseAdapter;
import database.PicDBHelper;
import enity.Pic;

public class MainActivity_table extends AppCompatActivity {


    public PicDBHelper mHelper;
    private List<Pic> list;
    String date_sql;
    String name_sql;
    ListView table;
    public Button btn;
    String name_old;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {    //actionbar上按钮
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {  //actionbar上按钮作用
        switch (item.getItemId()) {
            case R.id.menu_layout:
                Intent intent_1 = new Intent(this,MainActivity_table_2.class);
                startActivity(intent_1);
                break;
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCustomActionBar() {
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        View mActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar_layout_table, null);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(mActionBarView, lp);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setTitle("返回");
        int color = Color.parseColor("#6B8E23");
        ColorDrawable drawable = new ColorDrawable(color);
        actionBar.setBackgroundDrawable(drawable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setCustomActionBar();
        setContentView(R.layout.table_item);
        btn = findViewById(R.id.delete);
        table = findViewById(R.id.table);
        mHelper = PicDBHelper.getInstance(this);
        mHelper.openWriteLink();
        mHelper.openReadLink();
        list = mHelper.queryAll();
        PicBaseAdapter adapter = new PicBaseAdapter(this, list);
        table.setAdapter(adapter);
        //        mImageView.setImageURI(Uri.fromFile(new File(bitmapstring)));
        table.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                customDialog(position);
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog_1();
            }
        });

    }

    public void customDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity_table.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(MainActivity_table.this, R.layout.table_save, null);
        dialog.setView(dialogView);
        dialog.show();

        final EditText et_name = dialogView.findViewById(R.id.et_name);
        final EditText et_pwd = dialogView.findViewById(R.id.et_date);
        final Button btn_delete_single = dialogView.findViewById(R.id.btn_delete_single);
        final Button btn_save = dialogView.findViewById(R.id.btn_save);
        final Button btn_cancel = dialogView.findViewById(R.id.btn_cancel);
        ImageView image = dialogView.findViewById(R.id.image_save);
        String img_path = list.get(position).path_seg;
        image.setImageURI(Uri.fromFile(new File(img_path)));

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_sql = et_name.getText().toString();
                date_sql = et_pwd.getText().toString();
                if (TextUtils.isEmpty(name_sql) || TextUtils.isEmpty(date_sql)) {
                    Toast.makeText(MainActivity_table.this, "名字或日期不能为空!", Toast.LENGTH_SHORT).show();
                    return;
                }
                name_old = list.get(position).name;
                list.get(position).name = name_sql;
                list.get(position).date = date_sql;
                mHelper.update(name_old, name_sql, date_sql);
                Toast.makeText(MainActivity_table.this, "名字：" + name_sql + "\n" + "日期：" + date_sql, Toast.LENGTH_SHORT).show();
                PicBaseAdapter adapter = new PicBaseAdapter(MainActivity_table.this, list);
                table.setAdapter(adapter);
                dialog.dismiss();
            }
        });

        btn_delete_single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = list.get(position).name;
                customDialog_2(name);
                dialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void customDialog_1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity_table.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(MainActivity_table.this, R.layout.table_confirm, null);
        dialog.setView(dialogView);
        dialog.show();

        final Button btn_delete = dialogView.findViewById(R.id.btn_delete);
        final Button btn_cancel = dialogView.findViewById(R.id.btn_cancel_1);

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHelper.delete_all();
                list = mHelper.queryAll();
                PicBaseAdapter adapter = new PicBaseAdapter(MainActivity_table.this, list);
                table.setAdapter(adapter);
                dialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void customDialog_2(final String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity_table.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(MainActivity_table.this, R.layout.table_confirm, null);
        dialog.setView(dialogView);
        dialog.show();

        final Button btn_delete = dialogView.findViewById(R.id.btn_delete);
        final Button btn_cancel = dialogView.findViewById(R.id.btn_cancel_1);

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHelper.delete(name);
                list = mHelper.queryAll();
                PicBaseAdapter adapter = new PicBaseAdapter(MainActivity_table.this, list);
                table.setAdapter(adapter);
                dialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }



}
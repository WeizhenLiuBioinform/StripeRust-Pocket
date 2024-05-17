package org.pytorch.imagesegmentation;

import static android.provider.CalendarContract.CalendarCache.URI;

import androidx.appcompat.app.ActionBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.data.style.FontStyle;
import com.bin.david.form.utils.DensityUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import adapter.UserInfo;
import database.PicDBHelper;
import enity.Pic;
import util.Excel;

public class MainActivity_table_2 extends AppCompatActivity {


    public PicDBHelper mHelper;
    private List<Pic> list;
    public String path;
    public File excel;
    Uri URI = null;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {    //actionbar上按钮
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_layout_2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {  //actionbar上按钮作用
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity_table.class);
                startActivity(intent);
                break;
            case R.id.menu_layout_1:
                requestPermission();
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/bhne/";
                File files = new File(filePath);
                if (!files.exists()) {
                    files.mkdirs();
                }
                String[] title = {"Name", "Severity", "Date"};

                String excelFileName = "/" + "excel" + ".xls";
                String resultPath = files.getAbsolutePath() + excelFileName;
                path = resultPath;
                Log.e("rultPath", resultPath);
                Excel.initExcel(resultPath, title);
                File moudleFile = Excel.writeObjListToExcel(list, resultPath);
                excel = moudleFile;
                if (moudleFile != null) {
                    Toast.makeText(this, "已经保存到本地" + resultPath, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_layout_2:
                if(path==null){
                    Toast.makeText(this, " Please click on the save button first ", Toast.LENGTH_SHORT).show();
                }
                else {
                    File file = new File(path);
                    URI = FileProvider.getUriForFile(this, "org.pytorch.imagesegmentation.fileprovider", file);
                    try {
                        final Intent emailIntent = new Intent(
                                android.content.Intent.ACTION_SEND);
                        emailIntent.setType("plain/text");
                        if (URI != null) {
                            emailIntent.putExtra(Intent.EXTRA_STREAM, URI);
                        }
                        this.startActivity(Intent.createChooser(emailIntent,
                                "Sending email..."));
                    } catch (Throwable t) {
                        Toast.makeText(this,
                                "Request failed try again: " + t.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                }

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCustomActionBar() {
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        View mActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar_layout_table_2, null);
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

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
                return;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setCustomActionBar();
        setContentView(R.layout.table_item_2);
        SmartTable table = findViewById(R.id.table);
        mHelper = PicDBHelper.getInstance(this);
        mHelper.openWriteLink();
        mHelper.openReadLink();
        list = mHelper.queryAll();
        List<UserInfo> list_2 = new ArrayList<>();
        for (Pic pic : list) {
            list_2.add(new UserInfo(pic.name, pic.severity, pic.date));
        }
        table.setZoom(true);
        FontStyle.setDefaultTextSize(DensityUtils.sp2px(this, 24)); //设置全局字体大小
        table.setData(list_2);
        //        mImageView.setImageURI(Uri.fromFile(new File(bitmapstring)));

    }


}
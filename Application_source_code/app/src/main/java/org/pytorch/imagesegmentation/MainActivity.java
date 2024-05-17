package org.pytorch.imagesegmentation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.pytorch.Device;
import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import database.PicDBHelper;
import enity.Pic;
import util.Utils;


public class MainActivity extends AppCompatActivity implements Runnable {
    private ImageView mImageView;
    private Button mButtonSegment;
    private ProgressBar mProgressBar;
    private static final int COMPLETED = 0;
    private Bitmap mBitmap = null;
    private Module mModule = null;
    private Module mModule_2 = null;
    public static final int TAKE_PHOTO=1;
    private TextView mBtn;
    float red = 0;
    float green = 0;
    float percent = 0;
    private String path = "/storage/emulated/0/Pictures/";
    public PicDBHelper mHelper;
    String path_full;
    String date_sql;
    String name_sql;
    Uri imageUri = null;

    public long runtime;

    // see http://host.robots.ox.ac.uk:8080/pascal/VOC/voc2007/segexamples/index.html for the list of classes with indexes
    private static final int CLASSNUM = 3;
    private static final int CLASSNUM_2 = 2;
    private static final int bg = 0;
    private static final int healthy = 1;
    private static final int strip = 2;
    private static final int leaf = 1;

    private TextView per;

    private Handler handler = new Handler() {   //生成percent，写这里是防止线程问题
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
                DecimalFormat df = new DecimalFormat("#.0000");
                NumberFormat nt = NumberFormat.getPercentInstance();
                nt.setMinimumFractionDigits(2);
                per.setText("severity:" + nt.format(percent));
            }
        }
    };


    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    public void initPopWindow() {
        View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.measure_layout, null);
        final PopupWindow mPopWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //显示PopupWindow
        View rootview = LayoutInflater.from(MainActivity.this).inflate(R.layout.measure_layout, null);
        mPopWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);
        View view = mPopWindow.getContentView();
        Button tButton = view.findViewById(R.id.id_btn_take_photo);
        Button gButton = view.findViewById(R.id.id_btn_select);
        Button cButton = view.findViewById(R.id.id_btn_cancelo);
        tButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File outputImage=new File(getExternalCacheDir(),"output_image.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageUri= FileProvider.getUriForFile(MainActivity.this,
                        "org.pytorch.imagesegmentation.fileprovider",outputImage);
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);

                mPopWindow.dismiss();
            }
        });
        gButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), 0);
                mPopWindow.dismiss();
            }
        });
        cButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopWindow.dismiss();
            }
        });
    }


    public void saveBitmap(Bitmap bitmap, String path) {
        String savePath;
        File filePic;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            savePath = path + System.currentTimeMillis() + ".jpg";
            path_full = savePath;
        } else {
            Log.e("tag", "saveBitmap failure : sdcard not mounted");
            return;
        }
        try {
            filePic = new File(savePath);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            String str = Long.toString(runtime);
            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();

            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e("tag", "saveBitmap: " + e.getMessage());
            return;
        }
        Log.i("tag", "saveBitmap success: " + filePic.getAbsolutePath());
        return;
    }


    public void customDialog(Bitmap bm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(MainActivity.this, R.layout.table_save_1, null);
        dialog.setView(dialogView);
        dialog.show();

        final EditText et_name = dialogView.findViewById(R.id.et_name);
        final EditText et_pwd = dialogView.findViewById(R.id.et_date);

        final Button btn_save = dialogView.findViewById(R.id.btn_save);
        final Button btn_cancel = dialogView.findViewById(R.id.btn_cancel);
        ImageView image = dialogView.findViewById(R.id.image_save);
        image.setImageBitmap(bm);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_sql = et_name.getText().toString();
                date_sql = et_pwd.getText().toString();
                if (TextUtils.isEmpty(name_sql) || TextUtils.isEmpty(date_sql)) {
                    Toast.makeText(MainActivity.this, "ID或日期不能为空!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Pic pic = null;
                String path_leaf = "123";
                String percent_in = String.valueOf(percent);
                pic = new Pic(path_leaf, path_full, Float.parseFloat(percent_in), name_sql, date_sql);
                mHelper.insert(pic);
                Toast.makeText(MainActivity.this, "ID：" + name_sql + "\n" + "日期：" + date_sql, Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(this, MainActivity_table.class);
                intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setCustomActionBar() {
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        View mActionBarView = LayoutInflater.from(this).inflate(R.layout.actionbar_layout, null);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(mActionBarView, lp);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        int color = Color.parseColor("#6B8E23");
        ColorDrawable drawable = new ColorDrawable(color);
        actionBar.setBackgroundDrawable(drawable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        setCustomActionBar();
        setContentView(R.layout.activity_main); //显示布局(activity_main)
        mHelper = PicDBHelper.getInstance(this);
        mHelper.openWriteLink();
        mHelper.openReadLink();
        mBtn = findViewById(R.id.restartButton);
        mImageView = findViewById(R.id.imageView);
        per = (TextView) findViewById(R.id.percent);

        mBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initPopWindow();
//                startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), 0);
                per.setText(" ");
                mButtonSegment.setEnabled(true);
            }
        });


        mImageView = findViewById(R.id.imageView); //找到activity_main中的imageView
        mImageView.setImageBitmap(mBitmap);


        mButtonSegment = findViewById(R.id.segmentButton);
        mButtonSegment.setEnabled(false);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mButtonSegment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mButtonSegment.setEnabled(false);
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                mButtonSegment.setText(getString(R.string.run_model));

                Thread thread = new Thread(MainActivity.this);
                thread.start();
            }
        });

        try {
            mModule = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), "2stage_optimized.ptl"));
            mModule_2 = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), "1stage_optimized_res.ptl")); //deeplabv3+_seg2_scripted_optimized.ptl

        } catch (IOException e) {
            Log.e("ImageSegmentation", "Error reading assets", e);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHelper.closelink();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ContentResolver contentResolver = getContentResolver();

        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        mBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        mImageView.setImageBitmap(mBitmap);
                        mBitmap = Utils.small(mBitmap);
                        //将图片解析成Bitmap对象，并把它显现出来
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 0:
                try {
                    if (data == null) {
                        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.white);
                        mImageView.setImageBitmap(mBitmap);
                    } else {
                        mBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(data.getData()));
                        mBitmap = Utils.small(mBitmap);
                        Log.i("TAG", "从相册回传bitmap：" + mBitmap);
                        mImageView.setImageBitmap(mBitmap);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void run() {
        final Tensor inputTensor_2 = TensorImageUtils.bitmapToFloat32Tensor(mBitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        final float[] inputs_2 = inputTensor_2.getDataAsFloatArray();

        final long startTime_2 = SystemClock.elapsedRealtime();
//        Map<String, IValue> outTensors = mModule.forward(IValue.from(inputTensor)).toDictStringKey();
        final long inferenceTime_2 = SystemClock.elapsedRealtime() - startTime_2;
        Log.d("ImageSegmentation", "inference time (ms): " + inferenceTime_2);

//        final Tensor outputTensor = outTensors.get("out").toTensor();
        long startTime_test = System.currentTimeMillis();
        final Tensor outputTensor_2 = mModule_2.forward(IValue.from(inputTensor_2)).toTensor();

        final float[] scores_2 = outputTensor_2.getDataAsFloatArray();
        int width_2 = mBitmap.getWidth();
        int height_2 = mBitmap.getHeight();
        int[] intValues_2 = new int[width_2 * height_2];
        for (int j = 0; j < height_2; j++) {
            for (int k = 0; k < width_2; k++) {
                int maxi_2 = 0, maxj_2 = 0, maxk_2 = 0;
                double maxnum_2 = -Double.MAX_VALUE;
                for (int i = 0; i < CLASSNUM_2; i++) {
                    float score_2 = scores_2[i * (width_2 * height_2) + j * width_2 + k];
                    if (score_2 > maxnum_2) {
                        maxnum_2 = score_2;
                        maxi_2 = i;
                        maxj_2 = j;
                        maxk_2 = k;
                    }
                }
                if (maxi_2 == healthy) {
                    intValues_2[maxj_2 * width_2 + maxk_2] = 0xFF00FF00;
                } else if (maxi_2 == bg) {
                    intValues_2[maxj_2 * width_2 + maxk_2] = 0xFF000000;
                }
            }
        }

        Bitmap bmpSegmentation_2 = Bitmap.createScaledBitmap(mBitmap, width_2, height_2, true);
        Bitmap outputBitmap_2 = bmpSegmentation_2.copy(bmpSegmentation_2.getConfig(), true);
        Bitmap bmpSegmentation_31 = Bitmap.createScaledBitmap(mBitmap, width_2, height_2, true);
        Bitmap bmpSegmentation_3 = bmpSegmentation_31.copy(bmpSegmentation_31.getConfig(), true);
        outputBitmap_2.setPixels(intValues_2, 0, outputBitmap_2.getWidth(), 0, 0, outputBitmap_2.getWidth(), outputBitmap_2.getHeight());
        final Bitmap transferredBitmap_2 = Bitmap.createScaledBitmap(outputBitmap_2, mBitmap.getWidth(), mBitmap.getHeight(), true);
        for (int i = 0; i < mBitmap.getWidth(); i++) {
            for (int j = 0; j < mBitmap.getHeight(); j++) {
                if (transferredBitmap_2.getPixel(i, j) == 0xFF000000) {
                    bmpSegmentation_3.setPixel(i, j, 0xFF000000);
                }
            }
        }

        final Bitmap transferredBitmap_3 = Bitmap.createScaledBitmap(bmpSegmentation_3, mBitmap.getWidth(), mBitmap.getHeight(), true);

        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(transferredBitmap_3,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        final float[] inputs = inputTensor.getDataAsFloatArray();

        final long startTime = SystemClock.elapsedRealtime();
//        Map<String, IValue> outTensors = mModule.forward(IValue.from(inputTensor)).toDictStringKey();
        final long inferenceTime = SystemClock.elapsedRealtime() - startTime;
        Log.d("ImageSegmentation", "inference time (ms): " + inferenceTime);

//        final Tensor outputTensor = outTensors.get("out").toTensor();
        final Tensor outputTensor = mModule.forward(IValue.from(inputTensor)).toTensor();
        final float[] scores = outputTensor.getDataAsFloatArray();
        int width = transferredBitmap_2.getWidth();
        int height = transferredBitmap_2.getHeight();
        int[] intValues = new int[width * height];
        for (int j = 0; j < height; j++) {
            for (int k = 0; k < width; k++) {
                int maxi = 0, maxj = 0, maxk = 0;
                double maxnum = -Double.MAX_VALUE;
                for (int i = 0; i < CLASSNUM; i++) {
                    float score = scores[i * (width * height) + j * width + k];
                    if (score > maxnum) {
                        maxnum = score;
                        maxi = i;
                        maxj = j;
                        maxk = k;
                    }
                }
                if (maxi == healthy) {
                    intValues[maxj * width + maxk] = 0xFF008000;
                    green++;
                } else if (maxi == bg)
                    intValues[maxj * width + maxk] = 0xFF000000;
                else if (maxi == strip) {
                    intValues[maxj * width + maxk] = 0xFF800000;
                    red++;
                } else
                    intValues[maxj * width + maxk] = 0xFF000000;
            }
        }
        long endTime_test = System.currentTimeMillis();
        runtime = endTime_test - startTime_test;
        Bitmap bmpSegmentation = Bitmap.createScaledBitmap(transferredBitmap_3, width, height, true);
        Bitmap outputBitmap = bmpSegmentation.copy(bmpSegmentation.getConfig(), true);
        outputBitmap.setPixels(intValues, 0, outputBitmap.getWidth(), 0, 0, outputBitmap.getWidth(), outputBitmap.getHeight());
        final Bitmap transferredBitmap = Bitmap.createScaledBitmap(outputBitmap, transferredBitmap_3.getWidth(), transferredBitmap_3.getHeight(), true);
//        for (int i = 0; i < transferredBitmap.getWidth(); i++) {
//            for (int j = 0; j < transferredBitmap.getHeight(); j++) {
//                if (transferredBitmap.getPixel(i, j) == 0xFF800000) {
//                    red++;
//                }
//                else if (transferredBitmap.getPixel(i, j) == 0xFF008000){
//                    green++;
//                }
//            }
//        }
        float percent_1 = red / (red + green);
        percent = percent_1;
        percent_1 = 0 ;
        red = 0;
        green = 0;
        Message msg = new Message();  //传递完成信息，生成percen
        msg.what = COMPLETED;
        handler.sendMessage(msg);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImageView.setImageBitmap(transferredBitmap);
                saveBitmap(transferredBitmap, path);
                customDialog(transferredBitmap);
                mButtonSegment.setText(getString(R.string.segment));
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);

            }
        });
    }
}

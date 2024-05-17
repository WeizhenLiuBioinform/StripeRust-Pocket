package util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.graphics.BitmapCompat;

import org.pytorch.imagesegmentation.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {
    public static Bitmap small(Bitmap bitmap) {
        int i = BitmapCompat.getAllocationByteCount(bitmap);
        if (i > 12843623) {
            Matrix matrix = new Matrix();
            matrix.postScale(0.2f, 0.2f); //长和宽放大缩小的比例
            Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return resizeBmp;
        }
        else return bitmap;
    }
}

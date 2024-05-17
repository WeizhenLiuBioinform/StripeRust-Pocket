package adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.pytorch.imagesegmentation.R;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import database.PicDBHelper;
import enity.Pic;

public class PicBaseAdapter extends BaseAdapter {

    private Context mContext;
    private List<Pic> mPiclist;

    public PicBaseAdapter(Context mContext, List<Pic> mPiclist) {
        this.mContext = mContext;
        this.mPiclist = mPiclist;
    }

    @Override
    public int getCount() {
        return mPiclist.size();
    }

    @Override
    public Object getItem(int i) {
        return mPiclist.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View conview, ViewGroup viewGroup) {
//        错误代码LayoutInflater.from(mContext).inflate(R.layout.table_list, null);
        View view = LayoutInflater.from(mContext).inflate(R.layout.table_list, null);
        ImageView table_image = view.findViewById(R.id.table_image);
        TextView table_text_1 = view.findViewById(R.id.table_text_1);
        TextView table_text_2 = view.findViewById(R.id.table_text_2);
        TextView table_text_3 = view.findViewById(R.id.table_text_3);

        Pic pic = mPiclist.get(i);
        table_image.setImageURI(Uri.fromFile(new File(pic.path_seg)));
        table_text_1.setText("ID:" + pic.name);
        DecimalFormat decimalFormat = new DecimalFormat("0.00%");
        String text = decimalFormat.format(pic.severity);
//        String text = String.valueOf(pic.severity);
        table_text_2.setText("Severity:"+ text);
        table_text_3.setText("Date:" + pic.date);

        return view;
    }
}

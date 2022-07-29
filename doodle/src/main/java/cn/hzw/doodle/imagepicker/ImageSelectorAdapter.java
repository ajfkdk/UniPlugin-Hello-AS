package cn.hzw.doodle.imagepicker;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.chenzhouli.doodle.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;



public class ImageSelectorAdapter extends BaseAdapter {

    private Context mContext;
    private List<BeanImage> mList;
    LayoutInflater inflater;
    private DisplayImageOptions options;

    public ImageSelectorAdapter(Context context, List<BeanImage> list) {
        this.mList = list;
        this.mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.icon_default_image)
                .showImageOnLoading(R.drawable.icon_default_image)
                .showImageOnFail(R.drawable.icon_default_image)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
    }

    public void refreshPathList(List<BeanImage> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.doodle_imageselector_item, null);
            holder = new ViewHolder();
            holder.mImage = (ImageView) convertView.findViewById(R.id.doodle_image);
            holder.mImageSelected = (ImageView) convertView.findViewById(R.id.doodle_image_selected);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        BeanImage beanImage = mList.get(position);
        String filePath = beanImage.getFilePath();
        boolean isChooice = beanImage.isChhoice;
        holder.mImageSelected.setVisibility(isChooice ? View.VISIBLE : View.GONE);
        filePath = "file://" + filePath;
        Log.e("cdl", "======加载图片得路径===" + filePath);
        ImageSize imageSize = new ImageSize(100, 100);
        ImageViewAware imageViewAware = new ImageViewAware(holder.mImage);
        ImageLoader.getInstance()
                .displayImage(filePath, imageViewAware, options, imageSize, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
//                        holder.progressBar.setProgress(0);
                        holder.progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        holder.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        holder.progressBar.setVisibility(View.GONE);
                    }
                }, new ImageLoadingProgressListener() {
                    @Override
                    public void onProgressUpdate(String imageUri, View view, int current, int total) {
//                        holder.progressBar.setProgress(Math.round(100.0f * current / total));
                    }
                });
        return convertView;
    }

    private class ViewHolder {
        ImageView mImage;
        ImageView mImageSelected;
        ProgressBar progressBar;
    }
}
package ai.tomorrow.instagramclone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

import ai.tomorrow.instagramclone.R;

public class GridImageAdapter extends RecyclerView.Adapter<GridImageAdapter.ViewHolder> {
    private static final String TAG = "GridImageAdapter";

    public interface OnGridItemClickListener {
        void OnGridItemClick(int clickedItemIndex);
    }

    private Context mContext;
    private ArrayList<String> imgURLs;
    private String mAppend;
    private int layoutResource;
    private OnGridItemClickListener mOnGridItemClickListener;


    public GridImageAdapter(Context mContext, int layoutResource, String mAppend, ArrayList<String> imgURLs, OnGridItemClickListener listener) {
        this.mContext = mContext;
        this.imgURLs = imgURLs;
        this.mAppend = mAppend;
        this.layoutResource = layoutResource;
        this.mOnGridItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder.");

        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutResource, parent, shouldAttachToParentImmediately);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull GridImageAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder.");
        holder.bind(imgURLs.get(position), mAppend);
    }

    @Override
    public int getItemCount() {
        return imgURLs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        SquareImageView image;
        ProgressBar mProgressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.gridImageProgressbar);
            image = itemView.findViewById(R.id.gridImageView);
        }

        void bind(String imgURL, String append) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(append + imgURL, image, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    if (mProgressBar != null) {
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    if (mProgressBar != null) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    if (mProgressBar != null) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    if (mProgressBar != null) {
                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            });
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.d(TAG, "onClick: click grid view image index: " + getAdapterPosition());
            int clickedPosition = getAdapterPosition();
            mOnGridItemClickListener.OnGridItemClick(clickedPosition);
        }
    }
}

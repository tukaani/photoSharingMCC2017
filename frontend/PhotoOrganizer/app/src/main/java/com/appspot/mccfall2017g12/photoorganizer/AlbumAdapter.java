package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Locale;

public class AlbumAdapter extends LiveDataAdapter<Album.Extended, AlbumAdapter.ViewHolder> {

    private final View.OnClickListener onClickListener;

    public AlbumAdapter(LifecycleOwner owner, View.OnClickListener onClickListener) {
        super(owner);

        this.onClickListener = onClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_album_item, parent, false);
        if (onClickListener != null)
            view.setOnClickListener(onClickListener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Context context = holder.albumCoverImageView.getContext();

        Album.Extended albumExt = getItem(position);

        holder.nameTextView.setText(albumExt.album.name);
        holder.photoCountTextView.setText(
                String.format(Locale.getDefault(),"%1$d", albumExt.photoCount));

        holder.inCloudImageView.setImageResource(
                TextUtils.equals(albumExt.album.albumId, Album.PRIVATE_ALBUM_ID) ?
                R.drawable.ic_cloud_off_black_24dp :
                R.drawable.ic_cloud_queue_black_24dp);

        if (albumExt.file != null)
            Picasso.with(context)
                    .load(FileTools.get(albumExt.file))
                    .config(Bitmap.Config.RGB_565)
                    .into(holder.albumCoverImageView);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView albumCoverImageView;
        final TextView nameTextView;
        final TextView photoCountTextView;
        final ImageView inCloudImageView;

        public ViewHolder(View view) {
            super(view);

            this.albumCoverImageView = view.findViewById(R.id.albumCoverImageView);
            this.nameTextView = view.findViewById(R.id.nameTextView);
            this.photoCountTextView = view.findViewById(R.id.photoCountTextView);
            this.inCloudImageView = view.findViewById(R.id.inCloudImageView);
        }
    }
}

package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class PhotoAdapter extends HeaderAdapter<Photo.Extended, PhotoAdapter.HeaderViewHolder,
        PhotoAdapter.ItemViewHolder> {

    private final View.OnClickListener onClickListener;

    public PhotoAdapter(LifecycleOwner owner, View.OnClickListener onClickListener) {
        super(owner);

        this.onClickListener = onClickListener;
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_photo_item, parent, false);
        if (onClickListener != null)
            view.setOnClickListener(onClickListener);
        return new ItemViewHolder(view);
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_photo_header, parent, false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(ItemViewHolder holder, int position) {
        Photo photo = getItem(position).photo;

        Context context = holder.photoImageView.getContext();

        if (photo.file != null)
            Picasso.with(context)
                    .load(FileTools.get(photo.file))
                    .config(Bitmap.Config.RGB_565)
                    .into(holder.photoImageView);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder holder, int position) {
        Photo.Extended header = getItem(position);

        Context context = holder.headerTextView.getContext();

        String title;

        switch (header.itemType) {
            case Photo.Extended.TYPE_AUTHOR_HEADER:
                title = getAuthorDescription(context, header.photo.author);
                break;
            case Photo.Extended.TYPE_PEOPLE_HEADER:
                title = getPeopleDescription(context, header.photo.people);
                break;
            default:
                title = null;
                break;
        }

        holder.headerTextView.setText(title);
    }

    private String getAuthorDescription(Context context, String author) {
        if (author == null)
            return context.getString(R.string.unknown);
        return author;
    }

    private String getPeopleDescription(Context context, int people) {
        @StringRes
        int res;
        switch (people) {
            case Photo.PEOPLE_NA:
                res = R.string.uncategorized;
                break;
            case Photo.PEOPLE_NO:
                res = R.string.no_people;
                break;
            case Photo.PEOPLE_YES:
                res = R.string.people;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return context.getString(res);
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        final ImageView photoImageView;

        public ItemViewHolder(View view) {
            super(view);
            this.photoImageView = view.findViewById(R.id.photoImageView);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView headerTextView;

        public HeaderViewHolder(View view) {
            super(view);
            this.headerTextView = view.findViewById(R.id.headerTextView);
        }
    }
}

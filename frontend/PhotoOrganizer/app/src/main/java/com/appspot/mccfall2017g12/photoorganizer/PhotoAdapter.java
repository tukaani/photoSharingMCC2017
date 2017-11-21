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

public class PhotoAdapter extends HeaderAdapter<Photo.Extended, PhotoAdapter.ViewHolder,
        PhotoAdapter.ViewHolder.Header, PhotoAdapter.ViewHolder.Item> {

    private final View.OnClickListener onClickListener;

    public PhotoAdapter(LifecycleOwner owner, View.OnClickListener onClickListener) {
        super(owner);

        this.onClickListener = onClickListener;
    }

    @Override
    public ViewHolder.Item onCreateItemViewHolder(ViewGroup parent) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_photo_item, parent, false);
        if (onClickListener != null)
            view.setOnClickListener(onClickListener);
        return new ViewHolder.Item(view);
    }

    @Override
    public ViewHolder.Header onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_photo_header, parent, false);
        return new ViewHolder.Header(view);
    }

    @Override
    public void onBindItemViewHolder(ViewHolder.Item holder, int position) {
        Photo photo = getItem(position).photo;

        Context context = holder.photoImageView.getContext();

        Picasso.with(context)
                .load(photo.path)
                .config(Bitmap.Config.RGB_565)
                .into(holder.photoImageView);
    }

    @Override
    public void onBindHeaderViewHolder(ViewHolder.Header holder, int position) {
        Photo.Extended header = getItem(position);

        Context context = holder.headerTextView.getContext();

        String title;

        switch (header.itemType) {
            case Photo.Extended.TYPE_AUTHOR_HEADER:
                title = header.photo.author;
                break;
            case Photo.Extended.TYPE_PEOPLE_HEADER:
                title = getPeopleAppearanceDescription(context, header.photo.peopleAppearance);
                break;
            default:
                title = null;
                break;
        }

        holder.headerTextView.setText(title);
    }

    private String getPeopleAppearanceDescription(Context context, int peopleAppearance) {
        @StringRes
        int res;
        switch (peopleAppearance) {
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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View view) {
            super(view);
        }

        public static class Item extends ViewHolder {
            final ImageView photoImageView;

            public Item(View view) {
                super(view);
                this.photoImageView = view.findViewById(R.id.photoImageView);
            }
        }

        public static class Header extends ViewHolder {
            final TextView headerTextView;

            public Header(View view) {
                super(view);
                this.headerTextView = view.findViewById(R.id.headerTextView);
            }
        }
    }
}

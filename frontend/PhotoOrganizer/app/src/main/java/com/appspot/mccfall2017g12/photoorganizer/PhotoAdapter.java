package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class PhotoAdapter extends LiveDataAdapter<Photo, PhotoAdapter.ViewHolder> {

    public PhotoAdapter(LifecycleOwner owner, View.OnClickListener onClickListener) {
        super(owner, R.layout.layout_photo_item, onClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Context context = holder.photoImageView.getContext();

        Photo photo = getItem(position);

        holder.authorTextView.setText(context.getString(R.string.author, photo.author));

        switch (photo.peopleAppearance) {
            case Photo.PEOPLE_YES:
                holder.peopleImageView.setImageResource(R.drawable.ic_face_black_24dp);
                break;
            case Photo.PEOPLE_NO:
                holder.peopleImageView.setImageResource(R.drawable.ic_landscape_black_24dp);
                break;
            case Photo.PEOPLE_NA:
                holder.peopleImageView.setImageDrawable(null);
                break;
        }

        Picasso.with(context)
                .load(photo.path)
                .config(Bitmap.Config.RGB_565)
                .into(holder.photoImageView);
    }

    @Override
    protected ViewHolder createViewHolderInternal(View view) {
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView photoImageView;
        final TextView authorTextView;
        final ImageView peopleImageView;

        public ViewHolder(View view) {
            super(view);

            this.photoImageView = view.findViewById(R.id.photoImageView);
            this.authorTextView = view.findViewById(R.id.authorTextView);
            this.peopleImageView = view.findViewById(R.id.peopleImageView);
        }
    }
}

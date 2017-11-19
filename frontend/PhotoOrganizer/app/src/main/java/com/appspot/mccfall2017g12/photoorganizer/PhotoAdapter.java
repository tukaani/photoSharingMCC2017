package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Date;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private Photo[] photos = new Photo[0];

    public void setPhotos(final Photo[] photos) {
        Photo[] oldPhotos = this.photos;

        this.photos = photos;

        DiffUtil.calculateDiff(new PhotoDiffCallback(oldPhotos, photos))
                .dispatchUpdatesTo(this);

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_photo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Photo photo = this.photos[position];

        holder.authorTextView.setText("@" + photo.getAuthor());

        switch (photo.getPeopleAppearance()) {
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

        Context context = holder.photoImageView.getContext();
        Picasso.with(context)
                .load(photo.getPath())
                .into(holder.photoImageView);
    }

    @Override
    public int getItemCount() {
        return this.photos.length;
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

    private static class PhotoDiffCallback extends DiffUtil.Callback {

        private final Photo[] oldPhotos;
        private final Photo[] newPhotos;

        public PhotoDiffCallback(Photo[] oldPhotos, Photo[] newPhotos) {
            this.oldPhotos = oldPhotos;
            this.newPhotos = newPhotos;
        }

        @Override
        public int getOldListSize() {
            return oldPhotos.length;
        }

        @Override
        public int getNewListSize() {
            return newPhotos.length;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldPhotos[oldItemPosition].getKey().equals(newPhotos[newItemPosition].getKey());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return Photo.areContentsTheSame(oldPhotos[oldItemPosition], newPhotos[newItemPosition]);
        }
    }
}

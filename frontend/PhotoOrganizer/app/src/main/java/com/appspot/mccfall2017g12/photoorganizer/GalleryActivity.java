package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GalleryActivity extends AppCompatActivity
        implements PostExecutor<LiveData<Album.Extended[]>>, View.OnClickListener {

    private RecyclerView recyclerView;
    private AlbumAdapter adapter;
    private LiveData<Album.Extended[]> albums;

    private static final int COLUMN_COUNT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        this.recyclerView = findViewById(R.id.albums_recycler_view);
        this.recyclerView.setHasFixedSize(true);

        this.adapter = new AlbumAdapter(this, this);
        this.recyclerView.setAdapter(adapter);

        GridLayoutManager layoutManager = new GridLayoutManager(this, COLUMN_COUNT);
        this.recyclerView.setLayoutManager(layoutManager);

        GalleryDatabase.initialize(getApplicationContext());
        new GalleryDatabase.LoadAlbumsTask().with(this).execute();

        //TODO Remove
        {
            Album album1 = new Album();
            album1.albumKey = Album.PRIVATE_ALBUM_KEY;
            album1.name = "Private";

            Album album2 = new Album();
            album2.albumKey = "a1";
            album2.name = "Happy Hour";

            new GalleryDatabase.InsertAlbumTask().execute(album1, album2);
        }
    }

    @Override
    public void onPostExecute(final LiveData<Album.Extended[]> liveData) {
        this.adapter.setLiveData(liveData);
    }

    @Override
    public void onClick(View view) {
        int position = this.recyclerView.getChildAdapterPosition(view);
        String albumKey = this.adapter.getItem(position).album.albumKey;
        Intent intent = new Intent(this, AlbumActivity.class);
        intent.putExtra(AlbumActivity.EXTRA_ALBUM, albumKey);
        startActivity(intent);
    }
}

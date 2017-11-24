package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AlbumAdapter adapter;

    private static final int COLUMN_COUNT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        this.recyclerView = findViewById(R.id.albums_recycler_view);
        this.recyclerView.setHasFixedSize(true);

        this.adapter = new AlbumAdapter(this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = GalleryActivity.this.recyclerView.getChildAdapterPosition(view);
                String albumKey = GalleryActivity.this.adapter.getItem(position).album.albumId;

                Intent intent = new Intent(GalleryActivity.this, AlbumActivity.class);
                intent.putExtra(AlbumActivity.EXTRA_ALBUM, albumKey);
                startActivity(intent);
            }
        });
        this.recyclerView.setAdapter(adapter);

        GridLayoutManager layoutManager = new GridLayoutManager(this, COLUMN_COUNT);
        this.recyclerView.setLayoutManager(layoutManager);

        GalleryDatabase.initialize(getApplicationContext());
        new GalleryDatabase.LoadAlbumsTask().with(new PostExecutor<LiveData<Album.Extended[]>>() {
            @Override
            public void onPostExecute(LiveData<Album.Extended[]> liveData) {
                GalleryActivity.this.adapter.setLiveData(liveData);
            }
        }).execute();

        //TODO Remove
        {
            Album album1 = new Album();
            album1.albumId = Album.PRIVATE_ALBUM_ID;
            album1.name = "Private";

            Album album2 = new Album();
            album2.albumId = "a1";
            album2.name = "Happy Hour";

            new GalleryDatabase.InsertAlbumTask().execute(album1, album2);
        }
    }
}

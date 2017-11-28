package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;

public class AlbumActivity extends AppCompatActivity {

    public static final String EXTRA_ALBUM = "com.appspot.mccfall2017g12.photoorganizer.ALBUM";

    private RecyclerView recyclerView;
    private PhotoAdapter adapter;
    private String albumId;
    private LocalDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        final int columnCount = getResources().getInteger(R.integer.photo_column_count);

        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_ALBUM))
            this.albumId = getIntent().getStringExtra(EXTRA_ALBUM);
        else
            throw new IllegalArgumentException("Album key missing.");

        database = LocalDatabase.getInstance(this);

        this.recyclerView = findViewById(R.id.photos_recycler_view);
        this.recyclerView.setHasFixedSize(true);

        this.adapter = new PhotoAdapter(this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = AlbumActivity.this.recyclerView.getChildAdapterPosition(view);
                final Photo photo = AlbumActivity.this.adapter.getItem(position).photo;

                if (photo.file == null)
                    return;

                File file = FileTools.get(photo.file);
                if (file.exists()) {
                    Uri uri = FileProvider.getUriForFile(AlbumActivity.this,
                            BuildConfig.APPLICATION_ID + ".provider", file);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "image/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                }
                else {
                    Snackbar snackbar = Snackbar.make(AlbumActivity.this.recyclerView,
                            R.string.file_does_not_exists, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.remove, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ThreadTools.EXECUTOR.execute(new Runnable() {
                                @Override
                                public void run() {
                                    database.galleryDao().deletePhotos(photo);
                                }
                            });
                        }
                    });
                    snackbar.show();
                }
            }
        });
        this.recyclerView.setAdapter(this.adapter);

        GridLayoutManager layoutManager = new GridLayoutManager(this, columnCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (AlbumActivity.this.adapter.getItem(position).isHeader())
                    return columnCount;
                return 1;
            }
        });
        this.recyclerView.setLayoutManager(layoutManager);

        this.adapter.setLiveData(database.galleryDao().loadAlbumsPhotosByPeople(this.albumId));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sortByAuthorItem:
                this.adapter.setLiveData(database.galleryDao()
                        .loadAlbumsPhotosByAuthor(this.albumId));
                return true;
            case R.id.sortByPeopleItem:
                this.adapter.setLiveData(database.galleryDao()
                        .loadAlbumsPhotosByPeople(this.albumId));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

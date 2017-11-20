package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Random;
import java.util.UUID;

public class AlbumActivity extends AppCompatActivity {

    public static final String EXTRA_ALBUM = "com.appspot.mccfall2017g12.photoorganizer.ALBUM";
    private static final int RESULT_ADD_IMAGE_DEBUG = 1;

    private RecyclerView recyclerView;
    private PhotoAdapter adapter;
    private String albumKey;
    private final LoadItemsPostExecutor loadItemsPostExecutor = new LoadItemsPostExecutor();

    private static final int COLUMN_COUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_ALBUM))
            this.albumKey = getIntent().getStringExtra(EXTRA_ALBUM);
        else
            throw new IllegalArgumentException("Album key missing.");

        this.recyclerView = findViewById(R.id.photos_recycler_view);
        this.recyclerView.setHasFixedSize(true);

        this.adapter = new PhotoAdapter(this, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = AlbumActivity.this.recyclerView.getChildAdapterPosition(view);
                Photo photo = AlbumActivity.this.adapter.getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(photo.path));
                startActivity(intent);
            }
        });
        this.recyclerView.setAdapter(this.adapter);

        GridLayoutManager layoutManager = new GridLayoutManager(this, COLUMN_COUNT);
        this.recyclerView.setLayoutManager(layoutManager);

        GalleryDatabase.initialize(getApplicationContext());
        new GalleryDatabase.LoadPhotosByPeopleTask()
                .with(this.loadItemsPostExecutor)
                .execute(this.albumKey);

        //TODO Remove
        {
            findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, RESULT_ADD_IMAGE_DEBUG);

                }
            });
        }
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
                new GalleryDatabase.LoadPhotosByAuthorTask()
                        .with(this.loadItemsPostExecutor)
                        .execute(this.albumKey);
                return true;
            case R.id.sortByPeopleItem:
                new GalleryDatabase.LoadPhotosByPeopleTask()
                        .with(this.loadItemsPostExecutor)
                        .execute(this.albumKey);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //TODO Remove
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_ADD_IMAGE_DEBUG:
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();

                    Photo photo = new Photo();
                    photo.photoKey = UUID.randomUUID().toString();
                    photo.path = uri.toString();
                    photo.albumKey = albumKey;
                    Random random = new Random();
                    photo.author = new String[] {
                            "Frodo", "Sam", "Merry", "Pippin"
                    }[random.nextInt(4)];
                    photo.peopleAppearance = new int[] {
                            Photo.PEOPLE_NA, Photo.PEOPLE_NO, Photo.PEOPLE_YES
                    }[random.nextInt(3)];

                    new GalleryDatabase.InsertPhotoTask().execute(photo);
                }
        }
    }

    private class LoadItemsPostExecutor implements PostExecutor<LiveData<Photo[]>>  {
        @Override
        public void onPostExecute(LiveData<Photo[]> liveData) {
            AlbumActivity.this.adapter.setLiveData(liveData);
        }
    }
}

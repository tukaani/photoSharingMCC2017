package com.appspot.mccfall2017g12.photoorganizer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class QRActivity extends AppCompatActivity {

    private static final int QR_SIZE = 100;
    private final ValueEventListener listener;
    private final DatabaseReference reference;

    private ImageView qrView;

    public QRActivity() {
        final String groupId = User.get().getGroupId();

        reference = FirebaseDatabase.getInstance()
                .getReference("groups")
                .child(groupId)
                .child("token");

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String token = dataSnapshot.getValue(String.class);
                Bitmap bitmap = encodeAsBitmap(groupId + " " + token);
                qrView.setImageBitmap(Bitmap.createScaledBitmap(bitmap,
                        qrView.getWidth(), qrView.getHeight(), false));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        qrView = findViewById(R.id.qr_image);
    }

    @Override
    protected void onStart() {
        super.onStart();
        reference.addValueEventListener(listener);
    }

    @Override
    protected void onStop() {
        reference.removeEventListener(listener);
        super.onStop();
    }

    Bitmap encodeAsBitmap(String str) {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, null);
        } catch (Exception e) {
            return null;
        }

        int w = result.getWidth();
        int h = result.getHeight();

        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}

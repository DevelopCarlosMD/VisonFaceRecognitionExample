package com.coyotestudio.visonfacerecognitionexample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Made by Carlos Medina - Coyote Deve Studio
 * Examole using Vision API "Faces"
 * 10/12/2017
 * Beta version we have to analize the behaviour once is making the process
 * to offer a better user experience.
 */

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    @BindView(R.id.btn_process_face)
    Button mLaunchRecognizer;
    @BindView(R.id.imv_faces_container)
    ImageView mImageContainer;
    @BindView(R.id.ll_main_view)
    LinearLayout mLayoutContainer;



    private static final String TAG = MainActivity.class.getSimpleName();
    private FaceDetector faceDetector;
    private Canvas canvas;
    private Paint rectPaint;
    private Bitmap temporaryBitmap;

    private static final int CAMERA_REQUEST = 1888;
    private Bitmap photo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_process_face)
    public void launchRecognizer() {
        // BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        // bitmapOptions.inMutable = true;


        //Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(),
         //       R.drawable.img_test, bitmapOptions);

        temporalBitmap(photo);
        creteRectangle();

        if (!faceDetector.isOperational()) {
            new AlertDialog.Builder(this)
                    .setMessage("Face Detector could not be set up on your device :(")
                    .show();
            return;
        } else {
            Frame frame = new Frame.Builder().setBitmap(photo).build();
            SparseArray<Face> sparseArray = faceDetector.detect(frame);

            detectFaces(sparseArray);
            mImageContainer.setImageDrawable(new BitmapDrawable(getResources(), temporaryBitmap));

            faceDetector.release();
        }

        mLaunchRecognizer.setEnabled(false);
        Log.i(TAG, "launchRecognizer: listen onclick");
    }


    public void creteRectangle() {
        rectPaint = new Paint();
        rectPaint.setStrokeWidth(5);
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
    }

    public void temporalBitmap(Bitmap defaultBitmap) {
        temporaryBitmap =
                Bitmap.createBitmap(defaultBitmap.getWidth(), defaultBitmap.getHeight(),
                        Bitmap.Config.RGB_565);

        canvas = new Canvas(temporaryBitmap);
        canvas.drawBitmap(defaultBitmap, 0, 0, null);

    }

    public void initializeDetector() {
        faceDetector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();
    }

    public void detectFaces(SparseArray<Face> sparseArray) {

        for (int i = 0; i < sparseArray.size(); i++) {
            Face face = sparseArray.valueAt(i);

            float left = face.getPosition().x;
            float top = face.getPosition().y;
            float right = left + face.getWidth();
            float bottom = top + face.getHeight();
            float cornerRadius = 2.0f;

            RectF rectF = new RectF(left, top, right, bottom);

            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, rectPaint);
        }
    }

    @OnClick(R.id.btn_load_for_camera)
    public void loadImagesFromCamera() {
        showCameraPreview();
    }

    @OnClick(R.id.btn_load_for_media)
    public void loadImageFromGallery() {

        Log.i(TAG, "loadImageFromGallery: onclick was pressed");
    }

    private void showCameraPreview() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) {

            // Permission is already available, start camera preview
            Snackbar.make(mLayoutContainer,
                    "Camera permission is available. Starting preview.",
                    Snackbar.LENGTH_SHORT).show();
            startCamera();
        } else {
            // Permission is missing and must be requested.
            requestCameraPermission();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == CAMERA_REQUEST) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Snackbar.make(mLayoutContainer, "Camera permission was granted",
                        Snackbar.LENGTH_SHORT)
                        .show();

                startCamera();
            } else {
                // Permission request was denied.
                Snackbar.make(mLayoutContainer,
                        "Camera permission request was denied",
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void requestCameraPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {

            Snackbar.make(mLayoutContainer, "Camera access is required to display the camera preview",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK,", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_REQUEST);
                }
            }).show();
        } else {

            Snackbar.make(mLayoutContainer,
                    "Permission is not available. Requesting camera permission",
                    Snackbar.LENGTH_SHORT).show();

            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST);
        }
    }

    private void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK){
            photo = (Bitmap) data.getExtras().get("data");
            mImageContainer.setImageBitmap(photo);
            mLaunchRecognizer.setEnabled(true);

            initializeDetector();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        faceDetector.release();
    }
}

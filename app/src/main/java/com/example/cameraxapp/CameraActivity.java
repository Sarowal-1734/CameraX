package com.example.cameraxapp;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    private ImageView imageViewCaptureImage;
    private ImageView imageViewPreviewImage;
    private TextView textViewReTake;
    private TextView textViewNext;

    private PreviewView mPreviewView;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageViewCaptureImage = findViewById(R.id.imageViewCaptureImage);
        textViewReTake = findViewById(R.id.textViewReTake);
        textViewNext = findViewById(R.id.textViewNext);
        mPreviewView = findViewById(R.id.previewView);

        // Initialize the camera preview
        startCamera();

        // Capture image on bitton click
        imageViewCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        // Capture image on bitton click
        textViewReTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewPreviewImage.setImageURI(null);
                imageViewPreviewImage.setVisibility(View.GONE);
                mPreviewView.setVisibility(View.VISIBLE);
                imageViewCaptureImage.setEnabled(true);
                imageViewCaptureImage.clearColorFilter();
                textViewNext.setEnabled(false);
                textViewNext.setTextColor(Color.GRAY);
                textViewReTake.setEnabled(false);
                textViewReTake.setTextColor(Color.GRAY);
            }
        });

        // Capture image on bitton click
        textViewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CameraActivity.this, "Next Clicked", Toast.LENGTH_SHORT).show();
            }
        });

    }// Ending onCreate

    private void takePicture() {
        File photoFile = new File(getCacheDir().getAbsolutePath(), "NIDFRONT.jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(CameraActivity.this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri imageUri = Uri.fromFile(photoFile);
                        imageViewPreviewImage = findViewById(R.id.imageView);
                        mPreviewView.setVisibility(View.GONE);
                        imageViewPreviewImage.setVisibility(View.VISIBLE);
                        imageViewPreviewImage.setImageURI(imageUri);
                        imageViewCaptureImage.setEnabled(false);
                        imageViewCaptureImage.setColorFilter(Color.GRAY);
                        textViewNext.setEnabled(true);
                        textViewNext.setTextColor(Color.BLACK);
                        textViewReTake.setEnabled(true);
                        textViewReTake.setTextColor(Color.BLACK);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CameraActivity.this, "Failed to capture image!", Toast.LENGTH_SHORT).show();
                        exception.printStackTrace();
                    }
                });
    }

    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // Camera provider is now guaranteed to be available
                    ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                    // Set up the view finder use case to display camera preview
                    Preview preview = new Preview.Builder().build();
                    // Set up the capture use case to allow users to take photos
                    imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();
                    // Choose the camera Front or Back
                    CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                    // Set image dimension according to the preview
                    imageCapture.setCropAspectRatio(new Rational(350, 200));
                    // Connect the preview use case to the previewView
                    preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll();
                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle((LifecycleOwner) CameraActivity.this, cameraSelector, preview, imageCapture);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }
}
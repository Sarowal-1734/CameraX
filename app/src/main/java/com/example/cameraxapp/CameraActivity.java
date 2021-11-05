package com.example.cameraxapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
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

    private PreviewView mPreviewView;
    private Button buttonCaptureImage;
    ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mPreviewView = findViewById(R.id.previewView);
        buttonCaptureImage = findViewById(R.id.buttonCaptureImage);

        startCamera();

        buttonCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File photoFile = new File(getCacheDir().getAbsolutePath(), "NID.jpg");
                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
                imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(CameraActivity.this),
                        new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                Uri imageUri = Uri.fromFile(photoFile);
                                Toast.makeText(CameraActivity.this, imageUri.toString(), Toast.LENGTH_LONG).show();
                                ImageView imageView = findViewById(R.id.imageView);
                                imageView.setImageURI(imageUri);
                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                Toast.makeText(CameraActivity.this, "Failed to capture image!", Toast.LENGTH_SHORT).show();
                                exception.printStackTrace();
                            }
                        });
            }
        });

    }// Ending onCreate

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
                    // Set image dimension
                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(300, 250)).build(); // doesn't work
                    // Connect the preview use case to the previewView
                    preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll();
                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle((LifecycleOwner) CameraActivity.this, cameraSelector, preview, imageAnalysis, imageCapture);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }
}
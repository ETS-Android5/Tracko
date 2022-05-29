package com.kaavya.htface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.otaliastudios.cameraview.BitmapCallback;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class TakeAPhotoActivity extends AppCompatActivity {

    private CameraView camera = null;
    private FaceDetector firebaseDetector = null;

    private boolean isFaceDetectionCompleted = false;
    private String faceDetectionErrorMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_aphoto);

        initFireBase();

        camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);

        camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(PictureResult result) {
                // Picture was taken!
                // If planning to show a Bitmap, we will take care of
                // EXIF rotation and background threading for you...
                result.toBitmap(1080, 1920, new BitmapCallback() {
                    @Override
                    public void onBitmapReady(@Nullable Bitmap bitmap) {

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                isFaceDetectionCompleted = false;
                                faceDetectionErrorMessage = null;

                                //Store the captured bitmap
                                {
                                    File path = getFilesDir();
                                    File file = new File(path, "capture_" + new Date().getTime() + ".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
                                    if(!file.exists()) {
                                        try {
                                            file.createNewFile();
                                        } catch(Exception ex) {

                                        }
                                    }
                                    try(FileOutputStream fOut = new FileOutputStream(file)) {
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                                        fOut.flush(); // Not really required
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                //Check if the bitmap has a face
                                InputImage firebaseImage = InputImage.fromBitmap(bitmap, 0);

                                final ArrayList<Face> detectedFVFaces = new ArrayList<Face>();

                                Task<List<Face>> result =
                                        firebaseDetector.process(firebaseImage)
                                                .addOnSuccessListener(
                                                        new OnSuccessListener<List<Face>>() {
                                                            @Override
                                                            public void onSuccess(List<Face> faces) {
                                                                Face detectedFace = null;

                                                                if(faces.size() > 0) {
                                                                    detectedFace = faces.get(0);

                                                                    for (Face f : faces) {
                                                                        if(f.getBoundingBox().width() > detectedFace.getBoundingBox().width())
                                                                            detectedFace = f;
                                                                    }
                                                                }

                                                                if(detectedFace != null)
                                                                    detectedFVFaces.add(detectedFace);

                                                                isFaceDetectionCompleted = true;
                                                            }
                                                        })
                                                .addOnFailureListener(
                                                        new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                e.printStackTrace();
                                                                faceDetectionErrorMessage = e.getMessage();
                                                                isFaceDetectionCompleted = true;
                                                            }
                                                        });


                                while(!isFaceDetectionCompleted) {
                                    try {
                                        Thread.sleep(100);
                                    } catch(Exception ex) {

                                    }
                                };

                                if(faceDetectionErrorMessage != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(TakeAPhotoActivity.this, faceDetectionErrorMessage, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    return;
                                }

                                Face prominentFace = null;

                                for(Face face : detectedFVFaces) {
                                    if(prominentFace == null) {
                                        prominentFace = face;
                                        continue;
                                    }

                                    if(face.getBoundingBox().height() * face.getBoundingBox().width() > prominentFace.getBoundingBox().height() * prominentFace.getBoundingBox().width()) {
                                        prominentFace = face;
                                    }
                                }

                                if(prominentFace == null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(TakeAPhotoActivity.this, "No Face Detected", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    return;
                                }

                                //Bitmap img = getBitmap(firebaseImage.getByteBuffer(), firebaseImage.getWidth(), firebaseImage.getHeight(), firebaseImage.getRotationDegrees());
                                Bitmap img = bitmap;

                                int left = prominentFace.getBoundingBox().left;
                                int top = prominentFace.getBoundingBox().top;
                                int width = prominentFace.getBoundingBox().width();
                                int height = prominentFace.getBoundingBox().height();

                                //Let's give a 20% buffer on both sides
                                int widthBuffer = (int)(width * 0.2F);
                                int heightBuffer = (int)(height * 0.2F);

                                if((left - widthBuffer) < 0)
                                    left = 0;
                                else
                                    left = left - widthBuffer;

                                if((top - heightBuffer) < 0)
                                    top = 0;
                                else
                                    top = top - heightBuffer;

                                if((left + width + (2 * widthBuffer)) > img.getWidth()) {
                                    width = img.getWidth() - left;
                                } else {
                                    width = width + (2 * widthBuffer);
                                }

                                if((top + height + (2 * heightBuffer)) > img.getHeight()) {
                                    height = img.getHeight() - top;
                                } else {
                                    height = height + (2 * heightBuffer);
                                }

                                Bitmap croppedBitmap = Bitmap.createBitmap(img,
                                        left,
                                        top,
                                        width,
                                        height);

                                final Bitmap faceBitmap = resize(croppedBitmap, 480, 640);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Finish this
                                        Intent intent = new Intent();
                                        Bundle b = new Bundle();
                                        b.putByteArray("BITMAP", bitmapToByteArray(faceBitmap));
                                        intent.putExtras(b);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }
                                });
                            }
                        }).start();
                    }
                });
            }

            @Override
            public void onCameraOpened(@NonNull CameraOptions options) {
                Collection<Facing> facings = options.getSupportedFacing();

                Facing cameraFacing = camera.getFacing();
                SharedPreferences settings = getSharedPreferences("HYPERTRACKFACE", 0);
                String liveAuthCamFront = settings.getString("FACING", "NULL");
                if (liveAuthCamFront.equals("NULL")) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("FACING", cameraFacing == Facing.FRONT ? "FRONT" : "BACK");
                    editor.commit();
                }

                camera.setFacing(cameraFacing);

                if (facings.size() == 1) {
                    findViewById(R.id.btnCameraSwitch).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.btnCameraSwitch).setVisibility(View.VISIBLE);
                }

                boolean flashSupported = false;
                Collection<Flash> flashSupport = options.getSupportedFlash();
                for (Flash f : flashSupport) {
                    if (f.equals(Flash.TORCH)) {
                        flashSupported = true;
                        break;
                    }
                }

                if (flashSupported) {
                    findViewById(R.id.btnFlash).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.btnFlash).setVisibility(View.GONE);
                }
            }
        });

        findViewById(R.id.btnCameraSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCameraSwitch();
            }
        });

        findViewById(R.id.btnFlash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCameraFlash();
            }
        });

        findViewById(R.id.btnTakePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTakePhoto();
            }
        });
    }

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    public static Bitmap getBitmap(ByteBuffer data, int width, int height, int rotation) {
        data.rewind();
        byte[] imageInBuffer = new byte[data.limit()];
        data.get(imageInBuffer, 0, imageInBuffer.length);
        try {
            YuvImage image =
                    new YuvImage(
                            imageInBuffer, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compressToJpeg(new Rect(0, 0, width, height), 80, stream);

            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());

            stream.close();
            return rotateBitmap(bmp, rotation, false, false);
        } catch (Exception e) {
            Log.e("VisionProcessorBase", "Error: " + e.getMessage());
        }
        return null;
    }

    private static Bitmap rotateBitmap( Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees);

        // Mirror the image along the X or Y axis.
        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    private void doCameraSwitch() {
        SharedPreferences settings = getSharedPreferences("HYPERTRACKFACE", 0);

        if(camera.getFacing() == Facing.FRONT) {
            camera.setFacing(Facing.BACK);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("FACING", "BACK");
            editor.commit();
        } else {
            camera.setFacing(Facing.FRONT);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("FACING", "FRONT");
            editor.commit();
        }
    }

    private void doCameraFlash() {
        if(camera.getFlash() == Flash.OFF) {
            camera.setFlash(Flash.TORCH);
        } else {
            camera.setFlash(Flash.OFF);
        }
    }

    private void doTakePhoto() {
        camera.takePicture();
    }

    private void initFireBase() {
        if(firebaseDetector == null) {
            try {
                FaceDetectorOptions highAccuracyOpts =
                        new FaceDetectorOptions.Builder()
                                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                                .build();

                FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);

                firebaseDetector = detector;
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private byte[] bitmapToByteArray(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
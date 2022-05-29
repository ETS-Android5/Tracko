package com.kaavya.htface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.hypertrack.sdk.HyperTrack;
import com.hypertrack.sdk.TrackingError;
import com.hypertrack.sdk.TrackingStateObserver;
import com.kaavya.utils.Globals;
import com.kaavya.utils.OkHttpUtils;
import com.kaavya.xsd.FindRequest;
import com.kaavya.xsd.FindResponse;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.enums.EPickType;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TrackingStateObserver.OnTrackingStateChangeListener, IPickResult {

    private HyperTrack sdkInstance = null;
    private boolean shouldRequestPermissions = true;
    private TextView tvStatus = null;

    private FaceDetector firebaseDetector = null;
    private boolean isFaceDetectionCompleted = false;
    private String faceDetectionErrorMessage = null;

    private ProgressDialog pd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFireBase();
        tvStatus = findViewById(R.id.tvStatus);

        SharedPreferences settings = getSharedPreferences("HYPERTRACKFACE", 0);
        String deviceName = settings.getString("NAME", "UNKNOWN");
        sdkInstance = HyperTrack
                .getInstance(Globals.getHyperTrackPublishableKey())
                .setDeviceName(deviceName)
                .addTrackingListener(this);

        Button btnTrackPerson = findViewById(R.id.btnTrackPerson);

        btnTrackPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTrackPerson();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldRequestPermissions) {
            shouldRequestPermissions = false;
            sdkInstance.requestPermissionsIfNecessary();
            return;
        }

        if((sdkInstance != null) && (!sdkInstance.isRunning())) {
            sdkInstance.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sdkInstance.removeTrackingListener(this);
    }

    @Override
    public void onError(TrackingError trackingError) {
        switch (trackingError.code) {
            case TrackingError.INVALID_PUBLISHABLE_KEY_ERROR:
            case TrackingError.AUTHORIZATION_ERROR:
                Toast.makeText(this, "Please check your HyperTrack publishable key", Toast.LENGTH_LONG).show();
                break;
            case TrackingError.GPS_PROVIDER_DISABLED_ERROR:
                Toast.makeText(this, "Please enable GPS and location access", Toast.LENGTH_LONG).show();
                break;
            case TrackingError.PERMISSION_DENIED_ERROR:
                Toast.makeText(this, "Required permissions are not provided. Please grant access", Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(this, "Cannot start tracking", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onTrackingStart() {
        tvStatus.setText("Your location is currently being tracked");
    }

    @Override
    public void onTrackingStop() {
        tvStatus.setText("Your location is not tracked currently");
    }

    private void onTrackPerson() {
        PickSetup setup = new PickSetup();
        setup.setPickTypes(EPickType.GALLERY);
        PickImageDialog.build(setup).show(this);
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            String filePath = r.getPath();

            int orientation = 0;

            try {
                ExifInterface ei = new ExifInterface(filePath);
                orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            } catch(Exception ex) {

            }

            Log.e("***DBG", "Orientation is " + orientation);
            Bitmap selectedBitmap = r.getBitmap();
            Bitmap rotatedBitmap = null;
            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(selectedBitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(selectedBitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(selectedBitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = selectedBitmap;
            }

            //Check if there is a face in the selected image
            onImageSelected(rotatedBitmap);

        } else {
            //Handle possible errors
            //TODO: do what you have to do with r.getError();
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    private void onImageSelected(Bitmap bitmap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                isFaceDetectionCompleted = false;
                faceDetectionErrorMessage = null;

                //Check if the bitmap has a face
                InputImage firebaseImage = InputImage.fromBitmap(bitmap, 0);

                Log.d("***IMG***", "Rotation Angle is " + firebaseImage.getRotationDegrees());

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
                            Toast.makeText(MainActivity.this, faceDetectionErrorMessage, Toast.LENGTH_LONG).show();
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
                            Toast.makeText(MainActivity.this, "No Face Detected", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

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

                //Call server for getting the tracking info of the user
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        pd = new ProgressDialog(MainActivity.this);
                        pd.setTitle("Tracking User");
                        pd.setMessage("Please wait...");
                        pd.setCancelable(false);
                        pd.setIndeterminate(true);
                        pd.show();

                        FindRequest req = new FindRequest();
                        req.setPhoto(Base64.encodeToString(bitmapToByteArray(faceBitmap), Base64.NO_WRAP));

                        final String reqJson = new Gson().toJson(req);
                        new Thread() {
                            public void run() {
                                callTrack(reqJson);
                            }
                        }.start();
                    }
                });
        }
        }).start();
    }

    private void callTrack(String reqJson) {
        String resString = null;
        try {
            resString = OkHttpUtils.postRequest(Globals.getAppUrl() + "Find", "application/json", reqJson);
            //System.out.print("response : " + resString);
        } catch (Exception ex) {
            ex.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.dismiss();
                    Toast.makeText(MainActivity.this, "Error in connecting to server", Toast.LENGTH_LONG).show();
                }
            });

            return;
        }

        //Now deserialize the response
        final FindResponse resp = new Gson().fromJson(resString, FindResponse.class);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (resp.isError()) {
                    pd.dismiss();
                    final String errorMessage = resp.getErrorDescription();
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                } else {
                    proceedToMapActivity(resp.getLat(), resp.getLon(), resp.getName());
                }
            }
        });
    }

    private void proceedToMapActivity(double lat, double lon, String name) {
        Intent intent = new Intent(MainActivity.this, MapViewActivity.class);
        intent.putExtra("LAT", lat);
        intent.putExtra("LON", lon);
        intent.putExtra("NAME", name);
        startActivity(intent);
        MainActivity.this.finish();
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

    private byte[] bitmapToByteArray(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

}
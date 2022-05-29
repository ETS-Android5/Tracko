package com.kaavya.htface;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hypertrack.sdk.HyperTrack;
import com.kaavya.utils.Globals;
import com.kaavya.utils.OkHttpUtils;
import com.kaavya.xsd.RegisterRequest;
import com.kaavya.xsd.RegisterResponse;

import java.io.ByteArrayOutputStream;

public class RegisterActivity extends Activity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private TextView tvTakePhoto = null;
    private ImageView ivPhoto = null;
    private Button cirLoginButton = null;
    private byte[] photoContent = null;

    private Bitmap photo = null;
    private String name = null;
    private String emailAddress = null;

    private ProgressDialog pd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        tvTakePhoto = findViewById(R.id.tvTakePhoto);
        ivPhoto = findViewById(R.id.ivPhoto);
        cirLoginButton = findViewById(R.id.cirLoginButton);

        tvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTakePhoto();
            }
        });

        cirLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRegister();
            }
        });
    }

    private void onTakePhoto() {
        Intent takePictureIntent = new Intent(this, TakeAPhotoActivity.class);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            byte[] byImageBitmap = extras.getByteArray("BITMAP");
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(byImageBitmap, 0, byImageBitmap.length);
            ivPhoto.setImageBitmap(imageBitmap);
            photo = imageBitmap;
        }
    }

    private void onRegister() {
        EditText editTextName = findViewById(R.id.editTextName);
        name = editTextName.getText().toString().trim();
        if(name.length() == 0) {
            editTextName.requestFocus();
            Toast.makeText(this, "Please enter name", Toast.LENGTH_LONG).show();
            return;
        }

        EditText editTextEmail = findViewById(R.id.editTextEmail);
        emailAddress = editTextEmail.getText().toString().trim();
        if(emailAddress.length() == 0) {
            editTextEmail.requestFocus();
            Toast.makeText(this, "Please enter email address", Toast.LENGTH_LONG).show();
            return;
        }

        if(photo == null) {
            Toast.makeText(this, "Please Take a Photo before registering", Toast.LENGTH_LONG).show();
            return;
        }

        //Now register the app on the server
        String deviceId = HyperTrack.getInstance(Globals.getHyperTrackPublishableKey()).getDeviceID();

        pd = new ProgressDialog(this);
        pd.setTitle("Registering User");
        pd.setMessage("Please wait...");
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();

        RegisterRequest req = new RegisterRequest();
        req.setName(name);
        req.setDeviceId(deviceId);
        req.setUserId(emailAddress);
        req.setPhoto(Base64.encodeToString(bitmapToByteArray(photo), Base64.NO_WRAP));

        final String reqJson = new Gson().toJson(req);
        new Thread() {
            public void run() {
                callRegistration(reqJson);
            }
        }.start();
    }

    private void callRegistration(String reqJson) {
        String resString = null;
        try {
            resString = OkHttpUtils.postRequest(Globals.getAppUrl() + "Register", "application/json", reqJson);
            //System.out.print("response : " + resString);
        } catch (Exception ex) {
            ex.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.dismiss();
                    String errorMessage = "Error in sending/receiving data!";
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }

        //Now deserialize the response
        final RegisterResponse resp = new Gson().fromJson(resString, RegisterResponse.class);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (resp.isError()) {
                    pd.dismiss();
                    String errorMessage = resp.getErrorDescription();
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                } else {
                    handleRegistrationSuccess();
                }
            }
        });
    }

    private void handleRegistrationSuccess() {
        SharedPreferences settings = getSharedPreferences("HYPERTRACKFACE", 0);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("NAME", name);
        editor.putString("EMAILID", emailAddress);
        editor.putBoolean("REGISTERED", true);
        editor.commit();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private byte[] bitmapToByteArray(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
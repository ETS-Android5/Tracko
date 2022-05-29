package com.kaavya.htface;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hypertrack.sdk.HyperTrack;
import com.kaavya.utils.DialogUtils;
import com.kaavya.utils.Globals;
import com.kaavya.utils.OkHttpUtils;
import com.kaavya.xsd.LoginRequest;
import com.kaavya.xsd.LoginResponse;

import java.util.Date;

public class SplashScreenActivity extends Activity {

    private static final int REQUEST_ALL_PERMISSIONS = 100;
    private boolean isPermissionGranted = false;

    private String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private String permissionText = "This app requires Camera permissions.";

    private static final int SPLASH_SHOW_TIME = 2000;

    private ProgressDialog pd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        PackageInfo pInfo = null;
        String versionName = "Unknown";
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }

//        TextView tvVersion = (TextView) findViewById(R.id.lblVersion);
//        tvVersion.setText("Version " + versionName);
        new BackgroundSplashTask().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_ALL_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    boolean isPermissionGranted = true;
                    for(int result : grantResults) {
                        if(result != PackageManager.PERMISSION_GRANTED) {
                            isPermissionGranted = false;
                            break;
                        }
                    }

                    if(isPermissionGranted) {
                        handleNextActivity();
                        return;
                    }
                }

                // permission denied
                Toast.makeText(SplashScreenActivity.this, permissionText, Toast.LENGTH_SHORT);
                finish();
                return;
            }
            default:
                Toast.makeText(SplashScreenActivity.this, permissionText, Toast.LENGTH_SHORT);
                finish();
                break;
        }
    }

    private void handleNextActivity() {

        //If we have already registered, let's use the stored values
        SharedPreferences settings = getSharedPreferences("HYPERTRACKFACE", 0);
        if(settings.getBoolean("REGISTERED", false)) {
            //Now send the request to server for validation
            String emailAddress = settings.getString("EMAILID", null);
            String deviceId = HyperTrack.getInstance(Globals.getHyperTrackPublishableKey()).getDeviceID();

            pd = new ProgressDialog(this);
            pd.setTitle("Validating User");
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();

            LoginRequest req = new LoginRequest();
            req.setDeviceId(deviceId);
            req.setUserId(emailAddress);

            final String reqJson = new Gson().toJson(req);
            new Thread() {
                public void run() {
                    callLogin(reqJson);
                }
            }.start();
            return;
        }

        //Go to registration Screen
        proceedToRegistration();
        return;
    }

    private void callLogin(String reqJson) {
        String resString = null;
        try {
            resString = OkHttpUtils.postRequest(Globals.getAppUrl() + "Login", "application/json", reqJson);
            //System.out.print("response : " + resString);
        } catch (Exception ex) {
            ex.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd.dismiss();
                    DialogUtils.showErrorDialog(SplashScreenActivity.this, "Error in connecting to server", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SplashScreenActivity.this.finish();
                        }
                    });
                }
            });

            return;
        }

        //Now deserialize the response
        final LoginResponse resp = new Gson().fromJson(resString, LoginResponse.class);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (resp.isError()) {
                    pd.dismiss();
                    final String errorMessage = resp.getErrorDescription();
                    DialogUtils.showErrorDialog(SplashScreenActivity.this, errorMessage, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SplashScreenActivity.this.finish();
                        }
                    });
                } else {
                    proceedToMainActivity();
                }
            }
        });
    }

    private void proceedToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void proceedToRegistration() {

        //Switch to Registration Screen
        Intent i = new Intent(SplashScreenActivity.this, RegisterActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
        return;
    }

    /**
     * Async Task: can be used to load DB, images during which the splash screen
     * is shown to user
     */
    private class BackgroundSplashTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            Date start = new Date();
            Date now = new Date();

            long msecs = now.getTime() - start.getTime();
            if(msecs < SPLASH_SHOW_TIME) {
                try {
                    Thread.sleep(SPLASH_SHOW_TIME - msecs);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!hasPermissions(SplashScreenActivity.this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(SplashScreenActivity.this, PERMISSIONS, REQUEST_ALL_PERMISSIONS);
                return;
            }

            handleNextActivity();
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
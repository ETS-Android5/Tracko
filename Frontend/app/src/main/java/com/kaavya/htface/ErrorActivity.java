package com.kaavya.htface;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ErrorActivity extends AppCompatActivity {

    private TextView errorTextView;
    private String errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        Bundle bundle = this.getIntent().getExtras();
        errorMessage = (String) bundle.getSerializable("ERROR_MSG");

        errorTextView = (TextView) findViewById(R.id.error_activity_error_message);
        errorTextView.setText(errorMessage);
        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(view -> { doClose(); });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ErrorActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
    }

    private void doClose() {
        Intent intent = new Intent(ErrorActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        ErrorActivity.this.finish();
    }
}
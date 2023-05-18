package org.med.darknetandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        TextView textView = findViewById(R.id.error_textview);
        textView.setText("Error while loading resources, please check your connection and try again");
        Button button = findViewById(R.id.error_button);
        ImageView imageView = findViewById(R.id.error_imageview);
        imageView.setImageResource(R.drawable.error);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ErrorActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
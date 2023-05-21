package org.med.darknetandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.core.Mat;

public class BoundingBoxActivity extends AppCompatActivity {
    Bitmap mainBitmap;
    Context myContext;
    CanvasView itemImage;
    int imageHeight;
    int imageWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bounding_box);

        myContext = this;

        Bundle bundle = getIntent().getExtras();
        String filePath = bundle.getString("path");
        mainBitmap = BitmapFactory.decodeFile(filePath.substring(5));

        itemImage = findViewById(R.id.bounding_imageview);
        itemImage.setBackground(new BitmapDrawable(myContext.getResources(),mainBitmap));

        ViewTreeObserver vto = itemImage.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                itemImage.getViewTreeObserver().removeOnPreDrawListener(this);
                imageHeight = itemImage.getMeasuredHeight();
                imageWidth = itemImage.getMeasuredWidth();
                itemImage.setImageHeight(imageHeight);
                itemImage.setImageWidth(imageWidth);
                return true;
            }
        });

        Button cancelButton = findViewById(R.id.bounding_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED, null);
            }
        });

        Button clearButton = findViewById(R.id.bounding_clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemImage.onClear();
            }
        });

        Button submitButton = findViewById(R.id.bounding_submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(itemImage.getDrawn()){
                    float xBeginCoord = itemImage.getBeginX();
                    float yBeginCoord = itemImage.getBeginY();
                    float xEndCoord = itemImage.getEndX();
                    float yEndCoord = itemImage.getEndY();

                    float boundingHeight =  Math.abs(yBeginCoord - yEndCoord);
                    float boundingWidth = Math.abs(xBeginCoord - xEndCoord);
                    float boundingCenterX = boundingWidth/2;
                    float boundingCenterY = boundingHeight/2;

                    float boundingNormalizedHeight = boundingHeight/imageHeight;
                    float boundingNormalizedWidth = boundingWidth/imageWidth;
                    float normalizedCenterX = (Math.min(xBeginCoord,xEndCoord)+ boundingCenterX)/imageWidth;
                    float normalizedCenterY = (Math.min(xEndCoord, yEndCoord)+ boundingCenterY)/imageHeight;

                    if(Math.abs(xBeginCoord - xEndCoord) >= 50 || Math.abs(yBeginCoord - yEndCoord) >= 50){
                        Intent data = new Intent();
                        data.putExtra("normalizedHeight", boundingNormalizedHeight);
                        data.putExtra("normalizedWidth", boundingNormalizedWidth);
                        data.putExtra("normalizedCenterX", normalizedCenterX);
                        data.putExtra("normalizedCenterY", normalizedCenterY);
                        setResult(RESULT_OK, data);
                        finish();
                    }else{
                        Toast.makeText(myContext, "Rectangle too small, please draw again", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    Toast.makeText(myContext, "No Rectangle Drawn!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}

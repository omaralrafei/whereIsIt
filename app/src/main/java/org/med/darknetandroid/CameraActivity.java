package org.med.darknetandroid;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.imgproc.Imgproc;


// This activity is responsible for the callbacks to the javaCamera which implements the Dnn for YOLO to work on
public class CameraActivity extends AppCompatActivity implements CvCameraViewListener2 {
    private static final String TAG = "CameraActivity";
    private static List<String> classNames = new ArrayList<>();
    private static List<Scalar> colors=new ArrayList<>();
    private Net net;
    private CameraBridgeViewBase openCvCameraView;
    private String labelName = "";


    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    openCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    //this method initializes the labels and the openCvCamera and gets the labelName to recognize the object
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            openCvCameraView = findViewById(R.id.CameraView);
            openCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
            openCvCameraView.setCvCameraViewListener(this);
            classNames = readLabels(WelcomeActivity.labelsName, this);
            Bundle bundle = getIntent().getExtras();
            labelName = bundle.getString("labelName");
            for (int i = 0; i < classNames.size(); i++)
                colors.add(randomColor());
    }



    @Override
    public void onResume() {
        super.onResume();
        //When resuming activity, check the opencv loader and if it is found, then connect
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, loaderCallback);
        } else {
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }



    //this method gets the file for the model config as well as its weights and reads the Dnn
    @Override
    public void onCameraViewStarted(int width, int height) {
        String modelConfiguration = getFilesDir().toString()+"/"+ WelcomeActivity.cfgName;
        String modelWeights = getFilesDir().toString()+"/" + WelcomeActivity.weightsName;
        net = Dnn.readNetFromDarknet(modelConfiguration, modelWeights);
    }
    //This is the matrix on which it is divided to detect objects
    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Mat frame = inputFrame.rgba();
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
        Size frame_size = new Size(416, 416);
        Scalar mean = new Scalar(127.5);

        Mat blob = Dnn.blobFromImage(frame, 1.0 / 255.0, frame_size, mean, true, false);
        net.setInput(blob);


        List<Mat> result = new ArrayList<>();
        List<String> outBlobNames = net.getUnconnectedOutLayersNames();

        net.forward(result, outBlobNames);
        float confThreshold = 0.2f;

        for (int i = 0; i < result.size(); ++i) {
            // each row is a candidate detection, the 1st 4 numbers are
            // [center_x, center_y, width, height], followed by (N-4) class probabilities
            Mat level = result.get(i);
            for (int j = 0; j < level.rows(); ++j) {
                Mat row = level.row(j);
                Mat scores = row.colRange(5, level.cols());
                Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                float confidence = (float) mm.maxVal;
                Point classIdPoint = mm.maxLoc;
                if (confidence > confThreshold) {
                    int centerX = (int) (row.get(0, 0)[0] * frame.cols());
                    int centerY = (int) (row.get(0, 1)[0] * frame.rows());
                    int width = (int) (row.get(0, 2)[0] * frame.cols());
                    int height = (int) (row.get(0, 3)[0] * frame.rows());

                    int left = (int) (centerX - width * 0.5);
                    int top =(int)(centerY - height * 0.5);
                    int right =(int)(centerX + width * 0.5);
                    int bottom =(int)(centerY + height * 0.5);

                    Point left_top = new Point(left, top);
                    Point right_bottom=new Point(right, bottom);
                    Point label_left_top = new Point(left, top-5);
                    DecimalFormat df = new DecimalFormat("#.##");

                    int class_id = (int) classIdPoint.x;
                    String ogLabel = classNames.get(class_id);
                    String label= classNames.get(class_id) + ": " + df.format(confidence);
                    Scalar color= colors.get(class_id);

                    if(ogLabel.equalsIgnoreCase(labelName)){
                        Imgproc.rectangle(frame, left_top,right_bottom , color, 3, 2);
                        Imgproc.putText(frame, label, label_left_top, Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 0), 4);
                        Imgproc.putText(frame, label, label_left_top, Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 255, 255), 2);
                    }
                }
            }
        }
        return frame;
    }

    @Override
    public void onBackPressed() {
        Intent welcome = new Intent(this, ItemsActivity.class);
        startActivity(welcome);
        finish();
    }

    //This function extracts assets from the assets folder of android studio and copies them into the filesDir
    private static String getAssetsFile(String file, Context context) {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream;
        try {
            // Read data from assets.
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            // Create copy file in storage.
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            // Return a path to file which may be read in common way.
            return outFile.getAbsolutePath();
        } catch (IOException ex) {
            Log.i(TAG, "Failed to upload a file");
        }
        return "";
    }



    //this method reads the labels from a file in the device
    public static List<String> readLabels (String file, Context context)
    {
        BufferedInputStream inputStream;
        List<String> labelsArray = new ArrayList<>();
        try {
            // Read data from files directory
            File labelFile = new File(context.getFilesDir().toString(), file);
            FileInputStream fileInputStream = new FileInputStream(labelFile);
            inputStream = new BufferedInputStream(fileInputStream);
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            // Create copy file and reads its contents to populate the List of strings to be returned
            File outFile = new File(context.getFilesDir().toString(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            Scanner fileScanner = new Scanner(new File(outFile.getAbsolutePath())).useDelimiter("\n");
            String label;
            while (fileScanner.hasNext()) {
                label = fileScanner.next().split("\n")[0];
                labelsArray.add(label);
            }
            fileScanner.close();
        } catch (IOException ex) {
            Log.e(TAG, "Failed to read labels!");
        }
        return labelsArray;
    }

    //Choose a random color for the bounding box
    private Scalar randomColor() {
        Random random = new Random();
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        return new Scalar(r,g,b);
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (openCvCameraView != null)
            openCvCameraView.disableView();
    }
}

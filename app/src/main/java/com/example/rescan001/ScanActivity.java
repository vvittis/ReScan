package com.example.rescan001;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.media.Image;
import android.os.Bundle;
import android.os.Environment;

import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.camera.core.ImageCapture;


import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.CameraSource.PictureCallback;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class ScanActivity extends Activity {

    SurfaceView cameraPreview;
    Button capture;
    private static String path;
    private File dir;
    private ImageCapture imgCap;
    private String brc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_preview);

        //path= Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"photo1";
        //dir = new File(path);
        cameraPreview = (SurfaceView)findViewById(R.id.cam_prev);
        capture = (Button)findViewById(R.id.capture_but);
        createCameraSource();
    }


    private void createCameraSource() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).build();
        final CameraSource cameraSource = new CameraSource.Builder(this,barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1600,1024)
                .build();


        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {

                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });

        /**
         * NEW
         */
        capture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cameraSource.takePicture(null, new PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes) {
                        File file= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+"/Rescan");
                        // = new File(path);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes , 0, bytes .length);
                        if(bitmap!=null){
                            if(!file.isDirectory()){
                                file.mkdir();
                            }
                            //Save with the barcode value as the file name
                            file=new File(file,brc +".jpg");
                            try{
                                FileOutputStream fileOutputStream=new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG,100, fileOutputStream);
                                fileOutputStream.flush();
                                fileOutputStream.close();
                                //finish();
                            }
                            catch(Exception exception) {
                                Toast.makeText(getApplicationContext(),"Error saving: "+ exception.toString(),Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                });
            }
        });

        /**
         * NEW />
         */
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes=detections.getDetectedItems();
                if(barcodes.size()>0){

                    //Create an Intent to use the barcode in different Activities
                    Intent intent=new Intent();
                    intent.putExtra("barcode",barcodes.valueAt(0));

                    //Barcode Needs to be "Sanitized" for safe file names (/,*, etc)
                    brc=barcodes.valueAt(0).displayValue;

                    setResult(CommonStatusCodes.SUCCESS,intent);
                    //A buggy way to save the picture
                    //Needs future changing
                    capture.callOnClick();

                    //finish();
                }
            }
        });
    }



}
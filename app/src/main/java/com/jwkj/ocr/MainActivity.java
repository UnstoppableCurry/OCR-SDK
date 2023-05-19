package com.jwkj.ocr;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int OPEN_GALLERY_REQUEST_CODE = 0;
    private static final int TAKE_PHOTO_REQUEST_CODE = 1;
    protected TextView tvStatus;
    protected Predictor predictor2 = new Predictor();

    protected ImageView ivInputImage;
    protected ProgressDialog pbLoadModel;
    protected ProgressDialog pbRunModel;

    protected TextView tvInferenceTime;
    protected TextView tvOutputResult;

    private Bitmap cur_predict_image;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStatus = findViewById(R.id.tv_model_img_status);
        tvInferenceTime = findViewById(R.id.tv_inference_time);
        ivInputImage = findViewById(R.id.iv_input_image);
        tvOutputResult = findViewById(R.id.tv_output_result);
        tvOutputResult.setMovementMethod(ScrollingMovementMethod.getInstance());

    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        startActivityForResult(intent, OPEN_GALLERY_REQUEST_CODE);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".bmp",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }
    private void takePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("MainActitity", ex.getMessage(), ex);
                Toast.makeText(MainActivity.this,
                        "Create Camera temp file failed: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.i(TAG, "FILEPATH " + getExternalFilesDir("Pictures").getAbsolutePath());
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.jwkj.ocr.fileprovider",
                        photoFile);
                currentPhotoPath = photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST_CODE);
                Log.i(TAG, "startActivityForResult finished");
            }
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case OPEN_GALLERY_REQUEST_CODE:
                    if (data == null) {
                        break;
                    }
                    try {
                        ContentResolver resolver = getContentResolver();
                        Uri uri = data.getData();
                        Bitmap image = MediaStore.Images.Media.getBitmap(resolver, uri);
                        String[] proj = {MediaStore.Images.Media.DATA};
                        Cursor cursor = managedQuery(uri, proj, null, null, null);
                        cursor.moveToFirst();
                        if (image != null) {
                            cur_predict_image = image;
                            ivInputImage.setImageBitmap(image);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                    break;
                case TAKE_PHOTO_REQUEST_CODE:
                    if (currentPhotoPath != null) {
                        ExifInterface exif = null;
                        try {
                            exif = new ExifInterface(currentPhotoPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED);
                        Log.i(TAG, "rotation " + orientation);
                        Bitmap image = BitmapFactory.decodeFile(currentPhotoPath);
                        image = Utils.rotateBitmap(image, orientation);
                        if (image != null) {
                            cur_predict_image = image;
                            ivInputImage.setImageBitmap(image);
                        }
                    } else {
                        Log.e(TAG, "currentPhotoPath is null");
                    }
                    break;
                default:
                    break;
            }
        }
    }
    private boolean requestAllPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA},
                    0);
            return false;
        }
        return true;
    }

    public void btn_choice_img_click(View view) {
        if (requestAllPermissions()) {
            openGallery();
        }
    }

    public void btn_take_photo_click(View view) {
        if (requestAllPermissions()) {
            takePhoto();
        }
    }

    public void btn_run_model_click(View view) {
        Bitmap image = ((BitmapDrawable) ivInputImage.getDrawable()).getBitmap();
        if (image == null) {
            tvStatus.setText("STATUS: image is not exists");
         } else {
            tvStatus.setText("STATUS: run model ...... ");
            ArrayList<OcrResultModel> result=apiSDK(image);
//            Bitmap imageo=predictor2.getOutputImage();
//            if (imageo!= null){
//                ivInputImage.setImageBitmap(imageo);
//            }
            if (result!=null){
                onRunModelSuccessed(predictor2);

            }
            predictor2.releaseModel();

        }

    }
    public void onRunModelSuccessed(Predictor predictor) {
        tvStatus.setText("STATUS: run model successed");
        // Obtain results and update UI
        tvInferenceTime.setText("Inference time: " + predictor.inferenceTime() + " ms");
        Bitmap outputImage = predictor.outputImage();
        if (outputImage != null) {
            ivInputImage.setImageBitmap(outputImage);
        }
        tvOutputResult.setText(predictor.outputResult());
        tvOutputResult.scrollTo(0, 0);
    }
    public ArrayList<OcrResultModel> apiSDK(Bitmap inputImage){
        //模型参数配置
        String sdk_ModelPath="models/ch_PP-OCRv3";
        String sdk_labelPath="labels/ppocr_keys_v1.txt";
        int sdk_useOpencl=0;//0 cpu 1gpu
        int sdk_cpuThreadNum=4;
        String sdk_cpuPowerMode="LITE_POWER_HIGH"; //LITE_POWER_HIGH  LITE_POWER_LOW  LITE_POWER_FULL  LITE_POWER_NO_BIND LITE_POWER_RAND_HIGH LITE_POWER_RAND_LOW
        int sdk_detLongSize=960;
        float scoreThreshold = 0.1f;
        int sdk_use_run_det=1;
        int sdk_use_run_cls=1;
        int sdk_use_run_rec=1;
        //模型初始化
        predictor2.init(MainActivity.this, sdk_ModelPath, sdk_labelPath, sdk_useOpencl,
                sdk_cpuThreadNum,sdk_cpuPowerMode,sdk_detLongSize, scoreThreshold);

        if (predictor2.isLoaded()){
            predictor2.setInputImage(inputImage);
        }
        ArrayList<OcrResultModel> test=  predictor2.runModel_SDK(sdk_use_run_det, sdk_use_run_cls, sdk_use_run_rec);

        //释放模型
//        predictor2.releaseModel();
        return test;
    }

}
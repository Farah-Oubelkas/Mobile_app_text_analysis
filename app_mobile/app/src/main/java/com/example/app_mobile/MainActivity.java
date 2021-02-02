package com.example.app_mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;



public class MainActivity extends AppCompatActivity {
    private Button cap_btn,detect_btn,analyze_btn;
    private ImageView img;
    private TextView txt;
    private TextView txtpositive,txtnegative;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get the elements of the page
        cap_btn = findViewById(R.id.cap_btn);
        detect_btn = findViewById(R.id.detect_text_btn);
        analyze_btn = findViewById(R.id.analyse_btn3);
        img = findViewById(R.id.image_view);
        txt = findViewById(R.id.text_display);
        txtpositive = findViewById(R.id.txtpositive);
        txtnegative = findViewById(R.id.txtnegative);

        cap_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                txt.setText("");
                txtpositive.setText("");
                txtnegative.setText("");
           }
        });

        detect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                System.out.println("Je suis là");
                detectTextFromImage();
            }
        });

        analyze_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String source_text = (String)txt.getText();
                Toast.makeText(MainActivity.this, "DONE", Toast.LENGTH_SHORT).show();
                analyzeText(source_text);
            }
        });
//3231
    }

    //Open the camera
    private void dispatchTakePictureIntent() {
        //open camera => create intent object
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            //from the bundle object, we'll extract the image
            imageBitmap = (Bitmap) extras.get("data");
            //set the image in the iamageView
            img.setImageBitmap(imageBitmap);
        }
    }

    private void detectTextFromImage() {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        //FirebaseVision instance
        FirebaseVision firebaseVision = FirebaseVision.getInstance();
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();
        //create a task to process the image
        Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);
        task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                System.out.println("Je suis là");
                DisplayTextFromImage(firebaseVisionText);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
               Toast.makeText(MainActivity.this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
               Log.d("Error ", e.getMessage());
            }
        });
    }

    private void DisplayTextFromImage(FirebaseVisionText firebaseVisionText) {
                String text = firebaseVisionText.getText();
        Toast.makeText(this, "DONE", Toast.LENGTH_SHORT).show();
                txt.setText(text);
            }

    public void analyzeText(String source_text) {
        String getURL = "https://api.uclassify.com/v1/uclassify/sentiment/classify?readkey=XALashUyhyDc&text="+source_text;
        OkHttpClient client = new OkHttpClient();
        try {
            Request request = new Request.Builder()
                    .url(getURL)
                    .build();


            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    System.out.println("Bonjour ");
                    System.out.println(e.getMessage());
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    final JSONObject jsonResult;
                    final String result = response.body().string();
                    try {
                        jsonResult = new JSONObject(result);
                        final String convertedText = jsonResult.getString("positive");
                        final String convertedText1 = jsonResult.getString("negative");

                        Log.d("okHttp", jsonResult.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                float a;
                                a = Float.parseFloat(convertedText);
                                a = a * 100;
                                txtpositive.setText("Positive: " + String.valueOf(a) + "%");
                                a = Float.parseFloat(convertedText1);
                                a = a * 100;
                                txtnegative.setText("Negative: " + String.valueOf(a) + "%");
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            });

        } catch (Exception ex) {
            txtpositive.setText(ex.getMessage());
            txtnegative.setText(ex.getMessage());
        }

    }}
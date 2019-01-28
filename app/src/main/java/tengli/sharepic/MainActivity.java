package tengli.sharepic;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private String base64 = "nothing";
    private void setText(final TextView text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    public void sendPost() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                URL url = new URL("https://bkxetb5d8a.execute-api.ap-northeast-1.amazonaws.com/prod/sharepic");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("key1", "value1");
                jsonParam.put("key2", "value2");
                jsonParam.put("key3", base64);

                Log.i("Send JSON", jsonParam.toString());
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonParam.toString());

                os.flush();
                os.close();

                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                StringBuilder result = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                Log.i("Json response", result.toString());

                TextView jsResponse = findViewById(R.id.editText);

                JSONObject resp = new JSONObject(result.toString());
                Log.i("message response", resp.getString("message"));
                setText(jsResponse,resp.getString("message"));
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        });
        thread.start();
    }


    private class DownloadImageWithURLTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageWithURLTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String pathToFile = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(pathToFile).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public void showImg(View view){
        EditText url = findViewById(R.id.editText);

        //**************delete from here
        url.setText("http://image10.bizrate-images.com/resize?sq=60&uid=2216744464");
        //**************delete end

        DownloadImageWithURLTask downloadTask = new DownloadImageWithURLTask(imageView);
        downloadTask.execute(url.getText().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.imageView = this.findViewById(R.id.imageView);
        Button photoButton =  this.findViewById(R.id.button);
        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int permissionCheckRead = ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionCheckRead
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) getApplicationContext(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            CAMERA_REQUEST);
                } else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new
                        Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 1, baos);
            base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            sendPost();
        }
    }

//    public void sendGet(View view) {
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    URL url = new URL("https://bkxetb5d8a.execute-api.ap-northeast-1.amazonaws.com/prod/sharepic");
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    conn.setRequestMethod("GET");
//                    conn.setRequestProperty("Content-Type", "application/json");
//                    conn.setRequestProperty("Accept","application/json");
//
//                    InputStream in = new BufferedInputStream(conn.getInputStream());
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//                    String line;
//                    StringBuilder result = new StringBuilder();
//                    while ((line = reader.readLine()) != null) {
//                        result.append(line);
//                    }
//
//                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
//                    Log.i("Json response", result.toString());
//
//                    EditText jsResponse = (EditText) findViewById(R.id.editText);
//                    setText(jsResponse,result.toString());
//
//                    conn.disconnect();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        thread.start();
//    }
}
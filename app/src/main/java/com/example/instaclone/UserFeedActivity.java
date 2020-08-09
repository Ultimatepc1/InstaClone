package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class UserFeedActivity extends AppCompatActivity implements View.OnClickListener{
    LinearLayout linlayout;
    Intent intent;
    ImageView imgtest;
    int curre=0;
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.logout:
                ParseUser.logOut();
                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
                Intent logout1=new Intent(getApplicationContext(),MainActivity.class);
                logout1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(logout1);
                finish();
                return true;
            case R.id.share:
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }else{
                    getphoto();
                }
            default:return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    public void getphoto(){
        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,1);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                getphoto();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode== RESULT_OK){
                if(data!=null){
                    try {
                        Uri selectedimage=data.getData();
                        Bitmap bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedimage);
                        //Uploading image to parse
                        ByteArrayOutputStream stream=new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                        byte[] byteArray=stream.toByteArray();
                        ParseFile file=new ParseFile("image.png",byteArray);
                        ParseObject object=new ParseObject("Image");
                        object.put("image",file);
                        object.put("username",ParseUser.getCurrentUser().getUsername().toString());
                        object.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e==null){
                                    Toast.makeText(UserFeedActivity.this, "Successfully Shared", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(UserFeedActivity.this, "Something went wrong while uploading image", Toast.LENGTH_SHORT).show();
                                    Log.i("Image upload error",""+e);
                                }
                            }
                        });
                    }catch (Exception e){
                        Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                        Log.i("Data error",""+e);
                    }
                }else{
                    Toast.makeText(this, "No Data Passed", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(this, "Result not ok", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feed);
        if(ParseUser.getCurrentUser()==null){
            Toast.makeText(this, "Login to Proceed Further", Toast.LENGTH_SHORT).show();
            Intent logout1=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(logout1);
            finish();
        }
        imgtest=(ImageView)findViewById(R.id.img);
        imgtest.setOnClickListener(this);
        intent=getIntent();
        linlayout=(LinearLayout)findViewById(R.id.linlayoutdisplay);
        ParseQuery<ParseObject> imagegetter=ParseQuery.getQuery("Image");
        imagegetter.whereEqualTo("username",intent.getStringExtra("username"));
        imagegetter.orderByDescending("createdAt");
        imagegetter.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    if(objects.size()>0){
                        for(ParseObject obj:objects){
                            ParseFile file=(ParseFile)obj.get("image");
                            String urlfile=file.getUrl().toString();
                            String correcturl=urlfile.replace("/127.0.0.1:1337/","/3.14.68.221/");
                            GetBitmapFromParse getBitmapFromParse = new GetBitmapFromParse();
                            try {
                                Bitmap bitmap = getBitmapFromParse.execute(correcturl).get();
                                if(bitmap==null){
                                    Log.i("setting imagesuccess","no "+correcturl);
                                }
                                ImageView imageView=new ImageView(getApplicationContext());
                                imageView.setId(curre++);
                                imageView.setImageBitmap(bitmap);
                                imgtest.setImageBitmap(bitmap);
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Log.i("On imageclick",view.getId()+": "+view);
                                    }
                                });
//                                imageView.setLayoutParams(new ViewGroup.LayoutParams(
//                                        ViewGroup.LayoutParams.MATCH_PARENT,
//                                        ViewGroup.LayoutParams.WRAP_CONTENT
//                                ));
                                Drawable imagedrawable=new BitmapDrawable(bitmap);
                                imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT));
                                imageView.setImageDrawable(imgtest.getDrawable());
                                imageView.setVisibility(View.VISIBLE);
                                imageView.setAlpha(1);
                                //imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//                                imageView.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        imageView.setVisibility(View.GONE);
//                                        imageView.setVisibility(View.VISIBLE);
//                                    }
//                                });
                                imageView.invalidate();
                                imageView.requestLayout();
                                imgtest.setImageBitmap(bitmap);
                                imageView.setImageBitmap(bitmap);
                                linlayout.addView(imageView);
                                Log.i("setting imagesuccess","yes");
                            } catch (Exception e2) {
                                Log.i("setting image error",""+e2);
                                Toast.makeText(UserFeedActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }
                            //Log.i("Data notnull error","Image data "+file.getUrl());
                            /* //This wont work as the onclick button or inbuilt function returns wrong ip'ed url
                            //Hence we need to get data manually from the url
                            file.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] data, ParseException e) {
                                    if(e==null){
                                        if(data!=null){
                                            Bitmap bitmap= BitmapFactory.decodeByteArray(data,0,data.length);
                                            ImageView imageView=new ImageView(getApplicationContext());
                                            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                            ));
                                            imageView.setImageBitmap(bitmap);
                                            linlayout.addView(imageView);
                                            Log.i("Data notnull error","Image data");
                                        }else{
                                            Log.i("Data null error","Image data is null");
                                        }
                                    }else{
                                        Log.i("GET image error",""+e);
                                        Toast.makeText(UserFeedActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                             */
                        }
                        linlayout.invalidate();
                        linlayout.requestLayout();
                    }else{
                        Toast.makeText(UserFeedActivity.this, "No images are uploaded by "+intent.getStringExtra("username"), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else{
                    Toast.makeText(UserFeedActivity.this, "Userimage finding error", Toast.LENGTH_SHORT).show();
                    Log.i("Finding error",""+e);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        Log.i("On imageclick",view.getId()+": "+view);
    }

    public class GetBitmapFromParse extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();

                return BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.i("Async bitmap error",""+e);
            }

            return null;
        }
    }
}
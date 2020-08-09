package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseSession;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Onlogin extends AppCompatActivity {
    ListView userloggedinview;
    List<String> currentloggedinlist;
    ArrayAdapter<String> currentloggedinadapter;
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.logout:
                ParseUser.logOut();
                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            case R.id.share:
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onlogin);
        if(ParseUser.getCurrentUser()==null){
            Toast.makeText(this, "Login to Proceed Further", Toast.LENGTH_SHORT).show();
            Intent logout1=new Intent(getApplicationContext(),MainActivity.class);
            logout1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(logout1);
            finish();
        }
        userloggedinview=(ListView)findViewById(R.id.userloggedinview);
        currentloggedinlist=new ArrayList<String>();
        currentloggedinlist.add("Myself");
        currentloggedinadapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,currentloggedinlist);
        userloggedinview.setAdapter(currentloggedinadapter);
        userloggedinview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent nextintent=new Intent(getApplicationContext(),UserFeedActivity.class);
                nextintent.putExtra("username",currentloggedinlist.get(i));
                startActivity(nextintent);
            }
        });
        ParseQuery<ParseUser> query=ParseUser.getQuery();
        query.addAscendingOrder("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e==null){
                    if(objects.size()>0){
                        currentloggedinlist.clear();
                        for(ParseUser obj:objects){
                            if(!obj.getUsername().matches(ParseUser.getCurrentUser().getUsername()))
                                currentloggedinlist.add(obj.getUsername().toString());
                                //Log.i("Check of",obj.isAuthenticated()+" "+obj.getUsername());
                        }
                        currentloggedinadapter.notifyDataSetChanged();
                    }
                }
                else{
                    Toast.makeText(Onlogin.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                    Log.i("Listfind error",""+e);
                }
            }
        });

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
                                    Toast.makeText(Onlogin.this, "Successfully Shared", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(Onlogin.this, "Something went wrong while uploading image", Toast.LENGTH_SHORT).show();
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
}
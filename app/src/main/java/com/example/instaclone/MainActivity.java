package com.example.instaclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,View.OnKeyListener{
    TextView switchlog;
    Button signlog;
    EditText usernameview,passwordview;
    static String wronginput;
    Intent intent;
    SharedPreferences instaprefernece;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signlog=(Button)findViewById(R.id.signlog);
        switchlog=(TextView) findViewById(R.id.switchlog);
        usernameview=(EditText)findViewById(R.id.username);
        passwordview=(EditText)findViewById(R.id.password);
        signlog.setTag("signup");
        instaprefernece=this.getSharedPreferences("com.example.instaclone",MODE_PRIVATE);
        ImageView instalogoview=(ImageView)findViewById(R.id.instalogoview);
        RelativeLayout signloglayout=(RelativeLayout)findViewById(R.id.onloginlayout);
        TextView headingview=(TextView)findViewById(R.id.heading);
        instalogoview.setOnClickListener(this);
        signloglayout.setOnClickListener(this);
        headingview.setOnClickListener(this);
        if(ParseUser.getCurrentUser()!=null){
            Toast.makeText(this, ParseUser.getCurrentUser().getUsername()+" is Logged In", Toast.LENGTH_LONG).show();
            instaprefernece.edit().putBoolean("oncesigned",true).apply();
            intent=new Intent(this,Onlogin.class);
            startActivity(intent);
            finish();
        }
        if(instaprefernece.getBoolean("oncesigned",false)){
            changeswitcher(switchlog);
        }
        //Advanced keyboard features
        passwordview.setOnKeyListener(this);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    public  void changeswitcher(View view){
        if(signlog.getTag().toString().matches("signup")){
            signlog.setTag("login");
            signlog.setText("Login");
            switchlog.setText("SignUp");
        }
        else{
            signlog.setTag("signup");
            signlog.setText("Sign Up");
            switchlog.setText("Login");
        }
    }

    public void databasefunc(View view){
        if(signlog.getTag().toString().matches("signup"))
            signupfunc(view);
        else
            loginfunc();
    }

    public void signupfunc(final View view){
        final String username=usernameview.getText().toString();
        final String password=passwordview.getText().toString();
        wronginput="";
            ParseQuery<ParseUser> signupquery = ParseUser.getQuery();
            signupquery.whereEqualTo("username",username);
            signupquery.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if(e==null){
                        if(objects.size()>0){
                            wronginput="Username already exists,Please enter another username";
                            //Log.i("Signupfind error","exists"+wronginput);
                        }
                        else {
                            //Log.i("Signupfind error", "not exists " + username);
                            if(username=="" || username.length()<4){
                                wronginput="Invalid Username,Minimum length should be 4";
                            }
                        }
                        if(password=="" || password.length()<8){
                            //Log.i("Signupfind error",wronginput+" input");
                            if(wronginput!=null)
                                wronginput+="\n";
                            wronginput+="Invalid Password,Minimum length should be 8";
                        }
                        if(wronginput!="")
                            Toast.makeText(MainActivity.this, wronginput, Toast.LENGTH_LONG).show();
                        else{
                            ParseUser user=new ParseUser();
                            user.setUsername(username);
                            user.setPassword(password);
                            user.signUpInBackground(new SignUpCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e==null) {
                                        Toast.makeText(MainActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                        usernameview.setText("");
                                        passwordview.setText("");
                                        changeswitcher(view);
                                        intent=new Intent(getApplicationContext(),Onlogin.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else{
                                        Toast.makeText(MainActivity.this, "Something went wrong while signing up", Toast.LENGTH_SHORT).show();
                                        Log.i("Signup error",""+e);
                                    }
                                }
                            });
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        Log.i("Signupfind error",""+e);
                    }
                }
            });

    }

    private void loginfunc() {
        final String username = usernameview.getText().toString();
        final String password = passwordview.getText().toString();
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e==null){
                    Toast.makeText(MainActivity.this, "Login Sucessfull", Toast.LENGTH_SHORT).show();
                    usernameview.setText("");
                    passwordview.setText("");
                    instaprefernece.edit().putBoolean("oncesigned",true).apply();
                    intent=new Intent(getApplicationContext(),Onlogin.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(MainActivity.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                    Log.i("Login error",""+e);
                }
            }
        });
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if(i==KeyEvent.KEYCODE_ENTER && keyEvent.getAction()== KeyEvent.ACTION_DOWN){
            databasefunc(view);
        }
        return false;
    }
    public void onClick(View view){
        InputMethodManager inputMethodManager=(InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }
}
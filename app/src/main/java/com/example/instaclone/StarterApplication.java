package com.example.instaclone;

import android.app.Application;
import com.parse.Parse;
import com.parse.ParseACL;

public class StarterApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        //Every time on starting instance after stopping it serverurl changes so do the corresponding changes
        //the data entered in the server remains the same password:A3C6rvWdWlk9
        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("myappID")
                .clientKey("")
                .server("http://3.14.68.221/parse/")
                .build()
        );

/*
    ParseObject object = new ParseObject("ExampleObject");
    object.put("myNumber", 12368);
    object.put("myString", "pc");

    object.saveInBackground(new SaveCallback () {
      @Override
      public void done(ParseException ex) {
        if (ex == null) {
          Log.i("Parse Result", "Successful!");
        } else {
          Log.i("Parse Result", "Failed" + ex.toString());
        }
      }
    });
*/

/*
    //This line automatically creates a user login details as random strings.Not needed as now we will check for user.
    ParseUser.enableAutomaticUser();
*/

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

    }
}


package com.parse.wallet;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.PushService;

public class WalletApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // Add your initialization code here
    Parse.initialize(this, getString(R.string.parse_app_id),
        getString(R.string.parse_client_key));

    ParseACL defaultACL = new ParseACL();

    // If you would like all objects to be private by default, remove this line.
    defaultACL.setPublicReadAccess(true);

    ParseACL.setDefaultACL(defaultACL, true);

    // Specify a Activity to handle all pushes by default.
    PushService.setDefaultPushCallback(this, MainActivity.class);
    
    // Save the current installation.
    ParseInstallation.getCurrentInstallation().saveInBackground();  
  }
}

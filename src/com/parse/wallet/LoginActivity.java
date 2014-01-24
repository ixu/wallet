package com.parse.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginActivity extends Activity {
  private static final String TAG = "LoginActivity";
  
  /**
   * Logs the user in with the provided info.  If login suceeds
   * you should call finishSuccessfulLoginOrSignup() to close this
   * screen.  If login fails, you should call showLoginFailedToast().
   * @param email
   * @param password
   */
  private void handleLogIn(String email, String password) {
    ParseUser.logInInBackground(email, password, new LogInCallback() {
      @Override
      public void done(ParseUser user, ParseException e) {
        if (e == null) {
          Log.d(TAG, "Successfully logged in as " + user.getUsername());
          finishSuccessfulLoginOrSignup();
        } else {
          Log.d(TAG, "Login failed: " + e.getMessage());
          showLoginFailedToast(e);
        }
      }
    });
  }
  
  /**
   * Signs the user up with the provided info.  If signup succeeds,
   * you should call finishSuccessfulLoginOrSignup() to close this
   * screen.  If signup fails, you should call showSignupFailedToast();
   * @param email
   * @param password
   */
  private void handleSignUp(String email, String password) {
    ParseUser user = new ParseUser();
    user.setUsername(email);
    user.setPassword(password);
    user.setEmail(email);
    user.signUpInBackground(new SignUpCallback() {
      @Override
      public void done(ParseException e) {
        if (e == null) {
          Log.d(TAG, "Successfully signed up");
          finishSuccessfulLoginOrSignup();
        } else {
          Log.d(TAG, "Signup failed: " + e.getMessage());
          showSignupFailedToast(e);
        }
      }
    });
  }
    
  /***************** You don't need to change the code after this line ***********************/
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.login);
    setupUI();
  }
  
  private void setupUI() {
    final EditText inputEmail = (EditText) findViewById(R.id.input_email);
    final EditText inputPassword = (EditText) findViewById(R.id.input_password);

    Button loginButton = (Button) findViewById(R.id.login_button);
    Button signupButton = (Button) findViewById(R.id.signup_button);

    loginButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        handleLogIn(inputEmail.getText().toString(),
            inputPassword.getText().toString());
      }
    });

    signupButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          handleSignUp(inputEmail.getText().toString(),
              inputPassword.getText().toString());
        } catch (IllegalArgumentException e) {
          Log.d(TAG, "Signup threw exception: " + e.getMessage());
          Toast.makeText(LoginActivity.this,
              "Signup failed. " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
      }
    });
  }
  
  private void finishSuccessfulLoginOrSignup() {
    setResult(RESULT_OK);
    finish();
  }
  
  private void showLoginFailedToast(ParseException e) {
    Toast.makeText(LoginActivity.this,
        "Login failed, " + e.getMessage(), Toast.LENGTH_LONG)
        .show();
  }
  
  private void showSignupFailedToast(ParseException e) {
    Toast.makeText(LoginActivity.this,
        "Signup failed, " + e.getMessage(), Toast.LENGTH_LONG)
        .show();
  }
}

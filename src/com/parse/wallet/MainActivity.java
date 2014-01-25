package com.parse.wallet;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQuery.CachePolicy;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends Activity {
  public final static String QR_CODE = "com.parse.wallet.QRCODE";
  private static final String TAG = "MainActivity";
  private static final int LOGIN_REQUEST = 0;

  // Store all available meme background images
  private List<ParseObject> allPhotos;
  
  // Keep track of which meme is being shown right now,
  // need to access from listeners
  private int currentPhotoIndex = 0;
  
  /**
   * Called when the user clicks Save button.
   * 
   * You can get the current ParseFile photo by calling allPhotos.get(currentPhotoIndex)
   * @param topText text from top input box
   * @param bottomText text from bottom input box
   */
  private void handleSave(String topText, String bottomText) {
    ParseObject meme = new ParseObject("Meme");
    meme.put("top", topText);
    meme.put("bottom", bottomText);
    meme.put("photo", allPhotos.get(currentPhotoIndex));
    meme.put("user", ParseUser.getCurrentUser());
    meme.saveInBackground(new SaveCallback() {
      @Override
      public void done(ParseException e) {
        if (e == null) {
          showMemeSavedToast();
        } else {
          Log.d(TAG, "Failed to save meme: " + e.getMessage());
        }
      }
    });
  }
    
  /**
   * Called when the app starts.
   * 
   * Load all the available photos for the meme background,
   * then setup the rest of the photo-related UI.
   * 
   * You should:
   * - Set the allPhotos member variable to the photos retrieved from Parse
   * - Call setUpPhotoSelectorAndPreview(imageSelect, previewImage)
   * @param imageSelect
   * @param previewImage
   */
  private void getPhotosAndSetUpUI(final Spinner imageSelect, final ParseImageView previewImage) {
    ParseQuery<ParseObject> query = ParseQuery.getQuery("Photo");
    query.addDescendingOrder("createdAt");
    query.setCachePolicy(CachePolicy.CACHE_THEN_NETWORK);
    query.findInBackground(new FindCallback<ParseObject>() {
      @Override
      public void done(List<ParseObject> photos, ParseException e) {
        if (e == null) {
          Log.d(TAG, "Retrived " + photos.size() + " photos");
          allPhotos = photos;
          setUpPhotoSelectorAndPreview(imageSelect, previewImage);
        } else {
          Log.d(TAG, "Error retrieving photos: " + e.getMessage());
        }
      }
    });
  }
  
  /**
   * Called when the app starts.
   * 
   * Displays the login screen if the user is not logged in.
   * To display the login screen, you should call showLoginScreen().
   */
  private void maybeShowLoginScreen() {
    if (ParseUser.getCurrentUser() == null) {
      Log.d(TAG, "User not logged in");
      showLoginScreen();
    } else {
      Log.d(TAG, "User is already logged in");
    }
  }
  
  /**
   * Called when the user clicks the Log out button.
   * 
   * You should log the user out, and then call
   * showLoginScreen().
   */
  private void handleLogOut() {
    ParseUser.logOut();
    showLoginScreen();
  }

  
  /***************** You don't need to change the code after this line ***********************/
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    ParseAnalytics.trackAppOpened(getIntent());  
    
    // Set up the UI elements
    setupUI();

    // If the user is not already logged in, ask them to log in
    maybeShowLoginScreen();
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    
    // We expect the LoginActivity to pass back RESULT_OK if the user actually 
    // logged in.  Otherwise, we assume the user cancelled the login (e.g. by
    // hitting the back button).
    if (requestCode == LOGIN_REQUEST && resultCode != RESULT_OK) {
      // If login failed, exit the app
      Log.d(TAG, "User cancelled login, exiting");
      finish();
    } else {
      Log.d(TAG, "User successfully login");
    }
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu items for use in the action bar
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.layout.main_activity_actions, menu);
      return super.onCreateOptionsMenu(menu);
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      // Handle presses on the action bar items
      switch (item.getItemId()) {
          case R.id.pending_cards:
              openPendingCards();
              return true;
          case R.id.logout:
        	  handleLogOut();
        	  return true;
          default:
              return super.onOptionsItemSelected(item);
      }
  }
  
  public void openPendingCards(){
	  Intent intent = new Intent(this, PendingCardsActivity.class);
	  startActivity(intent);
  }
  public void openProfile(){
	  Intent intent = new Intent(this, ProfileActivity.class);
	  startActivity(intent);
  }
  public void displayQR(String code){
	  Intent intent = new Intent(this, DisplayQRActivity.class);
	  intent.putExtra(QR_CODE,code);
  }
  
  private void setupUI() {
    final Spinner imageSelect = (Spinner) findViewById(R.id.input_image_select);
    final EditText inputTopText = (EditText) findViewById(R.id.input_top_text);
    final EditText inputBottomText = (EditText) findViewById(R.id.input_bottom_text);
    final TextView previewTopText = (TextView) findViewById(R.id.preview_top_text);
    final TextView previewBottomText = (TextView) findViewById(R.id.preview_bottom_text);
    final ParseImageView previewImage = (ParseImageView) findViewById(R.id.preview_meme_image);
    
    Button saveButton = (Button) findViewById(R.id.save_meme_button);
    Button profile = (Button) findViewById(R.id.profile_button);
    for (int i=0; i<10; i++){
    	Button myButton = new Button(this);
    	myButton.setText("Push Me");//TODO

    	LinearLayout ll = (LinearLayout)findViewById(R.id.card_list);
    	LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	ll.addView(myButton, lp);
    	myButton.setOnClickListener(new OnClickListener() {
    	      @Override
    	      public void onClick(View v) {
    	        displayQR("");//TODO
    	      }
    	    });
    }
    
    getPhotosAndSetUpUI(imageSelect, previewImage);
        
    inputTopText.addTextChangedListener(new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        previewTopText.setText(s);
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count,
          int after) {
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });

    inputBottomText.addTextChangedListener(new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        previewBottomText.setText(s);
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count,
          int after) {
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });

    saveButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        handleSave(inputTopText.getText().toString(), 
            inputBottomText.getText().toString());
      }
    });

    
    profile.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          openProfile();
        }
      });
  }
  
  // Sets up the photo selector spinner drop down menu
  // Assumes that you already populated allPhotos with results of a
  // ParseQuery over the Photo class
  private void setUpPhotoSelectorAndPreview(final Spinner imageSelect, final ParseImageView previewImage) {
    
    // Create a list of photo names from the photo ParseObjects
    List<String> photoNames = new ArrayList<String>(allPhotos.size());
    for (ParseObject photo : allPhotos) {
      photoNames.add(photo.getString("name"));
    }

    // Set the spinner items to be backed by the photo name list
    ArrayAdapter<String> imageSelectAdapter = new ArrayAdapter<String>(
        MainActivity.this, android.R.layout.simple_spinner_item,
        photoNames);
    imageSelectAdapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    imageSelect.setAdapter(imageSelectAdapter);
    imageSelect.setOnItemSelectedListener(new OnItemSelectedListener() {

      @Override
      public void onItemSelected(AdapterView<?> parent, View view,
          int pos, long id) {
        Log.d(TAG, "Image position " + pos + " selected");
        currentPhotoIndex = pos;
        
        // Show the image in the ParseImageView
        previewImage
            .setParseFile(allPhotos.get(pos).getParseFile("file"));
        previewImage.loadInBackground(new GetDataCallback() {
          @Override
          public void done(byte[] arg0, ParseException e) {
            if (e == null) {
              Log.d(TAG, "Finished loading meme image");
            } else {
              Log.d(TAG, "Error loading meme image: " + e.getMessage());
            }
          }
        });
      }
      
      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });    
  }  
      
  private void showLoginScreen() {
    Log.d(TAG, "Starting LoginActivity");
    startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_REQUEST);
  }
  
  private void showMemeSavedToast() {
    Toast.makeText(MainActivity.this, R.string.meme_saved_toast_text,
        Toast.LENGTH_LONG).show();    
  }
}

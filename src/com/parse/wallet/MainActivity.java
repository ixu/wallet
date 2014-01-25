package com.parse.wallet;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
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
  
  private TextView emailText;
  private TextView userText;
  private ImageView qrImage;
  
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
    ParseQuery.clearAllCachedResults();
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
      setupUI();
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

  public void displayQR(String code){
	  Intent intent = new Intent(this, DisplayQRActivity.class);
	  intent.putExtra(QR_CODE,code);
  }
  
  private void setupUI() {

    userText = (TextView) findViewById(R.id.user_id);
    emailText = (TextView) findViewById(R.id.email_text);
    qrImage = (ImageView) findViewById(R.id.user_qr);
    


    Button meButton = (Button) findViewById(R.id.me_button);
    if (ParseUser.getCurrentUser() != null) {
	    encode(ParseUser.getCurrentUser().getObjectId());
	    emailText.setText(ParseUser.getCurrentUser().getEmail());
	    userText.setText(R.string.user_text);
    }

    meButton.setOnClickListener(new OnClickListener() {

        @Override

        public void onClick(View v) {

          startActivity(new Intent(MainActivity.this, MeActivity.class));

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
  private void encode(String uniqueID) {
      // TODO Auto-generated method stub
       BarcodeFormat barcodeFormat = BarcodeFormat.QR_CODE;

          int width0 = 500;
          int height0 = 500;

          int colorBack = 0xFF000000;
          int colorFront = 0xFFFFFFFF;

          QRCodeWriter writer = new QRCodeWriter();
          try
          {
              EnumMap<EncodeHintType, Object> hint = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
              //hint.put(EncodeHintType.CHARACTER_SET, "UTF-8");
              BitMatrix bitMatrix = writer.encode(uniqueID, barcodeFormat, width0, height0, hint);
              int width = bitMatrix.getWidth();
              int height = bitMatrix.getHeight();
              int[] pixels = new int[width * height];
              for (int y = 0; y < height; y++)
              {
                  int offset = y * width;
                  for (int x = 0; x < width; x++)
                  {

                      pixels[offset + x] = bitMatrix.get(x, y) ? colorBack : colorFront;
                  }
              }

              Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
              bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
              ImageView imageview = (ImageView)findViewById(R.id.user_qr);
              imageview.setImageBitmap(bitmap);
          } catch (WriterException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }
  }
}

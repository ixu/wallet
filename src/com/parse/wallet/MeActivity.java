package com.parse.wallet;

import java.util.EnumMap;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MeActivity extends Activity {
  private static final String TAG = "MeActivity";

  // UI elements for the meme shown

  private TextView cardnameText;
  private TextView expText;
  
  
  // All memes created by this user
  private List<ParseObject> myCards;
  
  // The index of the current meme shown
  private int currentCardIndex = 0;
  
  /**
   * @return the email of the current user, empty string otherwise
   */
  private String getUserEmail() {
    if (ParseUser.getCurrentUser() != null) {
      return ParseUser.getCurrentUser().getEmail();
    } else {
      return "";
    }
  }

  /**
   * Retrieve the memes, put them in the myMemes member variable,
   * and call updateMeme(0) to show the first meme.  If retrieving
   * memes fails, call showFailedToGetMemeToast().
   */
  private void getMemesAndDisplayFirst() {
    ParseQuery<ParseObject> query = ParseQuery.getQuery("UserCard");
    query.whereEqualTo("user", ParseUser.getCurrentUser());
    query.addDescendingOrder("createdAt");
    query.include("card");
    query.findInBackground(new FindCallback<ParseObject>() {
      public void done(List<ParseObject> usercards, ParseException e) {
        if (e == null) {
          Log.d(TAG, "Retrieved " + usercards.size() + " cards");
          myCards = usercards;
          if (usercards.size() > 0) {
            updateCard(0);
          }
        } else {
          Log.d(TAG, "Error retrieving cards: " + e.getMessage());
          showFailedToGetMemeToast();
        }
      }
    });
  }
  
  /**
   * Called when we initially display the first meme, or 
   * when the user clicks the next button.
   * 
   * Update the meme shown to the one at the current index in myMemes.
   * @param index
   */
  private void updateCard(int index) {
	Log.v("index", Integer.toString(index));
    ParseObject currentCard = myCards.get(index);
    String cardId = currentCard.getObjectId();
    encode(cardId);
    Log.v("cardid", cardId);
    cardnameText.setText(currentCard.getParseObject("card").getString("name"));
    expText.setText(currentCard.getString("exp"));

  }  
    
  /***************** You don't need to change the code after this line ***********************/
  
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.me);

    cardnameText = (TextView) findViewById(R.id.cardname_text);
    expText = (TextView) findViewById(R.id.exp_text);
    Button nextButton = (Button) findViewById(R.id.next_meme_button);

    getMemesAndDisplayFirst();
    
    nextButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (myCards != null && myCards.size() > 0) {
          currentCardIndex++;
          updateCard(currentCardIndex % myCards.size());
        }
      }
    });
  }
  
  private void showFailedToGetMemeToast() {
    Toast.makeText(MeActivity.this, 
        R.string.failed_to_get_meme_toast_text, Toast.LENGTH_LONG).show();
  }
  
  private void encode(String uniqueID) {
      // TODO Auto-generated method stub
       BarcodeFormat barcodeFormat = BarcodeFormat.QR_CODE;

          int width0 = 500;
          int height0 = 500;

          int colorBack = 0x00000000;
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
              ImageView imageview = (ImageView)findViewById(R.id.carousel_meme_image);
              imageview.setImageBitmap(bitmap);
          } catch (WriterException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
          }
  }
}

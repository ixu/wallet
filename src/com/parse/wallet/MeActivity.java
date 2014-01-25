package com.parse.wallet;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MeActivity extends Activity {
  private static final String TAG = "MeActivity";

  // UI elements for the meme shown
  private TextView topText;
  private TextView bottomText;
  private ParseImageView memeImage;
  
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
    ParseObject currentCard = myCards.get(index);
    memeImage.setParseFile(currentCard.getParseObject("photo").getParseFile(
        "file"));
    topText.setText(currentCard.getParseObject("card").getString("name"));
    bottomText.setText(currentCard.getString("exp"));
    memeImage.loadInBackground(new GetDataCallback() {
      @Override
      public void done(byte[] arg0, ParseException e) {
        if (e == null) {
          Log.d(TAG, "Finished loading meme image");
        } else {
          Log.d(TAG, "Error retrieving memes image: " + e.getMessage());
        }
      }
    });
  }  
    
  /***************** You don't need to change the code after this line ***********************/
  
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.me);

    TextView emailText = (TextView) findViewById(R.id.email_text);
    topText = (TextView) findViewById(R.id.carousel_top_text);
    bottomText = (TextView) findViewById(R.id.carousel_bottom_text);
    memeImage = (ParseImageView) findViewById(R.id.carousel_meme_image);
    Button nextButton = (Button) findViewById(R.id.next_meme_button);

    emailText.setText(getUserEmail());
    getMemesAndDisplayFirst();
    
    nextButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (myCards != null && myCards.size() > 0) {
          currentMemeIndex++;
          updateMeme(currentMemeIndex % myMemes.size());
        }
      }
    });
  }
  
  private void showFailedToGetMemeToast() {
    Toast.makeText(MeActivity.this, 
        R.string.failed_to_get_meme_toast_text, Toast.LENGTH_LONG).show();
  }
}

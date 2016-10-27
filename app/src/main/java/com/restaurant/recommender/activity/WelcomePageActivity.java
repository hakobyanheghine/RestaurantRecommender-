package com.restaurant.recommender.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.restaurant.recommender.R;
import com.restaurant.recommender.RestaurantRecommender;
import com.restaurant.recommender.backend.API;
import com.restaurant.recommender.data.ItemData;
import com.restaurant.recommender.manager.UserDataManager;
import com.restaurant.recommender.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class WelcomePageActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_welcome_page);
		
		((ProfilePictureView) findViewById(R.id.profile_picture)).setProfileId(UserDataManager.$().userData.fbId);
		String name = UserDataManager.$().userData.firstName + " " + UserDataManager.$().userData.lastName;
		((TextView)findViewById(R.id.name)).setText(name);
		((TextView)findViewById(R.id.address)).setText(UserDataManager.$().userData.location);
		
		Button sadMoodButton = (Button) findViewById(R.id.mood_melancholy_button);
		sadMoodButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getMoodRecommendation(Constants.MOOD_RECOMMENDATION_TYPE_SAD);
			}
		});
		
		Button musicMoodButton = (Button) findViewById(R.id.mood_music_button);
		musicMoodButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getMoodRecommendation(Constants.MOOD_RECOMMENDATION_TYPE_MUSIC);
			}
		});
		
		Button danceMoodButton = (Button) findViewById(R.id.mood_dance_button);
		danceMoodButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getMoodRecommendation(Constants.MOOD_RECOMMENDATION_TYPE_DANCE);
			}
		});
		
		Button coffeeMoodButton = (Button) findViewById(R.id.mood_coffee_button);
		coffeeMoodButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getMoodRecommendation(Constants.MOOD_RECOMMENDATION_TYPE_COFFEE);
			}
		});
		
		Button foodMoodButton = (Button) findViewById(R.id.mood_food_button);
		foodMoodButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				getMoodRecommendation(Constants.MOOD_RECOMMENDATION_TYPE_FOOD);
			}
		});
	}
	
	public void getMoodRecommendation(final String moodType) {
		if (UserDataManager.$().hasMoodTypeRecommendations(moodType)) {
			WelcomePageActivity.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					RestaurantRecommender.$().roActivity.startRecommendationsActivity(moodType);
				}
			});
		} else {
			API.getMoodRecommendations(moodType, new JsonHttpResponseHandler() {
				
				@Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        JSONArray recommenderItemsJson = response.getJSONArray("recommendations");
                        ArrayList<ItemData> moodRecommendations = new ArrayList<ItemData>();
                        for (int i = 0; i < recommenderItemsJson.length(); i++) {
                            ItemData item = new ItemData(recommenderItemsJson.getJSONObject(i));
                            moodRecommendations.add(item);
                        }

                        UserDataManager.$().moodRecommendationsData.put(moodType, moodRecommendations);
                        WelcomePageActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                RestaurantRecommender.$().roActivity.startRecommendationsActivity(moodType);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
				}
			});
		}
	}
}

package com.restaurant.recommender.manager;

import android.util.Log;
import android.util.SparseArray;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.restaurant.recommender.RestaurantRecommender;
import com.restaurant.recommender.backend.API;
import com.restaurant.recommender.data.FacebookPageData;
import com.restaurant.recommender.data.ItemData;
import com.restaurant.recommender.data.ItemReviewData;
import com.restaurant.recommender.data.UserData;
import com.restaurant.recommender.utils.Constants;
import com.restaurant.recommender.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class UserDataManager {
	
	private static UserDataManager instance;
	
	private UserDataManager() {
		
	}
	
	public static UserDataManager $() {
		if (instance == null) {
			instance = new UserDataManager();
		}
		
		return instance;
	}
	
	public UserData userData;
	public ArrayList<ItemData> recommendationsData = new ArrayList<ItemData>();
	public HashMap<String, ArrayList<ItemData>> moodRecommendationsData = new HashMap<>();
	public SparseArray<ArrayList<ItemReviewData>> itemRatings = new SparseArray<>();
	
	public HashMap<String, FacebookPageData> userRestaurantPages = new HashMap<>();
	
	public void getUserLikedRestaurants(JSONObject pagesJson) {
		if (pagesJson.has("data")) {
			try {
				String pageType;
				JSONArray pagesJsonArray = pagesJson.getJSONArray("data");
				for (int i = 0; i < pagesJsonArray.length(); i++) {
					pageType = pagesJsonArray.getJSONObject(i).optString("type", "");
					if (Utils.isPageTypeRestaurant(pageType)) {
						FacebookPageData pageData = new FacebookPageData(pagesJsonArray.getJSONObject(i));
						userRestaurantPages.put(pageData.pageId, pageData);
						Log.d("heghine", "page_id = " + pageData.pageId + " ; type = " + pageData.type);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} 
	}
	
	public boolean hasLikedRestaurantPages() {
		return userRestaurantPages.size() != 0;
	}
	
	public void updateUserLikedPageData(JSONObject pagesJson) {
		if (pagesJson.has("data")) {
			try {
				String pageId = "";
				JSONArray pagesJsonArray = pagesJson.getJSONArray("data");
				for (int i = 0; i < pagesJsonArray.length(); i++) {
					pageId = pagesJsonArray.getJSONObject(i).optString("page_id", "");
					userRestaurantPages.get(pageId).updateData(pagesJsonArray.getJSONObject(i));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setUserLikedPageDataInBackend() {
		if (hasLikedRestaurantPages()) {
			try {
				String preferences = Utils.getUserPreferenceString();
				Log.d("heghine", "user_preferences = " + preferences);
				API.setUserPreferences(preferences, new JsonHttpResponseHandler() {
					
					@Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
						boolean status = response.optInt("status", 0) != 0;
						if (status) {
							getRecommendations();
						}
					}

				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void getRecommendations() {
		API.getRecommendations(Constants.RECOMMENDATION_TYPE_PREDICTION, new JsonHttpResponseHandler() {
			
			@Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray recommenderItemsJson = null;
                try {
                    recommenderItemsJson = response.getJSONArray("values");

                    for (int i = 0; i < recommenderItemsJson.length(); i++) {
                        ItemData item = new ItemData(recommenderItemsJson.getJSONObject(i));
                        recommendationsData.add(item);
                    }
                    if (recommendationsData.size() > 0) {
                        RestaurantRecommender.$().roActivity.startRecommendationsActivity("");
                    } else {
                        RestaurantRecommender.$().roActivity.startWelcomePageActivity();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

		});
	}
	
	public ItemData getItemDataById(int itemId) {
		for (int i = 0; i < recommendationsData.size(); i++) {
			if (recommendationsData.get(i).itemId == itemId) {
				return recommendationsData.get(i);
			}
		}
		
		for (String moodType : moodRecommendationsData.keySet()) {
			for (int i = 0; i < moodRecommendationsData.get(moodType).size(); i++) {
				if (moodRecommendationsData.get(moodType).get(i).itemId == itemId) {
					return moodRecommendationsData.get(moodType).get(i);
				}
			}
		}
		
		return null;
	}
	
	public boolean hasMoodTypeRecommendations(String type) {
		return moodRecommendationsData.get(type) != null;
	}
	
	public boolean hasReviewedItem(int itemId) {
		if (itemRatings.get(itemId) != null) {
			for (int i = 0; i < itemRatings.get(itemId).size(); i++) {
				if (itemRatings.get(itemId).get(i).userId.equals(userData.userId)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void clearUserData() {
		userData = null;
		userRestaurantPages.clear();
		recommendationsData.clear();
		itemRatings.clear();
		PreferenceManager.$().setUserId("0");
	}
}

package com.restaurant.recommender.backend;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.restaurant.recommender.utils.Constants;

public class API {
	public static final String ADD_NEW_USER = "add_new_user";
	public static final String SET_USER_PREFERENCES = "set_user_preferences";
	public static final String GET_RECOMMENDATIONS = "get_recommendations";
	public static final String GET_MOOD_RECOMMENDATIONS = "get_mood_recommendations";
	public static final String GET_ITEM_REVIEWS = "get_item_reviews";
	public static final String SET_ITEM_REVIEW = "set_item_review";
	
	public static final String TAG = "API";
	public static final String secret = "0lymp#@creature5";
	public static final int appVersion = 1;

    public static String userId = "";
    public static String userFbId = "";

    private static AsyncHttpClient client = new AsyncHttpClient();

	public static void addNewUser(String userId, String userFbId, String firstName, String lastName, int gender, String location, AsyncHttpResponseHandler observer) {
		API.userId = userId;
		API.userFbId = userFbId;

		sendRequestAsync(ADD_NEW_USER, "first_name=" + firstName + "&last_name=" + lastName + "&g=" + gender + "&location=" + location, observer);
	}

	public static void setUserPreferences(String preferences, AsyncHttpResponseHandler observer) {
        RequestParams params = new RequestParams();
		params.add("preferences", preferences);
		sendRequestAsync(SET_USER_PREFERENCES, params, observer);
	}
	
	public static void getRecommendations(int type, AsyncHttpResponseHandler observer) {
		sendRequestAsync(GET_RECOMMENDATIONS, "type=" + type, observer);
	}
	
	public static void getMoodRecommendations(String type, AsyncHttpResponseHandler observer) {
		sendRequestAsync(GET_MOOD_RECOMMENDATIONS, "type=" + type, observer);
	}
	
	public static void getItemReviews(int itemId, AsyncHttpResponseHandler observer) {
		sendRequestAsync(GET_ITEM_REVIEWS, "item_id=" + itemId, observer);
	} 
	
	public static void setItemReview(int itemId, int rating, String reviewText, String itemType, AsyncHttpResponseHandler observer) {
        RequestParams params = new RequestParams();
		params.add("item_id", String.valueOf(itemId));
		params.add("rating", String.valueOf(rating));
		params.add("review_text", reviewText);
		params.add("item_type", itemType);
		
		sendRequestAsync(SET_ITEM_REVIEW, params, observer);
	}
	
	private static void sendRequestAsync(String command, String params, AsyncHttpResponseHandler responseHandler) {
		String paramsString = "&user_id=" + userId + "&user_fb_id=" + userFbId;
		if (params != null && !params.equals("")) {
			paramsString = paramsString + "&" + params;
		}
		String requestStr = Constants.SERVER_URL + "t=" + command + paramsString;

        RequestParams parameters = new RequestParams();

        client.post(requestStr, parameters, responseHandler);
	}
	
	private static void sendRequestAsync(String command, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		params.add("user_id", userId);
		params.add("user_fb_id", userFbId);
		String requestStr = Constants.SERVER_URL + "t=" + command;

        client.post(requestStr, params, responseHandler);
	}
}
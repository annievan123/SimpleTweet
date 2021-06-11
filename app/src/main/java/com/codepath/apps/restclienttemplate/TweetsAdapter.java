package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;


import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final String TAG = "TweetsAdapter";

    Context context;
    List<Tweet> tweets;

    Tweet myTweet;

    // Pass in the context and list of tweets
    // Constructor
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    // For each row, inflate the layout for a tweet
    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view); // refers to the inner class view holder below
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        // Get the data at position
        Tweet tweet = tweets.get(position);

        // Bind the tweet with the viewholder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvCreatedAt;
        ImageView ivMedia;
        EditText etReply;
        TextView tvReply;

        TwitterClient client;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            etReply = (EditText) itemView.findViewById(R.id.etReply);
            tvReply = itemView.findViewById(R.id.tvReply);

        }

        public void bind(Tweet tweet) {
            client = TwitterApp.getRestClient(context);

            //take out the different attributes of tweet and use it to fill in the view on screen
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
            tvCreatedAt.setText(
                    getRelativeTimeAgo(tweet.createdAt));
            if (tweet.url == ""){
                ivMedia.setVisibility(View.GONE);
            }
            else{
                GlideApp.with(context).load(tweet.url).into(ivMedia);
            }
            tvReply.setVisibility(View.GONE);

            myTweet = tweet;
            etReply.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // If the event is a key-down event on the "enter" button
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        System.out.println(etReply.getText().toString());
                        populateReply(etReply.getText().toString(), myTweet.id, myTweet.user.screenName);
                        String temp = etReply.getText().toString();
                        etReply.getText().clear();
                        tvReply.setText(temp);
                        tvReply.setVisibility(View.VISIBLE);

                        return true;
                    }
                    return false;
                }
            });


            GlideApp.with(context).load(tweet.user.profileImageUrl).into(ivProfileImage);
        }


        private void populateReply(String reply, Integer id, String usr){
            client.sendReply(reply, id, usr, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    Log.i("TweetsAdapter", "onSuccess!" + json.toString());
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

                }
            });

        }
    }


    public String getRelativeTimeAgo(String rawJsonDate){
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (ParseException e) {
            Log.i(TAG, "getRelativeTimeAgo failed");
            e.printStackTrace();
        }

        return "";
    }
}

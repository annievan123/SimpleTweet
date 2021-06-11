package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {
    EditText etCompose;
    Button btnTweet;

    TwitterClient client;

    public static final int MAX_TWEET_LENGTH = 140;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);

        client = TwitterApp.getRestClient(this);

        // Add listener on the button
        btnTweet.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()){
                    Toast.makeText(ComposeActivity.this, "Sorry your tweet cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tweetContent.length() > MAX_TWEET_LENGTH){
                    Toast.makeText(ComposeActivity.this, "Sorry your tweet is too long", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(ComposeActivity.this, tweetContent, Toast.LENGTH_LONG).show();
                populateHomeTimeline(tweetContent);

            }
        });
        // Make an API call to Twitter to publish the tweet
    }

    private void populateHomeTimeline(String msg){
        client.sendTweet(msg, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i("ComposeActivity", "onSuccess!" + json.toString());

                try {
                    // launch a subactivity and  when the subactivity is complete return the result to parent
                    Tweet jsonArray = (Tweet) Tweet.fromJsonArray(json.jsonArray);
                    Intent intent = new Intent();
                    intent.putExtra("tweet", Parcels.wrap(jsonArray));
                    setResult(RESULT_OK, intent);
                    System.out.print("hiiii");
                    finish();

                } catch (JSONException e) {
                    Log.e("ComposeActivity","JSON EXCEPTION", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

            }
        });

    }
}
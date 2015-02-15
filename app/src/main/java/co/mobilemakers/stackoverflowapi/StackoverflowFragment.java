package co.mobilemakers.stackoverflowapi;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class StackoverflowFragment extends Fragment {
    Button mButtonLoad;
    TextView mTextViewUsers, mTextViewComments, mTextViewAnswers, mTextViewApiRev;

    public StackoverflowFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stackoverflow, container, false);
        wireUpViews(rootView);
        prepareButton(rootView);
        return rootView;
    }

    private void wireUpViews(View rootView) {
        mTextViewAnswers = (TextView)rootView.findViewById(R.id.text_view_answers_data);
        mTextViewUsers = (TextView)rootView.findViewById(R.id.text_view_users_data);
        mTextViewComments = (TextView)rootView.findViewById(R.id.text_view_comments_data);
        mTextViewApiRev = (TextView)rootView.findViewById(R.id.text_view_api_rev_data);
    }

    private void prepareButton(View rootView) {
        mButtonLoad = (Button)rootView.findViewById(R.id.button_load);
        mButtonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchStatistics();
            }
        });
    }

    private void fetchStatistics(){

        try {
            URL url = constructQuery();
            Request request = new Request.Builder().url(url.toString()).build();
            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String responseString = response.body().string();
                    final String[] info = parseResponse(responseString).split(",");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextViewUsers.setText(info[0]);
                            mTextViewComments.setText(info[1]);
                            mTextViewAnswers.setText(info[2]);
                            mTextViewApiRev.setText(info[3]);
                        }
                    });
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String parseResponse(String response) {
        final String TOTAL_USERS = "total_users";
        final String TOTAL_COMMENTS = "total_comments";
        final String TOTAL_ANSWRES = "total_answers";
        final String API_REVISION = "api_revision";
        final String ARRAY_NAME = "items";

        List<String> data = new ArrayList<>();

        try {
            JSONObject respObject = new JSONObject(response);
            JSONArray arrayJson = respObject.getJSONArray(ARRAY_NAME);
            respObject = (JSONObject)arrayJson.get(0);
            data.add(respObject.get(TOTAL_USERS).toString());
            data.add(respObject.get(TOTAL_COMMENTS).toString());
            data.add(respObject.get(TOTAL_ANSWRES).toString());
            data.add(respObject.get(API_REVISION).toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return TextUtils.join(",", data);
    }

    private URL constructQuery() throws MalformedURLException {
        final String BASE_URL       = "api.stackexchange.com";
        final String API_VERSION    = "2.2";
        final String REPOS_ENDPOINT = "info";
        final String KEY            = "site";
        final String VALUE          = "stackoverflow";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https").
                authority(BASE_URL).
                appendPath(API_VERSION).
                appendPath(REPOS_ENDPOINT).
                appendQueryParameter(KEY,VALUE);

        Uri uri = builder.build();
        Log.d("URL", uri.toString());
        return new URL(uri.toString());
    }

}

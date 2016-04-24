package com.nasa_sac.aerosoul.aerosoul;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.Rating;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.nasa_sac.aerosoul.aerosoul.utilities.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient googleApiClient;

    HashMap<String, Integer> rating = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if(googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        Button buttonTrendAnalysis = (Button) findViewById(R.id.showTrendAnalysis);
        try {
            URL url = new URL(AppConstants.API_URL);
            new GetJSONArray().execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if(buttonTrendAnalysis != null) {
            buttonTrendAnalysis.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoTrendAnalysis();
                }
            });
        }

        String[] symptoms = new String[]{"Cough", "Sneezing", "Head Ache",
                "Itching", "Drowsy", "Nausea"};
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.height = 125;

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewRow;
        TextView symptomText;
        TextView symptomId;
        RatingBar ratingBar;
        LinearLayout layout;
        int i = 1;
        for (String symptom : symptoms) {
            viewRow = inflater.inflate(R.layout.symptom_rate, null);
            if (viewRow != null) {
                ratingBar = (RatingBar) viewRow.findViewById(R.id.symptomRate);
                symptomId = (TextView) viewRow.findViewById(R.id.symptomId);
                symptomText = (TextView) viewRow.findViewById(R.id.symptomText);

                if (ratingBar != null && symptomText != null && symptomId != null) {
                    symptomId.setText(symptom);
                    symptomText.setText(symptom);
                    ratingBar.setRating(i++ % 5);
                }

                layout = (LinearLayout) findViewById(R.id.knownSymptoms);
                if (layout != null) {
                    layout.addView(viewRow, layoutParams);
                }

                viewRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleClick(v);
                    }
                });
            }
        }
    }

    private void gotoTrendAnalysis() {
        Intent trendAnalysisIntent = new Intent(this, TrendAnalysisActivity.class);
        startActivity(trendAnalysisIntent);
        finish();
    }

    private void handleClick(View v) {
        TextView textViewId = (TextView) v.findViewById(R.id.symptomId);

        if (textViewId != null) {
            final String id = textViewId.getText().toString();
            final Dialog dialog = new Dialog(this);
            final RatingBar ratingBar;

            dialog.setCancelable(true);
            dialog.setContentView(R.layout.selectrating);
            dialog.show();
            ratingBar = (RatingBar) dialog.findViewById(R.id.ratingBarSelectRate);
            if(ratingBar != null && dialog != null) {
                Log.d("RatingBar",ratingBar.toString());
                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        setRating(id, (int) rating);
                    }
                });
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Toast.makeText(getApplicationContext(), "Rated " +id +": " +rating.get(id), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    private void setRating(String id, int rating) {
        this.rating.put(id, rating);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            Toast.makeText(this, "Your location: " +location.getLatitude() +", " +location.getLongitude(), Toast.LENGTH_SHORT).show();

        }catch (SecurityException e) {
            Toast.makeText(this, "Error: " +e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Error: Cannot connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Error: Connection Failed", Toast.LENGTH_SHORT).show();
    }

    private class GetJSONArray extends AsyncTask<URL, Integer, JSONArray> {
        String response = "";

        @Override
        protected JSONArray doInBackground(URL... urls) {
            if (urls.length == 1) {
                URL url = urls[0];
                try {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    int next = inputStream.read();
                    while (next != -1) {
                        response += (char) next;
                        next = inputStream.read();
                    }
                    try {
                        return new JSONArray(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray results) {
            try {
                if(results != null && results.length() > 0) {
                    String aqi = results.getJSONObject(0).getString("aqi");
                    updateAQI(aqi);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void afterResponseJSONArray(URL requestUrl, JSONArray jsonArray) {
        if(requestUrl.getPath().equals(AppConstants.API_URL)) {

        }
    }

    private void updateAQI(String aqi) {
        TextView textView = (TextView) findViewById(R.id.textAPI);
        if (textView != null) {
            textView.setText(aqi);
        }
    }

    private void updateTemperature(String temp) {
        TextView textView = (TextView) findViewById(R.id.textTemperature);
        if (textView != null) {
            textView.setText(temp);
        }
    }

    private void updateHumidity(String humidity) {
        TextView textView = (TextView) findViewById(R.id.textHumidity);
        if (textView != null) {
            textView.setText(humidity);
        }
    }
}

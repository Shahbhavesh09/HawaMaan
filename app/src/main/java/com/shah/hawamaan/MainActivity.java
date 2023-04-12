package com.shah.hawamaan;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {
    private OpenWeatherMapTask weatherTask;
    TextView temperature, humidity, pressure, windSpeed, sunrise, sunset,cityName, description;
    ImageView weatherIcon, search_icon;
    EditText search_city_name;
    String MainCityName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create an instance of the AsyncTask

        cityName = findViewById(R.id.city_name);
        temperature = findViewById(R.id.temperature);
        description = findViewById(R.id.description);
        windSpeed = findViewById(R.id.windspeed);
        humidity = findViewById(R.id.humidity);
        sunrise = findViewById(R.id.Sunrise);
        pressure = findViewById(R.id.Pressure);
        sunset = findViewById(R.id.sunset);
        weatherIcon = findViewById(R.id.api_cloud_icon);
        search_icon = findViewById(R.id.search);
        search_city_name = findViewById(R.id.search_city_name);
        //validate the city name for empty string and if not then call execute by passing the city name
        search_icon.setOnClickListener(v -> {
            if (search_city_name.getText().toString().trim().isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
            } else {
                MainCityName = search_city_name.getText().toString();
                weatherTask = new OpenWeatherMapTask(this);
                weatherTask.execute(MainCityName.trim());
            }
        });
        search_city_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (search_city_name.getText().toString().trim().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                    } else {
                        MainCityName = search_city_name.getText().toString();
                        weatherTask = new OpenWeatherMapTask(MainActivity.this);
                        weatherTask.execute(MainCityName.trim());
                    }
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(search_city_name.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        // Call the execute method to start the task
    }
}

class OpenWeatherMapTask extends AsyncTask<String, Void, String> {
    private String apiKey = "e5163e36cb1b8dc5e54b30b608f811bd";
    private MainActivity activity;
    public OpenWeatherMapTask(MainActivity activity) {
        this.activity = activity;
    }
    @Override
    protected String doInBackground(String... params) {
        try {
            URL url = new URL("https://api.openweathermap.org/data/2.5/weather?q=" + params[0] + "&appid=" + apiKey);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onPostExecute(String result) {
        TextView details;

        if (result != null)  {
            /*Log.d("Details", result);*/
            setData(result);
        } else {
            Toast.makeText(activity, "No Data Found..!", Toast.LENGTH_LONG).show();
        }
    }

    private void setData(String result) {
        try {
            //setting Description
            JSONObject response = new JSONObject(result); // replace 'jsonResponse' with your JSON string
            JSONArray weatherArray = response.getJSONArray("weather");
            JSONObject weatherObject = weatherArray.getJSONObject(0);
            String description = weatherObject.getString("description");

            //country code
            JSONObject sys = response.getJSONObject("sys");
            String countryCode = sys.getString("country");

            //Setting city name
            String city_name = response.getString("name");

            //Setting Temperature// assuming 'response' is the JSON string
            JSONObject main = response.getJSONObject("main"); // get the 'main' object
            double temp = main.getDouble("temp");
            temp = temp - 273.15;
            String formattedTemperature = Double.toString(temp).replaceAll("\\..*", "");
            System.out.println(formattedTemperature);

            //Setting Humidity
            int humidity = main.getInt("humidity");

            //Setting Pressure
            double pressure = main.getInt("pressure");
            String formattedPressure = Double.toString(pressure).replaceAll("\\..*", "");

            //Setting Wind Speed
            JSONObject wind = response.getJSONObject("wind");
            double windSpeed = wind.getDouble("speed");
            double windSpeedInMph = windSpeed * 2.23694; // convert wind speed from meters per second to miles per hour
            String formattedWindSpeed = Double.toString(windSpeed).replaceAll("\\..*", "");
            DecimalFormat df = new DecimalFormat("#");
            formattedWindSpeed = df.format(windSpeedInMph);

            //Setting Sunrise
            sys = response.getJSONObject("sys");
            long sunrise = sys.getLong("sunrise");
            long sunset = sys.getLong("sunset");

            LocalDateTime sunriseDateTime = null,sunsetDateTime = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                sunriseDateTime = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(sunrise),
                        ZoneId.systemDefault());
                sunsetDateTime = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(sunset),
                        ZoneId.systemDefault());
            }
            String sunriseTime = null,sunsetTime = null;
            // Format the LocalDateTime object into 12-hour format
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                sunriseTime = sunriseDateTime.format(
                        DateTimeFormatter.ofPattern("h:mm a"));
                sunsetTime = sunsetDateTime.format(
                        DateTimeFormatter.ofPattern("h:mm a"));
            }

            String icon = weatherObject.getString("icon");
            String iconUrl = "https://openweathermap.org/img/wn/"+icon+"@4x"+".png";
            Glide.with(activity)
                    .load(iconUrl)
                    .into(activity.weatherIcon);

            activity.cityName.setText(city_name.toUpperCase()+", "+countryCode.toUpperCase());
            activity.temperature.setText(formattedTemperature+"°C");
            activity.description.setText(description.toUpperCase());
            activity.humidity.setText(String.valueOf(humidity+"%"));
            activity.pressure.setText(String.valueOf(formattedPressure+"hPa"));
            activity.windSpeed.setText(String.valueOf(formattedWindSpeed+"km/h"));
            activity.sunrise.setText(sunriseTime);
            activity.sunset.setText(sunsetTime);


        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}

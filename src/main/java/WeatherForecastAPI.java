import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WeatherForecastAPI {
    private static final String API_KEY = "";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/forecast";

    public static List<WeatherData> getForecast(String city) {
        try {
            String urlString = API_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric&lang=ru";
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray forecastList = jsonResponse.getJSONArray("list");

                List<WeatherData> dailyForecasts = new ArrayList<>();
                LocalDateTime previousDate = null;

                for (int i = 0; i < forecastList.length(); i++) {
                    JSONObject forecastItem = forecastList.getJSONObject(i);
                    LocalDateTime forecastTime = LocalDateTime.parse(
                            forecastItem.getString("dt_txt"),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    );

                    if (forecastTime.getHour() == 12 &&
                            (previousDate == null || !forecastTime.toLocalDate().equals(previousDate.toLocalDate()))) {

                        WeatherData weatherData = new WeatherData();

                        JSONObject main = forecastItem.getJSONObject("main");
                        weatherData.setTemperature(main.getDouble("temp"));
                        weatherData.setFeelsLike(main.getDouble("feels_like"));
                        weatherData.setHumidity(main.getInt("humidity"));
                        weatherData.setPressure(main.getInt("pressure"));

                        JSONObject wind = forecastItem.getJSONObject("wind");
                        weatherData.setWindSpeed(wind.getDouble("speed"));
                        weatherData.setWindDegree(wind.getInt("deg"));

                        JSONObject clouds = forecastItem.getJSONObject("clouds");
                        weatherData.setCloudiness(clouds.getInt("all"));

                        JSONObject weather = forecastItem.getJSONArray("weather").getJSONObject(0);
                        weatherData.setDescription(weather.getString("description"));
                        weatherData.setMain(weather.getString("main"));
                        weatherData.setForecastDate(forecastTime);

                        dailyForecasts.add(weatherData);
                        previousDate = forecastTime;
                    }
                }

                return dailyForecasts;
            } else {
                System.out.println("Ошибка HTTP: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            System.out.println("Ошибка при получении прогноза погоды: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WeatherAPI {
    private static final String API_KEY = "";
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";

    public static WeatherData getWeather(String city) {
        try {
            String urlString = API_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";
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

                WeatherData weatherData = new WeatherData();

                JSONObject main = jsonResponse.getJSONObject("main");
                weatherData.setTemperature(main.getDouble("temp"));
                weatherData.setFeelsLike(main.getDouble("feels_like"));
                weatherData.setHumidity(main.getInt("humidity"));
                weatherData.setPressure(main.getInt("pressure"));

                JSONObject wind = jsonResponse.getJSONObject("wind");
                weatherData.setWindSpeed(wind.getDouble("speed"));
                weatherData.setWindDegree(wind.getInt("deg"));

                JSONObject clouds = jsonResponse.getJSONObject("clouds");
                weatherData.setCloudiness(clouds.getInt("all"));

                JSONObject weather = jsonResponse.getJSONArray("weather").getJSONObject(0);
                weatherData.setDescription(weather.getString("description"));
                weatherData.setMain(weather.getString("main"));

                return weatherData;
            } else {
                System.out.println("Ошибка HTTP: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            System.out.println("Ошибка при получении данных о погоде: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherData {
    private double temperature;
    private double feelsLike;
    private int humidity;
    private int pressure;
    private double windSpeed;
    private int windDegree;
    private int cloudiness;
    private String description;
    private String main;
    private LocalDateTime forecastDate;

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getWindDegree() {
        return windDegree;
    }

    public void setWindDegree(int windDegree) {
        this.windDegree = windDegree;
    }

    public int getCloudiness() {
        return cloudiness;
    }

    public void setCloudiness(int cloudiness) {
        this.cloudiness = cloudiness;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public LocalDateTime getForecastDate() {
        return forecastDate;
    }

    public void setForecastDate(LocalDateTime forecastDate) {
        this.forecastDate = forecastDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (forecastDate != null) {
            sb.append("📅 Дата: ")
                    .append(forecastDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    .append("\n");
        }

        sb.append("🌡️ Температура: ").append(String.format("%.1f", temperature)).append("°C\n")
                .append("🌬️ Ощущается как: ").append(String.format("%.1f", feelsLike)).append("°C\n")
                .append("💧 Влажность: ").append(humidity).append("%\n")
                .append("📊 Давление: ").append(pressure).append(" гПа\n")
                .append("🍃 Ветер: ").append(String.format("%.1f", windSpeed)).append(" м/с, ").append(getWindDirection(windDegree)).append("\n")
                .append("☁️ Облачность: ").append(cloudiness).append("%\n")
                .append("📝 Описание: ").append(translateWeatherDescription(description));

        return sb.toString();
    }

    private String getWindDirection(int degrees) {
        String[] directions = {"С", "СВ", "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ"};
        int index = (int)((degrees + 22.5) % 360 / 45);
        return directions[index];
    }

    private String translateWeatherDescription(String description) {
        switch (description.toLowerCase()) {
            case "clear sky": return "Ясное небо";
            case "few clouds": return "Небольшая облачность";
            case "scattered clouds": return "Рассеянные облака";
            case "broken clouds": return "Значительная облачность";
            case "overcast clouds": return "Пасмурно";
            case "light rain": return "Легкий дождь";
            case "moderate rain": return "Умеренный дождь";
            case "heavy rain": return "Сильный дождь";
            case "thunderstorm": return "Гроза";
            case "snow": return "Снег";
            case "light snow": return "Небольшой снег";
            case "heavy snow": return "Сильный снег";
            case "mist": return "Легкий туман";
            case "fog": return "Туман";
            case "haze": return "Легкая дымка";
            default: return description;
        }
    }
}
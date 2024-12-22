import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseManager {
    private static final String HOST = "localhost";
    private static final int PORT = 5432;
    private static final String DATABASE = "";
    private static final String USER = "";
    private static final String PASSWORD = "";

    private static Connection getConnection() throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%d/%s", HOST, PORT, DATABASE);

        Properties props = new Properties();
        props.setProperty("user", USER);
        props.setProperty("password", PASSWORD);
        props.setProperty("characterEncoding", "UTF-8");
        props.setProperty("useUnicode", "true");
        props.setProperty("ssl", "false");

        try {
            return DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            System.err.println("Детали ошибки подключения:");
            System.err.println("URL: " + url);
            System.err.println("Пользователь: " + USER);
            throw e;
        }
    }

    private static void createTablesIfNotExists(Connection conn) throws SQLException {
        String createWeatherRequestsTableSQL = """
        CREATE TABLE IF NOT EXISTS weather_requests (
            id SERIAL PRIMARY KEY,
            user_id BIGINT NOT NULL,
            username VARCHAR(255),
            city VARCHAR(255) NOT NULL,
            temperature DOUBLE PRECISION,
            feels_like DOUBLE PRECISION,
            humidity INT,
            pressure INT,
            wind_speed DOUBLE PRECISION,
            wind_direction INT,
            cloudiness INT,
            description VARCHAR(255),
            request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """;

        String createForecastTableSQL = """
        CREATE TABLE IF NOT EXISTS weather_forecasts (
            id SERIAL PRIMARY KEY,
            user_id BIGINT NOT NULL,
            username VARCHAR(255),
            city VARCHAR(255) NOT NULL,
            forecast_date TIMESTAMP NOT NULL,
            temperature DOUBLE PRECISION,
            feels_like DOUBLE PRECISION,
            humidity INT,
            pressure INT,
            wind_speed DOUBLE PRECISION,
            wind_direction INT,
            cloudiness INT,
            description VARCHAR(255),
            request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createWeatherRequestsTableSQL);
            stmt.execute(createForecastTableSQL);
            System.out.println("✅ Таблицы успешно созданы или уже существуют");
        }
    }

    public static void saveWeatherRequest(long userId, String username, WeatherData weatherData, String city) {
        String sql = """
            INSERT INTO weather_requests (
                user_id, username, city, temperature, feels_like, 
                humidity, pressure, wind_speed, wind_direction, 
                cloudiness, description
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, username != null ? username : "Неизвестный пользователь");
            pstmt.setString(3, city);
            pstmt.setDouble(4, weatherData.getTemperature());
            pstmt.setDouble(5, weatherData.getFeelsLike());
            pstmt.setInt(6, weatherData.getHumidity());
            pstmt.setInt(7, weatherData.getPressure());
            pstmt.setDouble(8, weatherData.getWindSpeed());
            pstmt.setInt(9, weatherData.getWindDegree());
            pstmt.setInt(10, weatherData.getCloudiness());
            pstmt.setString(11, weatherData.getDescription());

            pstmt.executeUpdate();
            System.out.println("✅ Запрос погоды успешно сохранен");
        } catch (SQLException e) {
            logSQLException(e);
        }
    }

    public static void saveForecastRequests(long userId, String username, List<WeatherData> forecastData, String city) {
        String sql = """
            INSERT INTO weather_forecasts (
                user_id, username, city, forecast_date, 
                temperature, feels_like, humidity, pressure, 
                wind_speed, wind_direction, cloudiness, description
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (WeatherData weatherData : forecastData) {
                pstmt.setLong(1, userId);
                pstmt.setString(2, username != null ? username : "Неизвестный пользователь");
                pstmt.setString(3, city);

                if (weatherData.getForecastDate() != null) {
                    pstmt.setTimestamp(4, Timestamp.valueOf(weatherData.getForecastDate()));
                } else {
                    pstmt.setNull(4, Types.TIMESTAMP);
                }

                pstmt.setDouble(5, weatherData.getTemperature());
                pstmt.setDouble(6, weatherData.getFeelsLike());
                pstmt.setInt(7, weatherData.getHumidity());
                pstmt.setInt(8, weatherData.getPressure());
                pstmt.setDouble(9, weatherData.getWindSpeed());
                pstmt.setInt(10, weatherData.getWindDegree());
                pstmt.setInt(11, weatherData.getCloudiness());
                pstmt.setString(12, weatherData.getDescription());

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            conn.commit();

            System.out.println("✅ Прогноз погоды успешно сохранен");
        } catch (SQLException e) {
            logSQLException(e);
        }
    }

    public static List<WeatherData> getWeatherHistory(long userId, String city) {
        List<WeatherData> weatherHistory = new ArrayList<>();
        String sql = """
            SELECT * FROM weather_requests 
            WHERE user_id = ? AND city = ? 
            ORDER BY request_date DESC 
            LIMIT 25
        """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, city);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    WeatherData weatherData = new WeatherData();
                    weatherData.setTemperature(rs.getDouble("temperature"));
                    weatherData.setFeelsLike(rs.getDouble("feels_like"));
                    weatherData.setHumidity(rs.getInt("humidity"));
                    weatherData.setPressure(rs.getInt("pressure"));
                    weatherData.setWindSpeed(rs.getDouble("wind_speed"));
                    weatherData.setWindDegree(rs.getInt("wind_direction"));
                    weatherData.setCloudiness(rs.getInt("cloudiness"));
                    weatherData.setDescription(rs.getString("description"));

                    weatherHistory.add(weatherData);
                }
            }
        } catch (SQLException e) {
            logSQLException(e);
        }

        return weatherHistory;
    }

    public static List<WeatherData> getForecastHistory(long userId, String city) {
        List<WeatherData> forecasts = new ArrayList<>();
        String sql = """
            SELECT * FROM weather_forecasts 
            WHERE user_id = ? AND city = ? 
            ORDER BY forecast_date DESC 
            LIMIT 25
        """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            pstmt.setString(2, city);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    WeatherData forecast = new WeatherData();
                    forecast.setTemperature(rs.getDouble("temperature"));
                    forecast.setFeelsLike(rs.getDouble("feels_like"));
                    forecast.setHumidity(rs.getInt("humidity"));
                    forecast.setPressure(rs.getInt("pressure"));
                    forecast.setWindSpeed(rs.getDouble("wind_speed"));
                    forecast.setWindDegree(rs.getInt("wind_direction"));
                    forecast.setCloudiness(rs.getInt("cloudiness"));
                    forecast.setDescription(rs.getString("description"));

                    Timestamp forecastDate = rs.getTimestamp("forecast_date");
                    if (forecastDate != null) {
                        forecast.setForecastDate(forecastDate.toLocalDateTime());
                    }

                    forecasts.add(forecast);
                }
            }
        } catch (SQLException e) {
            logSQLException(e);
        }

        return forecasts;
    }

    private static void logSQLException(SQLException e) {
        System.err.println("❌ Ошибка при работе с базой данных:");
        System.err.println("Код ошибки: " + e.getErrorCode());
        System.err.println("SQL состояние: " + e.getSQLState());
        System.err.println("Сообщение: " + e.getMessage());
        e.printStackTrace();
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            createTablesIfNotExists(conn);
            conn.close();
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Ошибка подключения к базе данных:");
            logSQLException(e);
            return false;
        }
    }

    public static void cleanupOldRecords() {
        String cleanupRequestsSQL = """
            DELETE FROM weather_requests 
            WHERE request_date < NOW() - INTERVAL '30 days'
        """;

        String cleanupForecastsSQL = """
            DELETE FROM weather_forecasts 
            WHERE request_date < NOW() - INTERVAL '30 days'
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            int requestsDeleted = stmt.executeUpdate(cleanupRequestsSQL);
            int forecastsDeleted = stmt.executeUpdate(cleanupForecastsSQL);

            System.out.println("🧹 Очистка базы данных:");
            System.out.println("Удалено старых записей погоды: " + requestsDeleted);
            System.out.println("Удалено старых прогнозов: " + forecastsDeleted);

        } catch (SQLException e) {
            logSQLException(e);
        }
    }

    public static DatabaseStats getDatabaseStats() {
        DatabaseStats stats = new DatabaseStats();

        String requestCountSQL = "SELECT COUNT(*) FROM weather_requests";
        String forecastCountSQL = "SELECT COUNT(*) FROM weather_forecasts";
        String uniqueCitiesSQL = "SELECT COUNT(DISTINCT city) FROM weather_requests";
        String uniqueUsersSQL = "SELECT COUNT(DISTINCT user_id) FROM weather_requests";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(requestCountSQL)) {
                if (rs.next()) stats.setTotalRequests(rs.getInt(1));
            }

            try (ResultSet rs = stmt.executeQuery(forecastCountSQL)) {
                if (rs.next()) stats.setTotalForecasts(rs.getInt(1));
            }

            try (ResultSet rs = stmt.executeQuery(uniqueCitiesSQL)) {
                if (rs.next()) stats.setUniqueCities(rs.getInt(1));
            }

            try (ResultSet rs = stmt.executeQuery(uniqueUsersSQL)) {
                if (rs.next()) stats.setUniqueUsers(rs.getInt(1));
            }

        } catch (SQLException e) {
            logSQLException(e);
        }

        return stats;
    }

    public static class DatabaseStats {
        private int totalRequests;
        private int totalForecasts;
        private int uniqueCities;
        private int uniqueUsers;

        public int getTotalRequests() { return totalRequests; }
        public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }
        public int getTotalForecasts() { return totalForecasts; }
        public void setTotalForecasts(int totalForecasts) { this.totalForecasts = totalForecasts; }
        public int getUniqueCities() { return uniqueCities; }
        public void setUniqueCities(int uniqueCities) { this.uniqueCities = uniqueCities; }
        public int getUniqueUsers() { return uniqueUsers; }
        public void setUniqueUsers(int uniqueUsers) { this.uniqueUsers = uniqueUsers; }

        @Override
        public String toString() {
            return "📊 Статистика базы данных:\n" +
                    "Всего запросов погоды: " + totalRequests + "\n" +
                    "Всего прогнозов: " + totalForecasts + "\n" +
                    "Уникальных городов: " + uniqueCities + "\n" +
                    "Уникальных пользователей: " + uniqueUsers;
        }
    }
}
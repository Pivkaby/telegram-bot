import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class WeatherBot extends TelegramLongPollingBot {
    private static final String BOT_TOKEN = "";
    private static final String BOT_USERNAME = "";

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            String username = update.getMessage().getFrom().getUserName() != null
                    ? update.getMessage().getFrom().getUserName()
                    : update.getMessage().getFrom().getFirstName();

            if (messageText.equals("/start")) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText(
                        "Привет! 👋\n" +
                                "Я бот для получения информации о погоде.\n" +
                                "Просто введи название города, и я покажу текущую погоду.\n" +
                                "Например: Москва, London, Paris\n\n" +
                                "Доступные команды:\n" +
                                "• {город} - получить текущую погоду\n" +
                                "• /forecast {город} - прогноз на 5 дней\n" +
                                "• /weather_history {город} - история погоды\n" +
                                "• /forecast_history {город} - история прогнозов\n" +
                                "• /db_stats - статистика использования"
                );
                sendMessage(message);
                return;
            }

            if (messageText.startsWith("/forecast ")) {
                String city = messageText.substring("/forecast ".length());
                SendMessage message = new SendMessage();
                message.setChatId(chatId);

                try {
                    List<WeatherData> forecasts = WeatherForecastAPI.getForecast(city);

                    if (forecasts != null && !forecasts.isEmpty()) {
                        DatabaseManager.saveForecastRequests(
                                chatId,
                                username,
                                forecasts,
                                city
                        );

                        StringBuilder forecastText = new StringBuilder("🌦️ Прогноз погоды для " + city + ":\n\n");

                        for (WeatherData forecast : forecasts) {
                            forecastText.append(forecast.toString()).append("\n\n");
                        }

                        message.setText(forecastText.toString());
                    } else {
                        message.setText("❌ Не удалось получить прогноз для города " + city);
                    }

                    sendMessage(message);

                } catch (Exception e) {
                    SendMessage errorMessage = new SendMessage();
                    errorMessage.setChatId(chatId);
                    errorMessage.setText(
                            "❗ Произошла ошибка при получении прогноза:\n" +
                                    e.getMessage()
                    );
                    sendMessage(errorMessage);
                }
                return;
            }

            if (messageText.startsWith("/weather_history ")) {
                String city = messageText.substring("/weather_history ".length());
                SendMessage message = new SendMessage();
                message.setChatId(chatId);

                List<WeatherData> weatherHistory = DatabaseManager.getWeatherHistory(chatId, city);

                if (!weatherHistory.isEmpty()) {
                    StringBuilder historyText = new StringBuilder("📋 История погоды для " + city + ":\n\n");

                    for (int i = 0; i < Math.min(5, weatherHistory.size()); i++) {
                        historyText.append("Запрос #").append(i + 1).append(":\n")
                                .append(weatherHistory.get(i).toString())
                                .append("\n\n");
                    }

                    message.setText(historyText.toString());
                } else {
                    message.setText("❌ История погоды для " + city + " не найдена.");
                }

                sendMessage(message);
                return;
            }

            if (messageText.startsWith("/forecast_history ")) {
                String city = messageText.substring("/forecast_history ".length());
                SendMessage message = new SendMessage();
                message.setChatId(chatId);

                List<WeatherData> forecastHistory = DatabaseManager.getForecastHistory(chatId, city);

                if (!forecastHistory.isEmpty()) {
                    StringBuilder historyText = new StringBuilder("📊 История прогнозов для " + city + ":\n\n");

                    for (int i = 0; i < Math.min(5, forecastHistory.size()); i++) {
                        historyText.append("Прогноз #").append(i + 1).append(":\n")
                                .append(forecastHistory.get(i).toString())
                                .append("\n\n");
                    }

                    message.setText(historyText.toString());
                } else {
                    message.setText("❌ История прогнозов для " + city + " не найдена.");
                }

                sendMessage(message);
                return;
            }

            // Обработка статистики базы данных
            if (messageText.equals("/db_stats")) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);

                DatabaseManager.DatabaseStats stats = DatabaseManager.getDatabaseStats();
                message.setText(stats.toString());

                sendMessage(message);
                return;
            }

            if (messageText.equals("/cleanup_db")) {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);

                DatabaseManager.cleanupOldRecords();
                message.setText("🧹 Старые записи успешно удалены.");

                sendMessage(message);
                return;
            }

            try {
                WeatherData weather = WeatherAPI.getWeather(messageText);

                SendMessage message = new SendMessage();
                message.setChatId(chatId);

                if (weather != null) {
                    DatabaseManager.saveWeatherRequest(
                            chatId,
                            username,
                            weather,
                            messageText
                    );

                    message.setText(
                            "🌍 Погода в городе " + messageText + ":\n\n" +
                                    weather.toString()
                    );
                } else {
                    message.setText(
                            "❌ Не удалось получить данные о погоде для города " + messageText +
                                    "\nПроверьте правильность названия города."
                    );
                }

                sendMessage(message);

            } catch (Exception e) {
                SendMessage errorMessage = new SendMessage();
                errorMessage.setChatId(chatId);
                errorMessage.setText(
                        "❗ Произошла ошибка при получении погоды:\n" +
                                e.getMessage()
                );
                sendMessage(errorMessage);
            }
        }
    }

    private void sendMessage(SendMessage message) {
        try {
            message.enableHtml(true);
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка при отправке сообщения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}
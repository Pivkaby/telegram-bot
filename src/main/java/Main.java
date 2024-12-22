import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        try {
            if (!DatabaseManager.testConnection()) {
                System.err.println("❌ Не удалось установить подключение к базе данных.");
                return;
            }

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            WeatherBot bot = new WeatherBot();
            botsApi.registerBot(bot);
            System.out.println("✅ Бот успешно запущен!");

        } catch (Exception e) {
            System.err.println("❌ Ошибка при запуске бота:");
            e.printStackTrace();
        }
    }
}
package pro.nextbit.telegramconstructor.handle;

import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class Bot extends TelegramLongPollingBot {

    private String botUserName;
    private String botToken;
    private static Map<Long, Handling> chats = new HashMap<>();
    private ApplicationContext context;
    private DataSource source;
    private static Bot instance;


    @Override
    public void onUpdateReceived(Update update) {

        try {

            Handling handling;
            Message message;
            long chatId;

            if (update.getMessage() != null){
                message = update.getMessage();
                chatId = message.getChatId();
            } else {
                message = update.getCallbackQuery().getMessage();
                chatId = message.getChatId();
            }

            if (chats.containsKey(chatId)) {
                handling = chats.get(chatId);
            } else {
                handling = new Handling();
                chats.put(chatId, handling);
            }

            handling.start(this, update, message);
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void setBotUserName(String botUserName){
        this.botUserName = botUserName;
    }

    public void setBotToken(String botToken){
        this.botToken = botToken;
    }

    public ApplicationContext getAppContext() {
        return this.context;
    }

    public void setAppContext(ApplicationContext context) {
        this.context = context;
    }

    public DataSource getSource() {
        return source;
    }

    public void setSource(DataSource source) {
        this.source = source;
    }

    public static Bot getInstance() {
        return instance;
    }

    public void setInstance(Bot bot) {
        instance = bot;
    }
}

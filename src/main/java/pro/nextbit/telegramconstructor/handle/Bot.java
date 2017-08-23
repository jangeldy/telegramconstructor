package pro.nextbit.telegramconstructor.handle;

import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.HashMap;
import java.util.Map;

public class Bot extends TelegramLongPollingBot {

    private String botUserName;
    private String botToken;
    private static Map<Long, Handling> chats = new HashMap<>();

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

}

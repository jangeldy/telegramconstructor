package pro.nextbit.telegramconstructor;

import org.telegram.telegrambots.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClearMessage {

    private static Map<Long, List<Integer>> msgMap = new HashMap<>();
    private static Map<Long, List<Integer>> msgClearedLater = new HashMap<>();
    private static Map<Long, List<Integer>> msgClearedOnClick = new HashMap<>();


    /**
     * Для очистки сообщения
     * @param message - сообщение
     */
    public static void clear(Message message) {
        set(message, msgMap);
    }


    /**
     * Для очистки сообщения
     * @param message - сообщение
     */
    public static void clearLater(Message message){
        set(message, msgClearedLater);
    }


    /**
     * Для очистки сообщения
     * при взаомодействии пользователя с message
     * @param message - сообщение
     */
    public static void clearOnClick(Message message) { set(message, msgClearedOnClick);}


    /**
     * Для очистки сообщения
     * @param message - сообщение
     */
    public static void removeAll(TelegramLongPollingBot bot, Message message) {
        ClearMessage clearMessage = new ClearMessage();
        clearMessage.remove(bot, message.getChatId(), clearMessage.get(message.getChatId()));
        clearMessage.remove(bot, message.getChatId(), clearMessage.getAsLater(message));
        clearMessage.remove(bot, message.getChatId(), clearMessage.getOnClick(message));
    }



    private List<Integer> get(long chatId) {
        List<Integer> list = new ArrayList<>();
        if (msgMap.containsKey(chatId)){
            list = msgMap.get(chatId);
            msgMap.remove(chatId);
        }
        return list;
    }

    private List<Integer> getAsLater(Message message) {
        if (msgClearedLater.containsKey(message.getChatId())){
            List<Integer> list = msgClearedLater.get(message.getChatId());
            if (list.contains(message.getMessageId())){
                return new ArrayList<>();
            }

            msgClearedLater.remove(message.getChatId());
            return list;

        }
        return new ArrayList<>();
    }

    private List<Integer> getOnClick(Message message) {

        if (msgClearedOnClick.containsKey(message.getChatId())){

            List<Integer> list = msgClearedOnClick.get(message.getChatId());
            if (list.contains(message.getMessageId())){

                List<Integer> newList = new ArrayList<>();
                newList.add(message.getMessageId());
                list.remove(message.getMessageId());

                return newList;
            }

            return new ArrayList<>();

        }
        return new ArrayList<>();
    }

    private static void set(Message message, Map<Long, List<Integer>> map) {
        if (map.containsKey(message.getChatId())){
            List<Integer> list = map.get(message.getChatId());
            list.add(message.getMessageId());
        } else {
            List<Integer> list = new ArrayList<>();
            list.add(message.getMessageId());
            map.put(message.getChatId(), list);
        }
    }

    private void remove(TelegramLongPollingBot bot, long chatId, List<Integer> list) {

        if (list != null) {
            for (int messageId : list) {
                try {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(String.valueOf(chatId));
                    deleteMessage.setMessageId(messageId);
                    bot.execute(deleteMessage);
                } catch (Exception ignore) {}
            }
        }
    }
}

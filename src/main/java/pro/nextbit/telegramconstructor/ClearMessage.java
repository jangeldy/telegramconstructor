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

    public void clear(Message message) {
        set(message, msgMap);
    }

    public void clearLater(Message message){
        set(message, msgClearedLater);
    }

    public void clearOnClick(Message message) { set(message, msgClearedOnClick);}

    public void remove(TelegramLongPollingBot bot, Message message) {
        remove(bot, message.getChatId(), get(message.getChatId()));
        remove(bot, message.getChatId(), getAsLater(message));
        remove(bot, message.getChatId(), getOnClick(message));
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

    private void set(Message message, Map<Long, List<Integer>> map) {
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
                    bot.deleteMessage(deleteMessage);
                } catch (Exception ignore) {}
            }
        }
    }
}

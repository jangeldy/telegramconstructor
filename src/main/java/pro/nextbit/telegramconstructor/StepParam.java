package pro.nextbit.telegramconstructor;

import pro.nextbit.telegramconstructor.database.DataRec;

import java.util.HashMap;
import java.util.Map;

public class StepParam {

    private static Map<String, DataRec> paramsMap = new HashMap<>();
    private DataRec params;
    private long chatId;
    private String step;

    public StepParam(long chatId, String step) {

        this.chatId = chatId;
        this.step = step;

        if (paramsMap.containsKey(step + chatId)){
            params = paramsMap.get(step + chatId);
        } else {
            params = new DataRec();
            paramsMap.put(step + chatId, params);
        }
    }

    public DataRec get() {
        return params;
    }

    public void remove() {
        paramsMap.remove(step + chatId);
    }

    public static DataRec send(long chatId, String step) {
        DataRec params;
        if (paramsMap.containsKey(step + chatId)){
            params = paramsMap.get(step + chatId);
        } else {
            params = new DataRec();
            paramsMap.put(step + chatId, params);
        }
        return params;
    }
}

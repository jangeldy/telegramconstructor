package pro.nextbit.telegramconstructor.handle;

import org.telegram.telegrambots.api.objects.Message;
import pro.nextbit.telegramconstructor.GlobalParam;
import pro.nextbit.telegramconstructor.StepParam;
import pro.nextbit.telegramconstructor.database.DataRec;

public class AbsHandleService {

    // параметры команды
    public String step;
    public String inputText;
    public DataRec queryData;
    public DataRec param;
    public long chatId;
    public Message message;


    /**
     * Параметры
     * @param globalParam - globalParam
     */
    public void setGlobalParam(
            GlobalParam globalParam,
            String step
    ){
        this.inputText = globalParam.getInputText();
        this.queryData = globalParam.getQueryData();
        this.chatId = globalParam.getChatId();
        this.step = step;
        this.param = new StepParam(chatId, step).get();
        this.message = globalParam.getMessage();
    }


    /**
     * Параметры для step
     * @param chatId - для кого
     * @param step - для какого step
     * @return - DataRec
     */
    public DataRec setParam(long chatId, String step){
        return StepParam.send(chatId, step);
    }

    /**
     * Параметры для step
     * @param step - для какого step
     * @return - DataRec
     */
    public DataRec setParam(String step){
        return StepParam.send(chatId, step);
    }

}

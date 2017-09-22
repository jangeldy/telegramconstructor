package pro.nextbit.telegramconstructor.handle;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import pro.nextbit.telegramconstructor.ClearMessage;
import pro.nextbit.telegramconstructor.GlobalParam;
import pro.nextbit.telegramconstructor.StepParam;
import pro.nextbit.telegramconstructor.accesslevel.AccessLevel;
import pro.nextbit.telegramconstructor.database.DataRec;
import pro.nextbit.telegramconstructor.stepmapping.Mapping;
import pro.nextbit.telegramconstructor.stepmapping.StepMapping;

public class AbsHandle {

    public Logger log = LogManager.getLogger(AbsHandle.class);
    public TelegramLongPollingBot bot;
    public Update update;

    // параметры команды
    public String step;
    public String inputText;
    public DataRec queryData;
    public AccessLevel accessLevel;
    public DataRec param;
    public long chatId;
    public Message message;

    //
    private Mapping redirectMapping;


    /**
     * Параметры
     * @param bot - bot
     * @param update - update
     * @param globalParam - globalParam
     */
    public void setGlobalParam(
            TelegramLongPollingBot bot,
            Update update,
            GlobalParam globalParam,
            String step
    ){
        this.bot = bot;
        this.update = update;
        this.inputText = globalParam.getInputText();
        this.queryData = globalParam.getQueryData();
        this.accessLevel = globalParam.getAccessLevel();
        this.chatId = globalParam.getChatId();
        this.redirectMapping = new Mapping();
        this.step = step;
        this.param = new StepParam(chatId, step).get();
        this.message = globalParam.getMessage();
    }

    public String getChangedStep() {
        return step;
    }

    public Mapping getRedirect(){
        return redirectMapping;
    }


    /**
     * Перенапрвления команды
     * @param step - шаг
     */
    public void redirect(String step){

        if (step == null
                || step.trim().equals("")
                || !StepMapping.containsStep(step)){
            throw new RuntimeException("Error in redirect command. Step can not be empty");
        }

        this.step = step;
        redirectMapping = StepMapping.getMappingByStep(step);
        redirectMapping.setRedirect(true);

    }


    /**
     * Для очистки сообщения
     * @param message - сообщение
     */
    public void clearMessage(Message message){
        new ClearMessage().clear(message);
    }

    /**
     * Для очистки сообщения
     * @param message - сообщение
     */
    public void clearMessageLater(Message message){
        new ClearMessage().clearLater(message);
    }

    /**
     * Для очистки сообщения
     * @param message - сообщение
     */
    public void clearMessageOnClick(Message message){
        new ClearMessage().clearOnClick(message);
    }


    /**
     * Параметры для step
     * @param chatId - для кого
     * @param step - для какого step
     * @return - DataRec
     */
    public DataRec setParam(long chatId, String step){
        return new StepParam(chatId, step).get();
    }

    /**
     * Параметры для step
     * @param step - для какого step
     * @return - DataRec
     */
    public DataRec setParam(String step){
        return new StepParam(chatId, step).get();
    }


    public String dataRequest(String messageText) throws Exception {

        DataRec param = new StepParam(chatId, step + "_dr").get();
        if (param.containsKey(messageText)){

            if (param.get(messageText).equals("requested")){
                param.put(messageText, inputText);
            }

            return param.getString(messageText);

        } else {

            param.put(messageText, "requested");
            clearMessage(bot.sendMessage(
                    new SendMessage()
                    .setChatId(chatId)
                    .setText(messageText)
            ));

            throw new RuntimeException("ignore");
        }
    }
}

package pro.nextbit.telegramconstructor.handle;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import pro.nextbit.telegramconstructor.ClearMessage;
import pro.nextbit.telegramconstructor.GlobalParam;
import pro.nextbit.telegramconstructor.StepParam;
import pro.nextbit.telegramconstructor.components.keyboard.IKeyboard;
import pro.nextbit.telegramconstructor.components.keyboard.Keyboard;
import pro.nextbit.telegramconstructor.database.DataRec;
import pro.nextbit.telegramconstructor.stepmapping.Mapping;
import pro.nextbit.telegramconstructor.stepmapping.StepMapping;

public class AbsHandle {

    public TelegramLongPollingBot bot;
    public Update update;

    // параметры команды
    public String step;
    public String inputText;
    public DataRec queryData;
    public DataRec param;
    public long chatId;
    public Message message;

    //
    private Mapping redirectMapping;


    /**
     * Для перехвата
     * до выполнения step метода
     * @return - если false метод step не выполняется
     */
    public boolean preInterceptor() throws Exception {
        // здесь размещаем методы для перехвата
        return true;
    }


    /**
     * Для перехвата
     * после выполнения step метода
     */
    public void postInterceptor() throws Exception {
        // здесь размещаем методы для перехвата
    }


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


    public String dataRequest(String messageText) throws Exception {

        DataRec param = new StepParam(chatId, step + "_dr").get();
        if (param.containsKey(messageText)){

            if (param.get(messageText).equals("requested")){
                param.put(messageText, inputText);
            }

            return param.getString(messageText);

        } else {

            param.put(messageText, "requested");
            sendMessage(messageText);

            throw new RuntimeException("ignore");
        }
    }

    /**
     * Отправка сообщений с клавиатурой
     */
    public void sendMessage(String text) throws Exception {
        ClearMessage.clear(
                bot.execute(
                        new SendMessage()
                                .setChatId(chatId)
                                .setText(text)
                )
        );
    }

    /**
     * Отправка сообщений с клавиатурой
     */
    public void sendMessage(String text, Keyboard keyboard) throws Exception {
        ClearMessage.clear(
                bot.execute(
                        new SendMessage()
                                .setChatId(chatId)
                                .setText(text)
                                .setReplyMarkup(keyboard.generate())
                )
        );
    }

    /**
     * Отправка сообщений с inline клавиатурой
     */
    public void sendMessage(String text, IKeyboard keyboard) throws Exception {
        ClearMessage.clear(
                bot.execute(
                        new SendMessage()
                                .setChatId(chatId)
                                .setText(text)
                                .setReplyMarkup(keyboard.generate())
                )
        );
    }
}

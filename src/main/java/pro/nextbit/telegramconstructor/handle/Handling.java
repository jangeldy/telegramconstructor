package pro.nextbit.telegramconstructor.handle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import pro.nextbit.telegramconstructor.ClearMessage;
import pro.nextbit.telegramconstructor.GlobalParam;
import pro.nextbit.telegramconstructor.StepParam;
import pro.nextbit.telegramconstructor.accesslevel.AccessLevel;
import pro.nextbit.telegramconstructor.accesslevel.AccessLevelMap;
import pro.nextbit.telegramconstructor.database.DataRec;
import pro.nextbit.telegramconstructor.stepmapping.Mapping;
import pro.nextbit.telegramconstructor.stepmapping.StepMapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

class Handling {

    private String step;
    private String lastStep;
    private AbsHandle handle;
    private Logger log = LogManager.getLogger(Handling.class);


    /**
     * Обработка входящих команд
     * @param bot    - бот
     * @param update - объект входящего запроса
     */
    void start(TelegramLongPollingBot bot, Update update, Message message) {

        GlobalParam globalParam = getGlobalParam(update, message.getChatId());
        Mapping mapping = getMapping(update, message, globalParam.getQueryData());

        if (globalParam.getAccessLevel() != AccessLevel.WITHOUT_ACCESS) {

            try {
                mapping = handlingMethod(bot, update, globalParam, mapping);
                while (mapping.isRedirect()) {
                    mapping = handlingMethod(bot, update, globalParam, mapping);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Ищет команду по базе
     * @param update - объект входящего запроса
     * @param chatId - chatId
     * @return - GlobalParam
     */
    private GlobalParam getGlobalParam(Update update, long chatId) {

        GlobalParam globalParam = new GlobalParam();
        String inputText;

        if (update.getMessage() == null) {
            inputText = update.getCallbackQuery().getMessage().getText();
            globalParam.setMessage(update.getCallbackQuery().getMessage());
        } else {
            inputText = update.getMessage().getText();
            globalParam.setMessage(update.getMessage());
        }

        globalParam.setInputText(inputText);
        globalParam.setChatId(chatId);
        globalParam.setAccessLevel(AccessLevelMap.get(chatId));
        globalParam.setQueryData(getQueryData(update));
        return globalParam;
    }


    /**
     * Ищет класс и метод для выполнения
     * @param update    - объект входящего запроса
     * @param queryData - скрытые данные инлайн кнопки
     * @return - Mapping
     */
    private Mapping getMapping(Update update, Message message, DataRec queryData) {

        Mapping mapping = null;
        if (update.getMessage() == null) {

            if (queryData.containsKey("step")) {
                String qd_step = queryData.getString("step");
                if (StepMapping.containsStep(qd_step)) {
                    mapping = StepMapping.getMappingByStep(qd_step);
                    step = mapping.getStep();
                }
            }

        } else {

            String commandText = update.getMessage().getText();
            if (StepMapping.containsCommandText(commandText)) {
                mapping = StepMapping.getMappingByCommandText(commandText);
                step = mapping.getStep();
            }
        }

        if (mapping == null && StepMapping.containsStep(step)) {
            mapping = StepMapping.getMappingByStep(step);
        } else if (mapping == null) {
            mapping = StepMapping.getMappingByStep("defaultStep");
        }

        if (message.isGroupMessage()
                || message.isSuperGroupMessage()
                || message.isChannelMessage()){
            mapping = StepMapping.getMappingByStep("groupMessage");
        }

        return mapping;
    }


    /**
     * Ищет скрытые данные
     * @param update - объект входящего запроса
     * @return - getQueryData
     */
    private DataRec getQueryData(Update update) {

        DataRec queryData = new DataRec();

        if (update.getMessage() == null) {
            String queryText = update.getCallbackQuery().getData();

            if (queryText != null && !queryText.equals("")) {
                try {
                    Type token = new TypeToken<DataRec>(){}.getType();
                    queryData = new Gson().fromJson(queryText, token);

                    for (DataRec.Entry<String, Object> entry : queryData.entrySet()) {
                        if (entry.getValue() instanceof Double) {
                            Double value = (Double) entry.getValue();
                            entry.setValue(value.intValue());
                        }
                    }
                } catch (Exception ignore) {}
            }
        }

        return queryData;
    }


    /**
     * Запуск обрабатывающего метода соответствующего класса
     * @param bot         - bot
     * @param update      - объект входящего запроса
     * @param globalParam - параметры
     * @param mapping     - маппинг
     * @return redirectMapping
     * @throws Exception - Exception
     */
    private Mapping handlingMethod(
            TelegramLongPollingBot bot, Update update,
            GlobalParam globalParam, Mapping mapping
    ) throws Exception {

        printMapping(mapping);
        String className = mapping.getHandleClassName();

        if (handle != null && className.equals(handle.getClass().getSimpleName())) {
            Class<?> clazz = handle.getClass();
            return processMethod(bot, update, globalParam, mapping, clazz);
        }
        else {

            Class<?> clazz = Class.forName(StepMapping.getHandlingPath() + "." + className);
            Constructor<?> ctor = clazz.getConstructor();
            handle = (AbsHandle) ctor.newInstance();
            return processMethod(bot, update, globalParam, mapping, clazz);
        }
    }


    private Mapping processMethod(
            TelegramLongPollingBot bot, Update update,
            GlobalParam globalParam,
            Mapping mapping, Class clazz
    ) throws Exception {

        new ClearMessage().remove(bot, globalParam.getMessage());
        handle.setGlobalParam(bot, update, globalParam, mapping.getStep());

        if (step != null && !step.equals(lastStep)){
            new StepParam(globalParam.getChatId(), lastStep + "_dr").remove();
            new StepParam(globalParam.getChatId(), lastStep).remove();
        }

        invokeMethod(clazz, mapping);

        if (step != null && !handle.getChangedStep().equals(step)){
            new StepParam(globalParam.getChatId(), mapping.getStep() + "_dr").remove();
            new StepParam(globalParam.getChatId(), mapping.getStep()).remove();
        }

        lastStep = handle.getChangedStep();
        step = handle.getChangedStep();
        return handle.getRedirect();
    }


    /**
     * Запсук метода обработки
     * @param clazz - класс
     * @param mapping - маппинг
     * @throws Exception - Exception
     */
    private void invokeMethod(Class clazz, Mapping mapping) throws Exception {
        try {
            Method method = clazz.getMethod(mapping.getHandleMethod());
            method.invoke(handle);
        } catch (InvocationTargetException e){
            if (!e.getTargetException().getMessage().equals("ignore")){
                e.getCause().printStackTrace();
            }
        }
    }


    /**
     * Вывод маппинга в консоль
     * @param mapping - mapping
     */
    private void printMapping(Mapping mapping) {

        String redirect = "-----> ";
        if (mapping.isRedirect()) {
            log.info(  "      |");
            redirect = "      └--> REDIRECT --> ";
        } else {
            log.info(" ");
        }

        log.info(
                redirect
                + mapping.getHandleClassName() + " ---> "
                + mapping.getStep() + " ---> "
                + mapping.getCommandText()
        );
        log.info(" ");
    }

}

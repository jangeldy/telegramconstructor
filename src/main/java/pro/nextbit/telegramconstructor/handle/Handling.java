package pro.nextbit.telegramconstructor.handle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import pro.nextbit.telegramconstructor.ClearMessage;
import pro.nextbit.telegramconstructor.GlobalParam;
import pro.nextbit.telegramconstructor.StepParam;
import pro.nextbit.telegramconstructor.database.DataRec;
import pro.nextbit.telegramconstructor.stepmapping.Mapping;
import pro.nextbit.telegramconstructor.stepmapping.StepMapping;

import javax.sql.DataSource;
import java.lang.reflect.*;

class Handling {

    private String step;
    private String lastStep;
    private AbsHandle handle;
    private Log log = LogFactory.getLog(Handling.class);


    /**
     * Обработка входящих команд
     * @param bot    - бот
     * @param update - объект входящего запроса
     */
    void start(Bot bot, Update update, Message message) {

        GlobalParam globalParam = getGlobalParam(update, message.getChatId());
        Mapping mapping = getMapping(update, message, globalParam.getQueryData());

        try {
            mapping = handlingMethod(bot, update, globalParam, mapping);
            while (mapping.isRedirect()) {
                mapping = handlingMethod(bot, update, globalParam, mapping);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            Bot bot, Update update,
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
            Constructor<?> constructor = clazz.getConstructor();
            handle = (AbsHandle) constructor.newInstance();

            if (bot.getAppContext() != null){
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    setHandleRepository(bot, handle, field);
                    setHandleService(bot, field, globalParam, mapping.getStep());
                    setHandleDao(bot, handle, field);
                }
            }

            return processMethod(bot, update, globalParam, mapping, clazz);
        }
    }


    // присваивание значений repository
    private void setHandleRepository(Bot bot, Object object, Field parentField) throws Exception {
        if (parentField.isAnnotationPresent(HandleRepository.class)) {
            Object repository = bot.getAppContext().getBean(parentField.getType());
            parentField.setAccessible(true);
            parentField.set(object, repository);
        }
    }


    // присваивание значений service
    private void setHandleService(Bot bot, Field parentField, GlobalParam globalParam, String step) throws Exception {
        if (parentField.isAnnotationPresent(HandleService.class)) {
            if (AbsHandleService.class.isAssignableFrom(parentField.getType())){
                Class<?> clazz = Class.forName(parentField.getType().getName());
                Constructor<?> constructor = clazz.getConstructor();
                AbsHandleService service = (AbsHandleService) constructor.newInstance();
                service.setGlobalParam(globalParam, step);

                parentField.setAccessible(true);
                parentField.set(handle, service);

                Field[] fields = clazz.getDeclaredFields();

                for (Field field : fields) {
                    setHandleRepository(bot, service, field);
                    setHandleDao(bot, service, field);
                }
            }
        }
    }


    // присваивание значений dao
    private void setHandleDao(Bot bot, Object object, Field parentField) throws Exception {
        if (parentField.isAnnotationPresent(HandleDao.class)) {
            String classForName = parentField.getType().getPackage().getName()
                    + ".impl." + parentField.getType().getSimpleName() + "Impl";

            Class<?> clazz = Class.forName(classForName);
            Constructor<?> constructor = clazz.getConstructor(DataSource.class);
            DataSource source = bot.getAppContext().getBean(DataSource.class);
            Object dao = constructor.newInstance(source);

            parentField.setAccessible(true);
            parentField.set(object, dao);
        }
    }


    private Mapping processMethod(
            Bot bot, Update update,
            GlobalParam globalParam,
            Mapping mapping, Class clazz
    ) throws Exception {

        ClearMessage.removeAll(bot, globalParam.getMessage());
        handle.setGlobalParam(bot, update, globalParam, mapping.getStep());

        if (step != null && !step.equals(lastStep)){
            new StepParam(globalParam.getChatId(), lastStep + "_dr").remove();
            new StepParam(globalParam.getChatId(), lastStep).remove();
        }

        if (handle.preInterceptor()) {
            invokeMethod(clazz, mapping);
            handle.postInterceptor();
        }

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
            boolean isIgnore = e.getTargetException().getMessage() != null &&
                    e.getTargetException().getMessage().equals("ignore");

            if (!isIgnore){
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
                + mapping.getHandleClassName() + " ---> ( "
                + mapping.getStep() + ", "
                + mapping.getCommandText() + " )"
        );
        log.info(" ");
    }

}

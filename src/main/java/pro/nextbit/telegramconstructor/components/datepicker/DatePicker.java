package pro.nextbit.telegramconstructor.components.datepicker;

import org.joda.time.DateTime;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import pro.nextbit.telegramconstructor.ClearMessage;
import pro.nextbit.telegramconstructor.Json;
import pro.nextbit.telegramconstructor.StepParam;
import pro.nextbit.telegramconstructor.components.keyboard.IKeyboard;
import pro.nextbit.telegramconstructor.database.DataRec;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DatePicker {

    private DateTime selectedDate;
    private DateTime date;
    private int startDay;
    private List<String> designate;
    private String step;
    private boolean isEdit = false;
    private DataRec queryData;

    public DatePicker(DataRec queryData, String step){
        this.designate = new ArrayList<>();
        this.step = step;
        this.queryData = queryData;
    }

    public DateTime getDate(
            TelegramLongPollingBot bot,
            String messageText, Message message
    ) throws TelegramApiException {

        DataRec param = new StepParam(message.getChatId(), step + "_dr").get();
        if (param.containsKey("dp_sel" + messageText)){
            selectedDate = new DateTime(param.getDate("dp_sel" + messageText));
        } else {
            calculateDates(bot, message);
        }

        if (selectedDate == null){

            if (isEdit) {

                bot.execute(
                        new EditMessageReplyMarkup()
                        .setReplyMarkup(generate())
                        .setMessageId(message.getMessageId())
                        .setChatId(message.getChatId())
                );

            } else {

                Message msg = bot.execute(
                        new SendMessage()
                                .setText(messageText)
                                .setChatId(message.getChatId())
                                .setReplyMarkup(generate())
                );
                ClearMessage.clearLater(msg);
            }

            throw new RuntimeException("ignore");

        } else {
            param.put("dp_sel" + messageText, selectedDate);
            return selectedDate;
        }
    }

    public void setDesignate(List<String> designate) {
        this.designate = designate;
    }

    private void calculateDates(TelegramLongPollingBot bot, Message message) {

        if (queryData.containsKey("dp_dt")) {
            this.isEdit = true;
        }

        if (queryData.containsKey("dp_sel")){
            selectedDate = new DateTime(queryData.getDate("dp_sel"));
            ClearMessage.removeAll(bot, message);
        }

        if (queryData.containsKey("dp_dt")){
            date = new DateTime(queryData.getDate("dp_dt"));
        } else {

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            date = new DateTime(cal.getTime());
        }

        if (queryData.containsKey("dp_next")){

            if (date.getDayOfMonth() <= 16){
                date = date.dayOfMonth().withMaximumValue();
                startDay = date.getDayOfMonth() - 16;
            } else {
                date = date.dayOfMonth().withMinimumValue().plusMonths(1);
                startDay = 1;
            }

        } else if (queryData.containsKey("dp_prev")){

            if (date.getDayOfMonth() <= 16){
                date = date.minusMonths(1).dayOfMonth().withMaximumValue();
                startDay = date.getDayOfMonth() - 16;
            } else {
                date = date.dayOfMonth().withMinimumValue();
                startDay = 1;
            }

        } else {

            if (date.getDayOfMonth() <= 16){
                date = date.dayOfMonth().withMinimumValue();
                startDay = 1;
            } else {
                date = date.dayOfMonth().withMaximumValue();
                startDay = date.getDayOfMonth() - 16;
            }
        }

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        designate.add(df.format(new Date()));

        queryData.remove("dp_sel");
        queryData.remove("dp_dt");
        queryData.remove("dp_next");
        queryData.remove("dp_prev");
    }

    private InlineKeyboardMarkup generate() {

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        String dateStr = df.format(date.toDate());

        IKeyboard keyboard = new IKeyboard();
        setHeader(keyboard, dateStr);
        setBody(keyboard, dateStr.substring(2));
        return keyboard.generate();
    }

    private void setHeader(IKeyboard keyboard, String dateStr){

        DateFormat df2 = new SimpleDateFormat("MMM");
        String monthName = df2.format(date.toDate()) + " " + date.getYear();

        keyboard.next(3);
        keyboard.add("<", Json.set("dp_dt", dateStr).set("dp_prev", "p").set("step", step));
        keyboard.add(monthName, Json.set("dp_dt", dateStr).set("step", step));
        keyboard.add(">", Json.set("dp_dt", dateStr).set("dp_next", "p").set("step", step));
    }

    private void setBody(IKeyboard keyboard, String monthYear) {

        keyboard.next(4,4,4,4);
        for (int a = 1; a <= 4; a++){
            for (int i = 1; i <= 4; i++) {

                String day = String.valueOf(startDay);
                String startDayStr = String.valueOf(startDay);

                if (startDay < 10) day = "0" + day;
                if (designate.contains(day + monthYear)) {
                    startDayStr = "• " + startDay + " •";
                }

                keyboard.add(startDayStr, Json.set("dp_sel", day + monthYear).set("step", step));
                startDay++;
            }
        }
    }
}

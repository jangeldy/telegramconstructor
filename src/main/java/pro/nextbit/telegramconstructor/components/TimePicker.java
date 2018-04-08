package pro.nextbit.telegramconstructor.components;

import org.joda.time.DateTime;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import pro.nextbit.telegramconstructor.ClearMessage;
import pro.nextbit.telegramconstructor.Json;
import pro.nextbit.telegramconstructor.StepParam;
import pro.nextbit.telegramconstructor.components.keyboard.IKeyboard;
import pro.nextbit.telegramconstructor.database.DataRec;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimePicker {

    private DateTime dateTime;
    private DateTime selectedTime;
    private DataRec queryData;
    private String step;
    private boolean isEdit = false;

    public TimePicker(DataRec queryData, String step, DateTime dateTime) throws Exception {
        this.queryData = queryData;
        this.step = step;
        this.dateTime = dateTime;
    }


    public DateTime getDateTime(
            TelegramLongPollingBot bot,
            String messageText, Message message
    ) throws Exception {

        DataRec param = new StepParam(message.getChatId(), step + "_dr").get();
        if (param.containsKey("tp_sel" + messageText)){
            selectedTime = new DateTime(param.getDate("tp_sel" + messageText));
        } else {
            calculateTime(bot, message);
        }

        if (selectedTime == null){

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
            param.put("tp_sel" + messageText, selectedTime);
            return selectedTime;
        }

    }


    private InlineKeyboardMarkup generate() {

        Date hourPlusOne = dateTime.plusHours(1).toDate();
        Date hourMinusOne = dateTime.minusHours(1).toDate();
        Date minuteMinusOne = dateTime.minusMinutes(1).toDate();
        Date minuteMinusTenth = dateTime.minusMinutes(10).toDate();
        Date minutePlusOne = dateTime.plusMinutes(1).toDate();
        Date minutePlusTenth = dateTime.plusMinutes(10).toDate();

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        DateFormat df2 = new SimpleDateFormat("HH:mm");

        IKeyboard kb = new IKeyboard();
        kb.next(3, 4);
        kb.add("<", Json.set("step", step).set("tp_dt", df.format(hourMinusOne)));
        kb.add(df2.format(dateTime.toDate()), Json.set("step", step).set("tp_sel", df.format(dateTime.toDate())));
        kb.add(">", Json.set("step", step).set("tp_dt", df.format(hourPlusOne)));
        kb.add("- 10м", Json.set("step", step).set("tp_dt", df.format(minuteMinusTenth)));
        kb.add("- м", Json.set("step", step).set("tp_dt", df.format(minuteMinusOne)));
        kb.add("+ м", Json.set("step", step).set("tp_dt", df.format(minutePlusOne)));
        kb.add("+ 10м", Json.set("step", step).set("tp_dt", df.format(minutePlusTenth)));

        return kb.generate();
    }


    private void calculateTime(TelegramLongPollingBot bot, Message message) throws ParseException {

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        if (queryData.containsKey("tp_dt")) {
            dateTime = new DateTime(df.parse(queryData.getString("tp_dt")));
            isEdit = true;
        } else {

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 12);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            dateTime = new DateTime(cal.getTime());
        }

        if (queryData.containsKey("tp_sel")) {
            selectedTime = new DateTime(df.parse(queryData.getString("tp_sel")));
            ClearMessage.removeAll(bot, message);
        }

        queryData.remove("tp_dt");
        queryData.remove("tp_sel");
    }
}

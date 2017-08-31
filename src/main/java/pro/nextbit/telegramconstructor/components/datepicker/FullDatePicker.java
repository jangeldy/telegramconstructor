package pro.nextbit.telegramconstructor.components.datepicker;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class FullDatePicker {

    private DataRec queryData = null;
    private DateTime tempDate = null;
    private DateTime selectedDate = null;
    private String step = null;
    private LinkedList<LinkedList<Integer>> monthDates = null;
    private boolean isEdit = false;
    private int week = -1;

    public FullDatePicker(DataRec queryData, String step) {
        this.step = step;
        this.queryData = queryData;
        this.monthDates = new LinkedList<>();

        if (queryData.containsKey("dp_dt")) {
            this.isEdit = true;
        }


    }

    public DateTime getDate(
            TelegramLongPollingBot bot,
            String messageText, Message message
    ) throws Exception {

        DataRec param = new StepParam(message.getChatId(), step + "_dr").get();
        if (param.containsKey("dp_sel" + messageText)){
            selectedDate = new DateTime(param.getDate("dp_sel" + messageText));
            week = param.getInt("dp_w" + messageText);
        } else {
            calculateDates(bot, message);
        }

        if (selectedDate == null) {

            if (isEdit) {

                bot.editMessageReplyMarkup(
                        new EditMessageReplyMarkup()
                                .setReplyMarkup(generate())
                                .setMessageId(message.getMessageId())
                                .setChatId(message.getChatId())
                );

            } else {

                Message msg = bot.sendMessage(
                        new SendMessage()
                                .setText(messageText)
                                .setChatId(message.getChatId())
                                .setReplyMarkup(generate())
                );
                new ClearMessage().clearLater(msg);
            }

            throw new RuntimeException("ignore");

        } else {
            param.put("dp_sel" + messageText, selectedDate);
            param.put("dp_w" + messageText, week);
            return selectedDate;
        }
    }


    public int getWeek() {
        return week;
    }


    public InlineKeyboardMarkup generate() {

        Date prevMonth = tempDate.minusMonths(1).dayOfMonth().withMinimumValue().toDate();
        Date nextMonth = tempDate.plusMonths(1).dayOfMonth().withMinimumValue().toDate();

        IKeyboard keyboard = new IKeyboard();
        generateHeader(keyboard, prevMonth, nextMonth);
        generateBody(keyboard, prevMonth, nextMonth);

        return keyboard.generate();
    }

    private void generateHeader(IKeyboard keyboard, Date prevMonth, Date nextMonth) {

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        DateFormat df2 = new SimpleDateFormat("MMM");
        String currentDate = df.format(tempDate.toDate());
        String monthName = df2.format(tempDate.toDate()) + " " + tempDate.getYear();

        keyboard.next(3, 7);
        keyboard.addButton("<", Json.set("dp_dt", df.format(prevMonth)).set("step", step));
        keyboard.addButton(monthName, Json.set("dp_dt", df.format(tempDate.toDate())).set("step", step));
        keyboard.addButton(">", Json.set("dp_dt", df.format(nextMonth)).set("step", step));
        keyboard.addButton("Пн", Json.set("dp_dt", currentDate).set("dp_w", 1).set("step", step));
        keyboard.addButton("Вт", Json.set("dp_dt", currentDate).set("dp_w", 2).set("step", step));
        keyboard.addButton("Ср", Json.set("dp_dt", currentDate).set("dp_w", 3).set("step", step));
        keyboard.addButton("Чт", Json.set("dp_dt", currentDate).set("dp_w", 4).set("step", step));
        keyboard.addButton("Пт", Json.set("dp_dt", currentDate).set("dp_w", 5).set("step", step));
        keyboard.addButton("Сб", Json.set("dp_dt", currentDate).set("dp_w", 6).set("step", step));
        keyboard.addButton("Вс", Json.set("dp_dt", currentDate).set("dp_w", 7).set("step", step));
    }

    private void generateBody(IKeyboard keyboard, Date prevMonth, Date nextMonth){

        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        String monthYear = df.format(tempDate.toDate()).substring(2);

        for (LinkedList<Integer> weekDates : monthDates) {
            keyboard.next(7);

            for (int day : weekDates) {

                if (day == -1) {
                    keyboard.addButton(
                            ".",
                            Json.set("dp_dt", df.format(prevMonth))
                                .set("step", step)
                    );
                } else if (day == -2){
                    keyboard.addButton(
                            ".",
                            Json.set("dp_dt", df.format(nextMonth))
                                .set("step", step)
                    );
                } else {

                    String dayStr = String.valueOf(day);
                    if (day < 10) dayStr = "0" + dayStr;

                    keyboard.addButton(
                            dayStr,
                            Json.set("dp_sel", day + monthYear)
                                .set("step", step)
                    );
                }
            }
        }
    }

    private void calculateDates(TelegramLongPollingBot bot, Message message) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (queryData.containsKey("dp_w")) {
            week = queryData.getInt("dp_w");
            selectedDate = new DateTime(cal.getTime());
            new ClearMessage().remove(bot, message);
        }

        if (queryData.containsKey("dp_sel")) {
            selectedDate = new DateTime(queryData.getDate("dp_sel"));
            new ClearMessage().remove(bot, message);
        }

        if (queryData.containsKey("dp_dt")) {
            tempDate = new DateTime(queryData.getDate("dp_dt"));
        } else {
            tempDate = new DateTime(cal.getTime());
        }

        DateTime firstDay = tempDate.dayOfMonth().withMinimumValue();
        DateTime lastDay = tempDate.dayOfMonth().withMaximumValue();
        LinkedList<Integer> weekDates = new LinkedList<>();

        int firstWeek = firstDay.getDayOfWeek() - 1;
        int lastWeek = lastDay.getDayOfWeek() + 1;

        while (firstWeek > 0) {
            weekDates.add(-1);
            firstWeek--;
        }


        for (int day = 1; day <= lastDay.getDayOfMonth(); day++) {
            weekDates.add(day);
            if (weekDates.size() >= 7) {
                monthDates.add(weekDates);
                weekDates = new LinkedList<>();
            }
        }

        while (lastWeek < 8) {
            weekDates.add(-2);
            lastWeek++;
        }

        if (weekDates.size() == 7) {
            monthDates.add(weekDates);
        }

        queryData.remove("dp_dt");
        queryData.remove("dp_sel");
        queryData.remove("dp_w");
    }
}

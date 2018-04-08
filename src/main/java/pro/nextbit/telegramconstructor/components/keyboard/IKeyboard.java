package pro.nextbit.telegramconstructor.components.keyboard;

import com.google.gson.Gson;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import pro.nextbit.telegramconstructor.database.DataRec;

import java.util.ArrayList;
import java.util.List;

public class IKeyboard {

    private int[] buttonCounts = null;
    private List<List<InlineKeyboardButton>> inlineList;
    private List<List<List<InlineKeyboardButton>>> inlineTables;


    public void next(int ...buttonCounts){
        this.buttonCounts = buttonCounts;
        setRows();
    }


    public void next(){
        this.buttonCounts = null;
        setRows();
    }


    public int size() {
        return inlineTables.size() + inlineList.size();
    }


    public InlineKeyboardMarkup generate() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (List<List<InlineKeyboardButton>> list:inlineTables){
            keyboard.addAll(list);
        }
        keyboard.addAll(inlineList);
        return new InlineKeyboardMarkup().setKeyboard(keyboard);
    }

    public InlineKeyboardButton addButton(String text, DataRec json) {
        return setButton(text, json);
    }

    public InlineKeyboardButton add(String text, DataRec json) {
        return setButton(text, json);
    }

    private void setRows(){
        if (inlineTables == null) {
            inlineTables = new ArrayList<>();
            inlineList = new ArrayList<>();
        }
        else {
            if (inlineList.size() == 0){
                throw new RuntimeException("You have not added any inline buttons");
            }
            else {
                inlineTables.add(inlineList);
                inlineList = new ArrayList<>();
            }
        }
    }


    private InlineKeyboardButton setButton(String text, DataRec json) {

        if (inlineTables == null){
            throw new RuntimeException("The method 'next' was not called");
        }

        List<InlineKeyboardButton> buttonList;
        InlineKeyboardButton button = new InlineKeyboardButton()
                .setText(text)
                .setCallbackData(new Gson().toJson(json));

        if (buttonCounts != null && buttonCounts.length > 0){

            if (inlineList.size() == 0){

                buttonList = new ArrayList<>();
                buttonList.add(button);
                inlineList.add(buttonList);

            } else {

                int buttonCount = buttonCounts[inlineList.size() - 1];
                buttonList = inlineList.get(inlineList.size() - 1);

                if (buttonList.size() == buttonCount){

                    if (buttonCounts.length == inlineList.size()){
                        throw new RuntimeException("The number of added buttons is more than the specified quantity");
                    } else {

                        buttonList = new ArrayList<>();
                        buttonList.add(button);
                        inlineList.add(buttonList);
                    }

                } else {
                    buttonList.add(button);
                }

            }

        } else {

            buttonList = new ArrayList<>();
            buttonList.add(button);
            inlineList.add(buttonList);

        }

        return button;
    }

}

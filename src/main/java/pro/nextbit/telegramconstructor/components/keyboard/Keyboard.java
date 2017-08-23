package pro.nextbit.telegramconstructor.components.keyboard;

import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class Keyboard {

    private int[] buttonCounts = null;
    private List<KeyboardRow> list;
    private List<List<KeyboardRow>> table;


    public void next(int ...buttonCounts){
        this.buttonCounts = buttonCounts;
        setRows();
    }


    public void next(){
        this.buttonCounts = null;
        setRows();
    }


    public KeyboardButton addButton(String text) {
        return setButton(text);
    }


    public KeyboardButton add(String text) {
        return setButton(text);
    }


    public ReplyKeyboardMarkup generate() {
        List<KeyboardRow> keyboard = new ArrayList<>();
        for (List<KeyboardRow> list:table){
            keyboard.addAll(list);
        }
        keyboard.addAll(list);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }


    private void setRows(){
        if (table == null) {
            table = new ArrayList<>();
            list = new ArrayList<>();
        }
        else {
            if (list.size() == 0){
                throw new RuntimeException("You have not added any buttons");
            }
            else {
                table.add(list);
                list = new ArrayList<>();
            }
        }
    }


    private KeyboardButton setButton(String text) {
        if (table == null){
            throw new RuntimeException("The method 'next' was not called");
        }

        KeyboardRow buttonList;
        KeyboardButton button = new KeyboardButton().setText(text);

        if (buttonCounts != null && buttonCounts.length > 0){

            if (list.size() == 0){

                buttonList = new KeyboardRow();
                buttonList.add(button);
                list.add(buttonList);

            } else {

                int buttonCount = buttonCounts[list.size() - 1];
                buttonList = list.get(list.size() - 1);

                if (buttonList.size() == buttonCount){

                    if (buttonCounts.length == list.size()){
                        throw new RuntimeException("The number of added buttons is more than the specified quantity");
                    } else {

                        buttonList = new KeyboardRow();
                        buttonList.add(button);
                        list.add(buttonList);
                    }

                } else {
                    buttonList.add(button);
                }

            }

        } else {

            buttonList = new KeyboardRow();
            buttonList.add(button);
            list.add(buttonList);

        }

        return button;
    }

}

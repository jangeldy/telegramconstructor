package pro.nextbit.telegramconstructor;

import pro.nextbit.telegramconstructor.database.DataRec;

public class Json {

    public static DataRec set(String key, Object value) {
        return new DataRec().set(key, value);
    }

}

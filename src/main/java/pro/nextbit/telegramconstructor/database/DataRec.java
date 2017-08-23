package pro.nextbit.telegramconstructor.database;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataRec extends HashMap<String, Object> {

    public Object getValue(String key){
        Object value = get(key);
        if (value == null) throw new IllegalArgumentException();
        else return value;
    }

    public boolean hasValue(String key) {
        return containsKey(key) && get(key) != null;
    }


    public DataRec set(String key, Object value){
        put(key, value);
        return this;
    }


    /**
     * Конвертирует значение в нужный тип
     *
     * @param key - входяще значение
     * @return - конвертированное значение
     */
    public long getLong(String key) {

        Object object = get(key);

        if (object instanceof Integer) {
            return (long) ((int) object);
        } else if (object instanceof Long) {
            return (long) object;
        } else if (object instanceof Double) {
            return ((Double) object).longValue();
        } else if (object instanceof BigDecimal) {
            return ((BigDecimal) object).longValue();
        } else if (object instanceof String) {

            String string = object.toString().trim();
            Matcher mDouble = Pattern.compile("^-?\\d+\\.?(\\d+)?$").matcher(string);
            Matcher mLong = Pattern.compile("^-?\\d+$").matcher(string);

            if (mLong.matches()) {
                return Long.parseLong(string);
            } else if (mDouble.matches()) {
                return ((Double) Double.parseDouble(string)).longValue();
            } else {
                throw new NumberFormatException();
            }

        } else {
            throw new NumberFormatException();
        }

    }

    /**
     * Конвертирует значение в нужный тип
     *
     * @param key - входяще значение
     * @return - конвертированное значение
     */
    public int getInt(String key) {

        Object object = get(key);

        if (object instanceof Integer) {
            return (int) object;
        } else if (object instanceof Long) {
            return Integer.parseInt(object.toString());
        } else if (object instanceof Double) {
            return ((Double) object).intValue();
        } else if (object instanceof BigDecimal) {
            return ((BigDecimal) object).intValue();
        } else if (object instanceof String) {

            String string = object.toString().trim();
            Matcher mDouble = Pattern.compile("^-?\\d+\\.?(\\d+)?$").matcher(string);
            Matcher mInt = Pattern.compile("^-?\\d+$").matcher(string);

            if (mInt.matches()) {
                return Integer.parseInt(string);
            } else if (mDouble.matches()) {
                return ((Double) Double.parseDouble(string)).intValue();
            } else {
                throw new NumberFormatException();
            }

        } else {
            throw new NumberFormatException();
        }

    }

    /**
     * Конвертирует значение в нужный тип
     *
     * @param key - входяще значение
     * @return - конвертированное значение
     */
    public boolean getBoolean(String key) {

        Object object = get(key);

        if (object instanceof Integer) {

            if ((int) object == 0) return false;
            else if ((int) object == 1) return true;
            else throw new IllegalArgumentException();

        } else if (object instanceof Long) {

            if ((long) object == 0) return false;
            else if ((long) object == 1) return true;
            else throw new IllegalArgumentException();

        } else if (object instanceof BigDecimal) {

            BigDecimal bg = (BigDecimal) object;
            if (bg.longValue() == 0) return false;
            else if (bg.longValue() == 1) return true;
            else throw new IllegalArgumentException();

        } else if (object instanceof Double) {

            long value = ((Double) object).longValue();
            if (value == 0) return false;
            else if (value == 1) return true;
            else throw new IllegalArgumentException();

        } else if (object instanceof String) {

            String string = object.toString().trim();
            switch (string) {
                case "false":
                    return false;
                case "true":
                    return true;
            }

            string = string.substring(0, 1);
            switch (string) {
                case "0":
                    return false;
                case "1":
                    return true;
            }

            throw new IllegalArgumentException();

        } else if (object instanceof Boolean) {
            return (boolean) object;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Конвертирует значение в нужный тип
     *
     * @param key - входяще значение
     * @return - конвертированное значение
     */
    public double getDouble(String key) {

        Object object = get(key);

        if (object instanceof Double) {
            return (double) object;
        } else if (object instanceof Integer) {
            return (double) object;
        } else if (object instanceof Long) {
            return (double) object;
        } else if (object instanceof BigDecimal) {
            return ((BigDecimal) object).doubleValue();
        } else if (object instanceof String) {

            String string = object.toString().trim();
            string = string.replaceAll(" ", "");
            Matcher mDouble = Pattern.compile("^-?\\d+\\.?(\\d+)?$").matcher(string);

            if (mDouble.matches()) return Double.parseDouble(string);
            else throw new NumberFormatException();

        } else {
            throw new NumberFormatException();
        }
    }

    /**
     * Конвертирует значение в нужный тип
     *
     * @param key - входяще значение
     * @return - конвертированное значение
     */
    public Date getDate(String key) {

        Object object = get(key);

        if (object instanceof Date) {
            return (Date) object;
        } else if (object instanceof DateTime) {
            return ((DateTime) object).toDate();
        } else if (object instanceof Timestamp){
            return new Date(((Timestamp) object).getTime());
        }else if (object instanceof String) {

            String string = object.toString().trim();
            Matcher m1 = Pattern.compile("^\\d\\d-\\d\\d-\\d\\d\\d\\d$").matcher(string);
            Matcher m2 = Pattern.compile("^\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d$").matcher(string);
            Matcher m3 = Pattern.compile("^\\d\\d\\d\\d-\\d\\d-\\d\\d$").matcher(string);
            Matcher m4 = Pattern.compile("^\\d\\d\\d\\d\\.\\d\\d\\.\\d\\d$").matcher(string);

            try {

                if (m1.matches()) {
                    DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                    return format.parse(string);
                } else if (m2.matches()) {
                    DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                    return format.parse(string);
                } else if (m3.matches()) {
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    return format.parse(string);
                } else if (m4.matches()) {
                    DateFormat format = new SimpleDateFormat("yyyy.MM.dd");
                    return format.parse(string);
                } else {
                    throw new IllegalArgumentException();
                }

            } catch (ParseException e) {
                e.printStackTrace();
                throw new IllegalArgumentException();
            }


        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Конвертирует значение в нужный тип
     *
     * @param key - входяще значение
     * @return - конвертированное значение
     */
    public String getString(String key) {
        Object object = get(key);
        String string = object.toString().trim();
        string = string.replaceAll("'", "");
        return string;
    }
}

package pro.nextbit.telegramconstructor.accesslevel;

import pro.nextbit.telegramconstructor.database.DataBaseUtils;
import pro.nextbit.telegramconstructor.database.DataRec;
import pro.nextbit.telegramconstructor.database.DataTable;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class AccessLevelMap {

    private static Map<Long, AccessLevel> accessLevelMap = new HashMap<>();
    private static DataSource source = null;

    public static AccessLevel get(long chatId) {
        return accessLevelMap.getOrDefault(chatId, AccessLevel.READ);
    }

    public static void set(long chatId, AccessLevel accessLevel) {

        DataBaseUtils utils = new DataBaseUtils(source);
        DataRec dataRec = utils.queryDataRec(
                "select * from access_level where enum_name = ?",
                accessLevel.toString()
        );
        DataTable dataTable = utils.query(
                "select * from access_info where chat_id = ?",
                chatId
        );

        if (dataTable.size() == 0) {
            utils.update("insert into access_info (chat_id, id_access_level)" +
                    "VALUES (?,?)", chatId, dataRec.get("id"));
        } else  {
            utils.update(
                    "update access_info set id_access_level = ? WHERE chat_id = ?",
                    dataRec.get("id"), chatId
            );
        }

        accessLevelMap.put(chatId, accessLevel);
    }

    public void init(DataSource dataSource) {

        source = dataSource;
        DataBaseUtils utils = new DataBaseUtils(source);
        DataTable accessLevelTable = utils.query(
                "SELECT table_schema,table_name " +
                        "FROM information_schema.tables " +
                        "WHERE table_schema not in ('information_schema', 'pg_catalog') " +
                        "AND table_name = 'access_level'"
        );

        DataTable accessTable = utils.query(
                "SELECT table_schema,table_name " +
                        "FROM information_schema.tables " +
                        "WHERE table_schema not in ('information_schema', 'pg_catalog') " +
                        "AND table_name = 'access_info'"
        );

        if (accessLevelTable.size() == 0){

            utils.execute(
                    " CREATE TABLE access_level (" +
                    " id INTEGER PRIMARY KEY NOT NULL, " +
                    " name VARCHAR(200), " +
                    " enum_name VARCHAR(200) NOT NULL )"
            );

            utils.execute("CREATE UNIQUE INDEX access_level_enum_name_uindex ON access_level (enum_name)");
            utils.update("INSERT INTO access_level (id, name, enum_name) VALUES (1, 'Администратор', 'ADMIN')");
            utils.update("INSERT INTO access_level (id, name, enum_name) VALUES (2, 'Чтение и запись', 'READ_AND_WRITE')");
            utils.update("INSERT INTO access_level (id, name, enum_name) VALUES (3, 'Чтение', 'READ')");
            utils.update("INSERT INTO access_level (id, name, enum_name) VALUES (4, 'Доступ запрещен', 'WITHOUT_ACCESS')");

        }

        if (accessTable.size() == 0){

            utils.execute(
                    "CREATE TABLE access_info " +
                            "( chat_id BIGINT NOT NULL, " +
                            "  id_access_level INTEGER, " +
                            "  CONSTRAINT access_info_access_level_id_fk FOREIGN KEY (id_access_level) REFERENCES access_level (id))"
            );

            utils.execute("CREATE UNIQUE INDEX access_info_chat_id_uindex ON access_info (chat_id)");

        } else {

            DataTable dataTable = utils.query(
                    "select a.chat_id, ac.enum_name from access_info a " +
                            "inner join access_level ac on ac.id = a.id_access_level ");

            for (DataRec rec : dataTable) {
                accessLevelMap.put(
                        rec.getLong("chat_id"),
                        AccessLevel.valueOf(rec.getString("enum_name"))
                );
            }

        }
    }
}

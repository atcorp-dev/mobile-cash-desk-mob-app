package ua.com.atcorp.mobilecashdesk.db;

import android.database.sqlite.SQLiteDatabase;

import com.reactiveandroid.annotation.Database;
import com.reactiveandroid.internal.database.migration.Migration;

@Database(name = "AppDatabase", version = 3)
public class AppDatabase {

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SQLiteDatabase database) {
            String[] sqlBuilder = new String[] {
                    "CREATE TABLE IF NOT EXISTS Users (",
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT,",
                    "RecordId TEXT,",
                    "Login TEXT,",
                    "Name TEXT,",
                    "CompanyId TEXT,",
                    "Email TEXT",
                    ");"
            };
            database.execSQL(getArrayAsString(sqlBuilder, "\n"));
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SQLiteDatabase database) {
            String[] sqlBuilder = new String[] {
                    "CREATE TABLE IF NOT EXISTS Carts (",
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT,",
                    "RecordId TEXT",
                    ");"
            };
            database.execSQL(getArrayAsString(sqlBuilder, "\n"));
            try {
                database.execSQL("ALTER TABLE CartItems ADD COLUMN Datetime DATETIME");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private static String getArrayAsString(String[] stringArray, String separator) {
        if(stringArray == null || stringArray.length == 0) return "";

        StringBuilder finalString = new StringBuilder(stringArray[0]);

        for(int i = 1; i < stringArray.length; ++i)
            finalString.append(separator).append(stringArray[i]);

        return finalString.toString();
    }
}

package ua.com.atcorp.mobilecashdesk.db;

import android.database.sqlite.SQLiteDatabase;

import com.reactiveandroid.annotation.Database;
import com.reactiveandroid.internal.database.migration.Migration;

@Database(name = "AppDatabase", version = 6)
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

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SQLiteDatabase database) {
            try {
                database.execSQL("DROP TABLE CartItems");
                database.execSQL("CREATE TABLE `CartItems` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT, `cartId` TEXT, `Datetime` INTEGER, `ItemBarCode` TEXT, `ItemCategory` INTEGER REFERENCES Categories(`_id`) ON DELETE NO ACTION ON UPDATE NO ACTION, `ItemCode` TEXT, `ItemCompany` INTEGER REFERENCES Companies(`_id`) ON DELETE NO ACTION ON UPDATE NO ACTION, `ItemImage` TEXT, `ItemName` TEXT, `ItemPrice` REAL, `ItemRecordId` TEXT, `qty` INTEGER)");
                database.execSQL("DROP TABLE Carts");
                database.execSQL("CREATE TABLE `Carts` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT, `RecordId` TEXT, `Type` INTEGER)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SQLiteDatabase database) {
            try {
                database.execSQL("ALTER TABLE Carts ADD COLUMN `ClientInfo` TEXT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SQLiteDatabase database) {
            try {
                database.execSQL("ALTER TABLE CartItems ADD COLUMN `Discount` REAL");
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

package lgztec.tecdaily;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class UserDb extends SQLiteOpenHelper{

    private static final String CREATE_TABLE = "create table "+DbCardData.TABLE_NAME+"("+DbCardData.CARD_ID+" TEXT PRIMARY KEY,"+DbCardData.CARD_TITLE+" TEXT,"+DbCardData.CRAD_TAG+" TEXT,"+DbCardData.CARD_TIME+" TEXT,"+DbCardData.CARD_DATE+" TEXT,"+DbCardData.CARD_FAV_STAT+" TEXT);";
    private static final String DROP_TABLE = "drop table if exists "+DbCardData.TABLE_NAME;


    public UserDb(Context context) {
        super(context,DbCardData.UDB_NAME ,  null, DbCardData.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
    }

    boolean checkDataBase(){

    }

    Boolean createUser(String user_name,String user_pass,String user_mail){

    }
}

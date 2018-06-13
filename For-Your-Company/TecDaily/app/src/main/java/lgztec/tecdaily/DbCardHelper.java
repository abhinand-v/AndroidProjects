package lgztec.tecdaily;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DbCardHelper extends SQLiteOpenHelper {

    private static final String CREATE_TABLE = "create table "+DbCardData.TABLE_NAME+"("+DbCardData.CARD_ID+" TEXT PRIMARY KEY,"+DbCardData.CARD_TITLE+" TEXT,"+DbCardData.CRAD_TAG+" TEXT,"+DbCardData.CARD_TIME+" TEXT,"+DbCardData.CARD_DATE+" TEXT,"+DbCardData.CARD_FAV_STAT+" TEXT);";
    private static final String DROP_TABLE = "drop table if exists "+DbCardData.TABLE_NAME;

    DbCardHelper(Context context) {
        super(context, DbCardData.DATABASE_NAME, null, DbCardData.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    void saveToDatabase(SQLiteDatabase db, String card_id, String card_title, String card_tag, String card_time, String card_date){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbCardData.CARD_ID,card_id);
        contentValues.put(DbCardData.CARD_TITLE,card_title);
        contentValues.put(DbCardData.CRAD_TAG,card_tag);
        contentValues.put(DbCardData.CARD_TIME,card_time);
        contentValues.put(DbCardData.CARD_DATE,card_date);
        contentValues.put(DbCardData.CARD_FAV_STAT,"false");

        long insertResult = db.insert(DbCardData.TABLE_NAME,null,contentValues);
        Log.d("TD_DB_insertResult","COUNT "+insertResult);
    }

    Cursor readToCard(SQLiteDatabase db){
        String[] projection = {DbCardData.CARD_ID,DbCardData.CARD_TITLE,DbCardData.CRAD_TAG,DbCardData.CARD_TIME,DbCardData.CARD_FAV_STAT};
        String Orderby = DbCardData.CARD_ID+" DESC";
        return db.query(DbCardData.TABLE_NAME,projection,null,null,null,null,Orderby);
    }

    Cursor readToFav(SQLiteDatabase db){
        String[] projection = {DbCardData.CARD_ID,DbCardData.CARD_TITLE,DbCardData.CRAD_TAG,DbCardData.CARD_TIME,DbCardData.CARD_FAV_STAT};
        String selection = DbCardData.CARD_FAV_STAT+" = ? ";
        return db.query(DbCardData.TABLE_NAME,projection,selection,new String[]{"true"},null,null,null);
    }

    void flushDbData(SQLiteDatabase db){
        int flushResult = db.delete(DbCardData.TABLE_NAME,null,null);
        Log.d("TD_DB_flushResult : ","COUNT "+flushResult);
    }

    void setFavorite(SQLiteDatabase db,String id){
        /*ContentValues contentValues = new ContentValues();
        contentValues.put(DbCardData.CARD_FAV_STAT,"true");
        String whereClause = DbCardData.CARD_FAV_STAT + " = ? ";
        String[] whereArgs = {id};*/
        String query = "UPDATE "+DbCardData.TABLE_NAME+" SET "+DbCardData.CARD_FAV_STAT+" = 'true' WHERE "+DbCardData.CARD_ID+" = '"+id+"'";
        db.execSQL(query);
        Log.d("TD_DB_updateResult",/*"COUNT "+updateResult+*/" ID: "+id);
    }

    void removeFavorite(SQLiteDatabase db,String id){
        /*ContentValues contentValues = new ContentValues();
        contentValues.put(DbCardData.CARD_FAV_STAT,"false");
        String whereClause = DbCardData.CARD_FAV_STAT + " = ? ";
        String[] whereArgs = {id};*/
        String query = "UPDATE "+DbCardData.TABLE_NAME+" SET "+DbCardData.CARD_FAV_STAT+" = 'false' WHERE "+DbCardData.CARD_ID+" = '"+id+"'";
        db.execSQL(query);
        Log.d("TD_DB_updateResult",/*"COUNT "+updateResult+*/" ID: "+id);
    }

     boolean isFavorite(SQLiteDatabase db,String id){
        String state = "false";
        /*String query = "SELECT "+DbCardData.CARD_FAV_STAT+" FROM "+DbCardData.TABLE_NAME+" WHERE "+DbCardData.CARD_ID+" = '"+id+"'";*/
        Cursor cursor = db.query(DbCardData.TABLE_NAME,new String[]{DbCardData.CARD_FAV_STAT},DbCardData.CARD_ID+" = ?",new String[]{id},null,null,null);
        while (cursor.moveToNext()){
            state = cursor.getString(cursor.getColumnIndex(DbCardData.CARD_FAV_STAT));
        }
        cursor.close();
        return state.equals("true");
    }

    boolean hasData(SQLiteDatabase db){
        String[] projection = {DbCardData.CARD_ID,DbCardData.CARD_TITLE,DbCardData.CRAD_TAG,DbCardData.CARD_TIME,DbCardData.CARD_FAV_STAT};
        boolean hasdata = false;
        Cursor cursor = db.query(DbCardData.TABLE_NAME,projection,null,null,null,null,null);
        while (cursor.moveToNext())
        {
            hasdata = true;
        }
        cursor.close();
        return hasdata;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
    }
}

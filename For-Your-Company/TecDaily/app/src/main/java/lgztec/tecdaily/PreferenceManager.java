package lgztec.tecdaily;


import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private Context context;
    private SharedPreferences sharedPreferences;

    PreferenceManager(Context context){
        this.context = context;
        getSharedPreference();
    }

    private void getSharedPreference(){
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.my_preferance),Context.MODE_PRIVATE);
    }

    private void writeDbSyncState(boolean state){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (state)
            editor.putString(context.getString(R.string.pref_key_database_update),"YES");
        else
            editor.putString(context.getString(R.string.pref_key_database_update),"NO");
        editor.apply();
    }

    private String readDbSyncState(){
        return sharedPreferences.getString(context.getString(R.string.pref_key_database_update),"NO");
    }


}
package lgztec.tecdaily;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

class Singleton {
    private static Singleton Instance;
    private static RequestQueue requestQueue;
    private static Context ctx;

    //constructor
    private Singleton(Context context){
        ctx = context;
        requestQueue = getRequestQueue();
    }

    //getting request queue
    private RequestQueue getRequestQueue(){
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        return requestQueue;
    }

    //getting instance of class
    public static synchronized Singleton getInstance(Context context){
        if (Instance == null)
            Instance = new Singleton(context);
        return Instance;
    }

    //adding request
    public <T>void addToRequestQueue(Request<T> request){
        requestQueue.add(request);
    }
}

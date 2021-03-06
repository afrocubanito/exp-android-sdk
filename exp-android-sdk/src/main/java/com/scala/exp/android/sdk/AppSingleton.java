package com.scala.exp.android.sdk;

import com.google.gson.Gson;
import com.scala.exp.android.sdk.model.Auth;

/**
 * Created by Cesar Oyarzun on 10/28/15.
 */
public class AppSingleton {

    private static AppSingleton instance = null;
    private static IExpEndpoint endpoint = null;
    private static Gson gson = new Gson();
    private static String host = null;
    private static String token = null;
    private static String hostSocket = null;
    private static Auth auth = null;

    protected AppSingleton() {
        // Exists only to defeat instantiation.
    }

    public static AppSingleton getInstance() {
        if(instance == null) {
            instance = new AppSingleton();
        }
        return instance;
    }

    public String getHostSocket() {
        return hostSocket;
    }

    public void setHostSocket(String hostSocket) {
        AppSingleton.hostSocket = hostSocket;
    }

    public void setAuth(Auth auth){
        AppSingleton.auth = auth;
    }

    public Auth getAuth(){
        return AppSingleton.auth;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        AppSingleton.token = token;
    }


    public Gson getGson() {
        return this.gson;
    }

    public void setEndPoint(IExpEndpoint endpoint){
        AppSingleton.endpoint = endpoint;
    }

    public void setHost(String host){
        AppSingleton.host = host;
    }

    public IExpEndpoint getEndPoint( ){
        return this.endpoint;
    }

    public String getHost(){
        return this.host;
    }



}

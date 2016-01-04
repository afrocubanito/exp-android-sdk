package com.scala.exp.android.sdk;

import com.scala.exp.android.sdk.model.Auth;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Cesar Oyarzun on 11/2/15.
 */
public class Runtime extends Exp{

    /**
     * Start with device credentials Host,UUID,Secret
     * @param consumerApp
     * @param uuid
     * @param secret
     * @return String token
     */
    private static String createToken(String uuid, String secret,boolean consumerApp){
        Map<String,Object> header = new HashMap<String,Object>();
        header.put(Utils.TYP, Utils.JWT);
        Map<String,Object> payload = new HashMap<String,Object>();
        if(consumerApp){
            payload.put(Utils.CONSUMER_APP_UUID, uuid);
        }else {
            payload.put(Utils.UUID, uuid);
        }
        return Jwts.builder().setHeader(header).setClaims(payload).signWith(SignatureAlgorithm.HS256, secret.getBytes()).compact();

    }

    /**
     * Start with  options
     * @param options
     * @return
     */
    public static Observable<Boolean> start(Map<String,String> options){
        Observable<Boolean> observable = null;
        Map<String,String> opts= new HashMap<>();
        String hostUrl = "";
        if(options.get(Utils.HOST)!=null){
            AppSingleton.getInstance().setHost(options.get(Utils.HOST));
        }
        if(options.get(Utils.USERNAME)!= null && options.get(Utils.PASSWORD)!= null && options.get(Utils.ORGANIZATION)!= null){
            observable = start_auth(options);
        }else if (options.get(Utils.UUID) != null && options.get(Utils.SECRET) != null) {
            opts.put("token",createToken(options.get(Utils.UUID), options.get(Utils.SECRET),false));
            observable = start_auth(opts);
        } else if (options.get(Utils.DEVICE_UUID) != null && options.get(Utils.SECRET) != null) {
            opts.put("token",createToken(options.get(Utils.DEVICE_UUID), options.get(Utils.SECRET),false));
            observable = start_auth(opts);
        } else if (options.get(Utils.NETWORK_UUID) != null && options.get(Utils.API_KEY) != null) {
            opts.put("token",createToken(options.get(Utils.NETWORK_UUID), options.get(Utils.API_KEY),true));
            observable = start_auth(opts);
        } else if (options.get(Utils.CONSUMER_APP_UUID) != null && options.get(Utils.API_KEY) != null) {
            opts.put("token",createToken(options.get(Utils.CONSUMER_APP_UUID), options.get(Utils.API_KEY),true));
            observable = start_auth(opts);
        } else {
            throw new RuntimeException("Credentials are missing from start call");
        }

        return observable;
    }

    /**
     * Start with options
     * @param options
     * @return
     */
    public static Observable<Boolean> start_auth(final Map<String, String> options){
        return ExpService.init(AppSingleton.getInstance().getHost())
                .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean result) {

                        return Exp.login(options)
                                .flatMap(new Func1<Auth, Observable<Boolean>>() {
                                    @Override
                                    public Observable<Boolean> call(Auth token) {
                                        AppSingleton.getInstance().setToken(token.getToken());
                                        final BigInteger expiration = token.getExpiration();
                                        return ExpService.init(AppSingleton.getInstance().getHost(), token.getToken())
                                                .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                                                    @Override
                                                    public Observable call(Boolean aBoolean) {

                                                        // refreshToken timeout
                                                        Observable.timer(getTimeOut(expiration), TimeUnit.SECONDS).flatMap(new Func1<Long, Observable<Long>>() {
                                                            @Override
                                                            public Observable<Long> call(Long aLong) {
                                                                return Exp.refreshToken()
                                                                        .flatMap(new Func1<Auth, Observable<Long>>() {
                                                                            @Override
                                                                            public Observable<Long> call(Auth auth) {
                                                                                AppSingleton.getInstance().setToken(auth.getToken());
                                                                                socketManager.refreshConnection();
                                                                                return refreshTokenAuth(auth);
                                                                            }
                                                                        });
                                                            }
                                                        }).subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread()).subscribe();
                                                        socketManager = new SocketManager();
                                                        return socketManager.startSocket();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    /**
     * Stop Connection to EXP
     */
    public static void stop(){
        socketManager.disconnect();
    }

    /**
     * Get Timout in seconds
     * @param expiration
     * @return
     */
    public static int getTimeOut(BigInteger expiration) {
        Date currentDate = new Date();
        return  (int) (( expiration.longValue() - currentDate.getTime() ) / 1000);
    }

    /**
     * Refresh Token with timer in seconds
     * @param auth
     * @return
     */
    private static Observable<Long> refreshTokenAuth(Auth auth){
        // refreshToken timeout
        Observable<Long> observableRefresh = (Observable<Long>) Observable.timer(getTimeOut(auth.getExpiration()), TimeUnit.SECONDS).flatMap(new Func1<Long, Observable<Long>>() {
            @Override
            public Observable<Long> call(Long aLong) {
                return Exp.refreshToken()
                        .flatMap(new Func1<Auth, Observable<Long>>() {
                            @Override
                            public Observable<Long> call(Auth auth) {
                                AppSingleton.getInstance().setToken(auth.getToken());
                                socketManager.refreshConnection();
                                return refreshTokenAuth(auth);
                            }
                        });
            }
        });

        return observableRefresh;
    }
}

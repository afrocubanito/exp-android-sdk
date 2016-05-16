package com.scala.exp.android.sdk.model;

import com.scala.exp.android.sdk.Exp;
import com.scala.exp.android.sdk.Utils;
import com.scala.exp.android.sdk.observer.ExpObservable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cesar Oyarzun on 4/25/16.
 */
public class Zone  extends AbstractModel{

    private Location location;

    public String getKey() {
        return getString(Utils.KEY);
    }

    public String getName() {
        return getString(Utils.NAME);
    }


    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ExpObservable<SearchResults<Device>> getDevices(){
        Map options = new HashMap();
        Location location = getLocation();
        if(location!=null){
            options.put(Utils.LOCATION_UUID, getLocation().getString(Utils.UUID));
            options.put(Utils.LOCATION_ZONES_KEY, getKey());
        }
        return Exp.findDevices(options);
    }

    public ExpObservable<SearchResults<Thing>> getThings(){
        Map options = new HashMap();
        Location location = getLocation();
        if(location!=null){
            options.put(Utils.LOCATION_UUID, getLocation().getString(Utils.UUID));
            options.put(Utils.LOCATION_ZONES_KEY, getKey());
        }
        return Exp.findThings(options);
    }


}

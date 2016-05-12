package com.sails.poi;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;

import java.net.URL;
import java.util.HashMap;

/**
 * Created by richard on 2016/5/11.
 */
@JsonObject
public class POI {
    static private HashMap<String,POI> map=new HashMap<>();
    @JsonField(name = "_id")
    private String id;
    public String getId() {
        return id;
    }
    public void setId(String Id) {
        id=Id;
    }

    @JsonField(name = "name")
    public String name;

    @JsonField(name = "img1")
    public String img1;

    @JsonField(name = "address")
    public String address;

    @JsonField(name = "area")
    public String area;

    @JsonField(name = "time")
    public String time;

    @JsonField(name = "tel")
    public String tel;

    @JsonField(name = "email")
    public String email;

    @JsonField(name = "website")
    public String url;

    @JsonField(name = "introduction")
    public String introduction;
    @OnJsonParseComplete
    void onParseComplete() {
        map.put(id,this);
    }
    public static void Clear() {
        map.clear();
    }
    public static POI GetPOIbyId(String id) {
        return map.get(id);
    }
}


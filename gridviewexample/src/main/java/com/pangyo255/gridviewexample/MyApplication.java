package com.pangyo255.gridviewexample;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .initialData(realm->{
                // Load from file "cities.json" first time
                List<City> cities = loadCities();
                if (cities != null) {
                    // Use insertOrUpdate() to convert the objects into proper RealmObjects managed by Realm.
                    realm.insertOrUpdate(cities);
                }
        })
        .deleteRealmIfMigrationNeeded()
        .build());
    }

    private List<City> loadCities() {
        // 이 경우 local assets에서 로딩 중입니다.
        // 참고 : 네트워크에서 쉽게로드 할 수 있습니다.
        // 그러나, 그 백그라운드 스레드에서 일어날 필요가있다.
        InputStream stream;
        try {
            stream = getAssets().open("cities.json");
        } catch (IOException e) {
            return null;
        }

        Gson gson = new GsonBuilder().create();

        JsonElement json = new JsonParser().parse(new InputStreamReader(stream));
        return gson.fromJson(json, new TypeToken<List<City>>() {}.getType());
    }
}

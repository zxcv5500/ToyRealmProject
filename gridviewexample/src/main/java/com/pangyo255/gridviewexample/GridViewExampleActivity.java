package com.pangyo255.gridviewexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class GridViewExampleActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private GridView gridView;
    private CityAdapter adapter;

    private Realm realm;
    private RealmResults<City> cities;
    private RealmChangeListener<RealmResults<City>> realmChangeListener = cities->{
        // 비동기 쿼리가 로드 될 때만 city를 어댑터로 설정하십시오.
        // 또한 Realm에 대한 앞으로의 글쓰기를 위해 불려질 것입니다.
        adapter.setData(cities);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);

        // This is the GridView adapter
        adapter = new CityAdapter();

        //This is the GridView which will display the list of cities
        gridView = findViewById(R.id.cities_list);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(GridViewExampleActivity.this);

        Realm.deleteRealm(Realm.getDefaultConfiguration());

        // Create a new empty instance of Realm
        realm = Realm.getDefaultInstance();

        // Obtain the cities in the Realm with asynchronous query.
        cities = realm.where(City.class).findAllAsync();

        // The RealmChangeListener will be called when the results are asynchronously loaded, and available for use.
        cities.addChangeListener(realmChangeListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cities.removeAllChangeListeners(); // Remove change listeners to prevent updating views not yet GCed.
        realm.close(); // Remember to close Realm when done.
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        City modifiedCity = adapter.getItem(position);

        // Acquire the name of the clicked City, in order to be able to query for it.
        final String name = modifiedCity.getName();

        // Create an asynchronous transaction to increment the vote count for the selected City in the Realm.
        // The write will happen on a background thread, and the RealmChangeListener will update the GridView automatically.
        realm.executeTransactionAsync(bgRealm -> {
            // We need to find the City we want to modify from the background thread's Realm
            City city = bgRealm.where(City.class).equalTo("name", name).findFirst();
            if (city != null) {
                // Let's increase the votes of the selected city!
                city.setVotes(city.getVotes() + 1);
            }
        });
    }
}

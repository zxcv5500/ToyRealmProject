package com.pangyo255.gridviewexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CityAdapter extends BaseAdapter {
    private List<City> cities = Collections.emptyList();

    public CityAdapter() {
    }

    public void setData(List<City> details) {
        if (details == null) {
            details = Collections.emptyList();
        }
        this.cities = details;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cities.size();
    }

    @Override
    public City getItem(int position) {
        return cities.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private static class ViewHolder {
        private TextView name;
        private TextView vote;

        public ViewHolder(View view) {
            this.name = view.findViewById(R.id.name);
            this.vote = view.findViewById(R.id.votes);
        }

        public void bind(City city) {
            name.setText(city.getName());
            vote.setText(String.format(Locale.US, "%d", city.getVotes()));
        }
    }

    @Override
    public View getView(int position, View currentView, ViewGroup parent) {
        // GridView는 최적의 성능을 보장하기 위해 ViewHolder 패턴이 필요합니다.
        ViewHolder viewHolder;
        if (currentView == null) {
            currentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_listitem, parent, false);
            viewHolder = new ViewHolder(currentView);
            currentView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) currentView.getTag();
        }

        City city = cities.get(position);
        viewHolder.bind(city);
        return currentView;
    }
}

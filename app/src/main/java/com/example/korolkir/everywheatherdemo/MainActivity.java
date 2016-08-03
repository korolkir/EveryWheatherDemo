package com.example.korolkir.everywheatherdemo;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.example.korolkir.everywheatherdemo.Model.Suggestions.CitySuggestionCreator;
import com.example.korolkir.everywheatherdemo.Model.DailyForecast;
import com.example.korolkir.everywheatherdemo.Model.WeatherRecyclerViewAdapter;

import com.crashlytics.android.Crashlytics;
import com.example.korolkir.everywheatherdemo.Presenter.ForecastPresenterImplementor;
import com.example.korolkir.everywheatherdemo.Model.Suggestions.CitySuggestion;
import com.example.korolkir.everywheatherdemo.View.ShowingView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import rx.Observable;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity implements ShowingView, FloatingSearchView.OnQueryChangeListener,
        SearchSuggestionsAdapter.OnBindSuggestionCallback, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.current_day_description) TextView currentDayDescription;
    @BindView(R.id.current_day_temperature) TextView currentDayTemp;
    @BindView(R.id.current_day_speed) TextView currentDaySpeed;
    @BindView(R.id.current_day_temperature_range) TextView currentDayTemperatureRange;
    @BindView(R.id.current_day_image) ImageView image;
    @BindView(R.id.current_day_linear_layout) LinearLayout currentDayLinear;
    @BindView(R.id.search_view) FloatingSearchView mSearchView;
    @BindView(R.id.city_name) TextView cityName;
    @BindView(R.id.main_layout) RelativeLayout mainLayout;
    @BindView(R.id.adView) AdView adView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    private ForecastPresenterImplementor mPresenter;
    private List<DailyForecast> mDailyForecastList;
    private WeatherRecyclerViewAdapter mAdapter;
    CitySuggestionCreator creator;
    List<CitySuggestion> cities;
    String city;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        mDailyForecastList = new ArrayList<>();
        mAdapter = new WeatherRecyclerViewAdapter(this,mDailyForecastList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(this)
                        .color(Color.WHITE)
                        .size(1)
                        .build());
        mPresenter = new ForecastPresenterImplementor(this);
        city = "Minsk";
        mPresenter.getForecast(city,false);
        mSearchView.setOnQueryChangeListener(this);
        cities = new ArrayList<>();
        creator = new CitySuggestionCreator(this);
        mSearchView.setOnBindSuggestionCallback(this);
        mSearchView.setCloseSearchOnKeyboardDismiss(true);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);
        swipeRefreshLayout.setOnRefreshListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }


    @Override
    public void showCurrentDayTemperature(int temperature) {
        currentDayTemp.setText(String.format("%dº", temperature));
    }

    @Override
    public void showCurrentDayDescription(String description) {
        currentDayDescription.setText(description);
    }

    @Override
    public void showCurrentDayWindSpeed(int speed) {
        currentDaySpeed.setText(String.format("%d mph", speed));
    }

    @Override
    public void showCurrentDayTemperatureRange(int minTemperature, int maxTemperature) {
        currentDayTemperatureRange.setText(String.format("%dº - %dº", minTemperature, maxTemperature));
    }

    @Override
    public void showCurrentDayImage(int imageId) {
        Picasso.with(this).load(imageId).
                into(image);
    }

    @Override
    public void setCurrentDayColor(int color) {
        currentDayLinear.setBackgroundColor(color);
        mSearchView.setBackgroundColor(color);
        mainLayout.setBackgroundColor(color);
    }

    @Override
    public void showWeatherList(List<DailyForecast> weatherList) {
        mDailyForecastList.clear();
        mDailyForecastList.addAll(weatherList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onSearchTextChanged(String oldQuery, String newQuery) {
        Observable<CitySuggestion> observable = creator.getCitiesList(newQuery);
        observable.subscribe(new Subscriber<CitySuggestion>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(CitySuggestion citySuggestion) {
                cities.add(citySuggestion);
                mSearchView.swapSuggestions(cities);

            }
        });
        cities.clear();

    }

    @Override
    public void setCityName(String city) {
        cityName.setText(city);
    }

    @Override
    public void onBindSuggestion(final View suggestionView, ImageView leftIcon, final TextView textView, final SearchSuggestion item, int itemPosition) {

        leftIcon.setColorFilter(getContext().getResources().getColor(R.color.colorTextWhite));
        textView.setTextColor(getContext().getResources().getColor(R.color.colorTextWhite));
        suggestionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                city = item.getBody();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPresenter.getForecast(city,true);
                        Log.i("Forecast", "Getting forecast");

                    }
                });
                mSearchView.clearSearchFocus();
                mSearchView.setSearchText(city);
            }
        });
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        mPresenter.getForecast(city,true);
        swipeRefreshLayout.setRefreshing(false);
    }
}

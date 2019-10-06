package jan.schuettken.bierpongleague.basic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.pm.PackageInfoCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.activities.AddGameActivity;
import jan.schuettken.bierpongleague.activities.ConfirmActivity;
import jan.schuettken.bierpongleague.activities.EloTrendActivity;
import jan.schuettken.bierpongleague.activities.LoginActivity;
import jan.schuettken.bierpongleague.activities.MyAreasActivity;
import jan.schuettken.bierpongleague.activities.OverviewActivity;
import jan.schuettken.bierpongleague.activities.PlayedGamesActivity;
import jan.schuettken.bierpongleague.activities.ScoreboardActivity;
import jan.schuettken.bierpongleague.data.EloData;
import jan.schuettken.bierpongleague.data.UserData;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.PreferencesHandler;

/**
 * Created by Jan Schüttken on 03.11.2018 at 11:50
 */
public abstract class BasicDrawerPage extends BasicPage implements NavigationView.OnNavigationItemSelectedListener {

    protected UserData currentUser = null;
    private LineDataSet lineDataSetPreview;
    private LineChart lineChartPreview;
    private Handler handler;
    private ApiHandler apiHandler;

    @Override
    public void setContentView(int contentView) {
        super.setContentView(contentView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //pass the current user, so it must not be read from the internet all time
        currentUser = (UserData) getObjectParameter(CURRENT_USER);
        if (currentUser == null) {
            ApiHandler apiHandler = createApiHandler();
            try {
                currentUser = apiHandler.getYourself();
            } catch (NoConnectionException | SessionErrorException | JSONException e) {
                e.printStackTrace();
                switchView(LoginActivity.class, true);
                return;
            }
        }

        //fill Header with information
        View head = navigationView.getHeaderView(navigationView.getHeaderCount() - 1);
        TextView welcome = head.findViewById(R.id.text_name);
        welcome.setText(currentUser.getFullName());
        TextView elo = head.findViewById(R.id.text_elo);
        elo.setText(getResString(R.string.your_elo, (int) currentUser.getElo()));

        //set up the add ne game FAB
        findViewById(R.id.fab).setOnClickListener(view -> switchView(AddGameActivity.class));

        handler = new Handler();
        apiHandler = createApiHandler();
        initializeLineChart(head);
        selectPage();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.

        switch (item.getItemId()) {
            case R.id.nav_overview:
                if (!(this instanceof OverviewActivity)) {
                    switchView(OverviewActivity.class, true, new Portable(CURRENT_USER, currentUser));
                }
                break;
            case R.id.nav_played_games:
                if (!(this instanceof PlayedGamesActivity)) {
                    switchView(PlayedGamesActivity.class, true, new Portable(CURRENT_USER, currentUser));
                }
                break;
            case R.id.nav_to_confirm:
                if (!(this instanceof ConfirmActivity)) {
                    switchView(ConfirmActivity.class, true, new Portable(CURRENT_USER, currentUser));
                }
                break;
            case R.id.nav_elo_trend:
                if (!(this instanceof EloTrendActivity)) {
                    switchView(EloTrendActivity.class, true, new Portable(CURRENT_USER, currentUser));
                }
                break;
            case R.id.nav_scoreboard:
                if (!(this instanceof ScoreboardActivity)) {
                    switchView(ScoreboardActivity.class, true, new Portable(CURRENT_USER, currentUser));
                }
                break;
            case R.id.nav_area:
                if (!(this instanceof MyAreasActivity)) {
                    switchView(MyAreasActivity.class, true, new Portable(CURRENT_USER, currentUser));
                }
                break;
            case R.id.nav_share:
                shareApp();
                break;
            case R.id.nav_version:
                showAppVersionToast();
                break;
            case R.id.nav_logout:
                logout();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout() {
        PreferencesHandler preferencesHandler = new PreferencesHandler(this);
        preferencesHandler.setPassword("");
        preferencesHandler.setUsername("");
        preferencesHandler.setSessionId("");
        switchView(LoginActivity.class, true);
    }

    private void showAppVersionToast() {
        PackageManager manager = getPackageManager();
        String versionName;
        int versionCode = -1;
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            versionName = info.versionName;
            versionCode = (int) PackageInfoCompat.getLongVersionCode(info);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionName = "Unknown";
        }

        showToast(getResString(R.string.show_version, versionName, versionCode));
    }

    private void shareApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "*Beerpong League*\nMesse dich mit anderen Beerpong Spielern in einem Rang System\n https://play.google.com/store/apps/details?id=jan.schuettken.bierpongleague");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    /**
     * Call {@link #selectPage(int) selectPage} with the right id
     */
    protected abstract void selectPage();

    /**
     * sets the right menu option as selected (displayed in blue)
     *
     * @param id the R.id.xxx for from the nav
     */
    protected void selectPage(int id) {
        ((NavigationView) findViewById(R.id.nav_view)).getMenu().findItem(id).setChecked(true);
    }


    private void initializeLineChart(View head) {
        lineChartPreview = head.findViewById(R.id.elo_preview_line_chart);

        ArrayList<Entry> lineEntries = new ArrayList<>();
        lineDataSetPreview = new LineDataSet(lineEntries, getResString(R.string.elo_trend));
        lineDataSetPreview.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSetPreview.setHighlightEnabled(false);
        lineDataSetPreview.setLineWidth(2);
        lineDataSetPreview.setColor(getColor(R.color.colorPrimary));
        lineDataSetPreview.setCircleColor(getColor(R.color.colorAccent));
        lineDataSetPreview.setCircleRadius(2);
        lineDataSetPreview.setCircleHoleRadius(1);
        lineDataSetPreview.setDrawCircles(false);
        lineDataSetPreview.setDrawHighlightIndicators(false);
        lineDataSetPreview.setHighLightColor(Color.RED);
        lineDataSetPreview.setValueTextSize(12);
        lineDataSetPreview.setValueTextColor(getColor(R.color.colorBlueDark));
        lineDataSetPreview.setDrawValues(false);
        lineDataSetPreview.setDrawFilled(true);

        LineData lineData = new LineData(lineDataSetPreview);

        lineChartPreview.getDescription().setText("");
        lineChartPreview.getDescription().setTextSize(12);
        lineChartPreview.setDrawMarkers(true);
//        lineChartPreview.setMarker(markerView(context));
//        lineChartPreview.getAxisLeft().addLimitLine(lowerLimitLine(2,"Lower Limit",2,12,getColor("defaultOrange"),getColor("defaultOrange")));
//        lineChartPreview.getAxisLeft().addLimitLine(upperLimitLine(5,"Upper Limit",2,12,getColor("defaultGreen"),getColor("defaultGreen")));
        lineChartPreview.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//        lineChartPreview.animateY(1000);
        lineChartPreview.getXAxis().setGranularityEnabled(false);
        lineChartPreview.getXAxis().setGranularity(1.0f);
        lineChartPreview.getXAxis().setLabelCount(lineDataSetPreview.getEntryCount());
        lineChartPreview.getXAxis().setDrawLabels(false);
        lineChartPreview.getXAxis().setDrawGridLines(false);
        lineChartPreview.getXAxis().setDrawLabels(false);
        lineChartPreview.getAxisLeft().setDrawLabels(false);
        lineChartPreview.getAxisRight().setDrawLabels(false);
        lineChartPreview.getAxisLeft().setDrawGridLines(false);
        lineChartPreview.getAxisRight().setDrawGridLines(false);
        lineChartPreview.setData(lineData);
        lineChartPreview.getLegend().setEnabled(false);

        refreshLineChart(head);
    }

    @SuppressLint("SetTextI18n")
    private void refreshLineChart(final View head) {

        new Thread(() -> {

            try {
                List<EloData> elos = apiHandler.getEloPreview();
                if (elos != null) {
                    final List<Entry> lineEntries = new ArrayList<>();
                    final double startElo = elos.get(0).getValue(), endElo = elos.get(elos.size() - 1).getValue();
                    for (int i = 0; i < elos.size(); i++) {
                        lineEntries.add(new Entry(i + 1, (float) elos.get(i).getValue()));
                    }
                    if (lineEntries.isEmpty()) {
                        lineEntries.add(new Entry(1, 0));
                    }
                    if (!lineEntries.isEmpty()) {
                        lineDataSetPreview.clear();
                        for (Entry e : lineEntries)
                            lineDataSetPreview.addEntry(e);
                        LineData lineData = new LineData(lineDataSetPreview);

                        lineChartPreview.setData(lineData);

                        handler.post(() -> {
                            lineChartPreview.invalidate();
                            TextView tv = head.findViewById(R.id.eloPreviewText);
                            int diff = (int) (endElo - startElo);
                            String prefix = "↑";
                            tv.setTextColor(getColor(R.color.colorGreenLight));
                            if (diff < 0) {
                                prefix = "↓";
                                tv.setTextColor(getColor(R.color.colorRedLight));
                            }
                            tv.setText(prefix + Math.abs(diff));
                        });
                    }
                }
            } catch (JSONException | SessionErrorException |
                    NoConnectionException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

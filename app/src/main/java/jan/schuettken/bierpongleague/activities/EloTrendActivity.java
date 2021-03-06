package jan.schuettken.bierpongleague.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicDrawerPage;
import jan.schuettken.bierpongleague.data.EloData;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.NoGamesException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.handler.ApiHandler;

public class EloTrendActivity extends BasicDrawerPage {

    private Handler handler;
    private ApiHandler apiHandler;
    private LineDataSet lineDataSet;
    private LineChart lineChart;
    /**
     * if showEntriesInSequence is false all entries are added to the line chart with the space of the days in between
     * TODO add to settings
     */
    private boolean showEntriesInSequence = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elo_trend);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.elo_trend);

        handler = new Handler();
        apiHandler = createApiHandler();
        if (apiHandler != null)
            initializeLineChart();
    }

    @Override
    protected void selectPage() {
        selectPage(R.id.nav_elo_trend);
    }

    private void initializeLineChart() {
        lineChart = findViewById(R.id.elo_trend_line_chart);

        ArrayList<Entry> lineEntries = new ArrayList<>();
        lineEntries.add(new Entry(0, 0));
        lineDataSet = new LineDataSet(lineEntries, getResString(R.string.elo_trend));
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(2);
        lineDataSet.setColor(getColor(R.color.colorPrimary));
        lineDataSet.setCircleColor(getColor(R.color.colorAccent));
        lineDataSet.setCircleRadius(4);
        lineDataSet.setCircleHoleRadius(2);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setHighLightColor(Color.RED);
        lineDataSet.setValueTextSize(12);
        lineDataSet.setValueTextColor(getColor(R.color.colorBlueDark));
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawFilled(true);

        LineData lineData = new LineData(lineDataSet);

        lineChart.getDescription().setText("");
        lineChart.getDescription().setTextSize(12);
        lineChart.setDrawMarkers(true);
//        lineChart.setMarker(markerView(context));
//        lineChart.getAxisLeft().addLimitLine(lowerLimitLine(2,"Lower Limit",2,12,getColor("defaultOrange"),getColor("defaultOrange")));
//        lineChart.getAxisLeft().addLimitLine(upperLimitLine(5,"Upper Limit",2,12,getColor("defaultGreen"),getColor("defaultGreen")));
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.animateY(1000);
//        lineChart.getXAxis().setGranularityEnabled(true);
//        lineChart.getXAxis().setGranularity(1.0f);
//        lineChart.getXAxis().setLabelCount(lineDataSet.getEntryCount());
        lineChart.getXAxis().setDrawLabels(true);
        lineChart.getXAxis().setDrawGridLines(true);
        lineChart.setData(lineData);


        refreshLineChart();
    }

    private void refreshLineChart() {

        new Thread(()-> {
                try {
                    List<EloData> elos = apiHandler.getEloLog();
                    final List<Entry> lineEntries = new ArrayList<>();
                    double maxElo = 0, minElo = 0;
                    Date tmpDate = null;
                    for (int i = 0, pos = 0; i < elos.size(); i++) {
                        if (elos.get(i).getValue() > maxElo)
                            maxElo = elos.get(i).getValue();
                        if (elos.get(i).getValue() < minElo)
                            minElo = elos.get(i).getValue();

                        if (elos.get(i).getGameDate() != null) {
                            if (tmpDate != null) {
                                pos += 1 + Math.abs(getDaysCount(tmpDate, elos.get(i).getGameDate()));
                            }
                            tmpDate = elos.get(i).getGameDate();
                            if (showEntriesInSequence)
                                pos = i;
                            lineEntries.add(new Entry(pos, (float) elos.get(i).getValue()));
                        }
                    }
                    if (lineEntries.isEmpty()) {
                        lineEntries.add(new Entry(0, 0));
                    }
                    if (!lineEntries.isEmpty()) {
                        lineDataSet.clear();
                        for (Entry e : lineEntries)
                            lineDataSet.addEntry(e);
                        LineData lineData = new LineData(lineDataSet);

                        lineChart.setData(lineData);

                        final int finalMinElo = (int) minElo, finalMaxElo = (int) maxElo;
                        handler.post(() -> {
                            findViewById(R.id.region_elo_trend).setVisibility(View.VISIBLE);
                            findViewById(R.id.region_no_games).setVisibility(View.GONE);
                            lineChart.invalidate();
                            TextView tv = findViewById(R.id.textViewMinValue);
                            tv.setText(finalMinElo + "");
                            tv = findViewById(R.id.textViewMaxValue);
                            tv.setText(finalMaxElo + "");
                        });
                    } else
                        throw new RuntimeException("No Entries for Data to show!");
                } catch (JSONException | SessionErrorException | NoConnectionException e) {
                    e.printStackTrace();
                } catch (NoGamesException e) {
                    handler.post(() -> {
                        findViewById(R.id.region_elo_trend).setVisibility(View.GONE);
                        findViewById(R.id.region_no_games).setVisibility(View.VISIBLE);
                    });
                }
        }).start();
    }


    public static int getDaysCount(Date begin, Date end) {
        Calendar start = Calendar.getInstance(), finish = Calendar.getInstance();
        start.setTime(begin);
        start.set(Calendar.MILLISECOND, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.HOUR_OF_DAY, 0);

        finish.setTime(end);
        finish.set(Calendar.MILLISECOND, 999);
        finish.set(Calendar.SECOND, 59);
        finish.set(Calendar.MINUTE, 59);
        finish.set(Calendar.HOUR_OF_DAY, 23);

        long delta = finish.getTimeInMillis() - start.getTimeInMillis();
        return (int) Math.ceil(delta / (1000.0 * 60 * 60 * 24));
    }
}

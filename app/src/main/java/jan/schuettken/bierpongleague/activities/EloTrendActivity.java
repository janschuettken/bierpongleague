package jan.schuettken.bierpongleague.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Objects;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicDrawerPage;
import jan.schuettken.bierpongleague.data.EloData;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.handler.ApiHandler;

public class EloTrendActivity extends BasicDrawerPage {

    private Handler handler;
    private ApiHandler apiHandler;
    private LineDataSet lineDataSet;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elo_trend);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.elo_trend);

        handler = new Handler();
        checkApiHandler();

        initializeLineChart();
    }

    private boolean checkApiHandler() {
        apiHandler = createApiHandler();
        return apiHandler != null;
    }

    @Override
    protected void selectPage() {
        selectPage(R.id.nav_elo_trend);
    }

    private void initializeLineChart() {
        lineChart = findViewById(R.id.elo_trend_line_chart);

        ArrayList<Entry> lineEntries = new ArrayList<>();
        lineDataSet = new LineDataSet(lineEntries, getResString(R.string.elo_trend));
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setLineWidth(2);
        lineDataSet.setColor(getColor(R.color.colorPrimary));
        lineDataSet.setCircleColor(getColor(R.color.colorAccent));
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleHoleRadius(3);
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
        lineChart.getXAxis().setGranularityEnabled(true);
        lineChart.getXAxis().setGranularity(1.0f);
        lineChart.getXAxis().setLabelCount(lineDataSet.getEntryCount());
        lineChart.getXAxis().setDrawLabels(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.setData(lineData);

        refreshLineChart();
    }

    private void refreshLineChart() {

        new Thread() {
            @Override
            public void run() {

                try {
                    List<EloData> elos = apiHandler.getEloLog();
                    final List<Entry> lineEntries = new ArrayList<>();
                    double maxElo = 0, minElo = 0;
                    for (int i = 0; i < elos.size(); i++) {
                        if (elos.get(i).getValue() > maxElo)
                            maxElo = elos.get(i).getValue();
                        if (elos.get(i).getValue() < minElo)
                            minElo = elos.get(i).getValue();
                        lineEntries.add(new Entry(i + 1, (float) elos.get(i).getValue()));
                    }
                    if (!lineEntries.isEmpty()) {
                        lineDataSet.clear();
                        for (Entry e : lineEntries)
                            lineDataSet.addEntry(e);
                        LineData lineData = new LineData(lineDataSet);

                        lineChart.setData(lineData);

                        final int finalMinElo = (int) minElo, finalMaxElo = (int) maxElo;
                        handler.post(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                lineChart.invalidate();
                                TextView tv = findViewById(R.id.textViewMinValue);
                                tv.setText(finalMinElo + "");
                                tv = findViewById(R.id.textViewMaxValue);
                                tv.setText(finalMaxElo + "");
                            }
                        });
                    }
                } catch (JSONException | SessionErrorException | NoConnectionException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }
}

package jan.schuettken.bierpongleague.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicDrawerPage;
import jan.schuettken.bierpongleague.custom.MyOnChartValueSelectedListener;
import jan.schuettken.bierpongleague.data.GameData;
import jan.schuettken.bierpongleague.exceptions.NoConnectionException;
import jan.schuettken.bierpongleague.exceptions.NoGamesException;
import jan.schuettken.bierpongleague.exceptions.SessionErrorException;
import jan.schuettken.bierpongleague.handler.ApiHandler;
import jan.schuettken.bierpongleague.handler.ColorFunctionProvider;

public class OverviewActivity extends BasicDrawerPage {

    private ApiHandler apiHandler;
    private PieChart pieChartWinLose;
    private MyOnChartValueSelectedListener pieChartWinLoseClick;

    @Override
    protected void selectPage() {
        selectPage(R.id.nav_overview);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
//        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.played_games);
//        initializeStats();
        initPieChart();
        refreshCharts();
        initializeElo();
    }

    private void initializeBeersDrunk(List<GameData> games) {
        float liters = 0;
        if (games != null) {
            for (GameData g : games) {
                double tmp;
                if (g.isWon()) {
                    tmp = (.5f * ((float) (10 - g.getScores()[0]) / 10.0f));
                } else {
                    tmp = .5f;
                    tmp += (.5 * (((float) g.getScores()[1]) / 10.0));
                }
                liters += tmp;
            }
        }
        final float finalLiters = liters;
        handler.post(() -> {
            TextView tv = findViewById(R.id.beers_drunk);
            tv.setText(getResString(R.string.beers_drunk_all_time, (int) finalLiters));
        });


    }

    @SuppressLint("SetTextI18n")
    private void initializeElo() {
        new Thread(() -> {
            try {
                TextView nameField = findViewById(R.id.name_field);
                TextView eloField = findViewById(R.id.your_elo_text);

                if (currentUser == null) {
                    ApiHandler apiHandler = _createApiHandler();
                    currentUser = apiHandler.getYourself();
                }

                handler.post(() -> {
                    eloField.setText(0 + "");
                    nameField.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                });

                slowlyIncreaseElo(currentUser.getElo(), eloField);
            } catch (SessionErrorException | JSONException | NoConnectionException e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void slowlyIncreaseElo(double elo, final TextView eloField) {
        final double wait = 10.0, steps, ms = 2400.0;
        steps = ms / wait;//should be an int otherwise is the shown elo not exactly precise

        final double increase = elo / steps;
        for (int i = 0; i <= steps; i++) {
            try {
                Thread.sleep((long) wait);
            } catch (InterruptedException ignored) {
            }
            final int finalI = i;
            handler.post(() -> eloField.setText(((int) (increase * finalI)) + ""));

        }

    }

    private void initPieChart() {
        pieChartWinLose = findViewById(R.id.pie_chart_category_out);
        pieChartWinLose.setUsePercentValues(true);
        pieChartWinLose.getDescription().setEnabled(false);
//        pieChartWinLose.setExtraOffsets(5, 10, 5, 5);
        pieChartWinLose.setExtraOffsets(0, 0, 0, 0);

        pieChartWinLose.setDragDecelerationFrictionCoef(0.95f);

//        pieChartWinLose.setCenterTextTypeface(mTfLight);
        pieChartWinLose.setCenterText(generateCenterSpannableText());

        pieChartWinLose.setDrawHoleEnabled(true);
        pieChartWinLose.setHoleColor(Color.WHITE);

        pieChartWinLose.setTransparentCircleColor(Color.WHITE);
        pieChartWinLose.setTransparentCircleAlpha(110);

        pieChartWinLose.setHoleRadius(68f);
        pieChartWinLose.setTransparentCircleRadius(61f);

        pieChartWinLose.setDrawCenterText(true);

        pieChartWinLose.setRotationAngle(0);
        // enable rotation of the chart by touch
        pieChartWinLose.setRotationEnabled(false);
        pieChartWinLose.setHighlightPerTapEnabled(true);

        // pieChartWinLose.setUnit(" €");
        // pieChartWinLose.setDrawUnitsInChart(true);

        // add a selection listener
        pieChartWinLoseClick = new MyOnChartValueSelectedListener(pieChartWinLose, getResString(R.string.win_ratio), getResString(R.string.games_all_time));
        pieChartWinLose.setOnChartValueSelectedListener(pieChartWinLoseClick);

        pieChartWinLose.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        // pieChartWinLose.spin(2000, 0, 360);

        Legend l = pieChartWinLose.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setWordWrapEnabled(true);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(0f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setEnabled(false);

        // entry label styling
        pieChartWinLose.setEntryLabelColor(Color.BLACK);
//        pieChartWinLose.setEntryLabelTypeface(mTfRegular);
        pieChartWinLose.setEntryLabelTextSize(12f);
        pieChartWinLose.setDrawEntryLabels(false);
    }

    private void refreshCharts() {
        new Thread() {
            @Override
            public void run() {
                try {
                    if (apiHandler == null)
                        apiHandler = createApiHandler();
                    final List<GameData> games = apiHandler.getGames(apiHandler.getYourself(), true);
                    initializeBeersDrunk(games);
                    handler.post(() -> {
                        if (games != null) {
                            findViewById(R.id.region_win_lose_stats).setVisibility(View.VISIBLE);
                            setItemDataCategoryChart(games);
                            pieChartWinLoseClick.onNothingSelected();
                            pieChartWinLose.invalidate();
                            pieChartWinLose.animateX(2500);
                        }

                    });
                } catch (NoConnectionException | SessionErrorException | JSONException | NoGamesException e) {
                    initializeBeersDrunk(null);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //don't show the win/lose stats if no games have been played
                            findViewById(R.id.region_win_lose_stats).setVisibility(View.GONE);
                        }
                    });
                }
            }
        }.start();
    }

    private void setItemDataCategoryChart(List<GameData> games) {
        if (games == null || games.isEmpty())
            return;
        List<Double> WinLoseRatio = new ArrayList<>();
        WinLoseRatio.add(0.0);//Wins
        WinLoseRatio.add(0.0);//Loses

        for (GameData i : games) {

            if (i.getScores()[0] > 0) {
                WinLoseRatio.set(0, WinLoseRatio.get(0) + 1);
            } else {
                WinLoseRatio.set(1, WinLoseRatio.get(1) + 1);
            }

        }
        pieChartWinLoseClick.setAllTime(games.size());
        float percent;
        if (WinLoseRatio.get(1) > 0)
            percent = (float) (WinLoseRatio.get(0) / WinLoseRatio.get(1));
        else
            percent = WinLoseRatio.get(0).floatValue();

        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        pieChartWinLoseClick.setPercent(getResString(R.string.win_ratio) + ": " + decimalFormat.format(percent));
        ArrayList<Integer> colorsOut = new ArrayList<>();
        ArrayList<PieEntry> entriesOut = new ArrayList<>();

        String textWin = "Gewonnen", textLose = "Verloren";

        for (int n = 0; n < 2; n++) {

            int colorARGB;
            if (n == 0) {
                PieEntry pe = new PieEntry(WinLoseRatio.get(n).floatValue(), textWin);
                entriesOut.add(pe);
                colorARGB = ColorFunctionProvider.setColor(0, 1, 0);
            } else {
                PieEntry pe = new PieEntry(WinLoseRatio.get(n).floatValue(), textLose);
                entriesOut.add(pe);
                colorARGB = ColorFunctionProvider.setColor(1, 0, 0);
            }

            colorARGB = ColorFunctionProvider.setAlphaToNull(colorARGB);
            colorsOut.add(colorARGB);
        }
        pieChartWinLoseClick.setEntries(entriesOut);
        PieDataSet dataSetOut = new PieDataSet(entriesOut, "");

        dataSetOut.setDrawIcons(false);
        dataSetOut.setSliceSpace(3f);
        dataSetOut.setIconsOffset(new MPPointF(0, 40));
        dataSetOut.setSelectionShift(5f);

        dataSetOut.setColors(colorsOut);
        //dataSetOut.setSelectionShift(0f);

        PieData dataOut = new PieData(dataSetOut);
        dataOut.setValueFormatter(new PercentFormatter());
        dataOut.setValueTextSize(13f);
        dataOut.setValueTextColor(Color.DKGRAY);
        pieChartWinLose.setEntryLabelColor(Color.BLACK);
        pieChartWinLose.setDrawingCacheBackgroundColor(Color.BLACK);
        pieChartWinLose.setNoDataTextColor(Color.BLACK);
//        dataOut.setValueTypeface(mTfLight);
        pieChartWinLose.setCenterText(MyOnChartValueSelectedListener.generateCenterSpannableText(getString(R.string.games_all_time) + "\n" + games.size()));
        pieChartWinLose.setData(dataOut);
        // undo all highlights
        pieChartWinLose.highlightValues(null);

        for (IDataSet<?> set : pieChartWinLose.getData().getDataSets())
            set.setDrawValues(false);
    }

    private SpannableString generateCenterSpannableText() {
        return MyOnChartValueSelectedListener.generateCenterSpannableText("");
    }
}

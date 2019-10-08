package jan.schuettken.bierpongleague.custom;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

/**
 * Created by Jan Schüttken on 27.09.2018 at 01:42
 */
public class MyOnChartValueSelectedListener implements OnChartValueSelectedListener {

    private PieChart pieChart;
    private float allTime;
    private ArrayList<PieEntry> entries;
    private String percent, base;

    public MyOnChartValueSelectedListener(PieChart pieChart, String percent, String base) {
        this.pieChart = pieChart;
        this.percent = percent;
        this.base = base;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        //DO NOTHING
        try {
            pieChart.setCenterText(generateCenterSpannableText(entries.get((int) h.getX()).getLabel() + "\n" + ((int) h.getY())));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onNothingSelected() {
        try {
            pieChart.setCenterText(generateCenterSpannableText(base + "\n" + ((int) allTime) + "\n" + percent));
        } catch (Exception ignored) {
        }
    }

    public static SpannableString generateCenterSpannableText(String text) {

        int startSecond = text.split("\n")[0].length();

        SpannableString s = new SpannableString(text);
        s.setSpan(new RelativeSizeSpan(1.7f), 0, startSecond, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), startSecond, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), startSecond, s.length(), 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), startSecond, s.length(), 0);
        try {
            int startThird = text.split("\n")[1].length();
            s.setSpan(new RelativeSizeSpan(1.9f), startSecond, startSecond + startThird + 1, 0);
            s.setSpan(new RelativeSizeSpan(.9f), startSecond + startThird + 1, s.length(), 0);
        } catch (IndexOutOfBoundsException e) {
            s.setSpan(new RelativeSizeSpan(2.5f), startSecond, s.length(), 0);
        }


        //s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 8, s.length(), 0);
        return s;
    }

    public PieChart getPieChart() {
        return pieChart;
    }

    public void setPieChart(PieChart pieChart) {
        this.pieChart = pieChart;
    }

    public float getAllTime() {
        return allTime;
    }

    public void setAllTime(float allTime) {
        this.allTime = allTime;
    }

    public ArrayList<PieEntry> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<PieEntry> entries) {
        this.entries = entries;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }
}

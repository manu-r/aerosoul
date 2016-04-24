package com.nasa_sac.aerosoul.aerosoul;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.Console;
import java.util.ArrayList;
import java.util.logging.Logger;

public class TrendAnalysisActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend_analysis);

        LineChart lineChart = (LineChart) findViewById(R.id.trendAnalysisChart);
        lineChart.setDragEnabled(true);

        ArrayList<Entry> entries = new ArrayList<Entry>();
        ArrayList<String> xValues = new ArrayList<String>();
        for( int i=0; i < 10; i++) {
            Entry entry = new Entry((int)(Math.random() * 10), i);
            entries.add(entry);
            xValues.add("Month " +i);
        }

        LineDataSet apiValues = new LineDataSet(entries, "API");
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(apiValues);

        Log.d("Trend, entries", "" +entries.size());
        Log.d("Trend, xValues", "" +xValues.size());

        LineData lineData = new LineData(xValues, dataSets);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }
}

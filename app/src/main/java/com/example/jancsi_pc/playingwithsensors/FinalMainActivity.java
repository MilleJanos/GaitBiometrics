package com.example.jancsi_pc.playingwithsensors;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class FinalMainActivity extends AppCompatActivity { //extend AdsFragmentActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_main);
    }
}

////////////////////////// IDEIGLENES A FELETTI

/*
package com.example.jancsi_pc.playingwithsensors;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FinalMainActivity extends AppCompatActivity { //extend AdsFragmentActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_main);

        //help: https://www.youtube.com/watch?v=xLaC0Z6wej0
        //drawLineChart();
    }


    private void drawLineChart(){
        LineChart lineChart = findViewById(R.id.myLineChart);
        Description description = new Description();
        description.setText("Changes of coordinates");
        lineChart.setDescription(description);

        LineDataSet lineDataSet = new LineDataSet(getDataSet(), "Changes of coordinates");
        lineDataSet.setDrawFilled(true);
        lineDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        LineData lineData = new LineData(lineDataSet);
        lineData.setValueFormatter(new RepoChartXAxisValueFormatter(getXAxisValues()));

        lineChart.setData(lineData);
        lineChart.animateXY(2000,2000);
        lineChart.invalidate();

    }

    private List<Entry> getDataSet(){
        List<Entry> entries = new ArrayList<Entry>();
        //ide jonnek majd a lekert xyz koordinatak, ido fuggvenyeben
        entries.add(new Entry(4f,0));
        entries.add(new Entry(8f,0));
        entries.add(new Entry(6f,2));
        entries.add(new Entry(12f,3));
        entries.add(new Entry(10f,4));
        entries.add(new Entry(9f,5));
        return entries;
    }

    //TODO ezt ki kell cserélni, hogy az adatokat mutassa, dinamikusan
    private List<String> getXAxisValues(){
        List<String> xAxis = new ArrayList<String>();
        xAxis.add("JAN");
        xAxis.add("FEB");
        xAxis.add("MAR");
        xAxis.add("APR");
        xAxis.add("MAY");
        xAxis.add("JUN");
        return xAxis;
    }

    private class RepoChartXAxisValueFormatter implements IValueFormatter{

        private List<String> labels;

        public RepoChartXAxisValueFormatter(List<String> labels){
            this.labels = labels;
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            try{
                int index = (int) value;
                return this.labels.get(index);
            }
            catch (Exception e){
                return null;
            }
        }
    }

}
*/
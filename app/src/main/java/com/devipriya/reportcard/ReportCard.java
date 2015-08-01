package com.devipriya.reportcard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Arrays;


public class ReportCard extends AppCompatActivity {

    int settingsDec, settingsPass;
    String dpResult, dpRank;
    SharedPreferences spSettings;
    SharedPreferences spSubject;
    SharedPreferences spTest;
    SharedPreferences spMarks;
    TextView dpTitleText;
    RelativeLayout dpRelativeLayout;
    int subSize, testSize, flag = 0;
    RelativeLayout.LayoutParams params;
    RelativeLayout.LayoutParams nParams;
    TextView cell;

    int maxSub, overallTotalObt, overallMaxMarks;
    int[] totTest, maxTest, totSub;
    float tempOverallPercent;
    float[] tempPercentTest, tempPercentSub;
    BigDecimal percentTest[], percentSub[];
    BigDecimal overallPercent, dpPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_card);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //get default decimal places set by user in settings. Set 2 if nothing is set
        spSettings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String tempDec = spSettings.getString("prefDecimalPlaces", "2");
        if(tempDec.equals(""))
            tempDec = "2";
        settingsDec = Integer.parseInt(tempDec);

        //get default pass percent set by user in settings. Set 30% if nothing is set
        dpPass = BigDecimal.ZERO;
        String tempPass = spSettings.getString("prefPassPercent", "30");
        if(tempPass.equals(""))
            tempPass = "30";
        settingsPass = Integer.parseInt(tempPass);
        dpPass = new BigDecimal(settingsPass);

        spSubject = getSharedPreferences("SUBJECT_LIST", Context.MODE_PRIVATE);
        spTest = getSharedPreferences("TEST_LIST", Context.MODE_PRIVATE);

        //get subject size and test size
        subSize = spSubject.getInt("subject_size", 0);
        testSize = spTest.getInt("test_size", 0);

        spMarks = getSharedPreferences("MARKS_LIST", Context.MODE_PRIVATE);

        //find view by ids
        dpRelativeLayout = (RelativeLayout) findViewById(R.id.dpRelativeLayout);
        dpTitleText = (TextView) findViewById(R.id.dpTitleText);

        //calculate total marks and % obtained in each test
        totTest = new int[testSize];
        maxTest = new int[testSize];
        tempPercentTest = new float[testSize];
        percentTest = new BigDecimal[testSize];
        Arrays.fill(totTest, 0);
        Arrays.fill(maxTest, 0);
        Arrays.fill(tempPercentTest, 0);
        Arrays.fill(percentTest, BigDecimal.ZERO);

        for(int i=0; i < testSize; i++) {
            maxTest[i] = (subSize * (spTest.getInt("testMaxMarks_"+i, 0)));
            for (int j = 0; j < subSize; j++) {
                totTest[i] = totTest[i] + spMarks.getInt("marks_" + i + "_" + j, 0);
            }
            tempPercentTest[i] = ((float) totTest[i] / ((float) maxTest[i])) * 100;
            percentTest[i] = new BigDecimal(tempPercentTest[i]);
            percentTest[i] = percentTest[i].setScale(settingsDec, BigDecimal.ROUND_HALF_EVEN);
        }

        //calculate total marks and % obtained in each subject
        totSub = new int[subSize];
        maxSub = 0;
        for(int i=0 ; i < testSize; i++)
            maxSub = maxSub + spTest.getInt("testMaxMarks_" + i, 0);
        tempPercentSub = new float[subSize];
        percentSub = new BigDecimal[subSize];
        Arrays.fill(totSub, 0);
        Arrays.fill(tempPercentSub,0);
        Arrays.fill(percentSub, BigDecimal.ZERO);

        for(int i=0; i < subSize; i++) {
            for (int j = 0; j < testSize; j++) {
                totSub[i] = totSub[i] + spMarks.getInt("marks_" + j + "_" + i, 0);
            }
            tempPercentSub[i] = ((float)totSub[i]/(float)maxSub)*100;
            percentSub[i] = new BigDecimal(tempPercentSub[i]);
            percentSub[i] = percentSub[i].setScale(settingsDec, BigDecimal.ROUND_HALF_EVEN);
        }

        //calculate overall marks and overall %
        overallTotalObt = 0;
        overallMaxMarks = 0;
        tempOverallPercent = 0;
        overallPercent = BigDecimal.ZERO;
        for(int i=0; i < testSize; i++){
            overallTotalObt = overallTotalObt + totTest[i];
            overallMaxMarks = overallMaxMarks + maxTest[i];
        }
        tempOverallPercent = ((float) overallTotalObt / (float) overallMaxMarks) * 100;
        overallPercent = new BigDecimal(tempOverallPercent);
        overallPercent = overallPercent.setScale(settingsDec, BigDecimal.ROUND_HALF_EVEN);

        //deciding result and rank
        calculate();

        //display report card view
        LinearLayout[] row = new LinearLayout[subSize+4];

        for(int i=0; i < subSize+4; i++) {
            row[i] = new LinearLayout(this);
            row[i].setOrientation(LinearLayout.HORIZONTAL);
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            if(i == 0) {
                params.addRule(RelativeLayout.BELOW, dpTitleText.getId());
            }
            else {
                params.addRule(RelativeLayout.BELOW, i);
            }
            row[i].setId(i + 1);
            row[i].setLayoutParams(params);
            if(i == 0) {
                //row for top headings
                for(int k=0; k < testSize+1; k++){
                    cell = new TextView(new ContextThemeWrapper(this, R.style.Cell_Style));
                    if(k==0)
                        cell.setText("");
                    else
                        cell.setText(spTest.getString("testName_"+(k-1), ""));
                    row[i].addView(cell);
                }
                cell = new TextView(new ContextThemeWrapper(this, R.style.Cell_Style));
                cell.setText("SUBJECT TOTAL");
                row[i].addView(cell);
                cell = new TextView(new ContextThemeWrapper(this, R.style.Cell_Style));
                cell.setText("SUBJECT %");
                row[i].addView(cell);
                dpRelativeLayout.addView(row[i]);
            }
            else if(i == 1){
                //row for maximum marks
                for(int k=0; k < testSize+1; k++){
                    cell = new TextView(new ContextThemeWrapper(this, R.style.Cell_Style));
                    if(k==0)
                        cell.setText("MAX MARKS");
                    else
                        cell.setText(String.valueOf(spTest.getInt("testMaxMarks_" + (k - 1), 0)));
                    row[i].addView(cell);
                }
                cell = new TextView(new ContextThemeWrapper(this, R.style.Cell_Style));
                cell.setText(String.valueOf(maxSub));
                row[i].addView(cell);
                cell = new TextView(new ContextThemeWrapper(this, R.style.Cell_Style));
                cell.setText("");
                row[i].addView(cell);
                dpRelativeLayout.addView(row[i]);
            }
            else if(i == subSize+2){
                //row for test total
                for(int k=0; k < testSize+1; k++){
                    cell = new TextView(new ContextThemeWrapper(this, R.style.Cell_Style));
                    if(k==0)
                        cell.setText("TEST TOTAL");
                    else
                        cell.setText(String.valueOf(totTest[k-1]));
                    row[i].addView(cell);
                }
                cell = new TextView(new ContextThemeWrapper(this, R.style.Double_Cell_Style));
                cell.setText(String.valueOf(overallTotalObt));
                row[i].addView(cell);
                dpRelativeLayout.addView(row[i]);
            }
            else if(i == subSize+3){
                //row for test percentage
                for(int k=0; k < testSize+1; k++){
                    cell = new TextView(new ContextThemeWrapper(this, R.style.Cell_Style));
                    if(k==0)
                        cell.setText("TEST %");
                    else
                        cell.setText((String.valueOf(percentTest[k-1]))+"%");
                    row[i].addView(cell);
                }
                cell = new TextView(new ContextThemeWrapper(this, R.style.Double_Cell_Style));
                cell.setText((String.valueOf(overallPercent))+"%");
                row[i].addView(cell);
                dpRelativeLayout.addView(row[i]);
            }
            else{
                //rows for marks in table format
                for(int k=0; k < testSize+1; k++){
                    cell = new TextView(new ContextThemeWrapper(this, R.style.Cell_Style));
                    if(k==0)
                        cell.setText(spSubject.getString("subject_"+(i-2), ""));
                    else
                        cell.setText(String.valueOf(spMarks.getInt("marks_" + (k - 1) + "_" + (i - 2) , 0)));
                    row[i].addView(cell);
                }
                cell = new TextView(new ContextThemeWrapper(this, R.style.Cell_Style));
                cell.setText(String.valueOf(totSub[i-2]));
                row[i].addView(cell);
                cell = new TextView(new ContextThemeWrapper(this, R.style.Cell_Style));
                cell.setText((String.valueOf(percentSub[i-2]))+"%");
                row[i].addView(cell);
                dpRelativeLayout.addView(row[i]);
            }
        }

        //display last four text views
        for(int m=0; m < 4; m++){
            //total marks
            nParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            nParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            if(m == 0) {
                cell = new TextView(new ContextThemeWrapper(this, R.style.Last_Note_Style));
                cell.setGravity(Gravity.CENTER);
                cell.setText("TOTAL MARKS OBTAINED: " + (String.valueOf(overallTotalObt)) + " OUT OF " + (String.valueOf(overallMaxMarks)) + " .");
                nParams.addRule(RelativeLayout.BELOW, subSize + 4);
                cell.setId(subSize + 4 + 1);
                cell.setLayoutParams(nParams);
                dpRelativeLayout.addView(cell);
            }
            else if(m == 1){
                //total percentage
                cell = new TextView(new ContextThemeWrapper(this, R.style.Last_Note_Style));
                cell.setGravity(Gravity.CENTER);
                cell.setText("TOTAL PERCENTAGE: " + (String.valueOf(overallPercent)) + " % .");
                nParams.addRule(RelativeLayout.BELOW, subSize + 4 + 1);
                cell.setId(subSize + 4 + 1 + 1);
                cell.setLayoutParams(nParams);
                dpRelativeLayout.addView(cell);
            }
            else if(m == 2){
                //pass or fail
                cell = new TextView(new ContextThemeWrapper(this, R.style.Last_Note_Style));
                cell.setGravity(Gravity.CENTER);
                cell.setText("RESULT: " + dpResult);
                nParams.addRule(RelativeLayout.BELOW, subSize + 4 + 2);
                cell.setId(subSize + 4 + 1 + 2);
                cell.setLayoutParams(nParams);
                dpRelativeLayout.addView(cell);
            }
            else if(m == 3){
                //rank secured
                cell = new TextView(new ContextThemeWrapper(this, R.style.Last_Note_Style));
                cell.setGravity(Gravity.CENTER);
                cell.setText("RANK: " + dpRank);
                nParams.addRule(RelativeLayout.BELOW, subSize + 4 + 3);
                cell.setId(subSize + 4 + 1 + 3);
                cell.setLayoutParams(nParams);
                dpRelativeLayout.addView(cell);
            }
        }
    }

    //deciding result and rank
    void calculate(){
        //result: pass or fail
        if(overallPercent.compareTo(dpPass) >= 0){
            dpResult = "PASS";
        }
        else
            dpResult = "FAIL";

        if(flag == 1) {
            //ranks: FCD or FC or...
        }
    }
}
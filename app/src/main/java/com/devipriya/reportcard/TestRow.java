package com.devipriya.reportcard;

/**
 * Created by Devipriya on 7/9/2015.
 */
public class TestRow {

    public String testName;
    public int testMaxMarks;

    public TestRow() {
        this.testName = "";
        this.testMaxMarks = 0;
    }

    public TestRow(String testName, int testMaxMarks) {
        this.testName = testName;
        this.testMaxMarks = testMaxMarks;
    }


    public String getTestName(){
        return testName;
    }

    public int getTestMaxMarks(){
        return testMaxMarks;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public void setTestMaxMarks(int testMaxMarks) {
        this.testMaxMarks = testMaxMarks;
    }
}

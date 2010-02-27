package org.ict.classifier.cca;

public class Parameters {
	
	public String clusterizatorType;
    public int cellsize;
    public double densityThreshold;
    public int emptyThreshold;
    public boolean forCCA;

    public int mMin;
    public int mDiff;
    public int mCol;
    public double stopCrit;

    public double backgroundpercent;
    public boolean formClusterStructure;

    public Parameters()
    {
        cellsize = 0;
        densityThreshold = 0;
        emptyThreshold = 0;
        backgroundpercent = 0;
        forCCA = true;
        mMin = 0;
        mDiff = 0;
        mCol = 0;
        clusterizatorType = "";
        formClusterStructure = false;
    }

}

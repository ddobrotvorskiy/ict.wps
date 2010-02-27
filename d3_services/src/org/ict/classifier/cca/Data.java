package org.ict.classifier.cca;
import java.util.Vector;


public class Data{
	 // class for image dataset
	
	private int n;                // width and height of image
    private int d;                        // data dimension
    public DataPoint[] pointsArray;       // array of data points
    public Vector bands;
    public int startPointX;
    public int starPointY;
    public int vw, vh;
    
    public Data(int n_, int d_)
    {
        n = n_;
        d = d_;
        pointsArray = new DataPoint[n];
        for (int i = 0; i < n; i++)
        {
            pointsArray[i] = new DataPoint(d);
        }
        bands = new Vector <Integer>();
    }

    public int getNData()
    {
        return n;
    }

    public int getDData()
    {
        return d;
    }

    // add points from dataPoints to data
    public void addDataPointsToData(Data data, Data dataPoints)
    {
        int nData = data.getNData();
        int nDataPoints = dataPoints.getNData();

        Data newData = new Data(nData + nDataPoints, data.getDData());
        for (int i = 0; i < nData; i++)
        {
            newData.pointsArray[i] = data.pointsArray[i];
        }
        for (int i = 0; i < nDataPoints; i++)
        {
            newData.pointsArray[nData + i] = dataPoints.pointsArray[i];
        }
        int nNew = nData + nDataPoints; 
        data = null;
        data = new Data(nNew, dataPoints.getDData());
        for (int i = 0; i < nNew; i++)
        {
            data.pointsArray[i] = newData.pointsArray[i];
        }
        newData = null;
    }

    public void addRangeToData(DataPoint[] dataPoints)
    {
        int nData = n;
        int nDataPoints = dataPoints.length;
        int count = nData + nDataPoints;

        DataPoint[] newDataPoint = new DataPoint[count];
        for (int i = 0; i < nData; i++)
        {
            newDataPoint[i] = pointsArray[i];
        }
        for (int i = 0; i < nDataPoints; i++)
        {
            newDataPoint[nData + i] = dataPoints[i];
        }
        pointsArray = newDataPoint;
        n = count;
    }

    public void addToData(DataPoint dataPoints)
    {
        int nData = n;
        int nDataPoints = 1;
        int count = nData + nDataPoints;

        DataPoint[] newDataPoint = new DataPoint[count];
        for (int i = 0; i < nData; i++)
        {
            newDataPoint[i] = pointsArray[i];
        }
        for (int i = 0; i < nDataPoints; i++)
        {
            newDataPoint[nData + i] = dataPoints;
        }
        pointsArray = newDataPoint;
        n = count;
    }

    public void copy(DataPoint point)
    {
        int d = point.getD();
        DataPoint dp = new DataPoint(d);
        for (int j = 0; j < d; j++)
            dp.coordinates[j] = point.coordinates[j];
        dp.clusterNumber = point.clusterNumber;
        dp.classNumber = point.classNumber;
        addToData(dp);
    }

}

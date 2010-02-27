package org.ict.classifier.cca;

public class DataPoint {
	// class for data point
    public int[] coordinates;          // point coordinates
    public int classNumber;
    public int clusterNumber;

    public DataPoint(int d)
    {
        coordinates = new int[d];
        for (int i = 0; i < d; i++)
            coordinates[i] = -1;
        classNumber = -1;
    }

    public int getD()
    {
        return coordinates.length;
    }
}

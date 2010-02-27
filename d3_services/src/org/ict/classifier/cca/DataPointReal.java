package org.ict.classifier.cca;

public class DataPointReal {
	// class for data point
	
	public double[] coordinates;          // point coordinates

    public DataPointReal(int d)
    {
        coordinates = new double[d];
        for (int i = 0; i < d; i++)
            coordinates[i] = -1;
    }

}

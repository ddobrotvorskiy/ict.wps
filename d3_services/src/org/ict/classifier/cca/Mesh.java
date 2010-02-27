package org.ict.classifier.cca;

public class Mesh {
	
    public int m;
    public int d;
    
    public int cellCount()
    {
        return (int)(Math.round( Math.pow((double) m, (double) d) ));
    }

    public Mesh(int m, int d)
    {
        this.m = m;
        this.d = d;
    }

    public Mesh()
    {
        m = -1;
        d = -1;
    }

	public double diap() {
		return 256;
	}

}

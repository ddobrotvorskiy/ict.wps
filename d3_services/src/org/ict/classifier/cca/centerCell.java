package org.ict.classifier.cca;

public class centerCell {
	
	public int ind;
    public Mesh mh;
    public int it;
    public int absCentr;
    public int numCompSv;
    
    public centerCell(int ind, Mesh mh, int it, int absCentr, int numCompSv)
    {
        this.ind = ind;
        this.mh = mh;
        this.it = it;
        this.absCentr = absCentr;
        this.numCompSv = numCompSv;
    }
    
    public centerCell(int ind, Mesh mh)
    {
        this.ind = ind;
        this.mh = mh;
    }

    public centerCell()
    {
        ind = -1;
        mh = new Mesh();
    }

}

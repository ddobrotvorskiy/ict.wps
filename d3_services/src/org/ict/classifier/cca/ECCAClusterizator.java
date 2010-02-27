package org.ict.classifier.cca;

import java.util.Vector;

public class ECCAClusterizator {
	
	public int absNumCenters;
	public int kolCluster;
	
	public void clustering(Data data, Parameters algorithmParameters)
	{
		absNumCenters = 0;
		
		int n = data.getNData();
	    int d = data.getDData();
	    int algCount = algorithmParameters.mCol;
	    
		int[][] clusters = new int[algCount][];
		int[][] kletki = new int[algCount][];
		Vector<centerCell> centerList = new Vector <centerCell>();
		Vector<centerCell> centerList2 = new Vector <centerCell>();
		
		algorithmParameters.forCCA = false;
		
		int it=0;
			algorithmParameters.cellsize = algorithmParameters.mMin;
			CCAClusterizator cca = new CCAClusterizator();
			cca.clustering(data, algorithmParameters);
			
			clusters[it] = cca.clustered;
			kletki[it] = cca.clusters;
			
			Mesh mh = new Mesh(algorithmParameters.cellsize, d);
			for (int i = 0; i < cca.centers.length; i++)
            {
                centerCell c = new centerCell(cca.centers[i], mh, it, i, i);
                centerList.add(c);
                absNumCenters++;
            }	
		
		for (it=1; it<(algCount-1); it++)	
		{
			algorithmParameters.cellsize = algorithmParameters.mMin + it*algorithmParameters.mDiff;
			cca = new CCAClusterizator();
			cca.clustering(data, algorithmParameters);
			
			clusters[it] = cca.clustered;
			kletki[it] = cca.clusters;
			
			mh = new Mesh(algorithmParameters.cellsize, d);
			addCenters(mh, centerList, centerList2, cca.centers, it);
		}
		
		it = (algCount-1);
			algorithmParameters.cellsize = algorithmParameters.mMin + it*algorithmParameters.mDiff;
			cca = new CCAClusterizator();
			cca.clustering(data, algorithmParameters);
			
			clusters[it] = cca.clustered;
			kletki[it] = cca.clusters;
			
			mh = new Mesh(algorithmParameters.cellsize, d);
			int[] sootv = new int [cca.centers.length];
			addLastCenters(mh, centerList, centerList2, cca.centers, sootv, it);
		
			
		int[][] priter = new int[absNumCenters][algCount];
		for (int i=0; i<absNumCenters; i++)
			for (int j=0; j<algCount; j++)
				priter[i][j] = -1;
		
		for (int i=0; i<centerList.size(); i++)
		{
			centerCell cell;
			cell = (centerCell) centerList.get(i);
			priter[cell.absCentr][cell.it] = cell.numCompSv;			
		}
		for (int i=0; i<centerList2.size(); i++)
		{
			centerCell cell;
			cell = (centerCell) centerList2.get(i);
			priter[cell.absCentr][cell.it] = cell.numCompSv;
		}
		
        int m = algorithmParameters.mMin;
        for (int j=0; j<absNumCenters; j++)
            for (it=0; it<algCount; it++)
                if (priter[j][it] == -1)       
                {
                    int[] numList = findCellListCovering( centerList.get(j), (m + it*algorithmParameters.mDiff));
                    int num = -2;
                    for (int l = 0; l < numList.length; l++)
                    {
                    	if (numList[l] >= 0)
                    	{
                    		if (clusters[it][numList[l]] >= 0)
                            {
                                num = clusters[it][numList[l]];
                                break;
                            }
                    	}                    	
                    }                        
                    if (num >= 0)
                    	priter[j][it] = num;  
                    else 
                    	priter[j][it] = -2;
                }
        
        double[][] matrdist = new double[absNumCenters][absNumCenters];
        
        for (it = 0; it < algCount; it++)
            for (int i = 0; i < absNumCenters; i++)
                for (int j = i + 1; j < absNumCenters; j++)
                {
                    if ((priter[i][it] < 0) || (priter[j][it] < 0))
                    	matrdist[i][j]++;
                    else 
                    {
                    	if ((kletki[it][priter[i][it]] != kletki[it][priter[j][it]]))
                    		matrdist[i][j]++;
                    }
                }
        for (int i = 0; i < absNumCenters; i++)
            for (int j = i + 1; j < absNumCenters; j++)
                matrdist[i][j] = matrdist[i][j] / (algCount + 1);
        
        DecisionSerge dendr = new DecisionSerge(algorithmParameters.stopCrit);
        dendr.dendrogramma_plus(matrdist);
        
        kolCluster = dendr.numOfClusters;

        for ( int i = 0; i < n; i++ )        	
        	data.pointsArray[i].classNumber = dendr.cl_num[ sootv[ clusters[algCount-1][ cca.kletka[i]]]];
		
	}

	
	private int[] findCellListCovering(centerCell center, int m) {

		int d = center.mh.d;
        Mesh mh = new Mesh(m, d);
        int count = (int)Math.round(Math.pow(2, d));
        int numberOfCells = (int)(Math.pow((double)m, (double)d));
        int[] numList = new int[count];
        int[] inum;
        int cellcur = -1;

        double[][] xtmp = findVertexes(center);
        for (int j = 0; j < count; j++)
        {
        	inum = findCell(xtmp[j], mh);
        	cellcur = funcNumber1(inum, mh);
            if ((cellcur >= 0) && (cellcur < numberOfCells))
            	numList[j] = cellcur;
            else
            	numList[j] = -1;
        }
		return numList;
	}


	private double[][] findVertexes(centerCell center) {
		int d = center.mh.d;
        int[] inum = new int[d + 1];
        CCAClusterizator.iNumCreate(center.ind, center.mh.m, inum, d);
        double[][] xall = new double[2][d];
        for (int j = 1; j <= d; j++)
        {
            xall[0][j - 1] = findCellSide(inum, -j, center.mh);
            xall[1][j - 1] = findCellSide(inum, j, center.mh);
        }
        double[][] x = new double[(int)Math.round( Math.pow(2, d))][d];
        int[] a = new int[ d + 1 ];
        int num = 0;
        while ( a[0] != 1 )
        {
            for (int j = 0; j < d; j++)
                x[num][j] = xall[a[j + 1]][j];
            CCAClusterizator.aplus11(a, d, 0, 1, false);
            num++;
        }
        
        return x;
	}


	private double findCellSide(int[] inum, int coord, Mesh mh) {
		int m = mh.m;
        int diap = (int)(mh.diap());
        if ((m * diap) > 0)
        {
            if (coord > 0)
                return (double) diap / m * (inum[coord] + 1);
            else return (double) diap / m * inum[-coord];
        }
        else return -1;
	}


	private int funcNumber1(int[] inum, Mesh mh) {
        int d = mh.d;
        int m = mh.m;
        int fnum = 0;
        for (int j = 0; j < d; j++)
            fnum += inum[j] * (int)Math.pow( (double)m, (double)(d - j - 1) );
        return fnum;
	}


	private int[] findCell(double[] x, Mesh mh) {
		int d = mh.d;
		int[] inum = new int[d];
        double e1 = 1e-4;
        inum = new int[d];
        for (int j = 0; j < d; j++)
            inum[j] = (int)Math.floor(x[j] * mh.m / mh.diap() + e1);
        
		return inum;
	}


	private void addCenters(Mesh mh, Vector<centerCell> centerList, Vector<centerCell> centerList2, int[] centers, int it) {
		int centCount1 = centerList.size();
        int cencount = centers.length;
        for (int i = 0; i < cencount; i++)
        {
            centerCell centertmp = new centerCell(centers[i], mh);
            centertmp.it = it;
            centertmp.numCompSv = i;
            
            int pos = intersec(centertmp, centerList, centCount1);
            if ( pos >= 0 )
            {
            	centertmp.absCentr = pos;
            	centerList2.add(centertmp);
            }
            else
            {
            	absNumCenters++;
            	centertmp.absCentr = absNumCenters-1;
            	centerList.add(centertmp);
            }
        } 
		
	}
	
	private int intersec(centerCell centertmp, Vector<centerCell> centerList, int posmax) {
        for (int i = 0; i < posmax; i++)
            if (intersecCells(centerList.get(i), centertmp))
                return i;
        return -1;
	}


	private boolean intersecCells(centerCell center1, centerCell center2) {
        int d = center1.mh.d;
        int m = center1.mh.m;
        int[] inum = new int[d + 1];
        for (int j = 0; j < d; j++)
        {
            CCAClusterizator.iNumCreate(center1.ind, m, inum, d);
            double xmin1 = findCellSide(inum, -(j + 1), center1.mh);
            double xmax1 = findCellSide(inum, j + 1, center1.mh);
            CCAClusterizator.iNumCreate(center2.ind, center2.mh.m, inum, d);
            double xmin2 = findCellSide(inum, -(j + 1), center2.mh);
            double xmax2 = findCellSide(inum, j + 1, center2.mh);
            if ((m < center2.mh.m) && ((xmax2 < xmin1) || (xmax1 < xmin2)))
                return false;
            else if ((m > center2.mh.m) && ((xmax1 < xmin2) || (xmax2 < xmin1)))
                return false;
        }
        return true;		
	}


	private void addLastCenters(Mesh mh, Vector<centerCell> centerList, Vector<centerCell> centerList2, int[] centers, int[] sootv, int it) {
		int centCount1 = centerList.size();
        int cencount = centers.length;
        for (int i = 0; i < cencount; i++)
        {
            centerCell centertmp = new centerCell(centers[i], mh);
            centertmp.it = it;
            centertmp.numCompSv = i;
            
            int pos = intersec(centertmp, centerList, centCount1);
            if ( pos >= 0 )
            {
            	centertmp.absCentr = pos;
            	centerList2.add(centertmp);
            }
            else
            {
            	absNumCenters++;
            	centertmp.absCentr = absNumCenters-1;
            	centerList.add(centertmp);            	
            }
            
            sootv[i] = centertmp.absCentr;
        } 
		
	}

}

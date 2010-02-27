package org.ict.classifier.cca;

import java.util.Vector;


public class CCAClusterizator {

    private int n = 0;
    int diap = 0;
    int[] clustered;
    public int[] kletka;
    public int[] centers;
    public int[] clusters;
    public int kolCluster;
    
    private int temp_k;
    private int temp_cl;
    private int temp_numOfClusters;
    private int temp_kolvo;
    
    static public int funcNumber(int[] iNum, int m, int d)
    {
        double fnum = 0;
        for (int j_ = 1; j_ <= d; j_++) fnum += iNum[j_] * Math.pow((double)(m), (double)(d - j_));
        return (int)(Math.floor(fnum + 0.5));
    }

    static public void iNumCreate(int i0, int m, int[] iNum, int d)
    {
        double vsp = i0;
        double e1 = 1e-4;
        for (int l = 1; l <= d; l++)
        {
            iNum[l] = (int)(vsp / Math.pow((double)(m), (double)(d - l)) + e1);
            vsp = vsp - iNum[l] * Math.pow((double)(m), (double)(d - l));
        }
    }

    private void splitOnCells(Data data, int[] matr_plot, int[] kletka, int m)
    {
        if (diap == 0) return;
        double e1=1e-4;
        int d = data.getDData();
        int[] a = new int[d + 1];
        for(int i=0; i<n; i++)
        {
	        for(int j = 0; j < d; j++) 
                a[j+1] = (int)Math.floor(data.pointsArray[i].coordinates[j] * m / diap + e1);
	        kletka[i] = funcNumber(a, m, d);
	        matr_plot[kletka[i]]++;
        }
    }

    private void aplus1(int[] a, int d)
    {
        int k2;
        if (a.length <= d) return;
        if (1 != a[d]) {a[d]++;} 
        else 
        {
	        k2 = d;
	        while (1 == a[k2])
            {
		        a[k2] = -1;
		        k2--;
	        }
	        if (0 == k2) {a[0] = 1;} 
	        else a[k2]++;
        }
    }

    private int cellOfMaxPlot(int[] matr_plot, int i0, int m, int d)
    {
        int[] a = new int[d + 1];
        int[] iNum = new int[d + 1];
        int itogo = m - 1;
        int pos = -1; 
        int pl = 0;
        boolean b = true;
        double maxpl = 0;

        for (int j1=0; j1 <= d; j1++) a[j1] = -1;
        pos = i0;
        while (1!=a[0]){
	        b = true;
	        iNumCreate(i0, m, iNum, d); 
	        for (int j1=1; j1 <= d; j1++)
            {
		        iNum[j1] += a[j1];
		        if (!((iNum[j1] >= 0)&&(iNum[j1]<=itogo)))
                {
			        b = false;
			        break;
		        }
	        }
    	
	        if (b)
            {
		        pl = funcNumber(iNum, m, d);
		        if (matr_plot[pl] >= maxpl)
                {
			        maxpl = matr_plot[pl]; 
			        pos = pl;
		        }
	        }
	        aplus1(a, d);
        }
        return pos;
    }

    private void findKlass(Vector<Integer> cell, Vector<Integer> center, int[] matr_plot, int[] clustered, int i0, int m, int d)
    {
    	int plot, number = 0;
        if ((i0 < 0) || (i0 >= clustered.length)) return;
        clustered[i0] = temp_k;
        plot = cellOfMaxPlot(matr_plot, i0, m, d);
        center.set(temp_k, plot);
        if ((-1 == clustered[plot]) && (plot!=i0))
        {
        	temp_cl++;
        	cell.add(plot);
	        findKlass(cell, center, matr_plot, clustered, plot, m, d);
        } else if (plot != i0)
        {
	        for (number=0; number<=temp_cl; number++) 
	        	clustered[cell.get(number)] = clustered[plot];
	        temp_k--;
	        center.setSize(temp_k+1);
        }
    }

    void splitOnClasters(Vector<Integer> center, int[] matr_plot, int[] clastered, int p, int m, int d)
    {    	
    	Vector<Integer> cell = new Vector <Integer>();
    	int i, itogo;
    	temp_cl=0;
        itogo = (int) Math.pow((double)m, (double)d)-1;
        temp_k = -1;
        for (i=0; i<=itogo; i++)
	        if (matr_plot[i] <= p) {clastered[i] = -2;} 
	        else clastered[i] = -1;
        for (i=0; i<=itogo; i++)
	        if (-1 == clastered[i])
            {
	        	temp_k++;
	        	temp_cl = 0;
		        cell.setSize(1);
		        cell.set(0, i);
		        center.setSize(temp_k+1);
		        findKlass(cell, center, matr_plot, clastered, i, m, d);
	        }
    }

    boolean sliyanieLine(int[] matr_plot, int n1, int n2, int m, double t2, int d)
    {
        double e1 = 1e-4;
        int[] iNum1 = new int[d+1];
        int[] iNum2 = new int[d+1]; 
        double minpl = 0, c, s, h, v, t; 
        int j1;
        int num;
        DataPointReal point = new DataPointReal(d + 1);
        DataPointReal a1 = new DataPointReal(d + 1);
        DataPointReal b1 = new DataPointReal(d + 1);
        iNumCreate(n1, m, iNum1, d);
        iNumCreate(n2, m, iNum2, d);
        for (j1=1; j1<=d; j1++)
        {
	        a1.coordinates[j1] = ((double)(diap))/((double)m)*(iNum1[j1]+0.5);
	        b1.coordinates[j1] = ((double)diap)/((double)(m))*(iNum2[j1]+0.5)-a1.coordinates[j1];
        }
        minpl = matr_plot[n1];
        t = 0;
        s = 0;
        h = ((double)(m))/4.0; 
        for (j1=1; j1<=d; j1++) s += b1.coordinates[j1] * b1.coordinates[j1];
        c = h/Math.sqrt(s);
        while (t<1)
        {
	        t += c;
	        for (j1=1; j1<=d; j1++) point.coordinates[j1] = a1.coordinates[j1] + t*b1.coordinates[j1];
	        for (j1=1; j1<=d; j1++)	iNum1[j1] = (int)(point.coordinates[j1] * m / diap + e1);
	        num = funcNumber(iNum1, m, d);
	        if (matr_plot[num]<minpl) minpl = matr_plot[num];
	        if (num==n2) break;
        }
        if ((matr_plot[n1]<matr_plot[n2]) && (matr_plot[n1]!=0))
        {
	        if (matr_plot[n1]!=0){
		        v = minpl/matr_plot[n1];
		        if (v>t2) return true;
		        else return false;
	        }
	        else return false;
        }
        else{
	        if (matr_plot[n2]!=0)
            {
		        v = minpl/matr_plot[n2];
		        if (v>t2) return true;
		        else return false;
	        }
	        else return false;
        }
    }

    public boolean Sliyanie2(int i1, int i2, int center1, int center2, int[] matr_plot, double t2)
    {
        int mincell, mincenter;
        if (matr_plot[i1] < matr_plot[i2]) mincell = matr_plot[i1];
        else mincell = matr_plot[i2];
        if (matr_plot[center1] < matr_plot[center2]) mincenter = matr_plot[center1];
        else mincenter = matr_plot[center2];
        if ((double)mincell / (double)mincenter > t2) return true;
        else return false;
    }

    int pr(int[][] prov, int a, int b)
    {
        int min;
        int max = 0;
        int s = a;
        if (a != b)
        {
	        if (a<b) 
            {
		        min = a; 
		        max = b; 
	        }
	        else
            { 
		        min = b;
		        max = a; 
	        }
	        if ((0 > min) || (min > prov.length) || (min > prov[min].length)) return -1;
            max = prov[min][min];
	        s = pr(prov, min, max);
        }
        return s;
    }

    private void mark(int[][] prov, int i, int k, int[] provereno)    
    {
        provereno[i] = 1;
        for (int j1=1; j1<=k; j1++)
	        if ((prov[i][j1]!=-1) && (prov[i][j1]!=-2) && (0 == provereno[j1]))
            {
		        prov[j1][j1] = prov[i][i];
		        mark(prov, j1, k, provereno);
	        }
    }

    static public void aplus11(int[] a, int d, int amin, int amax, boolean withoutZero)    
    {
    	int k2;
    	if (a[d] < amax) {a[d] += 1;}
    	else
    	{
    		k2 = d;
    		while ((a[k2] == amax) && (k2 > 0)) 
            {
                a[k2] = amin;
                k2--;
            }
            if (k2 == 0) a[0] = 1;
            else a[k2] = a[k2] + 1;
        }

    	if (withoutZero)
    	{
            int nonzeroPositions = 0;
            for (k2=1; k2<=d; k2++)
                if (a[k2] != 0) nonzeroPositions++;
            if (nonzeroPositions == 0)
            	aplus11(a, d, amin, amax, withoutZero);
        }
    }

    void obed(int[] matr_plot, Vector<Integer> center, int[] clustered, int[][] prov, int m, int k, double t2, int d, boolean forCCA)
    {
    	int i, num;
        int itogo;
        boolean sl, b; 
        int[] iNum = new int[d+1];
        int[] a = new int[d+1];
        int kolvo = 0;
        int[] provereno = new int[k+1]; 
        for (i=0; i<=k; i++)
	        for (int j=0; j<=k; j++)
		        prov[i][j] = -2;
        itogo = (int)(Math.pow((double)m, (double)d)-1);
    	
        for (i=1; i<=itogo; i++)
	        if (clustered[i]!=-2)
	        {
		        for (int j = 0; j <= d; j++) a[j] = -1;
		        while (a[0] < 1)
                {
			        b = true;
			        iNumCreate(i, m, iNum, d);
			        for (int j = 1; j <= d; j++)
                    {
				        iNum[j] = iNum[j] + a[j];
				        if (!((iNum[j]>=0)&&(iNum[j]<m)))
                        {
					        b = false;
					        break;
				        }
			        }
			        if (b){
				        num = funcNumber(iNum, m, d);
				        if (num<i)
                            if ((clustered[num] != clustered[i]) && (clustered[num] != -2) && (clustered[i] != -2))
                            {
                                if ((prov[clustered[i]][clustered[num]] == -2) || (prov[clustered[i]][clustered[num]] == -3))
                                {                                    
                                	sl = Sliyanie2(i, num, center.get(clustered[i]), center.get(clustered[num]), matr_plot, t2);                                	
                                    if ( sl )
                                    {   
                                    	prov[clustered[i]][clustered[num]] = clustered[num];
                                        prov[clustered[num]][clustered[i]] = clustered[i];								
							        } else{
                                        prov[clustered[i]][clustered[num]] = -3;
                                        prov[clustered[num]][clustered[i]] = -3;
							        }
						        }
					        }							
			        }
                    int amin = -1; 
                    int amax = 0;
			        aplus11(a, d, amin, amax, true);
		        }
	        }
	        for (i=0; i<=k; i++)
            {
		        provereno[i] = 0;
		        prov[i][i] = i;
	        }

            for (i=0; i<=k; i++)
                for(int j=0; j<=k; j++)
                    if (prov[i][j] == -3) prov[i][j] = -1;

	        kolvo = -1;
	        for (i=0; i<=k; i++)
		        if (0 == provereno[i])
                {
			        mark(prov, i, k, provereno); 
			        kolvo++; 
		        }

	        temp_numOfClusters = kolvo + 1;

            if (forCCA)
	            for (i=0; i<=itogo; i++)
                    if (clustered[i] >= 0)
                        clustered[i] = prov[clustered[i]][clustered[i]];
    }

    void outputResults(double per, Data data, int[] kletka, int[] clustered, double[][] col, int k)
    {
        double eps1 = 1e-4;
        int d = data.getDData();

        for (int i = 0; i < n; i++){
	        if (clustered[kletka[i]] >= 0){
		        for (int j=1; j<=d; j++)
			        col[clustered[kletka[i]]][j] = col[clustered[kletka[i]]][j]+data.pointsArray[i].coordinates[j-1] * eps1;
		        col[clustered[kletka[i]]][d+1]++;
	        }
        }
        for (int i=0; i<=k; i++){
	        if (col[i][d+1] <= per)
		        for (int j=1; j<=d; j++) col[i][j] = 0;
	        else
		        for (int j=1; j<=d; j++) col[i][j] = col[i][j]/(col[i][d+1]*eps1);
        }	
    }

    int newnumber(int i, int kolvo, int[] chooseClass)
    {
        int num = -2;
        for (int j=0; j<kolvo; j++)
	        if (chooseClass[j] == i){
		        num = j;
		        break;
	        }
        return num;
    }

    void kolvoPoints(int[] clustered, int[][] prov, double[][] col, int k, double per, int m, int d)
    {
        int kolvo = 0;
        for (int i=0; i<=k; i++)
	        if (col[i][d+1]>per){
		        kolvo++;
	        }
        int[] chooseClass = new int[kolvo];
        int num = 0;
        for (int i=0; i<=k; i++)
	        if (col[i][d+1]>per){
		        chooseClass[num] = i;
		        for (int j=1; j<=d+1; j++) col[num][j] = col[i][j];
		        num++;
	        } 
    	
        int itogo = (int)(Math.pow((double)m, (double)d));
        for (int i=0; i<itogo; i++)
	        if (clustered[i]>0)
		        clustered[i] = newnumber(clustered[i], kolvo, chooseClass);
        for (int i=0; i<=k; i++)
	        prov[i][i] = newnumber(prov[i][i], kolvo, chooseClass);
        for (int i=kolvo; i<=k; i++)
	        for (int j=1; j<=d+1; j++) col[i][j] = 0;
        
        temp_kolvo = kolvo;
    }

    public double findCellCenter(int[] inum, int coord, int m)
    {
       if (m * diap >= 0)
           return (double) diap/(double)(m) * (inum[coord] + 0.5);
       else
       {
           return -1;
       }
    }

    
    public void clustering(Data data, Parameters algorithmParameters)
    {
        
        n = data.getNData();
        int d = data.getDData();
        diap = 256;  
        if ((n<=0) || (d<=0)) return;


        int m = algorithmParameters.cellsize;
        double per = algorithmParameters.backgroundpercent;
        double t2 = algorithmParameters.densityThreshold;
        int p = algorithmParameters.emptyThreshold;
    	
        int numberOfCells = (int)(Math.pow((double)m, (double)d));
        clustered = new int[numberOfCells];
        kletka = new int[n];
        for(int i=0; i<n; i++) kletka[i] = 0;

        int[] matr_plot = new int[numberOfCells];
        for(int i=0; i<numberOfCells; i++) matr_plot[i] = 0;
        
        Vector<Integer> center = new Vector <Integer>();

        splitOnCells(data, matr_plot, kletka, m);

        int k = 0;
        temp_k = k;
        splitOnClasters(center, matr_plot, clustered, p, m, d);
        k = temp_k;
    	
        int[][] prov = new int[k+1][k+1];
        int numOfClusters = 0;

        temp_numOfClusters = numOfClusters;
        obed(matr_plot, center, clustered, prov, m, k, t2, d, algorithmParameters.forCCA);
        numOfClusters = temp_numOfClusters;
        
        if (algorithmParameters.forCCA == false)
        {
        	clusters = new int[prov.length];
        	for (int i=0; i<clusters.length; i++)
        		clusters[i] = prov[i][i];
        	
        	centers = new int[center.size()];
        	for (int i=0; i<centers.length; i++)
        		centers[i] = center.get(i);
        }
        else
        {
            double[][] col = new double[k+1][d+2];
            for(int i = 0; i <= k; i++)
            {
    	        for(int j = 0; j <= d+1; j++) col[i][j] = 0;
            }
        	        
            outputResults(per, data, kletka, clustered, col, k);
        	
            int kolvo = 0;
            temp_kolvo = kolvo;
            kolvoPoints(clustered, prov, col, k, per, m, d);
            kolvo = temp_kolvo;
            
            kolCluster = kolvo;
            
            for ( int i = 0; i < n; i++ )
            	if ( clustered[kletka[i]] >= 0 )
            	{
            		data.pointsArray[i].classNumber = clustered[kletka[i]];
            	}
                else
                {
                	data.pointsArray[i].classNumber = -2;
                }
        }
    }

}

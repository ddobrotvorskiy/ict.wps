package org.ict.classifier.cca;

public class DecisionSerge {
	
	public int numOfClusters;
	
    private int n;
    private double th;
    private double th2;
    
    private int i_abs=0;
    private int j_abs=0;
    
    public int[] cl_num;

    public DecisionSerge(double threashhold)
    {
        th = threashhold;
        th2 = threashhold + 1;
    }

    private double minim(double[][] matr, int[] cl_pl)
    {
    	int i1=0;
    	int j1=0;
    	
        double min = th2;
        for (int i = 0; i < n; i++)
        {
            if (cl_pl[i] != 0)
            {
                for (int j = i + 1; j < n; j++)
                {
                    if ((matr[i][j] < min) && (matr[i][j] >= 0))
                    {
                        min = matr[i][j];
                        i1 = i;
                        j1 = j;
                    }
                }
            }
        }
        i_abs = i1;
        j_abs = j1;
        return min;
    }

    public void dendrogramma(double[][] matrdist)
    {            
        n = matrdist.length;

        int i=0, j=0;
        int temp, temp2;

        int[] cl_pl = new int[n];
        for (i = 0; i < n; i++)
            cl_pl[i] = 1;

        cl_num = new int[n];
        for (i = 0; i < n; i++)
            cl_num[i] = -1;
                    
        while ( minim(matrdist, cl_pl) <= th )
        {
        	i = i_abs;
        	j = j_abs;
            if (cl_num[i] == -1)
            {
                if (cl_num[j] == -1)
                {
                    reculc(i, j, cl_pl, matrdist);
                    cl_num[i] = i;
                    cl_num[j] = i;
                    cl_pl[i] = 2;
                    cl_pl[j] = 0;
                }
                else
                {
                    reculc(cl_num[j], i, cl_pl, matrdist);
                    cl_num[i] = cl_num[j];
                    cl_pl[cl_num[j]]++;
                    cl_pl[i] = 0;
                }
            }
            else 
            {
                if (cl_num[j] == -1)
                {
                    reculc(cl_num[i], j, cl_pl, matrdist);
                    cl_num[j] = cl_num[i];
                    cl_pl[cl_num[i]]++;
                    cl_pl[j] = 0;
                }
                else 
                {
                    if (cl_pl[cl_num[i]] < cl_pl[cl_num[j]])
                    {
                        temp = cl_num[i];
                        temp2 = cl_num[j];
                    }
                    else
                    {
                        temp2 = cl_num[i];
                        temp = cl_num[j];
                    }

                    reculc(temp2, temp, cl_pl, matrdist);

                    for(int k=0; k<n; k++)
                    {
                        if (cl_num[k] == temp)                          
                            cl_num[k] = temp2;                            
                    }
                    cl_pl[temp2]+=cl_pl[temp];
                    cl_pl[temp] = 0;
                }
            }
        }

        for (int k = 0; k < n; k++)
            if (cl_num[k] < 0)
                cl_num[k] = k;

    }

    public void dendrogramma_plus(double[][] matrdist)
    {
        n = matrdist.length;

        int i = 0, j = 0;
        int temp, temp2;

        int[][] clusters = new int[n][n];
        for (i = 0; i < n; i++)
            clusters[i][0] = i;

        int[] cl_pl = new int[n];
        for (i = 0; i < n; i++)
            cl_pl[i] = 1;

        cl_num = new int[n];
        for (i = 0; i < n; i++)
            cl_num[i] = i;

        while (minim(matrdist, cl_pl) <= th)
        {             
        	i = i_abs;
        	j = j_abs;
        	
            if (cl_pl[cl_num[i]] < cl_pl[cl_num[j]])
            {
                temp = cl_num[i];
                temp2 = cl_num[j];
            }
            else
            {
                temp2 = cl_num[i];
                temp = cl_num[j];
            }
            
            reculc(temp2, temp, cl_pl, matrdist);

            for (int k = 0; k < cl_pl[temp]; k++)
            {
                cl_num[clusters[temp][k]] = temp2;
                clusters[temp2][cl_pl[temp2] + k] = clusters[temp][k]; 
            }

            cl_pl[temp2] += cl_pl[temp];
            cl_pl[temp] = 0;
        }
        
        numOfClusters = 0;
        for (i=0; i<n; i++)
        {
        	if (cl_pl[i] > 0)
        	{        		
        		for (int k = 0; k < cl_pl[i]; k++)
                {
                    cl_num[clusters[i][k]] = numOfClusters;
                }
        		numOfClusters++;
        	}        	
        }

    }

    private void reculc(int i1, int j1, int[] cl_pl, double[][] matrdist)
    {
        int i_pl = cl_pl[i1];
        int j_pl = cl_pl[j1];
        int pl = i_pl + j_pl;

        for (int i = 0; i < i1; i++)
        {
            if (i < j1)
            {
                if ((matrdist[i][i1] < 0) || (matrdist[i][j1] < 0))
                    matrdist[i][i1] = -1;
                else
                    matrdist[i][i1] = (matrdist[i][i1] * i_pl + matrdist[i][j1] * j_pl) / pl;
            }
            else
            {
                if ((matrdist[i][i1] < 0) || (matrdist[j1][i] < 0))
                    matrdist[i][i1] = -1;
                else
                matrdist[i][i1] = (matrdist[i][i1] * i_pl + matrdist[j1][i] * j_pl) / pl;
            }
        }
        for (int i = (i1 + 1); i < n; i++)
        {
            if (i < j1)
            {
                if ((matrdist[i1][i] < 0) || (matrdist[i][j1] < 0))
                    matrdist[i1][i] = -1;
                else
                    matrdist[i1][i] = (matrdist[i1][i] * i_pl + matrdist[i][j1] * j_pl) / pl;
            }
            else
            {
                if ((matrdist[i1][i] < 0) || (matrdist[j1][i] < 0))
                    matrdist[i1][i] = -1;
                else
                    matrdist[i1][i] = (matrdist[i1][i] * i_pl + matrdist[j1][i] * j_pl) / pl;
            }
        }


        for (int i = 0; i < j1; i++)
        {
            matrdist[i][j1] = -1;
        }
        for (int i = (j1 + 1); i < n; i++)
        {
            matrdist[j1][i] = -1;
        }

    }

}

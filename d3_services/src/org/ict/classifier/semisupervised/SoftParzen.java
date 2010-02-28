/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ict.classifier.semisupervised;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import org.ict.classifier.ClassificationTask;
import org.ict.classifier.Clazz;
//import org.ict.classifier.Point;

/**
 * SoftParzen semi-supervised classifier.
 * Works ONLY on small sets of data and when we have sample points for every class on image.
 *
 * This class is a simple port from C# code, dont try to understand, it can damage your brain!
 *
 * @author positron
 * @author Ekaterina Kulikova
 */
public class SoftParzen {
	private class Point {
		double[] coords;
		public Point(int dim) {coords=new double[dim];}
		public Point() {}
	}
	private class MarkedPoint extends Point{		
		int classId;
		public MarkedPoint() {classId=-1;}
		public MarkedPoint(Point p) {coords = Arrays.copyOf(p.coords, p.coords.length);classId=-1;}
		public MarkedPoint(int dim, int id) {super(dim); classId=id;}
		public MarkedPoint(int id) {classId=id;}
	}
	private Point[] data;
	private MarkedPoint[] sampleset;
	//private double[][] probabilities;
	private int whole;
	private int dim;
	private MarkedPoint[] wholeset;

	private int classCount;
	private double gamma, eps;
	private int step_num, sigma_max;
	private final double EPS=1e-10;

	private Point parsePoint(String dd, boolean sample) {
		StringTokenizer stk = new StringTokenizer(dd, " ");
		ArrayList<Double> apt = new ArrayList<Double>();
		while (stk.hasMoreTokens()) apt.add(Double.valueOf(stk.nextToken()));
		int dim_ = sample ? (apt.size()-1) : apt.size();
		if (dim==-1) dim=dim_;
		assert dim==dim_;
		if (sample) {
			MarkedPoint mpt = new MarkedPoint(dim, (int)apt.get(apt.size()-1).doubleValue());
			for(int i=0; i<dim; i++) mpt.coords[i]=apt.get(i);
			return mpt;
		} else {
			Point pt = new Point(dim);
			for(int i=0; i<dim; i++) pt.coords[i]=apt.get(i);
			return pt;
		}
	}
	public SoftParzen(String sampleFile, String dataFile) {
		dim=-1;
		try {
			String line;
			LineNumberReader rd = new LineNumberReader(new FileReader(sampleFile));
			ArrayList<Point> samplelist = new ArrayList<Point>();
			classCount = Integer.valueOf(rd.readLine());
			while ((line = rd.readLine() )!= null) samplelist.add(parsePoint(line,true) );
			rd.close();
			rd = new LineNumberReader(new FileReader(dataFile));
			ArrayList<Point> datalist = new ArrayList<Point>();
			while ((line = rd.readLine() )!= null) datalist.add(parsePoint(line,false) );
			rd.close();
			sampleset = samplelist.toArray( new MarkedPoint[samplelist.size()]);
			data = datalist.toArray( new Point[datalist.size()]);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public  SoftParzen(ClassificationTask task, List<double[]> points) {
		List<Clazz> clazzes = task.getClasses();
		classCount = clazzes.size();
		ArrayList<MarkedPoint> samplelist = new ArrayList<MarkedPoint>();
		for (Clazz cl: clazzes)
			for(org.ict.classifier.Point pt: cl.getPoints() ){
				MarkedPoint mp = new MarkedPoint(cl.getId() );
				mp.coords = pt.getArray();
				samplelist.add( mp );
			}
		sampleset = samplelist.toArray( new MarkedPoint[samplelist.size()] );
		dim = sampleset[0].coords.length;

		data = new Point[points.size()];
		int i=0;
		for (double[] cd : points) {
			assert dim==cd.length;
			Point pt = new Point();
			pt.coords = cd;
			data[i++] = pt;
		}
	}


	private void createProbabilities(double[][] probabilities )   {
		//int whole = data.length + nv;
		//probabilities = new double[whole][classCount];
		for (int i=0; i<whole; i++)
			java.util.Arrays.fill(probabilities[i], 0d);

		for (int i = 0; i < sampleset.length; i++ )
			probabilities[i][ sampleset[i].classId ] = 1;
	}

	private double dist(double[] c1, double[] c2) {
		//assert c1.length == c2.length;
		double res=0;
		for (int i=0; i<c1.length; i++) res+= (c1[i]-c2[i])*(c1[i]-c2[i]);
		return Math.sqrt(res);
	}

    private void calcDists(double[][] dists)  {
		//dists = new double[whole][];
		assert dists.length==whole;

		int datasize=data.length;
		for (int i = 0; i < whole; i++)
			dists[i] = new double[i];

		for (int i = 1; i < sampleCount0; i++)
			for (int j = 0; j < i; j++)
				dists[i][j] = dist(sampleset[i].coords, sampleset[j].coords);

		for (int i = 0; i < datasize; i++)
			for (int j = 0; j < sampleCount0; j++)
				dists[sampleCount0 + i][j] = dist(data[i].coords, sampleset[j].coords);

		for (int i = 0; i < datasize; i++)
			for (int j = 0; j < i; j++)
				dists[sampleCount0 + i][sampleCount0 + j] = dist(data[i].coords, data[j].coords);
	}

	private double kernel(Point p1, Point p2, double h) {
		double s = 1;
		for (int j = 0; j < dim; j++)
			s *= Math.exp(-(p1.coords[j]-p2.coords[j])*(p1.coords[j]-p2.coords[j])/(2*h*h)  );
		return s;
	}
	private double ln_sum_kernel(ArrayList<MarkedPoint> samples, int i0, double h) {
		double s = 0;
		int len = samples.size();
		for (int i = 0; i < i0; i++)
			s += kernel(samples.get(i0), samples.get(i), h);
		for (int i = i0 + 1; i < len; i++)
			s += kernel(samples.get(i0), samples.get(i), h);
		if (s > 0) return Math.log(s);
			else return -1e-60;
	}

	private int sampleCount, sampleCount0;
	private ArrayList<MarkedPoint> samples2;

	/** Note: it uses global members samples2 and sampleCount as output
	 */
	private double crossValidate(double sigma_max, double step, int nl, int[] softParzClasses) {
		//TODO: drop nl since it always equals sampleCount0
		int hCount = (int)step; //(int)Math.Round((sigma_max / step) - 0.5);

		samples2 = new ArrayList<MarkedPoint>(nl);
		for (int i = 0; i < nl; i++)	{
			MarkedPoint pt = new MarkedPoint();
			pt.coords = Arrays.copyOf(wholeset[i].coords, dim);
			assert wholeset[i].classId!=-1;
			pt.classId = wholeset[i].classId;
			samples2.add(pt);
		}

		//TODO: optimize this crap
		for (int i = nl; i < whole; i++)
			if (softParzClasses[i] >= 0) {
				MarkedPoint point = new MarkedPoint();
				point.coords = Arrays.copyOf(wholeset[i].coords, dim);
				point.classId = softParzClasses[i];
				samples2.add(point);
			}
		sampleCount = samples2.size();

		double[] log_likelyhood = new double[hCount];
		Arrays.fill(log_likelyhood, 0);
		for (int i2 = 0; i2 < hCount; i2++)	{
			for (int i = 0; i < sampleCount; i++)
				log_likelyhood[i2] += ln_sum_kernel(samples2, i, (i2+1) * step);
			log_likelyhood[i2] = log_likelyhood[i2] - sampleCount*dim*Math.log((i2+1)*step);
		}

		double max_log_likelyhood = log_likelyhood[0];
		double h_max_log_likelyhood = sigma_max / step;
		for (int i2 = 1; i2 < hCount; i2++)
			if (log_likelyhood[i2] > max_log_likelyhood){
				max_log_likelyhood = log_likelyhood[i2];
				h_max_log_likelyhood = (int)((i2+1) * sigma_max / step);
			}
		return h_max_log_likelyhood;
	}

	//** When to stop EM algorithm
	private boolean stopCriterion(double[][] probabilities, double[][] probNew, double gamma, int dataCount0) {
		int probSize = probabilities.length;
		double s = 0;
		for (int i = 0; i < probSize; i++)
			for (int j = 0; j < classCount; j++)	{
				s += Math.abs((probNew[i][j] - probabilities[i][j]) / probSize);
				if (s > gamma)		{
					for (int k = 0; k < dataCount0; k++)
						for (int l = 0; l < classCount; l++)
							probabilities[sampleCount0 + k][l] = probNew[sampleCount0 + k][l];
					return false;
				}
			}
		return true;
	}

	public void classify() {
		step_num = 5;
		sigma_max = 10;
		gamma = 0.001;
		eps = 1e-6;


		double step = step_num;
		boolean finish = false;
		int dataCount0 = data.length;		
		sampleCount0 = sampleset.length;
		whole = dataCount0 + sampleCount0;
		samples2 = new ArrayList<MarkedPoint>(sampleCount0);
		for(int i=0; i<sampleCount0; i++) samples2.add(sampleset[i]);

		//ArrayList<Point> wholeset = new ArrayList<Point>(whole);
		wholeset = new MarkedPoint[whole];
		System.arraycopy(sampleset, 0, wholeset, 0, sampleCount0);		
		for (int i=0; i<dataCount0; i++) wholeset[sampleCount0+i] = new MarkedPoint(data[i]);
	
		int[] softParzClasses = new int[whole];
		//Arrays.fill(softParzClasses, 0, sampleCount, 0);
		for (int i=0; i<sampleCount0; i++) softParzClasses[i] = sampleset[i].classId;
		Arrays.fill(softParzClasses, sampleCount0, whole, -1);
		//double[][] p = new double[0][];
		double[][] probabilities = new double[whole][classCount];
		createProbabilities(probabilities);

		double[] sigma = new double[step_num];
		double[][] pnew = new double[whole][];
		for (int i=0; i<whole; i++) pnew[i] = Arrays.copyOf(probabilities[i], classCount);
		double[][] dist = new double[whole][];
		calcDists(dist);
		
		sampleCount = sampleCount0;
		int t = 1;

		do	{
			sigma[t-1] = crossValidate(sigma_max, step, sampleCount0, softParzClasses);
			if ((t > 1) && Math.abs(sigma[t-2] - sigma[t-1])<EPS ) {
				finish = true;
				break;
			}
			if (step_num == 1) finish = true;
			int nit = 1;
			double eps2 = 1e-4;
			do {
				for (int i1 = 0; i1 < dataCount0; i1++)	{
					double sum = 0;
					for (int j = 0; j < classCount; j++) {
						double s = 0;
						for (int i_ = 0; i_ < whole; i_++) {
							double pmlr;
							if (sampleCount0 + i1 > i_)
								pmlr = Math.exp(-(dist[sampleCount0 + i1][i_]) * (dist[sampleCount0 + i1][i_]) /
										 (2 * sigma[t - 1] * sigma[t - 1]));
							else {
								if (sampleCount0 + i1 < i_)
								pmlr = Math.exp(-(dist[i_][sampleCount0 + i1]) * (dist[i_][sampleCount0 + i1]) /
										 (2 * sigma[t - 1] * sigma[t - 1]));
								else pmlr = 1;
							}
							if (pmlr > eps)
								s += probabilities[i_][j]*pmlr;
						}
						pnew[sampleCount0 + i1][j] = s;
						sum += s;
					}
					if (sum > 0)
						for (int j = 0; j < classCount; j++)
							pnew[sampleCount0 + i1][j] = pnew[sampleCount0 + i1][j]/sum;
					double pmax = 0;
					for (int j = 0; j < classCount; j++)
						pmax += pnew[sampleCount0 + i1][j];
					if (pmax > eps2) {
						pmax = pnew[sampleCount0 + i1][0];
						softParzClasses[sampleCount0 + i1] = 0;
						for (int j = 0; j < classCount; j++)
							if (pmax < pnew[sampleCount0 + i1][j]){
								pmax = pnew[sampleCount0 + i1][j];
								if (softParzClasses[sampleCount0 + i1] != j)
									softParzClasses[sampleCount0 + i1] = j;
							}
					}
				}
				nit++;			
			} while ( !stopCriterion(probabilities, pnew, gamma, dataCount0));
			t++;
		} while (!finish);

		for(int i=0; i<whole; i++) wholeset[i].classId = softParzClasses[i];
		//for (int i = 0; i < dataCount0; i++)  wholeset[sampleCount0+i].classId = softParzClasses[sampleCount0 + i];
	}


	public int[] getAllClasses() {
		int[] res = new int[whole];
		for (int i=0; i<whole; i++) {
			res[i] = wholeset[i].classId;
		}
		return res;
	}
	public int[] getClasses() {
		int[] res = new int[data.length ];
		for (int i=0; i<data.length; i++) {
			res[i] = wholeset[i+sampleCount0].classId;
		}
		return res;
	}

	public void dumpResult() {
		try {
			FileOutputStream fw = new FileOutputStream("sparzen-output.txt");
			PrintStream os = new PrintStream(fw);
			for (int i=0; i<wholeset.length; i++) {
				String out = String.format("%d %d %d", 
						(int)wholeset[i].coords[0], (int)wholeset[i].coords[1],
						wholeset[i].classId);
				os.println(out);
				//fw.write(out, 0, out.length());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}
}

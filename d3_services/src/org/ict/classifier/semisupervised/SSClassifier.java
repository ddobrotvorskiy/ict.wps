/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ict.classifier.semisupervised;

//import Jama.Matrix;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import org.ict.classifier.ClassificationTask;
import org.ict.classifier.Classifier;
import org.ict.classifier.Point;
import org.ict.classifier.io.*;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.ict.classifier.Clazz;

/**
 * Classifier for small sample sets.
 * Can extend sample sets and apply some other classifier.
 * Typical usage: Call constructor, then build, then classify, then toImage
 * @author positron
 */
public class SSClassifier  {
	private ClassificationTask task;
	private List<ExtendibleClazz> classes;
	Hashtable<Integer, ExtendibleClazz> classtable;
	private Classifier finalClassifier;
	private Point[][] data;
	//private ArrayList<Point> points;
	private int WW,HH;
	private int[][] idmap;

	public static void main(String[] args) {
		try {
			SSClassifier engine = null;
			
			AsciiTaskReader rd = new AsciiTaskReader(new FileReader("samples.txt"));
			ClassificationTask task = rd.readTask();
			//System.out.println(task);

			BufferedImage bim = javax.imageio.ImageIO.read( new File("image.bmp") );

			engine = new SSClassifier(task, bim.getRaster() );
		} catch  (Exception ex) {
			System.out.println("load: "+ex);
			ex.printStackTrace();
		}
	}

	public SSClassifier(ClassificationTask task, Raster image) {
		this.task = task;
		setRaster(image);
	}

	public void setRaster(Raster image) {
		WW = image.getWidth();
		HH = image.getHeight();
		//ArrayList<Point> points = new
		int mx=image.getMinX(), my=image.getMinY();
		data = new Point[WW][HH];
		double[] pixel = new double[ image.getNumBands() ];
		for (int x = 0; x < WW; x++)
			for (int y = 0; y < HH; y++) {
				double[] p = image.getPixel(mx + x, my + y, pixel);
				//points.add(Point.create(p));
				data[x][y] = Point.create(p);
			}
	}

	/**
	 * Creates an extended sample set using the given set and whole array of data from iamge.
	 */
	public void build() {
		//make our own copy of a sample set
		List<Clazz> oldset = task.getClasses();
		classes = new ArrayList<ExtendibleClazz>(oldset.size() );
		classtable = new Hashtable<Integer, ExtendibleClazz>();
		for(Clazz cc: oldset) {
			ExtendibleClazz ec = new ExtendibleClazz(cc);
			classes.add(ec);
			classtable.put(new Integer(cc.getId()), ec);
		}

		// extend sample set
		int GRIDX=10,GRIDY=10;
		ArrayList<Point> extpoints = new ArrayList<Point>(GRIDX*GRIDY);		
		for (int i=0; i<GRIDX; i++)
			for(int j=0; j<GRIDY;j++) {
				extpoints.add( data[i][j] );
			}
		for (Point p: extpoints) {
			//TODO: Here must be soft-Parzen instead of soft-parzen;
			int id=finalClassifier.classify(p);
			classtable.get(id).addPoint(p);
		}

	}

	public void classify() {
		//classify using extended sample set
		ClassificationTask.Builder builder = new ClassificationTask.Builder();
		for (Clazz cc: classes) builder.addClass(cc);
		ClassificationTask task2 = builder.createTask();

		//TODO: must be some other classifier
		finalClassifier = org.ict.classifier.Classifiers.createSimpleClassifier( task2 );
		for (int i=0; i<WW; i++)
			for(int j=0; j<HH;j++)
				idmap[i][j] = finalClassifier.classify(data[i][j]);
	}

	public ClassificationTask getTask() {
		return task;
	}

	public Image toImage() {
		BufferedImage bim = new BufferedImage(WW,HH,BufferedImage.TYPE_INT_RGB);
		for (int i=0; i<WW; i++)
			for(int j=0; j<HH;j++)
				bim.setRGB(i, j, classtable.get(j).getColor().getRGB() );
		return bim;
	}

}

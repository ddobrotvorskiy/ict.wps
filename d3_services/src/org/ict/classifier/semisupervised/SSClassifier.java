package org.ict.classifier.semisupervised;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import org.ict.classifier.ClassificationTask;
import org.ict.classifier.Classifier;
import org.ict.classifier.Point;
import org.ict.classifier.io.AsciiTaskReader;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.ict.classifier.Classifiers;
import org.ict.classifier.Clazz;

/**
 * Classifier for small sample sets.
 * Can extend sample sets and apply some other classifier.
 * Typical usage: Call constructor, then build, then classify, then toImage
 * @author positron
 */
public class SSClassifier  {
	private ClassificationTask task, task2;
	private List<Clazz> classes;
	//Hashtable<Integer, Clazz> classtable;
	private Classifier finalClassifier;
	private Point[][] data;
	//private ArrayList<Point> points;
	private int WW,HH;
	private int[][] idmap;

	public static void main(String[] args) {
		try {
		/*
			AsciiTaskReader rd = new AsciiTaskReader(new FileReader("sparzen-ss.txt"));
			SoftParzen cc = new SoftParzen("sparzen-ss.txt", "sparzen.txt");
			cc.classify();
			cc.dumpResult();
		*/
			
			SSClassifier engine = null;
			AsciiTaskReader rd = new AsciiTaskReader(new FileReader("samples-small.txt"));
			ClassificationTask task = rd.readTask();
			BufferedImage bim = javax.imageio.ImageIO.read( new File("image.bmp") );
			engine = new SSClassifier(task, bim.getData() );
			engine.build();
			engine.classify();

			javax.imageio.ImageIO.write(engine.toImage(), "bmp", new File("output.bmp") );
		} catch  (Exception ex) {
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
		Hashtable<Integer, Clazz.Builder> buildtable = new Hashtable<Integer, Clazz.Builder>();
		Clazz.Builder[] clazzBuilders = new Clazz.Builder[oldset.size() ];
		int t=0;
		for (Clazz cc: oldset) {
			clazzBuilders[t] = new Clazz.Builder(cc);
			buildtable.put(cc.getId(), clazzBuilders[t]);
			t++;
		}
		
		// extend sample set		
		int GRIDX=20,GRIDY=20;
		ArrayList<double[]> extpoints = new ArrayList<double[]>(GRIDX*GRIDY);
		int pointIntdices[][] = new int[GRIDX*GRIDY][2];
		t=0;
		for (int i=0; i<GRIDX; i++)
			for(int j=0; j<GRIDY;j++) {
				extpoints.add( data[i*WW/GRIDX][j*HH/GRIDY].getArray() );
				pointIntdices[t][0]=i*WW/GRIDX;
				pointIntdices[t][1]=j*HH/GRIDY;
				t++;
			}
		
		SoftParzen sp = new SoftParzen(task, extpoints);
		sp.classify();
		int ids[] = sp.getClasses();
		for (int i=0; i<extpoints.size(); i++)
			if (ids[i]!=-1)
				buildtable.get(ids[i]).addPoint( data[pointIntdices[i][0]][pointIntdices[i][1]] );
			//else System.out.printf("Unclassified point:%d/%d\n", pointIntdices[i][0], pointIntdices[i][1]);

		//TODO: Here must be soft-Parzen instead of this;
		/*int GRIDX=10,GRIDY=10;
		ArrayList<Point> extpoints = new ArrayList<Point>(GRIDX*GRIDY);
		for (int i=0; i<GRIDX; i++)	for(int j=0; j<GRIDY;j++)
			extpoints.add( data[i*WW/GRIDX][j*HH/GRIDY] );
		Classifier classifier2 = Classifiers.createClassifier(task);
		for (Point p: extpoints) {			
			int id=classifier2.classify(p);
			buildtable.get(id).addPoint(p);
		}*/
		
		classes = new ArrayList<Clazz>( oldset.size() );
		for(Clazz.Builder b : clazzBuilders) classes.add(b.createClass());

		//Create a task for classification;
		ClassificationTask.Builder taskBuilder = new ClassificationTask.Builder();
		for (Clazz cc: classes) taskBuilder.addClass(cc);
		task2 = taskBuilder.createTask();

		System.out.println("build finished");
		//** render temp image
		/*
		Hashtable<Integer, Integer> colors = new Hashtable<Integer, Integer>();
		for (Clazz cc: classes) colors.put(cc.getId(), cc.getColor().getRGB());
		BufferedImage bim = new BufferedImage(WW,HH,BufferedImage.TYPE_INT_RGB);
		for (int i=0; i<extpoints.size(); i++)
			if (ids[i]!=-1) 	bim.setRGB(pointIntdices[i][0],pointIntdices[i][1], colors.get(ids[i]) );
		try {
			javax.imageio.ImageIO.write(bim, "bmp", new File("tmp_classes.bmp"));
		} catch (IOException ex) {ex.printStackTrace();}
		 */
	}

	public void classify() {
		//classify using extended sample set
		idmap = new int[WW][HH];
		//TODO: must be some other classifier
		finalClassifier = Classifiers.createClassifier( task2 );
		for (int i=0; i<WW; i++)
			for(int j=0; j<HH;j++)
				idmap[i][j] = finalClassifier.classify(data[i][j]);
	}

	public ClassificationTask getTask() {
		return task;
	}

	public BufferedImage toImage() {
		Hashtable<Integer, Integer> colors = new Hashtable<Integer, Integer>();
		for (Clazz cc: classes) colors.put(cc.getId(), cc.getColor().getRGB());
		BufferedImage bim = new BufferedImage(WW,HH,BufferedImage.TYPE_INT_RGB);
		for (int i=0; i<WW; i++)
			for(int j=0; j<HH;j++)
				bim.setRGB(i, j, colors.get(idmap[i][j]) );
		return bim;
	}

}

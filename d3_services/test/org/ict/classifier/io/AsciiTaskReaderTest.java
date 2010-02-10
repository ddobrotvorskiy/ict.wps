package org.ict.classifier.io;

import junit.framework.TestCase;
import org.ict.classifier.ClassificationTask;
import org.ict.classifier.Clazz;
import org.ict.classifier.Point;
import org.junit.Test;

import java.awt.*;
import java.io.Reader;
import java.io.StringReader;

/**
 * User: mityok
 * Date: 08.02.2010
 * Time: 22:32:38
 */
public class AsciiTaskReaderTest extends TestCase {

  private static final String taskStr =
                  "; ENVI Output of ROIs (4.4) [Sun Feb 07 09:41:07 2010]\n" +
                  "; Number of ROIs: 4\n" +
                  "; File Dimension: 357 x 321\n" +
                  ";\n" +
                  "; ROI name: water\n" +
                  "; ROI rgb value: {0, 0, 255}\n" +
                  "; ROI npts: 3\n" +
                  ";\n" +
                  "; ROI name: light_forest\n" +
                  "; ROI rgb value: {0, 255, 0}\n" +
                  "; ROI npts: 5\n" +
                  ";\n" +
                  "; ROI name: concrete\n" +
                  "; ROI rgb value: {255, 0, 0}\n" +
                  "; ROI npts: 4\n" +
                  ";\n" +
                  "; ROI name: dark_forest\n" +
                  "; ROI rgb value: {0, 139, 0}\n" +
                  "; ROI npts: 8\n" +
                  ";   B1   B2   B3\n" +
                  "     1   17   92\n" +
                  "    16   32  106\n" +
                  "    10   27  100\n" +
                  "\n" +
                  "    33  195  104\n" +
                  "    44  201  111\n" +
                  "    28  194  102\n" +
                  "     5  176   80\n" +
                  "    34  202  108\n" +
                  "\n" +
                  "   103  141  158\n" +
                  "   126  165  185\n" +
                  "    83  135  157\n" +
                  "    92  146  162\n" +
                  "\n" +
                  "    19   71    0\n" +
                  "    51  100   18\n" +
                  "     8   68    0\n" +
                  "    17   84    7\n" +
                  "    12   72    0\n" +
                  "    27   84    7\n" +
                  "    20   81   10\n" +
                  "    20   88   23";
          
  @Test
  public void testReadTask() throws Exception {


    ClassificationTask.Builder tb = new ClassificationTask.Builder();

    Clazz.Builder cb = new Clazz.Builder(0);
    cb.setLegend("water");
    cb.setColor(new Color(0, 0, 255));
    cb.addPoint(Point.create(new double[] {1, 17, 92}));
    cb.addPoint(Point.create(new double[] {16, 32, 106}));
    cb.addPoint(Point.create(new double[] {10, 27, 100}));
    tb.addClass(cb.createClass());

    cb = new Clazz.Builder(1);
    cb.setLegend("light_forest");
    cb.setColor(new Color(0, 255, 0));
    cb.addPoint(Point.create(new double[] {33, 195, 104}));
    cb.addPoint(Point.create(new double[] {44, 201, 111}));
    cb.addPoint(Point.create(new double[] {28, 194, 102}));
    cb.addPoint(Point.create(new double[] {5, 176,  80}));
    cb.addPoint(Point.create(new double[] {34, 202, 108}));
    tb.addClass(cb.createClass());

    cb = new Clazz.Builder(2);
    cb.setLegend("concrete");
    cb.setColor(new Color(255, 0, 0));
    cb.addPoint(Point.create(new double[] {103, 141, 158}));
    cb.addPoint(Point.create(new double[] {126, 165, 185}));
    cb.addPoint(Point.create(new double[] {83, 135, 157}));
    cb.addPoint(Point.create(new double[] {92, 146, 162}));
    tb.addClass(cb.createClass());

    cb = new Clazz.Builder(3);
    cb.setLegend("dark_forest");
    cb.setColor(new Color(0, 139, 0));
    cb.addPoint(Point.create(new double[] {19, 71, 0}));
    cb.addPoint(Point.create(new double[] {51, 100, 18}));
    cb.addPoint(Point.create(new double[] {8, 68, 0}));
    cb.addPoint(Point.create(new double[] {17, 84, 7}));
    cb.addPoint(Point.create(new double[] {12, 72, 0}));
    cb.addPoint(Point.create(new double[] {27, 84, 7}));
    cb.addPoint(Point.create(new double[] {20, 81, 10}));
    cb.addPoint(Point.create(new double[] {20, 88, 23}));
    tb.addClass(cb.createClass());

    ClassificationTask expectedTask = tb.createTask();

    Reader reader = new StringReader(taskStr);
    ClassificationTask actualTask = new AsciiTaskReader(reader).readTask();

    assertNotNull(actualTask);
    assertEquals(expectedTask, actualTask);
  }

}

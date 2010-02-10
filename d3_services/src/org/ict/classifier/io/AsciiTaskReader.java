package org.ict.classifier.io;

import org.ict.classifier.ClassificationTask;
import org.ict.classifier.Clazz;
import org.ict.classifier.Point;

import java.awt.Color;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Parses ascii input and creates ClassificationTask
 * Input format supported by Envi 4.4 (Export ROIs to ascii. only band values)
 *
 * <ol>
 * <li> each row contains point band values separated with spaces </li>
 * <li> empty row means beginning of the next class </li>
 * <li> rows started with ";" are comments </li>
 * <li> rows started with "; ROI name: " define class legends.
 *      Remaining part will be class legend. </li>
 * <li> rows started with "; ROI rgb value: " define class colors.
 *      Remaining part will be parsed to class color. </li>
 * </ol>
 *
 * User: mityok
 * Date: 07.02.2010
 * Time: 14:15:10
 */
public class AsciiTaskReader {

  private final LineNumberReader reader;
  
  // Colors and legends parsed from commented strings
  private final List<Color> colors = new ArrayList<Color>();
  private final List<String> legends = new ArrayList<String>();

  public AsciiTaskReader(Reader reader) {
    this.reader = new LineNumberReader(reader);
  }

  boolean called = false;

  public ClassificationTask readTask() throws IOException {
    try {
      if (called) {
        throw new IllegalStateException("readTask() already called");
      }
      called = true;

      int classId = -1;

      ClassificationTask.Builder taskBuilder = new ClassificationTask.Builder();
      Clazz.Builder classBuilder = new Clazz.Builder(++classId);

      String line;

      boolean newclass = false;

      while ((line = reader.readLine() )!= null) {
        if (line.trim().isEmpty()) {
          newclass = true;
          continue;
        }

        if (newclass) {
          newclass = false;
          classBuilder.setLegend(getLegend(classId));
          classBuilder.setColor(getColor(classId));
          taskBuilder.addClass(classBuilder.createClass());

          classBuilder = new Clazz.Builder(++classId);
        }

        if (line.startsWith(";")) {
          if (line.startsWith("; ROI name: ")) {
            legends.add(line.substring("; ROI name: ".length()));
          } else if (line.startsWith("; ROI rgb value: ")) {
            colors.add(parseColor(line.substring("; ROI rgb value: ".length())));
          }
        } else {
          classBuilder.addPoint(parsePoint(line));
        }
      }

      classBuilder.setLegend(getLegend(classId));
      classBuilder.setColor(getColor(classId));
      taskBuilder.addClass(classBuilder.createClass());

      return taskBuilder.createTask();
    } catch (RuntimeException e) {  // parse exception
      throw new IOException(e);
    }
  }

  private Color getColor(int classId) {
    if (classId < colors.size()) {
      return colors.get(classId);
    }

    return null;
  }

  private String getLegend(int classId) {
    if (classId < legends.size()) {
      return legends.get(classId);
    }

    return null;
  }


  private ArrayList<Double> point = new ArrayList<Double>(256);

  private Point parsePoint(String line) {
    point.clear();
    StringTokenizer tokenizer = new StringTokenizer(line, " ");
    while (tokenizer.hasMoreTokens()) {
      point.add(Double.valueOf(tokenizer.nextToken()));
    }
    double[] p = new double[point.size()];
    for (int i = 0 ; i < point.size(); i++) {
      p[i] = point.get(i);
    }
    return Point.create(p);
  }


  /**
   *  Parses color defined as "{r, g, b}"
   *  <p>r, g ,b are integer values (0..255)
   *
   *
   * @param s string color representation
   * @return Color instance
   */
  private Color parseColor(String s) {
    StringTokenizer tokenizer = new StringTokenizer(s, "{}, ");
    int r = Integer.parseInt(tokenizer.nextToken());
    int g = Integer.parseInt(tokenizer.nextToken());
    int b = Integer.parseInt(tokenizer.nextToken());
    return new Color(r, g, b);
  }

}

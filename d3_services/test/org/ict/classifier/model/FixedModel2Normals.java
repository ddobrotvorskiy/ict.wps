package org.ict.classifier.model;

import org.ict.classifier.*;
import org.ict.classifier.Point;
import org.ict.classifier.io.AsciiTaskReader;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * User: mityok
 * Date: 21.02.2010
 * Time: 16:47:25
 */
public class FixedModel2Normals implements Model {

  public static FixedModel2Normals newInstance() throws IOException {
    ClassificationTask task = new AsciiTaskReader(new FileReader("request/2normals.teach.txt")).readTask();
    return new FixedModel2Normals(task);
  }

  private final ClassificationTask task;

  private FixedModel2Normals(ClassificationTask task) {
    this.task = task;
  }

  public ClassificationTask getTask() {
    return task;
  }

  @Override
  public double getExpectedError() {
    return 0.05;
  }

  @Override
  public double getAcceptableError() {
    return 0.055;
  }

  @Override
  public Iterator<ControlPoint> iterator() {
    return new Iter(task);
  }

  private static final class Iter implements Iterator<ControlPoint> {

    private final Iterator<Clazz> ci;
    private Clazz c = null;
    private Iterator<Point> pi = null;

    public Iter (ClassificationTask task) {
      ci = task.getClasses().iterator();
    }

    @Override
    public boolean hasNext() {

      while (pi == null || !pi.hasNext()) {
        if (!ci.hasNext())
          return false;
        c = ci.next();
        pi = c.getPoints().iterator();
      }

      return true;
    }

    @Override
    public ControlPoint next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      return new ControlPoint(pi.next(), c);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public static void main(String [] argv) throws Exception {

    final FixedModel2Normals model = newInstance();
    JFrame frame = new JFrame("Display image");
    Panel panel = new Panel() {
      public void paint(Graphics g) {

        for (ControlPoint cp : model) {
          g.setColor(cp.c.getColor());
          g.drawLine(250 + (int)Math.round(3 * cp.p.get(0)), 50 + (int)Math.round(cp.p.get(1)),
                     250 + (int)Math.round(3 * cp.p.get(0)), 50 + (int)Math.round(cp.p.get(1)));
        }

        //Classifier classifier = Classifiers.createClassifier(model.getTask());

      }
    };
    frame.getContentPane().add(panel);
    frame.setSize(500, 500);
    frame.setVisible(true);
  }
}



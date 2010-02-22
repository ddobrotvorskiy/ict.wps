package org.ict.classifier;

import Jama.Matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * ClassificationTask is IMMUTABLE implementation of supervised classification task.
 * It describes training samples for classes. 
 *
 *
 * User: mityok
 * Date: 26.01.2010
 * Time: 22:24:19
 */
public final class ClassificationTask {
  private final int dimension;
  private final List<Clazz> classes;

  private ClassificationTask(Builder builder) {
    dimension = builder.dimension;
    classes = new ArrayList<Clazz>(builder.classes);
  }

  public List<Clazz> getClasses() {
    return Collections.unmodifiableList(classes);
  }

  public int getDimension() {
    return dimension;
  }

  public Clazz getClassById(int id) {
    for (Clazz c : classes) {
      if (c.getId() == id)
        return c;
    }

    return null;
  }

  @Override
  public String toString() {
    return "ClassificationTask{" +
            "dimension=" + dimension +
            ", classes=" + classes +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ClassificationTask that = (ClassificationTask) o;

    if (dimension != that.dimension) return false;

    Iterator<Clazz> i1 = classes.iterator();
    Iterator<Clazz> i2 = that.classes.iterator();

    while (i1.hasNext()) {
      if (!i2.hasNext())  return false;
      if (!i1.next().deepEquals(i2.next())) return false;
    }

    return i1.hasNext() == i2.hasNext();
  }

  @Override
  public int hashCode() {
    int result = dimension;
    result = 31 * result + (classes != null ? classes.hashCode() : 0);
    return result;
  }

  ClassificationTask recountInNewFeatures(Matrix features) {

    Builder builder = new Builder();

    for (Clazz c : classes) {
      builder.addClass(c.recountInNewFeatures(features));
    }

    return builder.createTask();
  }

  public static ClassificationTask createTrivial(Clazz c) {
    ClassificationTask.Builder b = new ClassificationTask.Builder();
    b.addClass(c);
    return b.createTask();
  }

  public static final class Builder {
    private int dimension = -1;
    private final List<Clazz> classes;

    public Builder() {
      classes = new ArrayList<Clazz>();
    }

    public void addClass(Clazz clazz) {
      for (Clazz c : classes) {
        if (c.getId() == clazz.getId()) {
          throw new IllegalArgumentException("All classes in task should have different id's");
        }
        if (dimension == -1) {
          dimension = clazz.getDimension();
        } else if (dimension != clazz.getDimension()) {
          throw new IllegalArgumentException("All classes in task should have equal dimensions");
        }
      }
      classes.add(clazz);
    }

    public ClassificationTask createTask() {
      if (classes.isEmpty()) {
        throw new IllegalArgumentException("At least one class should be specified");
      }
      
      return new ClassificationTask(this);
    }
  }
}

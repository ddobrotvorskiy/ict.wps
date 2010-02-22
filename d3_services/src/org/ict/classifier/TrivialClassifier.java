package org.ict.classifier;

import Jama.Matrix;

/**
 * User: mityok
 * Date: 02.02.2010
 * Time: 0:15:22
 */
class TrivialClassifier implements Classifier {
  private final ClassificationTask task;
  private final int classId;

  TrivialClassifier(Clazz clazz) {
    ClassificationTask.Builder b = new ClassificationTask.Builder();
    b.addClass(clazz);
    task = b.createTask();
    classId = clazz.getId();
  }

  TrivialClassifier(ClassificationTask task) {
    if (task.getClasses().size() != 1)
      throw new IllegalArgumentException("provided task is not trivial");

    this.task = task;
    this.classId = task.getClasses().get(0).getId();
  }

  @Override
  public ClassificationTask getTask() {
    return task;
  }

  @Override
  public int classify(Point point) {
    return classId;
  }

  @Override
  public Classifier recountInNewFeatures(Matrix features) {
    return new TrivialClassifier(task.recountInNewFeatures(features));
  }

  @Override
  public int getSlidingExamError() {
    return 0;
  }
}

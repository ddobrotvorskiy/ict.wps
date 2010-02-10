package org.ict.classifier;

import Jama.Matrix;

import java.util.*;

/**
 * User: mityok
 * Date: 01.02.2010
 * Time: 22:05:48
 */
class TreeClassifier implements Classifier {

  private final ClassificationTask task;
  private final Node head;

  public TreeClassifier(ClassificationTask task) {
    this.task = task;
    this.head = buildNodes(task);
  }

  @Override
  public int classify(Point point) {
    return head.classify(point);
  }

  @Override
  public ClassificationTask getTask() {
    return task;
  }

  @Override
  public Classifier recountInNewFeatures(Matrix features) {
    throw new UnsupportedOperationException("unsupported for tree classifier"); 
  }

  // TODO : think about better distance function
  private static double dist(Clazz c1, Clazz c2) {
    if (c1 == c2)
      return 0.0;

    double dist = Double.MAX_VALUE;

    for (Point p1 : c1.getPoints()) {
      for (Point p2 : c2.getPoints()) {
        dist = Math.min(dist, Point.distance(p1, p2));
      }
    }
    return dist;
  }

  // TODO : think about better bounds function
  private static double[] getBounds(ClassificationTask task) {

    double minDist = Double.MAX_VALUE;
    boolean init = false;
    for (Clazz c1 : task.getClasses()) {
      for (Clazz c2 : task.getClasses()) {
        if (c1 != c2) {
          minDist = Math.min(minDist, dist(c1, c2));
          init = true;
        }
      }
    }

    return new double[] {init ? (minDist * 2.0) : 0.0};
  }

  private static void subGroup(Node node, List<Node> nodes, List<Node> result, double bound) {
    for (Iterator<Node> i = nodes.iterator(); i.hasNext(); ) mainLoop : {
      Node n = i.next();
      for (Clazz c1 : node.classifier.getTask().getClasses()) {
        for (Clazz c2 : n.classifier.getTask().getClasses()) {
          if (dist(c1, c2) < bound) {
            i.remove();
            result.add(n);
            subGroup(n, nodes, result, bound);
            break mainLoop;
          }
        }
      }
    }
  }

  private static Node mergeNodes(List<Node> nodes) {
    if (nodes.isEmpty())
      throw new IllegalArgumentException("Empty collection not allowed here");

    if (nodes.size() == 1) {
      return nodes.get(0);
    }

    Map<Integer, Node> map = new HashMap<Integer, Node>();
    ClassificationTask.Builder taskBuilder = new ClassificationTask.Builder();
    for (Node node : nodes) {

      Clazz clazz = node.classifier.getTask().getClasses().get(0);

      Clazz.Builder classBuilder = new Clazz.Builder(clazz.getId());

      StringBuilder legend = new StringBuilder();
      for (Clazz c : node.classifier.getTask().getClasses()) {
        if (legend.length() > 0)
          legend.append(" + ");
        legend.append(c.getLegend());
        classBuilder.addPoints(c.getPoints());
      }
      map.put(clazz.getId(), node);
      classBuilder.setColor(clazz.getColor());
      classBuilder.setLegend(legend.toString());

      taskBuilder.addClass(classBuilder.createClass());
    }

    return new Node(Classifiers.createClassifier(taskBuilder.createTask()), map);
  }

  private static Node buildNodes(ClassificationTask task) {
    double[] hierarchyBounds = getBounds(task);

    List<Node> nodes = new LinkedList<Node>();
    for (Clazz c : task.getClasses()) {
      nodes.add(new Node(new TrivialClassifier(c), Collections.<Integer, Node>emptyMap()));
    }

    for (double bound : hierarchyBounds) {

      if (nodes.size() == 1)
        break;

      List<Node> oldNodes = new LinkedList<Node>(nodes);

      while (!oldNodes.isEmpty()) {
        List<Node> subGroup = new LinkedList<Node>();
        Node node = oldNodes.remove(0);
        subGroup.add(node);

        subGroup(node, oldNodes, subGroup, bound);

        nodes.removeAll(subGroup);
        nodes.add(mergeNodes(subGroup));
      }
    }

    return mergeNodes(nodes);
  }


  private static class Node {

    private final Map<Integer, Node> subNodes;
    private final Classifier classifier;

    private Node(Classifier classifier, Map<Integer, Node> subNodes) {
      this.classifier = classifier;
      this.subNodes = subNodes;
    }

    int classify(Point p) {

      int c = classifier.classify(p);

      return subNodes.isEmpty() ? c :subNodes.get(c).classify(p);
    }

  }

}

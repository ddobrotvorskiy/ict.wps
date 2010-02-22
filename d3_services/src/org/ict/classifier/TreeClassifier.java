package org.ict.classifier;

import Jama.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * User: mityok
 * Date: 01.02.2010
 * Time: 22:05:48
 */
class TreeClassifier implements Classifier {

  private static final Logger log = LoggerFactory.getLogger(TreeClassifier.class);

  private final ClassificationTask task;
  private final Node head;

  public TreeClassifier(ClassificationTask task) {
    this.task = task;
    this.head = buildNodes(task);

    log.trace("Nodes : \n" + head.toString());
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

  @Override
  public final int getSlidingExamError() {
    int errors = 0;
    for (Clazz c : task.getClasses()) {
      for (Point p : c.getPoints()) {
        if (c.getId() != classify(p)) {
          errors ++;
        }
      }
    }
    return errors;
  }

  // TODO : think about better distance function
  private static double dist(Clazz c1, Clazz c2) {
    if (c1 == c2)
      return 0.0;

    // минимальное расстояние между точками класса сильно подвержено влиянию выбросов
    // Поэтому:
    //  1. считаем все расстяния между точками,
    //  2. отбрасываем 5% минимальных
    //  3. минимальное из оставшихся есть расстояние между классами

    int allCount = c1.getPoints().size() * c2.getPoints().size();
    int outliers = Math.max(1, Math.round(0.05f * allCount));
    TreeSet<Double> treeSet = new TreeSet<Double>();

    Double last;
    double d;
    for (Point p1 : c1.getPoints()) {
      for (Point p2 : c2.getPoints()) {
        last = treeSet.pollLast();
        d = Point.distance(p1, p2);
        if (last == null || last < d) {
          treeSet.add(d);

          if (last != null && treeSet.size() > outliers)
            treeSet.remove(last);
        }
      }
    }
    last = treeSet.pollLast();
    return last != null ? last : Double.MAX_VALUE;
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

    return new double[] {init ? (minDist * 5.0) : 0.0};
  }

  private static void subGroup(Node node, List<Node> nodes, List<Node> result, double bound) {
    for (Iterator<Node> i = nodes.iterator(); i.hasNext(); ) mainLoop : {
      Node n = i.next();
      for (Clazz c1 : node.originalTask.getClasses()) {
        for (Clazz c2 : n.originalTask.getClasses()) {
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

      Clazz clazz = node.originalTask.getClasses().get(0);

      Clazz.Builder classBuilder = new Clazz.Builder(clazz.getId());

      StringBuilder legend = new StringBuilder();
      for (Clazz c : node.originalTask.getClasses()) {
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

    return new Node(taskBuilder.createTask(), map);
  }

  private static Node buildNodes(ClassificationTask task) {
    double[] hierarchyBounds = getBounds(task);

    List<Node> nodes = new LinkedList<Node>();
    for (Clazz c : task.getClasses()) {
      nodes.add(new Node(ClassificationTask.createTrivial(c), Collections.<Integer, Node>emptyMap()));
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
    private final ClassificationTask originalTask;
    private final Classifier classifier;

    //private final Classifier classifier;

    private Node(ClassificationTask task, Map<Integer, Node> subNodes) {
      this.originalTask = task;
      this.subNodes = subNodes;
      this.classifier = Classifiers.createClassifier(task);
    }

    int classify(Point p) {

      int c = classifier.classify(p);

      return subNodes.isEmpty() ? c :subNodes.get(c).classify(p);
    }

    private void print(int level, StringBuilder sb) {
      for (int i = 0; i < level ; i++) {
        sb.append("  ");
      }
      for (Clazz c : classifier.getTask().getClasses()) {
        sb.append(c.getId()).append(" : ")
          .append(c.getLegend())
          .append(" (").append(c.getPoints().size()).append(")");

        sb.append("; ");
      }
      sb.append(" se = ").append(classifier.getSlidingExamError());

      sb.append("\n");
      for (Node subNode : subNodes.values()) {
        subNode.print(level + 1, sb);
      }
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      print(0, sb);
      return sb.toString();
    }
  }

}

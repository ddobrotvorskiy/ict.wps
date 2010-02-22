package org.ict.clusterizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Typical task for unsupervized clustering algorithm.
 * Contains clustering parameters.
 *
 * User: Yorik
 * Date: 22.02.2010
 * Time: 14:08:49
 */
public class ClusteringTask {
  private ArrayList<ClusteringParameter> params;

  public ClusteringTask(ArrayList<ClusteringParameter> params){
    this.params = new ArrayList<ClusteringParameter>(params);
  }

  public ClusteringTask(ClusteringParameter[] params){
    this.params = new ArrayList<ClusteringParameter>(params.length);

    for (int i = 0; i < params.length; i++)
      this.params.add(params[i]);
  }

  public ClusteringTask(int paramsCount){
    params = new ArrayList<ClusteringParameter>(paramsCount);
  }

  public ClusteringTask(){
    this(3);
  }

  public void addParameter(ClusteringParameter par){
    if (par == null)
      throw new IllegalArgumentException("Null parameter not allowed");

    params.add(par);
  }

  public void addParameters(Iterable<ClusteringParameter> pars){
    if (pars == null)
      throw new IllegalArgumentException("Null parameter not allowed");

    for (ClusteringParameter p : pars)
      addParameter(p);
  }

  public List<ClusteringParameter> getParameters(){
    return Collections.unmodifiableList(params);
  }

  public int getParametersCount(){
    return params.size();
  }

  public boolean contains(ClusteringParameter p){
    if(p == null)
      throw new IllegalArgumentException("Null parameter not allowed");

    return params.contains(p);
  }

  @Override
  public String toString(){
    StringBuilder s = new StringBuilder();

    s.append("Clustering task: ");
    for (ClusteringParameter p : params)
      s.append(p).append(" ");
    s.append("\n");

    return s.toString();
  }

  @Override
  public boolean equals(Object o){
    if (this == o) return true;
    if (! (o instanceof ClusteringTask)) return false;

    ClusteringTask that = (ClusteringTask) o;

    for (ClusteringParameter p : params){
      if (! that.contains(p)) return false;
    }

    return this.getParametersCount() == that.getParametersCount();
  }

  public boolean isCompartible(ClusteringTask that){
    if (this == that) return true;

    if (this.getParametersCount() != that.getParametersCount()) return false;

    List<ClusteringParameter> theParams = that.getParameters();
    boolean was;

    for (ClusteringParameter p : params){
      was = false;
      for (ClusteringParameter theP : theParams){
        if (theP.getName().equals(p.getName())){
          was = true;
          break;
        }
      }
      if (was == false) return false;
    }

    return true;
  }
}

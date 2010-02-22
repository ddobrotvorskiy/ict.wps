package org.ict.clusterizer;

/**
 * Typical clustering parameter.
 * Includes name and double value.
 * 
 * User: Yorik
 * Date: 22.02.2010
 * Time: 14:17:02
 */
public final class ClusteringParameter {
  private final String name;
  private final double value;

  public ClusteringParameter(String name, double value){
    this.name = name;
    this.value = value;
  }

  public final String getName(){
    return name;
  }

  public final double getValue(){
    return value;
  }

  @Override
  public boolean equals(Object o){
    if (this == o) return true;
    if (! (o instanceof ClusteringParameter)) return false;

    ClusteringParameter p = (ClusteringParameter) o;

    return value == p.getValue() &&
           (name == null ? p.getName() == null : name == p.getName());
  }

  @Override
  public String toString(){
    StringBuilder s = new StringBuilder();

    s.append(name).append(" = ").append(value).append(";");

    return s.toString();
  }
}

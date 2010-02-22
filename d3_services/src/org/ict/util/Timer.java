package org.ict.util;

import java.util.Map;
import java.util.HashMap;

/**
 *
 * Класс <tt>Timer</tt> используется для измерения времени,<br>
 * проведенного в различных состояниях. Полезен для поиска узких мест.
 * <p>
 * Для каждого состояния подсчитывается
 * <ul>
 * <li>Количество вхождений с состояние</li>
 * <li>Суммарное время, проведенное в состоянии</li>
 * <li>Минимальное время</li>
 * <li>Максимальное время</li>
 * <li>Среднее время</li>
 * <li>Доля от общего времени, зарегистрированного <tt>Timer</tt>-ом</li>
 * </ul>
 *
 * <p>
 * <b>Не thread safe !</b>
 *
 * <p>Пример иcпользования:
 *
 * <pre>
 * class Foo {
 *
 *    private void methodToBeProfiled () {
 *       final Timer timer = new Timer();
 *       ....
 *       timer.setState("Loading data from DB");
 *       getSomeDataFromDb();
 *       ....
 *       timer.setState("Processing data");
 *       ....
 *        processData();
 *       ....
 *       timer.setState("Waiting for predicate to be true");
 *       while (!predicate) {
 *          lock.wait();
 *       }
 *
 *       timer.stop();
 *       ...
 *       System.out.println("Report :" + timer.getReport()) *
 *    }
 * }
 *</pre>
 *
 *Вывод в консоль:
 *
 *<pre>
 *  Report :
 *  Loading data from DB [cnt=1 , time=3000, min=3000, max=3000, avg=3000]   30%
 *  Processing data [cnt=1 , time=1000, min=1000,  max=1000, avg=1000]  10%
 *  Waiting for lock [cnt=1 , time=6000, min=6000,  max=6000, avg=6000]   60%
 *  Total time 10000 ms
 * </pre>
 *
 *
 * User: mityok
 * Date: 07.09.2009
 * Time: 14:36:06
 */
public final class Timer {

  private final ThreadInfo info = new ThreadInfo();

  /**
   * Сбросить счетчики
   */
  public void clear() {
    getInfo().clear();
  }

  /**
   * Выставить состояние
   */
  public void setState(String state) {
    getInfo().setState(state);
  }

  /**
   *  Остановка. Время, проведенное в остановленном состоянии не считается.
   */
  public void stop() {
    getInfo().setNoState();
  }

  public String getReport() {
    StringBuffer buffer = new StringBuffer();
    getReport(buffer);
    return buffer.toString();
  }

  public void getReport(StringBuffer buffer) {
    buffer.append(Thread.currentThread().getName()).append("\n");
    getInfo().getReport(buffer);
    buffer.append("\n");
  }


  private ThreadInfo getInfo() {
    return info;
  }

  private static class ThreadInfo {

    private final Map<String, StateInfo> map = new HashMap<String, StateInfo>();

    private StateInfo currentInfo = null;
    private long startTime = System.currentTimeMillis();

    public ThreadInfo() {
      setNoState();
    }

    public void clear() {
      map.clear();
      setNoState();
    }

    public void setState(String state) {

      long currentTime = System.currentTimeMillis();

      if (currentInfo != null)
        currentInfo.addTime(currentTime - startTime);

      startTime = currentTime;

      if (state == null) {
        currentInfo = null;
      } else if (map.containsKey(state)) {
        currentInfo = map.get(state);
      } else {
        currentInfo = new StateInfo();
        map.put(state, currentInfo);
      }
    }

    public void setNoState() {
      setState(null);
    }

    public String getReport() {
      StringBuffer buffer = new StringBuffer();
      getReport(buffer);
      return buffer.toString();
    }

    public void getReport(StringBuffer buffer) {
      long totalTime = 0;

      for (StateInfo info : map.values()) {
        totalTime += info.time;
      }

      for (Map.Entry<String, StateInfo> e : map.entrySet()) {
        buffer.append(e.getKey()).append(" ").append(e.getValue())
          .append(" ").append( e.getValue().time * 100.0 / ((double) totalTime)).append(" %\n");
      }
      buffer.append("Total time ").append(totalTime).append(" ms\n");
    }

  }

  private static class StateInfo {
    private long count = 0l;
    private long time = 0l;
    private long minTime = 0l;
    private long maxTime = 0l;

    public String toString() {
      return  "[count=" + count + ", time=" + time + ", min=" + minTime + ", max=" + maxTime + ", avg=" +
              (Math.round((1000.0 * time) / ((double) count))/ 1000.0) + "]";
    }

    public void addTime(long time) {
      this.count ++;
      this.time += time;
      if (time > maxTime)
        maxTime = time;
    }
  }

}

//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/commons/utils/CollectionUtils.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.commons.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * <code>CollectionUtils</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 21302 $, $Date: 2009-12-08 14:30:46 +0600 (Втр, 08 Дек 2009) $
 */
public class CollectionUtils {

    /**
     * Use like this: System.out.println(map(List<double[]> object, DOUBLE_PRINTER)) Unfortunately, a generic version
     * for all primitive-arrays is not possible...
     */
    public static final Mapper<String, double[]> DOUBLE_PRINTER = new Mapper<String, double[]>() {
        public String apply( double[] u ) {
            return Arrays.toString( u );
        }
    };

    /**
     * @param <T>
     * @return a mapper to output an object array
     * */
    public static <T> Mapper<String, T[]> getArrayPrinter() {
        return new Mapper<String, T[]>() {
            public String apply( T[] u ) {
                return Arrays.toString( u );
            }
        };
    }

    /**
     * @param <T>
     * @param c
     * @return a list of booleans, true if u instanceof c
     */
    public static <T> Mapper<Boolean, T> getInstanceofMapper( final Class<?> c ) {
        return new Mapper<Boolean, T>() {
            public Boolean apply( T u ) {
                return c.isInstance( u );
            }
        };
    }

    /***/
    public static final Reducer<Boolean> AND = new Reducer<Boolean>() {
        public Boolean reduce( Boolean t1, Boolean t2 ) {
            return t1 && t2;
        }
    };

    /**
     * @param <T>
     * @param list
     * @return removes any duplicates in the list. Keeps the first occurrence of duplicates.
     */
    public static <T> List<T> removeDuplicates( List<T> list ) {
        HashSet<T> set = new HashSet<T>( list.size() );
        ListIterator<T> i = list.listIterator();
        while ( i.hasNext() ) {
            T cur = i.next();
            if ( set.contains( cur ) ) {
                i.remove();
            } else {
                set.add( cur );
            }
        }

        return list;
    }

    /**
     * @param <T>
     * @param <U>
     * @param col
     * @return two separate lists
     */
    public static <T, U> Pair<ArrayList<T>, ArrayList<U>> unzip( Collection<Pair<T, U>> col ) {
        ArrayList<T> list1 = new ArrayList<T>( col.size() );
        ArrayList<U> list2 = new ArrayList<U>( col.size() );
        for ( Pair<T, U> pair : col ) {
            list1.add( pair.first );
            list2.add( pair.second );
        }
        return new Pair<ArrayList<T>, ArrayList<U>>( list1, list2 );
    }

    /**
     * Wraps a for loop and the creation of a new list.
     * 
     * @param <T>
     * @param <U>
     * @param col
     * @param mapper
     * @return a list where the mapper has been applied to each element in the map
     */
    public static <T, U> LinkedList<T> map( U[] col, Mapper<T, U> mapper ) {
        LinkedList<T> list = new LinkedList<T>();

        for ( U u : col ) {
            list.add( mapper.apply( u ) );
        }

        return list;
    }

    /**
     * Wraps a for loop and the creation of a new list.
     * 
     * @param <T>
     * @param <U>
     * @param col
     * @param mapper
     * @return a list where the mapper has been applied to each element in the map
     */
    public static <T, U> LinkedList<T> map( Collection<U> col, Mapper<T, U> mapper ) {
        LinkedList<T> list = new LinkedList<T>();

        for ( U u : col ) {
            list.add( mapper.apply( u ) );
        }

        return list;
    }

    /**
     * @param <T>
     * @param identity
     * @param col
     * @param reducer
     * @return the folded value
     */
    public static <T> T reduce( T identity, Collection<T> col, Reducer<T> reducer ) {
        if ( col.isEmpty() ) {
            return identity;
        }

        Iterator<T> i = col.iterator();

        T acc = i.next();

        while ( i.hasNext() ) {
            acc = reducer.reduce( acc, i.next() );
        }

        return acc;
    }

    /**
     * <code>Reducer</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author: aschmitz $
     * 
     * @version $Revision: 21302 $, $Date: 2009-12-08 14:30:46 +0600 (Втр, 08 Дек 2009) $
     * @param <T>
     */
    public static interface Reducer<T> {
        /**
         * @param t1
         * @param t2
         * @return the folded value
         */
        public T reduce( T t1, T t2 );
    }

    /**
     * <code>Mapper</code> gives a name to a simple function.
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author: aschmitz $
     * 
     * @version $Revision: 21302 $, $Date: 2009-12-08 14:30:46 +0600 (Втр, 08 Дек 2009) $
     * @param <T>
     *            the return type of the function
     * @param <U>
     *            the argument type of the function
     */
    public static interface Mapper<T, U> {
        /**
         * @param u
         * @return an implementation defined value
         */
        public T apply( U u );
    }

}

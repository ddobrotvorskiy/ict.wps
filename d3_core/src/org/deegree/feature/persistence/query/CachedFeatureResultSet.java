//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/feature/persistence/query/CachedFeatureResultSet.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.feature.persistence.query;

import java.util.Iterator;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;

/**
 * {@link FeatureResultSet} backed by a {@link FeatureCollection}
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 21300 $, $Date: 2009-12-08 03:38:20 +0600 (Втр, 08 Дек 2009) $
 */
public class CachedFeatureResultSet implements FeatureResultSet {

    private FeatureCollection fc;

    /**
     * Creates a new {@link CachedFeatureResultSet} that is backed by the given {@link FeatureCollection}.
     * 
     * @param fc
     *            FeatureCollection to back the result set
     */
    public CachedFeatureResultSet( FeatureCollection fc ) {
        this.fc = fc;
    }

    @Override
    public void close() {
    }

    @Override
    public FeatureCollection toCollection() {
        return fc;
    }

    @Override
    public Iterator<Feature> iterator() {
        return fc.iterator();
    }
}

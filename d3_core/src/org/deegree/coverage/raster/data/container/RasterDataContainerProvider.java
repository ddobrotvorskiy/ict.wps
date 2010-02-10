//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/coverage/raster/data/container/RasterDataContainerProvider.java $
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
package org.deegree.coverage.raster.data.container;

import java.util.ServiceLoader;

import org.deegree.coverage.raster.data.container.RasterDataContainerFactory.LoadingPolicy;


/**
 * This interface is for all classes that provide {@link RasterDataContainer}. A RasterDataContainer can implement lazy
 * loading or caching of raster data. The implemented provider will be used by the new Java 6 ServiceLoader.
 *
 * @see ServiceLoader
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version $Revision: 18171 $, $Date: 2009-06-17 21:00:07 +0700 (Срд, 17 Июн 2009) $
 *
 */
public interface RasterDataContainerProvider {
    /**
     * Returns a new RasterDataContainer for given type or null, if the implementation doesn't provide this type.
     *
     * @param type
     * @return a RasterDataContainer for given type or null
     */
    RasterDataContainer getRasterDataContainer( LoadingPolicy type );
}

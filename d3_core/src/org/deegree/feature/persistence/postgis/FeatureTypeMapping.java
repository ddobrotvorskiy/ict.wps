//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/feature/persistence/postgis/FeatureTypeMapping.java $
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
package org.deegree.feature.persistence.postgis;

import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.feature.persistence.postgis.jaxbconfig.FeatureTypeMappingHints;
import org.deegree.feature.persistence.postgis.jaxbconfig.PropertyMappingType;
import org.deegree.feature.types.ApplicationSchema;

/**
 * Mapping information that enables a {@link PostGISFeatureStore} to map between the feature types defined in an
 * {@link ApplicationSchema} and a relational schema stored in a PostGIS database.
 * 
 * @see PostGISApplicationSchema
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 20700 $, $Date: 2009-11-11 00:16:03 +0600 (Срд, 11 Ноя 2009) $
 */
public class FeatureTypeMapping {

    private FeatureTypeMappingHints ftMapping;

    private Map<QName, PropertyMappingType> propNameToMapping;

    public FeatureTypeMapping( FeatureTypeMappingHints ftMapping, Map<QName, PropertyMappingType> propNameToMapping ) {
        this.ftMapping = ftMapping;
        this.propNameToMapping = propNameToMapping;
    }

    public FeatureTypeMappingHints getFeatureTypeHints() {
        return ftMapping;
    }

    public PropertyMappingType getPropertyHints( QName propName ) {
        return propNameToMapping.get( propName );
    }
}

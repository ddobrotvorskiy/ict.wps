//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/feature/types/property/StringOrRefPropertyType.java $
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
package org.deegree.feature.types.property;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.types.ows.StringOrRef;

/**
 * {@link PropertyType} that defines a property with a {@link StringOrRef} value.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 20853 $, $Date: 2009-11-18 03:50:10 +0600 (Срд, 18 Ноя 2009) $
 */
public class StringOrRefPropertyType extends AbstractPropertyType {

    public StringOrRefPropertyType( QName name, int minOccurs, int maxOccurs, boolean isAbstract,
                                    List<PropertyType<?>> substitutions ) {
        super( name, minOccurs, maxOccurs, isAbstract, substitutions );
    }

    @Override
    public String toString() {
        String s = "- stringOrRef property type: '" + name + "', minOccurs=" + minOccurs + ", maxOccurs=" + maxOccurs;
        return s;
    }
}

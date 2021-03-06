//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/filter/function/se/StringPosition.java $
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
package org.deegree.filter.function.se;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.rendering.r2d.se.parser.SymbologyParser.updateOrContinue;
import static org.deegree.rendering.r2d.se.unevaluated.Continuation.SBUPDATER;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.filter.MatchableObject;
import org.deegree.filter.expression.Function;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;

/**
 * <code>StringPosition</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 20558 $, $Date: 2009-11-04 18:20:57 +0600 (Срд, 04 Ноя 2009) $
 */
public class StringPosition extends Function {

    private StringBuffer lookup;

    private Continuation<StringBuffer> lookupContn;

    private StringBuffer value;

    private Continuation<StringBuffer> contn;

    private boolean forward = true;

    /***/
    public StringPosition() {
        super( "StringPosition", null );
    }

    @Override
    public Object[] evaluate( MatchableObject f ) {
        StringBuffer sb = new StringBuffer( value.toString().trim() );
        if ( contn != null ) {
            contn.evaluate( sb, f );
        }

        String val = sb.toString();
        sb.setLength( 0 );
        sb.append( lookup.toString().trim() );
        if ( lookupContn != null ) {
            lookupContn.evaluate( sb, f );
        }
        String lookup = sb.toString();

        return new Object[] { ( ( forward ? val.indexOf( lookup ) : val.lastIndexOf( lookup ) ) + 1 ) + "" };
    }

    /**
     * @param in
     * @throws XMLStreamException
     */
    public void parse( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "StringPosition" );

        String dir = in.getAttributeValue( null, "searchDirection" );
        if ( dir != null ) {
            forward = !dir.equals( "backToFront" );
        }

        while ( !( in.isEndElement() && in.getLocalName().equals( "StringPosition" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "LookupString" ) ) {
                lookup = new StringBuffer();
                lookupContn = updateOrContinue( in, "LookupString", lookup, SBUPDATER, null );
            }
            if ( in.getLocalName().equals( "StringValue" ) ) {
                value = new StringBuffer();
                contn = updateOrContinue( in, "StringValue", value, SBUPDATER, null );
            }

        }

        in.require( END_ELEMENT, null, "StringPosition" );
    }

}

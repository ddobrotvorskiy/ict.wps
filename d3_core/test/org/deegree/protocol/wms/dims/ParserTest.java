//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/test/org/deegree/protocol/wms/dims/ParserTest.java $
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

package org.deegree.protocol.wms.dims;

import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * <code>ParserTest</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 20190 $, $Date: 2009-10-19 19:02:53 +0700 (Пнд, 19 Окт 2009) $
 */
public class ParserTest extends TestCase {

    /**
     * @throws Exception
     */
    @Test
    public void testParser()
                            throws Exception {
        parser p = new parser( new DimensionLexer( new StringReader( "123.445" ) ) );
        assertEquals( "123.445", ( (List<?>) p.parse().value ).get( 0 ) );

        p = new parser( new DimensionLexer( new StringReader( "123.445/543/2" ) ) );
        assertEquals( "123.445/543/2", ( (List<?>) p.parse().value ).get( 0 ).toString() );

        p = new parser( new DimensionLexer( new StringReader( "123.445/543" ) ) );
        assertEquals( "123.445/543/0", ( (List<?>) p.parse().value ).get( 0 ).toString() );

        p = new parser( new DimensionLexer( new StringReader( "a,b,c" ) ) );
        // do the quick'n'dirty list 'equals'
        assertEquals( "[a, b, c]", p.parse().value.toString() );

        p = new parser( new DimensionLexer( new StringReader( "    a , b , c   " ) ) );
        assertEquals( "[a, b, c]", p.parse().value.toString() );

        p = new parser( new DimensionLexer( new StringReader( "a/b/c,b/c/a,c/b/a" ) ) );
        assertEquals( "[a/b/c, b/c/a, c/b/a]", p.parse().value.toString() );

        p = new parser( new DimensionLexer( new StringReader( "1,2," ) ) );
        assertEquals( "Expected another value after '2,'.", ( (Exception) p.parse().value ).getMessage() );

        p = new parser( new DimensionLexer( new StringReader( "1/3,2/54/gf," ) ) );
        assertEquals( "Expected another interval after '2/54/gf,'.", ( (Exception) p.parse().value ).getMessage() );

        p = new parser( new DimensionLexer( new StringReader( "1/3, single" ) ) );
        assertEquals( "[1/3/0, single]", p.parse().value.toString() );

        p = new parser( new DimensionLexer( new StringReader( "1/3, single/" ) ) );
        assertEquals( "Missing max value for interval.", ( (Exception) p.parse().value ).getMessage() );

        p = new parser( new DimensionLexer( new StringReader( "single, pseudointerval/" ) ) );
        assertEquals( "Missing max value for interval.", ( (Exception) p.parse().value ).getMessage() );

        p = new parser( new DimensionLexer( new StringReader( "one/two, three/four/, " ) ) );
        assertEquals( "Expected another interval after 'three/four/0,'.", ( (Exception) p.parse().value ).getMessage() );
    }

}

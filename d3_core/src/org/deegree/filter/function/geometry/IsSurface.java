//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/core/trunk/src/org/deegree/filter/function/geometry/IsSurface.java $
package org.deegree.filter.function.geometry;

import java.util.List;

import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.MatchableObject;
import org.deegree.filter.expression.Function;
import org.deegree.geometry.primitive.Surface;

/**
 * <code>IsSurface</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 20558 $, $Date: 2009-11-04 18:20:57 +0600 (Срд, 04 Ноя 2009) $
 */
public class IsSurface extends Function {

    /**
     * @param exprs
     */
    public IsSurface( List<Expression> exprs ) {
        super( "IsSurface", exprs );
    }

    @Override
    public Object[] evaluate( MatchableObject f )
                            throws FilterEvaluationException {
        Object[] vals = getParams()[0].evaluate( f );
        return new Object[] { new Boolean( vals != null && vals.length > 0 && vals[0] instanceof Surface ).toString() };
    }

}

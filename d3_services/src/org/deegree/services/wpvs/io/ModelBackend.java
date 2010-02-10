//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/io/ModelBackend.java $
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

package org.deegree.services.wpvs.io;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.deegree.commons.configuration.DatabaseType;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.DirectGeometryBuffer;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.BuildingRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.PositionableModel;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.TreeRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.RenderablePrototype;
import org.deegree.services.wpvs.exception.DatasourceException;
import org.deegree.services.wpvs.io.db.PostgisBackend;
import org.deegree.services.wpvs.io.file.FileBackend;
import org.deegree.services.wpvs.io.serializer.BillBoardSerializer;
import org.deegree.services.wpvs.io.serializer.ObjectSerializer;
import org.deegree.services.wpvs.io.serializer.PrototypeSerializer;
import org.deegree.services.wpvs.io.serializer.WROSerializer;

/**
 * The <code>ModelBackend</code> provides methods for connections to the wpvs model in a database.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: 20137 $, $Date: 2009-10-15 15:29:31 +0700 (Чтв, 15 Окт 2009) $
 * @param <G>
 *            the Geometry type used to create the envelope from.
 * 
 */
public abstract class ModelBackend<G> {

    float[] wpvsTranslationVector;

    /**
     * The <code>Types</code> known to the modelbackend
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author: rbezema $
     * @version $Revision: 20137 $, $Date: 2009-10-15 15:29:31 +0700 (Чтв, 15 Окт 2009) $
     * 
     */
    public enum Type {
        /**
         * Buildings are to be handled
         */
        BUILDING( "building" ),
        /**
         * Prototypes are to be handled
         */
        PROTOTYPE( "prototype" ),
        /**
         * Stages are to be handled
         */
        STAGE( "building" ),
        /**
         * Trees are to be handled
         */
        TREE( "tree" );

        private String modelType;

        private Type( String modelType ) {
            this.modelType = modelType;
        }

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }

        /**
         * @return the name of the given type in the database.
         */
        public String getModelTypeName() {
            return modelType;
        }

    }

    GeometryFactory geomFactory = new GeometryFactory();

    private BillBoardSerializer treeSerializer = new BillBoardSerializer();

    private WROSerializer buildingSerializer = new WROSerializer();

    private PrototypeSerializer prototypeSerializer = new PrototypeSerializer();

    /**
     * @return the treeSerializer
     */
    public final BillBoardSerializer getTreeSerializer() {
        return treeSerializer;
    }

    /**
     * @return the buildingSerializer
     */
    public final WROSerializer getBuildingSerializer() {
        return buildingSerializer;
    }

    /**
     * @return the prototypeSerializer
     */
    public final PrototypeSerializer getPrototypeSerializer() {
        return prototypeSerializer;
    }

    /**
     * Retrieve some information on the number of vertices and texture coordinates used in the backend.
     * 
     * @param type
     * @return some information on the backend.
     */
    public abstract ModelBackendInfo getBackendInfo( Type type );

    /**
     * Retrieves the WorldRenderable objects from the prototypes table in the database.
     * 
     * @param geometryBuffer
     * 
     * @return the list of prototypes or the empty list if an error occurred, never <code>null</code>.
     */
    public abstract List<RenderablePrototype> loadProtoTypes( DirectGeometryBuffer geometryBuffer );

    /**
     * Retrieves the WorldRenderable objects from the backend.
     * 
     * @param bm
     *            to add the building to
     * 
     */
    public abstract void loadBuildings( BuildingRenderer bm );

    /**
     * Retrieves the Billboard objects from the backend.
     * 
     * @param tm
     *            to add the trees to.
     * 
     */
    public abstract void loadTrees( TreeRenderer tm );

    /**
     * Retrieves and deserializes objects from the given type.
     * 
     * 
     * @param objectType
     * 
     * @param sqlWhere
     *            to select the objects to retrieve
     * @return the list of deserialized object or the emtpy list if no results matched.
     * 
     * @throws IOException
     */
    public abstract List<Object> getDeSerializedObjectsForSQL( Type objectType, String sqlWhere )
                            throws IOException;

    /**
     * Retrieves and deserializes an object from the given type.
     * 
     * 
     * @param objectType
     * 
     * @param uuid
     *            of the object
     * @return the deserialized object or <code>null</code> if given uuid did not match.
     * 
     * @throws IOException
     */
    public abstract Object getDeSerializedObjectForUUID( Type objectType, String uuid )
                            throws IOException;

    /**
     * Create an envelope from the given Geometry type.
     * 
     * @param someGeometry
     * @return the envelope
     */
    public abstract Envelope createEnvelope( G someGeometry );

    /**
     * @param geometry
     * @param dimension
     *            of the result geometry
     * @return an envelope created from the given geometry
     */
    public abstract G createBackendEnvelope( Envelope geometry, int dimension );

    /**
     * Delete some data from the backend.
     * 
     * @param uuid
     *            the id of the object to delete.
     * @param objectType
     *            defining the type of object.
     * @param sqlWhere
     * @param qualityLevel
     * @return the number of records deleted by the given id.
     * 
     * @throws IOException
     */
    public abstract BackendResult delete( String uuid, Type objectType, int qualityLevel, String sqlWhere )
                            throws IOException;

    /**
     * Insert given data into the backend.
     * 
     * @param <P>
     *            type of the positionable
     * 
     * @param objects
     * @param objectType
     *            defining the type of the dataModel
     * @return information about the inserted objects.
     * @throws IOException
     */
    public abstract <P extends PositionableModel> BackendResult insert( List<DataObjectInfo<P>> objects, Type objectType )
                            throws IOException;

    /**
     * Creates the appropriate database backend for the given connection id. Or throws an
     * {@link UnsupportedOperationException} if the driver was not supported.
     * 
     * @param id
     *            to be used to initialize the if it contains org.deegree.services.wpvs.io.file.FileBackend, the file
     *            backend will be initialized.
     * @param fileURL
     *            of the file backend files
     * @return a ModelBackendInstance associated with the given driver.
     * @throws DatasourceException
     *             if the driver was not found on the classpath, or could not be initialized.
     * @throws UnsupportedOperationException
     *             if the given driver has no implementing backend.
     */
    public static ModelBackend<?> getInstance( String id, String fileURL )
                            throws DatasourceException, UnsupportedOperationException {

        String d = id;
        if ( id == null || "".equals( id ) ) {
            d = "FileBackend";
        }

        if ( d.toLowerCase().contains( "filebackend" ) ) {
            try {
                return new FileBackend( fileURL );
            } catch ( IOException e ) {
                throw new DatasourceException( "Filebackend could not be loaded because: " + e.getLocalizedMessage(), e );
            }

        }
        DatabaseType type = null;
        try {
            type = ConnectionManager.getConnectionType( d );
        } catch ( SQLException e ) {
            throw new DatasourceException( "Given id: " + d + " was not known to the database configuration, "
                                           + e.getLocalizedMessage(), e );
        }
        if ( type == DatabaseType.POSTGIS ) {
            return new PostgisBackend( d );
        }
        throw new UnsupportedOperationException(
                                                 "The driver you supplied could not be mapped to an implementing Database backend, please ask the list at deegree-users@lists.sourceforge.net list for help." );

    }

    /**
     * @return the prefix of the backend, which should be prefixed to the hosturl.
     */
    protected abstract String getDriverPrefix();

    /**
     * @param objectType
     * @return the mapped object serializer.
     */
    public ObjectSerializer<?> getSerializerForType( Type objectType ) {
        switch ( objectType ) {
        case TREE:
            return getTreeSerializer();
        case PROTOTYPE:
            return getPrototypeSerializer();
        default:
            return getBuildingSerializer();
        }

    }

    /**
     * flush any cached data to the backend and close the connections.
     * 
     * @throws IOException
     *             if flushing was not successful
     * 
     */
    public abstract void flush()
                            throws IOException;

    /**
     * @return the 2 dimensional wpvsTranslationVector, never <code>null</code>
     */
    public final float[] getWPVSTranslationVector() {
        if ( wpvsTranslationVector == null ) {
            wpvsTranslationVector = new float[] { 0, 0 };
        }
        return wpvsTranslationVector;
    }

    /**
     * @param wpvsTranslationVector
     *            the 2 dimensional wpvsTranslationVector to set
     */
    public final void setWPVSTranslationVector( float[] wpvsTranslationVector ) {
        this.wpvsTranslationVector = wpvsTranslationVector;
    }
}

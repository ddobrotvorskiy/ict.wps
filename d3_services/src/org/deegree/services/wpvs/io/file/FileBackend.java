//$HeadURL: http://wald.intevation.org/svn/deegree/deegree3/services/trunk/src/org/deegree/services/wpvs/io/file/FileBackend.java $
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

package org.deegree.services.wpvs.io.file;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.BillBoard;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.DirectGeometryBuffer;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.BuildingRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.PositionableModel;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.TreeRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.RenderablePrototype;
import org.deegree.services.wpvs.io.BackendResult;
import org.deegree.services.wpvs.io.DataObjectInfo;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackendInfo;
import org.deegree.services.wpvs.io.serializer.PrototypeSerializer;
import org.deegree.services.wpvs.io.serializer.WROSerializer;

/**
 * The <code>FileBackend</code> is the access to the model in files on the local file system.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: mschneider $
 * @version $Revision: 18171 $, $Date: 2009-06-17 21:00:07 +0700 (Срд, 17 Июн 2009) $
 *
 */
public class FileBackend extends ModelBackend<Envelope> {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( FileBackend.class );

    private ModelFile<BillBoard> treeFile;

    private ModelFile<WorldRenderableObject> buildingFile;

    private ModelFile<RenderablePrototype> prototypeFile;

    /**
     * @param directoryName
     * @throws IOException
     */
    public FileBackend( String directoryName ) throws IOException {
        File fileDir = new File( directoryName );
        if ( fileDir.exists() ) {
            treeFile = getTreeFile( directoryName );
            buildingFile = getBuildingFile( directoryName );
            prototypeFile = getPrototypeFile( directoryName );
        } else {
            throw new IOException( "The given directory: " + directoryName + " does not exist." );
        }
    }

    /**
     * @param directoryName
     * @return
     * @throws IOException
     */
    private ModelFile<WorldRenderableObject> getBuildingFile( String directoryName )
                            throws IOException {
        File[] files = mapFileType( directoryName, Type.BUILDING );
        return new ModelFile<WorldRenderableObject>( new IndexFile( files[0] ),
                                                     new DataFile<WorldRenderableObject>( files[1],
                                                                                          getBuildingSerializer() ),
                                                     files[2] );
    }

    /**
     * @param directoryName
     * @return
     * @throws IOException
     */
    private ModelFile<RenderablePrototype> getPrototypeFile( String directoryName )
                            throws IOException {
        File[] files = mapFileType( directoryName, Type.PROTOTYPE );
        return new ModelFile<RenderablePrototype>( new IndexFile( files[0] ),
                                                   new DataFile<RenderablePrototype>( files[1],
                                                                                      getPrototypeSerializer() ),
                                                   files[2] );
    }

    /**
     *
     * @param directoryName
     * @return
     * @throws IOException
     */
    private ModelFile<BillBoard> getTreeFile( String directoryName )
                            throws IOException {
        File[] files = mapFileType( directoryName, Type.TREE );
        return new ModelFile<BillBoard>( new IndexFile( files[0] ), new DataFile<BillBoard>( files[1],
                                                                                             getTreeSerializer() ),
                                         files[2] );
    }

    private File[] mapFileType( String dir, Type objectType ) {
        String fileName = objectType.toString();
        File data = new File( dir, fileName + ".bin" );
        File idx = new File( dir, fileName + ".idx" );
        File info = new File( dir, fileName + ".info" );
        return new File[] { idx, data, info };
    }

    @Override
    public Envelope createBackendEnvelope( Envelope geometry, int dimension ) {
        return geometry;
    }

    @Override
    public Envelope createEnvelope( Envelope someGeometry ) {
        return someGeometry;
    }

    @Override
    public Object getDeSerializedObjectForUUID( Type objectType, String uuid )
                            throws IOException {
        ModelFile<?> mf = mapTypeToFile( objectType );
        return mf.getObject( uuid );
    }

    /**
     * @param objectType
     * @return
     */
    private ModelFile<? extends PositionableModel> mapTypeToFile( Type objectType ) {
        switch ( objectType ) {
        case TREE:
            return treeFile;
        case PROTOTYPE:
            return prototypeFile;
        default:
            return buildingFile;
        }
    }

    @Override
    protected String getDriverPrefix() {
        return "";
    }

    @Override
    public void loadBuildings( BuildingRenderer bm ) {
        if ( bm != null ) {
            try {
                WROSerializer serializer = getBuildingSerializer();
                serializer.setGeometryBuffer( bm.getGeometryBuffer() );
                List<DataObjectInfo<WorldRenderableObject>> readAllFromFile = buildingFile.readAllFromFile();
                for ( DataObjectInfo<WorldRenderableObject> doi : readAllFromFile ) {
                    WorldRenderableObject rp = doi.getData();
                    rp.setId( doi.getUuid() );
                    rp.setTime( new Timestamp( doi.getTime() ).toString() );
                    rp.setExternalReference( doi.getExternalRef() );
                    rp.setName( doi.getName() );
                    rp.setType( doi.getType() );
                    bm.add( rp );
                }
            } catch ( IOException e ) {
                LOG.error( "Could not read buildings from file backend because: " + e.getLocalizedMessage(), e );
            }
        }
    }

    @Override
    public List<RenderablePrototype> loadProtoTypes( DirectGeometryBuffer geometryBuffer ) {
        List<RenderablePrototype> result = new LinkedList<RenderablePrototype>();
        try {
            PrototypeSerializer serializer = getPrototypeSerializer();
            serializer.setGeometryBuffer( geometryBuffer );
            List<DataObjectInfo<RenderablePrototype>> readAllFromFile = prototypeFile.readAllFromFile();
            for ( DataObjectInfo<RenderablePrototype> doi : readAllFromFile ) {
                RenderablePrototype rp = doi.getData();
                rp.setId( doi.getUuid() );
                rp.setTime( new Timestamp( doi.getTime() ).toString() );
                rp.setExternalReference( doi.getExternalRef() );
                rp.setName( doi.getName() );
                rp.setType( doi.getType() );
                result.add( rp );
            }
        } catch ( IOException e ) {
            LOG.error( "Could not read prototypes from file backend because: " + e.getLocalizedMessage(), e );
        }
        return result;
    }

    @Override
    public void loadTrees( TreeRenderer tm ) {

        if ( tm != null ) {
            try {
                List<DataObjectInfo<BillBoard>> readAllFromFile = treeFile.readAllFromFile();
                for ( DataObjectInfo<BillBoard> doi : readAllFromFile ) {

                    tm.add( doi.getData() );
                }
            } catch ( IOException e ) {
                LOG.error( "Could not read trees from file backend because: " + e.getLocalizedMessage(), e );
            }
        }
    }

    @Override
    public BackendResult delete( String uuid, Type objectType, int qualityLevel, String sqlWhere )
                            throws IOException {
        throw new UnsupportedOperationException( "Deleting of objects is currently not supported by the filebackend." );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends PositionableModel> BackendResult insert( List<DataObjectInfo<P>> objects, Type objectType )
                            throws IOException {
        BackendResult result = new BackendResult();
        Iterator<DataObjectInfo<P>> iterator = objects.iterator();
        if ( objectType == Type.TREE ) {
            while ( iterator.hasNext() ) {
                if ( treeFile.add( (DataObjectInfo<BillBoard>) iterator.next() ) ) {
                    result.insertCount++;
                }
            }
        } else if ( objectType == Type.PROTOTYPE ) {
            while ( iterator.hasNext() ) {
                if ( prototypeFile.add( (DataObjectInfo<RenderablePrototype>) iterator.next() ) ) {
                    result.insertCount++;
                }
            }
        } else {
            while ( iterator.hasNext() ) {
                if ( buildingFile.add( (DataObjectInfo<WorldRenderableObject>) iterator.next() ) ) {
                    result.insertCount++;
                }
            }
        }
        return result;
    }

    @Override
    public void flush()
                            throws IOException {
        treeFile.close();
        buildingFile.close();
        prototypeFile.close();
    }

    @Override
    public ModelBackendInfo getBackendInfo( org.deegree.services.wpvs.io.ModelBackend.Type type ) {
        switch ( type ) {
        case TREE:
            return treeFile.getBackendInfo();
        case BUILDING:
        case STAGE:
            return buildingFile.getBackendInfo();
        case PROTOTYPE:
            return prototypeFile.getBackendInfo();
        }
        return null;
    }

    @Override
    public List<Object> getDeSerializedObjectsForSQL( Type objectType, String sqlWhere ) {
        throw new UnsupportedOperationException( "Updating is currently not supported in the file backend." );
    }
}

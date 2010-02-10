//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.ict.wps.process;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.wps.*;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.*;
import java.net.URI;
import java.net.URL;

public class SimpleProcesslet implements Processlet {

    private static final Logger LOG = LoggerFactory.getLogger( SimpleProcesslet.class );

    /**
     *  Raster image to be classified
     */
    private static final String INPUT_RASTER = "InputRaster";

    /**
     *  Classified image with fake colors
     */
    private static final String OUTPUT_RASTER = "OutputRaster";



    public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info ) throws ProcessletException {
                
        LOG.trace( "BEGIN SimpleProcesslet#execute(), context: " + OGCFrontController.getContext() );

        LOG.trace("parsing inputs");
        InputBundle inputBundle = parseInputs(in);

        LOG.trace("performing process logic");
        OutputBundle outputBundle = doProcess(inputBundle);

        LOG.trace("sending results");
        sendOutput(outputBundle, out);

        LOG.trace( "END SimpleProcesslet#execute()" );
    }

    public void destroy() {
        LOG.debug( "SimpleProcesslet#destroy() called" );
    }

    public void init() {
        LOG.debug( "SimpleProcesslet#init() called" );
    }


    /**
     *  Main method.
     *  Process logic should be implemented here
     *
     */
    private OutputBundle doProcess(InputBundle inputBundle) {

        return new OutputBundle(inputBundle.raster);
    }



    private InputBundle parseInputs(ProcessletInputs inputs) throws ProcessletException {
        try {

            ComplexInput inputRaster = (ComplexInput) inputs.getParameter(INPUT_RASTER);
            LOG.debug( "- inputRaster: " + inputRaster);
            LOG.debug( "- inputRaster.mimeType : " + inputRaster.getMimeType());
            LOG.debug( "- inputRaster.encoding : " + inputRaster.getEncoding());
            LOG.debug( "- inputRaster.schema   : " + inputRaster.getSchema());

            RasterIOOptions opts = new RasterIOOptions(RasterGeoReference.OriginLocation.OUTER);
            opts.add(RasterIOOptions.READ_WLD_FILE, null);

            MimeType mimeType = new MimeType(inputRaster.getMimeType());
            opts.add(RasterIOOptions.OPT_FORMAT, mimeType.getSubType());

            AbstractRaster raster = RasterFactory.loadRasterFromStream(inputRaster.getValueAsBinaryStream(), opts);

            return new InputBundle(raster);

        } catch (MimeTypeParseException e) {
            LOG.error(e.getMessage(), e);
            throw new ProcessletException(e.getMessage());
        } catch (IOException e) {
            LOG.error("Raster load failed with i/o exception", e);
            throw new ProcessletException("Raster load failed with i/o exception");
        }
    }

    private void sendOutput(OutputBundle outputBundle, ProcessletOutputs out) throws ProcessletException {
        try {

            ComplexOutput outputRaster = (ComplexOutput) out.getParameter(OUTPUT_RASTER);
            LOG.debug( "Setting output raster (requested=" + outputRaster.isRequested() + ")" );
            LOG.debug( "- outputRaster: " + outputRaster);
            LOG.debug( "- outputRaster.mimeType : " + outputRaster.getRequestedMimeType());
            LOG.debug( "- outputRaster.schema   : " + outputRaster.getRequestedSchema());

            RasterIOOptions options = new RasterIOOptions();
            MimeType mimeType = new MimeType(outputRaster.getRequestedMimeType());
            options.add(RasterIOOptions.OPT_FORMAT, mimeType.getSubType());

            options.add( RasterIOOptions.READ_WLD_FILE, null);

            // for debug
            LOG.debug("storing result in file");
            RasterFactory.saveRasterToFile(outputBundle.raster, new File("/tmp/wps/result.png"), options);

            LOG.debug("sending result to output stream");
            RasterFactory.saveRasterToStream(outputBundle.raster, outputRaster.getBinaryOutputStream(), options);

        } catch (MimeTypeParseException e) {
            LOG.error(e.getMessage(), e);
            throw new ProcessletException(e.getMessage());
        } catch (IOException e) {
            LOG.error("Result raster transfer failed with i/o exception", e);
            throw new ProcessletException("Result raster transfer failed with i/o exception");
        }
    }


    /**
     *  Composite class for process inputs in usable format
     *
     */
    private static class InputBundle {
        final AbstractRaster raster;
        // other parameters will be added here

        private InputBundle(AbstractRaster raster) {
            assert raster != null;
            this.raster = raster;
        }
    }

    /**
     *  Composite class for process outputs in usable format
     *
     */
    private static class OutputBundle {
        final AbstractRaster raster;
        // other parameters will be added here

        private OutputBundle(AbstractRaster raster) {
            assert raster != null;
            this.raster = raster;
        }
    }

    public static void main(String[] args) {


        try {
            AbstractRaster raster = null;
            try {
                URL url = new URL("file:///tmp/wps/codase-small.jpg");

                RasterIOOptions opts = new RasterIOOptions(RasterGeoReference.OriginLocation.OUTER);
                opts.add(RasterIOOptions.READ_WLD_FILE, null);
                opts.add(RasterIOOptions.OPT_FORMAT, "jpg" );
                raster = RasterFactory.loadRasterFromStream(new BufferedInputStream(url.openStream()), opts);
                LOG.debug("raster == " + (raster == null ? "null" : raster));
            } catch (IOException e) {
                LOG.error("Raster load failed with i/o exception", e);
                throw new ProcessletException("Raster load failed with i/o exception");
            }

            try {
                RasterIOOptions options = new RasterIOOptions();
                options.add( RasterIOOptions.OPT_FORMAT, "png" );
                LOG.debug("storing result in file");
                RasterFactory.saveRasterToFile(raster.copy(), new File("/tmp/wps/result.png"), options);

                LOG.debug("sending result to output stream");
                RasterFactory.saveRasterToStream(raster.copy(), new FileOutputStream("/tmp/wps/result-stream.png"), options);
            } catch (IOException e) {
                LOG.error("Result raster transfer failed with i/o exception", e);
                throw new ProcessletException("Result raster transfer failed with i/o exception");
            }


        } catch (ProcessletException e) {
            LOG.error("Shit happened", e);
        }
    }
}
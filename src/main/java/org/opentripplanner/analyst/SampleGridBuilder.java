package org.opentripplanner.analyst;

import com.vividsolutions.jts.geom.Coordinate;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import org.apache.commons.math3.util.FastMath;
import org.opentripplanner.analyst.request.SampleGridRenderer;
import org.opentripplanner.common.geometry.AccumulativeGridSampler;
import org.opentripplanner.common.geometry.SparseMatrixZSampleGrid;
import org.opentripplanner.common.geometry.SphericalDistanceLibrary;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

class SampleGridBuilder implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(SampleGridBuilder.class);

    SampleGridBuilder() {
    }

    // TODO Lazy-initialize sample grid on demand so initial SPT finishes faster, and only isolines lag behind.
    // however, the existing sampler needs an SPT, not general vertex-time mappings.
    SparseMatrixZSampleGrid<SampleGridRenderer.WTWD> makeSampleGrid(ShortestPathTree spt) {
        long t0 = System.currentTimeMillis();
        final double gridSizeMeters = 300; // Todo: set dynamically and make sure this matches isoline builder params
        final double V0 = 1.00; // off-road walk speed in m/sec
        Coordinate coordinateOrigin = new Coordinate();
        final double cosLat = FastMath.cos(FastMath.toRadians(coordinateOrigin.y));
        double dY = Math.toDegrees(gridSizeMeters / SphericalDistanceLibrary.RADIUS_OF_EARTH_IN_M);
        double dX = dY / cosLat;
        SparseMatrixZSampleGrid<SampleGridRenderer.WTWD> sampleGrid = new SparseMatrixZSampleGrid<>(16, spt.getVertexCount(), dX, dY, coordinateOrigin);
        SampleGridRenderer.sampleSPT(spt, sampleGrid, gridSizeMeters * 0.7, gridSizeMeters, V0, spt
                .getOptions().getMaxWalkDistance(), Integer.MAX_VALUE, cosLat);
        long t1 = System.currentTimeMillis();
        LOG.info("Made SampleGrid from SPT in {} msec.", (int) (t1 - t0));
        return sampleGrid;
    }

    /**
     * Create the SampleGrid from whatever values are already in the TimeSurface, rather than looking at the SPT.
     * This is not really ideal since it includes only intersection nodes, and no points along the road segments.
     */
    SparseMatrixZSampleGrid<SampleGridRenderer.WTWD> makeSampleGridWithoutSPT(TObjectIntMap times) {
        long t0 = System.currentTimeMillis();
        final double gridSizeMeters = 300; // Todo: set dynamically and make sure this matches isoline builder params
        // Off-road max distance MUST be APPROX EQUALS to the grid precision
        // TODO: Loosen this restriction (by adding more closing sample).
        // Change the 0.8 magic factor here with caution.
        final double D0 = 0.8 * gridSizeMeters; // offroad walk distance roughly grid size
        final double V0 = 1.00; // off-road walk speed in m/sec
        Coordinate coordinateOrigin = new Coordinate();
        final double cosLat = FastMath.cos(FastMath.toRadians(coordinateOrigin.y));
        double dY = Math.toDegrees(gridSizeMeters / SphericalDistanceLibrary.RADIUS_OF_EARTH_IN_M);
        double dX = dY / cosLat;
        SparseMatrixZSampleGrid<SampleGridRenderer.WTWD> sampleGrid = new SparseMatrixZSampleGrid<>(16, times.size(), dX, dY, coordinateOrigin);
        AccumulativeGridSampler.AccumulativeMetric<SampleGridRenderer.WTWD> metric = new SampleGridRenderer.WTWDAccumulativeMetric(cosLat, D0, V0, gridSizeMeters);
        AccumulativeGridSampler<SampleGridRenderer.WTWD> sampler = new AccumulativeGridSampler<>(sampleGrid, metric);
        // Iterate over every vertex in this timesurface, adding it to the ZSampleGrid
        // TODO propagation along street geometries could happen at this stage, rather than when the SPT is still available.
        for (TObjectIntIterator<Vertex> iter = times.iterator(); iter.hasNext(); ) {
            iter.advance();
            Vertex vertex = iter.key();
            int time = iter.value();
            SampleGridRenderer.WTWD z = new SampleGridRenderer.WTWD();
            z.w = 1.0;
            z.d = 0.0;
            z.wTime = time;
            z.wBoardings = 0; // unused
            z.wWalkDist = 0; // unused
            sampler.addSamplingPoint(vertex.getCoordinate(), z, V0);
        }
        sampler.close();
        long t1 = System.currentTimeMillis();
        LOG.info("Made scalar SampleGrid from TimeSurface in {} msec.", (int) (t1 - t0));
        return sampleGrid;
    }
}
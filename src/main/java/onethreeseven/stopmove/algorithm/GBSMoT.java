package onethreeseven.stopmove.algorithm;

import onethreeseven.datastructures.model.*;
import onethreeseven.geo.model.LatLonBounds;
import onethreeseven.geo.projection.AbstractGeographicProjection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This algorithms finds stops in raw spatio-temporal trajectories.
 * It does this by checking if entries reside within known geographic
 * regions for a specified amount of time.
 * This algorithm is based on:
 * "A Model for Enriching Trajectories with Semantic Geographical Information"
 * @author Luke Bermingham
 */
public class GBSMoT {

    public STStopTrajectory run(SpatioCompositeTrajectory traj,
                                double regionSize, long minTimeMillis){
        Collection<LatLonBounds> regions = getRegions(traj, regionSize);
        return run(traj, regions, minTimeMillis);
    }

    private Collection<LatLonBounds> getRegions(SpatioCompositeTrajectory traj, double regionSize){
        final LatLonBounds studyRegion = traj.calculateGeoBounds();
        final AbstractGeographicProjection projection = traj.getProjection();
        final double[] bottomLeft = projection.geographicToCartesian(studyRegion.getMinLat(), studyRegion.getMinLon());
        LatLonBounds[][] regions;
        {
            double[] topRight = projection.geographicToCartesian(studyRegion.getMaxLat(), studyRegion.getMaxLon());
            int[] extents = getGridCell(topRight, bottomLeft, regionSize);
            regions = new LatLonBounds[extents[0]+1][extents[1]+1];
        }

        //go through traj and make regions
        for (int i = 0; i < traj.size(); i++) {
            double[] xy = traj.getCoords(i, true);
            int[] gridCell = getGridCell(xy, bottomLeft, regionSize);
            LatLonBounds bounds = regions[gridCell[0]][gridCell[1]];
            if(bounds == null){
                double minX = bottomLeft[0] + (regionSize * gridCell[0]);
                double maxX = minX + regionSize;
                double minY = bottomLeft[1] + (regionSize * gridCell[1]);
                double maxY = minY + regionSize;
                double[] minLatLon = projection.cartesianToGeographic(new double[]{minX, minY});
                double[] maxLatLon = projection.cartesianToGeographic(new double[]{maxX, maxY});
                bounds = new LatLonBounds(minLatLon[0], maxLatLon[0], minLatLon[1], maxLatLon[1]);
                regions[gridCell[0]][gridCell[1]] = bounds;
            }
        }

        //go through each region
        ArrayList<LatLonBounds> regionList = new ArrayList<>();
        for (LatLonBounds[] regionArr : regions) {
            for (LatLonBounds region : regionArr) {
                if(region != null){
                    regionList.add(region);
                }
            }
        }
        return regionList;
    }

    private int[] getGridCell(double[] xy, double[] bottomLeft, double regionSize){
        double deltaX = Math.abs(xy[0]-bottomLeft[0]);
        double deltaY = Math.abs(xy[1]-bottomLeft[1]);
        int x = (int) Math.floor(deltaX/regionSize);
        int y = (int) Math.floor(deltaY/regionSize);
        return new int[]{x,y};
    }

    private LocalDateTime getTime(SpatioCompositeTrajectory traj, int i){
        CompositePt pt = traj.get(i);
        LocalDateTime entryTime;
        if(pt instanceof STPt){
            entryTime = ((STPt) pt).getTime();
        }
        else{
            entryTime = LocalDateTime.now();
        }
        return entryTime;
    }

    /**
     * Labels contiguous portions of a trajectory as stops if they reside within
     * a region for at least a certain amount of time. Otherwise they are labelled as moves.
     * @param traj The trajectory to find stops and move for.
     * @param regions The regions that we are interested in finding stops in.
     * @param minTimeMillis The duration (in milliseconds) that that a region must be visited for
     *                      to be considered a stop.
     * @return A spatio-temporal trajectory labelled with stop and move annotations.
     */
    public STStopTrajectory run(SpatioCompositeTrajectory traj,
                                Collection<LatLonBounds> regions, long minTimeMillis){

        STStopTrajectory output = new STStopTrajectory(false, traj.getProjection());

        LatLonBounds currentRegion = null;
        int enterIdx = -1;
        int exitIdx = -1;

        for (int i = 0; i < traj.size(); i++) {
            double[] latlon = traj.getCoords(i, false);
            double lat = latlon[0];
            double lon = latlon[1];

            boolean endRegionVisit = true;
            LatLonBounds entryRegion = getEnvelopingRegion(regions, lat, lon);

            if(entryRegion == null){
                //if this happens probably need to do region calculation wholly in Euclidean space
                output.addGeographic(traj.getCoords(i, false), new TimeAndStop(getTime(traj, i), false));
                continue;
            }


            //we have one or more points in the same region
            if(currentRegion != null && currentRegion.contains(entryRegion)){
                exitIdx = i;
                endRegionVisit = (i == traj.size()-1);
            }
            //we have a new region for the first time
            else if(currentRegion == null){
                enterIdx = i;
                exitIdx = i;
                currentRegion = entryRegion;
                endRegionVisit = (i == traj.size()-1);
            }
            //we have the end of potential region visit, add the entries to the trajectory
            if(endRegionVisit){
                LocalDateTime enterTime = getTime(traj, enterIdx);
                LocalDateTime exitTime = getTime(traj, exitIdx);
                long deltaMillis = ChronoUnit.MILLIS.between(enterTime, exitTime);
                //this is the line that indicates whether these visiting entries were stopped or not
                boolean wasStopped = deltaMillis >= minTimeMillis;
                for (int j = enterIdx; j <= exitIdx; j++) {
                    output.addGeographic(traj.getCoords(j, false), new TimeAndStop(getTime(traj, j), wasStopped));
                }

                //re-do the current index using its own region
                if(!currentRegion.contains(entryRegion)){
                    i--;
                }

                currentRegion = null;
                enterIdx = -1;
                exitIdx = -1;
            }
        }
        return output;
    }

    private LatLonBounds getEnvelopingRegion(Collection<LatLonBounds> regions, double lat, double lon){
        for (LatLonBounds region : regions) {
            if(region.contains(lat, lon)){
                return region;
            }
        }
        return null;
    }

}

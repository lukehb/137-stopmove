package onethreeseven.stopmove.algorithm;

import onethreeseven.datastructures.model.STStopPt;
import onethreeseven.datastructures.model.STStopTrajectory;
import java.time.temporal.ChronoUnit;

/**
 * Compute some basic stats about Stop/Move Trajectories.
 * # of stops
 * # of moves
 * # of moving episodes (contiguous moving recordings)
 * # of stopping episodes (contiguous stopping recordings)
 * duration of the trajectory (in seconds)
 * @author Luke Bermingham
 */
public class CountStopsAndMoves {

    private int nStops = 0;
    private int nMoves = 0;
    private int nMoveEpisodes = 0;
    private int nStopEpisodes = 0;
    private long durationSeconds = 0;
    private long intervalSeconds = 0L;

    /**
     * @param traj A stop/move annotated spatio-temporal trajectory.
     */
    public void run(STStopTrajectory traj){
        nStops = 0;
        nMoves = 0;
        nStopEpisodes = 0;
        nMoveEpisodes = 0;

        boolean isStopped = false;
        STStopPt prevPt = null;
        for (STStopPt pt : traj) {
            if(pt.isStopped()){
                nStops++;
                if(!isStopped){
                    isStopped = true;
                    nStopEpisodes++;
                }
            }
            else if(!pt.isStopped()){
                nMoves++;
                if(isStopped){
                    isStopped = false;
                    nMoveEpisodes++;
                }
            }
            if(prevPt != null){
                intervalSeconds += ChronoUnit.SECONDS.between(prevPt.getTime(), pt.getTime());
            }
            prevPt = pt;
        }

        durationSeconds = ChronoUnit.SECONDS.between(
                traj.getTime(0),
                traj.getTime(traj.size()-1));

        intervalSeconds = Math.round(intervalSeconds/(double)traj.size()-1);

    }

    public long getIntervalSeconds(){
        return intervalSeconds;
    }

    public int getnStops() {
        return nStops;
    }

    public int getnMoves() {
        return nMoves;
    }

    public int getnMoveEpisodes() {
        return nMoveEpisodes;
    }

    public int getnStopEpisodes() {
        return nStopEpisodes;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }
}

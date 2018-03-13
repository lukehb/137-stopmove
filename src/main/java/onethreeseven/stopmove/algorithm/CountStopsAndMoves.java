package onethreeseven.stopmove.algorithm;

import onethreeseven.common.util.Maths;
import onethreeseven.datastructures.model.STStopPt;
import onethreeseven.datastructures.model.STStopTrajectory;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private long minIntervalSeconds = 0L;
    private long maxIntervalSeconds = 0L;
    private long modalSamplingRate = 0L;

    /**
     * @param traj A stop/move annotated spatio-temporal trajectory.
     */
    public void run(STStopTrajectory traj){
        nStops = 0;
        nMoves = 0;
        nStopEpisodes = 0;
        nMoveEpisodes = 0;
        minIntervalSeconds = Long.MAX_VALUE;


        double[] deltaTimes = new double[traj.size()-1];

        boolean isStopped = false;
        STStopPt prevPt = null;
        for (int i = 0; i < traj.size(); i++) {
            STStopPt pt = traj.get(i);
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
                long deltaSeconds = ChronoUnit.SECONDS.between(prevPt.getTime(), pt.getTime());
                deltaTimes[i-1] = deltaSeconds;
                intervalSeconds += deltaSeconds;
                if(deltaSeconds < minIntervalSeconds){
                    minIntervalSeconds = deltaSeconds;
                }
                if(deltaSeconds > maxIntervalSeconds){
                    maxIntervalSeconds = deltaSeconds;
                }
            }
            prevPt = pt;
        }

        durationSeconds = ChronoUnit.SECONDS.between(
                traj.getTime(0),
                traj.getTime(traj.size()-1));

        modalSamplingRate = (long) Maths.mode(deltaTimes);

        intervalSeconds = Math.round(intervalSeconds/(double)traj.size()-1);

    }

    public Map<String, String> getAllStats(){
        Map<String, String> stats = new LinkedHashMap<>();
        stats.put("Stops:", String.valueOf(nStops));
        stats.put("Moves:", String.valueOf(nMoves));
        stats.put("Stop episodes:", String.valueOf(nStopEpisodes));
        stats.put("Move episodes:", String.valueOf(nMoveEpisodes));
        stats.put("Trajectory duration(s):", String.valueOf(getDurationSeconds()));
        stats.put("Minimum recording interval(s):", String.valueOf(getMinIntervalSeconds()));
        stats.put("Maximum recording interval(s):", String.valueOf(getMaxIntervalSeconds()));
        stats.put("Modal recording interval(s):", String.valueOf(getModalSamplingSeconds()));
        return stats;
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

    public long getMinIntervalSeconds() {
        return minIntervalSeconds;
    }

    public long getMaxIntervalSeconds() {
        return maxIntervalSeconds;
    }

    public long getModalSamplingSeconds() {
        return modalSamplingRate;
    }
}

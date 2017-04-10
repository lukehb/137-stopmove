package onethreeseven.stopmove.algorithm;

import onethreeseven.datastructures.model.STPt;
import onethreeseven.datastructures.model.SpatioCompositeTrajectory;
import java.util.*;

/**
 * Estimates the displacement value where stops becomes moves.
 * @author Luke Bermingham
 */
public class EstimateStopVariance {

    public double estimate(SpatioCompositeTrajectory<? extends STPt> traj){

        int k = 2;

        //get displacements
        double[] displacements = new double[traj.size()-1];
        for (int i = 1; i < traj.size(); i++) {
            displacements[i-1] = traj.getEuclideanDistance(i, i-1);
        }

        //get two clusters
        UnivariateKMeans.Cluster[] clusters = new UnivariateKMeans().run(displacements, k);

        //get a value in between the two cluster boundaries

        double c1Max = clusters[0].getMax();
        double c2Max = clusters[1].getMax();
        //double c1Min = clusters[0].getMin();
        //double c2Min = clusters[1].getMin();

        if(c1Max < c2Max){
            return clusters[0].getMean() + (clusters[0].getStd());
        }else{
            return clusters[1].getMean() + (clusters[1].getStd());
        }
    }







}

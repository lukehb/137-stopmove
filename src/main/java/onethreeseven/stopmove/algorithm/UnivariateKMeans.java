package onethreeseven.stopmove.algorithm;

import onethreeseven.common.util.Maths;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

/**
 * Lloyd's K-Means clustering.
 * See: https://en.wikipedia.org/wiki/K-means_clustering#Standard_algorithm
 * @author Luke Bermingham
 */
public class UnivariateKMeans {

    /**
     * Runs the K-Means algorithm.
     * @param data The data to clusters.
     * @param k How many clusters to find.
     * @return K clusters found in the data.
     */
    public Cluster[] run(double[] data, int k){
        Cluster[] clusters = new Cluster[k];
        for (int i = 0; i < k; i++) {
            clusters[i] = new Cluster();
        }

        //split displacements between each of the clusters
        {
            final double splitSize = (data.length - 1) / (double)clusters.length;
            for (int i = 1; i < data.length; i++) {
                int clusterIdx = (int) (Math.ceil(i / (splitSize))) - 1;
                clusters[clusterIdx].add(data[i]);
            }
            //to initialise the means
            for (Cluster cluster : clusters) {
                cluster.update();
            }
        }

        //this is the whole k-means algorithm, reassign values within clusters
        //then update the cluster's means
        boolean keepClustering = true;
        while(keepClustering){
            boolean reassignedValue = false;
            //reassign cluster values if possible
            for (int i = 0; i < clusters.length; i++) {
                if(reassign(clusters, i)){
                    reassignedValue = true;
                }
            }
            //update clusters if necessary
            if(reassignedValue){
                for (Cluster cluster : clusters) {
                    cluster.update();
                }
            }
            keepClustering = reassignedValue;
        }
        return clusters;
    }


    /**
     * Check all values in a certain cluster to see if they are closer
     * to another cluster's centroid. If they are move them to the closest cluster.
     * @param clusters The set of all clusters.
     * @param clusterIdx The index of the cluster who needs its values checked for reassignment.
     * @return True if any values swapped clusters, otherwise false.
     */
    private boolean reassign(Cluster[] clusters, int clusterIdx){
        boolean movedValue = false;
        Cluster clusterToReassign = clusters[clusterIdx];
        Iterator<Double> c1Iter = clusterToReassign.iterator();
        while(c1Iter.hasNext()){
            double value = c1Iter.next();

            double bestDist = Math.abs(clusterToReassign.centroid - value);;
            Cluster betterCluster = null;

            //check other clusters for a closer centroid
            for (int i = 0; i < clusters.length; i++) {
                if(i == clusterIdx){continue;}
                Cluster otherCluster = clusters[i];
                double distToOtherCluster = Math.abs(otherCluster.centroid - value);
                if(distToOtherCluster < bestDist){
                    bestDist = distToOtherCluster;
                    betterCluster = otherCluster;
                }
            }
            //if there was a closer centroid, move the value to the appropriate cluster
            if(betterCluster != null){
                c1Iter.remove();
                clusterToReassign.total -= value;
                betterCluster.add(value);
                movedValue = true;
            }
        }
        return movedValue;
    }

    public class Cluster implements Iterable<Double>{

        private final ArrayList<Double> values;
        private double centroid;
        private double total = 0;

        Cluster(){
            this.values = new ArrayList<>();
        }

        private void update(){
            this.centroid = total / values.size();
        }

        private void add(Double value){
            this.values.add(value);
            this.total += value;
        }

        public double getMean(){
            return centroid;
        }

        public double getStd(){
            return Maths.std(this.values.stream().mapToDouble(value -> value).toArray());
        }

        public double getMax(){
            Optional<Double> maxOpt = values.stream().max(Double::compare);
            if(maxOpt.isPresent()){
                return maxOpt.get();
            }
            throw new IllegalStateException("Somehow couldn't get max value...");
        }

        public double getMin(){
            Optional<Double> minOpt = values.stream().min(Double::compare);
            if(minOpt.isPresent()){
                return minOpt.get();
            }
            throw new IllegalStateException("Somehow couldn't get max value...");
        }

        @Override
        public Iterator<Double> iterator() {
            return this.values.iterator();
        }
    }

}

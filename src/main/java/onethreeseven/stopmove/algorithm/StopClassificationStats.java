package onethreeseven.stopmove.algorithm;

import onethreeseven.datastructures.model.STStopTrajectory;

/**
 * Compare calculated values to ground truth
 * in order to determine classification accuracy/truePositiveRate/precision
 * @author Luke Bermingham
 */
public class StopClassificationStats {

    private double tp = 0;
    private double tn = 0;
    private double fp = 0;
    private double fn = 0;
    private int nStops = 0;
    private int nMoves = 0;

    //stats
    private double precision = 0;
    private double truePositiveRate = 0;
    private double falsePositiveRate = 0;
    private double trueNegativeRate = 0;
    private double falseNegativeRate = 0;
    private double accuracy = 0;
    private double fScore = 0;
    private double mcc = 0;
    private double informedness = 0;

    public void calculateStats(STStopTrajectory truth, STStopTrajectory calculated){

        if(truth.size() != calculated.size()){
            throw new IllegalArgumentException("Data must be same size");
        }

        tp = 0;
        tn = 0;
        fp = 0;
        fn = 0;

        for (int i = 0; i < truth.size(); i++) {

            boolean truthIsStopped = truth.get(i).isStopped();
            boolean calculatedIsStopped = calculated.get(i).isStopped();

            if(calculatedIsStopped){
                nStops++;
            }else{
                nMoves++;
            }

            if(calculatedIsStopped && truthIsStopped){
                tp++;
            }
            else if(!calculatedIsStopped && !truthIsStopped){
                tn++;
            }
            else if(calculatedIsStopped){
                fp++;
            }
            else {
                fn++;
            }

        }
        //calculate stats
        precision = tp/(tp+fp);
        truePositiveRate = tp/(tp+fn);
        trueNegativeRate = tn/(tn+fp);
        falsePositiveRate = fp/(fp + tn);
        falseNegativeRate = fn/(tp + fn);
        accuracy = (tp+tn)/(tn+tp+fp+fn);
        fScore = 2 * (precision * truePositiveRate)/(precision + truePositiveRate);
        mcc = (tp * tn - fp * fn)/Math.sqrt( (tp+fp)*(tp+fn)*(tn+fp)*(tn+fn) );
        informedness = tp/(tp+fn) + tn/(tn+fp) - 1;
    }

    public void printStats(){
        System.out.println("True positive:" + tp);
        System.out.println("True negative:" + tn);
        System.out.println("False positive:" + fp);
        System.out.println("False negative:" + fn);
        System.out.println("Precision: " + precision);
        System.out.println("True positive rate: " + truePositiveRate);
        System.out.println("True negative rate: " + trueNegativeRate);
        System.out.println("False positive rate: " + falsePositiveRate);
        System.out.println("False negative rate: " + falseNegativeRate);
        System.out.println("Accuracy: " + accuracy);
        System.out.println("fScore: " + fScore);
        System.out.println("MCC: " + mcc);
        System.out.println("Informedness: " + informedness);
        System.out.println("# stops: " + nStops);
        System.out.println("# moves: " + nMoves);
    }

    public double getMCC() {
        return Double.isNaN(mcc) ? 0 : mcc;
    }

    public double getInformedness() {
        return informedness;
    }

    public int getTruePositive() {
        return (int) tp;
    }

    public int getTrueNegative() {
        return (int) tn;
    }

    public int getFalsePositive() {
        return (int) fp;
    }

    public int getFalseNegative() {
        return (int) fn;
    }

    public double getPrecision() {
        return precision;
    }

    public double getTruePositiveRate() {
        return truePositiveRate;
    }

    public double getTrueNegativeRate() {
        return trueNegativeRate;
    }

    public double getFalsePositiveRate() {
        return falsePositiveRate;
    }

    public double getFalseNegativeRate() {
        return falseNegativeRate;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public double getfScore() {
        return fScore;
    }

    public int getnStops() {
        return nStops;
    }

    public int getnMoves() {
        return nMoves;
    }
}

# 137-stopmove
Algorithms to automatically discover stops and moves in GPS trajectories.

## Algorithms
* **POSMIT** - Probability of Stops and Moves in Trajectories. Uses a probabilistic approach to determine if a given entry in the trajectory is stopping or moving. Unlike traditional approaches POSMIT gives the user a quantifiable accuracy in regards to the classifications of each stop/move.
* **CB-SMoT** - Clustering-based Stop and Moves of Trajectories. Uses a modified DB-SCAN algorithm to find stops. 

## Usages
Below are some code usages for each of the algorithms provided in this repo.

### POSMIT*
```java
//load in a trajectory to process
STTrajectory traj = ....
//how many entries to search either side of each entry 
int nSearchRadius = 2;
//how much a stop can spatially jitter around and still be considered a stop (remember GPS is quite noisy)
double stopVariance = 1.5;
//intialise the actual POSMIT algorithm
POSMIT algo = new POSMIT();
//find the stop probability for each entry in the trajectory
double[] stopProbabilities = algo.run(traj, nSearchRadius, stopVariance);
//set a threshold for an entry to be classified as a stop, otherwise it is a move.
//i.e in this case only entries with an 80% or higher probability become stops,
//whilst lower probability entries become moves.
double minStopConfidence = 0.8;
//convert the trajectory into a trajectory with stop/move annotations at each entry
STStopTrajectory stopTraj = algo.toStopTrajectory(traj, stopProbabilities, minStopConfidence);
```
*Note: see [`FindStopsPOSMIT.java`](https://github.com/lukehb/137-stopmove/blob/master/src/main/java/onethreeseven/stopmove/experiments/FindStopsPOSMIT.java) for a full example.

### CB-SMoT*
```java
//load in a trajectory to process
STTrajectory traj = ....
//Much like DB-SCAN you must specify a spatial epsilon to control cluster growth
double epsMeters = 1.5;
//CB-SMoT introduces a concept that a cluster can only become a stop if it has a 
//total duration equal to or exceeding a user-specified minimum stop duration
long minTimeMillis = 3000L;
//run the CB-SMoT algorithm
STStopTrajectory outTraj = new CBSMoT().run(traj, epsMeters, minTimeMillis);
```
*Note: see [`FindStopsCBSmot.java`](https://github.com/lukehb/137-stopmove/blob/master/src/main/java/onethreeseven/stopmove/experiments/FindStopsCBSmot.java) for a full example.

package hoop.gi3;

import java.util.Arrays;

public class MLE {
  
  private Fraction[][] history;
  
  private double[] attackingScores;
  private double[] defendingScores;
  
  private int players;
  
  public MLE (Fraction[][] history, int teams, int teamSize) {
    this.history = history;
    this.players = teams*teamSize;
    
    attackingScores = new double[players];
    defendingScores = new double[players];
    for (int i = 0; i < players; i++) {
      attackingScores[i] = 0.5;
      defendingScores[i] = 0.5;
    }
  }
  
  public void iterate(int numIters) {
    Pair[] pairs = new Pair[2*players];
    for (int i = 0; i < pairs.length; i++) 
      pairs[i] = new Pair(i % players, i / players == 0);
    
    Arrays.sort(pairs);
    
    for (int i = 0; i < numIters; i++) {
      for (int j = 0; j < pairs.length; j++) {
        setMle(pairs[j]);
      }
    }
  }
  
  // change to include formula for passing...
  public double likelihood(Pair p, double val) {
    double prob = 1.0;
    int index = p.index;
    for (int i = 0; i < players; i++) {
      if (p.attacking) {
        prob *= Math.pow((val + defendingScores[i])/2.0, history[index][i].made());
        prob *= Math.pow(1 - (val + defendingScores[i])/2.0, history[index][i].missed());
      }
      else {
        prob *= Math.pow((val + attackingScores[i])/2.0, history[i][index].made());
        prob *= Math.pow(1 - (val + attackingScores[i])/2.0, history[i][index].missed());
      }
    }
    return prob;
  }
  
  // Use log probabilities?
  public void setMle(Pair p) {
    double maxProb = Double.NEGATIVE_INFINITY;
    double argMax = 0.5;
 
    for (double i = 0.001; i < 1.0; i += 0.001) {
      double tempProb = likelihood(p, i);
      if (tempProb > maxProb) {
        maxProb = tempProb;
        argMax = i;
      }
    }
    
    if (p.attacking) {
      attackingScores[p.index] = argMax;
    }
    else {
      defendingScores[p.index] = argMax;
    }
  }
  
  public int attempts(Pair pair) {
    int index = pair.index;
    boolean attacking = pair.attacking;
    
    int count = 0;
    for (int i = 0; i < players; i++) {
      if (attacking) {
        count += history[index][i].attempted();
      }
      else {
        count += history[i][index].attempted();
      }
    }
    return count;
  }
    
  public class Pair implements Comparable<Pair> {
    public int index;
    public boolean attacking;
    
    public Pair(int index, boolean attacking) {
      this.index = index;
      this.attacking = attacking;
    }
    
    public int compareTo(Pair that) {
      return attempts(this) - attempts(that);
    }
    
  }
    
}
  
  
  
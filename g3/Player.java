package hoop.g3;

import java.util.*;

public class Player {
  public int team;
  public int id;
  
  private int baskets;
  private int basketsAttempted;
  
  private int blocks;
  private int blocksAttempted;
  
  private int passes;
  private int passesAttempted;
  
  private int steals;
  private int stealsAttempted;
  
  public Player (int team, int id) {
    this.team = team;
    this.id = id;
    
    baskets = 0;
    basketsAttempted = 0;
    
    blocks = 0;
    blocksAttempted = 0;
    
    passes = 0;
    passesAttempted = 0;
    
    steals = 0;
    stealsAttempted = 0;
  }
  
  public double shooting() {
    return (double) (1 + baskets)/(2 + basketsAttempted);
  }
  
  public double blocking() {
    double blockPct = (double) (1 + blocks)/(2 + blocksAttempted);
    return 1 - blockPct;
  }
  
  public double passing() {
    double passPct = (double) (9 + passes)/(10 + passesAttempted);
    double passScore = (passPct - 0.8)/0.2;
    if (passScore < 0.0)
      return 0.0;
    else
      return passScore;
  }
  
  public double stealing() {
    double stealPct = (double) (9 + steals)/(10 + stealsAttempted);
    double stealScore = (1.0 - stealPct)/0.2;
    if (stealScore > 1.0)
      return 1.0;
    else 
      return stealScore;
  }
  
  public void shotMade() {
    baskets++;
    basketsAttempted++;
  }
  
  public void shotMissed() {
    basketsAttempted++;
  }
  
  public void blockMade() {
    blocks++;
    blocksAttempted++;
  }
  
  public void blockMissed() {
    blocksAttempted++;
  }
  
  public void passMade() {
    passes++;
    passesAttempted++;
  }
  
  public void passMissed() {
    passesAttempted++;
  }
  
  public void stealMade() {
    steals++;
    stealsAttempted++;
  }
  
  public void stealMissed() {
    stealsAttempted++;
  }
  
}
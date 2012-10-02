/* 
 * Player.java
 * Group 2: David Mauskop, Manushi Shah, Shane Chine
 * 
 * Class that represents a basketball player
 * 
 */

package hoop.g3;

import java.util.*;

public class Player {
  public int team;                  // ID for this Player's team
  public int id;                    // Player ID (Jersey Number)
  
  private int baskets;              // Total shots made by this player
  private int basketsAttempted;     // Total shots attempted
  
  private int blocks;               // Total shots blocked
  private int blocksAttempted;      // Total block opportunities
  
  private int passes;               // Total passes completed
  private int passesAttempted;      // Total passes attempted
  
  private int steals;               // Total passes stolen
  private int stealsAttempted;      // Total steal opportunities
  
  
  /* Public constructor, takes team id and player id */
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
  
  /* Returns estimated shooting score */
  public double shooting() {
    return (double) (1 + baskets)/(2 + basketsAttempted);
  }
  
  /* Returns estimated blocking score */
  public double blocking() {
    double blockPct = (double) (1 + blocks)/(2 + blocksAttempted);
    return 1 - blockPct;
  }
  
  /* Returns estimated passing score */
  public double passing() {
    double passPct = (double) (9 + passes)/(10 + passesAttempted);
    double passScore = (passPct - 0.8)/0.2;
    if (passScore < 0.0)
      return 0.0;
    else
      return passScore;
  }
  
  /* Returns estimated stealing score */
  public double stealing() {
    double stealPct = (double) (9 + steals)/(10 + stealsAttempted);
    double stealScore = (1.0 - stealPct)/0.2;
    if (stealScore > 1.0)
      return 1.0;
    else 
      return stealScore;
  }
  
  /* Updates player counts after a made shot */
  public void shotMade() {
    baskets++;
    basketsAttempted++;
  }
  
  /* Updates player counts after a missed shot */
  public void shotMissed() {
    basketsAttempted++;
  }
  
  /* Updates player counts after a successful block */
  public void blockMade() {
    blocks++;
    blocksAttempted++;
  }
  
  /* Updates player counts after a failed block attempt */
  public void blockMissed() {
    blocksAttempted++;
  }
  
  /* Updates player counts after a successful pass */
  public void passMade() {
    passes++;
    passesAttempted++;
  }
  
  /* Updates player counts after an unsuccessful pass */
  public void passMissed() {
    passesAttempted++;
  }
  
  /* Updates player counts after a steal */
  public void stealMade() {
    steals++;
    stealsAttempted++;
  }
  
  /* Updates player counts after a failes steal attempt */
  public void stealMissed() {
    stealsAttempted++;
  }
  
}
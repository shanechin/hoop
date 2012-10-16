package hoop.g3;

import hoop.sim.*;
import java.util.*;

public class Team implements hoop.sim.Team {
  
  private HashMap<String, Integer> teamIDs;
  private Player[] myRoster;
  private ArrayList<Player[]> rosters;
  private int teamSize;
  private int id = 0;
  private int teamsSeen = 0;
  private int gamesPlayed = 0;
  private int gamesSeen = 0;
  
  private Player[] myTeam;
  private String opponent;
  private Player[] opposingTeam;
  private Player initHolder;
  private Player currentHolder;
  
  public String name() {
    return "Group 3";
  }
  
  private void init(int numPlayers) {
    teamSize = numPlayers;
    teamIDs = new HashMap<String, Integer>();
    teamIDs.put(name(), id);
    teamsSeen++;
    
    myRoster = new Player[teamSize];
    for (int i = 0; i < teamSize; i++)
      myRoster[i] = new Player(id, i+1);
    
    rosters = new ArrayList<Player[]>();  
    myTeam = new Player[5];
    opposingTeam = new Player[5];
  }
  
  private int initTeam(String teamName) {
    if (!teamIDs.containsKey(teamName)) {
      teamIDs.put(teamName, teamsSeen);
      Player[] roster = new Player[teamSize];
      for (int i = 0; i < teamSize; i++)
        roster[i] = new Player(teamsSeen, i+1);
      rosters.add(roster);
      teamsSeen++;
    }
    
    return teamIDs.get(teamName);
  }
  
  private void watchGame(Game g) {
    int teamA = initTeam(g.teamA);
    int teamB = initTeam(g.teamB);
    Player[] rosterA = rosters.get(teamA);
    Player[] rosterB = rosters.get(teamB);
    
    for (int i = 0; i < g.rounds(); i++) {
      Game.Round r = g.round(i);
      int[] attackers = r.holders();
      int[] defenders = r.defenders();

      for (int j = 0; j < attackers.length-1; j++) {
        if (r.attacksA) {
          rosterA[attackers[j]].passMade();
          rosterB[defenders[j]].stealMissed();
        }
        else {
          rosterB[attackers[j]].passMade();
          rosterA[defenders[j]].stealMissed();
        }
      }
      
      int lastAttacker = attackers[attackers.length-1];
      int lastDefender = defenders[defenders.length-1];
      switch (r.lastAction()) {
        case SCORED:
          if (r.attacksA) {
            rosterA[lastAttacker].shotMade();
            rosterB[lastDefender].blockMissed();
          }
          else {
            rosterB[lastAttacker].shotMade();
            rosterA[lastAttacker].blockMissed();
          }
          break;
        case MISSED:
          if (r.attacksA) {
            rosterA[lastAttacker].shotMissed();
            rosterB[lastDefender].blockMade();
          }
          else {
            rosterB[lastAttacker].shotMissed();
            rosterA[lastAttacker].blockMade();
          }
          break;
        case STOLEN:
          if (r.attacksA) {
            rosterA[lastAttacker].passMissed();
            rosterB[lastDefender].stealMade();
          }
          else {
            rosterB[lastAttacker].passMissed();
            rosterA[lastAttacker].stealMade();
          }
          break;
      }
    }
    gamesSeen++;
  }
  
  /* return 5 players in 1,2,3,4,...,P where P total players */
  public int[] pickTeam(String opponent, int totalPlayers, Game[] history) {
    if (gamesPlayed == 0) 
      init(totalPlayers);
    
    for (int i = gamesSeen; i < history.length; i++)
      watchGame(history[i]);
                
    this.opponent = opponent;
    if (opponent.equals(name())) {
      int startIndex;
      if (gamesPlayed == 0) 
        startIndex = 0;
      else 
        startIndex = (myTeam[myTeam.length-1].id + 1) % teamSize;
      
      for (int i = 0; i < 5; i++)
        myTeam[i] = myRoster[(startIndex+i) % teamSize];  
    }
    else {
      Player[] sorted = Arrays.copyOf(myRoster, teamSize);
      Arrays.sort(sorted);
      for (int i = 0; i < 5; i++)
        myTeam[i] = myRoster[sorted[i].id];
    }
    
    gamesPlayed++;  // Maybe I should wait until game ends to increment this?
    
    int[] lineup = new int[5];
    for (int i = 0; i < 5; i++)
      lineup[i] = myTeam[i].id;
      
    return lineup;
  }
  
  /* get players of opponent team */
  public void opponentTeam(int[] opponentPlayers) {
    Player[] opposingRoster = rosters.get(teamIDs.get(opponent));
    
    for (int i = 0; i < 5; i++) 
      opposingTeam[i] = opposingRoster[opponentPlayers[i]];
  }
  
  /* return one of 1,2,3,4,5 to pick initial ball holder */
  public int pickAttack(int yourScore, int opponentScore) {
    double maxPassing = Double.NEGATIVE_INFINITY;
    int bestPasser = -1;
    for (int i = 0; i < 5; i++) {
      if (myTeam[i].passing() > maxPassing) {
        maxPassing = myTeam[i].passing();
        bestPasser = i;
      }
    }
    initHolder = myTeam[bestPasser];
    return (bestPasser + 1);
  }
  
  /* if 0 then shoot, otherwise pass to 1,2,3,4,5 */
  public int action(int[] defenders) {
    if (currentHolder != initHolder)
      return 0;
    
    Player[] defense = new Player[5];
    for (int i = 0; i < 5; i++) 
      defense[i] = opposingTeam[defenders[i]-1];
    
    double biggestMismatch = Double.NEGATIVE_INFINITY;
    int shooter = -1;
    for (int i = 0; i < 5; i++) {
      double mismatch = myTeam[i].shooting() + defense[i].blocking();
      if (mismatch > biggestMismatch) {
        biggestMismatch = mismatch;
        shooter = i;
      }
    }
    currentHolder = myTeam[shooter];
    return (shooter + 1);
  }
  
  /* pick defenders, use 1,2,3,4,5 for players */
  public int[] pickDefend(int yourScore, int opponentScore, int ballHolder) {
    return (new int[] {1, 2, 3, 4, 5});
  }
  
}
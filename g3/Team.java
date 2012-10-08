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
  private int lineupSize = 5;
  
  private Player[] myTeam;
  private int opponentID;
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
    rosters.add(myRoster);
    myTeam = new Player[lineupSize];
    opposingTeam = new Player[lineupSize];
  }
  
  private int initTeam(String teamName) {
    if (!teamIDs.containsKey(teamName)) {
      teamIDs.put(teamName, teamsSeen);
      Player[] roster = new Player[teamSize];
      for (int i = 0; i < teamSize; i++)  // Start counting at 1
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
    
    System.out.println("Never watch a game?");
    for (int i = 0; i < g.rounds(); i++) {
      Game.Round r = g.round(i);
      int[] attackers = r.holders();
      int[] defenders = r.defenders();

      for (int j2 = 0; j2 < defenders.length; j2++) {
			System.out.println("defenders" + defenders[j2]);
		}
      for (int j2 = 0; j2 < attackers.length; j2++) {
			System.out.println("attackers" + attackers[j2]);
		}
      for (int j = 0; j < attackers.length-1; j++) {
        if (r.attacksA) {
          //System.out.println(rosterA.length);
          //System.out.println(rosterB.length);
          
         // rosterA[attackers[j]-1].passMade();
          rosterB[defenders[j]-1].stealMissed();
        }
        else {
          //rosterB[attackers[j]-1].passMade();
          rosterA[defenders[j]-1].stealMissed();
        }
      }
      
      int lastAttacker = attackers[attackers.length-1]-1;
      int lastDefender = defenders[defenders.length-1]-1;
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
    System.out.println("game history" + history.length);
    System.out.println("games played" + gamesPlayed);
    
    
    for (int i = gamesSeen; i < history.length; i++){
    	System.out.println("i" + i);
        System.out.println("his of i"+history[i]);
      watchGame(history[i]);
    }
    
    opponentID = initTeam(opponent);
    if (opponentID == id) {
    	System.out.println("Do we ever see this?");
      int startIndex;
      if (gamesPlayed == 0)
        startIndex = 0;
      else 
        startIndex = myTeam[lineupSize-1].id % teamSize;
      
      for (int i = 0; i < lineupSize; i++)
        myTeam[i] = myRoster[(startIndex + i) % teamSize];
    }
    else {
      Player[] sorted = Arrays.copyOf(myRoster, teamSize);
      Arrays.sort(sorted);
      for (int i = 0; i < lineupSize; i++)
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
    Player[] opposingRoster = rosters.get(opponentID);
    
    for (int i = 0; i < 5; i++) 
      opposingTeam[i] = opposingRoster[opponentPlayers[i]-1];
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
    currentHolder = initHolder;
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
      if ((mismatch > biggestMismatch) && (myTeam[i] != currentHolder)){
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
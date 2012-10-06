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
  private int lineupSize = 5;
  
  private Player[] myTeam;
  private int opponentID;
  private Player[] opposingTeam;
  private Player initHolder;
  private Player currentHolder;
  private Coach currentCoach;
  private Coach internalCoach;
  private Coach externalCoach;
  
  private boolean firstGame = true;

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
    internalCoach = new InternalCoach();
    externalCoach = new ExternalCoach();   
    
    firstGame = false;
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
    int[] playersA = g.playersA();
    int[] playersB = g.playersB();
    Player[] rosterA = rosters.get(teamA);
    Player[] rosterB = rosters.get(teamB);
    
    Player[] lineupA = new Player[lineupSize];
    Player[] lineupB = new Player[lineupSize];
    
    for (int i = 0; i < lineupSize; i++) {
      lineupA[i] = rosterA[playersA[i]-1];
      lineupB[i] = rosterB[playersB[i]-1];
    }
    
    for (int i = 0; i < g.rounds(); i++) {
      Game.Round r = g.round(i);
      int[] attackers = r.holders();
      int[] defenders = r.defenders();

      for (int j = 0; j < attackers.length-1; j++) {
        if (r.attacksA) {
          int index = attackers[j]-1;
          lineupA[index].passMade();
          lineupB[defenders[index]-1].stealMissed();
        }
        else {
          int index = attackers[j]-1;
          lineupB[index].passMade();
          lineupA[defenders[index]-1].stealMissed();
        }
      }
      
      int lastAttacker = attackers[attackers.length-1]-1;
      int lastDefender = defenders[lastAttacker]-1;
      switch (r.lastAction()) {
        case SCORED:
          if (r.attacksA) {
            lineupA[lastAttacker].shotMade();
            lineupB[lastDefender].blockMissed();
          }
          else {
            lineupB[lastAttacker].shotMade();
            lineupA[lastAttacker].blockMissed();
          }
          break;
        case MISSED:
          if (r.attacksA) {
            lineupA[lastAttacker].shotMissed();
            lineupB[lastDefender].blockMade();
          }
          else {
            lineupB[lastAttacker].shotMissed();
            lineupA[lastAttacker].blockMade();
          }
          break;
        case STOLEN:
          if (r.attacksA) {
            lineupA[lastAttacker].passMissed();
            lineupB[lastDefender].stealMade();
          }
          else {
            lineupB[lastAttacker].passMissed();
            lineupA[lastAttacker].stealMade();
          }
          break;
      }
    }
  }
  
  private void printCounts() {
    for (int i = 0; i < myRoster.length; i++) {
      Player a = myRoster[i];
      System.out.println("Player " + i + " Stats:");
      System.out.println("Shooting: " + myRoster[i].shooting() + " " + a.baskets + " " + a.basketsAttempted);
      System.out.println("Blocking: " + myRoster[i].blocking() + " " + a.blocks + " " + a.blocksAttempted);
      System.out.println("Passing: " + myRoster[i].passing() + " " + a.passes + " " + a.passesAttempted);
      System.out.println("Stealing: " + myRoster[i].stealing() + " " + a.steals + " " + a.stealsAttempted);
    }
  }
  
  /* return 5 players in 1,2,3,4,...,P where P total players */
  public int[] pickTeam(String opponent, int totalPlayers, Game[] history) {
    if (firstGame) 
      init(totalPlayers);
    
    for (int i = 0; i < history.length; i++)
      watchGame(history[i]);
    
    opponentID = initTeam(opponent);
    
    if (opponentID == id)
     currentCoach = internalCoach;
    else {
      printCounts();
     currentCoach = externalCoach;
    }
    
    myTeam = currentCoach.pickTeam(myRoster, teamSize, lineupSize);
    int[] lineup = new int[5];
    for (int i = 0; i < 5; i++)
      lineup[i] = myTeam[i].id;
      
    return lineup;
 
  }
  
  /* get players of opponent team */
  public void opponentTeam(int[] opponentPlayers) {
    if (currentCoach == internalCoach)
      return;
    
    Player[] opposingRoster = rosters.get(opponentID);
    
    for (int i = 0; i < 5; i++) 
      opposingTeam[i] = opposingRoster[opponentPlayers[i]-1];
    // doesn't need to be instance variable?
    
    currentCoach.setOpposing(opposingTeam);
  }
  
  /* return one of 1,2,3,4,5 to pick initial ball holder */
  public int pickAttack(int yourScore, int opponentScore, Game.Round previousRound) {
    return currentCoach.pickAttack(yourScore, opponentScore, previousRound);
  }
  
  /* if 0 then shoot, otherwise pass to 1,2,3,4,5 */
  public int action(int[] defenders) {
    return currentCoach.action(defenders);
  }
  
  /* pick defenders, use 1,2,3,4,5 for players */
  public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Game.Round previousRound) {
    return currentCoach.pickDefend(yourScore, opponentScore, ballHolder, previousRound);
  }
  
}
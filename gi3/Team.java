package hoop.gi3;

import hoop.sim.*;
import java.util.*;

public class Team implements hoop.sim.Team {
  
  private final int MAX_TEAMS = 8;
  
  private HashMap<String, Integer> teamIDs;
  private Player[] myRoster;
  private ArrayList<Player[]> rosters;
  private int teamSize;
  private int id = 0;
  private int teamsSeen = 0;
  private int lineupSize = 5;
  private int players;
  
  private Player[] myTeam;
  private int opponentID;
  private Player[] opposingTeam;
  private Player initHolder;
  private Player currentHolder;
  private Coach currentCoach;
  private Coach internalCoach;
  private Coach externalCoach;
  
  private boolean firstGame = true;
  private int gamesSeen;
  private int numTrainingGames;
  private int gamesPlayed;
  
  private boolean realGame = false;
  private boolean attacking;
  
  private HashMap<String, Integer> counts;
  
  private Fraction[][] shotHistory;
  
  public String name() {
    return "gi3";
  }
  
  /**
   * Initializes initial class variables
   * @param numPlayers
   */
  private void init(int numPlayers) {
    gamesPlayed = 0;
    teamSize = numPlayers;
    teamIDs = new HashMap<String, Integer>();
    teamIDs.put(name(), id);
    teamsSeen++;
    
    myRoster = new Player[teamSize];
    for (int i = 0; i < teamSize; i++)  
      myRoster[i] = new Player(id, i+1, teamSize);
    
    rosters = new ArrayList<Player[]>();  
    rosters.add(myRoster);
    myTeam = new Player[lineupSize];
    opposingTeam = new Player[lineupSize];
    internalCoach = new InternalCoach(teamSize);
    externalCoach = new ExternalCoach();   
    players = MAX_TEAMS * numPlayers;
    shotHistory = new Fraction[players][players];
    for (int i = 0; i < players; i++) {
      for (int j = 0; j < players; j++) {
        shotHistory[i][j] = new Fraction(0, 0);
      }
    }
    
    firstGame = false;
  }
  
  private int initTeam(String teamName) {
    if (!teamIDs.containsKey(teamName)) {
      teamIDs.put(teamName, teamsSeen);
      Player[] roster = new Player[teamSize];
      for (int i = 0; i < teamSize; i++)  // Start counting at 1
        roster[i] = new Player(teamsSeen, i+1, teamSize);
      rosters.add(roster);
      teamsSeen++;
    }
    
    return teamIDs.get(teamName);
  }
 
  private void checkTeam(int teamId, int games) {
    System.out.println("\nTeam " + teamId + " Sanity Check");
    if (teamId == id)
      System.out.println("------- MY TEAM ----------");
    System.out.println("Approximate number of my possessions: " + (games*49.5));
    
    if (rosters.size() <= teamId)
      return;
    
    Player[] roster = rosters.get(teamId);

    int passOps = 0;
    int stealOps = 0;
    int shotOps = 0;
    int blockOps = 0;
    for (int i = 0; i < myRoster.length; i++) {
      passOps += roster[i].passesAttempted;
      stealOps += roster[i].stealsAttempted;
      shotOps += roster[i].basketsAttempted;
      blockOps += roster[i].blocksAttempted;
    }
    System.out.println("Passes attempted: " + passOps);
    System.out.println("Steals attempted: " + stealOps);
    System.out.println("Shots attempted: " + shotOps);
    System.out.println("Blocks attempted: " + blockOps);
    System.out.println();
  }
  
  private void sanityCheck() {
    for (int i = 0; i < MAX_TEAMS; i++) {
      if (i == id)
        checkTeam(id, gamesPlayed);
      else 
        checkTeam(i, gamesPlayed - numTrainingGames);
    }
  }
  
  private void processRnd(Player[] attackLineup, Player[] defendLineup, Game.Round r) {
    
    int[] attackers = r.holders();
    int[] defenders = r.defenders();    
    
    for (int j = 0; j < attackers.length-1; j++) {
      int index = attackers[j]-1;
      //System.out.println(index + " " + (defenders[index]-1));
      Player attacker = attackLineup[index];
      Player defender = defendLineup[defenders[index]-1];
      attacker.passMade();
      defender.stealMissed();
      //passHistory[attacker.globalId][defender.globalId].made();
    }
    
    int lastAttacker = attackers[attackers.length-1]-1;
    int lastDefender = defenders[lastAttacker]-1;
    Player attacker = attackLineup[lastAttacker];
    Player defender = defendLineup[lastDefender];
    
    switch (r.lastAction()) {
      case SCORED:
        attacker.shotMade();
        defender.blockMissed();
        shotHistory[attacker.globalId][defender.globalId].made();
        break;
      case MISSED:
        attacker.shotMissed();
        defender.blockMade();
        shotHistory[attacker.globalId][defender.globalId].missed();
        break;
      case STOLEN:
        attacker.passMissed();
        defender.stealMade();
        //passHistory[attacker.globalId][defender.globalId].missed();
        break;
    }
  }
  
  private void watchGame(Game g) {
    
    //System.out.println("GAME HAS " + g.rounds() + " ROUNDS");
    
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
      if (g.round(i).attacksA) 
        processRnd(lineupA, lineupB, g.round(i));
      else
        processRnd(lineupB, lineupA, g.round(i));
    }
    
    //System.out.println(g.teamA + ": " + g.scoreA + ", " + g.teamB + ": " + g.scoreB);
    gamesSeen++;
  }
  
  
  
  private void printCounts() {
    for (int i = 0; i < myRoster.length; i++) {
      Player a = myRoster[i];
      //System.out.println(myRoster[i].shooting());
           System.out.println("Player " + i + " Stats:");
           System.out.println("Shooting: " + myRoster[i].shooting() + " " + a.baskets + " " + a.basketsAttempted);
           System.out.println("Blocking: " + myRoster[i].blocking() + " " + a.blocks + " " + a.blocksAttempted);
           System.out.println("Passing: " + myRoster[i].passing() + " " + a.passes + " " + a.passesAttempted);
           System.out.println("Stealing: " + myRoster[i].stealing() + " " + a.steals + " " + a.stealsAttempted);
    }
  }
  
  private void watchLastRound(Game g) {
    if (true)
      return;
    int rounds = g.rounds();
    Game.Round lastRound = g.round(rounds-1);
    if (attacking) 
      processRnd(myTeam, opposingTeam, lastRound);
    else
      processRnd(opposingTeam, myTeam, lastRound);
  }
  
  /* return 5 players in 1,2,3,4,...,P where P total players */
  public int[] pickTeam(String opponent, int totalPlayers, Game[] history) {
    if (firstGame) 
      init(totalPlayers);
    
    //sanityCheck();
    gamesPlayed++;
    
    opponentID = initTeam(opponent);
    
    if (opponentID == id) {
      currentCoach = internalCoach;
    }
    else {
      currentCoach = externalCoach;
      if (gamesSeen == 0) {
        for (int i = 0; i < history.length; i++)
          watchGame(history[i]);
        numTrainingGames = history.length;
        realGame = !realGame;
      }
      else {
        for (int i = gamesSeen-numTrainingGames; i < history.length-numTrainingGames; i++) {
          Game g = history[i];
          // Don't "watch" past regular season games involving us
          // We keep track of these stats as the game happens
          if (initTeam(g.teamA) == id || initTeam(g.teamB) == id) 
            watchLastRound(g);
          else
            watchGame(g);
        }
      }
    } 
    
    myTeam = currentCoach.pickTeam(myRoster, teamSize, lineupSize, opponent);
    int[] lineup = new int[5];
    for (int i = 0; i < 5; i++)
      lineup[i] = myTeam[i].id;
    
    return lineup;
    
  }
  
  /* get players of opponent team */
  public void opponentTeam(int[] opponentPlayers) {
    
    // If we got rid of this, we could keep track of internal games as they happen
    if (currentCoach == internalCoach)
      return;
    
    Player[] opposingRoster = rosters.get(opponentID);
    
    for (int i = 0; i < lineupSize; i++) 
      opposingTeam[i] = opposingRoster[opponentPlayers[i]-1];
    
    currentCoach.setOpposing(opposingTeam);
  }
  
  /* return one of 1,2,3,4,5 to pick initial ball holder */
  public int pickAttack(int yourScore, int opponentScore, Game.Round previousRound) {
    attacking = true;
    //before each round process the round before it   
   if (previousRound != null && realGame)
      processRnd(opposingTeam, myTeam, previousRound);  
    return currentCoach.pickAttack(yourScore, opponentScore, previousRound);
  }
  
  /* if 0 then shoot, otherwise pass to 1,2,3,4,5 */
  public int action(int[] defenders) {
    return currentCoach.action(defenders);
  }
  
  /* pick defenders, use 1,2,3,4,5 for players */
  public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Game.Round previousRound) {
    attacking = false;
    if (previousRound != null && realGame)
      processRnd(myTeam, opposingTeam, previousRound);
    return currentCoach.pickDefend(yourScore, opponentScore, ballHolder, previousRound);
  }
  
}
package hoop.g3;

import java.util.Arrays;

import hoop.sim.Game;

public class ExternalCoach implements Coach {
  
  private int lineupSize;
  private Player[] myTeam;
  private Player[] otherTeam;
  private int turnNumber;
  
  private Player initHolder;
  private Player currentHolder;
  
  private int turn;
  private boolean hasPassed;
  
  public ExternalCoach() {
    myTeam = new Player[5];
    otherTeam = new Player[5];
    turn = 0;
    hasPassed = false;
  }
  
  @Override
  public Player[] pickTeam(Player[] myRoster, int teamSize, int lineupSize) {
    this.lineupSize = lineupSize;
    Player[] sorted = Arrays.copyOf(myRoster, teamSize);
    Arrays.sort(sorted);
    //This is sorted from worst to best
    for (int i = 0; i < lineupSize; i++)
      myTeam[i] = myRoster[sorted[teamSize -i -1].id-1];
    
    return myTeam;
  }
  
  public void setOpposing(Player[] otherTeam) {
    this.otherTeam = otherTeam;
  }
  
  public void newGame() {
  }
  
  @Override
  public int pickAttack(int yourScore, int opponentScore, Game.Round previousRound) {
	if(!hasPassed){
		hasPassed = true;
		return getBestPasser(yourScore, opponentScore, previousRound);
	}
	else{		
		//If we get in here my understanding of the sim is flawed.
		System.out.println("This is a test.");
		return 1;
	}
  }
  
  
  public int getBestPasser(int yourScore, int opponentScore, 
		  				   Game.Round previousRound){
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
  
  
  public int getBestDefender(int yourScore, int opponentScore, 
			   Game.Round previousRound){
		double maxDefending = Double.NEGATIVE_INFINITY;
		int bestDefender = -1;
		for (int i = 0; i < 5; i++) {
		if (myTeam[i].blocking() > maxDefending) {
			maxDefending = myTeam[i].blocking();
			bestDefender = i;
		}
		}
		initHolder = myTeam[bestDefender];
		currentHolder = initHolder;
		return (bestDefender + 1);
}
  
  @Override
  public int action(int[] defenders) {
    // Later update to check for pass back
    if (currentHolder != initHolder)
      return 0;
    
    Player[] defense = new Player[5];
    for (int i = 0; i < 5; i++) 
      defense[i] = otherTeam[defenders[i]-1];
    
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
  
  @Override
  public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Game.Round previousRound) {
	hasPassed = false;
	Player[] defense = new Player[5];
    for (int i = 0; i < 5; i++) 
      defense[i] = otherTeam[i];
	
    return new int[] {1, 2, 3, 4, 5};
  }
}

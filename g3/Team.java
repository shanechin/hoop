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

	private int gamesPlayed = 0;
	private int gamesSeen = 0;
	
	private int opponentID;
	private Player[] opposingTeam;

	private Coach currentCoach;
	private Coach internalCoach;
	private Coach externalCoach;

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
		
		opposingTeam = new Player[lineupSize];
		internalCoach = new InternalCoach();
		externalCoach = new ExternalCoach();   
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

	public void watchGame(Game g) {
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
					rosterA[attackers[j]-1].passMade();
				//	rosterB[defenders[j]-1].stealMissed();
				}
				else {
					rosterB[attackers[j]-1].passMade();
				//	rosterA[defenders[j]-1].stealMissed();
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

		for (int i = gamesSeen; i < history.length; i++)
			watchGame(history[i]);

		opponentID = initTeam(opponent);

		if (opponentID == id) {
			currentCoach = internalCoach;
			currentCoach.gamesPlayed = gamesPlayed;
		}
		else 
			currentCoach = externalCoach;

		gamesPlayed++;  // Maybe I should wait until game ends to increment this?

		return currentCoach.pickTeam(myRoster, history);
	}

	/* get players of opponent team */
	public void opponentTeam(int[] opponentPlayers) {
		Player[] opposingRoster = rosters.get(opponentID);

		for (int i = 0; i < 5; i++) 
			opposingTeam[i] = opposingRoster[opponentPlayers[i]-1];
	}

	/* return one of 1,2,3,4,5 to pick initial ball holder */
	public int pickAttack(int yourScore, int opponentScore, Game.Round previousRound) {
		return currentCoach.pickAttack(yourScore, opponentScore);
	}

	/* if 0 then shoot, otherwise pass to 1,2,3,4,5 */
	public int action(int[] defenders) {
		return currentCoach.action(defenders, opposingTeam);
	}

	/* pick defenders, use 1,2,3,4,5 for players */
	public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Game.Round previousRound) {
		return currentCoach.pickDefend(yourScore, opponentScore, ballHolder);
	}

}
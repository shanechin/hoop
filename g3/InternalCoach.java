package hoop.g3;

import hoop.sim.Game;

public class InternalCoach extends Coach { 

	public InternalCoach() {
	}

	@Override
	public int[] pickTeam(Player[] myRoster, Game[] history) {
		teamSize = myRoster.length;
		int startIndex;		
		if (gamesPlayed == 0) 
			startIndex =  0;
		else startIndex = myTeam[lineupSize-1].id % teamSize;

		for (int i = 0; i < lineupSize; i++)
			myTeam[i] = myRoster[(startIndex + i) % teamSize];

		int[] lineup = new int[5];
		for (int i = 0; i < 5; i++)
			lineup[i] = myTeam[i].id;

		return lineup;
	}

	@Override
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

	@Override
	public int action(int[] defenders, Player[] opposingTeam) {
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

	@Override
	public int[] pickDefend(int yourScore, int opponentScore, int ballHolder) {
		return (new int[] {1, 2, 3, 4, 5});
	}
}

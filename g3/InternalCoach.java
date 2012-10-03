package hoop.g3;

import hoop.sim.Game;

public class InternalCoach extends Coach {

	public InternalCoach() {

	}

	@Override
	public Player[] pickTeam(Player[] myRoster, Game[] history, int teamSize) {
		this.teamSize = teamSize; 
		int startIndex;		

		startIndex = myTeam[lineupSize-1].id % teamSize;

		for (int i = 0; i < lineupSize; i++)
			myTeam[i] = myRoster[(startIndex + i) % teamSize];

		return myTeam;
	}

	@Override
	public int pickAttack(int yourScore, int opponentScore) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int action(int[] defenders) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] pickDefend(int yourScore, int opponentScore, int ballHolder) {
		// TODO Auto-generated method stub
		return null;
	}
}

package hoop.g3;

import java.util.Arrays;

import hoop.sim.Game;

public class ExternalCoach extends Coach {

	public ExternalCoach() {

	}

	@Override
	public Player[] pickTeam(Player[] myRoster, Game[] history, int teamSize) {
		this.teamSize = teamSize;
		Player[] sorted = Arrays.copyOf(myRoster, teamSize);
		Arrays.sort(sorted);
		for (int i = 0; i < lineupSize; i++)
			myTeam[i] = myRoster[sorted[i].id];

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

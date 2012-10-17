package hoop.g3;

import hoop.sim.Game;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class InternalCoach implements Coach {

	private int lineupSize;
	private Player[] team1;
	private Player[] team2;
	private boolean coaching1 = true;
	private int turnNumber;  
	private int holder;
	private HashMap<Integer, int[]> allPerms;
	private int[] currRot;
	private int curr = 1; 
	private int startIndex; 

	public InternalCoach(int teamSize) {
		allPerms = new HashMap<Integer, int[]>();
		allPermutations(teamSize);		
		currRot = allPerms.get(0);
	}

	@Override
	public Player[] pickTeam(Player[] myRoster, int teamSize, int lineupSize) {
		this.lineupSize = lineupSize;
		if (coaching1) {
			coaching1 = !coaching1;
			if (team1 == null) {
				team1 = new Player[lineupSize];
				team2 = new Player[lineupSize];
				startIndex = 0;
			}
			else {
				startIndex = (startIndex + lineupSize) % teamSize;
				if(startIndex == 0) {
					currRot = allPerms.get(curr);
					curr++;
					curr = curr % allPerms.size();
				}
			}

			for (int i = 0; i < lineupSize; i++)
				team1[i] = myRoster[currRot[(startIndex + i) % teamSize]];			

			return team1;
		}
		else {
			coaching1 = !coaching1;
			startIndex = (startIndex + lineupSize) % teamSize;
			for (int i = 0; i < lineupSize; i++)
				team2[i] = myRoster[currRot[(startIndex + i) % teamSize]];
			return team2;
		}
	}

	public void newGame() {
		turnNumber = 0;
	}

	@Override
	public int pickAttack(int yourScore, int opponentScore, Game.Round previousRound) {
		turnNumber++;
		holder = (turnNumber % lineupSize) + 1;
		return holder;
	}

	@Override
	public int action(int[] defenders) {
		if (holder == (turnNumber % lineupSize) + 1) {
			holder = ((turnNumber + 1) % lineupSize) + 1;
			return holder;
		}
		else
			return 0;
	}

	@Override
	public int[] pickDefend(int yourScore, int opponentScore, int ballHolder, Game.Round previousRound) {
		Integer [] myDefenders = new Integer[] {1, 2, 3, 4, 5};
		List<Integer> myTeam = Arrays.asList(myDefenders);
		Collections.shuffle(myTeam);
		return toIntArray(myTeam);
	}

	public int[] toIntArray(List<Integer> list)  {
		int[] ret = new int[list.size()];
		int i = 0;
		for (Integer e : list)  
			ret[i++] = e;
		return ret;
	}

	private void allPermutations(int teamSize) {
		int[] perms = new int[teamSize];
		int count = teamSize;
		int stepSize = 1; 

		while(count > 1) {
			int start = 0;			
			for(int j = 0; j < teamSize; j++) {
				if(j == 0) perms[j] = j;
				else {
					if (((j * stepSize + start) % teamSize) == start)  start++;
					perms[j] = (j * stepSize + start) % teamSize;
				}
			}

			allPerms.put((teamSize - count), perms.clone());
			count--;
			stepSize++;
		}
	}

	@Override
	public void setOpposing(Player[] otherTeam) {
	}
}
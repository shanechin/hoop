package hoop.g6;

import java.util.Comparator;

public class ShotsTakenComparator implements Comparator<Player> {
	public int compare(Player p1, Player p2) {
		if(p1.getShotsTaken() < p2.getShotsTaken())
			return -1;
		if(p1.getShotsTaken() > p2.getShotsTaken())
			return 1;
		return 0;
	}
}
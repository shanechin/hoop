package hoop.g6;

import java.util.Comparator;

public class ShotsTakenOnComparator implements Comparator<Player> {
	public int compare(Player p1, Player p2) {
		if(p1.getShotsTakenOn() < p2.getShotsTakenOn())
			return -1;
		if(p1.getShotsTakenOn() > p2.getShotsTakenOn())
			return 1;
		return 0;
	}
}
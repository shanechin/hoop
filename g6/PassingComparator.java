package hoop.g6;


import java.util.Comparator;

public class PassingComparator implements Comparator<Player> {
	public int compare(Player p1, Player p2)
	{
			return p1.getPassRate() < p2.getPassRate() ? 1 : (p1.getPassRate() == p2.getPassRate()) ? 0 : -1;
	}

}
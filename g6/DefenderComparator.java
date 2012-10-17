package hoop.g6;


import java.util.Comparator;

public class DefenderComparator implements Comparator<Player> {
	public int compare(Player p1, Player p2)
	{
			return p1.getDefendRate() < p2.getDefendRate() ? -1 : (p1.getDefendRate() == p2.getDefendRate()) ? 0 : 1;
	}

}
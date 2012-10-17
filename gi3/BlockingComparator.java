package hoop.gi3;

import java.util.Comparator; 

public class BlockingComparator implements Comparator<Player> {
  public int compare(Player p1, Player p2) {
    if (p1.blocking() > p2.blocking())
      return 1;
    else if (p1.blocking() < p2.blocking())
      return -1;
    else
      return 0;
  }
}
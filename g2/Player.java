package hoop.g2;

import java.util.Comparator;

public class Player {
	
	protected static final int REVERSE_SIGN = -1;
	private int id;
	private int shotsAttempt = 2;
	private int shotsMade = 1;
	private int passesAttempt = 2;
	private int passesMade = 1;
	private int blocksAttempt = 2;
	private int blocksMade = 1;
	private int stealsAttempt = 2;
	private int stealsMade = 1;
	
	public Player(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public int getShotsAttempt() {
		return shotsAttempt;
	}

	public int getShotsMade() {
		return shotsMade;
	}

	public int getPassesAttempt() {
		return passesAttempt;
	}

	public int getPassesMade() {
		return passesMade;
	}
	
	public int getBlocksAttempt() {
		return blocksAttempt;
	}
	
	public int getBlocksMade() {
		return blocksMade;
	}
	
	public int getStealsMade() {
		return stealsMade;
	}

	public int getStealsAttempt() {
		return stealsAttempt;
	}
	
	public void iterateShotsAttempt() {
		this.shotsAttempt++;
	}

	public void iterateShotsMade() {
		this.shotsMade++;
	}

	public void iteratePassesAttempt() {
		this.passesAttempt++;
	}

	public void iteratePassesMade() {
		this.passesMade++;
	}

	public void iterateBlocksAttempt() {
		this.blocksAttempt++;
	}

	public void iterateBlocksMade() {
		this.blocksMade++;
	}
	
	public void iterateStealsMade() {
		this.stealsMade++;
	}

	public void iterateStealsAttempt() {
		this.stealsAttempt++;
	}

	public double getShotPercent(){
		return (this.shotsMade/ (double) this.shotsAttempt);
	}
	
	public double getPassPercent(){
		return this.passesMade/ (double) this.passesAttempt;
	}

	public double getBlockPercent(){
		return (this.blocksMade/ (double) this.blocksAttempt);
	}
	
	public double getStealPercent(){
		return this.stealsMade/ (double) this.stealsAttempt;
	}
	
	public static Comparator<Player> PassesMadeComparator 
	= new Comparator<Player>() {
		public int compare(Player player1, Player player2) {
			return REVERSE_SIGN * Double.compare(player1.getPassPercent(), player2.getPassPercent());	
		}
	};
	
	public static Comparator<Player> ShotsMadeComparator 
	= new Comparator<Player>() {
		public int compare(Player player1, Player player2) {
			return REVERSE_SIGN * Double.compare(player1.getShotPercent(), player2.getShotPercent());	
		}
	};

	public static Comparator<Player> BlocksMadeComparator 
	= new Comparator<Player>() {
		public int compare(Player player1, Player player2) {
			return REVERSE_SIGN * Double.compare(player1.getBlockPercent(), player2.getBlockPercent());	
		}
	};
	
	public static Comparator<Player> StealsMadeComparator 
	= new Comparator<Player>() {
		public int compare(Player player1, Player player2) {
			return REVERSE_SIGN * Double.compare(player1.getStealPercent(), player2.getStealPercent());	
		}
	};
	
	//looks for best "sum" of shooting and shot-blocking
	public static Comparator<Player> AggregateComparator 
	= new Comparator<Player>() {
		public int compare(Player player1, Player player2) {
			double p1stats = player1.getShotPercent()+player1.getBlockPercent();
			double p2stats = player2.getShotPercent()+player2.getBlockPercent();
			return REVERSE_SIGN * Double.compare(p1stats, p2stats);	
		}
	};

	@Override
	public String toString() {
		return "Player [id=" + id + ", shooting % = " + getShotPercent() + ", blocking % = " +
				getBlockPercent() + ", shotsAttempt=" + shotsAttempt
				+ ", shotsMade=" + shotsMade + ", passesAttempt="
				+ passesAttempt + ", passesMade=" + passesMade
				+ ", blocksAttempt=" + blocksAttempt + ", blocksMade="
				+ blocksMade + ", stealsAttempt=" + stealsAttempt
				+ ", stealsMade=" + stealsMade + "]";
	}
	
	// TODO: Better aggregate comparators
}
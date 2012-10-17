package hoop.g6;

public class Player {

	private int id;
	private double passRate;
	private double attackRate;
	private double passBlockRate;
	private double defendRate;
	private int shotsTaken;
	private int shotsTakenOn;
	private boolean inTeam;
	private double attackRateD1;
	
	public Player(int id) {//, double passRate, double attackRate, double passBlockRate, double defendRate) {
		if(id <= 0)
			throw new RuntimeException();
		this.id = id;
		inTeam = false;
		// TODO Auto-generated constructor stub
	}
	
	public void setAttackRate(double attackRate) {
		this.attackRate = attackRate;
	}
	
	public void setAttackRateD1(double attackRateD1) {
		this.attackRateD1 = attackRateD1;
	}
	
	public void setInTeam(boolean inTeam) {
		this.inTeam = inTeam;
	}
	
	public boolean isInTeam() {
		return inTeam;
	}
	
	public void setDefendRate(double defendRate) {
		this.defendRate = defendRate;
	}
	
	public void setPassBlockRate(double passBlockRate) {
		this.passBlockRate = passBlockRate;
	}
	
	public void setPassRate(double passRate) {
		this.passRate = passRate;
	}
	
	public double getAttackRate() {
		return attackRate;
	}
	public double getAttackRateD1() {
		return attackRateD1;
	}
	
	public double getDefendRate() {
		return defendRate;
	}
	
	public int getId() {
		return id;
	}
	
	public double getPassBlockRate() {
		return passBlockRate;
	}
	
	public double getPassRate() {
		return passRate;
	}
	
	public void setShotsTaken(int shotsTaken) {
		this.shotsTaken = shotsTaken;
	}
	
	public int getShotsTaken() {
		return shotsTaken;
	}
	public int getShotsTakenOn() {
		return shotsTakenOn;
	}
	
	public String toString() {
		return "[Player" + id + ": def + " + defendRate + "| took " + shotsTaken + " shots, rec'd " + shotsTakenOn + ".]";
	}

	public void setShotsTakenOn(int countShotsTakenOn) {
		this.shotsTakenOn = countShotsTakenOn;
	}
}

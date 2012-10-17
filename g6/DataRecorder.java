package hoop.g6;

public class DataRecorder {
	private int[] passAttempts;
	private int[] passSuccesses;
	private int[] shotAttempts;
	private int[] shotAttemptsOn;
	private int[] shotSuccesses;
	private int[] shotAttemptsVDef1;
	private int[] shotSuccessesVDef1;
	
	public DataRecorder(int numPlayers) {
		passAttempts = new int[numPlayers + 1];
		passSuccesses = new int[numPlayers + 1];
		shotAttempts = new int[numPlayers + 1];
		shotAttemptsOn = new int[numPlayers + 1];
		shotSuccesses = new int[numPlayers + 1];
		shotAttemptsVDef1 = new int[numPlayers + 1];
		shotSuccessesVDef1 = new int[numPlayers + 1];
	}
	
	public void recordPass(int playerId, boolean success) {
		passAttempts[playerId]++;
		if(success)
			passSuccesses[playerId]++;
	}
	
	public void recordShot(int playerId, int defenderId, boolean success) {
		shotAttempts[playerId]++;
		if(success)
			shotSuccesses[playerId]++;
		shotAttemptsOn[defenderId]++;
	}
	
	public double getPassSuccessRatio(int playerId) {
		return passSuccesses[playerId] / (double) passAttempts[playerId];
	}
	
	public boolean hasPassInfo(int playerId) {
		return passAttempts[playerId] > 0;
	}
	
	public double getShotSuccessRatio(int playerId) {
		return shotSuccesses[playerId] / (double) shotAttempts[playerId];
	}
	
	public double getShotSuccessRatioAgainstD1(int playerId) {
		return shotSuccessesVDef1[playerId] / (double) shotAttemptsVDef1[playerId];
	}
	
	public boolean hasShotInfo(int playerId) {
		return shotAttempts[playerId] > 0;
	}
	
	public int countShotsTaken(int playerId) {
		return shotAttempts[playerId];
	}
	
	public int countShotsTakenOn(int playerId) {
		return shotAttemptsOn[playerId];
	}
	
	public void recordShotAgainstD1(int playerId, boolean success) {
		shotAttemptsVDef1[playerId]++;
		if(success)
			shotSuccessesVDef1[playerId]++;
	}
}

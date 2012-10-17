package hoop.g4;


public class Stats
{	
	public enum StatType {PASSES_S, PASSES_F, SHOOTS_S, SHOOTS_F, PASS_BLOCKS_S, PASS_BLOCKS_F, SHOOT_BLOCKS_S, SHOOT_BLOCKS_F};
	public enum WeightType {PASSES_W, SHOOTS_W, PASS_BLOCKS_W, SHOOT_BLOCKS_W};
	private static String name;

	// *_s is for successes, *_f is for failures, *_w is for weights.
	private int[] passes_s;
	private int[] passes_f;
	private float[] passes_w;
	private int[] shoots_s;
	private int[] shoots_f;
	private float[] shoots_w;
	private int[] pass_blocks_s;
	private int[] pass_blocks_f;
	private float[] pass_blocks_w;
	private int[] shoot_blocks_s;
	private int[] shoot_blocks_f;
	private float[] shoot_blocks_w;

	// Stats per player of a team.
	public Stats(String name, int players)
	{
		this.name = name;
		passes_s = new int[players+1];
		passes_f = new int[players+1];
		passes_w = new float[players+1];
		shoots_s = new int[players+1];
		shoots_f = new int[players+1];
		shoots_w = new float[players+1];
		pass_blocks_s = new int[players+1];
		pass_blocks_f = new int[players+1];
		pass_blocks_w = new float[players+1];
		shoot_blocks_s = new int[players+1];
		shoot_blocks_f = new int[players+1];
		shoot_blocks_w = new float[players+1];
	}


	// Get value of particular type of stats of a player of a team.
	public int getStats(StatType st, int player)
	{
		switch (st) {
			case PASSES_S:
				return passes_s[player];
			case PASSES_F:
				return passes_f[player];
			case SHOOTS_S:
				return shoots_s[player];
			case SHOOTS_F:
				return shoots_f[player];
			case PASS_BLOCKS_S:
				return pass_blocks_s[player];
			case PASS_BLOCKS_F:
				return pass_blocks_f[player];
			case SHOOT_BLOCKS_S:
				return shoot_blocks_s[player];
			case SHOOT_BLOCKS_F:
				return shoot_blocks_f[player];
			default:
				return -1;
		}
	}


	// Get weight of particular type of a player of a team.
	public float getWeights(WeightType wt, int player)
	{
		switch (wt) {
			case PASSES_W:
				return passes_w[player];
			case SHOOTS_W:
				return shoots_w[player];
			case PASS_BLOCKS_W:
				return pass_blocks_w[player];
			case SHOOT_BLOCKS_W:
				return shoot_blocks_w[player];
			default:
				return -1;
		}
	}

	// Update weights related to player abilities.
	// Passes and Shoots depend on successes, higher the better.
	// Pass blocks and shoot blocks depend on failures, lower the better.
	public void updateWeights(WeightType wt, int player)
	{
		switch (wt) {
			case PASSES_W:
				passes_w[player] = (float) passes_s[player] / (passes_s[player] + passes_f[player]);
				break;
			case SHOOTS_W:
				shoots_w[player] = (float) shoots_s[player] / (shoots_s[player] + shoots_f[player]);
				break;
			case PASS_BLOCKS_W:
				pass_blocks_w[player] = (float) pass_blocks_f[player] / (pass_blocks_s[player] + pass_blocks_f[player]);
				break;
			case SHOOT_BLOCKS_W:
				shoot_blocks_w[player] = (float) shoot_blocks_f[player] / (shoot_blocks_s[player] + shoot_blocks_f[player]);
				break;
		}
	}

	// Increase value of particular type of stats of a player of a team.
	public void incStats(StatType st, int player)
	{
		switch (st) {
			case PASSES_S:
				passes_s[player] ++;
				updateWeights(WeightType.PASSES_W, player);
				break;
			case PASSES_F:
				passes_f[player] ++;
				updateWeights(WeightType.PASSES_W, player);
				break;
			case SHOOTS_S:
				shoots_s[player] ++;
				updateWeights(WeightType.SHOOTS_W, player);
				break;
			case SHOOTS_F:
				shoots_f[player] ++;
				updateWeights(WeightType.SHOOTS_W, player);
				break;
			case PASS_BLOCKS_S:
				pass_blocks_s[player] ++;
				updateWeights(WeightType.PASS_BLOCKS_W, player);
				break;
			case PASS_BLOCKS_F:
				pass_blocks_f[player] ++;
				updateWeights(WeightType.PASS_BLOCKS_W, player);
				break;
			case SHOOT_BLOCKS_S:
				shoot_blocks_s[player] ++;
				updateWeights(WeightType.SHOOT_BLOCKS_W, player);
				break;
			case SHOOT_BLOCKS_F:
				shoot_blocks_f[player] ++;
				updateWeights(WeightType.SHOOT_BLOCKS_W, player);
				break;
		}
	}


	// Print all stats and weights of all players of a team
	public void printStats()
	{/*
		System.out.println("STATS AND WEIGHTS OF TEAM " + name);
		System.out.println("Player PASSES_S PASSES_F PASSES_W SHOOTS_S SHOOTS_F SHOOTS_W PASS_BLOCKS_S PASS_BLOCKS_F PASS_BLOCKS_W SHOOT_BLOCKS_S SHOOT_BLOCKS_F SHOOT_BLOCKS_W");
		for (int i = 1; i < passes_s.length; i++) {
			System.out.printf("%6d %8d %8d %8f %8d %8d %8f", i, passes_s[i], passes_f[i], passes_w[i], shoots_s[i], shoots_f[i], shoots_w[i]);
			System.out.printf(" %13d %13d %13f %14d %14d %14f\n", pass_blocks_s[i], pass_blocks_f[i], pass_blocks_w[i], shoot_blocks_s[i], shoot_blocks_f[i], shoot_blocks_w[i]);
		}*/
	}



}

package anja.GridEditor;

public interface SearchStrategy {
	
	/**
	 * Given by the GridEditor as reason, if movement fails
	 * because another robot occupies the cell in the given direction
	 * and does not leave it.
	 */
	static final int CELL_OCCUPIED = 1;
	
	/**
	 * Given by the GridEditor as reason, if movement fails
	 * because another robot occupies the cell in the given direction
	 * and does not leave it.
	 */
	static final int ROBOT_MOVES_TO_SAME_CELL = 2;
	
	/**
	 * Given by the GridEditor as reason, if movement fails
	 * because another robot occupies the cell in the given direction
	 * and does not leave it.
	 */
	static final int DIR_BLOCKED_BY_WALL = 3;	
	
	/**
	 * The start method is invoked before the actual execution of the
	 * search strategies. Each strategy can use this method to do
	 * necessary initializations.
	 * @param robot The robot the strategy belongs to
	 * @param grid The grid in which the robot runs
	 */
	public void start(Robot robot, Grid grid);
	
	/**
	 * The strategy has to return the direction in which the robot should
	 * move next. This method is invoked for each step right before
	 * the robot is meant to move from one cell to another.
	 * @param robot The robot the strategy belongs to
	 * @param grid The grid in which the robot runs
	 * @return The direction in which the robot should move next.
	 */
	public int getDirection(Robot robot, Grid grid);
	
	/**
	 * Invoked by the GridEditor, if the robot successfully moved in the direction
	 * given by getDirection.
	 * @param robot The robot the strategy belongs to
	 * @param dir The direction in which the robot moved
	 */
	
	public void directionSuccessful(Robot robot, Directions dir, int cause);
	
	/**
	 * Invoked by the GridEditor, if the robot could not move in to the direction
	 * given by getDirection and has to stay on its cell.
	 * @param robot The robot the strategy belongs to
	 * @param dir The direction in which the robot could not move
	 */
	
	public void directionFailed(Robot robot, Directions dir);
	
	/**
	 * Invoked after execution of the strategies is finished (robot reached
	 * target or animation is aborted).
	 * Search strategies could use this do to some cleanup before the next
	 * start() is called.
	 */
	
	public void stop();
	
}

package anja.GridEditor;

public class Cell {
	
	public boolean[] walls;
	public boolean start = false;
	public boolean target = false;
	
	
	
	public Cell() {
		walls = new boolean[4];
	}

}

package svb.nxt.robot.logic;

public class ArrowClass {
	private boolean red = false;
	private boolean yellow = false;
	private boolean blue = false;

	public ArrowClass(boolean red, boolean yellow, boolean blue) {
		this.setRed(red);
		this.setYellow(yellow);
		this.setBlue(blue);
	}

	public boolean isRed() {
		return red;
	}

	public void setRed(boolean red) {
		this.red = red;
	}

	public boolean isYellow() {
		return yellow;
	}

	public void setYellow(boolean yellow) {
		this.yellow = yellow;
	}

	public boolean isBlue() {
		return blue;
	}

	public void setBlue(boolean blue) {
		this.blue = blue;
	}
}
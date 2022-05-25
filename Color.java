package main;

public class Color {
	private int r;
	private int g;
	private int b;
	
	public Color(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public Color(int rgb) {
		this.r = (rgb >> 16) & 0xFF;
		this.g = (rgb >> 8) & 0xFF;
		this.b = rgb & 0xFF;
	}
	
	public int getInt() {
		return this.r*65536 + this.g*256 + this.b;
	}
	
	@Override
	public String toString() { 
	    return "r = " +  this.r + ", g = " + this.g + ", b = " + this.b;
	}
	
	public Color addNoise(int noise) {
		//Min + (int)(Math.random() * ((Max - Min) + 1))
		int rr = this.r;
		int gg = this.g;
		int bb = this.b;
		
		if(Math.random() < 0.5) {
			rr += -noise + (int)(Math.random() * (2*noise + 1));
			if(rr > 255) {
				rr = 255;
			}else if (rr < 0) {
				rr = 0;
			}
		}
		if(Math.random() < 0.5) {
			gg += -noise + (int)(Math.random() * (2*noise + 1));
			if(gg > 255) {
				gg = 255;
			}else if (gg < 0) {
				gg = 0;
			}
		}
		if(Math.random() < 0.5) {
			bb += -noise + (int)(Math.random() * (2*noise + 1));
			if(bb > 255) {
				bb = 255;
			}else if (bb < 0) {
				bb = 0;
			}
		}
		return new Color(rr, gg, bb);
	}

	public int getR() {
		return r;
	}

	public void setR(int r) {
		this.r = r;
	}

	public int getG() {
		return g;
	}

	public void setG(int g) {
		this.g = g;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}
	
}
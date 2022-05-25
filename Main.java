package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class Main implements Runnable {
	private boolean running = false;
	private Thread thread;
	private enum Algo {
		NEAREST,
		NEARESTNOISE,
		LINEAR,
	}

	public static void main(String[] args) {
		new Main().start();
	}
	
	public synchronized void start() {
		if(this.running == true) {
			return;
		}
		this.running = true;
		this.thread = new Thread(this);
		this.thread.start();
	}
	
	@Override
	public void run() {
		System.out.println("Running...");
		BufferedImage input = this.loadInput();
		this.display(input);
		
		BufferedImage output = this.upSclae(input, 300, Algo.LINEAR);
		//
		File outputfile = new File("D:\\Downloads\\image.jpg");
		try {
			ImageIO.write(output, "jpg", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		this.display(output);
	}
	
	private BufferedImage loadInput() {
		BufferedImage img = null;
		//URL url = getClass().getResource("/pussy.jpg");
		URL url = getClass().getResource("/colors.png");
		try {
			img = ImageIO.read(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return img;
	}
	
	private void display(BufferedImage image) {
		JFrame frame = new JFrame("Display");
		frame.setSize(950, 950);
		frame.add(new JScrollPane(new JLabel(new ImageIcon(image))));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.requestFocus();
	}
	
	private BufferedImage upSclae(BufferedImage input, int factor, Algo algo) {
		Color[][] inputPixels = this.bufferedImageToPixels(input);
		Color[][] outputPixels;
		
		if(algo == Algo.NEAREST) {
			outputPixels = new Color[inputPixels.length*factor][inputPixels[0].length*factor];
			for(int i = 0; i < outputPixels.length; i++) {
				for(int j = 0; j < outputPixels[0].length; j++) {
					outputPixels[i][j] = inputPixels[Math.floorDiv(i, factor)][Math.floorDiv(j, factor)];
				}
			}
		} else if(algo == Algo.NEARESTNOISE) {
			outputPixels = new Color[inputPixels.length*factor][inputPixels[0].length*factor];
			for(int i = 0; i < outputPixels.length; i++) {
				for(int j = 0; j < outputPixels[0].length; j++) {
					outputPixels[i][j] = inputPixels[Math.floorDiv(i, factor)][Math.floorDiv(j, factor)].addNoise(5);
				}
			}
		} else if(algo == Algo.LINEAR) {
			outputPixels = new Color[inputPixels.length*factor][inputPixels[0].length*factor];
			for(int i = 0; i < outputPixels.length; i++) {
				for(int j = 0; j < outputPixels[0].length; j++) {
					//find nearest centered known
					int nearestKnownX = factor*Math.floorDiv(i, factor) + Math.floorDiv(factor, 2);
					int nearestKnownY = factor*Math.floorDiv(j, factor) + Math.floorDiv(factor, 2);
					Color nearestKnownColor = inputPixels[Math.floorDiv(i, factor)][Math.floorDiv(j, factor)];
					if(i == nearestKnownX && j == nearestKnownY) {
						outputPixels[i][j] = nearestKnownColor;
					} else if(i < nearestKnownX && j < nearestKnownY) {
						//tl
						if(Math.floorDiv(j, factor) - 1 < 0 || Math.floorDiv(i, factor) - 1 < 0) {
							//can't interpolate
							outputPixels[i][j] = nearestKnownColor;
						} else {
							//interpolate
							//nearest known is bottom right
							Color topLeftColor = inputPixels[Math.floorDiv(i, factor)-1][Math.floorDiv(j, factor)-1];
							Color topRightColor = inputPixels[Math.floorDiv(i, factor)][Math.floorDiv(j, factor)-1];
							Color bottomLeftColor = inputPixels[Math.floorDiv(i, factor)-1][Math.floorDiv(j, factor)];
							Color bottomRightColor = nearestKnownColor;
							
							double distanceTopLeft = this.distance(i, j, nearestKnownX-factor, nearestKnownY-factor);
							double distanceTopRight = this.distance(i, j, nearestKnownX, nearestKnownY-factor);
							double distanceBottomLeft = this.distance(i, j, nearestKnownX-factor, nearestKnownY);
							double distanceBottomRight = this.distance(i, j, nearestKnownX, nearestKnownY);
							double total = distanceTopLeft + distanceTopRight + distanceBottomLeft + distanceBottomRight;

							double kTopLeft = (distanceBottomRight)  / (total);
							double kTopRight = (distanceBottomLeft)  / (total);
							double kBottomLeft = (distanceTopRight)  / (total);
							double kBottomRight = (distanceTopLeft)  / (total);
							
							int red = (int) (kBottomRight*bottomRightColor.getR() + kTopLeft*topLeftColor.getR() + kTopRight*topRightColor.getR() + kBottomLeft*bottomLeftColor.getR());
							int green = (int) (kBottomRight*bottomRightColor.getG() + kTopLeft*topLeftColor.getG() + kTopRight*topRightColor.getG() + kBottomLeft*bottomLeftColor.getG());
							int blue = (int) (kBottomRight*bottomRightColor.getB() + kTopLeft*topLeftColor.getB() + kTopRight*topRightColor.getB() + kBottomLeft*bottomLeftColor.getB());
							
							outputPixels[i][j] = new Color(red, green, blue).addNoise(7);
						}
					} else if(i < nearestKnownX && j > nearestKnownY) {
						//bl
						if(Math.floorDiv(j, factor) + 1 > inputPixels[0].length-1 || Math.floorDiv(i, factor) - 1 < 0) {
							//can't interpolate
							outputPixels[i][j] = nearestKnownColor;
						} else {
							//interpolate
							//nearest known is top right
							Color topLeftColor = inputPixels[Math.floorDiv(i, factor)-1][Math.floorDiv(j, factor)];
							Color topRightColor = nearestKnownColor;
							Color bottomLeftColor = inputPixels[Math.floorDiv(i, factor)-1][Math.floorDiv(j, factor)+1];
							Color bottomRightColor = inputPixels[Math.floorDiv(i, factor)][Math.floorDiv(j, factor)+1];
							
							double distanceTopLeft = this.distance(i, j, nearestKnownX-factor, nearestKnownY);
							double distanceTopRight = this.distance(i, j, nearestKnownX, nearestKnownY);
							double distanceBottomLeft = this.distance(i, j, nearestKnownX-factor, nearestKnownY+factor);
							double distanceBottomRight = this.distance(i, j, nearestKnownX, nearestKnownY+factor);
							double total = distanceTopLeft + distanceTopRight + distanceBottomLeft + distanceBottomRight;
							
							double kTopLeft = (distanceBottomRight)  / (total);
							double kTopRight = (distanceBottomLeft)  / (total);
							double kBottomLeft = (distanceTopRight)  / (total);
							double kBottomRight = (distanceTopLeft)  / (total);
							
							int red = (int) (kBottomRight*bottomRightColor.getR() + kTopLeft*topLeftColor.getR() + kTopRight*topRightColor.getR() + kBottomLeft*bottomLeftColor.getR());
							int green = (int) (kBottomRight*bottomRightColor.getG() + kTopLeft*topLeftColor.getG() + kTopRight*topRightColor.getG() + kBottomLeft*bottomLeftColor.getG());
							int blue = (int) (kBottomRight*bottomRightColor.getB() + kTopLeft*topLeftColor.getB() + kTopRight*topRightColor.getB() + kBottomLeft*bottomLeftColor.getB());
							
							outputPixels[i][j] = new Color(red, green, blue).addNoise(7);
						}
					} else if(i > nearestKnownX && j < nearestKnownY) {
						//tr
						if(Math.floorDiv(j, factor) - 1 < 0 || Math.floorDiv(i, factor) + 1 > inputPixels.length-1) {
							//can't interpolate
							outputPixels[i][j] = nearestKnownColor;
						} else {
							//interpolate
							//nearest known is bottom left
							Color topLeftColor = inputPixels[Math.floorDiv(i, factor)][Math.floorDiv(j, factor)-1];
							Color topRightColor = inputPixels[Math.floorDiv(i, factor)+1][Math.floorDiv(j, factor)-1];
							Color bottomLeftColor = nearestKnownColor;
							Color bottomRightColor = inputPixels[Math.floorDiv(i, factor)+1][Math.floorDiv(j, factor)];

							double distanceTopLeft = this.distance(i, j, nearestKnownX, nearestKnownY-factor);
							double distanceTopRight = this.distance(i, j, nearestKnownX+factor, nearestKnownY-factor);
							double distanceBottomLeft = this.distance(i, j, nearestKnownX, nearestKnownY);
							double distanceBottomRight =  this.distance(i, j, nearestKnownX+factor, nearestKnownY);
							double total = distanceTopLeft + distanceTopRight + distanceBottomLeft + distanceBottomRight;
							
							double kTopLeft = (distanceBottomRight)  / (total);
							double kTopRight = (distanceBottomLeft)  / (total);
							double kBottomLeft = (distanceTopRight)  / (total);
							double kBottomRight = (distanceTopLeft)  / (total);
							
							int red = (int) (kBottomRight*bottomRightColor.getR() + kTopLeft*topLeftColor.getR() + kTopRight*topRightColor.getR() + kBottomLeft*bottomLeftColor.getR());
							int green = (int) (kBottomRight*bottomRightColor.getG() + kTopLeft*topLeftColor.getG() + kTopRight*topRightColor.getG() + kBottomLeft*bottomLeftColor.getG());
							int blue = (int) (kBottomRight*bottomRightColor.getB() + kTopLeft*topLeftColor.getB() + kTopRight*topRightColor.getB() + kBottomLeft*bottomLeftColor.getB());
							
							outputPixels[i][j] = new Color(red, green, blue).addNoise(7);
						}
					} else if(i > nearestKnownX && j > nearestKnownY) {
						//br
						if(Math.floorDiv(j, factor) + 1 > inputPixels[0].length-1 || Math.floorDiv(i, factor) + 1 > inputPixels.length-1) {
							//can't interpolate
							outputPixels[i][j] = nearestKnownColor;
						} else {
							//interpolate
							//nearest known is top left
							Color topLeftColor = nearestKnownColor;
							Color topRightColor = inputPixels[Math.floorDiv(i, factor)+1][Math.floorDiv(j, factor)];
							Color bottomLeftColor = inputPixels[Math.floorDiv(i, factor)][Math.floorDiv(j, factor)+1];
							Color bottomRightColor = inputPixels[Math.floorDiv(i, factor)+1][Math.floorDiv(j, factor)+1];

							double distanceTopLeft = this.distance(i, j, nearestKnownX, nearestKnownY);
							double distanceTopRight = this.distance(i, j, nearestKnownX+factor, nearestKnownY);
							double distanceBottomLeft = this.distance(i, j, nearestKnownX, nearestKnownY+factor);
							double distanceBottomRight = this.distance(i, j, nearestKnownX+factor, nearestKnownY+factor);
							double total = distanceTopLeft + distanceTopRight + distanceBottomLeft + distanceBottomRight;
							
							double kTopLeft = (distanceBottomRight)  / (total);
							double kTopRight = (distanceBottomLeft)  / (total);
							double kBottomLeft = (distanceTopRight)  / (total);
							double kBottomRight = (distanceTopLeft)  / (total);
							
							int red = (int) (kBottomRight*bottomRightColor.getR() + kTopLeft*topLeftColor.getR() + kTopRight*topRightColor.getR() + kBottomLeft*bottomLeftColor.getR());
							int green = (int) (kBottomRight*bottomRightColor.getG() + kTopLeft*topLeftColor.getG() + kTopRight*topRightColor.getG() + kBottomLeft*bottomLeftColor.getG());
							int blue = (int) (kBottomRight*bottomRightColor.getB() + kTopLeft*topLeftColor.getB() + kTopRight*topRightColor.getB() + kBottomLeft*bottomLeftColor.getB());
							
							outputPixels[i][j] = new Color(red, green, blue).addNoise(7);
						}
					} else if(i == nearestKnownX && j > nearestKnownY) {
						//down
						if(Math.floorDiv(j, factor) + 1 > inputPixels[0].length-1) {
							//can't interpolate
							outputPixels[i][j] = nearestKnownColor;
						} else {
							//interpolate
							Color lerpColor = inputPixels[Math.floorDiv(i, factor)][Math.floorDiv(j, factor)+1];
							int lerpY = nearestKnownY + factor;
							
							int red = (int) (Math.abs(lerpY - j)/(1.0f*factor)*nearestKnownColor.getR() + Math.abs(nearestKnownY - j)/(1.0f*factor)*lerpColor.getR());
							int green = (int) (Math.abs(lerpY - j)/(1.0f*factor)*nearestKnownColor.getG() + Math.abs(nearestKnownY - j)/(1.0f*factor)*lerpColor.getG());
							int blue = (int) (Math.abs(lerpY - j)/(1.0f*factor)*nearestKnownColor.getB() + Math.abs(nearestKnownY - j)/(1.0f*factor)*lerpColor.getB());
							
							outputPixels[i][j] = new Color(red, green, blue);
						}
					} else if(i == nearestKnownX && j < nearestKnownY) {
						//up
						if(Math.floorDiv(j, factor) - 1 < 0) {
							//can't interpolate
							outputPixels[i][j] = nearestKnownColor;
						} else {
							//interpolate
							Color lerpColor = inputPixels[Math.floorDiv(i, factor)][Math.floorDiv(j, factor)-1];
							int lerpY = nearestKnownY - factor;
							
							int red = (int) (Math.abs(lerpY - j)/(1.0f*factor)*nearestKnownColor.getR() + Math.abs(nearestKnownY - j)/(1.0f*factor)*lerpColor.getR());
							int green = (int) (Math.abs(lerpY - j)/(1.0f*factor)*nearestKnownColor.getG() + Math.abs(nearestKnownY - j)/(1.0f*factor)*lerpColor.getG());
							int blue = (int) (Math.abs(lerpY - j)/(1.0f*factor)*nearestKnownColor.getB() + Math.abs(nearestKnownY - j)/(1.0f*factor)*lerpColor.getB());
							
							outputPixels[i][j] = new Color(red, green, blue);
						}
					} else if(i > nearestKnownX && j == nearestKnownY) {
						//right
						if(Math.floorDiv(i, factor) + 1 > inputPixels.length-1) {
							//can't interpolate
							outputPixels[i][j] = nearestKnownColor;
						} else {
							//interpolate
							Color lerpColor = inputPixels[Math.floorDiv(i, factor)+1][Math.floorDiv(j, factor)];
							int lerpX = nearestKnownX + factor;
							
							int red = (int) (Math.abs(lerpX - i)/(1.0f*factor)*nearestKnownColor.getR() + Math.abs(nearestKnownX - i)/(1.0f*factor)*lerpColor.getR());
							int green = (int) (Math.abs(lerpX - i)/(1.0f*factor)*nearestKnownColor.getG() + Math.abs(nearestKnownX - i)/(1.0f*factor)*lerpColor.getG());
							int blue = (int) (Math.abs(lerpX - i)/(1.0f*factor)*nearestKnownColor.getB() + Math.abs(nearestKnownX - i)/(1.0f*factor)*lerpColor.getB());
							
							outputPixels[i][j] = new Color(red, green, blue);
						}
					} else if(i < nearestKnownX && j == nearestKnownY) {
						//left
						if(Math.floorDiv(i, factor) - 1 < 0) {
							//can't interpolate
							outputPixels[i][j] = nearestKnownColor;
						} else {
							//interpolate
							Color lerpColor = inputPixels[Math.floorDiv(i, factor)-1][Math.floorDiv(j, factor)];
							int lerpX = nearestKnownX - factor;
							
							int red = (int) (Math.abs(lerpX - i)/(1.0f*factor)*nearestKnownColor.getR() + Math.abs(nearestKnownX - i)/(1.0f*factor)*lerpColor.getR());
							int green = (int) (Math.abs(lerpX - i)/(1.0f*factor)*nearestKnownColor.getG() + Math.abs(nearestKnownX - i)/(1.0f*factor)*lerpColor.getG());
							int blue = (int) (Math.abs(lerpX - i)/(1.0f*factor)*nearestKnownColor.getB() + Math.abs(nearestKnownX - i)/(1.0f*factor)*lerpColor.getB());
							
							outputPixels[i][j] = new Color(red, green, blue);
						}
					}
					
				}
			}
		} else {
			outputPixels = inputPixels;
		}
		return this.pixelsToBufferedImage(outputPixels);
	}
	
	private double distance(int i, int j, int x, int y) {
		return Math.hypot((x-i), (y-j));
	}
	
	private Color[][] bufferedImageToPixels(BufferedImage input) {
		Color[][] pixels = new Color[input.getWidth()][input.getHeight()];
		for(int i = 0; i < input.getWidth(); i++) {
			for(int j = 0; j < input.getHeight(); j++) {
				pixels[i][j] = new Color(input.getRGB(i, j));
			}
		}
		return pixels;
	}
	
	private BufferedImage pixelsToBufferedImage(Color[][] pixels) {
		int width = pixels.length;
		int height = pixels[0].length;
		BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				output.setRGB(i, j, pixels[i][j].getInt());
			}
		}
		return output;
	}
	
}
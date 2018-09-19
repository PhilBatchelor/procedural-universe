package rendercard;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class AuxilaryTexture {
	private int width=0;
	private int height=0;
	private int[] raster;

	public  AuxilaryTexture(File file) {
		try {
			BufferedImage image = ImageIO.read(file);
			height=image.getHeight();
			width=image.getWidth();
			raster=new int[width*height];

			for (int h=0; h<height; h++) {
				for (int w=0; w<width; w++) {
					int pixel = image.getRGB(w, h); 
					int index=(h*width)+w;
					raster[index]=pixel;
					/*
					red_raster[index] = (pixel >>> 16) & 0xff;
					green_raster[index] = (pixel >>> 8) & 0xff;
					blue_raster[index] = pixel & 0xff;
					 */
				}
			}
		}
		catch (Exception e) {
			System.out.println("Texture Load Failure");

		}
	}
	
	public int getRGB(int x, int y) {
		if (x>=width) x=x%width;
		if (y>=height) y=y%height;
		return raster[(y*width)+x];
	}

	public int[] getRaster() {
		return raster;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}


}

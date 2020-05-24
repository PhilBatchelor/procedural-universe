import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


public class Texture {

   public int height;
   public int width;
   public int[] bump_height;
   public int[] red_raster;
   public int[] green_raster;
   public int[] blue_raster;
 
   public Texture(int w, int h) {
		height=h;
		width=w;
		red_raster=new int[width*height];
		green_raster=new int[width*height];
		blue_raster=new int[width*height];
		bump_height=new int[width*height];
	}

   public Texture(File file) {
	try {
		BufferedImage image = ImageIO.read(file);
		height=image.getHeight();
		width=image.getWidth();
		red_raster=new int[width*height];
		green_raster=new int[width*height];
		blue_raster=new int[width*height];
		bump_height=new int[width*height];

		for (int h=0; h<height; h++) {
			for (int w=0; w<width; w++) {
				int pixel = image.getRGB(w, h); 
				int index=(h*width)+w;
				red_raster[index] = (pixel >>> 16) & 0xff;
				green_raster[index] = (pixel >>> 8) & 0xff;
				blue_raster[index] = pixel & 0xff;
			}
		}
	}
	catch (Exception e) {
		System.out.println("Texture Load Failure");
	}
   }
 
   public Texture(Texture source, int startx, int endx, int starty, int endy) {

	   if (endy>starty) {
		   width=endx-startx;
		   height=endy-starty;

		   red_raster=new int[width*height];
		   green_raster=new int[width*height];
		   blue_raster=new int[width*height];
		   bump_height=new int[width*height];

		   for (int h=0; h<height; h++) {
			   for (int w=0; w<width; w++) {
				   int index=(h*width)+w;
				   int source_index=((h+starty)*source.width)+startx+w;
				   red_raster[index] =   source.red_raster   [source_index];
				   green_raster[index] = source.green_raster [source_index];
				   blue_raster[index] =  source.blue_raster  [source_index];
			   }
		   }
	   }
	   
	   if (starty>endy) {
		   width=endx-startx;
		   height=starty-endy;

		   red_raster=new int[width*height];
		   green_raster=new int[width*height];
		   blue_raster=new int[width*height];
		   bump_height=new int[width*height];

		   for (int h=0; h<height; h++) {
			   for (int w=0; w<width; w++) {
				   int index=(h*width)+w;
				   int source_index=((starty-h)*source.width)+startx-w;
				   red_raster[index] =   source.red_raster   [source_index];
				   green_raster[index] = source.green_raster [source_index];
				   blue_raster[index] =  source.blue_raster  [source_index];
			   }
		   }
	   }
	   
	   
	   

 }

 public void setTexel(int x, int y, int r, int g, int b) {
				int index=(y*width)+x;
				red_raster[index] = r;
				green_raster[index] = g;
				blue_raster[index] = b;
			}
}



package rendercard;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.nio.ByteBuffer;


class Converter {
public static void main (String[] args) {
  String objPath=args[0];
  String mtlPath=args[1];
  GLModel model;
  try {
			FileInputStream r_path1 = new FileInputStream(objPath);
			BufferedReader b_read1 = new BufferedReader(new InputStreamReader(
					r_path1));
			model = new GLModel(b_read1, true,
					mtlPath);
       
			r_path1.close();
			b_read1.close();

		} catch (Exception e) {
			System.out.println("LOADING ERROR" + e);
}
 }
}
 

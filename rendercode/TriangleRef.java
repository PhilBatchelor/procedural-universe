
public class TriangleRef implements Comparable<TriangleRef> {

   public int ref=0;          // reference to the triangle in the RenderedScene object
   public double depth=0;     // depth of this triangle

   public TriangleRef(int r, double d) {
      this.ref=r;
      this.depth=d;
  
   } 

    public int compareTo(TriangleRef othertri) {
        if (this.depth>othertri.depth) {return -1;}
        if (this.depth<othertri.depth) {return 1;}
        return 0; 
   }
}



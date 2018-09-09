package rendercard;
public class Star extends OrbitingBody {
       public StellarClass stellarClass;
       
      	Star(StellarClass c) {
    		super(CelestialBodyType.STAR);
    		stellarClass=c;
    	}
}

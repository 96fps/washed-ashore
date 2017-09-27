import java.awt.Color;


public class Asteroid extends Entity {

	pos2d drift;//meters per second in X and Y
	double spinSpeed=(Math.random()-Math.random())*5;//meters per second in X and Y
	int level=4;//spawn 3-4, 
	
	static pos2d[] rock={new pos2d(-3.2, 2.4),new pos2d(-0.8, 3.2),
						 new pos2d( 0.0, 2.4),new pos2d( 2.4, 2.4),
						 new pos2d( 4.0, 0.8),new pos2d( 4.0,-1.6),
						 new pos2d( 3.2,-2.4),new pos2d( 3.2,-2.4),
						 new pos2d( 0.0,-4.0),new pos2d(-0.8,-4.0),
						 new pos2d(-1.6,-3.2),new pos2d(-3.2,-2.4),
						 new pos2d(-4.0, 0.0)
						 };
	static shape2d rockshape=new shape2d(rock);
	
	public Asteroid(pos2d location, pos2d movement, int size){
		super(location, rockshape, (int)(Math.random()*360), new Color(255,192,128));
		System.out.println("New Asteroid!, location="+location.x+"*"+location.y+", size="+size+", drifting="+movement.x+"*"+movement.y);
		drift=movement;
		level=size;
		
	}
}

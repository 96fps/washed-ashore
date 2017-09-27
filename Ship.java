import java.awt.Color;


public class Ship extends Entity {
	
	pos2d drift= new pos2d(0,0);//meters per second in X and Y
	int health=100;//percent health, 
	
	Color color= new Color(255,255,255);
	
	static pos2d[] ship={new pos2d(-3,-7),new pos2d(-4,-5),new pos2d(-6.5,-5),new pos2d(-6,-1),new pos2d(-4,0),new pos2d(-4,-5),new pos2d(-4,0),new pos2d(-1,7),new pos2d(0,7.5),
						 new pos2d(1,7),new pos2d(4,0),new pos2d(4,-5),new pos2d(4,0),new pos2d(6,-1),new pos2d(6.5,-5),new pos2d(4,-5),new pos2d(3,-7)};
	static shape2d spaceship=new shape2d(ship.length,ship);
	
	public Ship(){
		super(new pos2d(0,0), spaceship, (Math.random()*360), new Color(255,255,255));
		
		
	}
	public Ship(pos2d location, pos2d movement){
		super(location, spaceship, (Math.random()*360), new Color(255,255,255));
		drift=movement;
		
	}
}

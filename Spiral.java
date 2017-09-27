import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;


public class Spiral {
	public ArrayList<pos3d> pos= new ArrayList<pos3d>();
	
	public Spiral(){
		this((int)(Math.random()*64), (int)((Math.random()*64)*1.2), (int)(Math.random()*16+2), 10, (int)(Math.random()*3+2), (int)(Math.random()*3),1,(int)(Math.random()*40-20), new pos3d((Math.random()*180),(Math.random()*180),(Math.random()*180)));
	}
	public Spiral(int spirStart, int spirEnd, int spread, double detail, int starMin, int starMax, int spirs, double angle, pos3d slant)
	{	
		angle=angle*(Math.PI/180);
		int starSize=0;
		for(int y=0; y<spirs; y++){
			for(double x=spirStart; x<spirEnd;x+=1/(double)detail)
				//for(int y=0; y<4;y++)
			{
				double scaleDrawn=4;
				starSize=(int)((starMin+(Math.random()*(starMax)))*(scaleDrawn/8))+2;
				double xPos=((x*Math.sin(((double)x/12)))*4)+((Math.random()*2*spread-spread));
				double yPos=((x*Math.cos(((double)x/12)))*4)+((Math.random()*2*spread-spread));
				double xPos2=((xPos*Math.cos(angle)) - (yPos*Math.sin(angle)))*scaleDrawn/8;
				double yPos2=((yPos*Math.cos(angle)) + (xPos*Math.sin(angle)))*scaleDrawn/8;
				pos.add(	pos3d.rotate(new pos3d(xPos2, yPos2, (Math.random()-Math.random())*scaleDrawn*Math.random()*8),		slant	));
			}
			angle+=(2*Math.PI)/spirs;
		}
	}
	
	public Spiral(int numSpirs)
	{	
		for(int i=0; i<10;i++);
		
		double x=0, y=0, z=0;
		
		pos.add( new pos3d(x, y, z));
			
	}
}

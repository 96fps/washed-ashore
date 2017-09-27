import java.awt.Color;


public class StarScape {
	//stores simple closed 2d shapes
	int numPoints=128;
	public pos2d[] stars;
	public pos2d[] stellarDrift;
	Color[] colors;
	public StarScape(){
		double light;
		stars= new pos2d[numPoints];
		colors=new Color[numPoints];
		stellarDrift=new pos2d[numPoints];
		for(int s=0; s<numPoints;s++){
			light=Math.random();
			stars[s]= new pos2d(Math.random(),Math.random());
			colors[s]=new Color((int)(((Math.random()*64)+191)*light),(int)(((Math.random()*32)+191)*light),(int)(((Math.random()*64)+191)*light));
		}
	}public StarScape(int numStars){
		numPoints=numStars;
		double light;
		stars= new pos2d[numPoints];
		colors=new Color[numPoints];
		stellarDrift=new pos2d[numPoints];
		for(int s=0; s<numPoints;s++){
			stars[s]= new pos2d(Math.random(),Math.random());

			stellarDrift[s]=new pos2d(Math.random()-Math.random(),Math.random()-Math.random());
			colors[s]=new Color((int)(Math.random()*64)+191,(int)(Math.random()*32)+191,(int)(Math.random()*64)+191);
		}
	}
	
}

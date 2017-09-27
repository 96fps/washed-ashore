import java.awt.Color;
import javax.swing.Timer;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.imageio.*;

import org.ietf.jgss.GSSContext;


import java.awt.event.KeyListener;
import java.awt.Robot;

public class GamePanel extends JPanel implements MouseInputListener, KeyListener, ActionListener {
	static public boolean running=false;
	
	static public boolean keyW=false; //move forward
	static public boolean keyA=false; //move left
	static public boolean keyS=false; //move back
	static public boolean keyD=false; //move right
	static public boolean keyShift=false; //move up  -run
	static public boolean keySpace=false; //move up  -jump
	static public boolean keyCtrl=false; //move down
	static public boolean trackShip=false; //move down
	
	
	static public boolean keyUp=false;   //pitch up
	static public boolean keyDown=false; //pitch down
	static public boolean keyLeft=false; //yaw left
	static public boolean keyRight=false; //yaw right
	static public boolean keyCW=false; //roll CW
	static public boolean keyCCW=false;//roll CCW
	static public boolean mouselock=false;   //pitch up
	static boolean oversample=false;
	static boolean clearBG=false;
	static boolean in3dmode=false;
	static boolean LCam=true;
	static public double sealevel=0;
	double it=4;
	
	MouseEvent mouselook;
	
	Robot bot;
	
	int groundR,groundG,groundB;
	
	double drawWidthMult=1;
	//camera properties
	pos3d control=new pos3d(0,0,0);		//x-y-z units=m/s
	pos3d drift=new pos3d(0,0,0);		//x-y-z units=m/s
	pos3d position=new pos3d(0,128,0); 	//x-y-z units=meters
	pos3d gaze=new pos3d(0,0,0); 		//heading-pitch-roll. units=degrees

	pos3d shipVel=new pos3d(0,0,0);
	pos3d shipPos=new pos3d(0,128,0);
	pos3d shipRot=new pos3d();
	
	public ArrayList<Line3d> ellipse = new ArrayList<Line3d>();
	public ArrayList<Line3d> enterprise = new ArrayList<Line3d>();
	public ArrayList<Line3d> drawList = new ArrayList<Line3d>();
	public ArrayList<Line3d> sky = new ArrayList<Line3d>();
	
	public pos3d galaxytilt= new pos3d(90,0,0);
	public ArrayList<Spiral> galaxy = new ArrayList<Spiral>();
	public ArrayList<pos3d> stars=new ArrayList<pos3d>();
	
	int FPS=32; //(desired fps) 
	double tickerFactor=250/FPS;
	double frametime=1000/FPS;
	double bgD=1;
	//double FOVmod=4;
	
//	double celestialSpeed=.00001157407407;//=such that at 32fps, 24 hrs = i cycle.
	double celestialSpeed=0;//0.
	double celestialTicker=(double)0;//exposure
	double exposure=16*16;//

	pos2d gravCenter= new pos2d(0,0);
	pos2d satPos= new pos2d(-1,0);
	pos2d satForce= new pos2d(0,0); //
	double satAlt=Math.sqrt(Math.pow(gravCenter.x+satPos.x,2)+Math.pow(gravCenter.y+satPos.y,2));
	
	double movespeedfactor=1;
	double ticker=-500;

	double fametime=10;
	Timer clock= new Timer((int)(fametime), this);
	double fuel=500;

	double rand[]={Math.random(),Math.random(),Math.random(),Math.random()};

	double skyLight=0;
	
	public World island=new World();;
	
	int gameWidth=800;
	int gameHeight=480;
	
//	int gameWidth=1024;
//	int gameHeight=720;

//	int gameWidth=480;
//	int gameHeight=320;

//	int gameWidth=320;
//	int gameHeight=240;
	
	double FOVmod=2*gameWidth/480; //bigger = more zoom
	
	NoiseMap n=new NoiseMap();
	
	bezier3d b= new bezier3d(new pos3d(-100,0,0),new pos3d(-100,0,-1000),new pos3d(100,0,-1000),new pos3d(100,0,0));

	pos3d bez=new pos3d(0,2,3);
	pos3d velBez=new pos3d(0,2,3);
	
	ArrayList<poly3d> terrain= new ArrayList<poly3d>();
	ArrayList<poly3d> drawPoly=new ArrayList<poly3d>();
	double zoom;
	double pan;
	double pitch;
//	double shipVel=0;
	pos3d velA=new pos3d(), velB=new pos3d(), velC=new pos3d(), velD=new pos3d();
	
	double num;
	Ship player=new Ship();
	double playerSpin=(Math.random()-Math.random())*10;
		
	public pos3d colorMult=new pos3d(1,1,1);
	
	
	@Override
	public void actionPerformed(ActionEvent e) {//new game "tick"
		double offset=0.5;
		if(in3dmode){
		if(LCam)
			offset=2;
		if(!LCam)
			offset=-2;
		
		if(LCam)
			colorMult=new pos3d(1,0,0);
		if(!LCam)
			colorMult=new pos3d(0,0,1);
		}
		else
		{
			offset=0;
			colorMult=new pos3d(1,1,1);
		}
		position.z= position.z*.8+ ( shipPos.z/128 +pos3d.rotateb(new pos3d(offset,.5,1.5), shipRot).z)*.2;
		position.y= position.y*.8+ ( shipPos.y/128 +pos3d.rotateb(new pos3d(offset,.5,1.5), shipRot).y)*.2;
		position.x= position.x*.8+ ( shipPos.x/128 +pos3d.rotateb(new pos3d(offset,.5,1.5), shipRot).x)*.2;

		
		if(running){
		pos3d.mult(shipVel, 0.999);
		shipPos.add(shipVel);
		double tempx=128+(shipPos.x/128);
		double tempy=128-(shipPos.z/128);
		if(tempx<0){tempx=0;}
		if(tempx>255){tempx=0;}
		if(tempy<0){tempy=0;}
		if(tempy>255){tempy=255;}
		double surf=32*(island.terrain[(int)tempx][(int)tempy]-86);
		System.out.println(surf+"-"+shipPos.y);//+"="+shipPos.y+-surf
		System.out.println("="+shipPos.y+-surf);//+"="+shipPos.y+-surf
//		if(shipPos.y<=surf+0.1)
//		{
//			shipVel.y=Math.abs(shipVel.y)/4;
//			if(shipVel.y<0.1)
//				shipVel.y=0;
//			shipPos.y=surf;
//		}
//		else
//			shipVel.y-=5;
		
//		shipPos.y=surf;
		if(shipPos.y<surf)
		{
			shipPos.y=surf;
		}
	}
		control.x=0;
		control.y=0;
		control.z=0;

		
		
		it=4;
		if(running){
			shipPos.y+=0;
		{
		//			position=shipPos;
			gaze.z= gaze.z*.8+ ( shipRot.z)*.2;
			gaze.y= gaze.y*.8+ (180- shipRot.y)*.2+(4*offset);
			gaze.x= gaze.x*.8+ ( shipRot.x )*.2;
		}
			
//		{
//		shipRot.x=0;
//		shipRot.y+=Math.sin(Math.toRadians(ticker*4))*1;
//		shipRot.z=0;
//		shipPos.x+=Math.sin(Math.toRadians(-shipRot.y))*-shipVel*-8;
//		shipPos.y=32*128;
//		shipPos.z+=Math.cos(Math.toRadians(shipRot.y))*-shipVel*-8;
//		}
		pan=Math.atan2(position.x-shipPos.x/128, position.z-(shipPos.z+32)/128)*180/Math.PI+180;
		pitch=Math.atan2(position.y-(shipPos.y+128)/128, Math.sqrt(Math.pow(position.z-(shipPos.z+32)/128,2)+Math.pow(position.x-shipPos.x/128,2)))*-180/Math.PI;
		zoom=Math.sqrt(Math.pow(position.x-shipPos.x/128, 2)+Math.pow(position.y-shipPos.y/128, 2)+Math.pow(position.z-shipPos.z/128, 2))/256;
		if(trackShip){
//		if(FOVmod>zoom)
		FOVmod=FOVmod*3/4+zoom/4/2;
//			FOVmod=zoom;
			if(FOVmod<2)
				FOVmod=2;
			if(FOVmod>8)
				FOVmod=8;
//		if(gaze.y<0)
//			gaze.y+=360;
//		gaze.y=gaze.y*3/4+pan/4;
		gaze.y=pan;
			
		
		
//		gaze.x=gaze.x*3/4+pitch/4;
		gaze.x=pitch;
		}
		}
		if(ticker>0)//once game actually starts,
		{
			
		}
		if(running){
		
			if(keyW){
				control.z-=1;
//				control.x+=pos2d.rotY(new pos2d(1,0), gaze.y);
				}
			if(keyS){
				control.z+=1;
//				control.x+=pos2d.rotY(new pos2d(-1,0), gaze.y);
				}
			if(keyA){
				control.x-=1;
//				control.x+=pos2d.rotY(new pos2d(0,-1), gaze.y);
				}
			if(keyD){
//				control.z+=pos2d.rotX(new pos2d(0,1), gaze.y);
				control.x+=1;
//				control.x+=pos2d.rotY(new pos2d(0,1), gaze.y);
				}
			if(keyShift)
				control.y+=1;
			if(keyCtrl)
				control.y-=1;
			
		
			
	
			if(keyUp){
				shipRot.x-=1024/tickerFactor*90/FOVmod/gameWidth;
//				gaze.x-=1024/tickerFactor*90/FOVmod/gameWidth;
				}
			if(keyDown){
				shipRot.x+=1024/tickerFactor*90/FOVmod/gameWidth;
//				gaze.x+=1024/tickerFactor*90/FOVmod/gameWidth;
				}
			if(keyLeft){
				shipRot.y+=1024/tickerFactor*90/FOVmod/gameWidth;
//				gaze.y-=1024/tickerFactor*90/FOVmod/gameWidth;
				}
			if(keyRight){
				shipRot.y-=1024/tickerFactor*90/FOVmod/gameWidth;
//				gaze.y+=1024/tickerFactor*90/FOVmod/gameWidth;
				}
			if(keyCW){
				shipRot.z+=1024/tickerFactor*90/FOVmod/gameWidth;
//				gaze.z+=1024/tickerFactor*90/FOVmod/gameWidth;
				}
			if(keyCCW){
				shipRot.z-=1024/tickerFactor*90/FOVmod/gameWidth;
//				gaze.z-=1024/tickerFactor*90/FOVmod/gameWidth;
				}
		
		}
		if(running){
			{
				bezier3d b= new bezier3d(new pos3d(-250,0,250),new pos3d(-500,0,-500),new pos3d(500,0,-500),new pos3d(250,0,250));
			//	position=b.getpoint(ticker/360);
			
//			velBez.x+=(Math.random()-Math.random());
//			velBez.y+=(Math.random()-Math.random());
//			velBez.z+=(Math.random()-Math.random());
			
//			if(bez.y<0)
//				velBez.y+=8;
			
			
//			bez.x+=velBez.x*tickerFactor/1024;
//			bez.y+=velBez.y*tickerFactor/1024;
//			bez.z+=velBez.z*tickerFactor/1024;
//			
//			velBez.x*=.75;
//			velBez.y*=.95;
//			velBez.z*=.75;
//			
//			bez.x*=.95;
//			bez.y*=.95;
//			bez.z*=.95;
//
//			pos3d temp=Line3d.lerp(Line3d.lerp(b.a, b.d,0.5),
//					   Line3d.lerp(b.b, b.c,0.5), 0.8);
//			
//			bez.add(temp);
//			
//			b.a.add(pos3d.negate(temp));
//			b.b.add(pos3d.negate(temp));
//			b.c.add(pos3d.negate(temp));
//			b.d.add(pos3d.negate(temp));
//
//			
//			
//			velA.add(new pos3d(Math.random()-Math.random(),Math.random()-Math.random(),Math.random()-Math.random()));
//			velB.add(new pos3d(Math.random()-Math.random(),Math.random()-Math.random(),Math.random()-Math.random()));
//			velC.add(new pos3d(Math.random()-Math.random(),Math.random()-Math.random(),Math.random()-Math.random()));
//			velD.add(new pos3d(Math.random()-Math.random(),Math.random()-Math.random(),Math.random()-Math.random()));
//			b.a.x+=velA.x*tickerFactor/1024*2;
//			b.a.y+=velA.y*tickerFactor/1024;
//			b.a.z+=velA.z*tickerFactor/1024*2;
//			b.b.x+=velB.x*tickerFactor/1024*2;
//			b.b.y+=velB.y*tickerFactor/1024;
//			b.b.z+=velB.z*tickerFactor/1024*2;
//			b.c.x+=velC.x*tickerFactor/1024*2;
//			b.c.y+=velC.y*tickerFactor/1024;
//			b.c.z+=velC.z*tickerFactor/1024*2;
//			b.d.x+=velD.x*tickerFactor/1024*2;
//			b.d.y+=velD.y*tickerFactor/1024;
//			b.d.z+=velD.z*tickerFactor/1024*2;
//			
//			b.a.x*=.95;
//			b.a.y*=.95;
//			b.a.z*=.95;
//			b.b.x*=.95;
//			b.b.y*=.95;
//			b.b.z*=.95;
//			b.c.x*=.95;
//			b.c.y*=.95;
//			b.c.z*=.95;
//			b.d.x*=.95;
//			b.d.y*=.95;
//			b.d.z*=.95;
			}
			
			drawList=new ArrayList<Line3d>();;   //clear list
			//System.out.println("ellipse.size()="+ellipse.size());
			for(int i=0; i<ellipse.size(); i++){
				//System.out.println("itterator="+i);
				//System.out.println("ellipse.get(i)="+ellipse.get(i).toString());
				drawList.add(ellipse.get(i));
			} //populate world
			
			
			double itterator=Math.pow(2, -6);
//			for(double temp=0; temp<1; temp+=itterator)   //add "bez"
//			{
////				drawList.add(new Line3d(	new pos3d(b.getpoint(temp).x-position.x+bez.x,
////													  b.getpoint(temp).y-position.y+bez.y,
////													  b.getpoint(temp).z-position.z+bez.z),
////										    new pos3d(b.getpoint(temp+itterator).x-position.x+bez.x,
////													  b.getpoint(temp+itterator).y-position.y+bez.y,
////												      b.getpoint(temp+itterator).z-position.z+bez.z)) );   
//				drawList.add(new Line3d(	b.getpoint(temp),
//											b.getpoint(temp+itterator)) );   
//			}
//			drawList.add(new Line3d(b.a, b.b));  
//			drawList.add(new Line3d(b.c, b.d));   

		}
		
		if(mouselook!=null)
		{System.out.println("mouselook X"+mouselook.getX());
		System.out.println("mouselook Y"+mouselook.getY());}
		
		
//		if(mouselock){
//				//bot.mouseMove(800, 450);
////			gaze.x+=mouselook.x;
////			gaze.y+=mouselook.y;
//			
//		}
		
		//set up draw-list
		
		if(running){
//			ticker++;
			
//			
//			if((position.y<=0.6&&!keyCtrl)||position.y<=0.3){
//				if(keyShift){
//					control.x*=2;
//					control.z*=2;
//				}
//				
//				drift.x += control.x*2;
//				drift.z += control.z*2;
//				
//				
//				drift.y += control.y*5;
//				
//				drift.y=drift.y*0.25;
//				
//				if(drift.y<0.125)
//					drift.y=0;
//				
//				
//				if(keyCtrl)
//					position.y=0.25;
//				else	position.y=0.5;
//					
//				drift.x*=0.8;
//				if(Math.abs(drift.x)<0.125)
//					drift.x=0;
//						
//				drift.z*=0.8;
//				if(Math.abs(drift.z)<0.125)
//					drift.z=0;
//				
//				
//				
//			}
//			else if(position.y>0.5){
//				drift.x += control.x*0.2;
//				drift.z += control.z*0.2;
//				
//				drift= new pos3d(drift.x*0.99,drift.y*0.99-0.5,drift.z*0.99);
//			
//				
//			}
			drift.x*=0.95;
	    	drift.y*=0.95;
	    	drift.z*=0.95;
//	    	drift.x=0;
//	    	drift.y=0;
//	    	drift.z=0;
			
//	    	drift.x+=control.x;
//	    	drift.y+=control.y;
//	    	drift.z+=control.z;
//	    	
	    	{
//	    		shipRot.x=0;
//	    		shipRot.y+=Math.sin(Math.toRadians(ticker*4))*1;
//	    		shipRot.z=0;
//	    		shipPos.x+=Math.sin(Math.toRadians(-shipRot.y))*-shipVel*-8;
////	    		shipPos.y=32*128;
//	    		shipPos.z+=Math.cos(Math.toRadians(shipRot.y))*-shipVel*-8;
	    	}
	    	
	    	shipPos.x+=pos3d.rotateb(new pos3d(control.x*movespeedfactor,control.y*movespeedfactor,control.z*movespeedfactor), shipRot).x;
	    	shipPos.y+=pos3d.rotateb(new pos3d(control.x*movespeedfactor,control.y*movespeedfactor,control.z*movespeedfactor), shipRot).y;
	    	shipPos.z+=pos3d.rotateb(new pos3d(control.x*movespeedfactor,control.y*movespeedfactor,control.z*movespeedfactor), shipRot).z;
	    	//System.out.println(tickerFactor);
			position.x += drift.x*tickerFactor*movespeedfactor/400;
			position.y += drift.y*tickerFactor*movespeedfactor/400;
			position.z += drift.z*tickerFactor*movespeedfactor/400;
			
			
			control.x=0;
			control.y=0;
			control.z=0;

//			position.x%=4;
//			position.z%=4;
			
			
		}
		
		
		if(true){
			sky.clear();
			double tilt=215;
			
			
		
			for(int i=0; i<galaxy.size(); i++)
			{	for(int j=0; j<galaxy.get(i).pos.size(); j++)
				{
					sky.add(new Line3d( pos3d.add(pos3d.rotate(new pos3d(galaxy.get(i).pos.get(j).x+Math.sin(celestialTicker/100)*2+64,galaxy.get(i).pos.get(j).y+0,galaxy.get(i).pos.get(j).z+Math.cos(celestialTicker/100)*2+64),new pos3d(0,celestialTicker,tilt)), pos3d.mult(position, -0.01)), 
										pos3d.add(pos3d.rotate(new pos3d(galaxy.get(i).pos.get(j).x+Math.sin(celestialTicker/100)*2+64,galaxy.get(i).pos.get(j).y+0,galaxy.get(i).pos.get(j).z+Math.cos(celestialTicker/100)*2+64),new pos3d(0,celestialTicker,tilt)), pos3d.mult(position, -0.01))));
	
				}
			}
			
			
//			for(temp=0; temp<360; temp+=itterator)
//			{	//System.out.println("make sky");
//				sky.add(new Line3d(	new pos3d(  (Math.sin(Math.toRadians(temp			  )))*256,0,
//												(Math.sin(Math.toRadians(temp+90		  )))*256),
//									new pos3d(  (Math.sin(Math.toRadians(temp	+itterator)))*256,0, 
//												(Math.sin(Math.toRadians(temp+90+itterator)))*256)   ));   }
//			sky.add(new Line3d(pos3d.rotate(new pos3d(-0.125,0,0), new pos3d(32,celestialTicker/celestialSpeed,215)),
//			           pos3d.rotate(new pos3d(-0.125,0,0), new pos3d(32,celestialTicker/celestialSpeed,215))));
//			sky.add(new Line3d(pos3d.rotate(new pos3d(-0.07,0,0), new pos3d(32,celestialTicker/celestialSpeed,215)),
//			           pos3d.rotate(new pos3d(-0.07,0,0), new pos3d(32,celestialTicker/celestialSpeed,215))));
//			sky.add(new Line3d(pos3d.rotate(new pos3d(-0.25,0,0), new pos3d(32,celestialTicker/celestialSpeed,215)),
//			           pos3d.rotate(new pos3d(-0.25,0,0), new pos3d(32,celestialTicker/celestialSpeed,215))));
			sky.add(new Line3d(pos3d.add(pos3d.rotate(new pos3d(-1,0,0), new pos3d(0,celestialTicker,tilt)),pos3d.negate(position)),
							   pos3d.add(pos3d.rotate(new pos3d(-1,0,0), new pos3d(0,celestialTicker,tilt)), pos3d.negate(position))));
	
		}
		
		skyLight=((Math.cos((((celestialTicker)%360)/360)*2*Math.PI)+1)/2);//0-1
		skyLight=skyLight*0.75+0.25;
		//System.out.println("skylight="+skyLight);
		celestialTicker+=0;
		if(running)
			celestialTicker+=celestialSpeed;
//		celestialTicker=0;
//		ticker+=1;
//		ticker%=360;
//		if(ticker%90<45)
//		{	position= pos3d.rotate(new pos3d(0,16,128), new pos3d(0,ticker,0));
//			gaze =new pos3d(-8,180-ticker,0);
//			position.x+=Math.sin(Math.toRadians(ticker%360));}
////		else if(ticker<270)
////		{	position= pos3d.rotate(new pos3d(0,96,96), new pos3d(0,ticker,0));
////			gaze =new pos3d(-66,180-ticker,0);
////			position.x+=Math.sin(Math.toRadians(ticker%360));}
//		else
//		{	position= pos3d.rotate(new pos3d(0,256,64), new pos3d(0,ticker,0));
//			gaze =new pos3d(-75,180-ticker,0);
//			position.x+=Math.sin(Math.toRadians(ticker%360));
//			if(ticker%90==75)
//				island=new World();
//		}
		
		ellipse.clear();
		for(int i=0; i<enterprise.size(); i++)
		{
			ellipse.add(new Line3d(pos3d.add(pos3d.rotateb(pos3d.add(enterprise.get(i).a,new pos3d(0,-65,-230)),shipRot),shipPos),
						           pos3d.add(pos3d.rotateb(pos3d.add(enterprise.get(i).b,new pos3d(0,-65,-230)),shipRot),shipPos)) );
		}
		repaint();
		
	}	
	 	GamePanel()
	{
		
		clock.start();
		addKeyListener(this);

		

//		shipVel=2;
		
		setFocusable(true);
		
		addMouseListener(this);
		
		setPreferredSize(new Dimension(gameWidth, gameHeight));
		for(int i=0; i<32; i++){
		galaxy.add(new Spiral((int)(Math.random()*64), (int)((Math.random()*64)*1.2), (int)(Math.random()*16+2), 5, (int)(Math.random()*3+2), (int)(Math.random()*3),1,(int)(Math.random()*40-20)+(i%4)*90, galaxytilt));
		}
		
		for(int i=0; i<galaxy.size(); i++)
		{	for(int j=0; j<galaxy.get(i).pos.size(); j++)
			{
			sky.add(new Line3d( pos3d.add(pos3d.rotate(new pos3d(galaxy.get(i).pos.get(j).x,galaxy.get(i).pos.get(j).y,galaxy.get(i).pos.get(j).z),new pos3d(0,0,215)),position), 
								pos3d.add(pos3d.rotate(new pos3d(galaxy.get(i).pos.get(j).x,galaxy.get(i).pos.get(j).y,galaxy.get(i).pos.get(j).z),new pos3d(0,0,215)),position)));
			}
		}
//		for(int ss=0; ss<sky.size(); ss++){
//			sky.get(ss).a.x+=position.x*128;
//			sky.get(ss).a.y+=position.y*128;
//			sky.get(ss).a.x+=position.z*128;
//		
//			sky.get(ss).b.x+=position.x*128;
//			sky.get(ss).b.y+=position.y*128;
//			sky.get(ss).b.z+=position.z*128;
//		}
		
		double temp;
		
//		for(temp=0; temp<360; temp+=itterator)
//		{
//			ellipse.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp))),
//												(Math.sin(Math.toRadians(temp+90)))+1, 0),
//									new pos3d( (Math.sin(Math.toRadians(temp+itterator))), 
//												(Math.sin(Math.toRadians(temp+90+itterator)))+1, 0)   ));   }
//		for(temp=0; temp<360; temp+=itterator)
//		{
//			ellipse.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp))),0+1,
//												(Math.sin(Math.toRadians(temp+90)))),
//									new pos3d( (Math.sin(Math.toRadians(temp+itterator))),0+1, 
//												(Math.sin(Math.toRadians(temp+90+itterator))))   ));   }
//		for(temp=0; temp<360; temp+=itterator)
//		{
//			ellipse.add(new Line3d(	new pos3d(0, (Math.sin(Math.toRadians(temp)))+1,
//												(Math.sin(Math.toRadians(temp+90)))),
//									new pos3d(0, (Math.sin(Math.toRadians(temp+itterator)))+1, 
//												(Math.sin(Math.toRadians(temp+90+itterator))))   ));   }
//		
//		for(temp=0; temp<360; temp+=itterator)
//		{	System.out.println("make sky");
//			sky.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*1024,0,
//												(Math.sin(Math.toRadians(temp+90)))*1024),
//									new pos3d( (Math.sin(Math.toRadians(temp+itterator)))*1024,0, 
//												(Math.sin(Math.toRadians(temp+90+itterator)))*1024)   ));   }
//		
//		double itt=1;
//		for(temp=0; temp<360; temp+=itt)
//		{	System.out.println("make hills");
//			sky.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp))),		(Math.sin(Math.toRadians(temp*16))+1)/32,
//											(Math.sin(Math.toRadians(temp+90)))),
//								new pos3d( (Math.sin(Math.toRadians(temp+itt))),	(Math.sin(Math.toRadians((temp+itt)*16))+1)/32, 
//											(Math.sin(Math.toRadians(temp+90+itt))))   ));
//		
//			double temp2=1/(Math.random()*8);
//			sky.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp))),		temp2-0.125,
//											(Math.sin(Math.toRadians(temp+90)))),
//								new pos3d( (Math.sin(Math.toRadians(temp+itt/2))),	temp2-0.125, 
//											(Math.sin(Math.toRadians(temp+90+itt/2))))   ));
//		}
		
		for(temp=-64; temp<64; temp+=0.125)
		{
			ellipse.add(new Line3d(	new pos3d( temp,0,Math.pow(temp, 2)),
									new pos3d( temp+0.125,0,Math.pow(temp+0.125, 2)) )   );   }
		for(temp=-64; temp<64; temp+=0.125)
		{
			ellipse.add(new Line3d(	new pos3d( temp,0,-Math.pow(temp, 2)),
									new pos3d( temp+0.125,0,-Math.pow(temp+0.125, 2)) )   );   }
		for(temp=-64; temp<64; temp+=0.125)
		{
			ellipse.add(new Line3d(	new pos3d( Math.pow(temp, 2), 0, temp),
									new pos3d( Math.pow(temp+0.125, 2), 0, temp+0.125) )   );   }
		for(temp=-64; temp<64; temp+=0.125)
		{
			ellipse.add(new Line3d(	new pos3d(-Math.pow(temp, 2), 0, temp),
									new pos3d(-Math.pow(temp+0.125, 2), 0, temp+0.125) )   );   }
		
		
		
//		temp=8;
//		int ender=4;
//		
//		for(int i=ender-1; i>=0; i--)
//		for(double tempX=-temp; tempX<temp; tempX+=1)
//		{for(double tempZ=-temp; tempZ<temp; tempZ+=1)
//		{
//			//if(tempX<=temp*-.25||tempX>=temp*.25||tempZ<=temp*-.25||tempZ>=temp*.25||i==0){
//				System.out.println("X="+tempX+"Z="+tempZ);
//				ellipse.add(new Line3d(	new pos3d( tempX   *Math.pow( 2,Math.pow( 2,i)),  0, tempZ   *Math.pow( 2,Math.pow( 2,i))),
//										new pos3d((tempX+1)*Math.pow( 2,Math.pow( 2,i)),  0, tempZ   *Math.pow( 2,Math.pow( 2,i)))  ));  
//				
//				ellipse.add(new Line3d(	new pos3d( tempX   *Math.pow( 2,Math.pow( 2,i)),  0, tempZ   *Math.pow( 2,Math.pow( 2,i))),
//										new pos3d( tempX   *Math.pow( 2,Math.pow( 2,i)),  0,(tempZ+1)*Math.pow( 2,Math.pow( 2,i)))  ));   
//			//}
//
//			}
//		}
		
		
		
			
		running=false;
	}
	

	public void paintComponent(Graphics g)
	{	//System.out.println("valid");
//		if(oversample&!clearBG){
//			g.setColor(new Color(0,0,0,4));}
//		else
		groundR=(int)( 0*colorMult.x);
		groundG=(int)( 0*colorMult.y);
		groundB=(int)( 0*colorMult.z);
		
		int gree=31;
			g.setColor(new Color((int)(groundR*skyLight)/4,(int)(groundG*skyLight)/4,(int)(groundB*skyLight)/4));
		
		clearBG=false;
		
		g.fillRect(0, 0, gameWidth, gameHeight);
		
		ArrayList<Line3d> skybox = new ArrayList<Line3d>();
		for(int i=0; i<sky.size();i++){
			//System.out.println("sky rot");
			
			
			skybox.add(new Line3d(	pos3d.rotate(new pos3d(sky.get(i).a.x,
										sky.get(i).a.y,
										sky.get(i).a.z),gaze),
									pos3d.rotate(new pos3d(sky.get(i).b.x,
										sky.get(i).b.y,
										sky.get(i).b.z),gaze) )  );
			
		}
		
		
//		{
//		{
//		double v=2;		
//		double h=14;
//		pos3d e1, e2, e3, e4;
//		
//		double altitude=position.y/128;
//		double drawstart=(double)(Math.atan(-altitude/24)*56);
//		for(double w=drawstart; w<90; w+=v){
//			h=16*(90/(95-w))+1;
//			for(double q=-180; q<180; q+=h){
//		    // camera rot,   horz rot,   vert rot
//			e1=pos3d.rotate(pos3d.rotate(new pos3d(0,0,100), new pos3d(-w,0,0)), new pos3d(0,q,0));
//			e2=pos3d.rotate(pos3d.rotate(new pos3d(0,0,100), new pos3d(-w-v-2,0,0)), new pos3d(0,q,0));
//			e3=pos3d.rotate(pos3d.rotate(new pos3d(0,0,100), new pos3d(-w-v-2,0,0)), new pos3d(0,q+h,0));
//			e4=pos3d.rotate(pos3d.rotate(new pos3d(0,0,100), new pos3d(-w,0,0)), new pos3d(0,q+h,0));
//
////			e1.add(new pos3d(0,-Math.atan(position.y)*50,0));
////			e2.add(new pos3d(0,-Math.atan(position.y)*50,0));
////			e3.add(new pos3d(0,-Math.atan(position.y)*50,0));
////			e4.add(new pos3d(0,-Math.atan(position.y)*50,0));
//			
////			e2.add(position.mult(-0.125));
////			e3.add(position.mult(-0.125));
////			e4.add(position.mult(-0.125));
//			
//			e1=pos3d.rotate(e1,new pos3d(gaze.x,-gaze.y,-gaze.z));
//			e2=pos3d.rotate(e2,new pos3d(gaze.x,-gaze.y,-gaze.z));
//			e3=pos3d.rotate(e3,new pos3d(gaze.x,-gaze.y,-gaze.z));
//			e4=pos3d.rotate(e4,new pos3d(gaze.x,-gaze.y,-gaze.z));
//
//			//skybox.get(skybox.size()-1).a.x;
//			double alt=w;
//			alt+=altitude;
//			double atmosColorR=((((255/Math.pow((alt/45+1),4))*.75))+(((127/Math.pow((alt/45+1),3))*.75)+32)*(Math.cos(Math.toRadians(alt))*((Math.sin((q-(celestialTicker+45))   /360*2*Math.PI)+1)*1+0.5)+2*Math.sin(Math.toRadians(alt))))/(Math.abs(altitude/8)+1);
//			atmosColorR*=skyLight*0.5*0.33;
//			if(atmosColorR>255)
//				atmosColorR=255;
//			if(atmosColorR<0)
//					atmosColorR=0;
//			double atmosColorG=((((255/Math.pow((alt/45+1),4))*.75))+(((127/Math.pow((alt/45+1),3))*.75)+32)*(Math.cos(Math.toRadians(alt))*((Math.sin((q-(celestialTicker   ))   /360*2*Math.PI)+1)*1+0.5)+2*Math.sin(Math.toRadians(alt))))/(Math.abs(altitude/8)+1);
//			atmosColorG*=skyLight*0.5*0.5;
//			if(atmosColorG>255)
//				atmosColorG=255;
//			if(atmosColorG<0)
//					atmosColorG=0;
//			double atmosColorB=((((255/Math.pow((alt/45+1),4))*.75))+(((127/Math.pow((alt/45+1),3))*.75)+32)*(Math.cos(Math.toRadians(alt))*((Math.sin((q-(celestialTicker-45)+90)/360*2*Math.PI)+1)*1+0.5)+2*Math.sin(Math.toRadians(alt))))/(Math.abs(altitude/8)+1);
//			atmosColorB*=skyLight*0.5;
//			if(atmosColorB>255)
//				atmosColorB=255;
//			if(atmosColorB<0)
//				atmosColorB=0;
//		g.setColor(new Color((int)(atmosColorR),
//							 (int)(atmosColorG),
//							 (int)(atmosColorB)));
//		if (e1.z>0.125&&e2.z>0.125&&e3.z>0.125&&e4.z>0.125)
//		{
//		int[] x={(int)(( -FOVmod*(e1.x*128) / (e1.z))*drawWidthMult)	+gameWidth/2,
//				 (int)(( -FOVmod*(e2.x*128) / (e2.z))*drawWidthMult)	+gameWidth/2,
//				 (int)(( -FOVmod*(e3.x*128) / (e3.z))*drawWidthMult)	+gameWidth/2,
//				 (int)(( -FOVmod*(e4.x*128) / (e4.z))*drawWidthMult)	+gameWidth/2,}; 
//		int[] y={(int)(( -FOVmod*(e1.y*128) / (e1.z)))				+gameHeight/2,
//				 (int)(( -FOVmod*(e2.y*128) / (e2.z)))				+gameHeight/2,
//				 (int)(( -FOVmod*(e3.y*128) / (e3.z)))				+gameHeight/2,
//				 (int)(( -FOVmod*(e4.y*128) / (e4.z)))				+gameHeight/2,}; 
//		int i=4;
//		g.fillPolygon(x, y, i);
//		
//		}
//		
//		}}}
			
			
		
		
		//(0, 0, gameWidth, (int)((-FOVmod*(e.y*128) / (e.z)))	+gameHeight/2)
		
//		g.drawLine((int)(( FOVmod*(skybox.get(i).a.x*128) / (skybox.get(i).a.z))*drawWidthMult)	+gameWidth/2,
//				   (int)((-FOVmod*(skybox.get(i).a.y*128) / (skybox.get(i).a.z)))	+gameHeight/2,
//				   (int)(( FOVmod*(skybox.get(i).b.x*128) / (skybox.get(i).b.z))*drawWidthMult)	+gameWidth/2,
//				   (int)((-FOVmod*(skybox.get(i).b.y*128) / (skybox.get(i).b.z)))	+gameHeight/2);
//		
//		}}}
		
		
//		g.setColor(new Color(255,255,255));
		
		//g.drawString("X"+drift.x+"Y"+drift.y+"Z"+drift.z	,64, 64);
		
		
//		g.drawString("Player position: x="+position.x+", y="+position.y+"z="+position.z+".", 16, 16);
//		g.drawString("Bez position: x="+bez.x+", y="+bez.y+"z="+bez.z+".", 16, 32);
		
		
		
		ArrayList<Line3d> lines = new ArrayList<Line3d>();
		
		//System.out.println("drawList.size()="+drawList.size());
		
		
		
		for(int i=0; i<skybox.size();i++){
			if(skybox.get(i).a.z>0.06125&&skybox.get(i).b.z>0.06125)
					{
				   // System.out.println("SKY"+(int)( FOVmod*(skybox.get(i).a.x*128) / (skybox.get(i).a.z))+";"+(int)(-FOVmod*(skybox.get(i).a.y*128) / (skybox.get(i).a.z))	+gameHeight/2);

//				g.drawLine((int)(( FOVmod*(skybox.get(i).a.x*128) / (skybox.get(i).a.z))*drawWidthMult)	+gameWidth/2 -1,
//						   (int)((-FOVmod*(skybox.get(i).a.y*128) / (skybox.get(i).a.z)))	+gameHeight/2,
//						   (int)(( FOVmod*(skybox.get(i).b.x*128) / (skybox.get(i).b.z))*drawWidthMult)	+gameWidth/2 -1,
//						   (int)((-FOVmod*(skybox.get(i).b.y*128) / (skybox.get(i).b.z)))	+gameHeight/2);

				if(/*oversample&&*/ (32*exposure/(skybox.get(i).a.z+skybox.get(i).b.z))<255)
					g.setColor(new Color((int) (255*colorMult.x),(int) (255*colorMult.y),(int) (255*colorMult.z),((int)(32*exposure/(skybox.get(i).a.z+skybox.get(i).b.z))/2)));
				else
					g.setColor(new Color((int) (255*colorMult.x),(int) (255*colorMult.y),(int) (255*colorMult.z),127));
				if(i==skybox.size()-1)
					g.setColor(new Color((int) (255*colorMult.x),(int) (255*colorMult.y),(int) (255*colorMult.z)));
				
				if (sky.get(i).a.y>=0&&sky.get(i).b.y>=0)
				{
//					g.setColor(new Color((int)(FOVmod*32/ (skybox.get(i).a.z))+128,
//										 (int)(FOVmod*32/ (skybox.get(i).a.z))+128,
//										 (int)(FOVmod*32/ (skybox.get(i).a.z))));
//					
//				g.drawLine((int)(( FOVmod*(skybox.get(i).a.x*128) / (skybox.get(i).a.z))*drawWidthMult)	+gameWidth/2,
//						   (int)((-FOVmod*(skybox.get(i).a.y*128) / (skybox.get(i).a.z)))	+gameHeight/2,
//						   (int)(( FOVmod*(skybox.get(i).b.x*128) / (skybox.get(i).b.z))*drawWidthMult)	+gameWidth/2,
//						   (int)((-FOVmod*(skybox.get(i).b.y*128) / (skybox.get(i).b.z)))	+gameHeight/2);
//				
				g.fillOval((int)((( FOVmod*(skybox.get(i).a.x*128) / (skybox.get(i).a.z)) - FOVmod*8/(skybox.get(i).a.z*2)) *drawWidthMult)	+gameWidth/2,
							   (int)(((-FOVmod*(skybox.get(i).a.y*128) / (skybox.get(i).a.z)) - FOVmod*8/(skybox.get(i).a.z*2)))					+gameHeight/2,
							   (int)(FOVmod*(16/ (skybox.get(i).a.z*2))  *drawWidthMult)+1,
							   (int)(FOVmod*(16/ (skybox.get(i).a.z*2)))+1);
				}
//				g.drawLine((int)(( FOVmod*(skybox.get(i).a.x*128) / (skybox.get(i).a.z))*drawWidthMult)	+gameWidth/2 +1,
//						   (int)((-FOVmod*(skybox.get(i).a.y*128) / (skybox.get(i).a.z)))	+gameHeight/2,
//						   (int)(( FOVmod*(skybox.get(i).b.x*128) / (skybox.get(i).b.z))*drawWidthMult)	+gameWidth/2 +1,
//						   (int)((-FOVmod*(skybox.get(i).b.y*128) / (skybox.get(i).b.z)))	+gameHeight/2);
				

	}}
		//DRAW TERRAIN=======
		
				double playX=position.x+128;
				double playY=-position.z+128;
//				g.setColor(Color.WHITE);
//				g.drawString("x+"+playX, 64, 264);
//				g.drawString("y+"+playY, 64, 296);
//				it=8;
//				for(double x=0; x<=playX-it&&x<=256-2*it; x+=it)			//0-->X
//					for(double y=0; y<=playY-it&&y<=256-2*it; y+=it){	//0-->Y
//						drawTerrainTile(g, x, y, it);
//						}
//				for(double x=0; x<=playX-it&&x<=256-2*it; x+=it)			//0-->X
//					for(double y=256-2*it; y>=playY-it&&y>=0+it; y-=it){	//1-->Y
//						drawTerrainTile(g, x, y, it);
//						}
//				for(double x=256-2*it; x>=playX-it&&x>=0+it; x-=it)		//1-->X
//					for(double y=0; y<=playY-it&&y<=256-it; y+=it){	//0-->Y
//						drawTerrainTile(g, x, y, it);
//						}
//				for(double x=256-2*it; x>=playX-it&&x>=0+it; x-=it)		//1-->X
//					for(double y=256-2*it; y>=playY-it&&y>=0+it; y-=it){	//1-->Y
//						drawTerrainTile(g, x, y, it);
//						}
//				
				g.setColor(Color.WHITE);
//				g.drawString("x+"+playX, 64, 264);
//				g.drawString("y+"+playY, 64, 296);
				it=4;
				double itfactor=2;
				double dist=it*8;
				double lock=it*4;
				double start=32;
				double end=4;
				for(it=start; it>=end; it/=itfactor){
//				for(it=end; it<start; it*=itfactor){
					dist=it*16;
					lock=it*itfactor;
				for(double x=lock*(int)(playX/lock)-dist; x<=playX-it&&x<=256-it; x+=it)			//0-->X
					{if(x<0)x=0;
					for(double y=lock*(int)(playY/lock)-dist; y<=playY-it&&y<=256-it; y+=it){	//0-->Y
						if(y<0)y=0;
//						if(((y>=lock/itfactor*(int)(playY/(lock/itfactor))-dist
//								||y<lock/itfactor*(int)(playY/(lock/itfactor))-dist)
//								&&(x>=lock/itfactor*(int)(playX/(lock/itfactor))-dist
//								||x<lock/itfactor*(int)(playX/(lock/itfactor))-dist)))
							drawTerrainTile(g, x, y, it);
					}	}
				for(double x=lock*(int)(playX/lock)-dist; x<=playX-it&&x<=256-it; x+=it)			//0-->X
					{if(x<0)x=0;
					for(double y=lock*(int)(playY/lock)+dist; y>=playY-it&&y>=0+it; y-=it){	//1-->Y
						if(y+it>256)y=256-it;
//						if(!(y>=lock/itfactor*(int)(playY/(lock/itfactor))-dist&&y<lock/itfactor*(int)(playY/(lock/itfactor))-dist&&x>=lock/itfactor*(int)(playX/(lock/itfactor))-dist&&x<lock/itfactor*(int)(playX/(lock/itfactor))-dist))
								drawTerrainTile(g, x, y, it);
					}	}
				for(double x=lock*(int)(playX/lock)+dist; x>=playX-it&&x>=0+it; x-=it)		//1-->X
					{if(x+it>256)x=256-it;
					for(double y=lock*(int)(playY/lock)-dist; y<=playY-it&&y<=256-it; y+=it){	//0-->Y
						if(y<0)y=0;
//						if(!(y>=lock/itfactor*(int)(playY/(lock/itfactor))-dist&&y<lock/itfactor*(int)(playY/(lock/itfactor))-dist&&x>=lock/itfactor*(int)(playX/(lock/itfactor))-dist&&x<lock/itfactor*(int)(playX/(lock/itfactor))-dist))
								drawTerrainTile(g, x, y, it);
					}	}
				for(double x=lock*(int)(playX/lock)+dist; x>=playX-it&&x>=0+it; x-=it)		//1-->X
					{if(x+2*it>256)x=256-2*it;
					for(double y=lock*(int)(playY/lock)+dist; y>=playY-it&&y>=0+it; y-=it){	//1-->Y
						if(y+it>256)y=256-it;
						if(y+it>256)y=256-it;
//						if(!(y>=lock/itfactor*(int)(playY/(lock/itfactor))-dist&&y<lock/itfactor*(int)(playY/(lock/itfactor))-dist&&x>=lock/itfactor*(int)(playX/(lock/itfactor))-dist&&x<lock/itfactor*(int)(playX/(lock/itfactor))-dist))
								drawTerrainTile(g, x, y, it);
					}	}
				}
				
//				it=4;
//				for(double x=0; x<=playX-it&&x<=128-it; x+=it)			//0-->X
//					for(double y=0; y<=playY-it&&y<=256-it; y+=it){	//0-->Y
//						drawTerrainTile(g, x, y, it);
//					}
//				for(double x=0; x<=playX-it&&x<=128-it; x+=it)			//0-->X
//					for(double y=256-it; y>=playY-it&&y>=0+it; y-=it){	//1-->Y
//						drawTerrainTile(g, x, y, it);
//					}
//				for(double x=128-it; x>=playX-it&&x>=0+it; x-=it)		//1-->X
//					for(double y=0; y<=playY-it&&y<=256-it; y+=it){	//0-->Y
//						drawTerrainTile(g, x, y, it);
//					}
//				for(double x=128-it; x>=playX-it&&x>=0+it; x-=it)		//1-->X
//					for(double y=256-it; y>=playY-it&&y>=0+it; y-=it){	//1-->Y
//						drawTerrainTile(g, x, y, it);
//					}
				
						
//						System.out.println(island.elevation[x   ][y   ]);

				
		
		for(int i=0; 
				i<drawList.size();
				i++){
				lines.add(new Line3d(	pos3d.rotate(new pos3d(drawList.get(i).a.x-position.x*128,
															drawList.get(i).a.y-position.y*128,
															drawList.get(i).a.z-position.z*128),gaze),
									pos3d.rotate(new pos3d(drawList.get(i).b.x-position.x*128,
															drawList.get(i).b.y-position.y*128,
															drawList.get(i).b.z-position.z*128),gaze) )  );
				if(lines.get(i).a.z<0.125&&lines.get(i).b.z<0.125){
					lines.get(i).a=null;
					lines.get(i).b=null;	
				}
				else if(lines.get(i).a.z<0.125){
					lines.get(i).a=Line3d.lerp(lines.get(i).b, 
											   lines.get(i).a, 
											  (lines.get(i).a.z-0.15)/(lines.get(i).a.z-lines.get(i).b.z));
				}
				else if(lines.get(i).b.z<0.125){
					lines.get(i).b=Line3d.lerp(lines.get(i).b, 
											   lines.get(i).a, 
											  (lines.get(i).a.z-0.015)/(lines.get(i).a.z-lines.get(i).b.z));
				}
//				else if(lines.get(i).a.z<0.125){
//					lines.get(i).a=Line3d.lerp(	  lines.get(i).a,		lines.get(i).b, 
//												
//												(lines.get(i).a.z)  /  (lines.get(i).a.z-lines.get(i).b.z)   );
//					
//				}
//				else if(lines.get(i).b.z<0.125){
//					System.out.println("num lines"+lines.size());
//					lines.get(i).b=Line3d.lerp(lines.get(i).a,lines.get(i).b, lines.get(i).a.z/(lines.get(i).a.z-lines.get(i).b.z));
//					System.out.println("new lines"+lines.size());
//					
//				}
		}
		
//		int fuzz=2;
//		
//		pos3d[] fuzzyA=new pos3d[fuzz];
//		pos3d[] fuzzyB=new pos3d[fuzz];
//		for(int s=0; s<fuzz;s++){
//			fuzzyA[s]=new pos3d(Math.random()-Math.random(),Math.random()-Math.random(),Math.random()-Math.random());
//			fuzzyB[s]=new pos3d(Math.random()-Math.random(),Math.random()-Math.random(),(Math.random()-Math.random())/16);
//		}
		
		for(int i=0; i<lines.size();i++){
			if(lines.get(i).a!=null&&lines.get(i).b!=null)
			if(lines.get(i).a.z>0.0125&&lines.get(i).b.z>0.0125)
			{
				double ahr=((((2*exposure/(lines.get(i).b.z+lines.get(i).a.z)))*0.75+groundR*.25)*skyLight)*colorMult.x;
				double gee=((((2*exposure/(lines.get(i).b.z+lines.get(i).a.z)))*0.75+groundG*.25)*skyLight)*colorMult.y;
				double bee=((((2*exposure/(lines.get(i).b.z+lines.get(i).a.z)))*0.75+groundB*.25)*skyLight)*colorMult.z;
				double alfa=((((2*exposure/(lines.get(i).b.z+lines.get(i).a.z)))*0.75+.25)*skyLight);

				if (ahr<0) ahr=0;
				if (gee<0) gee=0;
				if (bee<0) bee=0;
				if (alfa<0) alfa=0;
				
				if (ahr>255) 
					ahr=255;
				if (gee>255) 
					gee=255;
				if (bee>255) 
					bee=255;
//				if (alfa>255) 
					alfa=255;
				
			
				
//				if(/*oversample&& */(2*exposure/(lines.get(i).b.z+lines.get(i).a.z))<255)
					///g.setColor(new Color((int)(groundR*skyLight),(int)(groundG*skyLight),(int)(groundB*skyLight)));
					
					g.setColor(new Color((int)ahr,(int)gee,(int)bee,(int)alfa));
//					g.setColor(new Color((int)(255*colorMult.x),(int)(255*colorMult.y*1),(int)(255*colorMult.z*2)));
					
//				else
//					g.setColor(new Color((int)(255*skyLight),
//										 (int)(255*skyLight),
//										 (int)(255*skyLight)));
				
				
//				fuzzyA=fuzzyB;
//				
//				for(int s=0; s<fuzz;s++){
//				 	fuzzyB[s]=new pos3d(Math.random()-Math.random(),Math.random()-Math.random(),(Math.random()-Math.random())/16);
//				}
//				
//				for(int t=0; t<fuzz;t++){
//					
//					g.drawLine((int)((( FOVmod*((lines.get(i).a.x*128) +fuzzyA[t].x) / (lines.get(i).a.z+fuzzyA[t].z)))*drawWidthMult)	+gameWidth/2,
//							   (int)(( -FOVmod*((lines.get(i).a.y*128)+fuzzyA[t].y) / (lines.get(i).a.z+fuzzyA[t].z)))					+gameHeight/2,
//							   (int)((( FOVmod*((lines.get(i).b.x*128) +fuzzyB[t].x) / (lines.get(i).b.z+fuzzyB[t].z)))*drawWidthMult)	+gameWidth/2,
//							   (int)(( -FOVmod*((lines.get(i).b.y*128)+fuzzyB[t].y) / (lines.get(i).b.z+fuzzyB[t].z)))					+gameHeight/2);
//							   }
//			g.drawLine((int)(( FOVmod*(lines.get(i).a.x*128) / (lines.get(i).a.z)+Math.random())*drawWidthMult)	+gameWidth/2,
//					   (int)( -FOVmod*(lines.get(i).a.y*128) / (lines.get(i).a.z)+Math.random())					+gameHeight/2,
//					   (int)(( FOVmod*(lines.get(i).b.x*128) / (lines.get(i).b.z)+Math.random())*drawWidthMult)	+gameWidth/2,
//					   (int)( -FOVmod*(lines.get(i).b.y*128) / (lines.get(i).b.z)+Math.random())					+gameHeight/2);
////			g.setColor(g.getColor().darker());
			g.drawLine((int)(( FOVmod*(lines.get(i).a.x*128) / (lines.get(i).a.z))*drawWidthMult)	+gameWidth/2,
					   (int)( -FOVmod*(lines.get(i).a.y*128) / (lines.get(i).a.z))					+gameHeight/2,
					   (int)(( FOVmod*(lines.get(i).b.x*128) / (lines.get(i).b.z))*drawWidthMult)	+gameWidth/2,
					   (int)( -FOVmod*(lines.get(i).b.y*128) / (lines.get(i).b.z))					+gameHeight/2);
////			g.setColor(g.getColor().darker());
//			g.drawLine((int)(( FOVmod*(lines.get(i).a.x*128) / (lines.get(i).a.z)+Math.random())*drawWidthMult)	+gameWidth/2,
//					   (int)( -FOVmod*(lines.get(i).a.y*128) / (lines.get(i).a.z)+Math.random())					+gameHeight/2,
//					   (int)(( FOVmod*(lines.get(i).b.x*128) / (lines.get(i).b.z)+Math.random())*drawWidthMult)	+gameWidth/2,
//					   (int)( -FOVmod*(lines.get(i).b.y*128) / (lines.get(i).b.z)+Math.random())					+gameHeight/2);
////			g.setColor(g.getColor().darker());
//			g.drawLine((int)(( FOVmod*(lines.get(i).a.x*128) / (lines.get(i).a.z)+Math.random()*2)*drawWidthMult)	+gameWidth/2,
//					   (int)( -FOVmod*(lines.get(i).a.y*128) / (lines.get(i).a.z)+Math.random()*2)					+gameHeight/2,
//					   (int)(( FOVmod*(lines.get(i).b.x*128) / (lines.get(i).b.z)+Math.random()*2)*drawWidthMult)	+gameWidth/2,
//					   (int)( -FOVmod*(lines.get(i).b.y*128) / (lines.get(i).b.z)+Math.random()*2)					+gameHeight/2);
//			g.setColor(g.getColor().darker());
//			g.drawLine((int)(( FOVmod*(lines.get(i).a.x*128) / (lines.get(i).a.z)+Math.random()*4)*drawWidthMult)	+gameWidth/2,
//					   (int)( -FOVmod*(lines.get(i).a.y*128) / (lines.get(i).a.z)+Math.random()*4)					+gameHeight/2,
//					   (int)(( FOVmod*(lines.get(i).b.x*128) / (lines.get(i).b.z)+Math.random()*4)*drawWidthMult)	+gameWidth/2,
//					   (int)( -FOVmod*(lines.get(i).b.y*128) / (lines.get(i).b.z)+Math.random()*4)					+gameHeight/2);
			
//			g.setColor(new Color(255,255,255));
//				
//				g.fillOval((int)((( FOVmod*(lines.get(i).a.x*128) / (lines.get(i).a.z)) - FOVmod*16/(lines.get(i).a.z)) *drawWidthMult)	+gameWidth/2,
//						   (int)(((-FOVmod*(lines.get(i).a.y*128) / (lines.get(i).a.z)) - FOVmod*16/(lines.get(i).a.z)))					+gameHeight/2,
//						   (int)((FOVmod*32/ (lines.get(i).a.z))*drawWidthMult),
//						   (int)((FOVmod*32/ (lines.get(i).a.z))));
//				g.fillOval((int)((( FOVmod*(lines.get(i).b.x*128) / (lines.get(i).b.z)) - FOVmod*16/(lines.get(i).b.z)) *drawWidthMult)	+gameWidth/2,
//						   (int)(((-FOVmod*(lines.get(i).b.y*128) / (lines.get(i).b.z)) - FOVmod*16/(lines.get(i).b.z)))					+gameHeight/2,
//						   (int)((FOVmod*32/ (lines.get(i).b.z))*drawWidthMult),
//						   (int)((FOVmod*32/ (lines.get(i).b.z))));
//		if(oversample&&(1/Math.random())*1/lines.get(i).b.z>1)
//					g.drawLine((int)(( FOVmod*(lines.get(i).a.x*128+Math.random()*1024) / (lines.get(i).a.z))*drawWidthMult)	+gameWidth/2,
//							   (int)(-FOVmod*(lines.get(i).a.y*128+Math.random()*1024) / (lines.get(i).a.z))	+gameHeight/2,
//							   (int)(( FOVmod*(lines.get(i).b.x*128+Math.random()*1024) / (lines.get(i).b.z))*drawWidthMult)	+gameWidth/2,
//							   (int)(-FOVmod*(lines.get(i).b.y*128+Math.random()*1024) / (lines.get(i).b.z))	+gameHeight/2);
//				else if((1/Math.random())*4/lines.get(i).b.z>1){
//					g.drawLine((int)(( FOVmod*(lines.get(i).a.x*128+Math.random()*1) / (lines.get(i).a.z))*drawWidthMult)	+gameWidth/2,
//							   (int)(-FOVmod*(lines.get(i).a.y*128+Math.random()*1) / (lines.get(i).a.z))	+gameHeight/2,
//						       (int)(( FOVmod*(lines.get(i).b.x*128+Math.random()*1024) / (lines.get(i).b.z))*drawWidthMult)	+gameWidth/2,
//						       (int)(-FOVmod*(lines.get(i).b.y*128+Math.random()*1024) / (lines.get(i).b.z))	+gameHeight/2);
//				}
				
//				g.drawLine((int)(( FOVmod*(lines.get(i).a.x*128) / (lines.get(i).a.z))*drawWidthMult)	+gameWidth/2 +1,
//						   (int)(-FOVmod*(lines.get(i).a.y*128) / (lines.get(i).a.z))	+gameHeight/2,
//						   (int)(( FOVmod*(lines.get(i).b.x*128) / (lines.get(i).b.z))*drawWidthMult)	+gameWidth/2 +1,
//						   (int)(-FOVmod*(lines.get(i).b.y*128) / (lines.get(i).b.z))	+gameHeight/2);
				}
			
//			else if(lines.get(i).a.z>0.125)
//			{
//				g.setColor(new Color(255,64,64));
//				pos3d tempo=(Line3d.lerp(lines.get(i).a.x,lines.get(i).b.x, lines.get(i).a.z/(lines.get(i).a.z-lines.get(i).b.z));
//				g.drawLine((int)( FOVmod*(lines.get(i).a.x*128) / (lines.get(i).a.z))	+gameWidth/2,
//						   (int)(-FOVmod*(lines.get(i).a.y*128) / (lines.get(i).a.z))	+gameHeight/2,
//						   (int)( FOVmod*(0*128) / (lines.get(i).b.z))	+gameWidth/2,
//						   (int)(-FOVmod*(lines.get(i).b.y*128) / (lines.get(i).b.z))	+gameHeight/2);}
//			else if(lines.get(i).b.z>0.125)
//			{
//				g.setColor(new Color(255,64,64));
//				
//				g.drawLine((int)( FOVmod*(lines.get(i).a.x*128) / (lines.get(i).a.z))	+gameWidth/2,
//						   (int)(-FOVmod*(lines.get(i).a.y*128) / (lines.get(i).a.z))	+gameHeight/2,
//						   (int)( FOVmod*(lines.get(i).b.x*128) / (lines.get(i).b.z))	+gameWidth/2,
//						   (int)(-FOVmod*(lines.get(i).b.y*128) / (lines.get(i).b.z))	+gameHeight/2);}
				
			
		}
		
//		
//		drawPoly(new pos3d(-1,0,-1), 
//				 new pos3d(1,0,-1),
//				 new pos3d(1,0,1),
//				 new pos3d(-1,0,1), g, new Color(0,0,127));

		//island
		
		
		
//		int it=128;
//		for(int x=0; x<=256-it; x+=it)
//			for(int y=0; y<=256-it; y+=it){
////				System.out.println(island.elevation[x   ][y   ]);
//				drawPoly(new pos3d(x   -1024,island.elevation[x   ][y   ]/6 ,y   -256), 
//						 new pos3d(x+it-1024,island.elevation[x+it][y   ]/6 ,y   -256),
//						 new pos3d(x+it-1024,island.elevation[x+it][y+it]/6 ,y+it-256),
//						 new pos3d(x   -1024,island.elevation[x   ][y+it]/6 ,y+it-256), g, 
//						 new Color(((255)/6)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*2+island.layers.get(2)[x/8][y/8]/4,
//								   ((255)/3)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*4+island.layers.get(2)[x/8][y/8]/2,
//								   ((255)/4)-((255)/4)*((island.elevation[x   ][y   ]+127)/128)));
//		}it=64;
//		for(int x=0; x<=256-it; x+=it)
//			for(int y=0; y<=256-it; y+=it){
////				System.out.println(island.elevation[x   ][y   ]);
//				drawPoly(new pos3d(x   -768,island.elevation[x   ][y   ]/6 ,y   -256), 
//						 new pos3d(x+it-768,island.elevation[x+it][y   ]/6 ,y   -256),
//						 new pos3d(x+it-768,island.elevation[x+it][y+it]/6 ,y+it-256),
//						 new pos3d(x   -768,island.elevation[x   ][y+it]/6 ,y+it-256), g, 
//						 new Color(((255)/6)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*2+island.layers.get(2)[x/8][y/8]/4,
//								   ((255)/3)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*4+island.layers.get(2)[x/8][y/8]/2,
//								   ((255)/4)-((255)/4)*((island.elevation[x   ][y   ]+127)/128)));
//		} it=32;
//		for(int x=0; x<=256-it; x+=it)
//			for(int y=0; y<=256-it; y+=it){
////				System.out.println(island.elevation[x   ][y   ]);
//				drawPoly(new pos3d(x   -512,island.elevation[x   ][y   ]/6 ,y   -256), 
//						 new pos3d(x+it-512,island.elevation[x+it][y   ]/6 ,y   -256),
//						 new pos3d(x+it-512,island.elevation[x+it][y+it]/6 ,y+it-256),
//						 new pos3d(x   -512,island.elevation[x   ][y+it]/6 ,y+it-256), g, 
//						 new Color(((255)/6)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*2+island.layers.get(2)[x/8][y/8]/4,
//								   ((255)/3)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*4+island.layers.get(2)[x/8][y/8]/2,
//								   ((255)/4)-((255)/4)*((island.elevation[x   ][y   ]+127)/128)));
//		} it=16;
//		for(int x=0; x<=256-it; x+=it)
//			for(int y=0; y<=256-it; y+=it){
////				System.out.println(island.elevation[x   ][y   ]);
//				drawPoly(new pos3d(x   -256,island.elevation[x   ][y   ]/6 ,y   -256), 
//						 new pos3d(x+it-256,island.elevation[x+it][y   ]/6 ,y   -256),
//						 new pos3d(x+it-256,island.elevation[x+it][y+it]/6 ,y+it-256),
//						 new pos3d(x   -256,island.elevation[x   ][y+it]/6 ,y+it-256), g, 
//						 new Color(((255)/6)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*2+island.layers.get(2)[x/8][y/8]/4,
//								   ((255)/3)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*4+island.layers.get(2)[x/8][y/8]/2,
//								   ((255)/4)-((255)/4)*((island.elevation[x   ][y   ]+127)/128)));
//		}it=8;
//		for(int x=0; x<=256-it; x+=it)
//			for(int y=0; y<=256-it; y+=it){
////				System.out.println(island.elevation[x   ][y   ]);
//				drawPoly(new pos3d(x   ,island.elevation[x   ][y   ]/6 ,y   -256), 
//						 new pos3d(x+it,island.elevation[x+it][y   ]/6 ,y   -256),
//						 new pos3d(x+it,island.elevation[x+it][y+it]/6 ,y+it-256),
//						 new pos3d(x   ,island.elevation[x   ][y+it]/6 ,y+it-256), g, 
//						 new Color(((255)/6)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*2+island.layers.get(2)[x/8][y/8]/4,
//								   ((255)/3)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*4+island.layers.get(2)[x/8][y/8]/2,
//								   ((255)/4)-((255)/4)*((island.elevation[x   ][y   ]+127)/128)));
//		}
		
			
//		it=2;
//		for(int x=128; x<=255-it; x+=it)
//			for(int y=128; y<=255-it; y+=it){
////				System.out.println(island.elevation[x   ][y   ]);
//				Color c=Color.blue;
//				if(island.elevation[x+it/2][y+it/2]+(island.layers.get(0)[x+it/2][y+it/2]*8)>60)
//					c= new Color((int)((((255)/12)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*2+island.layers.get(2)[x/8][y/8]/4)*skyLight),
//								 (int)((((255)/6)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*4+island.layers.get(2)[x/8][y/8]/2)*skyLight),
//								 (int)((((0)/4)-((0)/4)*((island.elevation[x   ][y   ]+127)/128))*skyLight));
//				else if(island.elevation[x+it/2][y+it/2]>20)
//					c= new Color((int)((((255)/6)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*2+island.layers.get(2)[x/8][y/8]/4)*skyLight),
//								 (int)(( ((255)/3)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*4+island.layers.get(2)[x/8][y/8]/2)*skyLight),
//								 (int)((((0)/4)-((0)/4)*((island.elevation[x   ][y   ]+127)/128))*skyLight));
//				else
//					c= new Color((int)(((127)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*2+island.layers.get(2)[x/8][y/8]/4)*skyLight),
//								 (int)(((127)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*4+island.layers.get(2)[x/8][y/8]/2)*skyLight),
//								 (int)(((64)*((island.elevation[x   ][y   ]+127)/128)+island.layers.get(0)[x][y]*4+island.layers.get(2)[x/8][y/8]/2)*skyLight));
//				if(island.elevation[x   ][y   ]==0&&63+(island.terrain[x][y]*2)>63&&63+(island.terrain[x][y]*1)<256)
//					c= new Color((int)(((island.terrain[x][y]*1)/4)*skyLight),
//								 (int)(((island.terrain[x][y]*1)/2)*skyLight),
//								 (int)((63+(island.terrain[x][y]/2))*skyLight));
//				else if(island.elevation[x   ][y   ]==0)
//					c= new Color(0,
//								 0,
//								 (int)(63*skyLight));
//			
//				drawPoly(new pos3d(x   -256,(island.terrain[x   ][y   ]-90)/(8*(2-(island.elevation[x   ][y   ]+127)/128))-32,y   -256), 
//						 new pos3d(x+it-256,(island.terrain[x+it][y   ]-90)/(8*(2-(island.elevation[x+it][y   ]+127)/128))-32,y   -256),
//						 new pos3d(x+it-256,(island.terrain[x+it][y+it]-90)/(8*(2-(island.elevation[x+it][y+it]+127)/128))-32,y+it-256),
//						 new pos3d(x   -256,(island.terrain[x   ][y+it]-90)/(8*(2-(island.elevation[x   ][y+it]+127)/128))-32,y+it-256), g, 
//						 c);
//		}
//		
	
		
//		it=4;
//		for(int x=0; x<=256-it; x+=it)
//			for(int y=0; y<=256-it; y+=it){
////				System.out.println(island.elevation[x   ][y   ]);
//				Color c=Color.black;
//				if(island.terrain[x   ][y   ]-90<1){
//					c= new Color(0,0,63,63);
//					if(island.terrain[x   ][y   ]-90>-8)
//						c= new Color(127,127,255,63);
//					
//				drawPoly(new pos3d(x   -256,0,y   -256), 
//						 new pos3d(x+it-256,0,y   -256),
//						 new pos3d(x+it-256,0,y+it-256),
//						 new pos3d(x   -256,0,y+it-256), g, 
//						 c, true);
//				}
//		}
		
//		if(position.y<sealevel){
//			double color=(sealevel-position.y);
//			color=Math.sqrt(color)*64;
//			if(color>255){color=255;}
//			if(color<000){color=000;}
//			g.setColor(new Color(0,0,(int)(255-color),(int)(color)));
//			g.fillRect(0, 0, gameWidth, gameHeight);
//		}
//		
		
//		groundR=96;
//		groundG=32;
//		groundB=196;
		
		double tide=Math.sin(ticker/20)*32+32;
//		clearBG=false;
		if(position.y*8<tide)
			{g.setColor(new Color((int)(0*colorMult.x),(int)(32*colorMult.y),(int)(64*colorMult.z),(int)(-100*Math.atan((position.y*8-tide)/3))));
			g.fillRect(0, 0, gameWidth, gameHeight);
			}
		
		
//		
//		g.setColor(new Color((int)(255*colorMult.x),(int)(255*colorMult.y),(int)(255*colorMult.z)));
//		g.fillOval(gameWidth/2-48, gameHeight-96, 96, 96);
//		g.setColor(new Color((int)(64*colorMult.x),(int)(127*colorMult.y),(int)(255*colorMult.z)));
//		g.fillOval(gameWidth/2-46, gameHeight-94, 92, 92);
//		g.setColor(new Color((int)(127*colorMult.x),(int)(64*colorMult.y),(int)(0*colorMult.z)));
//		g.fillArc(gameWidth/2-46, gameHeight-94, 92, 92,180,180);
//		g.setColor(new Color((int)(127*colorMult.x),(int)(64*colorMult.y),(int)(0*colorMult.z)));
//		g.setColor(new Color((int)(255*colorMult.x),(int)(128*colorMult.y),(int)(0*colorMult.z)));
//		g.fillArc(gameWidth/2-46, gameHeight-48-(int)(46*Math.sin(Math.toRadians(-shipRot.x))), 92, (int)(92*Math.sin(Math.toRadians(-shipRot.x))),0,180);
//		g.setColor(g.getColor().darker().darker());
//		g.fillArc(gameWidth/2-46, gameHeight-48-(int)(46*Math.sin(Math.toRadians(180+shipRot.x))), 92, (int)(92*Math.sin(Math.toRadians(180+shipRot.x))),0,180);
//		g.setColor(new Color((int)(64*colorMult.x),(int)(128*colorMult.y),(int)(255*colorMult.z)).brighter());
//		g.fillArc(gameWidth/2-46, gameHeight-49-(int)(46*Math.sin(Math.toRadians(shipRot.x))), 92, (int)(92*Math.sin(Math.toRadians(shipRot.x))),180,180);
//		g.setColor(g.getColor().darker().darker());
//		g.fillArc(gameWidth/2-46, gameHeight-49-(int)(46*Math.sin(Math.toRadians(180-shipRot.x))), 92, (int)(92*Math.sin(Math.toRadians(180-shipRot.x))),180,180);
//		g.setColor(Color.black);
//		g.drawOval((int)(gameWidth-(48*Math.sqrt(3)))/2, gameHeight-48+(int)(46*Math.sin(Math.toRadians(shipRot.x+22.5))), (int)(48*Math.sqrt(3)), (int)(46*Math.cos(Math.toRadians(shipRot.x+90-22.5)))-(int)(46*Math.cos(Math.toRadians(shipRot.x-90+22.5))));
//		g.drawOval((int)(gameWidth-(48*Math.sqrt(2)))/2, gameHeight-48+(int)(46*Math.sin(Math.toRadians(shipRot.x+45))), (int)(48*Math.sqrt(2)), (int)(46*Math.cos(Math.toRadians(shipRot.x+45)))-(int)(46*Math.cos(Math.toRadians(shipRot.x-45))));
//		g.drawOval((int)(gameWidth-(48*Math.sqrt(3)/2))/2, gameHeight-48+(int)(46*Math.sin(Math.toRadians(shipRot.x+90-22.5))), (int)(39), (int)(46*Math.cos(Math.toRadians(shipRot.x+22.5)))-(int)(46*Math.cos(Math.toRadians(shipRot.x-22.5))));
//		g.fillOval((gameWidth-4)/2, gameHeight-48+(int)(46*Math.sin(Math.toRadians(shipRot.x+89))), 4, (int)(46*Math.cos(Math.toRadians(shipRot.x+2)))-(int)(46*Math.cos(Math.toRadians(shipRot.x-2))));
//		
//		g.setColor(new Color((int)(255*colorMult.x),(int)(255*colorMult.y),(int)(255*colorMult.z)));
//		g.drawOval((int)(gameWidth-(48*+Math.sqrt(3)))/2, gameHeight-48-(int)(46*Math.sin(Math.toRadians(180-shipRot.x-22.5))), (int)(48*Math.sqrt(3)), (int)(46*Math.cos(Math.toRadians(180-shipRot.x-90+22.5)))-(int)(46*Math.cos(Math.toRadians(180-shipRot.x+90-22.5))));
//		g.drawOval((int)(gameWidth-(48*+Math.sqrt(2)))/2, gameHeight-48-(int)(46*Math.sin(Math.toRadians(180-shipRot.x-45))), (int)(48*Math.sqrt(2)), (int)(46*Math.cos(Math.toRadians(180-shipRot.x-45)))-(int)(46*Math.cos(Math.toRadians(180-shipRot.x+45))));
//		g.drawOval((int)(gameWidth-(48*+Math.sqrt(3)/2))/2, gameHeight-48-(int)(46*Math.sin(Math.toRadians(180-shipRot.x-90+22.5))), (int)(39), (int)(46*Math.cos(Math.toRadians(180-shipRot.x-22.5)))-(int)(46*Math.cos(Math.toRadians(180-shipRot.x+22.5))));
//		g.fillOval((gameWidth-4)/2, gameHeight-48+(int)(46*Math.sin(Math.toRadians(180-shipRot.x+89))), 4, (int)(46*Math.cos(Math.toRadians(180-shipRot.x-2)))-(int)(46*Math.cos(Math.toRadians(180-shipRot.x+2))));
//		
			
		g.setColor(new Color((int)(255*colorMult.x),(int)(255*colorMult.y),(int)(0*colorMult.z)));
		int[] xlist={-10+gameWidth/2,-3+gameWidth/2,0+gameWidth/2,3+gameWidth/2,10+gameWidth/2};
		int[] ylist={-2+gameHeight-48,-2+gameHeight-48,3+gameHeight-48,0-2+gameHeight-48,0-2+gameHeight-48};
		g.drawPolygon(xlist,ylist, 5);
		
		g.setColor(new Color((int)(255*colorMult.x),(int)(255*colorMult.y),(int)(255*colorMult.z)));
		if(ticker<0){
			g.drawString("Washed Ashore     v13.1.1       -96fps"	,64, 64);
			g.drawString("  Arrow keys to look around"		 		,64, 64+16);
			g.drawString("  WASD keys to move" 						,64, 64+32);
			g.drawString("  Shift to move up"						,64, 64+48);
			g.drawString("  Ctrl to move down"						,64, 64+64);
			//g.drawString("  CTRL to crouch"		 				,64, 64+80);
			}
		else
			g.drawString("3d animation testing, WIP enterprise model -96fps", 16, gameHeight-16);
		
		g.drawString("shipX="+shipPos.x/128+";Y="+shipPos.y/128+";Z="+shipPos.z/128+";", 16, 16);
		
		
		if(!running){	g.setColor(Color.BLACK);
		
			for(int x=0; x<2; x++)
			{g.drawString("PAUSED", gameWidth/2, gameHeight/2);
//			g.drawString("  WASD[shift+crt] keys to move [up/down]" 						,32+x, 64+32+x);
//			g.drawString("  IK,JL,UO keys to turn/look around" 						,32+x, 64+64+x);
//			g.drawString("  </> to adjust moving speed" 						,32+x, 64+96+x);
//			g.drawString("  T to auto-target camera" 						,32+x, 64+128+x);
//			g.drawString("  7/8 to adjust vector \"exposure\"" 						,32+x, 64+128+32+x);
//			g.drawString("  +/- to adjust FOV (zoom)" 						,32+x, 64+128+64+x);
//			g.drawString("  2 to spawn ship" 						,32+x, 64+128+96+x);
//			g.drawString("  3 to regen world" 						,32+x, 64+128+128+x);
//			g.drawString("  T to auto-target camera" 						,32+x, 64+256+32+x);
//			g.drawString("  ESC to unpause/hide this" 						,32+x, 64+256+64+x);
			
			g.setColor(new Color((int)(255*colorMult.x),(int)(255*colorMult.y),(int)(255*colorMult.z)));
			}
		}
		int z=8;
//		for(int x=-0; x<256;x+=z)
//			for(int y=0; y<256;y+=z){
//				g.setColor(new Color((int)((32*(island.terrain[(int)x][(int)y])))/32));
//				g.fillRect(x,y,z,z);}
		double tempx=128+(shipPos.x/128);
		double tempy=128-(shipPos.z/128);
		if(tempx<0){tempx=0;}
		if(tempx>255){tempx=0;}
		if(tempy<0){tempy=0;}
		if(tempy>255){tempy=255;}
		
//		g.setColor(Color.RED);
//		g.fillOval((int)tempx-2, (int)tempy-2, 4, 4);
		
		LCam=!LCam;
	}
	public void mouseClicked(MouseEvent e)
	{	
		if(running)mouselock=true;
		n=new NoiseMap();
		repaint();
	}

	@Override
	
	
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
	
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_W)
		{
			keyW=true;
		}
		if(e.getKeyCode()==KeyEvent.VK_A)
		{
			keyA=true;
		}
		if(e.getKeyCode()==KeyEvent.VK_S)
		{
			keyS=true;
		}
		if(e.getKeyCode()==KeyEvent.VK_D)
		{
			keyD=true;
		}if(e.getKeyCode()==KeyEvent.VK_SHIFT)
		{
			keyShift=true;
		}if(e.getKeyCode()==KeyEvent.VK_SPACE)
		{
			keySpace=true;
		}
		if(e.getKeyCode()==KeyEvent.VK_CONTROL)
		{
			keyCtrl=true;
		}
		if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
		{
			mouselock=false;
			running=!running;
			if(running)mouselock=true;
		}
		
		if(e.getKeyCode()==KeyEvent.VK_R)
		{
			drift=new pos3d(0,0,0);
			position=new pos3d(0,32+1,5);
			gaze=new pos3d(0,180,0);
			
			
			shipRot.x=0;
			shipRot.y=0;
			shipRot.z=0;
			shipPos.x=0;
			shipPos.y=32;
			shipPos.z=0;

//			shipVel=0;
			
		}
		
		if(e.getKeyCode()==KeyEvent.VK_I)
		{
			keyUp=true;
		}
		if(e.getKeyCode()==KeyEvent.VK_K)
		{
			keyDown=true;
		}
		if(e.getKeyCode()==KeyEvent.VK_T)
		{
			trackShip=!trackShip;
		}
		if(e.getKeyCode()==KeyEvent.VK_J)
		{
			keyLeft=true;
		}
		if(e.getKeyCode()==KeyEvent.VK_L)
		{
			keyRight=true;
		}
		if(e.getKeyCode()==KeyEvent.VK_O)
		{
			keyCW=true;
		}
		if(e.getKeyCode()==KeyEvent.VK_U)
		{
			keyCCW=true;
		}
		//movespeedfactor  celestialSpeed
		if(e.getKeyCode()==KeyEvent.VK_0)
		{
			celestialSpeed*=2;
//			shipVel++;
			System.out.println("wazoo");
		}
		if(e.getKeyCode()==KeyEvent.VK_9)
		{
			celestialSpeed*=0.5;

//			shipVel--;
		}
		if(e.getKeyCode()==KeyEvent.VK_7)
		{
			exposure*=0.5;
		}
		if(e.getKeyCode()==KeyEvent.VK_8)
		{
			exposure*=2;
		}
		
		if(e.getKeyCode()==KeyEvent.VK_5)
		{
			sealevel-=0.25;
		}
		if(e.getKeyCode()==KeyEvent.VK_6)
		{
			sealevel+=0.25;
		}
		if(e.getKeyCode()==KeyEvent.VK_COMMA)
		{
			movespeedfactor*=0.5;
		}
		if(e.getKeyCode()==KeyEvent.VK_PERIOD)
		{
			movespeedfactor*=2;
		}
		if(e.getKeyCode()==KeyEvent.VK_MINUS)
		{
			FOVmod*=0.8;
		}
		if(e.getKeyCode()==KeyEvent.VK_EQUALS)
		{
			FOVmod*=1.25;
		}
		
		
		if(e.getKeyCode()==KeyEvent.VK_1)
			{
			galaxy.clear();
			for(int i=0; i<32; i++){
			galaxy.add(new Spiral((int)(Math.random()*64), (int)((Math.random()*64)*1.2), (int)(Math.random()*128+128), 2, (int)(Math.random()*3+2), (int)(Math.random()*3),1,(int)(Math.random()*40-20)+(i%4)*90, galaxytilt));
			}
			
			
		}
		
		
		
		if(e.getKeyCode()==KeyEvent.VK_2){
			double temp=8;
			double detail=5.3333;
			
			detail=2;
			int ender=4;
			enterprise.clear();
//			for(int i=ender-1; i>=0; i--)
//			for(double tempX=-temp; tempX<temp; tempX+=1)
//			{for(double tempZ=-temp; tempZ<temp; tempZ+=1)
//			{
//				if(tempX<=temp*-.25||tempX>=temp*.25||tempZ<=temp*-.25||tempZ>=temp*.25||i==0)
//				{
//					System.out.println("X="+tempX+"Z="+tempZ);
//					ellipse.add(new Line3d(	new pos3d( tempX   *Math.pow( 4,i),  0, tempZ   *Math.pow( 4,i)),
//											new pos3d((tempX+1)*Math.pow( 4,i),  0, tempZ   *Math.pow( 4,i))  ));  
//					
//					ellipse.add(new Line3d(	new pos3d( tempX   *Math.pow( 4,i),  0, tempZ   *Math.pow( 4,i)),
//											new pos3d( tempX   *Math.pow( 4,i),  0,(tempZ+1)*Math.pow( 4,i))  ));   
//					
//				}
//
//				}
//			}
			
			double itter=11.25*detail;
//			itter=1;
			for(temp=0; temp<360; temp+=itter)//saucer
			{
				
				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*140,		86, //top ring
												   (Math.cos(Math.toRadians(temp)))*140),
										new pos3d( (Math.sin(Math.toRadians(temp+itter)))*140,	86, 
								                   (Math.cos(Math.toRadians(temp+itter)))*140)   ));

				
				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*105,		86, //top ring
												   (Math.cos(Math.toRadians(temp)))*105),
										new pos3d( (Math.sin(Math.toRadians(temp+itter)))*105,	86, 
								                   (Math.cos(Math.toRadians(temp+itter)))*105)   ));
				
				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*140,		86, //top surf
												   (Math.cos(Math.toRadians(temp)))*140),
						   			    new pos3d( (Math.sin(Math.toRadians(temp)))*105,	86, 
						   			    		   (Math.cos(Math.toRadians(temp)))*105)));


				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*33,		92, //top surf
												   (Math.cos(Math.toRadians(temp)))*33),
						   			    new pos3d( (Math.sin(Math.toRadians(temp)))*105,	86, 
						   			    		   (Math.cos(Math.toRadians(temp)))*105)));

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*33,		92, //top ring
												   (Math.cos(Math.toRadians(temp)))*33),
										new pos3d( (Math.sin(Math.toRadians(temp+itter)))*33,	92, 
								                   (Math.cos(Math.toRadians(temp+itter)))*33)   ));
				

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*33,		92, //top surf
												   (Math.cos(Math.toRadians(temp)))*33),
						   			    new pos3d( (Math.sin(Math.toRadians(temp)))*27,	101, 
						   			    		   (Math.cos(Math.toRadians(temp)))*27)));

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*14,		103, //top surf
												   (Math.cos(Math.toRadians(temp)))*14),
						   			    new pos3d( (Math.sin(Math.toRadians(temp)))*10,	109, 
						   			    		   (Math.cos(Math.toRadians(temp)))*10)));


				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*14,		103, //top ring
												   (Math.cos(Math.toRadians(temp)))*14),
										new pos3d( (Math.sin(Math.toRadians(temp+itter)))*14,	103, 
								                   (Math.cos(Math.toRadians(temp+itter)))*14)   ));
				

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*14,		103, //top surf
												   (Math.cos(Math.toRadians(temp)))*14),
						   			    new pos3d( (Math.sin(Math.toRadians(temp)))*27,	101, 
						   			    		   (Math.cos(Math.toRadians(temp)))*27)));

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*10,		109, //top surf
												   (Math.cos(Math.toRadians(temp)))*10),
						   			    new pos3d( (Math.sin(Math.toRadians(temp)))*6,	110, 
						   			    		   (Math.cos(Math.toRadians(temp)))*6)));

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*6,		110, //top ring
												   (Math.cos(Math.toRadians(temp)))*6),
										new pos3d( (Math.sin(Math.toRadians(temp+itter)))*6,	110, 
								                   (Math.cos(Math.toRadians(temp+itter)))*6)   ));
				

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*6,		110, //top surf
												   (Math.cos(Math.toRadians(temp)))*6),
						   			    new pos3d( 0,	113, 
						   			    		   0)));
				

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*132,	70, //low surf
												   (Math.cos(Math.toRadians(temp)))*132),
										new pos3d( (Math.sin(Math.toRadians(temp)))*118,	70, 
								                   (Math.cos(Math.toRadians(temp)))*118)   ));

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*118,	70, //low surf
												   (Math.cos(Math.toRadians(temp)))*118),
						   			    new pos3d( (Math.sin(Math.toRadians(temp)))*75,	75, 
						   			    		   (Math.cos(Math.toRadians(temp)))*75)));


				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*75,		75, //low surf
												   (Math.cos(Math.toRadians(temp)))*75),
						   			    new pos3d( (Math.sin(Math.toRadians(temp)))*52,			70, 
						   			    		   (Math.cos(Math.toRadians(temp)))*52)));


				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*52,		70, //low surf
												   (Math.cos(Math.toRadians(temp)))*52),
						   			    new pos3d( (Math.sin(Math.toRadians(temp)))*20,			55, 
						   			    		   (Math.cos(Math.toRadians(temp)))*20)));

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*20,		55, //low surf
												   (Math.cos(Math.toRadians(temp)))*20),
						   			    new pos3d( 0,										52, 
						   			    		   0)));
				
				
				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*140,		86, //vert edges
												   (Math.cos(Math.toRadians(temp)))*140),
										new pos3d( (Math.sin(Math.toRadians(temp)))*132,		70,
												   (Math.cos(Math.toRadians(temp)))*132)));
				
				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*132,		70,  //lower ring
												   (Math.cos(Math.toRadians(temp)))*132),
										new pos3d( (Math.sin(Math.toRadians(temp+itter)))*132,	70, 
												   (Math.cos(Math.toRadians(temp+itter)))*132)   ));


			}
			itter=22.5*detail;
//			itter=1;
			for(temp=0; temp<360; temp+=itter) //enginering hull
			{

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*24,		 //1 ring
												   (Math.cos(Math.toRadians(temp)))*24, 90),
										new pos3d( (Math.sin(Math.toRadians(temp+itter)))*24, 
								                   (Math.cos(Math.toRadians(temp+itter)))*24,	90)   ));

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*24,		 //1-2 surf
												   (Math.cos(Math.toRadians(temp)))*24, 90),
										new pos3d( (Math.sin(Math.toRadians(temp)))*32, 
								                   (Math.cos(Math.toRadians(temp)))*32,	106)   ));

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*32,		 //2 ring
												   (Math.cos(Math.toRadians(temp)))*32, 106),
										new pos3d( (Math.sin(Math.toRadians(temp+itter)))*32, 
								                   (Math.cos(Math.toRadians(temp+itter)))*32,	106)   ));
				
				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*32,		 //2-3 surf
												   (Math.cos(Math.toRadians(temp)))*32, 106),
										new pos3d( (Math.sin(Math.toRadians(temp)))*36, 
								                   (Math.cos(Math.toRadians(temp)))*36,	174)   ));

		
				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*36,		 //3 ring
												   (Math.cos(Math.toRadians(temp)))*36, 174),
										new pos3d( (Math.sin(Math.toRadians(temp+itter)))*36, 
								                   (Math.cos(Math.toRadians(temp+itter)))*36,	174)   ));
				
				
				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*36,		 //3-4 surf
												   (Math.cos(Math.toRadians(temp)))*36, 174),
										new pos3d( (Math.sin(Math.toRadians(temp)))*34, 
												   (Math.cos(Math.toRadians(temp)))*34-1,	200 +5*Math.atan(Math.cos(Math.toRadians(temp      ))*8))   ));


//				ellipse.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*34, 
//		                   						   (Math.cos(Math.toRadians(temp)))*34-1,	200 +5*Math.atan(Math.cos(Math.toRadians(temp      ))*8)),
//		                   				new pos3d( (Math.sin(Math.toRadians(temp+itter)))*34, 
//		                   						   (Math.cos(Math.toRadians(temp+itter)))*34-1,	200 +5*Math.atan(Math.cos(Math.toRadians(temp+itter))*8))   ));

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*26,		 //5 ring
												   (Math.cos(Math.toRadians(temp)))*26-4,         280 +40*Math.atan(Math.cos(Math.toRadians(temp      ))*8)  ),
										new pos3d( (Math.sin(Math.toRadians(temp)))*34, 
								                   (Math.cos(Math.toRadians(temp)))*34-1,	200 +5*Math.atan(Math.cos(Math.toRadians(temp      ))*8))   ));



				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*26,		 //5 ring
												   (Math.cos(Math.toRadians(temp)))*26-4,         280 +40*Math.atan(Math.cos(Math.toRadians(temp      ))*8)  ),
										new pos3d( (Math.sin(Math.toRadians(temp+itter)))*26, 
								                   (Math.cos(Math.toRadians(temp+itter)))*26-4,	280 +40*Math.atan(Math.cos(Math.toRadians(temp+itter))*8)  )));

				enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*26,		 //5 ring
												   (Math.cos(Math.toRadians(temp)))*26-4,         280 +40*Math.atan(Math.cos(Math.toRadians(temp      ))*8)  ),
										new pos3d( (Math.sin(Math.toRadians(360-temp)))*26, 
								                   (Math.cos(Math.toRadians(360-temp)))*26-4,	280 +40*Math.atan(Math.cos(Math.toRadians(360-temp))*8)  )));
				

//				ellipse.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*26,		 //5 ring
//						   						   (Math.cos(Math.toRadians(temp)))*26-4,         270 +40*Math.atan(Math.cos(Math.toRadians(temp      ))*8)  ),
//										new pos3d( (Math.sin(Math.toRadians(temp)))*10, 
//								                   (Math.cos(Math.toRadians(temp)))*10,	330)   ));


//				ellipse.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*10,		 //6 ring
//												   (Math.cos(Math.toRadians(temp)))*10, 330),
//										new pos3d( (Math.sin(Math.toRadians(temp+itter)))*10, 
//								                   (Math.cos(Math.toRadians(temp+itter)))*10,	330)   ));
				
				
			}
			itter=22.5*detail;

//			itter=1;
			for(int i=-1; i<=1; i+=2)
				for(temp=0; temp<360; temp+=itter)//warp nacelle
			{

					enterprise.add(new Line3d(	new pos3d( 100*i,		 //1 ring
													   100, 144),
											new pos3d( (Math.sin(Math.toRadians(temp)))*16*Math.sqrt(2)/2+100*i, 
									                   (Math.cos(Math.toRadians(temp)))*16*Math.sqrt(2)/2+100,	160-16*Math.sqrt(2)/2)   ));

					enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*16*Math.sqrt(2)/2 +100*i,		 //1 ring
													   (Math.cos(Math.toRadians(temp)))*16*Math.sqrt(2)/2 +100  , 160-16*Math.sqrt(2)/2),
											new pos3d( (Math.sin(Math.toRadians(temp)))*16 +100*i, 
									                   (Math.cos(Math.toRadians(temp)))*16 +100  ,	160)   ));

					enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*16*Math.sqrt(2)/2 +100*i,		 //1 ring
													   (Math.cos(Math.toRadians(temp)))*16*Math.sqrt(2)/2 +100  , 160-16*Math.sqrt(2)/2),
											new pos3d( (Math.sin(Math.toRadians(temp+itter)))*16*Math.sqrt(2)/2 +100*i,		 //1 ring
													   (Math.cos(Math.toRadians(temp+itter)))*16*Math.sqrt(2)/2 +100  , 160-16*Math.sqrt(2)/2)   ));

					enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*16 +100*i,		 //1 ring
													   (Math.cos(Math.toRadians(temp)))*16+100, 160),
											new pos3d( (Math.sin(Math.toRadians(temp+itter)))*16 +100*i, 
									                   (Math.cos(Math.toRadians(temp+itter)))*16+100,	160)   ));

					enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*16 +100*i,		 //1 ring
													   (Math.cos(Math.toRadians(temp)))*16 +100  , 160),
											new pos3d( (Math.sin(Math.toRadians(temp)))*16 +100*i, 
									                   (Math.cos(Math.toRadians(temp)))*16 +100  ,	460)   ));

					enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*16 +100*i,		 //1 ring
											    	   (Math.cos(Math.toRadians(temp)))*16+100, 460),
										    new pos3d( (Math.sin(Math.toRadians(temp+itter)))*16 +100*i, 
										    		   (Math.cos(Math.toRadians(temp+itter)))*16+100,	460)   ));

					enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*16 +100*i,		 //1 ring
										    		   (Math.cos(Math.toRadians(temp)))*16+100, 460),
									     	new pos3d( (Math.sin(Math.toRadians(temp+itter)))*16 +100*i, 
								                       (Math.cos(Math.toRadians(temp+itter)))*16+100,	460)   ));


					enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*16+100*i,		 //5 ring
													   (Math.cos(Math.toRadians(temp)))*16+100  ,         480 +8*Math.atan(Math.cos(Math.toRadians(temp      ))*8)  ),
											new pos3d( (Math.sin(Math.toRadians(temp)))*16+100*i, 
									                   (Math.cos(Math.toRadians(temp)))*16+100  ,	460)   ));

					enterprise.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*16+100*i,		 //5 ring
													   (Math.cos(Math.toRadians(temp)))*16+100  ,         480 +8*Math.atan(Math.cos(Math.toRadians(temp))*8)  ),
										    new pos3d( (Math.sin(Math.toRadians(temp+itter)))*16+100*i,		 //5 ring
													   (Math.cos(Math.toRadians(temp+itter)))*16+100  ,    480 +8*Math.atan(Math.cos(Math.toRadians(temp+itter))*8)  ) ));
			}
			{//pylons
				//=="neck"
				enterprise.add(new Line3d(	new pos3d( 4,		 //5 ring
												   70  ,     132  ),
										new pos3d( 4, 
												   30,	164)   ));



				enterprise.add(new Line3d(	new pos3d( -4,		 //5 ring
												   70  ,        132  ),
										new pos3d( -4, 
												   30,	164)   ));


				enterprise.add(new Line3d(	new pos3d( 4,		 //5 ring
												   70  ,     54  ),
										new pos3d( 4, 
												   30,	110)   ));

				enterprise.add(new Line3d(	new pos3d( -4,		 //5 ring
												   70  ,     54  ),
										new pos3d( -4, 
												   30,	110)   ));

				
				//====="warp engines pylons"
				enterprise.add(new Line3d(	new pos3d( 23,		 //5 ring
												   17  ,     230  ),
										new pos3d( 87, 
												   90,	230)   ));

				enterprise.add(new Line3d(	new pos3d( 23,		 //5 ring
												   17  ,     256  ),
										new pos3d( 87, 
												   90,	256)   ));



				enterprise.add(new Line3d(	new pos3d( 27,		 //5 ring
												   13  ,        230  ),
										new pos3d( 91, 
												   86,	230)   ));

				enterprise.add(new Line3d(	new pos3d( 27,		 //5 ring
												   13  ,        256  ),
										new pos3d( 91, 
												   86,	256)   ));

				//====="warp engines pylons"
				enterprise.add(new Line3d(	new pos3d(-23,		 //5 ring
												   17  ,     230  ),
										new pos3d(-87, 
												   90,	230)   ));

				enterprise.add(new Line3d(	new pos3d(-23,		 //5 ring
												   17  ,     256  ),
										new pos3d(-87, 
												   90,	256)   ));



				enterprise.add(new Line3d(	new pos3d(-27,		 //5 ring
												   13  ,        230  ),
										new pos3d(-91, 
												   86,	230)   ));

				enterprise.add(new Line3d(	new pos3d(-27,		 //5 ring
												   13  ,        256  ),
										new pos3d(-91, 
												   86,	256)   ));

//
//				ellipse.add(new Line3d(	new pos3d( 4,		 //5 ring
//												   70  ,     54  ),
//										new pos3d( 4, 
//												   30,	110)   ));
//
//				ellipse.add(new Line3d(	new pos3d( -4,		 //5 ring
//												   70  ,     54  ),
//										new pos3d( -4, 
//												   30,	110)   ));

			}
//			for(temp=0; temp<360;temp++)
//				for(double temp2=0; temp2<360;temp2++)
//					sky.add(new Line3d(	new pos3d( (Math.sin(Math.toRadians(temp)))*16*Math.sqrt(2)/2 +100,		 //1 ring
//											           (Math.cos(Math.toRadians(temp)))*16*Math.sqrt(2)/2 +100  , 160-16*Math.sqrt(2)/2),
//											new pos3d( (Math.sin(Math.toRadians(temp)))*16 -100, 
//													   (Math.cos(Math.toRadians(temp)))*16 -100  ,	160)   ));

		}
		
		if(e.getKeyCode()==KeyEvent.VK_3)
		{
		in3dmode=!in3dmode;
		}
		if(e.getKeyCode()==KeyEvent.VK_4)
		{
		island=new World();
		}
		
		
//		if(e.getKeyCode()==KeyEvent.VK_Y)
//		{
//			oversample=!oversample;
//			clearBG=true;
//		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		if(e.getKeyCode()==KeyEvent.VK_W)
		{
			keyW=false;
		}
		if(e.getKeyCode()==KeyEvent.VK_A)
		{
			keyA=false;
}
		if(e.getKeyCode()==KeyEvent.VK_S)
		{
			keyS=false;
		}
		if(e.getKeyCode()==KeyEvent.VK_D)
		{
			keyD=false;
		}
		if(e.getKeyCode()==KeyEvent.VK_SHIFT)
		{
			keyShift=false;
		}if(e.getKeyCode()==KeyEvent.VK_SPACE)
		{
			keySpace=false;
		}
		if(e.getKeyCode()==KeyEvent.VK_CONTROL)
		{
			keyCtrl=false;
		}
		
		
		if(e.getKeyCode()==KeyEvent.VK_I)
		{
			keyUp=false;
		}
		if(e.getKeyCode()==KeyEvent.VK_K)
		{
			keyDown=false;
		}
		if(e.getKeyCode()==KeyEvent.VK_J)
		{
			keyLeft=false;
		}
		if(e.getKeyCode()==KeyEvent.VK_L)
		{
			keyRight=false;
		}
		if(e.getKeyCode()==KeyEvent.VK_O)
		{
			keyCW=false;
		}
		if(e.getKeyCode()==KeyEvent.VK_U)
		{
			keyCCW=false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		}

	public void drawPoly(pos3d e1, pos3d e2, pos3d e3, pos3d e4, Graphics g, Color c, boolean l){
		g.setColor(c);
		e1.x-=position.x;
		e1.y-=position.y;
		e1.z+=position.z;
		
		e2.x-=position.x;
		e2.y-=position.y;
		e2.z+=position.z;
		
		e3.x-=position.x;
		e3.y-=position.y;
		e3.z+=position.z;
		
		e4.x-=position.x;
		e4.y-=position.y;
		e4.z+=position.z;

		e1.x=-e1.x;
		e1.y=+e1.y;
		e1.z=-e1.z;
		
		e2.x=-e2.x;
		e2.y=+e2.y;
		e2.z=-e2.z;
		
		e3.x=-e3.x;
		e3.y=+e3.y;
		e3.z=-e3.z;
		
		e4.x=-e4.x;
		e4.y=+e4.y;
		e4.z=-e4.z;
		
		e1=pos3d.rotate(e1, new pos3d(gaze.x,-gaze.y,-gaze.z));
		e2=pos3d.rotate(e2, new pos3d(gaze.x,-gaze.y,-gaze.z));
		e3=pos3d.rotate(e3, new pos3d(gaze.x,-gaze.y,-gaze.z));
		e4=pos3d.rotate(e4, new pos3d(gaze.x,-gaze.y,-gaze.z));
		
		//System.out.println("BEEP BEEP BEEP"+e1.x+","+e1.y+","+e1.z);
		if (e1.z>0.125&&e2.z>0.125&&e3.z>0.125&&e4.z>0.125)
		{
//			int[] x={(int)(( -FOVmod*(e1.x*128) / (e1.z))*drawWidthMult)	+gameWidth/2+(int)(Math.random()*8-4),
//					 (int)(( -FOVmod*(e2.x*128) / (e2.z))*drawWidthMult)	+gameWidth/2+(int)(Math.random()*8-4),
//					 (int)(( -FOVmod*(e3.x*128) / (e3.z))*drawWidthMult)	+gameWidth/2+(int)(Math.random()*8-4),
//					 (int)(( -FOVmod*(e4.x*128) / (e4.z))*drawWidthMult)	+gameWidth/2+(int)(Math.random()*8-4),}; 
//			int[] y={(int)(( -FOVmod*(e1.y*128) / (e1.z)))				+gameHeight/2+(int)(Math.random()*8-4),
//					 (int)(( -FOVmod*(e2.y*128) / (e2.z)))			+gameHeight/2+(int)(Math.random()*8-4),
//					 (int)(( -FOVmod*(e3.y*128) / (e3.z)))			+gameHeight/2+(int)(Math.random()*8-4),
//					 (int)(( -FOVmod*(e4.y*128) / (e4.z)))			+gameHeight/2+(int)(Math.random()*8-4),}; 
			int i=4;
////			g.fillPolygon(x, y, i);
//		if (l){
//			g.setColor(c.darker().darker());
//			g.drawPolygon(x, y, i);
//			}
		
		int[]f={(int)(( -FOVmod*(e1.x*128) / (e1.z))*drawWidthMult)	+gameWidth/2,
					 (int)(( -FOVmod*(e2.x*128) / (e2.z))*drawWidthMult)	+gameWidth/2,
					 (int)(( -FOVmod*(e3.x*128) / (e3.z))*drawWidthMult)	+gameWidth/2,
					 (int)(( -FOVmod*(e4.x*128) / (e4.z))*drawWidthMult)	+gameWidth/2,}; 
		int[]v={(int)(( -FOVmod*(e1.y*128) / (e1.z)))				+gameHeight/2,
					 (int)(( -FOVmod*(e2.y*128) / (e2.z)))			+gameHeight/2,
					 (int)(( -FOVmod*(e3.y*128) / (e3.z)))			+gameHeight/2,
					 (int)(( -FOVmod*(e4.y*128) / (e4.z)))			+gameHeight/2,}; 
			i=4;
			
			g.setColor(new Color((int)(c.getRed()*colorMult.x),(int)(c.getGreen()*colorMult.y),(int)(c.getBlue()*colorMult.z)));
			
			g.fillPolygon(f, v, i);
		if (l){
//			g.setColor(c);
			g.setColor(c.darker().darker());
			g.drawPolygon(f, v, i);
			}
		}
	}
	
	public void drawTerrainTile(Graphics g, double x, double y, double it){
		double shiny=0;
		double ahr;
		double gee;
		double bee;
		double ahr2;
		double gee2;
		double bee2;
		double alfa=256;
		boolean l=true;
		
		double a=island.terrain[(int)(x   )][(int)(y   )]-90, 
			   b=island.terrain[(int)(x+it)][(int)(y   )]-90,
			   c=island.terrain[(int)(x+it)][(int)(y+it)]-90,
			   d=island.terrain[(int)(x   )][(int)(y+it)]-90;
	
		double a2=0;
		double b2=0;
		double c2=0;
		double d2=0;
		
//		double a1=island.terrain[(int)(x   )][(int)(y   )], 
//		       b1=island.terrain[(int)(x+it)][(int)(y   )],
//		       c1=island.terrain[(int)(x+it)][(int)(y+it)],
//		       d1=island.terrain[(int)(x   )][(int)(y+it)];
//
//		double a2=island.terrain[(int)(x   )][(int)(y   )], 
//		       b2=island.terrain[(int)(x+it)][(int)(y   )],
//		       c2=island.terrain[(int)(x+it)][(int)(y+it)],
//		       d2=island.terrain[(int)(x   )][(int)(y+it)];
//		
//		double a3=island.terrain[(int)(x   )][(int)(y   )], 
//		       b3=island.terrain[(int)(x+it)][(int)(y   )],
//		       c3=island.terrain[(int)(x+it)][(int)(y+it)],
//		       d3=island.terrain[(int)(x   )][(int)(y+it)];
//		
//		double a4=island.terrain[(int)(x   )][(int)(y   )], 
//		       b4=island.terrain[(int)(x+it)][(int)(y   )],
//		       c4=island.terrain[(int)(x+it)][(int)(y+it)],
//		       d4=island.terrain[(int)(x   )][(int)(y+it)];

//		a-=90;
//		b-=90;
//		c-=90;
//		d-=90;
		if (a<0)
			a=4*-Math.sqrt(Math.abs(a));
		if (b<0)
			b=4*-Math.sqrt(Math.abs(b));
		if (c<0)
			c=4*-Math.sqrt(Math.abs(c));
		if (d<0)
			d=4*-Math.sqrt(Math.abs(d));
		
		if(island.elevation[(int)(x+it/2)][(int)(y+it/2)]+(island.layers.get(0)[(int)(x+it/2)][(int)(y+it/2)]*8)>60)
		{ //Forest
			ahr=((((255)/12     )*((island.elevation[(int)x   ][(int)y   ]+127)/128)+island.layers.get(0)[(int)x][(int)y]*2+island.layers.get(2)[(int)x/8][(int)y/8]/4)*skyLight);
			gee=((((255)/ 6     )*((island.elevation[(int)x   ][(int)y   ]+127)/128)+island.layers.get(0)[(int)x][(int)y]*4+island.layers.get(2)[(int)x/8][(int)y/8]/2)*skyLight);
			bee=((((0)/4)-((0)/4)*((island.elevation[(int)x   ][(int)y   ]+127)/128))*skyLight);
			shiny=.6;
		}
		else if(island.elevation[(int)(x+it/2)][(int)(y+it/2)]>20)
		{ //grass
			ahr=((((255)/6)*((island.elevation[(int)x   ][(int)y   ]+127)/128)+island.layers.get(0)[(int)x][(int)y]*2+island.layers.get(2)[(int)x/8][(int)y/8]/4)*skyLight);
			gee=(( ((255)/3)*((island.elevation[(int)x   ][(int)y   ]+127)/128)+island.layers.get(0)[(int)x][(int)y]*4+island.layers.get(2)[(int)x/8][(int)y/8]/2)*skyLight);
			bee=((((0)/4)-((0)/4)*((island.elevation[(int)x   ][(int)y   ]+127)/128))*skyLight);
			shiny=1;
		}
		else if(island.elevation[(int)(x+it/2)][(int)(y+it/2)]>0)
		{ //beach
			ahr=(((127)+island.layers.get(0)[(int)x][(int)y]*2+island.layers.get(2)[(int)x/8][(int)y/8]/4)*skyLight);
			gee=(((127)+island.layers.get(0)[(int)x][(int)y]*4+island.layers.get(2)[(int)x/8][(int)y/8]/2)*skyLight);
			bee=((( 64)+island.layers.get(0)[(int)x][(int)y]*4+island.layers.get(2)[(int)x/8][(int)y/8]/2)*skyLight);
			shiny=1.5;
//			if(a<0)
//				a=0;
//			if(b<0)
//				b=0;
//			if(c<0)
//				c=0;
//			if(d<0)
//				d=0;
			
		}
		else if(island.elevation[(int)x   ][(int)y   ]==0&&island.elevation[(int)x   ][(int)y   ]==0&&63+(island.terrain[(int)x][(int)y]*2)>63&&63+(island.terrain[(int)x][(int)y]*1)<256)
		{ //shallow water-->wet sand
			double wet=(16+island.terrain[(int)x][(int)y])/128;
			ahr=(((127*wet)+island.layers.get(0)[(int)x][(int)y]*2+island.layers.get(2)[(int)x/8][(int)y/8]/4)*skyLight);
			gee=(((127*wet)+island.layers.get(0)[(int)x][(int)y]*4+island.layers.get(2)[(int)x/8][(int)y/8]/2)*skyLight);
			bee=((( 64*wet)+island.layers.get(0)[(int)x][(int)y]*4+island.layers.get(2)[(int)x/8][(int)y/8]/2)*skyLight);
			
//			ahr=(((island.terrain[(int)x][(int)y]*0.8+10)/4)*skyLight);
//			gee=(((island.terrain[(int)x][(int)y]*0.8+10)/2)*skyLight);
//			bee=((63+(island.terrain[(int)x][(int)y]/2))*skyLight);
			shiny=0.1;
			alfa=island.terrain[(int)x][(int)y]*5-64;
			l=false;
//			if(a>0)
//				a=0;
//			if(b>0)
//				b=0;
//			if(c>0)
//				c=0;
//			if(d>0)
//				d=0;
			
		}
		else if(island.elevation[(int)x   ][(int)y   ]==0)
		{ //ocean
			ahr=0;
			gee=0;
			bee=127;
			shiny=0;
			alfa=0;
			l=false;
		}
		else
		{ //other
			ahr=(((127)+island.layers.get(0)[(int)x][(int)y]*2+island.layers.get(2)[(int)x/8][(int)y/8]/4)*skyLight);
			gee=(((127)+island.layers.get(0)[(int)x][(int)y]*4+island.layers.get(2)[(int)x/8][(int)y/8]/2)*skyLight);
			bee=((( 64)+island.layers.get(0)[(int)x][(int)y]*4+island.layers.get(2)[(int)x/8][(int)y/8]/2)*skyLight);
			shiny=0;
			alfa=0;
			l=true;
//			if(a<0)
//				a=0;
//			if(b<0)
//				b=0;
//			if(c<0)
//				c=0;
//			if(d<0)
//				d=0;
			
		}
		
		
		
//		if(x>it&&y>it){
////		if(a<0&&(island.terrain[(int)(x   )][(int)(y   )]>0
////			   ||island.terrain[(int)(x-it)][(int)(y   )]>0
////		       ||island.terrain[(int)(x-it)][(int)(y-it)]>0
////			   ||island.terrain[(int)(x   )][(int)(y-it)]>0))
////			a=0;
////		if(b<0&&(island.terrain[(int)(x+it)][(int)(y   )]>0
////			   ||island.terrain[(int)(x   )][(int)(y   )]>0
////			   ||island.terrain[(int)(x   )][(int)(y-it)]>0
////			   ||island.terrain[(int)(x+it)][(int)(y-it)]>0))
////			b=0;
//		if(c<0==(island.terrain[(int)(x+it)][(int)(y+it)]>0
//			   ||island.terrain[(int)(x   )][(int)(y+it)]>0
//			   ||island.terrain[(int)(x   )][(int)(y   )]>0
//			   ||island.terrain[(int)(x+it)][(int)(y   )]>0))
//			c=0;
//		
////		if(d<0&&(island.terrain[(int)(x   )][(int)(y+it)]>0
////			   ||island.terrain[(int)(x-it)][(int)(y+it)]>0
////		       ||island.terrain[(int)(x-it)][(int)(y   )]>0
////			   ||island.terrain[(int)(x   )][(int)(y   )]>0))
////			d=0;
//		}
//		
		ahr*=1.5;
		gee*=1.5;
		bee*=1.5;
		ahr+=(island.terrain[(int)(x+it)][(int) y    ]-island.terrain[(int) x    ][(int)(y+it)])*shiny/it*skyLight*3;
		gee+=(island.terrain[(int)(x+it)][(int) y    ]-island.terrain[(int) x    ][(int)(y+it)])*shiny/it*skyLight*3;
		bee+=(island.terrain[(int)(x+it)][(int) y    ]-island.terrain[(int) x    ][(int)(y+it)])*shiny/it*skyLight*3;
	double fact=0;
//		if(a<0||b<0||c<0||d<0)

	ahr2=ahr;
	gee2=gee;
	bee2=bee;
	 a2=a;
	 b2=b;
	 c2=c;
	 d2=d;
	{
			
			double wave=1;
			double tide=Math.sin(ticker/20)*32+32;
			if(a2< Math.sin((ticker/4+x+y-2*it)/2)*wave+tide){
				fact+=0.25;
				 a2=Math.sin((ticker/4+x+y-2*it)/2)*wave+tide;}
			if(b2< Math.sin((ticker/4+x+y-it)/2)*wave+tide){
				fact+=0.25;
				 b2=Math.sin((ticker/4+x+y-it)/2)*wave+tide;}
			if(c2< Math.sin((ticker/4+x+y+00)/2)*wave+tide){
				fact+=0.25;
				 c2=Math.sin((ticker/4+x+y+00)/2)*wave+tide;}
			if(d2< Math.sin((ticker/4+x+y-it)/2)*wave+tide){
				fact+=0.25;
				 d2=Math.sin((ticker/4+x+y-it)/2)*wave+tide;}
			
//			if(a>0)fact-=0.25;
//			if(b>0)fact-=0.25;
//			if(c>0)fact-=0.25;
//			if(d>0)fact-=0.25;
			
			if(fact>=.5)
//			{
//				ahr=48+Math.sin((ticker/4+x+y-it)/2)*6;
//				gee=48+island.terrain[(int)x][(int)y]-(32+Math.sin((ticker/4+x+y-it)*12));
//				bee=127+island.terrain[(int)x][(int)y]-(32+Math.sin((ticker/4+x+y-it)*12))+island.layers.get(0)[(int)x][(int)y]*12-24;;
//				}
//			else
			{
//				fact=1;
				if(fact>.5)fact=1;
				ahr2*=1-fact;
				gee2*=1-fact;
				bee2*=1-fact;
				ahr2+=(48+Math.sin((ticker/4+x+y-it)/2)*6)*fact;
				gee2+=(127+Math.atan((island.terrain[(int)x][(int)y]-tide)/16-3)*16-(32+Math.sin((ticker/4+x+y-it)*12)))*fact;
				bee2+=(196+Math.atan((island.terrain[(int)x][(int)y]-tide)/16-3)*16-(32+Math.sin((ticker/4+x+y-it)*12))+island.layers.get(0)[(int)x][(int)y]*12-24)*fact;;
				}
			fact=0.5;
			ahr*=1-fact;
			gee*=1-fact;
			bee*=1-fact;
			ahr+=(48-32+Math.atan((island.terrain[(int)x][(int)y]-tide)/16-3)*32+Math.sin((ticker/4+x+y-it)/2)*6)*fact;
			gee+=(127-48+Math.atan((island.terrain[(int)x][(int)y]-tide)/16-3)*48-(32+Math.sin((ticker/4+x+y-it)*12)))*fact;
			bee+=(196-48+Math.atan((island.terrain[(int)x][(int)y]-tide)/16-3)*48-(32+Math.sin((ticker/4+x+y-it)*12))+island.layers.get(0)[(int)x][(int)y]*12-24)*fact;;
			
//			System.out.println(island.terrain[(int)x][(int)y]);
//			if(a< Math.sin((ticker/4+x+y-2*it)/2)*2)
//				a=Math.sin((ticker/4+x+y-2*it)/2)*2;
//			if(b< Math.sin((ticker/4+x+y-it)/2)*2)
//				b=Math.sin((ticker/4+x+y-it)/2)*2;
//			if(c< Math.sin((ticker/4+x+y+00)/2)*2)
//				c=Math.sin((ticker/4+x+y+00)/2)*2;
//			if(d< Math.sin((ticker/4+x+y-it)/2)*2)
//				d=Math.sin((ticker/4+x+y-it)/2)*2;
			}
		if(ahr>255)
			ahr=255;
		if(ahr<0)
			ahr=0;
		if(gee>255)
			gee=255;
		if(gee<0)
			gee=0;
		if(bee>255)
			bee=255;
		if(bee<0)
			bee=0;
		if(alfa>255)
			alfa=255;
		if(alfa<0)
			alfa=0;
		
		if(ahr2>255)
			ahr2=255;
		if(ahr2<0)
			ahr2=0;
		if(gee2>255)
			gee2=255;
		if(gee2<0)
			gee2=0;
		if(bee2>255)
			bee2=255;
		if(bee2<0)
			bee2=0;
//		if(alfa2>255)
//			alfa2=255;
//		if(alfa2<0)
//			alfa2=0;
		
		Color color=new Color((int)ahr, (int)gee, (int)bee);
		drawPoly(new pos3d(x   -128,1*(a)/8,y   -128), 
				 new pos3d(x+it-128,1*(b)/8,y   -128),
				 new pos3d(x+it-128,1*(c)/8,y+it-128),
				 new pos3d(x   -128,1*(d)/8,y+it-128), g, 
				 color, false);
		Color color2=new Color((int)ahr2, (int)gee2, (int)bee2);
		drawPoly(new pos3d(x   -128,1*(a2)/8,y   -128), 
				 new pos3d(x+it-128,1*(b2)/8,y   -128),
				 new pos3d(x+it-128,1*(c2)/8,y+it-128),
				 new pos3d(x   -128,1*(d2)/8,y+it-128), g, 
				 color2, false);

		double ahr1=ahr;
		double gee1=gee;
		double bee1=bee;
		
		boolean o1=false, o2=false, o3=false, o4=false;
//		if(island.terrain[(int)  x   ][(int)  y   ] + Math.sin(Math.toRadians(ticker+x   )*8)+Math.sin(Math.toRadians(ticker+y   )*8) <=60)o1=false;
//		if(island.terrain[(int)(x+it)][(int)  y   ] + Math.sin(Math.toRadians(ticker+x+it)*8)+Math.sin(Math.toRadians(ticker+y   )*8) <=60)o2=false;
//		if(island.terrain[(int)(x+it)][(int)(y+it)] + Math.sin(Math.toRadians(ticker+x+it)*8)+Math.sin(Math.toRadians(ticker+y+it)*8) <=60)o3=false;
//		if(island.terrain[(int)  x   ][(int)(y+it)] + Math.sin(Math.toRadians(ticker+x   )*8)+Math.sin(Math.toRadians(ticker+y+it)*8) <=60)o4=false;
			//Math.sin(Math.toRadians(ticker+x+it)*8)+Math.sin(Math.toRadians(ticker+y   )*8)
//			if(false)
		
//		if(!(o1&&o2&&o3&&o4))
			{ //ocean
//				ahr=island.layers.get(0)[(int)x][(int)y]*8;
//				gee=island.layers.get(0)[(int)x][(int)y]*8;
//				bee=(63*skyLight)+island.layers.get(0)[(int)x][(int)y]*8;
				ahr=0;
				gee=0;
				bee=32;
				

				
				shiny=16;
				alfa=127;
				l=false;
//				if(a<0)
//					a=0;
//				if(b<0)
//					b=0;
//				if(c<0)
//					c=0;
//				if(d<0)
//					d=0;
				
//				if(a>sealevel){
//					ahr*=2;
//					gee*=2;
//					bee*=2;}
//				if(b>sealevel){
//					ahr*=2;
//					gee*=2;
//					bee*=2;}
//				if(c>sealevel){
//					ahr*=2;
//					gee*=2;
//					bee*=2;}
//				if(d>sealevel){
//					ahr*=2;
//					gee*=2;
//					bee*=2;}
				
			
				ahr+=(Math.sin(Math.toRadians(ticker+x+it)*8)+Math.sin(Math.toRadians(ticker+y   )*8)
					 -Math.sin(Math.toRadians(ticker+x   )*8)-Math.sin(Math.toRadians(ticker+y+it)*8))*shiny/it*skyLight*8;
				gee+=(Math.sin(Math.toRadians(ticker+x+it)*8)+Math.sin(Math.toRadians(ticker+y   )*8)
					 -Math.sin(Math.toRadians(ticker+x   )*8)-Math.sin(Math.toRadians(ticker+y+it)*8))*shiny/it*skyLight*8;
				bee+=(Math.sin(Math.toRadians(ticker+x+it)*8)+Math.sin(Math.toRadians(ticker+y   )*8)
				     -Math.sin(Math.toRadians(ticker+x   )*8)-Math.sin(Math.toRadians(ticker+y+it)*8))*shiny/it*skyLight*8;
				
				if(ahr>255)
					ahr=255;
				if(ahr<0)
					ahr=0;
				if(gee>255)
					gee=255;
				if(gee<0)
					gee=0;
				if(bee>255)
					bee=255;
				if(bee<0)
					bee=0;
				if(alfa>255)
					alfa=255;
				if(alfa<0)
					alfa=0;
				alfa=255;
				ahr=ahr/4;
				gee=gee/4;
				bee=bee/2+64*skyLight;
//				if(x%(it*1)==0&&y%(it*1)==0)
//				for(double i=sealevel*30;i>=island.terrain[(int)x][(int)y]-90;i-=256)
//				{color=new Color((int)ahr/4, (int)gee/4, (int)64, (int)32);
//				drawPoly(new pos3d(x   -128,
//								   1*(a)/8-2 +i/30,
//						           y   -128), 
//						 new pos3d(x+it*it*4-128,
//						           1*(b)/8-2 +i/30,
//					               y -128),
//						 new pos3d(x+it*it*4-128,
//						           1*(c)/8-2 +i/30,
//						           y+it*it*4-128),
//						 new pos3d(x   -128,
//						           1*(d)/8-2 +i/30,
//						           y+it*it*4-128), g, 
//				 color, l);
//				}
				
//				a=island.terrain[(int)(x   )][(int)(y   )]-90; 
//				b=island.terrain[(int)(x+it)][(int)(y   )]-90;
//				c=island.terrain[(int)(x+it)][(int)(y+it)]-90;
//				d=island.terrain[(int)(x   )][(int)(y+it)]-90;
				
				double g1= 1*(a)/8-2;
				double g2= 1*(b)/8-2;
				double g3= 1*(c)/8-2;
				double g4= 1*(d)/8-2;
				
				double e1= (Math.sin(Math.toRadians(ticker*1+x   )*16)+Math.sin(Math.toRadians(ticker*1+y   )*16))/4+sealevel;
				double e2= (Math.sin(Math.toRadians(ticker*1+x+it)*16)+Math.sin(Math.toRadians(ticker*1+y   )*16))/4+sealevel;
				double e3= (Math.sin(Math.toRadians(ticker*1+x+it)*16)+Math.sin(Math.toRadians(ticker*1+y+it)*16))/4+sealevel;
				double e4= (Math.sin(Math.toRadians(ticker*1+x   )*16)+Math.sin(Math.toRadians(ticker*1+y+it)*16))/4+sealevel;

//				if(g1>0){g1=0;}
//				if(g2>0){g2=0;}
//				if(g3>0){g3=0;}
//				if(g4>0){g4=0;}
//				if(g2>=e2){e2=g2;o2=true;}
//				if(g3>=e3){e3=g3;o3=true;}
//				if(g4>=e4){e4=g4;o4=true;}
				
				
//				if(g1>=e1){e1=g1;o1=true;}
//				if(g2>=e2){e2=g2;o2=true;}
//				if(g3>=e3){e3=g3;o3=true;}
//				if(g4>=e4){e4=g4;o4=true;}
				
//				if( true ){e1=g1;o1=true;}
//				if( true ){e2=g2;o2=true;}
//				if( true ){e3=g3;o3=true;}
//				if( true ){e4=g4;o4=true;}
				
//				if( true ){e1=g1;o1=false;}
//				if( true ){e2=g2;o2=false;}
//				if( true ){e3=g3;o3=false;}
//				if( true ){e4=g4;o4=false;}

//				ahr=127;
//				gee=127;
//				bee=0;
				
				if(g1>=e1){e1=g1;o1=true;
				ahr+=32;gee+=32;
				bee+=64;alfa-=64;
				}
				
				if(g2>=e2){e2=g2;o2=true;
				ahr+=32;gee+=32;
				bee+=64;alfa-=64;
				}
				
				if(g3>=e3){e3=g3;o3=true;
				ahr+=32;gee+=32;
				bee+=64;alfa-=64;
				}
				
				if(g4>=e4){e4=g4;o4=true;
				ahr+=32;gee+=32;
				bee+=64;alfa-=64;
				}
//				if(o1&&o2&&o3&&o4)
//				{
//					bee+=32;
//					o1=false; o2=false; o3=false;o4=false;
//				}
//				o1=false; o2=false; o3=false;o4=false;
				
				if(ahr>255)
					ahr=255;
				if(ahr<0)
					ahr=0;
				if(gee>255)
					gee=255;
				if(gee<0)
					gee=0;
				if(bee>255)
					bee=255;
				if(bee<0)
					bee=0;
				if(alfa>255)
					alfa=255;
				if(alfa<0)
					alfa=0;
				alfa=255;
				if(alfa!=0){
					color=new Color((int)ahr1, (int)gee1, (int)bee1);
//					drawPoly(new pos3d(x   -128,
//								 	 e1,
//									 y   -128), 
//							 new pos3d(x+it-128,
//									 e2,
//									 y -128),
//							 new pos3d(x+it-128,
//									 e3,
//									 y+it-128),
//							 new pos3d(x   -128,
//									 e4,
//									 y+it-128), g, 
//							 color, true);
					}
				if(true&&!(o1&&o2&&o3&&o4)){
				color=new Color((int)ahr, (int)gee, (int)bee, (int)alfa);
//				drawPoly(new pos3d(x   -128,
//							 	 e1,
//								 y   -128), 
//						 new pos3d(x+it-128,
//								 e2,
//								 y -128),
//						 new pos3d(x+it-128,
//								 e3,
//								 y+it-128),
//						 new pos3d(x   -128,
//								 e4,
//								 y+it-128), g, 
//						 color, true);
				}
				
			 
				
				
		}
	}
	
}
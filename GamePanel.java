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

	pos3d velA=new pos3d(), velB=new pos3d(), velC=new pos3d(), velD=new pos3d();
	
	double num;
	Ship player=new Ship();
	double playerSpin=(Math.random()-Math.random())*10;
		
	public pos3d colorMult=new pos3d(1,1,1);
	
	@Override
	public void actionPerformed(ActionEvent e) {//new game "tick"

		double offset=0;
		colorMult=new pos3d(1,1,1);
	
		position.z = 
			position.z*.8
			+ ( shipPos.z/128 +pos3d.rotateb(new pos3d(offset,.5,1.5), shipRot).z)*.2;
		
		position.y = 
			position.y*.8
			+ ( shipPos.y/128 +pos3d.rotateb(new pos3d(offset,.5,1.5), shipRot).y)*.2;
		
		position.x =
			position.x*.8
			+ ( shipPos.x/128 +pos3d.rotateb(new pos3d(offset,.5,1.5), shipRot).x)*.2;
		
		if(running)
		{
			pos3d.mult(shipVel, 0.999);

			shipPos.add(shipVel);

			double tempx=128+(shipPos.x/128);
			double tempy=128-(shipPos.z/128);
			
			if(tempx<0)
			{
				tempx=0;
			}
			if(tempx>255)
			{
				tempx=0;
			}
			if(tempy<0)
			{
				tempy=0;
			}
			if(tempy>255)
			{	
				tempy=255;
			}

			double surf=32*(island.terrain[(int)tempx][(int)tempy]-86);
			
			if(shipPos.y<surf)
			{
				shipPos.y=surf;
			}
		}
		control.x=0;
		control.y=0;
		control.z=0;
		
		it=4;
		if(running)
		{
			shipPos.y+=0;
			
			gaze.z= gaze.z*.8+ ( shipRot.z)*.2;
			gaze.y= gaze.y*.8+ (180- shipRot.y)*.2+(4*offset);
			gaze.x= gaze.x*.8+ ( shipRot.x )*.2;
			
			pan=Math.atan2(position.x-shipPos.x/128, 
			               position.z-(shipPos.z+32)/128)
			    * 180/Math.PI+180;

			pitch=Math.atan2(position.y-(shipPos.y+128)/128,
							Math.sqrt(Math.pow(position.z-(shipPos.z+32)/128,2)
								      + Math.pow(position.x-shipPos.x/128,2) ))
				*-180/Math.PI;
			
			zoom=Math.sqrt(Math.pow(position.x-shipPos.x/128, 2)+Math.pow(position.y-shipPos.y/128, 2)+Math.pow(position.z-shipPos.z/128, 2))/256;
			
			if(trackShip)
			{
				FOVmod=FOVmod*3/4+zoom/4/2;

				if(FOVmod<2)
				{
					FOVmod=2;
				}
				if(FOVmod>8)
				{
					FOVmod=8;
				}
				gaze.y=pan;
				gaze.x=pitch;
			}
		}
		if(ticker>0)//once game actually starts,
		{
			
		}
		if(running){
		
			if(keyW){
				control.z-=1;
				}
			if(keyS){
				control.z+=1;
				}
			if(keyA){
				control.x-=1;
				}
			if(keyD){
				control.x+=1;
				}
			if(keyShift)
				control.y+=1;
			if(keyCtrl)
				control.y-=1;
			

			if(keyUp)
			{
				shipRot.x-=1024/tickerFactor*90/FOVmod/gameWidth;
			}
			if(keyDown)
			{
				shipRot.x+=1024/tickerFactor*90/FOVmod/gameWidth;
			}
			if(keyLeft)
			{
				shipRot.y+=1024/tickerFactor*90/FOVmod/gameWidth;
			}
			if(keyRight)
			{
				shipRot.y-=1024/tickerFactor*90/FOVmod/gameWidth;
			}
			if(keyCW)
			{
				shipRot.z+=1024/tickerFactor*90/FOVmod/gameWidth;
			}
			if(keyCCW)
			{
				shipRot.z-=1024/tickerFactor*90/FOVmod/gameWidth;
			}
		}
		if(running)
		{
			bezier3d b= new bezier3d(new pos3d(-250,0,250),new pos3d(-500,0,-500),new pos3d(500,0,-500),new pos3d(250,0,250));
			
			drawList=new ArrayList<Line3d>();;   //clear list

			for(int i=0; i<ellipse.size(); i++)
			{
				drawList.add(ellipse.get(i));
			} 
			double itterator=Math.pow(2, -6);
		}

		if(running)
		{
			drift.x*=0.95;
	    	drift.y*=0.95;
	    	drift.z*=0.95;


	    	shipPos.x+=pos3d.rotateb(new pos3d(control.x*movespeedfactor,control.y*movespeedfactor,control.z*movespeedfactor), shipRot).x;
	    	shipPos.y+=pos3d.rotateb(new pos3d(control.x*movespeedfactor,control.y*movespeedfactor,control.z*movespeedfactor), shipRot).y;
	    	shipPos.z+=pos3d.rotateb(new pos3d(control.x*movespeedfactor,control.y*movespeedfactor,control.z*movespeedfactor), shipRot).z;
	    	
	    	position.x += drift.x*tickerFactor*movespeedfactor/400;
			position.y += drift.y*tickerFactor*movespeedfactor/400;
			position.z += drift.z*tickerFactor*movespeedfactor/400;
			
			control.x=0;
			control.y=0;
			control.z=0;
		}
		
		sky.clear();
		double tilt=215;

		for(int i=0; i<galaxy.size(); i++)
		{	
			for(int j=0; j<galaxy.get(i).pos.size(); j++)
			{
				sky.add(new Line3d( pos3d.add(pos3d.rotate(new pos3d(galaxy.get(i).pos.get(j).x+Math.sin(celestialTicker/100)*2+64,galaxy.get(i).pos.get(j).y+0,galaxy.get(i).pos.get(j).z+Math.cos(celestialTicker/100)*2+64),new pos3d(0,celestialTicker,tilt)), pos3d.mult(position, -0.01)), 
									pos3d.add(pos3d.rotate(new pos3d(galaxy.get(i).pos.get(j).x+Math.sin(celestialTicker/100)*2+64,galaxy.get(i).pos.get(j).y+0,galaxy.get(i).pos.get(j).z+Math.cos(celestialTicker/100)*2+64),new pos3d(0,celestialTicker,tilt)), pos3d.mult(position, -0.01))));

			}
		}
		
		sky.add(new Line3d(pos3d.add(pos3d.rotate(new pos3d(-1,0,0), new pos3d(0,celestialTicker,tilt)),pos3d.negate(position)),
						   pos3d.add(pos3d.rotate(new pos3d(-1,0,0), new pos3d(0,celestialTicker,tilt)), pos3d.negate(position))));
	
		skyLight=((Math.cos((((celestialTicker)%360)/360)*2*Math.PI)+1)/2);

		skyLight=skyLight*0.75+0.25;

		celestialTicker+=0;

		if(running)
		{
			celestialTicker+=celestialSpeed;
		}
		
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
		
		double temp;
	
		for(temp=-64; temp<64; temp+=0.125)
		{
			ellipse.add(
				new Line3d(
					new pos3d( temp,0,Math.pow(temp, 2)),
					new pos3d( temp+0.125,0,Math.pow(temp+0.125, 2))));   
		}
		for(temp=-64; temp<64; temp+=0.125)
		{
			ellipse.add(
				new Line3d(
					new pos3d( temp,0,-Math.pow(temp, 2)),
					new pos3d( temp+0.125,0,-Math.pow(temp+0.125, 2))));   
		}
		for(temp=-64; temp<64; temp+=0.125)
		{
			ellipse.add(
				new Line3d(
					new pos3d( Math.pow(temp, 2), 0, temp),
					new pos3d( Math.pow(temp+0.125, 2), 0, temp+0.125)));   
		}
		for(temp=-64; temp<64; temp+=0.125)
		{
			ellipse.add(
				new Line3d(
					new pos3d(-Math.pow(temp, 2), 0, temp),
					new pos3d(-Math.pow(temp+0.125, 2), 0, temp+0.125)));   
		}
			
		running=false;
	}
	
	public void paintComponent(Graphics g)
	{	
		groundR=(int)( 0*colorMult.x);
		groundG=(int)( 0*colorMult.y);
		groundB=(int)( 0*colorMult.z);
		
		int gree=31;

		g.setColor(
			new Color(
				(int)(groundR*skyLight)/4,
				(int)(groundG*skyLight)/4,
				(int)(groundB*skyLight)/4
			)
		);
		
		clearBG=false;
		
		g.fillRect(0, 0, gameWidth, gameHeight);
		
		ArrayList<Line3d> skybox = new ArrayList<Line3d>();
		for(int i=0; i<sky.size();i++)
		{
			skybox.add(
				new Line3d(
					pos3d.rotate(
						new pos3d(
							sky.get(i).a.x,
							sky.get(i).a.y,
							sky.get(i).a.z),
							gaze),
					pos3d.rotate(
						new pos3d(
							sky.get(i).b.x,
							sky.get(i).b.y,
							sky.get(i).b.z),
							gaze
					) 
				)  
			);
		}
		
		ArrayList<Line3d> lines = new ArrayList<Line3d>();
		
		for(int i=0; i<skybox.size();i++)
		{
			if(skybox.get(i).a.z>0.06125&&skybox.get(i).b.z>0.06125)
			{
				if( 32*exposure/(skybox.get(i).a.z+skybox.get(i).b.z) <255){
					g.setColor(new Color(
							(int) (255*colorMult.x),
				            (int) (255*colorMult.y),
				            (int) (255*colorMult.z),
				            (int) (32*exposure/(skybox.get(i).a.z+skybox.get(i).b.z))/2
				        )
					);
				}
				else{
					g.setColor(new Color(
							(int) (255*colorMult.x),
				            (int) (255*colorMult.y),
				            (int) (255*colorMult.z),
				            127
				        )
			        );
				}
				if(i==skybox.size()-1){
					g.setColor(new Color((int) (255*colorMult.x),(int) (255*colorMult.y),(int) (255*colorMult.z)));
				}
				
				if (sky.get(i).a.y>=0&&sky.get(i).b.y>=0)
				{
					g.fillOval((int)((( FOVmod*(skybox.get(i).a.x*128) / (skybox.get(i).a.z)) - FOVmod*8/(skybox.get(i).a.z*2)) *drawWidthMult)	+gameWidth/2,
							   (int)(((-FOVmod*(skybox.get(i).a.y*128) / (skybox.get(i).a.z)) - FOVmod*8/(skybox.get(i).a.z*2)))					+gameHeight/2,
							   (int)(FOVmod*(16/ (skybox.get(i).a.z*2))  *drawWidthMult)+1,
							   (int)(FOVmod*(16/ (skybox.get(i).a.z*2)))+1);
				}
			}
		}
		//DRAW TERRAIN=======
		
		double playX=position.x+128;
		double playY=-position.z+128;

		g.setColor(Color.WHITE);

		it=4;
		double itfactor=2;
		double dist=it*8;
		double lock=it*4;
		double start=32;
		double end=4;

		for(it=start; it>=end; it/=itfactor)
		{
			dist=it*16;
			lock=it*itfactor;
			for(double x=lock*(int)(playX/lock)-dist; x<=playX-it&&x<=256-it; x+=it)			//0-->X
			{
				if(x<0)
				{
					x=0;
				}
				for(double y=lock*(int)(playY/lock)-dist; y<=playY-it&&y<=256-it; y+=it)
				{
					if(y<0)
					{
						y=0;
					}
					drawTerrainTile(g, x, y, it);
				}
			}
			for(double x=lock*(int)(playX/lock)-dist; x<=playX-it&&x<=256-it; x+=it)			//0-->X
			{
				if(x<0)
				{
					x=0;
				}

				for(double y=lock*(int)(playY/lock)+dist; y>=playY-it&&y>=0+it; y-=it)
				{	//1-->Y
					if(y+it>256)
					{
						y=256-it;
					}
					drawTerrainTile(g, x, y, it);
				}	
			}
			for(double x=lock*(int)(playX/lock)+dist; x>=playX-it&&x>=0+it; x-=it)		//1-->X
			{
				if(x+it>256)
				{
					x=256-it;
				}
				for(double y=lock*(int)(playY/lock)-dist; y<=playY-it&&y<=256-it; y+=it)	//0-->Y
				{
					if(y<0)
					{
						y=0;
					}
					drawTerrainTile(g, x, y, it);
				}	
			}
			for(double x=lock*(int)(playX/lock)+dist; x>=playX-it&&x>=0+it; x-=it)		//1-->X
			{
				if(x+2*it>256)
				{
					x=256-2*it;
				}
				for(double y=lock*(int)(playY/lock)+dist; y>=playY-it&&y>=0+it; y-=it)		//1-->Y
				{	
					if(y+it>256){
						y=256-it;
					}
					if(y+it>256){
						y=256-it;
					}
					drawTerrainTile(g, x, y, it);
				}	
			}
		}
		
		for(int i=0; i<drawList.size(); i++)
		{
			lines.add(
				new Line3d(
					pos3d.rotate(
						new pos3d(
							drawList.get(i).a.x-position.x*128,
							drawList.get(i).a.y-position.y*128,
							drawList.get(i).a.z-position.z*128),gaze)
					,
					pos3d.rotate(
						new pos3d(
							drawList.get(i).b.x-position.x*128,
							drawList.get(i).b.y-position.y*128,
							drawList.get(i).b.z-position.z*128),gaze) 
					)  
				);

			if(lines.get(i).a.z<0.125&&lines.get(i).b.z<0.125) 
			{
				// if both vertices are close to or behind camera plane, remove them cull
				lines.get(i).a=null;
				lines.get(i).b=null;	
			}
			else if(lines.get(i).a.z<0.125)
			{
				// if only one, make the line stop early
				lines.get(i).a=Line3d.lerp(lines.get(i).b, 
										   lines.get(i).a, 
										  (lines.get(i).a.z-0.15)/(lines.get(i).a.z-lines.get(i).b.z));
			}
			else if(lines.get(i).b.z<0.125)
			{
				// if only one, make the line stop early
				lines.get(i).b=Line3d.lerp(lines.get(i).b, 
										   lines.get(i).a, 
										  (lines.get(i).a.z-0.015)/(lines.get(i).a.z-lines.get(i).b.z));
			}

		}
				
		for(int i=0; i<lines.size();i++)
		{
			if(lines.get(i).a!=null&&lines.get(i).b!=null)
			{
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
					// if (alfa>255) 
						alfa=255;
					

						g.setColor(new Color((int)ahr,(int)gee,(int)bee,(int)alfa));

				g.drawLine((int)(( FOVmod*(lines.get(i).a.x*128) / (lines.get(i).a.z))*drawWidthMult)	+gameWidth/2,
						   (int)( -FOVmod*(lines.get(i).a.y*128) / (lines.get(i).a.z))					+gameHeight/2,
						   (int)(( FOVmod*(lines.get(i).b.x*128) / (lines.get(i).b.z))*drawWidthMult)	+gameWidth/2,
						   (int)( -FOVmod*(lines.get(i).b.y*128) / (lines.get(i).b.z))					+gameHeight/2);
				}
			}
		}
		
		double tide=Math.sin(ticker/20)*32+32;

		if(position.y*8<tide)
		{
			g.setColor(new Color((int)(0*colorMult.x),(int)(32*colorMult.y),(int)(64*colorMult.z),(int)(-100*Math.atan((position.y*8-tide)/3))));
			g.fillRect(0, 0, gameWidth, gameHeight);
		}
			
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
		
		
		if(!running)
		{	
			g.setColor(Color.BLACK);
		
			for(int x=0; x<2; x++)
			{
				g.drawString("PAUSED", gameWidth/2, gameHeight/2);
				g.setColor(
					new Color(
						(int)(255*colorMult.x),
						(int)(255*colorMult.y),
						(int)(255*colorMult.z)));
			}
		}
		int z=8;

		double tempx=128+(shipPos.x/128);
		double tempy=128-(shipPos.z/128);
		if(tempx<0)
		{
			tempx=0;
		}
		if(tempx>255)
		{
			tempx=0;
		}
		if(tempy<0)
		{
			tempy=0;
		}
		if(tempy>255)
		{
			tempy=255;
		}

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
			
			double itter=11.25*detail;

			for(temp=0; temp<360; temp+=itter)//saucer
			{
				enterprise.add(
					new Line3d(
						new pos3d(
							(Math.sin(Math.toRadians(temp)))*140,
							86, //top ring
							(Math.cos(Math.toRadians(temp)))*140),
						new pos3d( 
							(Math.sin(Math.toRadians(temp+itter)))*140,	
							86, 
							(Math.cos(Math.toRadians(temp+itter)))*140)   
						)
					);

				
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
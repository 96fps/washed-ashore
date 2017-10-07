import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;

	
public class World {
//	Tile[][] world=new Tile[256][256];
	public int[][]elevation=new int[257][257];//elevation
	
	int width=256;
	int height=256;
	
	
	public double[][] terrain = new double[width+64][height+64];
	
	ArrayList <double[][]> layers = new ArrayList <double[][]>();
	
		
	
	
	
	{
		{
//			setPreferredSize(new Dimension(256,256));
			
			
			layers.add(new double[(width*2)+1][(width*2)+1]);
			layers.add(new double[width+1][height+1]);
			layers.add(new double[(width/2)+1][(height/2)+1]);
			layers.add(new double[(width/4)+1][(height/4)+1]);
			layers.add(new double[(width/8)+1][(height/8)+1]);
			layers.add(new double[(width/16)+1][(height/16)+1]);
			//layers.add(new int[(width/32)+1][(height/32)+1]);
			
			//generate noise
			for(int z=0;z<layers.size();z++){
				// System.out.println("2^"+z);
				// System.out.println(Math.pow(2,z));
				for(int x=0; x<(width/(Math.pow(2,z)))+1; x++)
					for(int y=0; y<(height/(Math.pow(2,z)))+1; y++)
					{
							{//System.out.println("Generating: "+"z="+z+"; x="+x+"; y="+y);
								
							layers.get(z)[x][y]=(int) (Math.random()*(Math.pow(2,z))*4);
						}
					}
			}
			
			//composite=======================================================================================
			for(int z=(layers.size()-1); z>-1;z--){
				for(int x=0; x<width+1; x++)
					for(int y=0; y<height+1; y++)	
					{	
						//System.out.println("Compositing: "+"z="+z+"; x="+x+"; y="+y);
							{
								if((x/Math.pow(2, z)%2!=0)&&(x/Math.pow(2, z)%2!=0))
									terrain[x][y] = (terrain[x][y]+terrain[x+(int)(Math.pow(2,z))][y]+terrain[x][y+(int)(Math.pow(2,z))]+terrain[x+(int)(Math.pow(2,z))][y+(int)(Math.pow(2,z))])/4+layers.get(z)[x/(int)Math.pow(2, z)][y/(int)Math.pow(2, z)];
								else if(x/Math.pow(2, z)%2!=0)
									terrain[x][y] = (terrain[x][y]+terrain[x+(int)(Math.pow(2,z))][y])/2+layers.get(z)[x/(int)Math.pow(2, z)][y/(int)Math.pow(2, z)];
								else if(y/Math.pow(2, z)%2!=0)
									terrain[x][y] = (terrain[x][y]+terrain[x][y+(int)(Math.pow(2,z))])/2+layers.get(z)[x/(int)Math.pow(2, z)][y/(int)Math.pow(2, z)];
								else
									terrain[x][y] = terrain[x][y]+layers.get(z)[x/(int)Math.pow(2, z)][y/(int)Math.pow(2, z)];
							}
						//System.out.println("x"+x+"y"+y+"z"+z);
					}
			}
			
			
		//	island-ify
			for(int x=0-(width/2); x<width-(width/2); x++){
				for(int y=0-(height/2); y<height-(height/2); y++)	{
					if(((x*x)+(y*y))/height>0&&((x*x)+(y*y))/height<256){
						terrain[x+(width/2)][y+(height/2)] = (int)((terrain[x+(width/2)][y+(height/2)])-2*(((x*x)/height)+((y*y)/height)));
					}
					else if(((x*x)+(y*y))/height>0)
					{
						terrain[x+(width/2)][y+(height/2)]=255;
					}
					else
					{
//						terrain[x+(width/2)][y+(height/2)]=255;
					}
					
				}

			
			}
		}
	}
	

	
	
	World(){
		
		for(int x=0; x<257; x++){
			for(int y=0; y<257; y++){
				if(x<256&&y<256){
				elevation[x][y] = (int)(terrain[x][y]-90);
				if(elevation[x][y]<0)
				elevation[x][y] = 0;}
				else if(y<256)
				elevation[x][y] = elevation[255][y  ];
				else if(x<256)
				elevation[x][y] = elevation[x  ][255];
				else
				elevation[x][y] = elevation[255][255];
				
			}
		}
		
		//trees
		for(double z=0;z<4096;z++)
		{
			int x=(int)(Math.random()*width);
			int y=(int)(Math.random()*height);
			// System.out.println("New Tree!"+x+","+y+"!");
			if(terrain[x][y]>96)
			{
			//	terrain[x][y]=10000;
			}
			else{
				z-=0.5;
			}
		}
	}
}

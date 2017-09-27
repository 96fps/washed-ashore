import java.util.ArrayList;


public class NoiseMap {
	int size=11;//given in power of two, has to be >2, can't be too large
	
	int start=5;
	int stop=5;
	
	int referenceScale=64;//this can be used if requesting a value. 
	
	double[][] noiseMap = new double[(int)Math.pow(2,size-1)][(int)Math.pow(2,size-1)];
	
	ArrayList <double[][]> layers = new ArrayList <double[][]>();
	
	public NoiseMap(int detail, int scale){
		size=detail+1;
		scale=referenceScale;
		new NoiseMap();
	}
	public NoiseMap(){
		
		for(int p=start; p<=stop; p++){

			  for(int x=0; x<Math.pow(2,p); x++){
			    for(int y=0; y<Math.pow(2,p);y++){

			      noiseMap[x*(int)Math.pow(2,p-stop)][y*(int)Math.pow(2,p-stop)]+=Math.random(); 					//set random
				   double res=Math.pow(2,p-stop);
			  
				   System.out.println(noiseMap[x*(int)Math.pow(2,p-stop)][y*(int)Math.pow(2,p-stop)]);
				   
				  noiseMap[x*(int)Math.pow(2,p-stop)+(int)Math.pow(2,p-stop)][y*(int)Math.pow(2,p-stop)]+=				//interlace x+
					(noiseMap[       x        *(int)Math.pow(2,p-stop)][  y      *(int)Math.pow(2,p-stop)]+
					 noiseMap[(int)((x+1)%res)*(int)Math.pow(2,p-stop)][  y      *(int)Math.pow(2,p-stop)])/2;
			     
				  noiseMap[x*(int)Math.pow(2,p-stop)][y*(int)Math.pow(2,p-stop)+(int)Math.pow(2,p-stop+1)]+=				//interlace y+
					(noiseMap[  x      *(int)Math.pow(2,p-stop)][       y        *(int)Math.pow(2,p-stop)]+
					 noiseMap[  x      *(int)Math.pow(2,p-stop)][(int)((y+1)%res)*(int)Math.pow(2,p-stop)])/2;
			    
			      noiseMap[x*(int)Math.pow(2,p-stop)+(int)Math.pow(2,p-stop+1)][y*(int)Math.pow(2,p-stop)+(int)Math.pow(2,p-stop+1)]+=	//interlace xy+
					(noiseMap[       x        *(int)Math.pow(2,p-stop)][       y        *(int)Math.pow(2,p-stop)]+
					 noiseMap[       x        *(int)Math.pow(2,p-stop)][(int)((y+1)%res)*(int)Math.pow(2,p-stop)]+
					 noiseMap[(int)((x+1)%res)*(int)Math.pow(2,p-stop)][       y        *(int)Math.pow(2,p-stop)]+
					 noiseMap[(int)((x+1)%res)*(int)Math.pow(2,p-stop)][(int)((y+1)%res)*(int)Math.pow(2,p-stop)])/4;
			    }
			  }
			}

		
	}
}

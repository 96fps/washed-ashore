
public class pos3d {
	public double x;
	public double y;
	public double z;
	
	public pos3d(){
		x=0;
		y=0;
		z=0;
	}
	public pos3d(double posX, double posY, double posZ){
		x=posX;
		y=posY;
		z=posZ;
	}
	public void add(double posX, double posY, double posZ){
		x+=posX;
		y+=posY;
		z+=posZ;
	}
	public void add(pos3d pos){
		x+=pos.x;
		y+=pos.y;
		z+=pos.z;
	}public static pos3d add(pos3d a, pos3d b){
		pos3d c= new pos3d();
		c.x=a.x+b.x;
		c.y=a.y+b.y;
		c.z=a.z+b.z;
		return c;
	}
	public pos3d mult(pos3d pos){
		return new pos3d(x*pos.x,
						 y*pos.y,
						 z*pos.z);
	}public pos3d mult(double n){
		return new pos3d(x*n,
				 		 y*n,
				 		 z*n);
	}
	public static pos3d mult(pos3d a, pos3d pos){
		return new pos3d(a.x*pos.x,
				 a.y*pos.y,
				 a.z*pos.z);
	}public static pos3d mult(pos3d a,double n){
		return new pos3d(a.x*n,
		 		 a.y*n,
		 		 a.z*n);
}
	public static pos3d negate(pos3d pos){
		return new pos3d(-pos.x, -pos.y, -pos.z);
	}
	public static pos3d rotate(pos3d pos, pos3d rot){
		double posX=pos2d.rotX(new pos2d(pos.x, pos.z), rot.y);
		double posY=pos.y;
		double posZ=pos2d.rotY(new pos2d(pos.x, pos.z), rot.y);
		pos3d temp= new pos3d();
		temp.x=posX;
		temp.y=pos2d.rotX(new pos2d(posY, posZ), rot.x);
		temp.z=pos2d.rotY(new pos2d(posY, posZ), rot.x);
		
		posX=pos2d.rotX(new pos2d(temp.x, temp.y), rot.z);
		posY=pos2d.rotY(new pos2d(temp.x, temp.y), rot.z);
		posZ=temp.z;
		
		
		return new pos3d(posX,posY,posZ);
	}
	public static pos3d rotateb(pos3d pos, pos3d rot){
		double posX=pos2d.rotX(new pos2d(pos.x, pos.y), rot.z);
		double posZ=pos.z;
		double posY=pos2d.rotY(new pos2d(pos.x, pos.y), rot.z);
		pos3d temp= new pos3d();
		temp.x=posX;
		temp.y=pos2d.rotX(new pos2d(posY, posZ), rot.x);
		temp.z=pos2d.rotY(new pos2d(posY, posZ), rot.x);
		
		posX=pos2d.rotX(new pos2d(temp.x, temp.z), rot.y);
		posY=temp.y;
//		posZ=temp.z;
		posZ=pos2d.rotY(new pos2d(temp.x, temp.z), rot.y);
		
		
		return new pos3d(posX,posY,posZ);
	}
	
}


public class bezier3d {
	pos3d a;//start
	pos3d b;
	pos3d c;
	pos3d d;//end
	
	public bezier3d(pos3d posa,pos3d posb,pos3d posc,pos3d posd){
		a=posa;
		b=posb;
		c=posc;
		d=posd;
	}
	public pos3d getpoint(double i){ //i= 0 <-> 1

		pos3d temp1a=Line3d.lerp(a, b, i);
		pos3d temp1b=Line3d.lerp(b, c, i);
		pos3d temp1c=Line3d.lerp(c, d, i);
		
		pos3d temp2a=Line3d.lerp(temp1a, temp1b, i);
		pos3d temp2b=Line3d.lerp(temp1b, temp1c, i);
		
		return Line3d.lerp(temp2a, temp2b, i);
	}
	
	
}

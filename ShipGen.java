import java.util.ArrayList;

public class ShipGen {

	public ArrayList<Line3d> generateShip(){

		//Generate Saucer Section

		for(int temp=0; temp<360; temp+=15)
		{

			double radius = 100;

			//top ring
			radius = 140;
			enterprise.add(
				new Line3d(
					new pos3d(
						(Math.sin(Math.toRadians(temp)))*radius, 86, 
						(Math.cos(Math.toRadians(temp)))*radius),
					new pos3d( 
						(Math.sin(Math.toRadians(temp+itter)))*radius, 86, 
						(Math.cos(Math.toRadians(temp+itter)))*radius)   
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

		//enginering hull
		for(temp=0; temp<360; temp+=itter) 
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
		}
		itter=22.5*detail;

		for(int i=-1; i<=1; i+=2)
		{
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
		}

		//pylons
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


		//=="warp engines pylons"
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
 		// the other one
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
	}

	public ArrayList<Line3d> generateRing(double radius, double y){
		for(int temp=0; temp<360; temp+=15)
		{
			enterprise.add(
				new Line3d(
					new pos3d(
						(Math.sin(Math.toRadians(temp)))*radius, y, 
						(Math.cos(Math.toRadians(temp)))*radius),
					new pos3d( 
						(Math.sin(Math.toRadians(temp+itter)))*radius, y, 
						(Math.cos(Math.toRadians(temp+itter)))*radius)   
					)
				);
		}
	}
}
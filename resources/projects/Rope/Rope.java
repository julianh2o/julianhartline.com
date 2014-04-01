//Copyright (c) Julian Hartline 2005
import java.util.*;
import java.awt.*;
import java.applet.*;
import java.text.*;
import java.math.*;

public class Rope extends Applet implements Runnable {
	private volatile Thread timer;
	double[] xpos,ypos;
	double[] xvel,yvel;
	boolean mousedown;
	double maccel=.2;
	int mousex;
	int mousey;
	double restdistance=.05;
	double Kelast=100;
	int points=100;
	String debug;
	String[] bigdebug;
	int choice=0;
	double gravity=.05;
	boolean grav=true;
	int menu;
	int m=1;
	boolean fixed=true;
	boolean slinkey=false;
	boolean lines=false;
	boolean quickdamp=false;
	double damp=.79;
	String[] colornames={"Black","Blue","Red","Green","Magenta","Yellow","Gray"};
	Color[] colors={Color.black,Color.blue,Color.red,Color.green,Color.magenta,Color.yellow,new Color(100,100,100)};
	int color=0;
	int steps;
	boolean slow;
	boolean dynamic=false;
	boolean thick=false;
	boolean showkey=false;
	boolean normal=true;
	boolean freeze=false;

	Image offscreen;
	
	public void init() {
		Dimension dim = getSize();
		offscreen = createImage((int)dim.getWidth(), (int)dim.getHeight());
		xpos=new double[points];
		ypos=new double[points];
		xvel=new double[points];
		yvel=new double[points];
		bigdebug=new String[points];
		int i;
		reset();
	}
	
	public void paint(Graphics sg) {
		Graphics g = offscreen.getGraphics();
		g.clearRect(0,0,offscreen.getWidth(this),offscreen.getHeight(this));
		if (debug!=null) g.drawString("debug="+debug,100,100);
		DecimalFormat f=new DecimalFormat("###.###");
		int next=0;
		if (menu==0) g.setColor(Color.blue); else g.setColor(Color.black);
		g.drawString("Gravity="+((grav)?f.format(gravity):"off")+"      ",next,10); next+=90;
		if (menu==1) g.setColor(Color.blue); else g.setColor(Color.black);
		g.drawString("Mass="+m+"      ",next,10); next+=60;
		if (menu==2) g.setColor(Color.blue); else g.setColor(Color.black);
		g.drawString("Elasticity="+f.format(Kelast)+"      ",next,10); next+=100;
		if (menu==3) g.setColor(Color.blue); else g.setColor(Color.black);
		g.drawString("Rest Separation="+f.format(restdistance)+"      ",next,10); next+=160;
		if (menu==4) g.setColor(Color.blue); else g.setColor(Color.black);
		g.drawString("Max Acceleration="+f.format(maccel)+"      ",next,10); next+=160;
		if (menu==5) g.setColor(Color.blue); else g.setColor(Color.black);
		g.drawString("Color: "+colornames[color]+"      ",next,10); next+=100;
		if (menu==6) g.setColor(Color.blue); else g.setColor(Color.black);
		g.drawString("Damping="+f.format(damp)+"      ",next,10);;
		g.setColor(colors[color]);
		int i;
		Font font=new Font("Arial",Font.PLAIN,9);
		g.setFont(font);
		for (i=1;i<=points-1;i++) {
			double d=distance(xpos[i-1],ypos[i-1],xpos[i],ypos[i]);
			if (d>12) d=12;
			if (dynamic) g.setColor(new Color((int)(20*d),0,(int)(255-15*d)));
			if (lines)g.drawLine((int)xpos[i-1],(int)ypos[i-1] ,(int)(xpos[0]+xpos[points-1])/2,(int)(ypos[0]+ypos[points-1])/2);
			if (slinkey) g.drawOval((int)xpos[i-1]-50,(int)ypos[i-1]-50,100,100);
			if (thick) g.fillOval((int)xpos[i-1]-25,(int)ypos[i-1]-25,50,50);
			if (normal) g.drawLine((int)xpos[i-1],(int)ypos[i-1],(int)xpos[i],(int)ypos[i]);
			//g.drawString("xvel["+i+"]="+xvel[i]+  "      yvel["+i+"]="+yvel[i],300,i*12);
			if (bigdebug[1]!=null) g.drawString("debug: "+bigdebug[i],(i>50)?500:200,(i>50)?15+i*10-500:15+i*10);
		}
		sg.drawImage(offscreen,0,0,this);
	}

	public void update(Graphics screen) {
		paint(screen);
	}
	
	private void reset() {
		int i;
		for (i=0; i<=points-1; i++) {
			xpos[i]=i*10;
			ypos[i]=50;
			xvel[i]=0;
			yvel[i]=0;
		}
	}
	
	public void start() {
		timer = new Thread(this);
		timer.start();
	}
	
	public void stop() {
		timer = null;
	}
	
	public boolean mouseDown(Event e,int x, int y) {
		double previousdist=0;
		int i;
		choice=-1;
		if (distance(xpos[0],ypos[0],x,y)<100) choice=0;
		if (distance(xpos[points-1],ypos[points-1],x,y)<100) choice=points-1;
		if (choice==-1) {
			for (i=1;i<=points-1;i++) {
				if (distance(xpos[i],ypos[i],x,y)<previousdist && previousdist<50) break;
				previousdist=distance(xpos[i],ypos[i],x,y);
				//bigdebug[i]="dist:"+previousdist;
			}
			i--;
			choice=i;
			
		}
		mousedown=true;
		mousex=x;
		mousey=y;
		return true;
	}
	
	public double distance(double x1,double y1,double x2,double y2) {
		return Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
	}
	
	public boolean mouseUp(Event e,int x, int y) {
		mousedown=false;
		mousex=x;
		mousey=y;
		return true;
	}
	
	public boolean mouseDrag(Event e,int x, int y) {
		mousex=x;
		mousey=y;
		return false;
	}
	
	public boolean keyDown(Event e,int key) {
		if (key==1004) {  //up
			switch (menu) {
				case 0: gravity+=.001; break;
				case 1: m+=5; break;
				case 2: Kelast+=10; break;
				case 3: restdistance+=.05; break;
				case 4: maccel+=.01; break;
				case 5: color+=1;break;
				case 6: damp+=.001;break;

			}
		}
		if (key==1005) {  //down
			switch (menu) {
				case 0: gravity-=.001; break;
				case 1: m-=5; break;
				case 2: Kelast-=10; break;
				case 3: restdistance-=.05; break;
				case 4: maccel-=.01; break;
				case 5: color-=1;break;
				case 6: damp-=.001;break;
			}
		}
		if (color>colors.length-1) color=0;
		if (color<0) color=colors.length-1;

		
		
		if (key==1006) menu--;					//left
		if (key==1007) menu++;					// right
		if (key==32) fixed=!fixed;				//space
		if (key==115) slinkey=!slinkey;			//s
		if (key==108) lines=!lines;				//l
		if (key==100) quickdamp=true;			//d
		if (key==99) dynamic=!dynamic;				//c button
		if (key==103) grav=!grav;				//g button
		if (key==107) showkey=!showkey;				//g button
		if (key==116) thick=!thick;				//g button
		if (key==110) normal=!normal;			//n key
		if (key==112) freeze=!freeze;
		if (key==61 && freeze) steps++;
		if (key==10 && freeze) steps+=9;
		if (key==111) slow=!slow;
		if (key==114) reset();
		
//		if (choice<0) choice=points-1;
//		if (choice>points-1) choice=0; 
//		if (key==49) choice=choice-1; //1 button
//		if (key==50) choice=choice+1; //2 button
		showkey = true;
		if (showkey) debug="key "+key;
		return true;
	}
	
	public double mass(int piece) {
		return m;
	}
	
	public void run() {
		int i;
		double dist,dist2;
		double accel,accel2;
		while(true) {
			while (freeze && steps==0){
				try {
					timer.sleep(10);
				} catch (InterruptedException e) { ; }
			}
			steps--;
			if (mousedown) {
				xpos[choice]=mousex;
				ypos[choice]=mousey;
				xvel[choice]=0;
				yvel[choice]=0;
			}
			for (i=1;i<=points-2;i++) {
				dist=Math.sqrt((xpos[i]-xpos[i-1])*(xpos[i]-xpos[i-1])+(ypos[i]-ypos[i-1])*(ypos[i]-ypos[i-1]));
				accel=-(restdistance-dist)*Kelast/mass(i);
				if (accel>maccel) accel=maccel;
				
	
				dist2=Math.sqrt((xpos[i+1]-xpos[i])*(xpos[i+1]-xpos[i])+(ypos[i+1]-ypos[i])*(ypos[i+1]-ypos[i]));
				accel2=-(restdistance-dist2)*Kelast/mass(i);
				if (accel2>maccel) accel2=maccel;
				
				xvel[i]-=(xpos[i]-xpos[i-1])*accel+(xpos[i]-xpos[i+1])*accel2;
				yvel[i]-=(ypos[i]-ypos[i-1])*accel+(ypos[i]-ypos[i+1])*accel2;
				
				//debug[i]="("+accel+")";
				if (grav) yvel[i]+=gravity; //gravity
				xpos[i]+=xvel[i];
				ypos[i]+=yvel[i];
				xvel[i]*=damp;
				yvel[i]*=damp;
				if (quickdamp) {
					xvel[i]*=.7;
					yvel[i]*=.7;
				}
			}
			if (!fixed) {
				//i++;
				dist=Math.sqrt((xpos[i]-xpos[i-1])*(xpos[i]-xpos[i-1])+(ypos[i]-ypos[i-1])*(ypos[i]-ypos[i-1]));
				accel=-(restdistance-dist)*Kelast/mass(i);
				if (accel>maccel) accel=maccel;
				xvel[i]-=(xpos[i]-xpos[i-1])*accel;
				yvel[i]-=(ypos[i]-ypos[i-1])*accel;
				if (grav) yvel[i]+=gravity; //gravity
				xpos[i]+=xvel[i];
				ypos[i]+=yvel[i];
				xvel[i]*=damp;
				yvel[i]*=damp;	
				if (quickdamp) {
					xvel[i]*=.7;
					yvel[i]*=.7;
				}
			}
			quickdamp=false;
			repaint();
			try {
				timer.sleep(((slow)?100:10));
			} catch (InterruptedException e) { ; }
		}
	}
}

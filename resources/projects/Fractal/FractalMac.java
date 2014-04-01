//Copyright (c) Julian Hartline 2005
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.applet.*;
import java.text.*;
import java.math.*;
import java.net.*;
import java.io.*;
import java.awt.event.*;

public class FractalMac extends Applet implements Runnable {
	private volatile Thread timer;
	Attribute name;
	Attribute display;
	Attribute rotation;
	Attribute delay;

	Image ioffscreen;
	Graphics offscreen;
	int width,height;
	boolean dot=false;
	boolean norm=true;
	boolean dotbranch=false;
	boolean animate=false;
	boolean dynamic=false;
	int tdelay=100;
	int fracx=500,fracy=300;
	int dx,dy;
	double currang=0,rot=0;
	int huemult=5;
	int hueshift=0;
	int dotsize=1;
	boolean square=true;
	Color color=Color.white;
	boolean drag=false;
	int fminx, fminy, fmaxx, fmaxy;
	Button save;
	Button makegif;

	Vector<Attribute> attr;
	
	String debug = "";

	public static final int I_NAME = 0;
	public static final int I_ITER = 1;
	public static final int I_DANG = 2;
	public static final int I_DLEN = 3;
	public static final int I_ILEN = 4;
	public static final int I_IBRAN = 5;
	public static final int I_BRANCH = 6;
	public static final int I_OFFSET = 7;
	public static final int I_DELAY = 8;
	public static final int I_ROTATE = 9;
	public static final int I_DISPLAY = 10;

	int fractalSteps;
	int cstep;

	private class Attribute extends TextField {
		double min, max, delta;
		boolean anim;
		boolean parse;
		String iden;

		public Attribute(String iden, int size) {
			super(size);
			parse = true;
			this.iden = iden;
		}

		public String toString() {
			return iden+"="+getText();
		}

		public String getIden() {
			return iden;
		}

		public boolean isAnim() {
			return anim;
		}

		public void setParse(boolean parse) {
			this.parse = parse;
		}

		public boolean isParse() {
			return parse;
		}

		public void read() {
			try {
				if (!parse) return;
				String s = getText();
				anim = false;
				if (s.indexOf(",") != -1) {
					String[] comma = s.split(",");
					min = Double.parseDouble(comma[0]);
					max = Double.parseDouble(comma[1]);
					delta = 1;
					if (comma.length == 3) delta = Double.parseDouble(comma[2]);
					anim = true;
				} else if (s.indexOf("~") != -1) {
					anim = true;
					String[] mspl = s.split("~");
					min = Double.parseDouble(mspl[0]);
					if (s.indexOf(";") != -1) {
						String[] spl2 = mspl[1].split(";");
						max = Double.parseDouble(spl2[0]);
						delta = Double.parseDouble(spl2[1]);
					} else {
						max = Double.parseDouble(mspl[1]);
						delta = 1;
					}
				} else {
					min = Double.parseDouble(s);
				}
			} catch (Exception e) {
			
			}
		}

		public double getValue(int step) {
			if (getSteps() != 0) step = step % (2*getSteps());
			if (!parse) return Double.parseDouble(getText());
			if (step > getSteps()) {
				return anim?min+(2*getSteps()-step)*delta:min;
			} else {
				return anim?min+step*delta:min;
			}
		}

		public int getInt(int step) {
			return (int)getValue(step);
		}

		public int getSteps() {
			return anim?(int)((max-min)/delta):0;
		}
	}

	public void debug(String s) {
		debug += s + "\n";
	}
	
	Panel p1,p2;
	
	public void init() {
		/*attr = new Vector<Attribute>();
		p1=new Panel();
		p2=new Panel();
		p1.setBackground(Color.white);
		p1.setLayout(new FlowLayout());
		p2.setBackground(Color.white);
		p2.setLayout(new FlowLayout());

		save=new Button("Save"); p1.add(save);
		p1.add(new Label("Name:"));
		attr.add(new Attribute("name",10));
		p1.add(attr.lastElement());
		name = attr.lastElement();
		name.setParse(false);

		p1.add(new Label("                   Iterations:"));
		attr.add(new Attribute("iter",5));
		p1.add(attr.lastElement());

		p1.add(new Label("Angle:"));
		attr.add(new Attribute("dang",5));
		p1.add(attr.lastElement());

		p1.add(new Label("Length:"));
		attr.add(new Attribute("dlen",5));
		p1.add(attr.lastElement());

		p1.add(new Label("Initial Length:"));
		attr.add(new Attribute("ilen",5));
		p1.add(attr.lastElement());


		makegif = new Button("Make Gif");
		///makegif.setDisabled(true);
		makegif.setActionCommand("disable");
		p2.add(makegif);

		p2.add(new Label("Initial Branches:"));
		attr.add(new Attribute("ibran",5));
		p2.add(attr.lastElement());

		p2.add(new Label("Branches:"));
		attr.add(new Attribute("branch",5));
		p2.add(attr.lastElement());

		p2.add(new Label("Offset:"));
		attr.add(new Attribute("offset",5));
		p2.add(attr.lastElement());

		p2.add(new Label("Delay:"));
		attr.add(new Attribute("delay",5));
		p2.add(attr.lastElement());
		delay = attr.lastElement();
		delay.setParse(false);

		p2.add(new Label("Rotate:"));
		attr.add(new Attribute("rotate",5));
		p2.add(attr.lastElement());
		rotation = attr.lastElement();
		rotation.setParse(false);

		p2.add(new Label("Display:"));
		attr.add(new Attribute("color",10));
		p2.add(attr.lastElement());
		display = attr.lastElement();
		display.setParse(false);

		add(p1);
		add(p2);

		for(int i=0; i<attr.size(); i++) {
			//attr.get(i).addTextListener(this);
			//attr.get(i).setText(getParameter(attr.get(i).getIden()));
		}
		*/
		//process();
	}

	public void popupWindow(String url) {
		try {
			getAppletContext().showDocument(new URL(url),"_blank");
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			debug = sw.toString();
		}
	}

	public boolean action(Event e, Object src) {
		if (e.target == save) save();
	//	if (e.target == makegif) sendGif();
		process();
		return true;
	}

	public void minmaxreset() {
		fminx = (int)size().getWidth();
		fminy = (int)size().getHeight();
		fmaxx = 0;
		fmaxy = 0;
	}

	public void process() {
		minmaxreset();
		fractalSteps = 0;
		cstep = 0;
		animate = false;

		for (int i=0; i<attr.size(); i++) {
			Attribute a = attr.get(i);
			if (a.isParse()) {
				a.read();
				if (a.isAnim()) {
					animate = true;
					if (a.getSteps() > fractalSteps) fractalSteps = a.getSteps();
				}
			}
		}

		String rotatetext=rotation.getText();
		if (rotatetext.indexOf("d")!=-1) {
			String[] split=rotatetext.split("d");
			animate=true;
			currang = 0;
			rot=Double.valueOf(split[0]).doubleValue()*Math.PI/180;
		} else {
			currang=asDouble(rotation)*Math.PI/180;
			rot=0;
		}
		
		String displaytext=display.getText();
		norm=true; dot=false; dotbranch=false; dynamic=false;
		if (displaytext.indexOf("dot")!=-1) {norm=false; dot=true; dotbranch=false;}
		dotsize=1;
		square=true;
		if (displaytext.indexOf("[")!=-1) {
			dotsize=Integer.parseInt(displaytext.substring(displaytext.indexOf("[")+1,displaytext.indexOf("]")));
			square=true;
		}
		if (displaytext.indexOf("{")!=-1) {
			dotsize=Integer.parseInt(displaytext.substring(displaytext.indexOf("{")+1,displaytext.indexOf("}")));
			square=false;
		}
		if (displaytext.indexOf("branch")!=-1) {norm=false; dot=false; dotbranch=true;}
		if (displaytext.indexOf("norm")!=-1) norm=true;
		if (displaytext.indexOf("dynamic")!=-1) dynamic=true;
		if (displaytext.indexOf("(")!=-1) {
			String[] carray=displaytext.substring(displaytext.indexOf("(")+1,displaytext.indexOf(")")).split(",");
			color=new Color(Integer.parseInt(carray[0]),Integer.parseInt(carray[1]),Integer.parseInt(carray[2]));
		}
	
		try {	
			tdelay=Integer.parseInt(delay.getText());
		} catch (Exception e) {
			tdelay = 50;
		}
		repaint();
	}

	public String getFractalURL() {
		String acc = "?";
		for(int i=0; i<attr.size(); i++) {
			if (acc.length() > 1) acc+="&";
			acc += attr.get(i).toString();
		}
		return acc;
	}
	
	
	private void save() {
		String acc = "save.php?";
		for(int i=0; i<attr.size(); i++) {
			if (i != 0) acc+="&";
			acc += attr.get(i).toString();
		}
		try {
			URL url = new URL(getDocumentBase(),acc);
			Object content = url.getContent();
		 } catch (final Exception e) {}
	}
	
	private double asDouble(TextField txt) {
		try {
			return Double.valueOf(txt.getText()).doubleValue();
		} catch (Exception e) {
			return 0;
		}
	}
	
	private String rembrack(String input) {
		String ret=input;
		if (input.indexOf("<")!=-1 && input.indexOf(">")!=-1) {
			String tempa[]=input.split("<");
			ret=tempa[0];
		}
		return ret;
	}
	
	private String parsebrack(String input) {
		String ret="0";
		if (input.indexOf("<")!=-1 && input.indexOf(">")!=-1) {
			ret=input.substring(input.indexOf("<")+1,input.indexOf(">"));
		}
		return ret;
	}
	
	public void paint(Graphics g) {
		offscreen.setColor(Color.black);
		offscreen.fillRect(0,0,width,height);
		offscreen.setColor(new Color(0,255,0));
		if (debug != null && debug.length() > 0) {
			String[] lines = debug.split("\n");
			for(int i=0; i<lines.length; i++) {
				offscreen.drawString(lines[i],50,110+12*i);
			}
		}
		paintFractal(offscreen,cstep);
		g.drawImage(ioffscreen,0,0,this);
	}

	public void paintFractal(Graphics g, int step) {
		g.setColor(color);
		double ilen = attr.get(I_ILEN).getValue(step);
		int ibranch = attr.get(I_IBRAN).getInt(step);
		double iter = attr.get(I_ITER).getValue(step);
		if (iter>20) {iter=20;}
		for (int i=0; i<ibranch; i++) {
			double fractalrot = 0;
			if (rot == 0) {
				fractalrot = currang;
			} else {
				fractalrot = step*rot;
			}

			double percent = 1;
			if (iter < 1) percent = iter;
			branch(fracx,fracy,percent*ilen,2*i*Math.PI/ibranch+currang+step*rot,iter,g,step);
		}
	}
	
	public void update(Graphics g)
	{
		paint(g);
	}

	public void minmax(int x, int y) {
		if (x < fminx) {
			fminx = x;
		}
		if (x > fmaxx) {
			fmaxx = x;
		}
		if (y < fminy) {
			fminy = y;
		}	
		if (y > fmaxy) {
			fmaxy = y;
		}
	}

	private void branch(double ix,double iy,double len,double ang,double iter,Graphics g, int step) {
		int i;
		double fx=ix+len*Math.cos(ang);
		double fy=iy+len*Math.sin(ang);
		Color ctemp=java.awt.Color.getHSBColor((float)iter*24/360,(float)1,(float)1);
		if (dynamic) g.setColor(ctemp);
		minmax((int)ix,(int)iy);
		minmax((int)fx,(int)fy);
		if (norm) g.drawLine((int)ix,(int)iy,(int)fx,(int)fy);
		if (dotbranch) {
			if (!square) {
				g.fillOval((int)(fx-dotsize/2),(int)(fy-dotsize/2),dotsize,dotsize);
			} else {
				g.fillRect((int)(fx-dotsize/2),(int)(fy-dotsize/2),dotsize,dotsize);
			}
		}
		if (iter>0) {
			int thisbranch=attr.get(I_BRANCH).getInt(step); // add randomness: -brrand+(int)(2*Math.random()*brrand);
			//double randval=attr.get(I_DANG).getValue(step); // add randomness: +2*Math.random()*dAngrand;
			double offset=Math.toRadians(attr.get(I_OFFSET).getValue(step));
			double dang=Math.toRadians(attr.get(I_DANG).getValue(step));
			double sang=ang-offset-(thisbranch-1)*(dang/2); // add randomness-offsetrand+2*offsetrand*Math.random()-(thisbranch-1)*(dAng-randval)/2;
			double cang=dang;
			double dlen = attr.get(I_DLEN).getValue(step);
			for (i=1; i<=thisbranch; i++) {
				double percent = 1;
				if (iter < 1) percent = iter;
				branch(fx,fy,percent*len*dlen,sang+cang*(i-1),iter-1,g,step);
			}
		} else if (dot) {
			if (!square) {
				g.fillOval((int)(fx-dotsize/2),(int)(fy-dotsize/2),dotsize,dotsize);
			} else {
				g.fillRect((int)(fx-dotsize/2),(int)(fy-dotsize/2),dotsize,dotsize);
			}
		}
	}

	public void start() {
		//double buffer
		width=size().width;
		height=size().height;
		ioffscreen=createImage(width,height);
		offscreen=ioffscreen.getGraphics();
		timer = new Thread(this);
		timer.start();
	}
	
	public void stop() {
		timer = null;
	}

	public boolean keyDown(Event e, int key) {
		showStatus(key+"");
		//s key
		if (key==115) {
		}
		if (key==122) {norm=true; dot=false; dotbranch=false;} 
		if (key==120) {norm=false; dot=true; dotbranch=false;} 
		if (key==99) {norm=false; dot=false; dotbranch=true;} 
		repaint();

		return false;
	}

	public boolean mouseMove(Event e,int x, int y) {
		//showStatus("("+x+","+y+")");
		return false;
	}
	public boolean mouseDown(Event e,int x, int y) {
		drag=false;
		if (y>80) drag=true;
		dx=x-fracx;
		dy=y-fracy;
		return false;
	}
	public boolean mouseUp(Event e,int x, int y) {
		return false;
	}
	public boolean mouseDrag(Event e,int x, int y) {
		drag=false;
		//showStatus("("+x+","+y+")");
		if (y>80) drag=true;
		if (drag) {
			fracx=x-dx;
			fracy=y-dy;
			minmaxreset();
			update(getGraphics());
		}
		return false;
	}
	public void run() {
		cstep = 0;
		repaint();
		while (true) {
			while(!animate) {
				try {
					timer.sleep(tdelay);
				} catch (InterruptedException e) { ; }
				cstep = 0;
			}
			cstep++;

			repaint();
			try {
				timer.sleep(tdelay);
			} catch (InterruptedException e) { ; }
		}
	}
	
}

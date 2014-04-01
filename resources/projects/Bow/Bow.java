//Copyright (c) Julian Hartline 2006
import java.util.*;
import java.awt.*;
import java.applet.*;
import java.text.*;
import java.math.*;

public class Bow extends Applet implements Runnable {
	private volatile Thread timer;
	double spin=0;
	boolean raytrace=false;
	boolean pause=false;
	boolean multi;
	boolean volley;
	boolean store=false;
	int width;
	int height;
	int mpart=1000;
	int arrows;
	int bw=100,bh=100,bxs=30,bys=30;
	int ox=50,oy=50;
	int sx=200;
	int sy=50;
	int sw=width-sx*2;
	int sh=height-100;
	int mobcount = 0;
	
	Dimension dim;
	Image offscreen;
	Graphics g;
	
	//single, multi, volley
	
	int[] pslvl=new int[] {0,0,0};
	int[] slvl=new int[] {20,20,20};
	int[] maxskill=new int[] {100,50,150};
	int[] purchased=new int[] {0,0,0};
	String[] skillname=new String[] {"Single","Multi-shot","Volley"};
	int[][] skillgains=new int[][] {
		{0,50,100,150},
		{0,5,15,50,100,150,250},
		{0,25,50,100}
	};
	
/*	int[][] skillgains=new int[][] {
		{0,100,200,300},
		{0,25,50,100,150,200,300},
		{0,100,200,300}
	};*/
	
	/*int[][] skillnumbers=new int[][] {
		{1,2,3,4,5,6,7,8,9},
		{2,3,4,5,6,7,8,9,10},
		{2,3,4,5,6,7,8,9}
	};*/
	int[][] skillnumbers=new int[][] {
		{1,2,3,4,5,6,7,8,9},
		{2,3,4,5,6,7,8,9,10},
		{2,3,4,5,6,7,8,9}
	};

	
	int[] skill=new int[] {skillnumbers[0][0],skillnumbers[1][0],skillnumbers[2][0]};


	String[] sslist= new String[] {"Arrows","Multi-shot","Volley"};

	//string,price,type,value,lvlreq
	//type: -1= arrows
	//		0= single upgrade
	//		1= multi upgrade
	//		2= volley upgrade
	int[][] stock=new int[][] {
	//	string,price,type,value,lvlreq,picture
		{0,20,-1,10,0,0}, //2
		{0,150,-1,100,0,1},//1.5
		{0,600,-1,500,0,2},//1.2
		{0,1000,-1,1000,0,3},//1
		{1,400,1,1,3,4},
		{1,600,1,2,6,5},
		{1,1000,1,3,10,6},
		{1,1800,1,4,20,7},
		{2,400,2,1,3,8},
		{2,600,2,2,6,9},
		{2,1000,2,3,10,10},
		{2,1800,2,4,20,11}
	};
	
	int level=1;
	int ammo=200;
	int score=0;
	int cash=100;
	//****
	//int cash=10000;
	//int ammo=10000;
	//****
	double between=Math.PI/18;
	int sprds=50;
	double arrowmass=1;
	double enemymass=50;
	int walls[][];
	int nwalls=10;
	int mmobs=100;
	int spawntime=0;

	int leng=15;
	//level stuff
	int multivolleysmade,singlevolleysmade,singleattacks,multiattacks,kills,totalshots; 

	double[] ax,ay; //arrow values
	double[] xvel,yvel,aangle;
	double[] axo,ayo,aao;
	double[] aalpha;
	int[] state;
	int[] hitmob;
	int[] hitlimb;
	
	double[] bx,by;
	double[] bxvel,byvel;
	int[] bstate;
	int[] bsize;
	double[] bz;
	int mblood=1000;
	
	int[] zonex,zoney,zonew,zoneh,zonestate;
	int mzones=100;
	
	double[] mobx,moby,mobxo,mobyo; //mob values
	double[] mobscale;
	int[] mobframe;
	int[] mobstate,mobstate2;
	double[][] moblimbs;
	double[] mobz;
	int[] mobaspeed;
	int[] bleed;
	int[] mobhp;
	int[] mobtween;
	int[] anim;
	double[] mobalpha;
	boolean[] isanimating;
	
	double[] rockx,rocky,rockxv,rockyv;
	int[] rockstate;
	int mrocks=100;
	
	String[] text;
	double[] textx,texty,textalpha;
	int[] textlife, texttype,textstate;
	int[][] textcolor;
	int mtext=20;
	
	
	int[] volleyx,volleyy,volleytime,volleycount,volleyrst;
	double[] volleyang,volleyvel;
	int mvolley=100;
	
	//[bodyang,armang1,armang2,armlower1,armlower2,legang1,legang2,leglower1,leglower2]
	int neckx;
	int necky;
	
	int elbow1x;
	int elbow1y;
	int elbow2x;
	int elbow2y;
	
	int hand1x;
	int hand1y;
	int hand2x;
	int hand2y;
	
	int waistx;
	int waisty;
	
	int kneex1;
	int kneey1;
	int kneex2;
	int kneey2;
	
	int foot1x;
	int foot1y;
	int foot2x;
	int foot2y;

    int head=10;
    int bleng=20;
    int auleng=13;
    int alleng=13;
    int luleng=15;
	int llleng=10;

	double length=30;
	int mousex,mousey,clickx,clicky;
	boolean mousedown;
	String debug;
	
	//TextField stickout;
	//Panel p1;
	int rotate;
	double ival;
	int[] joint=new int[] {9,9,9,1,2,0,0,5,6,-1};
	int[] arrx;
	int[] arry;
	double[] angl;
	
	int mshot=100;
	double[] dwx,dwy,xv,yv;
	
	int lmhit,llhit;
	//[bodyang,armang1,armang2,armlower1,armlower2,legang1,legang2,leglower1,leglower2]
	
	double[][][] animation=new double[][][] {
		{//walk  (0)
			{0.0,0.1354,-0.24,0.0,0.0,-0.1806,0.0577,0.5894,0.0,1.0,0,0},
			{0.0,0.2618,-0.3206,0.0,0.0,-0.4006,0.389,0.5894,0.0,1.0,0,0},
			{0.0,0.3914,-0.3685,0.2232,-0.1835,-0.5055,0.4181,0.3704,0.0,1.0,0,0},
			{0.0,0.2618,-0.3339,0.2402,-0.2127,-0.3901,0.219,0.128,0.4561,1.0,0,0},
			{0.0,0.0385,-0.185,0.0,0.0,-0.1553,-0.0697,0.0508,0.7624,1.0,0,0},
			{0.0,-0.1506,0.1849,0.1743,-0.1245,0.1717,-0.3443,0.0508,1.0245,1.0,0,0},
			{0.0,-0.4352,0.3884,0.1988,-0.2598,0.4177,-0.5185,0.0508,0.7748,1.0,0,0},
			{0.0,-0.441,0.491,0.0,0.0,0.5167,-0.468,0.0508,0.2974,1.0,0,0},
			{0.0,-0.5492,0.5444,0.0,0.0,0.3278,-0.2849,0.4579,0.0215,1.0,0,0},
			{0.0,-0.1615,0.1896,0.0,0.0,0.0029,-0.0717,0.5324,0.0215,1.0,0,0},
			{0,0,0,0,0,0,0,0,0,0,0}
		},
		{//die  (1)
			{0.0,0.1059,-0.4134,-0.6077,-0.34,0.1044,-0.3826,0.3094,0.5531,1.0,0.0,0.0},
			{0.0,-0.0515,-0.535,-1.2494,-0.7298,0.1044,-0.3826,0.9299,1.348,1.0,6.666666666666667,6.0},
			{0.0,-0.2531,-0.8867,-0.9692,-0.7298,0.1044,-0.3826,1.2793,1.7864,1.0,8.333333333333334,11.0},
			{0.0,-0.2531,-0.8867,-0.9692,-0.7298,0.1044,-0.3826,-4.8524,1.9095,1.0,8.0,13.333333333333334},
			{0.0,-0.2531,-0.8867,-0.9692,-0.7298,0.1044,-0.3826,-4.8524,1.9095,1.0,8.0,13.333333333333334},
			{0.0,-0.2531,-0.8867,-0.9692,-0.7298,0.1044,-0.3826,-4.8524,1.9095,1.0,8.0,13.333333333333334},
			{0.2026,-0.4393,-1.3018,-0.8745,-0.391,0.2195,0.0575,1.1451,1.3466,1.0,22.666666666666668,15.0},
			{0.5293,-1.0937,-1.7721,-0.4842,-0.3863,0.0107,-0.0432,7.2528,13.6032,1.0,33.666666666666664,19.0},
			{0.8203,-1.5165,-2.2861,-0.3735,-0.1171,0.0107,-0.0432,6.9701,19.6186,1.0,41.0,26.666666666666668},
			{1.4724,-2.8401,-3.0383,-0.2449,0.0352,6.2893,0.1086,12.5901,25.2363,1.0,47.0,49.666666666666664},
			{0,0,0,0,0,0,0,0,0,0,0}
		},
		{//climb   (2)
			{0.0,-0.2806,-2.7129,-1.8955,0.0,-0.2899,-2.5275,0.1477,2.3876,1.0,0.0,0.0},
			{0.0,-0.6989,-1.6511,-2.0982,-1.5053,-1.0761,-1.4388,1.5067,1.553,1.0,0.0,0.0},
			{0.0,-1.8838,-0.8614,-1.0666,-1.8994,-1.8397,-0.8821,1.7352,0.7223,1.0,0.0,0.0},
			{0.0,-2.4027,-0.3058,-0.1738,-1.7968,-2.6069,-0.5674,2.0922,0.1106,1.0,0.0,0.0},
			{0.0,-1.8457,-1.0851,-0.8966,-2.1173,-2.0492,-0.9105,1.8837,1.8827,1.0,0.0,0.0},
			{0.0,-1.3045,-1.8866,-1.3758,-0.6005,-1.3552,-1.699,1.2656,2.0461,1.0,0.0,0.0},
			{0,0,0,0,0,0,0,0,0,0,0}
		},
		{//wall top  (3)
			{0.0,-2.2985,-1.9275,-0.503,-0.8346,-0.4114,-1.7012,0.0,1.7325,1.0,0.0,0.0},
			{-0.0161,-0.6068,-0.275,-2.2553,-2.1657,-1.2242,-0.272,1.6952,0.0136,1.0,4.6667,-19.3333},
			{-0.0161,0.6724,0.1322,-2.2425,-1.3276,-0.6071,-0.2784,0.4418,0.2165,1.0,8.6667,-32.0},
			{-0.0161,0.4434,0.1048,-1.0203,-0.6577,-0.2169,0.0093,0.283,0.0621,1.0,10.0,-39.6667},
			{0.8538,-5.459,0.5915,-2.4742,-2.0979,-0.872,-0.5459,0.283,0.0621,1.0,30.6667,-29.3333},
			{1.3228,-1.2693,-1.7827,-0.5152,-0.5497,-1.1422,-0.9238,0.283,0.0621,1.0,42.0,-25.6667},
			{7.7801,-1.8833,-1.3304,-0.5152,-1.3744,-1.0024,-1.4101,1.0084,1.4381,1.0,49.0,-28.3333},
			{1.5692,-1.3081,-2.1341,-1.5962,-1.0195,-2.0648,-1.3895,-4.1315,-4.8717,1.0,56.3333,-27.6667},
			{7.3272,-0.9297,-1.5843,-0.7079,0.0045,-1.1599,-0.8562,-4.5022,-4.8717,1.0,53.3333,-39.6667},
			{6.8883,-0.6116,-1.026,-0.3359,0.0045,-2.116,-0.365,-4.3772,-4.8717,1.0,45.0,-46.0},
			{6.2754,0.4634,0.186,-1.4961,-1.6448,-1.4118,0.224,-4.9472,-4.8717,1.0,30.0,-54.0},
			{6.2754,0.6675,-0.2477,-1.4961,-1.3619,-0.5718,0.224,-5.8762,0.4377,1.0,30.3333,-66.6667},
			{6.2754,0.7716,-0.2477,-1.2672,-0.4661,-0.4122,0.224,-6.175,0.0129,1.0,31.6667,-69.0},
			{0,0,0,0,0,0,0,0,0,0,0}
		},
		{//fall off wall  (4)
			{0.0,-2.4226,-1.4091,-0.3404,-1.4508,-0.5789,-1.843,0.3096,1.9726,1.0,0.0,0.0},
			{-0.5593,-3.3709,-1.9944,0.2596,-0.6828,0.0283,-1.4385,0.3096,1.4156,1.0,-22.0,19.3333},
			{-0.9042,2.1285,-2.3632,-5.8028,-0.1414,-0.2395,-1.1375,0.0113,0.3016,1.0,-38.6667,50.6667},
			{-1.5691,2.8427,-2.6589,-5.7344,0.0581,-0.0851,-0.5969,-0.5058,-0.1612,1.0,-48.6667,91.3333},
			{0,0,0,0,0,0,0,0,0,0,0}
		},
		{//falling   (5)
			{-1.5686,2.8035,-2.7322,0.151,0.1295,0.3794,-0.2465,-0.1038,-0.1032,1.0,0.0,0.3333},
			{-1.5686,3.6141,2.846,-0.0801,-6.2596,-0.3573,0.3559,-0.1038,-0.1032,1.0,0.0,0.3333},
			{0,0,0,0,0,0,0,0,0,0,0}
		},
		{//throwing   (6)
			{0.0,-4.689,-0.2618,-5.0654,-2.4666,0.2626,-0.2218,0.2907,0.0,1.0,0.0,0.0},
			{0.212,-2.5596,0.9319,-5.7117,-2.4666,-0.077,-0.2218,0.3218,0.0,1.0,5.6667,0.3333},
			{0.2926,-1.7286,1.2457,-6.2364,-2.0007,-0.1455,-0.2218,0.3218,0.0,1.0,8.6667,1.3333},
			{0.0714,-1.1167,0.9372,-5.8069,-2.434,0.1579,-0.2218,0.3218,0.0,1.0,2.6667,1.6667},
			{0.0714,-0.7939,0.3516,-6.183,-2.6511,0.2688,-0.3941,0.1027,0.0,1.0,2.6667,1.6667},
			{0.0714,-0.3626,0.1634,-6.6737,-2.6511,0.2688,-0.3941,0.1027,0.0,1.0,2.6667,1.6667},
			{0.0219,0.7051,-0.2793,-7.4219,-2.6511,0.2688,-0.3941,0.1027,0.0,1.0,1.6667,0.0},
			{0,0,0,0,0,0,0,0,0,0,0}
		}
		
	};
		//walk			(0)
		//die			(1)
		//climb			(2)
		//wall top		(3)
		//fall off wall	(4)
		//falling		(5)
		//throwing		(6)

	public void init() {
		setBackground(Color.white);
		dim=getSize();
		width=dim.width;
		height=dim.height;
		
		walls=new int[nwalls][10];
		
		ax=new double[mpart];
		ay=new double[mpart];
		axo=new double[mpart];
		ayo=new double[mpart];
		aao=new double[mpart];
		aangle=new double[mpart];
		aalpha=new double[mpart];
		xvel=new double[mpart];
		yvel=new double[mpart];
		state=new int[mpart];
		hitmob=new int[mpart];
		hitlimb=new int[mpart];
		
		mobx=new double[mmobs];
		moby=new double[mmobs];
		mobxo=new double[mmobs];
		mobyo=new double[mmobs];
		mobz=new double[mmobs];
		mobscale=new double[mmobs];
		mobframe=new int[mmobs];
		mobstate=new int[mmobs];
		mobstate2=new int[mmobs];
		moblimbs=new double[mmobs][9];
		mobtween=new int[mmobs];
		mobhp=new int[mmobs];
		anim=new int[mmobs];
		bleed=new int[mmobs];
		mobaspeed=new int[mmobs];
		isanimating=new boolean[mmobs];
		mobalpha=new double[mmobs];
		
		bx=new double[mblood];
		by=new double[mblood];
		bz=new double[mblood];
		bxvel=new double[mblood];
		byvel=new double[mblood];
		bstate=new int[mblood];
		bsize=new int[mblood];
		
		dwx=new double[mshot];
		dwy=new double[mshot];
		xv=new double[mshot];
		yv=new double[mshot];
		
		zonex=new int[mzones];
		zoney=new int[mzones];
		zonew=new int[mzones];
		zoneh=new int[mzones];
		zonestate=new int[mzones];
		
		rockx=new double[mrocks];
		rocky=new double[mrocks];
		rockxv=new double[mrocks];
		rockyv=new double[mrocks];
		rockstate=new int[mrocks];
		
		volleyx=new int[mvolley];
		volleyy=new int[mvolley];
		volleyang=new double[mvolley];
		volleyvel=new double[mvolley];
		volleytime=new int[mvolley];
		volleycount=new int[mvolley];
		volleyrst=new int[mvolley];

		
		text=new String[mtext];
		textx=new double[mtext];
		texty=new double[mtext];
		textalpha=new double[mtext];
		textlife=new int[mtext];
		texttype=new int[mtext];
		textstate=new int[mtext];
		textcolor=new int[mtext][3];
		
		//x,y = center of head
		//[bodyang,armang1,armang2,legang1,legang2,leglower1,leglower2]
		newwall(500,450,2,5,50,50,2,2,10);
		//spawnmob(400,550,1,0,2,0,0,15);
		//spawnmob(440,451,1,0,2,0,0,15);
		newzone(660,450,340,250); //behind wall
		//newzone(900,40,100,400); //upright
		
		offscreen=createImage(width,height);
	}
	
	public void paint(Graphics gfx) {
		Graphics g = offscreen.getGraphics();
		g.clearRect(0,0,width,height);
		
		g.setFont(new Font("Arial",Font.BOLD,15));
		drawzones(g);
		g.setColor(new Color(0,0,0));
		if (mousedown) {
			g.drawLine(clickx,clicky,mousex,mousey);
			if (clickx!=mousex || clicky!=mousey) {
				double dist=distance(clickx,clicky,mousex,mousey);
				double ac=.1;
				g.drawString(""+((double)(int)(dist*10)/100),mousex+10,mousey+10);
				g.drawString(""+(int)deg(angle(clickx,clicky,mousex,mousey))+"i",clickx+10,clicky+10);
				int s=0,clr,i;
				double btw=between*sprds/dist;
				boolean aodd=true;
				if (arrows % 2==0) aodd=false;
				double start=-arrows*btw/2+((aodd)?btw/2:0);
				for (i=0; i<arrows; i++) {
					dwx[i]=clickx;
					dwy[i]=clicky;
					xv[i]=makexv(angle(clickx,clicky,mousex,mousey)+start+i*btw,dist/20);
					yv[i]=makeyv(angle(clickx,clicky,mousex,mousey)+start+i*btw,dist/20);
				}
				while (s<1000) {
					clr=s*3/2;
					if (clr<=255) g.setColor(new Color(255,clr,clr));
					for (i=0; i<arrows; i++) {
						g.drawLine((int)dwx[i],(int)dwy[i],(int)(dwx[i]+xv[i]),(int)(dwy[i]+yv[i]));
						dwx[i]+=xv[i];
						dwy[i]+=yv[i];
						yv[i]+=ac;
					}
					s++;
				}
			}
			g.setColor(new Color(0,0,0));
			myfillOval(clickx,clicky,7,7,g);
			myfillOval(mousex,mousey,7,7,g);
		}
		drawhud(g);
		drawarrow(g);
		drawrocks(g);
		drawblood(g);
		drawscenery(g);
		drawmobs(g);
		drawlevel(g);
		if (store) drawstore(g);
		drawtext(g);
		if (debug!=null) g.drawString(""+debug,10,10);
		
		gfx.drawImage(offscreen,0,0,this);
	}
	
	public void update(Graphics screen) {
		paint(screen);
	}
	
	public void drawhud(Graphics g) {
		int i;
		int orx=650;
		int ory=20;
		g.drawString("Score: "+score,orx,ory);
		orx+=120;
		g.drawString("Cash: $"+cash,orx,ory);
		orx+=120;
		g.drawString("Arrows: "+ammo,orx,ory);
		for (i=0; i<=2; i++) {
			g.drawString(skillname[i]+": "+slvl[i],10,20+15*i);
		}
		//g.drawString("Kills="+kills+"  Shots="+totalshots+"   Single Attacks="+singleattacks+"   Multi Attacks="+multiattacks+"   Single Volleys Fired="+singlevolleysmade+"   Multi Volleys Fired="+multivolleysmade,100,100);
		if (pause) g.drawString("Paused",500,300);
	}
	
	public void drawstore(Graphics g) {
		int xi,yi,i;
		g.setFont(new Font("Arial",Font.BOLD,20));
		g.setColor(new Color(255,255,255));
		g.fillRoundRect(sx,sy,sw,sh,50,50);
		g.setColor(new Color(0,0,0));
		g.drawRoundRect(sx,sy,sw,sh,50,50);
		center("Shop",sx+sw/2,sy+30,g);
		for (xi=0; xi<=3; xi++) {
			for (yi=0; yi<=3; yi++) {
				g.drawRoundRect(sx+ox+xi*bw+xi*bxs,sy+oy+yi*bh+yi*bys,bw,bh,20,20);
			}
		}
		g.setFont(new Font("Arial",Font.BOLD,12));
		int tbx=0;
		int tby=0;
		for (i=0; i<stock.length; i++) {
			drawinfo(sslist[stock[i][0]]+" "+((stock[i][2]==-1)?"x":"")+((stock[i][2]!=-1)?"lvl ":"")+stock[i][3],"$"+stock[i][1],tbx,tby,(stock[i][2]!=-1 && stock[i][4]>slvl[stock[i][2]]),g);
			drawpicture(tbx,tby,stock[i][5],g);
			tbx++;
			if (tbx>3) {tbx=0; tby++;}
		}
	}
	
	public void drawpicture(int tbx, int tby, int picture, Graphics g) {
	int i;
		switch (picture) {
			case 0:
				drawthisarrow(sx+ox+(tbx)*bw+(tbx)*bxs+bw/2,sy+oy+(tby)*bh+(tby)*bys+bh/2,rad(225),20,g);
			break;
			case 1:
				for (i=0; i<=1; i++) {
					drawthisarrow(sx+ox+(tbx)*bw+(tbx)*bxs+bw/2-11+i*13,sy+oy+(tby)*bh+(tby)*bys+bh/2,rad(225),20,g);
				}
			break;
			case 2:
				for (i=0; i<=2; i++) {
					drawthisarrow(sx+ox+(tbx)*bw+(tbx)*bxs+bw/2-13+i*13,sy+oy+(tby)*bh+(tby)*bys+bh/2,rad(225),20,g);
				}
			break;
			case 3:
				for (i=0; i<=3; i++) {
					drawthisarrow(sx+ox+(tbx)*bw+(tbx)*bxs+bw/2-24+i*13,sy+oy+(tby)*bh+(tby)*bys+bh/2,rad(225),20,g);
				}
			break;
			case 4:
				for (i=0; i<=2; i++) {
					drawthisarrow(sx+ox+(tbx)*bw+(tbx)*bxs+bw/2,sy+oy+(tby)*bh+(tby)*bys+bh/2,rad(225-50+i*25-25/2),20,g);
				}
			break;
			case 5:
				for (i=0; i<=3; i++) {
					drawthisarrow(sx+ox+(tbx)*bw+(tbx)*bxs+bw/2,sy+oy+(tby)*bh+(tby)*bys+bh/2,rad(225-50+i*25-25),20,g);
				}
			break;
			case 6:
				for (i=0; i<=4; i++) {
					drawthisarrow(sx+ox+(tbx)*bw+(tbx)*bxs+bw/2,sy+oy+(tby)*bh+(tby)*bys+bh/2,rad(225-50+i*25-75/2),20,g);
				}
			break;
			case 7:
				for (i=0; i<=5; i++) {
					drawthisarrow(sx+ox+(tbx)*bw+(tbx)*bxs+bw/2,sy+oy+(tby)*bh+(tby)*bys+bh/2,rad(225-50+i*20-40),20,g);
				}
			break;
			case 8:
				for (i=0; i<=2; i++) {
					drawthisarrow(sx+ox+(tbx)*bw+(tbx)*bxs+bw/2,sy+oy+(tby)*bh+(tby)*bys+bh/2,rad(225-50+i*25),20,g);
				}
			break;
			case 9:
				for (i=0; i<=2; i++) {
					drawthisarrow(sx+ox+(tbx)*bw+(tbx)*bxs+bw/2,sy+oy+(tby)*bh+(tby)*bys+bh/2,rad(225-50+i*25),20,g);
				}
			break;
			case 10:
				for (i=0; i<=2; i++) {
					drawthisarrow(sx+ox+(tbx)*bw+(tbx)*bxs+bw/2,sy+oy+(tby)*bh+(tby)*bys+bh/2,rad(225-50+i*25),20,g);
				}
			break;
			case 11:
				for (i=0; i<=2; i++) {
					drawthisarrow(sx+ox+(tbx)*bw+(tbx)*bxs+bw/2,sy+oy+(tby)*bh+(tby)*bys+bh/2,rad(225-50+i*25),20,g);
				}
			break;
		}
	}
	
	public void center(String s, int x, int y, Graphics g) {
		g.drawString(s,x-g.getFontMetrics().stringWidth(s)/2,y);
	}
	
	public void drawinfo(String name,String price, int boxx,int boxy,boolean flag,Graphics g) {
		int sx=200;
		int sy=50;
		int sw=width-sx*2;
		int sh=height-100;
		int bw=100,bh=100,bxs=30,bys=30;
		int ox=50,oy=50;
		g.setColor(new Color(0,0,0));
		if (flag) g.setColor(new Color(255,0,0));
		center(name,sx+ox+(boxx)*bw+(boxx)*bxs+bw/2,sy+oy+(boxy)*bh+(boxy)*bys+bh-2,g);
		center(price,sx+ox+(boxx)*bw+(boxx)*bxs+bw/2,sy+oy+(boxy)*bh+(boxy)*bys+12,g);
	}
	
	public void drawzones(Graphics g) {
		int i,w,h;
		g.setColor(new Color(0,155,0));
		int	sqw=15;
		for (i=0; i<mzones-1; i++) {
			if (zonestate[i]!=0) {
				for (h=0; h<=zoneh[i]/sqw-1; h++) {
					for (w=0; w<=zonew[i]/sqw-1; w++) {
						g.drawOval((zonew[i]%sqw)/2+zonex[i]+w*sqw,(zoneh[i]%sqw)/2+zoney[i]+h*sqw,sqw,sqw);
					}
				}
			}
		}
	}
	
	public void dpat(int i, int sqw, Graphics g) {
		int w,h;
		for (h=0; h<=zoneh[i]/sqw; h++) g.drawLine(zonex[i],zoney[i]+h*sqw,zonex[i]+(zoneh[i]-h*sqw),zoney[i]+zoneh[i]);
		for (w=0; w<=zonew[i]/sqw; w++) g.drawLine(zonex[i]+w*sqw,zoney[i],zonex[i]+zonew[i],zoney[i]+(zonew[i]-w*sqw));
	}
	
	public void drawarrow(Graphics g) {
		int i;
		for (i=0; i<mpart-1; i++) {
			if (state[i]!=0) {
		   		int c=(int)(255-(255*aalpha[i]/100));
			   	g.setColor(new Color(c,c,c));
				drawthisarrow(ax[i],ay[i],aangle[i],15,g);
			}
		g.setColor(new Color(255,255,255));
		}
	}

	public void drawthisarrow(double x, double y, double angle, int aleng, Graphics g) {
		double a=angle;
    	double a2=a+Math.PI/6;
    	double a3=a-Math.PI/6;
    	double tipx=x+aleng*Math.cos(a);
    	double newtipx=tipx+aleng*Math.cos(a2-a)*Math.cos(a)/2;
    	double tipy=y+aleng*Math.sin(a);
		double newtipy=tipy+aleng*Math.cos(a2-a)*Math.sin(a)/2;
    	tipx=newtipx;
 	   	tipy=newtipy;
		double tailx=x-aleng*Math.cos(a);
	   	double taily=y-aleng*Math.sin(a);
	   	g.drawLine((int)(tipx),(int)(tipy),(int)(tailx),(int)(taily)); //shaft
	   	g.drawLine((int)(tipx),(int)(tipy),(int)(tipx-aleng*Math.cos(a2)/2),(int)(tipy-aleng*Math.sin(a2)/2)); //head
	   	g.drawLine((int)(tipx),(int)(tipy),(int)(tipx-aleng*Math.cos(a3)/2),(int)(tipy-aleng*Math.sin(a3)/2)); //head
	   	g.drawLine((int)(tailx),(int)(taily),(int)(tailx-aleng*Math.cos(a2)/2),(int)(taily-aleng*Math.sin(a2)/2)); //tail
	   	g.drawLine((int)(tailx),(int)(taily),(int)(tailx-aleng*Math.cos(a3)/2),(int)(taily-aleng*Math.sin(a3)/2)); //tail
//		g.setColor(new Color(255,0,0));
//	   	g.drawLine((int)tipx,(int)tipy,(int)newtipx,(int)newtipy);
	}
	
	public void drawrocks(Graphics g) {
		int i;
		g.setColor(new Color(0,0,0));
		for (i=0; i<=mrocks-1; i++) {
			if (rockstate[i]!=0) {
				myfillOval((int)rockx[i],(int)rocky[i],5,5,g);
			}
		}
		g.setColor(new Color(255,255,255));
	}
	
	public void drawblood(Graphics g) {
		int i;
		g.setColor(new Color(255,0,0));
		for (i=0; i<=mblood-1; i++) {
			if (bstate[i]!=0) {
				g.drawLine((int)(bx[i]-5*bxvel[i]),(int)(by[i]-5*byvel[i]),(int)(bx[i]+5*bxvel[i]),(int)(by[i]+5*byvel[i]));
			}
		}
	}
	
	public void drawlevel(Graphics g) {
		int i;
		for (i=0; i<=nwalls-1; i++) {
			if (walls[i][0]==1) drawwall(walls[i][1],walls[i][2],walls[i][3],walls[i][4],walls[i][5],walls[i][6],walls[i][7],walls[i][8],walls[i][9],g);
		}
	}
	
	public void drawscenery(Graphics g) {
		
	}
	
	public void drawtext(Graphics g) {
		int i;
		for (i=0; i<=mtext-1; i++) {
			if (textstate[i]!=0) {
				int red=(int)(255-((255-textcolor[i][0])*textalpha[i]/100));
				int green=(int)(255-((255-textcolor[i][1])*textalpha[i]/100));
				int blue=(int)(255-((255-textcolor[i][2])*textalpha[i]/100));				
				//g.setColor(new Color(0,0,0));
				g.setColor(new Color(red,green,blue));
				center(/*"red:"+red+" green:"+green+" blue:"+blue+"  "+*/text[i],(int)textx[i],(int)texty[i],g);
				//g.fillOval((int)textx[i]-50,(int)texty[i],10,10);
			}
		}
	}
	
	public void drawmobs(Graphics g) {
		int i;
		g.setColor(new Color(255,255,255));
		for (i=0; i<=mmobs-1; i++) {
			if (mobstate[i]!=0) {
				drawthismob(mobx[i]+mobxo[i],moby[i]+mobyo[i],mobscale[i],mobstate[i],moblimbs[i][0],moblimbs[i][1],moblimbs[i][2],moblimbs[i][3],moblimbs[i][4],moblimbs[i][5],moblimbs[i][6],moblimbs[i][7],moblimbs[i][8],mobalpha[i],g);
			}
		}
	}
	
	public void drawthismob(double x, double y, double scl, int state, double b,double au1,double au2,double al1,double al2, double lu1, double lu2, double ll1, double ll2, double alpha, Graphics g) {
		int c=(int)(255-(255*alpha/100));
		g.setColor(new Color(c,c,c));
		generatethismob(x,y,scl,state,b,au1,au2,al1,al2,lu1,lu2,ll1,ll2);
		myOval((int)x,(int)y,(int)(head*scl),(int)(head*scl),g);
		
		g.drawLine(neckx,necky,waistx,waisty); //body
		//g.setColor(new Color(0,0,255));
		g.drawLine(neckx,necky,elbow1x,elbow1y); //armu1
		g.drawLine(elbow1x,elbow1y,hand1x,hand1y); //arml1
		g.drawLine(waistx,waisty,kneex1,kneey1); //legu1
		g.drawLine(kneex1,kneey1,foot1x,foot1y); //legl1
		//g.setColor(new Color(255,0,0));		
		g.drawLine(neckx,necky,elbow2x,elbow2y); //armu2
		g.drawLine(elbow2x,elbow2y,hand2x,hand2y); //arml2
		g.drawLine(waistx,waisty,kneex2,kneey2); //legu2
		g.drawLine(kneex2,kneey2,foot2x,foot2y); //legl2
		//int u=3;
		//myfillOval(arrx[u],arry[u],5,5,g);
		g.setColor(new Color(255,255,255));
	}
	
	public void generatenmob(int n) {
		generatethismob(mobxo[n]+mobx[n],mobxo[n]+moby[n],mobscale[n],mobstate[n],moblimbs[n][0],moblimbs[n][1],moblimbs[n][2],moblimbs[n][3],moblimbs[n][4],moblimbs[n][5],moblimbs[n][6],moblimbs[n][7],moblimbs[n][8]);
	}
	
	public void generatethismob(double x, double y, double scl, int state, double b,double au1,double au2,double al1,double al2, double lu1, double lu2, double ll1, double ll2) {
		neckx=(int)(x+.5*head*scl*Math.cos(b+Math.PI/2));
		necky=(int)(y+.5*head*scl*Math.sin(b+Math.PI/2));
		
		elbow1x=(int)(neckx+auleng*scl*Math.cos(b+au1+Math.PI/2));
		elbow1y=(int)(necky+auleng*scl*Math.sin(b+au1+Math.PI/2));
		elbow2x=(int)(neckx+auleng*scl*Math.cos(b+au2+Math.PI/2));
		elbow2y=(int)(necky+auleng*scl*Math.sin(b+au2+Math.PI/2));
		
		hand1x=(int)(elbow1x+alleng*scl*Math.cos(b+au1+al1+Math.PI/2));
		hand1y=(int)(elbow1y+alleng*scl*Math.sin(b+au1+al1+Math.PI/2));
		hand2x=(int)(elbow2x+alleng*scl*Math.cos(b+au2+al2+Math.PI/2));
		hand2y=(int)(elbow2y+alleng*scl*Math.sin(b+au2+al2+Math.PI/2));
		
		waistx=(int)(neckx+bleng*scl*Math.cos(b+Math.PI/2));
		waisty=(int)(necky+bleng*scl*Math.sin(b+Math.PI/2));
		
		kneex1=(int)(waistx+luleng*scl*Math.cos(b+lu1+Math.PI/2));
		kneey1=(int)(waisty+luleng*scl*Math.sin(b+lu1+Math.PI/2));
		kneex2=(int)(waistx+luleng*scl*Math.cos(b+lu2+Math.PI/2));
		kneey2=(int)(waisty+luleng*scl*Math.sin(b+lu2+Math.PI/2));
		
		foot1x=(int)(kneex1+luleng*scl*Math.cos(b+lu1+ll1+Math.PI/2));
		foot1y=(int)(kneey1+luleng*scl*Math.sin(b+lu1+ll1+Math.PI/2));
		foot2x=(int)(kneex2+luleng*scl*Math.cos(b+lu2+ll2+Math.PI/2));
		foot2y=(int)(kneey2+luleng*scl*Math.sin(b+lu2+ll2+Math.PI/2));
		
		int[] tx=new int[] {waistx,elbow1x,elbow2x,hand1x,hand2x,kneex1,kneex2,foot1x,foot2x,neckx};
		int[] ty=new int[] {waisty,elbow1y,elbow2y,hand1y,hand2y,kneey1,kneey2,foot1y,foot2y,necky};
		double[] ta=new double[] {b,b+au1,b+au2,b+au1+al1,b+au2+al2,b+lu1,b+lu2,b+lu1+ll1,b+lu2+ll2,b};

		angl=ta;
		arrx=tx;
		arry=ty;
	}
	
	public void drawwall(int x, int y, int nx, int ny, int cwidth, int cheight, int spacex,int spacey, int arc, Graphics g) {
		int i,l;
		g.setColor(new Color(0,0,0));
		g.fillRoundRect(x,y,(nx+1)*cwidth+(nx+1)*spacex,(ny+1)*cheight+(ny+1)*spacey,arc,arc);
		g.setColor(new Color(100,100,100));
		for (i=0; i<=nx; i++) {
			for (l=0; l<=ny; l++) {
				if (i % 2==0) {
					g.fillRoundRect(spacex/2+x+i*cwidth+i*spacex,spacey/2+y+l*cheight+l*spacey,cwidth,cheight,arc,arc);
				} else {
					if (l==0) {
						g.fillRoundRect(spacex/2+x+i*cwidth+i*spacex,spacey/2+y+l*cheight+l*spacey,cwidth,cheight/2,arc,arc);
					} else {
						g.fillRoundRect(spacex/2+x+i*cwidth+i*spacex,spacey/2+y+l*cheight+l*spacey-cwidth/2,cwidth,cheight,arc,arc);
					}
					if (l==ny) g.fillRoundRect(spacex/2+x+i*cwidth+i*spacex,spacey/2+y+l*cheight+l*spacey+cheight/2+spacey,cwidth,cheight/2-spacey,arc,arc);
				}
			}
		}
	}
	
	public void newwall(int x, int y, int nx, int ny, int cwidth, int cheight, int spacex,int spacey, int arc) {
		int i,p=-1;
		for (i=0; i<nwalls-1;i++) {if (walls[i][0]==0) {p=i; break;}}
		walls[p][0]=1;
		walls[p][1]=x;
		walls[p][2]=y;
		walls[p][3]=nx;
		walls[p][4]=ny;
		walls[p][5]=cheight;
		walls[p][6]=cwidth;
		walls[p][7]=spacex;
		walls[p][8]=spacey;
		walls[p][9]=arc;
	}
	
	public void newzone(int x,int y, int w, int h) {
		int i,p=-1;
		for (i=0; i<mzones-1;i++) {if (zonestate[i]==0) {p=i; break;}}
		zonex[p]=x;
		zoney[p]=y;
		zonew[p]=w;
		zoneh[p]=h;
		zonestate[i]=1;
	}
	
	public void spawnrock(double x, double y, double angle, double velocity) {
		int i,p=-1;
		for (i=0; i<mrocks-1;i++) {if (rockstate[i]==0) {p=i; break;}}	
		if (p!=-1) {
			rockx[p]=x;
			rocky[p]=y;
			rockxv[p]=makexv(angle,velocity);
			rockyv[p]=makeyv(angle,velocity);
			rockstate[p]=1;
		}
	}
	
	public void volley(int x, int y, double angle, double velocity,int count, int time) {
		int i,p=-1;
		for (i=0; i<mvolley-1;i++) {if (volleycount[i]==0) {p=i; break;}}
		if (p!=-1) {
			volleyx[p]=x;
			volleyy[p]=y;
			volleyang[p]=angle;
			volleyvel[p]=velocity;
			volleycount[p]=count;
			volleytime[p]=time;
			volleyrst[p]=time;
		}
	}
	
	public void spawnmob(int x,int y,double scale,int frame,int state,int anitype,int z,int aspeed) {
		int i,p=-1;
		for (i=0; i<mmobs-1;i++) {if (mobstate[i]==0) {p=i; break;}}
		if (p!=-1) {
			mobx[p]=x;
			moby[p]=y;
			mobxo[p]=0;
			mobyo[p]=0;
			mobz[p]=z;
			mobalpha[p]=100;
			mobscale[p]=scale;
			mobframe[p]=frame;
			mobstate[p]=state;
			mobaspeed[i]=aspeed;
			mobtween[p]=0;
			mobhp[p]=100;
			double[] a=new double[] {0,Math.PI/12,-Math.PI/12,0,0,Math.PI/12,-Math.PI/12,0,0};
			moblimbs[p]=a;
			bleed[p]=20;
			anim[p]=anitype;
		}
	}
	
	public void myfillOval(int x,int y,int rx, int ry,Graphics g) {
	   g.fillOval(x-rx/2,y-ry/2,rx,ry);
	}
	
	public void myOval(int x,int y,int rx, int ry,Graphics g) {
	   g.drawOval(x-rx/2,y-ry/2,rx,ry);
	}
	
	public void run() {
		while(true) {
			if (multi) arrows=skill[1]; else arrows=skill[0];
			if (spawntime<=0) {
				mobcount++;
				spawntime=200+(int)(1000*Math.random())-mobcount*2;
				spawntime=(int)(Math.random()*2*Math.min(300,spawntime));
				int z=0;//(int)(5*Math.random());
				spawnmob(0,650-z*10,1,0,2,0,z,15);
			}
			spawntime--;
			if (!raytrace) repaint(); else paint(getGraphics());
	
			if (!pause) {
				updatelvl();
				arrupdate();
				updaterocks();
				updatevolley();
				updateblood();
				mobupdate();
				colhandle();
			}
			updatetext();
			try {
				timer.sleep(10);
			} catch (InterruptedException e) { ; }
		}
	}
	
	public void updatelvl() {
		int i,l,max;
		for (i=0; i<=2; i++) {
			if (i==0) slvl[0]=singleattacks/10;//+singlevolleysmade;
			if (i==1) slvl[1]=multiattacks/10;//+multivolleysmade;
			if (i==2) slvl[2]=singlevolleysmade/10+multivolleysmade/10;
			//max=0;
			//for (l=0; l<=skillgains[i].length-1; l++) { if (skillgains[i][l]<slvl[i]) max=l; }
			if (pslvl[i]!=slvl[i]) { spawntext(skillname[i]+" level "+slvl[i],mousex,mousey,100,1,0,0,255);}
			//if (skillnumbers[i][max]>skill[i]) { spawntext(skillname[i]+" level up!",mousex,mousey,100,1); skill[i]=skillnumbers[i][max]; }
			pslvl[i]=slvl[i];
		}
	}
	
	public void arrupdate() {
		int i;
		for (i=0; i<mpart-1; i++) {
			double a=aangle[i];
			double tipx=ax[i]+leng*Math.cos(a);
			double tipy=ay[i]+leng*Math.sin(a);
			if(state[i]==1) {
				ax[i]+=xvel[i];
				ay[i]+=yvel[i];
				aangle[i]=angle(0,0,xvel[i],-yvel[i]);
				yvel[i]+=.1;
			}
			if(state[i]==2) {
				hitmob[i]++;
				if (hitmob[i]>500) aalpha[i]-=.5;
			}
			if(state[i]==3) {
				ax[i]=mobxo[hitmob[i]]+mobx[hitmob[i]]-axo[i];
				ay[i]=mobyo[hitmob[i]]+moby[hitmob[i]]-ayo[i];
				aalpha[i]=mobalpha[hitmob[i]];
			}
			if(state[i]==4) {
				aalpha[i]=mobalpha[hitmob[i]];
				generatenmob(hitmob[i]);
				if (hitlimb[i]==-1) {
					ax[i]=mobxo[hitmob[i]]+mobx[hitmob[i]]+axo[i];
					ay[i]=mobyo[hitmob[i]]+moby[hitmob[i]]+ayo[i];
					aangle[i]=aao[i]+angl[1];
				}
				ax[i]=arrx[joint[hitlimb[i]]]+axo[i]*Math.cos(aao[i]+angl[hitlimb[i]]);
				ay[i]=arry[joint[hitlimb[i]]]+axo[i]*Math.sin(aao[i]+angl[hitlimb[i]]);
				aangle[i]=angl[hitlimb[i]]+ayo[i];
				if (mobstate[hitmob[i]]==2 && mobhp[hitmob[i]]<100) {
					bleed[hitmob[i]]-=1;
				}
				if (bleed[hitmob[i]]<=0) {
					bleed[hitmob[i]]=200;
					spawnblood(tipx,tipy,3,.3,mobz[hitmob[i]]);
				}
				if (mobstate[hitmob[i]]==0) state[i]=0;
			}
			if (aalpha[i]<=0) state[i]=0;
		}
	}
	
	public void updatetext() {
		int i;
		for (i=0; i<=mtext-1; i++) {
			if (textstate[i]==1) {
				texty[i]-=.15;
				textlife[i]--;
				if (textlife[i]<=0) textstate[i]=2;
			}
			if (textstate[i]==2) {
				textalpha[i]--;
				texty[i]-=.15;
				if (textalpha[i]<=0) textstate[i]=0;
			}
		}
	
	}
	
	public void updatevolley() {
		int i;
		for (i=0; i<=mvolley-1; i++) {
			if (volleycount[i]>0) {
				volleytime[i]--;
				if (volleytime[i]<=0) {
					volleytime[i]=volleyrst[i];
					volleycount[i]--;
					spawn(volleyx[i],volleyy[i],volleyang[i],volleyvel[i]);
				}
			}
		}
	}
	
	public void updaterocks() {
		int i;
		for(i=0; i<=mrocks-1; i++) {
			if (rockstate[i]!=0) {
				rockx[i]+=rockxv[i];
				rocky[i]+=rockyv[i];
				rockyv[i]+=.1;
				if (oob(rockx[i],rocky[i])!=0) rockstate[i]=0;
			}
		}
	}
	
	public void updateblood() {
		int i;
		for (i=0; i<=mblood-1; i++) {
			if (bstate[i]==1) {
				bx[i]+=bxvel[i];
				by[i]+=byvel[i];
				byvel[i]+=.1;
				if (oob(bx[i],by[i])!=0) bstate[i]=0;
				//if (by[i]>=height+7-10*bz[i]) bstate[i]=2;
			}
		}
	}
	
	public void mobupdate() {
		int i,l;
		double a,b;
		for (i=0; i<=mmobs-1; i++) {
			generatenmob(i);
			if (isanimating[i]) {
				if (animation[anim[i]][mobframe[i]][9]==0) mobframe[i]=0;
				mobtween[i]++;
				for (l=0; l<=8; l++) {
					a=animation[anim[i]][mobframe[i]][l];
					if (animation[anim[i]][mobframe[i]+1][9]==0) { b=animation[anim[i]][0][l]; } else { b=animation[anim[i]][mobframe[i]+1][l]; }
					moblimbs[i][l]=a+mobtween[i]*(angledist(b,a))/mobaspeed[i];
				}
				a=animation[anim[i]][mobframe[i]][10];
				if (animation[anim[i]][mobframe[i]+1][9]==0) { b=animation[anim[i]][0][10]; } else { b=animation[anim[i]][mobframe[i]+1][10]; }
				mobxo[i]=a+mobtween[i]*(b-a)/mobaspeed[i];
				a=animation[anim[i]][mobframe[i]][11];
				if (animation[anim[i]][mobframe[i]+1][9]==0) { b=animation[anim[i]][0][11]; } else { b=animation[anim[i]][mobframe[i]+1][11]; }
				mobyo[i]=a+mobtween[i]*(b-a)/mobaspeed[i];
				if (mobtween[i]>=mobaspeed[i]) { mobtween[i]=0; mobframe[i]++; }
			}
			if (mobstate[i]==2) {
				mobaspeed[i]=15;
				isanimating[i]=true;
				mobx[i]+=.3;
				if (mobx[i]>=560) {
					mobstate[i]=15;
					mobframe[i]=0;
				}
				for (l=0; l<nwalls-1;l++) {
					if (walls[l][0]==1) {
						int wx=walls[l][1];
						int wy=walls[l][2];
						int wnx=walls[l][3];
						int wny=walls[l][4];
						int ww=walls[l][5];
						int wh=walls[l][6];
						int wsx=walls[l][7];
						int wsy=walls[l][8];
						if (within((int)mobx[i]+14,(int)moby[i],wx,wy,wx+(wnx+1)*ww+(wnx+1)*wsx,wy+(wny+1)*wh+(wny+1)*wsy)) {mobstate[i]=5; mobframe[i]=0; mobstate2[i]=l;}
					}
				}			
				if (mobhp[i]<=0) {
					killmob(i);
				}
			}
			if (mobstate[i]==4) {
				isanimating[i]=true;
				if (animation[anim[i]][mobframe[i]+1][9]==0) {isanimating[i]=false; mobstate[i]=10;}
			}
			if (mobstate[i]==5) {
				anim[i]=2;
				mobaspeed[i]=55; 
				moby[i]-=.1;
				if (moby[i]-15<walls[mobstate2[i]][2]) {mobstate[i]=6; mobframe[i]=0; }
				if (mobhp[i]<=0) { kills++; scoreme(1); mobstate[i]=11; /* fall */ mobframe[i]=0; }
			}
			if (mobstate[i]==6) {
				anim[i]=3;
				mobaspeed[i]=20;
				isanimating[i]=true;
				if (mobhp[i]<=0) { kills++; scoreme(1); mobstate[i]=11; /* fall */ mobframe[i]=0; }
				if (animation[anim[i]][mobframe[i]+1][9]==0) {mobx[i]+=mobxo[i]; mobxo[i]=0; moby[i]+=mobyo[i]; mobyo[i]=0; mobstate[i]=2; anim[i]=0; mobframe[i]=0;}
			}
			if (mobstate[i]==10) {
				mobalpha[i]-=.5;
				if (mobalpha[i]<=0) mobstate[i]=0;
			}
			if (mobstate[i]==11) {
				anim[i]=4;
				if (animation[anim[i]][mobframe[i]+1][9]==0) {mobx[i]+=mobxo[i]; mobxo[i]=0; moby[i]+=mobyo[i]; mobyo[i]=0; mobframe[i]=0; mobstate[i]=12; }	
			}
			if (mobstate[i]==12) {
				anim[i]=5;
				moby[i]+=.8;
				if (moby[i]>=690) {mobstate[i]=10; isanimating[i]=false; mobframe[i]=0;}
			}
			if (mobstate[i]==15) {
				anim[i]=6;
				if (mobframe[i]==1 && mobtween[i]==0) spawnrock(arrx[3],arry[3],.75,3+Math.random());
				if (mobhp[i]<=0) killmob(i);
			}
		}
	}
	
	public void killmob(int mob) {
		mobstate[mob]=4;
		mobframe[mob]=0;
		anim[mob]=1;
		kills++;
		scoreme(1);
	}
	
	public void scoreme(int val) {
		score+=10*val;
		cash+=10*val;
	}
	
	public void colhandle() {
	   int i,l,k,add;
	   for (i=0; i<mpart-1;i++) {
			if (state[i]==1) {
				double a=aangle[i];
				double tipx=ax[i]+leng*Math.cos(a);
				double tipy=ay[i]+leng*Math.sin(a);
				double tailx=ax[i]-leng*Math.cos(a);
				double taily=ay[i]-leng*Math.sin(a);
				for (l=0; l<nwalls-1;l++) {
					if (walls[l][0]==1) {
						int wx=walls[l][1];
						int wy=walls[l][2];
						int wnx=walls[l][3];
						int wny=walls[l][4];
						int ww=walls[l][5];
						int wh=walls[l][6];
						int wsx=walls[l][7];
						int wsy=walls[l][8];
						if (within((int)tipx,(int)tipy,wx,wy,wx+(wnx+1)*ww+(wnx+1)*wsx,wy+(wny+1)*wh+(wny+1)*wsy)) state[i]=2;
					}
				}
				for (l=0; l<mmobs; l++) {
					if (mobstate[l] != 0 && distance(mobxo[l]+mobx[l],mobyo[l]+moby[l],tipx,tipy)<100) {
						generatenmob(l);
						if (distance(mobxo[l]+mobx[l],mobyo[l]+moby[l],tipx,tipy)<head*mobscale[l]/2){
								k=0;
								aao[i]=-angle(arrx[joint[k]],arry[joint[k]],ax[i],ay[i])-angl[k];
								axo[i]=distance(ax[i],ay[i],(double)arrx[joint[k]],(double)arry[joint[k]]);
								ayo[i]=aangle[i]-angl[k];
								sprayblood(tipx,tipy,2,aangle[i],Math.sqrt(xvel[i]*xvel[i]+yvel[i]*yvel[i])/5,Math.PI/3,1,mobz[l]);
								hitmob[i]=l;
								hitlimb[i]=k;
								state[i]=4;
								mobhp[l]-=100;
								scoreme(1);
								spawntext("Headshot!",mobx[l],moby[l],150,1,0,0,0);
						} else {
							for (k=0; k<=8; k++) {
								add=0;
								if (arrx[k]==arrx[joint[k]]) add=1;
								if (intersect(tipx,tipy,tailx,taily,(double)arrx[k]+add,(double)arry[k],(double)arrx[joint[k]],(double)arry[joint[k]])) {
									aao[i]=-angle(arrx[joint[k]],arry[joint[k]],ax[i],ay[i])-angl[k];
									axo[i]=distance(ax[i],ay[i],(double)arrx[joint[k]],(double)arry[joint[k]]);
									ayo[i]=aangle[i]-angl[k];
									sprayblood(tipx,tipy,2,aangle[i],Math.sqrt(xvel[i]*xvel[i]+yvel[i]*yvel[i])/5,Math.PI/3,1,mobz[l]);
									mobhp[l]-=50;
									hitmob[i]=l;
									hitlimb[i]=k;
									lmhit=l;
									llhit=k;
									state[i]=4;
									break;
								}
							}
						}
					}
				}
				if (oob(tipx,tipy) == 1) state[i]=2;
			}
		}
	}
	
	private boolean intersect(double px,double py,double cx,double cy,double lx,double ly,double rx,double ry) {
		double xintersect,yintersect;
		double slopep,slopes,bp;
		boolean intersect=false;
		slopep=(cy-py)/(cx-px);
		slopes=(ly-ry)/(lx-rx);
		bp=(ly-py)+(px-lx)*slopep;
		xintersect=-bp/(slopes-slopep);
		yintersect=+slopes*xintersect;
		if ((px<xintersect+lx && cx>xintersect+lx)||(px>xintersect+lx && cx<xintersect+lx)) {
			if ((py<yintersect+ly && cy>yintersect+ly)||(py>yintersect+ly && cy<yintersect+ly)) {
				if ((lx<xintersect+lx && rx>xintersect+lx)||(lx>xintersect+lx && rx<xintersect+lx)) {
					if ((ly<yintersect+ly && ry>yintersect+ly)||(ly>yintersect+ly && ry<yintersect+ly)) {
						intersect=true;
					}
				}		
			}
		}
		return intersect;
	}
	
	public boolean within(int x,int y,int x1, int y1, int x2, int y2) { //is x,y within rectangle x1,y1 to x2,y2
		if (x>x1 && x<x2 && y>y1 && y<y2) return true;
		return false;
	}
	
	public void spawn(int x,int y,double angle,double velocity) {
		int i,p=-1;
		for (i=0; i<mpart-1;i++) {if (state[i]==0) {p=i; break;}}
		if (p!=-1 && ammo>0) {
			ax[p]=x;
			ay[p]=y;
			hitmob[p]=0;
			aalpha[p]=100;
			xvel[p]=makexv(angle,velocity);
			yvel[p]=makeyv(angle,velocity);
			state[p]=1;
			aangle[i]=angle(0,0,xvel[i],-yvel[i]);
			ammo-=1;
			totalshots++;
		}
	}
	
	public void spawntext(String str, double x, double y, int life, int type,int r, int g, int b) {
		int i,p=-1;
		for (i=0; i<mtext-1;i++) {if (textstate[i]==0) {p=i; break;}}
		if (p!=-1) {
			text[p]=str;
			textx[p]=x;
			texty[p]=y;
			textalpha[p]=100;
			textlife[p]=life;
			texttype[p]=type;
			textcolor[p][0]=r;
			textcolor[p][1]=g;
			textcolor[p][2]=b;
			textstate[p]=1;
		}
	}
	
	public double makexv(double angle, double velocity) {
		return Math.cos(angle)*velocity;
	}
	
	public double makeyv(double angle, double velocity) {
		return -Math.sin(angle)*velocity;
	}
	
	public void sprayblood(double x, double y, int n, double angle, double velocity, double dang, double dvel,double z) {
		int i;
		for (i=0; i<=n; i++) {
			spawnblood(x,y,angle-dang+2*dang*Math.random(),velocity-dvel+2*dvel*Math.random(),z);
		}
	}

	public void spawnblood(double x, double y,double angle, double velocity,double z) {
		int i,p=-1;
		for(i=0; i<=mblood-1; i++) {if (bstate[i]==0) {p=i; break;}}
		bx[p]=x;
		by[p]=y;
		bz[p]=z;
		bxvel[p]=velocity*Math.cos(angle);
		byvel[p]=velocity*Math.sin(angle);
		bsize[p]=2;
		bstate[p]=1;
	}

	public boolean mouseUp(Event e,int x, int y) {
		int xi=0,yi=0;
		mousex=x;
		mousey=y;
		if (mousedown && mousex!=clickx && mousey!=clicky) {
			if (multi) {
				if (volley) multivolleysmade++; else multiattacks++;
			} else {
				if (volley) singlevolleysmade++; else singleattacks++;
			}
			int i;
			double dist=distance(clickx,clicky,x,y);
			double btw=between*sprds/dist;
			boolean aodd=true;
			if (arrows % 2==0) aodd=false;
			double start=-arrows*btw/2+((aodd)?btw/2:0);
			for (i=0; i<arrows; i++) {
				spawn(clickx,clicky,angle(clickx,clicky,x,y)+start+i*btw+Math.random()/90,dist/20);
				if (volley) volley(clickx,clicky,angle(clickx,clicky,x,y)+start+i*btw,dist/20,skill[2]-1,20);
			}		
		}
		mousedown=false;
		if (store) {
			int cx=-1,cy=-1;
			for (xi=0; xi<=3; xi++) {
				for (yi=0; yi<=3; yi++) {
					int a=sx+ox+xi*bw+xi*bxs;
					int b=sy+oy+yi*bh+yi*bys;
					if (within(x,y,a,b,a+bw,b+bh)) { /*spawntext("("+xi+","+yi+")",x,y,100,1);*/ cx=xi; cy=yi;}
				}
			}
			//spawntext("("+cx+","+cy+")",x,y,100,1,0,0,0);
			while(cy>0) {cx+=4; cy--;}
			if (cx!=-1) {
				if (cash>=stock[cx][1]) {
					//purchase item
					if (stock[cx][2]==-1) {
						ammo+=stock[cx][3];
						cash-=stock[cx][1];
						spawntext("-$"+stock[cx][1],x,y,100,1,255,0,0);
						//spawntext("Purchased "+sslist[stock[cx][0]]+" "+((stock[cx][2]==-1)?"x":"")+((stock[cx][2]!=-1)?"lvl ":"")+stock[cx][3],x,y,100,1,0,100,0);
					}
					if (stock[cx][2]!=-1) {
						if (slvl[stock[cx][2]]>=stock[cx][4]) {
							if (purchased[stock[cx][2]]<stock[cx][3]) {
								cash-=stock[cx][1];
								purchased[stock[cx][2]]=stock[cx][3];
								updateskill();
								spawntext("-$"+stock[cx][1],x,y,100,1,255,0,0);
								//spawntext("Purchased "+sslist[stock[cx][0]]+" "+((stock[cx][2]==-1)?"x":"")+((stock[cx][2]!=-1)?"lvl ":"")+stock[cx][3],x,y,100,1,0,100,0);
							} else {
								spawntext("Already Purchased",x,y,100,1,100,100,100);
							}
						} else {
							spawntext("Requires level "+stock[cx][4],x,y,100,1,255,0,0);
						}
					}
				} else {
					spawntext("Not enough money",x,y,100,1,255,0,0);
				}
			}
			//spawntext(""+cx,x,y,100,1,0,0,0);
		}
		return true;
	}

	public void updateskill() {
		int i;
		for (i=0; i<3; i++) {
			skill[i]=skillnumbers[i][purchased[i]];
		}
	}

	public boolean mouseDown(Event e,int x, int y) {
		boolean in=false;
		int i;
		for (i=0; i<=mzones; i++) {
			if (zonestate[i]!=0) {
				if (within(x,y,zonex[i],zoney[i],zonex[i]+zonew[i],zoney[i]+zoneh[i])) {in=true; break;}
			}
		}
		if (in && !pause) {
			mousedown=true;
			clickx=x;
			clicky=y;
		} else {
			mousedown=false;
		}
		mousex=x;
		mousey=y;
		return true;
	}
	
	public boolean mouseMove(Event e,int x, int y) {
		mousex=x;
		mousey=y;
		return false;
	}
	
	public boolean mouseDrag(Event e,int x, int y) {
		mousex=x;
		mousey=y;
		return false;
	}
	
	public boolean keyDown(Event e,int key) {
		int k=0,j;
		debug="k="+key;
		if (key==112) { pause=!pause; mousedown=false; }
		
		/*if (key==32) {
		  int i;
		  for (i=0; i<mpart-1; i++) {
				state[i]=0;
			}
		}*/
		
		if (key==32) multi=true;
				
		if (key==116) raytrace=true;
		
		if (key==122) volley=true;
		
		if (key==47) store=!store;
		
		/*if (key==97) {
			double i;
			for (i=0; i<2*Math.PI; i+=Math.PI/12) {
				spawn(mousex,mousey,i,5);
			}
		}*/
		
		return false;
	}
	
	public boolean keyUp(Event e,int key) {
		if (key==116) raytrace=false;
		if (key==32) multi=false;
		if (key==122) volley=false;
		return false;
	}

	public int oob(int x, int y) {
		if (y > height) return 1; //top
		if (x > width) return 2; //right
		if (y < 0) return 3; //bottom
		if (x< 0) return 4; //left
		return 0;
	}
	public int oob(double x, double y) {
		if (y > height) return 1; //top
		if (x > width) return 2; //right
		if (y < 0) return 3; //bottom
		if (x< 0) return 4; //left
		return 0;
	}

	public void start() {
		timer = new Thread(this);
		timer.start();
	}
	
	public void stop() {
		timer = null;
	}
	
	private double distance(double x1,double y1,double x2,double y2) {
		return Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
	}

	private double angle(double x1,double y1,double x2,double y2) {
		double xdif=x2-x1;
		double ydif=y2-y1;
		double angl=-Math.atan( (ydif/xdif) );
		if (xdif<0 && ydif<0) {		//up left
			angl=angl+Math.PI;
		}else if(xdif>0 && ydif<0) {	//up right
			angl=angl;
		}else if(xdif<0 && ydif>0) {	//down left
			angl+=Math.PI;
		}else if(xdif>0 && ydif>0) {	//down right
			angl=angl+2*Math.PI;
		}
		if (angl==0 && xdif<0) angl=Math.PI;
		if (angl==-.5*Math.PI) angl=1.5*Math.PI;
		return angl;
	}
	
	private double angledist(double ang1, double ang2) {
		double dist,shortest;
		ang1=ang1 % (2*Math.PI);
		ang2=ang2 % (2*Math.PI);
		shortest= ang1-ang2;
		dist=(ang1+2*Math.PI)-ang2;
		if (Math.abs(dist)<Math.abs(shortest)) shortest=dist;
		dist=(ang1-2*Math.PI)-ang2;
		if (Math.abs(dist)<Math.abs(shortest)) shortest=dist;
		return shortest;
	}
	
	private double rad(double angle) {
		return (Math.PI*angle/180);
	}
	private double deg(double angle) {
		return (180*angle/Math.PI);
	}
}


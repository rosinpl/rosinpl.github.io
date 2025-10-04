import java.awt.*;
import java.awt.image.*;
import java.applet.*;
import java.lang.*;

public class Hough6 extends Applet implements Runnable {

public Image myImage,PictImage,ParamImage;

private int mouse_x, mouse_y;

private boolean down=false,up=false;

private int pict_width=200;
private int pict_heigth=200;
private int pict_origin_x=pict_width/2;
private int pict_origin_y=pict_heigth/2;
private int start_pict_x=70;
private int start_pict_y=0;


private int param_width=300;
private int param_heigth=200;
private int param_origin_theta=0;
private int param_origin_r=param_heigth/2;
private int start_param_x=20;
private int start_param_y=220;

private double scale_r,scale_phi;



private int xoff=0;
private int yoff=0;
private Point cosi[];
private Thread anim=null;

private Image offscreenPictImage;
private Image offscreenParamImage;
private Graphics offscreenparamgraph;
private Graphics offscreenpictgraph;
private Graphics pictgraph;
private Graphics paramgraph;
private int arrowdim=4;

public void init(){
resize (440,650);
anim=new Thread(this);
cosi=new Point[20];

PictImage=createImage(pict_width,pict_heigth);
pictgraph=PictImage.getGraphics();
pictgraph.setColor(Color.green);
pictgraph.drawRect(0,0,pict_width-1,pict_heigth-1);
pictgraph.setColor(Color.black);
arrowH(pictgraph,0,pict_heigth/2,pict_width-1,pict_heigth/2,arrowdim,"   x");
arrowV(pictgraph,pict_width/2,pict_heigth,pict_width/2,1,arrowdim,"y");
pictgraph.setColor(Color.green);

ParamImage=createImage(param_width,param_heigth);
paramgraph=ParamImage.getGraphics();
paramgraph.setColor(Color.red);
paramgraph.drawRect(0,0,param_width-1,param_heigth-1);
paramgraph.setColor(Color.black);
arrowH(paramgraph,0,param_heigth/2,param_width-1,param_heigth/2,arrowdim,"theta");
arrowV(paramgraph,0,param_heigth,0,1,arrowdim,"r");
paramgraph.setColor(Color.red);


offscreenPictImage=createImage(pict_width,pict_heigth);
offscreenpictgraph=offscreenPictImage.getGraphics();
offscreenpictgraph.setColor(Color.green);


offscreenParamImage=createImage(param_width,param_heigth);
offscreenparamgraph=offscreenParamImage.getGraphics();
offscreenparamgraph.setColor(Color.red);

paint2(offscreenpictgraph);
paint3(offscreenparamgraph);


scale_r = (param_heigth/2)/(Math.sqrt(pict_heigth*pict_heigth/4+pict_width*pict_width/4));
scale_phi = param_width/Math.PI;
}

public void start() {
//Start the run method
anim.start();
}

public void stop() {
if (anim!=null && anim.isAlive()) {
//Stop the run method if necessary
anim.stop();
}
}

public synchronized void update(Graphics g){

//paint2(offscreenpictgraph);
//paint3(offscreenparamgraph);

g.drawImage(offscreenPictImage,start_pict_x,start_pict_y,this);
g.drawImage(offscreenParamImage,start_param_x,start_param_y,this);

}

public void paint2(Graphics g) {
g.drawImage(PictImage,0,0,this);
}

public void paint3(Graphics g) {
g.drawImage(ParamImage,0,0,this);
}

public boolean mouseMove(Event evt, int x, int y) {
mouse_x = x;
mouse_y = y;
return true;
}

public double f(double r, double theta, double x, double y) {
return (x*Math.cos(theta)+y*Math.sin(theta)-r);
}
public void PerformPict(int x, int y) {

pictgraph.drawOval(x-3-start_pict_x,y-3-start_pict_y,6,6);
paint2(offscreenpictgraph);
}

public void PerformInvParam(int x, int y) {

paramgraph.drawOval(x-3-start_param_x,y-3-start_param_y,6,6);
paint3(offscreenparamgraph);
}

public void PerformInvPict(int m_x, int m_y, boolean ok) {
double a,b,r,theta,x,y;

Point corner[]=new Point[4];
boolean positiv[]=new boolean[4];
int s=0,t=1;
x=m_x-start_param_x;
y=m_y-start_param_y;
int x0=0,y0=0,x1=0,y1=0;

theta=(x-param_origin_theta)/scale_phi;
r=-(y-param_origin_r)/scale_r;

a=-(1/Math.tan(theta));
b=r/Math.sin(theta);

corner[0]=new Point(pict_width/2,pict_heigth/2);
corner[1]=new Point(-pict_width/2,pict_heigth/2);
corner[2]=new Point(-pict_width/2,-pict_heigth/2);
corner[3]=new Point(pict_width/2,-pict_heigth/2);

for (int i=0;i<4;i++){
	if (f(r,theta,corner[i].x,corner[i].y)>=0) positiv[i]=true;else positiv[i]=false ;
};
if (positiv[3]){
	for (int i=0;i<4;i++){
		positiv[i]=!positiv[i];
	};
};

for (int i=0;i<4;i++){
	if (positiv[i]) {s=s+t;};
	t=t*2;
};

switch(s){
	case 0:break;
	case 1:y0=(int)Math.round(pict_heigth/2);x0=(int)Math.round((y0-b)/a);
	x1=(int)Math.round(pict_width/2);y1=(int)Math.round(a*x1+b);break;
	case 2:y0=(int)Math.round(pict_heigth/2);x0=(int)Math.round((y0-b)/a);
	x1=(int)Math.round(-pict_width/2);y1=(int)Math.round(a*x1+b);break;
	case 3:x0=(int)Math.round(-pict_width/2);y0=(int)Math.round(a*x0+b);
	x1=(int)Math.round(pict_width/2);y1=(int)Math.round(a*x1+b);break;
	case 4:x0=(int)Math.round(-pict_width/2);y0=(int)Math.round(a*x0+b);
	y1=(int)Math.round(-pict_heigth/2);x1=(int)Math.round((y1-b)/a);break;
	case 6:y0=(int)Math.round(pict_heigth/2);x0=(int)Math.round((y0-b)/a);
	y1=(int)Math.round(-pict_heigth/2);x1=(int)Math.round((y1-b)/a);break;
	case 7:y0=(int)Math.round(-pict_heigth/2);x0=(int)Math.round((y0-b)/a);
	x1=(int)Math.round(pict_width/2);y1=(int)Math.round(a*x1+b);break;
	default:System.out.println("Error appended");
};

paint2(offscreenpictgraph);
if (s!=0) {
if (ok) {pictgraph.drawLine(x0+pict_width/2,-y0+pict_heigth/2,x1+pict_width/2,-y1+pict_heigth/2);
paint2(offscreenpictgraph);}
else offscreenpictgraph.drawLine(x0+pict_width/2,-y0+pict_heigth/2,x1+pict_width/2,-y1+pict_heigth/2);
};

}
public void PerformParam(int m_x, int m_y, boolean ok) {
double phi,x,y;

x = (m_x - start_pict_x) - pict_width/2;
y = -(m_y-start_pict_y)+pict_heigth/2;
phi = 0.0;
double pset=Math.PI/19.0;

for (int i=0;i<20;i++) {
	cosi[i]=new Point((int)Math.round(phi*scale_phi+param_origin_theta),
	(int)Math.round(-(y*Math.sin(phi)+x*Math.cos(phi))*scale_r+param_origin_r));
	phi=phi+pset;
}
paint3(offscreenparamgraph);
for (int i=0;i<19;i++) {
	if (ok) {paramgraph.drawLine(cosi[i].x,cosi[i].y,cosi[i+1].x,cosi[i+1].y);
	paint3(offscreenparamgraph);}
	else offscreenparamgraph.drawLine(cosi[i].x,cosi[i].y,cosi[i+1].x,cosi[i+1].y);
}

}

public void arrowH(Graphics g, int x0, int y0, int x1, int y1, int dim, String comment){
	Font f=getFont();
	g.drawLine(x0,y0,x1-dim,y1);
	g.drawLine(x1-dim,y1-dim,x1-dim,y1+dim);
	g.drawLine(x1-dim,y1-dim,x1,y1);
	g.drawLine(x1-dim,y1+dim,x1,y1);
	g.setFont(f);
	g.drawString(comment,x1-35,y1-6);
}

public void arrowV(Graphics g, int x0, int y0, int x1, int y1, int dim, String comment){
	Font f=getFont();
	g.drawLine(x0,y0,x1,y1+dim);
	g.drawLine(x1-dim,y1+dim,x1+dim,y1+dim);
	g.drawLine(x1-dim,y1+dim,x1,y1);
	g.drawLine(x1+dim,y1+dim,x1,y1);
	g.setFont(f);
	g.drawString(comment,x1+6,y1+12);
}

public boolean mouseDown(Event evt, int px, int py) {
int x=px,y=py;
if (x>start_pict_x && x<start_pict_x+pict_width && y>start_pict_y && y<start_pict_y+pict_heigth) {
	PerformParam(x,y,false);
}
if (x>start_param_x && x<start_param_x+param_width && y>start_param_y && y<start_param_y+param_heigth) {
	PerformInvPict(x,y,false);

}
return true;
}
public boolean mouseUp(Event evt, int px, int py) {
int x=px,y=py;
if (x>start_pict_x && x<start_pict_x+pict_width && y>start_pict_y && y<start_pict_y+pict_heigth) {
	PerformPict(x,y);
	PerformParam(x,y,true);
}
if (x>start_param_x && x<start_param_x+param_width && y>start_param_y && y<start_param_y+param_heigth) {
	PerformInvParam(x,y);
	PerformInvPict(x,y,true);

}
return true;
}

public boolean mouseDrag(Event evt, int px, int py) {
int x=px,y=py;
if (x>start_pict_x && x<start_pict_x+pict_width && y>start_pict_y && y<start_pict_y+pict_heigth) {
	PerformParam(x,y,false);
}
if (x>start_param_x && x<start_param_x+param_width && y>start_param_y && y<start_param_y+param_heigth) {
	PerformInvPict(x,y,false);
}
return true;
}

public void run() {
while (anim!=null) {
try {
//Suspend for 50 milliseconds
anim.sleep(50);
}
//In case something wakes us up
catch (InterruptedException e) {}
repaint();
}
}

private void setG() {


Graphics graph=myImage.getGraphics();
graph.setColor(Color.darkGray);
graph.drawLine(0,250,250,0);
}
}

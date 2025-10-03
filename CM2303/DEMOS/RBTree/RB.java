import java.io.*;
import java.util.*;
import java.awt.*;

class Dot
{
   int level;    // 0 is top, 1 is next, etc.
   int indent;   // 0 is leftmost, 1 is next, etc.
   float left;     // pixels in from left
   float top;      // pixels in from top
   Dot leftTree;
   Dot rightTree;
   int number;
   Dot shadow;
   Color color;

   public Dot(int n, Color clr)
   {
      number = n;
      color = clr;
      leftTree = null;
      rightTree = null;
      shadow = null;
   }

   int   numberOf()        { return number; }
   float setLeft(float lf) { return left = lf; }
   float setTop(float tp)  { return top = tp; }
   void  setLevel(int lv)  { level = lv; }
   void  setIndent(int in) { indent = in; }
   void  setShadow(Dot d)  { shadow = d; }
   void  setNumber(int n)  { number = n; }
   void  setLefttree (Dot lt)
   {
      leftTree = lt;
      lt.setIndent(2*indent);
      lt.setLevel(level+1);
   }
   void setLeftTree  (Dot lt) { leftTree = lt; }
   void setRightTree (Dot rt) { rightTree = rt; }
   void setRighttree (Dot rt)
   {
      rightTree = rt;
      rt.setIndent(2*indent+1);
      rt.setLevel(level+1);
   }
   void  setColor(Color c) { color = c; }
   Dot   shadowOf()    { return shadow; } 
   Dot   rightTreeOf() { return rightTree; }
   Dot   leftTreeOf()  { return leftTree; }
   float leftOf()      { return left; }
   float topOf()       { return top; }
   int   levelOf()     { return level; }
   int   indentOf()    { return indent; }
   Color colorOf()     { return color; }
}

class DotPanel extends Panel implements Runnable
{
   RB graph;
   Thread relaxer;
   Dot pick;
   Dot deletingNode;
   Color deletingSaveColor;
   boolean deleteNode = false, removingNode = false;

   DotPanel(RB graph) {  this.graph = graph;  }

   public void run()
   {
      while (true)
      {
         repaint();
         try
         {
            Thread.sleep(100 + graph.getNDots());
         }
         catch (InterruptedException e) {  break;  }
      }
   }

   Image offscreen;
   Dimension offscreensize;
   Graphics offgraphics;

   int left (Dot dot)
   {
      Dimension d = size();
      double wid = (double)d.width/(1+(1 << dot.levelOf()));
      return (int)(wid*(dot.indentOf()+1)) + 15;
   }

   int top(Dot dot) {  return 20+dot.levelOf()*50 + 15;  }

   int offset = 28;

   public void paintDot(Graphics g, Dot dot, FontMetrics fm, int ox, int oy)
   {
      if (dot == null) return;
     
      int x  = left(dot);
      int y  = top(dot);
      int tx = (int)dot.leftOf();
      int ty = (int)dot.topOf();

      String lbl = String.valueOf(dot.numberOf());
      int w = fm.stringWidth(lbl);
      int h = fm.getHeight();
      g.setColor(dot.colorOf());
      g.fillOval(tx+ox-offset, ty+oy, 30, 30);
      g.setColor(Color.white);
      g.drawString(lbl, tx+ox-offset-w/2+15, ty+oy+12+h/2);
      dot.setLeft((float)(.75*(dot.leftOf()-x) + x));
      dot.setTop((float)(.75*(dot.topOf()-y) + y));
   }

   public void paintPickedDot(Graphics g, Dot dot, FontMetrics fm)
   {
      if (dot == null) return;
      
      int tx = (int)dot.leftOf();
      int ty = (int)dot.topOf();

      String lbl = String.valueOf(dot.numberOf());
      int w = fm.stringWidth(lbl);
      int h = fm.getHeight();
      g.setColor(dot.colorOf());
      g.fillOval(tx-offset, ty, 30, 30);
      g.setColor(Color.white);
      g.drawString(lbl, tx-offset-w/2+15, ty+12+h/2);
   }

   public void paintEdgesOfDot(Graphics g, Dot dot)
   {
      if (dot == null) return;
      
      g.setColor(Color.black);
      int x = (int)dot.leftOf()+15;
      int y = (int)dot.topOf()+15;
      if (dot.leftTreeOf() != null)
      {
         int lx = (int)dot.leftTreeOf().leftOf()+15;
         int ly = (int)dot.leftTreeOf().topOf()+15;
         g.drawLine(x-offset,y,lx-offset,ly);
      }
      if (dot.rightTreeOf() != null)
      {
         int rx = (int)dot.rightTreeOf().leftOf()+15;
         int ry = (int)dot.rightTreeOf().topOf()+15;
         g.drawLine(x-offset,y,rx-offset,ry);
      }
   }

   public void update(Graphics g)
   {
      Dimension d = size();
      if ((offscreen == null) || (d.width != offscreensize.width) ||
          (d.height != offscreensize.height))
      {
         offscreen = createImage(d.width, d.height);
         offscreensize = d;
         offgraphics = offscreen.getGraphics();
         offgraphics.setFont(getFont());
      }

      offgraphics.setColor(graph.getColor());
      offgraphics.fillRect(0, 0, d.width, d.height);
      FontMetrics fm = offgraphics.getFontMetrics();
      Dot dt[] = graph.getDots();
      int nd  = graph.getNDots();
      for (int i=0 ; i < nd ; i++)
         paintEdgesOfDot(offgraphics, dt[i]);
      for (int i=0 ; i < nd ; i++)
         if (dt[i] != pick) paintDot(offgraphics, dt[i], fm, 0, 0);
         else paintPickedDot(offgraphics, dt[i], fm);
      if (graph.getNewest() != null)
         paintDot(offgraphics, graph.getNewest(), fm, -30, -15);
      g.drawImage(offscreen, 0, 0, null);
   }

   public boolean mouseDown(Event evt, int x, int y)
   {
      Dot dot[] = graph.getDots();
      for (int i=0 ; i < graph.getNDots() ; i++)
      {
         if (dot[i] == null) continue;
         if (x-dot[i].leftOf() < 0 && y-dot[i].topOf() < 30 &&
             x > dot[i].leftOf()-30 && y > dot[i].topOf())
         {
            pick = dot[i];
            if (deleteNode == true)
            {
               deleteNode = false;
               deletingNode = pick;
               deletingSaveColor = deletingNode.colorOf();
               deletingNode.setColor(Color.green);
               removingNode = true;
            }
            return true;
         }
      }
      return true;
   }

   public boolean mouseDrag(Event evt, int x, int y)
   {
      if (pick != null)
      {
         pick.setLeft(x+20);
         pick.setTop(y-10);
      }
      return true;
   }

   public boolean mouseUp(Event evt, int x, int y)
   {
      pick = null;
      return true;
   }

   public void start() { relaxer = new Thread(this);  relaxer.start(); }
   public void stop()  { relaxer.stop(); }
   
   public void setValue(boolean v) { deleteNode = v; }
   public boolean removingOf() { return removingNode; }
   public Dot dotOf() { return deletingNode; }
   public Color saveColorOf() { return deletingSaveColor; }
   public void setRemoving(boolean v) { removingNode = v; }
}

public class RB extends java.applet.Applet
{
   DotPanel panel;
   Dot shadow;
   int sysLock = 0;
   Dot cdot[] = new Dot[100];
   int cndots = 0;
   Dot dot[] = new Dot[100];
   int ndots = 0;
   Dot ss[] = new Dot[100];
   Dot rev1 = null, rev2 = null, rev3 = null;
   int ssindex = 0;
   Dot head = null;
   Dot chead = null;
   Dot temp = null;
   Dot newest = null;
   Dot stopdot = new Dot(0, Color.black);
   Dot pushdownward;
   Dot pushupward;
   Dot t_dot;
   Button addbutton,nextbutton,undobutton,startbutton,tempbutton,delbutton;
   TextField val;
   Panel p;
   Color bgcolor = new Color(190,190,190);
   Color downSaveColor;
   int step = 0;
   boolean inserting = false;

   Dot[] getDots()   { return dot; }
   int   getNDots()  { return ndots; }
   Dot   getNewest() { return newest; }
   Color getColor()
   {
      if (rev1 == null && rev2 == null && rev3 == null && !panel.removingOf() &&
          temp == null && !(head == null && newest != null) &&
          ssindex == 0 && (head != null && head.colorOf() == Color.black))
      {
         bgcolor = new Color(190,190,190);
         inserting = false;
      }
      else
      if (panel.removingOf()) bgcolor = new Color(0,190,190);

      return bgcolor;
   }

   public void init()
   {
      setLayout(new BorderLayout());

      panel = new DotPanel(this);
      add("Center", panel);
      p = new Panel();
      add("South", p);
      p.add(startbutton = new Button("Restart"));
      p.add(undobutton = new Button("Undo"));
      p.add(val = new TextField(5));
      p.add(addbutton = new Button("Add Node"));
      p.add(nextbutton = new Button("Next Step"));
      p.add(delbutton = new Button("Delete Node"));
   }

   public void start() {  panel.start();  }
   public void stop()  {  panel.stop();   }

   Dot copyTree(Dot root)
   {
      if (root == null) return null;
      Dot dot = new Dot(root.numberOf(), root.colorOf());
      dot.setTop(root.topOf());
      dot.setLeft(root.leftOf());
      dot.setLevel(root.levelOf());
      dot.setIndent(root.indentOf());
      dot.setLeftTree(copyTree(root.leftTreeOf()));
      dot.setRightTree(copyTree(root.rightTreeOf()));
      root.setShadow(dot);
      return dot;
   }

   int insertCopy(Dot root, Dot p[], int ss)
   {
      if (root == null) return ss;
      p[ss++] = root;
      ss = insertCopy(root.leftTreeOf(), p, ss);
      ss = insertCopy(root.rightTreeOf(), p, ss);
      return ss;
   }

   void reLevel(Dot dot, int ind, int lvl)
   {
      if (dot == null) return;
      dot.setLevel(lvl);
      dot.setIndent(ind);
      reLevel(dot.leftTreeOf(),  2*ind, lvl+1);
      reLevel(dot.rightTreeOf(), 2*ind+1 , lvl+1);
   }

   void leftRotate(Dot chld, Dot prnt)
   {
      prnt.setLeftTree(chld.rightTreeOf());
      chld.setRightTree(prnt.leftTreeOf().leftTreeOf());
      prnt.leftTreeOf().setLeftTree(chld);
      reLevel(head, 0, 0);
   }

   void srightRotate(Dot chld, Dot prnt)
   {
      prnt.setRightTree(chld.leftTreeOf());
      chld.setLeftTree(prnt.rightTreeOf().rightTreeOf());
      prnt.rightTreeOf().setRightTree(chld);
      reLevel(head, 0, 0);
   }

   void rightRotate(Dot chld, Dot prnt)
   {
      prnt.setLeftTree(chld.leftTreeOf());
      chld.setLeftTree(prnt.leftTreeOf().rightTreeOf());
      prnt.leftTreeOf().setRightTree(chld);
      reLevel(head, 0, 0);
   }

   void sleftRotate(Dot chld, Dot prnt)
   {
      prnt.setRightTree(chld.rightTreeOf());
      chld.setRightTree(prnt.rightTreeOf().leftTreeOf());
      prnt.rightTreeOf().setLeftTree(chld);
      reLevel(head, 0, 0);
   }

   void rightRotate(Dot dot)
   {
      head = dot.leftTreeOf();
      dot.setLeftTree(head.rightTreeOf());
      head.setRightTree(dot);
      reLevel(head, 0, 0);
   }

   void sleftRotate(Dot dot)
   {
      head = dot.rightTreeOf();
      dot.setRightTree(head.leftTreeOf());
      head.setLeftTree(dot);
      reLevel(head, 0, 0);
   }
  
   int upward()
   {
      if (ssindex > 0)
      {
         if (ss[ssindex-1].colorOf() == Color.red &&
             ss[ssindex].colorOf() == Color.red)
         {
            if (ssindex > 1 && ss[ssindex] == ss[ssindex-1].leftTreeOf() &&
                ss[ssindex-1] == ss[ssindex-2].leftTreeOf())
            {
               if (ss[ssindex-2].rightTreeOf() == null ||
                   ss[ssindex-2].rightTreeOf().colorOf() == Color.black)
               {
                  if (ssindex > 2)
                  {
                     if (ss[ssindex-3].leftTreeOf() == ss[ssindex-2])
                        rightRotate(ss[ssindex-2], ss[ssindex-3]);
                     else
                        srightRotate(ss[ssindex-2], ss[ssindex-3]);
                     play(getCodeBase(), "audio/ah.au");
                     rev1 = ss[ssindex-1];
                     rev2 = ss[ssindex-2];
                     ss[ssindex-2] = ss[ssindex-1];
                     ssindex -= 2;
                     return 1;
                  }
                  else
                  {
                     rightRotate(ss[ssindex-2]);
                     play(getCodeBase(), "audio/ooh.au");
                     rev1 = head;
                     rev2 = head.rightTreeOf();
                     ssindex = 0;
                     return 1;
                  }
               }
               else
               {
                  rev1 = ss[ssindex-2];
                  rev2 = ss[ssindex-2].leftTreeOf();
                  rev3 = ss[ssindex-2].rightTreeOf();
                  ssindex -= 2;
                  return 2;
               }
            }
            else
            if (ssindex > 1 && ss[ssindex] == ss[ssindex-1].rightTreeOf() &&
                ss[ssindex-1] == ss[ssindex-2].leftTreeOf())
            {
               if (ss[ssindex-2].rightTreeOf() == null ||
                   ss[ssindex-2].rightTreeOf().colorOf() == Color.black)
               {
                  leftRotate(ss[ssindex-1], ss[ssindex-2]);
                  play(getCodeBase(), "audio/ah.au");
                  Dot tt = ss[ssindex];
                  ss[ssindex] = ss[ssindex-1];
                  ss[ssindex-1] = tt;
                  return 1;
               }
               else
               {
                  rev1 = ss[ssindex-2];
                  rev2 = ss[ssindex-2].leftTreeOf();
                  rev3 = ss[ssindex-2].rightTreeOf();
                  ssindex -= 2;
                  return 2;
               }
            }
            else
            if (ssindex > 1 && ss[ssindex] == ss[ssindex-1].rightTreeOf() &&
                ss[ssindex-1] == ss[ssindex-2].rightTreeOf())
            {
               if (ss[ssindex-2].leftTreeOf() == null ||
                   ss[ssindex-2].leftTreeOf().colorOf() == Color.black)
               {
                  if (ssindex > 2)
                  {
                     if (ss[ssindex-3].rightTreeOf() == ss[ssindex-2])
                        sleftRotate(ss[ssindex-2], ss[ssindex-3]);
                     else
                        leftRotate(ss[ssindex-2], ss[ssindex-3]);
                     play(getCodeBase(), "audio/doorbell.au");
                     rev1 = ss[ssindex-1];
                     rev2 = ss[ssindex-2];
                     ss[ssindex-2] = ss[ssindex-1];
                     ssindex -= 2;
                     return 1;
                  }
                  else
                  {
                     sleftRotate(ss[ssindex-2]);
                     play(getCodeBase(), "audio/doorbell.au");
                     rev1 = head;
                     rev2 = head.leftTreeOf();
                     ssindex = 0;
                     return 1;
                  }
               }
               else
               {
                  rev1 = ss[ssindex-2];
                  rev2 = ss[ssindex-2].leftTreeOf();
                  rev3 = ss[ssindex-2].rightTreeOf();
                  ssindex -= 2;
                  return 2;
               }
            }
            else
            if (ssindex > 1 && ss[ssindex] == ss[ssindex-1].leftTreeOf() &&
                ss[ssindex-1] == ss[ssindex-2].rightTreeOf())
            {
               if (ss[ssindex-2].leftTreeOf() == null ||
                   ss[ssindex-2].leftTreeOf().colorOf() == Color.black)
               {
                  srightRotate(ss[ssindex-1], ss[ssindex-2]);
                  play(getCodeBase(), "audio/ah.au");
                  Dot tt = ss[ssindex];
                  ss[ssindex] = ss[ssindex-1];
                  ss[ssindex-1] = tt;
                  return 1;
               }
               else
               {
                  rev1 = ss[ssindex-2];
                  rev2 = ss[ssindex-2].leftTreeOf();
                  rev3 = ss[ssindex-2].rightTreeOf();
                  ssindex -= 2;
                  return 2;
               }
            }
         }
         else
         {
            return 0;
         }
      }
      return 0;
   }

   Dot insertDot(Dot dot, Dot root)
   {
      if (root == null) return null;

      if (dot.numberOf() == root.numberOf())
      {
         play(getCodeBase(), "audio/drip.au");
			ssindex = 0;
         return newest = temp = null;
      }
      else
      if (dot.numberOf() < root.numberOf())
      {
         dot.setLevel(root.levelOf()+1);
         dot.setIndent(2*root.indentOf());
         if (root.leftTreeOf() == null)
         {
            stopdot.setColor(Color.green);
            return stopdot;
         }
         return root.leftTreeOf();
      }
      else
      {
         dot.setLevel(root.levelOf()+1);
         dot.setIndent(2*root.indentOf()+1);
         if (root.rightTreeOf() == null)
         {
            stopdot.setColor(Color.yellow);
            return stopdot;
         }
         return root.rightTreeOf();
      }
   }

   Dot reverseColor(Dot dot)
   {
      if (dot.colorOf() == Color.black) dot.setColor(Color.red);
      else
      if (dot.colorOf() == Color.red) dot.setColor(Color.black);
      return null;
   }
   
   void deleteDot(Dot d)
   {
      int m;
      for (int k=0 ; k < ndots ; k++)
      {
         if (dot[k] == null) continue;
         if (dot[k] == d)
         {
            dot[k] = null;
            break;
         }
      }
      for (int k=0 ; k < ndots ; k++)
      {
         if (dot[k] == null) continue;
         if (dot[k].leftTreeOf() == d)
         {
            dot[k].setLeftTree(null);
            break;
         }
         if (dot[k].rightTreeOf() == d)
         {
            dot[k].setRightTree(null);
            break;
         }
      }
      for (int k=0 ; k < ndots ; k++) if (dot[k] != null) return;
      head = null;
      return;
   }

   Dot parentOf(Dot d)
   {
      for (int k=0 ; k < ndots ; k++)
      {
         if (dot[k] == null) continue;
         if (dot[k].leftTreeOf() == d) return dot[k];
         if (dot[k].rightTreeOf() == d) return dot[k];
      }
      return null;
   }

   public boolean action(Event evt, Object arg)
   {
      if (evt.target.equals(startbutton))
      {
         ndots = cndots = ssindex = 0;
         head = chead = rev1 = rev2 = rev3 = newest = temp = null;
         for (int i=0 ; i < 100 ; i++) { dot[i] = cdot[i] = ss[i] = null; }
         bgcolor = new Color(190, 190, 190);
         inserting = false;
         return super.action(evt, arg);
      }
      else
      if (evt.target.equals(undobutton))
      {
         if (panel.removingOf()) return super.action(evt, arg);
         if (chead == null) return super.action(evt, arg);
         for (int i=0 ; i < ndots ; i++)
         {
             if (dot[i] == null) continue;
             if ((shadow = dot[i].shadowOf()) != null)
             {
               shadow.setLeft(dot[i].leftOf());
               shadow.setTop(dot[i].topOf());
            }
         }
         for (int i=0 ; i < cndots ; i++) { dot[i] = cdot[i]; ss[i] = null; }
         ndots = cndots;
         head = chead;
         chead = null;
         rev1 = rev2 = rev3 = newest = temp = null;
         ssindex = 0;
         return super.action(evt, arg);
      }
      else
      if (evt.target.equals(addbutton))
      {
         if (rev1 != null || rev2 != null || rev3 != null ||
             temp != null || (head == null && newest != null) ||
             ssindex != 0 || (head != null && head.colorOf() != Color.black))
         {
            play(getCodeBase(), "audio/ooh.au");
         }
         else
         if (newest == null && !panel.removingOf())
         {
            int num = 0;
            inserting = true;
            ndots = insertCopy(head = copyTree(head), dot, 0); /* try it */
            cndots = insertCopy(chead = copyTree(head), cdot, 0);
            ssindex = 0;
            try
            {
               num = Integer.parseInt(val.getText());
            }
            catch (NumberFormatException e)
            {
               return super.action(evt, arg);
            }
            newest = new Dot(num, Color.red);
            newest.setLevel(0);
            newest.setIndent(0);
            temp = head;
            bgcolor = new Color(0,190,190);
         }
         return super.action(evt, arg);
      }
      else
      if (evt.target.equals(nextbutton))
      {
         if (panel.removingOf() && head != null)  // deleting a node
         {
            if (step == 0) // beginning to go down;
            {
               if (panel.dotOf().leftTreeOf() != null &&
                   panel.dotOf().rightTreeOf() != null) // both children there
               {
                  Dot P = parentOf(panel.dotOf());
                  Dot A = panel.dotOf().leftTreeOf();
                  Dot B = panel.dotOf();
                  Dot C = panel.dotOf().rightTreeOf();

                  if (C.leftTreeOf() == null && C.rightTreeOf() == null)
                  {
                     if (A.leftTreeOf() != null && A.rightTreeOf() != null)
                     {
                        if (P != null)
                        {
                           if (P.leftTreeOf() == B) P.setLeftTree(A);
                           else P.setRightTree(A);
                        }
                        else
                        head = A;
                        C.setLeftTree(A.rightTreeOf());
                        A.setRightTree(C);
                        A.leftTreeOf().setColor(Color.black);
                        C.setColor(Color.red);
                        C.leftTreeOf().setColor(Color.black);
                        A.setColor(Color.black);
                     }
                     else
                     if (A.leftTreeOf() == null && A.rightTreeOf() != null)
                     {
                        Dot E = A.rightTreeOf();
                        if (P != null)
                        {
                           if (P.leftTreeOf() == B) P.setLeftTree(E);
                           else P.setRightTree(E);
                        }
                        else
                        head = E;
                        E.setLeftTree(A);
                        E.setRightTree(C);
                        A.setRightTree(null);
                        A.setColor(Color.black);
                        C.setColor(Color.black);
                        E.setColor(panel.saveColorOf());
                     }
                     else
                     if (A.leftTreeOf() != null && A.rightTreeOf() == null)
                     {
                        if (P != null)
                        {
                           if (P.leftTreeOf() == B) P.setLeftTree(A);
                           else P.setRightTree(A);
                        }
                        else
                        head = A;
                        A.setRightTree(C);
                        A.setColor(panel.saveColorOf());
                        C.setColor(Color.black);
                        A.leftTreeOf().setColor(Color.black);
                     }
                     else
                     if (A.leftTreeOf() == null && A.rightTreeOf() == null)
                     {
                        if (P != null)
                        {
                           if (P.leftTreeOf() == B) P.setLeftTree(A);
                           else P.setRightTree(A);
                        }
                        else head = A;
                        
                        // one black & two reds or three blacks at top
                        if (panel.saveColorOf() == Color.red || P == null)
                        {
                           A.setRightTree(C);
                           A.setColor(Color.black);
                           C.setColor(Color.red);
                        }
                        else // two reds on bottom
                        if (A.colorOf() == Color.red)
                        {
                           A.setRightTree(C);
                           A.setColor(Color.black);
                        }
                        else  // triangle of 3 blacks at bottom, not at root
                        {
                           A.setRightTree(C);
                           A.setColor(Color.black);
                           C.setColor(Color.red);
                           if (P != null)
                           {
                              if (P.rightTreeOf() == panel.dotOf())
                                 P.setRightTree(A);
                              else
                              if (P.leftTreeOf() == panel.dotOf())
                                 P.setLeftTree(A);
                              pushupward = A;
                              deleteDot(B);
                              reLevel(head,0,0);
                              panel.setRemoving(true);
                              bgcolor = new Color(0,190,190);
                              step = 2;
                              return super.action(evt, arg);
                           }
                           else head = A;
                        }
                     }

                     deleteDot(B);
                     reLevel(head,0,0);
                     panel.setRemoving(false);
                     bgcolor = new Color(190,190,190);           
                     step = 0;   
                  }
                  else
                  if (C.leftTreeOf() == null)
                  {
                     if (P != null)
                     {
                        if (P.leftTreeOf() == B) P.setLeftTree(C);
                        else P.setRightTree(C);
                     }
                     else
                     head = C;
                     C.setLeftTree(A);
                     C.setColor(panel.saveColorOf());
                     C.rightTreeOf().setColor(Color.black);
                     deleteDot(B);
                     reLevel(head,0,0);
                     panel.setRemoving(false);
                     bgcolor = new Color(190,190,190);           
                     step = 0;
                  }
                  else
                  {
                     pushdownward = panel.dotOf().rightTreeOf();
                     downSaveColor = pushdownward.colorOf();
                     pushdownward.setColor(Color.yellow);
                     step = 1;
                  }
               }
               else
               if (panel.dotOf().leftTreeOf() != null)  // only left child
               {
                  t_dot = parentOf(panel.dotOf());
                  if (t_dot != null)
                  {
                     if (t_dot.leftTreeOf() == panel.dotOf())
                        t_dot.setLeftTree(panel.dotOf().leftTreeOf());
                     else
                        t_dot.setRightTree(panel.dotOf().leftTreeOf());
                  }
                  else
                     head = panel.dotOf().leftTreeOf();

                  panel.dotOf().leftTreeOf().setColor(Color.black);
                  deleteDot(panel.dotOf());
                  reLevel(head,0,0);
                  panel.setRemoving(false);
                  bgcolor = new Color(190,190,190);        
                  step = 0;
               }
               else
               if (panel.dotOf().rightTreeOf() != null) // only right child
               {
                  t_dot = parentOf(panel.dotOf());
                  if (t_dot != null)
                  {
                     if (t_dot.leftTreeOf() == panel.dotOf())
                        t_dot.setLeftTree(panel.dotOf().rightTreeOf());
                     else
                        t_dot.setRightTree(panel.dotOf().rightTreeOf());
                  }   
                  else
                     head = panel.dotOf().rightTreeOf();

                  panel.dotOf().rightTreeOf().setColor(Color.black);
                  deleteDot(panel.dotOf());
                  reLevel(head,0,0);
                  panel.setRemoving(false);
                  bgcolor = new Color(190,190,190);        
                  step = 0;
               }
               else // no children
               {
                  if (panel.dotOf() == head)
                  {
                     deleteDot(panel.dotOf());
                     head = null;
                  }
                  else
                  if (parentOf(panel.dotOf()).colorOf() == Color.red)
                  {
                     Dot A,B,C,D,P;
                     int z;
           
                     Dot parent = parentOf(panel.dotOf());
                     if (parent.leftTreeOf() == panel.dotOf()) z=0; else z=1;
           
                     if (z==0 && (parent.rightTreeOf().rightTreeOf() != null &&
                        parent.rightTreeOf().leftTreeOf() != null))
                     {
                        A = parent;
                        B = parent.rightTreeOf();
                        C = parent.rightTreeOf().leftTreeOf();
                        D = parent.rightTreeOf().rightTreeOf();
                        P = parentOf(parent);
                        A.setRightTree(C);
                        A.setLeftTree(null);
                        B.setLeftTree(A);
                        B.setRightTree(D);
                        C.setLeftTree(null);
                        C.setRightTree(null);
                        D.setLeftTree(null);
                        D.setRightTree(null);
                        if (P != null)
                        {
                           if (P.leftTreeOf() == parent)
                              P.setLeftTree(B);
                           else
                              P.setRightTree(B);
                        }
                        else head = B;
                        B.setColor(Color.red);
                        A.setColor(Color.black);
                        D.setColor(Color.black);
                        C.setColor(Color.red);
                     }
                     else
                     if (z==0 && (parent.rightTreeOf().rightTreeOf() == null &&
                        parent.rightTreeOf().leftTreeOf() != null))
                     {
                        A = parent;
                        B = parent.rightTreeOf();
                        C = parent.rightTreeOf().leftTreeOf();
                        P = parentOf(parent);
                        A.setRightTree(null);
                        A.setLeftTree(null);
                        B.setLeftTree(null);
                        B.setRightTree(null);
                        C.setLeftTree(A);
                        C.setRightTree(B);
                        if (P != null)
                        {
                           if (P.leftTreeOf() == parent)
                              P.setLeftTree(C);
                           else
                              P.setRightTree(C);
                        }
                        else head = C;
                        B.setColor(Color.black);
                        A.setColor(Color.black);
                        C.setColor(Color.red);
                     }
                     else
                     if (z==0 && (parent.rightTreeOf().rightTreeOf() != null &&
                        parent.rightTreeOf().leftTreeOf() == null))
                     {
                        A = parent;
                        B = parent.rightTreeOf();
                        D = parent.rightTreeOf().rightTreeOf();
                        P = parentOf(parent);
                        A.setRightTree(null);
                        A.setLeftTree(null);
                        B.setLeftTree(A);
                        B.setRightTree(D);
                        D.setLeftTree(null);
                        D.setRightTree(null);
                        if (P != null)
                        {
                           if (P.leftTreeOf() == parent)
                              P.setLeftTree(B);
                           else
                              P.setRightTree(B);
                        }
                        else head = B;
                        B.setColor(Color.red);
                        A.setColor(Color.black);
                        D.setColor(Color.black);
                     }
                     else
                     if (z==0 && (parent.rightTreeOf().rightTreeOf() == null &&
                        parent.rightTreeOf().leftTreeOf() == null))
                     {
                        A = parent;
                        B = parent.rightTreeOf();
                        A.setLeftTree(null);
                        B.setColor(Color.red);
                        A.setColor(Color.black);
                     }
                     else
                     if (z==1 && (parent.leftTreeOf().leftTreeOf() != null &&
                        parent.leftTreeOf().rightTreeOf() != null))
                     {
                        A = parent;
                        B = parent.leftTreeOf();
                        C = parent.leftTreeOf().rightTreeOf();
                        D = parent.leftTreeOf().leftTreeOf();
                        P = parentOf(parent);
                        A.setLeftTree(C);
                        A.setRightTree(null);
                        B.setRightTree(A);
                        B.setLeftTree(D);
                        C.setRightTree(null);
                        C.setLeftTree(null);
                        D.setRightTree(null);
                        D.setLeftTree(null);
                        if (P != null)
                        {
                           if (P.leftTreeOf() == parent)
                              P.setLeftTree(B);
                           else
                              P.setRightTree(B);
                        }
                        else head = B;
                        B.setColor(Color.red);
                        A.setColor(Color.black);
                        D.setColor(Color.black);
                        C.setColor(Color.red);
                     }
                     else
                     if (z==1 && (parent.leftTreeOf().leftTreeOf() == null &&
                        parent.leftTreeOf().rightTreeOf() != null))
                     {
                        A = parent;
                        B = parent.leftTreeOf();
                        C = parent.leftTreeOf().rightTreeOf();
                        P = parentOf(parent);
                        A.setLeftTree(null);
                        A.setRightTree(null);
                        B.setRightTree(null);
                        B.setLeftTree(null);
                        C.setRightTree(A);
                        C.setLeftTree(B);
                        if (P != null)
                        {
                           if (P.leftTreeOf() == parent)
                              P.setLeftTree(C);
                           else
                              P.setRightTree(C);
                        }
                        else head = C;
                        B.setColor(Color.black);
                        A.setColor(Color.black);
                        C.setColor(Color.red);
                     }
                     else
                     if (z==1 && (parent.leftTreeOf().leftTreeOf() != null &&
                        parent.leftTreeOf().rightTreeOf() == null))
                     {
                        A = parent;
                        B = parent.leftTreeOf();
                        D = parent.leftTreeOf().leftTreeOf();
                        P = parentOf(parent);
                        A.setLeftTree(null);
                        A.setRightTree(null);
                        B.setRightTree(A);
                        B.setLeftTree(D);
                        D.setRightTree(null);
                        D.setLeftTree(null);
                        if (P != null)
                        {
                           if (P.leftTreeOf() == parent)
                              P.setLeftTree(B);
                           else
                              P.setRightTree(B);
                        }
                        else head = B;
                        B.setColor(Color.red);
                        A.setColor(Color.black);
                        D.setColor(Color.black);
                     }
                     else
                     if (z==1 && (parent.leftTreeOf().leftTreeOf() == null &&
                        parent.leftTreeOf().rightTreeOf() == null))
                     {
                        A = parent;
                        B = parent.leftTreeOf();
                        A.setRightTree(null);
                        B.setColor(Color.red);
                        A.setColor(Color.black);
                     }
                     deleteDot(panel.dotOf());
                     reLevel(head,0,0);
                  }
                  else
                  if (parentOf(panel.dotOf()).colorOf() == Color.black)
                  {
                     Dot A,B,C,D,P;
                     int z;
           
                     Dot parent = parentOf(panel.dotOf());
                     if (parent != null)
                     {
                        if (parent.leftTreeOf() == panel.dotOf())
                           z = 0;
                        else
                           z = 1;
                     }
                     else z = -1;

                     if (panel.saveColorOf() == Color.red)
                     {
                        if (parent.leftTreeOf() == panel.dotOf())
                           parent.setLeftTree(null);
                        else
                           parent.setRightTree(null);
                     }
                     else
                     if (z==0 && (parent.rightTreeOf().rightTreeOf() != null &&
                         parent.rightTreeOf().leftTreeOf() != null))
                     {
                        A = parent;
                        B = parent.rightTreeOf();
                        C = parent.rightTreeOf().leftTreeOf();
                        D = parent.rightTreeOf().rightTreeOf();
                        P = parentOf(parent);
                        if (P != null)
                        {
                           if (P.leftTreeOf() == parent)
                              P.setLeftTree(B);
                           else
                              P.setRightTree(B);
                        }
                        else head = B;
                        
                        if (C.leftTreeOf() == null && C.rightTreeOf() == null)
                        {
                           B.setLeftTree(A);
                           A.setRightTree(C);
                           A.setLeftTree(null);
                           B.setColor(Color.black);
                           A.setColor(Color.black);
                           D.setColor(Color.black);
                           C.setColor(Color.red);
                           if (D.leftTreeOf() != null)
                              D.leftTreeOf().setColor(Color.red);
                           if (D.rightTreeOf() != null)
                              D.rightTreeOf().setColor(Color.red);           
                        }
                        else
                        if (C.leftTreeOf() == null && C.rightTreeOf() != null)
                        {
                           C.setLeftTree(A);
                           A.setLeftTree(null);
                           A.setRightTree(null);
                           B.setColor(Color.black);
                           C.setColor(Color.black);
                           D.setColor(Color.black);
                           A.setColor(Color.red);
                           C.rightTreeOf().setColor(Color.red);
                           if (D.leftTreeOf() != null)
                               D.leftTreeOf().setColor(Color.red);
                           if (D.rightTreeOf() != null)
                               D.rightTreeOf().setColor(Color.red);
                        }
                        else
                        if (C.leftTreeOf() != null && C.rightTreeOf() == null)
                        {
                           Dot E = C.leftTreeOf();
                           B.setLeftTree(E);
                           E.setRightTree(C);
                           E.setLeftTree(A);
                           A.setRightTree(null);
                           A.setLeftTree(null);
                           C.setLeftTree(null);
                           C.setRightTree(null);
                           B.setColor(Color.black);
                           E.setColor(Color.black);
                           D.setColor(Color.black);
                           A.setColor(Color.red);
                           C.setColor(Color.red);
                           if (D.leftTreeOf() != null)
                               D.leftTreeOf().setColor(Color.red);
                           if (D.rightTreeOf() != null)
                               D.rightTreeOf().setColor(Color.red);  
                        }
                        else
                        {
                           B.leftTreeOf().leftTreeOf().setLeftTree(A);
                           A.setLeftTree(null);
                           A.setRightTree(null);
                           B.setColor(Color.black);
                           C.setColor(Color.red);
                           D.setColor(Color.black);
                           C.leftTreeOf().setColor(Color.black);
                           C.rightTreeOf().setColor(Color.black);
                           if (D.leftTreeOf() != null)
                              D.leftTreeOf().setColor(Color.red);
                           if (D.rightTreeOf() != null)
                              D.rightTreeOf().setColor(Color.red);  
                           A.setColor(Color.red);
                        }
                     }
                     else
                     if (z==0 && (parent.rightTreeOf().rightTreeOf() != null &&
                         parent.rightTreeOf().leftTreeOf() == null))
                     {
                        A = parent;
                        B = parent.rightTreeOf();
                        D = parent.rightTreeOf().rightTreeOf();
                        P = parentOf(parent);
                        A.setRightTree(null);
                        A.setLeftTree(null);
                        B.setLeftTree(A);
                        B.setRightTree(D);
                        D.setLeftTree(null);
                        D.setRightTree(null);
                        if (P != null)
                        {
                           if (P.leftTreeOf() == parent)
                              P.setLeftTree(B);
                           else
                              P.setRightTree(B);
                        }
                        else head = B;
                        B.setColor(Color.black);
                        A.setColor(Color.black);
                        D.setColor(Color.black);
                     }
                     else
                     if (z==0 && (parent.rightTreeOf().rightTreeOf() == null &&
                         parent.rightTreeOf().leftTreeOf() != null))
                     {
                        A = parent;
                        B = parent.rightTreeOf();
                        C = parent.rightTreeOf().leftTreeOf();
                        P = parentOf(parent);
                        A.setRightTree(null);
                        A.setLeftTree(null);
                        B.setLeftTree(null);
                        B.setRightTree(null);
                        C.setLeftTree(A);
                        C.setRightTree(B);
                        if (P != null)
                        {
                           if (P.leftTreeOf() == parent)
                              P.setLeftTree(C);
                           else
                              P.setRightTree(C);
                        }
                        else head = C;
                        B.setColor(Color.black);
                        A.setColor(Color.black);
                        C.setColor(Color.black);
                     }
                     else
                     if (z==1 && (parent.leftTreeOf().leftTreeOf() != null &&
                         parent.leftTreeOf().rightTreeOf() != null))
                     {
                        A = parent;
                        B = parent.leftTreeOf();
                        C = parent.leftTreeOf().rightTreeOf();
                        D = parent.leftTreeOf().leftTreeOf();
                        P = parentOf(parent);
                        if (P != null)
                        {
                           if (P.leftTreeOf() == parent)
                              P.setLeftTree(B);
                           else
                              P.setRightTree(B);
                        }
                        else
                           head = B;
                        if (C.rightTreeOf() == null && C.leftTreeOf() == null)
                        {
                           B.setRightTree(A);
                           A.setLeftTree(C);
                           A.setRightTree(null);
                           B.setColor(Color.black);
                           A.setColor(Color.black);
                           D.setColor(Color.black);
                           C.setColor(Color.red);
                           if (D.rightTreeOf() != null)
                              D.rightTreeOf().setColor(Color.red);
                           if (D.leftTreeOf() != null)
                              D.leftTreeOf().setColor(Color.red);
                        }
                        else
                        if (C.rightTreeOf() == null && C.leftTreeOf() != null)
                        {
                           C.setRightTree(A);
                           A.setRightTree(null);
                           A.setLeftTree(null);
                           B.setColor(Color.black);
                           C.setColor(Color.black);
                           D.setColor(Color.black);
                           A.setColor(Color.red);
                           C.leftTreeOf().setColor(Color.red);
                           if (D.rightTreeOf() != null)
                              D.rightTreeOf().setColor(Color.red);
                           if (D.leftTreeOf() != null)
                              D.leftTreeOf().setColor(Color.red);
                        }
                        else
                        if (C.rightTreeOf() != null && C.leftTreeOf() == null)
                        {
                           Dot E = C.rightTreeOf();
                           B.setRightTree(E);
                           E.setLeftTree(C);
                           E.setRightTree(A);
                           A.setLeftTree(null);
                           A.setRightTree(null);
                           C.setRightTree(null);
                           C.setLeftTree(null);
                           B.setColor(Color.black);
                           E.setColor(Color.black);
                           D.setColor(Color.black);
                           A.setColor(Color.red);
                           C.setColor(Color.red);
                           if (D.rightTreeOf() != null)
                              D.rightTreeOf().setColor(Color.red);
                           if (D.leftTreeOf() != null)
                              D.leftTreeOf().setColor(Color.red);
                        }
                        else
                        {
                           B.rightTreeOf().rightTreeOf().setRightTree(A);
                           A.setLeftTree(null);
                           A.setRightTree(null);
                           B.setColor(Color.black);
                           C.setColor(Color.red);
                           D.setColor(Color.black);
                           C.rightTreeOf().setColor(Color.black);
                           C.leftTreeOf().setColor(Color.black);
                           if (D.rightTreeOf() != null)
                              D.rightTreeOf().setColor(Color.red);
                           if (D.leftTreeOf() != null)
                              D.leftTreeOf().setColor(Color.red);
                           A.setColor(Color.red);
                        }           
                     }
                     else
                     if (z==1 && (parent.leftTreeOf().leftTreeOf() != null &&
                         parent.leftTreeOf().rightTreeOf() == null))
                     {
                        A = parent;
                        B = parent.leftTreeOf();
                        D = parent.leftTreeOf().leftTreeOf();
                        P = parentOf(parent);
                        A.setLeftTree(null);
                        A.setRightTree(null);
                        B.setRightTree(A);
                        B.setLeftTree(D);
                        D.setRightTree(null);
                        D.setLeftTree(null);
                        if (P != null)
                        {
                           if (P.leftTreeOf() == parent)
                              P.setLeftTree(B);
                           else
                              P.setRightTree(B);
                        }
                        else head = B;
                        B.setColor(Color.black);
                        A.setColor(Color.black);
                        D.setColor(Color.black);
                     }
                     else
                     if (z==1 && (parent.leftTreeOf().leftTreeOf() == null &&
                         parent.leftTreeOf().rightTreeOf() != null))
                     {
                        A = parent;
                        B = parent.leftTreeOf();
                        C = parent.leftTreeOf().rightTreeOf();
                        P = parentOf(parent);
                        A.setLeftTree(null);
                        A.setRightTree(null);
                        B.setRightTree(null);
                        B.setLeftTree(null);
                        C.setRightTree(A);
                        C.setLeftTree(B);
                        if (P != null)
                        {
                           if (P.leftTreeOf() == parent)
                              P.setLeftTree(C);
                           else
                              P.setRightTree(C);
                        }
                        else head = C;
                        B.setColor(Color.black);
                        A.setColor(Color.black);
                        C.setColor(Color.black);
                     }
                     else  // triangle of three blacks at root
                     if (parentOf(parent) == null)
                     {
                        head = parent;
                        if (parent.leftTreeOf() == panel.dotOf())
                           parent.rightTreeOf().setColor(Color.red);
                        else
                           parent.leftTreeOf().setColor(Color.red);
                     }
                     else // There is a triangle of three blacks at bottom
                     {
                        B = parentOf(panel.dotOf());
                        if (B.rightTreeOf() == panel.dotOf())
                           B.leftTreeOf().setColor(Color.red);
                        else
                           B.rightTreeOf().setColor(Color.red);

                        pushupward = B;
                        deleteDot(panel.dotOf());
                        reLevel(head,0,0);
                        panel.setRemoving(true);
                        bgcolor = new Color(0,190,190);
                        step = 2;
                        return super.action(evt, arg);
                     }

                     deleteDot(panel.dotOf());
                     reLevel(head,0,0);
                  }
                  panel.setRemoving(false);
                  bgcolor = new Color(190,190,190);        
                  step = 0;
               }
            }
            else
            if (step == 1)
            {
               pushdownward.setColor(downSaveColor);
               if (pushdownward.leftTreeOf().leftTreeOf() != null)
               {
                  pushdownward = pushdownward.leftTreeOf();
                  downSaveColor = pushdownward.colorOf();
                  pushdownward.setColor(Color.yellow);
                  step = 1;
               }
               else
               {
                  pushdownward.leftTreeOf().setColor(Color.yellow);
                  step = 3;
               }
            }
            else
            if (step == 2) // going up with one less black depth on one side
            {
               Dot A,B,C,D,E,F,G,P;
               
               A = pushupward;
               P = parentOf(A);
               if (P == null)
               {
                  head = A;
                  panel.setRemoving(false);
                  bgcolor = new Color(190,190,190);
                  step = 0;
               }
               else
               {
                  E = parentOf(P);         // Grab E here before parent is lost
                  if (P.leftTreeOf() == A) // Rotate
                  {
                     B = P.rightTreeOf();
                     C = B.leftTreeOf();
                     D = B.rightTreeOf();
                     if (D.colorOf() == Color.black &&
                         C.colorOf() == Color.red &&
                         B.colorOf() == Color.black)
                     {
                        P.setRightTree(C);
                        B.setLeftTree(C.rightTreeOf());
                        C.setRightTree(B);
                        B.setColor(Color.red);
                        C.setColor(Color.black);
                        reLevel(head,0,0);
                        step = 2;
                        return super.action(evt, arg);
                     }
                     else
                     if (D.colorOf() == Color.black &&
                         C.colorOf() == Color.black &&
                         B.colorOf() == Color.red)
                     {
                        F = C.leftTreeOf();
                        G = C.rightTreeOf();
                        if (F != null && G != null)
                        {
                           if (G.colorOf() == Color.black)
                           {
                              if (E != null)
                              {
                                 if (E.leftTreeOf() == P) E.setLeftTree(B);
                                 else E.setRightTree(B);
                              }
                              else head = B;
                              B.setLeftTree(P);
                              P.setRightTree(C);
                              B.setColor(Color.black);
                              C.setColor(Color.red);
                           }
                           else
                           {
                              if (E != null)
                              {
                                 if (E.leftTreeOf() == P) E.setLeftTree(C);
                                 else E.setRightTree(C);
                              }
                              else head = C;
                              C.setLeftTree(P);
                              C.setRightTree(B);
                              B.setLeftTree(G);
                              P.setRightTree(F);
                              if (G.colorOf() == Color.red)
                              {
                                 G.setColor(Color.black);
                              }
                              else
                              {
                                 B.setColor(Color.black);
                                 D.setColor(Color.red);
                              }
                           }
                           reLevel(head,0,0);
                           step = 0;
                           panel.setRemoving(false);
                           bgcolor = new Color(190,190,190);
                           return super.action(evt, arg);
                        }
                     }
                     else
                     {
                        P.setRightTree(C);
                        B.setLeftTree(P);
                     }
                  }
                  else
                  {
                     B = P.leftTreeOf();
                     C = B.rightTreeOf();
                     D = B.leftTreeOf();
                     if (D.colorOf() == Color.black &&
                         C.colorOf() == Color.red &&
                         B.colorOf() == Color.black)
                     {
                        P.setLeftTree(C);
                        B.setRightTree(C.leftTreeOf());
                        C.setLeftTree(B);
                        B.setColor(Color.red);
                        C.setColor(Color.black);
                        reLevel(head,0,0);
                        step = 2;
                        return super.action(evt, arg);
                     }
                     else
                     if (D.colorOf() == Color.black &&
                         C.colorOf() == Color.black &&
                         B.colorOf() == Color.red)
                     {
                        F = C.rightTreeOf();
                        G = C.leftTreeOf();
                        if (F != null && G != null)
                        {
                           if (G.colorOf() == Color.black)
                             {
                              if (E != null)
                              {
                                 if (E.leftTreeOf() == P) E.setLeftTree(B);
                                 else E.setRightTree(B);
                              }
                              else head = B;
                              B.setRightTree(P);
                              P.setLeftTree(C);
                              B.setColor(Color.black);
                              C.setColor(Color.red);
                           }
                           else
                           {
                              if (E != null)
                              {
                                 if (E.leftTreeOf() == P) E.setLeftTree(C);
                                 else E.setRightTree(C);
                              }
                              else head = C;
                              C.setRightTree(P);
                              C.setLeftTree(B);
                              B.setRightTree(G);
                              P.setLeftTree(F);
                              if (G.colorOf() == Color.red)
                              {
                                 G.setColor(Color.black);
                              }
                              else
                              {
                                 B.setColor(Color.black);
                                 D.setColor(Color.red);
                              }
                           }
                           reLevel(head,0,0);
                           step = 0;
                           panel.setRemoving(false);
                           bgcolor = new Color(190,190,190);
                           return super.action(evt, arg);
                        }
                     }
                     else
                     {
                        P.setLeftTree(C);
                        B.setRightTree(P);
                     }
                  }
                  
                  if (E != null)
                  {
                     if (E.leftTreeOf() == P) E.setLeftTree(B);
                     else E.setRightTree(B);
                  }
                  else head = B;
                  
                  if (P.colorOf() == Color.red)
                  {
                     if (C.colorOf() == Color.red)
                     {
                        if (B != head) B.setColor(Color.red);
                        else B.setColor(Color.black);
                        B.leftTreeOf().setColor(Color.black);
                        B.rightTreeOf().setColor(Color.black);
                     }
                     panel.setRemoving(false);
                     bgcolor = new Color(190,190,190);
                     step = 0;
                  }
                  else
                  {
                     if (B.colorOf() == Color.red)
                     {
                        B.setColor(Color.black);
                        C.setColor(Color.red);
                        panel.setRemoving(false);
                        bgcolor = new Color(190,190,190);
                        step = 0;
                     }
                     else
                     if (D.colorOf() == Color.red)
                     {
                        D.setColor(Color.black);
                        panel.setRemoving(false);
                        bgcolor = new Color(190,190,190);
                        step = 0;
                     }
                     else // case D = B = P = black, C = red already covered
                     {
                        P.setColor(Color.red);
                        if (E == null)
                        {
                           panel.setRemoving(false);
                           bgcolor = new Color(190,190,190);
                           step = 0;
                        }
                        else
                        {
                           pushupward = B;
                           step = 2;
                        }
                     }
                  }
               }
               reLevel(head,0,0);
            }
            else
            if (step == 3)
            {
               t_dot = pushdownward.leftTreeOf().rightTreeOf();
               panel.dotOf().setNumber(pushdownward.leftTreeOf().numberOf());
               panel.dotOf().setColor(panel.saveColorOf());
               deleteDot(pushdownward.leftTreeOf());
               step = 4;     
            }
            else
            if (step == 4)
            {
               if (t_dot != null) t_dot.setColor(Color.black);
               else
               {
                  if (pushdownward.colorOf() == Color.black)
                  {
                     if (pushdownward.rightTreeOf() == null)
                     {
                        pushdownward.setLeftTree(null);
                     }
                     else
                     if (pushdownward.rightTreeOf().colorOf() == Color.red)
                     {
                        Dot P = parentOf(pushdownward);
                        Dot B = pushdownward.rightTreeOf();
                        Dot A = B.leftTreeOf();
                        Dot C = B.rightTreeOf();
                        if (A != null || C != null)
                        {
                           if (P.leftTreeOf() == pushdownward)
                              P.setLeftTree(B);
                           else
                              P.setRightTree(B);
                           if (A.leftTreeOf()==null && A.rightTreeOf()==null)
                           {
                              B.setLeftTree(pushdownward);
                              pushdownward.setRightTree(A);
                              pushdownward.setLeftTree(null);
                              B.setColor(Color.black);
                              pushdownward.setColor(Color.black);
                              C.setColor(Color.black);
                              A.setColor(Color.red);
                           }
                           else
                           if (A.leftTreeOf()==null && A.rightTreeOf()!=null)
                           {
                              A.setLeftTree(pushdownward);
                              pushdownward.setLeftTree(null);
                              pushdownward.setRightTree(null);
                              pushdownward.setColor(Color.red);
                              B.setColor(Color.black);
                           }
                           else
                           if (A.leftTreeOf()!=null && A.rightTreeOf()==null)
                           {
                              Dot E = A.leftTreeOf();
                              B.setLeftTree(E);
                              E.setLeftTree(pushdownward);
                              E.setRightTree(A);
                              A.setLeftTree(null);
                              A.setRightTree(null);
                              pushdownward.setLeftTree(null);
                              pushdownward.setRightTree(null);
                              E.setColor(Color.red);
                              B.setColor(Color.black);
                              pushdownward.setColor(Color.black);
                              A.setColor(Color.black);
                           }
                           else
                           {
                              Dot E = A.leftTreeOf();
                              B.setLeftTree(E);
                              E.setLeftTree(pushdownward);
                              E.setRightTree(A);
                              A.setLeftTree(null);
                              pushdownward.setLeftTree(null);
                              pushdownward.setRightTree(null);
                              E.setColor(Color.red);
                              B.setColor(Color.black);
                              pushdownward.setColor(Color.black);
                              A.setColor(Color.black);
                           }
                        }
                     }
                     else
                     if (pushdownward.rightTreeOf().rightTreeOf() != null)
                     {
                        Dot P = parentOf(pushdownward);
                        Dot B = pushdownward.rightTreeOf();
                        Dot A = B.leftTreeOf();
                        Dot C = B.rightTreeOf();
                        
                        if (P.leftTreeOf() == pushdownward)
                           P.setLeftTree(B);
                        else
                           P.setRightTree(B);
                        B.setLeftTree(pushdownward);
                        if (A != null) pushdownward.setRightTree(A);
                        else pushdownward.setRightTree(null);
                        
                        C.setColor(Color.black);
                        if (A != null) A.setColor(Color.red);

                        reLevel(head,0,0);
                        panel.setRemoving(false);
                        bgcolor = new Color(190,190,190);
                        step = 0;
                        return super.action(evt, arg);                        
                     }
                     else
                     if (pushdownward.rightTreeOf().leftTreeOf() != null)
                     {
                        Dot P = parentOf(pushdownward);
                        Dot B = pushdownward.rightTreeOf();
                        Dot A = B.leftTreeOf();
                        
                        if (P.leftTreeOf() == pushdownward)
                           P.setLeftTree(A);
                        else
                           P.setRightTree(A);
                        A.setLeftTree(pushdownward);
                        A.setRightTree(B);
                        pushdownward.setLeftTree(null);
                        pushdownward.setRightTree(null);
                        B.setLeftTree(null);
                        A.setColor(Color.black);
                        B.setColor(Color.black);
                        pushdownward.setColor(Color.black);
                        
                        reLevel(head,0,0);
                        panel.setRemoving(false);
                        bgcolor = new Color(190,190,190);
                        step = 0;
                        return super.action(evt, arg);                        
                     }
                     else  // triangle of three blacks at bottom
                     {
                        pushdownward.rightTreeOf().setColor(Color.red);
                        pushupward = pushdownward;
                        reLevel(head, 0, 0);
                        step = 2;
                        return super.action(evt, arg);
                     }
                  }
                  else // triangle one red, two blacks
                  {
                     Dot C = pushdownward.rightTreeOf();
                     Dot A = C.rightTreeOf();
                     Dot B = C.leftTreeOf();
                     Dot P = parentOf(pushdownward);
                     if (A == null && B == null)
                     {
                        pushdownward.setColor(Color.black);
                        C.setColor(Color.red);
                     }
                     else
                     if (A != null && B == null)
                     {
                        if (P.leftTreeOf() == pushdownward)
                           P.setLeftTree(C);
                        else
                           P.setRightTree(C);
                        C.setLeftTree(pushdownward);
                        pushdownward.setLeftTree(null);
                        pushdownward.setRightTree(null);
                        C.setColor(Color.red);
                        C.leftTreeOf().setColor(Color.black);
                        C.rightTreeOf().setColor(Color.black);
                        reLevel(head,0,0);
                     }
                     else
                     if (A == null && B != null)
                     {
                        if (P.leftTreeOf() == pushdownward)
                           P.setLeftTree(B);
                        else
                           P.setRightTree(B);
                        B.setLeftTree(pushdownward);
                        B.setRightTree(C);
                        pushdownward.setLeftTree(null);
                        pushdownward.setRightTree(null);
                        C.setLeftTree(null);
                        C.setRightTree(null);
                        B.setColor(Color.red);
                        B.leftTreeOf().setColor(Color.black);
                        B.rightTreeOf().setColor(Color.black);
                        reLevel(head,0,0);
                     }
                     else // both A and B are not null
                     {
                        if (P.leftTreeOf() == pushdownward)
                           P.setLeftTree(C);
                        else
                           P.setRightTree(C);
                          pushdownward.setRightTree(B);
                        pushdownward.setLeftTree(null);
                        C.setLeftTree(pushdownward);
                        C.setColor(Color.red);
                        pushdownward.setColor(Color.black);
                        A.setColor(Color.black);
                        B.setColor(Color.red);
                        reLevel(head,0,0);
                     }
                  }
               }
               step = 6;
            }
            if (step == 6)
            {
               if (t_dot != null) pushdownward.setLeftTree(t_dot);
               // walk up tree from triangle of blacks, possibly
               reLevel(head,0,0);
               panel.setRemoving(false);
               bgcolor = new Color(190,190,190);
               step = 0;
            }
         }
         else
         if (rev1 != null || rev2 != null || rev3 != null)
         {
            if (rev1 != null)  rev1 = reverseColor(rev1);
            if (rev2 != null)  rev2 = reverseColor(rev2);
            if (rev3 != null)  rev3 = reverseColor(rev3);
         }
         else
         if (temp != null) // Insert the dot (way down)
         {
            Color leftdot, topdot, rightdot;
            topdot = temp.colorOf();
            if (temp.leftTreeOf() != null)
               leftdot=temp.leftTreeOf().colorOf();
            else leftdot = Color.blue;
            if (temp.rightTreeOf() != null)
               rightdot=temp.rightTreeOf().colorOf();
            else rightdot = Color.blue;

            ss[ssindex++] = temp;

            if ((temp = insertDot(newest, temp)) == stopdot)
            {
               if (stopdot.colorOf()==Color.green)
               {
                  /*** Experimental - to fix non-red/blackness on add ***/
                  if (rightdot == Color.black) newest.setColor(Color.black);
                  ss[ssindex-1].setLefttree(newest);
               }
               else
               {
                  /*** Experimental - to fix non-red/blackness on add ***/
                  if (leftdot == Color.black) newest.setColor(Color.black);
                  ss[ssindex-1].setRighttree(newest);
               }
        
               dot[ndots++] = ss[ssindex] = newest;
               newest = temp = null;
               play(getCodeBase(), "audio/ooh.au");
            }
         }
         else
         if (head == null && newest != null)  // Special case - first dot
         {
            dot[ndots++] = ss[ssindex] = head = newest;
            newest.setColor(Color.black);
            newest = temp = null;
            play(getCodeBase(), "audio/cowbell.au");
         }
         else
         if (ssindex > 0)
         {
            int tell;
            if ((tell = upward()) == 2)
            {
               if (rev1 != null) rev1 = reverseColor(rev1);
               if (rev2 != null) rev2 = reverseColor(rev2);
               if (rev3 != null) rev3 = reverseColor(rev3);
            }
            else
            if (tell == 0 && rev1 == null && rev2 == null && rev3 == null)
            {
               play(getCodeBase(), "audio/train.au");
               ssindex = 0;
            }
         }
         else
         if (head != null && head.colorOf() != Color.black) head.setColor(Color.black);

         if (!panel.removingOf() && rev1 == null && rev2 == null && rev3 == null &&
             temp == null && !(head == null && newest != null) &&
             ssindex == 0 && (head != null && head.colorOf() == Color.black))
         {
            play(getCodeBase(), "audio/whistle.au");
            bgcolor = new Color(190,190,190);
            inserting = false;
         }
      }
      else
      if (evt.target.equals(delbutton))
      {
         if (!panel.removingOf() && !inserting && head != null)
         {
            panel.setValue(true);
            cndots = insertCopy(chead = copyTree(head), cdot, 0);
            bgcolor = new Color(0,190,190);
         }
      }
      return super.action(evt, arg);
   }
}

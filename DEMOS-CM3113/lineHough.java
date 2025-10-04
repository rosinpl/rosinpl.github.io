import java.awt.*;
import java.awt.image.*;
import java.applet.*;
import java.net.*;
import java.io.*;
import java.lang.Math;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JApplet;
import javax.imageio.*;
import javax.swing.event.*;

public class lineHough {

		int[] input;
		int[] output;
		float[] template={-1,0,1,-2,0,2,-1,0,1};;
		double progress;
		int width;
		int height;
		int[] acc;
		int accSize=30;
		int[] results;

		public void lineHough() {
			progress=0;
		}

		public void init(int[] inputIn, int widthIn, int heightIn) {
			width=widthIn;
			height=heightIn;
			input = new int[width*height];
			output = new int[width*height];
			input=inputIn;
			for(int x=0;x<width;x++) {
				for(int y=0;y<height;y++) {
					output[x*width+y] = 0xff000000;
				}
			}
		}
		public void setLines(int lines) {
			accSize=lines;		
		}
		// hough transform for lines (polar), returns the accumulator array
		public int[] process() {
	
			// for polar we need accumulator of 180degress * the longest length in the image
			int rmax = (int)Math.sqrt(width*width + height*height);
			acc = new int[rmax*180];
			int r;
			progress=0;
				
			for(int x=0;x<width;x++) {
				progress+=0.5;			
				for(int y=0;y<height;y++) {
				
					if ((input[y*width+x] & 0xff)== 255) {
					
						for (int theta=0; theta<180; theta++) {
							r = (int)(x*Math.cos(((theta)*Math.PI)/180) + y*Math.sin(((theta)*Math.PI)/180));
							if ((r > 0) && (r <= rmax))
								acc[r*180+theta] = acc[r*180+theta] + 1;
						}
					}
				}
			}
		
			// now normalise to 255 and put in format for a pixel array
			int max=0;
		
			// Find max acc value
			for(r=0; r<rmax; r++) {
				for(int theta=0; theta<180; theta++) {

					if (acc[r*180+theta] > max) {
						//System.out.println("Value :" + acc[r*180+theta] + " " + theta);
						max = acc[r*180+theta];
					}
				}
			}
		
			//System.out.println("Max :" + max);
		
			// Normalise all the values
			int value;
			for(r=0; r<rmax; r++) {
				for(int theta=0; theta<180; theta++) {

					value = (int)(((double)acc[r*180+theta]/(double)max)*255.0);
					acc[r*180+theta] = 0xff000000 | (value << 16 | value << 8 | value);
				}
			}

			//accSize=rmax;
			findMaxima();

			System.out.println("done");
			return output;
		}
		private int[] findMaxima() {
	
			// for polar we need accumulator of 180degress * the longest length in the image
			int rmax = (int)Math.sqrt(width*width + height*height);
			results = new int[accSize*3];
			int[] output = new int[width*height];

		
			for(int r=0; r<rmax; r++) {
				for(int theta=0; theta<180; theta++) {
					int value = (acc[r*180+theta] & 0xff);

					// if its higher than lowest value add it and then sort
					if (value > results[(accSize-1)*3]) {

						// add to bottom of array
						results[(accSize-1)*3] = value;
						results[(accSize-1)*3+1] = r;
						results[(accSize-1)*3+2] = theta;
					
						// shift up until its in right place
						int i = (accSize-2)*3;
						while ((i >= 0) && (results[i+3] > results[i])) {
							for(int j=0; j<3; j++) {
								int temp = results[i+j];
								results[i+j] = results[i+3+j];
								results[i+3+j] = temp;
							}
							i = i - 3;
							if (i < 0) break;
						}
					}
				}
			}
		
			double ratio=(double)(width/2)/accSize;
			System.out.println("top "+accSize+" matches:");
			for(int i=accSize-1; i>=0; i--){
				progress+=ratio;			
				//System.out.println("value: " + results[i*3] + ", r: " + results[i*3+1] + ", theta: " + results[i*3+2]);
				drawPolarLine(results[i*3], results[i*3+1], results[i*3+2]);
			}
			return output;
		}
	
	
		// draw a line given polar coordinates (and an input image to allow drawing more than one line) 
		private void drawPolarLine(int value, int r, int theta) {
			for(int x=0;x<width;x++) {
			
				for(int y=0;y<height;y++) {
				
					int temp = (int)(x*Math.cos(((theta)*Math.PI)/180) + y*Math.sin(((theta)*Math.PI)/180));
					if((temp - r) == 0)
						output[y*width+x] = 0xff000000 | (value << 16 | value << 8 | value);
					
				}
			}
		}

		public int[] getAcc() {
			return acc;
		}

		public int getProgress() {
			return (int)progress;
		}

	}

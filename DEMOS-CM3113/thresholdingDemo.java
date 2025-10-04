// hacked to remove edge detection stuff
// still has remnants, e.g. hysteresis scale
// PLR 2010

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



public class thresholdingDemo extends JApplet {
	
	Image edgeImage, accImage, outputImage;
	MediaTracker tracker = null;
	PixelGrabber grabber = null;
	int width = 0, height = 0;
	String fileNames[] = {"lena.png", "mit.png", "office1.png", "petrol.png"};

	javax.swing.Timer timer;
	//slider constraints
	static int TH_MIN = 5;
	static int TH_MAX = 255;
	static int TH_INIT = 60;
	int threshold=TH_INIT;
	static int TH_MIN2 = 5;
	static int TH_MAX2 = 255;
	static int TH_INIT2 = 80;
	int threshold2=TH_INIT2;
	static int mode=1;

	int imageNumber=0;
	static int progress=0;
	static int orig[] = null;
	static int backup[] = null;
	
	Image image[] = new Image[fileNames.length];
	
	JProgressBar progressBar;
	JPanel selectionPanel, controlPanel, imagePanel, progressPanel;
	JLabel origLabel, outputLabel, modeLabel,comboLabel,sigmaLabel,thresholdLabel,thresholdLabel2,processing;
	JSlider thresholdSlider, thresholdSlider2;
	JButton thresholdingDemo;
	JComboBox imSel;
	static sobel edgedetector;
	JRadioButton standardRadio;
	ButtonGroup radiogroup;

	static Image edges;
	 
	 
	   	// Applet init function	
	public void init() {
		
		tracker = new MediaTracker(this);
		for(int i = 0; i < fileNames.length; i++) {
			image[i] = getImage(this.getCodeBase(),fileNames[i]);
			image[i] = image[i].getScaledInstance(256, 256, Image.SCALE_SMOOTH);
			tracker.addImage(image[i], i);
		}
		try {
			tracker.waitForAll();
		}
		catch(InterruptedException e) {
			System.out.println("error: " + e);
		}
		
		Container cont = getContentPane();
		cont.removeAll();
		cont.setBackground(Color.black);
		cont.setLayout(new BorderLayout());
		
		controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(2,4,15,0));
		controlPanel.setBackground(new Color(192,204,226));
		imagePanel = new JPanel();
		imagePanel.setBackground(new Color(192,204,226));
		progressPanel = new JPanel();
		progressPanel.setBackground(new Color(192,204,226));
		progressPanel.setLayout(new GridLayout(2,1));

		comboLabel = new JLabel("IMAGE");
		comboLabel.setHorizontalAlignment(JLabel.CENTER);
		controlPanel.add(comboLabel);
		modeLabel = new JLabel("Mode");
		modeLabel.setHorizontalAlignment(JLabel.CENTER);
		controlPanel.add(modeLabel);
		thresholdLabel = new JLabel("TH Lower Val = "+TH_INIT);
		thresholdLabel.setHorizontalAlignment(JLabel.CENTER);
		controlPanel.add(thresholdLabel);
		thresholdLabel2 = new JLabel("TH Upper Val = "+TH_INIT2);
		thresholdLabel2.setHorizontalAlignment(JLabel.CENTER);
		controlPanel.add(thresholdLabel2);


		processing = new JLabel("Processing...");
		processing.setHorizontalAlignment(JLabel.LEFT);
		progressBar = new JProgressBar(0,100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true); //get space for the string
		progressBar.setString("");          //but don't paint it
		progressPanel.add(processing);
		progressPanel.add(progressBar);
		
		width = image[imageNumber].getWidth(null);
		height = image[imageNumber].getHeight(null);

		imSel = new JComboBox(fileNames);
		imageNumber = imSel.getSelectedIndex();
		imSel.addActionListener( 
			new ActionListener() {  
				public void actionPerformed(ActionEvent e) {
					imageNumber = imSel.getSelectedIndex();
					origLabel.setIcon(new ImageIcon(image[imageNumber]));	
					processImage();
				}
			}
		);
		controlPanel.add(imSel, BorderLayout.PAGE_START);

        timer = new javax.swing.Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
				progressBar.setValue((edgedetector.getProgress()));
            }
        });

		origLabel = new JLabel("Original Image", new ImageIcon(image[imageNumber]), JLabel.CENTER);
		origLabel.setVerticalTextPosition(JLabel.BOTTOM);
		origLabel.setHorizontalTextPosition(JLabel.CENTER);
		origLabel.setForeground(Color.blue);
		imagePanel.add(origLabel);
		
		outputLabel = new JLabel("Thresholded", new ImageIcon(image[imageNumber]), JLabel.CENTER);
		outputLabel.setVerticalTextPosition(JLabel.BOTTOM);
		outputLabel.setHorizontalTextPosition(JLabel.CENTER);
		outputLabel.setForeground(Color.blue);
		imagePanel.add(outputLabel);
	
		standardRadio = new JRadioButton("Standard");
    	standardRadio.setActionCommand("standard");
		standardRadio.setBackground(new Color(192,204,226));
    	standardRadio.setSelected(true);
	    radiogroup = new ButtonGroup();
	    radiogroup.add(standardRadio);
	    standardRadio.addActionListener(new radiolistener());
		selectionPanel = new JPanel();
		selectionPanel.setBackground(new Color(192,204,226));
		selectionPanel.add(standardRadio);
		controlPanel.add(selectionPanel);

		thresholdSlider = new JSlider(JSlider.HORIZONTAL, TH_MIN, TH_MAX, TH_INIT);
		thresholdSlider.addChangeListener(new thresholdListener());
		thresholdSlider.setMajorTickSpacing(60);
		thresholdSlider.setMinorTickSpacing(20);
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.setPaintLabels(true);
		thresholdSlider.setBackground(new Color(192,204,226));
		controlPanel.add(thresholdSlider);

		thresholdSlider2 = new JSlider(JSlider.HORIZONTAL, TH_MIN2, TH_MAX2, TH_INIT2);
		thresholdSlider2.addChangeListener(new thresholdListener2());
		thresholdSlider2.setMajorTickSpacing(60);
		thresholdSlider2.setMinorTickSpacing(20);
		thresholdSlider2.setPaintTicks(true);
		thresholdSlider2.setPaintLabels(true);
		thresholdSlider2.setBackground(new Color(192,204,226));
		controlPanel.add(thresholdSlider2);



		cont.add(controlPanel, BorderLayout.NORTH);
		cont.add(imagePanel, BorderLayout.CENTER);
		cont.add(progressPanel, BorderLayout.SOUTH);

		processImage();

	}
	class radiolistener implements ActionListener{
	    public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand()=="standard")
				mode=1;
	
			processImage();
	    }
	}
  	class thresholdListener implements ChangeListener {
	    public void stateChanged(ChangeEvent e) {
	        JSlider source = (JSlider)e.getSource();
	        if (!source.getValueIsAdjusting()) {
				System.out.println("threshold="+source.getValue());
				threshold=source.getValue();
				thresholdLabel.setText("TH Lower Val = "+source.getValue());
				processImage();
	        }    
	    }
	}
  	class thresholdListener2 implements ChangeListener {
	    public void stateChanged(ChangeEvent e) {
	        JSlider source = (JSlider)e.getSource();
	        if (!source.getValueIsAdjusting()) {
				System.out.println("threshold2="+source.getValue());
				threshold2=source.getValue();
				thresholdLabel2.setText("TH Upper Val = "+source.getValue());
				processImage();
	        }    
	    }
	}
	public int[] threshold(int[] original, int value) {
		for(int x=0; x<original.length; x++) {
			if((original[x] & 0xff)>=value)
				original[x]=0xffffffff;
			else
				original[x]=0xff000000;
		}
		return original;
	}
	private void processImage(){
		orig=new int[width*height];
		backup=new int[width*height];
		PixelGrabber grabber = new PixelGrabber(image[imageNumber], 0, 0, width, height, orig, 0, width);
		try {
			grabber.grabPixels();
		}
		catch(InterruptedException e2) {
			System.out.println("error: " + e2);
		}
		progressBar.setMaximum(width-4);

		processing.setText("Processing...");
		thresholdSlider.setEnabled(false);
		imSel.setEnabled(false);
		thresholdSlider2.setEnabled(false);
		standardRadio.setEnabled(false);
		edgedetector = new sobel();
		timer.start();

		new Thread(){
			public void run(){
				// PLR - make a copy
				for(int x=0; x<orig.length; x++)
					backup[x] = orig[x];

				threshold(orig, threshold);

				final Image output = createImage(new MemoryImageSource(width, height, orig, 0, width));
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						outputLabel.setIcon(new ImageIcon(output));	
						origLabel.setIcon(new ImageIcon(createImage(new MemoryImageSource(width, height,
						backup, 0, width))));	
						processing.setText("Done");
						thresholdSlider.setEnabled(true);
						if(mode!=1)
							thresholdSlider2.setEnabled(true);
						imSel.setEnabled(true);
						standardRadio.setEnabled(true);
					}
				});
			}
		}.start();
	}

}

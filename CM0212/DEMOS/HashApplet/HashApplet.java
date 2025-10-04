/*** PLR - modified to increase font size ***/
/******************************************************************************

HashApplet.java

CIS 495
Summer 2000
Dr. Bruce Maxim
Dr. Kiumi Akingbehin

Catalyst Software Team: Fadi Aoude, Doug Code, Ann VanDyne
Author: Doug Code
Last modified: 8-22-2000

	Usage:

	<APPLET CODE="HashApplet.class" WIDTH=760 HEIGHT=500>
	<Param Name ="HelpFile", Value = "HashHelp.htm" >
	</APPLET>

	or if class and sound files are archived (recommended):

	<APPLET CODE="HashApplet.class" ARCHIVE="HashApplet.jar" WIDTH=760 HEIGHT=500>
   <Param Name ="HelpFile", Value = "HashHelp.htm" >
   </APPLET>

******************************************************************************/
import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

/******************************************************************************
	Main applet code
******************************************************************************/
public class HashApplet extends Applet implements ActionListener, ItemListener,
																						Runnable{
	// applet layout
	private CardLayout cardManager;
	private SetupPanel setupCard;
	private ExecutionPanel executionCard;
	private ResultsPanel resultsCard;
	private PlotPanel plotCard;
	private ButtonPanel buttonPnl;
	private Panel displayDeck;
	// animation canvases
	private HashCanvas hashCanvas;
	private ProgressCanvas progressCanvas;
	private SplashCanvas splashCanvas;
	private PlotCanvas plotCanvas;
	// data structures
	private Setup settings[];
	private HashTable hashTable, overFlow;
	// collision resolution algorithms
	private HashAlgorithm currentAlg;
	private LinearProbAlgorithm linearProbAlg;
   private QuadraticProbAlgorithm quadraticProbAlg;
	private BucketChainAlgorithm bucketChainAlg;
 	private ChainingAlgorithm chainingAlg;
	private OverflowAlgorithm overflowAlg;
	// managers
	private DisplayManager displayManager;
	private SummaryManager summaryManager;
	private CodeManager codeManager;
	// dialogs
	private OptionsDialog optionsDlg;
	// thread stuff
	private volatile Thread animation = null;
	private int animationDelay = 60;
	// sound clips
	private AudioClip successSnd, unsuccessSnd, errorSnd, noticeSnd, doneSnd;
	// flags
   private volatile boolean abortFlag = false, suspendFlag = false,
   								 codeFlag = false, soundFlag = true,
									 initFlag = false;
	// contains commonly used constants, fonts, strings, etc.
   private Common c;
	// misc
	private int state, runMode, currentHash;
	private String helpPage;

	
	
	// This method is called when the applet is loaded by the browser.
	// It initializes the layout and creates the objects needed by the program.
	public void init(){

		abortFlag = false;
	 	suspendFlag = false;
		codeFlag = false;
	 	soundFlag = true;
		initFlag = false;

 		c = new Common();

		state = c.INTRO;
		runMode = c.RUN;
		currentHash = c.LINEARPROBING;
 
		// Get the name of the HTML help file  
		helpPage = null;
		helpPage = getParameter("HelpFile");
	
		// create and initialize Options dialog box
		optionsDlg = new OptionsDialog( this, animationDelay, "Options" );
		optionsDlg.setSize( 280, 250 );
		optionsDlg.setVisible( false );
		optionsDlg.setResizable( false );
		optionsDlg.setBackground( c.lightBlue );

		// Sounds
		successSnd = getAudioClip( getDocumentBase(), "success.au" );
		unsuccessSnd = getAudioClip( getDocumentBase(), "unsuccess.au" );
		errorSnd = getAudioClip( getDocumentBase(), "error.au" );
		noticeSnd = getAudioClip( getDocumentBase(), "ding.au" );
		
		successSnd.play();
		successSnd.stop();
		unsuccessSnd.play();
		unsuccessSnd.stop();
		errorSnd.play();
		errorSnd.stop();
		noticeSnd.play();
		noticeSnd.stop();
	
		// create setup objects for each algorithm
		settings = new Setup[5];
		settings[0] = new Setup( c.LINEARPROBING );
		settings[0].setMaximums( 0, 300 );
		settings[0].setDefaults( 100, 1, 0, 100, 50 );
		settings[1] = new Setup( c.QUADRATICPROBING );
		settings[1].setMaximums( 0, 300 );
		settings[1].setDefaults( 100, 1, 0, 100, 50 );
		settings[2] = new Setup( c.BUCKETCHAINING );
		settings[2].setMaximums( 0, 225 );
		settings[2].setDefaults( 75, 2, 0, 75, 50 );
		settings[3] = new Setup( c.LINKEDLISTCHAINING );
		settings[3].setMaximums( 0, 150 ); 
		settings[3].setDefaults( 50, 1, 0, 75, 50 );
		settings[4] = new Setup( c.CHAININGWITHOVERFLOW );
		settings[4].setMaximums( 40, 175 );
		settings[4].setDefaults( 45, 2, 24, 80, 50 );

		// images used in double buffered animation
		Image splashImg = createImage( 1060, 500 );
		Image hashImg = createImage( 1060, 500 );
		Image progressImg = createImage( 200, 150 );
		Image plotImg = createImage( 1060, 500 );

		// manages settings and results for completed runs
		summaryManager = new SummaryManager();

		// animation canvases
		hashCanvas = new HashCanvas( 560, 1200, hashImg );
		splashCanvas = new SplashCanvas( splashImg );
 	   progressCanvas = new ProgressCanvas( 200, 150, progressImg );
		plotCanvas = new PlotCanvas( plotImg, summaryManager );

		// "toolbar" along bottom of applet
 		buttonPnl = new ButtonPanel( this );
		buttonPnl.disableRunButtons();
		buttonPnl.setPlotButtonsBoth();
  
		// use CardLayout to manage the 5 possible views
		cardManager = new CardLayout();

		displayDeck = new Panel();
		displayDeck.setLayout( cardManager );

		SplashPanel splashCard = new SplashPanel( splashCanvas );
		displayDeck.add( splashCard, "splash" );

		setupCard = new SetupPanel( this, buttonPnl, settings );
		displayDeck.add( setupCard, "setup" );

		executionCard = new ExecutionPanel( this, hashCanvas, progressCanvas );
		displayDeck.add( executionCard, "execution" );

		resultsCard = new ResultsPanel( this, summaryManager, buttonPnl );
		displayDeck.add( resultsCard, "results" );

		plotCard = new PlotPanel( this, summaryManager, plotCanvas );
		displayDeck.add( plotCard, "plot" );

		setLayout( new BorderLayout() );
		add( displayDeck, BorderLayout.CENTER );
		add( buttonPnl, BorderLayout.SOUTH );

		// create hash tables
		hashTable = new HashTable( this, HashTable.TABLE );
		overFlow = new HashTable( this, HashTable.OVERFLOW );

		hashCanvas.addTable( hashTable );
		hashCanvas.addOverFlow( overFlow );
		hashCanvas.repaint();

		// manages display of algorithm pseudocode
		codeManager = new CodeManager( executionCard.getCodeDisplay() );

		// manages statistics, progress chart, etc  
		displayManager = new DisplayManager( executionCard.getControls(),
	 													 hashCanvas, progressCanvas );
		
		// create algorithm objects
		linearProbAlg = new LinearProbAlgorithm( this, displayManager,
	                   summaryManager, codeManager, buttonPnl,
						    settings[c.LINEARPROBING], hashTable ); 
		quadraticProbAlg = new QuadraticProbAlgorithm( this, displayManager,
	                   summaryManager, codeManager, buttonPnl,
						    settings[c.QUADRATICPROBING], hashTable );
		bucketChainAlg = new BucketChainAlgorithm( this, displayManager,
	                   summaryManager, codeManager, buttonPnl,
						    settings[c.BUCKETCHAINING], hashTable );
		overflowAlg = new OverflowAlgorithm( this, displayManager,
	                   summaryManager, codeManager, buttonPnl,
						    settings[c.CHAININGWITHOVERFLOW], hashTable, overFlow );
		chainingAlg = new ChainingAlgorithm( this, displayManager,
	                   summaryManager, codeManager, buttonPnl,
						    settings[c.LINKEDLISTCHAINING], hashTable );
  
		// set default algorithm
		currentAlg = linearProbAlg;
		
		initFlag = true;
	} // end init

	// This method restores the applet to it initial settings
	public void reset(){
		abortFlag = false;
	 	suspendFlag = false;
		codeFlag = false;
	 	soundFlag = true;
		initFlag = false;

		state = c.INTRO;
		runMode = c.RUN;
		currentHash = c.LINEARPROBING;
		animationDelay = 60;

		// restore defaults
		settings[0].reset();
		settings[1].reset();
		settings[2].reset();
		settings[3].reset();
		settings[4].reset();

		// create and initialize Options dialog box
		optionsDlg = new OptionsDialog( this, animationDelay, "Options" );
		optionsDlg.setSize( 280, 200 );
		optionsDlg.setVisible( false );
		optionsDlg.setResizable( false );
		optionsDlg.setBackground( c.lightBlue );

		buttonPnl.initialControlSettings();
		
		summaryManager.reset();

		currentAlg = linearProbAlg;
		cardManager.show( displayDeck, "splash" );

	}

	// Internet Explorer runs init() and then start() everytime the browser hits
	// the applet's page.  Netscape however only runs init() the first time it
	// loads the applet, after that if the user surfs away and returns only start()
	// is called.  Therefore, reset() is called to straighten things up if a
	// Netscape user left the page while the applet was running (stop() sets
 	// intFlag to false).
	public void start(){
		if( !initFlag )
			reset();
		startThread(); 
	}

	// user left HTML page, kill the thread and hide the option dialog
	public void stop(){
		if(animation != null ){
			// exit demo,  
			abortFlag = true;
  		   if( currentAlg != null )
				currentAlg.abort();
		}
		optionsDlg.setVisible(false);
		optionsDlg = null;
		initFlag = false;
		// collect garbage
		System.gc();
	}

	/********* sound methods **************************************************/
 
	// sets value of soundFlag which determines if sound clips are played
	public void setSound( boolean b ){
		soundFlag = b;
	}

	// play corresponding sound files
	public void playSuccessSound(){
		if( soundFlag )
			successSnd.play();
	}

	public void playUnsuccessSound(){
		if( soundFlag )
			unsuccessSnd.play();
	}

	public void playNoticeSound(){
		if( soundFlag )
			noticeSnd.play();
	}

	public void playErrorSound(){
		if( soundFlag )
			errorSnd.play();
	}

	public void playDoneSound(){
		if( soundFlag )
			doneSnd.play();
	}

	/********* animation control ***********************************************/

	// sets the pause variable for the animation thread
	public void setAnimationRate( int rate ){
		animationDelay = rate;
	}

	// start the thread running
	public void startThread(){
		if(animation == null ){
			animation = new Thread(this);
			animation.start();
		}
	}

	// run the splash animation or the current algorithm
	public void run(){
		if( state == c.INTRO ){
			while( state == c.INTRO && !abortFlag ){
				splashCanvas.updateGraphics();
				splashCanvas.repaint();
				pause( 40 );
			}
		}
		else	   
			currentAlg.run();
		animation = null;			// dipose of thread and collect garbage
		System.gc();           
	}

	// pause thread for "time" milliseconds
	public void pause( int time ){
		try{
			animation.sleep( time );
		}
		catch( InterruptedException ie ){}
	}

	// if we are in step mode we suspend otherwise we pause
	// if we are aborting, fall through
	public void stepOrPause(){
		if( !abortFlag ){
			if( getStep() )
				suspendFlag = true;
			else
 	 			pause( animationDelay ); /// &&&&
		}
	}

	// are we in step mode?
	public boolean getStep( ){
		if( runMode == c.STEP )
			return true;
		else
			return false;
	}

	// if we should be suspended call wait on the thread
	public synchronized void checkSuspended(){
		try{
			if( animation != null ) 
				while( suspendFlag ) 
						wait();
		}catch(InterruptedException e ){}
	}

	// resume the thread
	public synchronized void requestResume(){
		suspendFlag = false;
		if( animation != null )
			notifyAll();
	}

	/********* pseudocode display methods *************************************/

	// tell algorithm whether source pseudocode is displayed
	public void setCodeShowing( boolean b ){
		currentAlg.setCodeShowing( b );
		codeFlag = b;
	}

	// is pseudocode showing?
	public boolean getCodeShowing(){
		return codeFlag;
	}
			
	/********* event handlers *************************************************/

	// respond to "toolbar" button clicks
	public void actionPerformed( ActionEvent e ){
		// page display buttons
		if( e.getSource() == buttonPnl.setupBtn ){
			state = c.SETUP;
			setupCard.loadSettings( currentHash );
			cardManager.show( displayDeck, "setup" );
			buttonPnl.enablePageButtons( state );
			buttonPnl.disableRunButtons();
			buttonPnl.showRunButtons();
		}
		else if( e.getSource() == buttonPnl.executeBtn ){
			state = c.EXECUTE;
			cardManager.show( displayDeck, "execution" );
			newExecDisplay();
			buttonPnl.enablePageButtons( state );
			buttonPnl.setRunButtonsInitial();
			buttonPnl.showRunButtons();
		}
		else if( e.getSource() == buttonPnl.resultsBtn ){
			state = c.RESULTS;
			resultsCard.showSummaries();
			cardManager.show( displayDeck, "results" );
			buttonPnl.enablePageButtons( state );
			buttonPnl.setEditButtons( summaryManager.isSelection(),
		                             summaryManager.beenDeleted()); 
			buttonPnl.showResultEditButtons();
		}
		else if( e.getSource() == buttonPnl.plotBtn ){
			state = c.PLOT;
			plotCard.showSummaries();
			cardManager.show( displayDeck, "plot" );
			buttonPnl.enablePageButtons( state );
			buttonPnl.disableRunButtons();
			buttonPnl.showPlotViewButtons();
		}
		// thread control buttons
		else if( e.getSource() == buttonPnl.runBtn ){
			runMode = c.RUN;
			buttonPnl.setRunButtonsRun();
			if( !currentAlg.isRunning() ){
				hashTable.clear();
				overFlow.clear();
				currentAlg.initialize();
				startThread();
			}
			else
				requestResume();
		}
		else if( e.getSource() == buttonPnl.stepBtn ){
				buttonPnl.setRunButtonsPause();
   		runMode = c.STEP;
			if( !currentAlg.isRunning() ){
				hashTable.clear();
				overFlow.clear();
				currentAlg.initialize();
				startThread();
			}
			else
				requestResume();
		}
		else if( e.getSource() == buttonPnl.pauseBtn ){
			buttonPnl.setRunButtonsPause();
			suspendFlag = true;
			runMode = c.PAUSE;
		}
		else if( e.getSource() == buttonPnl.abortBtn ){
			abortFlag = true;  
			currentAlg.abort();
	 		if( runMode == c.PAUSE || runMode == c.STEP )
				requestResume();
			abortFlag = false;
		}
		// summary edit buttons
		else if( e.getSource() == buttonPnl.deleteBtn ){
			if( summaryManager.delete() )
				resultsCard.showSummaries();
				buttonPnl.setEditButtons( summaryManager.isSelection(),
			 			                    summaryManager.beenDeleted()); 
		}
		else if( e.getSource() == buttonPnl.undeleteBtn ){
			if( summaryManager.unDelete() )
				resultsCard.showSummaries();
				buttonPnl.setEditButtons( summaryManager.isSelection(),
			                             summaryManager.beenDeleted()); 
		}
		// plot display buttons
		else if( e.getSource() == buttonPnl.bothBtn ){
			plotCanvas.setView( PlotCanvas.BOTH );
			buttonPnl.setPlotButtonsBoth();
		}
		else if( e.getSource() == buttonPnl.timeBtn ){
			plotCanvas.setView( PlotCanvas.TIME );
			buttonPnl.setPlotButtonsTime();
		}
		else if( e.getSource() == buttonPnl.probesBtn ){
			plotCanvas.setView( PlotCanvas.PROBES );
			buttonPnl.setPlotButtonsProbes();
		}
		// options dialog button
		else if( e.getSource() == buttonPnl.optionsBtn ){
			optionsDlg.setVisible( true );
		}
		// help page button
		else if( e.getSource() == buttonPnl.helpBtn ){
 			showPage(helpPage, "Help" );
		}
	}

	// handles drop-down menu selections
	public void itemStateChanged( ItemEvent e ){
		if( e.getSource() == buttonPnl.algorithmCb )
		{
			currentHash = buttonPnl.algorithmCb.getSelectedIndex();
			switch( currentHash ){
				case 0:
					currentAlg = linearProbAlg;
					break;
				case 1:
					currentAlg = quadraticProbAlg;
					break;
				case 2:
					currentAlg = bucketChainAlg;
					break;
				case 3:
					currentAlg = chainingAlg;
					break;
				case 4:
					currentAlg = overflowAlg;
					break;
			}
			newExecDisplay();		
			setupCard.loadSettings( currentHash );
			// if splash screen is showing, switch to setup display
			if( state == c.INTRO ){
				state = c.SETUP;
				buttonPnl.enablePageButtons( state );
				cardManager.show( displayDeck, "setup" );
			}
		}
	}

	/********* misc methods ***************************************************/

	// utility method used to place components in a GridBagLayout
	public void addComponent( Panel p, Component c, GridBagConstraints gbc,
								int row, int col, int width, int height ){
	    gbc.gridx = col;
		 gbc.gridy = row;

		 gbc.gridwidth = width;
		 gbc.gridheight = height;
		 p.add( c, gbc );
	}

	// open html help file in new browser window
	public void showPage(String page, String pageTitle ){
		// if no page parameter was passed to applet  
		if (page == null){
      	playErrorSound();
			showStatus( "No Value specified for Param Name = HelpFile"  );
      	return;
      }
	
		URL pageAddr = null;

		try {
      pageAddr = new URL (getCodeBase(), page);
    	} catch (MalformedURLException mue) {
				playErrorSound();
            showStatus( "Caught MalformedURLException accessing requested page." );
            return;
      }
		// open page in new browser window (unless already open)  
		getAppletContext().showDocument( pageAddr, pageTitle );
	}

	// this method updates the execution display to reflect any changes made
	// to the setup object of the currently selected algorithm
	public void newExecDisplay(){
		codeManager.setCode( currentHash, c.STORE);  
		hashTable.setLimits( hashCanvas );
		hashTable.positionElements( settings[currentHash]);
		if( currentHash == c.CHAININGWITHOVERFLOW ){
			overFlow.setLimits( hashCanvas );
			overFlow.positionElements( settings[currentHash] );
		}
		else
			overFlow.reset();
		hashCanvas.repaint();
		displayManager.initialize(currentHash,
										  settings[currentHash].getExecMode(),
	 									  settings[currentHash].getBuckets(), 
										  settings[currentHash].getDataSize());
			
	}
}  // end HashApplet

/******************************************************************************
	The ButtonPanel class defines the "toolbar" along the bottom of the applet.
	Methods are available to display, hide and disable buttons as needed.
******************************************************************************/
class ButtonPanel extends Panel{
	
	private HashApplet parent;
	// panel layout
	private GridBagLayout gbLayout;
	private GridBagConstraints gbc;
	private Panel taskDeck;
	private CardLayout cardManager;
	// public components
	public Choice algorithmCb;
	public Button setupBtn, executeBtn, resultsBtn, plotBtn, runBtn, stepBtn,
 					  pauseBtn, abortBtn, helpBtn, optionsBtn, deleteBtn, undeleteBtn,
					  timeBtn, probesBtn, bothBtn;
	// contains commonly used constants, fonts, strings, etc.
   private Common c;
	
	// construction
	public ButtonPanel( HashApplet parent ){
 		this.parent = parent;

		gbLayout = new GridBagLayout();
		gbc = new GridBagConstraints();

		cardManager = new CardLayout();

		c = new Common();

		// algorithmPnl and components
	 	Panel algorithmPnl = new Panel();
		algorithmPnl.setBackground( c.lightBlue );
		algorithmPnl.setLayout( new GridLayout( 2, 1, 0, 0 ) );

		Label algorithmLbl = new Label( "Hashing algorithm", Label.CENTER );
		algorithmLbl.setFont( c.font12b );
		algorithmPnl.add( algorithmLbl );

		Panel algorithmSubPnl = new Panel();
		algorithmSubPnl.setLayout( new FlowLayout( FlowLayout.CENTER, 7, 2 ) );
						
		algorithmCb = new Choice();
		algorithmCb.add( "linear probing" );
		algorithmCb.add( "quadratic probing" );
		algorithmCb.add( "bucket chaining" );
	//	algorithmCb.add( "chaining w/o separate overflow" );
		algorithmCb.add( "linked list chaining" );
		algorithmCb.add( "chaining w/ separate overflow" );
		algorithmCb.addItemListener( parent );
		algorithmSubPnl.add( algorithmCb );

		algorithmPnl.add( algorithmSubPnl );

		// pagePnl and components
	 	Panel pagePnl = new Panel();
		pagePnl.setBackground( c.lightBlue );
		pagePnl.setLayout( new GridLayout( 2, 1, 0, 0 ) );

		Label pageLbl = new Label( "Display", Label.CENTER );
		pageLbl.setFont( c.font12b );
		pagePnl.add( pageLbl );

		Panel pageSubPnl = new Panel();
		pageSubPnl.setLayout( new FlowLayout( FlowLayout.CENTER, 7, 2 ) );

		setupBtn = new Button( "Setup");
		setupBtn.addActionListener( parent );
		pageSubPnl.add( setupBtn );

		executeBtn = new Button( "Execute");
		executeBtn.setEnabled( false );
		executeBtn.addActionListener( parent );
 		pageSubPnl.add( executeBtn );

		resultsBtn = new Button( "Results");
		resultsBtn.setEnabled( false );
		resultsBtn.addActionListener( parent );
		pageSubPnl.add( resultsBtn );

		plotBtn = new Button( "Plot");
		plotBtn.setEnabled( false );
		plotBtn.addActionListener( parent );
		pageSubPnl.add( plotBtn );

		pagePnl.add( pageSubPnl );

		// the taskDeck contains the subpanels with the Run buttons, Summary
		// edit, and Plot buttons
		taskDeck = new Panel();
		taskDeck.setLayout( cardManager );

			// executionCrd and components (Run buttons)
	 		Panel executionCrd = new Panel();
			executionCrd.setBackground( c.lightBlue );
			executionCrd.setLayout( new GridLayout( 2, 1, 0, 0 ));

			Label executionLbl = new Label( "Execution", Label.CENTER );
			executionLbl.setFont( c.font12b );
			executionCrd.add( executionLbl );

			Panel executionSubPnl = new Panel();
			executionSubPnl.setLayout( new FlowLayout( FlowLayout.CENTER, 7, 2 ) );

			runBtn = new Button( "Run");
			runBtn.addActionListener( parent );
			executionSubPnl.add( runBtn );

			stepBtn = new Button( "Step");
			stepBtn.addActionListener( parent );
			executionSubPnl.add( stepBtn );

			pauseBtn = new Button( "Pause");
			pauseBtn.addActionListener( parent );
			executionSubPnl.add( pauseBtn );

			abortBtn = new Button( "Abort");
			abortBtn.addActionListener( parent );
			executionSubPnl.add( abortBtn );

			
			executionCrd.add( executionSubPnl );

		taskDeck.add( executionCrd, "execute" );

			// resultsCrd and components (Result edit buttons)
	 		Panel resultsCrd = new Panel();
			resultsCrd.setBackground( c.lightBlue );
			resultsCrd.setLayout( new GridLayout( 2, 1, 0, 0 ));

			Label editResultsLbl = new Label( "Edit results", Label.CENTER );
			editResultsLbl.setFont( c.font12b );
			resultsCrd.add( editResultsLbl );

			Panel resultsSubPnl = new Panel();
			resultsSubPnl.setLayout( new FlowLayout( FlowLayout.CENTER, 7, 2 ) );

			deleteBtn = new Button( "Delete");
			deleteBtn.addActionListener( parent );
			resultsSubPnl.add( deleteBtn );

			undeleteBtn = new Button( "Undelete");
			undeleteBtn.addActionListener( parent );
			resultsSubPnl.add( undeleteBtn );

			resultsCrd.add( resultsSubPnl );

		taskDeck.add( resultsCrd, "edit" );

			// plotCrd and components (Plot buttons)
	 		Panel plotCrd = new Panel();
			plotCrd.setBackground( c.lightBlue );
			plotCrd.setLayout( new GridLayout( 2, 1, 0, 0 ));

			Label plotViewLbl = new Label( "Plot views", Label.CENTER );
			plotViewLbl.setFont( c.font12b );
			plotCrd.add( plotViewLbl );

			Panel plotSubPnl = new Panel();
			plotSubPnl.setLayout( new FlowLayout( FlowLayout.CENTER, 7, 2 ) );

			timeBtn = new Button( "Time");
			timeBtn.addActionListener( parent );
			plotSubPnl.add( timeBtn );

			probesBtn = new Button( "Probes");
			probesBtn.addActionListener( parent );
			plotSubPnl.add( probesBtn );

			bothBtn = new Button( "Both");
			bothBtn.addActionListener( parent );
			plotSubPnl.add( bothBtn );

			plotCrd.add( plotSubPnl );

		taskDeck.add( plotCrd, "plot" );

		// optionPnl and components
	 	Panel optionPnl = new Panel();
		optionPnl.setBackground( c.lightBlue );
		optionPnl.setLayout( new GridLayout( 2, 1, 0, 0 ));

		Label optionLbl = new Label( " " );
		optionPnl.add( optionLbl );

		Panel optionSubPnl = new Panel();
		optionSubPnl.setLayout( new FlowLayout( FlowLayout.CENTER, 7, 2 ) );

		optionsBtn = new Button( "Options");
		optionsBtn.addActionListener( parent );
		optionSubPnl.add( optionsBtn );

		helpBtn = new Button( "Help");
		helpBtn.addActionListener( parent );
		optionSubPnl.add( helpBtn );

		optionPnl.add( optionSubPnl );

		// add subpanels to ButtonPanel
		this.setBackground( c.darkBlue );
		this.setLayout( gbLayout );
		gbc.weightx = 100;
		gbc.weighty = 100;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets( 0, 0, 2, 2 );

		parent.addComponent( this, algorithmPnl, gbc, 0, 0, 1, 1 );
		parent.addComponent( this, pagePnl, gbc, 0, 1, 1, 1 );
		parent.addComponent( this, taskDeck, gbc, 0, 2, 1, 1 );

		gbc.insets = new Insets( 0, 0, 2, 0 );
		parent.addComponent( this, optionPnl, gbc, 0, 3, 1, 1 );

	}
	
	// display the Run, Step, Pause, Abort buttons
	public void showRunButtons(){
		cardManager.show( taskDeck, "execute" );
	}

	// display the Delete, Undelete buttons
	public void showResultEditButtons(){
		cardManager.show( taskDeck, "edit" );
	}

	// display the Time, Probes, Both buttons
	public void showPlotViewButtons(){
		cardManager.show( taskDeck, "plot" );
	}

	// enable or disable each edit button
	public void setEditButtons( boolean b1, boolean b2 ){
		deleteBtn.setEnabled( b1 );
		undeleteBtn.setEnabled( b2 );
	}

	// only the Both plot button is disabled
	public void setPlotButtonsBoth(){
		bothBtn.setEnabled( false );
		timeBtn.setEnabled( true );
		probesBtn.setEnabled( true );
	}

	// only the Time plot button is disabled
	public void setPlotButtonsTime(){
		bothBtn.setEnabled( true );
		timeBtn.setEnabled( false );
		probesBtn.setEnabled( true );
	}

	// only the Probes plot button is disabled
	public void setPlotButtonsProbes(){
		bothBtn.setEnabled( true );
		timeBtn.setEnabled( true );
		probesBtn.setEnabled( false );
	}

	// enable or disable all page display buttons
	public synchronized void enablePageButtons( boolean b ){
		setupBtn.setEnabled( b );
		executeBtn.setEnabled( b );
	 	resultsBtn.setEnabled( b );
	 	plotBtn.setEnabled( b );
	}

	// the initial control settings when the applet loads
	public synchronized void initialControlSettings(){
		algorithmCb.setEnabled( true );
		algorithmCb.select(0);
		
		setupBtn.setEnabled( true );
		executeBtn.setEnabled( false );
	 	resultsBtn.setEnabled( false );
	 	plotBtn.setEnabled( false );

		disableRunButtons();
		showRunButtons();
		setPlotButtonsBoth();
		setEditButtons( false, false );
	}

	// disable only the page button corresponding to the current display
	public synchronized void enablePageButtons( int state ){
		setupBtn.setEnabled( state !=c.SETUP );
		executeBtn.setEnabled( state !=c.EXECUTE );
	 	resultsBtn.setEnabled( state !=c.RESULTS );
	 	plotBtn.setEnabled( state !=c.PLOT );
   }

	// enable or disable the algorithm selection menu
	public synchronized void enableMenu( boolean b ){
		algorithmCb.setEnabled( b );
	}

	// disable all the Run buttons
	public synchronized void disableRunButtons(){
		runBtn.setEnabled( false );
		stepBtn.setEnabled( false );
		pauseBtn.setEnabled( false );
		abortBtn.setEnabled( false );
	}

	// for use when algorithm is running, disable the Run and Step buttons
	public synchronized void setRunButtonsRun(){
		runBtn.setEnabled( false );
		stepBtn.setEnabled( false );
		pauseBtn.setEnabled( true );
		abortBtn.setEnabled( true );
	}

	// for use when algorithm hasn't started yet, disable the Pause and
   // Abort buttons
	public synchronized void setRunButtonsInitial(){
		runBtn.setEnabled( true );
		stepBtn.setEnabled( true );
		pauseBtn.setEnabled( false );
		abortBtn.setEnabled( false );
	}

	// for use when algorithm is paused, disable the Pause button
	public synchronized void setRunButtonsPause(){
		runBtn.setEnabled( true );
		stepBtn.setEnabled( true );
		pauseBtn.setEnabled( false );
		abortBtn.setEnabled( true );
	}
	
}  // end ButtonPanel


/******************************************************************************
	The SplashPanel class defines the panel which displays title animation.
******************************************************************************/
class SplashPanel extends Panel{
	private SplashCanvas splashCv;

	// construction
	public SplashPanel( SplashCanvas canvas ){
		splashCv = canvas;

		setLayout( new BorderLayout() );
		add( splashCv, BorderLayout.CENTER );
	}
}  // end SplashPanel


/******************************************************************************
	The SetupPanel class allows the user to make table, data and execution
	selections for each algorithm.
******************************************************************************/
class SetupPanel extends Panel implements ActionListener, ItemListener{
	// references to other system objects
	private HashApplet parent;
	private ButtonPanel buttonPnl;
	private Setup settings[];
	// layout
	private GridBagLayout gbLayout1, gbLayout2, gbLayout3, gbLayout4, gbLayout5;
	private GridBagConstraints gbc1, gbc2, gbc3, gbc4, gbc5, gbc6, gbc7, gbc8;
	private CardLayout cardManager;
	private Panel tableDeck, dataDeck, execModeDeck;
	// controls and components
	private Button tableSaveSlotsBtn, tableSaveBucketsBtn, tableSaveOverBtn, 
						tableChangeBtn, dataSaveBtn, dataChangeBtn,
 						execChangeBtn, execSaveBtn;
	private Checkbox primeCbx, quickLoadCbx;
	private Choice dataTypeCB, execModeCb;
	private Label titleLbl, bucketNumLbl, slotsNumLbl, overFlowNumLbl,
					  bucketNum2Lbl, slotsNum2Lbl, overFlowNum2Lbl,
					  bucketNum3Lbl, slotsNum3Lbl, overFlowNum3Lbl,
 					  bucketRangeLbl, overFlowRangeLbl, limitNumLbl, dataSizeNumLbl,
					  dataSizeRangeLbl, dataTypeNumLbl, modeNumLbl, successNumLbl,
					  quickLoadOnLbl, quickLoad2Lbl;
	private TextArea helpTA;
	private TextField bucketTF, slotsTF, overflowTF, upperLimitTF, dataSizeTF,
						   successTF;
	// contains commonly used constants, fonts, strings, etc.
   private Common c;
	// misc
	private int currentHash;
	private String setupStr, slotStr, bucketStr, overflowStr, dataStr,
 						executionStr, shrinkDataStr, shrinkBucketsStr;
 	private BitSet sieve;
	
	// construction
	public SetupPanel( HashApplet parent, ButtonPanel bp, Setup settings[] ){
		
		this.parent = parent;
		buttonPnl = bp;
		this.settings = settings;
		c = new Common();

		setupStr = "Click a section's Change button to edit the settings.";
		slotStr = "Enter the number of slots per bucket.";
		bucketStr = "Enter the number of buckets in the hash table\n\n" +
	 		"Note:\n" +
			"Division hashing works best with a prime number of buckets. " +
			"Click the checkbox to substitute the closest valid prime " +
			"for the number of buckets you entered.";
		overflowStr = "Enter the size of the overflow area";
		dataStr = "Enter the following:\n" +
			"Upper limit - the maximum value an element in the table may have.\n" +
			"Data size - the number of elements to store or retrieve.\n" +
			"Data type - odd, even or mixed.";
		executionStr = "Enter the following:\n" +
			"Execution mode - store or retrieve data from the table.\n" +
			"Percent successful - In retrieve mode, the percentage of " +
			"searches that are successful.\n" +
			"Quick load - In retrieve mode, check to bypass loading animation.";
		shrinkDataStr = "Note:\nData size was automatically reduced to fit" +
	      " within table.";
		shrinkBucketsStr = "Note:\nBucket size was automatically reduced to" +
	      " fit display.";

 		sieve = new BitSet( 1024 );
 		initSieve();

		/********* panel controls and layout *************************************/
		setBackground( c.darkBlue );

		gbLayout1 = new GridBagLayout();
		gbLayout2 = new GridBagLayout();
		gbLayout3 = new GridBagLayout();
		gbLayout4 = new GridBagLayout();
		gbc1 = new GridBagConstraints();
		gbc2 = new GridBagConstraints();
		gbc3 = new GridBagConstraints();
		gbc4 = new GridBagConstraints();
		gbc5 = new GridBagConstraints();
		gbc6 = new GridBagConstraints();
		gbc7 = new GridBagConstraints();
		gbc8 = new GridBagConstraints();

		this.setLayout( gbLayout1 );

		gbc1.weightx = 100;
		gbc1.weighty = 0;
		gbc1.anchor = GridBagConstraints.NORTH;
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.insets = new Insets( 0, 0, 2, 0 );

		titleLbl = new Label( "Setup For Linear Probing", Label.CENTER );
		titleLbl.setBackground( c.mediumBlue );
		titleLbl.setFont( c.font14b );
		parent.addComponent( this, titleLbl, gbc1, 0, 0, 5, 1 );

		// spacer Label used to help position main subpanels
		gbc1.insets = new Insets( 0, 2, 0, 2 );
		gbc1.weighty = 100;
		gbc1.fill = GridBagConstraints.BOTH;
		Label spacer1Lbl = new Label( "  " );
		spacer1Lbl.setBackground( Color.white );
		parent.addComponent( this, spacer1Lbl, gbc1, 1, 0, 5, 1 );

		/********* table subpanel layout ****************************************/
		cardManager = new CardLayout();
		tableDeck = new Panel();
		tableDeck.setBackground( Color.yellow );
		tableDeck.setLayout( cardManager );

			// panel that shows current table settings
			Panel tableShowCrd = new Panel();
			tableShowCrd.setBackground( c.mediumBlue );
			tableShowCrd.setLayout( gbLayout2 );
			tableShowCrd.setFont( c.font12b );

			gbc2.weightx = 100;
			gbc2.weighty = 0;
			gbc2.anchor = GridBagConstraints.NORTH;
			gbc2.fill = GridBagConstraints.HORIZONTAL;
			gbc2.insets = new Insets( 0, 0, 2, 0 );
			Label tableTitle1Lbl = new Label ("TABLE SETUP", Label.CENTER );
			tableTitle1Lbl.setFont( c.font14b );
			parent.addComponent( tableShowCrd, tableTitle1Lbl, gbc2, 0, 0, 2, 1 );

			gbc2.insets = new Insets( 0, 2, 3, 0 );
			Label bucketsLbl = new Label ("Buckets", Label.LEFT );
			bucketsLbl.setBackground( c.lightBlue );
			parent.addComponent( tableShowCrd, bucketsLbl, gbc2, 2, 0, 1, 1 );

			gbc2.insets = new Insets( 0, 0, 3, 2 );
			bucketNumLbl = new Label ("35", Label.LEFT );
			bucketNumLbl.setBackground( c.lightBlue );
			parent.addComponent( tableShowCrd, bucketNumLbl, gbc2, 2, 1, 1, 1 );

			gbc2.insets = new Insets( 0, 2, 3, 0 );
			Label slotsLbl = new Label ("Slots per bucket", Label.LEFT );
			slotsLbl.setBackground( c.lightBlue );
			parent.addComponent( tableShowCrd, slotsLbl, gbc2, 1, 0, 1, 1 );
			gbc2.insets = new Insets( 0, 0, 3, 2 );
			slotsNumLbl = new Label ("2", Label.LEFT );
			slotsNumLbl.setBackground( c.lightBlue );
			parent.addComponent( tableShowCrd, slotsNumLbl, gbc2, 1, 1, 1, 2 );

			gbc2.insets = new Insets( 0, 2, 3, 0 );
			Label overFlowLbl = new Label ("Size of overflow", Label.LEFT );
			overFlowLbl.setBackground( c.lightBlue );
			parent.addComponent( tableShowCrd, overFlowLbl, gbc2, 3, 0, 1, 1 );
			gbc2.insets = new Insets( 0, 0, 3, 2 );
			gbc2.fill = GridBagConstraints.BOTH;
			overFlowNumLbl = new Label ("NA", Label.LEFT );
			overFlowNumLbl.setBackground( c.lightBlue );
			parent.addComponent( tableShowCrd, overFlowNumLbl, gbc2, 3, 1, 1, 1 );
		
			gbc2.ipady = 0;
			gbc2.weighty = 100;
			parent.addComponent( tableShowCrd, new Label( " " ), gbc2, 4, 0, 2, 1 );
			tableChangeBtn = new Button( "Change" );
			tableChangeBtn.setFont( c.font12p );
			tableChangeBtn.addActionListener( this );
			gbc2.fill = GridBagConstraints.NONE;
			parent.addComponent( tableShowCrd, tableChangeBtn, gbc2, 5, 0, 2, 1 );

		tableDeck.add( tableShowCrd, "tableShow" );

			// panel that allows user to change number of slots
			Panel tableChangeSlotsCrd = new Panel();
			tableChangeSlotsCrd.setBackground( c.mediumBlue );
			tableChangeSlotsCrd.setLayout( gbLayout3 );
			tableChangeSlotsCrd.setFont( c.font12b );

			gbc3.weightx = 100;
			gbc3.weighty = 0;
			gbc3.anchor = GridBagConstraints.NORTH;
			gbc3.fill = GridBagConstraints.HORIZONTAL;
			gbc3.insets = new Insets( 0, 0, 2, 0 );
			Label tableTitle2Lbl = new Label ("TABLE SETUP", Label.CENTER );
			tableTitle2Lbl.setFont( c.font14b );
			parent.addComponent( tableChangeSlotsCrd, tableTitle2Lbl, gbc3, 0, 0, 3, 1 );

			gbc3.insets = new Insets( 0, 2, 3, 0 );
			Label slots2Lbl = new Label ("Slots per bucket", Label.LEFT );
			slots2Lbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeSlotsCrd, slots2Lbl, gbc3, 1, 0, 1, 1 );

			gbc3.insets = new Insets( 0, 0, 3, 0 );
			slotsTF = new TextField( 2 );
			parent.addComponent( tableChangeSlotsCrd, slotsTF, gbc3, 1, 1, 1, 1 );

			gbc3.insets = new Insets( 0, 0, 3, 2 );
			Label slotsRangeLbl = new Label (" (1-3)", Label.LEFT );
			slotsRangeLbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeSlotsCrd, slotsRangeLbl, gbc3, 1, 2, 1, 2 );

			gbc3.insets = new Insets( 0, 2, 3, 0 );
			Label buckets2Lbl = new Label ("Buckets", Label.LEFT );
			buckets2Lbl.setBackground( c.lightBlue );
			parent.addComponent(tableChangeSlotsCrd, buckets2Lbl, gbc3, 2, 0, 1, 1 );
			
			gbc3.insets = new Insets( 0, 0, 3, 2 );
			bucketNum2Lbl = new Label ("35", Label.LEFT );
			bucketNum2Lbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeSlotsCrd, bucketNum2Lbl, gbc3, 2, 1, 2, 1 );
					
			gbc3.insets = new Insets( 0, 2, 3, 0 );
			Label overFlow2Lbl = new Label ("Size of overflow", Label.LEFT );
			overFlow2Lbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeSlotsCrd, overFlow2Lbl, gbc3, 3, 0, 1, 1 );
		 
		   gbc3.insets = new Insets( 0, 0, 3, 2 );
			gbc3.fill = GridBagConstraints.BOTH;
			overFlowNum2Lbl = new Label ("NA", Label.LEFT );
			overFlowNum2Lbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeSlotsCrd, overFlowNum2Lbl, gbc3, 3, 1, 2, 1 );

			gbc3.ipady = 0;
			gbc3.weighty = 100;
			parent.addComponent( tableChangeSlotsCrd, new Label( " " ), gbc3, 4, 0, 2, 1 );
			tableSaveSlotsBtn = new Button( "Next" );
			tableSaveSlotsBtn.setFont( c.font12p );
			tableSaveSlotsBtn.addActionListener( this );
			gbc3.fill = GridBagConstraints.NONE;
			parent.addComponent( tableChangeSlotsCrd, tableSaveSlotsBtn, gbc3, 5, 0, 3, 1 );

		tableDeck.add( tableChangeSlotsCrd, "tableChangeSlots" );

			// panel that allows user to change number of buckets
			Panel tableChangeBucketsCrd = new Panel();
			tableChangeBucketsCrd.setBackground( c.mediumBlue );
			tableChangeBucketsCrd.setLayout( gbLayout3 );
			tableChangeBucketsCrd.setFont( c.font12b );

			gbc3.weightx = 100;
			gbc3.weighty = 0;
			gbc3.anchor = GridBagConstraints.NORTH;
			gbc3.fill = GridBagConstraints.HORIZONTAL;
			gbc3.insets = new Insets( 0, 0, 2, 0 );
			Label tableTitle3Lbl = new Label ("TABLE SETUP", Label.CENTER );
			tableTitle3Lbl.setFont( c.font14b );
			parent.addComponent( tableChangeBucketsCrd, tableTitle3Lbl, gbc3, 0, 0, 3, 1 );

			gbc3.insets = new Insets( 0, 2, 3, 0 );
			Label slots3Lbl = new Label ("Slots per bucket", Label.LEFT );
			slots3Lbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeBucketsCrd, slots3Lbl, gbc3, 1, 0, 1, 1 );
	 
	 		gbc3.insets = new Insets( 0, 0, 3, 2 );
			slotsNum2Lbl = new Label ("2", Label.LEFT );
			slotsNum2Lbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeBucketsCrd, slotsNum2Lbl, gbc3, 1, 1, 2, 1 );

			gbc3.insets = new Insets( 0, 2, 3, 0 );
			Label buckets3Lbl = new Label ("Buckets", Label.LEFT );
			buckets3Lbl.setBackground( c.lightBlue );
			parent.addComponent(tableChangeBucketsCrd, buckets3Lbl, gbc3, 2, 0, 1, 1 );
			 
			gbc3.insets = new Insets( 0, 0, 3, 0 );
			bucketTF = new TextField( 2 );
			parent.addComponent(tableChangeBucketsCrd, bucketTF, gbc3, 2, 1, 1, 1 );

			gbc3.insets = new Insets( 0, 0, 3, 2 );
			bucketRangeLbl = new Label (" see Notes", Label.LEFT );
			bucketRangeLbl.setBackground( c.lightBlue );
			parent.addComponent(tableChangeBucketsCrd, bucketRangeLbl, gbc3, 2, 2, 1, 1 );

			gbc3.insets = new Insets( 0, 2, 3, 0 );
			Label PrimeLbl = new Label ("Use nearest prime", Label.LEFT );
			PrimeLbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeBucketsCrd, PrimeLbl, gbc3, 3, 0, 1, 1 );
		 
		   gbc3.insets = new Insets( 0, 0, 3, 2 );
			gbc3.fill = GridBagConstraints.BOTH;
			primeCbx = new Checkbox("");
			primeCbx.setBackground( c.lightBlue );
			parent.addComponent( tableChangeBucketsCrd, primeCbx, gbc3, 3, 1, 2, 1 );
		   
			gbc3.insets = new Insets( 0, 2, 3, 0 );
			Label overFlow3Lbl = new Label ("Size of overflow", Label.LEFT );
			overFlow3Lbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeBucketsCrd, overFlow3Lbl, gbc3, 4, 0, 1, 1 );
		
		   gbc3.insets = new Insets( 0, 0, 3, 2 );
			gbc3.fill = GridBagConstraints.BOTH;
			overFlowNum3Lbl = new Label ("NA", Label.LEFT );
			overFlowNum3Lbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeBucketsCrd, overFlowNum3Lbl, gbc3, 4, 1, 2, 1 );

			tableSaveBucketsBtn = new Button( "Next" );
			tableSaveBucketsBtn.setFont( c.font12p );
			tableSaveBucketsBtn.addActionListener( this );
			gbc3.fill = GridBagConstraints.NONE;
			parent.addComponent( tableChangeBucketsCrd, tableSaveBucketsBtn, gbc3, 5, 0, 3, 1 );

		tableDeck.add( tableChangeBucketsCrd, "tableChangeBuckets" );

			// panel that allows user to change overflow size
			Panel tableChangeOverCrd = new Panel();
			tableChangeOverCrd.setBackground( c.mediumBlue );
			tableChangeOverCrd.setLayout( gbLayout3 );
			tableChangeOverCrd.setFont( c.font12b );

			gbc3.weightx = 100;
			gbc3.weighty = 0;
			gbc3.anchor = GridBagConstraints.NORTH;
			gbc3.fill = GridBagConstraints.HORIZONTAL;
			gbc3.insets = new Insets( 0, 0, 2, 0 );
			Label tableTitle4Lbl = new Label ("TABLE SETUP", Label.CENTER );
			tableTitle4Lbl.setFont( c.font14b );
			parent.addComponent( tableChangeOverCrd, tableTitle4Lbl, gbc3, 0, 0, 3, 1 );

			gbc3.insets = new Insets( 0, 2, 3, 0 );
			Label slots4Lbl = new Label ("Slots per bucket", Label.LEFT );
			slots4Lbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeOverCrd, slots4Lbl, gbc3, 1, 0, 1, 1 );

			gbc3.insets = new Insets( 0, 0, 3, 2 );
			slotsNum3Lbl = new Label ("2", Label.LEFT );
			slotsNum3Lbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeOverCrd, slotsNum3Lbl, gbc3, 1, 1, 2, 1 );

			gbc3.insets = new Insets( 0, 2, 3, 0 );
			Label buckets4Lbl = new Label ("Buckets", Label.LEFT );
			buckets4Lbl.setBackground( c.lightBlue );
			parent.addComponent(tableChangeOverCrd, buckets4Lbl, gbc3, 2, 0, 1, 1 );
			
			gbc3.insets = new Insets( 0, 0, 3, 2 );
			bucketNum3Lbl = new Label ("35", Label.LEFT );
			bucketNum3Lbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeOverCrd, bucketNum3Lbl, gbc3, 2, 1, 2, 1 );

			gbc3.insets = new Insets( 0, 2, 3, 0 );
			Label overFlow4Lbl = new Label ("Size of overflow", Label.LEFT );
			overFlow4Lbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeOverCrd, overFlow4Lbl, gbc3, 3, 0, 1, 1 );
		  
			gbc3.insets = new Insets( 0, 0, 3, 0 );
			overflowTF = new TextField( 2 );
			parent.addComponent( tableChangeOverCrd, overflowTF, gbc3, 3, 1, 1, 1 );

			gbc3.insets = new Insets( 0, 0, 3, 2 );
			gbc3.fill = GridBagConstraints.BOTH;
			overFlowRangeLbl = new Label (" ", Label.LEFT );
			overFlowRangeLbl.setBackground( c.lightBlue );
			parent.addComponent( tableChangeOverCrd, overFlowRangeLbl, gbc3, 3, 2, 1, 1 );
		   
			gbc3.ipady = 0;
			gbc3.weighty = 100;
			parent.addComponent( tableChangeOverCrd, new Label( " " ), gbc3, 4, 0, 2, 1 );
			tableSaveOverBtn = new Button( " OK " );
			tableSaveOverBtn.setFont( c.font12p );
			tableSaveOverBtn.addActionListener( this );
			gbc3.fill = GridBagConstraints.NONE;
			parent.addComponent( tableChangeOverCrd, tableSaveOverBtn, gbc3, 5, 0, 3, 1 );

		tableDeck.add( tableChangeOverCrd, "tableChangeOver" );

		// spacer Label used to help position main subpanels
		gbc1.weighty = 100;
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.insets = new Insets( 0, 2, 0, 0 );
		Label spacer2Lbl = new Label( "  " );
		spacer2Lbl.setBackground( Color.white );
		parent.addComponent( this, spacer2Lbl, gbc1, 2, 0, 1, 3 );

		gbc1.anchor = GridBagConstraints.NORTHWEST;
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.insets = new Insets( 2, 2, 2, 2 );
		parent.addComponent( this, tableDeck, gbc1, 2, 1, 1, 1 );

		// spacer Label used to help position main subpanels
		gbc1.insets = new Insets( 0, 0, 0, 0 );
		Label spacer3Lbl = new Label( "   " );
		spacer3Lbl.setBackground( Color.white );
		parent.addComponent( this, spacer3Lbl, gbc1, 2, 2, 1, 3 );

		// spacer Label used to help position main subpanels
		gbc1.insets = new Insets( 0, 0, 0, 0 );
		Label spacer31Lbl = new Label( "  " );
		spacer31Lbl.setBackground( Color.white );
		parent.addComponent( this, spacer31Lbl, gbc1, 3, 1, 1, 1 );

		/********* data subpanel layout *****************************************/
		dataDeck = new Panel();
		dataDeck.setLayout( cardManager );

			// panel that shows current data settings
			Panel dataShowCrd = new Panel();
			dataShowCrd.setBackground( c.mediumBlue );
			dataShowCrd.setLayout( gbLayout2 );
			dataShowCrd.setFont( c.font12b );

			gbc4.weightx = 100;
			gbc4.weighty = 0;
			gbc4.anchor = GridBagConstraints.NORTH;
			gbc4.fill = GridBagConstraints.HORIZONTAL;
			gbc4.insets = new Insets( 0, 0, 2, 0 );
			Label dataTitle1Lbl = new Label ("DATA SETUP", Label.CENTER );
			dataTitle1Lbl.setFont( c.font14b );
			parent.addComponent( dataShowCrd, dataTitle1Lbl, gbc4, 0, 0, 2, 1 );

			gbc4.insets = new Insets( 0, 2, 3, 0 );
			Label uplimitLbl = new Label ("Upper limit", Label.LEFT );
			uplimitLbl.setBackground( c.lightBlue );
			parent.addComponent( dataShowCrd, uplimitLbl, gbc4, 1, 0, 1, 1 );

			gbc4.insets = new Insets( 0, 0, 3, 2 );
			limitNumLbl = new Label ("999", Label.LEFT );
			limitNumLbl.setBackground( c.lightBlue );
			parent.addComponent( dataShowCrd, limitNumLbl, gbc4, 1, 1, 1, 1 );

			gbc4.insets = new Insets( 0, 2, 3, 0 );
			Label dataSizeLbl = new Label ("Data size", Label.LEFT );
			dataSizeLbl.setBackground( c.lightBlue );
			parent.addComponent( dataShowCrd, dataSizeLbl, gbc4, 2, 0, 1, 1 );
			gbc4.insets = new Insets( 0, 0, 3, 2 );

			dataSizeNumLbl = new Label ("50", Label.LEFT );
			dataSizeNumLbl.setBackground( c.lightBlue );
			parent.addComponent( dataShowCrd, dataSizeNumLbl, gbc4, 2, 1, 1, 1 );  /// ,2 );

			gbc4.insets = new Insets( 0, 2, 3, 0 );
			Label dataTypeLbl = new Label ("Data type", Label.LEFT );
			dataTypeLbl.setBackground( c.lightBlue );
			parent.addComponent( dataShowCrd, dataTypeLbl, gbc4, 3, 0, 1, 1 );
			gbc4.insets = new Insets( 0, 0, 3, 2 );
			gbc4.fill = GridBagConstraints.BOTH;
			dataTypeNumLbl = new Label ("what?", Label.LEFT );
			dataTypeNumLbl.setBackground( c.lightBlue );
			parent.addComponent( dataShowCrd, dataTypeNumLbl, gbc4, 3, 1, 1, 1 );
		
			gbc4.ipady = 0;
			gbc4.weighty = 100;
			parent.addComponent( dataShowCrd, new Label( " " ), gbc4, 4, 0, 2, 1 );
			dataChangeBtn = new Button( "Change" );
			dataChangeBtn.setFont( c.font12p );
			dataChangeBtn.addActionListener( this );
			gbc4.fill = GridBagConstraints.NONE;
			parent.addComponent( dataShowCrd, dataChangeBtn, gbc4, 5, 0, 2, 1 );

 			dataDeck.add( dataShowCrd, "dataShow" );
	
			// panel that allows user to change data size, upper limit and type
			Panel dataChangeCrd = new Panel();

			dataChangeCrd.setBackground( c.mediumBlue );
			dataChangeCrd.setLayout( gbLayout3 );
			dataChangeCrd.setFont( c.font12b );

			gbc5.weightx = 100;
			gbc5.weighty = 0;
			gbc5.anchor = GridBagConstraints.NORTH;
			gbc5.fill = GridBagConstraints.HORIZONTAL;
			gbc5.insets = new Insets( 0, 0, 2, 0 );
			Label dataTitle2Lbl = new Label ("DATA SETUP", Label.CENTER );
			dataTitle2Lbl.setFont( c.font14b );
			parent.addComponent( dataChangeCrd, dataTitle2Lbl, gbc5, 0, 0, 3, 1 );
	 
			gbc5.insets = new Insets( 0, 2, 3, 0 );
			Label upperLimit2Lbl = new Label ("Upper limit", Label.LEFT );
			upperLimit2Lbl.setBackground( c.lightBlue );
			parent.addComponent( dataChangeCrd, upperLimit2Lbl, gbc5, 1, 0, 1, 1 );

			gbc5.insets = new Insets( 0, 0, 3, 0 );
			upperLimitTF = new TextField( 2 );
			parent.addComponent( dataChangeCrd, upperLimitTF, gbc5, 1, 1, 1, 1 );

			gbc5.insets = new Insets( 0, 0, 3, 2 );
			Label upperLimitRangeLbl = new Label (" (1-999)", Label.LEFT );
			upperLimitRangeLbl.setBackground( c.lightBlue );
			parent.addComponent( dataChangeCrd, upperLimitRangeLbl, gbc5, 1, 2, 1, 1 );

			gbc5.insets = new Insets( 0, 2, 3, 0 );
			Label dataSize2Lbl = new Label ("Data size", Label.LEFT );
			dataSize2Lbl.setBackground( c.lightBlue );
			parent.addComponent( dataChangeCrd, dataSize2Lbl, gbc5, 2, 0, 1, 1 );

			gbc5.insets = new Insets( 0, 0, 3, 0 );
			dataSizeTF = new TextField( 2 );
			parent.addComponent( dataChangeCrd, dataSizeTF, gbc5, 2, 1, 1, 1 );


			gbc5.insets = new Insets( 0, 0, 3, 2 );
			dataSizeRangeLbl = new Label (" (1-150)", Label.LEFT );
			dataSizeRangeLbl.setBackground( c.lightBlue );
			parent.addComponent( dataChangeCrd, dataSizeRangeLbl, gbc5, 2, 2, 1, 2 );

			gbc5.insets = new Insets( 0, 2, 3, 0 );
			Label dataType2Lbl = new Label ("Data type", Label.LEFT );
			dataType2Lbl.setBackground( c.lightBlue );
			parent.addComponent( dataChangeCrd, dataType2Lbl, gbc5, 3, 0, 1, 1 );

			gbc5.insets = new Insets( 0, 0, 3, 0 );
			dataTypeCB = new Choice( );
			dataTypeCB.add( "Odd" );
			dataTypeCB.add( "Even" );
			dataTypeCB.add( "Mixed" );
			dataTypeCB.addItemListener( this );
			parent.addComponent( dataChangeCrd, dataTypeCB, gbc5, 3, 1, 1, 1 );

			gbc5.insets = new Insets( 0, 0, 3, 2 );
			gbc5.fill = GridBagConstraints.BOTH;
			Label blankLbl = new Label (" ", Label.LEFT );
			blankLbl.setBackground( c.lightBlue );
			parent.addComponent( dataChangeCrd, blankLbl, gbc5, 3, 2, 1, 1 );
		
			gbc5.ipady = 0;
			gbc5.weighty = 100;
			parent.addComponent( dataChangeCrd, new Label( " " ), gbc5, 4, 0, 2, 1 );
			dataSaveBtn = new Button( " OK " );
			dataSaveBtn.setFont( c.font12p );
			dataSaveBtn.addActionListener( this );
			gbc5.fill = GridBagConstraints.NONE;
			parent.addComponent( dataChangeCrd, dataSaveBtn, gbc5, 5, 0, 3, 1 );
	
		dataDeck.add( dataChangeCrd, "dataChange" );
  
		gbc1.weighty = 100;
		gbc1.insets = new Insets( 2, 2, 2, 2 );
		gbc1.anchor = GridBagConstraints.NORTHWEST;
		gbc1.fill = GridBagConstraints.BOTH;
		parent.addComponent( this, dataDeck, gbc1, 4, 1, 1, 1 );

		cardManager.show( dataDeck, "dataShow" );

		/********* help subpanel layout *****************************************/

		// panel that shows the help TextArea
		Panel helpPnl = new Panel();
		helpPnl.setBackground( c.mediumBlue );
		helpPnl.setLayout( gbLayout4 );

			gbc6.weightx = 100;
			gbc6.weighty = 0;
			gbc6.anchor = GridBagConstraints.NORTH;
			gbc6.fill = GridBagConstraints.HORIZONTAL;
			gbc6.insets = new Insets( 0, 0, 2, 0 );
			Label helpTitleLbl = new Label ("NOTES", Label.CENTER );
			helpTitleLbl.setFont( c.font14b );
			parent.addComponent( helpPnl, helpTitleLbl, gbc6, 0, 0, 1, 1 );

			helpTA = new TextArea( setupStr, 8, 20, 
													TextArea.SCROLLBARS_VERTICAL_ONLY );
			helpTA.setEditable( false );
			helpTA.setBackground( Color.white );
			gbc6.insets = new Insets( 2, 2, 2, 2 );
			gbc6.fill = GridBagConstraints.BOTH;
			parent.addComponent( helpPnl, helpTA, gbc6, 1, 0, 1, 1 );
		 
		gbc1.weighty = 100;
		gbc1.anchor = GridBagConstraints.NORTHEAST;
		gbc1.fill = GridBagConstraints.BOTH;
		parent.addComponent( this, helpPnl, gbc1, 2, 3, 1, 1 );

		// spacer Label used to help position main subpanels
		gbc1.insets = new Insets( 0, 0, 0, 2 );
		gbc1.weighty = 100;
		gbc1.fill = GridBagConstraints.BOTH;
		Label spacer24Lbl = new Label( "  " );
		spacer24Lbl.setBackground( Color.white );
		parent.addComponent( this, spacer24Lbl, gbc1, 2, 4, 1, 3 );

		// spacer Label used to help position main subpanels
		Label spacer33Lbl = new Label( " " );
		spacer33Lbl.setBackground( Color.white );
		gbc1.insets = new Insets( 0, 0, 0, 0 );
		parent.addComponent( this, spacer33Lbl, gbc1, 3, 3, 1, 1 );

		/********* execution mode subpanel layout *******************************/
		execModeDeck = new Panel();
		execModeDeck.setBackground( Color.orange );
		execModeDeck.setLayout( cardManager );

			// panel that shows current execution settings
			Panel execShowCrd = new Panel();
			execShowCrd.setBackground( c.mediumBlue );
			execShowCrd.setLayout( gbLayout2 );
			execShowCrd.setFont( c.font12b );

			gbc7.weightx = 100;
			gbc7.weighty = 0;
			gbc7.anchor = GridBagConstraints.NORTH;
			gbc7.fill = GridBagConstraints.HORIZONTAL;
			gbc7.insets = new Insets( 0, 0, 2, 0 );
			Label execTitle1Lbl = new Label ("EXECUTION MODE", Label.CENTER );
			execTitle1Lbl.setFont( c.font14b );
			parent.addComponent( execShowCrd, execTitle1Lbl, gbc7, 0, 0, 2, 1 );

			gbc7.insets = new Insets( 0, 2, 3, 0 );
			Label modeLbl = new Label ("Execution mode", Label.LEFT );
			modeLbl.setBackground( c.lightBlue );
			parent.addComponent( execShowCrd, modeLbl, gbc7, 1, 0, 1, 1 );

			gbc7.insets = new Insets( 0, 0, 3, 2 );
			modeNumLbl = new Label ("Retrieve", Label.LEFT );
			modeNumLbl.setBackground( c.lightBlue );
			parent.addComponent( execShowCrd, modeNumLbl, gbc7, 1, 1, 1, 1 );

			gbc7.insets = new Insets( 0, 2, 3, 0 );
			Label successLbl = new Label ("Percent successful", Label.LEFT );
			successLbl.setBackground( c.lightBlue );
			parent.addComponent( execShowCrd, successLbl, gbc7, 2, 0, 1, 1 );
			gbc7.insets = new Insets( 0, 0, 3, 2 );

			successNumLbl = new Label ("50", Label.LEFT );
			successNumLbl.setBackground( c.lightBlue );
			parent.addComponent( execShowCrd, successNumLbl, gbc7, 2, 1, 1, 1 );  //,2

			gbc7.insets = new Insets( 0, 2, 3, 0 );
			Label quickLoadLbl = new Label ("Quick load", Label.LEFT );
			quickLoadLbl.setBackground( c.lightBlue );
			parent.addComponent( execShowCrd, quickLoadLbl, gbc7, 3, 0, 1, 1 );

			gbc7.insets = new Insets( 0, 0, 3, 2 );
			gbc7.fill = GridBagConstraints.BOTH;
			quickLoadOnLbl = new Label("NA", Label.LEFT);
			quickLoadOnLbl.setBackground( c.lightBlue );
			parent.addComponent( execShowCrd, quickLoadOnLbl, gbc7, 3, 1, 1, 1 );
					
			gbc7.ipady = 0;
			gbc7.weighty = 100;
			gbc7.insets = new Insets( 0, 2, 3, 2 );
			parent.addComponent( execShowCrd, new Label( " " ), gbc7, 4, 0, 2, 1 );
			execChangeBtn = new Button( "Change" );
			execChangeBtn.setFont( c.font12p );
			execChangeBtn.addActionListener( this );
			gbc7.fill = GridBagConstraints.NONE;
			parent.addComponent( execShowCrd, execChangeBtn, gbc7, 5, 0, 2, 1 );

 			execModeDeck.add( execShowCrd, "execShow" );
		
			// panel that allows user to change execution mode and in the case of
			// retrieve, choose the percent successful and whether to use quickload
			Panel execChangeCrd = new Panel();

			execChangeCrd.setBackground( c.mediumBlue );
			execChangeCrd.setLayout( gbLayout3 );
			execChangeCrd.setFont( c.font12b );

			gbc8.weightx = 100;
			gbc8.weighty = 0;
			gbc8.anchor = GridBagConstraints.NORTH;
			gbc8.fill = GridBagConstraints.HORIZONTAL;
			gbc8.insets = new Insets( 0, 0, 2, 0 );
			Label execTitle2Lbl = new Label ("EXECUTION MODE", Label.CENTER );
			execTitle2Lbl.setFont( c.font14b );
			parent.addComponent( execChangeCrd, execTitle2Lbl, gbc8, 0, 0, 3, 1 );
	 
			gbc8.insets = new Insets( 0, 2, 3, 0 );
			Label mode2Lbl = new Label ("Execution mode", Label.LEFT );
			mode2Lbl.setBackground( c.lightBlue );
			parent.addComponent( execChangeCrd, mode2Lbl, gbc8, 1, 0, 1, 1 );

			gbc8.insets = new Insets( 0, 0, 3, 0 );
			execModeCb = new Choice();
			execModeCb.add( "Store" );
			execModeCb.add( "Retrieve" );
			execModeCb.addItemListener( this );
			parent.addComponent( execChangeCrd, execModeCb, gbc8, 1, 1, 1, 1 );

			gbc8.insets = new Insets( 0, 0, 3, 2 );
			Label blank2Lbl = new Label();
			blank2Lbl.setBackground( c.lightBlue );
			parent.addComponent( execChangeCrd, blank2Lbl, gbc8, 1, 2, 1, 1 );

			gbc8.insets = new Insets( 0, 2, 3, 0 );
			Label success2Lbl = new Label ("Percent successful", Label.LEFT );
			success2Lbl.setBackground( c.lightBlue );
			parent.addComponent( execChangeCrd, success2Lbl, gbc8, 2, 0, 1, 1 );

			gbc8.insets = new Insets( 0, 0, 3, 0 );
			successTF = new TextField( 2 );
			parent.addComponent( execChangeCrd, successTF, gbc8, 2, 1, 1, 1 );

			gbc8.insets = new Insets( 0, 0, 3, 2 );
			Label blank3Lbl = new Label();
			blank3Lbl.setBackground( c.lightBlue );
			parent.addComponent( execChangeCrd, blank3Lbl, gbc8, 2, 2, 1, 1 );

			gbc8.insets = new Insets( 0, 2, 3, 0 );
			quickLoad2Lbl = new Label ("Quick load", Label.LEFT );
			quickLoad2Lbl.setBackground( c.lightBlue );
			parent.addComponent( execChangeCrd, quickLoad2Lbl, gbc8, 3, 0, 1, 1 );

			gbc8.insets = new Insets( 0, 0, 3, 0 );
			gbc8.fill = GridBagConstraints.BOTH;
			quickLoadCbx = new Checkbox();
			quickLoadCbx.setBackground( c.lightBlue );
			parent.addComponent( execChangeCrd, quickLoadCbx, gbc8, 3, 1, 1, 1 );
	
			gbc8.insets = new Insets( 0, 0, 3, 2 );
			Label execBlank2Lbl = new Label (" ", Label.LEFT );
			execBlank2Lbl.setBackground( c.lightBlue );
			parent.addComponent( execChangeCrd, execBlank2Lbl, gbc8, 3, 2, 1, 1 );
	 
			gbc8.ipady = 0;
			gbc8.weighty = 100;
			gbc8.fill = GridBagConstraints.BOTH;
			parent.addComponent( execChangeCrd, new Label( " " ), gbc8, 4, 0, 2, 1 );
			execSaveBtn = new Button( " OK " );
			execSaveBtn.setFont( c.font12p );
			execSaveBtn.addActionListener( this );
			gbc8.fill = GridBagConstraints.NONE;
			gbc8.anchor = GridBagConstraints.SOUTH;
			parent.addComponent( execChangeCrd, execSaveBtn, gbc8, 5, 0, 3, 1 );
	
		execModeDeck.add( execChangeCrd, "execChange" );
 
  	   gbc1.weighty = 100;
		gbc1.anchor = GridBagConstraints.NORTH;
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.insets = new Insets( 2, 2, 2, 2 );
		parent.addComponent( this, execModeDeck, gbc1, 4, 3, 1, 1 );

		cardManager.show( execModeDeck, "execShow" );

		// spacer Label used to help position main subpanels
		Label spacer50Lbl = new Label( " " );
		spacer50Lbl.setBackground( Color.white );
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.insets = new Insets( 0, 2, 3, 2 );
		parent.addComponent( this, spacer50Lbl, gbc1, 5, 0, 5, 1 );	
	}// end construction

	// This method sets the page's labels and controls to reflect the values
	// and ranges of the setup object for the currently selected algorithm.
   // The integer argument identifies the algorithm and is used to select
	// the correct setup object from the settings array
	public void loadSettings( int algorithm ){
		currentHash = algorithm;
		titleLbl.setText( "Setup for " + c.titles[algorithm] );
		// get table settings
		bucketNumLbl.setText( "" + settings[algorithm].getBuckets() );
		bucketNum2Lbl.setText( "" + settings[algorithm].getBuckets() );
		bucketNum3Lbl.setText( "" + settings[algorithm].getBuckets() );
 		bucketTF.setText( "" + settings[algorithm].getBuckets() );
		primeCbx.setState( settings[algorithm].getUsePrime() );
		slotsNumLbl.setText( "" + settings[algorithm].getSlots() );
		slotsNum2Lbl.setText( "" + settings[algorithm].getSlots() );
		slotsNum3Lbl.setText( "" + settings[algorithm].getSlots() );
		slotsTF.setText( "" + settings[algorithm].getSlots() );
 	   if( algorithm == c.CHAININGWITHOVERFLOW ){
			overFlowNumLbl.setText( "" + settings[algorithm].getOverflow() );
			overFlowNum2Lbl.setText( "" + settings[algorithm].getOverflow() );
			overFlowNum3Lbl.setText( "" + settings[algorithm].getOverflow() );
			overflowTF.setEditable( true );
			overflowTF.setText( "" + settings[algorithm].getOverflow() );
			overFlowRangeLbl.setText( " (0-" + settings[algorithm].getMaxOverflow() + ")" );
 	   }
		else{
			overFlowNumLbl.setText( "NA" );
			overFlowNum2Lbl.setText( "NA" );
			overFlowNum3Lbl.setText( "NA" );
 			overflowTF.setEditable( false );
 			overflowTF.setText( "" );
 			overFlowRangeLbl.setText( "" );
		}
		bucketRangeLbl.setText( " (1-" + settings[algorithm].getMaxBuckets() + ")" );

 	   // get data settings
		limitNumLbl.setText( "" + settings[algorithm].getUpperLimit() );
		upperLimitTF.setText( "" + settings[algorithm].getUpperLimit() );
		dataSizeNumLbl.setText( "" + settings[algorithm].getDataSize() );
		dataSizeTF.setText( "" + settings[algorithm].getDataSize() );
		dataSizeRangeLbl.setText( " (1-" + settings[algorithm].getMaxDataSize() + ")" );
		dataTypeCB.select( settings[algorithm].getDataType() );
		dataTypeNumLbl.setText( dataTypeCB.getSelectedItem() );

		// get execution settings
		execModeCb.select( settings[algorithm].getExecMode() );
		modeNumLbl.setText( execModeCb.getSelectedItem() );
		quickLoadCbx.setState( settings[algorithm].getQuickLoad() );
		if( execModeCb.getSelectedIndex() == 1 )
			retrieveMode( true );
		else
			retrieveMode( false );
	}

	// This method changes the labels and controls of the execution mode
	// subpanel to reflect whether the demo will be storing or retrieving data
	public void retrieveMode( boolean b ){
		if( b ){
			successNumLbl.setText( "" + settings[currentHash].getSuccess() );
 		   successTF.setText( "" + settings[currentHash].getSuccess() );
			quickLoad2Lbl.setText( "Quick load" );
			if( settings[currentHash].getQuickLoad() )
				quickLoadOnLbl.setText( "On" );
			else
				quickLoadOnLbl.setText( "Off" );	
		}
		else{
			successNumLbl.setText( "NA" );
 		   successTF.setText( "NA" );
			quickLoadOnLbl.setText( "NA" );
			quickLoad2Lbl.setText( "Quick load (NA)" );
		}
		successTF.setEnabled( b );
		quickLoadCbx.setEnabled( b );
	}
 
	// This method disables the Setup panel change buttons and the toolbar
	// buttons. It is called when a user opens a subpanel to modify data.
	public void disableControls(){
		tableChangeBtn.setEnabled(false);
		dataChangeBtn.setEnabled(false);
		execChangeBtn.setEnabled(false);
		buttonPnl.enablePageButtons(false);
		buttonPnl.enableMenu( false );
	}

	// This method enables the Setup panel change buttons and the toolbar
	// buttons. It is called when a user closes a subpanel after modifying data.
	public void enableControls(){
		tableChangeBtn.setEnabled(true);
		dataChangeBtn.setEnabled(true);
		execChangeBtn.setEnabled(true);
		buttonPnl.enablePageButtons(c.SETUP);
		buttonPnl.enableMenu( true );
	}
	
	// This method restores the background of all TextFields to white in case
	// any of them were set to red due to an error condition.
	public void clearFields(){
 		bucketTF.setBackground( Color.white );
 		slotsTF.setBackground( Color.white );
 		overflowTF.setBackground( Color.white );
		upperLimitTF.setBackground( Color.white );
		dataSizeTF.setBackground( Color.white );
		successTF.setBackground( Color.white );
	}

   // This method saves the slot settings from the table subpanel to the setup
	// object.  It returns true if successful, false for an invalid TextField
	// entry.
	public boolean saveSlotChanges(){
		clearFields();
		int slots = readTextField( slotsTF, settings[currentHash].getMaxSlots(), 1 );
		if(  slots == -1 )
			return false;
		settings[currentHash].setSlots( slots );
 		settings[currentHash].setMaxDataSize();
		return true;
	}

	// This method saves the bucket settings from the table subpanel to the setup
	// object.  It returns true if successful, false for an invalid TextField
	// entry.
	public boolean saveBucketChanges(){
		clearFields();
		int buckets = readTextField(bucketTF, settings[currentHash].getMaxBuckets(), 1);
		if(  buckets == -1 )
			return false;
		if( primeCbx.getState() ) 
			buckets = getNearestPrime(buckets, settings[currentHash].getMaxBuckets()); 
		
		settings[currentHash].setUsePrime( primeCbx.getState() );
		settings[currentHash].setBuckets( buckets );
 		settings[currentHash].setMaxDataSize();
		return true;
	}

	// This method saves the overflow settings from the table subpanel to the setup
	// object.  It returns true if successful, false for an invalid TextField
	// entry.
	public boolean saveOverFlowChanges(){
		clearFields();
		int overflow = readTextField(overflowTF, settings[currentHash].getMaxOverflow(), 0);
		if(  overflow == -1 )
			return false;
		settings[currentHash].setOverflow( overflow );
 		settings[currentHash].setMaxDataSize();
		return true;
	}

	// This method saves the data settings from the data subpanel to the setup
	// object.  It returns true if successful, false for any invalid TextField
	// entries.
	public boolean saveDataChanges(){
		clearFields();
		int limit = readTextField(upperLimitTF, settings[currentHash].getMaxUpperLimit(),1);
		if(  limit == -1 )
			return false;
		int size = readTextField( dataSizeTF, settings[currentHash].getMaxDataSize(), 1 );
		if(  size == -1 )
			return false;

		settings[currentHash].setData( limit, size, dataTypeCB.getSelectedIndex()  );

		return true;
	}

	// This method saves the execution settings from the execution subpanel to the 
	// setup object.  It returns true if successful, false for a invalid TextField
	// entry.
	public boolean saveExecutionChanges(){
		clearFields();
		if( execModeCb.getSelectedIndex() == 1 ){
			int success = readTextField( successTF, 100, 0 );
			if(  success == -1 )
				return false;
			else
				settings[currentHash].setSuccess( success );
			settings[currentHash].setQuickLoad( quickLoadCbx.getState() );
		}
		
		settings[currentHash].setExecMode( execModeCb.getSelectedIndex()  );
		return true;
	}

	// Utility function which tests for valid input in a TextField.
	// If input is a valid integer within the specified range it is returned,
	// otherwise the method returns -1
	public int readTextField( TextField tf, int max, int min ){
		int value = -1;
		try{
			value = Integer.parseInt( tf.getText() );
		}
		catch( NumberFormatException nfe ){
			tf.setBackground( Color.red );
	      helpTA.setText( "Input must be an integer" );
			parent.playErrorSound();
			return -1;
		}
		if( value < min || value > max ){
			tf.setBackground( Color.red );
			helpTA.setText( "Input out of range" );
			parent.playErrorSound();
			return -1;
		}
		else
			return value;
	}
  
	// Sieve of Eratosthenes used to generate prime numbers
	// See Deitel and Deitel, Java - How To Program, 2nd Ed, pg 971
	public void initSieve(){
		int size = sieve.size();
		 
		for( int i = 1; i < size; i++ )
			sieve.set(i);
		int finalBit = (int)Math.sqrt( sieve.size() );

		for( int i = 2; i <finalBit; i++ )
			if( sieve.get(i) )
				for( int j = 2*i; j < size; j+=i )
				sieve.clear( j );	
	}
  
	// Method returns the prime number nearest to num that is not larger than max
	public int getNearestPrime( int num, int max ){
		int oldPrime = 1, newPrime = 1; 
		for( int i = 1; i <= max; i++ ){
 			if( sieve.get( i ) ){
				oldPrime = newPrime;
				newPrime = i;
				if( newPrime > num )
					break;
			}
		}

		if( Math.abs( num - oldPrime ) < Math.abs( newPrime - num ) )
			return oldPrime;
		else
			return newPrime;
	}

	/********* event handlers *************************************************/

	// This method handles the Save and OK button clicks for each subpanel	 
	public void actionPerformed( ActionEvent e ){	
		if( e.getSource() == tableChangeBtn ){
			cardManager.show( tableDeck, "tableChangeSlots" );
			helpTA.setText( slotStr );
			disableControls();
		}
		else if( e.getSource() == tableSaveSlotsBtn ){
 			if( saveSlotChanges() ){
				// update allowed bucket range and notify user if the number of 
				// buckets was automatically reduced
				if( settings[currentHash].adjustBucketSize() ){
					parent.playNoticeSound();
					helpTA.setText( shrinkBucketsStr + "\n\n" + bucketStr );
				}
				else
					helpTA.setText( bucketStr );
				// update display with changes
				loadSettings( currentHash );
				if( currentHash == c.CHAININGWITHOVERFLOW )
 		   		tableSaveBucketsBtn.setLabel( "Next" );
				else
					tableSaveBucketsBtn.setLabel( " OK " );
				cardManager.show( tableDeck, "tableChangeBuckets" );
				 
 			}
		}
		else if( e.getSource() == tableSaveBucketsBtn ){
 			if( saveBucketChanges() ){
				if( currentHash == c.CHAININGWITHOVERFLOW ){
					cardManager.show( tableDeck, "tableChangeOver" );
					helpTA.setText( overflowStr );
 			   }
				else{
					cardManager.show( tableDeck, "tableShow" );
					helpTA.setText( setupStr );
					// update allowed data range and notify user if the data size 
					// was automatically reduced
					if( settings[currentHash].adjustDataSize() ){
						parent.playNoticeSound();
						helpTA.setText( shrinkDataStr );
					}
					enableControls();
 				}
				// update display with changes
				loadSettings( currentHash );
			}
		}
		else if( e.getSource() == tableSaveOverBtn ){
 			if( saveOverFlowChanges() ){
				cardManager.show( tableDeck, "tableShow" );
				helpTA.setText( setupStr );
				// update allowed data range and notify user if the data size 
				// was automatically reduced
				if( settings[currentHash].adjustDataSize() ){
					parent.playNoticeSound();
					helpTA.setText( shrinkDataStr );
				}
				enableControls();
				// update display with changes
				loadSettings( currentHash );
 			}
		}
		else if( e.getSource() == dataSaveBtn ){
			if( saveDataChanges() ){
				loadSettings( currentHash );
				cardManager.show( dataDeck, "dataShow" );
				helpTA.setText( setupStr );
				enableControls();
			}
		}
		else if( e.getSource() == dataChangeBtn ){
			cardManager.show( dataDeck, "dataChange" );
			helpTA.setText( dataStr );
			disableControls();
		}
		else if( e.getSource() == execSaveBtn ){
			if( saveExecutionChanges() ){
				loadSettings( currentHash );
				cardManager.show( execModeDeck, "execShow" );
				helpTA.setText( setupStr );
				enableControls();
			}
		}
		else if( e.getSource() == execChangeBtn ){
			cardManager.show( execModeDeck, "execChange" );
			helpTA.setText( executionStr );
			disableControls();
		}
	}

	// This method Store and Retrieve selections on the Execution mode menu	 
	public void itemStateChanged( ItemEvent e ){
		if( execModeCb.getSelectedIndex() == 1 )
			retrieveMode( true );
		else
			retrieveMode( false );
	}
} // end SetupPanel


/******************************************************************************
	The ExecutionPanel class defines the Panel which displays the hashing
	animation canvas, pseudocode display and the algorithm statistics.
******************************************************************************/
class ExecutionPanel extends Panel implements ActionListener{
	// references to other system objects
	private HashApplet parent;
	private HashCanvas hashCanvas;
	private ProgressCanvas progressCanvas;
	// layout
	private Panel statsAndCodeDeck;
	private GridBagLayout gbLayout, gbLayout2, gbLayout3;
	private GridBagConstraints gbc1, gbc2, gbc3, gbc4;
	private CardLayout cardManager;
	// controls and components
	private Button sourceBtn, statsBtn;
	private Label numStoredLbl, hxLbl, homeAddressLbl, timeLbl, probesLbl,
 					  loadLbl, searchesLbl, unsearchesLbl;
	private java.awt.List sourceLst;
	
	// construction
	public ExecutionPanel(HashApplet parent, HashCanvas canvas1, ProgressCanvas canvas2){
		
		this.parent = parent;
		hashCanvas = canvas1;
		progressCanvas = canvas2;

		Common c = new Common();

		/********* panel controls and layout *************************************/

		gbLayout = new GridBagLayout();
		gbLayout2 = new GridBagLayout();
		gbLayout3 = new GridBagLayout();
		gbc1 = new GridBagConstraints();
		gbc2 = new GridBagConstraints();
		gbc3 = new GridBagConstraints();
		gbc4 = new GridBagConstraints();
		
		// panel that shows data value, hash function and home address
		Panel dataPnl = new Panel();
		dataPnl.setLayout( gbLayout );
		dataPnl.setBackground( c.darkBlue );

		Label hashFuncLbl = new Label ("HASH FUNCTION", Label.CENTER );
		hashFuncLbl.setBackground( c.mediumBlue );
		hashFuncLbl.setFont( c.font14b );
 
		gbc1.weightx = 100;
		gbc1.weighty = 0;
		gbc1.anchor = GridBagConstraints.NORTH;
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.insets = new Insets( 0, 0, 2, 0 );

		parent.addComponent( dataPnl, hashFuncLbl, gbc1, 0, 0, 3, 1 );
		
		Panel hashFuncPnl = new Panel();
		hashFuncPnl.setBackground( c.lightBlue );
		hashFuncPnl.setFont( c.font12b );
		hashFuncPnl.setLayout( gbLayout);

		gbc2.weightx = 0;
		gbc2.weighty = 0;
		gbc2.anchor = GridBagConstraints.SOUTH;
		gbc2.fill = GridBagConstraints.HORIZONTAL;
		gbc2.insets = new Insets( 0, 0, 0, 0 );

		Label numLbl = new Label( "Number", Label.CENTER );
		gbc2.ipady = -10;
		parent.addComponent( hashFuncPnl, numLbl, gbc2, 0, 0, 1, 1 );

		Label homeLbl = new Label( "Home", Label.CENTER );
		parent.addComponent( hashFuncPnl, homeLbl, gbc2, 0, 2, 1, 1 );

		Label storedLbl = new Label( "Stored", Label.CENTER );
		parent.addComponent( hashFuncPnl, storedLbl, gbc2, 1, 0, 1, 1 );

		Label hofXLbl = new Label( "h( x )", Label.CENTER );
		gbc2.weighty = 100;
		parent.addComponent( hashFuncPnl, hofXLbl, gbc2, 1, 1, 1, 1 );

	   Label addressLbl = new Label( "Address", Label.CENTER );
		gbc2.weighty = 0;
		parent.addComponent( hashFuncPnl, addressLbl, gbc2, 1, 2, 1, 1 );

		numStoredLbl = new Label("", Label.CENTER );
		numStoredLbl.setFont( c.font12p );
		numStoredLbl.setBackground( Color.white );
		gbc2.fill = GridBagConstraints.HORIZONTAL;
		gbc2.ipady = 0;
		gbc2.insets = new Insets( 5, 5, 5, 5 );
		parent.addComponent( hashFuncPnl, numStoredLbl, gbc2, 2, 0, 1, 1 );

		hxLbl = new Label( "mod 43", Label.CENTER );
		hxLbl.setBackground( Color.white );
		hxLbl.setFont( c.font12p );
		gbc2.insets = new Insets( 5, 0, 5, 0 );
		parent.addComponent( hashFuncPnl, hxLbl, gbc2, 2, 1, 1, 1 );

		homeAddressLbl = new Label("", Label.CENTER);
		homeAddressLbl.setBackground( Color.white );
		homeAddressLbl.setFont( c.font12p );
		gbc2.insets = new Insets( 5, 5, 5, 5 );
		parent.addComponent( hashFuncPnl, homeAddressLbl, gbc2, 2, 2, 1, 1 );

		parent.addComponent( dataPnl, hashFuncPnl, gbc1, 1, 0, 1, 1 );

		// add progressCanvas (pie chart)
		gbc1.weighty = 100;
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.insets = new Insets( 0, 0, 0, 0 );
		parent.addComponent( dataPnl, progressCanvas, gbc1, 2, 0, 1, 1 );

		// This panel shows either the pseudocode or current algorithm statistics 
		cardManager = new CardLayout();
		statsAndCodeDeck = new Panel();
		statsAndCodeDeck.setLayout( cardManager );

	   // This panel contains the Labels that display the current algorithm statistics
		Panel statsCard = new Panel();
		statsCard.setBackground( c.mediumBlue );
		statsCard.setLayout( gbLayout2 );
		statsCard.setFont( c.font12b );

		gbc3.weightx = 100;
		gbc3.weighty = 0;
		gbc3.anchor = GridBagConstraints.NORTH;
		gbc3.fill = GridBagConstraints.HORIZONTAL;
		gbc3.insets = new Insets( 0, 0, 2, 0 );
		Label statsTitleLbl = new Label ("STATISTICS TABLE", Label.CENTER );
		statsTitleLbl.setFont( c.font14b );
		parent.addComponent( statsCard, statsTitleLbl, gbc3, 0, 0, 2, 1 );

		gbc3.ipady = -5;
		gbc3.insets = new Insets( 0, 2, 3, 0 );
		Label timeStaticLbl = new Label ("TIME", Label.LEFT );
		timeStaticLbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, timeStaticLbl, gbc3, 1, 0, 1, 1 );

		gbc3.insets = new Insets( 0, 0, 3, 2 );
		timeLbl = new Label ("00:00:00", Label.LEFT );
		timeLbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, timeLbl, gbc3, 1, 1, 1, 1 );

		gbc3.insets = new Insets( 0, 2, 0, 0 );
		gbc3.ipady = -10;
		Label averageLbl = new Label ("AVERAGE", Label.LEFT );
		averageLbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, averageLbl, gbc3, 2, 0, 1, 1 );
		gbc3.anchor = GridBagConstraints.CENTER;
		gbc3.insets = new Insets( 0, 0, 3, 2 );
		gbc3.fill = GridBagConstraints.BOTH;
		probesLbl = new Label ("0.0", Label.LEFT );
		probesLbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, probesLbl, gbc3, 2, 1, 1, 2 );
		gbc3.anchor = GridBagConstraints.NORTH;
		gbc3.fill = GridBagConstraints.HORIZONTAL;
		gbc3.insets = new Insets( 0, 2, 3, 0 );
		Label probesStaticLbl = new Label ("PROBES", Label.LEFT );
		probesStaticLbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, probesStaticLbl, gbc3, 3, 0, 1, 1 );

		gbc3.insets = new Insets( 0, 2, 0, 0 );
		Label loadStaticLbl = new Label ("LOAD", Label.LEFT );
		loadStaticLbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, loadStaticLbl, gbc3, 4, 0, 1, 1 );
		gbc3.anchor = GridBagConstraints.CENTER;
		gbc3.insets = new Insets( 0, 0, 3, 2 );
		gbc3.fill = GridBagConstraints.BOTH;
		loadLbl = new Label ("0.0%", Label.LEFT );
		loadLbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, loadLbl, gbc3, 4, 1, 1, 2 );
		gbc3.anchor = GridBagConstraints.NORTH;
		gbc3.fill = GridBagConstraints.HORIZONTAL;
		gbc3.insets = new Insets( 0, 2, 3, 0 );
		Label factorLbl = new Label ("FACTOR", Label.LEFT );
		factorLbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, factorLbl, gbc3, 5, 0, 1, 1 );

		gbc3.insets = new Insets( 0, 2, 0, 0 );
		Label successLbl = new Label ("SUCCESSFULL", Label.LEFT );
		successLbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, successLbl, gbc3, 6, 0, 1, 1 );
		gbc3.anchor = GridBagConstraints.CENTER;
		gbc3.insets = new Insets( 0, 0, 3, 2 );
		gbc3.fill = GridBagConstraints.BOTH;
		searchesLbl = new Label ("0", Label.LEFT );
		searchesLbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, searchesLbl, gbc3, 6, 1, 1, 2 );
		gbc3.anchor = GridBagConstraints.NORTH;
		gbc3.fill = GridBagConstraints.HORIZONTAL;
		gbc3.insets = new Insets( 0, 2, 3, 0 );
		Label searchStatic1Lbl = new Label ("SEARCHES", Label.LEFT );
		searchStatic1Lbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, searchStatic1Lbl, gbc3, 7, 0, 1, 1 );

		gbc3.insets = new Insets( 0, 2, 0, 0 );
		Label unsuccessLbl = new Label ("UNSUCCESSFULL", Label.LEFT );
		unsuccessLbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, unsuccessLbl, gbc3, 8, 0, 1, 1 );
		gbc3.anchor = GridBagConstraints.CENTER;
		gbc3.insets = new Insets( 0, 0, 3, 2 );
		gbc3.fill = GridBagConstraints.BOTH;
		unsearchesLbl = new Label ("0", Label.LEFT );
		unsearchesLbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, unsearchesLbl, gbc3, 8, 1, 1, 2 );
		gbc3.anchor = GridBagConstraints.NORTH;
		gbc3.fill = GridBagConstraints.HORIZONTAL;
		gbc3.insets = new Insets( 0, 2, 3, 0 );
		Label searchStatic2Lbl = new Label ("SEARCHES", Label.LEFT );
		searchStatic2Lbl.setBackground( c.lightBlue );
		parent.addComponent( statsCard, searchStatic2Lbl, gbc3, 9, 0, 1, 1 );

		gbc3.ipady = 0;
		gbc3.weighty = 100;
		parent.addComponent( statsCard, new Label( " " ), gbc3, 10, 0, 2, 1 );
		sourceBtn = new Button( "Source Code" );
		sourceBtn.setFont( c.font12p );
		sourceBtn.addActionListener( this );
		gbc3.fill = GridBagConstraints.NONE;
		parent.addComponent( statsCard, sourceBtn, gbc3, 11, 0, 2, 1 );

		// This panel contains the List (sourceLst) that display the pseudocode
		Panel codeCard = new Panel();
		codeCard.setBackground( c.mediumBlue );
		codeCard.setLayout( gbLayout3 );
		codeCard.setFont( c.font12b );

		gbc4.weightx = 100;
		gbc4.weighty = 0;
		gbc4.anchor = GridBagConstraints.NORTH;
		gbc4.fill = GridBagConstraints.HORIZONTAL;
		gbc4.insets = new Insets( 0, 0, 2, 0 );
		Label codeTitleLbl = new Label ("ALGORITHM CODE", Label.CENTER );
		codeTitleLbl.setBackground( c.mediumBlue );
		codeTitleLbl.setFont( c.font14b );

		parent.addComponent( codeCard, codeTitleLbl, gbc4, 0, 0, 1, 1 );

 		gbc4.fill = GridBagConstraints.BOTH;
		gbc4.weighty = 10;
		sourceLst = new java.awt.List( 11, false );
		sourceLst.setFont( c.font11p );
 		parent.addComponent( codeCard, sourceLst, gbc4, 1, 0, 1, 1 );

		gbc4.fill = GridBagConstraints.NONE;
		statsBtn = new Button( "Statistics" );
		statsBtn.setFont( c.font12p );
		statsBtn.addActionListener( this );
		parent.addComponent( codeCard, statsBtn, gbc4, 2, 0, 1, 1 );

		statsAndCodeDeck.add( statsCard, "stats" );
		statsAndCodeDeck.add( codeCard, "code" );

		gbc1.weighty = 0;
		gbc1.insets = new Insets( 0, 0, 2, 0 );
		parent.addComponent( dataPnl, statsAndCodeDeck, gbc1, 3, 0, 1, 1 );

		Panel hashPnl = new Panel( );
		hashPnl.setBackground( c.darkBlue );
		hashPnl.setLayout( new BorderLayout() );
		hashPnl.add( hashCanvas, BorderLayout.CENTER );

		setLayout( new BorderLayout() );
		add( hashPnl, BorderLayout.CENTER );
		add( dataPnl, BorderLayout.EAST );
	}
 /*
	///!!!
	public void Append( String s ){
		debugTA.append( s );
	}

	public void SetText( String s ){
		debugTA.setText( s );
	}
 */
 	// This method returns references to the statistics Labels which are updated
	// during the demo
	public Component[] getControls(){
		Component controls[] = new Component[8];
		controls[0] = numStoredLbl;
		controls[1] = hxLbl;
		controls[2] = homeAddressLbl;
		controls[3] = timeLbl;
		controls[4] = probesLbl;
		controls[5] = loadLbl; 
		controls[6] = searchesLbl;
		controls[7] = unsearchesLbl;

		return controls;
	}

	// This method returns a reference to the pseudocode display List
	public java.awt.List getCodeDisplay(){
		return sourceLst;
	}

	/********* event handlers *************************************************/

	// This method toggles the pseudocode/statistics display 
	public void actionPerformed( ActionEvent e ){
		if( e.getSource() == sourceBtn ){
			parent.setCodeShowing( true );
 	   	cardManager.show( statsAndCodeDeck, "code" );
		}
 	   else{
			parent.setCodeShowing( false );
			cardManager.show( statsAndCodeDeck, "stats" );
		}

	}
}  // end ExecutionPanel


/******************************************************************************
	The ResultsPanel class defines the Panel that displays the settings and
	result summaries for up to 5 completed runs. Methods are provided that
	allow the user to select or unselect each displayed summary.
******************************************************************************/
class ResultsPanel extends Panel implements MouseListener{
	// references to other system objects
	private HashApplet parent;
	private SummaryManager summaryManager;
	private ButtonPanel buttonPnl;
	// layout
	private GridBagLayout gbLayout;
	private GridBagConstraints gbc;
	// controls and components
	private Label resultsLbl[][];
	private Button deleteBtn, undeleteBtn;
	// colors
	private Color frontNormal, backNormal, frontSelected, backSelected;
	private int summaryCount; // number of summaries displayed
//!!!	int i = 1;

	// construction
	public ResultsPanel( HashApplet parent, SummaryManager sm, ButtonPanel bp ){
		
		this.parent = parent;
		summaryManager = sm;
		buttonPnl = bp;

		summaryCount = 0;
		
		Common c = new Common();

		frontNormal = Color.black;
		frontSelected = Color.white;
		backNormal = Color.green;
		backSelected = Color.black;

		setBackground( c.darkBlue );

		/********* panel controls and layout *************************************/

		gbLayout = new GridBagLayout();
		gbc = new GridBagConstraints();
		this.setLayout( gbLayout );

		gbc.weightx = 100;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets( 0, 0, 2, 0 );

		Label titleLbl = new Label( "Current Results", Label.CENTER );
		titleLbl.setBackground( c.mediumBlue );
		titleLbl.setFont( c.font14b );
		parent.addComponent( this, titleLbl, gbc, 0, 0, 3, 1 );

		Panel headerPnl = new Panel();
		headerPnl.setFont( c.font11p );
		headerPnl.setLayout( new GridLayout( 15, 1, 0, 1 ));

		Label runLbl = new Label( " Run number:", Label.LEFT );
		runLbl.setBackground( Color.white );
		headerPnl.add( runLbl );
		Label algorithmLbl = new Label( " Algorithm:", Label.LEFT );
		algorithmLbl.setBackground( Color.white );
		headerPnl.add( algorithmLbl );
		Label slotsLbl = new Label( " Slots per Bucket:" , Label.LEFT );
		slotsLbl.setBackground( Color.white );
		headerPnl.add( slotsLbl );
		Label bucketsLbl = new Label( " Buckets:", Label.LEFT );
		bucketsLbl.setBackground( Color.white );
		headerPnl.add( bucketsLbl );
		Label overflowLbl = new Label( " Overflow size:", Label.LEFT );
		overflowLbl.setBackground( Color.white );
		headerPnl.add( overflowLbl );
		Label upperLimitLbl = new Label( " Upper limit:", Label.LEFT );
		upperLimitLbl.setBackground( Color.white );
		headerPnl.add( upperLimitLbl );
		Label dataSizeLbl = new Label( " Data size:", Label.LEFT );
		dataSizeLbl.setBackground( Color.white );
		headerPnl.add( dataSizeLbl );
		Label dataTypeLbl = new Label( " Data type:", Label.LEFT );
		dataTypeLbl.setBackground( Color.white );
		headerPnl.add( dataTypeLbl );
		Label execModeLbl = new Label( " Execution mode:", Label.LEFT );
		execModeLbl.setBackground( Color.white );
		headerPnl.add( execModeLbl );
		Label percentSuccLbl = new Label( " Success setting:", Label.LEFT );
		percentSuccLbl.setBackground( Color.white );
		headerPnl.add( percentSuccLbl );
		Label timeLbl = new Label( " Time:", Label.LEFT );
		timeLbl.setBackground( Color.white );
		headerPnl.add( timeLbl );
		Label probesLbl = new Label( " Average probes:", Label.LEFT );
		probesLbl.setBackground( Color.white );
		headerPnl.add( probesLbl );
		Label loadLbl = new Label( " Load factor:", Label.LEFT );
		loadLbl.setBackground( Color.white );
		headerPnl.add( loadLbl );
		Label succSearchesLbl = new Label( " Successful searches:", Label.LEFT );
		succSearchesLbl.setBackground( Color.white );
		headerPnl.add( succSearchesLbl );
		Label unsuccSearchesLbl = new Label( " Unsuccessful searches:", Label.LEFT );
		unsuccSearchesLbl.setBackground( Color.white );
		headerPnl.add( unsuccSearchesLbl );

		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets( 0, 2, 0, 4 );

		parent.addComponent( this, headerPnl, gbc, 1, 0, 1, 16 );

		Panel resultPnl = new Panel();
		resultPnl.setFont( c.font11p );
		resultPnl.setLayout( new GridLayout( 15, 5, 3, 1 ) );

 		// Labels display the summary data for each run.
		// Summaries are arranged in columns
		resultsLbl = new Label[16][5];

		for(int i = 0; i<15; i++ )
			for(int j = 0; j<5; j++ ){
				if( i == 0 ){
					resultsLbl[i][j] = new Label("" + (j+1), Label.CENTER);
					resultsLbl[i][j].setBackground( Color.white );	
				}
				else{
					resultsLbl[i][j] = new Label("chain w/o overflow", Label.LEFT);
					resultsLbl[i][j].setBackground( backNormal );
				}
				resultsLbl[i][j].addMouseListener( this );
				resultPnl.add(resultsLbl[i][j]);
			}


	   gbc.weightx = 100;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets( 0, 2, 0, 2 );

		parent.addComponent( this, resultPnl, gbc, 1, 1, 2, 16 );

		Label instructLbl = new Label( "  Click a Run to Select it", Label.LEFT );
		instructLbl.setForeground( Color.yellow );
		instructLbl.setFont( c.font12p );

		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets( 0, 0, 1, 0 );
		parent.addComponent( this, instructLbl, gbc, 17, 0, 3, 1 );
	}// end construction

	// This method displays the summary data for each Summary object 
	// stored in the SummaryManager
	public void showSummaries(){  
		clearDisplay();
		summaryManager.clearSelections();
		summaryCount = summaryManager.getCount();
		Summary sum;
		for( int i = 0; i < summaryCount; i++ ){
			sum = summaryManager.getSummary(i);
				resultsLbl[1][i].setText( "  " + sum.settings.getAlgString() );
				resultsLbl[2][i].setText( "  " + sum.settings.getSlots() );
				resultsLbl[3][i].setText( "  " + sum.settings.getBuckets() );
				resultsLbl[4][i].setText( "  " + sum.settings.getOverflowString());
				resultsLbl[5][i].setText( "  " + sum.settings.getUpperLimit() );
				resultsLbl[6][i].setText( "  " + sum.settings.getDataSize() );
				resultsLbl[7][i].setText( "  " + sum.settings.getDataTypeString());
				resultsLbl[8][i].setText( "  " + sum.settings.getExecModeString());
				resultsLbl[9][i].setText( "  "  + sum.settings.getSuccessString());
				resultsLbl[10][i].setText( "  "  + sum.results.getTime());
				resultsLbl[11][i].setText( "  " + sum.results.getProbes() );
				resultsLbl[12][i].setText( "  " + sum.results.getLoad() + "%" );
				resultsLbl[13][i].setText( "  " + sum.results.getSuccess() );
				resultsLbl[14][i].setText( "  " + sum.results.getUnsuccess() );
		}   
	}
 
	// This method erases all data from the results Labels and restores their
	// foreground and background colors to unselected values 
	public void clearDisplay(){
		for( int j=0; j<5; j++ ){
			showUnselected( j );
			for( int i = 0; i<15; i++ ){
				resultsLbl[i][j].setText( "" );
	  		}
			resultsLbl[0][j].setText( "" + (j+1) );
		}			
	}
	
	// This method changes all the result labels in a column to their "selected"
   // colors
	public void showSelected( int col ){
		resultsLbl[0][col].setText( "" + (col+1) + " *" );
		for( int i = 0; i<15; i++ ){
			resultsLbl[i][col].setBackground( backSelected );
			resultsLbl[i][col].setForeground( frontSelected );
		}
	}

	// This method changes all the result labels in a column to their 
	// "unselected" colors
	public void showUnselected( int col ){
		resultsLbl[0][col].setText( "" + (col+1) );
		for( int i = 0; i<15; i++ ){
			resultsLbl[i][col].setBackground( backNormal );
			resultsLbl[i][col].setForeground( frontNormal );
		}
	}

	/********* event handlers *************************************************/
	
	// This method selects or unselects a summary when the user clicks on one 
	// of its Labels.  The SummaryManager is queried to determine whether the
	// summary associated with the Label is selected or not and then the summary
	// is set to the opposite state.
	public void mouseClicked( MouseEvent e){
		for( int i = 0; i<15; i++ )
			for( int j=0; j < summaryCount; j++ )
				if( e.getSource() == resultsLbl[i][j] )
					if( summaryManager.isSelected( j )){
						summaryManager.setSelected( j, false );
						showUnselected(j);
					}
					else{
						summaryManager.setSelected( j, true );	
						showSelected( j );
				 	}
		// enable or disable Delete and Undelete buttons to reflect available 
		// options
		buttonPnl.setEditButtons( summaryManager.isSelection(),
	                             summaryManager.beenDeleted());  
	}

	// Methods required by MouseListener interface but not used
	public void mousePressed( MouseEvent e){}

	public void mouseReleased( MouseEvent e){}

	public void mouseEntered( MouseEvent e){}

	public void mouseExited( MouseEvent e){}
} // end ResultsPanel


/******************************************************************************
	The PlotPanel class defines the Panel that displays the settings and
	result summaries for up to 5 completed runs. Methods are provided that
	allow the user to select or unselect each displayed summary.
******************************************************************************/
class PlotPanel extends Panel implements MouseListener{
	// references to other system objects
	private HashApplet parent;
	private SummaryManager summaryManager;
	private PlotCanvas plotCanvas;
	// controls and components
	private Label resultsLbl[][];
	// colors
	private Color frontNormal, backNormal, frontSelected[], backSelected;
	// number of summaries displayed
	private int summaryCount;	
	// contains commonly used constants, fonts, strings, etc.
	private Common c;

	// construction
	public PlotPanel( HashApplet parent, SummaryManager sm, PlotCanvas canvas ){
		
		this.parent = parent;
		summaryManager = sm;
		plotCanvas = canvas;

		c = new Common();

		summaryCount = 0;

		backNormal = Color.green;
		frontNormal = Color.black;
		// each algorithm plot is draw in a different color
		frontSelected = new Color[5];
		frontSelected[0] = Color.orange;
		frontSelected[1] = Color.cyan;
		frontSelected[2] = Color.red;
		frontSelected[3] = Color.magenta;
		frontSelected[4] = Color.green;
		backSelected = Color.black;

		this.setBackground( c.lightBlue );
		
		/********* panel controls and layout *************************************/

		GridBagLayout gbLayout = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		this.setLayout( new BorderLayout() );

		Panel titlePnl = new Panel();
		titlePnl.setLayout( gbLayout );
		titlePnl.setBackground( c.darkBlue );

		gbc.weightx = 100;
		gbc.weighty = 100;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets( 0, 0, 2, 0 );

		Label titleLbl = new Label( "Result Plots", Label.CENTER );
		titleLbl.setBackground( c.mediumBlue );
		titleLbl.setFont( c.font14b );
		parent.addComponent( titlePnl, titleLbl, gbc, 0, 0, 1, 1 );

		this.add( titlePnl, BorderLayout.NORTH );
		this.add( plotCanvas, BorderLayout.CENTER );

		Panel runPnl = new Panel();
		runPnl.setLayout( gbLayout );
		runPnl.setBackground( c.darkBlue );
		runPnl.setFont( c.font11p );
		
		// Labels display some summary data for each run.
		// Summaries are arranged in rows
		resultsLbl = new Label[6][8];

		// create row headers
		resultsLbl[0][0] = new Label("Run", Label.CENTER);
		resultsLbl[0][0].setBackground( Color.white );

		resultsLbl[0][1] = new Label("   Algorithm   ", Label.CENTER);
		resultsLbl[0][1].setBackground( Color.white );
		
		resultsLbl[0][2] = new Label("Buckets", Label.CENTER);
		resultsLbl[0][2].setBackground( Color.white );

		resultsLbl[0][3] = new Label("Slots/bucket", Label.CENTER);
		resultsLbl[0][3].setBackground( Color.white );

		resultsLbl[0][4] = new Label("Overflow size", Label.CENTER);
		resultsLbl[0][4].setBackground( Color.white );

		resultsLbl[0][5] = new Label("Data size", Label.CENTER);
		resultsLbl[0][5].setBackground( Color.white );

		resultsLbl[0][6] = new Label("Execution mode", Label.CENTER);
		resultsLbl[0][6].setBackground( Color.white );

		resultsLbl[0][7] = new Label("Unsucc searches", Label.CENTER);
		resultsLbl[0][7].setBackground( Color.white );
 
		gbc.weightx = 100;
		gbc.weighty = 100;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets( 1, 1, 1, 1 );
		gbc.ipady = -6;

		for(int i = 0; i < 8; i++ ){
			if( i == 1 )
				gbc.weightx = 100;
			else
				gbc.weightx = 50;
			parent.addComponent( runPnl, resultsLbl[0][i], gbc, 0, i, 1, 1 );
		}

		gbc.ipady = -8;
		// create Labels that hold actual summary data
		for(int i = 1; i<=5; i++ ){
			for(int j = 0; j<8; j++ ){
				if( j == 0 ){
					resultsLbl[i][j] = new Label("" + i, Label.CENTER);
					resultsLbl[i][j].setBackground( Color.white );
					resultsLbl[i][j].setFont( c.font11p );
				}
				else{
					resultsLbl[i][j] = new Label("HUH", Label.LEFT);
					resultsLbl[i][j].setBackground( backNormal );
				}
				resultsLbl[i][j].addMouseListener( this );
				if( i == 1 )
					gbc.weightx = 100;
				else
					gbc.weightx = 50;
 				parent.addComponent( runPnl, resultsLbl[i][j], gbc, i, j, 1, 1 );
			}
		 }

		 this.add( runPnl, BorderLayout.SOUTH );
	}// end construction

	// This method displays summary data for each Summary object 
	// stored in the SummaryManager
	public void showSummaries(){  
		clearDisplay();
		summaryManager.clearSelections();
		summaryCount = summaryManager.getCount();
		Summary sum;
		for( int i = 1; i < summaryCount+1; i++ ){
			sum = summaryManager.getSummary(i-1);
				resultsLbl[i][1].setText( "  " + sum.settings.getAlgString() );
				resultsLbl[i][2].setText( "  " + sum.settings.getBuckets() );
				resultsLbl[i][3].setText( "  " + sum.settings.getSlots() );
				resultsLbl[i][4].setText( "  " + sum.settings.getOverflowString());
			 	resultsLbl[i][5].setText( "  " + sum.settings.getDataSize() );
				resultsLbl[i][6].setText( "  " + sum.settings.getExecModeString());
				resultsLbl[i][7].setText( "  " + sum.results.getUnsuccess() );
		}   
	}
 
	// This method erases all data from the results Labels and restores their
	// foreground and background colors to unselected values 
	public void clearDisplay(){
		for( int j=1; j<6; j++ ){
			showUnselected( j );
			for( int i = 0; i<8; i++ ){
				resultsLbl[j][i].setText( "" );
	  		}
			resultsLbl[j][0].setText( "" + j );
		}			
	}
	
	// This method changes all the result labels in a row to their "selected"
   // colors
	public void showSelected( int row ){
		if( summaryManager.getSummary( row - 1 ).settings.getExecMode() == c.STORE )
			resultsLbl[row][0].setText( "" + row + " *" );
		else
			resultsLbl[row][0].setText( "" + row + " **" );
		for( int i = 0; i<8; i++ ){
			resultsLbl[row][i].setBackground( backSelected );
			resultsLbl[row][i].setForeground( frontSelected[row-1] );
		}
	}

	// This method changes all the result labels in a row to their "unselected"
   // colors
	public void showUnselected( int row ){
		resultsLbl[row][0].setText( "" + row );
		for( int i = 0; i<8; i++ ){
			if( i == 0 )
				resultsLbl[row][i].setBackground( Color.white );
			else
				resultsLbl[row][i].setBackground( backNormal );
			resultsLbl[row][i].setForeground( frontNormal );
		}
	}

	/********* event handlers *************************************************/
	
	// This method selects or unselects a summary when the user clicks on one 
	// of its Labels.  The SummaryManager is queried to determine whether the
	// summary associated with the Label is selected or not and then the summary
	// is set to the opposite state. Data for all selected summaries is then
	// plotted by calling repaint().
	public void mouseClicked( MouseEvent e){
		for( int i = 1; i<=summaryCount; i++ )
			for( int j=0; j<8; j++ )
				if( e.getSource() == resultsLbl[i][j] ){
					if( summaryManager.isSelected( i-1 )){
						summaryManager.setSelected( i-1, false );
						showUnselected(i);
					}
					else{
						summaryManager.setSelected( i-1, true );	
						showSelected( i );
				 	}
				}
		plotCanvas.repaint();
	}

	// Methods required by MouseListener interface but not used
	public void mousePressed( MouseEvent e){}

	public void mouseReleased( MouseEvent e){}

	public void mouseEntered( MouseEvent e){}

	public void mouseExited( MouseEvent e){}
} // end PlotPanel


/******************************************************************************
	The SplashCanvas class defines the drawing area and paint method that 
	displays the opening animation sequence.
******************************************************************************/
class SplashCanvas extends Canvas{
	// image and Graphics context
	private Image image;
	private Graphics offscreen;
	static final int NUM_STARS  =  50;		
	private int scrnWidth, scrnHeight,					// canvas dimensions
	 			   hashX, hashEndX, hashY, hashEndY,   // position variables for
	            animX, animEndX, animY, animEndY,	// 	animated titles
					toolX, toolY, toolEndX, toolEndY,
					byX, byY, byEndY, instrY, 
	 				starsX[], starsY[], 						// star coordinates
					colorIndex = 0;
  	double starsAngle[];										// star positioning variables
  	double starsRadius[];
  	double starsMinRadius[];
	double starsMaxRadius[];
	// animated title text
	private String hashStr, animStr, toolStr, byStr, instructStr;
	private Font font48b, font14i;
	// misc
	private boolean firstShow = true;
	private Color fadeColor;

	// construction
	public SplashCanvas( Image img ){
		image = img;
		offscreen = image.getGraphics();
		starsX = new int[NUM_STARS];
  	   starsY = new int[NUM_STARS];
		starsAngle = new double[NUM_STARS];
  		starsRadius = new double[NUM_STARS];
  		starsMinRadius = new double[NUM_STARS];
		starsMaxRadius = new double[NUM_STARS];

		hashStr = "Hashing";
		animStr = "Animation";
		toolStr = "Tool";
		char copy = '\u00A9';  // unicode copyright symbol
		byStr = "By Catalyst Software " + copy + " 2000";
		instructStr = "Select an algorithm or click Setup to begin";

		fadeColor = Color.black;

		font48b = new Font( "SanSerif", Font.BOLD, 48 );
		font14i = new Font( "Serif", Font.ITALIC, 14 );
	}
  
	// position a new star near the center of the screen
	public void initStar(int i) {
		starsAngle[i] = Math.random() * 2 * Math.PI;
    	starsMaxRadius[i] = Math.max(scrnWidth, scrnHeight) / 2 +
      Math.random() * Math.min(scrnWidth, scrnHeight) / 4;
    	starsRadius[i] = starsMaxRadius[i] / (Math.random() * 9 + 1);
  	}

	// the method moves stars outward, if a start leaves the viewing area, a new 
	// star is placed near the center
  	public void updateStars() {
    	int i;
    	// move stars outward
    	for (i = 0; i < NUM_STARS; i++) {
      	if((starsRadius[i] *= 1.05) > starsMaxRadius[i])
        		initStar(i);
      	starsX[i] = (int) (scrnWidth / 2  + starsRadius[i] * Math.sin(starsAngle[i]));
      	starsY[i] = (int) (scrnHeight / 2 + starsRadius[i] * Math.cos(starsAngle[i]));
    	}
  	}

	// method is called to modify the coordinates of animated elements, it should
	// be followed by a call to repaint the canvas
	public void updateGraphics(){
		updateStars();					// position stars
	
		if( hashY < hashEndY )		// change the coordinates of each title String
			hashY+=2;
		if( animX < animEndX )
			animX += 4;
		if( toolX > toolEndX )
			toolX -= 4;
		if( byY > byEndY )
			byY -= 2;
		else{								// after title is in position change color of 
			if( colorIndex < 255 )  // 	instruction string from black to yellow
				colorIndex +=5;
			fadeColor = new Color( colorIndex, colorIndex, 0 );
		}
	}

	// this method determines the size of the canvas and calculates the start and
	// end coordinates for the animated title strings
	public void initialize(Graphics g){
		Dimension d = this.getSize();
		scrnWidth = d.width;
		scrnHeight = d.height;
		for( int i = 0; i < NUM_STARS; i++ )
			initStar(i);

		offscreen.setFont( font48b );
		FontMetrics fm = offscreen.getFontMetrics();
		int height = fm.getHeight();
		animY = animEndY = scrnHeight/2;
		int hashWidth = fm.stringWidth( hashStr );
		hashX = hashEndX = (scrnWidth - hashWidth)/2;
		hashEndY = animY - height;
		int animWidth = fm.stringWidth( animStr );
		animEndX = (scrnWidth - animWidth)/2;
		int toolWidth = fm.stringWidth( toolStr );

		toolEndX = (scrnWidth - toolWidth)/2;
 		toolY = toolEndY = animY + height;
		int bip = 150;     			// this represents the number of animation
		animX = animEndX - 4*bip;  // 	cycles required to position the title
		hashY = hashEndY - 2*bip;  // startPos = endPos - (pixels/cycle)*cycles
		toolX = toolEndX + 4*bip;

		byX = toolEndX + toolWidth;
		byEndY = toolEndY + 25;
		byY = byEndY + 2*bip;

		instrY = scrnHeight - 15;

		firstShow = false;			
	}

	// draws the titles and stars to the canvas	
	public void paint(Graphics g){
		if( firstShow )			// only calculate the start and end positions once
 	   	initialize(g);
			
	 	offscreen.setColor( Color.black );
 	 	offscreen.fillRect(0, 0, scrnWidth, scrnHeight );
		offscreen.setColor( Color.white );
		for (int i = 0; i < NUM_STARS; i++)
      	offscreen.drawLine(starsX[i], starsY[i], starsX[i], starsY[i]);
		offscreen.setColor( Color.red );
 	   offscreen.setFont( font48b );
		offscreen.drawString( hashStr, hashX, hashY );
		offscreen.drawString( animStr, animX, animY );
		offscreen.drawString( toolStr, toolX, toolY );
		offscreen.setFont( font14i );
		offscreen.setColor( Color.yellow );
		offscreen.drawString( byStr, byX, byY );
		offscreen.setFont( Common.font12p );
		offscreen.setColor( fadeColor );
		offscreen.drawString( instructStr, 10, instrY );
	 	g.drawImage( image, 0, 0, this );
 	
	}

	// override update for smoother animation
	public void update(Graphics g ){
		paint(g);
	}
} // end SplashCanvas


/******************************************************************************
	The HashCanvas class defines the drawing area and paint method that 
	displays the animated hash tables and the status text.
******************************************************************************/
class HashCanvas extends Canvas{
	// references to other system objects
	private HashTable hashTable, overFlow;
	private Common c;
	private HashStatus hashStatus;
	// image and Graphics context
	private Image image;
	private Graphics offscreen;
	// canvas dimensions
	private int width, height;		
	
	// construction
	public HashCanvas( int w, int h, Image img ){
		width = w; 
		height = h;
		image = img;
		offscreen = image.getGraphics();
		c = new Common();
	}

	// associate HashStatus variable with system object
	public void setHashStatus( HashStatus hs ){
		hashStatus = hs;
	}

	// associate HashTable variable with system hash table object
	public void addTable( HashTable ht ){
		hashTable = ht;
	}

	// associate HashTable variable with system overflow table object
	public void addOverFlow( HashTable of ){
		overFlow = of;
	}

	// draws the hash tables and hash status to the canvas	
	public void paint(Graphics g){
		Dimension d = this.getSize();
 	 	offscreen.setColor( c.darkBlue );
  	 	offscreen.fillRect(0, 0, d.width-1, d.height-1 );
		hashStatus.paint( offscreen );
		hashTable.paint( offscreen );
		overFlow.paint( offscreen );
	 	g.drawImage( image, 0, 0, this );
 	
	}

	// override update for smoother animation
  	public void update(Graphics g ){
   		paint(g);
  	}
} // end HashCanvas


/******************************************************************************
	The ProgressCanvas class defines the drawing area and paint method that 
	displays the animated pie chart that indicates the remaining number of 
	values that need to be stored or retrieved.
******************************************************************************/
class ProgressCanvas extends Canvas{
	// image and Graphics context
	private Image image;
	private Graphics offscreen;
	// contains commonly used constants, fonts, strings, etc.
	private Common c;
	// canvas dimensions, number remaining, angle of pie slice
	private int width, height, numLeft, angle;

	// construction
	public ProgressCanvas( int w, int h, Image img ){
		width = w; 
		height = h;
		setSize( w, h );

		Common c = new Common();

		image = img;
		offscreen = image.getGraphics();
	}

	// This method calculates the angle of the pie slice based on the total 
	// number of values and the number remaining. This is followed by a call
   // to repaint the canvas
	public void drawPie( int total, int numLeft ){
		this.numLeft = numLeft;
		angle = -360*(total - numLeft)/total;
		repaint();
	}

	// draws the pie chart to the canvas as well as the number remaining	
	public void paint(Graphics g){
		int x = 100, y = 20, diam = 60;
	 	offscreen.setColor( Color.black );
 	 	offscreen.fillRect(0, 0, width, height );
		offscreen.setColor( Color.green );
		offscreen.setFont( c.font12b );
		offscreen.drawString( "Numbers", 15, 15 );
		offscreen.drawString( "Remaining", 15, 30 );
		offscreen.drawString( "" + numLeft, 25, 45 );
		FontMetrics fm = offscreen.getFontMetrics();
		offscreen.setFont( c.font10b );
		int width = fm.stringWidth( "100" );
		offscreen.drawString( "100%", x + diam/2 - width/2, 15 );
		offscreen.drawLine( x + diam/2, y, x + diam/2, y - 3 ); 
		offscreen.fillOval( x, y, diam, diam );
		offscreen.setColor( Color.black );
		offscreen.fillArc( x+1, y+1, diam-2, diam-2, 90, angle);
		offscreen.drawLine( 0, 100, 200, 100 );
	 	g.drawImage( image, 0, 0, this );
	}

	// override update for smoother animation
 	public void update(Graphics g ){
 		paint(g);
 	}
} // end Progress canvas

/******************************************************************************
	The PlotCanvas class defines the drawing area and methods that plot graphs
	of average time and average probes vs average load factor and 
	numbers searched so far/ total numbers to search. Plots for up to five
	demos may be displayed and each is drawn in a different color.  There are
	three possible displays: Two small plots or one large time plot or one
	large average probes plot.
******************************************************************************/
class PlotCanvas extends Canvas{
	// references to other system objects
	private SummaryManager summaryManager;
	private Common c;
	// image and Graphics context
	private Image image;
	private Graphics offscreen;
	// constants corresponding to the three possible views
	public static final int TIME = 0, PROBES = 1, BOTH = 2;
	// canvas dimensions
	private int width, height;
	// plot colors
	private Color lineColor[];
	// plot drawing variables
	private int xAxisLength, bigXaxisLength, yAxisLength, originX, originX1,
 					originY, originX2, margin, maxX, maxY, yAxisTitleLength,
				   view; // current view

	// construction
	public PlotCanvas( Image img, SummaryManager sm ){
		image = img;
		summaryManager = sm;
		offscreen = image.getGraphics();

		c = new Common();
		FontMetrics fm = getFontMetrics( c.font12b );
 	   lineColor = new Color[5];
		lineColor[0] = Color.orange;
		lineColor[1] = Color.cyan;
		lineColor[2] = Color.red;
		lineColor[3] = Color.magenta;
		lineColor[4] = Color.green;

		view = BOTH;
	}

	// selects one of the 3 possible views and repaints the canvas to display it
	public void setView( int view ){
		this.view = view;
		repaint();
	}

	/********* methods to draw the grids and axis labels ***********************/

	// This method determines the dimensions of the canvas and calculates the
	// the values for the coordinates of the three origins
	public void calcOrigins( Graphics g){
		margin = 20;
		FontMetrics fm = g.getFontMetrics( c.font12b );
		yAxisTitleLength = fm.stringWidth( "Average 0.000" );
	
		Dimension d = this.getSize();
		maxX = d.width - 1;
		maxY = d.height - 1;
		xAxisLength = 200;
		yAxisLength = 200;
		// small time plot origin
		originX1 = ((maxX - 2*xAxisLength)/3) - 10;

		// large plots origin
		bigXaxisLength = 400;
		originX = (maxX - bigXaxisLength)/2;
		
		// small probes plot origin
		originX2 = maxX/2 + originX1;
		originY = 2 + yAxisLength ;
	}

	// This method draws the grids for the two small plots
	public void draw2Grids( Graphics g ){
		g.setColor( Color.white );

		// grid 1 axes and titles
			g.drawLine( originX1, 2, originX1, 2+yAxisLength );
			drawXaxisLabel( g, originX1, originY, originX1 + xAxisLength, 20 );
			g.drawLine( originX1, 2+yAxisLength, originX1 + xAxisLength, 2 + yAxisLength );
			drawYaxisTimeLabel( g, originX1, originY-20, 2, 20 );

			drawGridTimeTitle( g, originX1 );

			// draw horizontal dashed lines
 			for( int i = originY -10; i >= 2; i -=10)
 				drawHorizDashedLine( g, originX1+1, i, originX1 + xAxisLength, 1 );

			// draw vertical dashed lines
			for( int i = originX1 +20; i <= originX1 + xAxisLength; i +=20)
				drawVertDashedLine( g, i, originY-1, originY - yAxisLength, 1 );

		// grid 2 axes and titles
			g.drawLine( originX2, 2, originX2, 2+yAxisLength );
			drawXaxisLabel( g, originX2, originY, originX2 + xAxisLength, 20 );
			g.drawLine( originX2, 2+yAxisLength, originX2 + xAxisLength, 2 + yAxisLength );
			drawYaxisProbeLabel( g, originX2, originY-20, 2, 20 );

			drawGridProbeTitle( g, originX2 );

			// draw horizontal dashed lines
 			for( int i = originY -10; i >= 2; i -=10)
 				drawHorizDashedLine( g, originX2+1, i, originX2 + xAxisLength, 1 );

			// draw vertical dashed lines
			for( int i = originX2 +20; i <= originX2 + xAxisLength; i +=20)
				drawVertDashedLine( g, i, originY-1, originY - yAxisLength, 1 );

		 draw2XTitles( g );
	}

	// This method draws the large grid that is used for both the large time
	// and large probe plots
	public void draw1Grid( Graphics g ){
		g.setColor( Color.white );

		// grid axes and X axis values
		g.drawLine( originX, 2, originX, 2+yAxisLength );
		drawXaxisLabel( g, originX, originY, originX + bigXaxisLength, 40 );
		g.drawLine( originX, 2+yAxisLength, originX + bigXaxisLength, 2 + yAxisLength );

		// draw horizontal dashed lines
 		for( int i = originY -10; i >= 2; i -=10)
 			drawHorizDashedLine( g, originX+1, i, originX + bigXaxisLength, 1 );

		// draw vertical dashed lines
		for( int i = originX +40; i <= originX + bigXaxisLength; i +=40)
			drawVertDashedLine( g, i, originY-1, originY - yAxisLength, 1 );

		 draw1XTitles( g );
	}

	// draws a dashed horizontal line between the specified x values
	public void drawHorizDashedLine( Graphics g, int x1, int y1, int x2, int dashLen ){
		for( int i = x1; i <= x2; i+=3*dashLen )
			g.drawLine( i, y1, i+dashLen-1, y1);
   }

	// draws a dashed vertical line between the specified y values
	public void drawVertDashedLine( Graphics g, int x1, int y1, int y2, int dashLen ){
		for( int i = y1; i >= y2; i-=3*dashLen )
			g.drawLine( x1, i, x1, i-(dashLen-1));
   }

	// draws the X axis values and tic marks
	public void drawXaxisLabel( Graphics g, int xstart, int y, int xstop, int gap ){
		int lbl = 0;
		for( int i = xstart; i <= xstop; i+=gap, lbl+=10 ){
 	   	g.drawLine( i, y, i, y+3 );
			g.setFont( c.font11p );
			FontMetrics fm = g.getFontMetrics();
			int width = fm.stringWidth( "" + lbl );
			g.drawString("" + lbl, i-width/2, y + 14 );
 	   }
	}

	// draws the Y axis values and tic marks for the Average Time plot
	public void drawYaxisTimeLabel( Graphics g, int x, int ystart, int ystop, int gap ){
		double lbl = .20;
		for( int i = ystart; i > ystop; i-=gap, lbl+=.20 ){
 	   	g.drawLine( x, i, x-3, i );
			g.setFont( c.font11p );
			FontMetrics fm = g.getFontMetrics();
			int height = fm.getAscent();
			lbl = (Math.floor(lbl*10 + .5))/10; 
			g.drawString("" + lbl, x-25, i+height/2  );
 	   }
	}
	
	// draws the Y axis values and tic marks for the Average Probe plot
	public void drawYaxisProbeLabel( Graphics g, int x, int ystart, int ystop, int gap ){
		int lbl = 2;
		for( int i = ystart; i > ystop; i-=gap, lbl+=2 ){
 	   	g.drawLine( x, i, x-3, i );
			g.setFont( c.font11p );
			FontMetrics fm = g.getFontMetrics();
			int height = fm.getAscent();
 	   	if( lbl < 10 )
				g.drawString(" " + lbl, x-20, i+height/2 );
			else
				g.drawString("" + lbl, x-20, i+height/2  );
 	   }
	}		

	// draws the "Average Time" label midway up the y axis from the specified 
	// x origin cooridinate
	public void drawGridTimeTitle( Graphics g, int xOrigin ){
		g.setFont( c.font12p );
		FontMetrics fm = g.getFontMetrics();
		int height = fm.getAscent();
		int strWidth1 = fm.stringWidth( "Average" );
		int strWidth2 = fm.stringWidth( "Time" );
		int yMidPoint = originY - yAxisLength/2;
		int xpos = xOrigin - yAxisTitleLength; 
		g.drawString( "Average", xpos, yMidPoint  - height/2); 
		g.drawString( "Time", xpos +(strWidth1-strWidth2)/2, yMidPoint + height/2);
   }

	// draws the "Average Probe" label midway up the y axis from the specified 
	// x origin cooridinate
	public void drawGridProbeTitle( Graphics g, int xOrigin ){
		g.setFont( c.font12p );
		FontMetrics fm = g.getFontMetrics();
		int height = fm.getAscent();
		int strWidth1 = fm.stringWidth( "Average" );
		int strWidth2 = fm.stringWidth( "Probes" );
		int yMidPoint = originY - yAxisLength/2;
		int xpos = xOrigin - yAxisTitleLength; 
		g.drawString( "Average", xpos, yMidPoint  - height/2); 
		g.drawString( "Probes", xpos +(strWidth1-strWidth2)/2, yMidPoint + height/2);
   }

	// Draws two sets of X axis labels for the two small plots
	public void draw2XTitles( Graphics g ){
		g.setFont( c.font12p );
		FontMetrics fm = g.getFontMetrics();
		int strWidth = fm.stringWidth( "* Average Load Factor" );
		g.drawString( "* Average Load Factor", originX1 + (xAxisLength-strWidth)/2, originY + 30 );
		g.drawString( "* Average Load Factor", originX2 + (xAxisLength-strWidth)/2, originY + 30 );
		strWidth = fm.stringWidth( "** numbers searched/total numbers %" );
		g.drawString( "** numbers searched / total numbers %", originX1 + (xAxisLength-strWidth)/2, originY + 45 );
		g.drawString( "** numbers searched / total numbers %", originX2 + (xAxisLength-strWidth)/2, originY + 45 );
	}

	// Draws one set of X axis labels for the large plots
	public void draw1XTitles( Graphics g ){
		g.setFont( c.font12p );
		FontMetrics fm = g.getFontMetrics();
		int strWidth = fm.stringWidth( "* Average Load Factor" );
		g.drawString( "* Average Load Factor", originX + (bigXaxisLength-strWidth)/2, originY + 30 );
		strWidth = fm.stringWidth( "** numbers searched/total numbers %" );
		g.drawString( "** numbers searched / total numbers %", originX + (bigXaxisLength-strWidth)/2, originY + 45 );
	}

	/********* methods to plot the data on the grids ***************************/

	// scales x axis values from logical to screen value
	public int convertX( double num ){
		if(view == BOTH)
			return (int)Math.round( 2*num );
		else
			return (int)Math.round( 4*num );
	}

	// scales average probes y axis values from logical to screen value
	public int convertProbeY( double num ){
		return - (int)Math.round( num*200/20 );
	}

	// scales average time y axis values from logical to screen value
	public int convertTimeY( double num ){
		return - (int)Math.round( num*200/2 );
	}

	// This method plots the data for a run on the two small grids.  The integer,
	// i, passed to the method determines which of the runs available in the
   // SummaryManager is used.
	public void plotBoth( int i, Graphics g ){
		int x1, x2, y1, y2;
		Summary sum = summaryManager.getSummary(i);
		// the number of entries in the array
		int count = (int)sum.results.plotData[0][0];
		// draw the first line from the origin
		if( count >= 1 ){
			x2 = convertX( sum.results.plotData[0][1] );
			y2 = convertProbeY( sum.results.plotData[1][1] );
			g.drawLine( originX2, originY, x2+originX2, y2+originY );

			y2 = convertTimeY( sum.results.plotData[2][1] );
			g.drawLine( originX1, originY, x2+originX1, y2+originY );
		}
		// draw the remaining line segments
		for( int j = 1; j < count; j++ ){
			x1 = convertX( sum.results.plotData[0][j] );
 		   if( sum.results.plotData[0][j+1] > 100 )
				break;
			x2 = convertX( sum.results.plotData[0][j+1] );
			y1 = convertProbeY( sum.results.plotData[1][j] ); 
			y2 = convertProbeY( sum.results.plotData[1][j+1] );
			g.drawLine( x1+originX2, y1+originY, x2+originX2, y2+originY );

			y1 = convertTimeY( sum.results.plotData[2][j] ); 
			y2 = convertTimeY( sum.results.plotData[2][j+1] );
			g.drawLine( x1+originX1, y1+originY, x2+originX1, y2+originY );
		}
	}

	// This method plots the data for a run on the large Time grid.  The integer,
	// i, passed to the method determines which of the runs available in the
   // SummaryManager is used.
	public void plotTime( int i, Graphics g ){
		int x1, x2, y1, y2;
		Summary sum = summaryManager.getSummary(i);
		// the number of entries in the array
		int count = (int)sum.results.plotData[0][0];
		// draw the first line from the origin
		if( count >= 1 ){
			x2 = convertX( sum.results.plotData[0][1] );
			y2 = convertTimeY( sum.results.plotData[2][1] );
			g.drawLine( originX, originY, x2+originX, y2+originY );
		}
		// draw the remaining line segments
		for( int j = 1; j < count; j++ ){
			x1 = convertX( sum.results.plotData[0][j] );
 		   if( sum.results.plotData[0][j+1] > 100 )
				break;
			x2 = convertX( sum.results.plotData[0][j+1] );
			y1 = convertTimeY( sum.results.plotData[2][j] ); 
			y2 = convertTimeY( sum.results.plotData[2][j+1] );
			g.drawLine( x1+originX, y1+originY, x2+originX, y2+originY );
		}
	}

	// This method plots the data for a run on the large Probes grid.  The integer,
	// i, passed to the method determines which of the runs available in the
   // SummaryManager is used.
	public void plotProbes( int i, Graphics g ){
		int x1, x2, y1, y2;
		Summary sum = summaryManager.getSummary(i);
		// the number of entries in the array
		int count = (int)sum.results.plotData[0][0];
		// draw the first line from the origin
		if( count >= 1 ){
			x2 = convertX( sum.results.plotData[0][1] );
			y2 = convertProbeY( sum.results.plotData[1][1] );
			g.drawLine( originX, originY, x2+originX, y2+originY );
		}
		// draw the remaining line segments
		for( int j = 1; j < count; j++ ){
			x1 = convertX( sum.results.plotData[0][j] );
 		   if( sum.results.plotData[0][j+1] > 100 )
				break;
			x2 = convertX( sum.results.plotData[0][j+1] );
			y1 = convertProbeY( sum.results.plotData[1][j] ); 
			y2 = convertProbeY( sum.results.plotData[1][j+1] );
			g.drawLine( x1+originX, y1+originY, x2+originX, y2+originY );

		}
	}

	// This method draws the plot(s) corresponding to the selected view.
	// Data is only plotted for runs marked as selected within the SummaryManager.
	// Each run is plotted in a different color
	public void paint(Graphics g){
		calcOrigins( g );
		Dimension d = this.getSize();
		int maxX = d.width - 1;
		int maxY = d.height - 1;
	 	offscreen.setColor( Color.black );
 	 	offscreen.fillRect(0, 0, maxX+1, maxY+1 );
	
		offscreen.setColor( Color.yellow );
		offscreen.drawString( "Click a run to plot it", 5, maxY - 5);
		if( view == BOTH ){
			draw2Grids(offscreen);
			for( int i = 0; i < summaryManager.getCount(); i++ ){
				if( summaryManager.isSelected( i ) ){
					offscreen.setColor( lineColor[i] );
					plotBoth( i, offscreen );
				}
			}
		}
		else if( view == TIME ){
			draw1Grid( offscreen );
			drawYaxisTimeLabel( offscreen, originX, originY-20, 2, 20 );
			drawGridTimeTitle( offscreen, originX );
			for( int i = 0; i < summaryManager.getCount(); i++ ){
				if( summaryManager.isSelected( i ) ){
					offscreen.setColor( lineColor[i] );
					plotTime( i, offscreen );
				}
			}
		}
		else if( view == PROBES ){
			draw1Grid( offscreen );
			drawYaxisProbeLabel( offscreen, originX, originY-20, 2, 20 );
			drawGridProbeTitle( offscreen, originX );
			for( int i = 0; i < summaryManager.getCount(); i++ ){
				if( summaryManager.isSelected( i ) ){
					offscreen.setColor( lineColor[i] );
					plotProbes( i, offscreen );
				}
			}
		}
		g.drawImage( image, 0, 0, this );
	}

	// override update for smoother animation
	public void update(Graphics g ){
		paint(g);
	}
} // end Plot Canvas


/******************************************************************************
	The NumberGenerator class generates random numbers for use as input to the
	hash table.  Numbers returned are between 0 and the configured upper limit
	and are of the specified type: odd, even or mixed.
******************************************************************************/
class NumberGenerator{
	private int limit, type;
	private Common c;
	private Random r;

	// construction
	public NumberGenerator(){
		c = new Common();
		r = new Random();
	}
	
	// This method specifies the upper limit of the data range and whether the
	// numbers should be odd even or mixed
	public void configure( int limit, int type ){
		this.limit = limit;
		this.type = type;
   }

	// This method returns a number of the specified type and less than or equal
	// to the upper limit.
	public int getNumber(){
		int value;
		if( type == c.MIXED )
			value = Math.abs( r.nextInt() ) % (limit + 1);
 	   else if( type == c.ODD ){
			value = Math.abs( r.nextInt() ) % limit;		// values up to limit - 1
			if( value % 2 == 0 )		// if value is even add 1 to it
				value++;
		}
		else{	// EVEN
			value = Math.abs( r.nextInt() ) % limit; // values up to limit - 1
			if( value % 2 != 0 )		// if value is odd add 1 to it
				value++;
		}
		return value;	
	}
}

/******************************************************************************
	The HashAlgorithm class is the abstract parent class of the five algorithms
	demonstated by the applet.  It defines the attributes and methods common to
	each of classes that inherit from it.
******************************************************************************/
abstract class HashAlgorithm
{
	// references to other system objects
	protected HashApplet applet;
	protected ButtonPanel buttonPnl;
	protected DisplayManager displayManager;
	protected SummaryManager summaryManager;
	protected CodeManager codeManager;
	protected Setup settings;
	protected Results results;
	protected HashTable hashTable;
	protected NumberGenerator numGen;
	protected Common c;
	// the settings for the demo
	protected int buckets, slots, upperLimit, dataSize, dataType,
					execMode, success, overflowSize,
					// holds the numbers generated to store or retrieve
					input[], retrieve[];
	
	protected boolean runFlag = false,  // true if the algorithm is currently storing 
													// or retrieving
 							codeFlag = false, // true if pseudocode is showing
						   quickLoad;		   // true if the user wants to load the table
													// without animation prior to a retrieve demo
	protected volatile boolean abortFlag = false; // if true, kill the demo

	// This method is called by the run method of the applet's thread and executes the
	// demo
	abstract void run();

	// This method is called prior to each demo
	public void initialize(){
		loadSettings();
		generateInput();
	}

	// This method reads the demo settings from the Setup object associated with
	// the algorithm
	public void loadSettings(){
		buckets = settings.getBuckets();
		slots = settings.getSlots();
		upperLimit = settings.getUpperLimit();
		dataSize = settings.getDataSize();
		dataType = settings.getDataType();
		execMode = settings.getExecMode();
		success = settings.getSuccess();
		overflowSize = settings.getOverflow();
		quickLoad = settings.getQuickLoad();
  	}

	// This method generates random input of the specified type and range
	// and stores it in the input array
	public void generateInput(){
		numGen.configure( upperLimit, dataType );
		for( int i = 0; i < dataSize; i++ )
			input[i] = numGen.getNumber();
	}

	// This method generates the data the algorithm will search the hash table 
	// for.  It receives an array containing all the data currently stored in
	// the hash table.  It fills the retrieve array with values that are and aren't
	// found in the hash table.  The mixture of these values depends on the "sucess"
	// setting read from the Setup object
	public void genRetrieveData( int[] tableData, int tableCnt ){
		int numGood = (int)Math.round( (double)success*dataSize/100);
		int numBad = dataSize - numGood;
		int goodData[] = new int[numGood];
		int badData[] = new int[numBad];
		boolean usedData[] = new boolean[ upperLimit + 1 ];
		int index, value;
		// randomly select data that is in the hash table and place it in the goodData
		// array
		for( int i = 0; i < numGood; i++ ){
			index = (int)(Math.random()*tableCnt);
	 		goodData[i] = tableData[index];
	  	}

		// set a flag for each value that is present in the table
		for( int i = 0; i < tableCnt; i++ ){
			usedData[tableData[i]] = true;
		}

		// fill the bad data array with values that are not found in the hash table
		int count = 0;
		while( count < numBad ){
			value = (int)(Math.random()*1000);
			if( value > upperLimit ){
				badData[count] = value;
				count++;
			}
			else if( !usedData[value] ){
 		   	badData[count] = value;
				count++;
		   }
		}

		// Shuffle the good and bad data into the retrieve array
		int goodCount = 0, badCount = 0, totalCount = 0, chance;
		while( totalCount < dataSize ){
			chance = (int)(Math.random()*101);
			if( chance <= success && goodCount < numGood ){
				retrieve[totalCount] = goodData[goodCount];
				goodCount++;
				totalCount++;
			}
			else if( chance > success && badCount < numBad){
				retrieve[totalCount] = badData[badCount];
				badCount++;
				totalCount++;
			}
 	   }
	}

	// return a flag indicating if a store or retrieve demo is in progress
	public boolean isRunning(){
		return runFlag;
	}

	// set a flag indicating if the pseudocode is showing
	public void setCodeShowing( boolean b ){
		codeFlag = b;
	}

	// This method sets the abortFlag to true which causes a demo to exit
 	// without finishing
	public void abort(){
		abortFlag = true;
	}

	// If the applet is in RUN mode, this method delays the execution thread
 	// to allow the animation to be seen. IF the applet is in STEP mode the
	// thread is paused.
	public void stepOrPause(){
		if( !abortFlag )
			applet.stepOrPause();
		applet.checkSuspended();
	}

	// This method is the same as stepOrPause() above except the call to the applet's
	// stepOrPause() method only occurs if the pseudocode is displayed
	public void codePause(){
		if( codeFlag && !abortFlag )
			applet.stepOrPause();
		applet.checkSuspended();
	}
}


/******************************************************************************
	The LinearProbAlgorithm class defines the methods which demonstrate the
	linear probing collision resolution algorithm.
******************************************************************************/
class LinearProbAlgorithm extends HashAlgorithm{
	// construction
	public LinearProbAlgorithm( HashApplet parent, DisplayManager dm,	SummaryManager sm,
 									 CodeManager cm, ButtonPanel bp, Setup setup, HashTable ht ){
		applet = parent;
		displayManager = dm;
		summaryManager = sm;
		codeManager = cm;
		buttonPnl = bp;
		settings = setup;
		hashTable = ht;
		input = new int[300];
		retrieve = new int[300];
		numGen = new NumberGenerator();
		c = new Common();
	}
 
	// This method is called by the run method of the applet's thread and executes the
	// demo
	public void run(){
		codeFlag = applet.getCodeShowing();
		runFlag = true;
		// disable controls while demo is executing
		buttonPnl.enablePageButtons( false );
		buttonPnl.enableMenu( false );

		if( execMode == c.STORE )
			store();
		else
			retrieve();
		runFlag = false;
		displayManager.setStatus( HashStatus.DONE );
		codeManager.clearSelections();
		if( abortFlag ){
			hashTable.clear();
			displayManager.setStatus( HashStatus.READY );
			displayManager.initialize( c.LINEARPROBING, execMode, buckets, dataSize );
			abortFlag = false;
		}
		displayManager.paintHash();
		// enable controls again
		buttonPnl.enablePageButtons( c.EXECUTE );
		buttonPnl.enableMenu( true );
		buttonPnl.setRunButtonsInitial();	
	}

	// This method stores data from the input array in the hash table.  When
	// collisions occur, the linear probing collision resolution algorithm is used.
	// As the demo runs, the table graphics, statistics and pseudocode display
	// are updated to show the current state of the table
	public void store(){
		// create a Results object to store the statistics
		Results results = new Results();
 		displayManager.setResults( results );
		// setup the pie chart
 		displayManager.setNumLeft( dataSize );
 		displayManager.updateStats();
		// show the hash function ( mod buckets )
		displayManager.setHx( buckets );
		// setup the HashStatus
		displayManager.setPhase( c.STORE );
		// initialize variables
		long start = new Date().getTime();
		int address;
		double size = slots*buckets;
		int probes = 0;
		boolean found;		// true when an empty slot is found
		
		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			displayManager.setStatus( HashStatus.SEARCH );
			displayManager.paintHash();

			codeManager.selectLine(0); // loc = X % numBuckets
			// show the full hash equation ( address = input[i] mod buckets)
			displayManager.setInput( input[i] );
			address = input[i] % buckets;
			displayManager.setHomeAddress( address );
			displayManager.updateStats();
			codePause();	// only pause if pseudocode is showing

			codeManager.selectLine(1); // if(Table[loc] == EMPTY
			found = hashTable.hasEmptySlot(address);
			if( found ){
				applet.playSuccessSound();
				results.incSuccess();
				displayManager.setStatus( HashStatus.FOUND );
			}
			displayManager.paintHash();
			// update the average number of probes required per value stored
			results.setProbes( (double)++probes/(i+1));
			results.setTime( new Date().getTime() - start );
			displayManager.updateStats();
			statPause( found ); // pause if code is showing or found is false
			if( !found )
				hashTable.clearProbes();

			if( found ){
				codeManager.selectLine(2); // Table[loc] = X
				// insert data at bucket address
				hashTable.insert( address, input[i] );
				displayManager.paintHash();
				// update the current table load
				results.setLoad( (i+1)*100/size );
				results.setTime( new Date().getTime() - start );
				// record current stats for future plotting
				results.setPlotData( i+1, size );
				// update the pie chart
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
 				displayManager.updateStats();
				if( i < dataSize -1 || codeFlag )
					stepOrPause();
			}
			else{ 		// home address was full, look in subsequent buckets until
				do{		// an empty slot is found (this will always happen)
					hashTable.clearProbes();
					displayManager.paintHash();

					codeManager.selectLine(5); // loc++
					address = nextAddress( address );
					codePause();	// only pause if pseudocode is showing

					codeManager.selectLine(6); // while(Table[loc]==FULL
					found = hashTable.hasEmptySlot(address);
					if( found ){
						applet.playSuccessSound();
						results.incSuccess();
						displayManager.setStatus( HashStatus.FOUND );
					}
					displayManager.paintHash();
					// update the average number of probes required per value stored
					results.setProbes( (double)++probes/(i+1));
					results.setTime( new Date().getTime() - start );
					displayManager.updateStats();
					statPause( found ); // pause if code is showing or found is false
				}while( !found && !abortFlag );

				codeManager.selectLine(7); // Table[loc] = X
				// insert data at bucket address
				hashTable.insert( address, input[i] );
				displayManager.paintHash();
				// update the current table load
				results.setLoad( (i+1)*100/size );
				results.setTime( new Date().getTime() - start );
				// record current stats for future plotting
				results.setPlotData( i+1, size );
				// update the pie chart
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
 				displayManager.updateStats();
				if( i < dataSize -1 || codeFlag )
					stepOrPause();	
			}
			hashTable.clearProbes();	
		} // end for loop

		// if we aren't doing a retrieve next create new summary object and add it
 	   // to the SummaryManager. The object is cloned so that subsequent runs do not
	   // overwrite the data
		if( !abortFlag && execMode == c.STORE ){
			Summary sum = new Summary();
			sum.setData( settings, results );
			summaryManager.add( (Summary)sum.clone() );
		}
	}

	// This method inserts data into the hash table without table animation,
	// statistics or source code display updates.  The pie chart progress meter
	// is updated to provide user feedback
	public void quickLoad(){
		// display status to user
		displayManager.setPhase( c.STORE );
		displayManager.setStatus( HashStatus.QUICKLOAD );
		displayManager.paintHash();
		// let paint finish before inserting data
		applet.pause( 50 );  

		// create Results object for use by DisplayManager
		Results results = new Results();
 		displayManager.setResults( results );
 		displayManager.setNumLeft( dataSize );
 		displayManager.updateStats();
		boolean found;
		int address;
		double size = slots*buckets;
		displayManager.setHx( buckets );
		
		// Main loop which inserts data
		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			address = input[i] % buckets;
			found = hashTable.hasEmptySlot(address);

			// If bucket is full,
			// look for empty slot using linear probing collision resolution
		   // algorithm.  Restrictions on the input guarantee an empty slot
		   // will always be found.
			while( !found && !abortFlag ){
				address = nextAddress( address );
				found = hashTable.hasEmptySlot(address);
			}
			
			// empty slot found, insert data
			hashTable.insert( address, input[i] );

			// update results object for calling function and
			// display progress pie chart for the user
			results.setLoad( (i+1)*100/size );
			displayManager.setNumLeft( dataSize - ( i + 1 ) );
			displayManager.updatePie();
		} // end for loop

		// clean up highlighted bucket frames
		hashTable.clearProbes();	
	}
			
	// This method searches the hash table for the data in the retrieve array.
	// As the demo runs, the table graphics, statistics and pseudocode display
	// are updated to show the current state of the table
	public void retrieve(){
		boolean found;
		
		codeManager.setCode( c.LINEARPROBING, c.STORE );
		// run quickLoad or store to populate the table
		if( quickLoad )
 			quickLoad();
		else
			store();

		// generate the data that we will search for
		genRetrieveData(input, dataSize);
		// display the linear probing retrieve pseudocode
		codeManager.setCode( c.LINEARPROBING, c.RETRIEVE );
		// update the HashStatus
		displayManager.setPhase( c.RETRIEVE );
 	   displayManager.setStatus( HashStatus.LOADED );
		displayManager.paintHash();
		displayManager.clearInputAndAddress();
		stepOrPause();
		// create a Results object to store the statistics
		Results results = new Results();
		// display the table load
		results.setLoad( dataSize*100/(slots*buckets) );
 		displayManager.setResults( results );
 		
		 // setup the pie chart
		displayManager.setNumLeft( dataSize );
		displayManager.updateStats();
		
		// initialize variables
		long start = new Date().getTime();
		int address;
		double size = slots*buckets;
		int probes = 0, searchCnt;

		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			displayManager.setStatus( HashStatus.SEARCH );
			// show the full hash equation ( address = retrieve[i] mod buckets )
			displayManager.setInput( retrieve[i] );
			address = retrieve[i] % buckets;
			displayManager.setHomeAddress( address );
  		   searchCnt = 1;  // reset the search counter

			codeManager.selectLine( 0 );		// if( Table[loc] == X )
			found = hashTable.search( address, retrieve[i] );
			// update the average number of probes required per value searched
			results.setProbes( (double)++probes/(i+1));
			results.setTime( new Date().getTime() - start );
			displayManager.updateStats();
			displayManager.paintHash();
 			codePause();	// delay or pause if pseudocode is showing

			if( found ){
				codeManager.selectLine( 1 );		// return FOUND
				displayManager.setStatus( HashStatus.FOUND );
 		   	applet.playSuccessSound();
				results.incSuccess();
				// update the pie chart
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
				results.setTime( new Date().getTime() - start );
				// record current stats for future plotting
				results.setPlotData( i+1, dataSize );
				displayManager.paintHash();
 				displayManager.updateStats();
			}
			else{ // the home address did not contain the value, search subsequent buckets
					// until either the value is found, an empty slot is found, or the search
					// has wrapped around to the starting point
				while( !abortFlag ){
					codeManager.selectLine(3); // while(Table[loc] != X && Table[loc] != EMPTY)
					displayManager.paintHash();
					if( found || hashTable.hasEmptySlot(address) || searchCnt == buckets ){
 				   	codePause();
						break;
					}
					stepOrPause();

					codeManager.selectLine(5); // loc++;
					hashTable.clearProbes();
					displayManager.paintHash();
					address = nextAddress( address );
					codePause();		// delay or pause if pseudocode is showing
					found = hashTable.search( address, retrieve[i] );
 				   searchCnt++;
					// update the average number of probes required per value searched
					results.setProbes( (double)++probes/(i+1));
					results.setTime( new Date().getTime() - start );
					displayManager.updateStats();
					displayManager.paintHash();
				} 
				codeManager.selectLine(6); // if( Table[loc] == X )
				codePause();
				if( found  ){
					codeManager.selectLine(7); // return FOUND
					displayManager.setStatus( HashStatus.FOUND );
					applet.playSuccessSound();
					results.incSuccess();
				}
				else{
					codeManager.selectLine(8); // return NOT_FOUND
					displayManager.setStatus( HashStatus.NOT_FOUND );
					applet.playUnsuccessSound();
					results.incUnsuccess();
				}
				results.setTime( new Date().getTime() - start );
				// record current stats for future plotting
				results.setPlotData( i+1, dataSize );
				// update the pie chart
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
				displayManager.paintHash();
 				displayManager.updateStats();
			} // end else
			if( i < dataSize -1 )
				stepOrPause();
			hashTable.clearProbes();	
		} // end for loop
		displayManager.paintHash();

		// create new Summary object and add it to the SummaryManager
		if( !abortFlag ){
			Summary sum = new Summary();
			sum.setData( settings, results );
			summaryManager.add( (Summary)sum.clone() );
		}
		
 	}

	// This method returns the address following the address passed to the method.
	// It uses wrap around if necessary
	public int nextAddress( int addr ){
		if( ++addr >= buckets )
			return 0;
		else
			return addr;
	}

	// This method calls the applet's stepOrPause method only if the pseudocode
	// is displayed or the boolean argument is false
	public void statPause( boolean b){
		if( (codeFlag || !b )&& !abortFlag )
			applet.stepOrPause();
		applet.checkSuspended();
   }
}


/******************************************************************************
	The QuadraticProbAlgorithm class defines the methods which demonstrate the
	linear probing collision resolution algorithm.
******************************************************************************/
class QuadraticProbAlgorithm extends HashAlgorithm{
	private int[] inTable;	// holds the values that are currently stored in the table
	private int inTableCnt; // the number of values currently stored in the table

	// construction
	public QuadraticProbAlgorithm(HashApplet parent, DisplayManager dm, SummaryManager sm,
 									CodeManager cm, ButtonPanel bp, Setup setup, HashTable ht ){
		applet = parent;
		displayManager = dm;
		summaryManager = sm;
		codeManager = cm;
		buttonPnl = bp;
		settings = setup;
		hashTable = ht;
		input = new int[300];
		retrieve = new int[300];
		numGen = new NumberGenerator();
		c = new Common();
		inTable = new int[300];
	}
 
	// This method is called by the run method of the applet's thread and executes the
	// demo
	public void run(){
		codeFlag = applet.getCodeShowing();
		runFlag = true;
		// disable controls while demo is executing
		buttonPnl.enablePageButtons( false );
		buttonPnl.enableMenu( false );

		if( execMode == c.STORE )
			store();
		else
			retrieve();
		runFlag = false;
		displayManager.setStatus( HashStatus.DONE );
		codeManager.clearSelections();
		if( abortFlag ){
			hashTable.clear();
			displayManager.setStatus( HashStatus.READY );
			displayManager.initialize( c.QUADRATICPROBING, execMode, buckets, dataSize );
			abortFlag = false;
		}
		displayManager.paintHash();
		// enable controls again
		buttonPnl.enablePageButtons( c.EXECUTE );
		buttonPnl.enableMenu( true );
		buttonPnl.setRunButtonsInitial();
		
	}

	// This method stores data from the input array in the hash table.  When
	// collisions occur, the quadratic probing collision resolution algorithm is used.
	// As the demo runs, the table graphics, statistics and pseudocode display
	// are updated to show the current state of the table
	public void store(){
		// create a Results object to store the statistics
		Results results = new Results();;
 		displayManager.setResults( results );
		// setup the pie chart
 		displayManager.setNumLeft( dataSize );
 		displayManager.updateStats();
		// show the hash function( mod buckets )
		displayManager.setHx( buckets );
		// setup the HashStatus
		displayManager.setPhase( c.STORE );
		// initialize variables
		long start = new Date().getTime();
		int home, address;
		double size = slots*buckets;
		int probes = 0;
		int searchCnt = 0;
		inTableCnt = 0;
		boolean found;		// true when an empty slot is found
		
		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			displayManager.setStatus( HashStatus.SEARCH );
			displayManager.paintHash();

			codeManager.selectLine(0); // loc = X % numBuckets; i = 0;
			// show the full hash equation ( home = input[i] mod buckets )
			displayManager.setInput( input[i] );
			home = input[i] % buckets;
			displayManager.setHomeAddress( home );
			displayManager.updateStats();
			codePause();		// pause or delay if pseudocode is showing

			codeManager.selectLine(1); // if(Table[loc] == EMPTY
			found = hashTable.hasEmptySlot(home);
			searchCnt = 1;
			if( found ){
				applet.playSuccessSound();
				results.incSuccess();
				displayManager.setStatus( HashStatus.FOUND );
			}
			displayManager.paintHash();
			// update the average number of probes required per value stored
			results.setProbes( (double)++probes/(i+1));
			results.setTime( new Date().getTime() - start );
			displayManager.updateStats();
			statPause( found );	// pause if code is showing or found is false
			if( !found )
				hashTable.clearProbes();

			if( found ){
				codeManager.selectLine(2); // Table[loc] = X
				// insert data at bucket home address
				hashTable.insert( home, input[i] );
				// save data in case we will be doing a Retrieve next
				inTable[inTableCnt] = input[i];
				inTableCnt++;
				displayManager.paintHash();
				// update the current table load
				results.setLoad( inTableCnt*100/size );
				results.setTime( new Date().getTime() - start );
				// record current stats for future plotting
				results.setPlotData( inTableCnt, size );
				// update the pie chart
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
 				displayManager.updateStats();
				if( i < dataSize -1 || codeFlag )
					stepOrPause();
			}
			else{			// home address was full, look in subsequent buckets until
				do{		// an empty slot is found or the number of searches equals
							// the number of buckets 
					hashTable.clearProbes();
					displayManager.paintHash();

					codeManager.selectLine(5); // i = (i+1)*(i+1);
					address = nextAddress( home, searchCnt );
					codePause();	// only pause if pseudocode is showing

					codeManager.selectLine(6); // while(Table[loc]==FULL
					found = hashTable.hasEmptySlot(address);
					searchCnt++;
					if( found ){
						applet.playSuccessSound();
						results.incSuccess();
						displayManager.setStatus( HashStatus.FOUND );
					}
					displayManager.paintHash();
					// update the average number of probes required per value attempted
				 	// to store 
					results.setProbes( (double)++probes/(i+1));
					results.setTime( new Date().getTime() - start );
					displayManager.updateStats();
					statPause( found ); 	// pause if code is showing or found is false
				}while( !found && !abortFlag && searchCnt != buckets );

				if( found ){
					codeManager.selectLine(7); // Table[loc] = X
					// insert data at bucket address
					hashTable.insert( address, input[i] );
					// save data in case we will be doing a Retrieve next
					inTable[inTableCnt] = input[i];
					inTableCnt++;
					displayManager.paintHash();
					// update the current table load
					results.setLoad( inTableCnt*100/size );
					results.setTime( new Date().getTime() - start );
					// record current stats for future plotting
					results.setPlotData( inTableCnt, size );
				}
				else{
					displayManager.setStatus( HashStatus.NOT_FOUND );
					applet.playUnsuccessSound();
					results.incUnsuccess();
					displayManager.paintHash();
				}
				// update the pie chart		
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
 				displayManager.updateStats();
				if( i < dataSize -1 || codeFlag )
					stepOrPause();	
			}
			hashTable.clearProbes();	
		} // end for loop

		// if we aren't doing a retrieve next create new summary object and add it
 	   // to the SummaryManager. The object is cloned so that subsequent runs do not
	   // overwrite the data
		if( !abortFlag && execMode == c.STORE ){
			Summary sum = new Summary();
			sum.setData( settings, results );
			summaryManager.add( (Summary)sum.clone() );
		}
	}

	// This method inserts data into the hash table without table animation,
	// statistics or source code display updates.  The pie chart progress meter
	// is updated to provide user feedback
	public void quickLoad(){
		// display status to user
		displayManager.setPhase( c.STORE );
		displayManager.setStatus( HashStatus.QUICKLOAD );
		displayManager.paintHash();
		// let paint finish before inserting data
		applet.pause( 50 );  

		// create results object for use by DisplayManager
		Results results = new Results();;
 		displayManager.setResults( results );
 		displayManager.setNumLeft( dataSize );
 		displayManager.updateStats();
		// initialize variables
		boolean found;
		int home, address;
		double size = slots*buckets;
		int searchCnt = 0;
		inTableCnt = 0;
		displayManager.setHx( buckets );
		
		// main loop which inserts data
		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			home = input[i] % buckets;
			found = hashTable.hasEmptySlot(home);
			searchCnt = 1;
			displayManager.updatePie();

			if( found ){
				// empty slot found, insert data
				hashTable.insert( home, input[i] );
				// save data, it will be used to help generate the data to retrieve
				inTable[inTableCnt] = input[i];
				inTableCnt++;
			}
			else{			// home address was full, search other buckets until
				do{		// an empty slot is found or the number of searches equals
							// the number of buckets 
	 				address = nextAddress( home, searchCnt );
					found = hashTable.hasEmptySlot(address);
					searchCnt++;
				}while( !found && !abortFlag && searchCnt != buckets );
				if( found ){
					// empty slot found, insert data
					hashTable.insert( address, input[i] );
					// save data, it will be used to help generate the data to retrieve
					inTable[inTableCnt] = input[i];
					inTableCnt++;
				}
			}
			// update the results object for the calling function adn
			// display pie chart for the user
			results.setLoad( inTableCnt*100/size );
			displayManager.setNumLeft( dataSize - ( i + 1 ) );
			displayManager.updatePie();
			
		} // end for loop

		//clean up highlighted bucket frames
		hashTable.clearProbes();	
	}
	
	// This method searches the hash table for the data in the retrieve array.
	// As the demo runs the table graphics, statistics and pseudocode display
	// are updated to show the current state of the table
	public void retrieve(){
		boolean found;
		codeManager.setCode( c.QUADRATICPROBING, c.STORE );

		// run quickLoad() or store() to populate the table
		if( quickLoad )
 			quickLoad();
		else
			store();

		// generate the data that we will search for based on the data successfully
		// stored in the table
		genRetrieveData( inTable, inTableCnt );
		// display the quadratic probing retrieve pseudocode
		codeManager.setCode( c.QUADRATICPROBING, c.RETRIEVE );
		// update the HashStatus
		displayManager.setPhase( c.RETRIEVE );
 	   displayManager.setStatus( HashStatus.LOADED );
		displayManager.paintHash();
		displayManager.clearInputAndAddress();
		stepOrPause();
 
		// create a Results object to store the statistics
		Results results = new Results();
		// display the table load
		results.setLoad( inTableCnt*100/(slots*buckets) );
 		displayManager.setResults( results );
 		
		// setup the pie chart
		displayManager.setNumLeft( dataSize );
		displayManager.updateStats();
		
		// initialize variables
		long start = new Date().getTime();
		int home, address;
		double size = slots*buckets;
		int probes = 0, searchCnt;

		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			displayManager.setStatus( HashStatus.SEARCH );
			// show the full hash equation: home = retrieve[i] mod buckets
			displayManager.setInput( retrieve[i] );
			home = retrieve[i] % buckets;
			displayManager.setHomeAddress( home );
  		   searchCnt = 1;  // reset the search counter

			codeManager.selectLine( 0 );		// if( Table[loc] == X )
			found = hashTable.search( home, retrieve[i] );
			// update the average number of probes required per value searched
			results.setProbes( (double)++probes/(i+1));
			results.setTime( new Date().getTime() - start );
			displayManager.updateStats();
			displayManager.paintHash();
 			codePause(); 	// delay or pause if pseudocode is showing

			if( found ){
				codeManager.selectLine( 1 );		// return FOUND
				displayManager.setStatus( HashStatus.FOUND );
 		   	applet.playSuccessSound();
				results.incSuccess();
				// update the pie chart
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
				results.setTime( new Date().getTime() - start );
				// record current stats for future plotting
				results.setPlotData( i+1, dataSize );
				displayManager.paintHash();
 				displayManager.updateStats();
			}
			else{	// the home address did not contain the value, search other buckets using
 		   		// quadratic probing until either the value is found, an empty slot is
					// found, or the search counter equals the number of buckets
				address = home;
				while( !abortFlag ){
					codeManager.selectLine(3); // while(Table[loc] != X && Table[loc] != EMPTY)
					displayManager.paintHash();
					if( found || hashTable.hasEmptySlot(address) || searchCnt == buckets ){
						codePause();
						break;
					}
					stepOrPause();

					codeManager.selectLine(5); // i = (i+1)*(i+1);
					hashTable.clearProbes();
					displayManager.paintHash();
 					address = nextAddress( home, searchCnt );
					codePause();		// delay or pause if pseudocode is showing
					found = hashTable.search( address, retrieve[i] );
 				   searchCnt++;
					// update the average number of probes required per value searched
					results.setProbes( (double)++probes/(i+1));
					results.setTime( new Date().getTime() - start );
					displayManager.updateStats();
					displayManager.paintHash();
				} 
				codeManager.selectLine(6); // if( Table[loc] == X )
				codePause();
				if( found  ){
					codeManager.selectLine(7); // return FOUND
					displayManager.setStatus( HashStatus.FOUND );
					applet.playSuccessSound();
					results.incSuccess();
				}
				else{
					codeManager.selectLine(8); // return NOT_FOUND
					displayManager.setStatus( HashStatus.NOT_FOUND );
					applet.playUnsuccessSound();
					results.incUnsuccess();
				}
				results.setTime( new Date().getTime() - start );
				// record the current stats for future plotting
				results.setPlotData( i+1, dataSize );
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
				displayManager.paintHash();
 				displayManager.updateStats();
			} // end else
			if( i < dataSize -1 )
				stepOrPause();
			hashTable.clearProbes();	
		} // end for loop
		displayManager.paintHash();

		// create new Summary object and add it to the SummaryManager.
		// The object is cloned so that subsequent runs do not overwrite the data
		if( !abortFlag ){
			Summary sum = new Summary();
			sum.setData( settings, results );
			summaryManager.add( (Summary)sum.clone() );
		}	
 	}

	// This method return the next address that should be searched based on the
	// current address (addr) and the number of previous searches (i).
	// This is the quadratic probing collision resolution formula.
	public int nextAddress( int addr, int i ){
		addr = addr + i*i;
		while( addr >= buckets )
			addr -= buckets;
		return addr;
	}

	// This method calls the applet's stepOrPause method only if the pseudocode
	// is displayed of the boolean argument is false.
	public void statPause( boolean b){
		if( (codeFlag || !b )&& !abortFlag )
			applet.stepOrPause();
		applet.checkSuspended();
   }	
}


/******************************************************************************
	The BucketChainAlgorithm class defines the methods which demonstrate the
	bucket chaining collision resolution algorithm.
******************************************************************************/
class BucketChainAlgorithm extends HashAlgorithm{
	private int[] inTable;	// holds the values that are currently stored in the table
	private int inTableCnt;	// the number of values currently stored in the table

	// construction
	public BucketChainAlgorithm( HashApplet parent, DisplayManager dm, SummaryManager sm,
 									CodeManager cm, ButtonPanel bp, Setup setup, HashTable ht ){
		applet = parent;
		displayManager = dm;
		summaryManager = sm;
		codeManager = cm;
		buttonPnl = bp;
		settings = setup;
		hashTable = ht;
		input = new int[300];
		retrieve = new int[300];
		numGen = new NumberGenerator();
		c = new Common();
		inTable = new int[300];
	}
 
	// This method is called by the run method of the applet's thread and executes the
	// demo
	public void run(){
		codeFlag = applet.getCodeShowing();
		runFlag = true;
		// disable controls while demo is executing
		buttonPnl.enablePageButtons( false );
		buttonPnl.enableMenu( false );

		if( execMode == c.STORE )
			store();
		else
			retrieve();
		runFlag = false;
		displayManager.setPass(0);
		displayManager.setStatus( HashStatus.DONE );
		codeManager.clearSelections();
		if( abortFlag ){
			hashTable.clear();
			displayManager.setStatus( HashStatus.READY );
			displayManager.initialize( c.BUCKETCHAINING, execMode, buckets, dataSize );
			abortFlag = false;
		}
		
		displayManager.paintHash();
		// enable controls again
		buttonPnl.enablePageButtons( c.EXECUTE );
		buttonPnl.enableMenu( true );
		buttonPnl.setRunButtonsInitial();	
	}
	
	// This method stores data from the input array in the hash table.  When
	// collisions occur, the bucket chaining collision resolution algorithm is used.
	// As the demo runs, the table graphics, statistics and pseudocode display
	// are updated to show the current state of the table
	public void store(){
		// create a Results object to store the statistics
		Results results = new Results();;
 		displayManager.setResults( results );
		// setup the pie chart
 		displayManager.setNumLeft( dataSize );
 		displayManager.updateStats();
		// show the has function (mode buckets)
		displayManager.setHx( buckets );
		// setup the HashStatus
		displayManager.setPhase( c.STORE );
		// this array holds the data that isn't stored on the first pass 
		int[] chained = new int[300];
		// initialize variables
		long start = new Date().getTime();
		int address;
		double size = slots*buckets;
		int probes = 0;
		inTableCnt = 0;
		int chainCnt = 0;
		int searchCnt = 0;
		boolean found = false;

		// The storing algorithm takes two passes.  On the first pass, if there is no
 	   // collision the data is inserted in the table, otherwise the data is saved
		// in the chained array for the second pass
		
		// First pass 
		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			// set the HashStatus to indicate the pass
			displayManager.setPass( 1 );
			displayManager.setStatus( HashStatus.SEARCH );
			displayManager.paintHash();

			codeManager.selectLine(1); // loc = X % numBuckets
			// show the full hash equation ( address = input[i] mod buckets )
			displayManager.setInput( input[i] );
			address = input[i] % buckets;
			displayManager.setHomeAddress( address );
			displayManager.updateStats();
			codePause(); 	// only pause if pseudocode is showing

			codeManager.selectLine(2); // if(Table[loc] == EMPTY
			found = hashTable.hasEmptySlot(address);
			if( found ){
				applet.playSuccessSound();
				results.incSuccess();
				displayManager.setStatus( HashStatus.FOUND );
			}
			displayManager.paintHash();
			// update the average number of probes required per value attempted
			// to store 
			results.setProbes( (double)++probes/(i + 1));
			results.setTime( new Date().getTime() - start );
			displayManager.updateStats();
	 		statPause( found );	// pause if code is showing or found is false
			if( !found )
				hashTable.clearProbes();
				displayManager.paintHash();

			if( found ){
	 			codeManager.selectLine(3); // Table[loc] = X
				// insert data at home address
				hashTable.insert( address, input[i] );
				// save data, it will be used to help generate the data to retrieve
				inTable[inTableCnt] = input[i];
				inTableCnt++;
				displayManager.paintHash();
				// update the current table load
				results.setLoad( inTableCnt*100/size );
				results.setTime( new Date().getTime() - start );
				// record the current stats for future plotting
				results.setPlotData( inTableCnt, size );
				// update the pie chart
				displayManager.setNumLeft( dataSize - inTableCnt );
 				displayManager.updateStats();
				stepOrPause();
			}
			else{ 	// the home address is full, save the data for the chaining pass
				codeManager.selectLine(5); // Temp[count++] = X
				chained[chainCnt] = input[i];
				chainCnt++;
				codePause();	 	// only pause if pseudocode is showing
			}
			hashTable.clearProbes();
			displayManager.paintHash();
			
		} // end for loop

		// Only execute second pass if all data wasn't inserted on the first pass
		if( chainCnt > 0){
  
			// The second pass works as follows: For each value in the chained array
			// the algorithm checks the link value of the home address.  If the link is -1,
			// a wrap around linear probing search is done for a completely empty bucket.
 		   // If an empty bucket is found the value is inserted and the link value of the
			// home address is set to the address where the value was inserted.
			//
			// If the link at the home address is not -1, the algorithm searches for an 
			// empty slot in the bucket corresponding to the link value.  If there is an 
			// empty slot the value is inserted.  If the bucket is full and the link value
			// is -1, the algorithm performs a wrap around linear probing search, otherwise
			// it searches the next linked bucket.  When an empty slot is found, the link
			// value of the previous chained bucket is set to the address where the value
			// was inserted.
			int home, link, lastChain, nextChain;
			// display the bucket chaining pass 2 store pseudocode
			codeManager.setPass2();
			for( int i = 0; (i < chainCnt )&&!abortFlag; i++ ){
				// update the HashStatus
				displayManager.setPass( 2 );
				displayManager.setStatus( HashStatus.SEARCH );
				hashTable.clearProbes();
				displayManager.paintHash();

				// revisit the home address
	 			codeManager.selectLine(1); // loc = X % numBuckets
				// show the full hash equation ( home = chained[i] mod buckets )
				displayManager.setInput( chained[i] );
				home = chained[i] % buckets;
				displayManager.setHomeAddress( home );
				displayManager.updateStats();
				codePause();	// only pause if pseudocode is showing

 				codeManager.selectLine(4); // if(Table[loc] == EMPTY
				hashTable.isEmpty( home );
				// update the average number of probes required per value attempted to store
				results.setProbes( (double)++probes/(dataSize));
				displayManager.updateStats();
				displayManager.paintHash();
 				stepOrPause();
				// get the link value at the home address
				link = hashTable.getLinkValue(home);
	
				// the home bucket is not chained
				if( link == -1 ){		
					searchCnt = 1;
					address = home;
					// do a linear probing search until an empty bucket is found or the search
					// count equals the number of buckets
					while( !abortFlag ){
						hashTable.clearProbes();
						displayManager.paintHash();

						codeManager.selectLine(7); // loc = Table[loc].link
						address = nextAddress( address );
						codePause();		// only pause if pseudocode is showing

						codeManager.selectLine(4); // if(Table[loc] == EMPTY
						found = hashTable.isEmpty( address );
						// update the average number of probes required per value attempted
					   // to store
						results.setProbes( (double)++probes/(dataSize));
						displayManager.updateStats();
						displayManager.paintHash();
						searchCnt++;
	
						if( found ){
							applet.playSuccessSound();
							displayManager.setStatus( HashStatus.FOUND );
							displayManager.paintHash();
							results.incSuccess();
					   }
						if( searchCnt == buckets )
							break;
						// pause if pseudocode is showing or found is false
					 	conditionalPause( codeFlag || !found );

						if( found )
							break;
					} // end while

					// An empty bucket was found, insert the value and set the link at the
					// home address bucket
					if( found ){
						
						codeManager.selectLine(5); // Table[loc] = X;
						hashTable.insert( address, chained[i] );
						hashTable.setLinkValue( home, address );
						// save data, it will be used to help generate the data to retrieve
						inTable[inTableCnt] = chained[i];
						inTableCnt++;
						
						// update the table load
						results.setLoad( inTableCnt*100/size );
						results.setTime( new Date().getTime() - start );
						// record the current stats for future plotting
						results.setPlotData( inTableCnt, size );
						// update the pie chart
						displayManager.decNumLeft();
					}
					else{
						displayManager.setStatus( HashStatus.NOT_FOUND );
						// update pie chart
						displayManager.decNumLeft();
						applet.playUnsuccessSound();
						results.incUnsuccess();
					}
					results.setTime( new Date().getTime() - start );
					displayManager.updateStats();
					displayManager.paintHash();
					if( i < chainCnt -1 )
						stepOrPause();
				}
				// the home bucket is chained ( link != -1 )
				else{  
					codeManager.selectLine(7); // loc = Table[loc].link
					hashTable.clearProbes();
					displayManager.paintHash();
					codePause();		// only pause if pseudocode is showing
					searchCnt = 0;
					address = home;

					// search the chain until an empty slot is found, the chain
					// terminates with a full bucket that has a link equal to -1
					// or the number of searches equals the number of buckets
					while( !abortFlag ){
						hashTable.clearProbes();
						displayManager.paintHash();

						codeManager.selectLine(4); // if(Table[loc] == EMPTY
						found = hashTable.hasEmptySlot( link );
						// update the average number of probes required per value attempted
					   // to store
						results.setProbes( (double)++probes/(dataSize));
						displayManager.updateStats();
						displayManager.paintHash();
						searchCnt++;
									
						if( found ){
							applet.playSuccessSound();
							displayManager.setStatus( HashStatus.FOUND );
							displayManager.paintHash();
							results.incSuccess();
						}
						if( searchCnt == buckets )
							break;
						// pause if pseudocode is showing or found is false
					 	conditionalPause( codeFlag || !found );
						if( found )
							break;
						hashTable.clearProbes();
						displayManager.paintHash();

						codeManager.selectLine(7); // loc = Table[loc].link
						address = link;
						link = hashTable.getLinkValue(address);
						displayManager.paintHash();

						// the last bucket in the chain is full and the link equals -1
						if( link == -1 )
							break;
						codePause();		// only pause if pseudocode is showing
					} // end while

					// The search failed, update the stats and pie chart.
					// (This situation probably never happens, but better safe than sorry)
 					if( !found && searchCnt == buckets ){  
						displayManager.setStatus( HashStatus.NOT_FOUND );
						applet.playUnsuccessSound();
						results.incUnsuccess();
						results.setTime( new Date().getTime() - start );
						// update pie chart
						displayManager.decNumLeft();
						displayManager.updateStats();
						displayManager.paintHash();
						if( i < chainCnt -1 )
							stepOrPause();
					}
					// an empty slot was found, insert the data
					else if( found ){
						codeManager.selectLine(5); // Table[loc] = X;
						// insert data at bucket address
						hashTable.insert( link, chained[i] );
						// save data, it will be used to help generate the data to retrieve
						inTable[inTableCnt] = chained[i];
						inTableCnt++;
						displayManager.paintHash();
						// update the current table load
						results.setLoad( inTableCnt*100/size );
						results.setTime( new Date().getTime() - start );
						// record the current stats for future plotting
						results.setPlotData( inTableCnt, size );
						// update the pie chart
						displayManager.decNumLeft();
						displayManager.updateStats();
						if( i < chainCnt -1 )
							stepOrPause();
					}
					// the chain terminates with a full bucket that has a link equal to -1
					// do a linear probing search until an empty bucket is found or the search
					// count equals the number of buckets
					else if( link == -1 ) {
						searchCnt = 1;
						int startAddr = address;
						while( !abortFlag ){
							hashTable.clearProbes();
							displayManager.paintHash();

							codeManager.selectLine(7); // loc = Table[loc].link
							address = nextAddress( address );
							codePause();		// only pause if pseudocode is showing

							codeManager.selectLine(4); // if(Table[loc] == EMPTY
							found = hashTable.isEmpty( address );
							// update the average number of probes required per value attempted
							// to store
							results.setProbes( (double)++probes/(dataSize));
							displayManager.updateStats();
							displayManager.paintHash();
							searchCnt++;
			
							if( found ){
								applet.playSuccessSound();
								displayManager.setStatus( HashStatus.FOUND );
								displayManager.paintHash();
								results.incSuccess();
							}
	
							if( searchCnt == buckets )
								break;
							// pause if pseudocode is showing or found is false
					 		conditionalPause( codeFlag || !found );
	
							if( found )
								break;
						}
						if( found ){
							codeManager.selectLine(5); // Table[loc] = X;
							// insert data at bucket address
							hashTable.insert( address, chained[i] );
							hashTable.setLinkValue( startAddr, address );
  						   // save data, it will be used to help generate the data to retrieve
							inTable[inTableCnt] = chained[i];
							inTableCnt++;
							// update the current table load
							results.setLoad( inTableCnt*100/size );
							results.setTime( new Date().getTime() - start );
							// record current stats for future plotting
							results.setPlotData( inTableCnt, size );
							// update pie chart
							displayManager.decNumLeft();
						}
						else{
							displayManager.setStatus( HashStatus.NOT_FOUND );
							// update pie chart
							displayManager.decNumLeft();
							applet.playUnsuccessSound();
							results.incUnsuccess();
						}
						results.setTime( new Date().getTime() - start );
						displayManager.updateStats();
						displayManager.paintHash();
						if( i < chainCnt -1 )
							stepOrPause();
						
					}
				}//end else link != -1
				hashTable.clearProbes();
				displayManager.paintHash();

			}//end for pass 2

		} // end if( chainCnt > 0)
 
		// if we aren't doing a retrieve next create new summary object and add it
 	   // to the SummaryManager. The object is cloned so that subsequent runs do not
	   // overwrite the data
		if( !abortFlag && execMode == c.STORE ){
			Summary sum = new Summary();
			sum.setData( settings, results );
			summaryManager.add( (Summary)sum.clone() );
		}
		 
	}
  
	// This method inserts data into the hash table without table animation,
	// statistics or source code display updates.  The pie chart progress meter
	// is updated to provide user feedback
	public void quickLoad(){
		// display status to user
		displayManager.setPhase( c.STORE );
		displayManager.setStatus( HashStatus.QUICKLOAD );
		displayManager.paintHash();
		// let paint finish before inserting data
		applet.pause( 50 );

		// create Results object for use by DisplayManager 
		Results results = new Results();;
 		displayManager.setResults( results );
		// setup the pie chart
 		displayManager.setNumLeft( dataSize );
 		displayManager.updateStats();
		// show the hash function ( mod buckets )
		displayManager.setHx( buckets );
		// this array holds the data that isn't stored on the first pass
		int[] chained = new int[300];
		// initialize variables
		int address;
		double size = slots*buckets;
		int probes = 0;
		inTableCnt = 0;
		int chainCnt = 0;
		int searchCnt = 0;
		boolean found = false;
		
		// The storing algorithm takes two passes.  On the first pass, if there is no
 	   // collision the data is inserted in the table, otherwise the data is saved
		// in the chained array for the second pass
		
		// First pass 
		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			address = input[i] % buckets;
			displayManager.updatePie();
			found = hashTable.hasEmptySlot(address);
			
			if( found ){
				// insert data at home address
				hashTable.insert( address, input[i] );
				// save data, it will be used to help generate the data to retrieve
				inTable[inTableCnt] = input[i];
				inTableCnt++;
				// update the current table load
				results.setLoad( inTableCnt*100/size );
				// update the pie chart
				displayManager.setNumLeft( dataSize - inTableCnt );
 				displayManager.updatePie();
			}
			else{	// the home address is full, save the data for the chaining pass
				chained[chainCnt] = input[i];
				chainCnt++;
			}
			
		} // end for loop

		// Only execute second pass if all data wasn't inserted on the first pass
		if( chainCnt > 0){
  
			// The second pass works as follows: For each value in the chained array
			// the algorithm checks the link value of the home address.  If the link is -1,
			// a wrap around linear probing search is done for a completely empty bucket.
 		   // If an empty bucket is found the value is inserted and the link value of the
			// home address is set to the address where the value was inserted.
			//
			// If the link at the home address is not -1, the algorithm searches for an 
			// empty slot in the bucket corresponding to the link value.  If there is an 
			// empty slot the value is inserted.  If the bucket is full and the link value
			// is -1, the algorithm performs a wrap around linear probing search, otherwise
			// it searches the next linked bucket.  When an empty slot is found, the link
			// value of the previous chained bucket is set to the address where the value
			// was inserted.
			int home, link;
			for( int i = 0; (i < chainCnt )&&!abortFlag; i++ ){
				home = chained[i] % buckets;
				displayManager.updatePie();
				hashTable.isEmpty( home );
				// get the link value at the home address
				link = hashTable.getLinkValue(home);
	
				// the home bucket is not chained
				if( link == -1 ){
					searchCnt = 1;
					address = home;

					// do a linear probing search until an empty bucket is found or the search
					// count equals the number of buckets
					while( !abortFlag ){
						address = nextAddress( address );
						found = hashTable.isEmpty( address );
						searchCnt++;
	
						if( searchCnt == buckets )
							break;
					
						if( found )
							break;
					} // end while

					if( found ){
						// insert data at bucket address
						hashTable.insert( address, chained[i] );
						hashTable.setLinkValue( home, address );
						// save data, it will be used to help generate the data to retrieve
						inTable[inTableCnt] = chained[i];
						inTableCnt++;
						// update the current table load
						results.setLoad( inTableCnt*100/size );
					}
					// update the pie chart
					displayManager.decNumLeft();
					displayManager.updatePie();
				}
				// the home bucket is chained ( link != -1 )
				else{   
					searchCnt = 0;
					address = home;

					// search the chain until an empty slot is found, the chain
					// terminates with a full bucket that has a link equal to -1
					// or the number of searches equals the number of buckets
					while( !abortFlag ){
						found = hashTable.hasEmptySlot( link );
						displayManager.updatePie();
						searchCnt++;
									
						if( searchCnt == buckets )
							break;
						if( found )
							break;
						
						address = link;
						link = hashTable.getLinkValue(address);
			
						// the last bucket in the chain is full and the link equals -1
						if( link == -1 )
							break;
					}

					// The search failed, update the pie chart
					// (This situation probably never happens, but better safe than sorry)
 					if( !found && searchCnt == buckets ){   
						displayManager.decNumLeft();
						displayManager.updatePie();
					}
					// an empty slot was found, insert the data
					else if( found ){
						// insert data at bucket address
						hashTable.insert( link, chained[i] );
						// save data, it will be used to help generate the data to retrieve
						inTable[inTableCnt] = chained[i];
						inTableCnt++;
						// update the current table load
						results.setLoad( inTableCnt*100/size );
						// update the pie chart
						displayManager.decNumLeft();
						displayManager.updatePie();
					}
					// the chain terminates with a full bucket that has a link equal to -1
					// do a linear probing search until an empty bucket is found or the search
					// count equals the number of buckets
					else if( link == -1 ) {
						searchCnt = 1;
						int startAddr = address;
						while( !abortFlag ){
							address = nextAddress( address );
							found = hashTable.isEmpty( address );
							searchCnt++;
			
							if( searchCnt == buckets )
								break;
	
							if( found )
								break;
						}
						if( found ){
							// insert data at bucket address
							hashTable.insert( address, chained[i] );
							hashTable.setLinkValue( startAddr, address ); 
 						   // save data, it will be used to help generate the data to retrieve
							inTable[inTableCnt] = chained[i];
							inTableCnt++;
							// update the current table load
							results.setLoad( inTableCnt*100/size );
						}
						// update the pie chart
						displayManager.decNumLeft();
						displayManager.updatePie();
												
					}
				}//end else link != -1
				hashTable.clearProbes();
			//	displayManager.paintHash();

			}//end for pass 2

		} // end if( chainCnt > 0 ) 	
	}
	
	// This method searches the hash table for the data in the retrieve array.
	// As the demo runs, the table graphics, statistics and pseudocode display
	// are updated to show the current state of the table
	public void retrieve(){
		boolean found;
		
		codeManager.setCode( c.BUCKETCHAINING, c.STORE );

		// run quickLoad() or store() to populate the table
		if( quickLoad )
 			quickLoad();
		else
			store();

		// generate the data that we will search for
		genRetrieveData( inTable, inTableCnt );
		// display the bucketchaining retrieve pseudocode
		codeManager.setCode( c.BUCKETCHAINING, c.RETRIEVE );
		// update the HashStatus
		displayManager.setPass(0);
		displayManager.setPhase( c.RETRIEVE );
 	   displayManager.setStatus( HashStatus.LOADED );
		displayManager.paintHash();
		displayManager.clearInputAndAddress();
		stepOrPause();
 		// create a Results object to store the statistics
		Results results = new Results();
		// set the table load
		results.setLoad( inTableCnt*100/(slots*buckets) );
 		displayManager.setResults( results );
 		
		// setup the pie chart
		displayManager.setNumLeft( dataSize );
		displayManager.updateStats();
		
		// initialize variables
		long start = new Date().getTime();
		int home, address;
		double size = slots*buckets;
		int probes = 0, searchCnt;

 	   // The algorithm starts by searching the home address for the value. If the
		// value is present the search is successful. If the value is not there and the
		// link for the bucket is -1, the search is unsuccessful.  If the value is not
		// in the bucket and the bucket's link is not -1, the algorithm searches each 
		// bucket in the chain until the value is found or a bucket with a -1 link is
		// reached
		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			displayManager.setStatus( HashStatus.SEARCH );
			// show the full hash equation ( home = retrieve[i] mod buckets )
			displayManager.setInput( retrieve[i] );
			home = retrieve[i] % buckets;
			displayManager.setHomeAddress( home );
  		   searchCnt = 1;	// reset the search counter
   
			codeManager.selectLine( 0 );		// if( Table[loc] == X )
			found = hashTable.search( home, retrieve[i] );
			// update the average number of probes required per value searched
			results.setProbes( (double)++probes/(i+1));
			results.setTime( new Date().getTime() - start );
			displayManager.updateStats();
			displayManager.paintHash();
 			codePause();		// only pause if pseudocode is showing

			if( found ){
				codeManager.selectLine( 1 );		// return FOUND
				displayManager.setStatus( HashStatus.FOUND );
 		   	applet.playSuccessSound();
				results.incSuccess();
				// update the pie chart
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
				results.setTime( new Date().getTime() - start );
				// record current stats for future plotting
				results.setPlotData( i+1, dataSize );
				displayManager.paintHash();
 				displayManager.updateStats();
			}
			else{	// the home address did not contain the value, search the chain until
		         // either the value is found, the bucket's link equals -1 or the search
					// count equals the number of buckets
				address = home;
				while( !abortFlag ){
					codeManager.selectLine(3); // while(Table[loc] != X 
														//		&& Table[loc].link != NULL)
					displayManager.paintHash();
					if( found || hashTable.getLinkValue(address) == -1 || searchCnt == buckets ){
						codePause();
						break;
					}
					stepOrPause();

					codeManager.selectLine(5); // loc = Table[loc].link;
					hashTable.clearProbes();
					displayManager.paintHash();
 					address = hashTable.getLinkValue(address);
					codePause();		// only pause if pseudocode is showing
					if( address != -1 )
						found = hashTable.search( address, retrieve[i] );
 				   searchCnt++;
					// update the average number of probes required per value searched
					results.setProbes( (double)++probes/(i+1));
					results.setTime( new Date().getTime() - start );
					displayManager.updateStats();
					displayManager.paintHash();
				} 
				codeManager.selectLine(6); // if( Table[loc] == X )
				codePause();		// only pause if pseudocode is showing
				if( found  ){
					codeManager.selectLine(7); // return FOUND
					displayManager.setStatus( HashStatus.FOUND );
					applet.playSuccessSound();
					results.incSuccess();
				}
				else{
					codeManager.selectLine(8); // return NOT_FOUND
					displayManager.setStatus( HashStatus.NOT_FOUND );
					applet.playUnsuccessSound();
					results.incUnsuccess();
				}
				results.setTime( new Date().getTime() - start );
				// record current stats for future plotting
				results.setPlotData( i+1, dataSize );
				// update the pie chart
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
				displayManager.paintHash();
 				displayManager.updateStats();
			} // end else
			if( i < dataSize -1 )
				stepOrPause();
			hashTable.clearProbes();
	
		} // end for loop
		displayManager.paintHash();

		// create new Summary object and add it to the SummaryManager.
		// The object is cloned so that subsequent runs do not overwrite the data
		if( !abortFlag ){
			Summary sum = new Summary();
			sum.setData( settings, results );
			summaryManager.add( (Summary)sum.clone() );
		}
 	}
	
	// This method returns the address following the address passed to the method.
	// It uses wrap around if necessary
	public int nextAddress( int addr ){
		if( ++addr >= buckets )
			return 0;
		else
			return addr;
	}
  
	// This method calls the applet's stepOrPause method if the boolean argument
	// is true
	public void conditionalPause( boolean b ){
		if( b && !abortFlag )
			applet.stepOrPause();
		applet.checkSuspended();
	}

	// This method calls the applet's stepOrPause method only if the pseudocode
	// is displayed or the boolean argument is false
	public void statPause( boolean b){
		if( (codeFlag || !b )&& !abortFlag )
			applet.stepOrPause();
		applet.checkSuspended();
   }	
}


/******************************************************************************
	The ChainingAlgorithm class defines the methods which demonstrate the
	linked-list chaining collision resolution algorithm.
******************************************************************************/ 
class ChainingAlgorithm extends HashAlgorithm{

	// construction
	public ChainingAlgorithm( HashApplet parent, DisplayManager dm, SummaryManager sm,
 									CodeManager cm, ButtonPanel bp, Setup setup, HashTable ht ){	
		applet = parent;
		displayManager = dm;
		summaryManager = sm;
		codeManager = cm;
		buttonPnl = bp;
		settings = setup;
		hashTable = ht;
		input = new int[300];
		retrieve = new int[300];
		numGen = new NumberGenerator();
		c = new Common();
	}
	
   // This method is called by the run method of the applet's thread and executes the
	// demo
	public void run(){
		codeFlag = applet.getCodeShowing();
		runFlag = true;
		// disable controls while demo is executing
		buttonPnl.enablePageButtons( false );
		buttonPnl.enableMenu( false );

		if( execMode == c.STORE )
			store();
		else
			retrieve();
		runFlag = false;
		displayManager.setStatus( HashStatus.DONE );
		codeManager.clearSelections();
		if( abortFlag ){
			hashTable.clear();
			displayManager.setStatus( HashStatus.READY );
			displayManager.initialize( c.LINKEDLISTCHAINING, execMode, buckets, dataSize );
			abortFlag = false;
		}
		displayManager.paintHash();
		// enable controls again
		buttonPnl.enablePageButtons( c.EXECUTE );
		buttonPnl.enableMenu( true );
		buttonPnl.setRunButtonsInitial();
	}

	
	// This method stores data from the input array in the hash table.  When
	// collisions occur, a node containing the data value is inserted in the
 	// front of the link-list linked to the home address bucket.
	// As the demo runs, the table graphics, statistics and pseudocode display
	// are updated to show the current state of the table
	public void store(){
		// create a Results object to store the statistics
		Results results = new Results();;
 		displayManager.setResults( results );
		// setup the pie chart
 		displayManager.setNumLeft( dataSize );
 		displayManager.updateStats();
		// show the hash function ( mod buckets )
		displayManager.setHx( buckets );
		// setup the HashStatus
		displayManager.setPhase( c.STORE );
		// initialize variables
		long start = new Date().getTime();
		int address;
		double size = slots*buckets;
		int probes = 0;
		boolean found;
		
		
		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			displayManager.setStatus( HashStatus.SEARCH );
			displayManager.paintHash();
			// show the full hash equation: address = input[i] mod buckets
			codeManager.selectLine(0); // loc = X % numBuckets
			displayManager.setInput( input[i] );
			address = input[i] % buckets;
			displayManager.setHomeAddress( address );
			displayManager.updateStats();
			codePause();		// only pause if pseudocode is showing

			codeManager.selectLine(1); // if(Table[loc] == EMPTY
			found = hashTable.hasEmptySlot(address);
			displayManager.paintHash();
			// update the average number of probes required per value attempted to store
			results.setProbes( (double)++probes/(i+1) );
			results.setTime( new Date().getTime() - start );
			displayManager.updateStats();
			codePause();		// only pause if pseudocode is showing

			// The home address has a vacant slot
			if( found ){
				displayManager.setStatus( HashStatus.FOUND );
				codeManager.selectLine(2); // Table[loc] = X;
				// insert data at home address
				hashTable.insert( address, input[i] );
				displayManager.paintHash();
			}
			else{ 	// the home address is full, insert the data as a new node
 		   			// in the front of the bucket's linked list
				codeManager.selectLine(4); // InsertNodeInFront(X);
				int len = hashTable.getListLength( address );
				
				// if the list is not empty move the existing nodes right to 
				// make room for the insertion
				if( len != 0 )
					moveList( address, false );
				hashTable.insertNode( address, input[i] );
				// make the node visible
				hashTable.showNode( address );
				displayManager.paintHash();
			}
			applet.playSuccessSound();
			results.incSuccess();
			displayManager.setStatus( HashStatus.FOUND );
			displayManager.paintHash();
			// update the current table load
			results.setLoad( (i+1)*100/size );
			results.setTime( new Date().getTime() - start );
			// record the current stats for future plotting
			results.setPlotData( i+1, size );
			// update the pie chart
			displayManager.setNumLeft( dataSize - ( i + 1 ) );
 			displayManager.updateStats();
			if( i < dataSize -1 || codeFlag )
				stepOrPause();
			hashTable.clearProbes();
		}
					
  	   // if we aren't doing a retrieve next create new summary object and add it
 	   // to the SummaryManager. The object is cloned so that subsequent runs do not
	   // overwrite the data
		if( !abortFlag && execMode == c.STORE ){
			Summary sum = new Summary();
			sum.setData( settings, results );
			summaryManager.add( (Summary)sum.clone() );
		} 
	}

	// This method inserts data into the hash table without table animation,
	// statistics or source code display updates.  The pie chart progress meter
	// is updated to provide user feedback
	public void quickLoad(){
		// display the status to the user
		displayManager.setPhase( c.STORE );
		displayManager.setStatus( HashStatus.QUICKLOAD );
		displayManager.paintHash();
		// let paint finish before inserting date
		applet.pause( 50 );

		// create Results object for use by DisplayManager
		Results results = new Results();
 		displayManager.setResults( results );
		// setup the pie chart
 		displayManager.setNumLeft( dataSize );
 		displayManager.updateStats();
		
		boolean found;
		int address;
		double size = slots*buckets;
		displayManager.setHx( buckets );

		// Main loop which inserts data
		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			address = input[i] % buckets;
			found = hashTable.hasEmptySlot(address);
			
			// The home address has a vacant slot
			if( found ){
				// insert data at home address
				hashTable.insert( address, input[i] );
			}
			else{ 	// the home address is full, insert the data as a new node
 		   			// in the front of the bucket's linked list
				int len = hashTable.getListLength( address );

				// if the list is not empty move the existing nodes right to 
				// make room for the insertion
				if( len != 0 )
					moveList( address, true );
				hashTable.insertNode( address, input[i] );
				// set the node as visible
				hashTable.showNode( address );
			}
			// update results object for the calling function and
			// display the progress pie chart for the user
			results.setLoad( (i+1)*100/size );
			displayManager.setNumLeft( dataSize - ( i + 1 ) );
 			displayManager.updatePie();
			
		}
		// clean up highlighted bucket frames and nodes
		hashTable.clearProbes();
	}

	// This method searches the hash table for the data in the retrieve array.
	// As the demo runs, the table graphics, statistics and pseudocode display
	// are updated to show the current state of the table
	public void retrieve(){
		codeManager.setCode( c.LINKEDLISTCHAINING, c.STORE );
		// generate the data that we will search for
		genRetrieveData(input, dataSize);
		
		// run quickLoad() or store() to populate the table
		if( quickLoad )
 			quickLoad();
		else
			store();

		// display the linked-list chaining retrieve pseudocode
		codeManager.setCode( c.LINKEDLISTCHAINING, c.RETRIEVE );
		// update the HashStatus
		displayManager.setPhase( c.RETRIEVE );
 	   displayManager.setStatus( HashStatus.LOADED );
		displayManager.paintHash();
		displayManager.clearInputAndAddress();
		stepOrPause();
		// create a Results object to store the statistics
		Results results = new Results();
		// set the table load
		results.setLoad( dataSize*100/(slots*buckets) );
 		displayManager.setResults( results );
 		
		// setup the pie chart
		displayManager.setNumLeft( dataSize );
		displayManager.updateStats();
		
		// initialize variables
		long start = new Date().getTime();
		int address;
		double size = slots*buckets;
		int probes = 0, searchCnt;
		boolean found;

		// The algorithm starts by searching the slots at the home address bucket.  If the
		// value is present the search is successful.  If the value is not there and the
		// buckets linked list chain has a zero length the search is unsuccessful. If the
		// chain has a non zero length, the algorithm searches each node of the chain until
		// the value is found or the end of the chain is reached.
		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			displayManager.setStatus( HashStatus.SEARCH );
			// show the full hash equation: address = retrieve[i] mode buckets
			displayManager.setInput( retrieve[i] );
			address = retrieve[i] % buckets;
			displayManager.setHomeAddress( address );
  	 
			codeManager.selectLine( 0 );		// if( Table[loc] == X )
			found = hashTable.search( address, retrieve[i] );
			// update the average number of probes required per value searched
			results.setProbes( (double)++probes/(i+1));
			results.setTime( new Date().getTime() - start );
			displayManager.updateStats();
			displayManager.paintHash();
 			codePause();		// only pause if pseudocode is showing

			// the value was present in the slots of the home address bucket
			if( found ){
				codeManager.selectLine( 1 );		// return FOUND
				displayManager.setStatus( HashStatus.FOUND );
 		   	applet.playSuccessSound();
				results.incSuccess();
				// update the pie chart
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
				results.setTime( new Date().getTime() - start );
				// record the current stats for future plotting
				results.setPlotData( i+1, dataSize );
				displayManager.paintHash();
 				displayManager.updateStats();
			}
			else{	// the value was not in the bucket's slot, check if the bucket
					// has a chain
				codeManager.selectLine(2); // else if( Table[loc].link != NULL )
				displayManager.paintHash();
				int nodeCount = hashTable.getListLength( address );
				if( nodeCount == 0 )
			 		codePause();		// only pause if pseudocode is showing
				
				// search each node of the chain until the value is found or the end
				// of the chain is reached
				for( int j = 0; j < nodeCount; j++ ){
					stepOrPause();
					codeManager.selectLine(3); // while(Node.Data != X && Node.Next != NULL)
					hashTable.clearProbes();
					displayManager.paintHash();
					found = hashTable.searchNode( address, j, retrieve[i] );
					// update the average number of probes required per value searched
					results.setProbes( (double)++probes/(i+1));
					results.setTime( new Date().getTime() - start );
					displayManager.updateStats();
					displayManager.paintHash();
					codePause();		// only pause if pseudocode is showing

					if( found || abortFlag || (j == nodeCount-1) )
						break;
			
					if( codeFlag )
						hashTable.clearProbes();
					displayManager.paintHash();
					codeManager.selectLine(5);  // Node = Node.Next;
				}
				if( nodeCount > 0 ){
					codeManager.selectLine(6); // if( Node.Data == X )
					codePause();		// only pause if pseudocode is showing
				}
				if( found  ){
					codeManager.selectLine(7); // return FOUND
					displayManager.setStatus( HashStatus.FOUND );
					applet.playSuccessSound();
					results.incSuccess();
				}
				else{
					codeManager.selectLine(9); // return NOT_FOUND
					displayManager.setStatus( HashStatus.NOT_FOUND );
					applet.playUnsuccessSound();
					results.incUnsuccess();
				}
				results.setTime( new Date().getTime() - start );
				// record current stats for future plotting
				results.setPlotData( i+1, dataSize );
				// update the pie chart
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
				displayManager.paintHash();
 				displayManager.updateStats();	
			} // end else
			if( i < dataSize -1 )
				stepOrPause();
			hashTable.clearProbes();
	
		} // end for loop
		displayManager.paintHash();

		// create new Summary object and add it to the SummaryManager.
		// The object is cloned so that subsequent runs do not overwrite the data
		if( !abortFlag ){
			Summary sum = new Summary();
			sum.setData( settings, results );
			summaryManager.add( (Summary)sum.clone() );
		}	
 	}

	// This method moves all the linked-list nodes of the bucket at the
	// specified address right one position.  This process is animated
	// unless the quick flag is true
	public void moveList( int address, boolean quick ){
		for(int i = 0; i < 20; i++ ){
			hashTable.moveList( address );
			if( !quick ){
				displayManager.paintHash();
				applet.pause( 15 );
			}
		}
	}
}


/******************************************************************************
	The OverflowAlgorithm class defines the methods which demonstrate the
	chaining with separate overflow table collision resolution algorithm.
******************************************************************************/ 
class OverflowAlgorithm extends HashAlgorithm{
	private HashTable overFlow;// the overflow table used by the algorithm
	private int[] inTable;		// holds the values that are currently stored in the table
	private int inTableCnt;		// the number of values currently stored in the table
	
	// construction
	public OverflowAlgorithm( HashApplet parent, DisplayManager dm,
 									  SummaryManager sm, CodeManager cm, ButtonPanel bp,
									  Setup setup, HashTable ht, HashTable of ){
		applet = parent;
		displayManager = dm;
		summaryManager = sm;
		codeManager = cm;
		buttonPnl = bp;
		settings = setup;
		hashTable = ht;
		overFlow = of;
		input = new int[300];
		retrieve = new int[300];
		numGen = new NumberGenerator();
		c = new Common();
		inTable = new int[300];
	}
  
	// This method is called by the run method of the applet's thread and executes the
	// demo
	public void run(){
		codeFlag = applet.getCodeShowing();
		runFlag = true;
		// disable controls while demo is executing
		buttonPnl.enablePageButtons( false );
		buttonPnl.enableMenu( false );

		if( execMode == c.STORE )
			store();
		else
			retrieve();
		runFlag = false;
		displayManager.setStatus( HashStatus.DONE );
		codeManager.clearSelections();
		if( abortFlag ){
			hashTable.clear();
			overFlow.clear();
			displayManager.setStatus( HashStatus.READY );
			displayManager.initialize( c.CHAININGWITHOVERFLOW, execMode, buckets, dataSize );
			abortFlag = false;
		}
		displayManager.paintHash();
		// enable controls again
		buttonPnl.enablePageButtons( c.EXECUTE );
		buttonPnl.enableMenu( true );
		buttonPnl.setRunButtonsInitial();		
	}

	// This method stores data from the input array in the hash table.  When
	// collisions occur, the chaining-with-separate-overflow collision resolution 
	// algorithm is used.  As the demo runs, the table graphics, statistics and 
	// pseudocode display are updated to show the current state of the table
	public void store(){
		// create a Results object to store the statistics
		Results results = new Results();;
 		displayManager.setResults( results );
		// setup the pie chart
		displayManager.setNumLeft( dataSize );
 		displayManager.updateStats();
		// show the hash function ( mod buckets )
		displayManager.setHx( buckets );
		// setup the HashStatus
		displayManager.setPhase( c.STORE );
		// initialize variables
		long start = new Date().getTime();
		int address, link, ovPos;
		double size = slots*buckets + overflowSize;
		int probes = 0;
		inTableCnt = 0;
		int searchCnt = 0;
		boolean found1 = false, found2 = false;
		
		// The storing algorithm attempts to insert data at its home address. If the
 	   // home bucket is full and the home bucket's link equals -1, it searches for 
		// the first empty bucket in the overflow table, inserts the data there and
		// sets the home buckets link value to the overflow bucket's address.  If the
		// home bucket is full and has a link to the overflow table, the algorithm finds
		// the last bucket in the chain and inserts the value in the first empty bucket
		// it finds after the last chain bucket.  It then sets the last chain bucket's
		// link value to the address of the bucket where the value was inserted
		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			// update the HashStatus
			displayManager.setStatus( HashStatus.SEARCH );
			displayManager.paintHash();

			codeManager.selectLine(0); // loc = X % numBuckets
			// show the full hash equation (address = input[i] mod buckets )
			displayManager.setInput( input[i] );
			address = input[i] % buckets;
			displayManager.setHomeAddress( address );
			displayManager.updateStats();
			codePause();		// only pause if pseudocode is showing

			codeManager.selectLine(1); // if(Table[loc] == EMPTY
			found1 = hashTable.hasEmptySlot(address);
			if( found1 ){
				applet.playSuccessSound();
				results.incSuccess();
				displayManager.setStatus( HashStatus.FOUND );
			}
			displayManager.paintHash();
			// update the average number of probes required per value attempted to store
			results.setProbes( (double)++probes/(i + 1));
			results.setTime( new Date().getTime() - start );
			displayManager.updateStats();
	 		statPause( found1 );	// pause if code is showing or found is false
			if( !found1 )
				hashTable.clearProbes();
				displayManager.paintHash();

			if( found1 ){
	 			codeManager.selectLine(2); // Table[loc] = X
				// insert data at home address
				hashTable.insert( address, input[i] );
				// save data, it will be used to help generate the data to retrieve
				inTable[inTableCnt] = input[i];
				inTableCnt++;
				displayManager.paintHash();
				// update the current table load
				results.setLoad( inTableCnt*100/size );
				results.setTime( new Date().getTime() - start );
				// record the current stats for future plotting
				results.setPlotData( inTableCnt, size );
				// update the pie chart
				displayManager.decNumLeft();
 				displayManager.updateStats();
				stepOrPause();
			}
			else{	  // home address if full, attempt to store data in the overflow table
				codeManager.selectLine(4); // while( OvFlow[pos] == FULL )
				ovPos = 0;
				link = hashTable.getLinkValue(address);

				// the home bucket is not chained, use first empty overflow bucket
				if( link == -1 ){	  			 
					searchCnt = 0;
					// do a linear probing search until an empty bucket is found in the
					// overflow table or the search count equals the number of buckets in
					// the overflow table
					while( !abortFlag ){
						overFlow.clearProbes();
						displayManager.paintHash();

						codeManager.selectLine(4); // while( OvFlow[pos] == FULL )
						found2 = overFlow.hasEmptySlot( ovPos );
						// update the average number of probes required per value attempted
					   // to store
						results.setProbes( (double)++probes/(i + 1));
						results.setTime( new Date().getTime() - start );
						displayManager.updateStats();
						searchCnt++;

						if( found2 ){
							applet.playSuccessSound();
							displayManager.setStatus( HashStatus.FOUND );
							displayManager.paintHash();
							results.incSuccess();
						}

						if( searchCnt == overflowSize )
							break;
						// pause if pseudocode is showing or found is false
						conditionalPause( codeFlag || !found2 );
					   if( found2 )
							break;

						codeManager.selectLine(5); // pos++;
						ovPos = nextOverPosition( ovPos );
						overFlow.clearProbes();
						displayManager.paintHash();
						codePause();		// only pause if pseudocode is showing
					} // end while

					if( found2 ){
						codeManager.selectLine(6); // ovFlow[pos] = X
						// insert data into the overflow table
						overFlow.insert( ovPos, input[i] );
						displayManager.paintHash();
						codePause();		// only pause if pseudocode is showing

						codeManager.selectLine(7); // UpdateLinks();
 					   hashTable.setLinkValue( address, ovPos );
						// save data, it will be used to help generate the data to retrieve
						inTable[inTableCnt] = input[i];
						inTableCnt++;
						// update the current table load
						results.setLoad( inTableCnt*100/size );
						results.setTime( new Date().getTime() - start );
						// record the current stats for future plotting
						results.setPlotData( inTableCnt, size );
						
					}
					else{
						displayManager.setStatus( HashStatus.NOT_FOUND );
						applet.playUnsuccessSound();
						results.incUnsuccess();
					}
					// update the pie chart
					displayManager.decNumLeft();
					results.setTime( new Date().getTime() - start );
					displayManager.updateStats();
					displayManager.paintHash();
					if( i < dataSize -1 )
						stepOrPause();
				}
				// the home bucket is chained (link != -1)
				else{  
					searchCnt = 0;

					// search the chain until it terminates with a link equal to -1,
					// then do a linear probing search to find the next empty bucket.
 				   // If the number of searches equals the number of buckets in the
					// overflow table the search is unsuccessful
					while( !abortFlag ){
						overFlow.clearProbes();
						displayManager.paintHash();
						codeManager.selectLine(4); // while( OvFlow[pos] == FULL )
						found2 = overFlow.hasEmptySlot( link );
						// update the average number of probes required per value attempted
						// to store
						results.setProbes( (double)++probes/(i + 1));
						results.setTime( new Date().getTime() - start );
						searchCnt++;
				 	   displayManager.updateStats();

						if( searchCnt == overflowSize )
							break;
						// pause if pseudocode is showing or found is false
						conditionalPause( codeFlag || !found2 );
	 
						overFlow.clearProbes();
						displayManager.paintHash();

						codeManager.selectLine(5); // pos++;
						ovPos = link;
						link = overFlow.getLinkValue( ovPos );
						if( link == -1 )
							break;
						codePause();		// only pause if pseudocode is showing
					}
					// the search failed, update the stats and pie chart.
					if( !found2 && searchCnt == overflowSize ){  
						displayManager.setStatus( HashStatus.NOT_FOUND );
						applet.playUnsuccessSound();
						results.incUnsuccess();
						results.setTime( new Date().getTime() - start );
						// update the pie chart
						displayManager.decNumLeft();
						displayManager.updateStats();
						if( i < dataSize -1 )
							stepOrPause();	
					}
					// the search found the end of the chain, do a linear probing
					// search until an empty bucket is found or the search count
					// equals the number of buckets in the overflow table
					else if( link == -1 ){  
						searchCnt = 1;
						int startPos = ovPos;
						while( !abortFlag ){
							overFlow.clearProbes();
							displayManager.paintHash();

							codeManager.selectLine(5); //pos++;
							ovPos = nextOverPosition( ovPos );
							codePause();		// only pause if pseudocode is showing

							codeManager.selectLine(4); // while( OvFlow[pos] == FULL )
							found2 = overFlow.isEmpty( ovPos );
							// update the average number of probes required per value
							// attempted to store
							results.setProbes((double)++probes/(i + 1));
							displayManager.updateStats();
							displayManager.paintHash();
							searchCnt++;

							if( found2){
								applet.playSuccessSound();
								displayManager.setStatus( HashStatus.FOUND );
								displayManager.paintHash();
								results.incSuccess();
							}

							if( searchCnt == overflowSize )
								break;
							// pause if pseudocode is showing or found is false
							conditionalPause( codeFlag || !found2 );
					   	if( found2 )
								break;
						}
						if( found2 ){
							codeManager.selectLine(6); // ovFlow[pos] = X
							// insert data into the overflow table 
							overFlow.insert( ovPos, input[i] );
							// save data, it will be used to help generate the data to retrieve
							inTable[inTableCnt] = input[i];
							inTableCnt++;
							displayManager.paintHash();
							// update the current table load
							results.setLoad( inTableCnt*100/size );
							results.setTime( new Date().getTime() - start );
							// record current stats for future plotting
							results.setPlotData( inTableCnt, size );
							// update the pie chart
							displayManager.decNumLeft();
							displayManager.updateStats();
							codePause();		// only pause if pseudocode is showing

							codeManager.selectLine(7); // UpdateLinks();
 					  	 	overFlow.setLinkValue( startPos, ovPos );    
							displayManager.paintHash();
						}
						else{
							displayManager.setStatus( HashStatus.NOT_FOUND );
							applet.playUnsuccessSound();
							results.incUnsuccess();
							results.setTime( new Date().getTime() - start );
							// update the pie chart
							displayManager.decNumLeft();
							displayManager.updateStats();
						}
						displayManager.paintHash();
						if( i < dataSize -1 )
							stepOrPause();
					}// end else link == -1

				}// end else link != -1
				overFlow.clearProbes(); //!!!
				displayManager.paintHash();
			}
			hashTable.clearProbes();
			overFlow.clearProbes();
			displayManager.paintHash();
			
		} // end for loop

		// if we aren't doing a retrieve next create new summary object and add it
 	   // to the SummaryManager. The object is cloned so that subsequent runs do not
	   // overwrite the data
		if( !abortFlag && execMode == c.STORE ){
			Summary sum = new Summary();
			sum.setData( settings, results );
			summaryManager.add( (Summary)sum.clone() );
		}
		
	}

	// This method inserts data into the hash table without table animation,
	// statistics or source code display updates.  The pie chart progress meter
	// is updated to provide user feedback
	public void quickLoad(){
		// display status to user
		displayManager.setPhase( c.STORE );
		displayManager.setStatus( HashStatus.QUICKLOAD );
		displayManager.paintHash();
		// let paint finish before inserting data
		applet.pause( 50 );

		// create Results object for use by DisplayManager
		Results results = new Results();;
 		displayManager.setResults( results );
 		displayManager.setNumLeft( dataSize );
 		displayManager.updateStats();
		displayManager.setHx( buckets );
		boolean found1 = false, found2 = false;
		int searchCnt = 0;
		int address, link, ovPos;
		double size = slots*buckets + overflowSize;
		inTableCnt = 0;
		
		// The storing algorithm attempts to insert data at its home address. If the
 	   // home bucket is full and the home bucket's link equals -1, it searches for 
		// the first empty bucket in the overflow table, inserts the data there and
		// sets the home buckets link value to the overflow bucket's address.  If the
		// home bucket is full and has a link to the overflow table, the algorithm finds
		// the last bucket in the chain and inserts the value in the first empty bucket
		// it finds after the last chain bucket.  It then sets the last chain bucket's
		// link value to the address of the bucket where the value was inserted 
		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			address = input[i] % buckets;
			found1 = hashTable.hasEmptySlot(address);
			
			if( found1 ){
				// empty slot found, insert data
	 			hashTable.insert( address, input[i] );
				// save data, it will be used to help generate the data to retrieve
				inTable[inTableCnt] = input[i];
				inTableCnt++;
				// update the current table load
				results.setLoad( inTableCnt*100/size );
				// update pie chart
				displayManager.decNumLeft();
 			}
			else{	  // home address if full, attempt to store data in the overflow table
				ovPos = 0;
				link = hashTable.getLinkValue(address);

				// the home bucket is not chained, use first empty overflow bucket
				if( link == -1 ){	  		 
					searchCnt = 0;

					// do a linear probing search until an empty bucket is found in the
					// overflow table or the search count equals the number of buckets in
					// the overflow table
					while( !abortFlag ){
						found2 = overFlow.hasEmptySlot( ovPos );
						searchCnt++;

						if( searchCnt == overflowSize )
							break;
					   if( found2 )
							break;
						
						ovPos = nextOverPosition( ovPos );
						
					} // end while

					if( found2 ){
						// insert data into the overflow table
						overFlow.insert( ovPos, input[i] );
						hashTable.setLinkValue( address, ovPos );
						// save data, it will be used to help generate the data to retrieve
						inTable[inTableCnt] = input[i];
						inTableCnt++;
						// update the current table load
						results.setLoad( inTableCnt*100/size );	
					}
					// update pie chart
					displayManager.decNumLeft();
					
				}
				// the home bucket is chained (link != -1)
				else{  
				   searchCnt = 0;

					// search the chain until it terminates with a link equal to -1,
					// then do a linear probing search to find the next empty bucket.
 				   // If the number of searches equals the number of buckets in the
					// overflow table the search is unsuccessful
					while( !abortFlag ){
						found2 = overFlow.hasEmptySlot( link );
						searchCnt++;

						if( searchCnt == overflowSize )
							break;
					   ovPos = link;
						link = overFlow.getLinkValue( ovPos );
						if( link == -1 )
							break;
					}
					// the search failed, update pie chart.
					if( !found2 && searchCnt == overflowSize ){  
						// update pie chart
						displayManager.decNumLeft();							
					}
					// the search found the end of the chain, do a linear probing
					// search until an empty bucket is found or the search count
					// equals the number of buckets in the overflow table
					else if( link == -1 ){  
						searchCnt = 1;
						int startPos = ovPos;
						while( !abortFlag ){
							ovPos = nextOverPosition( ovPos );
							found2 = overFlow.isEmpty( ovPos );
							searchCnt++;

							if( searchCnt == overflowSize )
								break;
					   	if( found2 )
								break;
						}
						if( found2 ){
							// insert data into the overflow table 
							overFlow.insert( ovPos, input[i] );
							// save data, it will be used to help generate the data to retrieve
							inTable[inTableCnt] = input[i];
							inTableCnt++;
					//		displayManager.paintHash();
							// update the current table load
							results.setLoad( inTableCnt*100/size );
							overFlow.setLinkValue( startPos, ovPos );    
						}
						// update pie chart
						displayManager.decNumLeft();
								
					}// end else link == -1

				}// end else link != -1
			}
			// update pie chart
			displayManager.updatePie();
						
		} // end for loop
		hashTable.clearProbes();
		overFlow.clearProbes();
		displayManager.paintHash();
	}

	
	// This method searches the hash and overflow tables for the data in the retrieve 
	// array.  As the demo runs, the table graphics, statistics and pseudocode display
	// are updated to show the current state of the table
	public void retrieve(){
		boolean found;
		codeManager.setCode( c.CHAININGWITHOVERFLOW, c.STORE );

		// run quickLoad() or store() to populate the table
		if( quickLoad )
 			quickLoad();
		else
			store();

		// generate the data that we will search for
		genRetrieveData( inTable, inTableCnt );
		// display the chaining with separate overflow retrieve pseudocode
		codeManager.setCode( c.CHAININGWITHOVERFLOW, c.RETRIEVE );
		// update the HashStatus
		displayManager.setPhase( c.RETRIEVE );
 	   displayManager.setStatus( HashStatus.LOADED );
		displayManager.paintHash();
		displayManager.clearInputAndAddress();
		stepOrPause();
		// create a Results object to store the statistics
		Results results = new Results();
		double size = slots*buckets + overflowSize;
		// set the table load
		results.setLoad( inTableCnt*100/(size) );
 		displayManager.setResults( results );
 		
		// setup the pie chart
		displayManager.setNumLeft( dataSize );
		displayManager.updateStats();
		
		// initialize variables
		long start = new Date().getTime();
		int home, address, link = 0;
		int probes = 0, searchCnt;
 
		// The algorithm starts by searching the home address for the value. If the
		// value is present the search is successful. If the value is not there and the
		// link for the bucket is -1, the search is unsuccessful.  If the value is not
		// in the bucket and the bucket's link is not -1, the algorithm searches each 
		// bucket in the chain in the overflow table until the value is found or a bucket
		//  with a -1 link is reached
		for( int i = 0; (i < dataSize)&&!abortFlag; i++ ){
			displayManager.setStatus( HashStatus.SEARCH );
			// show the full hash equation ( home = retrieve[i] mod buckets )
			displayManager.setInput( retrieve[i] );
			home = retrieve[i] % buckets;
			displayManager.setHomeAddress( home );
  		   searchCnt = 1;	// reset the search counter
   
			codeManager.selectLine( 0 );		// if( Table[loc] == X )
			found = hashTable.search( home, retrieve[i] );
			// update the average number of probes required per value searched
			results.setProbes( (double)++probes/(i+1));
			results.setTime( new Date().getTime() - start );
			displayManager.updateStats();
			displayManager.paintHash();
 			codePause();		// only pause if pseudocode is showing

			if( found ){
				codeManager.selectLine( 1 );		// return FOUND
				displayManager.setStatus( HashStatus.FOUND );
 		   	applet.playSuccessSound();
				results.incSuccess();
				// update pie chart
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
				results.setTime( new Date().getTime() - start );
				// record current stats for future plotting
				results.setPlotData( i+1, dataSize );
				displayManager.paintHash();
 				displayManager.updateStats();
			}
			else{	// The home address did not contain the value.  If the home address
					// is not linked the search is unsuccessful, otherwise search the chain
				 	// in the overflow table until either the value is found, the bucket's
				 	// link equals -1 or the search count equals the number of buckets in
					// the overflow table
				codeManager.selectLine(2); // else if( Table[loc].link ! =-1 );
				link = hashTable.getLinkValue(home); 
				
				// If the home address in not linked, the value will not be in
				// the overflow table. The search is unsuccessful.
				if( link == -1 ){
					codePause();		// only pause if pseudocode is showing
					codeManager.selectLine(9); // return NOT_FOUND
					displayManager.setStatus( HashStatus.NOT_FOUND );
					applet.playUnsuccessSound();
					results.incUnsuccess();
				}
				else{	// The home address is linked ( link != -1 )
					stepOrPause();
					hashTable.clearProbes();
					displayManager.paintHash();
					address = link;

					// Search the chain in the overflow table until either the value
				 	// is found, the bucket's link equals -1 or the search count equals
				 	// the number of buckets in the overflow table
					while( !abortFlag ){
						codeManager.selectLine(3); // while(OvFlow[pos] != X &&
					 										// 	OvFlow[pos].link != NULL)
						found = overFlow.search( address, retrieve[i] );
						searchCnt++;
						// update the average number of probes required per value searched
						results.setProbes( (double)++probes/(i+1));
						results.setTime( new Date().getTime() - start );
						displayManager.updateStats();
 					   link = overFlow.getLinkValue( address);
						displayManager.paintHash();

						if( link == -1 )
							codePause();		// only pause if pseudocode is showing
						if( found || link == -1 || searchCnt == overflowSize )
							break;
						stepOrPause();

						codeManager.selectLine(5); // pos = OvFlow[pos].link;
						address = link;
						overFlow.clearProbes();
						hashTable.clearProbes();
						displayManager.paintHash();
 						codePause();		// only pause if pseudocode is showing
						 				  		
						displayManager.paintHash();
					} 
					if( found  ){
						codeManager.selectLine(6); // if( OvFlow[pos] == X )
						codePause();		// only pause if pseudocode is showing

						codeManager.selectLine(7); // return FOUND
						displayManager.setStatus( HashStatus.FOUND );
						applet.playSuccessSound();
						results.incSuccess();
					}
					else{
						codeManager.selectLine(9); // return NOT_FOUND
						displayManager.setStatus( HashStatus.NOT_FOUND );
						applet.playUnsuccessSound();
						results.incUnsuccess();
					}
					results.setTime( new Date().getTime() - start );
					// record the current stats for future plotting
					results.setPlotData( i+1, dataSize );
				}
				// update the pie chart
				displayManager.setNumLeft( dataSize - ( i + 1 ) );
				displayManager.paintHash();
 				displayManager.updateStats();
				
			} // end else
			if( i < dataSize -1 )
				stepOrPause();
			overFlow.clearProbes();
			hashTable.clearProbes();
		} // end for loop
		displayManager.paintHash();

		// create new Summary object and add it to the SummaryManager.
		// The object is cloned so that subsequent runs do not overwrite the data
		if( !abortFlag ){
			Summary sum = new Summary();
			sum.setData( settings, results );
			summaryManager.add( (Summary)sum.clone() );
		}
 	}
	
	// This method returns the address following the overflow table address passed
	//  to the method.  It uses wrap around if necessary
	public int nextOverPosition( int index ){
		if( ++index >= overflowSize )
			return 0;
		else
			return index;
	}
 
	// This method calls the applet's stepOrPause method if the boolean argument
	// is true
	public void conditionalPause( boolean b ){
		if( b && !abortFlag )
			applet.stepOrPause();
		applet.checkSuspended();
	}

	// This method calls the applet's stepOrPause method only if the pseudocode
	// is displayed or the boolean argument is false
	public void statPause( boolean b){
		if( (codeFlag || !b )&& !abortFlag )
			applet.stepOrPause();
		applet.checkSuspended();
   }	
}


/******************************************************************************
	The Setup class stores all the algorithm, table, data, and execution
 	parameters needed by the algorithm to run the demo. Accessor methods
	are provided as well methods to adjust the data size and the bucket limits
 	when the user changes the number of slots or buckets.
******************************************************************************/  
class Setup implements Cloneable{
	private Common c;
					
	private int // current settings
					alg, buckets, slots, overflow, upperLimit, dataSize, dataType,
					execMode, success,
					// default settings
					def_buckets, def_slots, def_overflow, def_upperLimit, def_dataSize,
					def_dataType, def_execMode, def_success,
					// absolute maximums
					absMaxBuckets, absMaxSlots, absMaxOverflow, absMaxUpperLimit, 
					absMaxDataSize, absMaxSuccess,
					// current maximums
					currMaxBuckets, currMaxDataSize;
  	private String algStr[];
	private boolean usePrime = false, quickLoad = false;

	// construction
   public Setup( int alg ){
		c = new Common();
		this.alg = alg;
		upperLimit = def_upperLimit = absMaxUpperLimit = 999;
		dataType = def_dataType = c.MIXED;
		execMode = def_execMode = c.STORE;
		absMaxSuccess = 100;
		absMaxSlots = 3;
 
		algStr = new String[5];
		algStr[c.LINEARPROBING] = "Linear Probing";
		algStr[c.QUADRATICPROBING] = "Quadratic Probing";
		algStr[c.BUCKETCHAINING] = "Bucket Chaining";
		algStr[c.LINKEDLISTCHAINING] = "Linked list chaining";
		algStr[c.CHAININGWITHOVERFLOW] = "Chain w/ Overflow";
 	
	}

	// This method makes a separate copy of this object.  This is necessary
   // to save data from more than one run of the same algorithm in the SummaryManager. 
	public Object clone(){
		try{
			return super.clone();
		}
		catch(CloneNotSupportedException e){
			return null;
		}
	}
	
	// This method sets the initial settings the user will see when the Setup
	// page for the algorithm is displayed for the first time
	public void setDefaults( int buckets, int slots, int overflow, int dataSize,
   								 int success ){
		def_buckets = this.buckets = buckets;
		def_slots = this.slots = slots;
		def_overflow = this.overflow = overflow;
		def_dataSize = this.dataSize = dataSize;
		def_success = this.success = success;
   }

	// this method restores the settings to their default values
	public void reset(){
		upperLimit = def_upperLimit;
		buckets = def_buckets;
		slots = def_slots;
		overflow = def_overflow;
		dataSize = def_dataSize;
		success = def_success;
		dataType = def_dataType;
		execMode = def_execMode;
		usePrime = false;
	 	quickLoad = false;
	}

	// This method sets the maximum data size and overflow table size for the algorithm
	public void setMaximums( int overflow, int dataSize ){
		absMaxOverflow = overflow;
		absMaxDataSize = currMaxDataSize = dataSize;
	}

	// set the number of slots per bucket
	public void setSlots( int slots ){
		this.slots = slots;
	}

	// set the number of buckets in the table
	public void setBuckets( int buckets ){
		this.buckets = buckets;
	}

	// set the number of single slot buckets in the overflow table
	public void setOverflow( int overflow ){
		this.overflow = overflow;
   }

	// set the upper limit of the data range, the number of values to store or
	// retrieve and the data type (odd, even or mixed)
	public void setData( int upperLimit, int dataSize, int dataType ){
		this.upperLimit = upperLimit;
		this.dataSize = dataSize;
		this.dataType = dataType;
	}

	// set the execution mode (store or retrieve)
	public void setExecMode( int execMode ){
		this.execMode = execMode;
	}

	// set the percentage of successful searches in retrieve mode
	public void setSuccess( int success ){
		this.success = success;
   }

	// Calculates the new maximum data size. This method is called after the table
	// settings are changed
	public void setMaxDataSize( ){
		switch( alg ){
			case 3:
		 		currMaxDataSize = buckets*slots + buckets*(4-slots)-buckets/2;
		 	break;
			case 4:
				currMaxDataSize = buckets*slots + overflow;
			break;
			default:
				currMaxDataSize = buckets*slots;
			break;
		}
	}

	// This method clips the data size if the user has reduced the size of the table.
	// The method returns true if an adjustment is made.
	public boolean adjustDataSize(){
		if( dataSize > currMaxDataSize ){
			dataSize = currMaxDataSize;
			return true;
		}
		else
			return false;
	}

	// This method returns the maximum number of buckets allowed based on the algorithm
	// and the number of slots.  This limit is imposed so a complete table will be
	// displayed
	public int getMaxBuckets(){
		int result = absMaxBuckets;
		if( alg == c.LINEARPROBING || alg==c.QUADRATICPROBING ){
			result =200;
			if( slots == 3 )
				result = 100;
			else if( slots == 2 )
				result = 125;
		}
		else if( alg == c.BUCKETCHAINING ){
			result =125;
			if( slots == 3 )
				result = 75;
			else if( slots == 2 )
				result = 100;
		}
		else if( alg == c.CHAININGWITHOVERFLOW ){ 
			result = 75;
			if( slots == 2 )
				result = 60;
			else if( slots == 3 )
				result = 45;
		}
		else if( alg == c.LINKEDLISTCHAINING ){
			result = 50;
		}
		return result;
	}

	// This method clips the number of buckets if the user has increased the number 
	// of slots per bucket. The method returns true if an adjustment is made.
	public boolean adjustBucketSize(){
		if( buckets > getMaxBuckets() ){
			buckets = getMaxBuckets();
			return true;
		}
		else
			return false;
	}

	// returns integer corresponding to the current algorithm (see Common class)
	public int getAlgorithm(){
		return alg;
	}

	// returns String title corresponding to the current algorithm
	public String getAlgString(){
		return algStr[alg];
	}
	
	// returns the number of buckets in the table
	public int getBuckets(){
		return buckets;
	}

	// returns the number of slots per bucket
	public int getSlots(){
		return slots;
   }

	// returns the number of buckets in the overflow table
	public int getOverflow(){
		return overflow;
   }

	// returns an "overflow" string if the Setup object is for the chaining with
	// separate overflow algorithm, otherwise "NA" is returned
	public String getOverflowString(){
		if( alg == c.CHAININGWITHOVERFLOW )
			return( "" + overflow );
		else
			return( "NA" );
	}

	// returns the upper limit of the data range
	public int getUpperLimit(){
		return upperLimit;
	}

	// returns the number of values to store or retrieve
	public int getDataSize(){
		return dataSize;
	}

	// returns an integer corresponding to the data type (odd, even or mixed, see the 
	// Common class for details)
	public int getDataType(){
		return dataType;
	}

	// returns a String identifying the current data type
	public String getDataTypeString(){
		if( dataType == c.ODD )
			return "Odd";
		else if( dataType == c.EVEN )
			return "Even";
 	   else 
			return "Even and odd";
	}

	// returns an integer corresponding to the execution mode (store or retrieve, see
	// the Common class for details)
	public int getExecMode(){
		return execMode;
	}

	// returns a String identifying the current execution mode
	public String getExecModeString(){
		if( execMode == c.STORE )
			return "Store";
		else
			return "Retrieve";
	}

	// returns integer corresponding to percentage of searches that are successful in
	// retrieve mode
	public int getSuccess(){
		return success;
	}

	// Returns a string corresponding to the percentage of successful searches.  In
	// store mode "Mixed" is returned since the percentage of successes is not
	// calculated ahead of time
	public String getSuccessString(){
		if( execMode == c.STORE )
			return "Mixed";
		else
			return "" + success + "%";
	}

	// returns the upper limit of slots per bucket
	public int getMaxSlots(){
		return absMaxSlots;
	}

	// returns the maximum allowable number of buckets in the overflow table
	public int getMaxOverflow(){
		return absMaxOverflow;
	}

	// Returns the absolute maximum upper limit on the data range. This restricts
	// the number of digits so the value will fit in the Sprite
	public int getMaxUpperLimit(){
		return absMaxUpperLimit;
   }
 
	// returns the current maximum data size 
 	public int getMaxDataSize(){
		switch( alg ){
			case 3:
		 		currMaxDataSize = buckets*slots + buckets*(4-slots)-buckets/2;
		 	break;
			case 4:
				currMaxDataSize = buckets*slots + overflow;
			break;
			default:
				currMaxDataSize = buckets*slots;
			break;
		}
		return currMaxDataSize;
	}

	// If b is true, the number of buckets should be adjusted to the nearest prime
   // number that does not exceed the bucket limit.
	public void setUsePrime( boolean b ){
		usePrime = b;
	}

	// returns the value of the usePrime flag
	public boolean getUsePrime(){
		return usePrime;
	}

	// If b is true, the loading of the table will not be animated for retrieve
	// execution mode
	public void setQuickLoad( boolean b ){
		quickLoad = b;
	}

	// returns the value of the quickLoad flag
	public boolean getQuickLoad(){
		return quickLoad;
	}
}


/******************************************************************************
	The Results class stores all the statistics( time, load, probes, successful
	searches ) and the data necessary to create the plots of average time and
 	average probes vs average load factor and	(numbers searched so far)/
   (total numbers to search). Accessor methods are provided as well methods
   to adjust the data format.
******************************************************************************/ 
class Results implements Cloneable{
	private Common c;
	// constants
	private final int MAXDATA = 301, XAXIS = 0, PROBE = 1, TIME = 2;
	// statistics			
	private int success, unsuccess, plotCounter;
	private long time;
	private double avgLoad, avgProbe;
   // stores data for plotting, plotData[0][0] holds the number of entries
	// in the array
	public double plotData[][]; 

	// construction
	public Results(){
		plotData = new double[3][MAXDATA];
		plotCounter = 0;
	}

	// This method makes a separate copy of this object.  This is necessary
   // to save data from more than one run of the same algorithm in the SummaryManager. 
 	public Object clone(){
 		try{
			Results r = (Results)super.clone();
			r.plotData = (double[][])plotData.clone();
			for( int i = 0; i < 3; i++ )
				for( int j = 0; j < MAXDATA; j++ ) 
					r.plotData[i][j] = plotData[i][j];
			return r;
 		}
 		catch(CloneNotSupportedException e){
 			return null;
 		}
	}
	
   // sets the length of time the algorithm has been running (milliseconds)
	public void setTime( long time ){
		this.time = time;
	}

	// Returns a String representing the length of time the algorithm has been 
	// running. The String is in Min:Sec:CentiSecond format
	public String getTime( ){
		return convertTime();
	}

	// Constructs and returns a String representing the millisecond time variable
	// in Min:Sec:CentiSecond format.
	public String convertTime(){
		String minStr, secStr, csStr;
		long min = time/60000;
		if( min < 10 )
			minStr = new String("0" + min + ":");
		else
			minStr = new String("" + min + ":");

		long sec = (time - min*60000)/1000;
		if( sec < 10 )
			secStr = new String("0" + sec + ":");
		else
			secStr = new String("" + sec + ":");
		long cs = (time - min*60000 - sec*1000)/10;
		if( cs < 10 )
			csStr = new String("0" + cs);
		else
			csStr = new String("" + cs);
		
		return minStr + secStr + csStr;
   }

	// sets the table load and rounds to 2 decimal places
	public void setLoad( double load ){
		avgLoad = (double)(Math.round( load*100))/100;
	}

	// Adds an entry to the plotData array. The first row is percentage table load
   // for store mode or the percentage of numbers searched out of the total number to
	// search for retrieve mode. The second row is the number of probes per numbers
	// processed.  The third row is number of seconds required to process each number.
	// plotData[0][0] stores the number of columns that contain data.
	public void setPlotData( int dataCount, double total ){
		double xValue = (double)dataCount/total;
		plotData[XAXIS][plotCounter+1] = (double)(Math.round( xValue*10000))/100;
		plotData[PROBE][plotCounter+1] = avgProbe;
		plotData[TIME][plotCounter+1] = ((double)(time))/(dataCount*1000);
		plotCounter++;
		plotData[0][0] = plotCounter;
   }

	// returns the current table load
	public double getLoad( ){
		return avgLoad;
	}

	// sets the number of probes/number of values processed rounded to 2 decimal places
	public void setProbes( double probes ){
		avgProbe = (double)(Math.round( probes*100))/100;
	}

	// returns the current average probes value
	public double getProbes( ){
		return avgProbe;
	}

	// adds one to the successful searches counter
	public void incSuccess(){
		success++;
	}

	// returns the current number of successful searches
	public int getSuccess( ){
		return success;
	}

	// adds one to the unsuccessful searches counter
	public void incUnsuccess(){
		unsuccess++;
	}

	// returns the current number of unsuccessful searches
	public int getUnsuccess( ){
		return unsuccess;
	}
}


/******************************************************************************
	The Summary class combines the Setup and Results objects for a run so that
	they may be handled as one object for display on the Results and Plot pages.
 ******************************************************************************/  
class Summary implements Cloneable{
	public Setup settings;
	public Results results;

	public Summary(){
	}

	// This method makes a separate copy of this object.  This is necessary
   // to save data from more than one run of the same algorithm in the SummaryManager. 
	public Object clone(){
		try{
			Summary s = (Summary)super.clone();
			s.settings = (Setup)settings.clone();
			s.results = (Results)results.clone();
			return s;
		}
		catch(CloneNotSupportedException e){
			return null;
		}
	}

	// set the Setup and Results data members
	public void setData( Setup s, Results r ){
		settings = s;
		results = r;
	}
}


/******************************************************************************
	The SummaryManager class holds up to five current and five deleted Summary
	objects.  This class provides the methods that allow Summaries to be 
	selected, unselected, deleted and undeleted by the Results and Plot pages.
 ******************************************************************************/  
class SummaryManager{
	private Summary[] current, deleted;
	private boolean[] selected;
	private final int MAXVALUES = 5;
	private int currentCount, deletedCount;

	// construction
	public SummaryManager(){
		currentCount = 0;
		deletedCount = 0;
		current = new Summary[MAXVALUES];
		deleted = new Summary[MAXVALUES];
		selected = new boolean[MAXVALUES];
   }

	// returns the number of summaries in the current array
	public int getCount(){
		return currentCount;
	}

	// returns true if the current array is full
	public boolean isFull(){
		return ( currentCount == MAXVALUES );
	}

	// returns true if there are any summaries in the deleted array
	public boolean beenDeleted(){
		return ( deletedCount > 0 );
	}

	// Adds a Summary object to the end of the current array. If the array
	// is full the summary in the first position is discarded and the other
	// summaries are shifted forward
	public void add( Summary s ){
		if( currentCount < MAXVALUES ){
 	   	current[currentCount] = s;
			currentCount++;
		}
		else{
			for(int i = 0; i < MAXVALUES -1 ; i++ )
				current[i] = current[i+1];
		  	current[MAXVALUES-1] = s;
		}
	}

	// Adds a Summary object to the end of the deleted array. If the array
	// is full the summary in the first position is discarded and the other
	// summaries are shifted forward
	public void addToDeleted( Summary s ){
		if( deletedCount < MAXVALUES ){
 	   	deleted[deletedCount] = s;
			deletedCount++;
		}
		else{
			for(int i = 0; i < MAXVALUES -1 ; i++ )
				deleted[i] = current[i+1];
		  	deleted[MAXVALUES-1] = s;
		}
	}

	// This method moves summaries marked as selected in the current array
	// to the deleted array. The method returns true for success, false if
	// there are no summaries to delete.
	public boolean delete(){
		if( currentCount == 0 )
			return false;
		Summary[] temp = new Summary[MAXVALUES];
		int count = 0;
		// save a copy of the summaries we wish to keep
		for( int i = 0; i < currentCount; i++ )
			if( !selected[i] ){
				temp[count] = current[i];
				count++;
			}
			else	// delete the others
				addToDeleted( (Summary)current[i].clone() );

		currentCount = count;
		// copy the saved summaries back into the array so there are no empty columns
		for( int i = 0; i < currentCount; i++ )
			current[i] = temp[i];
		return true;
	}

	// This method copies the last deleted summary back into the current array
	// if there is room. The method returns true for success, false otherwise.
	public boolean unDelete(){
		if( deletedCount > 0 && currentCount < MAXVALUES ){ 
			add((Summary)deleted[--deletedCount].clone());
			return true;
		}
		return false;
 	}

	// This method returns a reference to the summary stored at the specified index 
	public Summary getSummary( int index ){
		return current[index];
	}

	// This method marks the summary at the specified index as true if the b is true,
	// otherwise the summary is marked as unselected.
	public void setSelected( int index, boolean b ){
		selected[index] = b;
	}

	// returns true if the summary at the specified index is marked as selected
	public boolean isSelected( int index ){
		return selected[ index ];
	}

	// returns true if any of the summaries in the current index are selected
	public boolean isSelection(){
		for( int i = 0; i < currentCount; i++ )
			if( selected[i] )
				return true;
		return false;
	}

	// marks all summaries in the current index as unselected
	public void clearSelections(){
		for( int i = 0; i < MAXVALUES; i++ )
			selected[i] = false;
	}

	// empties the current and deleted arrays
	public void reset(){
		currentCount = 0;
		deletedCount = 0;
		clearSelections();
	}
}


/******************************************************************************
	The DisplayManager class provides the methods the algorithms use to produce
	changes in the execution panel display. This includes updating the
	statistics display, the progress pie chart, the hash function equation,
	the status string and the table graphics
 ******************************************************************************/  
class DisplayManager{
	// references to other system objects
	private HashCanvas hashCanvas;
	private HashStatus hashStatus;
	private ProgressCanvas progressCanvas;
	private Results results;
	private Common c;
	// references to the hash equation and statistics Labels
 	private Label numStoredLbl, hxLbl, homeAddressLbl, timeLbl, probesLbl,
   				  loadLbl, searchesLbl, unsearchesLbl;
	// statistics and hash equation variables
	private int input, hx, address, total, numLeft, mode, phase, pass;
	
	// construction	
	public DisplayManager( Component[] cmp, HashCanvas hc, ProgressCanvas pc ){
		numStoredLbl = (Label)cmp[0]; 
		hxLbl = (Label)cmp[1];
		homeAddressLbl = (Label)cmp[2];
		timeLbl = (Label)cmp[3];
		probesLbl = (Label)cmp[4];
		loadLbl = (Label)cmp[5];
		searchesLbl = (Label)cmp[6];
		unsearchesLbl = (Label)cmp[7];
		hashStatus = new HashStatus();
		hashCanvas = hc;
		hashCanvas.setHashStatus( hashStatus );
		progressCanvas = pc;
		pass = 0;
		c = new Common();
	}

	// This method sets up the initial display prior to the start of the demo.
	// hash - the algorithm
	// mode - store or retrieve
	// buckets - number of buckets in the table
	// total - the data size
	public void initialize( int hash, int mode, int buckets, int total ){
		
		hxLbl.setText( "mod " + buckets );
		
		timeLbl.setText( "00:00:00" );
		probesLbl.setText( "0.0" );
		loadLbl.setText( "0.0%" );
		searchesLbl.setText( "0" );
		unsearchesLbl.setText( "0" );
	 	progressCanvas.drawPie( total, total );
		this.total = total;
		clearInputAndAddress();
		setMode( mode );
		hashStatus.setStatus( HashStatus.READY );
		// show the Overflow label if there is an overflow table
		if( hash == c.CHAININGWITHOVERFLOW )
			hashStatus.setOverflow( true );
		else
			hashStatus.setOverflow( false );
	}

	// clears the address and input field in the hash equation display
	// ( address = input mod buckets )
	public void clearInputAndAddress(){
		numStoredLbl.setText( "" );
		homeAddressLbl.setText( "" );
	}

	// set the execution mode variable in the HashStatus object
	public void setMode( int mode ){
		this.mode = mode;
		hashStatus.setMode( mode );
	}

	// Set the pass variable in the HashStatus object.  This variable is used to 
	// indicate which bucket chaining storage pass is taking place
	public void setPass( int pass ){
		hashStatus.setPass( pass );
	}

	// Set the phase variable in the HashStatus object. This variable is used to
	// differetiate between a Store demo and the Store phase of a Retrieve demo
	public void setPhase( int phase ){
		this.phase = phase;
	}

	// Updates the status display of the HashStatus object. Valid integer values are the
	// static integer variables in the HashStatus class
	public void setStatus( int status ){
		if( mode == Common.STORE )
			hashStatus.setStatus( status );
		else if( mode == Common.RETRIEVE && phase == Common.RETRIEVE )
			hashStatus.setStatus( status );
		else{
			if( status == HashStatus.READY )
				hashStatus.setStatus( status );
			else if( status == HashStatus.QUICKLOAD )
				hashStatus.setStatus( status );
			else
				hashStatus.setStatus( HashStatus.LOAD );
		}
	}

	// set the class's Results data member
	public void setResults( Results results ){
		this.results = results;
	}

	// set the input value being stored or retrieved
	public void setInput( int input ){
		this.input = input;
	}

	// set the number of values remaining to store or retrieve
	public void setNumLeft( int numLeft ){
		this.numLeft = numLeft;
	}

	// decrement the number of values remaining to store or retrieve
	public void decNumLeft(){
		numLeft--;
	}

	// display the "mod buckets" portion of the hash equation
	public void setHx( int hx ){
		hxLbl.setText( " mod " + hx );
	}

	// set the home address result of the hash equation
	public void setHomeAddress( int ha ){
		address = ha;
	}

	// This method repaints the canvas that displays the hash table, overflow
	// table and HashStatus
	public void paintHash(){
		hashCanvas.repaint();
	}

	// update all the statistics labels and the progress pie chart to reflect
	// the current variable values
	public void updateStats(){
		numStoredLbl.setText( "" + input );
		homeAddressLbl.setText( "" + address );
		timeLbl.setText( results.getTime() );
		probesLbl.setText( "" + results.getProbes() );
		loadLbl.setText( "" + results.getLoad() + "%" );
		searchesLbl.setText( "" + results.getSuccess() );
		unsearchesLbl.setText( "" + results.getUnsuccess() );
 		updatePie();
	}

	// This method is used to update the display of the progress pie chart.
	// It is used with quickLoad.
	public void updatePie(){
		progressCanvas.drawPie( total, numLeft);
   }
}


/******************************************************************************
	The CodeManager class contains the pseudocode Strings for each algorithm
	and the methods to display them.
 ******************************************************************************/  
class CodeManager{
	// the List object which displays and highlights the lines of code
	private java.awt.List sourceLst;
	// the current algorithm and the execution mode (store or retrieve)
	private int algorithm, execMode;

	// the pseudocode
	private String[] linearStore = {
	"loc = X % numBuckets;                  ",
	"if( Table[loc] == EMPTY )              ",
	"   Table[loc] = X;                     ",
	"else{                                  ",
	"   do{                                 ",
	"      loc++;                           ",
	"   }while( Table[loc] == FULL )         ",
	"   H_Table[loc] = X;                   ",
	"}"};

	private String[] linearRetrieve = {
	"if( Table[loc] == X )             ",
	"   return FOUND;                  ",
	"else{                             ",
	"   while(Table[loc] != X &&       ", 
	"              Table[loc] != EMPTY)",
	"      loc++;                      ",
	"   if( [Table[loc] == X )         ",
	"      return FOUND;               ",
	"   else return NOT_FOUND;         ",
	"}"};
	
	private String[] quadraticStore = {
	"loc = X % numBuckets; i = 0;           ",
	"if( Table[loc] == EMPTY )              ",
	"   Table[loc] = X;                     ",
	"else{                                  ",
	"   do{                                 ",
	"      i = (i+1)*(i+1);                 ",
	"   }while( Table[loc+i] == FULL )      ",
	"   H_Table[loc] = X;                   ",
	"}"};

	private String[] quadraticRetrieve = {
	"if( Table[loc] == X )             ",
	"   return FOUND;                  ",
	"else{  i = 0;                     ",
	"   while(Table[loc+i] != X &&     ", 
	"            Table[loc+i] != EMPTY)",
	"      i = (i+1)*(i+1);            ",
	"   if( [Table[loc+i] == X )       ",
	"      return FOUND;               ",
	"   else return NOT_FOUND;         ",
	"}"};

	private String[] bucketStorePass1 = {
	"// first pass                     ",
	"loc = X % numBuckets;             ",
	"if( Table[loc] == EMPTY )         ",
	"   Table[loc] = X;                ",
	"else                              ",
	"   Temp[ count++ ] = X;           ",
	"}                                 ",
	"                                  ",
	"                                  "};

	private String[] bucketStorePass2 = {
	"// second pass                     ",
	"loc = X % numBuckets;              ",
	"found = false;                     ",
	"while( !found && moreData ){       ",
	"   if( Table[loc] == EMPTY )       ",
	"      Table[loc] = X; found = true;",
	"   else                            ",
	"      loc = Table[loc].link;       ",
	"}"};

	private String[] bucketRetrieve = {
	"if( Table[loc] == X )             ",
	"   return FOUND;                  ",
	"else{                             ",
	"   while(Table[loc] != X &&       ", 
	"         Table[loc].link != NULL) ",
	"      loc = Table[loc].link;      ",
	"   if( Table[loc] == X )          ",
	"      return FOUND;               ",
	"   else return NOT_FOUND;         ",
	"}"};

	private String[] overflowStore = {
	"loc = X % numBuckets;             ",
	"if( Table[loc] == EMPTY )         ",
	"   Table[loc] = X;                ",
	"else{  // use OverFlow            ",
	"   while( OvFlow[pos] == FULL )   ",
	"      pos = nextPos();            ",
	"   OvFlow[pos] = X;               ",
	"   UpdateLinks();                 ",
	"}"};

	private String[] overflowRetrieve = {
	"if( Table[loc] == X )             ",
	"   return FOUND;                  ",
	"else if( Table[loc].link != NULL){",
	"   while(OvFlow[pos] != X &&      ", 
	"         OvFlow[pos].link != NULL)",
	"      pos = OvFlow[pos].link;     ",
	"   if( OvFlow[pos] == X )         ",
	"      return FOUND;               ",
	"}                                 ",
	"return NOT_FOUND;                 "};

	private String[] chainingStore = {
	"loc = X % numBuckets;             ",
	"if( Table[loc] == EMPTY )         ",
	"   Table[loc] = X;                ",
	"else{  // add Node                ",
	"   InsertNodeInFront( X );        ",
	"}"};

	private String[] chainingRetrieve = {
	"if( Table[loc] == X )             ",
	"   return FOUND;                  ",
	"else if(Table[loc].link != NULL){ ",
	"   while(Node.Data != X &&        ", 
	"              Node.Next != NULL)  ",
	"      Node = Node.Next;           ",
	"   if( Node.Data == X )           ",
	"      return FOUND;               ",
	"}                                 ",
	"return NOT_FOUND;                 "};
	 	
	// construction
	public CodeManager( java.awt.List source ){
		sourceLst = source;
	}

	// this method displays the pseudocode Strings corresponding to 
	// the algorithm and execution mode arguments
	public void setCode( int alg, int mode ){
		algorithm = alg;
		execMode = mode;
		if( alg == Common.LINEARPROBING ){
			if( mode == Common.STORE ) 
				setLinearStore();
			else
				setLinearRetrieve();
		}
		else if( alg == Common.QUADRATICPROBING ){
			if( mode == Common.STORE ) 
				setQuadraticStore();
			else
				setQuadraticRetrieve();
		}
		else if( alg == Common.BUCKETCHAINING ){
			if( mode == Common.STORE ) 
				setBucketStore();
			else
				setBucketRetrieve();
		}
		else if( alg == Common.CHAININGWITHOVERFLOW ){
			if( mode == Common.STORE ) 
				setOverflowStore();
			else
				setOverflowRetrieve();
		}
		else if( alg == Common.LINKEDLISTCHAINING ){
			if( mode == Common.STORE ) 
				setChainingStore();
			else
				setChainingRetrieve();
		}
	}

	/********* code loading methods ********************************************/
	 
	public void setLinearStore(){
		sourceLst.removeAll();
		for( int i = 0; i < linearStore.length; i++ )
			sourceLst.add( linearStore[i] );
	}

	public void setLinearRetrieve(){
		sourceLst.removeAll();
		for( int i = 0; i < linearRetrieve.length; i++ )
			sourceLst.add( linearRetrieve[i] );
	}

	public void setQuadraticStore(){
		sourceLst.removeAll();
		for( int i = 0; i < quadraticStore.length; i++ )
			sourceLst.add( quadraticStore[i] );
	}

	public void setQuadraticRetrieve(){
		sourceLst.removeAll();
		for( int i = 0; i < quadraticRetrieve.length; i++ )
			sourceLst.add( quadraticRetrieve[i] );
	}

	public void setBucketStore(){
		sourceLst.removeAll();
		for( int i = 0; i < bucketStorePass1.length; i++ )
			sourceLst.add( bucketStorePass1[i] );
	}

	public void setBucketStore2(){
		sourceLst.removeAll();
		for( int i = 0; i < bucketStorePass2.length; i++ )
			sourceLst.add( bucketStorePass2[i] );
	}

	public void setPass2( ){
		if( algorithm == Common.BUCKETCHAINING ){
			if( execMode == Common.STORE )
				setBucketStore2();
		}
	}

	public void setBucketRetrieve(){
		sourceLst.removeAll();
		for( int i = 0; i < bucketRetrieve.length; i++ )
			sourceLst.add( bucketRetrieve[i] );
	}

	public void setOverflowStore(){
		sourceLst.removeAll();
		for( int i = 0; i < overflowStore.length; i++ )
			sourceLst.add( overflowStore[i] );
	}

	public void setOverflowRetrieve(){   
		sourceLst.removeAll();
		for( int i = 0; i < overflowRetrieve.length; i++ )
			sourceLst.add( overflowRetrieve[i] );
	}

	public void setChainingStore(){
		sourceLst.removeAll();
		for( int i = 0; i < chainingStore.length; i++ )
			sourceLst.add( chainingStore[i] );
	}

	public void setChainingRetrieve(){
		sourceLst.removeAll();
		for( int i = 0; i < chainingRetrieve.length; i++ )
			sourceLst.add( chainingRetrieve[i] );
	}

	// this method highlights the specified line of the currently displayed 
	// pseudo code
	public void selectLine( int lineNumber ){
		sourceLst.select( lineNumber );
	}

	// this method clears all highlighted lines
	public void clearSelections(){
		int lines = 0;
		if( algorithm == Common.LINEARPROBING ){
			if( execMode == Common.STORE ) 
				lines = linearStore.length;	
			else
				lines = linearRetrieve.length;	
		}
		if( algorithm == Common.QUADRATICPROBING ){
			if( execMode == Common.STORE ) 
				lines = quadraticStore.length;	
			else
				lines = quadraticRetrieve.length;	
		}
		if( algorithm == Common.CHAININGWITHOVERFLOW ){
			if( execMode == Common.STORE ) 
				lines = overflowStore.length;	
			else
				lines = overflowRetrieve.length;	
		}
		if( algorithm == Common.BUCKETCHAINING ){
			if( execMode == Common.STORE ) 
				lines = bucketStorePass1.length;	
			else
				lines = bucketRetrieve.length;	
		}
		if( algorithm == Common.LINKEDLISTCHAINING ){
			if( execMode == Common.STORE ) 
				lines = chainingStore.length;	
			else
				lines = chainingRetrieve.length;	
		}
		for( int i = 0; i < lines; i++ )
			sourceLst.deselect(i);
	}
}


/******************************************************************************
	The NodeSprite class is the abstract parent class of the Slot, Link and List
	objects that make up the buckets of the hash and overflow tables.
   It defines the attributes and methods common to	each of classes that inherit
   from it.
******************************************************************************/ 
abstract class NodeSprite{
	// combinations of these values define the possible states of the Sprite
	static final int EMPTY = 0;
	static final int FULL = 1;
	static final int PROBED = 10;
	protected int state,				// the current Sprite state
 						value,			// the number stored in the Sprite
					   xpos, ypos,	   // the coordinates of the upper left corner
					 	height, width; // the sprite dimensions
	protected boolean visible;
	// contains commonly used constants, fonts, strings, etc.
	protected Common c;
	// the colors of the Sprite's background and border
	protected Color backColor, borderColor;


	// this method should remove any value and set the state to EMPTY
	abstract void clear();
	// this method inserts a value in the Sprite 
	abstract void setValue( int val );

	// clear the Sprite and don't paint it
	public void reset(){
		clear();
		visible = false;
	}

	// set the position coordinates
	public void setPosition( int x, int y ){
		xpos = x;
		ypos = y;
   }

	// return the height
	public int getHeight(){
		return height;
	}

	// return the width
	public int getWidth(){
		return width;
	}

	// set the visible data member, the member will not be painted if visible
	// is false
	public void setVisible( boolean b ){
		visible = b;
	}

	// return the value stored in the node
	public int getValue(){
   	return value;	
	}
}
 	

/******************************************************************************
	The SlotSprite class defines the objects that make up the individual slots
	within the hash table.  The methods change the colors of the Sprite to 
   indicate its state
******************************************************************************/ 
class SlotSprite extends NodeSprite{
	
	// construction
	public SlotSprite(){
		state = EMPTY;
		value = -1;
		visible = false;
		/*** PLR ***/
		/* height = 12; */
		height = 18;
		width = 35;
		c = new Common();
		backColor = Color.lightGray;
		borderColor = Color.black;
	}

	// remove any value and set the state to EMPTY
	public void clear(){
		state = EMPTY;
		value = -1;
		backColor = Color.lightGray;
		borderColor = Color.black;
	}

	// insert a number into the slot and update its state
	public void setValue( int val ){
		value = val;
		state += FULL;
		backColor = Color.magenta;
	}

	// Returns true if num is present in the slot.  The background color is
	// set to green to indicate a successful search
	public boolean searchValue( int num ){
		if( value == num  ){
			backColor = Color.green;
   		return true;
		}
		return false;	
	}

	// update the state to indicate the slot has been searched
	public void setProbed(){
		state += PROBED;
		borderColor = Color.yellow;
	}

	// restore the slot to it's appearance prior to being probed
	public void setNormal(){
		state -= PROBED;
		borderColor = Color.black;
		if( backColor == Color.green )
			backColor = Color.magenta;
	}

	// if the slot has been probed restore its normal appearance
	public void clearProbe(){
		if( state >= 10 )
			setNormal();
   }
		
	// draw the Sprite if it should be visible
	public void paint( Graphics g ){
		if( visible ){
			g.setColor( backColor );
			g.fillRect( xpos, ypos, width, height );
			g.setColor( borderColor );
			g.drawRect( xpos, ypos, width-1, height-1);
			// if the state is not EMPTY, draw the value
			if( ( state & 1 )== 1 ){
				g.setColor( Color.black );
				g.setFont( c.font10b );
				// center the value in the rectangle
				FontMetrics fm = g.getFontMetrics();
				int valWidth = fm.stringWidth( "" + value );
				int pos = (width-valWidth)/2 + xpos;
				g.drawString( "" + value, pos, ypos + height - 4  );
			}
		}
	}
}


/******************************************************************************
	The ListSprite class defines the objects that make up the individual nodes
	of the linked lists the hash table (for linked list chaining).
 	The methods allow the Sprite to be linked to other ListSprites, and animated
	They also change the colors of the Sprite to indicate its state
******************************************************************************/ 
class ListSprite extends NodeSprite{
	private Color dashColor,			// the color of the dashed arrow
 					  myOrange;	   		// custom background color
	private int boxWidth,				// the width of the rectangle portion of the Sprite
 					shaft = 9, index,	   // the length of the arrow pointer
				   buckets, slots,		// the size of the hash table
				 	pos, maxPos;			// the current position in the chain and the position
												// where the Sprite should be drawn as a dashed arrow
	private ListSprite nextSprite;	// pointer to the next node in the chain

	// construction
	public ListSprite(){
		state = EMPTY;
		value = 999;
		visible = false;
		/*** PLR ***/
		/* height = 12; */
		height = 18;
		boxWidth = 31;
		width = boxWidth + shaft;
		c = new Common();
		myOrange = new Color( 255, 127, 0 );
		backColor = myOrange;
		borderColor = Color.black;
		dashColor = Color.white;
		nextSprite = null;
	}

	// set the bucket address, position in the chain and hash table size
	public void setData( int index, int pos, int buckets, int slots ){
		this.index = index;
		this.pos = pos;
		this.buckets = buckets;
		this.slots = slots;
	}

	// Calculate the value of maxPos. As nodes are added to the chain the Sprite
	// will be moved to the right. Eventually the Sprite could overlap with the 
	// buckets in the next column or go off the canvas.  Instead of doing that
	// these nodes are made invisible and their presence is indicated by a dashed
	// arrow on the last visible node in the chain.  The last visible node is at
	// maxPos.
	public void calcMaxPos(){
   	maxPos = 6 - slots;
		if(( buckets <= index + 25)&& index < 25)
			maxPos +=6;
	}

	// a node was inserted prior to this Sprite, update its position in the chain
	public void incPosition(){
		pos++;
	}

	// set the pointer to the next node
	public void setPointer( ListSprite next ){
		nextSprite = next;
	}

	// return the node being pointed to
	public ListSprite getPointer(){
		return nextSprite;
	}

	// move the Sprite to the right 2 pixels
	public void moveRight(){ 
		xpos +=2;
	}

	// remove any value and set the state to EMPTY
	public void clear(){
		state = EMPTY;
		value = -1;
		borderColor = Color.black;
	}

	// insert a number into the node and update its state
	public void setValue( int val ){
		value = val;
		state += FULL;
	}

	// update the state to indicate the node has been searched
	public void setProbed(){
		state += PROBED;
		borderColor = Color.yellow;
		dashColor = Color.yellow;
	}

	// this method is used to set the color of dashed arrow at maxPos green
	// to indicate a successful search of an invisible node
	public void setFound(){
		state += PROBED;
		borderColor = Color.yellow;
		dashColor = Color.green;
	}

	// restore the node to it's appearance prior to being probed
	public void setNormal(){
		state -= PROBED;
		borderColor = Color.black;
		backColor = myOrange;
		dashColor = Color.white;
	}

	// Returns true if num is present in the node.  The background color is
	// set to green to indicate a successful search
	public boolean searchValue( int num ){
		setProbed();
		if( value == num  ){
			backColor = Color.green;
   		return true;
		}
		return false;
	}

	// if the slot has been probed restore its normal appearance
	public void clearProbe(){
		if( state >= 10 )
			setNormal();
   }

	// this method draws an arrow to the left of the rectangle which displays
	// the data
	public void drawArrow( Graphics g ){
		int xtip = xpos + shaft;
		int ytip = ypos + height/2;
		g.drawLine( xpos, ytip, xtip, ytip );
		Polygon head = new Polygon();
		head.addPoint( xtip, ytip );
		head.addPoint( xtip-3, ytip-3 );
		head.addPoint( xtip-3, ytip+3 );
		g.fillPolygon( head );
	}

	// this method draws an arrow with a dashed shaft to indicate there
	// are more nodes in the chain that aren't shown
	public void drawDashedArrow( Graphics g ){
		g.setColor( dashColor );
		int xtip = xpos + shaft;
		int ytip = ypos + height/2;
		g.drawLine( xpos, ytip, xpos+1, ytip );
		g.drawLine( xpos+4, ytip, xpos+5, ytip );
		g.drawLine( xpos+8, ytip, xpos+9, ytip );
	
		Polygon head = new Polygon();
		head.addPoint( xtip, ytip );
		head.addPoint( xtip-3, ytip-3 );
		head.addPoint( xtip-3, ytip+3 );
		g.fillPolygon( head );
	}
		
	// If the node position is less than maxPos draw it normally.
	// If the node is at maxPos draw it as a dashed arrow.
	public void paint( Graphics g ){
		if( visible && pos <= maxPos ){
			if( pos == maxPos ){
				drawDashedArrow( g );
			}
			else{
				g.setColor( backColor );
				g.fillRect( xpos+shaft, ypos, boxWidth, height );
				g.setColor( Color.white );
				drawArrow( g );
				g.setColor( borderColor );
				g.drawRect( xpos+shaft, ypos, boxWidth-1, height-1);
				g.setColor( Color.black );
			
				g.setFont( c.font10b );
				FontMetrics fm = g.getFontMetrics();
				int valWidth = fm.stringWidth( "" + value );
				int pos = (boxWidth-valWidth)/2 + xpos+shaft;
				g.drawString( "" + value, pos, ypos + height - 3  );
			}
			
		}
	}
}


/******************************************************************************
	The LinkSprite class defines the bucket link objects that seen with bucket
	chaining and chaining with separate overflow.
 	The methods allow the Sprite to be linked to other bucket addresses and
	change the colors of the Sprite to indicate its state
******************************************************************************/   
class LinkSprite extends NodeSprite{
	
	// construction
	public LinkSprite(){
		state = EMPTY;
		value = -1;
		visible = false;
		height = 14;
		width = 35;
		c = new Common();
		backColor = Color.cyan;
		borderColor = Color.black;
	}

	// remove any value and set the state to EMPTY
	public void clear(){
		state = EMPTY;
		value = -1;
		borderColor = Color.black;
	}

	// insert a link value into the Sprite and update its state
	public void setValue( int val ){
		value = val;
		state += FULL;
	}

	// update the state to indicate the bucket has been searched
	public void setProbed(){
		state += PROBED;
		borderColor = Color.yellow;
	}

	// restore the Sprite to it's appearance prior to being probed
	public void setNormal(){
		state -= PROBED;
		borderColor = Color.black;
		backColor = Color.cyan;
	}

	// if the Sprite has been probed restore its normal appearance
	public void clearProbe(){
		if( state >= 10 )
			setNormal();
   }
		
	// Draw the Sprite.  If there is no link value (link == -1 ), draw a 
	// diagonal line through the rectangle
	public void paint( Graphics g ){
		if( visible ){
			g.setColor( backColor );
			g.fillRect( xpos, ypos, width, height );
			g.setColor( borderColor );
			g.drawRect( xpos, ypos, width-1, height-1);
			g.setColor( Color.black );
			if( ( state & 1 )== 1 ){
				g.setFont( c.font10b );
				FontMetrics fm = g.getFontMetrics();
				int valWidth = fm.stringWidth( "" + value );
				int pos = (width-valWidth)/2 + xpos;
				g.drawString( "" + value, pos, ypos + height - 4  );
			}
			else
				g.drawLine( xpos, ypos, xpos + width, ypos + height );
		}
	}
}


/******************************************************************************
	The BucketSprite class defines the buckets that make up the hash and
 	overflow tables.  Each BucketSprite is composed of 3 SlotSprites,
	1 LinkSprite and a dynamically created linked list of ListSprites.
 	The methods allow access and positioning of these Sprites. 
******************************************************************************/  
class BucketSprite{
	// component Sprites
	private SlotSprite[] slots;
	private LinkSprite link;
	private ListSprite firstNode;			   // pointer to front of list
	private Common c;
	private int index, 							// address in the hash table
 					numBuckets,						// number of buckets in table
 					xpos, ypos,						// coordinates of upper left corner
				   numSlots, listLength,	   // component counts
 					maxIndexWidth;   				// width of index text String
	private boolean visible,
 						 hasLink;					// if true, show the LinkSprite
	
	// construction
	public BucketSprite( int index ){
		this.index = index;
		visible = false;
		hasLink = false;
		c = new Common();
		slots = new SlotSprite[3];
		for( int i = 0; i < 3; i++ )
			slots[i] = new SlotSprite();
		link = new LinkSprite();
		firstNode = new ListSprite();
		firstNode.setVisible( false );
		listLength = 0;
		maxIndexWidth = 18; 					// temporary value until paint is called
	}

	// this method resets all the Sprites (makes them invisible)
	// and deletes the list nodes
	public void reset(){
		for( int i = 0; i < 3; i++ )
			slots[i].reset();
		link.reset();
		hasLink = false;
		deleteList();
		listLength = 0;
	}

	// remove all the node from the list so they may be garbage collected
	public void deleteList(){
		for(int i = 0; i < listLength; i++ )
			dequeue();
	}

	// delete a node from the front of the list
	public void dequeue(){
		ListSprite tempNode = firstNode.getPointer();
		firstNode.setPointer(firstNode.getPointer());
		tempNode = null;
	}

	// move all nodes in the link list 2 pixels to the right
	public void moveList(){
		ListSprite tempNode = firstNode.getPointer();
		tempNode.moveRight();
		while( tempNode.getPointer() != null ){
			tempNode = tempNode.getPointer();
			tempNode.moveRight();
		}
	}

	// returns the number of nodes in the linked list
	public int getListLength(){
		return listLength;
	}

	// Display the first node in the list.  This is used after an insertion at the front
	// of the list
	public void showNode(){
		firstNode.getPointer().setVisible(true);
	}

	// Clear the link and slot Sprites and delete the linked list. An empty bucket
	// is displayed.
	public void clear(){
		for( int i = 0; i < 3; i++ )
			slots[i].clear();
		link.clear();
		deleteList();
		listLength = 0;
	}

	// Set the positions of the Sprites in the bucket. A call to setNumSlots must be
	// done first.
	public void setPosition( int x, int y ){
		xpos = x;
		ypos = y;
		for( int i = 0; i < 3; i++ )
			slots[i].setPosition( xpos + i*slots[i].getWidth() + maxIndexWidth, y );
		link.setPosition( xpos + numSlots*slots[0].getWidth() + maxIndexWidth, y );
   }

	// returns the height of the bucket
	public int getHeight(){
		return slots[0].getHeight();
	}

	// returns the width of the bucket (not including the linked list)
	public int getWidth(){
		int width = numSlots*slots[0].getWidth() + maxIndexWidth;
		if( hasLink )
			width += slots[0].getWidth();
		return width;
	}

	// sets the visibility of the buckets
	public void setVisible( boolean b ){
		visible = b;
	}

	// set the number of slots to display
	public void setNumSlots( int num ){
		numSlots = num;
		for( int i = 0; i < numSlots; i++ )
			slots[i].setVisible( true );
	}

	// Set the number of buckets in the table.  This is needed to determine the allowable
   // list length
	public void setNumBuckets( int num ){
		numBuckets = num;
	}

	// set the bucket to display the LinkSprite
	public void setLinked(){
		link.setVisible( true );
		hasLink = true;
	}

	// inserts a node at the front of the linked list
	public void insertNode( int val ){
		listLength++;
		ListSprite newNode = new ListSprite();
		if( listLength > 1 ){
			// increment the positions of the other nodes
 	   	incListPositions();
			newNode.setPointer( firstNode.getPointer() );
		}
		firstNode.setPointer(newNode);
		newNode.setValue( val );
		newNode.setPosition( xpos + numSlots*slots[0].getWidth() + maxIndexWidth, ypos+1 );
		newNode.setData( index, 0, numBuckets, numSlots );
		newNode.calcMaxPos();
	}

	// paint the nodes in the linked list
	public void paintList( Graphics g ){
		ListSprite tempNode = firstNode.getPointer();
		tempNode.paint(g);
		while( tempNode.getPointer() != null ){
			tempNode = tempNode.getPointer();
			tempNode.paint(g );
		}
	}

	// Increment the position variables of the nodes in the list. This is done
	// after a node is inserted at the front of the list
	public void incListPositions(){
		ListSprite tempNode = firstNode.getPointer();
		tempNode.incPosition();
		while( tempNode.getPointer() != null ){
			tempNode = tempNode.getPointer();
			tempNode.incPosition();
		}
	}
			
	// Inserts a value in the first empty slot in the bucket. If all slots are full
	// the method returns false
	public boolean setValue( int val ){
		for( int i = 0; i < numSlots; i++ ){
			if( slots[i].getValue() == -1 ){
				slots[i].setValue( val );
				return true;
			}
		}
		return false;
	}

	// This method returns true if one of the slots in the bucket contains the value
	// argument
	public boolean findValue( int value ){
		setProbed();
   	for( int i = 0; i < numSlots; i++ ){
			if( slots[i].searchValue( value ) ) 
				return true;	 
		}
		return false;
	}

	// This method determines if the value is present at the specified node
   // (list position).  If the node is not visible the dashed arrow's color
	// is adjusted to reflect probes and successful searches
	public boolean searchNode( int node, int value ){
		ListSprite tempNode = firstNode.getPointer();
		for( int i = 0; i < node; i++ )
			tempNode = tempNode.getPointer();
	  	boolean result = tempNode.searchValue( value ); 

		int maxPos = 6 - numSlots;
		if(( numBuckets <= index + 25)&& index < 25)
		maxPos +=6;

		// node is not being displayed, adjust color of dashed arrow at maxPos
		if( node >= maxPos ){
			tempNode = firstNode.getPointer();
			for( int i = 0; i <maxPos; i++ )
				tempNode = tempNode.getPointer();
		if( result )
			tempNode.setFound();
		else
	  		tempNode.setProbed();
 	   }

		return result; 

	}

	// set the address value in the LinkSprite
	public void setLinkValue( int val ){
		link.setValue( val );
	}

	// returns the address value in the LinkSprite
	public int getLinkValue(){
		return link.getValue();
	}

	// returns true if all slots in the bucket are empty
	public boolean isEmpty(){
		setProbed();
		for( int i = 0; i < numSlots; i++ ){
			if( slots[i].getValue() != -1 ){
				return false;
			}
		}
		return true;
	}

	// returns true if any slot in the bucket is empty
	public boolean hasEmptySlot(){
		setProbed();  
		for( int i = 0; i < numSlots; i++ ){
			if( slots[i].getValue() == -1 ){
				return true;
			}
		}
		return false;
	}

	// highlight all SlotSprite and LinkSprite borders to indicate a probe is taking
	// place
	public void setProbed(){
		for( int i = 0; i < numSlots; i++ ) 
			slots[i].setProbed();
		if( hasLink )
			link.setProbed();
	}
	
	// clear all the probe indicators in the bucket
	public void clearProbe(){
		for( int i = 0; i < numSlots; i++ ) 
			slots[i].clearProbe();
		if( hasLink )
			link.clearProbe();
		if( listLength > 0 ){
			ListSprite tempNode = firstNode.getPointer();
			tempNode.clearProbe();
			while( tempNode.getPointer() != null ){
				tempNode = tempNode.getPointer();
				tempNode.clearProbe();
			}
		}
	}

	// draw the Sprites and the address text String
	public void paint( Graphics g ){
		if( visible ){
			for( int i = 0; i < numSlots; i++ )
				slots[i].paint( g );
			if( hasLink )
				link.paint( g );
			if( listLength > 0 )
				paintList(g);
			g.setColor( Color.white );
			g.setFont( c.font10b );
			FontMetrics fm = g.getFontMetrics();
			maxIndexWidth = fm.stringWidth( "888" );
			int indexWidth = fm.stringWidth( "" + index );
			int pos;
			if( index < 100 )
				pos = xpos + maxIndexWidth - indexWidth-4;
			else
				pos = xpos + maxIndexWidth - indexWidth-2;
			int height = slots[0].getHeight();
			g.drawString( "" + index, pos, ypos + height - 5  );	
		}
	}
}


/******************************************************************************
	The HashTable class defines the hash table object.  The table consists
	of 300 BucketSprites. Methods are provided to position the Sprites and
	to insert and search for values at specific addresses. 
******************************************************************************/  
class HashTable{
	final int MAXSIZE = 300;									// maximum size of table
	public static final int TABLE = 0, OVERFLOW = 1;	// the two possible table types
	// buckets
	private BucketSprite bucketSprite[];
	// references to other system objects
	private HashApplet parent;
	private Common c;
	private int type,									// hash table or overflow table
					buckets, slots,					// hash table dimensions
				 	overFlow,							// number of buckets for overflow
				 	canvasWidth, canvasHeight;	   // dimensions of display canvas
   
	// construction
	public HashTable( HashApplet parent, int type ){
		this.parent = parent;
		this.type = type;
		bucketSprite = new BucketSprite[MAXSIZE];
		for( int i = 0; i < MAXSIZE; i++ )
			bucketSprite[i] = new BucketSprite( i );
		c = new Common();
	}

	// set the dimensions of the display canvas
	public void setLimits( HashCanvas hc ){
		Dimension d = hc.getSize();
		canvasWidth = d.width;
		canvasHeight = d.height;
   }

	// set the dimensiions of the table 
	public void setSize( int buckets, int slots ){
		this.buckets = buckets;
		this.slots = slots;

		for( int i = 0; i < buckets; i++ ){
			bucketSprite[i].setVisible( true );
			bucketSprite[i].setNumSlots( slots );
		}
	}

  // position the bucketsprites
  public void positionElements( Setup settings ){
		int topMargin = 20;
 		int maxColumns;
		int alg = settings.getAlgorithm();
		buckets = settings.getBuckets();
		slots = settings.getSlots();

		// make the previous table display invisible
		reset();

		// this is an overflow table, position it near the bottom and show the LinkSprites
		if( type == OVERFLOW ){
			overFlow = settings.getOverflow();
			for( int i = 0; i < overFlow; i++ ){
				bucketSprite[i].setNumSlots( 1 );
				bucketSprite[i].setLinked();
				bucketSprite[i].setVisible( true );
			}
			maxColumns = 5;
			// calculate the distance between columns
			int gap = (canvasWidth - maxColumns*bucketSprite[0].getWidth())/
																							(maxColumns + 1);
			int top = topMargin + 17*bucketSprite[0].getHeight();
			int column, leftmargin;
			for( int i = 0; i < overFlow; i++ ){
				column = 1 + i/8;
				leftmargin = column*gap + (column - 1)*bucketSprite[i].getWidth(); 
				bucketSprite[i].setPosition( leftmargin, 
															(i%8)*bucketSprite[i].getHeight() + top );
			}
		}
		// this is the main hash table
		else{
			for( int i = 0; i < buckets; i++ ){
				bucketSprite[i].setNumSlots( slots );
				// show the LinkSprites for bucket chaining or overflow chaining
				if( alg == c.BUCKETCHAINING || alg == c.CHAININGWITHOVERFLOW )
					bucketSprite[i].setLinked();
				bucketSprite[i].setVisible( true );
			}
			// These algorithms don't use links. Determine the number of maximum number of
			// columns displayed based on the number of slots per bucket.
			if( alg == c.LINEARPROBING || alg == c.QUADRATICPROBING ){
				if( slots == 1)
					maxColumns = 8;
				else if( slots == 2 )
					maxColumns = 5;
				else
					maxColumns = 4;
				int gap = (canvasWidth - maxColumns*bucketSprite[0].getWidth())/
																								(maxColumns + 1);
			
				int column, leftmargin;
				for( int i = 0; i < buckets; i++ ){
					column = 1 + i/25;
					leftmargin = column*gap + (column - 1)*bucketSprite[i].getWidth(); 
					bucketSprite[i].setPosition( leftmargin,
				 									(i%25)*bucketSprite[i].getHeight() + topMargin );
				}	
			}
			// This algorithm uses links. The buckets are wider so we use fewer columns.
			else if( alg == c.BUCKETCHAINING ){
				if( slots == 1)
					maxColumns = 5;
				else if( slots == 2 )
					maxColumns = 4;
				else
					maxColumns = 3;
				int gap = (canvasWidth - maxColumns*bucketSprite[0].getWidth())/
																								(maxColumns + 1);

				int column, leftmargin;
				for( int i = 0; i < buckets; i++ ){
					column = 1 + i/25;
					leftmargin = column*gap + (column - 1)*bucketSprite[i].getWidth(); 
					bucketSprite[i].setPosition( leftmargin,
				 									(i%25)*bucketSprite[i].getHeight() + topMargin );
				}
			}
			// This algorithm uses links. The buckets are wider so we use fewer columns.
		 	// Also fewer rows are allowed to leave room for the overflow table
			else if( alg == c.CHAININGWITHOVERFLOW && type != OVERFLOW){
				if( slots == 1)
					maxColumns = 5;
				else if( slots == 2 )
					maxColumns = 4;
				else
					maxColumns = 3;
				int gap = (canvasWidth - maxColumns*bucketSprite[0].getWidth())/
																								(maxColumns + 1);

				int column, leftmargin;
				for( int i = 0; i < buckets; i++ ){
					column = 1 + i/15;
					leftmargin = column*gap + (column - 1)*bucketSprite[i].getWidth(); 
					bucketSprite[i].setPosition( leftmargin,
				 									(i%15)*bucketSprite[i].getHeight() + topMargin );
				}
			}
			// only two columns are used to leave room to display the linked lists
			else if( alg == c.LINKEDLISTCHAINING ){
				int gap = 20;

				int column, leftmargin;
				for( int i = 0; i < buckets; i++ ){
					if( i < 25 )
						leftmargin = gap;
 				   else
						leftmargin = canvasWidth/2 + gap;
					bucketSprite[i].setPosition( leftmargin,
				 									(i%25)*bucketSprite[i].getHeight() + topMargin );
					bucketSprite[i].setNumBuckets( buckets );
				}
			}
		}
	}

	// Inserts "value" at the bucket address specified by "index". The method returns
	// true if the insert was successful
	public boolean insert( int index, int value ){
		return bucketSprite[index].setValue( value );
	}

	// Inserts "value" in a node at the front of the link list at the bucket address
 	// specified by "index". 
	public void insertNode( int index, int value ){
		bucketSprite[index].insertNode( value );
	}

	// Set the node at the front of the linked list at the bucket address specified
	// by "index" visible
	public void showNode( int index ){
		bucketSprite[index].showNode();
	}

	// shift all nodes in the linked list at the bucket address specified by "index"
	// right 2 pixels
	public void moveList( int index ){
		bucketSprite[index].moveList();
	}

	// returns the length of the linked list at the bucket address specified by "index"
	public int getListLength( int index ){
		return bucketSprite[index].getListLength();
	}
		
	// returns the true if the value is present in one of the slots of the bucket
 	// specified by  the address, "index"
	public boolean search( int index, int value ){
		return bucketSprite[index].findValue( value );
	}

	// returns the true if the value is present in specified link list node of the bucket
 	// specified by  the address, "index"
	public boolean searchNode( int address, int node, int value ){
   	return bucketSprite[address].searchNode( node, value );
	}

	// returns true if the bucket at address, "index" has an empty slot
	public boolean hasEmptySlot( int index ){
		return bucketSprite[index].hasEmptySlot();
	}

	// returns true if the bucket at address, "index" is completely empty
	public boolean isEmpty( int index ){
		return bucketSprite[index].isEmpty();
	}

	// returns the value of link node at bucket address, "index"
	public int getLinkValue( int index ){
		return bucketSprite[index].getLinkValue();
	}

	// sets the value of link node at bucket address, "index"
	public void setLinkValue( int index, int value ){
		bucketSprite[index].setLinkValue( value );
	}

	// resets all buckets to empty, invisible and no linked-list
	public void reset(){
	 	overFlow = 0;
		for( int i = 0; i < MAXSIZE; i++ )
			bucketSprite[i].reset();
	}

	// restores the table to an empty state
	public void clear(){
		for( int i = 0; i < MAXSIZE; i++ )
			bucketSprite[i].clear();
	}

	// clears any probe highlighting in the table
	public void clearProbes(){
		int count;
		if( type == OVERFLOW )
			count = overFlow;
 	   else 
			count = buckets;
		for( int i = 0; i < count; i++ )
			bucketSprite[i].clearProbe();
	}

	// draws the hash or overflow table
	public void paint( Graphics g ){
		int count;
		if( type == OVERFLOW )
			count = overFlow;
 	   else 
			count = buckets;
		for( int i = 0; i < count; i++ )
			bucketSprite[i].paint( g );
	}
}


/******************************************************************************
	The HashStatus class defines the text Strings displayed above the hash and
	overflow table. These Strings indicate to the user what the algorithm is
	currently doing or if a search was successful or not
******************************************************************************/  
class HashStatus{
	private String modeStr,			// String which displays the execution mode
 						statusStr;	   // String which displays the status
	// the possible status states
	public static final int LOAD = 0, READY = 1, DONE = 2, SEARCH = 3, FOUND = 4,
 								   NOT_FOUND = 5, LOADED = 6, QUICKLOAD = 7;

	private int xpos, ypos, 		// the position the text starts at
 					pass;				   // normally 0, 1 or 2 for bucket chaining storage passes
 	private boolean overFlow;

	// construction
	public HashStatus(){
		xpos = 20;
		ypos = 15;
		modeStr = "Store";
		statusStr = "Ready";
		pass = 0;
	}

	// set the start position of the text
	public void setPosition( int x, int y ){
		xpos = x;
		ypos = y;
	}

	// sets the mode String value
	public void setMode( int mode ){
		if( mode == Common.STORE )
			modeStr = "Store";
		else
			modeStr = "Retrieve";
	}

	// set the pass value (normally 0, it's 1 or 2 for bucket chaining storage passes)
	public void setPass( int pass ){
		this.pass = pass;
	}
 
	// true if there is an overflow table displayed
	public void setOverflow( boolean b ){
		overFlow = b;
	}

	// set the status String value
	public void setStatus( int status ){
		switch( status ){
			case LOAD:
				statusStr = "Loading...";
			break;
			case READY:
				statusStr = "Ready";
			break;
			case DONE:
				statusStr = "Done";
			break;
			case SEARCH:
				statusStr = "Searching...";
			break;
			case FOUND:
				statusStr = "Found";
			break;
			case NOT_FOUND:
				statusStr = "Not found";
			break;
			case LOADED:
				statusStr = "Loaded";
			break;
			case QUICKLOAD:
				statusStr = "Quick Load";
			break;
		}
		// if we are doing bucket chaining storage, also indicate which pass we are on
		if( pass == 1 )
			statusStr = "Pass 1 - " + statusStr;
		else if( pass == 2 )
			statusStr = "Pass 2 - " + statusStr;
		
	}

	// draw the String, if a overflow table is present put a label above it
	public void paint( Graphics g ){
		g.setFont( Common.font12p );
		g.setColor( Color.yellow );
		g.drawString( "Hash Table        Execution mode: " + modeStr, xpos, ypos );
 	   g.drawString( "Status: " + statusStr, xpos + 250, ypos );
		if( overFlow )
			g.drawString( "Overflow", xpos, 250 );
	}
}


/******************************************************************************
	The OptionsDialog class defines the pop-up dialog window that allows the
	user to set the animation rate and enable or disable sound
******************************************************************************/  
class OptionsDialog extends Frame implements ItemListener, AdjustmentListener
{
		private HashApplet parent;
		private Common c;
		// components
		private Scrollbar animationSb;
		private Checkbox soundCb;
		private GridBagLayout gbLayout;
		private GridBagConstraints gbc;
		private Label animationRateLbl, animationSlowLbl, animationFastLbl;

		// construction
		public OptionsDialog( HashApplet applet, int anRate, String title ){
			super( title );
			addWindowListener( new WL() );

			parent = applet;
			c = new Common();

			soundCb = new Checkbox( "Sound" );
			soundCb.setFont( c.font12b );
			soundCb.addItemListener( this );
			soundCb.setState( true );
			
			animationRateLbl = new Label( "Animation delay: " + anRate + " ms", Label.LEFT );
			animationRateLbl.setFont( c.font12b );
			animationSlowLbl = new Label( "Slow" );
			animationFastLbl = new Label( "Fast", Label.RIGHT );

			// Set the range of from 0 to 1000. anRate is the default animation delay
			animationSb = new Scrollbar( Scrollbar.HORIZONTAL, 1000-anRate, 100, 0, 1100 );
			animationSb.addAdjustmentListener( this );


			// layout the dialog
			gbLayout = new GridBagLayout();
			gbc = new GridBagConstraints();

			Panel p = new Panel();

			p.setLayout( gbLayout );
			gbc.weightx = 50;
			gbc.anchor = gbc.WEST;
			gbc.insets = new Insets( 0, 30, 0, 30 );
	  		parent.addComponent( p, soundCb, gbc, 0, 0, 1, 1 );
	 	 	gbc.insets = new Insets( 10, 30, 0, 30 );
			gbc.ipady = -5;
			gbc.fill = GridBagConstraints.HORIZONTAL;
 	 		parent.addComponent( p, animationRateLbl, gbc, 1, 0, 2, 1 );
			gbc.insets = new Insets( 0, 30, 0, 30 );
			gbc.weightx = 0;
			gbc.anchor = gbc.WEST;
	 		parent.addComponent( p, animationSlowLbl, gbc, 2, 0, 1, 1 );
			gbc.anchor = gbc.EAST;
	 		parent.addComponent( p, animationFastLbl, gbc, 2, 1, 1, 1 );
			gbc.ipady = 0;
			gbc.weightx = 50;
			gbc.fill = GridBagConstraints.HORIZONTAL;
	 		parent.addComponent( p, animationSb, gbc, 4, 0, 2, 1 );
				 
			add( p, BorderLayout.CENTER );
			
		}
 
		// allow user to close the window by clicking the "X"
		public class WL extends WindowAdapter{
			public void windowClosing( WindowEvent e ){
				setVisible( false );
			}
		}

		/********* event handlers *************************************************/

		// checkbox toggles the soundFlag in the applet 
		public void itemStateChanged( ItemEvent e ){
			parent.setSound( soundCb.getState() );
		
		}

		// slide control which adjusts the thread delay in the applet
		public void adjustmentValueChanged( AdjustmentEvent ae ){
			// prevent a negative delay ( only happens on certain systems )
			int rate = Math.max( 1000 - ae.getValue(), 0 );
			parent.setAnimationRate( rate );
			String rateStr = "Animation delay: " + rate  + " ms";
			animationRateLbl.setText( rateStr );
		}
}


/******************************************************************************
	The Common class defines commonly used constant integers, fonts, 
 	strings, and colors used within the system.  
******************************************************************************/  
class Common{
	// fonts		 
	/*** PLR ***/
	/*
  	public static final Font font10b = new Font( "SansSerif", Font.BOLD, 10 );
	*/
  	public static final Font font10b = new Font( "SansSerif", Font.BOLD, 11 );
	public static final Font font11p = new Font( "SansSerif", Font.PLAIN, 11 );
	public static final Font font12p = new Font( "SanSerif", Font.PLAIN, 12 );
	public static final Font font12b = new Font( "SanSerif", Font.BOLD, 12 );
	public static final Font font14b = new Font( "SansSerif", Font.BOLD, 14 );
	// layout colors
	public static final Color lightBlue = new Color( 110, 170, 255 );
	public static final Color mediumBlue = new Color( 50, 110, 190 );
	public static final Color darkBlue = new Color( 0, 20, 110 );
	// applet states
	public static final int INTRO = 0;
	public static final int SETUP = 1;
	public static final int EXECUTE =2;
	public static final int RESULTS = 3;
	public static final int PLOT =4;
	// algorithims
	public static final int LINEARPROBING = 0;
	public static final int QUADRATICPROBING = 1;
	public static final int BUCKETCHAINING = 2;
	public static final int LINKEDLISTCHAINING = 3;
	public static final int CHAININGWITHOVERFLOW = 4;
	// execution modes
	public static final int STORE = 0;
	public static final int RETRIEVE = 1;
	// data types
	public static final int ODD = 0;
	public static final int EVEN = 1;
	public static final int MIXED = 2;
	// thread control
	public static final int RUN = 0;
	public static final int STEP = 1;
	public static final int PAUSE =2;


	public static String titles[] = { "Linear Probing", "Quadratic Probing",
 												 "Bucket Chaining", "Linked List Chaining", 
												 "Chaining with Seperate Overflow" };
	
}

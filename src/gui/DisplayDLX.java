package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.DataFileParser;
import util.Tile;
import dlx.DLX;

/**
 * A class to display the result in a GUI. The DisplayResults instance has two
 * panels, a control panel and a tiles display panel. The control panel includes
 * two parts, configuration and result display. Previously Control panel is
 * programmed in a stand-alone class.
 *
 * @author Dawei Fan, Deyuan Guo
 * @version 1.0 11/16/2014
 *
 * 			1.1 11/27/2014
 * 			1, Used new class DLX to replace DancingLinks.
 *			2, Initialized board outside DLX.
 */
public class DisplayDLX extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	/**
	 * DLX instance.
	 *
	 */
	private DLX dlx;

	/**
	 * The solution got from DLX.solve. For every tile, the first element is the number of
	 * the tile, others are positions.
	 *
	 */
	private List<List<List<Integer>>> solution;

	/**
	 * The number of possible solutions.
	 */
	private int numOfSolution;

	private char board[][];

	/** The color of each tiles */
	private List<Color> colors = null;

	/**
	 * Control panel.
	 */
	private JPanel pControl;

	/**
	 * Display panel to show tiles.
	 */
	private JPanel pDisplay;

	/**
	 * Use to map solution from DancingLinks class to actual position. In order
	 * to reduce the time complexity to O(1), an array is used.
	 */
	private int posMap[];

	/**
	 * The speed of the single step and single solution. from 1ms to 500ms.
	 */
	private int speed = 1000;

	/**
	 * Configure panel
	 */
	JPanel pConfig;
	JCheckBox cbEnableSpin;
	JCheckBox cbEnableSpinFlip;
	JCheckBox cbExtra;
	JCheckBox cbRmSymm;
	JButton bSolveAll;
	JButton bSolveStep;
	JButton bSolveTrail;
	JButton bPause;
	JButton bStop;
	JLabel lSpeed;
	JSlider sSpeed;

	/**
	 * Result panel.
	 */
	JPanel pResult;
	JLabel tResultInfo;
	JTextField tIndex;
	JButton bShowResult;
	JButton bPre;
	JButton bNext;
	JButton bPlay;
	JSlider sNumSolution;

	/**
	 * TileListPanel
	 */
	JPanel pTileList;
	JScrollPane scroll;


	/* Menu related */
	/**
	 * Declare of menu variables. b- for button, t- for text field, l- for
	 * label; m- for menu, mi- for menu item;
	 *
	 */
	private static JMenuBar mBar;
	private JMenu mFile;
	private JMenu mHelp;
	private JMenuItem miExit;
	private JMenuItem miRead;
	private JMenuItem miAbout;

	private JFileChooser fc;

	/**
	 * For autoplaying all solutions.
	 */
	private boolean isRunning = false;


	/**
	 * For single step and single solution.
	 */
	private boolean isPaused = false;

	/**
	 * For playing all solutions.
	 */
	private boolean isThread = false;

	/**
	 * Size parameters.
	 */
	private static final int frameSize[] = { 820+340-75-130, 610-10 };
	private static final int framePos[] = { 200, 20 };
	private static final int displaySize[] = { 600-65, 535 };
	private static final int displayPos[] = { 195, 10 };
	private static final int tileListSize[] = { 340-130, 535 };
	private static final int tileListPos[] = { 800-65, 10 };

	private int OffsetX = 5;
	private int OffsetY = 15;

	private static final int gridWidth = 3;

	private int sizeBlock;
	private int sizeTile;
	/**
	 * The origin point from the left-top of the board to pDisplay panel.
	 */
	private int origin[] = { 20, 20 };
	private int originTile[] = { 20 + gridWidth / 2, 20 + gridWidth / 2 };

	private CalculateAll calculateAll = null;
	private CalculateSingleSolution calculateSSol = null;
	private CalculateSingleStep calculateSStep = null;

	private class CalculateAll extends SwingWorker<List<List<List<Integer>>>, Void>{

		@Override
		protected List<List<List<Integer>>> doInBackground() {

			solution = new ArrayList<List<List<Integer>>>();

			/* Disable all useless buttons to prevent incorrect operations. */
			bSolveAll.setEnabled(false);
			bSolveStep.setEnabled(false);
			bSolveTrail.setEnabled(false);

			bShowResult.setEnabled(false);
			bPre.setEnabled(false);
			bNext.setEnabled(false);
			bPlay.setEnabled(false);
			tIndex.setText("0");


			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			/* Before every new process must reset DLX (DLX.config is also reset, DON'T reset enable options)*/
			dlx.Config.reset();
			dlx.preProcess();
			cbExtra.setSelected(dlx.Config.isEnableExtra());
			tResultInfo.setText("Calculating...");
			solution.addAll(dlx.solveAll());

//			dlx.printAllSolutions();
//			System.out.println("In background:");
//			System.out.println(solution);
			return solution;
		}
		@Override
		protected void done(){
			bSolveAll.setEnabled(true);
			bSolveStep.setEnabled(true);
			bSolveTrail.setEnabled(true);

			bShowResult.setEnabled(true);
			bPre.setEnabled(true);
			bNext.setEnabled(true);
			bPlay.setEnabled(true);

			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			try {
				solution = get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("The background task has been canceled!");
			}
			numOfSolution = solution.size();

			/* After geting the numofSolution, set the min and max of the slider. */
			/* Note: only when there is state change the solution could be displayed! */
			sNumSolution.setMinimum(1);
			sNumSolution.setMaximum(numOfSolution);
			sNumSolution.setMinimum(0);
			sNumSolution.setValue(0);
			sNumSolution.setValue(1);
			sNumSolution.setMinimum(1);


			if(numOfSolution == 0)
				tResultInfo.setText("No solutions!");
			else if(numOfSolution == 1)
				tResultInfo.setText("Only 1 solution!");
			else
				tResultInfo.setText(numOfSolution+" solutions!");
		}

	}

	private class CalculateSingleSolution extends SwingWorker<Integer, List<List<Integer>>>{

		@Override
		protected Integer doInBackground() {
			solution = new ArrayList<List<List<Integer>>>();
			bSolveAll.setEnabled(false);
			bSolveStep.setEnabled(false);
			bSolveTrail.setEnabled(false);
			bPause.setEnabled(true);
			bStop.setEnabled(true);

			bShowResult.setEnabled(false);
			bPre.setEnabled(false);
			bNext.setEnabled(false);
			bPlay.setEnabled(false);

			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			/* Before every new process must reset DLX (DLX.config is also reset, DON'T reset enable options)*/
			dlx.Config.reset();
			dlx.preProcess();
			cbExtra.setSelected(dlx.Config.isEnableExtra());
			tResultInfo.setText("Calculating...");
			tIndex.setText("0");

			int number = 0;

			List<List<Integer>> sol = dlx.nextSolution();
			while(sol!=null && !isCancelled()){
				while(isPaused){
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						System.err.println("Sleep Interrupt!");
					}
				};
				try {
					Thread.sleep(speed);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.err.println("Sleep interrupt");
				}
				number++;
				solution.add(sol);
				publish(sol);
				sol =  dlx.nextSolution();
			}

			return number;
		}

		@Override
		protected void done(){
			bSolveAll.setEnabled(true);
			bSolveStep.setEnabled(true);
			bSolveTrail.setEnabled(true);
			bPause.setEnabled(false);
			bStop.setEnabled(false);

			bShowResult.setEnabled(true);
			bPre.setEnabled(true);
			bNext.setEnabled(true);
			bPlay.setEnabled(true);

			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			/*
			try {
				numOfSolution = get();
			} catch (InterruptedException e) {
				System.err.println("The background task has been interrupt!");
			} catch (ExecutionException e) {
				System.err.println("The background task has been excuted incorrectly!");
			} catch (CancellationException e){
				System.err.println("The background task has been canceled!");
			}
			*/

			numOfSolution = solution.size();


			/* After geting the numofSolution, set the min and max of the slider. */
			sNumSolution.setMinimum(1);
			sNumSolution.setMaximum(numOfSolution);
			sNumSolution.setValue(1);

			if(numOfSolution == 0)
				tResultInfo.setText("No solutions!");
			else if(numOfSolution == 1)
				tResultInfo.setText("Only 1 solution!");
			else
				tResultInfo.setText(numOfSolution+" solutions!");
		}

		@Override
		protected void process(List<List<List<Integer>>> r){
//			System.out.println("The size of the list: "+r.size());
			cleanTiles();
			displayStep(r.get(r.size()-1));
			String s = tResultInfo.getText();
			String t =s.replaceAll("Calculating...", "");

			if(t.length()==0)
				t="0 solutions";
			String y =t.replaceAll(" solutions", "");
			int i = Integer.parseInt(y);
			tResultInfo.setText("Calculating..." + (i+1) +" solutions");
		}

	}

	private class CalculateSingleStep extends SwingWorker<List<List<List<Integer>>>, List<List<Integer>>>{

		@Override
		protected List<List<List<Integer>>> doInBackground() {
			solution = new ArrayList<List<List<Integer>>>();
			bSolveAll.setEnabled(false);
			bSolveStep.setEnabled(false);
			bSolveTrail.setEnabled(false);
			bPause.setEnabled(true);
			bStop.setEnabled(true);

			bShowResult.setEnabled(false);
			bPre.setEnabled(false);
			bNext.setEnabled(false);
			bPlay.setEnabled(false);

			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			/* Before every new process must reset DLX (DLX.config is also reset, DON'T reset enable options)*/
			dlx.Config.reset();
			dlx.preProcess();
			cbExtra.setSelected(dlx.Config.isEnableExtra());

			tResultInfo.setText("Calculating...");
			tIndex.setText("0");

			List<List<Integer>> sol = dlx.nextSingleStep();
			while(sol!=null && !isCancelled()){
				while(isPaused){
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						System.err.println("Sleep Interrupt!");
					}
				};
				try {
					Thread.sleep(speed);
				} catch (InterruptedException e) {
					System.err.println("Sleep Interrupt!");
				}

				publish(sol);
				sol =  dlx.nextSingleStep();
			}
			List<List<List<Integer>>> s = dlx.getSolutions();
			return s;
		}

		@Override
		protected void done(){
			bSolveAll.setEnabled(true);
			bSolveStep.setEnabled(true);
			bSolveTrail.setEnabled(true);
			bPause.setEnabled(false);
			bStop.setEnabled(false);

			bShowResult.setEnabled(true);
			bPre.setEnabled(true);
			bNext.setEnabled(true);
			bPlay.setEnabled(true);


			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			try {
				solution = get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("The background task has been canceled!");
			}
			numOfSolution = solution.size();

			/* After geting the numofSolution, set the min and max of the slider. */
			sNumSolution.setMinimum(1);
			sNumSolution.setMaximum(numOfSolution);
			sNumSolution.setValue(1);

			if(numOfSolution == 0)
				tResultInfo.setText("No solutions!");
			else if(numOfSolution == 1)
				tResultInfo.setText("Only 1 solution!");
			else
				tResultInfo.setText(numOfSolution+" solutions!");
		}

		@Override
		protected void process(List<List<List<Integer>>> r){
			cleanTiles();
			displayStep(r.get(r.size()-1));

			String s = tResultInfo.getText();
			String t =s.replaceAll("Calculating...", "");

			if(t.length()==0)
				t="0 solutions";
			String y =t.replaceAll(" solutions", "");

			int i = Integer.parseInt(y);
			int j = dlx.getSolutions().size();
			if(j > i);
			tResultInfo.setText("Calculating..." + j +" solutions");

		}

	}

	public DisplayDLX() {
		super(null);
		setBackground(Color.WHITE);

		this.setLocation(0, 0);
		this.setSize(810, 520);
		this.setOpaque(true);
		this.setVisible(true);
		this.setFocusable(true);

		setupMenu();
		setupControlPanel();
		setupDisplayPanel();
		setupTileListPanel();
		setComponents(false);
	}

	/**
	 * Setup DLX instance.
	 *
	 * @param dlx
	 */
	private void setDLX(DLX d) {
		dlx = d;
	}

	private void setupControlPanel() {

		pControl = new JPanel();
		pControl.setBackground(Color.WHITE);
		pControl.setLocation(5, 5);
		pControl.setSize(190, 540);
		pControl.setOpaque(true);
		pControl.setVisible(true);
		pControl.setFocusable(true);
		pControl.setLayout(null);
		/*
		 * this.setBorder(BorderFactory.createCompoundBorder(
		 * BorderFactory.createTitledBorder(""),
		 * BorderFactory.createEmptyBorder(5,5,5,5)));
		 */

		/* Initialize control sub-panel. */
		pConfig = new JPanel();
		pConfig.setBackground(Color.WHITE);
		pConfig.setLocation(5, 5);
		pConfig.setSize(180, 350);
		pConfig.setOpaque(true);
		pConfig.setVisible(true);
		pConfig.setFocusable(true);
		pConfig.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Control"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		pConfig.setLayout(null);

		cbEnableSpin = new JCheckBox("Enable spin");
		cbEnableSpin.setBackground(Color.WHITE);
		cbEnableSpin.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		cbEnableSpin.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		cbEnableSpin.setSelected(false);
		cbEnableSpin.setSize(160, 30);
		cbEnableSpin.setLocation(10, 20);
		cbEnableSpin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (cbEnableSpin.isSelected())
					dlx.Config.setEnableSpin(true);
				else{
					dlx.Config.setEnableSpin(false);
					cbEnableSpinFlip.setSelected(false);
					dlx.Config.setEnableSpinFlip(false);
				}
			}

		});
		pConfig.add(cbEnableSpin);

		cbEnableSpinFlip = new JCheckBox("Enable spin + flip");
		cbEnableSpinFlip.setBackground(Color.WHITE);
		cbEnableSpinFlip.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		cbEnableSpinFlip.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		cbEnableSpinFlip.setSelected(false);
		cbEnableSpinFlip.setSize(160, 30);
		cbEnableSpinFlip.setLocation(10, 50);
		cbEnableSpinFlip.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (cbEnableSpinFlip.isSelected()){
					dlx.Config.setEnableSpinFlip(true);
					cbEnableSpin.setSelected(true);
					dlx.Config.setEnableSpin(true);
				}
				else
					dlx.Config.setEnableSpinFlip(false);
			}

		});
		pConfig.add(cbEnableSpinFlip);

		cbExtra = new JCheckBox("Extra blocks");
		cbExtra.setBackground(Color.WHITE);
		cbExtra.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		cbExtra.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		cbExtra.setSelected(false);
		cbExtra.setSize(160, 30);
		cbExtra.setLocation(10, 80);
		cbExtra.setEnabled(false);
		pConfig.add(cbExtra);


		cbRmSymm = new JCheckBox("Remove symmetry");
		cbRmSymm.setBackground(Color.WHITE);
		cbRmSymm.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		cbRmSymm.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		cbRmSymm.setSelected(true);
		cbRmSymm.setSize(160, 30);
		cbRmSymm.setLocation(10, 110);
		cbRmSymm.setEnabled(true);
		cbRmSymm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cbRmSymm.isSelected())
					dlx.Config.setEliminateSymmetry(true);
				else
					dlx.Config.setEliminateSymmetry(false);
			}

		});
		pConfig.add(cbRmSymm);


		bSolveAll = new JButton("Get all solutions");
		bSolveAll.setBackground(Color.WHITE);
		bSolveAll.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		bSolveAll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				calculateAll = new CalculateAll();
				calculateAll.execute();
			}

		});
		bSolveAll.setSize(160, 30);
		bSolveAll.setLocation(10, 150);
		pConfig.add(bSolveAll);


		bSolveStep = new JButton("Solve & display steps");
		bSolveStep.setBackground(Color.WHITE);
		bSolveStep.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		bSolveStep.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				calculateSSol = new CalculateSingleSolution();
				calculateSSol.execute();
			}

		});
		bSolveStep.setSize(160, 30);
		bSolveStep.setLocation(10, 190);
		pConfig.add(bSolveStep);

		bSolveTrail = new JButton("Solve & display trails");
		bSolveTrail.setBackground(Color.WHITE);
		bSolveTrail.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		bSolveTrail.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				calculateSStep = new CalculateSingleStep();
				calculateSStep.execute();
			}

		});
		bSolveTrail.setSize(160, 30);
		bSolveTrail.setLocation(10, 230);
		pConfig.add(bSolveTrail);


		lSpeed = new JLabel("Speed (1x)");
		lSpeed.setSize(140, 20);
		lSpeed.setLocation(10, 265);
		pConfig.add(lSpeed);

		sSpeed = new JSlider(-2, 6, 0); //from 2^-2 to 2^6
		sSpeed.setBackground(Color.WHITE);
		sSpeed.setSize(160, 30);
		sSpeed.setLocation(10, 280);
		sSpeed.setSnapToTicks(false);
		sSpeed.setPaintTicks(false);
		sSpeed.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				int s = sSpeed.getValue();
				if (s < 6) {
					speed =  (int)(1000 / Math.pow(2, sSpeed.getValue()));
				} else {
					speed = 1; //unlimited
				}
				if (s >= 0 && s < 6) {
					lSpeed.setText("Speed (" + (int)Math.pow(2, s) + "x)");
				} else if (s < 0) {
					lSpeed.setText("Speed (1/" + (int)Math.pow(2, -s) + "x)");
				} else {
					lSpeed.setText("Speed (Unlimited)");
				}
			}

		});
		pConfig.add(sSpeed);


		bPause = new JButton("Pause");

		bPause.setBackground(Color.WHITE);
		bPause.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		bPause.setEnabled(false);
		bPause.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				isPaused = !isPaused;
				if(isPaused){
					bPause.setText("Resume");
				}
				else{
					bPause.setText("Pause");
				}
			}

		});
		bPause.setSize(75, 30);
		bPause.setLocation(10, 310);
		pConfig.add(bPause);

		bStop = new JButton("Stop");
		bStop.setBackground(Color.WHITE);
		bStop.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		bStop.setEnabled(false);
		bStop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if(calculateSSol!=null && calculateSSol.getState() == SwingWorker.StateValue.STARTED){
					calculateSSol.cancel(true);
				}
				else if(calculateSStep!=null && calculateSStep.getState() == SwingWorker.StateValue.STARTED){
					calculateSStep.cancel(true);
				}

			}

		});
		bStop.setSize(75, 30);
		bStop.setLocation(95, 310);
		pConfig.add(bStop);


		pControl.add(pConfig);


		/* Initialize result sub-panel. */
		pResult = new JPanel();
		pResult.setBackground(Color.WHITE);
		pResult.setSize(180, 180);
		pResult.setLocation(5, 360);
		pResult.setOpaque(true);
		pResult.setVisible(true);
		pResult.setFocusable(true);
		pResult.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Result"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		pResult.setLayout(null);

		tResultInfo = new JLabel("Press button to solve");
		tResultInfo.setBackground(Color.WHITE);
		tResultInfo.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		// tResultInfo.setPreferredSize(new Dimension(60, 30));
		tResultInfo.setSize(new Dimension(160, 30));
		tResultInfo.setLocation(10, 20);
		tResultInfo.setVisible(true);
		pResult.add(tResultInfo);

		tIndex = new JTextField(" index", 10);
		tIndex.setBackground(Color.WHITE);
		tIndex.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		// tIndex.setPreferredSize(new Dimension(80, 25));
		tIndex.setSize(new Dimension(80, 30));
		// tIndex.setMaximumSize(new Dimension(80, 25));
		tIndex.setVisible(true);
		tIndex.setLocation(10, 50);
		pResult.add(tIndex);

		bShowResult = new JButton("Show!");
		bShowResult.setBackground(Color.WHITE);
		bShowResult.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		bShowResult.setSize(80, 30);
		bShowResult.setLocation(90, 50);
		bShowResult.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				int id = Integer.parseInt(tIndex.getText());
				if (id >= 1 && id <= numOfSolution) {
					cleanTiles();
					displayResults(id - 1);

					/* Set slider. */
					sNumSolution.setValue(id);
				}

				else {
					System.err.println("Out of Range!");
					JOptionPane.showMessageDialog(null, "Index out of range!",
							"Warning", JOptionPane.WARNING_MESSAGE);
				}
			}

		});
		pResult.add(bShowResult);

		bPre = new JButton("Prev");
		bPre.setBackground(Color.WHITE);
		bPre.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		bPre.setSize(80, 30);
		bPre.setLocation(10, 80);
		bPre.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int cur = Integer.parseInt(tIndex.getText());
				if (cur > 1) {
					cur--;
					tIndex.setText(Integer.toString(cur));
					cleanTiles();
					displayResults(cur - 1);
					/* Set slider. */
					sNumSolution.setValue(cur);
				}
			}

		});
		pResult.add(bPre);

		bNext = new JButton("Next");
		bNext.setBackground(Color.WHITE);
		bNext.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		bNext.setSize(80, 30);
		bNext.setLocation(90, 80);
		bNext.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int cur = Integer.parseInt(tIndex.getText());
				if (cur < numOfSolution) {
					cur++;
					tIndex.setText(Integer.toString(cur));
					cleanTiles();
					displayResults(cur - 1);
					/* Set slider. */
					sNumSolution.setValue(cur);
				}

			}

		});
		pResult.add(bNext);

		sNumSolution = new JSlider();
		sNumSolution.setBackground(Color.WHITE);
		sNumSolution.setSize(160, 30);
		sNumSolution.setLocation(10, 110);
		sNumSolution.setExtent(0);
		sNumSolution.setMinimum(1);
		sNumSolution.setMaximum(100);
		sNumSolution.setSnapToTicks(false);
		sNumSolution.setPaintTicks(false);
		sNumSolution.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				tIndex.setText(Integer.toString(sNumSolution.getValue()));
				cleanTiles();
				if(sNumSolution.getValue()>=1 && sNumSolution.getValue()<=numOfSolution)
					displayResults(sNumSolution.getValue() - 1);
			}

		});

		pResult.add(sNumSolution);

		bPlay = new JButton("Autoplay all solutions");
		bPlay.setBackground(Color.WHITE);
		bPlay.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		bPlay.setSize(160, 30);
		bPlay.setLocation(10, 140);
		bPlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (!isRunning) {
					isRunning = true;
					bPlay.setText("Stop");
					if (!isThread) {
						isThread = true;
						new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub

								while (tIndex.getText() != ""
										&& Integer.parseInt(tIndex.getText()) < numOfSolution) {
									if (isRunning) {
										int i = Integer.parseInt(tIndex
												.getText());
										cleanTiles();
										displayResults(i);
										sNumSolution.setValue(i + 1);
										try {
											Thread.sleep(500);
										} catch (InterruptedException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

									}
								}
								isThread = false;
								isRunning = false;
								bPlay.setText("Autoplay all solutions");
								sNumSolution.setValue(1);
							}
						}).start();

					}
				}

				/* If now it is running, stop it! */
				else {
					isRunning = false;
					bPlay.setText("Autoplay all solutions");

				}
			}
		});
		pResult.add(bPlay);
		pControl.add(pResult);
		this.add(pControl);
	}

	private void setupDisplayPanel() {
		pDisplay = new JPanel();
		pDisplay.setBackground(Color.WHITE);
		pDisplay.setLayout(null);
		pDisplay.setLocation(displayPos[0], displayPos[1]);
		pDisplay.setSize(displaySize[0], displaySize[1]);
		pDisplay.setOpaque(true);
		pDisplay.setVisible(true);
		pDisplay.setFocusable(true);
		pDisplay.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Solution"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		this.add(pDisplay);
	}

	private void setupTileListPanel(){
		pTileList = new JPanel();
		pTileList.setBackground(Color.WHITE);
		pTileList.setLayout(null);
		pTileList.setSize(tileListSize[0], tileListSize[1]);
		pTileList.setLocation(tileListPos[0], tileListPos[1]);
		pTileList.setOpaque(true);
		pTileList.setVisible(true);
		pTileList.setFocusable(true);
		pTileList.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Tiles candidates"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		this.add(pTileList);
	}

	private void setupTileList(List<Tile> t){

		List<Color> colorList = genColors(t.size());
		for (int i = 0; i < t.size(); i++) {
			t.get(i).printTile();
		}

		final int width = (tileListSize[0] - 30) / 3;
		int lMax = Math.max(t.get(0).l, t.get(0).w);
		for (int i = 1; i < t.size(); i++) {
			if (lMax < Math.max(t.get(i).l, t.get(i).w))
				lMax = Math.max(t.get(i).l, t.get(i).w);
		}

		int grid = width / lMax - 1;
		if (grid == 0) grid = 1; // too small?

		JPanel block[] = new JPanel[t.size()];
		for (int i = 0; i < t.size(); i++) {
			JPanel pic = new JPanel(null);
			pic.setSize(width, width);
			pic.setLocation(i % 3 * width + 15, i / 3 * width + 25);
			pic.setBackground(Color.white);
			pTileList.add(pic);

			for (int j = 0; j < t.get(i).data.length; j++){
				for (int k = 0; k < t.get(i).data[0].length; k++){
					if (t.get(i).data[j][k] != ' ') {
						block[i] = new JPanel();
						block[i].setBackground(colorList.get(i));
						block[i].setBorder(new LineBorder(Color.black));
						block[i].setSize(grid + 1, grid + 1);
						block[i].setLocation(k * grid, j * grid);
						pic.add(block[i]);
					}
				}
			}
		}

	}

	private void setupMenu() {
		mBar = new JMenuBar();
		mBar.setBackground(Color.WHITE);
		mBar.setOpaque(true);

		mFile = new JMenu("File");
		mFile.setMnemonic(KeyEvent.VK_F);

		miRead = new JMenuItem("Read...");
		miRead.setMnemonic(KeyEvent.VK_R);
		miRead.addActionListener(this);
		mFile.add(miRead);

		miExit = new JMenuItem("Exit");
		miExit.setMnemonic(KeyEvent.VK_E);
		miExit.addActionListener(this);
		mFile.add(miExit);

		mHelp = new JMenu("Help");
		mHelp.setMnemonic(KeyEvent.VK_H);
		miAbout = new JMenuItem("About");
		miAbout.setMnemonic(KeyEvent.VK_A);
		miAbout.addActionListener(this);
		mHelp.add(miAbout);

		mBar.add(mFile);
		mBar.add(mHelp);

		fc = new JFileChooser();
		fc.setCurrentDirectory(new File("./tests"));

	}

	/**
	 * Initiate the board from input and draw the board.
	 */
	private void setupBoard(char[][] b) {

		/* Initialize board array. */
		board = new char[b.length][b[0].length];
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[0].length; j++) {
				board[i][j] = b[i][j];
			}
		}

		/*
		 * Initialize board related size.
		 * There should be some redundancy for the sizeblock, or the edges cannot
		 * be drawn.
		 */
		sizeBlock = Math.min(
				(displaySize[0] - origin[0] - 20) / board[0].length,
				(displaySize[1] - origin[1] - 20) / board.length)
				- gridWidth;
		sizeTile = sizeBlock + gridWidth;

		/* There are n kind of colors in the board. */
		Set<Character> set = new HashSet<Character>();

		int w = board.length;
		int l = board[0].length;

		/* Set offset, to make the board display in the middle. */
		OffsetX = displaySize[0]/2 - (l * (sizeBlock + gridWidth) + gridWidth)/2-18;
		OffsetY = displaySize[1]/2 - (w * (sizeBlock + gridWidth) + gridWidth)/2-10;


		/* First is x, second is y. */
		int sizeGridH[] = { l * (sizeBlock + gridWidth) + gridWidth, gridWidth };
		int sizeGridV[] = { gridWidth, w * (sizeBlock + gridWidth) + gridWidth };
		JPanel gridH[] = new JPanel[w + 1];
		JPanel gridV[] = new JPanel[l + 1];
		JPanel gridH_w[] = new JPanel[w + 1];
		JPanel gridV_w[] = new JPanel[l + 1];

		/* Setup horizontal and vertical white grids */
		for (int i = 0; i <= w; i++) {
			gridH_w[i] = new JPanel();
			gridH_w[i].setBackground(Color.white);
			gridH_w[i].setSize(sizeGridH[0] - 2, sizeGridH[1] - 2);
			gridH_w[i].setLocation(origin[0] + OffsetX, origin[1] + (sizeBlock + gridWidth)
					* i + OffsetY);
			gridH_w[i].setOpaque(true);
			gridH_w[i].setVisible(true);
			pDisplay.add(gridH_w[i]);
		}

		for (int j = 0; j <= l; j++) {
			gridV_w[j] = new JPanel();
			gridV_w[j].setBackground(Color.white);
			gridV_w[j].setSize(sizeGridV[0] - 2, sizeGridV[1] - 2);
			gridV_w[j].setLocation(origin[0] + (sizeBlock + gridWidth) * j + OffsetX,
					origin[1] + OffsetY);
			gridV_w[j].setOpaque(true);
			gridV_w[j].setVisible(true);
			pDisplay.add(gridV_w[j]);
		}

		/* Setup horizontal and vertical black grids */
		for (int i = 0; i <= w; i++) {
			gridH[i] = new JPanel();
			gridH[i].setBackground(Color.black);
			gridH[i].setSize(sizeGridH[0], sizeGridH[1]);
			gridH[i].setLocation(origin[0]+OffsetX-1, origin[1] + (sizeBlock + gridWidth)
					* i+OffsetY-1);
			gridH[i].setOpaque(true);
			gridH[i].setVisible(true);
			pDisplay.add(gridH[i]);
		}

		for (int j = 0; j <= l; j++) {
			gridV[j] = new JPanel();
			gridV[j].setBackground(Color.black);
			gridV[j].setSize(sizeGridV[0], sizeGridV[1]);
			gridV[j].setLocation(origin[0] + (sizeBlock + gridWidth) * j+OffsetX-1,
					origin[1]+OffsetY-1);
			gridV[j].setOpaque(true);
			gridV[j].setVisible(true);
			pDisplay.add(gridV[j]);
		}

		/* Setup missing blocks if there are any. */
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < l; j++) {
				if (board[i][j] == ' ') {

					JPanel block = new JPanel() {
						private static final long serialVersionUID = 1L;

						@Override
						public void paintComponent(Graphics g) {
							for (int i = 0; i < sizeTile; i += 4) {
								g.drawLine(sizeTile - i, 0, 0, sizeTile - i);
								g.drawLine(sizeTile, i, i, sizeTile);
							}
						}
					};
					block.setSize(sizeTile, sizeTile);
					int x = originTile[0] + (j) * sizeTile + OffsetX;
					int y = originTile[1] + (i) * sizeTile + OffsetY;
					block.setLocation(x, y);
					block.setOpaque(true);
					block.setVisible(true);
					pDisplay.add(block);
				} else
					set.add(board[i][j]);
			}
		}

		/* Setup board colors (now use chars to represent) if there are more than one colors. */
		if (set.size() > 1) {
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < l; j++) {
					if (board[i][j] == ' ') continue;
					JLabel block = new JLabel(Character.toString(board[i][j]));
					block.setSize(sizeTile/2, sizeTile/2);
					block.setFont(new Font("Arial", Font.PLAIN, sizeTile/2));
					block.setVerticalAlignment(SwingConstants.CENTER);
					block.setHorizontalAlignment(SwingConstants.CENTER);
					int x = originTile[0] + (j) * sizeTile + OffsetX + sizeTile/4;
					int y = originTile[1] + (i) * sizeTile + OffsetY + sizeTile/4;
					block.setLocation(x, y);
					block.setOpaque(false);  //set to transparent
					block.setVisible(true);
					pDisplay.add(block);
				}
			}
		}
	}

	public void displayResults(int id) {

		List<List<Integer>> pos = solution.get(id);
		int number = pos.size();

		if (id >= numOfSolution) {
			System.err.println("Index of solutions out of range!");
			return;
		}

		for (int i = 0; i < number; i++) {
			List<Integer> tilePos = new ArrayList<Integer>();
			tilePos = pos.get(i);
			Color c = colors.get(tilePos.get(0));
			for (int j = 1; j < tilePos.size(); j++) {
				JPanel block = new JPanel();
				block.setBackground(c);
				block.setSize(sizeTile, sizeTile);
				int x = originTile[0]
						+ (posMap[tilePos.get(j)] % (board[0].length))
						* sizeTile+OffsetX;
				int y = originTile[0]
						+ (posMap[tilePos.get(j)] / (board[0].length))
						* sizeTile+OffsetY;
				block.setLocation(x, y);
				block.setOpaque(true);
				block.setVisible(true);
				pDisplay.add(block);

			}
		}
		pDisplay.repaint();

	}

	public void displayStep(List<List<Integer>> pos) {

		int number = pos.size();

		for (int i = 0; i < number; i++) {
			List<Integer> tilePos = new ArrayList<Integer>();
			tilePos = pos.get(i);
			Color c = colors.get(tilePos.get(0));
			for (int j = 1; j < tilePos.size(); j++) {
				JPanel block = new JPanel();
				block.setBackground(c);
				block.setSize(sizeTile, sizeTile);
				int x = originTile[0]
						+ (posMap[tilePos.get(j)] % (board[0].length))
						* sizeTile+OffsetX ;
				int y = originTile[0]
						+ (posMap[tilePos.get(j)] / (board[0].length))
						* sizeTile+OffsetY;
				block.setLocation(x, y);
				block.setOpaque(true);
				block.setVisible(true);
				pDisplay.add(block);

			}
		}
		pDisplay.repaint();
	}

	private void cleanTiles() {

		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				/* Avoid deleting the grids. */
				int x = originTile[0] + j * sizeTile + gridWidth + 2+OffsetX;
				int y = originTile[0] + i * sizeTile + gridWidth + 2+OffsetY;

				if (board[i][j] != ' ') {
					Component t = pDisplay.getComponentAt(x, y);
					pDisplay.remove(t);
				}
			}
		}
		pDisplay.repaint();
	}

	/**
	 * This is a map between the assigned number of the board in DLX and the real position
	 * for drawing when there are holes in the board.
	 * If there are no holes this function does nothing
	 */
	private void setPosMap() {
		posMap = new int[board.length * board[0].length];
		for (int j = 0; j < posMap.length; j++)
			posMap[j] = j;

		/* This list is for board with missing blocks. */
		List<Integer> missing = new ArrayList<Integer>();
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				if (board[i][j] == ' ')
					missing.add(i * board[0].length + j);
			}
		}

		for (int i = 0; i < missing.size(); i++) {
			for (int j = missing.get(i) - i; j < posMap.length; j++)
				posMap[j]++;
		}
	}

	/**
	 * Generate n colors for n different tiles.
	 * @param n - the number of tiles
	 * @return a list of Color
	 */
	private List<Color> genColors(int n) {
		List<Color> colors = new ArrayList<Color>();

		/* Default color list. */
		colors.addAll(Arrays.asList(
				Color.cyan,
				Color.blue,
				Color.green,
				Color.red,
				Color.yellow,
				new Color(46, 139, 87),
				new Color(148, 0, 211),
				new Color(135, 51, 36),
				Color.magenta,
				Color.gray,
				Color.pink,
				new Color(175, 255, 225),
				new Color(130, 175, 190)
				));

		double goldenRatio = 0.618033988749895;
		double hue = 0.0; //or use random start value between 0 and 1
		double saturation = 0.5;
		double value = 0.95;

		int m = n - colors.size();
		for (int i = 0; i < m; i++) {
			hue = (hue + goldenRatio) % 1.0;

			/* HSV to RGB */
			int h = (int)(hue * 6);
			double f = hue * 6 - h;
			double p = value * (1 - saturation);
			double q = value * (1 - f * saturation);
			double t = value * (1 - (1 - f) * saturation);

			double r, g, b;
			switch (h) {
			case 0: r = value; g = t; b = p; break;
			case 1: r = q; g = value; b = p; break;
			case 2: r = p; g = value; b = t; break;
			case 3: r = p; g = q; b = value; break;
			case 4: r = t; g = p; b = value; break;
			case 5: r = value; g = p; b = q; break;
			default: throw new RuntimeException("Error in genColor.");
			}

			colors.add(new Color((int)(r*256), (int)(g*256), (int)(b*256)));
		}
		return colors;
	}

	private void setComponents(boolean b){
		cbEnableSpin.setEnabled(b);
		cbEnableSpinFlip.setEnabled(b);
		cbRmSymm.setEnabled(b);
		bSolveAll.setEnabled(b);
		bSolveTrail.setEnabled(b);
		bSolveStep.setEnabled(b);
		bPause.setEnabled(b);
		bStop.setEnabled(b);
		lSpeed.setEnabled(b);
		sSpeed.setEnabled(b);
		tIndex.setEnabled(b);
		bShowResult.setEnabled(b);
		bPre.setEnabled(b);
		bNext.setEnabled(b);
		bPlay.setEnabled(b);

	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == miExit)
			System.exit(0);

		if (e.getSource() == miAbout) {
			JOptionPane.showMessageDialog(null, "Tiling Puzzle v1.0\n"
					+ "Date: 11/24/2014\n" + "Designed by Dawei Fan and Deyuan Guo",
					"About", JOptionPane.INFORMATION_MESSAGE);
		}
		if (e.getSource() == miRead) {
			/* Read a ASCII file and solve it. */
			File file = null;
			if (fc.showOpenDialog(DisplayDLX.this) == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
			//	System.out.println(file.getAbsolutePath());
				System.out.println(file.getName());
			}
			// not select any files
			else {
				JOptionPane.showConfirmDialog(null, "No file is selected!",
						"Warning", JOptionPane.CLOSED_OPTION,
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			/* Delete all panels in the pDisplay and pTileList if there are. */
			pTileList.removeAll();
			pDisplay.removeAll();

			/* Reset configuration . */
			cbEnableSpin.setSelected(false);
			cbEnableSpinFlip.setSelected(false);
			cbRmSymm.setSelected(true);

			/* Reset text field */
			tResultInfo.setText("Press button to solve");
			tIndex.setText("0");

			pDisplay.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Solution ("+file.getName()+")"),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
			DataFileParser dfp = new DataFileParser(file.getAbsolutePath());
			/* Extract puzzle pieces, board are included in this list. */
			List<Tile> tileList = dfp.ExtractTiles();
			/* Get the board and the remained is tileList. */
			Tile board = tileList.get(0);
			tileList.remove(0);

			/* Initiate the color list */
			colors = genColors(tileList.size());

			/* Initiate a new DLX Solver and set it. */
			DLX dlx = new DLX(board, tileList);
			setDLX(dlx);
			setComponents(true);
			/* Initialize the board and posMap. */
			setupBoard(board.data);
			setPosMap();
			/* Initialize and display the tile. */
			setupTileList(tileList);
			repaint();

		}
	}

	public static void createAndShowGUI() {

		JFrame frame = new JFrame("Tilling Puzzle Solver");
		frame.setContentPane(new DisplayDLX());
		frame.setJMenuBar(mBar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setSize(frameSize[0], frameSize[1]);
		frame.setLocation(framePos[0], framePos[1]);
		frame.setResizable(false);
		frame.setVisible(true);
	}

	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(new Runnable(){

			@Override
			public void run() {
				createAndShowGUI();
			}

		});

	}

}
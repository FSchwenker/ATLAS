package de.atlas.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import de.atlas.collections.ObjectLine;
import de.atlas.collections.ScalarTrack;
import de.atlas.messagesystem.*;
import de.atlas.misc.AnnotationKeyListener;
import de.atlas.misc.AtlasProperties;
import de.atlas.data.LabelClass;
import de.atlas.data.LabelClassEntity;
import de.atlas.data.LabelClasses;
import de.atlas.data.LabelObject;
import de.atlas.data.Project;
import de.atlas.data.ScrollType;
import eu.semaine.exceptions.SystemConfigurationException;
import eu.semaine.system.ComponentRunner;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.InputEvent;

//javahelp imports
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.help.*;

public class Atlas implements WindowListener {

	private static String projectXml_ = null;
	private final String version = new String("2.0.0 alpha");
	private JFrame lcollFrame;
	private JFrame controlFrame;
	private JToggleButton playButton;
	private Thread thread;
	private GuiUpdater guiThread;
	public LabelProperties labelProps;
	public LabelTrackTabular labelTabular;
	public LabelLegend labelLegend;
	public ClassEditor classEditor;
	public TrackEditor trackEditor;
	public ProjectCollectionEditor projectCollectionEditor;
	public LearningWindow learningWindow;
	public SimpleActiveLearner simpleActiveLearner;
	public SpeakerSegmenter speakerSegmenter;
	public ScalarTrack2Labels scalarTrack2Labels;
	public ClearLabelTrack clearLabelTrack;
	public ShiftLabels shiftLabels;
	public SearchLabels searchLabels;
	public MatlabIO matlabIO;
	public LiveData liveData;
	private JMenu viewMenu;
	private JMenu mnFile;
	private JMenuItem saveProjectMenu;
	private JMenuItem loadProjectMenu;
	private JMenuItem reloadProjectMenu;
	private JMenu addMenu;
	private JMenuItem newProjectMenu;
	private JMenuItem closeMenuItem_2;
	private JMenuItem mntmImportLabeltrack;
	private JMenuItem mntmImportScalartrack;
	private JMenuItem mntmImportVectortrack;
	private JMenuItem mntmImportDatatrack;
	private JMenuItem mntmImportClass;
	private JMenuItem mntmImportAudio;
	private JMenuItem mntmImportVideo;
	private JMenuItem mntmLiveData;
	private JButton prevFrameButton;
	private JButton nextFrameButton;
	private JButton stopButton;
	private JMenu importMenu;
	private JMenu setScrollModeMenu;
	private JRadioButtonMenuItem rdbtnmntmHalf;
	private JRadioButtonMenuItem rdbtnmntmFullJump;
	private JRadioButtonMenuItem rdbtnmntmSlide;
	private JCheckBoxMenuItem muteVideoMenu;
	private JCheckBoxMenuItem autoArrangeWindowsMenu;
	private JMenuItem shotMenu;
	private JMenuItem mntmSuggestLabels;
	private JMenuItem mntmActiveLearning;
	private JMenuItem mntmSpeakerSegementation;
	private JMenu mnTools;
	private JMenuItem mntmCommandline;
	private JMenuItem mntmMatlabIO;
	private JMenuItem mntmDebug;


	// always on top booleans
	private JCheckBoxMenuItem aotAtlasMain;
	private JCheckBoxMenuItem aotLabelArea;
	private JCheckBoxMenuItem aotLabelProps;
	private JCheckBoxMenuItem aotTrackTable;
	private JCheckBoxMenuItem aotSearchLabels;
	private JCheckBoxMenuItem aotLabelLegend;

	private JToggleButton loopPlayToggleButton_;
	private LabelObject activeLabel;

	public Atlas(String projectXml) {

		ComponentRunner runner;
		try {
			runner = new ComponentRunner("ATLAS_Semaine.config");
			runner.go();
			System.out.println("Connected to server, running in server mode");
		} catch (UnknownHostException e1) {
			System.out.println("UnknownHostException, running in stand alone mode");
			System.out.println(e1.getMessage());
		}catch (SystemConfigurationException e1) {
			//e1.printStackTrace();
			System.out.println("SystemConfigurationException, running in stand alone mode");
		} catch (IOException e1) {
			System.out.println("IOException, running in stand alone mode");
		} catch (ClassNotFoundException e1) {
			System.out.println("ClassNotFoundException, running in stand alone mode");
		} catch (NoSuchMethodException e1) {
			System.out.println("NoSuchMethodException, running in stand alone mode");
		} catch (IllegalAccessException e1) {
			System.out.println("IllegalAccessException, running in stand alone mode");
		} catch (InstantiationException e1) {
			System.out.println("InstantiationException, running in stand alone mode");
		} catch (InvocationTargetException e1) {
			System.out.println("InvocationTargetException, running in stand alone mode");
		} 

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		labelProps = new LabelProperties();
		classEditor = new ClassEditor();
		labelTabular = new LabelTrackTabular();
		labelLegend = new LabelLegend();
		learningWindow = new LearningWindow();
		simpleActiveLearner = new SimpleActiveLearner();
		speakerSegmenter = new SpeakerSegmenter();
		scalarTrack2Labels = new ScalarTrack2Labels();
		clearLabelTrack = new ClearLabelTrack();
		shiftLabels = new ShiftLabels();
		searchLabels = new SearchLabels();
		matlabIO = new MatlabIO();
		liveData = new LiveData();
		//matlabIO.setVisible(true);
		//matlabIO.setAlwaysOnTop(true);
		lcollFrame = new JFrame();
		controlFrame = new JFrame();
		projectCollectionEditor = new ProjectCollectionEditor(this);

		labelProps.setIconImage(new ImageIcon(Project.class
				.getResource("/icon_labelPropertiesFrame.gif")).getImage());
		classEditor.setIconImage(new ImageIcon(Project.class
				.getResource("/icon_classEditorFrame.gif")).getImage());
		labelTabular.setIconImage(new ImageIcon(Project.class
				.getResource("/icon_labelTabularFrame.gif")).getImage());
		labelLegend.setIconImage(new ImageIcon(Project.class
				.getResource("/icon_labelLedgendFrame.gif")).getImage());
		learningWindow.setIconImage(new ImageIcon(Project.class
				.getResource("/icon.gif")).getImage());
		simpleActiveLearner.setIconImage(new ImageIcon(Project.class
				.getResource("/icon.gif")).getImage());
		scalarTrack2Labels.setIconImage(new ImageIcon(Project.class
				.getResource("/icon.gif")).getImage());
		clearLabelTrack.setIconImage(new ImageIcon(Project.class
				.getResource("/icon.gif")).getImage());
		shiftLabels.setIconImage(new ImageIcon(Project.class
				.getResource("/icon.gif")).getImage());
		searchLabels.setIconImage(new ImageIcon(Project.class
				.getResource("/icon.gif")).getImage());
		lcollFrame.setIconImage(new ImageIcon(Project.class
				.getResource("/icon2.gif")).getImage());
		controlFrame.setIconImage(new ImageIcon(Project.class
				.getResource("/icon2.gif")).getImage());
		projectCollectionEditor.setIconImage(new ImageIcon(Project.class
				.getResource("/icon2.gif")).getImage());
		matlabIO.setIconImage(new ImageIcon(Project.class
				.getResource("/icon.gif")).getImage());
		liveData.setIconImage(new ImageIcon(Project.class
				.getResource("/icon.gif")).getImage());

		controlFrame.setResizable(false);
		controlFrame.getContentPane().setFocusable(true);
		controlFrame.getContentPane().addKeyListener(
				AnnotationKeyListener.getInstance());

		lcollFrame.setTitle("Atlas - LabelArea");

		controlFrame.setTitle("Atlas");
		int xres = (int) dim.getWidth();
		int yres = (int) dim.getHeight();
		if (xres > 2560) {
			xres = 2560;
		}
		controlFrame.setBounds(0, (yres / 2) - 135, 330, 433/*135*/);
		controlFrame.addWindowListener(this);
		controlFrame.setAlwaysOnTop(false);
		lcollFrame.setBounds(0, yres / 2, xres - 10, (yres / 2) - 25);
		lcollFrame.addWindowListener(this);

		lcollFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		controlFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		lcollFrame.getContentPane()
				.setLayout(
						new BoxLayout(lcollFrame.getContentPane(),
								BoxLayout.PAGE_AXIS));

		lcollFrame.getContentPane().add(Project.getInstance().getLcoll());

		JPanel buttonPanel = new JPanel();
		controlFrame.getContentPane().add(buttonPanel);
		buttonPanel.setLayout(null);

		playButton = new JToggleButton();
		playButton.setToolTipText("Start / Pause video playback");
		playButton.setFocusable(false);
		playButton.setBounds(198, 12, 50, 50);
		// ImageIcon playIcon = new ImageIcon("play.gif");
		ImageIcon playIcon = new ImageIcon(
				Project.class.getResource("/play.gif"));
		playIcon.setImage(playIcon.getImage().getScaledInstance(
				playButton.getHeight() - 4, playButton.getHeight() - 4,
				Image.SCALE_SMOOTH));
		ImageIcon pauseIcon = new ImageIcon(
				Project.class.getResource("/pause.gif"));
		pauseIcon.setImage(pauseIcon.getImage().getScaledInstance(
				playButton.getHeight() - 4, playButton.getHeight() - 4,
				Image.SCALE_SMOOTH));
		playButton.setIcon(playIcon);
		playButton.setSelectedIcon(pauseIcon);
		playButton.setEnabled(false);
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				playpauseButton();
			}
		});

		buttonPanel.add(playButton);

		stopButton = new JButton();
		stopButton.setToolTipText("STOP - reset timeline to beginning");
		stopButton.setFocusable(false);
		stopButton.setBounds(74, 12, 50, 50);
		ImageIcon stopIcon = new ImageIcon(
				Project.class.getResource("/stop.gif"));
		stopIcon.setImage(stopIcon.getImage().getScaledInstance(
				stopButton.getHeight() - 4, stopButton.getHeight() - 4,
				Image.SCALE_SMOOTH));
		stopButton.setIcon(stopIcon);
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startoverButton();
			}
		});

		buttonPanel.add(stopButton);

		prevFrameButton = new JButton();
		prevFrameButton.setToolTipText("Skip back one video frame");
		prevFrameButton.setFocusable(false);
		prevFrameButton.setBounds(12, 12, 50, 50);
		ImageIcon prevIcon = new ImageIcon(
				Project.class.getResource("/prev.gif"));
		prevIcon.setImage(prevIcon.getImage().getScaledInstance(
				prevFrameButton.getHeight() - 4,
				prevFrameButton.getHeight() - 4, Image.SCALE_SMOOTH));
		prevFrameButton.setIcon(prevIcon);
		prevFrameButton.setEnabled(false);
		prevFrameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				prevFrame();
			}
		});

		buttonPanel.add(prevFrameButton);

		nextFrameButton = new JButton();
		nextFrameButton.setToolTipText("Skip forward one video frame");
		nextFrameButton.setFocusable(false);
		nextFrameButton.setBounds(260, 12, 50, 50);
		ImageIcon nextIcon = new ImageIcon(
				Project.class.getResource("/next.gif"));
		nextIcon.setImage(nextIcon.getImage().getScaledInstance(
				nextFrameButton.getHeight() - 4,
				nextFrameButton.getHeight() - 4, Image.SCALE_SMOOTH));
		nextFrameButton.setIcon(nextIcon);
		nextFrameButton.setEnabled(false);
		nextFrameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				nextFrame();
			}
		});

		buttonPanel.add(nextFrameButton);

		loopPlayToggleButton_ = new JToggleButton();
		loopPlayToggleButton_.setToolTipText("Loop selection");
		loopPlayToggleButton_.setFocusable(false);
		loopPlayToggleButton_.setBounds(136, 12, 50, 50);
		ImageIcon repeatIcon = new ImageIcon(
				Project.class.getResource("/repeat.gif"));
		repeatIcon.setImage(repeatIcon.getImage().getScaledInstance(
				loopPlayToggleButton_.getHeight() - 4,
				loopPlayToggleButton_.getHeight() - 4, Image.SCALE_SMOOTH));
		loopPlayToggleButton_.setIcon(repeatIcon);
		loopPlayToggleButton_.setEnabled(false);
		loopPlayToggleButton_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loopPlay();
			}
		});

		buttonPanel.add(loopPlayToggleButton_);

		BufferedImage atlasImage = null;
		try {
			atlasImage = ImageIO.read(Project.class.getResource("/icon_large.gif"));
			JLabel atlasLabel = new JLabel(new ImageIcon(atlasImage.getScaledInstance(300,290, Image.SCALE_SMOOTH)));
			//atlasLabel.setBounds(0,150,300,300);
			controlFrame.getContentPane().add(atlasLabel, BorderLayout.SOUTH);
		} catch (Exception e) {
			e.printStackTrace();
		}
		;


		JMenuBar menuBar = new JMenuBar();
		menuBar.addKeyListener(AnnotationKeyListener.getInstance());
		controlFrame.getContentPane().add(menuBar, BorderLayout.NORTH);

		mnFile = new JMenu("Project");
		menuBar.add(mnFile);

		saveProjectMenu = new JMenuItem("Save");
		saveProjectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_MASK));
		saveProjectMenu.setEnabled(false);
		saveProjectMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Project.getInstance().saveProject();
			}
		});

		newProjectMenu = new JMenuItem("New");
		newProjectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				InputEvent.CTRL_MASK));
		newProjectMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newProject();
			}
		});
		mnFile.add(newProjectMenu);
		mnFile.add(saveProjectMenu);

		loadProjectMenu = new JMenuItem("Load");
		loadProjectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,InputEvent.CTRL_MASK));
		loadProjectMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadProject();
			}
		});
		mnFile.add(loadProjectMenu);

		reloadProjectMenu = new JMenuItem("Reload");
		reloadProjectMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));
		reloadProjectMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				reloadProject();
			}
		});
		mnFile.add(reloadProjectMenu);

		shotMenu = new JMenuItem("Screenshot");
		shotMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				InputEvent.CTRL_MASK));
		shotMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				makeScreenshot();
			}
		});
		mnFile.add(shotMenu);

		JMenu mnFileExport = new JMenu("Export");
		mnFileExport.setMnemonic(KeyEvent.VK_E);
        JMenuItem exportLabelTrackAsCsv = new JMenuItem("LabelTrack as csv");
        exportLabelTrackAsCsv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                exportLabelTrackAsCsv();
            }
        });
        mnFileExport.add(exportLabelTrackAsCsv);
        JMenuItem exportContinuousLabelTrack = new JMenuItem("Sample Cont.LabelTrack as csv");
        exportContinuousLabelTrack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                exportContinuousLabelTrackAsCSV();
            }
        });
        mnFileExport.add(exportContinuousLabelTrack);
		mnFile.add(mnFileExport);

		closeMenuItem_2 = new JMenuItem("Exit");
		closeMenuItem_2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				InputEvent.CTRL_MASK));
		closeMenuItem_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				systemExit();
			}
		});
		mnFile.add(closeMenuItem_2);

		viewMenu = new JMenu("View");
		//viewMenu.setEnabled(false);
		menuBar.add(viewMenu);

		JMenuItem menuViewLabelProperties = new JMenuItem("LabelProperties");
		menuViewLabelProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				labelProps.setVisible(true);
				labelProps.toFront();
			}
		});
		menuViewLabelProperties.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_P, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		viewMenu.add(menuViewLabelProperties);

		JMenuItem menuViewClassEditor = new JMenuItem("ClassEditor");
		menuViewClassEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				classEditor.setVisible(true);
				classEditor.toFront();
			}
		});
		menuViewClassEditor.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_C, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		viewMenu.add(menuViewClassEditor);

		JMenuItem menuViewProjectCollectionEditor = new JMenuItem("ProColl editor");
		menuViewProjectCollectionEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				projectCollectionEditor.setVisible(true);
				projectCollectionEditor.toFront();
			}
		});
		menuViewProjectCollectionEditor.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_P, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		viewMenu.add(menuViewProjectCollectionEditor);

		JMenuItem menuViewTrackEditor = new JMenuItem("TrackEditor");
		menuViewTrackEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				trackEditor.setVisible(true);
				trackEditor.toFront();
			}
		});
		menuViewTrackEditor.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_T, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		viewMenu.add(menuViewTrackEditor);

		JMenuItem menuViewTrackTable = new JMenuItem("TrackTable");
		menuViewTrackTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				labelTabular.setVisible(true);
				labelTabular.toFront();
			}
		});
		menuViewTrackTable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		viewMenu.add(menuViewTrackTable);

		JMenuItem menuViewLabelLegend = new JMenuItem("LabelLegend");
		menuViewLabelLegend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				labelLegend.setVisible(true);
				labelLegend.toFront();
			}
		});
		menuViewLabelLegend.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_L, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		viewMenu.add(menuViewLabelLegend);

		JMenu mnSetVideoSpeed = new JMenu("set Video speed");
		viewMenu.add(mnSetVideoSpeed);
		ButtonGroup videoSpeedBtnGroup = new ButtonGroup();

		JRadioButtonMenuItem tenthSpeedRadioButton = new JRadioButtonMenuItem("1/10");
		tenthSpeedRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MessageManager.getInstance().playSpeedChanged(new PlaySpeedEvent(this, 0.1));
			}
		});
		mnSetVideoSpeed.add(tenthSpeedRadioButton);
		videoSpeedBtnGroup.add(tenthSpeedRadioButton);

		JRadioButtonMenuItem fourthSpeedRadioButton = new JRadioButtonMenuItem("1/4");
		fourthSpeedRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MessageManager.getInstance().playSpeedChanged(new PlaySpeedEvent(this, 0.25));
			}
		});
		mnSetVideoSpeed.add(fourthSpeedRadioButton);
		videoSpeedBtnGroup.add(fourthSpeedRadioButton);

		JRadioButtonMenuItem halfSpeedRadioButton = new JRadioButtonMenuItem("1/2");
		halfSpeedRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MessageManager.getInstance().playSpeedChanged(new PlaySpeedEvent(this, 0.5));
			}
		});
		mnSetVideoSpeed.add(halfSpeedRadioButton);
		videoSpeedBtnGroup.add(halfSpeedRadioButton);

		JRadioButtonMenuItem fullSpeedRadioButton = new JRadioButtonMenuItem("1");
		fullSpeedRadioButton.setSelected(true);
		fullSpeedRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MessageManager.getInstance().playSpeedChanged(new PlaySpeedEvent(this, 1.0));
			}
		});
		mnSetVideoSpeed.add(fullSpeedRadioButton);
		videoSpeedBtnGroup.add(fullSpeedRadioButton);

		JRadioButtonMenuItem twiceSpeedRadioButton = new JRadioButtonMenuItem("2");
		twiceSpeedRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MessageManager.getInstance().playSpeedChanged(new PlaySpeedEvent(this, 2.0));
			}
		});
		mnSetVideoSpeed.add(twiceSpeedRadioButton);
		videoSpeedBtnGroup.add(twiceSpeedRadioButton);

		JRadioButtonMenuItem fourSpeedRadioButton = new JRadioButtonMenuItem("4");
		fourSpeedRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MessageManager.getInstance().playSpeedChanged(new PlaySpeedEvent(this, 4.0));
			}
		});
		mnSetVideoSpeed.add(fourSpeedRadioButton);
		videoSpeedBtnGroup.add(fourSpeedRadioButton);

		JRadioButtonMenuItem tenSpeedRadioButton = new JRadioButtonMenuItem("10");
		tenSpeedRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MessageManager.getInstance().playSpeedChanged(new PlaySpeedEvent(this, 10.0));
			}
		});
		mnSetVideoSpeed.add(tenSpeedRadioButton);
		videoSpeedBtnGroup.add(tenSpeedRadioButton);


		JMenu mnSetVideoSize = new JMenu("set Video size");
		viewMenu.add(mnSetVideoSize);

		ButtonGroup videoZoomBtnGroup = new ButtonGroup();

		JRadioButtonMenuItem fourthRadioButton = new JRadioButtonMenuItem("1/4");
		fourthRadioButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
				InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		fourthRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AtlasProperties.getInstance().setVideoZoomFactor(0.25);
				// Project.getInstance().setVideoZoomFactor(0.25);
				MessageManager.getInstance().videoZoomChanged(new VideoZoomEvent(this, 0.25));//Project.getInstance().getMcoll().setVideoSize(0.25);
				if (AtlasProperties.getInstance().isAutoarranging()) {
					WindowManager.getInstance().autoarrange();
				}
			}
		});
		mnSetVideoSize.add(fourthRadioButton);
		videoZoomBtnGroup.add(fourthRadioButton);

		JRadioButtonMenuItem halfRadioButton = new JRadioButtonMenuItem("1/2");
		halfRadioButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,
				InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		halfRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AtlasProperties.getInstance().setVideoZoomFactor(0.5);
				// Project.getInstance().setVideoZoomFactor(0.5);
				MessageManager.getInstance().videoZoomChanged(new VideoZoomEvent(this, 0.5));//Project.getInstance().getMcoll().setVideoSize(0.5);
				if (AtlasProperties.getInstance().isAutoarranging()) {
					WindowManager.getInstance().autoarrange();
				}
			}
		});
		mnSetVideoSize.add(halfRadioButton);
		videoZoomBtnGroup.add(halfRadioButton);

		JRadioButtonMenuItem fullRadioButton = new JRadioButtonMenuItem("1/1");
		fullRadioButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3,
				InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		fullRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AtlasProperties.getInstance().setVideoZoomFactor(1.0);

				// Project.getInstance().setVideoZoomFactor(1.0);
				MessageManager.getInstance().videoZoomChanged(new VideoZoomEvent(this, 1));//Project.getInstance().getMcoll().setVideoSize(1.0);
				if (AtlasProperties.getInstance().isAutoarranging()) {
					WindowManager.getInstance().autoarrange();
				}
			}
		});
		fullRadioButton.setSelected(true);
		mnSetVideoSize.add(fullRadioButton);
		videoZoomBtnGroup.add(fullRadioButton);

		JRadioButtonMenuItem twiceRadioButton = new JRadioButtonMenuItem("2/1");
		twiceRadioButton.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4,
				InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		twiceRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Project.getInstance().setVideoZoomFactor(2.0);
				AtlasProperties.getInstance().setVideoZoomFactor(2.0);
				MessageManager.getInstance().videoZoomChanged(new VideoZoomEvent(this, 2));//Project.getInstance().getMcoll().setVideoSize(2.0);
				if (AtlasProperties.getInstance().isAutoarranging()) {
					WindowManager.getInstance().autoarrange();
				}
			}
		});
		mnSetVideoSize.add(twiceRadioButton);
		videoZoomBtnGroup.add(twiceRadioButton);

		ButtonGroup scrollBtnGroup = new ButtonGroup();

		setScrollModeMenu = new JMenu("set Scroll mode");
		viewMenu.add(setScrollModeMenu);

		rdbtnmntmHalf = new JRadioButtonMenuItem("half jump");
		rdbtnmntmHalf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Project.getInstance().setScrollType(ScrollType.HALF);
			}
		});
		setScrollModeMenu.add(rdbtnmntmHalf);
		scrollBtnGroup.add(rdbtnmntmHalf);
		rdbtnmntmHalf.setSelected(true);

		rdbtnmntmFullJump = new JRadioButtonMenuItem("full jump");
		rdbtnmntmFullJump.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Project.getInstance().setScrollType(ScrollType.FULL);
			}
		});
		setScrollModeMenu.add(rdbtnmntmFullJump);
		scrollBtnGroup.add(rdbtnmntmFullJump);

		rdbtnmntmSlide = new JRadioButtonMenuItem("slide");
		rdbtnmntmSlide.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Project.getInstance().setScrollType(ScrollType.SLIDE);
			}
		});
		setScrollModeMenu.add(rdbtnmntmSlide);
		scrollBtnGroup.add(rdbtnmntmSlide);

		muteVideoMenu = new JCheckBoxMenuItem("mute Video");
		muteVideoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
				InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		muteVideoMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Project.getInstance().getMcoll()
						.muteVideo(muteVideoMenu.isSelected());
			}
		});
		viewMenu.add(muteVideoMenu);

		autoArrangeWindowsMenu = new JCheckBoxMenuItem("auto arrange Videos");
		autoArrangeWindowsMenu.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_A, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		autoArrangeWindowsMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AtlasProperties.getInstance().setAutoarranging(
						autoArrangeWindowsMenu.isSelected());
				if (autoArrangeWindowsMenu.isSelected()) {
					WindowManager.getInstance().autoarrange();
				}
			}
		});
		viewMenu.add(autoArrangeWindowsMenu);

		JMenu mnViewAlwaysOnTop = new JMenu("Always on Top");
		viewMenu.add(mnViewAlwaysOnTop);

		aotAtlasMain = new JCheckBoxMenuItem("Atlas Main Window");
		aotAtlasMain.setSelected(false);
		aotAtlasMain.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (aotAtlasMain.isSelected()) {
					controlFrame.setAlwaysOnTop(true);
				} else {
					controlFrame.setAlwaysOnTop(false);
				}
			}
		});
		mnViewAlwaysOnTop.add(aotAtlasMain);

		aotLabelArea = new JCheckBoxMenuItem("Label Area");
		aotLabelArea.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (aotLabelArea.isSelected()) {
					lcollFrame.setAlwaysOnTop(true);
				} else {
					lcollFrame.setAlwaysOnTop(false);
				}
			}
		});
		mnViewAlwaysOnTop.add(aotLabelArea);

		aotLabelProps = new JCheckBoxMenuItem("LabelProperties");
		aotLabelProps.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (aotLabelProps.isSelected()) {
					labelProps.setAlwaysOnTop(true);
				} else {
					labelProps.setAlwaysOnTop(false);
				}
			}
		});
		mnViewAlwaysOnTop.add(aotLabelProps);

		aotTrackTable = new JCheckBoxMenuItem("TrackTable");
		aotTrackTable.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (aotTrackTable.isSelected()) {
					labelTabular.setAlwaysOnTop(true);
				} else {
					labelTabular.setAlwaysOnTop(false);
				}
			}
		});
		mnViewAlwaysOnTop.add(aotTrackTable);

		aotLabelLegend = new JCheckBoxMenuItem("LabelLegend");
		aotLabelLegend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (aotLabelLegend.isSelected()) {
					labelLegend.setAlwaysOnTop(true);
				} else {
					labelLegend.setAlwaysOnTop(false);
				}
			}
		});
		mnViewAlwaysOnTop.add(aotLabelLegend);

		aotSearchLabels = new JCheckBoxMenuItem("Search for Labels");
		aotSearchLabels.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (aotSearchLabels.isSelected()) {
					searchLabels.setAlwaysOnTop(true);
				} else {
					searchLabels.setAlwaysOnTop(false);
				}
			}
		});
		mnViewAlwaysOnTop.add(aotSearchLabels);

		addMenu = new JMenu("Add");
		addMenu.setEnabled(false);
		menuBar.add(addMenu);

		JMenuItem mntmNewLabelTrackMenuItem = new JMenuItem("empty LabelTrack");
		addMenu.add(mntmNewLabelTrackMenuItem);
		mntmNewLabelTrackMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addLabelTrack();
			}
		});

		JMenuItem mntmNewScalarTrackMenuItem = new JMenuItem("empty ScalarTrack");
		addMenu.add(mntmNewScalarTrackMenuItem);
		mntmNewScalarTrackMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addScalarTrack();
			}
		});

		importMenu = new JMenu("Import");
		importMenu.setEnabled(false);
		menuBar.add(importMenu);

		mntmImportLabeltrack = new JMenuItem("LabelTrack");
		mntmImportLabeltrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				ImportLabelTrack.showDialog(controlFrame,
						"Add a LabelTrack to Collection", Project.getInstance()
								.getLcoll(), Project.getInstance()
								.getProjectPath());
			}
		});
		importMenu.add(mntmImportLabeltrack);

		mntmImportScalartrack = new JMenuItem("ScalarTrack");
		mntmImportScalartrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				importScalarTrack();
			}
		});
		importMenu.add(mntmImportScalartrack);

		mntmImportVectortrack = new JMenuItem("VectorTrack");
		mntmImportVectortrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				importVectorTrack();
			}
		});
		importMenu.add(mntmImportVectortrack);

		mntmImportDatatrack = new JMenuItem("DataTrack");
		mntmImportDatatrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				importDataTrack();
			}
		});
		importMenu.add(mntmImportDatatrack);

		mntmImportClass = new JMenuItem("Class");
		mntmImportClass.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addLabelClass();
			}
		});
		importMenu.add(mntmImportClass);



		mntmImportAudio = new JMenuItem("Audio");
		mntmImportAudio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				importAudioTrack();
			}
		});
		importMenu.add(mntmImportAudio);

		mntmImportVideo = new JMenuItem("Video");
		mntmImportVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				importVideoTrack();
			}
		});
		importMenu.add(mntmImportVideo);

		mntmLiveData = new JMenuItem("LiveData");
		mntmLiveData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				liveData.setVisible(true);
			}
		});
		importMenu.add(mntmLiveData);

		mnTools = new JMenu("Tools");
		mnTools.setEnabled(false);
		menuBar.add(mnTools);

		mntmDebug = new JMenuItem("Execute debug command");
		mntmDebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// learningWindow.setLocation(0, 0);
				execDebug();
			}
		});
		mnTools.add(mntmDebug);

		mntmCommandline = new JMenuItem("Execute command");
		mntmCommandline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// learningWindow.setLocation(0, 0);
				execCommand();
			}
		});
		mnTools.add(mntmCommandline);

		mntmMatlabIO = new JMenuItem("Execute Matlab function");
		mntmMatlabIO.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// learningWindow.setLocation(0, 0);
				matlabIO.setVisible(true);
			}
		});
		mnTools.add(mntmMatlabIO);

		mntmActiveLearning = new JMenuItem("Active Learning (alpha)");
		mntmActiveLearning.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// learningWindow.setLocation(0, 0);
				learningWindow.setVisible(true);
			}
		});
		mnTools.add(mntmActiveLearning);

		mntmSuggestLabels = new JMenuItem("Suggest Labels");
		mntmSuggestLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				suggestLabels();
			}
		});
		mnTools.add(mntmSuggestLabels);

		mntmSpeakerSegementation = new JMenuItem("Speaker Segementation");
		mntmSpeakerSegementation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				segmentSpeakers();
			}

		});
		mnTools.add(mntmSpeakerSegementation);

		JMenuItem mntmScalartracklabels = new JMenuItem(
				"ScalarTrack --> Labels");
		mntmScalartracklabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				scalarTrack2Labels.setVisible(true);
			}
		});
		mnTools.add(mntmScalartracklabels);

		JMenuItem mntmClearLabeltrack = new JMenuItem("Clear LabelTrack");
		mntmClearLabeltrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clearLabelTrack.setVisible(true);
			}
		});
		mnTools.add(mntmClearLabeltrack);

		JMenuItem mntmShiftAllLabels = new JMenuItem("Shift all Labels");
		mntmShiftAllLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				shiftLabels.setVisible(true);
			}
		});
		mnTools.add(mntmShiftAllLabels);

		JMenuItem mntmSearchLabels = new JMenuItem("Search for Labels");
		mntmSearchLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				searchLabels.setVisible(true);
			}
		});
		mnTools.add(mntmSearchLabels);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmHelp = new JMenuItem("About");
		mntmHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(controlFrame,
						"Annotation-Tool, Version " + version);
			}
		});
		mnHelp.add(mntmHelp);

		// init JavaHelp and add gui menue Help element
		JMenuItem mnHelpUpstart = new JMenuItem("Help");
		String hsName = "jhelpset.hs";
		try {
			HelpSet hs = null;
			ClassLoader cl = Atlas.class.getClassLoader();
			URL hsURL = HelpSet.findHelpSet(cl, hsName);
			hs = new HelpSet(null, hsURL);
			HelpBroker hb = hs.createHelpBroker();

			mnHelpUpstart.addActionListener(new CSH.DisplayHelpFromSource(hb));

		} catch (Exception ee) {
			System.err.println("HelpSet " + hsName + " not found");
			mnHelpUpstart.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					System.err
							.println("CALLED HELP - javahelp helpset not found - doing nothing");
				}
			});
		}
		mnHelp.add(mnHelpUpstart);

		lcollFrame.setVisible(true);
		controlFrame.setVisible(true);
		labelProps.setVisible(false);

		guiThread = new GuiUpdater("Updater", Project.getInstance().getMcoll());
		thread = new Thread(guiThread);
		thread.start();
		trackEditor = new TrackEditor(Project.getInstance().getLcoll(), Project
				.getInstance().getMcoll());
		trackEditor.setIconImage(new ImageIcon(Project.class
				.getResource("/icon_trackEditorFrame.gif")).getImage());
		MessageManager.getInstance().addLabelSelectionListener(
				new LabelSelectionListener() {
					@Override
					public void selectionChanged(LabelSelectionEvent e) {
						activeLabel = e.getLabelObject();

					}
				});
		MessageManager.getInstance().addPlayPauseListener(
				new PlayPauseListener() {

					@Override
					public void PlayPause(PlayPauseEvent e) {
						if (!(e.getSource().getClass().toString()
								.equals(Atlas.class
										.toString()))) {
							if (e.isLooping()) {
								loopPlayToggleButton_.doClick();
							} else {
								playButton.doClick();
							}
						}
					}
				});
		
		MessageManager.getInstance().addProjectLoadedListener(
				new ProjectLoadedListener() {

					@Override
					public void projectLoaded(ProjectLoadedEvent e) {
						activateButtons();
						lcollFrame.setTitle("Atlas - LabelArea - "
								+ Project.getInstance().getName());
					}
				});
		MessageManager.getInstance().addVideoZoomChangedListener(new VideoZoomChangedListener() {
			@Override
			public void videoZoomChanged(VideoZoomEvent e) {
				if (!(e.getSource().getClass().toString().equals(Atlas.class.toString()))) {
					if (e.getFactor() == 0.25) {
						videoZoomBtnGroup.clearSelection();
						fourthRadioButton.setSelected(true);
					} else if (e.getFactor() == 0.5) {
						videoZoomBtnGroup.clearSelection();
						halfRadioButton.setSelected(true);
					} else if (e.getFactor() == 1) {
						videoZoomBtnGroup.clearSelection();
						fullRadioButton.setSelected(true);
					} else if (e.getFactor() == 2) {
						videoZoomBtnGroup.clearSelection();
						twiceRadioButton.setSelected(true);
					}
				}
			}
		});
		// add listener_ for JFrame position
		AtlasProperties.getInstance().addJFrameBoundsWatcher(
				"controlFrame", controlFrame, true, false);
		AtlasProperties.getInstance().addJFrameBoundsWatcher("lcollFrame",
				lcollFrame, true, true);

		if (null != projectXml) {
			System.out.println("loading project from command line arguments: " + projectXml);
			loadProject(projectXml);
		}
	}

	private void execDebug() {
		ObjectLine oL = Project.getInstance().getLcoll().getTrackByName("ScalarTrack");
		ScalarTrack sT=null;
		if(oL.getTrack() instanceof ScalarTrack)sT=(ScalarTrack)oL.getTrack();
		ArrayList<Double> time = new ArrayList<>();
		ArrayList<Double> val = new ArrayList<>();
		time.add(500.0);
		time.add(600.0);
		time.add(700.0);
		time.add(800.0);
		val.add(.5);
		val.add(.5);
		val.add(.5);
		val.add(.5);
		sT.appendDataUnsafe(10,val);
		MessageManager.getInstance().requestTrackUpdate(new UpdateTracksEvent(this));
	}

	private void execCommand() {

		String s = JOptionPane.showInputDialog(null,"Exec string",Project.getInstance().getCommandlineString());
		if(s==null)return;
		Project.getInstance().setCommandlineString(s);

		Runtime rt = Runtime.getRuntime();
		try {
			Process pr = rt.exec(Project.getInstance().getCommandlineString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void exportContinuousLabelTrackAsCSV() {
        ContinuousLabelTrackCsvExporter export = new ContinuousLabelTrackCsvExporter();
        export.showDialog(controlFrame, "Export a LabelTrack", Project.getInstance().getMcoll(), Project.getInstance().getLcoll(), Project.getInstance().getProjectPath());
    }

    private void suggestLabels() {
		simpleActiveLearner.setVisible(true);
	}

	private void segmentSpeakers() {
		speakerSegmenter.setVisible(true);
		
	}

	private void makeScreenshot() {
		JFileChooser fc = new JFileChooser(Project.getInstance()
				.getProjectPath());
		FileFilter ff = null;
		ff = new FileNameExtensionFilter("png", "png");
		fc.setFileFilter(ff);
		fc.setAcceptAllFileFilterUsed(false);

		int returnVal = fc.showSaveDialog(controlFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			int height = lcollFrame.getHeight();
			int width = lcollFrame.getWidth();
			BufferedImage img = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			lcollFrame.paint(img.getGraphics());
			try {
				String outName = fc.getSelectedFile().toString();

				// if no file extention exists and one is selected, append it
				if (!outName.matches(".*\\.[pP][nN][gG]$")) {
					outName += ".png";
				}
				ImageIO.write(img, "png", new File(outName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void systemExit() {
		stopPlayback();

		// save properties
		AtlasProperties.getInstance().write();

		if (Project.getInstance().getProjectPath().length() > 0) {
			Object[] options = { "yes", "no", "cancel" };
			int n = JOptionPane
					.showOptionDialog(
							controlFrame,
							"Do you want to save the current project before exiting Atlas?",
							"Save Project?", JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[0]);
			if (n == 2) {// cancel
				return;
			} else if (n == 0) {// yes
				Project.getInstance().saveProject();
			}
		}
		System.exit(0);
	}

	private void importDataTrack() {
		ImportDataTrack.showDialog(controlFrame,
				"Add a DataTrack to Collection", Project.getInstance()
						.getLcoll(), Project.getInstance().getProjectPath());
	}

	private void importVectorTrack() {
		ImportVectorTrack.showDialog(controlFrame,
				"Add a VectorTrack to Collection", Project.getInstance()
						.getLcoll(), Project.getInstance().getProjectPath());
	}

	private void importScalarTrack() {
		ImportScalarTrack.showDialog(controlFrame,
				"Add a ScalarTrack to Collection", Project.getInstance()
						.getLcoll(), Project.getInstance().getProjectPath());
	}

	private void importAudioTrack() {
		ImportAudio.showDialog(controlFrame, "Import an AudioTrack", Project
				.getInstance().getMcoll(), Project.getInstance().getLcoll(),
				Project.getInstance().getProjectPath());
	}

	private void importVideoTrack() {
		ImportVideo.showDialog(controlFrame, "Import a VideoTrack", Project
				.getInstance().getMcoll(), Project.getInstance()
				.getProjectPath());
		controlFrame.toFront();
	}

	private void newProject() {
		if (Project.getInstance().getProjectPath().length() > 0) {
			Object[] options = { "yes", "no", "cancel" };
			int n = JOptionPane.showOptionDialog(controlFrame,
					"Do you want to save the current project?",
					"Save Project?", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 2) {// cancel
				return;
			} else if (n == 0) {// yes
				Project.getInstance().saveProject();
			}
			Object[] options2 = { "yes", "no" };
			n = JOptionPane.showOptionDialog(null,
					"Do you want to keep LabelClasses?", "Keep Classes?",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, options2, options2[0]);
			if (n == 1) {// no
				// kill classes
				LabelClasses.getInstance().deleteAll();
				LabelClasses.getInstance().addGenericClass();

				if (this.classEditor.isVisible()) {
					// classes changed
					MessageManager.getInstance().requestClassChanged(
							new ClassChangedEvent(this));
				} else {
				}
			}

			// no project existing currently
		} else {
			// generic class
			LabelClass l = new LabelClass("generic");
			l.addEntity(new LabelClassEntity(l, "labeled", 0, Color.lightGray, Color.darkGray));
			LabelClasses.getInstance().addClass(l);
		}

		if (NewProject.showDialog(controlFrame, "Start a new Project")) {
			activateButtons();
			Project.getInstance().saveProject();
		}

	}

	private void activateButtons() {
		this.saveProjectMenu.setEnabled(true);
		this.viewMenu.setEnabled(true);
		this.importMenu.setEnabled(true);
		this.addMenu.setEnabled(true);
		this.mnTools.setEnabled(true);

		this.loopPlayToggleButton_.setEnabled(true);
		this.stopButton.setEnabled(true);
		this.playButton.setEnabled(true);
		this.prevFrameButton.setEnabled(true);
		this.nextFrameButton.setEnabled(true);

	}

	private void addLabelClass() {
		JFileChooser fc = new JFileChooser();
		FileFilter ff = new FileNameExtensionFilter("XML", "xml");
		fc.setFileFilter(ff);

		int returnVal = fc.showOpenDialog(controlFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			LabelClass tmp;
			tmp = new LabelClass(fc.getSelectedFile());
			LabelClasses.getInstance().addClass(tmp);
			MessageManager.getInstance().requestClassChanged(
					new ClassChangedEvent(this));
		}
	}

	private void exportLabelTrackAsCsv() {
		LabelTrackCsvExporter export = new LabelTrackCsvExporter();
		export.showDialog(controlFrame, "Export a LabelTrack", Project
				.getInstance().getMcoll(), Project.getInstance().getLcoll(),
				Project.getInstance().getProjectPath());
	}

	private void reloadProject(){
		stopPlayback();
		if (Project.getInstance().getProjectPath().length() > 0) {
			String s = Project.getInstance().getProjectFile().getAbsolutePath();

			Project.getInstance().loadProject(s, true);
		}
	}

	private void loadProject(String projectXmlPath) {
		Project.getInstance().loadProject(projectXmlPath, false);
	}

	private void loadProject() {
		stopPlayback();
		if (Project.getInstance().getProjectPath().length() > 0) {
			Object[] options = { "yes", "no", "cancel" };
			int n = JOptionPane.showOptionDialog(controlFrame,
					"Do you want to save the current project?",
					"Save Project?", JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 2) {// cancel
				return;
			} else if (n == 0) {// yes
				Project.getInstance().saveProject();
			}
		}
		JFileChooser fc = new JFileChooser(Project.getInstance().getProjectPath());
		FileFilter ff = null;
		ff = new FileNameExtensionFilter("xml (project)", "xml");
		fc.setFileFilter(ff);
		fc.setAcceptAllFileFilterUsed(false);

		int returnVal = fc.showOpenDialog(controlFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Project.getInstance().getLcoll().reset();
			Project.getInstance().getMcoll().reset();
			// kill classes
			LabelClasses.getInstance().deleteAll();
			MessageManager.getInstance().requestTrackUpdate(new UpdateTracksEvent(this));
			MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			Project.getInstance().loadProject(fc.getSelectedFile().getPath(),false);
			controlFrame.toFront();
		}
	}

	/**
	 * Stops playback of video and audio - use when asking critical dialogs like
	 * project closing...
	 */
	void stopPlayback() {
		if (playButton.isSelected() || this.loopPlayToggleButton_.isSelected()) {
			this.loopPlayToggleButton_.setEnabled(true);
			this.stopButton.setEnabled(true);
			this.prevFrameButton.setEnabled(true);
			this.nextFrameButton.setEnabled(true);
			guiThread.setPlaying(false);
			guiThread.setLooping(false);
			playButton.setSelected(false);
			loopPlayToggleButton_.setSelected(false);

			Project.getInstance().getMcoll().pause();
		}
	}

	private void nextFrame() {
		Project.getInstance().getMcoll().nextFrame();
	}

	private void prevFrame() {
		Project.getInstance().getMcoll().prevFrame();
	}

	private void addLabelTrack() {
		AddLabelTrack.showDialog(controlFrame, "Add a LabelTrack to Collection",
				Project.getInstance().getLcoll(), Project.getInstance()
						.getProjectPath());
	}
	private void addScalarTrack(){
		AddScalarTrack.showDialog(controlFrame, "Add a ScalarTrack to Collection", Project.getInstance().getLcoll(), Project.getInstance().getProjectPath());
	}

	private void startoverButton() {
		MessageManager.getInstance().timeChanged(new TimeEvent(this, 0));
		MessageManager.getInstance().slotChanged(new SlotEvent(this, 0));
	}

	private void loopPlay() {
		// if useLoopArea is selected, else loop selected label
		// if ( useLoopArea){
		if (Project.getInstance().getLcoll().getTimeTrack_().getTimetrackP()
				.isUseLoopArea()) {
			if (this.loopPlayToggleButton_.isSelected()) {
				this.playButton.setEnabled(false);
				this.stopButton.setEnabled(false);
				this.prevFrameButton.setEnabled(false);
				this.nextFrameButton.setEnabled(false);
				MessageManager.getInstance()
						.timeChanged(
								new TimeEvent(this, Project.getInstance()
										.getLcoll().getTimeTrack_()
										.getTimetrackP().getLoopStart()));
				guiThread.setPlaying(false);
				guiThread.setLooping(true);
				guiThread.setTimestamp(System.currentTimeMillis());
				Project.getInstance().getMcoll().play();
				MessageManager.getInstance().playPause(
						new PlayPauseEvent(this, true));
			} else {
				this.playButton.setEnabled(true);
				this.stopButton.setEnabled(true);
				this.prevFrameButton.setEnabled(true);
				this.nextFrameButton.setEnabled(true);
				guiThread.setPlaying(false);
				guiThread.setLooping(false);
				Project.getInstance().getMcoll().pause();
				MessageManager.getInstance().playPause(
						new PlayPauseEvent(this, true));
			}
		} else if (this.activeLabel != null) {
			if (this.loopPlayToggleButton_.isSelected()) {
				this.playButton.setEnabled(false);
				this.stopButton.setEnabled(false);
				this.prevFrameButton.setEnabled(false);
				this.nextFrameButton.setEnabled(false);
				MessageManager.getInstance().timeChanged(
						new TimeEvent(this, this.activeLabel.getStart()));
				guiThread.setActiveLabel(this.activeLabel);
				guiThread.setPlaying(false);
				guiThread.setLooping(true);
				guiThread.setTimestamp(System.currentTimeMillis());
				Project.getInstance().getMcoll().play();
				MessageManager.getInstance().playPause(
						new PlayPauseEvent(this, true));
			} else {
				this.playButton.setEnabled(true);
				this.stopButton.setEnabled(true);
				this.prevFrameButton.setEnabled(true);
				this.nextFrameButton.setEnabled(true);
				guiThread.setPlaying(false);
				guiThread.setLooping(false);
				Project.getInstance().getMcoll().pause();
				MessageManager.getInstance().playPause(
						new PlayPauseEvent(this, true));
			}

		} else {
			this.loopPlayToggleButton_.setSelected(false);
		}
	}

	private void playpauseButton() {
		if (playButton.isSelected()) {
			this.loopPlayToggleButton_.setEnabled(false);
			this.stopButton.setEnabled(false);
			this.prevFrameButton.setEnabled(false);
			this.nextFrameButton.setEnabled(false);
			Project.getInstance().getMcoll().play();

			guiThread.setPlaying(true);
			guiThread.setLooping(false);
		} else {
			this.loopPlayToggleButton_.setEnabled(true);
			this.stopButton.setEnabled(true);
			this.prevFrameButton.setEnabled(true);
			this.nextFrameButton.setEnabled(true);
			guiThread.setPlaying(false);
			guiThread.setLooping(false);

			Project.getInstance().getMcoll().pause();
		}
		MessageManager.getInstance().playPause(new PlayPauseEvent(this, false));
	}

	protected boolean isPlaying() {
		return guiThread.isPlaying();
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// ignore

	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// ignore

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// save properties
		systemExit();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// ignore
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// ignore
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// ignore
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// ignore
	}

	public static void main(String[] args) {
		if (null != args) {
			if (0 < args.length) {
				projectXml_ = args[0];
			}
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new Atlas(projectXml_);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}

package de.atlas.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JTextField;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import de.atlas.collections.LabelTrack;
import de.atlas.collections.ObjectLine;
import de.atlas.data.ClassifikationResult;
import de.atlas.data.LabelObject;
import de.atlas.data.LabelType;
import de.atlas.data.Project;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.messagesystem.UpdateTracksListener;
import de.atlas.misc.AtlasProperties;
import de.atlas.misc.Classifier;
import de.atlas.misc.HelperFunctions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;

import javax.swing.JSeparator;

public class LearningWindow extends JFrame implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Classifier clf = new Classifier();
	private JTextField svmCommandlineTextField;
	private JComboBox labelTrackReadComboBox;
	private JComboBox labelTrackWriteComboBox;
	private JComboBox labelTrackSelectionComboBox;
	private JComboBox featureTrackComboBox;
	private JCheckBox chckbxCreateProbabilityScalartrack;
	private JTextField textFieldThreshold;
	private JTextField textFieldBlockLength;
	private JTextField textFieldSuperBlockAmount;
	private JButton btnLearn;
	private JButton btnLoad;
	private JButton btnSave;
	private JButton btnClassify;
	private JCheckBox chkVote;

	/**
	 * Create the frame.
	 */
	public LearningWindow() {
		setResizable(false);
		setTitle("Magic Window");
		setBounds(0, 0, 322, 570);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JLabel lblSvmCommandline = new JLabel("SVM Command:");
		lblSvmCommandline.setBounds(12, 12, 134, 15);

		svmCommandlineTextField = new JTextField();
		svmCommandlineTextField.setBounds(135, 12, 173, 19);
		svmCommandlineTextField.setText("-t 1 -d 2 -b 1");
		svmCommandlineTextField.setColumns(30);
		contentPane.setLayout(null);
		contentPane.add(lblSvmCommandline);
		contentPane.add(svmCommandlineTextField);

		JLabel lblLabeltrackToLearn = new JLabel("LabelTrack to read from");
		lblLabeltrackToLearn.setBounds(12, 145, 193, 15);
		contentPane.add(lblLabeltrackToLearn);

		labelTrackReadComboBox = new JComboBox();
		labelTrackReadComboBox.setBounds(12, 160, 296, 37);
		contentPane.add(labelTrackReadComboBox);

		btnLearn = new JButton("learn");
		btnLearn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				triggerLearning();
			}
		});
		btnLearn.setBounds(12, 291, 90, 25);
		contentPane.add(btnLearn);

		btnLoad = new JButton("load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(Project.getInstance()
						.getProjectPath());
				FileFilter ff = null;
				ff = new FileNameExtensionFilter("Support Vector Machine",
				"svm");
				fc.setFileFilter(ff);

				int returnVal = fc.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					clf.loadSVM(fc.getSelectedFile());
				}
			}
		});
		btnLoad.setBounds(115, 291, 90, 25);
		contentPane.add(btnLoad);

		btnSave = new JButton("save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(Project.getInstance()
						.getProjectPath());
				FileFilter ff = null;
				ff = new FileNameExtensionFilter("Support Vector Machine",
				"svm");
				fc.setFileFilter(ff);

				int returnVal = fc.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					clf.saveSVM(fc.getSelectedFile());
				}
			}
		});
		btnSave.setBounds(218, 291, 90, 25);
		contentPane.add(btnSave);

		btnClassify = new JButton("classify");
		btnClassify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				classify();
			}
		});
		btnClassify.setBounds(12, 264, 90, 25);
		contentPane.add(btnClassify);

		JLabel lblLabeltrackToClassify = new JLabel("LabelTrack to write to");
		lblLabeltrackToClassify.setBounds(12, 200, 193, 15);
		contentPane.add(lblLabeltrackToClassify);

		labelTrackWriteComboBox = new JComboBox();
		labelTrackWriteComboBox.setBounds(12, 215, 296, 37);
		contentPane.add(labelTrackWriteComboBox);

		labelTrackSelectionComboBox = new JComboBox();
		labelTrackSelectionComboBox.setBounds(12, 50, 296, 37);
		contentPane.add(labelTrackSelectionComboBox);

		JLabel lblDataSelectionTrack = new JLabel("FeatureTrack");
		lblDataSelectionTrack.setBounds(12, 90, 193, 15);
		contentPane.add(lblDataSelectionTrack);

		featureTrackComboBox = new JComboBox();
		featureTrackComboBox.setBounds(12, 105, 296, 37);
		contentPane.add(featureTrackComboBox);

		JLabel label = new JLabel("Data Selection Track");
		label.setBounds(12, 35, 193, 15);
		contentPane.add(label);

		chckbxCreateProbabilityScalartrack = new JCheckBox(
		"create conf. ScalarTrack");
		chckbxCreateProbabilityScalartrack.setBounds(115, 263, 233, 23);
		contentPane.add(chckbxCreateProbabilityScalartrack);

		JSeparator separator = new JSeparator();
		separator.setBounds(12, 329, 296, 8);
		contentPane.add(separator);

		textFieldThreshold = new JTextField();
		textFieldThreshold.setText("0.01");
		textFieldThreshold.setBounds(241, 363, 62, 19);
		contentPane.add(textFieldThreshold);
		textFieldThreshold.setColumns(10);

		JLabel lblUsefulThings = new JLabel("Useful things:");
		lblUsefulThings.setBounds(12, 335, 134, 15);
		contentPane.add(lblUsefulThings);

		JButton btnScalarTrackToLabels = new JButton("ScalarTrack -> Labels");
		btnScalarTrackToLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				scalarTrackToLabels(Double.parseDouble(textFieldThreshold
						.getText()));
			}
		});
		btnScalarTrackToLabels.setBounds(12, 360, 217, 25);
		contentPane.add(btnScalarTrackToLabels);

		JButton btnBlockLabels = new JButton("create block labels");
		btnBlockLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				createBlockLabels(Integer.parseInt(textFieldBlockLength
						.getText()));
			}
		});
		btnBlockLabels.setBounds(12, 395, 217, 25);
		contentPane.add(btnBlockLabels);

		textFieldBlockLength = new JTextField();
		textFieldBlockLength.setText("10000");
		textFieldBlockLength.setBounds(241, 398, 62, 19);
		contentPane.add(textFieldBlockLength);
		textFieldBlockLength.setColumns(10);

		JButton btnSuperBlock = new JButton("create superBlocks");
		btnSuperBlock.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				createSuperBlock(Double.parseDouble(textFieldSuperBlockAmount
						.getText()));
			}

		});
		btnSuperBlock.setBounds(12, 430, 217, 25);
		contentPane.add(btnSuperBlock);

		textFieldSuperBlockAmount = new JTextField();
		textFieldSuperBlockAmount.setText("0.1");
		textFieldSuperBlockAmount.setBounds(241, 433, 62, 19);
		contentPane.add(textFieldSuperBlockAmount);
		textFieldSuperBlockAmount.setColumns(10);

		JButton btnClearLabeltrack = new JButton("clear LabelTrack");
		btnClearLabeltrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((LabelTrack) ((ObjectLine) (labelTrackWriteComboBox
						.getSelectedItem())).getTrack()).clear();
			}
		});
		btnClearLabeltrack.setBounds(12, 465, 217, 25);
		contentPane.add(btnClearLabeltrack);

		JButton btnWriteSVMProblem = new JButton("write SVM-problem file");
		btnWriteSVMProblem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(Project.getInstance()
						.getProjectPath());
				FileFilter ff = null;
				ff = new FileNameExtensionFilter("SVM-problem", "prob");
				fc.setFileFilter(ff);

				int returnVal = fc.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					writeProblem(fc.getSelectedFile());
				}
			}
		});
		btnWriteSVMProblem.setBounds(12, 500, 217, 25);
		contentPane.add(btnWriteSVMProblem);

		chkVote = new JCheckBox("vote");
		chkVote.setBounds(241, 460, 193, 23);
		contentPane.add(chkVote);

		MessageManager.getInstance().addUpdateTracksListener(
				new UpdateTracksListener() {
					@Override
					public void updateTracks(UpdateTracksEvent e) {
						if (!(e.getSource().getClass().toString()
								.equals(LearningWindow.class
										.toString()))) {
							buildTrackList();
						}

					}
				});
		AtlasProperties.getInstance().addJFrameBoundsWatcher("learnWin",
				this, true, false);
	}

	private void buildTrackList() {
		DefaultComboBoxModel learnListModel = new DefaultComboBoxModel();
		DefaultComboBoxModel classifyListModel = new DefaultComboBoxModel();
		DefaultComboBoxModel selctionListModel = new DefaultComboBoxModel();
		DefaultComboBoxModel featureListModel = new DefaultComboBoxModel();
		Iterator<ObjectLine> iT = Project.getInstance().getLcoll().getList()
		.iterator();
		while (iT.hasNext()) {
			ObjectLine ol = iT.next();
			if (ol.getTrack() instanceof LabelTrack) {
				learnListModel.addElement(ol);
				classifyListModel.addElement(ol);
				selctionListModel.addElement(ol);
			}
			if (ol.isLearnable())
				featureListModel.addElement(ol);
		}
		labelTrackReadComboBox.setModel(learnListModel);
		labelTrackWriteComboBox.setModel(classifyListModel);
		this.labelTrackSelectionComboBox.setModel(selctionListModel);
		this.featureTrackComboBox.setModel(featureListModel);
	}

	private void triggerLearning() {
		new Thread(this).start();
	}

	private void createSuperBlock(double amount) {
		LabelTrack inTrack = (LabelTrack) ((ObjectLine) (labelTrackReadComboBox
				.getSelectedItem())).getTrack();
		LabelTrack outTrack = (LabelTrack) ((ObjectLine) (labelTrackWriteComboBox
				.getSelectedItem())).getTrack();

		ArrayList<LabelObject> labels = inTrack.getLabels();
		double[] vals = new double[labels.size()];
		for (int i = 0; i < labels.size(); i++) {
			vals[i] = labels.get(i).getValue();
		}
		java.util.Arrays.sort(vals);
		//System.out.print(vals.length);

		for (int i = vals.length - 1; i > vals.length * (1 - amount); i--) {
			for (int l = 0; l < labels.size(); l++) {
				if (vals[i] == labels.get(l).getValue()) {
					outTrack.addLabel(new LabelObject(labels.get(l).getText(),
							labels.get(l).getComment(), labels.get(l)
							.getStart(), labels.get(l).getEnd(), labels
							.get(l).getValue(), labels.get(l)
							.getLabelType(), labels.get(l)
							.getLabelClass(), labels.get(l)
							.getLabelClassEntity(), System
							.currentTimeMillis()));

				}
			}
		}
		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));

	}

	private void createBlockLabels(int blockLength) {
		LabelTrack inTrack = (LabelTrack) ((ObjectLine) (labelTrackReadComboBox
				.getSelectedItem())).getTrack();
		LabelTrack outTrack = (LabelTrack) ((ObjectLine) (labelTrackWriteComboBox
				.getSelectedItem())).getTrack();
		int classCount = inTrack.getLabelClass().getLabelClassEntities().size();
		HashMap<String, Integer> classesToInt = new HashMap<String, Integer>();
		HashMap<Integer, String> intToClasses = new HashMap<Integer, String>();
		for (int i = 0; i < classCount; i++) {
			classesToInt.put(inTrack.getLabelClass().getLabelClassEntities()
					.get(i).getName(), i);
			intToClasses.put(i, inTrack.getLabelClass().getLabelClassEntities()
					.get(i).getName());
		}
		for (long i = 0; i < Project.getInstance().getProjectLength(); i += blockLength) {
			double[] confidences = new double[classCount];
			ArrayList<LabelObject> labels = inTrack.getLabels(i, i
					+ blockLength);
			for (int l = 0; l < labels.size(); l++) {
				if (chkVote.isSelected()) {
					confidences[classesToInt.get(labels.get(l)
							.getLabelClassEntity().getName())] += 1 * (labels
									.get(l).getEnd() - labels.get(l).getStart());
				} else {
					confidences[classesToInt.get(labels.get(l)
							.getLabelClassEntity().getName())] += (labels
									.get(l).getValue() * (labels.get(l).getEnd() - labels
											.get(l).getStart()));
				}
			}
			double total = 0;
			double max = 0;
			int maxI = -1;
			for (int l = 0; l < classCount; l++) {
				total += confidences[l];
				if (max < confidences[l]) {
					max = confidences[l];
					maxI = l;
				}
			}
			if (max > 0) {
				outTrack.addLabel(new LabelObject(intToClasses.get(maxI), "",
						i, i + blockLength, max / total, LabelType.AUTOMATIC,
						outTrack.getLabelClass(), outTrack.getLabelClass()
						.getEntityByName(intToClasses.get(maxI)),
						System.currentTimeMillis()));
			}
		}
		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));

	}

	private void scalarTrackToLabels(double thresh) {

		LabelTrack selectionTrack = (LabelTrack) ((ObjectLine) (labelTrackSelectionComboBox
				.getSelectedItem())).getTrack();
		ObjectLine dataTrack = (ObjectLine) ((ObjectLine) (featureTrackComboBox
				.getSelectedItem()));
		LabelTrack destinationLabelTrack = (LabelTrack) ((ObjectLine) (labelTrackWriteComboBox
				.getSelectedItem())).getTrack();

		ArrayList<LabelObject> selectionLabels = selectionTrack.getLabels(0l,
				(long) Project.getInstance().getProjectLength());
		Iterator<LabelObject> iL = selectionLabels.iterator();

		while (iL.hasNext()) {
			LabelObject tmp = iL.next();
			double[][] tmpData = dataTrack
			.getData(tmp.getStart(), tmp.getEnd());
			double[] tmpDataTimePoints = dataTrack.getDataTimePoints(
					tmp.getStart(), tmp.getEnd());
			boolean above = false;
			double start = 0, end = 0;
			for (int i = 0; i < tmpData.length; i++) {
				if (tmpData[i][0] >= thresh && !above) {
					above = true;
					start = tmpDataTimePoints[i];
				}
				if (tmpData[i][0] < thresh && above) {
					above = false;
					end = tmpDataTimePoints[i];
					destinationLabelTrack.addLabel(new LabelObject(
							"above thresh (" + thresh + ")", "", (long) start,
							(long) end, 0.0, LabelType.AUTOMATIC,
							destinationLabelTrack.getLabelClass(), null, System
							.currentTimeMillis()));
				}
			}
			if (above) {
				destinationLabelTrack.addLabel(new LabelObject("above thresh ("
						+ thresh + ")", "", (long) start, (long) end, 0.0,
						LabelType.AUTOMATIC, destinationLabelTrack
						.getLabelClass(), null, System
						.currentTimeMillis()));
			}
		}
		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
	}

	// very very similar to learn!!!!!
	private void writeProblem(File file) {
		ObjectLine dataTrack = null;
		LabelTrack labelTrack = null;
		LabelTrack selectionTrack = null;

		labelTrack = (LabelTrack) ((ObjectLine) (labelTrackReadComboBox
				.getSelectedItem())).getTrack();
		selectionTrack = (LabelTrack) ((ObjectLine) (labelTrackSelectionComboBox
				.getSelectedItem())).getTrack();
		dataTrack = (ObjectLine) ((ObjectLine) (featureTrackComboBox
				.getSelectedItem()));

		ArrayList<LabelObject> selectionLabels = selectionTrack.getLabels(0l,
				(long) Project.getInstance().getProjectLength());
		ArrayList<LabelObject> learnLabels = new ArrayList<LabelObject>();
		Iterator<LabelObject> iL = selectionLabels.iterator();
		while (iL.hasNext()) {
			LabelObject tmp = iL.next();
			learnLabels.addAll(labelTrack.getLabels(tmp.getStart(),
					tmp.getEnd()));
		}
		// remove dublicate entrys by hashSet trick
		HashSet<LabelObject> hashSet = new HashSet<LabelObject>(learnLabels);
		learnLabels = new ArrayList<LabelObject>(hashSet);

		iL = selectionLabels.iterator();
		ArrayList<double[]> dataList = new ArrayList<double[]>();
		ArrayList<Double> dataTimePointsList = new ArrayList<Double>();

		while (iL.hasNext()) {
			LabelObject tmp = iL.next();
			double[][] tmpData = dataTrack
			.getData(tmp.getStart(), tmp.getEnd());
			double[] tmpDataTimePoints = dataTrack.getDataTimePoints(
					tmp.getStart(), tmp.getEnd());
			for (int i = 0; tmpData != null && i < tmpData.length; i++) {
				dataList.add(tmpData[i]);
				dataTimePointsList.add(tmpDataTimePoints[i]);
			}
		}

		double[][] data = new double[dataList.size()][dataTrack
		                                              .getDataDimesion()];
		double[] dataTimePoints = new double[dataList.size()];
		double[] classes = new double[dataList.size()];

		for (int i = 0; i < classes.length; i++) {
			dataTimePoints[i] = dataTimePointsList.get(i);
			data[i] = dataList.get(i);

			long time = (long) dataTimePoints[i];
			iL = learnLabels.iterator();
			boolean gotIt = false;
			classes[i] = -1.0;
			while (iL.hasNext() && !gotIt) {
				LabelObject label = iL.next();
				if ((label.getLabelType() == LabelType.MANUAL || label
						.getLabelType() == LabelType.AUTO_ACCEPTED)
						&& label.hasTimePoint(time)) {
					classes[i] = (double) label.getLabelClassEntity().getId();
					gotIt = true;
				}
			}
		}
		clf.writeSVMProblem(data, classes, file);
	}

	// if changed, look at writeProblem if same change is necessary
	private void learn() {

		ObjectLine dataTrack = null;
		LabelTrack labelTrack = null;
		LabelTrack selectionTrack = null;

		labelTrack = (LabelTrack) ((ObjectLine) (labelTrackReadComboBox
				.getSelectedItem())).getTrack();
		selectionTrack = (LabelTrack) ((ObjectLine) (labelTrackSelectionComboBox
				.getSelectedItem())).getTrack();
		dataTrack = (ObjectLine) ((ObjectLine) (featureTrackComboBox
				.getSelectedItem()));

		ArrayList<LabelObject> selectionLabels = selectionTrack.getLabels(0l,
				(long) Project.getInstance().getProjectLength());
		ArrayList<LabelObject> learnLabels = new ArrayList<LabelObject>();
		Iterator<LabelObject> iL = selectionLabels.iterator();
		while (iL.hasNext()) {
			LabelObject tmp = iL.next();
			learnLabels.addAll(labelTrack.getLabels(tmp.getStart(),
					tmp.getEnd()));
		}
		// remove dublicate entrys by hashSet trick
		HashSet<LabelObject> hashSet = new HashSet<LabelObject>(learnLabels);
		learnLabels = new ArrayList<LabelObject>(hashSet);

		iL = selectionLabels.iterator();
		ArrayList<double[]> dataList = new ArrayList<double[]>();
		ArrayList<Double> dataTimePointsList = new ArrayList<Double>();

		while (iL.hasNext()) {
			LabelObject tmp = iL.next();
			double[][] tmpData = dataTrack
			.getData(tmp.getStart(), tmp.getEnd());
			double[] tmpDataTimePoints = dataTrack.getDataTimePoints(
					tmp.getStart(), tmp.getEnd());
			for (int i = 0; tmpData != null && i < tmpData.length; i++) {
				dataList.add(tmpData[i]);
				dataTimePointsList.add(tmpDataTimePoints[i]);
			}
		}

		double[][] data = new double[dataList.size()][dataTrack
		                                              .getDataDimesion()];
		double[] dataTimePoints = new double[dataList.size()];
		double[] classes = new double[dataList.size()];

		for (int i = 0; i < classes.length; i++) {
			dataTimePoints[i] = dataTimePointsList.get(i);
			data[i] = dataList.get(i);

			long time = (long) dataTimePoints[i];
			iL = learnLabels.iterator();
			boolean gotIt = false;
			classes[i] = -1.0;
			while (iL.hasNext() && !gotIt) {
				LabelObject label = iL.next();
				if ((label.getLabelType() == LabelType.MANUAL || label
						.getLabelType() == LabelType.AUTO_ACCEPTED)
						&& label.hasTimePoint(time)) {
					classes[i] = (double) label.getLabelClassEntity().getId();
					gotIt = true;
				}
			}
		}

		clf.resetParams();

		clf.setSVMParams("-g " + (1.0 / data[0].length));

		clf.setSVMParams(svmCommandlineTextField.getText());

		clf.trainSVM(data, classes);

	}

	private double[] classify(double[][] data, double[] dataTimePoints,
			LabelTrack labelTrack) {
		if (data != null) {
			double[] maxProbs = new double[data.length];
			ClassifikationResult prevResult = clf.classify(data[0]);
			double prevTimePoint = dataTimePoints[0];
			double sum = prevResult.getMaxProbability();
			double count = 1.0;
			for (int i = 0; i < data.length; i++) {
				ClassifikationResult clfResult = clf.classify(data[i]);

				if (clfResult.getResultClassID() != prevResult
						.getResultClassID() || i == data.length - 1) {
					if (prevResult.getResultClassID() != -1) {
						labelTrack.addLabel(new LabelObject(labelTrack
								.getLabelClass()
								.getEntityByID(prevResult.getResultClassID())
								.getName(), "", (long) prevTimePoint,
								(long) dataTimePoints[i], sum / count,
								LabelType.AUTOMATIC,
								labelTrack.getLabelClass(), labelTrack
								.getLabelClass().getEntityByID(
										prevResult.getResultClassID()),
										System.currentTimeMillis()));
					}
					sum = clfResult.getMaxProbability();
					maxProbs[i] = clfResult.getMaxProbability();
					count = 1.0;
					prevResult = clfResult;
					prevTimePoint = dataTimePoints[i];
				} else {
					count++;
					sum += clfResult.getMaxProbability();
					maxProbs[i] = clfResult.getMaxProbability();
				}
				// System.out.println(clfResult.getResultClassID()+" : "+clfResult.getMaxProbability());
			}
			return maxProbs;
		}
		return null;
	}
	private double[][] classify(double[][] data, double[] dataTimePoints, int numClasses) {
		if (data != null) {
			double[][] probs = new double[data.length][numClasses];
			for (int i = 0; i < data.length; i++) {
				ClassifikationResult clfResult = clf.classify(data[i]);
					probs[i] = clfResult.getProbilityVector();
				}
			return probs;
		}
		return null;
	}

	private void classify() {
		LabelTrack selectionTrack = (LabelTrack) ((ObjectLine) (labelTrackSelectionComboBox
				.getSelectedItem())).getTrack();
		ObjectLine dataTrack = (ObjectLine) ((ObjectLine) (featureTrackComboBox
				.getSelectedItem()));
		LabelTrack labelTrack = (LabelTrack) ((ObjectLine) (labelTrackWriteComboBox
				.getSelectedItem())).getTrack();

		if (this.chckbxCreateProbabilityScalartrack.isSelected()) {
			File file;
			File[] files = new File[labelTrack.getLabelClass().getSize()]; 
			for(int i=0;i<labelTrack.getLabelClass().getSize();i++){
				if ((file = HelperFunctions.testAndGenerateFile(Project
						.getInstance().getProjectPath()
						+ "datatracks/"
						+ labelTrack.getName() + "_probs_" + labelTrack.getLabelClass().getEntityAt(i).getName() + ".raw")) == null) {
					return;
				}
				files[i] = file;
			}
			if ((file = HelperFunctions.testAndGenerateFile(Project
					.getInstance().getProjectPath()
					+ "datatracks/"
					+ labelTrack.getName() + "_probs.raw")) == null) {
				return;
			}
			
			ArrayList<Double> timePointsList = new ArrayList<Double>();
			ArrayList<Double> maxProbsList = new ArrayList<Double>();
			ArrayList<Double>[] probsList = (ArrayList<Double>[])new ArrayList[labelTrack.getLabelClass().getSize()];   //(ArrayList<String>[])new ArrayList[10]; 
			for(int i=0;i<labelTrack.getLabelClass().getSize();i++){
				probsList[i] = new ArrayList<Double>();
			}
			Iterator<LabelObject> iL = selectionTrack.getLabels().iterator();
			while (iL.hasNext()) {
				LabelObject tmp = iL.next();
				double[] timePoints = dataTrack.getDataTimePoints(
						tmp.getStart(), tmp.getEnd());
				double[] maxProbs = classify(
						dataTrack.getData(tmp.getStart(), tmp.getEnd()),
						timePoints, labelTrack);
				double[][] probs = classify(dataTrack.getData(tmp.getStart(), tmp.getEnd()),timePoints, labelTrack.getLabelClass().getSize());
				for (int i = 0; i < timePoints.length; i++) {
					timePointsList.add(timePoints[i]);
					maxProbsList.add(maxProbs[i]);
					for(int lc=0;lc<labelTrack.getLabelClass().getSize();lc++){
						probsList[lc].add(probs[i][lc]);
					}
				}
				
			}
			RandomAccessFile raf;
			RandomAccessFile[] rafs = new RandomAccessFile[labelTrack.getLabelClass().getSize()];
			try {
				raf = new RandomAccessFile(file, "rw");
				FileChannel fChannel = raf.getChannel();
				DoubleBuffer dB = fChannel.map(FileChannel.MapMode.READ_WRITE, 0, timePointsList.size() * (Double.SIZE / 8) * 2).asDoubleBuffer();
				for (int i = 0; i < timePointsList.size(); i++) {
					dB.put(timePointsList.get(i));
					dB.put(maxProbsList.get(i));
				}
				Project.getInstance().getLcoll().addScalarTrack(labelTrack.getName() + "_probs",Project.getInstance().getProjectPath() + "datatracks/" + labelTrack.getName() + "_probs.raw",true,true,true,0,1,Project.getInstance().getLcoll().getOlinesSize(), false,ObjectLine.MEDIUM, false);
				
				for(int lc=0;lc<labelTrack.getLabelClass().getSize();lc++){
					raf = new RandomAccessFile(files[lc], "rw");
					fChannel = raf.getChannel();
					dB = fChannel.map(FileChannel.MapMode.READ_WRITE, 0, timePointsList.size() * (Double.SIZE / 8) * 2).asDoubleBuffer();
					for (int i = 0; i < timePointsList.size(); i++) {
						dB.put(timePointsList.get(i));
						dB.put(probsList[lc].get(i));
					}
					Project.getInstance().getLcoll().addScalarTrack(labelTrack.getName() + "_probs_" + labelTrack.getLabelClass().getEntityAt(lc).getName(),Project.getInstance().getProjectPath() + "datatracks/" + labelTrack.getName() + "_probs_" + labelTrack.getLabelClass().getEntityAt(lc).getName() + ".raw",true,true,true,0,1,Project.getInstance().getLcoll().getOlinesSize(), false,ObjectLine.MEDIUM, false);
					
				}
				
				
				MessageManager.getInstance().requestTrackUpdate(new UpdateTracksEvent(this));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Iterator<LabelObject> iL = selectionTrack.getLabels().iterator();
			while (iL.hasNext()) {
				LabelObject tmp = iL.next();
				classify(
						dataTrack.getData(tmp.getStart(), tmp.getEnd()),
						dataTrack.getDataTimePoints(tmp.getStart(),
								tmp.getEnd()), labelTrack);
			}
		}
		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
		// MessageManager.getInstance().requestTrackUpdate(new
		// UpdateTracksEvent(this));

	}

	@Override
	public void run() {
		this.btnClassify.setEnabled(false);
		this.btnLearn.setEnabled(false);
		this.btnLoad.setEnabled(false);
		this.btnSave.setEnabled(false);
		learn();
		this.btnClassify.setEnabled(true);
		this.btnLearn.setEnabled(true);
		this.btnLoad.setEnabled(true);
		this.btnSave.setEnabled(true);

	}
}

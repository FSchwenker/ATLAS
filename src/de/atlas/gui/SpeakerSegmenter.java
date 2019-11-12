package de.atlas.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Dialog.ModalExclusionType;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JButton;

import de.atlas.collections.AudioTrack;
import de.atlas.collections.LabelTrack;
import de.atlas.collections.ObjectLine;
import de.atlas.exceptions.FilterSizeException;
import de.atlas.exceptions.WavFileException;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.messagesystem.UpdateTracksListener;
import de.atlas.misc.AtlasProperties;
import de.atlas.misc.Classifier;
import de.atlas.misc.HelperFunctions;
import de.atlas.data.ClassifikationResult;
import de.atlas.data.LabelObject;
import de.atlas.data.LabelType;
import de.atlas.data.Project;
import de.atlas.data.WavFile;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

import comirva.audio.util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import javax.swing.JCheckBox;

public class SpeakerSegmenter extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JComboBox cmbLabelTrack;
	private JComboBox cmbAudioTrack;
	private JTextField txtSVMCommand;
	private JCheckBox chckbxCreateConfidenceTrack;

	private Classifier clf = new Classifier();
	private LabelTrack tmpLabelTrack;
	private JComboBox cmbMask;
	private AudioTrack mfccComputed = null;
	private double[] monoWavData;
	private int sampleRate;
	private WavFile wavFile;
	private int numPointsPerWindow = 512;
	private double energy[];
	private double energyDist[];
	private double energy_max = 0;
	private double energy_min = Double.MAX_VALUE;
	private double mfcc[][];
	private double time[];
	
	/**
	 * Create the frame.
	 */
	public SpeakerSegmenter() {
		setTitle("Speaker Segmentation");
		setResizable(false);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setBounds(100, 100, 325, 340);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblAudiotrack = new JLabel("AudioTrack");
		lblAudiotrack.setBounds(22, 141, 193, 15);
		contentPane.add(lblAudiotrack);

		cmbAudioTrack = new JComboBox();
		cmbAudioTrack.setBounds(12, 157, 296, 37);
		contentPane.add(cmbAudioTrack);

		JLabel lblLabeltrack = new JLabel("LabelTrack");
		lblLabeltrack.setBounds(22, 206, 193, 15);
		contentPane.add(lblLabeltrack);

		cmbLabelTrack = new JComboBox();
		cmbLabelTrack.setBounds(12, 221, 296, 37);
		contentPane.add(cmbLabelTrack);

		txtSVMCommand = new JTextField();
		txtSVMCommand.setText("-b 1");
		txtSVMCommand.setColumns(30);
		txtSVMCommand.setBounds(128, 10, 180, 19);
		contentPane.add(txtSVMCommand);

		JLabel label_2 = new JLabel("SVM Command:");
		label_2.setBounds(12, 12, 134, 15);
		contentPane.add(label_2);

		JButton btnSuggest = new JButton("suggest");
		btnSuggest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				suggest();
			}
		});
		btnSuggest.setBounds(17, 270, 134, 25);
		contentPane.add(btnSuggest);

		JButton btnCancel = new JButton("cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		btnCancel.setBounds(177, 270, 134, 25);
		contentPane.add(btnCancel);

		JLabel lblMask = new JLabel("Mask");
		lblMask.setBounds(22, 76, 296, 15);
		contentPane.add(lblMask);

		cmbMask = new JComboBox();
		cmbMask.setBounds(12, 91, 296, 37);
		contentPane.add(cmbMask);

		chckbxCreateConfidenceTrack = new JCheckBox("create confidence Track");
		chckbxCreateConfidenceTrack.setBounds(12, 37, 296, 23);
		contentPane.add(chckbxCreateConfidenceTrack);
		MessageManager.getInstance().addUpdateTracksListener(new UpdateTracksListener(){
			@Override
			public void updateTracks(UpdateTracksEvent e) {
				if(!(e.getSource().getClass().toString().equals(LearningWindow.class.toString()))){
					buildTrackList();
				}

			}			 
		});
		AtlasProperties.getInstance().addJFrameBoundsWatcher("speakerSegementer", this, true, false);
	}
	private void computeFeatures(){
		File file = ((AudioTrack) (cmbAudioTrack.getSelectedItem())).getFile();
		// read wav file
		try {
			wavFile = WavFile.openWavFile(file);
			sampleRate = (int) wavFile.getSampleRate();
			int numChannels = wavFile.getNumChannels();
			int numFrames = (int)wavFile.getNumFrames();
			int bufLen = 20000;
			double[] buffer = new double[bufLen*numChannels];
			monoWavData = new double[numFrames];

			int framesRead = 0;
			int index=0;
			do{
				framesRead = wavFile.readFrames(buffer, bufLen);
				for (int s=0 ; s<framesRead * numChannels ; s+=numChannels)
				{
					monoWavData[index++] = buffer[s];
				}
			}while(framesRead!=0);
			wavFile.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (WavFileException e) {
			e.printStackTrace();
		}

		// compute energy and mfcc
		if(sampleRate>20000)numPointsPerWindow = 1024;
		int numWindows = (monoWavData.length/numPointsPerWindow)*2-1;
		energy = new double[numWindows];
		energyDist = new double[numWindows];
		mfcc = new double[numWindows][20];
		time = new double[numWindows];
		MFCC comirvaMFCC = new MFCC(wavFile.getSampleRate(),numPointsPerWindow,21,false);
		for (int i=0;i<numWindows;i++){
			double sum = 0;
			double win[] = new double[numPointsPerWindow];
			for(int j=0;j<numPointsPerWindow;j++){
				sum += Math.pow(monoWavData[(int)(((double)i*numPointsPerWindow/2.0)+j)],2);
				win[j] = monoWavData[(int)(((double)i*numPointsPerWindow/2.0)+j)];
			}
			energy[i] = Math.sqrt(sum)/(double)numPointsPerWindow;
			energyDist[i] = energy[i];
			if(energy_max<energy[i])energy_max=energy[i];
			if(energy_min>energy[i])energy_min=energy[i];
			mfcc[i]=comirvaMFCC.processWindow(win, 0);
			time[i]=(long) (i*(numPointsPerWindow/2.0)*(1000.0/sampleRate)+(numPointsPerWindow/2.0)*(1000.0/sampleRate));
		}
		java.util.Arrays.sort(energyDist);


	}
	private ArrayList<LabelObject> collectTrainLabels(){
		LabelTrack labelTrack = (LabelTrack) ((ObjectLine) (cmbLabelTrack.getSelectedItem())).getTrack();
		ArrayList<LabelObject> labels = new ArrayList<LabelObject>();
		if(this.cmbMask.getSelectedItem() instanceof ObjectLine){
			//			LabelTrack selectionTrack = (LabelTrack) ((ObjectLine) (cmbMask.getSelectedItem())).getTrack();
			Iterator<LabelObject> iM = ((LabelTrack) ((ObjectLine) (cmbMask.getSelectedItem())).getTrack()).getLabels().iterator();
			while(iM.hasNext()){
				LabelObject maskTMP = iM.next();
				Iterator<LabelObject> iL = labelTrack.getLabels(maskTMP.getStart(), maskTMP.getEnd()).iterator();
				while(iL.hasNext())	labels.add(iL.next());
			}
		}else{
			labels = labelTrack.getLabels();
		}
		ArrayList<LabelObject> labelsToUse = new ArrayList<LabelObject>();
		ArrayList<LabelObject> labelsToRemove = new ArrayList<LabelObject>();
		Iterator<LabelObject> iL = labels.iterator();
		while(iL.hasNext()){
			LabelObject tmp = iL.next();
			if(tmp.getLabelType()== LabelType.MANUAL||tmp.getLabelType()==LabelType.AUTO_ACCEPTED){
				labelsToUse.add(tmp);
			}else{
				labelsToRemove.add(tmp);  //do not remove labels while loop to prevent java.util.ConcurrentModificationException
			}
		}
		iL = labelsToRemove.iterator();
		while(iL.hasNext())labelTrack.removeLabel(iL.next());
		return labelsToUse;
	}

	private double computeEnergyThresh(ArrayList<LabelObject> labels){
		//compute average class amount
		long labeld_amount_ms = 0;
		Iterator<LabelObject> iL = labels.iterator(); 
		while(iL.hasNext()){
			LabelObject tmp = iL.next();
			labeld_amount_ms+=(tmp.getEnd()-tmp.getStart());
		}
		int min_amount_windows = (int) (labeld_amount_ms/((numPointsPerWindow/2.0)*(1000.0/sampleRate)))/((LabelTrack) ((ObjectLine) (cmbLabelTrack.getSelectedItem())).getTrack()).getLabelClass().getEntityCount();

		//compute hist of energy and use max (hopefully silence) as threshold
		int hist[] = new int[1001];
		for(int i=0;i<energy.length;i++){
			//System.out.println(energy[i] + " " + energy_min + " " + energy_max);
			hist[(int)(((energy[i]-energy_min)/energy_max)*1000)]++;
		}
		int maxIdx = -1;
		int maxHist = 0;
		for(int i=0;i<hist.length;i++){
			if(maxHist<hist[i]){
				maxHist = hist[i];
				maxIdx = i;
			}
		}			
		double hist_thresh = (((double)(maxIdx))/hist.length*energy_max)+energy_min;
		int hist_ammount_windows = 0;
		for(int i=0;i<energyDist.length;i++){
			if(energyDist[i]>=hist_thresh){
				hist_ammount_windows=i;
				break;
			}
		}

		//compute energy distribution of used labels and use 5% percentile of energy as threshold
		int perc_ammount_windows = 0;
		double perc_thresh = energyDist[(int) (energyDist.length*0.05)];
		for(int i=0;i<energyDist.length;i++){
			if(energyDist[i]>=perc_thresh){
				perc_ammount_windows=i;
				break;
			}
		}
		if(min_amount_windows>hist_ammount_windows)min_amount_windows=hist_ammount_windows;
		if(min_amount_windows>perc_ammount_windows)min_amount_windows=perc_ammount_windows;
		return energyDist[min_amount_windows];
	}
	private double[][] classify(double[][] data, double[] dataTimePoints, LabelTrack labelTrack){
		if(data!=null){
			int[] classIDs = clf.getLabels();
			for(int i=0;i<classIDs.length;i++)System.out.println(classIDs[i]);
			int silenceIDidx=-1;
			for(int i=0;i<classIDs.length;i++)if(classIDs[i]==-43)silenceIDidx=i;
			double[][] resultProbs = new double[data.length][clf.getClassCount()];
			for(int i=0;i<data.length;i++){
				ClassifikationResult clfResult = clf.classify(data[i]);
				resultProbs[i]=clfResult.getProbilityVector();
			}
			// filter the results
			double[][] result_T = HelperFunctions.transpose(resultProbs);
			double[][] result_Median_T = HelperFunctions.transpose(resultProbs);
			try {
				for(int i=0;i<clf.getClassCount();i++){
					result_Median_T[i] = HelperFunctions.signalFilter(result_T[i], 7, HelperFunctions.filterType.MEDIAN);
				}
			} catch (FilterSizeException e) {
				e.printStackTrace();
			}
			resultProbs = HelperFunctions.transpose(result_Median_T);
			//store maxProb
			int prevResult = HelperFunctions.argmax(resultProbs[0]);
			double prevTimePoint = dataTimePoints[0];
			double sum = HelperFunctions.max(resultProbs[0]);
			double count = 1.0;
			for(int i=0;i<data.length;i++){
				if(HelperFunctions.argmax(resultProbs[i])!=prevResult || i==data.length-1){
					if(prevResult!=silenceIDidx){
						labelTrack.addLabel(new LabelObject(
								labelTrack.getLabelClass().getEntityByID(classIDs[prevResult]).getName(),
								"",
								(long)prevTimePoint,
								(long)dataTimePoints[i],
								sum/count ,
								LabelType.AUTOMATIC,
								labelTrack.getLabelClass(),
								labelTrack.getLabelClass().getEntityByID(classIDs[prevResult]),
								System.currentTimeMillis()));
					}
					sum = HelperFunctions.max(resultProbs[i]);
					count = 1.0;
					prevResult = HelperFunctions.argmax(resultProbs[i]);
					prevTimePoint = dataTimePoints[i];
				}else{
					count++;
					sum+=HelperFunctions.max(resultProbs[i]);
				}
			}
			return resultProbs;
		}
		return null;
	}
	private void classify(){
		double[][] resultProbs;
		double[] resultTimes;
		ArrayList<double[]> resultProbsList = new ArrayList<double[]>();
		ArrayList<Double> resultProbsTimeList = new ArrayList<Double>();

		LabelTrack labelTrack = (LabelTrack) ((ObjectLine)(this.cmbLabelTrack.getSelectedItem())).getTrack();

		tmpLabelTrack = new LabelTrack((int) Project.getInstance().getProjectLength(),labelTrack.getLabelClass(),"noname");

		LabelTrack maskTrack = null;
		Iterator<LabelObject> iL = null;
		if(this.cmbMask.getSelectedItem() instanceof ObjectLine){
			maskTrack = ((LabelTrack)((ObjectLine)(this.cmbMask.getSelectedItem())).getTrack());
			iL = maskTrack.getLabels().iterator();
			while(iL.hasNext()){
				LabelObject tmp = iL.next();
				int start = (int) (tmp.getStart()/((numPointsPerWindow/2.0)*(1000.0/sampleRate)));
				int end = (int) (tmp.getEnd()/((numPointsPerWindow/2.0)*(1000.0/sampleRate)));
				double[][] mfccSnipplet = new double[end-start][20]; 
				double[]   timeSnipplet = new double[end-start]; 
				for(int i=start;i<end;i++){
					mfccSnipplet[i-start] = mfcc[i];
					timeSnipplet[i-start] = time[i];
				}
				resultProbs = classify(mfccSnipplet,timeSnipplet,tmpLabelTrack);
				for(int i=0;i<resultProbs.length;i++){
					resultProbsList.add(resultProbs[i]);
					resultProbsTimeList.add(timeSnipplet[i]);
				}
			}
			resultProbs = new double[resultProbsList.size()][clf.getClassCount()];
			resultTimes = new double[resultProbsList.size()];
			for(int i=0;i<resultTimes.length;i++){
				resultProbs[i]=resultProbsList.get(i);
				resultTimes[i]=resultProbsTimeList.get(i);
			}
		}else{
			resultProbs = classify(mfcc,time,tmpLabelTrack);
			resultTimes = time;
		}
		if(this.chckbxCreateConfidenceTrack.isSelected()){
			try {
				//delete possible old files
				Project.getInstance().getLcoll().removeTrack("segmenter_result");
				File file = new File(Project.getInstance().getProjectPath() + "datatracks/segmenter_res.raw");
				if(file.exists())file.delete();
				
				RandomAccessFile raf_res;
				raf_res = new RandomAccessFile(new File(Project.getInstance().getProjectPath() + "datatracks/segmenter_res.raw"), "rw");
				FileChannel fChannel_res = raf_res.getChannel();
				DoubleBuffer dB_res = fChannel_res.map(FileChannel.MapMode.READ_WRITE, 0,resultTimes.length * (Double.SIZE / 8) * (labelTrack.getLabelClass().getEntityCount()+2)).asDoubleBuffer();
				for (int i = 0; i < resultTimes.length; i++) {
					dB_res.put(resultTimes[i]);
					for(int j=0;j<labelTrack.getLabelClass().getEntityCount()+1;j++){
						dB_res.put(resultProbs[i][j]);					
					}
				}
				raf_res.close();
				Project.getInstance().getLcoll().addVectorTrack("segmenter_result",Project.getInstance().getProjectPath()+ "datatracks/segmenter_res.raw", true, "Colorful", 0, 1, labelTrack.getLabelClass().getEntityCount()+1,
						Project.getInstance().getLcoll().getOlinesSize(),false, ObjectLine.MEDIUM, true, false, false);

				MessageManager.getInstance().requestTrackUpdate(new UpdateTracksEvent(this));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	private void suggest(){
		LabelTrack labelTrack = (LabelTrack) ((ObjectLine)(this.cmbLabelTrack.getSelectedItem())).getTrack();
		
		if(mfccComputed != (AudioTrack) (cmbAudioTrack.getSelectedItem())){
			computeFeatures();
			mfccComputed = (AudioTrack) (cmbAudioTrack.getSelectedItem());
		}

		ArrayList<LabelObject> labelsToUse = collectTrainLabels();
		double energyThresh = computeEnergyThresh(labelsToUse);

		//collect label data
		ArrayList<Double> speakerLabel = new ArrayList<Double>();
		ArrayList<double[]> speakerFeatures = new ArrayList<double[]>();
		Iterator<LabelObject> iL = labelsToUse.iterator();
		int idx=0;
		while(iL.hasNext()){
			LabelObject tmp = iL.next();
			int start = (int) (tmp.getStart()/((numPointsPerWindow/2.0)*(1000.0/sampleRate)));
			int end = (int) (tmp.getEnd()/((numPointsPerWindow/2.0)*(1000.0/sampleRate)));
			double label = tmp.getLabelClassEntity().getId();
			for(int i=start;i<=end;i++){
				speakerLabel.add(label);
				speakerFeatures.add(mfcc[i]);
				idx++;
			}
		}
		//add silence data
		for(int i=0;i<energy.length;i++){
			if(energy[i]<=energyThresh){
				speakerLabel.add(-43.0);
				speakerFeatures.add(mfcc[i]);
			}
		}

		double[][] data = new double[speakerLabel.size()][20];
		double[] classes = new double[speakerLabel.size()];
		Iterator<Double> iC = speakerLabel.iterator();
		Iterator<double[]> iD = speakerFeatures.iterator();
		idx = 0;
		while(iC.hasNext()){
			classes[idx]=iC.next().doubleValue();
			data[idx]=iD.next();
			idx++;
		}

		clf.resetParams();

		clf.setSVMParams("-b 1");
		clf.setSVMParams("-g " + (1.0 / 20.0));
		clf.setSVMParams(txtSVMCommand.getText());

		clf.trainSVM(data, classes);

		classify();

		removeOverlappingLabels(labelTrack);
		tmpLabelTrack.removeShortLabels(150);
		closeLabelGaps(tmpLabelTrack,350);
		HelperFunctions.copyLabels(tmpLabelTrack, labelTrack);

		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));

	}

	private void removeOverlappingLabels(LabelTrack labelTrack) {
		ArrayList<LabelObject> labels = labelTrack.getLabels(0l,(long)Project.getInstance().getProjectLength());

		Iterator<LabelObject> iL = labels.iterator();
		while(iL.hasNext()){
			LabelObject tmp = iL.next();
			tmpLabelTrack.removeLabels(tmp.getStart(),tmp.getEnd());
		}

	}
	public void closeLabelGaps(LabelTrack labelTrack, long maxGap){
		Iterator<LabelObject> iO = labelTrack.getLabels().iterator();
		ArrayList<LabelObject> toRemove = new ArrayList<LabelObject>();
		LabelObject prevObj = null;
		if(iO.hasNext())prevObj = iO.next();
		while (iO.hasNext()) {
			LabelObject obj = iO.next();
			if(prevObj.getLabelClassEntity().getName().equalsIgnoreCase(obj.getLabelClassEntity().getName())&&
					prevObj.getLabelType()==obj.getLabelType()&&
					obj.getStart()-prevObj.getEnd()<maxGap){
				obj.setStart(prevObj.getStart());
				obj.setValue(((prevObj.getEnd()-prevObj.getStart())*prevObj.getValue()+(obj.getEnd()-obj.getStart())*obj.getValue())/(prevObj.getEnd()-prevObj.getStart()+obj.getEnd()-obj.getStart()));
				toRemove.add(prevObj);
			}
			prevObj = obj;
		}

		Iterator<LabelObject> iL = toRemove.iterator();
		while(iL.hasNext())labelTrack.removeLabel(iL.next());
	}


	private void buildTrackList(){
		DefaultComboBoxModel labelListModel = new DefaultComboBoxModel();
		DefaultComboBoxModel audioListModel = new DefaultComboBoxModel();
		DefaultComboBoxModel maskListModel = new DefaultComboBoxModel();
		maskListModel.addElement("NONE (use all data)");
		Iterator<ObjectLine> iT = Project.getInstance().getLcoll().getList().iterator();
		while(iT.hasNext()){			
			ObjectLine ol = iT.next();
			if(ol.getTrack() instanceof LabelTrack){
				labelListModel.addElement(ol);
				maskListModel.addElement(ol);
			}
			if(ol.getTrack() instanceof AudioTrack){
				audioListModel.addElement(ol);
			}
		}
		Iterator<AudioTrack> aT = Project.getInstance().getMcoll().getAudioList().iterator();
		while(aT.hasNext()){			
			AudioTrack a = aT.next();
			audioListModel.addElement(a);
		}
		this.cmbLabelTrack.setModel(labelListModel);
		this.cmbAudioTrack.setModel(audioListModel);
		this.cmbMask.setModel(maskListModel);
	}
}

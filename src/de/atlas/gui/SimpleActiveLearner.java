package de.atlas.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Dialog.ModalExclusionType;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;

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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JSlider;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;

public class SimpleActiveLearner extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JComboBox cmbLabelTrack;
	private JComboBox cmbFeatureTrack;
	private JTextField txtSVMCommand;

	private Classifier clf = new Classifier();
	private LabelTrack tmpLabelTrack;
	private long blockLength = 10000;
	private double predictionPercentage = 0.2;
	private JSlider sliAmount;
	private JSlider sliLength;
	private JComboBox cmbMask;
	private JLabel lblAmount;
	private JLabel lblGranularity;


	/**
	 * Create the frame.
	 */
	public SimpleActiveLearner() {
		setTitle("Suggest Labels by Active Learning");
		setResizable(false);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 325, 375);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel label = new JLabel("FeatureTrack");
		label.setBounds(12, 165, 193, 15);
		contentPane.add(label);

		cmbFeatureTrack = new JComboBox();
		cmbFeatureTrack.setBounds(12, 180, 296, 37);
		contentPane.add(cmbFeatureTrack);

		JLabel lblLabeltrack = new JLabel("LabelTrack");
		lblLabeltrack.setBounds(12, 229, 193, 15);
		contentPane.add(lblLabeltrack);

		cmbLabelTrack = new JComboBox();
		cmbLabelTrack.setBounds(12, 244, 296, 37);
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
		btnSuggest.setBounds(12, 293, 134, 25);
		contentPane.add(btnSuggest);

		JButton btnCancel = new JButton("cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		btnCancel.setBounds(172, 293, 134, 25);
		contentPane.add(btnCancel);

		JLabel lblMask = new JLabel("Mask");
		lblMask.setBounds(12, 101, 193, 15);
		contentPane.add(lblMask);

		cmbMask = new JComboBox();
		cmbMask.setBounds(12, 116, 296, 37);
		contentPane.add(cmbMask);

		sliAmount = new JSlider();
		sliAmount.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				lblAmount.setText(String.valueOf(sliAmount.getValue()));
			}
		});
		sliAmount.setMinorTickSpacing(1);
		sliAmount.setValue(20);
		sliAmount.setMajorTickSpacing(10);
		sliAmount.setBounds(128, 41, 180, 19);
		contentPane.add(sliAmount);

		sliLength = new JSlider();
		sliLength.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				lblGranularity.setText(String.valueOf(sliLength.getValue()));
			}
		});
		sliLength.setValue(10);
		sliLength.setMajorTickSpacing(1);
		sliLength.setMaximum(60);
		sliLength.setMinimum(1);
		sliLength.setBounds(128, 72, 180, 16);
		contentPane.add(sliLength);

		JLabel lblSuggestionAmount = new JLabel("Amount:");
		lblSuggestionAmount.setBounds(12, 41, 89, 15);
		contentPane.add(lblSuggestionAmount);

		JLabel lblLength = new JLabel("Granularity:");
		lblLength.setBounds(12, 72, 98, 15);
		contentPane.add(lblLength);
		
		lblAmount = new JLabel("20");
		lblAmount.setBounds(107, 41, 53, 15);
		contentPane.add(lblAmount);
		
		lblGranularity = new JLabel("10");
		lblGranularity.setBounds(107, 72, 39, 15);
		contentPane.add(lblGranularity);
		MessageManager.getInstance().addUpdateTracksListener(new UpdateTracksListener(){
			@Override
			public void updateTracks(UpdateTracksEvent e) {
				if(!(e.getSource().getClass().toString().equals(LearningWindow.class.toString()))){
					buildTrackList();
				}

			}			 
		});
		AtlasProperties.getInstance().addJFrameBoundsWatcher("simActLearner",
				this, true, false);
	}
	private void suggest(){
		this.blockLength = this.sliLength.getValue()*1000;
		this.predictionPercentage = (double)this.sliAmount.getValue()/100.0;

		learn();
		classify();
		createBlocks();
		removeRejectedLabels();
		removeOverlappingLabels();
		removeLessConfLabels();
		copyToFinalTrack();
		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));

	}
	private void removeRejectedLabels() {
		LabelTrack labelTrack = (LabelTrack) ((ObjectLine)(this.cmbLabelTrack.getSelectedItem())).getTrack();
		ArrayList<LabelObject> labels = labelTrack.getLabels(0l,(long) Project.getInstance().getProjectLength());
		//ArrayList<LabelObject> tmpLabels = tmpLabelTrack.getLabels(0l,(long)Project.getInstance().getProjectLength());

		Iterator<LabelObject> iL = labels.iterator();
		while(iL.hasNext()){
			LabelObject tmp = iL.next();
			if(tmp.getLabelType()== LabelType.AUTO_REJECTED){
				labelTrack.removeLabel(tmp);
				ArrayList<LabelObject> tmpLabels = tmpLabelTrack.getLabels(tmp.getStart(),tmp.getEnd());
				Iterator<LabelObject> iTL = tmpLabels.iterator();
				while(iTL.hasNext()){
					LabelObject tmp2 = iTL.next();
					if(tmp2.getLabelClassEntity().getName().endsWith(tmp.getLabelClassEntity().getName())){
						tmpLabelTrack.removeLabel(tmp2);
					}
				}

			}
		}

	}
	private void removeOverlappingLabels() {
		LabelTrack labelTrack = (LabelTrack) ((ObjectLine)(this.cmbLabelTrack.getSelectedItem())).getTrack();
		ArrayList<LabelObject> labels = labelTrack.getLabels(0l,(long)Project.getInstance().getProjectLength());
		//ArrayList<LabelObject> tmpLabels = tmpLabelTrack.getLabels(0l,(long)Project.getInstance().getProjectLength());

		Iterator<LabelObject> iL = labels.iterator();
		while(iL.hasNext()){
			LabelObject tmp = iL.next();
			tmpLabelTrack.removeLabels(tmp.getStart(),tmp.getEnd());
		}

	}
	private void copyToFinalTrack() {
		LabelTrack labelTrack = (LabelTrack) ((ObjectLine)(this.cmbLabelTrack.getSelectedItem())).getTrack();

		ArrayList<LabelObject> labels = tmpLabelTrack.getLabels(0l,(long)Project.getInstance().getProjectLength());
		//ArrayList<LabelObject> tmpLabels = tmpLabelTrack.getLabels(0l,(long)Project.getInstance().getProjectLength());

		Iterator<LabelObject> iL = labels.iterator();
		while(iL.hasNext()){
			LabelObject tmp = iL.next();
			labelTrack.addLabel(new LabelObject(tmp.getText(), tmp.getComment(), tmp.getStart(), tmp.getEnd(),tmp.getValue(), tmp.getLabelType(), tmp.getLabelClass(), tmp.getLabelClassEntity(), tmp.getTimestamp()));
		}
		tmpLabelTrack = null;

	}
	private void removeLessConfLabels() {
		LabelTrack inTrack = tmpLabelTrack;
		LabelTrack outTrack = new LabelTrack((int) Project.getInstance().getProjectLength(),tmpLabelTrack.getLabelClass(),"noname");

		ArrayList<LabelObject> labels = inTrack.getLabels();
		double[] vals = new double[labels.size()];
		for(int i=0;i<labels.size();i++){
			vals[i] = labels.get(i).getValue();
		}
		java.util.Arrays.sort(vals);

		for(int i=vals.length-1;i>vals.length*(1-predictionPercentage);i--){
			for(int l=0;l<labels.size();l++){
				if(vals[i]==labels.get(l).getValue()){
					outTrack.addLabel(new LabelObject(labels.get(l).getText(),
							labels.get(l).getComment(),
							labels.get(l).getStart(),
							labels.get(l).getEnd(),
							labels.get(l).getValue(),
							labels.get(l).getLabelType(),
							labels.get(l).getLabelClass(),
							labels.get(l).getLabelClassEntity(),
							System.currentTimeMillis()));

				}
			}
		}
		tmpLabelTrack = outTrack;

	}
	private void createBlocks() {
		LabelTrack inTrack = tmpLabelTrack;
		LabelTrack outTrack = new LabelTrack((int) Project.getInstance().getProjectLength(),tmpLabelTrack.getLabelClass(),"noname");
		int classCount = inTrack.getLabelClass().getLabelClassEntities().size();
		HashMap<String,Integer> classesToInt = new HashMap<String,Integer>();
		HashMap<Integer,String> intToClasses = new HashMap<Integer,String>();
		for(int i=0;i<classCount;i++){
			classesToInt.put(inTrack.getLabelClass().getLabelClassEntities().get(i).getName(), i);
			intToClasses.put(i, inTrack.getLabelClass().getLabelClassEntities().get(i).getName());
		}
		for(long i=0;i<Project.getInstance().getProjectLength();i+=blockLength ){
			double[] confidences = new double[classCount];
			ArrayList<LabelObject> labels = inTrack.getLabels(i, i+blockLength);
			for(int l=0;l<labels.size();l++){
				confidences[classesToInt.get(labels.get(l).getLabelClassEntity().getName())]+=(labels.get(l).getValue()*(labels.get(l).getEnd()-labels.get(l).getStart()));					
			}
			double total =0;
			double max = 0;
			int maxI = -1;
			for(int l=0;l<classCount;l++){
				total+=confidences[l];
				if(max<confidences[l]){
					max=confidences[l];
					maxI=l;
				}
			}
			if(max>0){
				outTrack.addLabel(new LabelObject(intToClasses.get(maxI),
						"",
						i,
						i+blockLength,
						max/total ,
						LabelType.AUTOMATIC,
						outTrack.getLabelClass(),
						outTrack.getLabelClass().getEntityByName(intToClasses.get(maxI)),
						System.currentTimeMillis()));
			}
		}
		tmpLabelTrack = outTrack;
	}
	private void classify(){
		LabelTrack labelTrack = (LabelTrack) ((ObjectLine)(this.cmbLabelTrack.getSelectedItem())).getTrack();
		ObjectLine featureTrack = ((ObjectLine)(this.cmbFeatureTrack.getSelectedItem()));

		tmpLabelTrack = new LabelTrack((int)Project.getInstance().getProjectLength(),labelTrack.getLabelClass(),"noname");

		LabelTrack maskTrack = null;
		Iterator<LabelObject> iL = null;
		if(this.cmbMask.getSelectedItem() instanceof ObjectLine){
			maskTrack = ((LabelTrack)((ObjectLine)(this.cmbMask.getSelectedItem())).getTrack());
			iL = maskTrack.getLabels().iterator();
			while(iL.hasNext()){
				LabelObject tmp = iL.next();
				classify(featureTrack.getData(tmp.getStart(),tmp.getEnd()),featureTrack.getDataTimePoints(tmp.getStart(),tmp.getEnd()),tmpLabelTrack);
			}
		}else{
			classify(featureTrack.getData(0L,(long)Project.getInstance().getProjectLength()),featureTrack.getDataTimePoints(0L,(long)Project.getInstance().getProjectLength()),tmpLabelTrack);
		}


	}
	private double[] classify(double[][] data, double[] dataTimePoints, LabelTrack labelTrack){
		if(data!=null){
			double[] maxProbs = new double[data.length];
			ClassifikationResult prevResult = clf.classify(data[0]);
			double prevTimePoint = dataTimePoints[0];
			double sum = prevResult.getMaxProbability();
			double count = 1.0;
			for(int i=0;i<data.length;i++){
				ClassifikationResult clfResult = clf.classify(data[i]);

				if(clfResult.getResultClassID()!=prevResult.getResultClassID() || i==data.length-1){
					if(prevResult.getResultClassID()!=-1){
						labelTrack.addLabel(new LabelObject(
								labelTrack.getLabelClass().getEntityByID(prevResult.getResultClassID()).getName(),
								"",
								(long)prevTimePoint,
								(long)dataTimePoints[i],
								sum/count ,
								LabelType.AUTOMATIC,
								labelTrack.getLabelClass(),
								labelTrack.getLabelClass().getEntityByID(prevResult.getResultClassID()),
								System.currentTimeMillis()));
					}
					sum = clfResult.getMaxProbability();
					maxProbs[i]=clfResult.getMaxProbability();
					count = 1.0;
					prevResult = clfResult;
					prevTimePoint = dataTimePoints[i];
				}else{
					count++;
					sum+=clfResult.getMaxProbability();
					maxProbs[i]=clfResult.getMaxProbability();
				}
			}
			return maxProbs;
		}
		return null;
	}
	private void learn(){
		Object[] options = {"yes", "no"};
		int n = JOptionPane.showOptionDialog(null, "Do you want to use a precomputed SVM?", "Use precomputed SVM?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,options, options[0]);
		if(n==0){//yes
			clf.resetParams();
			clf.setSVMParams("-g "+(1.0/((ObjectLine)(this.cmbLabelTrack.getSelectedItem())).getDataDimesion()));
			clf.setSVMParams(this.txtSVMCommand.getText());
			JFileChooser fc = new JFileChooser(Project.getInstance().getProjectPath());
			fc.setFileFilter(new FileNameExtensionFilter("SVM", "svm"));

			int returnVal = fc.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION){
				clf.loadSVM(fc.getSelectedFile());
			}		
		}else{
			LabelTrack labelTrack = (LabelTrack) ((ObjectLine)(this.cmbLabelTrack.getSelectedItem())).getTrack();
			ObjectLine featureTrack = ((ObjectLine)(this.cmbFeatureTrack.getSelectedItem()));

			ArrayList<LabelObject> labels = null;
			ArrayList<double[]> dataList = new ArrayList<double[]>();
			ArrayList<Double> dataTimePointsList = new ArrayList<Double>();
			labels = labelTrack.getLabels(0l,(long)Project.getInstance().getProjectLength());		

			ArrayList<LabelObject> labelsToUse = new ArrayList<LabelObject>();

			Iterator<LabelObject> iL = labels.iterator();

			while(iL.hasNext()){
				LabelObject tmp = iL.next();
				if(tmp.getLabelType()==LabelType.MANUAL||tmp.getLabelType()==LabelType.AUTO_ACCEPTED){
					labelsToUse.add(tmp);
				}
			}
			labels=labelsToUse;


			if(this.cmbMask.getSelectedItem() instanceof ObjectLine){
				LabelTrack maskTrack = ((LabelTrack)((ObjectLine)(this.cmbMask.getSelectedItem())).getTrack());
				ArrayList<LabelObject> maskLabels = new ArrayList<LabelObject>();
				iL = labels.iterator();
				while(iL.hasNext()){
					LabelObject tmp = iL.next();
					maskLabels.addAll(maskTrack.getLabels(tmp.getStart(),tmp.getEnd()));
				}
				HashSet<LabelObject> hashSet = new HashSet<LabelObject>(maskLabels);
				maskLabels = new ArrayList<LabelObject>(hashSet);

				iL = maskLabels.iterator();
			}else{
				iL = labels.iterator();
			}

			while(iL.hasNext()){
				LabelObject tmp = iL.next();			
				double[][] tmpData = featureTrack.getData(tmp.getStart(),tmp.getEnd());
				double[] tmpDataTimePoints = featureTrack.getDataTimePoints(tmp.getStart(),tmp.getEnd());
				for(int i=0;tmpData!=null&&i<tmpData.length;i++){
					dataList.add(tmpData[i]);
					dataTimePointsList.add(tmpDataTimePoints[i]);
				}
			}

			double[][] data = new double[dataList.size()][featureTrack.getDataDimesion()];
			double[] dataTimePoints = new double[dataList.size()];
			double[] classes = new double[dataList.size()];
			for(int i=0;i<classes.length;i++){
				dataTimePoints[i]=dataTimePointsList.get(i);
				data[i]=dataList.get(i);

				long time=(long)dataTimePoints[i];
				iL = labels.iterator();
				boolean gotIt = false;
				classes[i]=-1.0;
				while(iL.hasNext()&&!gotIt){			
					LabelObject label = iL.next();
					if((label.getLabelType()==LabelType.MANUAL||label.getLabelType()==LabelType.AUTO_ACCEPTED)&&label.hasTimePoint(time)){
						classes[i]=(double)label.getLabelClassEntity().getId();
						gotIt=true;
					}
				}
			}
			System.out.println("Useing "+ classes.length + " datapoints with dimension of" + data[0].length + "to train");
			clf.resetParams();
			clf.setSVMParams("-g "+(1.0/data[0].length));
			clf.setSVMParams(this.txtSVMCommand.getText());
			clf.trainSVM(data,classes);				
		}
	}
	private void buildTrackList(){
		DefaultComboBoxModel labelListModel = new DefaultComboBoxModel();
		DefaultComboBoxModel featureListModel = new DefaultComboBoxModel();
		DefaultComboBoxModel maskListModel = new DefaultComboBoxModel();
		maskListModel.addElement("NONE (use all data)");
		Iterator<ObjectLine> iT = Project.getInstance().getLcoll().getList().iterator();
		while(iT.hasNext()){			
			ObjectLine ol = iT.next();
			if(ol.getTrack() instanceof LabelTrack){
				labelListModel.addElement(ol);
				maskListModel.addElement(ol);
			}
			if(ol.isLearnable())featureListModel.addElement(ol);
		}
		this.cmbLabelTrack.setModel(labelListModel);
		this.cmbFeatureTrack.setModel(featureListModel);
		this.cmbMask.setModel(maskListModel);
	}
}

package de.atlas.gui;

import de.atlas.collections.*;
import de.atlas.data.Project;
import de.atlas.exceptions.LabelClassNotFoundException;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.messagesystem.UpdateTracksListener;
import de.atlas.misc.AtlasProperties;
import de.atlas.misc.HelperFunctions;
import matlabcontrol.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.Dialog.ModalExclusionType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class MatlabIO extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtMatlabInit;
	private JTextField txtMatlabFunction;
	private JCheckBox chkOverwrite;
	private JCheckBox chkThread;
	private JList<Object> trackList;
	private JScrollPane scrollPane;
	private JLabel matlabLabel;

	private static final String STATUS_DISCONNECTED = "Connection Status: Disconnected",
			STATUS_CONNECTING = "Connection Status: Connecting",
			STATUS_CONNECTED_EXISTING = "Connection Status: Connected (Existing)",
			STATUS_CONNECTED_LAUNCHED = "Connection Status: Connected (Launched)";
	//Factory to create proxy
	private MatlabProxyFactory matlabFactory = null;

	//Proxy to communicate with MATLAB
	private final AtomicReference<MatlabProxy> proxyHolder = new AtomicReference<MatlabProxy>();


	/**
	 * Create the frame.
	 */
	public MatlabIO() {
		setTitle("Call Matlab Backend");
		setResizable(false);
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 325, 385);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel label_1 = new JLabel("Pre command:");
		label_1.setBounds(12, 12, 134, 15);
		contentPane.add(label_1);
		txtMatlabInit = new JTextField();
		txtMatlabInit.setText(AtlasProperties.getInstance().getMatlabCommand());
		txtMatlabInit.setColumns(30);
		txtMatlabInit.setBounds(128, 10, 180, 19);
		contentPane.add(txtMatlabInit);

		JLabel label_2 = new JLabel("Matlab function:");
		label_2.setBounds(12, 42, 134, 15);
		contentPane.add(label_2);
		txtMatlabFunction = new JTextField();
		txtMatlabFunction.setText(AtlasProperties.getInstance().getMatlabFunction());
		txtMatlabFunction.setColumns(30);
		txtMatlabFunction.setBounds(128, 40, 180, 19);
		contentPane.add(txtMatlabFunction);

		JLabel label_3 = new JLabel("Allow overwrite:");
		label_3.setBounds(12, 72, 134, 15);
		chkOverwrite = new JCheckBox();
		chkOverwrite.setSelected(AtlasProperties.getInstance().isAllowMatlabOverwrite());
		chkOverwrite.setBounds(128,70,30,19);
		contentPane.add(label_3);
		contentPane.add(chkOverwrite);

		JLabel label_4 = new JLabel("Run in background:");
		label_4.setBounds(12, 102, 134, 15);
		chkThread = new JCheckBox();
		chkThread.setSelected(AtlasProperties.getInstance().isRunMatlabInBackground());
		chkThread.setBounds(128,100,30,19);
		contentPane.add(label_4);
		contentPane.add(chkThread);

		JLabel label_5 = new JLabel("Tracks to send:");
		label_5.setBounds(12, 132, 134, 15);
		contentPane.add(label_5);
		trackList = new JList<Object>();//(createSendableList());
		trackList.setCellRenderer(new CheckListRenderer());
		trackList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		trackList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int index = trackList.locationToIndex(e.getPoint());
				Object track = trackList.getModel().getElementAt(index);
				if(track instanceof ObjectLine){
					((ObjectLine)track).setSendToMatlab(!((ObjectLine)track).isSendToMatlab());
				} else if (track instanceof AudioTrack) {
					((AudioTrack)track).setSendToMatlab(!((AudioTrack)track).isSendToMatlab());
				} else if (track instanceof VideoTrack) {
					((VideoTrack)track).setSendToMatlab(!((VideoTrack)track).isSendToMatlab());
				}
				Rectangle rect = trackList.getCellBounds(index, index);
				trackList.repaint(rect);
			}
		});
		scrollPane = new JScrollPane(trackList);
		scrollPane.setBounds(128,130,180,150);
		contentPane.add(scrollPane);

		JButton btnCall = new JButton("call");
		btnCall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(chkThread.isSelected()){
					new Thread(() -> callMatlab()).start();
				}else{
					callMatlab();
				}
			}
		});
		btnCall.setBounds(12, 293, 134, 25);
		contentPane.add(btnCall);


		JButton btnCancel = new JButton("cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		btnCancel.setBounds(172, 293, 134, 25);
		contentPane.add(btnCancel);

		matlabLabel = new JLabel("Matlab is doing some computations");
		matlabLabel.setForeground(Color.RED);
		matlabLabel.setVisible(false);
		matlabLabel.setBounds(12, 325, 300, 15);
		contentPane.add(matlabLabel);

		MessageManager.getInstance().addUpdateTracksListener(new UpdateTracksListener(){
			@Override
			public void updateTracks(UpdateTracksEvent e) {
				if(!(e.getSource().getClass().toString().equals(LearningWindow.class.toString()))){
					buildTrackList();
				}

			}			 
		});
		AtlasProperties.getInstance().addJFrameBoundsWatcher("matlabIO",
				this, true, false);
	}

	private void callMatlab(){
		matlabLabel.setVisible(true);
		AtlasProperties.getInstance().setMatlabCommand(txtMatlabInit.getText());
		AtlasProperties.getInstance().setMatlabFunction(txtMatlabFunction.getText());
		AtlasProperties.getInstance().setAllowMatlabOverwrite(chkOverwrite.isSelected());
		AtlasProperties.getInstance().setRunMatlabInBackground(chkThread.isSelected());
		try {
			if(txtMatlabInit.getText()!="")proxyHolder.get().eval(txtMatlabInit.getText());

			String projectXML = Project.getInstance().getAsXMLString();
			String[] tracks = new String[5];

			ArrayList<String> audioTracks = new ArrayList<>();
			tracks[0] = "AudioTracks";
			Iterator<AudioTrack> aIterator = Project.getInstance().getMcoll().getAudioList().iterator();
			while (aIterator.hasNext()){
				AudioTrack audioTrack = aIterator.next();
				if(audioTrack.isSendToMatlab()){
					audioTracks.add(audioTrack.getPath());
					tracks[0]=tracks[0]+";"+audioTrack.getName();
				}
			}

			ArrayList<String> videoTracks = new ArrayList<>();
			tracks[1] = "VideoTracks";
			Iterator<VideoTrack> vIterator = Project.getInstance().getMcoll().getVideoList().iterator();
			while (vIterator.hasNext()){
				VideoTrack videoTrack = vIterator.next();
				if(videoTrack.isSendToMatlab()){
					videoTracks.add(videoTrack.getPath());
					tracks[1]=tracks[1]+";"+videoTrack.getName();
				}
			}

			ArrayList<String> labelTracks = new ArrayList<>();
			ArrayList<double[][]> scalarTracks = new ArrayList<>();
			ArrayList<double[][]> vectorTracks = new ArrayList<>();
			tracks[2] = "LabelTracks";
			tracks[3] = "ScalarTracks";
			tracks[4] = "VectorTracks";
			Iterator<ObjectLine> oIterator = Project.getInstance().getLcoll().getList().iterator();
			while (oIterator.hasNext()){
				ObjectLine objectLine = oIterator.next();
				if(objectLine.isSendToMatlab()) {
					if (objectLine.getTrack() instanceof LabelTrack) {
						labelTracks.add(((LabelTrack) objectLine.getTrack()).getAsXML());
						tracks[2] = tracks[2] + ";" + objectLine.getName();
					} else if (objectLine.getTrack() instanceof ScalarTrack) {
						tracks[3] = tracks[3] + ";" + objectLine.getName();
						scalarTracks.add(((ScalarTrack) objectLine.getTrack()).getDataWithTime());
					} else if (objectLine.getTrack() instanceof VectorTrack) {
						tracks[4] = tracks[4] + ";" + objectLine.getName();
						vectorTracks.add(((VectorTrack) objectLine.getTrack()).getDataWithTime());
					}
				}
			}
			int nargout = 6;
			separateMatlabReturnValues(proxyHolder.get().returningFeval(txtMatlabFunction.getText(), nargout, projectXML, tracks, audioTracks.toArray(), videoTracks.toArray(), labelTracks.toArray(), scalarTracks.toArray(), vectorTracks.toArray()));

		} catch (MatlabInvocationException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage() + "\n\n" + e.getCause().getMessage() +"\n","Matlab IO Error", JOptionPane.ERROR_MESSAGE );
		}
		matlabLabel.setVisible(false);
	}

	private void separateMatlabReturnValues(Object[] result){
		Object res = result;
		String[] tmp;
		String[] tracks;
		Object[] dataLines;

		// get data from Matlab
		tracks = (String[])result[0];

		// get names of all tracks to receive
		ArrayList<String> audioTrackNames = new ArrayList<String>();
		tmp = tracks[0].split(";");
		for(int i=1;i<tmp.length;i++)audioTrackNames.add(tmp[i]);

		ArrayList<String> videoTrackNames = new ArrayList<String>();
		tmp = tracks[1].split(";");
		for(int i=1;i<tmp.length;i++)videoTrackNames.add(tmp[i]);

		ArrayList<String> labelTrackNames = new ArrayList<String>();
		tmp = tracks[2].split(";");
		for(int i=1;i<tmp.length;i++)labelTrackNames.add(tmp[i]);

		ArrayList<String> scalarTrackNames = new ArrayList<String>();
		tmp = tracks[3].split(";");
		for(int i=1;i<tmp.length;i++)scalarTrackNames.add(tmp[i]);

		ArrayList<String> vectorTrackNames = new ArrayList<String>();
		tmp = tracks[4].split(";");
		for(int i=1;i<tmp.length;i++)vectorTrackNames.add(tmp[i]);


		// get audio file paths
		tracks = (String[])result[1];
		ArrayList<String> audioFiles = new ArrayList<>();
		for(int i=0;i<tracks.length;i++)audioFiles.add(tracks[i]);

		//get video file paths
		tracks = (String[])result[2];
		ArrayList<String> videoFiles = new ArrayList<>();
		for(int i=0;i<tracks.length;i++)videoFiles.add(tracks[i]);

		// get labelTrack data
		tracks = (String[])result[3];
		ArrayList<String> labelXML = new ArrayList<>();
		for(int i=0;i<tracks.length;i++)labelXML.add(tracks[i]);

		//get scalarData
		ArrayList<double[][]> scalarData = new ArrayList<>();
		try {
			dataLines = (Object[]) result[4];
			for (int t = 0; t < dataLines.length; t++) {
				Object[] dataLine = (Object[]) dataLines[t];
				double[][] data = new double[dataLine.length][2];
				for (int s = 0; s < dataLine.length; s++) {
					data[s] = (double[]) dataLine[s];
				}
				scalarData.add(data);
			}
		}catch(ClassCastException e){

		}
		//get vectorData
		ArrayList<double[][]> vectorData = new ArrayList<>();
		try {
			dataLines = (Object[])result[5];
			for(int t=0;t<dataLines.length;t++){
				Object[] dataLine = (Object[])dataLines[t];
				double[][] data = new double[dataLine.length][2];
				for(int s=0; s<dataLine.length;s++) {
					data[s] = (double[]) dataLine[s];
				}
				vectorData.add(data);
			}
		}catch(ClassCastException e){

		}


		// create or update tracks from the data

		// audio
		for(int i=0; i<audioTrackNames.size();i++){
			if(Project.getInstance().getMcoll().hasAudioTrackWithName(audioTrackNames.get(i))&&chkOverwrite.isSelected()){
				Project.getInstance().getMcoll().getAudioTrackByName(audioTrackNames.get(i)).changeFile(audioFiles.get(i));
			}
			if(!Project.getInstance().getMcoll().hasAudioTrackWithName(audioTrackNames.get(i))){
				Project.getInstance().getMcoll().addAudioTrack(audioTrackNames.get(i),audioFiles.get(i), true, false);
				Project.getInstance().getMcoll().setTime(Project.getInstance().getTime());
			}
		}
		// video
		for(int i=0; i<videoTrackNames.size();i++){
			if(Project.getInstance().getMcoll().hasVideoTrackWithName(videoTrackNames.get(i))&&chkOverwrite.isSelected()){
				Project.getInstance().getMcoll().getVideoTrackByName(videoTrackNames.get(i)).changeFile(videoFiles.get(i));
			}
			if(!Project.getInstance().getMcoll().hasVideoTrackWithName(videoTrackNames.get(i))){
				Project.getInstance().getMcoll().addVideoTrack(videoTrackNames.get(i),videoFiles.get(i), true, false);
				Project.getInstance().getMcoll().setTime(Project.getInstance().getTime());
			}
		}
		// scalars
		for(int i=0; i<scalarTrackNames.size();i++){
			if(Project.getInstance().getLcoll().hasTrackWithName(scalarTrackNames.get(i))&&chkOverwrite.isSelected()){
				((ScalarTrack)Project.getInstance().getLcoll().getTrackByName(scalarTrackNames.get(i)).getTrack()).updateData(scalarData.get(i));
			}
			if(!Project.getInstance().getLcoll().hasTrackWithName(scalarTrackNames.get(i))){
				double[] mm = HelperFunctions.toRawFile(scalarData.get(i), true, Project.getInstance().getProjectPath()+ "datatracks/"+ scalarTrackNames.get(i) + ".raw");
				Project.getInstance().getLcoll().addScalarTrack(
						scalarTrackNames.get(i),
						Project.getInstance().getProjectPath()+ "datatracks/"+ scalarTrackNames.get(i) + ".raw",
						true, true,	true,
						mm[0], mm[1],
						Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);//videoTrackNames.get(i),videoFiles.get(i), true);
			}
		}
		// vectors
		for(int i=0; i<vectorTrackNames.size();i++){
			if(Project.getInstance().getLcoll().hasTrackWithName(vectorTrackNames.get(i))&&chkOverwrite.isSelected()){
				((VectorTrack)Project.getInstance().getLcoll().getTrackByName(vectorTrackNames.get(i)).getTrack()).updateData(vectorData.get(i));
			}
			if(!Project.getInstance().getLcoll().hasTrackWithName(vectorTrackNames.get(i))){
				double[] mm = HelperFunctions.toRawFile(vectorData.get(i), true, Project.getInstance().getProjectPath()+ "datatracks/"+ vectorTrackNames.get(i) + ".raw");
				Project.getInstance().getLcoll().addVectorTrack(
						vectorTrackNames.get(i),
						Project.getInstance().getProjectPath() + "datatracks/" + vectorTrackNames.get(i) + ".raw",
						true, "Colorful",
						mm[0], mm[1], vectorData.get(i)[0].length-1,
						Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false, false, false);//videoTrackNames.get(i),videoFiles.get(i), true);
			}
		}
		//labels
		for(int i=0; i<labelTrackNames.size();i++){
			if(Project.getInstance().getLcoll().hasTrackWithName(labelTrackNames.get(i))&&chkOverwrite.isSelected()){
				((LabelTrack)Project.getInstance().getLcoll().getTrackByName(labelTrackNames.get(i)).getTrack()).updateData(labelXML.get(i));
			}
			if(!Project.getInstance().getLcoll().hasTrackWithName(labelTrackNames.get(i))){
				try {
					Project.getInstance().getLcoll().addLabelTrack(new LabelTrack(labelXML.get(i)), true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);
				} catch (LabelClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}


		// refresh the whole system
		MessageManager.getInstance().requestTrackUpdate(new UpdateTracksEvent(this));
		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
	}




	public void setVisible(boolean visible){
		super.setVisible(visible);
		if(visible&&matlabFactory==null){

			connectToMatlab();

		}
	}

	private void connectToMatlab(){
		try {
			MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
					.setUsePreviouslyControlledSession(true)
					.setMatlabLocation(null)
					.build();
			matlabFactory = new MatlabProxyFactory(options);
			//Request a proxy
			matlabFactory.requestProxy(proxy -> {
                proxyHolder.set(proxy);

                proxy.addDisconnectionListener(proxy1 -> proxyHolder.set(null));
            });

		} catch (MatlabConnectionException ex) {
			System.err.println(ex);
		}
	}

	private void buildTrackList(){
		DefaultListModel listModel = new DefaultListModel();
		listModel.addElement("-------------Data-------------");
		Iterator<ObjectLine> iT = Project.getInstance().getLcoll().getList().iterator();
		while (iT.hasNext()) {
			listModel.addElement(iT.next());
		}
		listModel.addElement("-------------Audio------------");
		Iterator<AudioTrack> aT = Project.getInstance().getMcoll().getAudioList().iterator();
		while (aT.hasNext()) {
			listModel.addElement(aT.next());
		}
		listModel.addElement("-------------Video------------");
		Iterator<VideoTrack> vT = Project.getInstance().getMcoll().getVideoList().iterator();
		while (vT.hasNext()) {
			listModel.addElement(vT.next());
		}
		trackList.setModel(listModel);


/*		Iterator<ObjectLine> iT = Project.getInstance().getLcoll().getList().iterator();
		while(iT.hasNext()){			
			ObjectLine ol = iT.next();
			if(ol.getTrack() instanceof LabelTrack){
			}
		}
		scrollPane.removeAll();
		trackList = new JList<Object>();
		scrollPane.add(trackList);*/
	}
}

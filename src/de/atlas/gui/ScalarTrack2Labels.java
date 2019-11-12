package de.atlas.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;

import java.util.ArrayList;
import java.util.Iterator;

import de.atlas.collections.LabelTrack;
import de.atlas.collections.ObjectLine;
import de.atlas.collections.ScalarTrack;
import de.atlas.data.LabelObject;
import de.atlas.data.LabelType;
import de.atlas.data.Project;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.messagesystem.UpdateTracksListener;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;

public class ScalarTrack2Labels extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox cmbLabelTrack;
	private JComboBox cmbMask;
	private JComboBox cmbFeatureTrack;
	private JTextField textFieldThreshold;
	private JRadioButton rdbtnLess;
	private JRadioButton rdbtnEquals;
	private JRadioButton rdbtnGreater;

	/**
	 * Create the frame.
	 */
	public ScalarTrack2Labels() {
		setResizable(false);
		setTitle("ScalarTrack --> Labels");
		setBounds(0, 0, 351, 340);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblLabeltrackToClassify = new JLabel("Add Labels to LabelTrack");
		lblLabeltrackToClassify.setBounds(12, 117, 193, 15);
		contentPane.add(lblLabeltrackToClassify);

		cmbLabelTrack = new JComboBox();
		cmbLabelTrack.setBounds(12, 132, 323, 37);
		contentPane.add(cmbLabelTrack);

		cmbMask = new JComboBox();
		cmbMask.setBounds(12, 15, 323, 37);
		contentPane.add(cmbMask);

		JLabel lblDataSelectionTrack = new JLabel("ScalarTrack");
		lblDataSelectionTrack.setBounds(12, 55, 193, 15);
		contentPane.add(lblDataSelectionTrack);

		cmbFeatureTrack = new JComboBox();
		cmbFeatureTrack.setBounds(12, 70, 323, 37);
		contentPane.add(cmbFeatureTrack);

		JLabel label = new JLabel("Data Selection Track");
		label.setBounds(12, 0, 193, 15);
		contentPane.add(label);

		textFieldThreshold = new JTextField();
		textFieldThreshold.setText("0.01");
		textFieldThreshold.setBounds(116, 208, 219, 19);
		contentPane.add(textFieldThreshold);
		textFieldThreshold.setColumns(10);

		JButton btnScalarTrackToLabels = new JButton("OK");
		btnScalarTrackToLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				scalarTrackToLabels();
			}
		});
		btnScalarTrackToLabels.setBounds(12, 262, 100, 25);
		contentPane.add(btnScalarTrackToLabels);

		JButton btnChancel = new JButton("Cancel");
		btnChancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				abort();
			}
		});
		btnChancel.setBounds(237, 262, 100, 25);
		contentPane.add(btnChancel);

		rdbtnLess = new JRadioButton("less");
		rdbtnLess.setBounds(12, 177, 100, 23);
		contentPane.add(rdbtnLess);

		rdbtnEquals = new JRadioButton("equals");
		rdbtnEquals.setBounds(12, 204, 96, 23);
		contentPane.add(rdbtnEquals);

		rdbtnGreater = new JRadioButton("greater");
		rdbtnGreater.setSelected(true);
		rdbtnGreater.setBounds(12, 231, 96, 23);
		contentPane.add(rdbtnGreater);

		ButtonGroup group = new ButtonGroup();
	    group.add(rdbtnLess);
	    group.add(rdbtnEquals);
	    group.add(rdbtnGreater);
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
		buildTrackList();
	}

	protected void abort() {
		this.setVisible(false);
	}

	private void buildTrackList() {
		DefaultComboBoxModel labelListModel = new DefaultComboBoxModel();
		DefaultComboBoxModel featureListModel = new DefaultComboBoxModel();
		DefaultComboBoxModel maskListModel = new DefaultComboBoxModel();
		maskListModel.addElement("NONE (use all data)");
		Iterator<ObjectLine> iT = Project.getInstance().getLcoll().getList()
		.iterator();
		while (iT.hasNext()) {
			ObjectLine ol = iT.next();
			if (ol.getTrack() instanceof LabelTrack) {
				labelListModel.addElement(ol);
				maskListModel.addElement(ol);
			}
			if (ol.getTrack() instanceof ScalarTrack)
				featureListModel.addElement(ol);
		}
		this.cmbLabelTrack.setModel(labelListModel);
		this.cmbFeatureTrack.setModel(featureListModel);
		this.cmbMask.setModel(maskListModel);
	}

	private void scalarTrackToLabels() {
		LabelTrack selectionTrack = null;
		if (cmbMask.getSelectedItem() instanceof ObjectLine) {
			selectionTrack = (LabelTrack) ((ObjectLine) (cmbMask
					.getSelectedItem())).getTrack();
			ObjectLine dataTrack = (ObjectLine) ((ObjectLine) (cmbFeatureTrack
					.getSelectedItem()));
			LabelTrack destinationLabelTrack = (LabelTrack) ((ObjectLine) (cmbLabelTrack
					.getSelectedItem())).getTrack();

			ArrayList<LabelObject> selectionLabels = selectionTrack.getLabels(
					0l, (long) Project.getInstance().getProjectLength());
			Iterator<LabelObject> iL = selectionLabels.iterator();

			while (iL.hasNext()) {
				LabelObject tmp = iL.next();
				double[][] tmpData = dataTrack.getData(tmp.getStart(),
						tmp.getEnd());
				double[] tmpDataTimePoints = dataTrack.getDataTimePoints(
						tmp.getStart(), tmp.getEnd());
				createLabels(tmpData,tmpDataTimePoints,destinationLabelTrack);
				
			}
		} else {
			ObjectLine dataTrack = (ObjectLine) ((ObjectLine) (cmbFeatureTrack
					.getSelectedItem()));
			LabelTrack destinationLabelTrack = (LabelTrack) ((ObjectLine) (cmbLabelTrack
					.getSelectedItem())).getTrack();

			double[][] tmpData = dataTrack.getData(0l, (long) Project
					.getInstance().getProjectLength());
			double[] tmpDataTimePoints = dataTrack.getDataTimePoints(0l,
					(long) Project.getInstance().getProjectLength());
			createLabels(tmpData,tmpDataTimePoints,destinationLabelTrack);
		}
		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
		this.setVisible(false);
	}

	private void createLabels(double[][] tmpData, double[] tmpDataTimePoints, LabelTrack destinationLabelTrack) {
		double thresh = Double.parseDouble(textFieldThreshold.getText());
		boolean above = false;
		double start = 0, end = 0;

		if(this.rdbtnGreater.isSelected()){
			for (int i = 0; i < tmpData.length; i++) {
				if (tmpData[i][0] >= thresh && !above) {
					above = true;
					start = tmpDataTimePoints[i];
				}
				if (tmpData[i][0] < thresh && above) {
					above = false;
					end = tmpDataTimePoints[i];
					destinationLabelTrack.addLabel(new LabelObject(
							"above thresh (" + thresh + ")", "",
							(long) start, (long) end, 0.0,
							LabelType.AUTOMATIC, destinationLabelTrack
							.getLabelClass(), null, System
							.currentTimeMillis()));
				}
			}
			if (above) {
				destinationLabelTrack.addLabel(new LabelObject(
						"above thresh (" + thresh + ")", "", (long) start,
						(long) end, 0.0, LabelType.AUTOMATIC,
						destinationLabelTrack.getLabelClass(), null, System
						.currentTimeMillis()));
			}
		}else if(this.rdbtnLess.isSelected()){
			for (int i = 0; i < tmpData.length; i++) {
				if (tmpData[i][0] <= thresh && !above) {
					above = true;
					start = tmpDataTimePoints[i];
				}
				if (tmpData[i][0] > thresh && above) {
					above = false;
					end = tmpDataTimePoints[i];
					destinationLabelTrack.addLabel(new LabelObject(
							"above thresh (" + thresh + ")", "",
							(long) start, (long) end, 0.0,
							LabelType.AUTOMATIC, destinationLabelTrack
							.getLabelClass(), null, System
							.currentTimeMillis()));
				}
			}
			if (above) {
				destinationLabelTrack.addLabel(new LabelObject(
						"above thresh (" + thresh + ")", "", (long) start,
						(long) end, 0.0, LabelType.AUTOMATIC,
						destinationLabelTrack.getLabelClass(), null, System
						.currentTimeMillis()));
			}
		}else if(this.rdbtnEquals.isSelected()){
			for (int i = 0; i < tmpData.length; i++) {
				if (tmpData[i][0] == thresh && !above) {
					above = true;
					start = tmpDataTimePoints[i];
				}
				if (tmpData[i][0] != thresh && above) {
					above = false;
					end = tmpDataTimePoints[i];
					destinationLabelTrack.addLabel(new LabelObject(
							"above thresh (" + thresh + ")", "",
							(long) start, (long) end, 0.0,
							LabelType.AUTOMATIC, destinationLabelTrack
							.getLabelClass(), null, System
							.currentTimeMillis()));
				}
			}
			if (above) {
				destinationLabelTrack.addLabel(new LabelObject(
						"above thresh (" + thresh + ")", "", (long) start,
						(long) end, 0.0, LabelType.AUTOMATIC,
						destinationLabelTrack.getLabelClass(), null, System
						.currentTimeMillis()));
			}
		}		
	}
}

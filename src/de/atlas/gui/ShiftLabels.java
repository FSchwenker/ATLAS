package de.atlas.gui;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;

import java.util.ArrayList;
import java.util.Iterator;

import de.atlas.collections.LabelTrack;
import de.atlas.collections.ObjectLine;
import de.atlas.data.LabelObject;
import de.atlas.data.Project;
import de.atlas.messagesystem.LabelSelectionEvent;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.messagesystem.UpdateTracksListener;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ShiftLabels extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox cmbLabelTrack;
	private JTextField txtShift;


	/**
	 * Create the frame.
	 */
	public ShiftLabels() {
		setResizable(false);
		setTitle("Shift all Labels");
		setBounds(0, 0, 326, 175);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblLabeltrackToClassify = new JLabel("LabelTrack");
		lblLabeltrackToClassify.setBounds(12, 12, 193, 15);
		contentPane.add(lblLabeltrackToClassify);

		cmbLabelTrack = new JComboBox();
		cmbLabelTrack.setBounds(12, 27, 295, 37);
		contentPane.add(cmbLabelTrack);

		JButton btnScalarTrackToLabels = new JButton("Shift");
		btnScalarTrackToLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				shift(Long.parseLong(txtShift.getText()));
				setVisible(false);
				MessageManager.getInstance().selectionChanged(new LabelSelectionEvent(this, null,null,null));
			}
		});
		btnScalarTrackToLabels.setBounds(12, 103, 100, 25);
		contentPane.add(btnScalarTrackToLabels);

		JButton btnChancel = new JButton("Cancel");
		btnChancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				abort();
			}
		});
		btnChancel.setBounds(207, 103, 100, 25);
		contentPane.add(btnChancel);
		
		JLabel lblTimeToShift = new JLabel("Shift in milliseconds:");
		lblTimeToShift.setBounds(12, 76, 173, 15);
		contentPane.add(lblTimeToShift);
		
		txtShift = new JTextField();
		txtShift.setText("0");
		txtShift.setBounds(166, 76, 141, 19);
		contentPane.add(txtShift);
		txtShift.setColumns(10);

		MessageManager.getInstance().addUpdateTracksListener(new UpdateTracksListener(){
			@Override
			public void updateTracks(UpdateTracksEvent e) {
				if(!(e.getSource().getClass().toString().equals(LearningWindow.class.toString()))){
					buildTrackList();
				}

			}			 
		});
		buildTrackList();


	}
	protected void shift(long val) {
		LabelTrack destinationLabelTrack = (LabelTrack) ((ObjectLine)(cmbLabelTrack.getSelectedItem())).getTrack();

		ArrayList<LabelObject> labels = destinationLabelTrack.getLabels(0l,(long) Project.getInstance().getProjectLength());
		Iterator<LabelObject> iL = labels.iterator();

		while(iL.hasNext()){
			LabelObject tmp = iL.next();
			tmp.setStart(tmp.getStart()+val);
			tmp.setEnd(tmp.getEnd()+val);
		}

	}
	protected void abort() {
		this.setVisible(false);
	}
	private void buildTrackList(){
		DefaultComboBoxModel labelListModel = new DefaultComboBoxModel();
		Iterator<ObjectLine> iT = Project.getInstance().getLcoll().getList().iterator();
		while(iT.hasNext()){			
			ObjectLine ol = iT.next();
			if(ol.getTrack() instanceof LabelTrack){
				labelListModel.addElement(ol);
			}
		}
		this.cmbLabelTrack.setModel(labelListModel);
	}
}

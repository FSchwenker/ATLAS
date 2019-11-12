package de.atlas.gui;


import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;

import java.util.Iterator;

import de.atlas.collections.LabelTrack;
import de.atlas.collections.ObjectLine;
import de.atlas.data.Project;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.messagesystem.UpdateTracksListener;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ClearLabelTrack extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox cmbLabelTrack;


	/**
	 * Create the frame.
	 */
	public ClearLabelTrack() {
		setResizable(false);
		setTitle("Clear LabelTrack");
		setBounds(0, 0, 329, 150);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblLabeltrackToClassify = new JLabel("LabelTrack");
		lblLabeltrackToClassify.setBounds(12, 12, 193, 15);
		contentPane.add(lblLabeltrackToClassify);

		cmbLabelTrack = new JComboBox();
		cmbLabelTrack.setBounds(12, 27, 291, 37);
		contentPane.add(cmbLabelTrack);

		JButton btnScalarTrackToLabels = new JButton("Clear");
		btnScalarTrackToLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				((LabelTrack) ((ObjectLine)(cmbLabelTrack.getSelectedItem())).getTrack()).clear();
				setVisible(false);
			}
		});
		btnScalarTrackToLabels.setBounds(12, 76, 100, 25);
		contentPane.add(btnScalarTrackToLabels);

		JButton btnChancel = new JButton("Cancel");
		btnChancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				abort();
			}
		});
		btnChancel.setBounds(203, 76, 100, 25);
		contentPane.add(btnChancel);

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

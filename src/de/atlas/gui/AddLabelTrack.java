package de.atlas.gui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.atlas.collections.LineCollection;
import de.atlas.collections.ObjectLine;
import de.atlas.data.LabelClass;
import de.atlas.data.LabelClasses;
import de.atlas.data.Project;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.misc.HelperFunctions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AddLabelTrack extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField nameTextField;
	private LineCollection lcoll;
	private JComboBox classComboBox;

	/**
	 * Create the dialog.
	 */
	public AddLabelTrack() {
		this.setIconImage(new ImageIcon(Project.class.getResource("/icon.gif"))
				.getImage());
		setTitle("Add LabelTrack");
		setBounds(100, 100, 470, 170);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JButton okButton = new JButton("OK");
			okButton.setBounds(286, 71, 54, 25);
			contentPanel.add(okButton);
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					addTrack();
				}
			});
			okButton.setActionCommand("OK");
			getRootPane().setDefaultButton(okButton);
		}
		{
			JButton cancelButton = new JButton("Cancel");
			cancelButton.setBounds(352, 71, 81, 25);
			contentPanel.add(cancelButton);
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
				}
			});
			cancelButton.setActionCommand("Cancel");
		}

		classComboBox = new JComboBox();
		classComboBox.setBounds(71, 39, 147, 24);
		contentPanel.add(classComboBox);
		classComboBox.setModel(LabelClasses.getInstance());
		classComboBox.setSelectedIndex(0);

		JLabel lblClass = new JLabel("Class:");
		lblClass.setBounds(10, 44, 43, 15);
		contentPanel.add(lblClass);

		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(10, 12, 45, 15);
		contentPanel.add(lblName);

		nameTextField = new JTextField();
		nameTextField.setBounds(71, 10, 362, 19);
		contentPanel.add(nameTextField);
		nameTextField.setColumns(18);
	}

	public static void showDialog(Component owner, String title,
			LineCollection lcoll, String path) {

		AddLabelTrack dialog = new AddLabelTrack();
		dialog.lcoll = lcoll;
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setTitle(title);
		dialog.setModal(true);
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);

	}

	private void addTrack() {
		if (this.nameTextField.getText().length() > 0) {
			if ((HelperFunctions.testAndGenerateFile(Project.getInstance()
					.getProjectPath()
					+ "labeltracks/"
					+ this.nameTextField.getText() + ".xml")) == null) {
				return;
			}
			lcoll.addLabelTrack(this.nameTextField.getText(),
					(LabelClass) this.classComboBox.getSelectedItem(), Project
							.getInstance().getLcoll().getOlinesSize(),
					ObjectLine.MEDIUM);
			lcoll.updateViewport();
			Project.getInstance().saveProject();
		}
		MessageManager.getInstance().requestTrackUpdate(
				new UpdateTracksEvent(this));
		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
		this.setVisible(false);
	}
}

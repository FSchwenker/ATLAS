package de.atlas.gui;

import de.atlas.collections.LineCollection;
import de.atlas.collections.ObjectLine;
import de.atlas.data.LabelClass;
import de.atlas.data.LabelClasses;
import de.atlas.data.Project;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.misc.HelperFunctions;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class AddScalarTrack extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
    private JTextField nameTextField;
    private JTextField minTextField;
    private JTextField maxTextField;
	private LineCollection lcoll;

	/**
	 * Create the dialog.
	 */
	public AddScalarTrack() {
		this.setIconImage(new ImageIcon(Project.class.getResource("/icon.gif"))
				.getImage());
		setTitle("Add ScalarTrack");
		setBounds(100, 100, 470, 170);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JButton okButton = new JButton("OK");
			okButton.setBounds(286, 85, 54, 25);
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
			cancelButton.setBounds(352, 85, 81, 25);
			contentPanel.add(cancelButton);
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
				}
			});
			cancelButton.setActionCommand("Cancel");
		}

		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(20, 12, 45, 15);
		contentPanel.add(lblName);

		nameTextField = new JTextField();
		nameTextField.setBounds(71, 10, 362, 19);
		contentPanel.add(nameTextField);
		nameTextField.setColumns(18);

        JLabel lblMin = new JLabel("Min:");
        lblMin.setBounds(30,50,45,15);
        contentPanel.add(lblMin);

        minTextField = new JTextField();
        minTextField.setColumns(5);
        minTextField.setText("0.0");
        minTextField.setBounds(71,48,50,19);
        contentPanel.add(minTextField);

        JLabel lblMax = new JLabel("Max:");
        lblMax.setBounds(150,50,45,15);
        contentPanel.add(lblMax);

        maxTextField = new JTextField();
        maxTextField.setColumns(5);
        maxTextField.setText("1.0");
        maxTextField.setBounds(195,48,50,19);
        contentPanel.add(maxTextField);

    }

	public static void showDialog(Component owner, String title,
			LineCollection lcoll, String path) {

		AddScalarTrack dialog = new AddScalarTrack();
		dialog.lcoll = lcoll;
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setTitle(title);
		dialog.setModal(true);
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);

	}

	private void addTrack() {
		if (this.nameTextField.getText().length() > 0) {
			String fileName = Project.getInstance().getProjectPath() + "datatracks/" + this.nameTextField.getText() + ".raw";
		    if ((HelperFunctions.testAndGenerateFile(fileName)) == null) return;
			try {
				File file = new File(fileName);
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			lcoll.addScalarTrack(nameTextField.getText(),fileName,true,true,true, Double.parseDouble(minTextField.getText()),Double.parseDouble(maxTextField.getText()),lcoll.getOlinesSize(),false,ObjectLine.MEDIUM,false);
			lcoll.updateViewport();
			//Project.getInstance().saveProject();
		}
		MessageManager.getInstance().requestTrackUpdate(new UpdateTracksEvent(this));
		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
		this.setVisible(false);
	}
}

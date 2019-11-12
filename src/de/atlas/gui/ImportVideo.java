package de.atlas.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.atlas.collections.MediaCollection;
import de.atlas.data.Project;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import eu.c_bauer.userinputverifier.UserInputVerifier;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

public class ImportVideo extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JButton fileButton;
	private JTextField nameTextField;
	private JTextField fileTextField;
	private MediaCollection mcoll;
	private String path;
	private JButton okButton;

	/**
	 * Create the dialog.
	 */
	public ImportVideo() {
		setTitle("Import Video");
		this.setIconImage(new ImageIcon(Project.class.getResource("/icon.gif"))
				.getImage());
		setBounds(100, 100, 460, 155);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		fileButton = new JButton("...");
		fileButton.setBounds(387, 34, 49, 25);
		contentPanel.add(fileButton);

		fileTextField = new JTextField();
		fileTextField.setBounds(75, 37, 300, 19);
		contentPanel.add(fileTextField);
		fileTextField.setColumns(28);

		JLabel lblFile = new JLabel("File:");
		lblFile.setBounds(12, 39, 30, 15);
		contentPanel.add(lblFile);

		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(12, 12, 45, 15);
		contentPanel.add(lblName);

		nameTextField = new JTextField();
		nameTextField.setBounds(75, 10, 300, 19);
		contentPanel.add(nameTextField);
		nameTextField.setColumns(18);
		fileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fileChooser();
			}
		});
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						addTrack();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		UserInputVerifier uiv = new UserInputVerifier();
		uiv.setTextFieldRestriction_ExistingNormalFile(fileTextField, "tfFile",
				true, false, false);
		uiv.setTextFieldRestriction_NonEmpty(fileTextField, "tfFile", false,
				false);
		uiv.setTextFieldRestriction_WindowsFilename(nameTextField, "tfname",
				false, true, false, false);
		uiv.setTextFieldRestriction_NonEmpty(nameTextField, "tfname", false,
				false);
		uiv.addDependentButton(okButton);
	}

	public static void showDialog(Component owner, String title,
			MediaCollection mcoll, String path) {

		ImportVideo dialog = new ImportVideo();
		dialog.mcoll = mcoll;
		dialog.path = path;
		if (!(new File(path).isDirectory())) {
			dialog.fileTextField.setText(path);
		}
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setTitle(title);
		dialog.setLocationRelativeTo(owner);
		dialog.setModal(true);
		dialog.setVisible(true);

	}

	private void fileChooser() {

		JFileChooser fc = new JFileChooser(this.path);
		FileFilter ff = null;
		ff = new FileNameExtensionFilter("Media", "avi", "mp4", "m4v", "mpg",
				"mpeg", "wmv", "mov", "FLV");

		if (ff != null) {
			fc.setFileFilter(ff);
		}

		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			this.fileTextField.setText(fc.getSelectedFile().getPath());
		}

	}

	private void addTrack() {
		if (this.nameTextField.getText().length() > 0
				&& this.fileTextField.getText().length() > 0) {
			mcoll.addVideoTrack(this.nameTextField.getText(),
					this.fileTextField.getText(), true, false);
			mcoll.setTime(Project.getInstance().getTime());

		}

		MessageManager.getInstance().requestTrackUpdate(
				new UpdateTracksEvent(this));
		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
		this.setVisible(false);
	}
}

package de.atlas.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.atlas.data.Project;
import eu.c_bauer.userinputverifier.UserInputVerifier;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class NewProject extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JButton fileButton;
	private JTextField nameTextField;
	private JTextField baseDirTextField;
	private UserInputVerifier uiv_;

	JButton okButton;

	private static boolean newProject = false;

	/**
	 * Create the dialog.
	 */
	public NewProject() {
		setResizable(false);
		setTitle("New Project");
		this.setIconImage(new ImageIcon(Project.class.getResource("/icon.gif"))
				.getImage());
		setBounds(100, 100, 502, 135);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		baseDirTextField = new JTextField();
		baseDirTextField.setBounds(119, 37, 301, 19);
		contentPanel.add(baseDirTextField);
		baseDirTextField
				.setToolTipText("Provide a folder for the project - Atlas will create a subfolder with the projects name.");
		baseDirTextField.setColumns(29);

		JLabel lblFile = new JLabel("Base Folder:");
		lblFile.setBounds(12, 39, 89, 15);
		contentPanel.add(lblFile);

		fileButton = new JButton("...");
		fileButton.setBounds(432, 34, 56, 25);
		contentPanel.add(fileButton);

		JLabel lblName = new JLabel("Project Name:");
		lblName.setBounds(12, 12, 99, 15);
		contentPanel.add(lblName);

		nameTextField = new JTextField();
		nameTextField.setBounds(119, 10, 301, 19);
		contentPanel.add(nameTextField);
		nameTextField.setToolTipText("provide a name for the project");
		nameTextField.setColumns(28);
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
						newProject();
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

		// set userinputverifier
		uiv_ = new UserInputVerifier();
		uiv_.setTextFieldRestriction_NonEmpty(nameTextField, "tfName", false,
				false);
		uiv_.setTextFieldRestriction_WindowsFilename(nameTextField, null,
				false, true, false, false);
		uiv_.setTextFieldRestriction_NonEmpty(baseDirTextField, "tfBaseDir",
				false, false);
		uiv_.setTextFieldRestriction_ExistingDirectory(baseDirTextField, null,
				true, false, false);
		uiv_.addDependentButton(okButton);
	}

	public static boolean showDialog(Component owner, String title) {

		NewProject dialog = new NewProject();
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setTitle(title);
		dialog.setModal(true);
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);

		return newProject;

	}

	private void fileChooser() {

		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			this.baseDirTextField.setText(fc.getSelectedFile().getPath());
		}

	}

	private void newProject() {

		if (this.nameTextField.getText().length() > 0
				&& this.baseDirTextField.getText().length() > 0) {
			Project.getInstance().newProject(this.baseDirTextField.getText(),
					this.nameTextField.getText());
		}
		NewProject.newProject = true;
		this.setVisible(false);
	}
}

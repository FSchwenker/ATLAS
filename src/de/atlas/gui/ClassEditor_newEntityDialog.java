package de.atlas.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.atlas.data.LabelClass;
import de.atlas.data.LabelClassEntity;
import de.atlas.data.Project;
import de.atlas.messagesystem.ClassChangedEvent;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import eu.c_bauer.userinputverifier.UserInputVerifier;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ClassEditor_newEntityDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
    private static Color defaultNewEntityColor = Color.lightGray;
    private static Color defaultNewEntityContinuousColor = Color.darkGray;

	private JTextField tfName;
	private LabelClass selectedClass = null;

	/**
	 * Create the dialog.
	 */
	public ClassEditor_newEntityDialog(String[] forbiddenStrings) {
		setResizable(false);
		this.setIconImage(new ImageIcon(Project.class.getResource("/icon.gif"))
				.getImage());
		setBounds(100, 100, 391, 112);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(12, 12, 45, 15);
		contentPanel.add(lblName);

		tfName = new JTextField();
		tfName.setBounds(75, 10, 300, 19);
		contentPanel.add(tfName);
		tfName.setColumns(18);

		UserInputVerifier uiv = new UserInputVerifier();
		uiv.setTextFieldRestriction_WindowsFilename(tfName, "tfname", false,
				true, false, false);
		uiv.setTextFieldRestriction_NonEmpty(tfName, "tfname", false, false);
		uiv.setTextFieldRestriction_ForbiddenStrings(tfName, "tfname",
				forbiddenStrings, true, false, false);

		JButton okButton = new JButton("OK");
		okButton.setBounds(228, 41, 54, 25);
		contentPanel.add(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addEntity();
			}
		});
		okButton.setActionCommand("OK");
		getRootPane().setDefaultButton(okButton);
		uiv.addDependentButton(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBounds(294, 41, 81, 25);
		contentPanel.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		cancelButton.setActionCommand("Cancel");
	}

	public static void showDialog(Component owner, LabelClass selectedClass,
			String title, String[] forbiddenStrings) {

		ClassEditor_newEntityDialog dialog = new ClassEditor_newEntityDialog(
				forbiddenStrings);
		dialog.setClass(selectedClass);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setTitle(title);
		dialog.setLocationRelativeTo(owner);
		dialog.setModal(true);
		dialog.setVisible(true);
	}

	private void setClass(LabelClass selectedClass) {
		this.selectedClass = selectedClass;
	}

	private void addEntity() {

		if (null != selectedClass) {
			String name = tfName.getText();
			LabelClassEntity entity = new LabelClassEntity(selectedClass, name,
					0, defaultNewEntityColor, defaultNewEntityContinuousColor);
			selectedClass.addEntity(entity);

			// selectedClass.addEntity(new LabelClassEntity(selectedClass,
			// this.classEntityNameTextField.getText(), id,
			// this.classEntityColorButton.getBackground()));

			MessageManager.getInstance().requestClassChanged(
					new ClassChangedEvent(this));
			MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
		} else {
			System.out.println("selectedClass is null");
		}
		this.setVisible(false);
	}
}

package de.atlas.gui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.DefaultComboBoxModel;
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

import de.atlas.collections.LabelTrack;
import de.atlas.collections.LineCollection;
import de.atlas.collections.MediaCollection;
import de.atlas.collections.ObjectLine;
import de.atlas.data.LabelClassEntity;
import de.atlas.data.LabelObject;
import de.atlas.data.Project;
import eu.c_bauer.userinputverifier.UserInputVerifier;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import au.com.bytecode.opencsv.CSVWriter;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LabelTrackCsvExporter extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel_ = new JPanel();
	private JButton fileButton_;
	private JTextField tfOutFile_;
	private String path_;
	private JButton okButton_;
	private JButton cancelButton_;
	private JTextField tfDelimiter_;
	private JComboBox cbLabelTrack_;
	private JTextField tfStartTime_;
	private JTextField tfEndTime_;
	private JCheckBox chckbxWriteHeaderLine_;
	private JCheckBox chckbxAll_;

	/**
	 * Create the dialog.
	 */
	public LabelTrackCsvExporter() {
		setTitle("Export LabelTrack as CSV");
		this.setIconImage(new ImageIcon(Project.class.getResource("/icon.gif")).getImage());
		setBounds(100, 100, 520, 290);
		getContentPane().setLayout(new BorderLayout());
		contentPanel_.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel_, BorderLayout.CENTER);
		contentPanel_.setLayout(null);

		JLabel lblLabelTrack = new JLabel("LabelTrack:");
		lblLabelTrack.setBounds(12, 14, 82, 15);
		contentPanel_.add(lblLabelTrack);
		{
			cancelButton_ = new JButton("Cancel");
			cancelButton_.setBounds(383, 195, 100, 25);
			contentPanel_.add(cancelButton_);
			cancelButton_.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
				}
			});
			cancelButton_.setActionCommand("Cancel");
		}

		JLabel lblFile = new JLabel("File:");
		lblFile.setBounds(12, 54, 30, 15);
		contentPanel_.add(lblFile);

		tfOutFile_ = new JTextField();
		tfOutFile_.setBounds(60, 52, 362, 19);
		contentPanel_.add(tfOutFile_);
		tfOutFile_.setColumns(28);

		fileButton_ = new JButton("...");
		fileButton_.setBounds(434, 49, 49, 25);
		contentPanel_.add(fileButton_);

		okButton_ = new JButton("OK");
		okButton_.setBounds(271, 195, 100, 25);
		contentPanel_.add(okButton_);
		okButton_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!exportLabelTrack()) {
					System.err.println("csv export failed");
				}
			}
		});
		okButton_.setActionCommand("OK");
		getRootPane().setDefaultButton(okButton_);

		fileButton_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fileChooser();
			}
		});

		JLabel lblDelimiter = new JLabel("Delimiter:");
		lblDelimiter.setBounds(12, 83, 69, 15);
		contentPanel_.add(lblDelimiter);

		tfDelimiter_ = new JTextField();
		tfDelimiter_.setText(",");
		tfDelimiter_.setBounds(99, 81, 39, 19);
		contentPanel_.add(tfDelimiter_);
		tfDelimiter_.setColumns(10);

		JLabel lblDelimiterTabExplain = new JLabel("(\"\\t\" for tabulator)");
		lblDelimiterTabExplain.setBounds(156, 85, 189, 15);
		contentPanel_.add(lblDelimiterTabExplain);

		chckbxWriteHeaderLine_ = new JCheckBox("write header line");
		chckbxWriteHeaderLine_.setBounds(12, 106, 147, 23);
		contentPanel_.add(chckbxWriteHeaderLine_);

		cbLabelTrack_ = new JComboBox();
		cbLabelTrack_.setBounds(106, 3, 316, 37);
		contentPanel_.add(cbLabelTrack_);

		JLabel lblLimitExportBy = new JLabel(
				"Limit export by time (in millis):");
		lblLimitExportBy.setBounds(12, 137, 212, 15);
		contentPanel_.add(lblLimitExportBy);

		tfStartTime_ = new JTextField();
		tfStartTime_.setColumns(10);
		tfStartTime_.setBounds(79, 164, 130, 19);
		contentPanel_.add(tfStartTime_);

		tfEndTime_ = new JTextField();
		tfEndTime_.setColumns(10);
		tfEndTime_.setBounds(286, 164, 136, 19);
		contentPanel_.add(tfEndTime_);

		JLabel lblStart = new JLabel("Start");
		lblStart.setBounds(22, 164, 39, 19);
		contentPanel_.add(lblStart);

		JLabel lblEnd = new JLabel("End");
		lblEnd.setBounds(238, 166, 30, 15);
		contentPanel_.add(lblEnd);

		chckbxAll_ = new JCheckBox("all");
		chckbxAll_.setBounds(434, 10, 49, 23);
		contentPanel_.add(chckbxAll_);

		init();
	}

	private void init() {
		cbLabelTrack_.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				// set textFields for time
				long end = (long) Project.getInstance().getProjectLength();
				tfStartTime_.setText("0");
				tfEndTime_.setText("" + end);
			}
		});

		chckbxAll_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (chckbxAll_.isSelected()) {
					cbLabelTrack_.setEnabled(false);
				} else {
					cbLabelTrack_.setEnabled(true);
					cbLabelTrack_.setSelectedIndex(-1);
					cbLabelTrack_.setSelectedIndex(0);
				}
			}
		});

		// labeltracks in combobox
		buildTrackList();
		// trigger item selection action
		try {
			cbLabelTrack_.setSelectedIndex(-1);
			cbLabelTrack_.setSelectedIndex(0);
		} catch (Exception e) {
		}

		// add UserInputVerifier
		UserInputVerifier uiv = new UserInputVerifier();

		uiv.setTextFieldRestriction_WindowsFilename(tfOutFile_, "tfFile", true,
				true, false, false);
		uiv.setTextFieldRestriction_NonEmpty(tfOutFile_, null, false, false);
		uiv.addDependentButton(okButton_);
		uiv.setTextFieldRestriction_PositiveNumericValue(tfStartTime_,
				"tfStartTime", true, false, false);
		uiv.setTextFieldRestriction_PositiveNumericValue(tfEndTime_,
				"tfEndTime", true, false, false);
		uiv.setTextFieldRestriction_Int(tfStartTime_, null, true, false, false);
		uiv.setTextFieldRestriction_Int(tfEndTime_, null, true, false, false);
		uiv.setTextFieldRestriction_NonEmpty(tfStartTime_, null, false, false);
		uiv.setTextFieldRestriction_NonEmpty(tfEndTime_, null, false, false);
	}

	public void showDialog(Component owner, String title,
                           MediaCollection mcoll, LineCollection lcoll, String path) {

		LabelTrackCsvExporter dialog = new LabelTrackCsvExporter();
		dialog.path_ = path;
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setTitle(title);
		dialog.setLocationRelativeTo(owner);
		dialog.setModal(true);
		dialog.setVisible(true);
	}

	private void fileChooser() {
		JFileChooser fc = new JFileChooser(path_);
		FileFilter ff = null;
		ff = new FileNameExtensionFilter("csv", "csv");

		if (ff != null) {
			fc.setFileFilter(ff);
		}
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String path = fc.getSelectedFile().getPath();
			if (!path.toLowerCase().endsWith(".csv")) {
				path += ".csv";
			}
			tfOutFile_.setText(path);
		}
	}

	private void buildTrackList() {
		DefaultComboBoxModel labelListModel = new DefaultComboBoxModel();
		Iterator<ObjectLine> iT = Project.getInstance().getLcoll().getList()
				.iterator();
		while (iT.hasNext()) {
			ObjectLine ol = iT.next();
			if (ol.getTrack() instanceof LabelTrack) {
				labelListModel.addElement(ol);
			}
		}
		cbLabelTrack_.setModel(labelListModel);
	}

	private boolean exportLabelTrack() {

		// disallow gui changes
		cbLabelTrack_.setEnabled(false);
		tfDelimiter_.setEditable(false);
		tfEndTime_.setEditable(false);
		tfOutFile_.setEditable(false);
		tfStartTime_.setEditable(false);
		chckbxAll_.setEnabled(false);

		ArrayList<LabelTrack> exportTracks = new ArrayList<LabelTrack>();

		if (chckbxAll_.isSelected()) {
			// all LabelTracks
			Iterator<ObjectLine> it = Project.getInstance().getLcoll()
					.getList().iterator();
			while (it.hasNext()) {
				ObjectLine ol = it.next();
				if (ol.getTrack() instanceof LabelTrack) {
					exportTracks.add((LabelTrack) ol.getTrack());
				}
			}
		} else {
			// add selected
			Object o = cbLabelTrack_.getSelectedItem();
			if (!(o instanceof ObjectLine)) {
				System.err
						.println("selected labeltrack did not return ObjectLine");
				return false;
			}
			o = ((ObjectLine) o).getTrack();
			if (!(o instanceof LabelTrack)) {
				System.err
						.println("ObjectLine did not return LabelTrack with getTrack()");
				return false;
			}
			exportTracks.add((LabelTrack) o);
		}

		char delimiter = tfDelimiter_.getText().trim().charAt(0);
		CSVWriter writer = null;
		boolean success = true;
		try {
			writer = new CSVWriter(new FileWriter(tfOutFile_.getText()),
					delimiter, CSVWriter.NO_QUOTE_CHARACTER);

			// we use one line per label with the following info
			String[] entries = new String[11];
			entries[0] = "name";
			entries[1] = "externalchange";
			entries[2] = "classname";
			entries[3] = "starttime";
			entries[4] = "endtime";
			entries[5] = "timestamp";
			entries[6] = "value";
			entries[7] = "comment";
			entries[8] = "type";
			entries[9] = "text";
			entries[10] = "classentity";

			// write headers if wanted
			if (chckbxWriteHeaderLine_.isSelected()) {
				writer.writeNext(entries);
			}

			// write all chosen tracks
			for (int j = 0; j < exportTracks.size(); j++) {
				LabelTrack lt = exportTracks.get(j);

				// set labeltrack info
				entries[0] = lt.getName();
				entries[1] = String.valueOf(lt.getLastExternalChange());
				entries[2] = lt.getLabelClass().getName();

				ArrayList<LabelObject> labels = lt.getLabels(
						Long.parseLong(tfStartTime_.getText()),
						Long.parseLong(tfEndTime_.getText()));
				for (int i = 0; i < labels.size(); i++) {
					LabelObject label = labels.get(i);

					entries[3] = String.valueOf(label.getStart());
					entries[4] = String.valueOf(label.getEnd());
					entries[5] = String.valueOf(label.getTimestamp());
					entries[6] = String.valueOf(label.getValue());
					entries[7] = setEmptyStringForNull(label.getComment());
					entries[8] = setEmptyStringForNull(String.valueOf(label
							.getLabelType()));
					entries[9] = setEmptyStringForNull(label.getText());
					LabelClassEntity lce = label.getLabelClassEntity();
					if (null != lce) {
						entries[10] = setEmptyStringForNull(label
								.getLabelClassEntity().getName());
					} else {
						entries[10] = "none";
					}
					writer.writeNext(entries);
				}// all labels - i < labels.size()
			}// all labeltracks - i < exportTracks.size()
		} catch (IOException e) {
			e.printStackTrace();
			success = false;
		} finally {
			try {
				writer.close();
			} catch (Exception ex) {
				// may fail if writer not ok, so ignore
			}
		}
		if (!success) {
			try {
				File f = new File(tfOutFile_.getText());
				f.delete();
			} catch (Exception e) {
			}
		}

		// allow gui changes
		// cbLabelTrack.setEnabled(true); done over chckbxAll_ change
		tfDelimiter_.setEditable(true);
		tfEndTime_.setEditable(true);
		tfOutFile_.setEditable(true);
		tfStartTime_.setEditable(true);
		chckbxAll_.setEnabled(true);
		if (chckbxAll_.isSelected()) {
			cbLabelTrack_.setEnabled(false);
		} else {
			cbLabelTrack_.setEnabled(true);
		}

		setVisible(false);
		return true;
	}

	/**
	 * Returns the input string unless input is null in which case the returned
	 * string will be empty.
	 * 
	 * @param input
	 * @return input or empty string in input is null
	 */
	private String setEmptyStringForNull(String input) {
		if (null == input) {
			return "";
		} else {
			return input;
		}
	}
}
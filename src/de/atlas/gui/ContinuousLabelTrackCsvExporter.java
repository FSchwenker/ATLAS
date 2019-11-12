package de.atlas.gui;

import au.com.bytecode.opencsv.CSVWriter;
import de.atlas.collections.LabelTrack;
import de.atlas.collections.LineCollection;
import de.atlas.collections.MediaCollection;
import de.atlas.collections.ObjectLine;
import de.atlas.data.LabelObject;
import de.atlas.data.Project;
import eu.c_bauer.userinputverifier.UserInputVerifier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class ContinuousLabelTrackCsvExporter extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel_ = new JPanel();
    private final JTextField tfSampleRate;
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
	public ContinuousLabelTrackCsvExporter() {
		setTitle("Export continuous LabelTrack as CSV");
		this.setIconImage(new ImageIcon(Project.class.getResource("/icon.gif")).getImage());
		setBounds(100, 100, 520, 320);
		getContentPane().setLayout(new BorderLayout());
		contentPanel_.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel_, BorderLayout.CENTER);
		contentPanel_.setLayout(null);

		JLabel lblLabelTrack = new JLabel("LabelTrack:");
		lblLabelTrack.setBounds(12, 14, 82, 15);
		contentPanel_.add(lblLabelTrack);
		{
			cancelButton_ = new JButton("Cancel");
			cancelButton_.setBounds(383, 195+29, 100, 25);
			contentPanel_.add(cancelButton_);
			cancelButton_.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
				}
			});
			cancelButton_.setActionCommand("Cancel");
		}

        JLabel lblSampleRate = new JLabel("Sample rate in Hz:");
        lblSampleRate.setBounds(12, 54, 140, 15);
        contentPanel_.add(lblSampleRate);

        tfSampleRate = new JTextField();
        tfSampleRate.setBounds(146, 52, 106, 19);
        contentPanel_.add(tfSampleRate);

        JLabel lblFile = new JLabel("File:");
        lblFile.setBounds(12, 54+29, 30, 15);
        contentPanel_.add(lblFile);

		tfOutFile_ = new JTextField();
		tfOutFile_.setBounds(60, 52+29, 362, 19);
		contentPanel_.add(tfOutFile_);
		tfOutFile_.setColumns(28);

		fileButton_ = new JButton("...");
		fileButton_.setBounds(434, 49+29, 49, 25);
		contentPanel_.add(fileButton_);

		okButton_ = new JButton("OK");
		okButton_.setBounds(271, 195+29, 100, 25);
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
		lblDelimiter.setBounds(12, 83+29, 69, 15);
		contentPanel_.add(lblDelimiter);

		tfDelimiter_ = new JTextField();
		tfDelimiter_.setText(",");
		tfDelimiter_.setBounds(99, 81+29, 39, 19);
		contentPanel_.add(tfDelimiter_);
		tfDelimiter_.setColumns(10);

		JLabel lblDelimiterTabExplain = new JLabel("(\"\\t\" for tabulator)");
		lblDelimiterTabExplain.setBounds(156, 85+29, 189, 15);
		contentPanel_.add(lblDelimiterTabExplain);

		chckbxWriteHeaderLine_ = new JCheckBox("write header line");
		chckbxWriteHeaderLine_.setBounds(12, 106+29, 147, 23);
		contentPanel_.add(chckbxWriteHeaderLine_);

		cbLabelTrack_ = new JComboBox();
		cbLabelTrack_.setBounds(106, 3, 316, 37);
		contentPanel_.add(cbLabelTrack_);

		JLabel lblLimitExportBy = new JLabel("Limit export by time (in millis):");
		lblLimitExportBy.setBounds(12, 137+29, 212, 15);
		contentPanel_.add(lblLimitExportBy);

		tfStartTime_ = new JTextField();
		tfStartTime_.setColumns(10);
		tfStartTime_.setBounds(79, 164+29, 130, 19);
		contentPanel_.add(tfStartTime_);

		tfEndTime_ = new JTextField();
		tfEndTime_.setColumns(10);
		tfEndTime_.setBounds(286, 164+29, 136, 19);
		contentPanel_.add(tfEndTime_);

		JLabel lblStart = new JLabel("Start");
		lblStart.setBounds(22, 164+29, 39, 19);
		contentPanel_.add(lblStart);

		JLabel lblEnd = new JLabel("End");
		lblEnd.setBounds(238, 166+29, 30, 15);
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
        uiv.setTextFieldRestriction_NonEmpty(tfSampleRate, null, false, false);
        uiv.setTextFieldRestriction_PositiveNumericValue(tfSampleRate,null,true,false,false);
	}

	public void showDialog(Component owner, String title,
                           MediaCollection mcoll, LineCollection lcoll, String path) {

		ContinuousLabelTrackCsvExporter dialog = new ContinuousLabelTrackCsvExporter();
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
        tfSampleRate.setEnabled(false);

        ArrayList<LabelTrack> exportTracks = new ArrayList<LabelTrack>();

        if (chckbxAll_.isSelected()) {
            // all LabelTracks
            Iterator<ObjectLine> it = Project.getInstance().getLcoll().getList().iterator();
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
                System.err.println("selected labeltrack did not return ObjectLine");
                return false;
            }
            o = ((ObjectLine) o).getTrack();
            if (!(o instanceof LabelTrack)) {
                System.err.println("ObjectLine did not return LabelTrack with getTrack()");
                return false;
            }
            exportTracks.add((LabelTrack) o);
        }

        char delimiter = tfDelimiter_.getText().trim().charAt(0);
        CSVWriter writer = null;
        for (int j = 0; j < exportTracks.size(); j++) {
            LabelTrack lt = exportTracks.get(j);
            try {
                writer = new CSVWriter(new FileWriter(exportTracks.size()>1?tfOutFile_.getText().substring(0,tfOutFile_.getText().length()-4)+"_"+lt.getName()+tfOutFile_.getText().substring(tfOutFile_.getText().length()-4):tfOutFile_.getText()), delimiter, CSVWriter.NO_QUOTE_CHARACTER);
                // we use one line per label with the following info
                String[] entries = new String[3];
                entries[0] = "time";
                entries[1] = "value";
                entries[2] = "classentity";

                // write headers if wanted
                if (chckbxWriteHeaderLine_.isSelected()) {
                    writer.writeNext(entries);
                }
                for(double i=Double.parseDouble(tfStartTime_.getText());i<Project.getInstance().getProjectLength()&&i<Double.parseDouble(tfEndTime_.getText());i+=1000.0/Double.parseDouble(tfSampleRate.getText())){
                    LabelObject lo = lt.getLabelAt(i);
                    if(lo!=null){
                        entries[0] = String.valueOf(i);
                        entries[1] = String.valueOf(lo.getInterpolationValueAt(i-lo.getStart(), lt.getInterpolationType()));
                        entries[2] = lo.getLabelClassEntity()!=null?lo.getLabelClassEntity().getName():"none";
                    }else{
                        entries[0] = String.valueOf(i);
                        entries[1] = "NaN";
                        entries[2] = "none";
                    }
                    writer.writeNext(entries);
                }
                writer.close();
            }catch(IOException e){
                e.printStackTrace();
            }finally{
                try {
                    writer.close();
                } catch (Exception ex) {
                    // may fail if writer not ok, so ignore
                }
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
        tfSampleRate.setEnabled(true);

		setVisible(false);
		return true;
	}

}
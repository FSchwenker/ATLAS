package de.atlas.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import de.atlas.collections.LabelTrack;
import de.atlas.collections.ObjectLine;
import de.atlas.data.LabelObject;
import de.atlas.data.LabelType;
import de.atlas.data.Project;
import de.atlas.messagesystem.LabelSelectionEvent;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.messagesystem.UpdateTracksListener;
import de.atlas.misc.AtlasProperties;
import eu.c_bauer.userinputverifier.UserInputVerifier;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class SearchLabels extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JComboBox cmbLabelTrack_;
	private JTextField tfSearchFor_;
	private JTextField tfStartTimeMin_;
	private JTextField tfStartTimeMax_;
	private JTextField tfEndTimeMin_;
	private JTextField tfEndTimeMax_;
	private JTextField tfValueMin_;
	private JTextField tfValueMax_;
	private JTextField tfEntity_;
	private JCheckBox chckbxSearchAllLabeltracks_;
	private JButton btnFindPrevious_;
	private JButton btnFindNext_;
	private JCheckBox chckbxInText_;
	private JCheckBox chckbxInComment_;
	private JCheckBox checkBoxRegex_;
	private JComboBox comboBoxType = null;
	private UserInputVerifier uiv_;
	private boolean useRegex_ = false;
	private long millisSearchStart_ = 0;
	private LabelTrack labelTrackUsed_ = null;
	private Object lastSearchObjectLineSelection_ = "";

	/**
	 * Create the frame.
	 */
	public SearchLabels() {
		setResizable(false);
		setTitle("Search for Labels");
		setBounds(0, 0, 445, 470);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblLabeltrackToClassify = new JLabel("Search LabelTrack");
		lblLabeltrackToClassify.setBounds(12, 12, 193, 15);
		contentPane.add(lblLabeltrackToClassify);

		cmbLabelTrack_ = new JComboBox();
		cmbLabelTrack_.setBounds(12, 39, 421, 37);
		contentPane.add(cmbLabelTrack_);

		btnFindNext_ = new JButton("Find Next");
		btnFindNext_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doSearch(false);
			}
		});
		btnFindNext_.setBounds(233, 392, 200, 25);
		contentPane.add(btnFindNext_);

		chckbxSearchAllLabeltracks_ = new JCheckBox("Search all LabelTracks");
		chckbxSearchAllLabeltracks_.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				// just gui changes - logic is in searchbutton actions
				if (chckbxSearchAllLabeltracks_.isSelected()) {
					cmbLabelTrack_.setEnabled(false);
				} else {
					cmbLabelTrack_.setEnabled(true);
					cmbLabelTrack_.setSelectedIndex(-1);
					cmbLabelTrack_.setSelectedIndex(0);
				}
			}
		});
		chckbxSearchAllLabeltracks_.setBounds(12, 84, 184, 23);
		contentPane.add(chckbxSearchAllLabeltracks_);

		JLabel lblSearchFor = new JLabel("for");
		lblSearchFor.setBounds(12, 115, 20, 19);
		contentPane.add(lblSearchFor);

		tfSearchFor_ = new JTextField();
		tfSearchFor_.setBounds(50, 115, 383, 19);
		contentPane.add(tfSearchFor_);
		tfSearchFor_.setColumns(10);

		chckbxInText_ = new JCheckBox("in text");
		chckbxInText_.setSelected(true);
		chckbxInText_.setBounds(50, 142, 70, 23);
		contentPane.add(chckbxInText_);

		chckbxInComment_ = new JCheckBox("in comment");
		chckbxInComment_.setSelected(true);
		chckbxInComment_.setBounds(50, 169, 129, 23);
		contentPane.add(chckbxInComment_);

		JLabel lblRestrictions = new JLabel("Restrictions:");
		lblRestrictions.setBounds(12, 200, 100, 15);
		contentPane.add(lblRestrictions);

		JLabel lblMinStarttime = new JLabel("starttime min.");
		lblMinStarttime.setBounds(12, 227, 108, 15);
		contentPane.add(lblMinStarttime);

		tfStartTimeMin_ = new JTextField();
		tfStartTimeMin_.setBounds(138, 225, 114, 19);
		contentPane.add(tfStartTimeMin_);
		tfStartTimeMin_.setColumns(10);

		JLabel lblEndTimeMin = new JLabel("endtime min.");
		lblEndTimeMin.setBounds(12, 258, 108, 15);
		contentPane.add(lblEndTimeMin);

		JLabel lblValueMin = new JLabel("value min.");
		lblValueMin.setBounds(12, 285, 100, 15);
		contentPane.add(lblValueMin);

		JLabel lblMax = new JLabel("max.");
		lblMax.setBounds(270, 227, 33, 15);
		contentPane.add(lblMax);

		tfStartTimeMax_ = new JTextField();
		tfStartTimeMax_.setBounds(321, 225, 114, 19);
		contentPane.add(tfStartTimeMax_);
		tfStartTimeMax_.setColumns(10);

		tfEndTimeMin_ = new JTextField();
		tfEndTimeMin_.setColumns(10);
		tfEndTimeMin_.setBounds(138, 254, 114, 19);
		contentPane.add(tfEndTimeMin_);

		JLabel label = new JLabel("max.");
		label.setBounds(270, 256, 33, 15);
		contentPane.add(label);

		tfEndTimeMax_ = new JTextField();
		tfEndTimeMax_.setColumns(10);
		tfEndTimeMax_.setBounds(321, 254, 114, 19);
		contentPane.add(tfEndTimeMax_);

		tfValueMin_ = new JTextField();
		tfValueMin_.setColumns(10);
		tfValueMin_.setBounds(138, 285, 114, 19);
		contentPane.add(tfValueMin_);

		tfValueMax_ = new JTextField();
		tfValueMax_.setColumns(10);
		tfValueMax_.setBounds(321, 285, 114, 19);
		contentPane.add(tfValueMax_);

		JLabel label_1 = new JLabel("max.");
		label_1.setBounds(270, 287, 33, 15);
		contentPane.add(label_1);

		JLabel lblType = new JLabel("type");
		lblType.setBounds(12, 323, 70, 15);
		contentPane.add(lblType);

		comboBoxType = new JComboBox();
		comboBoxType.setModel(new DefaultComboBoxModel(new String[] { "ALL",
				"MANUAL", "AUTOMATIC", "AUTO_ACCEPTED", "AUTO_REJECTED" }));
		comboBoxType.setBounds(138, 312, 295, 37);
		contentPane.add(comboBoxType);

		JLabel lblEntity = new JLabel("entity");
		lblEntity.setBounds(12, 363, 70, 15);
		contentPane.add(lblEntity);

		tfEntity_ = new JTextField();
		tfEntity_.setBounds(138, 361, 295, 19);
		contentPane.add(tfEntity_);
		tfEntity_.setColumns(10);

		checkBoxRegex_ = new JCheckBox("regular expression");

		checkBoxRegex_.setBounds(270, 169, 163, 23);
		contentPane.add(checkBoxRegex_);

		btnFindPrevious_ = new JButton("Find Previous");
		btnFindPrevious_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				doSearch(true);
			}
		});
		btnFindPrevious_.setBounds(12, 392, 200, 25);
		contentPane.add(btnFindPrevious_);

		// listen for track changes
		MessageManager.getInstance().addUpdateTracksListener(
				new UpdateTracksListener() {
					@Override
					public void updateTracks(UpdateTracksEvent e) {
						buildTrackList();
						// it the currently used track is now invisible, switch
						if (null != labelTrackUsed_) {
							ArrayList<ObjectLine> ols = new ArrayList<ObjectLine>();
							ols = Project.getInstance().getLcoll().getList();
							for (int i = 0; i < ols.size(); i++) {
								if (ols.get(i).getTrack() == labelTrackUsed_) {
									if (!ols.get(i).isActive()) {
										switchLabelTrack(false);
									}
								}
							}
						}
					}
				});

		setUserInputVerifier();
		AtlasProperties.getInstance().addJFrameBoundsWatcher("searchLab", this,
				true, false);
	}

	private void setUserInputVerifier() {
		uiv_ = new UserInputVerifier();
		uiv_.setTextFieldRestriction_Long(tfStartTimeMin_, "startMin", true,
				false, false);
		uiv_.setTextFieldRestriction_Long(tfStartTimeMax_, "startMax", true,
				false, false);
		uiv_.setTextFieldRestriction_Long(tfEndTimeMin_, "endMin", true, false,
				false);
		uiv_.setTextFieldRestriction_Long(tfEndTimeMax_, "endMax", true, false,
				false);
		uiv_.setTextFieldRestriction_Double(tfValueMin_, "valueMin", true,
				false, false);
		uiv_.setTextFieldRestriction_Double(tfValueMax_, "valueMax", true,
				false, false);

		uiv_.addDependentButton(btnFindNext_);
		uiv_.addDependentButton(btnFindPrevious_);

		checkBoxRegex_.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (checkBoxRegex_.isSelected()) {
					useRegex_ = true;
					uiv_.setTextFieldRestriction_Regex(tfSearchFor_,
							"searchFor", true, false, false);
				} else {
					useRegex_ = false;
					uiv_.removeRestrictedObject(tfSearchFor_);
				}
			}
		});
	}

	protected void shift(long val) {
		LabelTrack destinationLabelTrack = (LabelTrack) ((ObjectLine) (cmbLabelTrack_
				.getSelectedItem())).getTrack();

		ArrayList<LabelObject> labels = destinationLabelTrack.getLabels(0l,
				(long) Project.getInstance().getProjectLength());
		Iterator<LabelObject> it = labels.iterator();

		while (it.hasNext()) {
			LabelObject tmp = it.next();
			tmp.setStart(tmp.getStart() + val);
			tmp.setEnd(tmp.getEnd() + val);
		}

	}

	private void buildTrackList() {
		DefaultComboBoxModel labelListModel = new DefaultComboBoxModel();
		Iterator<ObjectLine> it = Project.getInstance().getLcoll().getList()
				.iterator();
		while (it.hasNext()) {
			ObjectLine ol = it.next();
			if (ol.getTrack() instanceof LabelTrack) {
				if (ol.isActive()) {
					labelListModel.addElement(ol);
				}
			}
		}
		this.cmbLabelTrack_.setModel(labelListModel);
		try {
			this.cmbLabelTrack_.setSelectedItem(lastSearchObjectLineSelection_);
		} catch (Exception e) {
			// ignore
		}
	}

	/**
	 * Search for the next matching label. Start at time and labeltrack
	 * remembered from last time.
	 * 
	 * @param backwards
	 *            do search backwards
	 */
	private void doSearch(boolean backwards) {
		// to remember the position of search we use:
		// private long searchTime_ = 0;
		// private LabelTrack labelTrack_ = null;
		// private int labelIndex_ = 0;

		// 0. security and get gui restrictions

		// 0. security check
		// check if there are any labeltracks exist
		if (0 == cmbLabelTrack_.getModel().getSize()) {
			return;
		}

		// reset labelTrackUsed_ if it has no labels
		try {
			if (0 == labelTrackUsed_.getLabels().size()) {
				labelTrackUsed_ = null;
			}
		} catch (Exception e) {
			labelTrackUsed_ = null;
		}

		// or if the tracks to search have changed
		if (this.chckbxSearchAllLabeltracks_.isSelected()) {
			if (null != lastSearchObjectLineSelection_) {
				lastSearchObjectLineSelection_ = null;
				labelTrackUsed_ = null;
			}
		} else {
			try {
				if (this.cmbLabelTrack_.getSelectedItem() != lastSearchObjectLineSelection_) {
					lastSearchObjectLineSelection_ = cmbLabelTrack_
							.getSelectedItem();
					labelTrackUsed_ = null;
				}
			} catch (Exception e) {
				// no track selected - empty
				lastSearchObjectLineSelection_ = null;
				labelTrackUsed_ = null;
			}
		}

		// check if activeLabel is in search selection, if yes then start there
		// if no active label exists, we use visual area for time
		// with multiple tracks to get search the last one used is the starting
		// point.
		LabelObject activeLabel = Project.getInstance().getLcoll()
				.getActiveLabel();

		boolean searchFromActiveLabel = false;
		if (null != activeLabel) {
			if (chckbxSearchAllLabeltracks_.isSelected()) {
				ComboBoxModel model = cmbLabelTrack_.getModel();
				for (int outer = 0; outer < model.getSize(); outer++) {
					LabelTrack track = (LabelTrack) ((ObjectLine) model
							.getElementAt(outer)).getTrack();
					ArrayList<LabelObject> labels = track.getLabels();
					for (int i = 0; i < labels.size(); i++) {
						if (labels.get(i) == activeLabel) {
							labelTrackUsed_ = track;
							searchFromActiveLabel = true;
							break;
						}
					}
					if (searchFromActiveLabel) {
						break;
					}
				}

			} else {
				Object o = cmbLabelTrack_.getSelectedItem();
				if (!(o instanceof ObjectLine)) {
					System.err
							.println("selected labeltrack did not return ObjectLine");
					return;
				}
				o = ((ObjectLine) o).getTrack();
				if (!(o instanceof LabelTrack)) {
					System.err
							.println("ObjectLine did not return LabelTrack with getTrack()");
					return;
				}
				labelTrackUsed_ = (LabelTrack) o;
				ArrayList<LabelObject> labels = labelTrackUsed_.getLabels();
				for (int i = 0; i < labels.size(); i++) {
					if (labels.get(i) == activeLabel) {
						searchFromActiveLabel = true;
						break;
					}
				}
			}
		} else {
			// null===activeLabel
			if (null == labelTrackUsed_) {
				// use first available track
				if (chckbxSearchAllLabeltracks_.isSelected()) {
					Iterator<ObjectLine> it = Project.getInstance().getLcoll()
							.getList().iterator();
					while (it.hasNext()) {
						ObjectLine ol = it.next();
						if (ol.getTrack() instanceof LabelTrack) {
							// label track must have labels
							if (0 < ((LabelTrack) ol.getTrack()).getLabels()
									.size()) {
								labelTrackUsed_ = (LabelTrack) ol.getTrack();
								// start from last track if backwards, so don't break
								if (!backwards) {
									break;
								}
							}
						}
					}
				} else { // !chckbxSearchAllLabeltracks_.isSelected()
					Object o = cmbLabelTrack_.getSelectedItem();
					if (!(o instanceof ObjectLine)) {
						System.err
								.println("selected labeltrack did not return ObjectLine");
						return;
					}
					o = ((ObjectLine) o).getTrack();
					if (!(o instanceof LabelTrack)) {
						System.err
								.println("ObjectLine did not return LabelTrack with getTrack()");
						return;
					}
					if (0 < ((LabelTrack) o).getLabels().size()) {
						labelTrackUsed_ = (LabelTrack) o;
					}
				}
			}
		}

		// we must have an labelTrackUsed_ not null now
		if (null == labelTrackUsed_) {
			return;
		}

		if (searchFromActiveLabel) {
			if (backwards) {
				millisSearchStart_ = activeLabel.getStart();
			} else {
				millisSearchStart_ = activeLabel.getEnd();
			}

		} else {
			// get millisSearchStart_ from displayed area
			int visibleAreaStart = Project.getInstance().getLcoll()
					.getTimeScrollBar().getValue();
			int visibleAreaEnd = (int) (visibleAreaStart + (Project
					.getInstance().getLcoll().getWidth() - Project
					.getInstance().getLcoll().getTimeTrack_().getNameP()
					.getWidth())
					/ Project.getInstance().getZoom());

			if (backwards) {
				millisSearchStart_ = visibleAreaEnd;
			} else {
				millisSearchStart_ = visibleAreaStart;
			}
		}

		// we now have:
		// - labeltrack st start search from - labelTrackUsed_
		// startpoint in millis - millisSearchStart_

		int position = 0;
		// go to initial position
		ArrayList<LabelObject> labels = labelTrackUsed_.getLabels();

		if (backwards) {
			for (position = labels.size() - 1; position >= 0; position--) {
				if (millisSearchStart_ > labels.get(position).getEnd()) {
					break;
				}
			}
			if (0 > position) {
				// use last labeltrack and start with last label
				if (this.chckbxSearchAllLabeltracks_.isSelected()) {
					switchLabelTrack(backwards);
				}
				position = labelTrackUsed_.getLabels().size() - 1;
			}
		} else {
			for (position = 0; position < labels.size(); position++) {
				if (millisSearchStart_ < labels.get(position).getStart()) {
					break;
				}
			}
			if (labels.size() <= position
			// && labels.get(position).getStart() < millisSearchStart_
			) {
				// use last labeltrack and start with last label
				if (this.chckbxSearchAllLabeltracks_.isSelected()) {
					switchLabelTrack(backwards);
				}
				position = 0;
			}
		}

		// get gui info
		String searchFor = tfSearchFor_.getText();
		Pattern regexP = Pattern.compile(searchFor);
		boolean searchInText = chckbxInText_.isSelected();
		boolean searchInComment = chckbxInComment_.isSelected();
		String type = comboBoxType.getSelectedItem().toString();
		String entity = tfEntity_.getText();
		// set largest possible intervals for default restrictions
		String tmp = "";
		long startTimeMin = Long.MIN_VALUE; // 0 would be small enough
		tmp = tfStartTimeMin_.getText();
		if (!tmp.isEmpty()) {
			startTimeMin = Long.parseLong(tmp);
		}
		long startTimeMax = Long.MAX_VALUE;
		tmp = tfStartTimeMax_.getText();
		if (!tmp.isEmpty()) {
			startTimeMax = Long.parseLong(tmp);
		}
		long endTimeMin = Long.MIN_VALUE; // 0 would be small enough
		tmp = tfEndTimeMin_.getText();
		if (!tmp.isEmpty()) {
			endTimeMin = Long.parseLong(tmp);
		}
		long endTimeMax = Long.MAX_VALUE;
		tmp = tfEndTimeMax_.getText();
		if (!tmp.isEmpty()) {
			endTimeMax = Long.parseLong(tmp);
		}
		double valueMin = Double.MIN_VALUE;
		tmp = tfValueMin_.getText();
		if (!tmp.isEmpty()) {
			valueMin = Double.parseDouble(tmp);
		}
		double valueMax = Double.MAX_VALUE;
		tmp = tfValueMax_.getText();
		if (!tmp.isEmpty()) {
			valueMax = Double.parseDouble(tmp);
		}

		// search loop
		LabelObject firstSearched = null;
		while (true) {
			labels = labelTrackUsed_.getLabels();
			LabelObject curLabel = labels.get(position);
			if (null == firstSearched) {
				firstSearched = curLabel;
			} else if (firstSearched == curLabel) {
				break;
			}

			// check if we have a match
			boolean foundMatch = false;
			if (searchFor.isEmpty()) {
				// don't check text
				foundMatch = true;
			} else {
				if (searchInText) {
					if (useRegex_) {
						if (regexP.matcher(curLabel.getText()).matches()) {
							foundMatch = true;
						}
					} else {
						if (curLabel.getText().contains(searchFor)) {
							foundMatch = true;
						}
					}
				}
				if (searchInComment) {
					if (useRegex_) {
						if (regexP.matcher(curLabel.getComment()).matches()) {
							foundMatch = true;
						}
					} else {
						if (curLabel.getComment().contains(searchFor)) {
							foundMatch = true;
						}
					}
				}
			}

			// check label type
			if (foundMatch) {
				if (!"ALL".equals(type)) {
					LabelType requiredType = LabelType.MANUAL;
					if ("AUTOMATIC".equals(type)) {
						requiredType = LabelType.AUTOMATIC;
					} else if ("AUTO_ACCEPTED".equals(type)) {
						requiredType = LabelType.AUTO_ACCEPTED;
					} else if ("AUTO_REJECTED".equals(type)) {
						requiredType = LabelType.AUTO_REJECTED;
					}

					if (curLabel.getLabelType() != requiredType) {
						foundMatch = false;
					}
				}
			}

			// check label entity
			if (foundMatch) {
				if (!entity.isEmpty()) {
					try {
						if (!curLabel.getLabelClassEntity().getName()
								.equals(entity)) {
							foundMatch = false;
						}
					} catch (Exception e) {
						// entity must match, we got none
						foundMatch = false;
					}
				}
			}

			if (foundMatch) {
				if (curLabel.getStart() < startTimeMin) {
					foundMatch = false;
				}
			}

			if (foundMatch) {
				if (curLabel.getStart() > startTimeMax) {
					foundMatch = false;
				}
			}
			if (foundMatch) {
				if (curLabel.getEnd() < endTimeMin) {
					foundMatch = false;
				}
			}
			if (foundMatch) {
				if (curLabel.getEnd() > endTimeMax) {
					foundMatch = false;
				}
			}
			if (foundMatch) {
				if (curLabel.getValue() < valueMin) {
					foundMatch = false;
				}
			}
			if (foundMatch) {
				if (curLabel.getValue() > valueMax) {
					foundMatch = false;
				}
			}

			// update active label
			if (foundMatch) {
				LabelObject preLabel = null;
				LabelObject postLabel = null;
				try {
					preLabel = labels.get(position - 1);
				} catch (Exception e) {
					// ignore
				}
				try {
					postLabel = labels.get(position + 1);
				} catch (Exception e) {
					// ignore
				}
				MessageManager.getInstance().selectionChanged(
						new LabelSelectionEvent(this, preLabel, curLabel,
								postLabel));
				return;
			}

			if (backwards) {
				position--;
				if (0 > position) {
					// reached start of track
					if (this.chckbxSearchAllLabeltracks_.isSelected()) {
						switchLabelTrack(backwards);
					}
					position = labelTrackUsed_.getLabels().size() - 1;
				}
			} else {
				position++;
				if (labels.size() <= position) {
					// reached end of track
					if (this.chckbxSearchAllLabeltracks_.isSelected()) {
						switchLabelTrack(backwards);
					}
					position = 0;
				}
			}
		}

		// we only get here if no match was found - deselect active label
		MessageManager.getInstance().selectionChanged(
				new LabelSelectionEvent(this, null, null, null));
	}

	/**
	 * When going beyond the first label (backwards) of the last label
	 * (!backwards), the track before or after the current track will be used
	 * for further searching respectively. This method sets the member variable
	 * labelTrackUsed_ accordingly.
	 * 
	 * @param backwards
	 *            search direction
	 */
	private void switchLabelTrack(boolean backwards) {
		ComboBoxModel model = this.cmbLabelTrack_.getModel();
		ArrayList<LabelTrack> tracks = new ArrayList<LabelTrack>();

		for (int i = 0; i < model.getSize(); i++) {
			tracks.add((LabelTrack) ((ObjectLine) model.getElementAt(i))
					.getTrack());
		}
		if (0 == tracks.size()) {
			System.err.println("no tracks with labels found");
			return;
		}
		int trackPos = 0;
		for (trackPos = 0; trackPos < tracks.size(); trackPos++) {
			if (tracks.get(trackPos) == labelTrackUsed_) {
				break;
			}
		}
		if (backwards) {
			trackPos--;
			if (0 > trackPos) {
				// continue with last track
				trackPos = tracks.size() - 1;
			}
		} else {
			trackPos++;
			if (tracks.size() <= trackPos) {
				// continue with first track
				trackPos = 0;
			}
		}
		labelTrackUsed_ = tracks.get(trackPos);
	}
}
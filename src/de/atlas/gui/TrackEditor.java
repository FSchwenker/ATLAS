package de.atlas.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.util.Iterator;

import de.atlas.misc.AtlasProperties;
import de.atlas.collections.AudioTrack;
import de.atlas.collections.LabelTrack;
import de.atlas.collections.LineCollection;
import de.atlas.collections.MediaCollection;
import de.atlas.collections.ObjectLine;
import de.atlas.collections.ScalarTrack;
import de.atlas.collections.VectorTrack;
import de.atlas.collections.DataTrack;
import de.atlas.collections.VideoTrack;
import de.atlas.messagesystem.LabelSelectionEvent;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.MinMaxChangedEvent;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.messagesystem.UpdateTracksListener;
import eu.c_bauer.userinputverifier.UserInputVerifier;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class TrackEditor extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	private JTextField tfName;
	private JTextField tfType;
	private LineCollection lcoll;
	private MediaCollection mcoll;
	private JList trackList;
	private JCheckBox checkBoxIsVisible;
	private JCheckBox checkBoxIsFeature;
	private JSpinner spinnerMin;
	private JSpinner spinnerMax;
    private JComboBox comboBoxInterpolation;
	private JTextPane tfFile;
	private JRadioButton rdbtnSmallLine_;
	private JRadioButton rdbtnMediumLine_;
	private JRadioButton rdbtnLargeLine_;
	private int lastSelectedTrack = -1;
	private JButton buttonUp;
	private JButton buttonDown;
	private JButton buttonDelete;
	private boolean lastSelectionFrozen = false;
    private boolean comboBoxInterpolationFlag = false;

    /**
	 * Create the frame.
	 */
	public TrackEditor(LineCollection lc, MediaCollection mc) {
		setResizable(false);
		setTitle("TrackEditor");
		this.lcoll = lc;
		this.mcoll = mc;
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(0, 0, 500, 695);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 253, 658);
		contentPane.add(panel);
		panel.setLayout(null);

		JLabel lblTracks = new JLabel("Tracks:");
		lblTracks.setBounds(12, 12, 155, 15);
		panel.add(lblTracks);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 39, 229, 607);
		panel.add(scrollPane);

		trackList = new JList();
		trackList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				selectionChanged();
			}
		});
		trackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPane.setViewportView(trackList);

		this.buildTrackList();

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(261, 0, 225, 302);
		contentPane.add(panel_1);

		buttonUp = new JButton("up");
		buttonUp.setBounds(0, 44, 86, 38);
		buttonUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				moveUp();
			}
		});

		checkBoxIsVisible = new JCheckBox("visible");
		checkBoxIsVisible.setBounds(0, 171, 98, 38);
		checkBoxIsVisible.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				activeStateChanged();
			}
		});

		buttonDelete = new JButton("delete");
		buttonDelete.setBounds(101, 44, 113, 83);
		buttonDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteTrack();
			}
		});
		panel_1.setLayout(null);
		panel_1.add(checkBoxIsVisible);
		panel_1.add(buttonUp);
		panel_1.add(buttonDelete);

		checkBoxIsFeature = new JCheckBox("is feature");
		checkBoxIsFeature.setBounds(0, 135, 116, 38);
		checkBoxIsFeature.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				learnableStateChanged();

			}

		});
		panel_1.add(checkBoxIsFeature);

		buttonDown = new JButton("down");
		buttonDown.setBounds(0, 89, 86, 38);
		buttonDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				moveDown();
			}
		});
		panel_1.add(buttonDown);

		rdbtnSmallLine_ = new JRadioButton("small Line");
		rdbtnSmallLine_.setBounds(0, 213, 149, 23);
		panel_1.add(rdbtnSmallLine_);

		rdbtnMediumLine_ = new JRadioButton("medium Line");
		rdbtnMediumLine_.setBounds(0, 244, 149, 23);
		panel_1.add(rdbtnMediumLine_);

		rdbtnLargeLine_ = new JRadioButton("large Line");
		rdbtnLargeLine_.setBounds(0, 271, 149, 23);
		panel_1.add(rdbtnLargeLine_);

		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnSmallLine_);
		group.add(rdbtnMediumLine_);
		group.add(rdbtnLargeLine_);

		rdbtnLargeLine_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lastSelectionFrozen = true;
				if (trackList.getSelectedValue() instanceof ObjectLine) {
					((ObjectLine) trackList.getSelectedValue())
							.setTrackHeight(ObjectLine.LARGE);
					MessageManager.getInstance().requestTrackUpdate(
							new UpdateTracksEvent(this));
				}
				setLastSelection();
			}
		});
		rdbtnMediumLine_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lastSelectionFrozen = true;
				if (trackList.getSelectedValue() instanceof ObjectLine) {
					((ObjectLine) trackList.getSelectedValue())
							.setTrackHeight(ObjectLine.MEDIUM);
					MessageManager.getInstance().requestTrackUpdate(
							new UpdateTracksEvent(this));
				}
				setLastSelection();
			}
		});
		rdbtnSmallLine_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lastSelectionFrozen = true;
				if (trackList.getSelectedValue() instanceof ObjectLine) {
					((ObjectLine) trackList.getSelectedValue())
							.setTrackHeight(ObjectLine.SMALL);
					MessageManager.getInstance().requestTrackUpdate(
							new UpdateTracksEvent(this));
				}
				setLastSelection();
			}
		});

		JPanel panel_2 = new JPanel();
		panel_2.setBounds(261, 314, 225, 344);
		contentPane.add(panel_2);
		panel_2.setLayout(null);

		JLabel lblNewLabel = new JLabel("Name:");
		lblNewLabel.setBounds(0, 58, 49, 15);
		panel_2.add(lblNewLabel);

		tfName = new JTextField();
		tfName.setBounds(0, 85, 216, 19);
		panel_2.add(tfName);
		tfName.setColumns(10);

		JLabel lblType = new JLabel("Type:");
		lblType.setBounds(0, 0, 49, 15);
		panel_2.add(lblType);

		tfType = new JTextField();
		tfType.setBounds(0, 27, 216, 19);
		panel_2.add(tfType);
		tfType.setEditable(false);
		tfType.setColumns(10);

		JLabel lblMin = new JLabel("Min:");
		lblMin.setBounds(0, 116, 70, 15);
		panel_2.add(lblMin);

		JLabel lblMax = new JLabel("Max:");
		lblMax.setBounds(122, 116, 70, 15);
		panel_2.add(lblMax);

		spinnerMin = new JSpinner();
		spinnerMin.setEnabled(false);
		spinnerMin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setNewMin();
			}
		});
		spinnerMin.setModel(new SpinnerNumberModel(new Double(0), null, null,
				new Double(1)));
		spinnerMin.setBounds(0, 143, 94, 20);
		panel_2.add(spinnerMin);

		spinnerMax = new JSpinner();
		spinnerMax.setEnabled(false);
		spinnerMax.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setNewMax();
			}
		});
		spinnerMax.setModel(new SpinnerNumberModel(new Double(1), null, null,
				new Double(1)));
		spinnerMax.setBounds(122, 143, 94, 20);
		panel_2.add(spinnerMax);

        JLabel lblInterpolation = new JLabel("Cont.label interpolation:");
        lblInterpolation.setBounds(0,175,216,15);
        panel_2.add(lblInterpolation);

        String[] str = {"Linear","B-Spline","Soft edge","Stairs"};
        comboBoxInterpolation = new JComboBox(str);
        comboBoxInterpolation.setBounds(0,202,216,25);
        comboBoxInterpolation.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(!comboBoxInterpolationFlag)setNewInterpolation();
                comboBoxInterpolationFlag=false;
            }
        });
        panel_2.add(comboBoxInterpolation);

        JLabel lblFile = new JLabel("File:");
        lblFile.setBounds(0, 240, 49, 15);
        panel_2.add(lblFile);

        tfFile = new JTextPane();
		tfFile.setEditable(false);
		tfFile.setBounds(0, 260, 216, 75);
		panel_2.add(tfFile);

		MessageManager.getInstance().addUpdateTracksListener(
				new UpdateTracksListener() {
					@Override
					public void updateTracks(UpdateTracksEvent e) {
						buildTrackList();
					}
				});
		AtlasProperties.getInstance().addJFrameBoundsWatcher("traEd", this,
				true, false);

		// init gui with no selected items
		selectionChanged();
		final UserInputVerifier uiv = new UserInputVerifier();
		uiv.setTextFieldRestriction_NonEmpty(tfName, "tfName", false, false);
		uiv.setTextFieldRestriction_WindowsFilename(tfName, "tfName", false,
				true, false, false);

		PropertyChangeListener uivChangeListenerName = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				String setName = null;

				if (trackList.getSelectedValue() instanceof ObjectLine) {
					setName = ((ObjectLine) trackList.getSelectedValue()).getName();
				} else if (trackList.getSelectedValue() instanceof AudioTrack) {
					setName = ((AudioTrack) trackList.getSelectedValue()).getName();
				} else if (trackList.getSelectedValue() instanceof VideoTrack) {
					setName = ((VideoTrack) trackList.getSelectedValue()).getName();
				}

				if (null != setName && uiv.isAllInputsOk() && !tfName.getText().equals(setName)) {
					final int caretPos = tfName.getCaretPosition();
					tfName.setEnabled(false);
					setNewTrackName();

					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							try {
								tfName.requestFocusInWindow();
								tfName.setCaretPosition(caretPos);
							} catch (Exception ex) {
								System.err.println("error trying to set old caretpostion in name textfield");
							}
						}
					});

					tfName.setEnabled(true);
				}
			}
		};
		uiv.addPropertyChangeListener(uivChangeListenerName);
	}

    private void setNewInterpolation() {
        lastSelectionFrozen = true;
        if (trackList.getSelectedValue() != null) {
            if (trackList.getSelectedValue() instanceof ObjectLine && ((ObjectLine) trackList.getSelectedValue()).getTrack() instanceof LabelTrack) {
                ((LabelTrack) ((ObjectLine) trackList.getSelectedValue()).getTrack()).setInterpolation(comboBoxInterpolation.getSelectedIndex() + 1);
                MessageManager.getInstance().requestTrackUpdate(new UpdateTracksEvent(this));
            }
        }
        setLastSelection();
    }

    private void setNewMin() {
		lastSelectionFrozen = true;
		if (trackList.getSelectedValue() != null) {
			if (trackList.getSelectedValue() instanceof ObjectLine
					&& ((ObjectLine) trackList.getSelectedValue()).getTrack() instanceof VectorTrack) {
				((VectorTrack) ((ObjectLine) trackList.getSelectedValue())
						.getTrack()).setMin((Double) spinnerMin.getValue());
				MessageManager.getInstance().minMaxChanged(
						new MinMaxChangedEvent(this, ((ObjectLine) trackList
								.getSelectedValue())));

			} else if (trackList.getSelectedValue() instanceof ObjectLine
					&& ((ObjectLine) trackList.getSelectedValue()).getTrack() instanceof ScalarTrack) {
				((ScalarTrack) ((ObjectLine) trackList.getSelectedValue())
						.getTrack()).setMin((Double) spinnerMin.getValue());
				MessageManager.getInstance().minMaxChanged(
						new MinMaxChangedEvent(this, ((ObjectLine) trackList
								.getSelectedValue())));
			}else if (trackList.getSelectedValue() instanceof ObjectLine
					&& ((ObjectLine) trackList.getSelectedValue()).getTrack() instanceof DataTrack) {
				((DataTrack) ((ObjectLine) trackList.getSelectedValue())
						.getTrack()).setMin((Double) spinnerMin.getValue());
				MessageManager.getInstance().minMaxChanged(
						new MinMaxChangedEvent(this, ((ObjectLine) trackList
								.getSelectedValue())));
			}

		}
		setLastSelection();
	}

	private void setNewMax() {
		lastSelectionFrozen = true;
		if (trackList.getSelectedValue() != null) {
			if (trackList.getSelectedValue() instanceof ObjectLine
					&& ((ObjectLine) trackList.getSelectedValue()).getTrack() instanceof VectorTrack) {
				((VectorTrack) ((ObjectLine) trackList.getSelectedValue())
						.getTrack()).setMax((Double) spinnerMax.getValue());
				MessageManager.getInstance().minMaxChanged(
						new MinMaxChangedEvent(this, ((ObjectLine) trackList
								.getSelectedValue())));
			} else if (trackList.getSelectedValue() instanceof ObjectLine
					&& ((ObjectLine) trackList.getSelectedValue()).getTrack() instanceof ScalarTrack) {
				((ScalarTrack) ((ObjectLine) trackList.getSelectedValue())
						.getTrack()).setMax((Double) spinnerMax.getValue());
				MessageManager.getInstance().minMaxChanged(
						new MinMaxChangedEvent(this, ((ObjectLine) trackList
								.getSelectedValue())));
			}else if (trackList.getSelectedValue() instanceof ObjectLine
					&& ((ObjectLine) trackList.getSelectedValue()).getTrack() instanceof DataTrack) {
				((DataTrack) ((ObjectLine) trackList.getSelectedValue())
						.getTrack()).setMax((Double) spinnerMax.getValue());
				MessageManager.getInstance().minMaxChanged(
						new MinMaxChangedEvent(this, ((ObjectLine) trackList
								.getSelectedValue())));
			}

		}
		setLastSelection();
	}

	private void setNewTrackName() {
		lastSelectionFrozen = true;
		if (this.trackList.getSelectedValue() != null) {
			if (this.trackList.getSelectedValue() instanceof ObjectLine) {
				((ObjectLine) this.trackList.getSelectedValue())
						.setName(this.tfName.getText());
			} else if (this.trackList.getSelectedValue() instanceof AudioTrack) {
				((AudioTrack) this.trackList.getSelectedValue())
						.setName(this.tfName.getText());
			} else if (this.trackList.getSelectedValue() instanceof VideoTrack) {
				((VideoTrack) this.trackList.getSelectedValue())
						.setName(this.tfName.getText());
			}

			MessageManager.getInstance().requestTrackUpdate(
					new UpdateTracksEvent(this));
		}
		setLastSelection();
	}

	private void deleteTrack() {
		if (this.trackList.getSelectedValue() != null) {
			if (this.trackList.getSelectedValue() instanceof ObjectLine) {
				lcoll.removeTrack((ObjectLine) this.trackList
						.getSelectedValue());
			} else {
				mcoll.removeTrack(this.trackList.getSelectedValue());
			}
			MessageManager.getInstance().requestTrackUpdate(
					new UpdateTracksEvent(this));
		}
		// try to set a selection again
		try {
			this.trackList.setSelectedIndex(0);
		} catch (Exception e) {
		}
	}

	private void moveUp() {
		if (this.trackList.getSelectedValue() instanceof ObjectLine) {
			ObjectLine tmp = ((ObjectLine) this.trackList.getSelectedValue());
			int i = lcoll.getList().indexOf(tmp);
			if (i > 0) {
				lcoll.swap(i, i - 1);
				buildTrackList();
				MessageManager.getInstance().requestTrackUpdate(
						new UpdateTracksEvent(this));
				// MessageManager.getInstance().requestRepaint(new
				// RepaintEvent(this));
				this.trackList.setSelectedValue(tmp, true);
			}
		}
	}

	private void moveDown() {
		if (this.trackList.getSelectedValue() instanceof ObjectLine) {
			ObjectLine tmp = ((ObjectLine) this.trackList.getSelectedValue());
			int i = lcoll.getList().indexOf(tmp);
			if (i + 1 < lcoll.getList().size()) {
				lcoll.swap(i, i + 1);
				buildTrackList();
				MessageManager.getInstance().requestTrackUpdate(
						new UpdateTracksEvent(this));
				// MessageManager.getInstance().requestRepaint(new
				// RepaintEvent(this));
				this.trackList.setSelectedValue(tmp, true);
			}
		}
	}

	private void buildTrackList() {
		DefaultListModel listModel = new DefaultListModel();
		listModel.addElement("-------------Data-------------");
		Iterator<ObjectLine> iT = lcoll.getList().iterator();
		while (iT.hasNext()) {
			listModel.addElement(iT.next());
		}
		listModel.addElement("-------------Audio------------");
		Iterator<AudioTrack> aT = mcoll.getAudioList().iterator();
		while (aT.hasNext()) {
			listModel.addElement(aT.next());
		}
		listModel.addElement("-------------Video------------");
		Iterator<VideoTrack> vT = mcoll.getVideoList().iterator();
		while (vT.hasNext()) {
			listModel.addElement(vT.next());
		}
		trackList.setModel(listModel);
	}

	private void activeStateChanged() {
		lastSelectionFrozen = true;
		ObjectLine selectedOl = null;
		if (this.trackList.getSelectedValue() instanceof ObjectLine) {
			if (((ObjectLine) this.trackList.getSelectedValue()).isActive()) {
				// will be switched to inactive
				// we need to deselect activelabel if it is in a labeltrack
				// switched to not visible - remember objectline for later, if
				// track is set to not visible
				selectedOl = ((ObjectLine) this.trackList.getSelectedValue());
			}
			((ObjectLine) this.trackList.getSelectedValue())
					.setActive(this.checkBoxIsVisible.isSelected());
		} else if (this.trackList.getSelectedValue() instanceof AudioTrack) {
			((AudioTrack) this.trackList.getSelectedValue())
					.setActive(this.checkBoxIsVisible.isSelected());
		} else if (this.trackList.getSelectedValue() instanceof VideoTrack) {
			((VideoTrack) this.trackList.getSelectedValue())
					.setActive(this.checkBoxIsVisible.isSelected());
			if (AtlasProperties.getInstance().isAutoarranging()) {
				WindowManager.getInstance().autoarrange();
			}
		}

		// deselect active label if it is in now invisible track
		if (null != selectedOl) {
			if (selectedOl.getTrack() instanceof LabelTrack) {
				if (((LabelTrack) selectedOl.getTrack()).hasSelectedLabel()) {
					// active label is in this track
					MessageManager.getInstance().selectionChanged(
							new LabelSelectionEvent(this, null, null, null));
				}
			}
		}

		MessageManager.getInstance().requestTrackUpdate(
				new UpdateTracksEvent(this));

		this.trackList.repaint();
		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
		setLastSelection();
	}

	private void learnableStateChanged() {
		lastSelectionFrozen = true;
		if (trackList.getSelectedValue() instanceof ObjectLine) {
			((ObjectLine) trackList.getSelectedValue())
					.setLearnable(this.checkBoxIsFeature.isSelected());
		}
		setLastSelection();
	}

	private void selectionChanged() {
		if (!lastSelectionFrozen) {
			this.lastSelectedTrack = this.trackList.getSelectedIndex();
		}
		if (this.trackList.getSelectedValue() instanceof ObjectLine) {
			activateSizeRadioButtons((ObjectLine) this.trackList.getSelectedValue());
			setTrackButtonsEnabled(true);
			this.tfType.setEnabled(true);
			this.tfType.setText(((ObjectLine) this.trackList.getSelectedValue()).getType());
            this.tfName.setEnabled(true);
			this.tfName.setText(((ObjectLine) this.trackList.getSelectedValue()).getName());
            this.tfFile.setText(((ObjectLine) this.trackList.getSelectedValue()).getPath());
            this.checkBoxIsVisible.setEnabled(true);
			this.checkBoxIsVisible.setSelected(((ObjectLine) this.trackList.getSelectedValue()).isActive());
			this.checkBoxIsFeature.setEnabled(true);
			this.checkBoxIsFeature.setSelected(((ObjectLine) this.trackList.getSelectedValue()).isLearnable());
			if (((ObjectLine) this.trackList.getSelectedValue()).getTrack() instanceof VectorTrack) {
                this.comboBoxInterpolation.setEnabled(false);
				this.spinnerMax.setEnabled(true);
				this.spinnerMin.setEnabled(true);
				spinnerMin.setValue(((VectorTrack) ((ObjectLine) trackList.getSelectedValue()).getTrack()).getMin());
				spinnerMax.setValue(((VectorTrack) ((ObjectLine) trackList.getSelectedValue()).getTrack()).getMax());
			} else if (((ObjectLine) this.trackList.getSelectedValue()).getTrack() instanceof ScalarTrack) {
				this.comboBoxInterpolation.setEnabled(false);
				this.spinnerMax.setEnabled(true);
				this.spinnerMin.setEnabled(true);
				spinnerMin.setValue(((ScalarTrack) ((ObjectLine) trackList.getSelectedValue()).getTrack()).getMin());
				spinnerMax.setValue(((ScalarTrack) ((ObjectLine) trackList.getSelectedValue()).getTrack()).getMax());
			} else if (((ObjectLine) this.trackList.getSelectedValue()).getTrack() instanceof DataTrack) {
				this.comboBoxInterpolation.setEnabled(false);
				this.spinnerMax.setEnabled(true);
				this.spinnerMin.setEnabled(true);
				spinnerMin.setValue(((DataTrack) ((ObjectLine) trackList.getSelectedValue()).getTrack()).getMin());
				spinnerMax.setValue(((DataTrack) ((ObjectLine) trackList.getSelectedValue()).getTrack()).getMax());
			} else {
                this.comboBoxInterpolation.setEnabled(true);
                this.comboBoxInterpolationFlag = true;
                this.comboBoxInterpolation.setSelectedIndex(((LabelTrack)((ObjectLine) trackList.getSelectedValue()).getTrack()).getInterpolationType()-1);
                this.spinnerMax.setEnabled(false);
				this.spinnerMin.setEnabled(false);
				this.spinnerMax.setValue(0.0);
				this.spinnerMin.setValue(0.0);
			}
		} else if (this.trackList.getSelectedValue() instanceof AudioTrack) {
			activateSizeRadioButtons(null);
			setTrackButtonsEnabled(true);
			this.tfType.setEnabled(true);
			this.tfType.setText(((AudioTrack) this.trackList.getSelectedValue()).getType());
            this.tfName.setEnabled(true);
			this.tfName.setText(((AudioTrack) this.trackList.getSelectedValue()).getName());
            this.tfFile.setText(((AudioTrack) this.trackList.getSelectedValue()).getPath());
            this.checkBoxIsVisible.setEnabled(true);
			this.checkBoxIsVisible.setSelected(((AudioTrack) this.trackList.getSelectedValue()).isActive());
			this.checkBoxIsFeature.setEnabled(false);
			this.checkBoxIsFeature.setSelected(false);
			this.spinnerMax.setEnabled(false);
			this.spinnerMin.setEnabled(false);
            this.spinnerMax.setValue(0.0);
			this.spinnerMin.setValue(0.0);
            this.comboBoxInterpolation.setEnabled(false);
		} else if (this.trackList.getSelectedValue() instanceof VideoTrack) {
			activateSizeRadioButtons(null);
			setTrackButtonsEnabled(true);
			this.tfType.setEnabled(true);
			this.tfType.setText(((VideoTrack) this.trackList.getSelectedValue()).getTrackType());
            this.tfName.setEnabled(true);
			this.tfName.setText(((VideoTrack) this.trackList.getSelectedValue()).getName());
            this.tfFile.setText(((VideoTrack) this.trackList.getSelectedValue()).getPath());
            this.checkBoxIsVisible.setEnabled(true);
			this.checkBoxIsVisible.setSelected(((VideoTrack) this.trackList.getSelectedValue()).isActive());
			this.checkBoxIsFeature.setEnabled(false);
			this.checkBoxIsFeature.setSelected(false);
			this.spinnerMax.setEnabled(false);
			this.spinnerMin.setEnabled(false);
			this.spinnerMax.setValue(0.0);
			this.spinnerMin.setValue(0.0);
            this.comboBoxInterpolation.setEnabled(false);
		} else {
			activateSizeRadioButtons(null);
			setTrackButtonsEnabled(false);
			this.tfType.setText("");
			this.tfType.setEnabled(false);
			this.tfName.setText("");
			this.tfName.setEnabled(false);
			this.tfFile.setText("");
			this.checkBoxIsFeature.setEnabled(false);
			this.checkBoxIsFeature.setSelected(false);
			this.checkBoxIsVisible.setSelected(false);
			this.checkBoxIsVisible.setEnabled(false);
			this.spinnerMax.setEnabled(false);
			this.spinnerMin.setEnabled(false);
			this.spinnerMax.setValue(0.0);
			this.spinnerMin.setValue(0.0);
            this.comboBoxInterpolation.setEnabled(false);
		}
	}

	/**
	 * Enables and disables small, medium and large radiobuttons and sets the
	 * existing values from the ObjectLine, if it is not null.
	 * 
	 * @param objectLine
	 *            selected ObjectLine, null if selected Track is not an
	 *            ObjectLine
	 */
	private void activateSizeRadioButtons(ObjectLine objectLine) {
		if (null == objectLine) {
			rdbtnSmallLine_.setEnabled(false);
			rdbtnMediumLine_.setEnabled(false);
			rdbtnLargeLine_.setEnabled(false);
		} else {
			rdbtnSmallLine_.setEnabled(true);
			rdbtnMediumLine_.setEnabled(true);
			rdbtnLargeLine_.setEnabled(true);

			int height = objectLine.getTrackHeight();

			switch (height) {
			case ObjectLine.SMALL:
				rdbtnSmallLine_.setSelected(true);
				break;
			case ObjectLine.MEDIUM:
				rdbtnMediumLine_.setSelected(true);
				break;
			case ObjectLine.LARGE:
				rdbtnLargeLine_.setSelected(true);
				break;
			default:
				break;

			}
		}
	}

	/**
	 * Disables and Enables up, down and delete buttons dependent on the
	 * selection.
	 * 
	 * @param isTrack
	 *            selection is a track
	 */
	private void setTrackButtonsEnabled(boolean isTrack) {
		if (isTrack) {
			buttonDelete.setEnabled(true);
			buttonDown.setEnabled(true);
			buttonUp.setEnabled(true);
		} else {
			buttonDelete.setEnabled(false);
			buttonDown.setEnabled(false);
			buttonUp.setEnabled(false);
		}
	}

	/**
	 * Tries to set the last selected track AFTER edt is done with current
	 * messages.
	 */
	private void setLastSelection() {
		try {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					lastSelectionFrozen = false;
					trackList.setSelectedIndex(lastSelectedTrack);
				}
			};
			SwingUtilities.invokeLater(r);
		} catch (Exception e) {
		}
	}
}
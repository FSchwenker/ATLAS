package de.atlas.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.atlas.misc.AtlasProperties;
import de.atlas.data.LabelClassEntity;
import de.atlas.data.LabelObject;
import de.atlas.data.LabelType;
import de.atlas.data.Project;
import de.atlas.data.TimeType;
import de.atlas.messagesystem.ClassChangedEvent;
import de.atlas.messagesystem.ClassChangedListener;
import de.atlas.messagesystem.LabelSelectionEvent;
import de.atlas.messagesystem.LabelSelectionListener;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.ShowLabelprobsWindowEvent;
import de.atlas.messagesystem.ShowLabelprobsWindowListener;
import de.atlas.messagesystem.TimeTypeChangedListener;
import de.atlas.messagesystem.TimeTypeEvent;
import eu.c_bauer.userinputverifier.UserInputVerifier;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class LabelProperties extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextArea textTextArea;
	private JSpinner starttimeSpinner;
	private JSpinner endtimeSpinner;
	private Calendar cal;
	private LabelObject prev;
	private LabelObject label;
	private LabelObject post;
	private JTextArea commentTextArea;
	private JComboBox classEntityComboBox;
	private JButton classEntityColorButton;
	private JRadioButton acceptRadioButton;
	private JRadioButton rejectRadioButton;
	private JFormattedTextField valueTextField;
	private JTextField labeltypeTextField;
	private NoneSelectedButtonGroup bgroup;// ButtonGroup bgroup;
	private Color manualC, autoC, acceptC, rejectC;
	private JTextField ClassTextField;
	private JLabel classEntityLabel;
	private JLabel classLabel;
	private String tmpvalue = "";
	private JSpinner dateSpinner;
	private TimeType timeType = TimeType.MILLIS;
	private Date date;
	private boolean internalChangeStart = false;
	private boolean internalChangeEnd = false;
	private UserInputVerifier uiv_ = null;
    private JCheckBox showAsFlagCheckBox;

	/**
	 * Create the frame.
	 */
	public LabelProperties() {
		setResizable(false);
		setTitle("LabelProperties");
		cal = new GregorianCalendar();
		date = new Date(-cal.getTimeZone().getOffset(0) + 100);

		manualC = AtlasProperties.getInstance().getManualColor();
		autoC = AtlasProperties.getInstance().getAutoColor();
		acceptC = AtlasProperties.getInstance().getAcceptColor();
		rejectC = AtlasProperties.getInstance().getRejectColor();

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 375, 490);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel timePanel = new JPanel();
		timePanel.setBounds(5, 5, 356, 35);
		contentPane.add(timePanel);
		timePanel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

		JPanel starttimePanel = new JPanel();
		timePanel.add(starttimePanel);

		JLabel starttimeLabel = new JLabel("Start:");
		starttimePanel.add(starttimeLabel);

		starttimeSpinner = new JSpinner();
		starttimeSpinner
				.setToolTipText("Start Index of label in hh:min:sec:millis");
		starttimeSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (internalChangeStart) {
					internalChangeStart = false;
				} else {
					adaptStartTime();
				}
			}
		});

		starttimeSpinner.setMinimumSize(new Dimension(120, 20));
		starttimeSpinner.setPreferredSize(new Dimension(120, 20));
		starttimePanel.add(starttimeSpinner);

		JPanel endtimePanel = new JPanel();
		timePanel.add(endtimePanel);

		JLabel endtimeLabel = new JLabel("End:");
		endtimePanel.add(endtimeLabel);

		endtimeSpinner = new JSpinner();
		endtimeSpinner
				.setToolTipText("End Index of label in hh:min:sec:millis");
		endtimeSpinner.setMinimumSize(new Dimension(120, 20));
		endtimeSpinner.setPreferredSize(new Dimension(120, 20));
		endtimeSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (internalChangeEnd) {
					internalChangeEnd = false;
				} else {
					adaptEndTime();
				}
			}
		});
		endtimePanel.add(endtimeSpinner);
		endtimeSpinner.setModel(new SpinnerDateModel(date, new Date(-cal
				.getTimeZone().getOffset(0)), new Date(-cal.getTimeZone()
				.getOffset(0) + (3600000 * 23)), Calendar.MILLISECOND));
		JSpinner.DateEditor de_endtimeSpinner = new JSpinner.DateEditor(
				endtimeSpinner, "HH:mm:ss:SSS");
		endtimeSpinner.setEditor(de_endtimeSpinner);
		endtimeSpinner
				.setValue(new Date(-cal.getTimeZone().getOffset(0) + 350));

		JPanel valuePanel = new JPanel();
		valuePanel.setBounds(5, 292, 356, 32);
		contentPane.add(valuePanel);
		valuePanel.setLayout(null);

		JLabel valueLabel = new JLabel("Value:");
		valueLabel.setBounds(5, 10, 45, 15);
		valuePanel.add(valueLabel);

		valueTextField = new JFormattedTextField();
		valueTextField.setBounds(55, 8, 136, 19);
		valueTextField.setText("1.0");
		valueTextField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (label != null) {
					try {
						if (valueTextField.getText().length() > 0) {
							label.setValue(Double.parseDouble(valueTextField
									.getText()));
							// Double.parseDouble(valueTextField.getText());
							tmpvalue = valueTextField.getText();
							MessageManager.getInstance().requestRepaint(
									new RepaintEvent(this));
						} else {
							label.setValue(0.0);
						}
					} catch (NumberFormatException ex) {
						valueTextField.setText(tmpvalue);
					}

				}
			}
		});

		valuePanel.add(valueTextField);
		valueTextField.setColumns(12);

		classEntityColorButton = new JButton(" ");
		classEntityColorButton.setBounds(246, 5, 98, 25);
		classEntityColorButton.setHorizontalAlignment(SwingConstants.RIGHT);
		valuePanel.add(classEntityColorButton);
		classEntityColorButton.setBackground(Color.gray);
		classEntityColorButton.setEnabled(false);

		JPanel textPanel = new JPanel();
		textPanel.setBounds(5, 35, 356, 222);
		contentPane.add(textPanel);
		textPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JPanel commentPanel = new JPanel();
		textPanel.add(commentPanel);
		commentPanel.setLayout(new BoxLayout(commentPanel, BoxLayout.Y_AXIS));

		JPanel commentlabelPanel = new JPanel();
		commentPanel.add(commentlabelPanel);
		commentlabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JLabel commentLabel = new JLabel("Comment");
		commentlabelPanel.add(commentLabel);

		commentTextArea = new JTextArea(5, 31);
		commentTextArea.setToolTipText("Add comment for this label");
		commentTextArea.setLineWrap(true);
		commentTextArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (label != null) {
					label.setComment(commentTextArea.getText());
					MessageManager.getInstance().requestRepaint(
							new RepaintEvent(this));
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(commentTextArea);
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		commentPanel.add(scrollPane);

		JPanel texterPanel = new JPanel();
		textPanel.add(texterPanel);
		texterPanel.setLayout(new BoxLayout(texterPanel, BoxLayout.Y_AXIS));

		JPanel textlabelPanel = new JPanel();
		FlowLayout fl_textlabelPanel = (FlowLayout) textlabelPanel.getLayout();
		fl_textlabelPanel.setAlignment(FlowLayout.LEFT);
		texterPanel.add(textlabelPanel);

		JLabel textLabel = new JLabel("Text");
		textlabelPanel.add(textLabel);

		textTextArea = new JTextArea(5, 31);
		textTextArea.setToolTipText("Add text for label");
		textTextArea.setLineWrap(true);
		textTextArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (label != null) {
					label.setText(textTextArea.getText());
					MessageManager.getInstance().requestRepaint(
							new RepaintEvent(this));
				}
			}

		});
		// panel_3.add(textArea);
		JScrollPane scrollPane_1 = new JScrollPane(textTextArea);
		texterPanel.add(scrollPane_1);

		JPanel classEntityPanel = new JPanel();
		classEntityPanel.setBounds(5, 256, 356, 35);
		contentPane.add(classEntityPanel);
		classEntityPanel.setLayout(null);

		classLabel = new JLabel("Class:");
		classLabel.setBounds(5, 9, 43, 15);
		classEntityPanel.add(classLabel);

		ClassTextField = new JTextField();
		ClassTextField.setBounds(53, 7, 136, 19);
		ClassTextField.setEditable(false);
		classEntityPanel.add(ClassTextField);
		ClassTextField.setColumns(12);

		classEntityLabel = new JLabel("Entity:");
		classEntityLabel.setBounds(194, 9, 46, 15);
		classEntityPanel.add(classEntityLabel);

		classEntityComboBox = new JComboBox();
		classEntityComboBox.setBounds(245, 5, 100, 24);

		classEntityComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if (classEntityComboBox.getSelectedIndex() > -1) {
					if (label != null) {
						label.setLabelClassEntity((LabelClassEntity) classEntityComboBox
								.getSelectedItem());
						classEntityColorButton.setBackground(label.getColor());
						MessageManager.getInstance().requestRepaint(
								new RepaintEvent(this));
					}
				}
			}
		});

		classEntityPanel.add(classEntityComboBox);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) buttonPanel.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		buttonPanel.setBounds(5, 323, 356, 96);
		contentPane.add(buttonPanel);

		JLabel stateLabel = new JLabel("State:");
		buttonPanel.add(stateLabel);

		labeltypeTextField = new JTextField();
		labeltypeTextField.setFont(new Font("Dialog", Font.BOLD, 12));
		labeltypeTextField.setText("Manual Labled");
		labeltypeTextField.setEditable(false);
		buttonPanel.add(labeltypeTextField);
		labeltypeTextField.setColumns(12);

		JPanel radioPanel = new JPanel();
		buttonPanel.add(radioPanel);
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));

		acceptRadioButton = new JRadioButton("accept");
		acceptRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (label != null) {
					label.setLabelType(LabelType.AUTO_ACCEPTED);
					labeltypeTextField.setText("Accepted autolabel");
					labeltypeTextField.setForeground(acceptC);
					MessageManager.getInstance().requestRepaint(
							new RepaintEvent(this));
				}
			}
		});
		radioPanel.add(acceptRadioButton);

		rejectRadioButton = new JRadioButton("reject");
		rejectRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (label != null) {
					label.setLabelType(LabelType.AUTO_REJECTED);
					labeltypeTextField.setText("Rejected autolabel");
					labeltypeTextField.setForeground(rejectC);
					MessageManager.getInstance().requestRepaint(
							new RepaintEvent(this));
				}
			}
		});
		radioPanel.add(rejectRadioButton);
		bgroup = new NoneSelectedButtonGroup();// ButtonGroup();
		bgroup.add(acceptRadioButton);
		bgroup.add(rejectRadioButton);

        JLabel lblLabelCreatedAt = new JLabel("Label created at: ");
		buttonPanel.add(lblLabelCreatedAt);

		dateSpinner = new JSpinner();
		dateSpinner.setModel(new SpinnerDateModel(new Date(0), null, null,
				Calendar.DAY_OF_YEAR));
		JSpinner.DateEditor de_timestampSpinner = new JSpinner.DateEditor(
				dateSpinner, "dd.MM.yyyy HH:mm:ss:SSS");
		dateSpinner.setEditor(de_timestampSpinner);
		dateSpinner.setEnabled(false);
		buttonPanel.add(dateSpinner);

        showAsFlagCheckBox = new JCheckBox("ShowAsFlag");
        JPanel showAsFlagPanel = new JPanel();
        showAsFlagPanel.setBounds(5,410,356,35);
        showAsFlagPanel.add(showAsFlagCheckBox);
        contentPane.add(showAsFlagPanel);
        showAsFlagCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
                boolean selected = abstractButton.getModel().isSelected();
                label.setShowAsFlag(selected);
                endtimeSpinner.setValue(label.getEnd());
                MessageManager.getInstance().requestRepaint(
                        new RepaintEvent(this));
            }
        });



		MessageManager.getInstance().addLabelSelectionListener(
				new LabelSelectionListener() {

					public void selectionChanged(LabelSelectionEvent e) {
						prev = e.getPrevObject();
						label = e.getLabelObject();
						post = e.getPostObject();
						showData(e.getLabelObject());
					}
				});
		MessageManager.getInstance().addClassChangedListener(
				new ClassChangedListener() {

					public void classChanged(ClassChangedEvent e) {
						showData(LabelProperties.this.label);
					}

				});
		MessageManager.getInstance().addTimeTypeChangedListener(
				new TimeTypeChangedListener() {

					@Override
					public void timeTypeChanged(TimeTypeEvent e) {
						timeType = e.getTimeType();
						changeTimeType();
					}
				});
		MessageManager.getInstance().addShowLabelprobsWindowChangedListener(new ShowLabelprobsWindowListener(){

			@Override
			public void showLabelprobsWindow(ShowLabelprobsWindowEvent e) {
				setVisible(true);
				toFront();
			}
			
		});
		
		changeTimeType();
		uiv_ = new UserInputVerifier();
		uiv_.setTextFieldRestriction_NonEmpty(valueTextField, null, true, false);
		uiv_.setTextFieldRestriction_Double(valueTextField, null, true, false,
				false);

		AtlasProperties.getInstance().addJFrameBoundsWatcher("labProp",
				this, true, false);
	}

	private void changeTimeType() {
		switch (this.timeType) {
		case MILLIS:
			starttimeSpinner.setModel(new SpinnerNumberModel(new Long(0),
					new Long(0), null, new Long(1)));
			JSpinner.NumberEditor ne_starttimeSpinner = new JSpinner.NumberEditor(
					starttimeSpinner, "#");
			starttimeSpinner.setEditor(ne_starttimeSpinner);

			endtimeSpinner.setModel(new SpinnerNumberModel(new Long(0),
					new Long(0), null, new Long(1)));
			JSpinner.NumberEditor ne_endtimeSpinner = new JSpinner.NumberEditor(
					endtimeSpinner, "#");
			endtimeSpinner.setEditor(ne_endtimeSpinner);

			showData(label);
			break;
		case HHmmssSS:
			starttimeSpinner.setModel(new SpinnerDateModel(date, new Date(-cal
					.getTimeZone().getOffset(0)), new Date(-cal.getTimeZone()
					.getOffset(0) + (3600000 * 23)), Calendar.MILLISECOND));
			JSpinner.DateEditor de_starttimeSpinner = new JSpinner.DateEditor(
					starttimeSpinner, "HH:mm:ss:SSS");
			starttimeSpinner.setEditor(de_starttimeSpinner);

			endtimeSpinner.setModel(new SpinnerDateModel(date, new Date(-cal
					.getTimeZone().getOffset(0)), new Date(-cal.getTimeZone()
					.getOffset(0) + (3600000 * 23)), Calendar.MILLISECOND));
			JSpinner.DateEditor de_endtimeSpinner = new JSpinner.DateEditor(
					endtimeSpinner, "HH:mm:ss:SSS");
			endtimeSpinner.setEditor(de_endtimeSpinner);

			showData(label);
			break;
		case HHmmssff:
			starttimeSpinner.setModel(new SpinnerDateModel(date, new Date(-cal
					.getTimeZone().getOffset(0)), new Date(-cal.getTimeZone()
					.getOffset(0) + (3600000 * 23)), Calendar.MILLISECOND));
			JSpinner.DateEditor de2_starttimeSpinner = new JSpinner.DateEditor(
					starttimeSpinner, "HH:mm:ss:SS");
			starttimeSpinner.setEditor(de2_starttimeSpinner);

			endtimeSpinner.setModel(new SpinnerDateModel(date, new Date(-cal
					.getTimeZone().getOffset(0)), new Date(-cal.getTimeZone()
					.getOffset(0) + (3600000 * 23)), Calendar.MILLISECOND));
			JSpinner.DateEditor de2_endtimeSpinner = new JSpinner.DateEditor(
					endtimeSpinner, "HH:mm:ss:SS");
			endtimeSpinner.setEditor(de2_endtimeSpinner);

			showData(label);
			break;
		case FRAMES:
			starttimeSpinner.setModel(new SpinnerNumberModel(new Long(0),
					new Long(0), null, new Long(1)));
			JSpinner.NumberEditor fe_starttimeSpinner = new JSpinner.NumberEditor(
					starttimeSpinner, "#");
			starttimeSpinner.setEditor(fe_starttimeSpinner);

			endtimeSpinner.setModel(new SpinnerNumberModel(new Long(0),
					new Long(0), null, new Long(1)));
			JSpinner.NumberEditor fe_endtimeSpinner = new JSpinner.NumberEditor(
					endtimeSpinner, "#");
			endtimeSpinner.setEditor(fe_endtimeSpinner);

			showData(label);
			break;
		}
	}

	private void adaptEndTime() {
		/*
		 * if(label!=null){ long newEnd =
		 * (cal.getTimeZone().getOffset(0)+((Date)
		 * endtimeSpinner.getValue()).getTime());
		 * 
		 * 
		 * if(newEnd>=label.getStart()+(1000.0/Project.getInstance().getProjectFPS
		 * ()) && newEnd > label.getStart()){ if(post!=null){
		 * if(newEnd<post.getStart()){ label.setEnd((long)(newEnd)); }else{
		 * label.setEnd(post.getStart()); } }else{ if(newEnd<
		 * Project.getInstance().getProjectLength()){ label.setEnd(newEnd);
		 * }else{ label.setEnd((long)Project.getInstance().getProjectLength());
		 * } } }
		 * 
		 * this.endtimeSpinner.setValue(new
		 * Date(-cal.getTimeZone().getOffset(0)+label.getEnd()));
		 * MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
		 * }
		 */

		if (label != null) {
			long newEnd = 0;
			switch (this.timeType) {
			case MILLIS:
				newEnd = (Long) (endtimeSpinner.getValue());

				if (newEnd > label.getStart()){
					if (post != null) {
						if (newEnd < post.getStart()) {
							label.setEnd((long) (newEnd));
						} else {
							label.setEnd(post.getStart());
						}
					} else {
						if (newEnd < Project.getInstance().getProjectLength()) {
							label.setEnd(newEnd);
						} else {
							label.setEnd((long) Project.getInstance()
									.getProjectLength());
						}
					}
				}
				this.endtimeSpinner.setValue(label.getEnd());
				break;
			case HHmmssSS:
				newEnd = (cal.getTimeZone().getOffset(0) + ((Date) endtimeSpinner
						.getValue()).getTime());

				if (newEnd > label.getStart()) {
					if (post != null) {
						if (newEnd < post.getStart()) {
							label.setEnd((long) (newEnd));
						} else {
							label.setEnd(post.getStart());
						}
					} else {
						if (newEnd < Project.getInstance().getProjectLength()) {
							label.setEnd(newEnd);
						} else {
							label.setEnd((long) Project.getInstance()
									.getProjectLength());
						}
					}
				}
				this.endtimeSpinner.setValue(new Date(-cal.getTimeZone()
						.getOffset(0) + label.getEnd()));
				break;
			case HHmmssff:
				newEnd = (cal.getTimeZone().getOffset(0) + ((Date) endtimeSpinner
						.getValue()).getTime());

				long val = (long) (newEnd);// -(1000.0/Project.getInstance().getProjectFPS()));
				long hour = ((val) % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
				long min = ((val) % (60 * 60 * 1000)) / (60 * 1000);
				long sec = ((val) % (60 * 1000)) / (1000);
				long ff = 0;
				if (((val) % (1000)) == 999) {
					ff = (long) (1000 - (1000.0 / Project.getInstance()
							.getProjectFPS()));
				} else {
					ff = ((long) (((val) % (1000)) * (1000.0 / Project
							.getInstance().getProjectFPS())));
				}

				newEnd = (hour * 3600 * 1000) + (min * 60 * 1000)
						+ (sec * 1000) + ff;

				if (newEnd >= label.getStart()
						+ (1000.0 / Project.getInstance().getProjectFPS())
						&& newEnd > label.getStart()) {
					if (post != null) {
						if (newEnd < post.getStart()) {
							label.setEnd((long) (newEnd));
						} else {
							label.setEnd(post.getStart());
						}
					} else {
						if (newEnd < Project.getInstance().getProjectLength()) {
							label.setEnd(newEnd);
						} else {
							label.setEnd((long) Project.getInstance()
									.getProjectLength());
						}
					}
				}
				val = (long) (label.getEnd());
				hour = ((val) % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
				min = ((val) % (60 * 60 * 1000)) / (60 * 1000);
				sec = ((val) % (60 * 1000)) / (1000);
				ff = ((long) (((val) % (1000)) / (1000.0 / Project
						.getInstance().getProjectFPS())));
				long time = (hour * 3600 * 1000) + (min * 60 * 1000)
						+ (sec * 1000) + ff;

				this.endtimeSpinner.setValue(new Date(-cal.getTimeZone()
						.getOffset(0) + time));
				break;
			case FRAMES:
				long tmp1 = ((Long) endtimeSpinner.getValue()).longValue() - 1;
				double tmp2 = 1000.0 / Project.getInstance().getProjectFPS();
				newEnd = (long) (tmp1 * tmp2);

				if (newEnd >= label.getStart()
						+ (1000.0 / Project.getInstance().getProjectFPS())
						&& newEnd > label.getStart()) {
					if (post != null) {
						if (newEnd < post.getStart()) {
							label.setEnd((long) (newEnd));
						} else {
							label.setEnd(post.getStart());
						}
					} else {
						if (newEnd < Project.getInstance().getProjectLength()) {
							label.setEnd(newEnd);
						} else {
							label.setEnd((long) Project.getInstance()
									.getProjectLength());
						}
					}
				}
				this.endtimeSpinner
						.setValue((long) (((Long) label.getEnd()) / (1000.0 / Project
								.getInstance().getProjectFPS())) + 1);
				break;
			}
			MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
		}
	}

	private void adaptStartTime() {

		if (label != null) {
			long newStart = 0;
			switch (this.timeType) {
			case MILLIS:
				newStart = (Long) (starttimeSpinner.getValue());

				if (newStart <= label.getEnd()
						- (1000.0 / Project.getInstance().getProjectFPS())) {
					if (prev != null) {
						if (newStart > prev.getEnd()) {
							label.setStart((int) (newStart));
						} else {
							label.setStart(prev.getEnd());
						}
					} else {
						if ((newStart) > 0) {
							label.setStart((int) (newStart));
						} else {
							label.setStart(0);
						}
					}
				}
				this.starttimeSpinner.setValue(label.getStart());
				break;
			case HHmmssSS:
				newStart = (cal.getTimeZone().getOffset(0) + ((Date) starttimeSpinner
						.getValue()).getTime());
				if (newStart <= label.getEnd()
						- (1000.0 / Project.getInstance().getProjectFPS())) {

					if (prev != null) {
						if (newStart > prev.getEnd()) {
							label.setStart((int) (newStart));
						} else {
							label.setStart(prev.getEnd());
						}
					} else {
						if ((newStart) > 0) {
							label.setStart((int) (newStart));
						} else {
							label.setStart(0);
						}
					}
				}
				this.starttimeSpinner.setValue(new Date(-cal.getTimeZone()
						.getOffset(0) + label.getStart()));
				break;
			case HHmmssff:
				newStart = (cal.getTimeZone().getOffset(0) + ((Date) starttimeSpinner
						.getValue()).getTime());

				long val = (long) (newStart);// -(1000.0/Project.getInstance().getProjectFPS()));
				long hour = ((val) % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
				long min = ((val) % (60 * 60 * 1000)) / (60 * 1000);
				long sec = ((val) % (60 * 1000)) / (1000);
				long ff = 0;
				if (((val) % (1000)) == 999) {
					ff = (long) (1000 - (1000.0 / Project.getInstance()
							.getProjectFPS()));
				} else {
					ff = ((long) (((val) % (1000)) * (1000.0 / Project
							.getInstance().getProjectFPS())));
				}

				newStart = (hour * 3600 * 1000) + (min * 60 * 1000)
						+ (sec * 1000) + ff;

				if (newStart <= label.getEnd()
						- (1000.0 / Project.getInstance().getProjectFPS())) {
					if (prev != null) {
						if (newStart > prev.getEnd()) {
							label.setStart((int) (newStart));
						} else {
							label.setStart(prev.getEnd());
						}
					} else {
						if ((newStart) > 0) {
							label.setStart((int) (newStart));
						} else {
							label.setStart(0);
						}
					}
				}
				val = (long) (label.getStart());
				hour = ((val) % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
				min = ((val) % (60 * 60 * 1000)) / (60 * 1000);
				sec = ((val) % (60 * 1000)) / (1000);
				ff = ((long) (((val) % (1000)) / (1000.0 / Project
						.getInstance().getProjectFPS())));
				long time = (hour * 3600 * 1000) + (min * 60 * 1000)
						+ (sec * 1000) + ff;

				this.starttimeSpinner.setValue(new Date(-cal.getTimeZone()
						.getOffset(0) + time));
				break;
			case FRAMES:
				long tmp1 = ((Long) starttimeSpinner.getValue()).longValue() - 1;
				double tmp2 = 1000.0 / Project.getInstance().getProjectFPS();
				newStart = (long) (tmp1 * tmp2);

				if (newStart <= label.getEnd()
						- (1000.0 / Project.getInstance().getProjectFPS())) {
					if (prev != null) {
						if (newStart > prev.getEnd()) {
							label.setStart((int) (newStart));
						} else {
							label.setStart(prev.getEnd());
						}
					} else {
						if ((newStart) > 0) {
							label.setStart((int) (newStart));
						} else {
							label.setStart(0);
						}
					}
				}
				this.starttimeSpinner
						.setValue((long) (((Long) label.getStart()) / (1000.0 / Project
								.getInstance().getProjectFPS())) + 1);
				break;
			}
			MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
		}
	}

	private void showData(LabelObject obj) {

		this.classLabel.setEnabled(false);
		this.ClassTextField.setEnabled(false);
		this.classEntityLabel.setEnabled(false);
		this.classEntityComboBox.setEnabled(false);
		this.classEntityColorButton.setEnabled(false);
		this.ClassTextField.setText("");
		this.commentTextArea.setText("");
		this.textTextArea.setText("");
		this.classEntityComboBox.setSelectedIndex(-1);
		this.valueTextField.setText("0.0");
		this.dateSpinner.setValue(new Date(0));
        this.showAsFlagCheckBox.setSelected(false);

		if (obj != null) {

			this.label = obj;
			this.textTextArea.setText(obj.getText());
			this.commentTextArea.setText(obj.getComment());
            this.showAsFlagCheckBox.setSelected(obj.isShowAsFlag());

			switch (this.timeType) {
			case MILLIS:
				internalChangeStart = true;
				this.starttimeSpinner.setValue(obj.getStart());
				internalChangeEnd = true;
				this.endtimeSpinner.setValue(obj.getEnd());
				break;
			case HHmmssSS:
				internalChangeStart = true;
				this.starttimeSpinner.setValue(new Date(-cal.getTimeZone()
						.getOffset(0) + obj.getStart()));
				internalChangeEnd = true;
				this.endtimeSpinner.setValue(new Date(-cal.getTimeZone()
						.getOffset(0) + obj.getEnd()));
				break;
			case HHmmssff:
				internalChangeStart = true;
				long val = (long) (obj.getStart() + (1000.0 / Project
						.getInstance().getProjectFPS()));
				long hour = ((val) % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
				long min = ((val) % (60 * 60 * 1000)) / (60 * 1000);
				long sec = ((val) % (60 * 1000)) / (1000);
				long ff = ((long) (((val) % (1000)) / (1000.0 / Project
						.getInstance().getProjectFPS())));
				long time = (hour * 3600 * 1000) + (min * 60 * 1000)
						+ (sec * 1000) + ff;
				this.starttimeSpinner.setValue(new Date(-cal.getTimeZone()
						.getOffset(0) + time));

				internalChangeEnd = true;
				val = (long) (obj.getEnd() + (1000.0 / Project.getInstance()
						.getProjectFPS()));
				hour = ((val) % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
				min = ((val) % (60 * 60 * 1000)) / (60 * 1000);
				sec = ((val) % (60 * 1000)) / (1000);
				ff = ((long) (((val) % (1000)) / (1000.0 / Project
						.getInstance().getProjectFPS())));
				time = (hour * 3600 * 1000) + (min * 60 * 1000) + (sec * 1000)
						+ ff;
				this.endtimeSpinner.setValue(new Date(-cal.getTimeZone()
						.getOffset(0) + time));
				break;
			case FRAMES:
				internalChangeStart = true;
				this.starttimeSpinner
						.setValue((long) (((Long) obj.getStart()) / (1000.0 / Project
								.getInstance().getProjectFPS())) + 1);
				internalChangeEnd = true;
				this.endtimeSpinner
						.setValue((long) (((Long) obj.getEnd()) / (1000.0 / Project
								.getInstance().getProjectFPS())) + 1);
				break;
			}

			this.valueTextField.setText(Double.toString(label.getValue()));
			tmpvalue = Double.toString(label.getValue());
			this.dateSpinner.setValue(new Date(obj.getTimestamp()));
			switch (obj.getLabelType()) {
			case MANUAL:
				this.labeltypeTextField.setText("Manual labled");
				this.labeltypeTextField.setForeground(this.manualC);
				this.acceptRadioButton.setEnabled(false);
				this.rejectRadioButton.setEnabled(false);
				this.acceptRadioButton.setSelected(false);
				this.rejectRadioButton.setSelected(false);

				break;
			case AUTOMATIC:
				this.labeltypeTextField.setText("Automatic labled");
				this.labeltypeTextField.setForeground(this.autoC);
				this.acceptRadioButton.setEnabled(true);
				this.rejectRadioButton.setEnabled(true);
				this.acceptRadioButton.setSelected(false);
				this.rejectRadioButton.setSelected(false);
				// this.bgroup.setSelected(bgroup.getSelection(), false);
				break;
			case AUTO_ACCEPTED:
				this.labeltypeTextField.setText("Accepted autolabel");
				this.labeltypeTextField.setForeground(this.acceptC);
				this.acceptRadioButton.setEnabled(true);
				this.rejectRadioButton.setEnabled(true);
				this.rejectRadioButton.setSelected(false);
				this.acceptRadioButton.setSelected(true);
				break;
			case AUTO_REJECTED:
				this.labeltypeTextField.setText("Rejected autolabel");
				this.labeltypeTextField.setForeground(this.rejectC);
				this.acceptRadioButton.setEnabled(true);
				this.rejectRadioButton.setEnabled(true);
				this.acceptRadioButton.setSelected(false);
				this.rejectRadioButton.setSelected(true);
				break;
			}
			if (label.getLabelClass() != null) {
				this.classLabel.setEnabled(true);
				this.ClassTextField.setEnabled(true);
				this.classEntityLabel.setEnabled(true);
				this.classEntityComboBox.setEnabled(true);
				this.classEntityColorButton.setEnabled(true);
				this.classEntityComboBox.setModel(label.getLabelClass());
				this.ClassTextField.setText(label.getLabelClass().toString());
				if (label.getLabelClassEntity() != null) {
					this.classEntityComboBox.setSelectedItem(label.getLabelClassEntity());
					this.classEntityColorButton.setBackground(label.getColor());
				}
			}
		}

	}
}

class NoneSelectedButtonGroup extends ButtonGroup {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setSelected(ButtonModel model, boolean selected) {
		if (selected) {
			super.setSelected(model, selected);
		} else {
			clearSelection();
		}
	}
}

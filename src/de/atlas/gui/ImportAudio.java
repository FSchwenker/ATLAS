package de.atlas.gui;

import java.awt.BorderLayout;
import java.awt.Component;

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

import de.atlas.collections.LineCollection;
import de.atlas.collections.MediaCollection;
import de.atlas.collections.ObjectLine;
import de.atlas.data.Project;
import de.atlas.data.WavFile;
import de.atlas.exceptions.WavFileException;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.misc.HelperFunctions;
import eu.c_bauer.userinputverifier.UserInputVerifier;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import javax.swing.JCheckBox;

public class ImportAudio extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel_ = new JPanel();
	private JButton fileButton_;
	private JTextField nameTextField_;
	private JTextField fileTextField_;
	private MediaCollection mcoll_;
	private LineCollection lcoll_;
	private String path_;
	private JButton okButton_;
	private JButton cancelButton_;
	private JTextField tfSampleRate_;
	private JLabel lblTimesPerMillisecond_;
	private int selectedWavSampleRate_ = -1;
	private int maxSamplesPerSecond_ = -1;
	private JCheckBox chckbxCreateScalartrackFrom_;
	private JLabel lblsamplesPerMillilabel_;
	private JCheckBox checkboxUseOnlyFirstChannel_;

	/**
	 * Create the dialog.
	 */
	public ImportAudio() {
		setTitle("Import Audio");
		this.setIconImage(new ImageIcon(Project.class.getResource("/icon.gif"))
				.getImage());
		setBounds(100, 100, 530, 280);
		getContentPane().setLayout(new BorderLayout());
		contentPanel_.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel_, BorderLayout.CENTER);
		contentPanel_.setLayout(null);

		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(19, 12, 45, 15);
		contentPanel_.add(lblName);
		{
			cancelButton_ = new JButton("Cancel");
			cancelButton_.setBounds(383, 192, 100, 25);
			contentPanel_.add(cancelButton_);
			cancelButton_.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
				}
			});
			cancelButton_.setActionCommand("Cancel");
		}

		nameTextField_ = new JTextField();
		nameTextField_.setBounds(82, 10, 340, 19);
		contentPanel_.add(nameTextField_);
		nameTextField_.setColumns(18);

		JLabel lblFile = new JLabel("File:");
		lblFile.setBounds(19, 39, 30, 15);
		contentPanel_.add(lblFile);

		fileTextField_ = new JTextField();
		fileTextField_.setBounds(82, 37, 340, 19);
		contentPanel_.add(fileTextField_);
		fileTextField_.setColumns(28);

		fileButton_ = new JButton("...");
		fileButton_.setBounds(434, 34, 49, 25);
		contentPanel_.add(fileButton_);
		{
			okButton_ = new JButton("OK");
			okButton_.setBounds(271, 192, 100, 25);
			contentPanel_.add(okButton_);
			okButton_.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					addTracks();
				}
			});
			okButton_.setActionCommand("OK");
			getRootPane().setDefaultButton(okButton_);
		}

		fileButton_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fileChooser();
			}
		});

		chckbxCreateScalartrackFrom_ = new JCheckBox(
				"Create ScalarTrack(s) from import");
		chckbxCreateScalartrackFrom_.setSelected(true);
		chckbxCreateScalartrackFrom_.setBounds(19, 74, 376, 23);
		contentPanel_.add(chckbxCreateScalartrackFrom_);

		tfSampleRate_ = new JTextField();
		tfSampleRate_.setText("1000");
		tfSampleRate_.setBounds(68, 134, 68, 19);
		contentPanel_.add(tfSampleRate_);
		tfSampleRate_.setColumns(10);

		lblsamplesPerMillilabel_ = new JLabel("Sample min and max values");
		lblsamplesPerMillilabel_.setBounds(58, 107, 278, 15);
		contentPanel_.add(lblsamplesPerMillilabel_);

		lblTimesPerMillisecond_ = new JLabel("times per second.");
		lblTimesPerMillisecond_.setBounds(154, 136, 329, 15);
		contentPanel_.add(lblTimesPerMillisecond_);
		checkboxUseOnlyFirstChannel_ = new JCheckBox(
				"use only first wav channel");

		// add UserInputVerifier and propertylistener for
		// selection of datatrack import for wav files
		UserInputVerifier uiv = new UserInputVerifier();
		PropertyChangeListener uivChangeListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {

				// set max samplerate - -1 if not wav or exception
				if (fileTextField_.getText().toLowerCase().endsWith(".wav")) {
					try {
						selectedWavSampleRate_ = (int) (WavFile
								.openWavFile(new File(fileTextField_.getText())))
								.getSampleRate();
						maxSamplesPerSecond_ = selectedWavSampleRate_ / 2;
					} catch (IOException e1) {
						selectedWavSampleRate_ = -1;
						maxSamplesPerSecond_ = -1;
						e1.printStackTrace();
					} catch (WavFileException e2) {
						selectedWavSampleRate_ = -1;
						maxSamplesPerSecond_ = -1;
						e2.printStackTrace();
					}
				} else {
					selectedWavSampleRate_ = -1;
					maxSamplesPerSecond_ = -1;
				}

				if (-1 == selectedWavSampleRate_) {
					chckbxCreateScalartrackFrom_.setEnabled(false);
					lblsamplesPerMillilabel_.setEnabled(false);
					lblTimesPerMillisecond_.setEnabled(false);
					lblTimesPerMillisecond_.setText("times per second.");
					tfSampleRate_.setEnabled(false);
					checkboxUseOnlyFirstChannel_.setEnabled(false);
				} else {
					chckbxCreateScalartrackFrom_.setEnabled(true);
					lblsamplesPerMillilabel_.setEnabled(true);
					lblTimesPerMillisecond_
							.setText("times per second (max allowed: "
									+ maxSamplesPerSecond_ + ").");
					lblTimesPerMillisecond_.setEnabled(true);
					tfSampleRate_.setEnabled(true);
					checkboxUseOnlyFirstChannel_.setEnabled(true);
				}
			}
		};

		uiv.addPropertyChangeListener(uivChangeListener);
		// set restrictions
		uiv.setTextFieldRestriction_ExistingNormalFile(fileTextField_,
				"tfFile", true, false, false);
		uiv.setTextFieldRestriction_NonEmpty(fileTextField_, "tfFile", false,
				false);

		uiv.setTextFieldRestriction_WindowsFilename(nameTextField_, "tfName",
				false, true, false, false);
		uiv.setTextFieldRestriction_NonEmpty(nameTextField_, "tfName", false,
				false);

		uiv.setTextFieldRestriction_NonEmpty(tfSampleRate_, "tfSampleRate",
				false, false);
		uiv.setTextFieldRestriction_NonZeroNumericValue(tfSampleRate_,
				"tfSampleRate", true, false, false);
		uiv.setTextFieldRestriction_PositiveNumericValue(tfSampleRate_,
				"tfSampleRate", true, false, false);
		uiv.setTextFieldRestriction_Int(tfSampleRate_, "tfSampleRate", true,
				false, false);

		uiv.addDependentButton(okButton_);

		checkboxUseOnlyFirstChannel_.setBounds(19, 161, 210, 23);
		contentPanel_.add(checkboxUseOnlyFirstChannel_);
	}

	public static void showDialog(Component owner, String title,
			MediaCollection mcoll, LineCollection lcoll, String path) {

		ImportAudio dialog = new ImportAudio();

		// drag & drop directory enters directory, which is not wanted
		if (!(new File(path).isDirectory())) {
			dialog.fileTextField_.setText(path);
		}
		dialog.mcoll_ = mcoll;
		dialog.lcoll_ = lcoll;
		dialog.path_ = path;
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.setTitle(title);
		dialog.setLocationRelativeTo(owner);
		dialog.setModal(true);
		dialog.setVisible(true);

	}

	private void fileChooser() {

		JFileChooser fc = new JFileChooser(this.path_);
		FileFilter ff = null;
		ff = new FileNameExtensionFilter("Media", "wav", "mp3");

		if (ff != null) {
			fc.setFileFilter(ff);
		}

		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			this.fileTextField_.setText(fc.getSelectedFile().getPath());
		}
	}

	private void addTracks() {
		// don't allow relevant gui element changes
		tfSampleRate_.setEditable(false);
		fileTextField_.setEditable(false);
		nameTextField_.setEditable(false);
		fileButton_.setEnabled(false);
		okButton_.setEnabled(false);
		cancelButton_.setEnabled(false);

		int numChannels = -1;
		// if we have a wav file, variable is set
		if (-1 < maxSamplesPerSecond_
				&& chckbxCreateScalartrackFrom_.isSelected()) {
			try {
				numChannels = (WavFile.openWavFile(new File(fileTextField_
						.getText()))).getNumChannels();

				// restrict to one channel from gui
				if (this.checkboxUseOnlyFirstChannel_.isSelected()) {
					numChannels = 1;
				}

				String trackNameAppend = "";
				int channel = 0;
				for (channel = 0; channel < numChannels; channel++) {
					// append to name with multiple channels
					if (1 < numChannels) {
						trackNameAppend = "_ch_" + channel;
					}
					addTrack(channel, trackNameAppend);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (WavFileException e) {
				e.printStackTrace();
			}
		}
		mcoll_.addAudioTrack(nameTextField_.getText(),fileTextField_.getText(), true, false);
		mcoll_.setTime(Project.getInstance().getTime());
		MessageManager.getInstance().requestTrackUpdate(
				new UpdateTracksEvent(this));
		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
		this.setVisible(false);

		// restore gui settings
		tfSampleRate_.setEditable(true);
		fileTextField_.setEditable(true);
		nameTextField_.setEditable(true);
		fileButton_.setEnabled(true);
		okButton_.setEnabled(true);
		cancelButton_.setEnabled(true);

	}

	private void addTrack(int channel, String trackNameAppend) {

		if (nameTextField_.getText().length() > 0
				&& fileTextField_.getText().length() > 0) {
			if (fileTextField_.getText().toLowerCase().endsWith(".wav")) {

				// use min of entered and max available
				int targetSampleRatePerSecond = Math.min(
						Integer.valueOf(tfSampleRate_.getText()),
						maxSamplesPerSecond_);

				String rawFilePath = null;
				String trackName = null;
				try {
					rawFilePath = Project.getInstance().getProjectPath()
							+ "datatracks/" + nameTextField_.getText()
							+ trackNameAppend + ".raw";
					trackName = nameTextField_.getText() + trackNameAppend;
					File file;
					if ((file = HelperFunctions
							.testAndGenerateFile(rawFilePath)) == null) {
						return;
					}
					RandomAccessFile raf = new RandomAccessFile(file, "rw");
					FileChannel fChannel = raf.getChannel();
					// Open the wav file specified as the first argument
					WavFile wavFile = WavFile.openWavFile(new File(
							fileTextField_.getText()));
					long currentFramePos = 0;
					int nextSampleIntervalNum = 0; // 0 millis is the first
													// entry
					int sampleRate = (int) wavFile.getSampleRate();

					// Get the number of audio channels in the wav file
					int numChannels = wavFile.getNumChannels();

					// we use two buffers - if wavWorkingBuffer does not
					// contain
					// enough for evaluation for the next milli, remaining
					// data will be copied to start of buffer and new
					// data will be appended through use of wavReadBuffer
					// this is NOT nice, but will work

					// enough space for a complete wavReadBuffer (only one
					// channel) plus
					// additional space for one more than max frames per
					// millis (rounding)
					int bufLen = 20000;
					double[] wavReadBuffer = new double[numChannels * bufLen];
					double[] channelBuffer = new double[bufLen + sampleRate
							/ targetSampleRatePerSecond + 1];
					int currentBufferPos = 0;
					int framesInBuffer = 0;

					// read wav file
					int framesRead = 0;
					while (0 != (framesRead = wavFile.readFrames(wavReadBuffer,
							bufLen))) {
						framesInBuffer = addWavChannelDataToBuffer(
								channelBuffer, currentBufferPos, framesInBuffer
										- currentBufferPos, wavReadBuffer,
								channel, numChannels, framesRead);
						if (-1 == framesInBuffer) {
							System.err.println("error copying wav frames");
							break;
						}

						currentBufferPos = 0;

						// calculating it this way there is no div problem
						// this is the number of entries that will at least
						// be done
						int totalIntervalsAddable = (int) ((targetSampleRatePerSecond * (currentFramePos + framesInBuffer)) / sampleRate);

						if ((long) (((double) totalIntervalsAddable + 0.5) * (double) sampleRate)
								/ targetSampleRatePerSecond > (currentFramePos + (long) framesInBuffer)) {
							totalIntervalsAddable--;
						}

						// position in raf is nextmillitoadd * space for one
						// entry
						// first entry is for milli = 0
						long position = (long) ((nextSampleIntervalNum) * 4 * (Double.SIZE / 8));
						// addable - nextToAdd + 1, because we go up to <=
						long size = (long) ((totalIntervalsAddable
								- nextSampleIntervalNum + 1) * 4 * (Double.SIZE / 8));

						DoubleBuffer dB = null;
						try {
							dB = fChannel.map(FileChannel.MapMode.READ_WRITE,
									position, size).asDoubleBuffer();
						} catch (Exception e) {
							e.printStackTrace();
							break;
						}

						while (nextSampleIntervalNum <= totalIntervalsAddable) {
							double min = Double.MAX_VALUE;
							double max = Double.MIN_VALUE;
							double time_min_in_millis = 0;
							double time_max_in_millis = 0;
							long endFrameForInterval = (long) ((nextSampleIntervalNum + 0.5) * ((double) sampleRate / (double) targetSampleRatePerSecond));

							while (currentFramePos < endFrameForInterval) {
								if (channelBuffer[currentBufferPos] < min) {
									min = channelBuffer[currentBufferPos];
									time_min_in_millis = ((double) currentFramePos * 1000.0)
											/ (double) sampleRate;
								}
								if (channelBuffer[currentBufferPos] > max) {
									max = channelBuffer[currentBufferPos];
									time_max_in_millis = ((double) currentFramePos * 1000.0)
											/ (double) sampleRate;
								}
								currentBufferPos++;
								currentFramePos++;
							}
							try {
								if (time_min_in_millis < time_max_in_millis) {
									dB.put(time_min_in_millis);// time
									dB.put(min);// value
									dB.put(time_max_in_millis);// time
									dB.put(max);// value
								} else {
									dB.put(time_max_in_millis);// time
									dB.put(max);// value
									dB.put(time_min_in_millis);// time
									dB.put(min);// value
								}
							} catch (Exception e) {
								System.err
										.println("ImportAudio: putting min, max and time to dB failed.");
							}
							// add values
							nextSampleIntervalNum++;
						}
					} // end while (0!=(framesRead =

					// Close the wavFile

					wavFile.close();
				} catch (Exception e) {
					System.err.println(e);
				}
				lcoll_.addScalarTrack(trackName, rawFilePath, true, true, true,
						-1.0, 1.0, Project.getInstance().getLcoll()
								.getOlinesSize(), false, ObjectLine.MEDIUM, false);
			}
		}
	}

	/**
	 * Extracts channel data from a wav file. The data is provided in an array
	 * [numberOfFrames*numberOfChannels] and the data belonging to selected
	 * channel is added to the targetBuffer.
	 * 
	 * It is possible to have still unprocessed data in the targetBuffer (see
	 * targetBufferUnprocessedDataStartIndex and
	 * targetBufferUnprocessedDataRemaining) which will be moved to the
	 * beginning of the targetBuffer. The length of the targetBuffer's relevant
	 * data is targetBufferUnprocessedDataRemaining + numFramesWavBufferToAdd.
	 * The rest of the targetBuffer will not be touched, so the callee must
	 * track the new size of relevant data in targetBuffer.
	 * 
	 * @param targetBuffer
	 *            buffer the channel data is added to
	 * @param targetBufferUnprocessedDataStartIndex
	 *            still to use data in the targetBuffer left start index. If
	 *            targetBufferUnprocessedDataRemaining == 0, irrelevant
	 * @param targetBufferUnprocessedDataRemaining
	 *            still to use data in the targetBuffer left end index
	 * @param wavBufferToAdd
	 *            data read from wav
	 * @param channel
	 *            channel in wavBufferToAdd to use - first channel is 0, second
	 *            1...
	 * @param numOfChannels
	 *            number of channels in the wav
	 * @param numFramesWavBufferToAdd
	 *            number of frames in wavBufferToAdd (doesn't need to be fully
	 *            filled)
	 * @return -1 at error, else number of wav frames in buffer, first is in
	 *         position 0, therefore this number is equivalent to array.length()
	 */
	private int addWavChannelDataToBuffer(double[] targetBuffer,
			int targetBufferUnprocessedDataStartIndex,
			int targetBufferUnprocessedDataRemaining, double[] wavBufferToAdd,
			int channel, int numOfChannels, int numFramesWavBufferToAdd) {

		// security check
		if (targetBuffer.length < Math.max(0,
				targetBufferUnprocessedDataRemaining) + numFramesWavBufferToAdd) {
			System.err.println("targetBuffer.length: " + targetBuffer.length);
			System.err.println("targetBufferUnprocessedDataRemaining: "
					+ targetBufferUnprocessedDataRemaining);
			System.err.println("numFramesWavBufferToAdd: "
					+ numFramesWavBufferToAdd);
			System.err.println("targetBuffer too small");

			return -1;
		} else if (0 > targetBufferUnprocessedDataStartIndex
				|| 0 > targetBufferUnprocessedDataRemaining
				|| 1 > numOfChannels) {
			System.err.println("targetBufferUnprocessedDataStartIndex"
					+ "or targetBufferUnprocessedDataRemaining negative or"
					+ "1 > numOfChannels");
			return -1;
		}

		// copy remaining data to start of targetBuffer if there is
		if (0 < targetBufferUnprocessedDataRemaining) {
			for (int i = 0; i < targetBufferUnprocessedDataRemaining; i++) {
				targetBuffer[i] = targetBuffer[i
						+ targetBufferUnprocessedDataStartIndex];
			}
		}

		// add data from wavBufferToAdd - channel as channel data-offset
		int i;
		for (i = 0; i < numFramesWavBufferToAdd; i++) {
			targetBuffer[i + targetBufferUnprocessedDataRemaining] = wavBufferToAdd[channel
					+ i * numOfChannels];
		}

		return (i + targetBufferUnprocessedDataRemaining);
	}
}

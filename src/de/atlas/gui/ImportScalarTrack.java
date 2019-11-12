package de.atlas.gui;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import de.atlas.collections.LineCollection;
import de.atlas.collections.ObjectLine;
import de.atlas.data.Project;
import de.atlas.data.WavFile;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.misc.HelperFunctions;
import eu.c_bauer.userinputverifier.UserInputVerifier;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ImportScalarTrack extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel_ = new JPanel();
	private JButton fileButton_;
	private JTextField nameTextField_;
	private JTextField fileTextField_;
	private LineCollection lcoll_;
	private String path_;
	private JTextField minTextField_;
	private JTextField maxTextField_;
	private JButton okButton_;
	private JButton cancelButton_;
	private UserInputVerifier uiv_ = null;
	private String csvSplitRegex_ = "[,;\t]";

	/**
	 * Create the dialog.
	 */
	public ImportScalarTrack() {
		setTitle("Import ScalarTrack");
		this.setIconImage(new ImageIcon(Project.class.getResource("/icon.gif"))
				.getImage());
		setBounds(100, 100, 450, 240);
		getContentPane().setLayout(new BorderLayout());
		contentPanel_.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel_, BorderLayout.CENTER);
		contentPanel_.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(12, 7, 424, 34);
		contentPanel_.add(panel);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JLabel lblName = new JLabel("Name:");
		panel.add(lblName);

		nameTextField_ = new JTextField();
		panel.add(nameTextField_);
		nameTextField_.setColumns(18);

		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_2.setBounds(12, 53, 436, 34);
		contentPanel_.add(panel_2);

		JLabel lblClass = new JLabel("Min:");
		panel_2.add(lblClass);

		minTextField_ = new JTextField();
		minTextField_.setColumns(5);
		minTextField_.setText("0.0");
		// minTextField_.addKeyListener(new KeyAdapter() {
		//
		// @Override
		// public void keyReleased(KeyEvent e) {
		// try{
		// if(minTextField_.getText().length()>0){
		// Double.parseDouble(minTextField_.getText());
		// tmpvalueMin_=minTextField_.getText();
		// }else{
		// minTextField_.setText("0.0");
		// }
		// }catch(NumberFormatException ex){
		// minTextField_.setText(tmpvalueMin_);
		// }
		//
		//
		// }
		// });
		panel_2.add(minTextField_);

		JLabel lblMax = new JLabel("Max:");
		panel_2.add(lblMax);

		maxTextField_ = new JTextField();
		maxTextField_.setColumns(5);
		maxTextField_.setText("1.0");
		// maxTextField_.addKeyListener(new KeyAdapter() {
		//
		// @Override
		// public void keyReleased(KeyEvent e) {
		// try{
		// if(maxTextField_.getText().length()>0){
		// Double.parseDouble(maxTextField_.getText());
		// tmpvalueMax_=maxTextField_.getText();
		// }else{
		// maxTextField_.setText("0.0");
		// }
		// }catch(NumberFormatException ex){
		// maxTextField_.setText(tmpvalueMax_);
		// }
		//
		//
		// }
		// });
		panel_2.add(maxTextField_);

		JPanel filePanel = new JPanel();
		filePanel.setBounds(12, 104, 424, 34);
		contentPanel_.add(filePanel);
		filePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		JLabel lblFile = new JLabel("File:");
		filePanel.add(lblFile);

		fileTextField_ = new JTextField();
		filePanel.add(fileTextField_);
		fileTextField_.setColumns(28);

		fileButton_ = new JButton("...");
		fileButton_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fileChooser();
			}
		});
		filePanel.add(fileButton_);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton_ = new JButton("OK");
				okButton_.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {

						addTrack();
					}
				});
				okButton_.setActionCommand("OK");
				buttonPane.add(okButton_);
				getRootPane().setDefaultButton(okButton_);
			}
			{
				cancelButton_ = new JButton("Cancel");
				cancelButton_.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						setVisible(false);
					}
				});
				cancelButton_.setActionCommand("Cancel");
				buttonPane.add(cancelButton_);
			}
		}

		uiv_ = new UserInputVerifier();

		uiv_.setTextFieldRestriction_NonEmpty(fileTextField_, "tfFile", false,
				false);
		uiv_.setTextFieldRestriction_ExistingNormalFile(fileTextField_, null,
				true, false, false);

		uiv_.setTextFieldRestriction_NonEmpty(maxTextField_, "tfMax", true,
				false);
		uiv_.setTextFieldRestriction_Double(maxTextField_, null, true, false,
				false);

		uiv_.setTextFieldRestriction_NonEmpty(minTextField_, "tfMin", true,
				false);
		uiv_.setTextFieldRestriction_Double(minTextField_, null, true, false,
				false);

		uiv_.setTextFieldRestriction_NonEmpty(nameTextField_, "tfName", false,
				false);
		uiv_.setTextFieldRestriction_WindowsFilename(nameTextField_, null,
				false, true, false, false);

		uiv_.addDependentButton(okButton_);
	}

	public static void showDialog(Component owner, String title,
			LineCollection lcoll, String path) {
		ImportScalarTrack dialog = new ImportScalarTrack();
		dialog.lcoll_ = lcoll;
		if (new File(path).isFile()) {
			dialog.fileTextField_.setText(path);
		}
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.path_ = path + "datatracks/";
		dialog.setTitle(title);
		dialog.setLocationRelativeTo(owner);
		dialog.setModal(true);
		dialog.setVisible(true);

	}

	private void fileChooser() {
		JFileChooser fc = new JFileChooser(this.path_);

		FileFilter ff = new FileNameExtensionFilter("Data", "raw", "wav",
				"xml", "mat", "nex", "nex_ul", "nex_md", "csv");
		fc.setFileFilter(ff);

		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			this.fileTextField_.setText(fc.getSelectedFile().getPath());
		}

	}

	private void addTrack() {

		if (this.nameTextField_.getText().length() > 0
				&& this.fileTextField_.getText().length() > 0) {
			if (this.fileTextField_.getText().endsWith(".raw")) {
				lcoll_.addScalarTrack(this.nameTextField_.getText(),
						this.fileTextField_.getText(), true, true, true,
						Double.parseDouble(this.minTextField_.getText()),
						Double.parseDouble(this.maxTextField_.getText()),
						Project.getInstance().getLcoll().getOlinesSize(),
						false, ObjectLine.MEDIUM, false);
			} else if (this.fileTextField_.getText().endsWith(".xml")) {

				File xmlfile = new File(this.fileTextField_.getText());
				SAXBuilder sxbuild = new SAXBuilder();
				InputSource is = null;
				try {
					is = new InputSource(new FileInputStream(
							this.fileTextField_.getText()));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				Document doc;
				try {
					File file;
					if ((file = HelperFunctions.testAndGenerateFile(Project
							.getInstance().getProjectPath()
							+ "datatracks/"
							+ xmlfile.getName().substring(0,
									xmlfile.getName().length() - 4) + ".raw")) == null) {
						return;
					}
					RandomAccessFile raf = new RandomAccessFile(file, "rw");
					FileChannel fChannel = raf.getChannel();

					doc = sxbuild.build(is);
					Element root = doc.getRootElement();
					@SuppressWarnings("unchecked")
					List<Element> sampleList = ((List<Element>) root
							.getChildren("sample"));
					double last = 0;
					DoubleBuffer dB = fChannel.map(
							FileChannel.MapMode.READ_WRITE, 0,
							sampleList.size() * (Double.SIZE / 8) * 2)
							.asDoubleBuffer();
					for (Element sample : sampleList) {
						last = sample.getAttribute("time").getDoubleValue();
						dB.put(sample.getAttribute("time").getDoubleValue());
						dB.put(Double.parseDouble(sample.getValue()));
					}
					Project.getInstance().setProjectLength(last);
					raf.close();
				} catch (JDOMException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				lcoll_.addScalarTrack(
						this.nameTextField_.getText(),
						Project.getInstance().getProjectPath()
								+ "datatracks/"
								+ xmlfile.getName().substring(0,
										xmlfile.getName().length() - 4)
								+ ".raw", true, true, true,
						Double.parseDouble(this.minTextField_.getText()),
						Double.parseDouble(this.maxTextField_.getText()),
						Project.getInstance().getLcoll().getOlinesSize(),
						false, ObjectLine.MEDIUM, false);
			} else if (this.fileTextField_.getText().endsWith(".wav")) {
				File wave = new File(this.fileTextField_.getText());
				try {
					File file;
					if ((file = HelperFunctions.testAndGenerateFile(Project
							.getInstance().getProjectPath()
							+ "datatracks/"
							+ wave.getName().substring(0,
									wave.getName().length() - 4) + ".raw")) == null) {

						return;
					}
					RandomAccessFile raf = new RandomAccessFile(file, "rw");

					FileChannel fChannel = raf.getChannel();
					// Open the wav file specified as the first argument
					WavFile wavFile = WavFile.openWavFile(wave);
					// Get the number of audio channels in the wav file
					int numChannels = wavFile.getNumChannels();
					// Create a buffer of 100 frames
					double[] buffer = new double[20000 * numChannels];
					int framesRead;
					int frameCount = 0;
					long totalSamples = 0;
					do {
						// Read frames into buffer
						framesRead = wavFile.readFrames(buffer, 20000);
						DoubleBuffer dB = fChannel.map(
								FileChannel.MapMode.READ_WRITE,
								frameCount * 2 * (Double.SIZE / 8),
								framesRead * (Double.SIZE / 8) * 2)
								.asDoubleBuffer();
						frameCount += framesRead;
						// Loop through frames and look for minimum and maximum
						// value
						for (int s = 0; s < framesRead * numChannels; s += numChannels) {
							totalSamples++;
							dB.put(((totalSamples * 1.0) / (wavFile
									.getSampleRate() * 1.0)) * 1000.0);// time
							dB.put(buffer[s]);// value
						}
					} while (framesRead != 0);
					// Close the wavFile
					Project.getInstance()
							.setProjectLength(
									((totalSamples * 1.0) / (wavFile
											.getSampleRate() * 1.0)) * 1000.0);
					wavFile.close();
				} catch (Exception e) {
					System.err.println(e);
				}
				lcoll_.addScalarTrack(
						this.nameTextField_.getText(),
						Project.getInstance().getProjectPath()
								+ "datatracks/"
								+ wave.getName().substring(0,
										wave.getName().length() - 4) + ".raw",
						true, true, true,
						Double.parseDouble(this.minTextField_.getText()),
						Double.parseDouble(this.maxTextField_.getText()),
						Project.getInstance().getLcoll().getOlinesSize(),
						false, ObjectLine.MEDIUM, false);
			} else if (this.fileTextField_.getText().endsWith(".mat")) {
				File matfile = new File(this.fileTextField_.getText());
				try {
					int i = 0;
					MatFileReader mfr = new MatFileReader(
							this.fileTextField_.getText());
					Map<String, MLArray> datamap = mfr.getContent();
					MLDouble data = (MLDouble) datamap.values().iterator()
							.next();

					File file;
					if ((file = HelperFunctions.testAndGenerateFile(Project
							.getInstance().getProjectPath()
							+ "datatracks/"
							+ matfile.getName().substring(0,
									matfile.getName().length() - 4) + ".raw")) == null) {

						return;
					}

					RandomAccessFile raf = new RandomAccessFile(file, "rw");
					FileChannel fChannel = raf.getChannel();
					DoubleBuffer dB = fChannel.map(
							FileChannel.MapMode.READ_WRITE, 0,
							data.getN() * (Double.SIZE / 8) * 2)
							.asDoubleBuffer();
					for (i = 0; i < data.getN(); i++) {
						dB.put(data.get(0, i));
						dB.put(data.get(1, i));
					}
					Project.getInstance().setProjectLength(i);

					raf.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				lcoll_.addScalarTrack(
						this.nameTextField_.getText(),
						Project.getInstance().getProjectPath()
								+ "datatracks/"
								+ matfile.getName().substring(0,
										matfile.getName().length() - 4)
								+ ".raw", true, true, true,
						Double.parseDouble(this.minTextField_.getText()),
						Double.parseDouble(this.maxTextField_.getText()),
						Project.getInstance().getLcoll().getOlinesSize(),
						false, ObjectLine.MEDIUM, false);

			} else if (this.fileTextField_.getText().endsWith(".nex")) {
				RandomAccessFile in;
				try {
					File f = new File(this.fileTextField_.getText());
					in = new RandomAccessFile(this.fileTextField_.getText(),
							"r");

					String line = "";

					int speed = 0;
					int length = 0;

					double[] minimas = { Double.MAX_VALUE, Double.MAX_VALUE,
							Double.MAX_VALUE, Double.MAX_VALUE,
							Double.MAX_VALUE, Double.MAX_VALUE };
					double[] maximas = { -1.0 * Double.MAX_VALUE,
							-1.0 * Double.MAX_VALUE, -1.0 * Double.MAX_VALUE,
							-1.0 * Double.MAX_VALUE, -1.0 * Double.MAX_VALUE,
							-1.0 * Double.MAX_VALUE };

					// ArrayList<LabelTrack> tracks = new
					// ArrayList<LabelTrack>();
					ArrayList<DoubleBuffer> files = new ArrayList<DoubleBuffer>();
					ArrayList<RandomAccessFile> rafs = new ArrayList<RandomAccessFile>();
					ArrayList<File> outfiles = new ArrayList<File>();
					ArrayList<String> names = new ArrayList<String>();
					while ((line = in.readLine()) != null) {
						String[] vals = line.split(Pattern.quote("\t"));
						if (vals.length > 1) {
							if (vals[0].equals("Dauer:")
									|| vals[0].equals("Duration:")) {
								length = Integer.parseInt(vals[1]);
							}
							if (vals[0].equals("Ausgabegeschwindigkeit:")
									|| vals[0].equals("Output rate:")) {
								speed = Integer.parseInt(vals[1]);
							}
							if (vals[0].equals("ZEIT")
									|| vals[0].equals("TIME")) {
								for (int i = 1; i < vals.length - 2; i++) {
									File file;
									if ((file = HelperFunctions
											.testAndGenerateFile(Project
													.getInstance()
													.getProjectPath()
													+ "datatracks/"
													+ f.getName()
															.substring(
																	0,
																	f.getName()
																			.length() - 4)
													+ "_"
													+ vals[i].replace("/", "-")
															.replace(" ", "_")
													+ ".raw")) == null) {
										return;
									}
									outfiles.add(file);
								}
								for (int i = 1; i < vals.length - 2; i++) {
									RandomAccessFile raf = new RandomAccessFile(
											outfiles.get(i - 1), "rw");
									rafs.add(raf);
									FileChannel fChannel = raf.getChannel();
									DoubleBuffer dB = fChannel.map(
											FileChannel.MapMode.READ_WRITE,
											0,
											speed * length * (Double.SIZE / 8)
													* 2).asDoubleBuffer();
									files.add(dB);
									names.add(f.getName().substring(0,
											f.getName().length() - 4)
											+ "_"
											+ vals[i].replace("/", "-")
													.replace(" ", "_"));
								}
								break;
							}
						}
					}
					in.readLine();
					in.readLine();
					int count = 0;
					while ((line = in.readLine()) != null) {

						String[] vals = line.split(Pattern.quote("\t"));
						for (int i = 1; i < vals.length; i++) {
							files.get(i - 1)
									.put((Double.parseDouble(vals[0]) / (double) speed) * 1000.0);// zeitpunkt;
							files.get(i - 1).put(Double.parseDouble(vals[i]));// wert;
							if (Double.parseDouble(vals[i]) > maximas[i - 1]) {
								maximas[i - 1] = Double.parseDouble(vals[i]);
							}
							if (Double.parseDouble(vals[i]) < minimas[i - 1]) {
								minimas[i - 1] = Double.parseDouble(vals[i]);
							}

						}

						count++;
					}
					in.close();
					for (int i = 0; i < files.size(); i++) {
						rafs.get(i).close();
						lcoll_.addScalarTrack(names.get(i), Project
								.getInstance().getProjectPath()
								+ "datatracks/"
								+ names.get(i) + ".raw", true, true, true,
								minimas[i], maximas[i], Project.getInstance()
										.getLcoll().getOlinesSize(), false,
								ObjectLine.MEDIUM, false);
					}
					MessageManager.getInstance().requestTrackUpdate(
							new UpdateTracksEvent(this));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (this.fileTextField_.getText().endsWith(".nex_ul")) {
				RandomAccessFile in;
				try {
					File f = new File(this.fileTextField_.getText());
					in = new RandomAccessFile(this.fileTextField_.getText(),
							"r");

					String line = "";

					int speed = 512;
					int length = 0;

					double[] minimas = { Double.MAX_VALUE, Double.MAX_VALUE,
							Double.MAX_VALUE, Double.MAX_VALUE,
							Double.MAX_VALUE };
					double[] maximas = { -1.0 * Double.MAX_VALUE,
							-1.0 * Double.MAX_VALUE, -1.0 * Double.MAX_VALUE,
							-1.0 * Double.MAX_VALUE, -1.0 * Double.MAX_VALUE };
					String[] columns = { "EMG_CORR", "EMG_ZYG", "SCL", "BVP",
							"RESP" };

					ArrayList<DoubleBuffer> files = new ArrayList<DoubleBuffer>();
					ArrayList<RandomAccessFile> rafs = new ArrayList<RandomAccessFile>();
					ArrayList<File> outfiles = new ArrayList<File>();
					ArrayList<String> names = new ArrayList<String>();

					for (int i = 0; i < 5; i++) {
						File file;
						if ((file = HelperFunctions.testAndGenerateFile(Project
								.getInstance().getProjectPath()
								+ "datatracks/"
								+ f.getName().substring(0,
										f.getName().length() - 5)
								+ "_"
								+ columns[i] + ".raw")) == null) {
							return;
						}
						outfiles.add(file);
					}

					while ((line = in.readLine()) != null) {
						length++;
					}
					in.seek(0);

					for (int i = 0; i < 5; i++) {
						RandomAccessFile raf = new RandomAccessFile(
								outfiles.get(i), "rw");
						rafs.add(raf);
						FileChannel fChannel = raf.getChannel();
						DoubleBuffer dB = fChannel.map(
								FileChannel.MapMode.READ_WRITE, 0,
								length * (Double.SIZE / 8) * 2)
								.asDoubleBuffer();
						files.add(dB);
						names.add(f.getName().substring(0,
								f.getName().length() - 5)
								+ "_" + columns[i]);
					}

					int count = 0;
					while ((line = in.readLine()) != null) {

						String[] vals = line.split(Pattern.quote(","));
						for (int i = 1; i < vals.length; i++) {

							files.get(i - 1).put(
									(count / (double) speed) * 1000.0);// zeitpunkt;
							files.get(i - 1).put(Double.parseDouble(vals[i]));// wert;

							if (count > 1) {
								if (Double.parseDouble(vals[i]) > maximas[i - 1]) {
									maximas[i - 1] = Double
											.parseDouble(vals[i]);
								}
								if (Double.parseDouble(vals[i]) < minimas[i - 1]) {
									minimas[i - 1] = Double
											.parseDouble(vals[i]);
								}
							}
						}
						count++;
					}
					in.close();
					for (int i = 0; i < files.size(); i++) {
						rafs.get(i).close();
						lcoll_.addScalarTrack(names.get(i), Project
								.getInstance().getProjectPath()
								+ "datatracks/"
								+ names.get(i) + ".raw", true, true, true,
								minimas[i], maximas[i], Project.getInstance()
										.getLcoll().getOlinesSize(), false,
								ObjectLine.MEDIUM, false);
					}
					MessageManager.getInstance().requestTrackUpdate(
							new UpdateTracksEvent(this));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (this.fileTextField_.getText().endsWith(".nex_md")) {
				RandomAccessFile in;
				try {
					File f = new File(this.fileTextField_.getText());
					in = new RandomAccessFile(this.fileTextField_.getText(),
							"r");

					String line = "";

					// int speed=512;
					int length = 0;

					double[] minimas = { Double.MAX_VALUE, Double.MAX_VALUE,
							Double.MAX_VALUE, Double.MAX_VALUE,
							Double.MAX_VALUE };
					double[] maximas = { -1.0 * Double.MAX_VALUE,
							-1.0 * Double.MAX_VALUE, -1.0 * Double.MAX_VALUE,
							-1.0 * Double.MAX_VALUE, -1.0 * Double.MAX_VALUE };
					String[] columns = { "Ch_28", "Ch_29", "Ch_30", "Ch_31" };

					ArrayList<DoubleBuffer> files = new ArrayList<DoubleBuffer>();
					ArrayList<RandomAccessFile> rafs = new ArrayList<RandomAccessFile>();
					ArrayList<File> outfiles = new ArrayList<File>();
					ArrayList<String> names = new ArrayList<String>();

					for (int i = 0; i < 4; i++) {
						File file;
						if ((file = HelperFunctions.testAndGenerateFile(Project
								.getInstance().getProjectPath()
								+ "datatracks/"
								+ f.getName().substring(0,
										f.getName().length() - 7)
								+ "_"
								+ columns[i] + ".raw")) == null) {
							return;
						}
						outfiles.add(file);
					}

					while ((line = in.readLine()) != null) {
						length++;
					}
					in.seek(0);
					in.readLine();

					for (int i = 0; i < 4; i++) {
						RandomAccessFile raf = new RandomAccessFile(
								outfiles.get(i), "rw");
						rafs.add(raf);
						FileChannel fChannel = raf.getChannel();
						DoubleBuffer dB = fChannel.map(
								FileChannel.MapMode.READ_WRITE, 0,
								length * (Double.SIZE / 8) * 2)
								.asDoubleBuffer();
						files.add(dB);
						names.add(f.getName().substring(0,
								f.getName().length() - 7)
								+ "_" + columns[i]);
					}

					int count = 0;
					while ((line = in.readLine()) != null) {

						String[] vals = line.split(Pattern.quote("\t"));
						for (int i = 1; i < vals.length; i++) {

							files.get(i - 1).put(Double.parseDouble(vals[0]));// zeitpunkt;
							files.get(i - 1).put(Double.parseDouble(vals[i]));// wert;

							if (count > 1) {
								if (Double.parseDouble(vals[i]) > maximas[i - 1]) {
									maximas[i - 1] = Double
											.parseDouble(vals[i]);
								}
								if (Double.parseDouble(vals[i]) < minimas[i - 1]) {
									minimas[i - 1] = Double
											.parseDouble(vals[i]);
								}
							}
						}
						count++;
					}
					in.close();
					for (int i = 0; i < files.size(); i++) {
						rafs.get(i).close();
						lcoll_.addScalarTrack(names.get(i), Project
								.getInstance().getProjectPath()
								+ "datatracks/"
								+ names.get(i) + ".raw", true, true, true,
								minimas[i], maximas[i], Project.getInstance()
										.getLcoll().getOlinesSize(), false,
								ObjectLine.MEDIUM, false);
					}
					MessageManager.getInstance().requestTrackUpdate(
							new UpdateTracksEvent(this));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (this.fileTextField_.getText().endsWith(".csv")) {
				RandomAccessFile in;
				try {
					ArrayList<DoubleBuffer> files = new ArrayList<DoubleBuffer>();
					ArrayList<RandomAccessFile> rafs = new ArrayList<RandomAccessFile>();

					// File f = new File(this.fileTextField.getText());
					in = new RandomAccessFile(this.fileTextField_.getText(), "r");

					String line = "";
					int length = 1;
					int dimension = in.readLine().split(csvSplitRegex_).length - 1;
					double[] minimas = new double[dimension];
					double[] maximas = new double[dimension];

					while ((line = in.readLine()) != null) {
						length++;
					}

					in.seek(0);

					ArrayList<String> names = new ArrayList<String>();
					boolean hasHeaderLine = false;
					String[] splitLine = in.readLine().split(csvSplitRegex_);
					for (int i = 0; i < dimension; i++) {
						try {
							Double.parseDouble(splitLine[i + 1]);
						} catch (Exception e) {
							// not parsable as a double - must be header line
							// with names
							hasHeaderLine = true;
						}
					}

					for (int i = 0; i < dimension; i++) {
						minimas[i] = Double.MAX_VALUE;
						maximas[i] = -1.0 * Double.MAX_VALUE;
						String trackName = "";
						// first splitLine entry is dummy, so length must be
						// dimension +1
						// just check for greater dimension and ignore following
						// entries -
						if (hasHeaderLine && splitLine.length > dimension && !splitLine[i + 1].isEmpty()) {
							trackName = splitLine[i + 1];
						} else {
							if (dimension == 1) {
								trackName = nameTextField_.getText();
							} else {
								trackName = nameTextField_.getText() + "_" + i;
							}
						}
						if (trackName.isEmpty()) {
							trackName = "ERROR track " + i;
							System.err.println(trackName);
						}
						names.add(trackName);

						File file;
						if ((file = HelperFunctions.testAndGenerateFile(Project.getInstance().getProjectPath() + "datatracks/" + names.get(i) + ".raw")) == null) {
							return;
						}
						RandomAccessFile raf = new RandomAccessFile(file, "rw");
						rafs.add(raf);
						FileChannel fChannel = raf.getChannel();
						DoubleBuffer dB = fChannel.map(
								FileChannel.MapMode.READ_WRITE, 0,
								(length) * (Double.SIZE / 8) * 2)
								.asDoubleBuffer();
						files.add(dB);
					}

					// redo first line if it was not header line
					if (!hasHeaderLine) {
						in.seek(0);
					}

					while ((line = in.readLine()) != null) {
						String[] vals = line.split(csvSplitRegex_);
						if (vals.length != dimension + 1) {
							continue;
						}
						for (int i = 1; i < vals.length; i++) {
							files.get(i - 1).put(Double.parseDouble(vals[0]));// zeitpunkt;
							files.get(i - 1).put(Double.parseDouble(vals[i]));// wert;

							// if(count>1){
							if (Double.parseDouble(vals[i]) > maximas[i - 1]) {
								maximas[i - 1] = Double.parseDouble(vals[i]);
							}
							if (Double.parseDouble(vals[i]) < minimas[i - 1]) {
								minimas[i - 1] = Double.parseDouble(vals[i]);
							}
							// }
						}
						// count++;
					}
					in.close();
					for (int i = 0; i < files.size(); i++) {
						rafs.get(i).close();
						lcoll_.addScalarTrack(names.get(i), Project
								.getInstance().getProjectPath()
								+ "datatracks/"
								+ names.get(i) + ".raw", true, true, true,
								minimas[i], maximas[i], Project.getInstance()
										.getLcoll().getOlinesSize(), false,
								ObjectLine.MEDIUM, false);
					}
					MessageManager.getInstance().requestTrackUpdate(
							new UpdateTracksEvent(this));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			lcoll_.updateViewport();
			MessageManager.getInstance().requestTrackUpdate(new UpdateTracksEvent(this));
			MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			this.setVisible(false);

		}
	}
}

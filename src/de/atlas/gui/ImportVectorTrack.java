package de.atlas.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.atlas.collections.LineCollection;
import de.atlas.collections.ObjectLine;
import de.atlas.data.Project;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.misc.HelperFunctions;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

import eu.c_bauer.userinputverifier.UserInputVerifier;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;

public class ImportVectorTrack extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel_ = new JPanel();
	private JButton fileButton_;
	private JButton okButton_;
	private JTextField nameTextField_;
	private JTextField fileTextField_;
	private LineCollection lcoll_;
	private String path_;
	private JTextField minTextField_;
	private JTextField maxTextField_;
	private JComboBox colormapComboBox_;
	private JTextField dimensionTextField_;
	private UserInputVerifier uiv_;

	/**
	 * Create the dialog.
	 */
	public ImportVectorTrack() {

		setTitle("Import VectorTrack");
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

		JLabel lblColormap = new JLabel("ColorMap:");
		panel.add(lblColormap);

		colormapComboBox_ = new JComboBox();
		colormapComboBox_.setModel(new DefaultComboBoxModel(new String[] {
				"Sun", "Ocean", "Islands", "Grey", "Fire", "Colorful" }));
		panel.add(colormapComboBox_);

		JLabel lblName = new JLabel("Name:");
		panel.add(lblName);

		nameTextField_ = new JTextField();
		panel.add(nameTextField_);
		nameTextField_.setColumns(15);

		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_2.setBounds(12, 53, 436, 34);
		contentPanel_.add(panel_2);

		JLabel lblClass = new JLabel("Min:");
		panel_2.add(lblClass);

		minTextField_ = new JTextField();
		minTextField_.setColumns(5);
		minTextField_.setText("-50.0");
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
		maxTextField_.setText("50.0");
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

		JLabel lblDim = new JLabel("Dim:");
		panel_2.add(lblDim);

		dimensionTextField_ = new JTextField();
		dimensionTextField_.setColumns(5);
		dimensionTextField_.setText("50");
		// dimensionTextField_.addKeyListener(new KeyAdapter() {
		//
		// @Override
		// public void keyReleased(KeyEvent e) {
		// try{
		// if(dimensionTextField_.getText().length()>0){
		// Integer.parseInt(dimensionTextField_.getText());
		// tmpvalueDim_=dimensionTextField_.getText();
		// }else{
		// dimensionTextField_.setText("0");
		// }
		// }catch(NumberFormatException ex){
		// dimensionTextField_.setText(tmpvalueDim_);
		// }
		//
		//
		// }
		// });

		panel_2.add(dimensionTextField_);

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

		uiv_ = new UserInputVerifier();

		// name
		uiv_.setTextFieldRestriction_NonEmpty(nameTextField_, "tfname", false,
				false);
		uiv_.setTextFieldRestriction_WindowsFilename(nameTextField_, "tfname",
				false, true, false, false);

		// min
		uiv_.setTextFieldRestriction_NonEmpty(minTextField_, "tfMin", true,
				false);
		uiv_.setTextFieldRestriction_Double(minTextField_, "tfMin", true,
				false, false);

		// max
		uiv_.setTextFieldRestriction_NonEmpty(maxTextField_, "tfMax", true,
				false);
		uiv_.setTextFieldRestriction_Double(maxTextField_, "tfMax", true,
				false, false);

		// dim
		uiv_.setTextFieldRestriction_NonEmpty(dimensionTextField_, "tfDim",
				true, false);
		uiv_.setTextFieldRestriction_Int(dimensionTextField_, "tfDim", true,
				false, false);

		// file
		uiv_.setTextFieldRestriction_NonEmpty(fileTextField_, "tfFile", false,
				false);
		uiv_.setTextFieldRestriction_ExistingNormalFile(fileTextField_,
				"tfFile", true, false, false);

		uiv_.addDependentButton(okButton_);
	}

	public static void showDialog(Component owner, String title,
			LineCollection lcoll, String path) {

		ImportVectorTrack dialog = new ImportVectorTrack();
		dialog.lcoll_ = lcoll;
		if (new File(path).isFile()) {
			dialog.fileTextField_.setText(path);
		}
		// when using drag & drop, file extensions are provided and
		// name is suggest
		if (path.toLowerCase().endsWith(".xml")
				|| path.toLowerCase().endsWith(".mat")) {
			dialog.nameTextField_
					.setText(path.split(File.separator)[path
							.split(File.separator).length - 1]
							.substring(0,
									path.split(File.separator)[path
											.split(File.separator).length - 1]
											.length() - 4));
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

		FileFilter ff = new FileNameExtensionFilter("Data", "raw", "xml",
				"mat", "csv");
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
				lcoll_.addVectorTrack(this.nameTextField_.getText(),
						this.fileTextField_.getText(), true,
						(String) this.colormapComboBox_.getSelectedItem(),
						Double.parseDouble(this.minTextField_.getText()),
						Double.parseDouble(this.maxTextField_.getText()),
						Integer.parseInt(this.dimensionTextField_.getText()),
						Project.getInstance().getLcoll().getOlinesSize(),
						false, ObjectLine.MEDIUM, false, false, false);
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
					List<Element> vectorList = ((List<Element>) root
							.getChildren("vector"));

					DoubleBuffer dB = fChannel.map(
							FileChannel.MapMode.READ_WRITE,
							0,
							vectorList.size()
									* (Double.SIZE / 8)
									* (Integer
											.parseInt(this.dimensionTextField_
													.getText()) + 1))
							.asDoubleBuffer();
					double last = 0;
					for (Element vector : vectorList) {
						@SuppressWarnings("unchecked")
						List<Element> valueList = ((List<Element>) vector
								.getChildren("value"));
						last = vector.getAttribute("time").getDoubleValue();
						dB.put(vector.getAttribute("time").getDoubleValue());
						for (Element value : valueList) {
							dB.put(Double.parseDouble(value.getValue()));
						}
					}
					Project.getInstance().setProjectLength(last);
					raf.close();
					lcoll_.addVectorTrack(
							this.nameTextField_.getText(),
							Project.getInstance().getProjectPath()
									+ "datatracks/"
									+ xmlfile.getName().substring(0,
											xmlfile.getName().length() - 4)
									+ ".raw",
							true,
							(String) this.colormapComboBox_.getSelectedItem(),
							Double.parseDouble(this.minTextField_.getText()),
							Double.parseDouble(this.maxTextField_.getText()),
							Integer.parseInt(this.dimensionTextField_.getText()),
							Project.getInstance().getLcoll().getOlinesSize(),
							false, ObjectLine.MEDIUM, false, false, false);
				} catch (JDOMException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (this.fileTextField_.getText().endsWith(".mat")) {
				File matfile = new File(this.fileTextField_.getText());
				try {
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
							data.getN() * (Double.SIZE / 8) * data.getM())
							.asDoubleBuffer();
					int i = 0;
					for (i = 0; i < data.getN(); i++) {
						for (int j = 0; j < data.getM(); j++) {
							dB.put(data.get(j, i));

						}
					}

					Project.getInstance().setProjectLength(data.get(0, --i));
					raf.close();
					lcoll_.addVectorTrack(
							this.nameTextField_.getText(),
							Project.getInstance().getProjectPath()
									+ "datatracks/"
									+ matfile.getName().substring(0,
											matfile.getName().length() - 4)
									+ ".raw", true,
							(String) this.colormapComboBox_.getSelectedItem(),
							Double.parseDouble(this.minTextField_.getText()),
							Double.parseDouble(this.maxTextField_.getText()),
							data.getM() - 1, Project.getInstance().getLcoll()
									.getOlinesSize(), false, ObjectLine.MEDIUM, false, false, false);

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (this.fileTextField_.getText().endsWith(".csv")) {
				File csvfile = new File(this.fileTextField_.getText());
				try {
					RandomAccessFile in = new RandomAccessFile(
							this.fileTextField_.getText(), "r");

					File file;
					if ((file = HelperFunctions.testAndGenerateFile(Project
							.getInstance().getProjectPath()
							+ "datatracks/"
							+ csvfile.getName().substring(0,
									csvfile.getName().length() - 4) + ".raw")) == null) {
						return;
					}

					int length = 1;
					int dimension = in.readLine().split("[,;\t]").length - 1;
					while ((in.readLine()) != null) {
						length++;
					}

					in.seek(0);
					// in.readLine();

					RandomAccessFile raf = new RandomAccessFile(file, "rw");
					FileChannel fChannel = raf.getChannel();
					DoubleBuffer dB = fChannel.map(
							FileChannel.MapMode.READ_WRITE, 0,
							length * (Double.SIZE / 8) * (dimension + 1))
							.asDoubleBuffer();
					int i = 0;
					String[] vals = null;
					for (i = 0; i < length; i++) {
						vals = in.readLine().split("[,;\t]");
						for (int j = 0; j < dimension + 1; j++) {
							dB.put(Double.parseDouble(vals[j]));
						}
					}

					Project.getInstance().setProjectLength(
							Double.parseDouble(vals[0]));
					raf.close();
					in.close();
					lcoll_.addVectorTrack(
							this.nameTextField_.getText(),
							Project.getInstance().getProjectPath()
									+ "datatracks/"
									+ csvfile.getName().substring(0,
											csvfile.getName().length() - 4)
									+ ".raw", true,
							(String) this.colormapComboBox_.getSelectedItem(),
							Double.parseDouble(this.minTextField_.getText()),
							Double.parseDouble(this.maxTextField_.getText()),
							dimension, Project.getInstance().getLcoll()
									.getOlinesSize(), false, ObjectLine.MEDIUM, false, false, false);

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			lcoll_.updateViewport();
			MessageManager.getInstance().requestTrackUpdate(
					new UpdateTracksEvent(this));
			MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			this.setVisible(false);
		}
	}
}

package de.atlas.gui;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.sun.deploy.util.ArrayUtil;
import de.atlas.collections.DataTrack;
import de.atlas.collections.LineCollection;
import de.atlas.collections.ObjectLine;
import de.atlas.data.Project;
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
import java.util.*;
import java.util.List;

public class ImportDataTrack extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel_ = new JPanel();
	private final JTextField sampleRateTextField_;
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
	private JCheckBox chkIgnoreFirstColumn;
	private JCheckBox chkSeparateTracks;
	private UserInputVerifier uiv_;

	/**
	 * Create the dialog.
	 */
	public ImportDataTrack() {

		setTitle("Import DataTrack");
		this.setIconImage(new ImageIcon(Project.class.getResource("/icon.gif"))
				.getImage());
		setBounds(100, 100, 500, 240);
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
		nameTextField_.setColumns(18);

		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_2.setBounds(12, 53, 436, 34);
		contentPanel_.add(panel_2);

		JLabel lblMin = new JLabel("Min:");
		//panel_2.add(lblMin);

		minTextField_ = new JTextField();
		minTextField_.setColumns(5);
		minTextField_.setText("-50.0");
		//panel_2.add(minTextField_);

		JLabel lblMax = new JLabel("Max:");
		//panel_2.add(lblMax);

		maxTextField_ = new JTextField();
		maxTextField_.setColumns(5);
		maxTextField_.setText("50.0");
		//panel_2.add(maxTextField_);

		JLabel lblDim = new JLabel("Dim:");
		//panel_2.add(lblDim);

		dimensionTextField_ = new JTextField();
		dimensionTextField_.setColumns(5);
		dimensionTextField_.setText("50");
		//panel_2.add(dimensionTextField_);

		JLabel lblSampleRate = new JLabel("Sample frequency:");
		panel_2.add(lblSampleRate);
		sampleRateTextField_ = new JTextField();
		sampleRateTextField_.setColumns(5);
		sampleRateTextField_.setText("0");
		panel_2.add(sampleRateTextField_);

		chkIgnoreFirstColumn = new JCheckBox("Ignore first column");
		panel_2.add(chkIgnoreFirstColumn);

		chkSeparateTracks = new JCheckBox("Separate tracks");
		panel_2.add(chkSeparateTracks);

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
		uiv_.setTextFieldRestriction_NonEmpty(nameTextField_, "tfname", false, false);
		uiv_.setTextFieldRestriction_WindowsFilename(nameTextField_, "tfname", false, true, false, false);

		// min
		uiv_.setTextFieldRestriction_NonEmpty(minTextField_, "tfMin", true, false);
		uiv_.setTextFieldRestriction_Double(minTextField_, "tfMin", true, false, false);

		// max
		uiv_.setTextFieldRestriction_NonEmpty(maxTextField_, "tfMax", true, false);
		uiv_.setTextFieldRestriction_Double(maxTextField_, "tfMax", true, false, false);

		// dim
		uiv_.setTextFieldRestriction_NonEmpty(dimensionTextField_, "tfDim", true, false);
		uiv_.setTextFieldRestriction_Int(dimensionTextField_, "tfDim", true, false, false);

		// dim
		uiv_.setTextFieldRestriction_NonEmpty(sampleRateTextField_, "tfSR", true, false);
		uiv_.setTextFieldRestriction_Int(sampleRateTextField_, "tfSR", true, false, false);

		// file
		uiv_.setTextFieldRestriction_NonEmpty(fileTextField_, "tfFile", false, false);
		uiv_.setTextFieldRestriction_ExistingNormalFile(fileTextField_, "tfFile", true, false, false);

		uiv_.addDependentButton(okButton_);
	}

	public static void showDialog(Component owner, String title, LineCollection lcoll, String path) {

		ImportDataTrack dialog = new ImportDataTrack();
		dialog.lcoll_ = lcoll;
		if (new File(path).isFile()) {
			dialog.fileTextField_.setText(path);
		}
		// when using drag & drop, file extensions are provided and
		// name is suggest
		if (path.toLowerCase().endsWith(".csv")) {
			dialog.nameTextField_.setText(path.split(File.separator)[path.split(File.separator).length - 1].substring(0,path.split(File.separator)[path.split(File.separator).length - 1].length() - 4));
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

		FileFilter ff = new FileNameExtensionFilter("Data",  "csv");
		fc.setFileFilter(ff);

		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			this.fileTextField_.setText(fc.getSelectedFile().getPath());
		}

	}

	private void addTrack(){
		if(this.nameTextField_.getText().length()==0)return;
		if(!this.fileTextField_.getText().endsWith(".csv"))return;
		try {
			String regEx = "[,;\t]";
			double min = Double.MAX_VALUE;
			double max = -Double.MAX_VALUE;
			double trackLength = 0;
			int dimension = 0;
			double[] lineValues = null;
			double sampleRate = Double.parseDouble(sampleRateTextField_.getText());
			RandomAccessFile csvFile = new RandomAccessFile(this.fileTextField_.getText(),"r");

			//check for header and number of columns
			String[] header = csvFile.readLine().split(regEx);
			int columns = header.length;
			try {
				for (String s : header) {
					Double.parseDouble(s);
				}
				//no header;
				csvFile.seek(0);
				header = null;
				dimension = columns;
				if(sampleRate==0||(sampleRate!=0&&chkIgnoreFirstColumn.isSelected()))dimension--;

			}catch (NumberFormatException e){
				//has header;
			}

			//read the csv file to RAM
			ArrayList<double[]> data = new ArrayList<>();
			while(true){
				String line = csvFile.readLine();
				if(line==null)break;
				String[] elements = line.split(regEx);
				lineValues = new double[columns];
				for(int i=0;i<columns;i++){
					lineValues[i] = Double.parseDouble(elements[i]);
				}
				data.add(lineValues);
			}

			//copy RAM to RAW file, find minimum/maximum and add track
			if(!chkSeparateTracks.isSelected()){
				//create one multidimensional track
				File rawFile;
				if ((rawFile = HelperFunctions.testAndGenerateFile(Project.getInstance().getProjectPath() + "datatracks/" + nameTextField_.getText() + ".raw")) == null) return;
				DoubleBuffer dB = new RandomAccessFile(rawFile, "rw").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, data.size() * (Double.SIZE / 8) * (chkIgnoreFirstColumn.isSelected()?columns-1:columns)).asDoubleBuffer();
				//put data into buffer
				Iterator<double[]> dataIterator = data.iterator();
				while (dataIterator.hasNext()){
					lineValues = dataIterator.next();
					for(int i=chkIgnoreFirstColumn.isSelected()?1:0;i<lineValues.length;i++){
						if((sampleRate!=0||i!=0)){
							min = Double.min(lineValues[i],min);
							max = Double.max(lineValues[i],max);
						}
						dB.put(lineValues[i]);
					}
				}
                //add track to project
				lcoll_.addDataTrack(this.nameTextField_.getText(), Project.getInstance().getProjectPath() + "datatracks/" + nameTextField_.getText() + ".raw", true, (String) this.colormapComboBox_.getSelectedItem(),
						min, max, dimension, sampleRate, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, "lines", false);
			}else{
				//create multiple unidimensional tracks
                for(int i=(chkIgnoreFirstColumn.isSelected()||sampleRate==0)?1:0;i<columns;i++){
                    min = Double.MAX_VALUE;
                    max = -Double.MAX_VALUE;
                    String name=nameTextField_.getText()+"_"+(header==null?Integer.toString(i):header[i]);
                    File rawFile;
                    if ((rawFile = HelperFunctions.testAndGenerateFile(Project.getInstance().getProjectPath() + "datatracks/" + name + ".raw")) == null) return;
                    RandomAccessFile raf = new RandomAccessFile(rawFile, "rw");
                    DoubleBuffer dB = new RandomAccessFile(rawFile, "rw").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, data.size() * (Double.SIZE / 8) * (sampleRate==0?2:1)).asDoubleBuffer();

                    //put data into buffers
                    Iterator<double[]> dataIterator = data.iterator();
                    while (dataIterator.hasNext()){
                        lineValues = dataIterator.next();
                        if((sampleRate!=0||i!=0)){
                            min = Double.min(lineValues[i],min);
                            max = Double.max(lineValues[i],max);
                        }
                        if(sampleRate==0)dB.put(lineValues[0]);
                        dB.put(lineValues[i]);
                    }
                    //add tracks to project
                    lcoll_.addDataTrack(name, Project.getInstance().getProjectPath() + "datatracks/" + name + ".raw",true, (String) this.colormapComboBox_.getSelectedItem(),
                            min, max, 1, sampleRate, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, "lines", false);
                }
            }

			//clean up and update everything
			csvFile.close();
			Project.getInstance().setProjectLength(sampleRate!=0?sampleRate*data.size()*1000:lineValues[0]);
			lcoll_.updateViewport();
			MessageManager.getInstance().requestTrackUpdate(new UpdateTracksEvent(this));
			MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			this.setVisible(false);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void addTrackOld() {

		if (this.nameTextField_.getText().length() > 0 && this.fileTextField_.getText().length() > 0) {
			if (this.fileTextField_.getText().endsWith(".csv")) {
				File csvfile = new File(this.fileTextField_.getText());
				try {
					RandomAccessFile in = new RandomAccessFile(this.fileTextField_.getText(), "r");
					File file;
					if ((file = HelperFunctions.testAndGenerateFile(Project.getInstance().getProjectPath() + "datatracks/" + csvfile.getName().substring(0, csvfile.getName().length() - 4) + ".raw")) == null) {
						return;
					}

					double sampleRate = Double.parseDouble(sampleRateTextField_.getText());
					int dimension = in.readLine().split("[,;\t]").length;
					int length = 1;
					while ((in.readLine()) != null) {
						length++;
					}
					in.seek(0);
					RandomAccessFile raf = new RandomAccessFile(file, "rw");
					FileChannel fChannel = raf.getChannel();

					DoubleBuffer dB = fChannel.map(FileChannel.MapMode.READ_WRITE, 0, length * (Double.SIZE / 8) * (dimension)).asDoubleBuffer();
					int i = 0;
					String[] vals = null;
					double min = Double.MAX_VALUE;
					double max = -Double.MAX_VALUE;
					for (i = 0; i < length; i++) {
						vals = in.readLine().split("[,;\t]");
						for (int j = 0; j < dimension; j++) {
							if(j!=0||!chkIgnoreFirstColumn.isSelected()){
								double val = Double.parseDouble(vals[j]);
								dB.put(val);
								if((j!=0)||(j==0&&sampleRate!=0&&!chkIgnoreFirstColumn.isSelected())){
									min = Double.min(val,min);
									max = Double.max(val,max);
								}
							}
						}
					}

					Project.getInstance().setProjectLength(Double.parseDouble(vals[0]));
					raf.close();
					in.close();
					if(sampleRate==0||chkIgnoreFirstColumn.isSelected())dimension--;
					lcoll_.addDataTrack(this.nameTextField_.getText(), Project.getInstance().getProjectPath() + "datatracks/" + csvfile.getName().substring(0, csvfile.getName().length() - 4) + ".raw", true,
							(String) this.colormapComboBox_.getSelectedItem(), min, max, dimension, sampleRate, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, "lines", false);
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

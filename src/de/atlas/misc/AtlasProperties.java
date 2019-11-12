package de.atlas.misc;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import de.atlas.data.TimeType;

public class AtlasProperties {

	private static AtlasProperties instance = new AtlasProperties();

	// LineCollection
	private Color actualTimeColor = Color.blue;//Color.magenta; //Color.orange;
	private Color selectionColor = Color.getHSBColor(0.75f, 0.8f, 1f);

	// LabelObject
	private Color standardObjectColor = Color.getHSBColor(0.3f, 0.5f, 0.85f);;
	// LabelTrack
	private Font labelFont = new Font("monospaced", Font.BOLD, 15);
	private int labelTextspace = 5;
	private int mouseSensitivitySpace = 5;

	// TimeTrack
	private TimeType timeTrackType = TimeType.MILLIS;
	private int timeTrackSmallRaster = 20;
	private int timeTrackBigRaster = 100;
	private int timeTrackSmallFrameRaster = 40;
	private int timeTrackBigFrameRaster = 5 * this.timeTrackSmallFrameRaster;
	private Font timeTrackFont = new Font("monospaced", Font.BOLD, 9);

	// GUIUpdater
	private long updateTime = 1;

	// LabelProperties
	private Color manualColor = new Color(1, 125, 1);
	private Color autoColor = new Color(176, 2, 2);
	private Color acceptColor = new Color(1, 125, 125);
	private Color rejectColor = new Color(201, 112, 2);

	// PROJECT
	private File lastProjectPath;
	// private double scrollbarBlockValue;

	// Semaine
	private long semaineTimeSendInterval = 40;

	//matlabIO
	private boolean allowMatlabOverwrite = false;
	private boolean runMatlabInBackground = false;
	private String matlabCommand = "";
	private String matlabFunction = "";

	//LiveData
	private String liveDataURL = new String("");
	private int liveDataPort = -1;

	// THIS
	private Properties props;

	private boolean autoArranging = false;
	private double videoZoomFactor = 1.0;
	private LinkedList<JFrameBoundsWatcher> jFrameBoundsWatchers_ = new LinkedList<JFrameBoundsWatcher>();
	private String jFrameDimensions_ = "";
	private String jFrameLocations_ = "";


	public AtlasProperties() {

		props = new Properties();
		// write();

		FileInputStream in;
		try {
			in = new FileInputStream("Atlas.properties");

			props.load(in);
			try {
				this.semaineTimeSendInterval = Long.parseLong(props
						.getProperty("semaineTimeSendInterval"));
			} catch (Exception e) {
			}
			try {
				this.actualTimeColor = new Color(Integer.parseInt(props
						.getProperty("actualTimeColor")));
			} catch (Exception e) {
			}
			try {
				this.selectionColor = new Color(Integer.parseInt(props
						.getProperty("selectionColor")));
			} catch (Exception e) {
			}
			try {
				this.standardObjectColor = new Color(Integer.parseInt(props
						.getProperty("standardObjectColor")));
			} catch (Exception e) {
			}
			try {

				this.labelFont = new Font(props.getProperty("labelFont_name"),
						Integer.parseInt(props.getProperty("labelFont_style")),
						Integer.parseInt(props.getProperty("labelFont_size")));
			} catch (Exception e) {
			}
			try {

				this.labelTextspace = Integer.parseInt(props
						.getProperty("labelTextspace"));
			} catch (Exception e) {
			}
			try {
				this.mouseSensitivitySpace = Integer.parseInt(props
						.getProperty("mouseSensitivitySpace"));
			} catch (Exception e) {
			}
			try {
				this.timeTrackFont = new Font(
						props.getProperty("timeTrackFont_name"),
						Integer.parseInt(props.getProperty("timeTrackFont_style")),
						Integer.parseInt(props.getProperty("timeTrackFont_size")));
			} catch (Exception e) {
			}
			try {
				this.timeTrackType = TimeType.valueOf(props
						.getProperty("timeTrackType"));
			} catch (Exception e) {
			}
			try {

				this.timeTrackSmallRaster = Integer.parseInt(props
						.getProperty("timeTrackSmallRaster"));
			} catch (Exception e) {
			}
			try {
				this.timeTrackBigRaster = Integer.parseInt(props
						.getProperty("timeTrackBigRaster"));
			} catch (Exception e) {
			}
			try {
				this.timeTrackSmallFrameRaster = Integer.parseInt(props
						.getProperty("timeTrackSmallFrameRaster"));
			} catch (Exception e) {
			}
			try {
				this.timeTrackBigFrameRaster = Integer.parseInt(props
						.getProperty("timeTrackBigFrameRaster"));
			} catch (Exception e) {
			}
			try {
				this.updateTime = Integer.parseInt(props
						.getProperty("updatetime"));
			} catch (Exception e) {
			}
			try {
				this.manualColor = new Color(Integer.parseInt(props
						.getProperty("manualColor")));
			} catch (Exception e) {
			}
			try {
				this.autoColor = new Color(Integer.parseInt(props
						.getProperty("autoColor")));
			} catch (Exception e) {
			}
			try {
				this.acceptColor = new Color(Integer.parseInt(props
						.getProperty("acceptColor")));
			} catch (Exception e) {
			}
			try {
				this.rejectColor = new Color(Integer.parseInt(props
						.getProperty("rejectColor")));
			} catch (Exception e) {
			}
			try {
				this.autoArranging = Boolean.parseBoolean(props
						.getProperty("autoArrangeing"));
			} catch (Exception e) {
			}

			try {
				videoZoomFactor = Double.parseDouble(props
						.getProperty("videoZoomFactor"));
			} catch (Exception e) {
			}
			try {
				matlabFunction = props.getProperty("matlabFunction");
			}catch (Exception e){
			}
			try {
				matlabCommand = props.getProperty("matlabCommand");
			}catch (Exception e){
			}
			try {
				allowMatlabOverwrite = Boolean.parseBoolean(props.getProperty("matlabOverwrite"));
			}catch (Exception e){
			}
			try {
				runMatlabInBackground = Boolean.parseBoolean(props.getProperty("matlabRunInBackground"));

			}catch (Exception e){
			}
			try {
				liveDataURL = props.getProperty("liveDataURL")==null?"":props.getProperty("liveDataURL");
			}catch (Exception e){
			}
			try {
				liveDataPort = Integer.parseInt(props.getProperty("liveDataPort"));
			}catch (Exception e){
			}

			// don't allow null
			this.jFrameLocations_ = props.getProperty("JFrameLocations");
			if (null == this.jFrameLocations_) {
				this.jFrameLocations_ = "";
			}
			this.jFrameDimensions_ = props.getProperty("JFrameDimensions");
			if (null == this.jFrameDimensions_) {
				this.jFrameDimensions_ = "";
			}

			in.close();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,
					"Can not read \"Atlas.properties\" (FileNotFound)",
					e.getMessage(), JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Can not read \"Atlas.properties\" (IO)",
					e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
	}

	public void write() {

		try {

			props.setProperty("actualTimeColor",
					String.valueOf(actualTimeColor.getRGB()));
			props.setProperty("selectionColor",
					String.valueOf(selectionColor.getRGB()));
			props.setProperty("standardObjectColor",
					String.valueOf(standardObjectColor.getRGB()));

			props.setProperty("labelFont_name",
					String.valueOf(labelFont.getFontName()));
			props.setProperty("labelFont_style",
					String.valueOf(labelFont.getStyle()));
			props.setProperty("labelFont_size",
					String.valueOf(labelFont.getSize()));

			props.setProperty("labelTextspace", String.valueOf(labelTextspace));
			props.setProperty("mouseSensitivitySpace",
					String.valueOf(mouseSensitivitySpace));

			props.setProperty("timeTrackFont_name",
					String.valueOf(timeTrackFont.getFontName()));
			props.setProperty("timeTrackFont_style",
					String.valueOf(timeTrackFont.getStyle()));
			props.setProperty("timeTrackFont_size",
					String.valueOf(timeTrackFont.getSize()));

			props.setProperty("timeTrackType", String.valueOf(timeTrackType));

			props.setProperty("timeTrackSmallRaster",
					String.valueOf(timeTrackSmallRaster));
			props.setProperty("timeTrackBigRaster",
					String.valueOf(timeTrackBigRaster));
			props.setProperty("timeTrackSmallFrameRaster",
					String.valueOf(timeTrackSmallFrameRaster));
			props.setProperty("timeTrackBigFrameRaster",
					String.valueOf(timeTrackBigFrameRaster));

			props.setProperty("updatetime", String.valueOf(updateTime));

			props.setProperty("manualColor",
					String.valueOf(manualColor.getRGB()));
			props.setProperty("autoColor", String.valueOf(autoColor.getRGB()));
			props.setProperty("acceptColor",
					String.valueOf(acceptColor.getRGB()));
			props.setProperty("rejectColor",
					String.valueOf(rejectColor.getRGB()));

			props.setProperty("autoArrangeing", String.valueOf(autoArranging));
			props.setProperty("videoZoomFactor",
					String.valueOf(videoZoomFactor));
			props.setProperty("JFrameLocations", this.jFrameLocations_);
			props.setProperty("JFrameDimensions", this.jFrameDimensions_);
			props.setProperty("semaineTimeSendInterval",
					String.valueOf(this.semaineTimeSendInterval));
			props.setProperty("matlabFunction", matlabFunction);
			props.setProperty("matlabCommand", matlabCommand);
			props.setProperty("matlabOverwrite", String.valueOf(allowMatlabOverwrite));
			props.setProperty("matlabRunInBackground", String.valueOf(runMatlabInBackground));

			props.setProperty("liveDataURL", liveDataURL);
			props.setProperty("liveDataPort", String.valueOf(liveDataPort));

			FileOutputStream out = new FileOutputStream("Atlas.properties");
			props.store(out, null);
			out.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Can not write \"Atlas.properties\"", e.getMessage(),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public String getMatlabCommand() {
		return matlabCommand;
	}

	public void setMatlabCommand(String command) {
		this.matlabCommand = command;
	}

	public String getMatlabFunction() {
		return matlabFunction;
	}

	public void setMatlabFunction(String function) {
		this.matlabFunction = function;
	}

	public boolean isRunMatlabInBackground() {
		return runMatlabInBackground;
	}

	public void setRunMatlabInBackground(boolean runInBackground) {
		this.runMatlabInBackground = runInBackground;
	}

	public boolean isAllowMatlabOverwrite() {
		return allowMatlabOverwrite;
	}

	public void setAllowMatlabOverwrite(boolean allowOverwrite) {
		this.allowMatlabOverwrite = allowOverwrite;
	}


	public long getSemaineTimeSendInterval() {
		return this.semaineTimeSendInterval;
	}

	public boolean isAutoarranging() {
		return autoArranging;
	}

	public void setAutoarranging(boolean autoarranging) {
		this.autoArranging = autoarranging;
	}

	public int getTimeTrackSmallFrameRaster() {
		return timeTrackSmallFrameRaster;
	}

	public void setTimeTrackSmallFrameRaster(int timeTrackSmallFrameRaster) {
		this.timeTrackSmallFrameRaster = timeTrackSmallFrameRaster;
	}

	public int getTimeTrackBigFrameRaster() {
		return timeTrackBigFrameRaster;
	}

	public void setTimeTrackBigFrameRaster(int timeTrackBigFrameRaster) {
		this.timeTrackBigFrameRaster = timeTrackBigFrameRaster;
	}

	public Color getSelectionColor() {
		return selectionColor;
	}

	public void setSelectionColor(Color selectionColor) {
		this.selectionColor = selectionColor;
	}

	public Color getRejectColor() {
		return rejectColor;
	}

	public void setRejectColor(Color rejectColor) {
		this.rejectColor = rejectColor;
	}

	public Color getManualColor() {
		return manualColor;
	}

	public void setManualColor(Color manualColor) {
		this.manualColor = manualColor;
	}

	public Color getAutoColor() {
		return autoColor;
	}

	public void setAutoColor(Color autoColor) {
		this.autoColor = autoColor;
	}

	public Color getAcceptColor() {
		return acceptColor;
	}

	public void setAcceptColor(Color acceptColor) {
		this.acceptColor = acceptColor;
	}

	public static AtlasProperties getInstance() {
		return instance;
	}

	public Color getActualTimeColor() {
		return actualTimeColor;
	}

	public void setActualTimeColor(Color actualTimeColor) {
		this.actualTimeColor = actualTimeColor;
	}

	public Color getStandardObjectColor() {
		return standardObjectColor;
	}

	public void setStandardObjectColor(Color standardObjectColor) {
		this.standardObjectColor = standardObjectColor;
	}

	public Font getLabelFont() {
		return labelFont;
	}

	public void setLabelFont(Font labelFont) {
		this.labelFont = labelFont;
	}

	public int getLabelTextspace() {
		return labelTextspace;
	}

	public void setLabelTextspace(int labelTextspace) {
		this.labelTextspace = labelTextspace;
	}

	public int getMouseSensitivitySpace() {
		return mouseSensitivitySpace;
	}

	public void setMouseSensitivitySpace(int mouseSensitivitySpace) {
		this.mouseSensitivitySpace = mouseSensitivitySpace;
	}

	public Font getTimeTrackFont() {
		return timeTrackFont;
	}

	public void setTimeTrackFont(Font timeTrackFont) {
		this.timeTrackFont = timeTrackFont;
	}

	public TimeType getTimeTrackType() {
		return timeTrackType;
	}

	public void setTimeTrackType(TimeType timeTrackType) {
		this.timeTrackType = timeTrackType;
	}

	public int getTimeTrackSmallRaster() {
		return timeTrackSmallRaster;
	}

	public void setTimeTrackSmallRaster(int timeTrackSmallRaster) {
		this.timeTrackSmallRaster = timeTrackSmallRaster;
	}

	public int getTimeTrackBigRaster() {
		return timeTrackBigRaster;
	}

	public void setTimeTrackBigRaster(int timeTrackBigRaster) {
		this.timeTrackBigRaster = timeTrackBigRaster;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long sleeptime) {
		this.updateTime = sleeptime;
	}

	public File getLastProjectPath() {
		return lastProjectPath;
	}

	public void setLastProjectPath(File lastProjectPath) {
		this.lastProjectPath = lastProjectPath;
	}

	public double getVideoZoomFactor() {
		return videoZoomFactor;
	}

	public void setVideoZoomFactor(double zoomFactor) {
		this.videoZoomFactor = zoomFactor;
	}

	/**
	 * Returns a substring for location and size by name. Format:
	 * ".....;name,....;..." input will return "name,...."
	 * 
	 * @param name
	 *            name of wanted section
	 * @param input
	 *            string containing the information
	 * @return substring with name, null if no match
	 */
	private String getStringSection(String name, String input) {
		String result = null;
		String[] strings = input.split(";");
		for (int i = 0; i < strings.length; i++) {
			if (strings[i].startsWith(name + ",")) {
				result = strings[i];
				break;
			}
		}
		return result;
	}

	/**
	 * Sets a substring of the locations new.
	 * 
	 * @param newSection
	 *            new string
	 */
	private void setLocationSection(String newSection) {
		String[] locations = this.jFrameLocations_.split(";");
		boolean found = false;
		for (int i = 0; i < locations.length; i++) {
			if (locations[i].startsWith(newSection.split(",")[0] + ",")) {
				locations[i] = newSection;
				found = true;
				break;
			}
		}
		String result = "";
		for (int i = 0; i < locations.length; i++) {
			if (i > 0) {
				result += ";";
			}
			result += locations[i];
		}
		// append newSection if no matching name was found
		if (!found) {
			if (0 < result.length()) {
				result += ";";
			}
			result += newSection;
		}
		this.jFrameLocations_ = result;
	}

	/**
	 * Sets a substring of the locations new.
	 * 
	 * @param newSection
	 *            new string
	 */
	private void setDimensionSection(String newSection) {
		String[] strings = this.jFrameDimensions_.split(";");
		boolean found = false;
		for (int i = 0; i < strings.length; i++) {
			if (strings[i].startsWith(newSection.split(",")[0] + ",")) {
				strings[i] = newSection;
				found = true;
				break;
			}
		}
		String result = "";
		for (int i = 0; i < strings.length; i++) {
			if (i > 0) {
				result += ";";
			}
			result += strings[i];
		}
		// append newSection if no matching name was found
		if (!found) {
			if (0 < result.length()) {
				result += ";";
			}
			result += newSection;
		}
		this.jFrameDimensions_ = result;
	}

	/**
	 * Reads the properties file and applies its location information of the
	 * JFrame. If a JFrameBoundsWatcher with the supplied name is not found,
	 * nothing is done.
	 * 
	 * @param name
	 *            name of the JFrameBoundsWatcher
	 */
	public void restoreLocationFromProperties(String name) {
		JFrameBoundsWatcher watcher = null;
		// find watcher
		for (int i = 0; i < this.jFrameBoundsWatchers_.size(); i++) {
			if (name.equals(this.jFrameBoundsWatchers_.get(i).getName())) {
				watcher = this.jFrameBoundsWatchers_.get(i);
				break;
			}
		}
		if (null == watcher) {
			return;
		}
	}

	/**
	 * Adds a JFrameBoundsWatcher that sets size and location information of
	 * JFrames. The name must be unique.
	 * 
	 * @param name
	 *            name for the watched JFrame - if set to null jFrame.getName()
	 *            is used.
	 * @param jFrame
	 *            JFrame to monitor
	 * @param watchPosition
	 *            watch position of JFrame
	 * @param watchDimension
	 *            watch size of JFrame
	 * @return JFrameBoundsWatcher successfully added
	 */
	public boolean addJFrameBoundsWatcher(String name, JFrame jFrame,
			boolean watchPosition, boolean watchDimension) {

		// check name if null
		if (null == name) {
			name = jFrame.getName();
		}

		// check for duplicate name
		for (int i = 0; i < jFrameBoundsWatchers_.size(); i++) {
			if (name.equals(jFrameBoundsWatchers_.get(i).getName())) {
				System.err.println("FrameBoundsWatchers: name already used (" + name +")");
				return false;
			}
		}

		JFrameBoundsWatcher watcher = null;
		try {
			watcher = new JFrameBoundsWatcher(name, jFrame, watchPosition,
					watchDimension);
		} catch (Exception e) {
			System.err.println("error creating JFrameBoundsWatcher");
			return false;
		}
		jFrameBoundsWatchers_.add(watcher);
		return true;
	}

	public String getLiveDataURL() {
		return liveDataURL;
	}

	public int getLiveDataPort() {
		return liveDataPort;
	}

	public void setLiveDataURL(String liveDataURL) {
		this.liveDataURL = liveDataURL;
	}

	public void setLiveDataPort(int liveDataPort) {
		this.liveDataPort = liveDataPort;
	}

	// class for location and size
	private class JFrameBoundsWatcher {
		private String name_ = null;
		private JFrame jFrameWatched_ = null;
		// private HierarchyBoundsListener componentListener_ = null;
		private boolean watchLocation_ = false;
		private boolean watchDimension_ = false;
		private ComponentListener componentListener_ = null;

		private JFrameBoundsWatcher(String name, JFrame jFrame,
				boolean watchLocation, boolean watchDimension) {
			watchLocation_ = watchLocation;
			watchDimension_ = watchDimension;
			if (!watchLocation_ && !watchDimension_) {
				throw new IllegalArgumentException(
						"at least position or dimension must be watched or a WindowPositionWatcher"
								+ " is useless.");
			} else if (null == jFrame) {
				throw new IllegalArgumentException("jFrame is null");
			} else {
				jFrameWatched_ = jFrame;
				if (null == name) {
					name_ = jFrame.getName();
				} else {
					name_ = name;
				}
				componentListener_ = new ComponentListener() {
					@Override
					public void componentResized(ComponentEvent e) {
						if (watchDimension_) {
							// may not be
							String result = "";
							result += name_
									+ ","
									+ (int) jFrameWatched_.getSize()
											.getHeight() + ","
									+ (int) jFrameWatched_.getSize().getWidth();
							AtlasProperties.getInstance()
									.setDimensionSection(result);
						}
					}

					@Override
					public void componentMoved(ComponentEvent e) {
						if (watchLocation_) {
							String result = "";
							result += name_ + ","
									+ (int) jFrameWatched_.getLocation().getX()
									+ ","
									+ (int) jFrameWatched_.getLocation().getY();
							AtlasProperties.getInstance()
									.setLocationSection(result);
						}
					}

					@Override
					public void componentShown(ComponentEvent e) {
						// do nothing
					}

					@Override
					public void componentHidden(ComponentEvent e) {
						// do nothing
					}
				};

				jFrameWatched_.addComponentListener(componentListener_);

				// restore location and dimension
				if (watchLocation_) {
					String locationStr = getStringSection(this.name_,
							jFrameLocations_);
					if (null != locationStr) {
						try {
							int x = Integer.parseInt(locationStr.split(",")[1]);
							int y = Integer.parseInt(locationStr.split(",")[2]);
							Dimension screenDim = new Dimension(0,0); //Toolkit.getDefaultToolkit().getScreenSize();
							for(GraphicsDevice screen : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
							{
								screenDim.width = screenDim.width + screen.getDisplayMode().getWidth();
								screenDim.height = screen.getDisplayMode().getHeight() > screenDim.height ? screen.getDisplayMode().getHeight() : screenDim.height;
							}

							// restore last used location, if in screen
							// add 15 pixel to allow mouse access to the frame
							// !!! this is not neccessarily the real area of the
							// screen, so more than 15 pixel in the screen may also
							// result in ignoring the last position - see doc for
							// getLocation
							if (x < screenDim.width - 15 && y < screenDim.height - 15) {
								this.jFrameWatched_.setLocation(x, y);
							}else{
								this.jFrameWatched_.getName();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				if (watchDimension_) {
					String dimStr = getStringSection(this.name_,
							jFrameDimensions_);
					if (null != dimStr) {
						try {
							int height = Integer.parseInt(dimStr.split(",")[1]);
							int width = Integer.parseInt(dimStr.split(",")[2]);
							this.jFrameWatched_.setSize(width, height);
						} catch (Exception e) {
						}
					}
				}
			}

		}

		/**
		 * Get unique name of JFrameBoundsWatcher.
		 * 
		 * @return name of JFrameBoundsWatcher
		 */
		public String getName() {
			return name_;
		}

		protected void finalize() {
			try {
				this.jFrameWatched_.removeComponentListener(componentListener_);
			} catch (Exception e) {

			}
		}
	}

}

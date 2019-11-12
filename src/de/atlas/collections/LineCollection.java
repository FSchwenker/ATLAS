package de.atlas.collections;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import de.atlas.colormap.*;
import de.atlas.gui.GuiUpdater;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

import de.atlas.data.LabelClass;
import de.atlas.data.LabelClasses;
import de.atlas.data.LabelObject;
import de.atlas.data.Project;
import de.atlas.data.TimeType;
import de.atlas.gui.ImportAudio;
import de.atlas.gui.ImportLabelTrack;
import de.atlas.gui.ImportScalarTrack;
import de.atlas.gui.ImportVectorTrack;
import de.atlas.gui.ImportVideo;
import de.atlas.messagesystem.ClassChangedEvent;
import de.atlas.messagesystem.ClassChangedListener;
import de.atlas.messagesystem.LabelSelectionEvent;
import de.atlas.messagesystem.LabelSelectionListener;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.RepaintListener;
import de.atlas.messagesystem.SlotChangedListener;
import de.atlas.messagesystem.SlotEvent;
import de.atlas.messagesystem.TimeChangedListener;
import de.atlas.messagesystem.TimeEvent;
import de.atlas.messagesystem.TimeTypeChangedListener;
import de.atlas.messagesystem.TimeTypeEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.messagesystem.UpdateTracksListener;
import de.atlas.messagesystem.ZoomChangedListener;
import de.atlas.messagesystem.ZoomEvent;
import de.atlas.misc.AnnotationKeyListener;
import de.atlas.misc.AtlasProperties;

import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class LineCollection extends JPanel implements DropTargetListener, MouseListener, MouseMotionListener, MouseWheelListener  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<ObjectLine> olines_ = new ArrayList<ObjectLine>();
	JScrollBar timeScrollBar_;
	private JScrollPane vertScrollPane_;
	private Box scrollBox_;
	//private Box box_;

	private long currentTime_ = 0;
	private int start_;

	private int mouseButton_;
	private LabelObject activeLabel_;
	private double scrollbarBlockValue = 10;
	private ObjectLine timeTrack_;
	private DataFlavor fileDataFlavor_;

	private TimeType timeType_ = TimeType.MILLIS;

	private NumberFormat formatter2_ = new DecimalFormat("#00");
	private NumberFormat formatter3_ = new DecimalFormat("#000");
	private FontMetrics fm;

	public LineCollection() {
		try {
			if (System.getProperty("os.name").startsWith("Linux")) {
				// fileDataFlavor_ = DataFlavor.javaFileListFlavor;
				fileDataFlavor_ = new DataFlavor(
						"text/uri-list;class=java.lang.String");
			}

			if (System.getProperty("os.name").startsWith("Windows")) {
				fileDataFlavor_ = new DataFlavor(
						"application/x-java-file-list;class=java.util.List");
			}
			if (System.getProperty("os.name").startsWith("Mac")) {
				fileDataFlavor_ = new DataFlavor(
						"text/uri-list;class=java.lang.String");
			}
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		this.setLayout(null);
		this.setBounds(1, 1, 600, 0);
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.setFocusable(true);
		this.addKeyListener(AnnotationKeyListener.getInstance());
		new DropTarget(this, this);
		//box_ = Box.createVerticalBox();
		//box_.setSize(600, 60);

		scrollBox_ = Box.createVerticalBox();
		scrollBox_.setSize(600, 60);

		timeScrollBar_ = new JScrollBar();
		getTimeScrollBar().setOrientation(JScrollBar.HORIZONTAL);
		getTimeScrollBar().setMaximum(0);
		getTimeScrollBar().setMinimum(0);
		getTimeScrollBar().addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				adjustmentChanged();
			}
		});

		vertScrollPane_ = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		vertScrollPane_.setViewportView(scrollBox_);

		this.add(getTimeScrollBar());
		start_ = 0;

		this.addTimeTrack(this.getWidth());
		scrollBox_.addMouseListener(this);
		scrollBox_.addMouseMotionListener(this);
		scrollBox_.addMouseWheelListener(this);

		MessageManager.getInstance().addTimeChangedListener(
				new TimeChangedListener() {
					@Override
					public void timeChanged(TimeEvent e) {
						setTime(e.getTime());
						if ((e.getSource().getClass().toString().equals(GuiUpdater.class.toString()))||e.getSource().getClass().toString().contains("LiveData")) {
							switch (Project.getInstance().getScrollType()) {
							case HALF:
								if (e.getTime() > start_
										+ (getWidth() - timeTrack_.getNameP()
												.getWidth())
										/ Project.getInstance().getZoom()) {
									getTimeScrollBar()
											.setValue(
													(int) (e.getTime() - ((getWidth() - timeTrack_
															.getNameP()
															.getWidth()) / 2)
															/ Project
																	.getInstance()
																	.getZoom()));
									adjustmentChanged();
								}

								break;
							case FULL:
								if (e.getTime() > start_
										+ (getWidth() - timeTrack_.getNameP()
												.getWidth())
										/ Project.getInstance().getZoom()) {
									getTimeScrollBar().setValue(
											(int) (e.getTime()));
									adjustmentChanged();
								}

								break;
							case SLIDE:
								if (e.getTime() > start_
										+ (((getWidth() - timeTrack_.getNameP()
												.getWidth()) / 2.0))
										/ Project.getInstance().getZoom()) {
									getTimeScrollBar()
											.setValue(
													(int) (e.getTime() - ((getWidth() - timeTrack_
															.getNameP()
															.getWidth()) / 2)
															/ Project
																	.getInstance()
																	.getZoom()));
									adjustmentChanged();
								}
								break;
							}
						}
					}
				});
		MessageManager.getInstance().addZoomChangedListener(
				new ZoomChangedListener() {
					@Override
					public void zoomChanged(ZoomEvent e) {
						adjustZoom();
					}
				});
		MessageManager.getInstance().addUpdateTracksListener(
				new UpdateTracksListener() {

					@Override
					public void updateTracks(UpdateTracksEvent e) {
						scrollBox_.removeAll();
						Iterator<ObjectLine> oI = olines_.iterator();
						while (oI.hasNext()) {
							scrollBox_.add(oI.next());
						}
						vertScrollPane_.setViewportView(scrollBox_);

					}

				});
		MessageManager.getInstance().addLabelSelectionListener(
				new LabelSelectionListener() {
					@Override
					public void selectionChanged(LabelSelectionEvent e) {
						activeLabel_ = e.getLabelObject();
						int start = getTimeScrollBar().getValue();
						int end = (int) (start + (getWidth() - timeTrack_.getNameP().getWidth()) / Project.getInstance().getZoom());

						if (null != activeLabel_ && (activeLabel_.getStart() < start || activeLabel_.getEnd() > end)) {
							int labelMiddle = (activeLabel_.getStart().intValue() + activeLabel_.getEnd().intValue()) / 2;
							getTimeScrollBar().setValue((int) ((labelMiddle) - (getWidth() - timeTrack_.getNameP().getWidth()) / Project.getInstance().getZoom() / 2.0));
                            adjustmentChanged();
						}
						paintMe();
					}
				});
		MessageManager.getInstance().addRepaintListener(new RepaintListener() {
			@Override
			public void repaintRequested(RepaintEvent e) {
				paintMe();
			}
		});
		// this.add(box_);
		MessageManager.getInstance().addSlotChangedListener(
				new SlotChangedListener() {
					@Override
					public void slotChanged(SlotEvent e) {
						if (!(e.getSource().getClass().toString()
								.equals(LineCollection.class
										.toString()))) {
							LineCollection.this.getTimeScrollBar().setValue(
									e.getStart());
						}
					}
				});
		MessageManager.getInstance().addClassChangedListener(
				new ClassChangedListener() {
					public void classChanged(ClassChangedEvent e) {
						paintMe();
					}
				});
		MessageManager.getInstance().addTimeTypeChangedListener(
				new TimeTypeChangedListener() {

					@Override
					public void timeTypeChanged(TimeTypeEvent e) {
						timeType_ = e.getTimeType();
					}
				});

		changeTimeType();

		this.add(vertScrollPane_);
	}

	private void changeTimeType() {
		// TODO Auto-generated method stub

	}

	public void reset() {
		this.olines_.clear();
		this.scrollBox_.removeAll();
		currentTime_ = 0;
		start_ = 0;
		activeLabel_ = null;
	}

	public double getScrollbarBlockValue() {
		return scrollbarBlockValue;
	}

	public void setScrollbarBlockValue(double scrollbarBlockValue) {
		this.scrollbarBlockValue = scrollbarBlockValue;
	}

	public void setScrollBarMax(double val) {
		this.getTimeScrollBar().setMaximum((int) val);
		double block = (int) (((double) (Project.getInstance().getLcoll()
				.getWidth() / (double) Project.getInstance().getZoom())));

		this.getTimeScrollBar().setBlockIncrement((int) ((block / 2.0)));
		this.getTimeScrollBar().setUnitIncrement((int) ((block / 20.0)));
	}

	public void writeXML() {
		Iterator<ObjectLine> oI = olines_.iterator();
		while (oI.hasNext()) {
			oI.next().writeXML();
		}
	}

	public ArrayList<ObjectLine> getList() {
		return olines_;
	}

	private void paintMe() {
		//vertScrollPane_.setViewportView(scrollBox_);
        vertScrollPane_.updateUI();
		this.repaint();
	}

	private void adjustZoom() {
		double block = (int) (((double) (Project.getInstance().getLcoll()
				.getWidth() / (double) Project.getInstance().getZoom())));

		this.getTimeScrollBar().setBlockIncrement((int) ((block / 2.0)));
		this.getTimeScrollBar().setUnitIncrement((int) ((block / 20.0)));
		if (this.activeLabel_ != null) {
			double shift = ((Project.getInstance().getLcoll().getWidth() / 2.0)
					/ Project.getInstance().getZoom() - ((this.activeLabel_
					.getEnd() - this.activeLabel_.getStart()) / 2.0));
			this.getTimeScrollBar().setValue(
					(int) ((this.activeLabel_.getStart() * 1.0) - (shift)));
		} else {
			double shift = ((Project.getInstance().getLcoll().getWidth() / 2.0) / 1.0);
			this.getTimeScrollBar().setValue(
					(int) (currentTime_ - (shift / Project.getInstance()
							.getZoom())));
		}
		this.paintMe();
	}

	public void updateTime(int t) {
		getTimeScrollBar().setMaximum(t);
		// tline.updateTime(t);
	}

	public void swap(int i, int j) {
		olines_.get(i).setOrder(j);
		olines_.get(j).setOrder(i);
		Collections.swap(olines_, i, j);
		scrollBox_.removeAll();
		Iterator<ObjectLine> oI = olines_.iterator();
		while (oI.hasNext()) {
			scrollBox_.add(oI.next());
		}
		vertScrollPane_.setViewportView(scrollBox_);
	}

	public void addLabelTrack(String name, LabelClass lc, int order, int trackHeight) {
		ObjectLine ol = new ObjectLine(new LabelTrack(this.getWidth(), lc, name));
		this.olines_.add(ol);
		ol.setActive(true);
		ol.setTrackHeight(trackHeight);
		ol.setOrder(order);
		Collections.sort(olines_);
		scrollBox_.add(ol);
		// scrollBox_.add(Box.createVerticalStrut(0));
		// box_.add(ol);
		// box_.add(Box.createVerticalStrut(0));
		this.setBounds(1, 1, this.getWidth(), this.getHeight() + ol.getHeight());
		repaint();
	}

	public void addLabelTrack(LabelTrack lt, boolean active, int order,
			boolean learnable, int trackHeight, boolean sentToMatlab) {

		ObjectLine ol = new ObjectLine(lt);
		ol.setActive(active);
		ol.setLearnable(learnable);
		ol.setTrackHeight(trackHeight);
		ol.setOrder(order);
		ol.setSendToMatlab(sentToMatlab);
		this.olines_.add(ol);
		Collections.sort(olines_);
		scrollBox_.add(ol);
		// scrollBox_.add(Box.createVerticalStrut(0));
		// box_.add(ol);
		// box_.add(Box.createVerticalStrut(0));
		this.setBounds(1, 1, this.getWidth(), this.getHeight() + ol.getHeight());
		repaint();
	}

	public void addScalarTrack(String name, String file, boolean active, boolean showpoints, boolean showline, double min, double max,
			int order, boolean learnable, int trackHeight, boolean sendToMatlab) {
		ScalarTrack tmp = new ScalarTrack(this.getWidth(), file, name);
		tmp.setShowPoints(showpoints);
		tmp.setShowLine(showline);
		tmp.setMin(min);
		tmp.setMax(max);
		ObjectLine ol = new ObjectLine(tmp);
		ol.setActive(active);
		ol.setLearnable(learnable);
		ol.setTrackHeight(trackHeight);
		ol.setOrder(order);
		ol.setSendToMatlab(sendToMatlab);
		this.olines_.add(ol);
		Collections.sort(olines_);
		scrollBox_.add(ol);
		// scrollBox_.add(Box.createVerticalStrut(0));
		// box_.add(ol);
		// box_.add(Box.createVerticalStrut(0));
		this.setBounds(1, 1, this.getWidth(), this.getHeight() + ol.getHeight());
		repaint();
	}

	public void addVectorTrack(String name, String file, boolean active, String colormap, double min, double max, int dimension, int order,
			boolean learnable, int trackHeight, boolean asScalar, boolean isStacked, boolean sendToMatlab) {

		ColorMap cm;
		if (colormap.equalsIgnoreCase("Sun")) {
			cm = new ColorMap_Sun();
		} else if (colormap.equalsIgnoreCase("Ocean")) {
			cm = new ColorMap_Ocean();
		} else if (colormap.equalsIgnoreCase("Islands")) {
			cm = new ColorMap_Islands();
		} else if (colormap.equalsIgnoreCase("Gray")) {
			cm = new ColorMap_Gray();
		} else if (colormap.equalsIgnoreCase("Fire")) {
			cm = new ColorMap_Fire();
		} else if (colormap.equalsIgnoreCase("Colorful")) {
			cm = new ColorMap_Colorful();
		} else {
			cm = new ColorMap_Sun();
		}
		VectorTrack tmp = new VectorTrack(this.getWidth(), file, name, cm, min,
				max, dimension);

		ObjectLine ol = new ObjectLine(tmp);
		ol.setActive(active);
		ol.setLearnable(learnable);
		ol.setAsScalar(asScalar);
		ol.setStacked(isStacked);
		ol.setTrackHeight(trackHeight);
		ol.setOrder(order);
		ol.setSendToMatlab(sendToMatlab);
		this.olines_.add(ol);
		Collections.sort(olines_);
		scrollBox_.add(ol);
		scrollBox_.add(Box.createVerticalStrut(0));
		this.setBounds(1, 1, this.getWidth(), this.getHeight() + ol.getHeight());
		repaint();
	}
	public void addDataTrack(String name, String file, boolean active, String colormap, double min, double max, int dimension, double sampleRate, int order,
							   boolean learnable, int trackHeight, String style, boolean sendToMatlab) {

		ColorMap cm;
		if (colormap.equalsIgnoreCase("Sun")) {
			cm = new ColorMap_Sun();
		} else if (colormap.equalsIgnoreCase("Ocean")) {
			cm = new ColorMap_Ocean();
		} else if (colormap.equalsIgnoreCase("Islands")) {
			cm = new ColorMap_Islands();
		} else if (colormap.equalsIgnoreCase("Gray")) {
			cm = new ColorMap_Gray();
		} else if (colormap.equalsIgnoreCase("Fire")) {
			cm = new ColorMap_Fire();
		} else if (colormap.equalsIgnoreCase("Colorful")) {
			cm = new ColorMap_Colorful();
		} else {
			cm = new ColorMap_Sun();
		}
		DataTrack tmp = new DataTrack(this.getWidth(), file, name, cm, min, max, dimension, sampleRate);

		ObjectLine ol = new ObjectLine(tmp);
		ol.setActive(active);
		ol.setLearnable(learnable);
		ol.setStyle(style);
		ol.setTrackHeight(trackHeight);
		ol.setOrder(order);
		ol.setSendToMatlab(sendToMatlab);
		this.olines_.add(ol);
		Collections.sort(olines_);
		scrollBox_.add(ol);
		scrollBox_.add(Box.createVerticalStrut(0));
		this.setBounds(1, 1, this.getWidth(), this.getHeight() + ol.getHeight());
		repaint();
	}
    private void addTimeTrack(int time) {
		timeTrack_ = new ObjectLine(new TimeTrack(time));
		// this.olines.add(ol);
		this.add(timeTrack_);
		// box_.add(Box.createVerticalStrut(0));
		this.setBounds(1, 1, this.getWidth(),
				this.getHeight() + timeTrack_.getHeight());

		repaint();
	}

	public void adjustmentChanged() {
		int val = getTimeScrollBar().getValue();
		start_ = val;
		// scalar,video,label tracks
		MessageManager.getInstance().slotChanged(new SlotEvent(this, start_));
		this.repaint();
	}

	public void paint(Graphics g) {
		if(g==null)return;
		super.paint(g);
		double frameDuration;
		long hour, min, sec, ms;

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// DRAW TIMEMARKER

		g2.setColor(AtlasProperties.getInstance().getActualTimeColor());
		int x = (int) (currentTime_ * Project.getInstance().getZoom()
				+ timeTrack_.getNameP().getWidth() - this.getTimeScrollBar()
				.getValue() * Project.getInstance().getZoom());
		if (x > timeTrack_.getNameP().getWidth()) {
//			g2.fillRect((int) (currentTime_ * Project.getInstance().getZoom() + timeTrack_.getNameP().getWidth() - this.getTimeScrollBar().getValue() * Project.getInstance().getZoom()), this.getTimeScrollBar().getHeight() + 27, 1, this.getHeight());
			g2.fillRect((int) (currentTime_ * Project.getInstance().getZoom() + timeTrack_.getNameP().getWidth() - this.getTimeScrollBar().getValue() * Project.getInstance().getZoom())-1, this.getTimeScrollBar().getHeight() + 27, 3, this.getHeight());

			Polygon poly = new Polygon();
			poly.addPoint((int) (currentTime_ * Project.getInstance().getZoom()
					+ timeTrack_.getNameP().getWidth() - this
					.getTimeScrollBar().getValue()
					* Project.getInstance().getZoom()) - 9, this
					.getTimeScrollBar().getHeight() + 13);
			poly.addPoint((int) (currentTime_ * Project.getInstance().getZoom()
					+ timeTrack_.getNameP().getWidth() - this
					.getTimeScrollBar().getValue()
					* Project.getInstance().getZoom()), this.getTimeScrollBar()
					.getHeight() + 27);
			poly.addPoint((int) (currentTime_ * Project.getInstance().getZoom()
					+ timeTrack_.getNameP().getWidth() - this
					.getTimeScrollBar().getValue()
					* Project.getInstance().getZoom()) + 9, this
					.getTimeScrollBar().getHeight() + 13);

			g2.fillPolygon(poly);

			if (timeTrack_.getTimetrackP().showMarkerTime) {
				g2.setColor(Color.BLUE);
				switch (timeType_) {
				case MILLIS:
					g2.drawString(String.valueOf(currentTime_),
							(int) (currentTime_
									* Project.getInstance().getZoom()
									+ timeTrack_.getNameP().getWidth() - this
									.getTimeScrollBar().getValue()
									* Project.getInstance().getZoom()) + 9,
							this.getTimeScrollBar().getHeight() + 27);
					break;
				case HHmmssSS:
					hour = ((currentTime_) % (24 * 60 * 60 * 1000))
							/ (60 * 60 * 1000);
					min = ((currentTime_) % (60 * 60 * 1000)) / (60 * 1000);
					sec = ((currentTime_) % (60 * 1000)) / (1000);
					ms = ((currentTime_) % (1000));
					g2.drawString(
							formatter2_.format(hour) + ":"
									+ formatter2_.format(min) + ":"
									+ formatter2_.format(sec) + ":"
									+ formatter3_.format(ms),
							(int) (currentTime_
									* Project.getInstance().getZoom()
									+ timeTrack_.getNameP().getWidth() - this
									.getTimeScrollBar().getValue()
									* Project.getInstance().getZoom()) + 9,
							this.getTimeScrollBar().getHeight() + 27);
					break;
				case HHmmssff:
					frameDuration = (int) ((1000.0 / Project.getInstance()
							.getProjectFPS()));
					hour = ((currentTime_) % (24 * 60 * 60 * 1000))
							/ (60 * 60 * 1000);
					min = ((currentTime_) % (60 * 60 * 1000)) / (60 * 1000);
					sec = ((currentTime_) % (60 * 1000)) / (1000);
					ms = (long) (((currentTime_) % (1000)) / (1000.0 / Project
							.getInstance().getProjectFPS())) + 1;
					g2.drawString(
							formatter2_.format(hour) + ":"
									+ formatter2_.format(min) + ":"
									+ formatter2_.format(sec) + ":"
									+ formatter2_.format(ms),
							(int) (currentTime_
									* Project.getInstance().getZoom()
									+ timeTrack_.getNameP().getWidth() - this
									.getTimeScrollBar().getValue()
									* Project.getInstance().getZoom()) + 9,
							this.getTimeScrollBar().getHeight() + 27);
					break;
				case FRAMES:
					frameDuration = (int) ((1000.0 / Project.getInstance()
							.getProjectFPS()));
					g2.drawString(String
							.valueOf((int) (currentTime_ / frameDuration) + 1),
							(int) (currentTime_
									* Project.getInstance().getZoom()
									+ timeTrack_.getNameP().getWidth() - this
									.getTimeScrollBar().getValue()
									* Project.getInstance().getZoom()) + 9,
							this.getTimeScrollBar().getHeight() + 27);
					break;
				// g2.setColor(Color.black);
				}
			}
		}
		// TODO
		if (activeLabel_ != null) {
			g2.setColor(AtlasProperties.getInstance().getSelectionColor());
			// vordere
			x = (int) (activeLabel_.getStart()
					* Project.getInstance().getZoom()
					+ timeTrack_.getNameP().getWidth() - this
					.getTimeScrollBar().getValue()
					* Project.getInstance().getZoom());
			if (x > timeTrack_.getNameP().getWidth()) {

				g2.fillRect((int) (activeLabel_.getStart()
						* Project.getInstance().getZoom()
						+ timeTrack_.getNameP().getWidth() - this
						.getTimeScrollBar().getValue()
						* Project.getInstance().getZoom()), this
						.getTimeScrollBar().getHeight() + 20, 1, this
						.getHeight());
				if (timeTrack_.getTimetrackP().showMarkerTime) {

					switch (timeType_) {
					case MILLIS:
						g2.drawString(
								String.valueOf(activeLabel_.getStart()),
								(int) (activeLabel_.getStart()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom()) + 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					case HHmmssSS:
						hour = ((activeLabel_.getStart()) % (24 * 60 * 60 * 1000))
								/ (60 * 60 * 1000);
						min = ((activeLabel_.getStart()) % (60 * 60 * 1000))
								/ (60 * 1000);
						sec = ((activeLabel_.getStart()) % (60 * 1000)) / (1000);
						ms = ((activeLabel_.getStart()) % (1000));
						g2.drawString(
								formatter2_.format(hour) + ":"
										+ formatter2_.format(min) + ":"
										+ formatter2_.format(sec) + ":"
										+ formatter3_.format(ms),
								(int) (activeLabel_.getStart()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom()) + 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					case HHmmssff:
						frameDuration = (int) ((1000.0 / Project.getInstance()
								.getProjectFPS()));
						hour = ((activeLabel_.getStart()) % (24 * 60 * 60 * 1000))
								/ (60 * 60 * 1000);
						min = ((activeLabel_.getStart()) % (60 * 60 * 1000))
								/ (60 * 1000);
						sec = ((activeLabel_.getStart()) % (60 * 1000)) / (1000);
						ms = (long) (((activeLabel_.getStart()) % (1000)) / (1000.0 / Project
								.getInstance().getProjectFPS())) + 1;
						g2.drawString(
								formatter2_.format(hour) + ":"
										+ formatter2_.format(min) + ":"
										+ formatter2_.format(sec) + ":"
										+ formatter2_.format(ms),
								(int) (activeLabel_.getStart()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom()) + 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					case FRAMES:
						frameDuration = (int) ((1000.0 / Project.getInstance()
								.getProjectFPS()));
						g2.drawString(
								String.valueOf((int) (activeLabel_.getStart() / frameDuration) + 1),
								(int) (activeLabel_.getStart()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom()) + 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					// g2.setColor(Color.black);
					}
				}
			}
			x = (int) (activeLabel_.getEnd() * Project.getInstance().getZoom()
					+ timeTrack_.getNameP().getWidth() - this
					.getTimeScrollBar().getValue()
					* Project.getInstance().getZoom());
			if (x > timeTrack_.getNameP().getWidth()&&!activeLabel_.isShowAsFlag()) {
				// hintere
                g2.fillRect((int) (activeLabel_.getEnd()
                        * Project.getInstance().getZoom()
                        + timeTrack_.getNameP().getWidth() - this
                        .getTimeScrollBar().getValue()
                        * Project.getInstance().getZoom()), this
                        .getTimeScrollBar().getHeight() + 20, 1, this
                        .getHeight());
				if (timeTrack_.getTimetrackP().showMarkerTime) {
					switch (timeType_) {
					case MILLIS:
						g2.drawString(
								String.valueOf(activeLabel_.getEnd()),
								(int) (activeLabel_.getEnd()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom()) + 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					case HHmmssSS:
						hour = ((activeLabel_.getEnd()) % (24 * 60 * 60 * 1000))
								/ (60 * 60 * 1000);
						min = ((activeLabel_.getEnd()) % (60 * 60 * 1000))
								/ (60 * 1000);
						sec = ((activeLabel_.getEnd()) % (60 * 1000)) / (1000);
						ms = ((activeLabel_.getEnd()) % (1000));
						g2.drawString(
								formatter2_.format(hour) + ":"
										+ formatter2_.format(min) + ":"
										+ formatter2_.format(sec) + ":"
										+ formatter3_.format(ms),
								(int) (activeLabel_.getEnd()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom()) + 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					case HHmmssff:
						frameDuration = (int) ((1000.0 / Project.getInstance()
								.getProjectFPS()));
						hour = ((activeLabel_.getEnd()) % (24 * 60 * 60 * 1000))
								/ (60 * 60 * 1000);
						min = ((activeLabel_.getEnd()) % (60 * 60 * 1000))
								/ (60 * 1000);
						sec = ((activeLabel_.getEnd()) % (60 * 1000)) / (1000);
						ms = (long) (((activeLabel_.getEnd()) % (1000)) / (1000.0 / Project
								.getInstance().getProjectFPS())) + 1;
						g2.drawString(
								formatter2_.format(hour) + ":"
										+ formatter2_.format(min) + ":"
										+ formatter2_.format(sec) + ":"
										+ formatter2_.format(ms),
								(int) (activeLabel_.getEnd()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom()) + 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					case FRAMES:
						frameDuration = (int) ((1000.0 / Project.getInstance()
								.getProjectFPS()));
						g2.drawString(
								String.valueOf((int) (activeLabel_.getEnd() / frameDuration) + 1),
								(int) (activeLabel_.getEnd()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom()) + 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					// g2.setColor(Color.black);
					}
				}
			}
		}
		if (timeTrack_.getTimetrackP().isUseLoopArea()) {
			g2.setColor(Color.cyan);
			// vordere
			x = (int) (timeTrack_.getTimetrackP().getLoopStart()
					* Project.getInstance().getZoom()
					+ timeTrack_.getNameP().getWidth() - this
					.getTimeScrollBar().getValue()
					* Project.getInstance().getZoom());
			if (x > timeTrack_.getNameP().getWidth()) {

				g2.fillRect((int) (timeTrack_.getTimetrackP().getLoopStart()
						* Project.getInstance().getZoom()
						+ timeTrack_.getNameP().getWidth() - this
						.getTimeScrollBar().getValue()
						* Project.getInstance().getZoom()), this
						.getTimeScrollBar().getHeight() + 20, 1, this
						.getHeight());
				if (timeTrack_.getTimetrackP().showMarkerTime) {
					fm = g2.getFontMetrics();
					switch (timeType_) {
					case MILLIS:
						g2.drawString(
								String.valueOf(timeTrack_.getTimetrackP()
										.getLoopStart()),
								(int) (timeTrack_.getTimetrackP()
										.getLoopStart()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom())
										- fm.stringWidth(String
												.valueOf(timeTrack_
														.getTimetrackP()
														.getLoopStart())) - 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					case HHmmssSS:
						hour = ((timeTrack_.getTimetrackP().getLoopStart()) % (24 * 60 * 60 * 1000))
								/ (60 * 60 * 1000);
						min = ((timeTrack_.getTimetrackP().getLoopStart()) % (60 * 60 * 1000))
								/ (60 * 1000);
						sec = ((timeTrack_.getTimetrackP().getLoopStart()) % (60 * 1000)) / (1000);
						ms = ((timeTrack_.getTimetrackP().getLoopStart()) % (1000));
						g2.drawString(
								formatter2_.format(hour) + ":"
										+ formatter2_.format(min) + ":"
										+ formatter2_.format(sec) + ":"
										+ formatter3_.format(ms),
								(int) (timeTrack_.getTimetrackP()
										.getLoopStart()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom())
										- fm.stringWidth(formatter2_
												.format(hour)
												+ ":"
												+ formatter2_.format(min)
												+ ":"
												+ formatter2_.format(sec)
												+ ":"
												+ formatter3_.format(ms)) - 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					case HHmmssff:
						frameDuration = (int) ((1000.0 / Project.getInstance()
								.getProjectFPS()));
						hour = ((timeTrack_.getTimetrackP().getLoopStart()) % (24 * 60 * 60 * 1000))
								/ (60 * 60 * 1000);
						min = ((timeTrack_.getTimetrackP().getLoopStart()) % (60 * 60 * 1000))
								/ (60 * 1000);
						sec = ((timeTrack_.getTimetrackP().getLoopStart()) % (60 * 1000)) / (1000);
						ms = (long) (((timeTrack_.getTimetrackP()
								.getLoopStart()) % (1000)) / (1000.0 / Project
								.getInstance().getProjectFPS())) + 1;
						g2.drawString(
								formatter2_.format(hour) + ":"
										+ formatter2_.format(min) + ":"
										+ formatter2_.format(sec) + ":"
										+ formatter2_.format(ms),
								(int) (timeTrack_.getTimetrackP()
										.getLoopStart()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom())
										- fm.stringWidth(formatter2_
												.format(hour)
												+ ":"
												+ formatter2_.format(min)
												+ ":"
												+ formatter2_.format(sec)
												+ ":"
												+ formatter2_.format(ms)) - 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					case FRAMES:
						frameDuration = (int) ((1000.0 / Project.getInstance()
								.getProjectFPS()));
						g2.drawString(
								String.valueOf((int) (timeTrack_
										.getTimetrackP().getLoopStart() / frameDuration) + 1),
								(int) (timeTrack_.getTimetrackP()
										.getLoopStart()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom())
										- fm.stringWidth(String
												.valueOf((int) (timeTrack_
														.getTimetrackP()
														.getLoopStart() / frameDuration) + 1))
										- 9, this.getTimeScrollBar()
										.getHeight() + 27);
						break;
					// g2.setColor(Color.black);
					}
				}
			}
			x = (int) (timeTrack_.getTimetrackP().getLoopEnd()
					* Project.getInstance().getZoom()
					+ timeTrack_.getNameP().getWidth() - this
					.getTimeScrollBar().getValue()
					* Project.getInstance().getZoom());
			if (x > timeTrack_.getNameP().getWidth()) {
				// hintere
				g2.fillRect((int) (timeTrack_.getTimetrackP().getLoopEnd()
						* Project.getInstance().getZoom()
						+ timeTrack_.getNameP().getWidth() - this
						.getTimeScrollBar().getValue()
						* Project.getInstance().getZoom()), this
						.getTimeScrollBar().getHeight() + 20, 1, this
						.getHeight());
				if (timeTrack_.getTimetrackP().showMarkerTime) {
					switch (timeType_) {
					case MILLIS:
						g2.drawString(String.valueOf(timeTrack_.getTimetrackP()
								.getLoopEnd()), (int) (timeTrack_
								.getTimetrackP().getLoopEnd()
								* Project.getInstance().getZoom()
								+ timeTrack_.getNameP().getWidth() - this
								.getTimeScrollBar().getValue()
								* Project.getInstance().getZoom()) + 9, this
								.getTimeScrollBar().getHeight() + 27);
						break;
					case HHmmssSS:
						hour = ((timeTrack_.getTimetrackP().getLoopEnd()) % (24 * 60 * 60 * 1000))
								/ (60 * 60 * 1000);
						min = ((timeTrack_.getTimetrackP().getLoopEnd()) % (60 * 60 * 1000))
								/ (60 * 1000);
						sec = ((timeTrack_.getTimetrackP().getLoopEnd()) % (60 * 1000)) / (1000);
						ms = ((timeTrack_.getTimetrackP().getLoopEnd()) % (1000));
						g2.drawString(
								formatter2_.format(hour) + ":"
										+ formatter2_.format(min) + ":"
										+ formatter2_.format(sec) + ":"
										+ formatter3_.format(ms),
								(int) (timeTrack_.getTimetrackP().getLoopEnd()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom()) + 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					case HHmmssff:
						frameDuration = (int) ((1000.0 / Project.getInstance()
								.getProjectFPS()));
						hour = ((timeTrack_.getTimetrackP().getLoopEnd()) % (24 * 60 * 60 * 1000))
								/ (60 * 60 * 1000);
						min = ((timeTrack_.getTimetrackP().getLoopEnd()) % (60 * 60 * 1000))
								/ (60 * 1000);
						sec = ((timeTrack_.getTimetrackP().getLoopEnd()) % (60 * 1000)) / (1000);
						ms = (long) (((activeLabel_.getEnd()) % (1000)) / (1000.0 / Project
								.getInstance().getProjectFPS())) + 1;
						g2.drawString(
								formatter2_.format(hour) + ":"
										+ formatter2_.format(min) + ":"
										+ formatter2_.format(sec) + ":"
										+ formatter2_.format(ms),
								(int) (timeTrack_.getTimetrackP().getLoopEnd()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom()) + 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					case FRAMES:
						frameDuration = (int) ((1000.0 / Project.getInstance()
								.getProjectFPS()));
						g2.drawString(
								String.valueOf((int) (timeTrack_
										.getTimetrackP().getLoopEnd() / frameDuration) + 1),
								(int) (timeTrack_.getTimetrackP().getLoopEnd()
										* Project.getInstance().getZoom()
										+ timeTrack_.getNameP().getWidth() - this
										.getTimeScrollBar().getValue()
										* Project.getInstance().getZoom()) + 9,
								this.getTimeScrollBar().getHeight() + 27);
						break;
					// g2.setColor(Color.black);
					}
				}
			}
		}

	}

    public int getNameLabelWidth() {
        return this.timeTrack_.getNameP().getWidth();
    }

   // @Override
	public void mouseDragged(MouseEvent e) {

		if (this.mouseButton_ == MouseEvent.BUTTON1
				&& this.getCursor().getType() == Cursor.DEFAULT_CURSOR
				&& olines_.size() > 0
				&& e.getX() > olines_.get(0).getNameP().getWidth()) {
			currentTime_ = (int) ((((e.getX() - timeTrack_.getNameP()
					.getWidth()) / Project.getInstance().getZoom()) + this
					.getTimeScrollBar().getValue()));
			if (Project.getInstance() != null
					&& currentTime_ > Project.getInstance().getProjectLength()) {
				currentTime_ = (long) Project.getInstance().getProjectLength();
			}
			MessageManager.getInstance().timeChanged(
					new TimeEvent(this, currentTime_));

			this.repaint();
		} else if (this.mouseButton_ == MouseEvent.BUTTON1
				&& this.getCursor().getType() == Cursor.E_RESIZE_CURSOR
				&& e.getX() >= 100) {
			timeTrack_.getTimetrackP().setBounds(e.getX() + 1, this.getY(),
					timeTrack_.getTimetrackP().getWidth(),
					timeTrack_.getTimetrackP().getHeight());

			timeTrack_.getNameP().setBounds(1, 1, e.getX(),
					timeTrack_.getHeight());
			timeTrack_.getNameP().setMinimumSize(
					new Dimension(e.getX(), (int) timeTrack_.getNameP()
							.getMinimumSize().getHeight()));
			timeTrack_.getNameP().setPreferredSize(
					new Dimension(e.getX(), (int) timeTrack_.getNameP()
							.getPreferredSize().getHeight()));
			timeTrack_.getNameP().setMaximumSize(
					new Dimension(e.getX(), (int) timeTrack_.getNameP()
							.getMaximumSize().getHeight()));
			for (int i = 0; i < olines_.size(); i++) {
				olines_.get(i)
						.getNameP()
						.setMinimumSize(
								new Dimension(e.getX(), (int) olines_.get(i)
										.getNameP().getMinimumSize()
										.getHeight()));
				olines_.get(i)
						.getNameP()
						.setPreferredSize(
								new Dimension(e.getX(), (int) olines_.get(i)
										.getNameP().getPreferredSize()
										.getHeight()));
				olines_.get(i)
						.getNameP()
						.setMaximumSize(
								new Dimension(e.getX(), (int) olines_.get(i)
										.getNameP().getMaximumSize()
										.getHeight()));
				olines_.get(i).getNameP()
						.setBounds(1, 1, e.getX(), olines_.get(i).getHeight());
				// olines_.get(i).repaint();
			}
			MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
		}
	}

//	@Override
	public void mouseMoved(MouseEvent e) {
		if (e.getX() <= timeTrack_.getNameP().getWidth()
				&& e.getX() >= timeTrack_.getNameP().getWidth() - 5) {
			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		} else {
			setCursor(Cursor.getDefaultCursor());
		}
	}

//	@Override
	public void mouseClicked(MouseEvent e) {
		timeTrack_.getZoominBtn().requestFocus();
	}

//	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

//	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

//	@Override
	public void mousePressed(MouseEvent e) {
		this.mouseButton_ = e.getButton();

		if (e.getButton() == MouseEvent.BUTTON3) {
			Component tmp = e.getComponent().getComponentAt(e.getPoint());
			Point p = e.getPoint();
			p.x -= tmp.getX();
			p.y -= tmp.getY();
			tmp = tmp.getComponentAt(p);
			p.x -= tmp.getX();
			p.y -= tmp.getY();
			tmp = tmp.getComponentAt(p);

			if (tmp instanceof TimeTrack) {
				((TimeTrack) tmp).mousePressed(new MouseEvent(tmp, e.getID(), e
						.getWhen(), e.getModifiers(), p.x, p.y, e
						.getClickCount(), true));
			} else if (tmp instanceof ScalarTrack) {
				((ScalarTrack) tmp).mousePressed(new MouseEvent(tmp, e.getID(),
						e.getWhen(), e.getModifiers(), p.x, p.y, e
								.getClickCount(), true));
			} else if (tmp instanceof VectorTrack) {
				((VectorTrack) tmp).mousePressed(new MouseEvent(tmp, e.getID(),
						e.getWhen(), e.getModifiers(), p.x, p.y, e
								.getClickCount(), true));
			} else if (tmp instanceof DataTrack) {
				((DataTrack) tmp).mousePressed(new MouseEvent(tmp, e.getID(),
						e.getWhen(), e.getModifiers(), p.x, p.y, e
						.getClickCount(), true));
			}
		} else if (e.getButton() == MouseEvent.BUTTON1
				&& e.getX() > timeTrack_.getNameP().getWidth()) {

			currentTime_ = (int) ((((e.getX() - timeTrack_.getNameP()
					.getWidth()) / Project.getInstance().getZoom()) + this.timeScrollBar_
					.getValue()));

			MessageManager.getInstance().timeChanged(
					new TimeEvent(this, currentTime_));
		}
		this.repaint();

	}

//	@Override
	public void mouseReleased(MouseEvent e) {
        this.mouseButton_ = 0;

	}

	public void updateViewport() {
		vertScrollPane_.setViewportView(scrollBox_);
	}

	private void setTime(long time) {
		currentTime_ = time;
		this.repaint();
	}

	public void removeTrack(ObjectLine ol) {

		for (int i = olines_.indexOf(ol) + 1; i < olines_.size(); i++) {
			olines_.get(i).setOrder(olines_.get(i).getOrder() - 1);
		}
		olines_.remove(ol);
		scrollBox_.remove(ol);
		this.paintMe();
	}

//	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {

		if (e.getWheelRotation() == -1) {
			timeTrack_.zoomIn();
		} else {
			timeTrack_.zoomOut();
		}

	}

	public int getOlinesSize() {
		if (olines_ != null) {
			return olines_.size();
		}
		return 0;
	}

	@Override
	public void dragEnter(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragExit(DropTargetEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragOver(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent dtde) {
		try {
			// Ok, get the dropped object and try to figure out what it is
			Transferable tr = dtde.getTransferable();
			DataFlavor[] flavors = tr.getTransferDataFlavors();
			if (isFileFlavor(flavors)) {
				List<File> droppedFiles = null;
				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

				if (System.getProperty("os.name").startsWith("Windows")) {
					droppedFiles = (List<File>) tr
							.getTransferData(fileDataFlavor_);
				} else {

					// if (System.getProperty("os.name").startsWith("Linux")) {
					String urls = (String) tr.getTransferData(fileDataFlavor_);
					droppedFiles = new LinkedList<File>();
					StringTokenizer tokens = new StringTokenizer(urls);
					while (tokens.hasMoreTokens()) {
						String urlString = tokens.nextToken();
						URL url = new URL(urlString);
						try {
							droppedFiles.add(new File(URLDecoder.decode(
									url.getFile(), "UTF-8")));
						} catch (UnsupportedEncodingException uee) {
							System.err
									.println("Drag & Drop with unsupported encoding");
						}
					}
				}
				importDnDFiles(droppedFiles);
				dtde.dropComplete(true);
				return;
			}
			dtde.rejectDrop();
		} catch (Exception e) {
			e.printStackTrace();
			dtde.rejectDrop();
		}
	}

	private boolean isFileFlavor(DataFlavor[] flavors) {
		for (int i = 0; i < flavors.length; i++) {

			if (fileDataFlavor_.equals(flavors[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {

	}

	private void importDnDFiles(List<File> droppedFiles) {

		Iterator<File> it = droppedFiles.iterator();
		while (it.hasNext()) {
			File droppedFile = it.next();
			if (!droppedFile.isFile() || !droppedFile.exists()) {
				// isFile to not accept directories, !exists should not be
				// possible
				continue;
			}
			String droppedFilePath = droppedFile.getPath();
			if (droppedFilePath.endsWith(".wav")
					&& Project.getInstance().isExisting()) {
				ImportAudio.showDialog(this, "Import an AudioTrack", Project
						.getInstance().getMcoll(), Project.getInstance()
						.getLcoll(), droppedFilePath);
			} else if ((droppedFilePath.endsWith(".avi")
					|| droppedFilePath.endsWith(".mp4")
					|| droppedFilePath.endsWith(".m4v")
					|| droppedFilePath.endsWith(".mpg")
					|| droppedFilePath.endsWith(".mpeg")
					|| droppedFilePath.endsWith(".wmv")
					|| droppedFilePath.endsWith(".mov") || droppedFilePath
					.endsWith(".FLV")) && Project.getInstance().isExisting()) {
				ImportVideo.showDialog(this, "Import a VideoTrack", Project
						.getInstance().getMcoll(), droppedFilePath);
			} else if (droppedFilePath.endsWith(".xml")) {
				SAXBuilder sxbuild = new SAXBuilder();
				InputSource is = null;
				try {
					is = new InputSource(new FileInputStream(droppedFilePath));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				Document doc;
				try {
					doc = sxbuild.build(is);
					Element root = doc.getRootElement();
					if (root.getName().equalsIgnoreCase("AnnotationProject")) {
						// wie zur hecke komme ich von hier aus da dran?

						/*
						 * if
						 * (!playButton.isSelected()||!this.loopLabelToggleButton
						 * .isSelected()) {
						 * this.loopLabelToggleButton.setEnabled(true);
						 * this.stopButton.setEnabled(true);
						 * this.prevFrameButton.setEnabled(true);
						 * this.nextFrameButton.setEnabled(true);
						 * guiThread.setPlaying(false);
						 * guiThread.setLooping(false);
						 * playButton.setSelected(false);
						 * loopLabelToggleButton.setSelected(false);
						 * 
						 * Project.getInstance().getMcoll().pause(); }
						 */
						if (Project.getInstance().isExisting()) {
							Object[] options = { "yes", "no", "cancel" };
							int n = JOptionPane.showOptionDialog(null,
									"Do you want to save the current project?",
									"Save Project?",
									JOptionPane.YES_NO_CANCEL_OPTION,
									JOptionPane.QUESTION_MESSAGE, null,
									options, options[0]);
							if (n == 2) {// cancel
								return;
							} else if (n == 0) {// yes
								Project.getInstance().saveProject();
							}
						}
						Project.getInstance().getLcoll().reset();
						Project.getInstance().getMcoll().reset();
						LabelClasses.getInstance().clear();
						MessageManager.getInstance().requestTrackUpdate(
								new UpdateTracksEvent(this));
						MessageManager.getInstance().requestRepaint(
								new RepaintEvent(this));
						Project.getInstance().loadProject(droppedFilePath,false);
					} else if (root.getName().equalsIgnoreCase("LabelTrack")
							&& Project.getInstance().isExisting()) {
						ImportLabelTrack.showDialog(this,
								"Add a LabelTrack to Collection", Project
										.getInstance().getLcoll(),
								droppedFilePath);
					} else if (root.getName().equalsIgnoreCase("LabelClass")
							&& Project.getInstance().isExisting()) {
						LabelClass tmp;
						tmp = new LabelClass(droppedFile);
						LabelClasses.getInstance().addClass(tmp);
						MessageManager.getInstance().requestClassChanged(
								new ClassChangedEvent(this));
					} else if (root.getName().equalsIgnoreCase("VectorTrack")
							&& Project.getInstance().isExisting()) {
						ImportVectorTrack.showDialog(this,
								"Add a VectorTrack to Collection", Project
										.getInstance().getLcoll(),
								droppedFilePath);
					} else if (root.getName().equalsIgnoreCase("ScalarTrack")
							&& Project.getInstance().isExisting()) {
						ImportScalarTrack.showDialog(this,
								"Add a ScalarTrack to Collection", Project
										.getInstance().getLcoll(),
								droppedFilePath);
					} 

				} catch (JDOMException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else if (droppedFilePath.endsWith(".mat")
					&& Project.getInstance().isExisting()) {
				try {
					// int i=0;
					MatFileReader mfr = new MatFileReader(droppedFilePath);
					Map<String, MLArray> datamap = mfr.getContent();
					MLDouble data = (MLDouble) datamap.values().iterator()
							.next();
					if (data.getM() == 2) {
						ImportScalarTrack.showDialog(this,
								"Add a ScalarTrack to Collection", Project
										.getInstance().getLcoll(),
								droppedFilePath);
					} else {
						ImportVectorTrack.showDialog(this,
								"Add a VectorTrack to Collection", Project
										.getInstance().getLcoll(),
								droppedFilePath);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if ((droppedFilePath.endsWith(".nex")
					|| droppedFilePath.endsWith(".nex_ul") || droppedFilePath
					.endsWith(".nex_md")) && Project.getInstance().isExisting()) {
				ImportScalarTrack.showDialog(this,
						"Add a ScalarTrack to Collection", Project
								.getInstance().getLcoll(), droppedFilePath);
			} else if ((droppedFilePath.endsWith(".flk")
					|| droppedFilePath.endsWith(".DSVa")
					|| droppedFilePath.endsWith(".pres")
					|| droppedFilePath.endsWith(".trig")
					|| droppedFilePath.endsWith(".avec_visual") || droppedFilePath
					.endsWith(".avec_audio"))
					&& Project.getInstance().isExisting()) {
				ImportLabelTrack.showDialog(this,
						"Add a LabelTrack to Collection", Project.getInstance()
								.getLcoll(), droppedFilePath);
			}
		}

	}

	public ObjectLine getTimeTrack_() {
		return timeTrack_;
	}

	public void removeTrack(String name){
		for(int i=0;i<this.olines_.size();i++){
			if(this.olines_.get(i).getName().equalsIgnoreCase(name))this.removeTrack(this.olines_.get(i));
		}
	}

	public LabelObject getActiveLabel() {
		return this.activeLabel_;
	}

	public JScrollBar getTimeScrollBar() {
		return timeScrollBar_;
	}

	public boolean hasTrackWithName(String name) {
		boolean found = false;
		Iterator<ObjectLine> oi = olines_.iterator();
		while (oi.hasNext()){
			if(oi.next().getName().equalsIgnoreCase(name))found=true;
		}
		return found;
	}

	public ObjectLine getTrackByName(String name) {
		Iterator<ObjectLine> oi = olines_.iterator();
		while (oi.hasNext()){
			ObjectLine o= oi.next();
			if(o.getName().equalsIgnoreCase(name))return o;
		}
		return null;
	}
}

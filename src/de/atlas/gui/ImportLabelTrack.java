package de.atlas.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;

import de.atlas.collections.LabelTrack;
import de.atlas.collections.LineCollection;
import de.atlas.collections.ObjectLine;
import de.atlas.exceptions.LabelClassNotFoundException;
import de.atlas.misc.HelperFunctions;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import de.atlas.data.LabelClass;
import de.atlas.data.LabelClassEntity;
import de.atlas.data.LabelClasses;
import de.atlas.data.LabelObject;
import de.atlas.data.LabelType;
import de.atlas.data.Project;
import de.atlas.messagesystem.ClassChangedEvent;
import de.atlas.messagesystem.LabelSelectionEvent;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.UpdateTracksEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ImportLabelTrack extends JFileChooser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private LineCollection lcoll;

	JFileChooser fc = null;

	/**
	 * Create the dialog.
	 */
	public ImportLabelTrack(String path) {
		super(path);
	}

	public static void showDialog(Component owner, String title,
			LineCollection lcoll, String path) {

		ImportLabelTrack dialog = new ImportLabelTrack(path);
		dialog.lcoll = lcoll;

		dialog.setVisible(true);

		FileFilter ff = new FileNameExtensionFilter(
				"XML/FLK/DSVa/pres/trig/avec_visual/avec_audio", "xml", "flk",
				"DSVa", "pres", "trig", "avec_visual", "avec_audio");
		dialog.setFileFilter(ff);
		dialog.setSelectedFile(new File(path));
		int returnVal = dialog.showOpenDialog(owner);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			dialog.addTrack();
		}

	}

	@SuppressWarnings("unchecked")
	private void addTrack() {
		if (this.getSelectedFile().getPath().endsWith(".xml")) {
			try {
				LabelTrack tmp;
				tmp = new LabelTrack(this.getSelectedFile());
				Project.getInstance().getLcoll().addLabelTrack(tmp, true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);
				Project.getInstance().setProjectLength(tmp.getLength());
            } catch (LabelClassNotFoundException e) {
				JOptionPane.showMessageDialog(null,
						"The assoziated LabelClass was not found.",
						e.getMessage(), JOptionPane.ERROR_MESSAGE);
			}
		} else if (this.getSelectedFile().getPath().endsWith(".flk")) {

			if (LabelClasses.getInstance().getClassByName("GATT") == null) {
				LabelClasses.getInstance().addClass(new LabelClass("GATT"));
				LabelClass anf = LabelClasses.getInstance().getClassByName(
						"GATT");
				anf.addEntity(new LabelClassEntity(anf, "word", 0, new Color(-10040065), Color.darkGray));
				anf.addEntity(new LabelClassEntity(anf, "pause", 1, new Color(-16711885), Color.darkGray));
				anf.addEntity(new LabelClassEntity(anf, "breathe", 2, new Color(-52429), Color.darkGray));
				anf.addEntity(new LabelClassEntity(anf, "non-phonological", 3, new Color(-3407668), Color.darkGray));
				anf.addEntity(new LabelClassEntity(anf, "unparsed", 4, new Color(-1713050), Color.darkGray));
			}
			HashMap<String, Double> pauses = new HashMap<String, Double>();
			pauses.put("micro", 100.0);
			pauses.put("short", 200.0);
			pauses.put("medium", 500.0);
			pauses.put("long", 800.0);

			SAXBuilder sxbuild = new SAXBuilder();
			InputSource is = null;
			try {
				is = new InputSource(new FileInputStream(this.getSelectedFile()
						.getPath()));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			Document doc;
			HashMap<String, LabelTrack> tracks = new HashMap<String, LabelTrack>();
			HashMap<String, Double> times = new HashMap<String, Double>();
			try {
				doc = sxbuild.build(is);
				Element root = doc.getRootElement();
				List<Element> speakerList = ((List<Element>) root.getChild(
						"speakers").getChildren("speaker"));
				for (Element speaker : speakerList) {
					// File file;
					if ((HelperFunctions.testAndGenerateFile(Project
							.getInstance().getProjectPath()
							+ "labeltracks/"
							+ this.getSelectedFile()
									.getName()
									.substring(
											0,
											this.getSelectedFile().getName()
													.length() - 4)
							+ "_"
							+ speaker.getChild("name").getValue() + ".xml")) == null) {
						return;
					}
				}
				for (Element speaker : speakerList) {
					tracks.put(
							speaker.getAttribute("speaker-id").getValue(),
							new LabelTrack(Project.getInstance().getLcoll()
									.getWidth(), LabelClasses.getInstance()
									.getClassByName("GATT"), this
									.getSelectedFile()
									.getName()
									.substring(
											0,
											this.getSelectedFile().getName()
													.length() - 4)
									+ "_" + speaker.getChild("name").getValue()));
				}
				// add a meta information track
				tracks.put(
						"meta",
						new LabelTrack(Project.getInstance().getLcoll()
								.getWidth(), LabelClasses.getInstance()
								.getClassByName("GATT"), this
								.getSelectedFile()
								.getName()
								.substring(
										0,
										this.getSelectedFile().getName()
												.length() - 4)
								+ "_meta"));
				List<Element> timepointList = ((List<Element>) root.getChild(
						"timeline").getChildren("timepoint"));
				for (Element timepoint : timepointList) {
					times.put(
							timepoint.getAttribute("timepoint-id").getValue(),
							timepoint.getAttribute("absolute-time")
									.getDoubleValue() * 1000.0);
				}
				List<Element> contributionList = ((List<Element>) root
						.getChildren("contribution"));
				for (Element contribution : contributionList) {
					LabelTrack actualTrack;
					if (contribution.getAttribute("speaker-reference") != null) {
						actualTrack = tracks.get(contribution.getAttribute(
								"speaker-reference").getValue());
					} else {
						actualTrack = tracks.get("meta");
					}
					double starttime = times.get(contribution.getAttribute(
							"start-reference").getValue());
					double endtime = times.get(contribution.getAttribute(
							"end-reference").getValue());
					ArrayList<Double> tli = new ArrayList<Double>();
					tli.add(starttime);
					List<Element> timeList = ((List<Element>) contribution
							.getChildren("time"));
					for (Element time : timeList) {
						tli.add(times.get(time.getAttribute(
								"timepoint-reference").getValue()));
					}
					tli.add(endtime);

					ArrayList<LabelObject> labels = new ArrayList<LabelObject>();
					List<Element> dataList = ((List<Element>) contribution
							.getChildren());
					for (Element data : dataList) {

						// public LabelObject(String txt, String cmt, long
						// start, long end,double val, LabelType type,
						// LabelClass lc, LabelClassEntity lce, long timestamp){
						if (data.getName().equals("w")) {
							labels.add(new LabelObject(data.getValue(), "", 0,
									0, 1.0, LabelType.MANUAL, LabelClasses
											.getInstance().getClassByName(
													"GATT"), LabelClasses
											.getInstance()
											.getClassByName("GATT")
											.getEntityByName("word"), System
											.currentTimeMillis()));
						} else if (data.getName().equals("non-phonological")) {
							labels.add(new LabelObject(
									data.getAttribute("description").getValue(),
									"",
									0,
									0,
									1.0,
									LabelType.MANUAL,
									LabelClasses.getInstance().getClassByName(
											"GATT"),
									LabelClasses
											.getInstance()
											.getClassByName("GATT")
											.getEntityByName("non-phonological"),
									System.currentTimeMillis()));
						} else if (data.getName().equals("pause")) {
							String text = "";
							double duration = 0.0;
							if (pauses.get(data.getAttribute("duration")
									.getValue()) != null) {
								text = data.getAttribute("duration").getValue();
								duration = pauses.get(data.getAttribute(
										"duration").getValue());
							} else {
								text = data.getAttribute("duration").getValue();
								duration = data.getAttribute("duration")
										.getDoubleValue() * 1000.0;

							}
							labels.add(new LabelObject(text, "", 0,
									(long) duration, 1.0, LabelType.MANUAL,
									LabelClasses.getInstance().getClassByName(
											"GATT"), LabelClasses.getInstance()
											.getClassByName("GATT")
											.getEntityByName("pause"), System
											.currentTimeMillis()));
						} else if (data.getName().equals("breathe")) {
							labels.add(new LabelObject("breathe: "
									+ data.getAttribute("type").getValue()
									+ " "
									+ data.getAttribute("length").getValue(),
									data.getAttribute("type").getValue(), 0, 0,
									data.getAttribute("length")
											.getDoubleValue(),
									LabelType.MANUAL, LabelClasses
											.getInstance().getClassByName(
													"GATT"), LabelClasses
											.getInstance()
											.getClassByName("GATT")
											.getEntityByName("breathe"), System
											.currentTimeMillis()));
						} else if (data.getName().equals("unparsed")) {
							List<Object> contentList = ((List<Object>) data
									.getContent());
							LabelObject tmp = null;
							double timer = tli.get(0);
							for (Object content : contentList) {

								if (content.getClass() == org.jdom.Text.class) {

									tmp = new LabelObject(
											((org.jdom.Text) content).getText(),
											"",
											0,
											0,
											1.0,
											LabelType.MANUAL,
											LabelClasses.getInstance()
													.getClassByName("GATT"),
											LabelClasses
													.getInstance()
													.getClassByName("GATT")
													.getEntityByName("unparsed"),
											System.currentTimeMillis());
								} else if (content.getClass() == org.jdom.Element.class) {

									if (tmp != null) {
										tmp.setStart((long) timer);
										timer = times.get(((Element) content)
												.getAttribute(
														"timepoint-reference")
												.getValue());
										tmp.setEnd((long) timer);
										actualTrack.addLabel(tmp);
										tmp = null;
									}
								}
							}
							if (tmp != null) {
								tmp.setStart((long) timer);
								tmp.setEnd((tli.get(1).longValue()));
								actualTrack.addLabel(tmp);
								tmp = null;
							}
						} else if (data.getName().equals("time")) {

							double blockLength = tli.get(1) - tli.get(0);

							int wordCount = 0;

							for (int i = 0; i < labels.size(); i++) {

								blockLength = blockLength
										- labels.get(i).getEnd();
								if (labels.get(i).getEnd() == 0) {
									wordCount++;
								}
							}

							blockLength = blockLength / (double) wordCount;

							double timeVal = tli.get(0);
							for (int i = 0; i < labels.size(); i++) {
								labels.get(i).setStart((long) timeVal);
								if (labels.get(i).getEnd() == 0) {
									timeVal += blockLength;
								} else {
									timeVal += labels.get(i).getEnd();
								}
								labels.get(i).setEnd((long) timeVal);
								actualTrack.addLabel(labels.get(i));

							}
							labels.clear();
							tli.remove(tli.get(0));
						}
					}
					if (labels.size() != 0) {

						double blockLength = tli.get(1) - tli.get(0);

						int wordCount = 0;

						for (int i = 0; i < labels.size(); i++) {

							blockLength = blockLength - labels.get(i).getEnd();
							if (labels.get(i).getEnd() == 0) {
								wordCount++;
							}
						}

						blockLength = blockLength / (double) wordCount;

						double timeVal = tli.get(0);
						for (int i = 0; i < labels.size(); i++) {
							labels.get(i).setStart((long) timeVal);
							if (labels.get(i).getEnd() == 0) {
								timeVal += blockLength;
							} else {
								timeVal += labels.get(i).getEnd();
							}
							labels.get(i).setEnd((long) timeVal);
							actualTrack.addLabel(labels.get(i));

						}
						labels.clear();
						tli.remove(tli.get(0));
					}
				}

				Iterator<LabelTrack> iL = tracks.values().iterator();
				while (iL.hasNext()) {
					Project.getInstance()
							.getLcoll()
							.addLabelTrack(iL.next(), true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);
				}
				MessageManager.getInstance().requestTrackUpdate(
						new UpdateTracksEvent(this));

			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (this.getSelectedFile().getPath().endsWith(".DSVa")) {
			if ((HelperFunctions
					.testAndGenerateFile(Project.getInstance().getProjectPath()
							+ "labeltracks/"
							+ this.getSelectedFile()
									.getName()
									.substring(
											0,
											this.getSelectedFile().getName()
													.length() - 5) + "_modul"
							+ ".xml")) == null) {
				return;
			}
			if ((HelperFunctions
					.testAndGenerateFile(Project.getInstance().getProjectPath()
							+ "labeltracks/"
							+ this.getSelectedFile()
									.getName()
									.substring(
											0,
											this.getSelectedFile().getName()
													.length() - 5) + "_rubrik"
							+ ".xml")) == null) {
				return;
			}
			if ((HelperFunctions
					.testAndGenerateFile(Project.getInstance().getProjectPath()
							+ "labeltracks/"
							+ this.getSelectedFile()
									.getName()
									.substring(
											0,
											this.getSelectedFile().getName()
													.length() - 5) + "_data"
							+ ".xml")) == null) {
				return;
			}
			RandomAccessFile in;
			try {
				in = new RandomAccessFile(this.getSelectedFile(), "r");

				if (LabelClasses.getInstance().getClassByName("DSVa") == null) {
					LabelClasses.getInstance().addClass(new LabelClass("DSVa"));
					LabelClass anf = LabelClasses.getInstance().getClassByName(
							"DSVa");
					anf.addEntity(new LabelClassEntity(anf, "none", 0,
							new Color(-3355444), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "wizard", 1,
							new Color(-6710785), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "erfolgreich", 2,
							new Color(-13382656), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "erfolglos", 3,
							new Color(-205), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "falsch", 4,
							new Color(-65536), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "rest", 5,
							new Color(-6710887), Color.darkGray));
				}

				LabelTrack modul = new LabelTrack(Project.getInstance()
						.getLcoll().getWidth(), LabelClasses.getInstance()
						.getClassByName("generic"), this
						.getSelectedFile()
						.getName()
						.substring(0,
								this.getSelectedFile().getName().length() - 5)
						+ "_modul");
				LabelTrack rubrik = new LabelTrack(Project.getInstance()
						.getLcoll().getWidth(), LabelClasses.getInstance()
						.getClassByName("generic"), this
						.getSelectedFile()
						.getName()
						.substring(0,
								this.getSelectedFile().getName().length() - 5)
						+ "_rubrik");
				LabelTrack data = new LabelTrack(Project.getInstance()
						.getLcoll().getWidth(), LabelClasses.getInstance()
						.getClassByName("DSVa"), this
						.getSelectedFile()
						.getName()
						.substring(0,
								this.getSelectedFile().getName().length() - 5)
						+ "_data");
				String line = "";

				int rubrikCount = 0;
				int modulCount = 0;
				int lastRubrikStart = 0;
				DateFormat sdf = new SimpleDateFormat("mm:ss");
				Calendar cal = new GregorianCalendar();
				ArrayList<Date> times = new ArrayList<Date>();
				in.readLine();// kopf abschneiden

				while ((line = in.readLine()) != null) {
					String[] vals = line.split(Pattern.quote(" "));
					// public LabelObject(String txt, String cmt, long start,
					// long end,double val, LabelType type, LabelClass lc,
					// LabelClassEntity lce, long timestamp)
					if (vals.length > 1) {
						try {
							times.add(sdf.parse(vals[0].substring(1,
									vals[0].length() - 1)));

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				times.add(new Date(
						times.get(times.size() - 1).getTime() + 10000));
				in.seek(0);
				in.readLine();// kopf abschneiden
				while ((line = in.readLine()) != null) {
					String[] vals = line.split(Pattern.quote(" "));
					// public LabelObject(String txt, String cmt, long start,
					// long end,double val, LabelType type, LabelClass lc,
					// LabelClassEntity lce, long timestamp)
					if (vals.length > 1) {
						rubrik.addLabel(new LabelObject("rubrik "
								+ (rubrikCount + 1), "", cal.getTimeZone()
								.getOffset(0)
								+ times.get(rubrikCount).getTime(), cal
								.getTimeZone().getOffset(0)
								+ times.get(rubrikCount + 1).getTime(), 1.0,
								LabelType.MANUAL, LabelClasses.getInstance()
										.getClassByName("generic"),
								LabelClasses.getInstance()
										.getClassByName("generic")
										.getEntityByName("labeled"), System
										.currentTimeMillis()));
						long starttime = cal.getTimeZone().getOffset(0)
								+ times.get(rubrikCount).getTime();
						long endtime = cal.getTimeZone().getOffset(0)
								+ times.get(rubrikCount + 1).getTime();
						long shift = (endtime - starttime) / (vals.length - 1);
						for (int i = 1; i < vals.length; i++) {

							data.addLabel(new LabelObject(vals[i], "",
									starttime, starttime + shift, 1.0,
									LabelType.MANUAL, LabelClasses
											.getInstance().getClassByName(
													"DSVa"), null, System
											.currentTimeMillis()));
							starttime += shift;
						}
						rubrikCount++;
					}
					if (vals.length == 1) {
						modul.addLabel(new LabelObject("modul "
								+ (modulCount + 1), "", cal.getTimeZone()
								.getOffset(0)
								+ times.get(lastRubrikStart).getTime(), cal
								.getTimeZone().getOffset(0)
								+ times.get(rubrikCount).getTime(), 1.0,
								LabelType.MANUAL, LabelClasses.getInstance()
										.getClassByName("generic"),
								LabelClasses.getInstance()
										.getClassByName("generic")
										.getEntityByName("labeled"), System
										.currentTimeMillis()));
						lastRubrikStart = rubrikCount;
						modulCount++;
					}

				}

				in.close();
				Project.getInstance().getLcoll()
						.addLabelTrack(modul, true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);
				Project.getInstance().getLcoll()
						.addLabelTrack(rubrik, true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);
				Project.getInstance().getLcoll()
						.addLabelTrack(data, true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);
				MessageManager.getInstance().requestTrackUpdate(
						new UpdateTracksEvent(this));

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (this.getSelectedFile().getPath().endsWith(".trig")) {
			RandomAccessFile in;
			try {
				in = new RandomAccessFile(this.getSelectedFile(), "r");
				String line = "";
				in.readLine();// KopfAbschneiden
				ArrayList<String> names = new ArrayList<String>();
				ArrayList<Double> start = new ArrayList<Double>();
				ArrayList<Double> end = new ArrayList<Double>();
				line = in.readLine();
				String[] vals = line.split(Pattern.quote(","));
				names.add(vals[4]); // name
				start.add(Double.parseDouble(vals[6]));// start
				while ((line = in.readLine()) != null) {
					vals = line.split(Pattern.quote(","));
					names.add(vals[4]); // name
					start.add(Double.parseDouble(vals[6]));// start
					end.add(Double.parseDouble(vals[7]));// end
				}
				end.add(start.get(start.size() - 1) + 1000);
				float s = 0.75f;
				float b = 1.0f;
				float h_step = 1.0f / 37.0f;
				int count = 1;
				if (LabelClasses.getInstance().getClassByName("Behavior") == null) {
					LabelClasses.getInstance().addClass(
							new LabelClass("Behavior"));
					LabelClass anf = LabelClasses.getInstance().getClassByName(
							"Behavior");
					anf.addEntity(new LabelClassEntity(anf, "five", 0, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "one", 1, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "leisb", 2, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "leiss", 3, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "first", 4, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "secon", 5, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "delay", 6, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "wrong", 7, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "hit", 8, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "miss", 9, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "abbru", 10, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "pos", 11, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "neg", 12, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "entsp", 13, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "two", 14, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "init", 15, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "ende", 16, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "start", 17, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "half", 18, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "abwol", 19, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "cog_pos_start",
							20, Color.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "cog_neg_start",
							21, Color.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "cog_ent_start",
							22, Color.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "stsp", 23, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "stpo", 24, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "enpo", 25, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "ensp", 26, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));

					anf.addEntity(new LabelClassEntity(anf, "vid1", 27, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "vid2", 28, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "vid3", 29, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "vid4", 30, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "vid5", 31, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "vid6", 32, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "vid7", 33, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "vid8", 34, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "vid9", 35, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "vid10", 36, Color
							.getHSBColor(h_step * count++, s, b), Color.darkGray));

				}

				LabelTrack behavior = new LabelTrack(Project.getInstance()
						.getLcoll().getWidth(), LabelClasses.getInstance()
						.getClassByName("Behavior"), this
						.getSelectedFile()
						.getName()
						.substring(0,
								this.getSelectedFile().getName().length() - 5)
						+ "_Behavior");

				for (int i = 0; i < names.size(); i++) {
					behavior.addLabel(new LabelObject(names.get(i), "", start
							.get(i).longValue(), end.get(i).longValue(), 1.0,
							LabelType.MANUAL, LabelClasses.getInstance()
									.getClassByName("Behavior"), LabelClasses
									.getInstance().getClassByName("Behavior")
									.getEntityByName(names.get(i)), System
									.currentTimeMillis()));
				}
				in.close();
				Project.getInstance().getLcoll()
						.addLabelTrack(behavior, true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (this.getSelectedFile().getPath().endsWith(".avec_visual")) {
			RandomAccessFile in;
			String name = this
					.getSelectedFile()
					.getPath()
					.substring(0,
							this.getSelectedFile().getPath().length() - 12);
			try {
				in = new RandomAccessFile(this.getSelectedFile(), "r");
				String line = "";
				double avec_video_rate = 49.979;
				LabelTrack track = new LabelTrack(Project.getInstance()
						.getLcoll().getWidth(), LabelClasses.getInstance()
						.getClassByName("generic"), this
						.getSelectedFile()
						.getName()
						.substring(0,
								this.getSelectedFile().getName().length() - 12));
				double prev = 0;
				long frame = 0;
				LabelObject tmp = null;
				while ((line = in.readLine()) != null) {
					// 0-1
					double val = Double.parseDouble(line);
					if (val == 1 && prev == 0) {
						name = name.split("/")[name.split("/").length - 1];
						tmp = new LabelObject(name.split("_")[3], "",
								(long) (frame * (1000.0 / avec_video_rate)), 0,
								1.0, LabelType.MANUAL, LabelClasses
										.getInstance()
										.getClassByName("generic"),
								LabelClasses.getInstance()
										.getClassByName("generic")
										.getEntityByName("labeled"),
								System.currentTimeMillis());
					}
					// 1-0
					if (val == 0 && prev == 1) {
						tmp.setEnd((long) (frame * (1000.0 / avec_video_rate)));
						track.addLabel(tmp);
						tmp = null;
					}
					frame++;
					prev = val;
				}
				if (tmp != null) {
					tmp.setEnd((long) (frame * (1000.0 / avec_video_rate)));
					track.addLabel(tmp);
				}
				in.close();
				Project.getInstance().getLcoll()
						.addLabelTrack(track, true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (this.getSelectedFile().getPath().endsWith(".avec_audio")) {
			RandomAccessFile in;
			RandomAccessFile inA;
			RandomAccessFile inE;
			RandomAccessFile inP;
			RandomAccessFile inV;
			// String name = this.getSelectedFile().getPath().substring(0,
			// this.getSelectedFile().getPath().length()-11);
			try {

				File arousalfile = new File(new File(this.getSelectedFile()
						.getParent()).getParent()
						+ "/labels/"
						+ "audio_labels_"
						+ this.getSelectedFile().getName().split("_")[0]
						+ this.getSelectedFile()
								.getName()
								.substring(
										16,
										this.getSelectedFile().getName()
												.length() - 11)
						+ "_arousal.dat");
				File expectancyfile = new File(new File(this.getSelectedFile()
						.getParent()).getParent()
						+ "/labels/"
						+ "audio_labels_"
						+ this.getSelectedFile().getName().split("_")[0]
						+ this.getSelectedFile()
								.getName()
								.substring(
										16,
										this.getSelectedFile().getName()
												.length() - 11)
						+ "_expectancy.dat");
				File powerfile = new File(new File(this.getSelectedFile()
						.getParent()).getParent()
						+ "/labels/"
						+ "audio_labels_"
						+ this.getSelectedFile().getName().split("_")[0]
						+ this.getSelectedFile()
								.getName()
								.substring(
										16,
										this.getSelectedFile().getName()
												.length() - 11) + "_power.dat");
				File valencefile = new File(new File(this.getSelectedFile()
						.getParent()).getParent()
						+ "/labels/"
						+ "audio_labels_"
						+ this.getSelectedFile().getName().split("_")[0]
						+ this.getSelectedFile()
								.getName()
								.substring(
										16,
										this.getSelectedFile().getName()
												.length() - 11)
						+ "_valence.dat");

				in = new RandomAccessFile(this.getSelectedFile(), "r");
				inA = new RandomAccessFile(arousalfile, "r");
				inE = new RandomAccessFile(expectancyfile, "r");
				inP = new RandomAccessFile(powerfile, "r");
				inV = new RandomAccessFile(valencefile, "r");
				String line = "";
				boolean beginTurn = false;
				long turnStart = 0;
				long turnEnd = 0;
				String turnName = "";
				LabelTrack turnTrack = new LabelTrack(Project.getInstance()
						.getLcoll().getWidth(), LabelClasses.getInstance()
						.getClassByName("generic"), this
						.getSelectedFile()
						.getName()
						.substring(0,
								this.getSelectedFile().getName().length() - 11)
						+ "_turns");
				LabelTrack wordTrack = new LabelTrack(Project.getInstance()
						.getLcoll().getWidth(), LabelClasses.getInstance()
						.getClassByName("generic"), this
						.getSelectedFile()
						.getName()
						.substring(0,
								this.getSelectedFile().getName().length() - 11)
						+ "_words");

				LabelTrack arousalTrack = new LabelTrack(Project.getInstance()
						.getLcoll().getWidth(), LabelClasses.getInstance()
						.getClassByName("generic"), this
						.getSelectedFile()
						.getName()
						.substring(0,
								this.getSelectedFile().getName().length() - 11)
						+ "_arousal");
				LabelTrack expectancyTrack = new LabelTrack(Project
						.getInstance().getLcoll().getWidth(), LabelClasses
						.getInstance().getClassByName("generic"), this
						.getSelectedFile()
						.getName()
						.substring(0,
								this.getSelectedFile().getName().length() - 11)
						+ "_expectancy");
				LabelTrack powerTrack = new LabelTrack(Project.getInstance()
						.getLcoll().getWidth(), LabelClasses.getInstance()
						.getClassByName("generic"), this
						.getSelectedFile()
						.getName()
						.substring(0,
								this.getSelectedFile().getName().length() - 11)
						+ "_power");
				LabelTrack valenceTrack = new LabelTrack(Project.getInstance()
						.getLcoll().getWidth(), LabelClasses.getInstance()
						.getClassByName("generic"), this
						.getSelectedFile()
						.getName()
						.substring(0,
								this.getSelectedFile().getName().length() - 11)
						+ "_valence");

				while ((line = in.readLine()) != null) {
					if (line.startsWith("-")) {
						beginTurn = true;
						turnName = line.replace("-", "");
					} else if (line.startsWith(".")) {
						turnTrack.addLabel(new LabelObject(turnName, "",
								turnStart, turnEnd, 1.0, LabelType.MANUAL,
								LabelClasses.getInstance().getClassByName(
										"generic"), LabelClasses.getInstance()
										.getClassByName("generic")
										.getEntityByName("labeled"), System
										.currentTimeMillis()));
					} else {
						String[] vals = line.split(" ");
						if (beginTurn) {
							beginTurn = false;
							turnStart = Long.parseLong(vals[0]);
						}
						if (vals.length >= 3) {
							wordTrack.addLabel(new LabelObject(vals[2], "",
									Long.parseLong(vals[0]), Long
											.parseLong(vals[1]), 1.0,
									LabelType.MANUAL, LabelClasses
											.getInstance().getClassByName(
													"generic"), LabelClasses
											.getInstance()
											.getClassByName("generic")
											.getEntityByName("labeled"), System
											.currentTimeMillis()));
							if (Double.parseDouble(inA.readLine()) == 1) {
								arousalTrack.addLabel(new LabelObject(
										"arousal", "", Long.parseLong(vals[0]),
										Long.parseLong(vals[1]), 1.0,
										LabelType.MANUAL, LabelClasses
												.getInstance().getClassByName(
														"generic"),
										LabelClasses.getInstance()
												.getClassByName("generic")
												.getEntityByName("labeled"),
										System.currentTimeMillis()));
							}
							if (Double.parseDouble(inE.readLine()) == 1) {
								expectancyTrack.addLabel(new LabelObject(
										"expectancy", "", Long
												.parseLong(vals[0]), Long
												.parseLong(vals[1]), 1.0,
										LabelType.MANUAL, LabelClasses
												.getInstance().getClassByName(
														"generic"),
										LabelClasses.getInstance()
												.getClassByName("generic")
												.getEntityByName("labeled"),
										System.currentTimeMillis()));
							}
							if (Double.parseDouble(inP.readLine()) == 1) {
								powerTrack.addLabel(new LabelObject("power",
										"", Long.parseLong(vals[0]), Long
												.parseLong(vals[1]), 1.0,
										LabelType.MANUAL, LabelClasses
												.getInstance().getClassByName(
														"generic"),
										LabelClasses.getInstance()
												.getClassByName("generic")
												.getEntityByName("labeled"),
										System.currentTimeMillis()));
							}
							if (Double.parseDouble(inV.readLine()) == 1) {
								valenceTrack.addLabel(new LabelObject(
										"valence", "", Long.parseLong(vals[0]),
										Long.parseLong(vals[1]), 1.0,
										LabelType.MANUAL, LabelClasses
												.getInstance().getClassByName(
														"generic"),
										LabelClasses.getInstance()
												.getClassByName("generic")
												.getEntityByName("labeled"),
										System.currentTimeMillis()));
							}
						}
						turnEnd = Long.parseLong(vals[1]);
					}
				}

				in.close();
				inA.close();
				inE.close();
				inP.close();
				inV.close();
				Project.getInstance().getLcoll()
						.addLabelTrack(turnTrack, true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);
				Project.getInstance().getLcoll()
						.addLabelTrack(wordTrack, true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);
				Project.getInstance().getLcoll()
						.addLabelTrack(arousalTrack, true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);
				Project.getInstance().getLcoll()
						.addLabelTrack(expectancyTrack, true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);
				Project.getInstance().getLcoll()
						.addLabelTrack(powerTrack, true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);
				Project.getInstance().getLcoll()
						.addLabelTrack(valenceTrack, true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (this.getSelectedFile().getPath().endsWith(".pres")) {
			RandomAccessFile in;
			try {
				in = new RandomAccessFile(this.getSelectedFile(), "r");
				String line = "";
				ArrayList<String> names = new ArrayList<String>();
				ArrayList<String> text = new ArrayList<String>();
				ArrayList<Double> start = new ArrayList<Double>();
				ArrayList<Double> end = new ArrayList<Double>();
				line = in.readLine();
				String[] vals = line.split("[,;\t]");
				names.add(vals[1]); // name
				// text.add(vals[0] + " - " + vals[2]); //text
				text.add(vals[2]); // text
				start.add(Double.parseDouble(vals[3]));// start
				while ((line = in.readLine()) != null) {
					vals = line.split("[,;\t]");
					names.add(vals[1]); // name
					// text.add(vals[0] + " - " + vals[2]); //text
					text.add(vals[2]); // text
					start.add(Double.parseDouble(vals[3]));// start
					end.add(Double.parseDouble(vals[3]));// end
				}
				end.add(start.get(start.size() - 1) + 5000);
				if (LabelClasses.getInstance().getClassByName("Presentation") == null) {
					LabelClasses.getInstance().addClass(
							new LabelClass("Presentation"));
					LabelClass anf = LabelClasses.getInstance().getClassByName(
							"Presentation");
					anf.addEntity(new LabelClassEntity(anf, "Picture", 0,
							Color.cyan, Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "Response", 1,
							Color.yellow, Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "Sound", 2,
							Color.green, Color.darkGray));
					anf.addEntity(new LabelClassEntity(anf, "Video", 3,
							Color.red, Color.darkGray));
				}

				LabelTrack presentation = new LabelTrack(Project.getInstance()
						.getLcoll().getWidth(), LabelClasses.getInstance()
						.getClassByName("Presentation"), this
						.getSelectedFile()
						.getName()
						.substring(0,
								this.getSelectedFile().getName().length() - 5));

				for (int i = 0; i < names.size(); i++) {
					presentation.addLabel(new LabelObject(text.get(i), "",
							start.get(i).longValue(), end.get(i).longValue(),
							1.0, LabelType.MANUAL, LabelClasses.getInstance()
									.getClassByName("Presentation"),
							LabelClasses.getInstance()
									.getClassByName("Presentation")
									.getEntityByName(names.get(i)), System
									.currentTimeMillis()));
				}
				in.close();
				Project.getInstance().getLcoll()
						.addLabelTrack(presentation, true, Project.getInstance().getLcoll().getOlinesSize(), false, ObjectLine.MEDIUM, false);
				Project.getInstance().setProjectLength(presentation.getLength());

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Project.getInstance().saveProject();
		MessageManager.getInstance().requestClassChanged(
				new ClassChangedEvent(this));
		MessageManager.getInstance().selectionChanged(
				new LabelSelectionEvent(this, null, null, null));
		lcoll.updateViewport();
		MessageManager.getInstance().requestTrackUpdate(
				new UpdateTracksEvent(this));
		MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
		this.setVisible(false);

	}
}

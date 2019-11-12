package de.atlas.collections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import de.atlas.data.*;
import de.atlas.messagesystem.*;
import de.atlas.exceptions.LabelClassNotFoundException;
import de.atlas.misc.AtlasProperties;

import de.atlas.misc.Interpolator;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

public class LabelTrack extends JPanel implements MouseMotionListener, MouseListener/*, MouseWheelListener*/ {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private int trackLength;
    private int trackHeight = ObjectLine.MEDIUM;
    private ArrayList<LabelObject> labels = new ArrayList<LabelObject>();
    private int atX = 1;
    private int atY;
    private int start;
    private Font f = AtlasProperties.getInstance().getLabelFont();
    private FontMetrics fm;
    private int textspace = AtlasProperties.getInstance()
            .getLabelTextspace();
    private BufferedImage img;
    private int space, points;
    private boolean rezising = false;
    private LabelObject activeLabel = null;
    private LabelObject preLabel = null;
    private LabelObject postLabel = null;
    private LabelObject selectedLabel = null;
    private long pressedX, pressedS, pressedE;
    private int mousesensitivityspace = AtlasProperties.getInstance()
            .getMouseSensitivitySpace();
    private LabelClass labelClass = null;
    private File file;
    private String name;
    private long lastExternalChange = 0;
    private boolean unlockLabels = false;
    private boolean showText = true;
    private boolean showVal = false;
    private boolean showState = false;
    private boolean showEntity = false;
    private boolean showContinuous = false;
    private SamplePoint activeSamplePoint =null;
    private int interpolationType = Interpolator.MEAN;

    public LabelTrack(int length, LabelClass lc, String name) {
        this.name = name;
        this.file = new File(Project.getInstance().getProjectPath()
                + "labeltracks/" + this.name + ".xml");
        this.trackLength = length;
        this.labelClass = lc;

        this.setBackground(Color.lightGray);
        this.setBounds(1, 1, this.trackLength, this.trackHeight);
        this.setPreferredSize(new Dimension(this.trackLength, this.trackHeight));

        this.setBorder(BorderFactory.createLineBorder(Color.black));
        this.setLayout(null);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        //this.addMouseWheelListener(this);

        MessageManager.getInstance().addSlotChangedListener(
                new SlotChangedListener() {
                    @Override
                    public void slotChanged(SlotEvent e) {
                        updateSlot(e.getStart());
                    }
                });
        MessageManager.getInstance().addUnlockLabelsListener(
                new UnlockLabelsListener() {
                    @Override
                    public void unlockLabels(UnlockLabelsEvent e) {
                        unlockLabels = e.isUnlocked();
                    }
                });
        MessageManager.getInstance().addLabelSelectionListener(
                new LabelSelectionListener() {
                    @Override
                    public void selectionChanged(LabelSelectionEvent e) {
                        updateSelection(e.getLabelObject());
                    }
                });
        MessageManager.getInstance().addKeyTypedListener(
                new KeyTypedListener() {

                    @Override
                    public void keyTyped(KeyEvent e) {
                        if (selectedLabel != null) {
                            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                                shiftSelectedLabelLeft();
                            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                                shiftSelectedLabelRight();
                            }
                        }
                    }
                });

    }

    public LabelTrack(File file) throws LabelClassNotFoundException {

        this.file = file;
        try {
            loadData(this.file);
        } catch (LabelClassNotFoundException e) {
            throw e;
        }

        this.setBackground(Color.lightGray);
        this.setBounds(1, 1, this.trackLength, this.trackHeight);
        this.setPreferredSize(new Dimension(this.trackLength, this.trackHeight));

        this.setBorder(BorderFactory.createLineBorder(Color.black));
        this.setLayout(null);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        //this.addMouseWheelListener(this);



        MessageManager.getInstance().addSlotChangedListener(
                new SlotChangedListener() {
                    @Override
                    public void slotChanged(SlotEvent e) {
                        updateSlot(e.getStart());
                    }
                });
        MessageManager.getInstance().addUnlockLabelsListener(
                new UnlockLabelsListener() {
                    @Override
                    public void unlockLabels(UnlockLabelsEvent e) {
                        unlockLabels = e.isUnlocked();
                    }
                });
        MessageManager.getInstance().addLabelSelectionListener(
                new LabelSelectionListener() {
                    @Override
                    public void selectionChanged(LabelSelectionEvent e) {
                        updateSelection(e.getLabelObject());
                    }
                });
        MessageManager.getInstance().addKeyTypedListener(
                new KeyTypedListener() {

                    @Override
                    public void keyTyped(KeyEvent e) {
                        if (selectedLabel != null) {
                            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                                shiftSelectedLabelLeft();
                            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                                shiftSelectedLabelRight();
                            }
                        }
                    }
                });
    }

    public LabelTrack(String XML) throws LabelClassNotFoundException{
        updateData(XML);

        this.setBackground(Color.lightGray);
        this.setBounds(1, 1, this.trackLength, this.trackHeight);
        this.setPreferredSize(new Dimension(this.trackLength, this.trackHeight));

        this.setBorder(BorderFactory.createLineBorder(Color.black));
        this.setLayout(null);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);

        MessageManager.getInstance().addSlotChangedListener(
                new SlotChangedListener() {
                    @Override
                    public void slotChanged(SlotEvent e) {
                        updateSlot(e.getStart());
                    }
                });
        MessageManager.getInstance().addUnlockLabelsListener(
                new UnlockLabelsListener() {
                    @Override
                    public void unlockLabels(UnlockLabelsEvent e) {
                        unlockLabels = e.isUnlocked();
                    }
                });
        MessageManager.getInstance().addLabelSelectionListener(
                new LabelSelectionListener() {
                    @Override
                    public void selectionChanged(LabelSelectionEvent e) {
                        updateSelection(e.getLabelObject());
                    }
                });
        MessageManager.getInstance().addKeyTypedListener(
                new KeyTypedListener() {

                    @Override
                    public void keyTyped(KeyEvent e) {
                        if (selectedLabel != null) {
                            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                                shiftSelectedLabelLeft();
                            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                                shiftSelectedLabelRight();
                            }
                        }
                    }
                });
    }


    private void shiftSelectedLabelRight() {
        if (this.postLabel != null) {
            this.preLabel = this.activeLabel;
            this.selectedLabel = this.postLabel;

            if (labels.indexOf(this.postLabel) + 1 < labels.size()) {
                this.postLabel = labels.get(labels.indexOf(this.postLabel) + 1);
            } else {
                this.postLabel = null;
            }
        }
        this.activeLabel = this.selectedLabel;

        MessageManager.getInstance().selectionChanged(
                new LabelSelectionEvent(this, this.preLabel,
                        this.selectedLabel, this.postLabel));
    }

    public boolean hasSelectedLabel() {
        if (this.selectedLabel != null) return true;
        return false;
    }

    public LabelObject getSelectedLabel() {
        return this.selectedLabel;
    }

    public void setSelectedLabel(LabelObject l) {
        if (l == null) {
            return;
        }
        this.selectedLabel = l;
        if (labels.indexOf(this.selectedLabel) > 0) {
            this.preLabel = labels.get(labels.indexOf(this.selectedLabel) - 1);
        } else {
            this.preLabel = null;
        }
        if (labels.indexOf(this.selectedLabel) < labels.size() - 1) {
            this.postLabel = labels.get(labels.indexOf(this.selectedLabel) + 1);
        } else {
            this.postLabel = null;
        }
        MessageManager.getInstance().selectionChanged(new LabelSelectionEvent(this, this.preLabel, this.selectedLabel, this.postLabel));
    }

    private void shiftSelectedLabelLeft() {
        if (this.preLabel != null) {
            this.postLabel = this.activeLabel;
            this.selectedLabel = this.preLabel;

            if (labels.indexOf(this.preLabel) > 0) {
                this.preLabel = labels.get(labels.indexOf(this.preLabel) - 1);
            } else {
                this.preLabel = null;
            }
        }
        this.activeLabel = this.selectedLabel;

        MessageManager.getInstance().selectionChanged(
                new LabelSelectionEvent(this, this.preLabel,
                        this.selectedLabel, this.postLabel));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<LabelObject> getLabels() {
        return labels;
    }

    public ArrayList<LabelObject> getLabels(long from, long to) {
        Iterator<LabelObject> iO = labels.iterator();
        ArrayList<LabelObject> data = new ArrayList<LabelObject>();
        while (iO.hasNext()) {
            LabelObject obj = iO.next();
            if (obj.getStart() < to && obj.getEnd() > from) {
                data.add(obj);
            }
        }
        return data;
    }

    public File getFile() {
        return this.file;
    }

    public void writeXML() {

        try {
            FileOutputStream out = new FileOutputStream(file);
            XMLOutputter serializer = new XMLOutputter();
            serializer.setFormat(Format.getPrettyFormat());
            serializer.output(getXMLDocument(), out);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void updateSelection(LabelObject obj) {
        // if(this.selectedLabel!=null && !this.selectedLabel.equals(obj)){
        // this.selectedLabel=null;
        // }
        if (/* this.selectedLabel!=null && */!this.labels.contains(obj)) {
            this.selectedLabel = null;
        } else {
            this.selectedLabel = obj;
        }
    }

    private void updateSlot(int start) {
        this.start = start;
        this.repaint();
    }

    public String getPath() {
        return file.getPath();
    }

    public LabelClass getLabelClass() {
        return this.labelClass;
    }

    private void loadData(Document doc) throws DataConversionException, LabelClassNotFoundException {
        Element root = doc.getRootElement();
        this.name = root.getAttribute("name").getValue();
        this.lastExternalChange = root.getAttribute("externalchange").getLongValue();
        this.showContinuous = root.getAttribute("isContinuous")!=null?root.getAttribute("isContinuous").getBooleanValue():false;
        this.interpolationType = root.getAttribute("interpolationType")!=null?root.getAttribute("interpolationType").getIntValue():Interpolator.LINEAR;
        this.labelClass = LabelClasses.getInstance().getClassByName(root.getAttribute("classname").getValue());
        if (this.labelClass == null) {

            throw new LabelClassNotFoundException();
        }
        this.clear();
        @SuppressWarnings("unchecked")
        List<Element> labelList = ((List<Element>) root
                .getChildren("label"));
        for (Element label : labelList) {
            try {
                LabelType t = LabelType.MANUAL;
                if (label.getChild("type").getValue()
                        .equalsIgnoreCase("MANUAL")) {
                    t = LabelType.MANUAL;
                } else if (label.getChild("type").getValue()
                        .equalsIgnoreCase("AUTOMATIC")) {
                    t = LabelType.AUTOMATIC;
                } else if (label.getChild("type").getValue()
                        .equalsIgnoreCase("AUTO_ACCEPTED")) {
                    t = LabelType.AUTO_ACCEPTED;
                } else if (label.getChild("type").getValue()
                        .equalsIgnoreCase("AUTO_REJECTED")) {
                    t = LabelType.AUTO_REJECTED;
                }

                LabelObject tmp = new LabelObject(label.getChild("text").getValue(), label.getChild("comment").getValue(), Long.parseLong(label.getChild("starttime").getValue()), Long.parseLong(label.getChild("endtime").getValue()), Double.parseDouble(label.getChild("value").getValue()), t, this.labelClass, this.labelClass.getEntityByName(label.getChild("classentity").getValue()), Long.parseLong(label.getChild("timestamp").getValue()));

                if (label.getChild("continuousSamplingPoints") != null) {
                    List<Element> splinePointsList = label.getChild("continuousSamplingPoints").getChildren();
                    tmp.getSamplePoints().clear();
                    for (Element sp : splinePointsList) {
                        double x = sp.getAttribute("t").getDoubleValue();
                        double y = sp.getAttribute("y").getDoubleValue();
                        tmp.getSamplePoints().add(new SamplePoint(x, y));
                    }
                }
                if (label.getChild("showAsFlag") != null) {
                    tmp.setShowAsFlag(Boolean.parseBoolean(label.getChild("showAsFlag").getValue()));
                }
                this.addLabel(tmp);
            }catch(Exception e){
                System.out.println("label could not be added");
                e.printStackTrace();
            }
        }

    }
    public void loadData(File file) throws LabelClassNotFoundException {

        if (file.getPath().endsWith(".xml")) {

            SAXBuilder sxbuild = new SAXBuilder();
            try {
                loadData(sxbuild.build(new InputSource(new FileInputStream(file.getPath()))));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
    public void updateData(String xml) {
        SAXBuilder sxbuild = new SAXBuilder();
        try {
            loadData(sxbuild.build(new ByteArrayInputStream(xml.getBytes("UTF-8"))));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LabelClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public long getLength(){
        return labels.get(labels.size()-1).getEnd();
    }

    public void triggerSelectionUpdate() {
        MessageManager.getInstance().selectionChanged(
                new LabelSelectionEvent(this, this.preLabel,
                        this.selectedLabel, this.postLabel));
    }

    public void addLabel(LabelObject lo) {
        LabelObject pre = null, mid = null, post = null;

        this.labels.add(lo);
        Collections.sort(this.labels);
        mid = lo;

        int i = this.labels.indexOf(mid);
        if (i > 0) {
            pre = this.labels.get(i - 1);
        }
        if (i < this.labels.size() - 1) {
            post = this.labels.get(i + 1);
        }

        if ((mid.getStart() < 0)
                || (pre != null && mid.getStart() < pre.getEnd())) {
            this.labels.remove(lo);
            return;
        }
        if (mid.getEnd() <= 0
                || (post != null && mid.getEnd() > post.getStart())) {
            this.labels.remove(lo);
            return;
        }

        this.selectedLabel = mid;
        this.preLabel = pre;
        this.postLabel = post;
        // MessageManager.getInstance().requestTrackUpdate(new
        // UpdateTracksEvent(this));

    }

  //  @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && this.activeLabel != null) {
            MessageManager.getInstance().showLabelprobsWindow(
                    new ShowLabelprobsWindowEvent(this));
        }
    }

  //  @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

  //  @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

   // @Override
    public void mousePressed(MouseEvent e) {

        this.activeLabel = null;
        this.preLabel = null;
        this.postLabel = null;
        this.activeSamplePoint = null;
        Iterator<LabelObject> iO = labels.iterator();
        while (iO.hasNext()) {
            LabelObject obj = iO.next();
            if (obj.getStart() > start
                    + (this.getWidth() / Project.getInstance().getZoom())) {
                break;
            }
            if (obj.getEnd() >= start) {
//                if(obj.isShowAsFlag()) {
//                    if ((e.getX() / Project.getInstance().getZoom()) + start > obj.getStart() && e.getX() < (obj.getStart() -start) * Project.getInstance().getZoom() + this.getHeight()/2 && e.getY() < this.getHeight()/2) {
//                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//                    }
                if (((e.getX() / Project.getInstance().getZoom()) + start < obj.getEnd() && (e.getX() / Project.getInstance().getZoom()) + start > obj.getStart())||
                    (obj.isShowAsFlag()&&(e.getX() / Project.getInstance().getZoom()) + start > obj.getStart() && e.getX() < (obj.getStart() -start) * Project.getInstance().getZoom() + this.getHeight()/2 && e.getY() < this.getHeight()/2) ){
                    this.pressedX = (int) (e.getX() / Project.getInstance().getZoom());
                    this.pressedS = obj.getStart();
                    this.pressedE = obj.getEnd();
                    this.activeLabel = obj;
                    int i = this.labels.indexOf(obj);
                    if (i > 0) {
                        this.preLabel = this.labels.get(i - 1);
                    }
                    if (i < this.labels.size() - 1) {
                        this.postLabel = this.labels.get(i + 1);
                    }
                    if (this.getCursor().getType() == (Cursor.E_RESIZE_CURSOR) || this.getCursor().getType() == (Cursor.W_RESIZE_CURSOR)) {
                        this.rezising = true;
                    }else if(this.getCursor().getType()==Cursor.MOVE_CURSOR){
                        this.activeSamplePoint = obj.getSamplePoint((e.getX() / Project.getInstance().getZoom()) + start - obj.getStart(), (e.getY() / (double) getHeight() - 1.0) * -1.0, mousesensitivityspace / Project.getInstance().getZoom(), mousesensitivityspace / (double) getHeight());
                    }
                }
            }
        }
        this.selectedLabel = this.activeLabel;
        // if(this.selectedLabel!=null){
        MessageManager.getInstance().selectionChanged( new LabelSelectionEvent(this, this.preLabel, this.selectedLabel, this.postLabel));
    }

  //  @Override
    public void mouseDragged(MouseEvent e) {
        if (this.selectedLabel != null) {
            MessageManager.getInstance().selectionChanged(new LabelSelectionEvent(this, this.preLabel, this.selectedLabel, this.postLabel));
        }
        if (this.activeLabel != null) {
            boolean unlock = false;
            if (this.unlockLabels) {
                unlock = true;
            }
            if (this.activeLabel.getTimestamp() > lastExternalChange
                    && this.activeLabel.getLabelType() == LabelType.MANUAL) {
                unlock = true;
            }
            if (unlock) {
                if (this.rezising) {
                    if (this.getCursor().getType() == (Cursor.E_RESIZE_CURSOR)) {
                        if (start + (e.getX() / Project.getInstance().getZoom()) > this.activeLabel.getStart()+1) {
                            if (this.postLabel != null) {
                                if (start + (e.getX() / Project.getInstance().getZoom()) < this.postLabel.getStart()) {
                                    this.activeLabel.setEnd((long) (start + (e.getX() / Project.getInstance().getZoom())));
                                } else {
                                    this.activeLabel.setEnd(this.postLabel.getStart());
                                }
                            } else {
                                if (((double) e.getX() / Project.getInstance().getZoom() + start) < Project.getInstance().getProjectLength()) {
                                    this.activeLabel.setEnd((long) ((e.getX() / Project.getInstance().getZoom()) + start));
                                } else {
                                    this.activeLabel.setEnd((long) (Project.getInstance().getProjectLength()));
                                }
                            }
                        }
                    } else if (this.getCursor().getType() == (Cursor.W_RESIZE_CURSOR)) {// VERKLEINERN/VERGROESSERN
                        if (start + (e.getX() / Project.getInstance().getZoom()) < this.activeLabel.getEnd()-1) {
                            if (this.preLabel != null) {
                                if (start + (e.getX() / Project.getInstance().getZoom()) > this.preLabel.getEnd()) {
                                    this.activeLabel.setStart((int) (start + (e.getX() / Project.getInstance().getZoom())));
                                } else {
                                    this.activeLabel.setStart(this.preLabel.getEnd());
                                }
                            } else {
                                if ((e.getX() / Project.getInstance().getZoom()) > 0) {
                                    this.activeLabel.setStart((int) ((e.getX() / Project.getInstance().getZoom()) + start));
                                } else {
                                    this.activeLabel.setStart(start);
                                }
                            }
                        }
                    }
                } else {
                    if (this.getCursor().getType() == (Cursor.HAND_CURSOR)) {// VERSCHIEBEN
                        if ((e.getX() / Project.getInstance().getZoom())
                                - pressedX > 0) {
                            // nach rechts
                            if (this.postLabel != null) {
                                // nachbar existiert
                                if (this.pressedE
                                        + ((e.getX() / Project.getInstance()
                                        .getZoom()) - pressedX) < this.postLabel
                                        .getStart()) {
                                    // normales scrollen solange nachbar nicht
                                    // erreicht
                                    this.activeLabel
                                            .setEnd((long) (this.pressedE
                                                    + (e.getX() / Project
                                                    .getInstance()
                                                    .getZoom()) - pressedX));
                                    this.activeLabel
                                            .setStart((long) (this.pressedS
                                                    + (e.getX() / Project
                                                    .getInstance()
                                                    .getZoom()) - pressedX));
                                } else {// fixer dock an die grenze des nachbarn
                                    this.activeLabel.setEnd(this.postLabel
                                            .getStart());
                                    this.activeLabel.setStart(this.postLabel
                                            .getStart()
                                            - (this.pressedE - this.pressedS));
                                }
                            } else {// kein nachbar
                                if (this.pressedE
                                        + ((e.getX() / Project.getInstance()
                                        .getZoom()) - pressedX) < Project
                                        .getInstance().getProjectLength()) {
                                    // normales scrollen bis rand
                                    this.activeLabel
                                            .setEnd((long) (this.pressedE
                                                    + (e.getX() / Project
                                                    .getInstance()
                                                    .getZoom()) - pressedX));
                                    this.activeLabel
                                            .setStart((long) (this.pressedS
                                                    + (e.getX() / Project
                                                    .getInstance()
                                                    .getZoom()) - pressedX));
                                } else {
                                    // rand erreicht fix setzen
                                    // HIER SOLLTE NOCH ADAPTIERT WERDEN NUR AM
                                    // ZEITPUNKT 0 und ZEITPUNKT ENDE nicht
                                    // fenster!!!
                                    this.activeLabel.setEnd((long) (Project
                                            .getInstance().getProjectLength()));
                                    this.activeLabel.setStart((long) Project.getInstance().getProjectLength()
                                            - (this.pressedE - this.pressedS));
                                }
                            }
                        } else {
                            // nach links
                            if (this.preLabel != null) {
                                if (this.pressedS
                                        + ((e.getX() / Project.getInstance()
                                        .getZoom()) - pressedX) > this.preLabel
                                        .getEnd()) {
                                    this.activeLabel
                                            .setEnd((long) (this.pressedE
                                                    + (e.getX() / Project
                                                    .getInstance()
                                                    .getZoom()) - pressedX));
                                    this.activeLabel
                                            .setStart((long) (this.pressedS
                                                    + (e.getX() / Project
                                                    .getInstance()
                                                    .getZoom()) - pressedX));
                                } else {
                                    this.activeLabel.setStart(this.preLabel
                                            .getEnd());
                                    this.activeLabel.setEnd(this.preLabel
                                            .getEnd()
                                            + (this.pressedE - this.pressedS));
                                }
                            } else {
                                if (this.pressedS
                                        + ((e.getX() / Project.getInstance()
                                        .getZoom()) - pressedX) > 0) {
                                    this.activeLabel
                                            .setEnd((long) (this.pressedE
                                                    + (e.getX() / Project
                                                    .getInstance()
                                                    .getZoom()) - pressedX));
                                    this.activeLabel
                                            .setStart((long) (this.pressedS
                                                    + (e.getX() / Project
                                                    .getInstance()
                                                    .getZoom()) - pressedX));
                                } else {
                                    this.activeLabel.setStart(0);
                                    this.activeLabel
                                            .setEnd((this.pressedE - this.pressedS));
                                }
                            }
                        }

                    }else if(this.getCursor().getType() == Cursor.MOVE_CURSOR){
                        if(e.getY()>=0&&e.getY()<=getHeight()){
                            activeSamplePoint.setY((e.getY() / (double) getHeight() - 1.0) * -1.0);
                        }
                        int idx = activeLabel.getSamplePoints().indexOf(activeSamplePoint);
                        if(!(idx == 0||idx==activeLabel.getSamplePoints().size()-1)){
                            if(activeLabel.getSamplePoints().get(idx-1).getX()<(e.getX() / Project.getInstance().getZoom()) + start - activeLabel.getStart()&&
                               activeLabel.getSamplePoints().get(idx+1).getX()>(e.getX() / Project.getInstance().getZoom()) + start - activeLabel.getStart()) {
                                activeSamplePoint.setX((e.getX() / Project.getInstance().getZoom()) + start - activeLabel.getStart());
                            }
                        }
                        activeLabel.interpolationValid(false);

                    }

                }
            }
            this.getParent().getParent().getParent().repaint();
        }

    }

   // @Override
    public void mouseMoved(MouseEvent e) {
        Iterator<LabelObject> iO = labels.iterator();
        boolean flag = false;
        while (iO.hasNext()) {
            LabelObject obj = iO.next();
            if (obj.getStart() > start + (this.getWidth() / Project.getInstance().getZoom())) {
                break;
            }
            if (obj.getEnd() >= start) {
                if(obj.isShowAsFlag()) {
                    if ((e.getX() / Project.getInstance().getZoom()) + start > obj.getStart() && e.getX() < (obj.getStart() -start) * Project.getInstance().getZoom() + this.getHeight()/2 && e.getY() < this.getHeight()/2) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        flag = true;
                    }
                }else if((e.getX() / Project.getInstance().getZoom()) + start < obj.getEnd() && (e.getX() / Project.getInstance().getZoom()) + start > obj.getStart()) {
                    this.setToolTipText(obj.getText());
                    flag = true;
                    if (showContinuous && obj.hasSampleCloseTo((e.getX() / Project.getInstance().getZoom()) + start - obj.getStart(), (e.getY() / (double) getHeight() - 1.0) * -1.0, mousesensitivityspace / Project.getInstance().getZoom(), mousesensitivityspace / (double) getHeight())) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    } else if ((e.getX() / Project.getInstance().getZoom()) + start > obj.getStart() && (e.getX() / Project.getInstance().getZoom()) + start < obj.getStart() + mousesensitivityspace / Project.getInstance().getZoom()) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                    } else if ((e.getX() / Project.getInstance().getZoom()) + start < obj.getEnd() && (e.getX() / Project.getInstance().getZoom()) + start > obj.getEnd() - mousesensitivityspace / Project.getInstance().getZoom()) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                    } else if ((e.getX() / Project.getInstance().getZoom()) + start < obj.getEnd() && (e.getX() / Project.getInstance().getZoom()) + start > obj.getStart()) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                } else if (!flag) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    this.setToolTipText("");
                }
            }
        }
    }

  //  @Override
    public void mouseReleased(MouseEvent e) {
        // if(this.selectedLabel!=null){
        // MessageManager.getInstance().selectionChanged(new
        // LabelSelectionEvent(this,this.selectedLabel));
        // }
        this.rezising = false;
        if (e.getButton() == MouseEvent.BUTTON3) {
            showContextMenu(e);
        }
    }

    public void showContextMenu(MouseEvent evt) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("add label");
        JMenuItem delItem = new JMenuItem("delete label");
        JCheckBoxMenuItem showTextItem = new JCheckBoxMenuItem("show Text");
        JCheckBoxMenuItem showValItem = new JCheckBoxMenuItem("show Value");
        JCheckBoxMenuItem showStateItem = new JCheckBoxMenuItem("show State");
        JCheckBoxMenuItem showEntityItem = new JCheckBoxMenuItem("show Entity");
        JCheckBoxMenuItem showContinuumItem = new JCheckBoxMenuItem("show Continuum");
        JMenuItem addSplinePointItem = new JMenuItem("add sampling point");
        JMenuItem delSplinePointItem = new JMenuItem("delete sampling point");

        atY = evt.getY();
        atX = evt.getX();
        if (((atX / Project.getInstance().getZoom()) + start) < Project
                .getInstance().getProjectLength()) {
            addItem.setEnabled(true);
            delItem.setEnabled(true);
            addSplinePointItem.setEnabled(true);
            delSplinePointItem.setEnabled(true);
        } else {
            addItem.setEnabled(false);
            delItem.setEnabled(false);
            addSplinePointItem.setEnabled(false);
            delSplinePointItem.setEnabled(false);
        }
        showTextItem.setSelected(showText);
        showValItem.setSelected(showVal);
        showStateItem.setSelected(showState);
        showEntityItem.setSelected(showEntity);
        showContinuumItem.setSelected(showContinuous);

        showTextItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showText = !showText;
                update();
            }
        });
        showValItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showVal = !showVal;
                update();
            }
        });
        showStateItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showState = !showState;
                update();
            }
        });
        showEntityItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showEntity = !showEntity;
                update();
            }
        });
        showContinuumItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showContinuous = !showContinuous;
                update();
            }
        });
        addItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuAdd();
            }
        });
        delItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuDelete();
            }
        });
        addSplinePointItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuSplinePointAdd();
            }
        });
        delSplinePointItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuSplinePointDelete();
            }
        });
        if (this.selectedLabel == null) {
            delItem.setEnabled(false);
        }
        if(this.activeSamplePoint ==null){
            delSplinePointItem.setEnabled(false);
        }
        if(activeLabel==null)addSplinePointItem.setEnabled(false);
        if(activeLabel!=null)addItem.setEnabled(false);
        menu.add(addItem);
        menu.add(delItem);
        menu.add(showTextItem);
        menu.add(showValItem);
        menu.add(showStateItem);
        menu.add(showEntityItem);
        menu.add(showContinuumItem);
        menu.add(addSplinePointItem);
        menu.add(delSplinePointItem);
        menu.show(this, evt.getX(), evt.getY());
    }

    private void menuSplinePointAdd() {
        // x in active label?
        double t = atX / Project.getInstance().getZoom() + start - activeLabel.getStart();
        double v = (atY / (double) getHeight() - 1.0) * -1.0;
        // insert splinePoint at t,v
        activeLabel.addSamplePoint(new SamplePoint(t, v));
        // redraw
        MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
    }

    private void menuSplinePointDelete() {
        int idx = activeLabel.getSamplePoints().indexOf(activeSamplePoint);
        if(idx>0&&idx<activeLabel.getSamplePoints().size()){
            activeLabel.getSamplePoints().remove(activeSamplePoint);
            activeLabel.interpolationValid(false);
            MessageManager.getInstance().requestRepaint(
                    new RepaintEvent(this));
        }
    }

    protected void update() {
        MessageManager.getInstance().selectionChanged(
                new LabelSelectionEvent(this, this.preLabel,
                        this.selectedLabel, this.postLabel));

    }

    private void menuDelete() {
        if (this.selectedLabel != null) {
            deleteLabel(selectedLabel);
        }
    }

    private void menuAdd() {

        int begin = (int) (atX / Project.getInstance().getZoom()) + start;
        int end = (int) (atX / Project.getInstance().getZoom() + (1000.0 / Project
                .getInstance().getProjectFPS())
                / Project.getInstance().getZoom()) + start;
        if (Project.getInstance() != null && end - begin < (1000.0 / Project.getInstance().getProjectFPS())) {
            end = (int) (begin + (1000.0 / Project.getInstance().getProjectFPS()));
        }
        LabelObject tmp = new LabelObject("label" + (this.labels.size() + 1), "", begin, end, 1.0, LabelType.MANUAL, this.labelClass, null, System.currentTimeMillis());
        this.addLabel(tmp);

        MessageManager.getInstance().selectionChanged(
                new LabelSelectionEvent(this, this.preLabel,
                        this.selectedLabel, this.postLabel));

    }

    public void deleteLabel(LabelObject lo) {
        Object[] options = {"yes", "no"};
        if (lo.getTimestamp() < this.lastExternalChange) {
            int n = JOptionPane
                    .showOptionDialog(
                            this,
                            "This label was used for external training. Do you really want to delete?",
                            "Delete Label???", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, options,
                            options[0]);
            if (n == 0) {// yes
                this.labels.remove(lo);
                MessageManager.getInstance().selectionChanged(
                        new LabelSelectionEvent(this, null, null, null));
                MessageManager.getInstance().requestRepaint(
                        new RepaintEvent(this));
                MessageManager.getInstance().requestTrackUpdate(
                        new UpdateTracksEvent(this));
            }
        } else if (lo.getLabelType() == LabelType.AUTO_ACCEPTED
                || lo.getLabelType() == LabelType.AUTO_REJECTED
                || lo.getLabelType() == LabelType.AUTOMATIC) {
            int n = JOptionPane.showOptionDialog(this,
                    "This label was autogenerated ("
                            + lo.getLabelType().toString()
                            + ") do you really want to delete?",
                    "Delete Label???", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (n == 0) {// yes
                this.labels.remove(lo);
                MessageManager.getInstance().selectionChanged(
                        new LabelSelectionEvent(this, null, null, null));
                MessageManager.getInstance().requestRepaint(
                        new RepaintEvent(this));
                MessageManager.getInstance().requestTrackUpdate(
                        new UpdateTracksEvent(this));
            }
        } else if (lo.getLabelType() == LabelType.MANUAL) {
            int n = JOptionPane.showOptionDialog(this,
                    "Do you really want to delete?", "Delete Label???",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            if (n == 0) {// yes
                this.labels.remove(lo);
                MessageManager.getInstance().selectionChanged(
                        new LabelSelectionEvent(this, null, null, null));
                MessageManager.getInstance().requestRepaint(
                        new RepaintEvent(this));
                MessageManager.getInstance().requestTrackUpdate(
                        new UpdateTracksEvent(this));
            }
        }

    }

    public double[][] getData(long from, long to) {
        Iterator<LabelObject> iO = labels.iterator();
        ArrayList<Double> data = new ArrayList<Double>();
        while (iO.hasNext()) {
            LabelObject obj = iO.next();
            if (obj.getStart() < to && obj.getEnd() > from) {
                data.add((double) obj.getLabelClassEntity().getId());
            }
        }
        double[][] ret = new double[data.size()][1];
        Iterator<Double> iD = data.iterator();
        int i = 0;
        while (iD.hasNext()) {
            ret[i++][0] = iD.next().doubleValue();
        }
        return ret;
    }

    private void drawIMG(int start) {
        if (this.getParent() == null) {
            return;
        }
        if (this.getParent().isVisible()) {

            img = new BufferedImage(this.getWidth(), this.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(f);
            g2.setColor(Color.gray);
            g2.fillRect(0, 0, img.getWidth(), img.getHeight());
            if (fm == null) {
                fm = g2.getFontMetrics();
                space = fm.stringWidth(" ");
                points = fm.stringWidth("...");
            }
            g2.setColor(Color.black);
            int height = getHeight();
            int[] flagY=new int[3];
            flagY[0]=1;
            flagY[1]=height/4;
            flagY[2]=height/2;

            Iterator<LabelObject> iO = labels.iterator();
            while (iO.hasNext()) {
                LabelObject obj = iO.next();
                if (obj.getStart() > start + this.getWidth()
                        / Project.getInstance().getZoom()) {
                    break;
                }
                if (obj.getEnd() >= start) {
                    int width = (int) ((obj.getEnd() - obj.getStart()) * Project
                            .getInstance().getZoom());
                    if (width == 0)
                        width = 1;
                    int round = height / 4;
                    if (this.selectedLabel != null && this.selectedLabel.equals(obj)) {
                        if(!obj.isShowAsFlag()){
                            g2.setColor(obj.getColor());
                            g2.setStroke(new BasicStroke(0));
                            g2.fillRoundRect((int) ((obj.getStart() - start) * Project.getInstance().getZoom()), 1, width, height - 1, round, round);
                            g2.setColor(AtlasProperties.getInstance().getSelectionColor());
                            g2.setStroke(new BasicStroke(4));
                            g2.drawRoundRect((int) ((obj.getStart() - start) * Project.getInstance().getZoom()), 1, width - 2, height - 1, round, round);
                        }else{
                            g2.setColor(Color.white);
                            g2.setStroke(new BasicStroke(2));
                            g2.drawLine((int) ((obj.getStart() - start) * Project.getInstance().getZoom()), 1,(int)((obj.getStart() - start) * Project.getInstance().getZoom()),height);
                            int[] polyX=new int[3];
                            polyX[0]=polyX[2]=(int) ((obj.getStart() - start) * Project.getInstance().getZoom());
                            polyX[1]=(int) ((obj.getStart() - start) * Project.getInstance().getZoom())+height/2;
                            g2.setColor(obj.getColor());
                            g2.setStroke(new BasicStroke(0));
                            g2.fillPolygon(polyX,flagY,3);

                            g2.setStroke(new BasicStroke(4));
                            g2.setColor(AtlasProperties.getInstance().getSelectionColor());
                            g2.drawPolygon(polyX,flagY,3);
                        }

                    } else {
                        g2.setStroke(new BasicStroke(0));
                        g2.setColor(obj.getColor());
                        if(!obj.isShowAsFlag()) {
                            g2.fillRoundRect((int) ((obj.getStart() - start) * Project.getInstance().getZoom()), 1, width, height - 1, round, round);
                        }else{
                            int[] polyX=new int[3];
                            polyX[0]=polyX[2]=(int) ((obj.getStart() - start) * Project.getInstance().getZoom());
                            polyX[1]=(int) ((obj.getStart() - start) * Project.getInstance().getZoom())+height/2;
                            g2.setColor(obj.getColor());
                            g2.setStroke(new BasicStroke(0));
                            g2.fillPolygon(polyX,flagY,3);

                            g2.setColor(Color.white);
                            g2.setStroke(new BasicStroke(2));
                            g2.drawLine((int) ((obj.getStart() - start) * Project.getInstance().getZoom()), 1,(int)((obj.getStart() - start) * Project.getInstance().getZoom()),height);
                        }
                    }
                    if (showContinuous&&!obj.isShowAsFlag()) {
                        if (this.selectedLabel != null && this.selectedLabel.equals(obj)) {
                            g2.setStroke(new BasicStroke(3));
                        }else{
                            g2.setStroke(new BasicStroke(1));
                        }
                        g2.setColor(obj.getLabelClassEntity() == null ? Color.darkGray : obj.getLabelClassEntity().getContinuousColor());

                        for (int i = 0; i < (int)((obj.getEnd() - obj.getStart()) * Project.getInstance().getZoom()); i++) {
                            int labelPixelStartX = (int)((obj.getStart() - start) * Project.getInstance().getZoom());
                            int x1 = i   + labelPixelStartX;
                            int y1 = (int) ((height) - (obj.getInterpolationValueAt(i / Project.getInstance().getZoom(), interpolationType) * height));
                            int x2 = i+1 + labelPixelStartX;
                            int y2 = (int) ((height) - (obj.getInterpolationValueAt((i + 1) / Project.getInstance().getZoom(), interpolationType) * height));
                                    g2.drawLine(x1, y1, x2, y2);

//                            g2.drawLine(i   + labelPixelStartX, (int) ((height) - (obj.getInterpolationValueAt(i / Project.getInstance().getZoom(), interpolationType) * height)),
//                                    i+1 + labelPixelStartX, (int) ((height) - (obj.getInterpolationValueAt((i + 1) / Project.getInstance().getZoom(), interpolationType) * height)));
                        }
                        for (int i = 0; i < obj.getSamplePoints().size(); i++) {
                            g2.drawOval((int) ((obj.getStart() - start + obj.getSamplePoint(i).getX()) * Project.getInstance().getZoom() - 2), (int) ((height) - (obj.getSamplePoint(i).getY() * height)) - 2, 5, 5);
                        }

                    }
                    g2.setColor(Color.black);
                    String text = "";
                    if (showText) {
                        if (text.length() != 0)
                            text = text + " | ";
                        text = text + obj.getText();
                    }
                    if (showVal) {
                        if (text.length() != 0)
                            text = text + " | ";
                        text = text + obj.getValue();
                    }
                    if (showState) {
                        if (text.length() != 0)
                            text = text + " | ";
                        text = text + obj.getLabelType();
                    }
                    if (showEntity) {
                        if (text.length() != 0)
                            text = text + " | ";
                        if (obj.getLabelClassEntity() != null) {
                            text = text + obj.getLabelClassEntity().getName();
                        } else {
                            text = text + "none";
                        }
                    }

                    if(obj.isShowAsFlag()){
                        g2.setColor(Color.white);
                        g2.drawString(
                                text,
                                5+(int) (obj.getStart() * Project.getInstance().getZoom() - start * Project.getInstance().getZoom()),
                                (int)(fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 1.5));
                                //ToDo: what if text to long? do we know where the next label is?
                    }else if (points >= width - 2 * textspace) {
                        // wenn die punkte nimmer passen keinen text malen
                    } else if (fm.stringWidth(text) <= width - 2 * textspace) {
                        g2.drawString(
                                text,
                                (int) (((width - textspace - fm
                                        .stringWidth(text)) / 2)
                                        + obj.getStart()
                                        * Project.getInstance().getZoom() - start
                                        * Project.getInstance().getZoom()), (fm
                                        .getAscent() + (height - (fm
                                        .getAscent() + fm.getDescent())) / 2));
                    } else {
                        StringTokenizer st = new StringTokenizer(text);
                        String word = "";
                        String disp = "";
                        int z = 0;

                        while (st.hasMoreTokens()) {
                            word = st.nextToken();
                            int w = fm.stringWidth(word);
                            if (((z + space + w + points)) > width) {
                                disp = disp + "...";
                                // DRAW THE STRING

                                g2.drawString(
                                        disp,
                                        (int) (((width - textspace - fm
                                                .stringWidth(disp)) / 2) + (obj
                                                .getStart()
                                                * Project.getInstance()
                                                .getZoom() - start
                                                * Project.getInstance()
                                                .getZoom())),
                                        (fm.getAscent() + (height - (fm
                                                .getAscent() + fm.getDescent())) / 2));
                                break;
                            } else {
                                disp = disp + " ";
                            }
                            disp = disp + word;
                            z = z + space + w;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {

        this.drawIMG(start);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        if (img != null)
            g2.drawImage(img, 0, 0, this);

    }

    public double[] getDataTimePoints(long from, long to) {
        Iterator<LabelObject> iO = labels.iterator();
        ArrayList<Double> data = new ArrayList<Double>();
        while (iO.hasNext()) {
            LabelObject obj = iO.next();
            if (obj.getStart() > from && obj.getEnd() < to) {
                data.add((double) (obj.getStart() + obj.getEnd()) / 2.0);
            }
        }
        double[] ret = new double[data.size()];
        Iterator<Double> iD = data.iterator();
        int i = 0;
        while (iD.hasNext()) {
            ret[i++] = iD.next().doubleValue();
        }
        return ret;
    }

    public void clear() {
        // TODO Auto-generated method stub
        labels.clear();
        MessageManager.getInstance().selectionChanged(
                new LabelSelectionEvent(this, null, null, null));

    }

    public void removeLabels(long from, long to) {
        Iterator<LabelObject> iO = labels.iterator();
        ArrayList<LabelObject> labelsToRemove = new ArrayList<LabelObject>();
        while (iO.hasNext()) {
            LabelObject obj = iO.next();
            if (obj.getStart() < to && obj.getEnd() > from) {
                labelsToRemove.add(obj);
            }
        }
        iO = labelsToRemove.iterator();
        while (iO.hasNext()) {
            labels.remove(iO.next());
        }
        MessageManager.getInstance().selectionChanged(
                new LabelSelectionEvent(this, null, null, null));

    }

    public void removeLabel(LabelObject tmp) {
        labels.remove(tmp);
        MessageManager.getInstance().selectionChanged(
                new LabelSelectionEvent(this, null, null, null));
    }

    public LabelObject getLabelClosestTo(long time) {
        Iterator<LabelObject> iO = labels.iterator();
        long min = Long.MAX_VALUE;
        LabelObject ret = null;
        while (iO.hasNext()) {
            LabelObject obj = iO.next();
            if (Math.abs(time - obj.getStart()) < min) {
                min = Math.abs(time - obj.getStart());
                ret = obj;
            }
            if (Math.abs(time - obj.getEnd()) < min) {
                min = Math.abs(time - obj.getEnd());
                ret = obj;
            }
        }
        return ret;

    }

    public void removeShortLabels(long minDuration) {
        Iterator<LabelObject> iO = labels.iterator();
        ArrayList<LabelObject> toRemove = new ArrayList<LabelObject>();
        while (iO.hasNext()) {
            LabelObject obj = iO.next();
            if (obj.getEnd() - obj.getStart() < minDuration)
                toRemove.add(obj); //do not remove labels while loop to prevent java.util.ConcurrentModificationException
        }
        Iterator<LabelObject> iL = toRemove.iterator();
        while (iL.hasNext()) removeLabel(iL.next());
    }

    public long getLastExternalChange() {
        return this.lastExternalChange;
    }

    public void setInterpolation(int i) {
        this.interpolationType = i;
    }

    public int getInterpolationType() {
        return interpolationType;
    }

    public LabelObject getLabelAt(double time) {
        Iterator<LabelObject> iO = labels.iterator();
        while (iO.hasNext()) {
            LabelObject obj = iO.next();
            if(time>=obj.getStart()&&time<=obj.getEnd())return obj;
        }
        return null;
    }

    public String getAsXML() {
            XMLOutputter xmOut = new XMLOutputter();
            xmOut.setFormat(Format.getPrettyFormat());
            String str = xmOut.outputString(getXMLDocument());

            return str;
    }

    private Document getXMLDocument() {
        Element root = new Element("LabelTrack");
        root.setAttribute("name", this.name);
        root.setAttribute("externalchange", String.valueOf(this.lastExternalChange));
        root.setAttribute("classname", this.labelClass.getName());
        root.setAttribute("isContinuous", String.valueOf(this.showContinuous));
        root.setAttribute("interpolationType", String.valueOf(this.interpolationType));

        Iterator<LabelObject> lI = labels.iterator();
        while (lI.hasNext()) {
            LabelObject tmp = lI.next();
            Element label = new Element("label");

            Element starttime = new Element("starttime");
            starttime.setText(String.valueOf(tmp.getStart()));
            label.addContent(starttime);
            Element endtime = new Element("endtime");
            endtime.setText(String.valueOf(tmp.getEnd()));
            label.addContent(endtime);
            Element timestamp = new Element("timestamp");
            timestamp.setText(String.valueOf(tmp.getTimestamp()));
            label.addContent(timestamp);
            Element value = new Element("value");
            value.setText(String.valueOf(tmp.getValue()));
            label.addContent(value);
            Element comment = new Element("comment");
            comment.setText(String.valueOf(tmp.getComment()));
            label.addContent(comment);
            Element type = new Element("type");
            type.setText(String.valueOf(tmp.getLabelType()));
            label.addContent(type);
            Element text = new Element("text");
            text.setText(String.valueOf(tmp.getText()));
            label.addContent(text);
            Element classentity = new Element("classentity");
            if (tmp.getLabelClassEntity() != null) {
                classentity.setText(String.valueOf(tmp.getLabelClassEntity()
                        .getName()));
            } else {
                classentity.setText(String.valueOf("none"));
            }
            label.addContent(classentity);
            Element showAsFlag = new Element("showAsFlag");
            showAsFlag.setText(String.valueOf(tmp.isShowAsFlag()));
            label.addContent(showAsFlag);
            Element samplingPoints = new Element("continuousSamplingPoints");
            Iterator<SamplePoint> iS = tmp.getSamplePoints().iterator();
            while (iS.hasNext()){
                SamplePoint sp = iS.next();
                Element esp = new Element("samplePoint");
                esp.setAttribute("t",String.valueOf(sp.getX()));
                esp.setAttribute("y",String.valueOf(sp.getY()));
                samplingPoints.addContent(esp);
            }
            label.addContent(samplingPoints);
            root.addContent(label);
        }
        return new Document(root);
    }


/*    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {

    }
*/

}

package de.atlas.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import de.atlas.collections.*;
import de.atlas.exceptions.LabelClassNotFoundException;
import de.atlas.messagesystem.*;
import de.atlas.misc.AtlasProperties;
import de.atlas.gui.WindowManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;


public class Project {
    private static Project instance = new Project();
    private String projectPath = "";


    private File projectFile;
    private double length = 0;
    private String name = "";
    private LineCollection lcoll;
    private MediaCollection mcoll;
    private double zoom = 1.0;
    private long time = 0;
    private double projectFPS = 50.0;
    private ScrollType scrollType = ScrollType.HALF;
    private boolean existing = false;
    private String alternativeMediaPath;
    private String commandlineString = "";


    public Project() {
        lcoll = new LineCollection();
        mcoll = new MediaCollection();

        MessageManager.getInstance().addZoomChangedListener(
                new ZoomChangedListener() {
                    @Override
                    public void zoomChanged(ZoomEvent e) {
                        adjustZoom(e.getZoom());
                    }
                });
        MessageManager.getInstance().addTimeChangedListener(
                new TimeChangedListener() {

                    @Override
                    public void timeChanged(TimeEvent e) {
                        time = e.getTime();
                    }
                });
    }

    public static Project getInstance() {
        return instance;
    }

    public boolean isExisting() {
        return existing;
    }

    public void setExisting(boolean exists) {
        this.existing = exists;
    }

    public ScrollType getScrollType() {
        return scrollType;
    }

    public void setScrollType(ScrollType scrollType) {
        this.scrollType = scrollType;
    }

    public double getProjectFPS() {
        return projectFPS;
    }

    public void setProjectFPS(double projectFPS) {
        if (0.1 < projectFPS) {
            this.projectFPS = projectFPS;
        }
    }

    public File getProjectFile() {
        return projectFile;
    }

    public String getCommandlineString() {
        return commandlineString;
    }

    public void setCommandlineString(String commandlineString) {
        this.commandlineString = commandlineString;
    }

    public double getProjectLength() {
        return this.length;
    }

    public void setProjectLength(double length) {
        if (length > this.length) {
            this.length = length;
            this.lcoll.setScrollBarMax(this.length);
            MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
        }
    }

    private void adjustZoom(double zoom) {
        this.zoom = zoom;
    }

    public double getZoom() {
        return zoom;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public LineCollection getLcoll() {
        return lcoll;
    }

    public void setLcoll(LineCollection lcoll) {
        this.lcoll = lcoll;
    }

    public MediaCollection getMcoll() {
        return mcoll;
    }

    public void setMcoll(MediaCollection mcoll) {
        this.mcoll = mcoll;
    }

    @SuppressWarnings("unchecked")
    public void loadProject(String path, boolean forceLoad) {
        lcoll.reset();
        mcoll.reset();
        if (forceLoad) LabelClasses.getInstance().deleteAll();
        this.projectFile = new File(path);
        this.projectPath = this.projectFile.getParent() + "/";
        this.alternativeMediaPath = projectPath + "media/";
        // System.out.println("this.alternativeMediaPath="+this.alternativeMediaPath);

        // try {
        // System.out.println("Name: "+this.projectFile.getName());
        // System.out.println("Parent: "+this.projectFile.getParent());
        /*
         * System.out.println("Path: "+this.projectFile.getPath());
         * System.out.println
         * ("AbsolutePath: "+this.projectFile.getAbsolutePath());
         * System.out.println
         * ("CanonicalPath: "+this.projectFile.getCanonicalPath());
         * System.out.println
         * ("AbsoluteFile: "+this.projectFile.getAbsoluteFile());
         * System.out.println
         * ("CanonicalFile: "+this.projectFile.getCanonicalFile());
         * System.out.println("ParentFile: "+this.projectFile.getParentFile());
         * } catch (IOException e1) { e1.printStackTrace(); }
         */
        // READ PROJECT.XML
        if (this.projectFile.getPath().endsWith(".xml")) {
            SAXBuilder sxbuild = new SAXBuilder();
            InputSource is = null;
            try {
                is = new InputSource(new FileInputStream(this.projectFile.getPath()));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            Document doc;
            try {
                // long start = System.currentTimeMillis();
                doc = sxbuild.build(is);
                Element root = doc.getRootElement();
                this.name = root.getAttribute("name").getValue();

                this.commandlineString = root.getAttribute("commandlineExecutionString") != null ? root.getAttribute("commandlineExecutionString").getValue() : "";
                // this.projectPath =
                // root.getAttribute("projectPath").getValue();
                this.setProjectLength(root.getAttribute("length")
                        .getDoubleValue());
                this.zoom = root.getAttribute("zoom").getDoubleValue();
                MessageManager.getInstance().zoomChanged(new ZoomEvent(this, this.zoom));

                // READ CLASSES
                List<Element> classList = ((List<Element>) root.getChild("labelclasses").getChildren("lclass"));

                for (Element lclass : classList) {
                    LabelClasses.getInstance().readClassFromXML(new File((this.projectPath + "classes/" + lclass.getValue())));
                }

                // READ LABELTRACKS
                List<Element> labeltrackList = ((List<Element>) root.getChild(
                        "labeltracks").getChildren("ltrack"));
                for (Element ltrack : labeltrackList) {
                    try {
                        lcoll.addLabelTrack(
                                new LabelTrack(new File(Project.getInstance().getProjectPath() + "labeltracks/" + ltrack.getValue())),
                                ltrack.getAttribute("active").getBooleanValue(),
                                ltrack.getAttribute("order").getIntValue(),
                                ltrack.getAttribute("learnable") != null ? ltrack.getAttribute("learnable").getBooleanValue() : false,
                                ltrack.getAttribute("trackHeight") != null ? ltrack.getAttribute("trackHeight").getIntValue() : ObjectLine.MEDIUM,
                                ltrack.getAttribute("sendToMatlab") != null ? ltrack.getAttribute("sendToMatlab").getBooleanValue() : false);
                    } catch (LabelClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                // READ AUDIOTRACKS
                List<Element> atracklist = ((List<Element>) root
                        .getChild("mediatracks").getChild("audio")
                        .getChildren("atrack"));
                for (Element atrack : atracklist) {
                    mcoll.addAudioTrack(atrack.getAttribute("name").getValue(), atrack.getValue(), atrack.getAttribute("active").getBooleanValue(),
                            atrack.getAttribute("sendToMatlab") != null ? atrack.getAttribute("sendToMatlab").getBooleanValue() : false);

                }

                // READ VIDEOTRACKS
                List<Element> vtracklist = ((List<Element>) root
                        .getChild("mediatracks").getChild("video")
                        .getChildren("vtrack"));
                for (Element vtrack : vtracklist) {
                    mcoll.addVideoTrack(vtrack.getAttribute("name").getValue(), vtrack.getValue(), vtrack.getAttribute("active").getBooleanValue(),
                            vtrack.getAttribute("sendToMatlab") != null ? vtrack.getAttribute("sendToMatlab").getBooleanValue() : false);
                }

                // READ SCALARTRACK
                List<Element> scatracklist = ((List<Element>) root
                        .getChild("datatracks").getChild("scalartrack")
                        .getChildren("scatrack"));
                for (Element scatrack : scatracklist) {
                    lcoll.addScalarTrack(
                            scatrack.getAttribute("name").getValue(),
                            (this.projectPath + "datatracks/" + scatrack.getValue()),
                            scatrack.getAttribute("active").getBooleanValue(),
                            scatrack.getAttribute("showpoints").getBooleanValue(),
                            scatrack.getAttribute("showline").getBooleanValue(),
                            scatrack.getAttribute("min").getDoubleValue(),
                            scatrack.getAttribute("max").getDoubleValue(),
                            scatrack.getAttribute("order").getIntValue(),
                            scatrack.getAttribute("learnable") != null ? scatrack.getAttribute("learnable").getBooleanValue() : false,
                            scatrack.getAttribute("trackHeight") != null ? scatrack.getAttribute("trackHeight").getIntValue() : ObjectLine.MEDIUM,
                            scatrack.getAttribute("sendToMatlab") != null ? scatrack.getAttribute("sendToMatlab").getBooleanValue() : false);
                }

                // READ VECTORTRACK
                List<Element> vectracklist = ((List<Element>) root
                        .getChild("datatracks").getChild("vectortrack")
                        .getChildren("vectrack"));
                for (Element vectrack : vectracklist) {
                    lcoll.addVectorTrack(
                            vectrack.getAttribute("name").getValue(),
                            (this.projectPath + "datatracks/" + vectrack.getValue()),
                            vectrack.getAttribute("active").getBooleanValue(),
                            vectrack.getAttribute("colormap").getValue(),
                            vectrack.getAttribute("min").getDoubleValue(),
                            vectrack.getAttribute("max").getDoubleValue(),
                            vectrack.getAttribute("dimension").getIntValue(),
                            vectrack.getAttribute("order").getIntValue(),
                            vectrack.getAttribute("learnable") != null ? vectrack.getAttribute("learnable").getBooleanValue() : false,
                            vectrack.getAttribute("trackHeight") != null ? vectrack.getAttribute("trackHeight").getIntValue() : ObjectLine.MEDIUM,
                            vectrack.getAttribute("asScalar") != null ? vectrack.getAttribute("asScalar").getBooleanValue() : false,
                            vectrack.getAttribute("stacked") != null ? vectrack.getAttribute("stacked").getBooleanValue() : false,
                            vectrack.getAttribute("sendToMatlab") != null ? vectrack.getAttribute("sendToMatlab").getBooleanValue() : false);
                }

                // READ DATATRACK
                List<Element> datatracklist = ((List<Element>) root.getChild("datatracks").getChildren("datatrack"));
                for (Element datatrack : datatracklist) {
                    lcoll.addDataTrack(
                            datatrack.getAttribute("name").getValue(),
                            (this.projectPath + "datatracks/" + datatrack.getValue()),
                            datatrack.getAttribute("active").getBooleanValue(),
                            datatrack.getAttribute("colormap").getValue(),
                            datatrack.getAttribute("min").getDoubleValue(),
                            datatrack.getAttribute("max").getDoubleValue(),
                            datatrack.getAttribute("dimension").getIntValue(),
                            datatrack.getAttribute("samplerate").getDoubleValue(),
                            datatrack.getAttribute("order").getIntValue(),
                            datatrack.getAttribute("learnable") != null ? datatrack.getAttribute("learnable").getBooleanValue() : false,
                            datatrack.getAttribute("trackHeight") != null ? datatrack.getAttribute("trackHeight").getIntValue() : ObjectLine.MEDIUM,
                            datatrack.getAttribute("style").getValue(),
                            datatrack.getAttribute("sendToMatlab") != null ? datatrack.getAttribute("sendToMatlab").getBooleanValue() : false);
                }


                MessageManager.getInstance().projectLoaded(new ProjectLoadedEvent(this, this.projectFile.getName()));
                MessageManager.getInstance().slotChanged(new SlotEvent(this, 0));
                MessageManager.getInstance().requestTrackUpdate(new UpdateTracksEvent(this));
                MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
                MessageManager.getInstance().requestClassChanged(new ClassChangedEvent(this));


                //AtlasProperties.getInstance().setVideoZoomFactor(1.0);

                //Project.getInstance().setVideoZoomFactor(1.0);
                MessageManager.getInstance().videoZoomChanged(new VideoZoomEvent(this, AtlasProperties.getInstance().getVideoZoomFactor()));
                if (AtlasProperties.getInstance().isAutoarranging()) {
                    WindowManager.getInstance().autoarrange();
                }


                this.setExisting(true);
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        MessageManager.getInstance().selectionChanged(
                new LabelSelectionEvent(this, null, null, null));

    }

    public void saveProject() {

        // line xmls
        lcoll.writeXML();

        // class xmls
        LabelClasses.getInstance().writeXML();


        try {
            FileOutputStream out = new FileOutputStream(Project.getInstance().getProjectPath() + "" + name + ".xml");
            XMLOutputter serializer = new XMLOutputter();
            serializer.setFormat(Format.getPrettyFormat());
            serializer.output(getXMLDocument(), out);
        } catch (IOException e) {
            System.err.println(e);
        }
        System.out.println("project " + this.name + " saved");
    }

    public String getAsXMLString() {
        XMLOutputter xmOut = new XMLOutputter();
        xmOut.setFormat(Format.getPrettyFormat());
        String str = xmOut.outputString(getXMLDocument());

        return str;
    }

    private Document getXMLDocument() {
// project xml
        Element root = new Element("AnnotationProject");
        root.setAttribute("name", String.valueOf(this.name));
        // root.setAttribute("projectPath", this.projectPath);
        root.setAttribute("length", String.valueOf(this.length));
        root.setAttribute("zoom", String.valueOf(this.zoom));
        root.setAttribute("commandlineExecutionString", this.commandlineString);

        // CLASSES
        Element labelclasses = new Element("labelclasses");

        for (int i = 0; i < LabelClasses.getInstance().size(); i++) {
            LabelClass tmp = (LabelClass) LabelClasses.getInstance().get(i);
            Element lclass = new Element("lclass");
            lclass.setAttribute("name", tmp.getName());
            lclass.setText(tmp.getName() + ".xml");
            labelclasses.addContent(lclass);
        }
        root.addContent(labelclasses);
        // LABELTRACKS
        Element labeltracks = new Element("labeltracks");
        Iterator<ObjectLine> oI = lcoll.getList().iterator();
        while (oI.hasNext()) {
            ObjectLine tmp = oI.next();
            if (tmp.getType().equals("LabelTrack")) {
                Element ltrack = new Element("ltrack");
                ltrack.setAttribute("name", tmp.getName());
                ltrack.setAttribute("class", ((LabelTrack) tmp.getTrack())
                        .getLabelClass().getName());
                ltrack.setAttribute("active", String.valueOf(tmp.isActive()));
                ltrack.setAttribute("order", String.valueOf(tmp.getOrder()));
                ltrack.setAttribute("learnable",
                        String.valueOf(tmp.isLearnable()));
                ltrack.setAttribute("sendToMatlab",
                        String.valueOf(tmp.isSendToMatlab()));
                ltrack.setAttribute("trackHeight",
                        String.valueOf(tmp.getTrackHeight()));
                ltrack.setText(((LabelTrack) (tmp.getTrack())).getFile()
                        .getName());
                labeltracks.addContent(ltrack);
            }
        }
        root.addContent(labeltracks);
        // DATATRACKS
        Element datatracks = new Element("datatracks");
        Element stracks = new Element("scalartrack");
        oI = lcoll.getList().iterator();
        while (oI.hasNext()) {
            ObjectLine tmp = oI.next();
            if (tmp.getType().equals("ScalarTrack")) {
                Element strack = new Element("scatrack");
                strack.setAttribute("name", String.valueOf(tmp.getName()));
                strack.setAttribute("active", String.valueOf(tmp.isActive()));
                strack.setAttribute("type", tmp.getType());
                strack.setAttribute("showpoints", String
                        .valueOf(((ScalarTrack) tmp.getTrack()).isShowPoints()));
                strack.setAttribute("showline", String
                        .valueOf(((ScalarTrack) tmp.getTrack()).isShowLine()));
                strack.setAttribute("min",
                        String.valueOf(((ScalarTrack) tmp.getTrack()).getMin()));
                strack.setAttribute("max",
                        String.valueOf(((ScalarTrack) tmp.getTrack()).getMax()));
                strack.setAttribute("order", String.valueOf(tmp.getOrder()));
                strack.setAttribute("learnable",
                        String.valueOf(tmp.isLearnable()));
                strack.setAttribute("sendToMatlab",
                        String.valueOf(tmp.isSendToMatlab()));
                strack.setAttribute("trackHeight",
                        String.valueOf(tmp.getTrackHeight()));
                strack.setText(((ScalarTrack) tmp.getTrack()).getFileName());
                stracks.addContent(strack);
            }
        }
        datatracks.addContent(stracks);
        // VECTORTRACKS
        Element vtracks = new Element("vectortrack");
        oI = lcoll.getList().iterator();
        while (oI.hasNext()) {
            ObjectLine tmp = oI.next();
            if (tmp.getType().equals("VectorTrack")) {
                Element vtrack = new Element("vectrack");
                vtrack.setAttribute("name", String.valueOf(tmp.getName()));
                vtrack.setAttribute("active", String.valueOf(tmp.isActive()));
                vtrack.setAttribute("type", tmp.getType());
                vtrack.setAttribute("colormap", String
                        .valueOf(((VectorTrack) tmp.getTrack()).getColorMap()
                                .getName()));
                vtrack.setAttribute("min",
                        String.valueOf(((VectorTrack) tmp.getTrack()).getMin()));
                vtrack.setAttribute("max",
                        String.valueOf(((VectorTrack) tmp.getTrack()).getMax()));
                vtrack.setAttribute("dimension", String
                        .valueOf(((VectorTrack) tmp.getTrack()).getDimension()));
                vtrack.setAttribute("order", String.valueOf(tmp.getOrder()));
                vtrack.setAttribute("learnable",
                        String.valueOf(tmp.isLearnable()));
                vtrack.setAttribute("sendToMatlab",
                        String.valueOf(tmp.isSendToMatlab()));
                vtrack.setAttribute("trackHeight",
                        String.valueOf(tmp.getTrackHeight()));
                vtrack.setAttribute("asScalar",
                        String.valueOf(((VectorTrack) tmp.getTrack()).isAsScalar()));
                vtrack.setAttribute("stacked",
                        String.valueOf(((VectorTrack) tmp.getTrack()).isStacked()));
                vtrack.setText(((VectorTrack) tmp.getTrack()).getFileName());

                vtracks.addContent(vtrack);
            }
        }
        datatracks.addContent(vtracks);
        // DATATRACKS
        oI = lcoll.getList().iterator();
        while (oI.hasNext()) {
            ObjectLine tmp = oI.next();
            if (tmp.getType().equals("DataTrack")) {
                Element dataTrack = new Element("datatrack");
                dataTrack.setAttribute("name", String.valueOf(tmp.getName()));
                dataTrack.setAttribute("active", String.valueOf(tmp.isActive()));
                dataTrack.setAttribute("type", tmp.getType());
                dataTrack.setAttribute("colormap", String
                        .valueOf(((DataTrack) tmp.getTrack()).getColorMap()
                                .getName()));
                dataTrack.setAttribute("min",
                        String.valueOf(((DataTrack) tmp.getTrack()).getMin()));
                dataTrack.setAttribute("max",
                        String.valueOf(((DataTrack) tmp.getTrack()).getMax()));
                dataTrack.setAttribute("dimension", String
                        .valueOf(((DataTrack) tmp.getTrack()).getDimension()));
                dataTrack.setAttribute("samplerate", String
                        .valueOf(((DataTrack) tmp.getTrack()).getSampleRate()));
                dataTrack.setAttribute("order", String.valueOf(tmp.getOrder()));
                dataTrack.setAttribute("learnable",
                        String.valueOf(tmp.isLearnable()));
                dataTrack.setAttribute("sendToMatlab",
                        String.valueOf(tmp.isSendToMatlab()));
                dataTrack.setAttribute("trackHeight",
                        String.valueOf(tmp.getTrackHeight()));
                dataTrack.setAttribute("style",
                        String.valueOf(((DataTrack) tmp.getTrack()).getStyleAsString()));
                dataTrack.setText(((DataTrack) tmp.getTrack()).getFileName());

                datatracks.addContent(dataTrack);
            }
        }

        root.addContent(datatracks);

        // MEDIATRACKS
        Element mediatracks = new Element("mediatracks");
        Element audio = new Element("audio");
        Iterator<AudioTrack> aI = mcoll.getAudioList().iterator();
        while (aI.hasNext()) {
            AudioTrack tmp = aI.next();
            Element atrack = new Element("atrack");
            atrack.setAttribute("name", String.valueOf(tmp.getName()));
            atrack.setAttribute("active", String.valueOf(tmp.isActive()));
            atrack.setAttribute("sendToMatlab", String.valueOf(tmp.isSendToMatlab()));

            atrack.setText(tmp.getPath());
            audio.addContent(atrack);
        }
        mediatracks.addContent(audio);
        Element video = new Element("video");
        Iterator<VideoTrack> vI = mcoll.getVideoList().iterator();
        while (vI.hasNext()) {
            VideoTrack tmp = vI.next();
            Element vtrack = new Element("vtrack");
            vtrack.setAttribute("name", String.valueOf(tmp.getName()));
            vtrack.setAttribute("active", String.valueOf(tmp.isActive()));
            vtrack.setAttribute("sendToMatlab", String.valueOf(tmp.isSendToMatlab()));
            vtrack.setText(tmp.getPath());
            video.addContent(vtrack);
        }
        mediatracks.addContent(video);
        root.addContent(mediatracks);

        return new Document(root);
    }

    public String getName() {
        return name;
    }

    public void newProject(String path, String name) {
        // ordner evtl anlegen...

        this.name = name;
        this.projectPath = path + "/" + name + "/";
        this.alternativeMediaPath = projectPath + "media/";

        File projectDir = new File(this.projectPath);
        if (!projectDir.isDirectory()) {
            projectDir.mkdir();
        }

        File classesDir = new File(this.projectPath + "classes/");
        if (!classesDir.isDirectory()) {
            classesDir.mkdir();
        }
        File datatracksDir = new File(this.projectPath + "datatracks/");
        if (!datatracksDir.isDirectory()) {
            datatracksDir.mkdir();
        }
        File labeltracksDir = new File(this.projectPath + "labeltracks/");
        if (!labeltracksDir.isDirectory()) {
            labeltracksDir.mkdir();
        }
        File mediaDir = new File(this.projectPath + "media/");
        if (!mediaDir.isDirectory()) {
            mediaDir.mkdir();
        }

        lcoll.reset();
        mcoll.reset();
        this.setExisting(true);
        MessageManager.getInstance().slotChanged(new SlotEvent(this, 0));
        MessageManager.getInstance().requestTrackUpdate(
                new UpdateTracksEvent(this));
        MessageManager.getInstance().requestRepaint(new RepaintEvent(this));

    }

    public long getTime() {
        return time;
    }

    public String getMediaPath() {
        return alternativeMediaPath;
    }

    public void setMediaPath(String alternativeMediaPath) {
        this.alternativeMediaPath = alternativeMediaPath;
    }
}

package de.atlas.tools;

import de.atlas.data.LabelClassEntity;
import de.atlas.data.LabelObject;
import de.atlas.data.LabelType;
import de.atlas.data.SamplePoint;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ContentFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by smeudt on 27/04/15.
 */
public class IAExpLog2Track {
    private final File logFile;
    private String trackNamePreFix = "";
    private enum TaskState{
        SEARCHING,
        RESPONDING,
        WAITING
    }
    private int[] taskMaxTimes = new int[130];

    public IAExpLog2Track(File logFile) {
        this.logFile = logFile;
        this.trackNamePreFix = logFile.getName().substring(0,25);
        initTaskMaxTimes();
    }


    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("no input file");
            return;
        }
        if (!args[0].endsWith(".xml")) {
            System.out.println("not a XML");
            return;
        }
        if (!new File(args[0]).exists()) {
            System.out.println("file not found");
        }

        IAExpLog2Track parser = new IAExpLog2Track(new File(args[0]));
        parser.createRightWrongClass(parser.getLogFilePath());
        parser.createRoundClass(parser.getLogFilePath());
        parser.createSpeakingClass(parser.getLogFilePath());

        parser.createBehaviourTrack(parser.getLogFilePath());
        parser.createSequenceTrack(parser.getLogFilePath());
        parser.createSpeechTrack(parser.getLogFilePath());
    }

    private void createSequenceTrack(String path) {
        Element logRoot = readLogfile();

        Element trackRoot = new Element("LabelTrack");
        trackRoot.setAttribute("name", "sequences");
        trackRoot.setAttribute("externalchange", "0");
        trackRoot.setAttribute("classname", "sequence");
        trackRoot.setAttribute("isContinuous", "false");
        trackRoot.setAttribute("interpolationType", "1");

        ArrayList<LabelObject> labels = new ArrayList<>();
        LabelObject label = null;
        LabelClassEntity samEntity = new LabelClassEntity(null, "sam", 9, new Color(-4144960), Color.darkGray);
        LabelClassEntity breathEntity = new LabelClassEntity(null, "breath", 7, new Color(-4144960), Color.darkGray);
        LabelClassEntity ratingEntity = new LabelClassEntity(null, "questions", 10, new Color(-4144960), Color.darkGray);
        LabelClassEntity freeEntity = new LabelClassEntity(null, "free", 8, new Color(-4144960), Color.darkGray);
        List<Element> messages = logRoot.getChildren("Message");

        List<Element> applicationLogMessages = attributeFilter(messages, "Topic", "semaine.data.application.log");
        Iterator<Element> i = applicationLogMessages.iterator();
        int sequenceNumber = 0;
        boolean nextIsEnd = false;
        while (i.hasNext()) {
            Element message = i.next();
            if (message.getChildren("TaskStarted").size() > 0) {
                Element e = message.getChild("TaskStarted");
                if(Integer.parseInt(e.getAttributeValue("SequenceTaskNumber"))==1){
                    label = new LabelObject(
                            sequenceNumber==0?"Intro":"Seq: " + sequenceNumber,
                            "",
                            Long.parseLong(message.getAttributeValue("RelativeTime_ms")),
                            Long.parseLong(message.getAttributeValue("RelativeTime_ms"))+1,
                            sequenceNumber,
                            LabelType.AUTOMATIC,
                            null,
                            new LabelClassEntity(null,sequenceNumber==0?"intro":String.valueOf(sequenceNumber),sequenceNumber,new Color(-6882666),Color.darkGray),
                            System.currentTimeMillis());
                    sequenceNumber++;
                }
                if(Integer.parseInt(e.getAttributeValue("SequenceTaskNumber"))==Integer.parseInt(e.getAttributeValue("SequenceTotalTasks"))){
                    nextIsEnd=true;
                }
            }
            if (nextIsEnd&&message.getChildren("TaskComplete").size() > 0) {
                nextIsEnd=false;
                label.setEnd(Long.parseLong(message.getAttributeValue("RelativeTime_ms"))+1000);
                labels.add(label);
            }
        }
        Collections.sort(labels);
        writeXML(new File(path + "/"+trackNamePreFix + "Sequences.xml"), addLabels(trackRoot, labels));

    }

    private void createSpeechTrack(String path) {
        Element logRoot = readLogfile();

        Element trackRoot = new Element("LabelTrack");
        trackRoot.setAttribute("name", "speech");
        trackRoot.setAttribute("externalchange", "0");
        trackRoot.setAttribute("classname", "speechRecognizer");
        trackRoot.setAttribute("isContinuous", "false");
        trackRoot.setAttribute("interpolationType", "1");

        ArrayList<LabelObject> labels = new ArrayList<>();
        LabelObject label = null;
        LabelClassEntity timeOutEntity = new LabelClassEntity(null,"labeled",0,new Color(-4144960), Color.darkGray);
        List<Element> messages = logRoot.getChildren("Message");
        List<Element> applicationLogMessages = attributeFilter(messages, "Topic", "semaine.data.user.speech.log");

        Iterator<Element> i = applicationLogMessages.iterator();
        while (i.hasNext()) {
            Element message = i.next();
            if (message.getChildren("SpeechIgnored").size()>0){
                Element e = (Element) message.getChildren("SpeechIgnored").get(0);
                label = new LabelObject(
                        e.getChild("recognitionResult").getChild("utterance").getChild("text").getValue(),
                        "AudioLevel:" + e.getAttributeValue("AudioLevel") + "Confidence:"+ e.getChild("recognitionResult").getChild("utterance").getChild("semanticConfidence").getValue(),
                        Long.parseLong(message.getAttributeValue("RelativeTime_ms"))+Long.parseLong(e.getChild("recognitionResult").getAttributeValue("onset")),
                        Long.parseLong(message.getAttributeValue("RelativeTime_ms"))+Long.parseLong(e.getChild("recognitionResult").getAttributeValue("offset")),
                        Double.parseDouble(e.getChild("recognitionResult").getChild("utterance").getChild("semanticConfidence").getValue()),
                        LabelType.AUTOMATIC,
                        null,
                        new LabelClassEntity(null,"ignored",1,new Color(-6882666),Color.darkGray),
                        System.currentTimeMillis());
                labels.add(label);
            }
        }

        applicationLogMessages = attributeFilter(messages, "Topic", "semaine.data.user.speech");

        i = applicationLogMessages.iterator();
        while (i.hasNext()) {
            Element message = i.next();
            if (message.getChildren("recognitionResult").size()>0){
                Element e = (Element) message.getChildren("recognitionResult").get(0);
                label = new LabelObject(
                        e.getChild("utterance").getChild("text").getValue(),
                        "Confidence:"+ e.getChild("utterance").getChild("semanticConfidence").getValue(),
                        Long.parseLong(message.getAttributeValue("RelativeTime_ms"))+Long.parseLong(e.getAttributeValue("onset")),
                        Long.parseLong(message.getAttributeValue("RelativeTime_ms"))+Long.parseLong(e.getAttributeValue("offset")),
                        Double.parseDouble(e.getChild("utterance").getChild("semanticConfidence").getValue()),
                        LabelType.AUTOMATIC,
                        null,
                        new LabelClassEntity(null,"accepted",0,new Color(-6882666),Color.darkGray),
                        System.currentTimeMillis());
                labels.add(label);
            }
        }
        Collections.sort(labels);
        writeXML(new File(path + "/"+trackNamePreFix +"SpeechRecognizer.xml"), addLabels(trackRoot, labels));

    }

    private String getLogFilePath() {
        return logFile.getParentFile().getPath();
    }

    public void createBehaviourTrack(String path){
        Element logRoot = readLogfile();

        Element timeOutTrackRoot = new Element("LabelTrack");
        timeOutTrackRoot.setAttribute("name", "timeOut");
        timeOutTrackRoot.setAttribute("externalchange", "0");
        timeOutTrackRoot.setAttribute("classname", "generic");
        timeOutTrackRoot.setAttribute("isContinuous", "false");
        timeOutTrackRoot.setAttribute("interpolationType", "1");

        ArrayList<LabelObject> timeOutLabels = new ArrayList<>();
        LabelObject timeOutLabel = null;
        LabelClassEntity timeOutEntity = new LabelClassEntity(null,"labeled",0,new Color(-4144960), Color.darkGray);

        Element trackRoot = new Element("LabelTrack");
        trackRoot.setAttribute("name", "probandBehaviour");
        trackRoot.setAttribute("externalchange", "0");
        trackRoot.setAttribute("classname", "behaviour");
        trackRoot.setAttribute("isContinuous", "false");
        trackRoot.setAttribute("interpolationType", "1");

        ArrayList<LabelObject> labels = new ArrayList<>();
        LabelObject label = null;
        LabelClassEntity searchingEntity = new LabelClassEntity(null,"search",0,new Color(-106), Color.darkGray);
        LabelClassEntity rightEntity = new LabelClassEntity(null,"right",1,new Color(-6882666), Color.darkGray);
        LabelClassEntity wrongEntity = new LabelClassEntity(null,"wrong",2,new Color(-26986), Color.darkGray);

        List<Element> messages = logRoot.getChildren("Message");
        List<Element> applicationLogMessages = attributeFilter(messages, "Topic", "semaine.data.application.log");

        Iterator<Element> i = applicationLogMessages.iterator();
        TaskState state = TaskState.WAITING;
        boolean timedOut = false;
        int taskNumber = 0;
        while (i.hasNext()){
            Element message = i.next();
            switch (state) {
                case WAITING:
                    if (message.getChildren("TaskStarted").size()>0){
                        Element e = (Element) message.getChildren("TaskStarted").get(0);
                        label = new LabelObject(
                                "Task: " + e.getAttributeValue("TotalTaskNumber"),
                                "SeqTask: " + e.getAttributeValue("SequenceTaskNumber") + "  Difficulty: " + e.getAttributeValue("LevelOfDifficulty") + "  Sol: " + e.getAttributeValue("Solitaire"),
                                Long.parseLong(message.getAttributeValue("RelativeTime_ms")),
                                Long.parseLong(message.getAttributeValue("RelativeTime_ms")) + 1,
                                0.0,
                                LabelType.AUTOMATIC,
                                null,
                                searchingEntity,
                                System.currentTimeMillis());
                        state = TaskState.SEARCHING;
                        taskNumber = Integer.parseInt(e.getAttributeValue("TotalTaskNumber"));
                    }

                    break;
                case SEARCHING:
                    if (message.getChildren("TaskComplete").size()>0) {
                        label.setEnd(Long.parseLong(message.getAttributeValue("RelativeTime_ms")) - 1);
                        labels.add(label);
                        Element e = (Element) message.getChildren("TaskComplete").get(0);
                        LabelClassEntity entity = rightEntity;
                        if (e.getAttributeValue("Result").equalsIgnoreCase("false")) entity = wrongEntity;
                        label = new LabelObject(
                                e.getAttributeValue("Result"),
                                "Time: " + e.getAttributeValue("CompletionTime_s"),
                                Long.parseLong(message.getAttributeValue("RelativeTime_ms")),
                                Long.parseLong(message.getAttributeValue("RelativeTime_ms")) + 3000,
                                0.0,
                                LabelType.AUTOMATIC,
                                null,
                                entity,
                                System.currentTimeMillis());
                        state = TaskState.RESPONDING;

                        double completionTime = labels.get(labels.size()-1).getEnd()-labels.get(labels.size()-1).getStart();
                        if(taskMaxTimes[taskNumber-1]<completionTime) {
                            timeOutLabel = new LabelObject("TimeOut", "",
                                    Long.parseLong(message.getAttributeValue("RelativeTime_ms")) - (long)(completionTime - taskMaxTimes[taskNumber-1]),
                                    Long.parseLong(message.getAttributeValue("RelativeTime_ms")) - (long)(completionTime - taskMaxTimes[taskNumber-1]) + 3000,
                                    0.0, LabelType.AUTOMATIC, null, timeOutEntity, System.currentTimeMillis());
                            timedOut = true;
                        }
                    }
                    break;
                case RESPONDING:
                    if (message.getChildren("TaskStarted").size()>0) {
                        label.setEnd(Long.parseLong(message.getAttributeValue("RelativeTime_ms")) - 1);
                        labels.add(label);
                        if(timedOut) {
                            timeOutLabel.setEnd(Long.parseLong(message.getAttributeValue("RelativeTime_ms")) - 1);
                            timeOutLabels.add(timeOutLabel);
                            timedOut = false;
                        }
                        Element e = (Element) message.getChildren("TaskStarted").get(0);
                        label = new LabelObject(
                                "Task " + e.getAttributeValue("TotalTaskNumber"),
                                "SeqTask:" + e.getAttributeValue("SequenceTaskNumber") + "  Difficulty:" + e.getAttributeValue("LevelOfDifficulty") + "  Sol:" + e.getAttributeValue("Solitaire"),
                                Long.parseLong(message.getAttributeValue("RelativeTime_ms")),
                                Long.parseLong(message.getAttributeValue("RelativeTime_ms")) + 1,
                                0.0,
                                LabelType.AUTOMATIC,
                                null,
                                searchingEntity,
                                System.currentTimeMillis());
                        state = TaskState.SEARCHING;
                        taskNumber = Integer.parseInt(e.getAttributeValue("TotalTaskNumber"));
                        break;
                    } else if (message.getChildren("SAM_Valenz").size()>0) {
                        if(timedOut) {
                            timeOutLabels.add(timeOutLabel);
                            timedOut = false;
                        }
                        labels.add(label);
                        state = TaskState.WAITING;
                    }
            }
        }
//        writeXML(new File(path + "/applicationLog.xml"), applicationLogMessages.get(0));
        Collections.sort(labels);
        writeXML(new File(path+"/"+trackNamePreFix+"Behaviour.xml"),addLabels(trackRoot,labels));

        Collections.sort(timeOutLabels);
        writeXML(new File(path + "/"+trackNamePreFix+"TimeOut.xml"), addLabels(timeOutTrackRoot, timeOutLabels));

    }

    private List<Element> attributeFilter(List<Element> list, String attribute, String value){
        List<Element> outList = new ArrayList<>();

        Iterator<Element> i = list.iterator();
        while (i.hasNext()) {
            Element e = i.next();
            if (e.getAttribute(attribute).getValue().equalsIgnoreCase(value)) outList.add(e);
        }
        return outList;
    }

    private Element addLabels(Element root, ArrayList<LabelObject> labels){
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
                classentity.setText(String.valueOf(tmp.getLabelClassEntity().getName()));
            } else {
                classentity.setText(String.valueOf("none"));
            }
            label.addContent(classentity);
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
        return root;
    }

    private void writeXML(File file, Element root){
        try {
            Document doc = new Document(root);
            FileOutputStream out = new FileOutputStream(file);
            XMLOutputter serializer = new XMLOutputter();
            serializer.setFormat(Format.getPrettyFormat());
            serializer.output(doc, out);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private Element readLogfile(){
        try {
            SAXBuilder sxBuild = new SAXBuilder();
            InputSource is = new InputSource(new FileInputStream(logFile.getPath()));
            Document logDocument;
            logDocument = sxBuild.build(is);
            Element logRoot = logDocument.getRootElement();
            return logRoot;

        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void writeTextFile(File file, String text){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createRightWrongClass(String path){
        writeTextFile(new File(path+"/behaviour.xml"),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "   <LabelClass classname=\"behaviour\">" +
                        "       <entity>" +
                        "           <name>right</name>" +
                        "           <color>-6882666</color>" +
                        "           <id>0</id>" +
                        "       </entity>" +
                        "       <entity>" +
                        "           <name>wrong</name>" +
                        "           <color>-26986</color>" +
                        "           <id>1</id>" +
                        "       </entity>" +
                        "       <entity>" +
                        "           <name>search</name>" +
                        "           <color>-106</color>" +
                        "           <id>3</id>" +
                        "       </entity>" +
                        "   </LabelClass>"
        );
    }

    private void createRoundClass(String path){
        writeTextFile(new File(path+"/sequence.xml"),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "   <LabelClass classname=\"sequence\">" +
                        "       <entity>" +
                        "           <name>intro</name>" +
                        "           <color>-3355393</color>" +
                        "           <continuousColor>-12566464</continuousColor>" +
                        "           <id>0</id>" +
                        "       </entity>" +
                        "       <entity>" +
                        "           <name>1</name>" +
                        "           <color>-13369447</color>" +
                        "           <continuousColor>-12566464</continuousColor>" +
                        "           <id>1</id>" +
                        "       </entity>" +
                        "       <entity>" +
                        "           <name>2</name>" +
                        "           <color>-10027213</color>" +
                        "           <continuousColor>-12566464</continuousColor>" +
                        "           <id>2</id>" +
                        "       </entity>" +
                        "       <entity>" +
                        "           <name>3</name>" +
                        "           <color>-205</color>" +
                        "           <continuousColor>-12566464</continuousColor>" +
                        "           <id>3</id>" +
                        "       </entity>" +
                        "       <entity>" +
                        "           <name>4</name>" +
                        "           <color>-26317</color>" +
                        "           <continuousColor>-12566464</continuousColor>" +
                        "           <id>4</id>" +
                        "       </entity>" +
                        "       <entity>" +
                        "           <name>5</name>" +
                        "           <color>-65485</color>" +
                        "           <continuousColor>-12566464</continuousColor>" +
                        "           <id>5</id>" +
                        "       </entity>" +
                        "       <entity>" +
                        "           <name>6</name>" +
                        "           <color>-10079233</color>" +
                        "           <continuousColor>-12566464</continuousColor>" +
                        "           <id>6</id>" +
                        "       </entity>" +
                        "       <entity>" +
                        "           <name>breath</name>" +
                        "           <color>-3355393</color>" +
                        "           <continuousColor>-12566464</continuousColor>" +
                        "           <id>7</id>" +
                        "       </entity>" +
                        "       <entity>" +
                        "           <name>free</name>" +
                        "           <color>-16737895</color>" +
                        "           <continuousColor>-12566464</continuousColor>" +
                        "           <id>8</id>" +
                        "       </entity>" +
                        "       <entity>" +
                        "           <name>SAM</name>" +
                        "           <color>-13369345</color>" +
                        "           <continuousColor>-12566464</continuousColor>" +
                        "           <id>9</id>" +
                        "       </entity>" +
                        "       <entity>" +
                        "           <name>questions</name>" +
                        "           <color>-3342337</color>" +
                        "           <continuousColor>-12566464</continuousColor>" +
                        "           <id>10</id>" +
                        "       </entity>" +
                        "   </LabelClass>"
        );
    }

    private void createSpeakingClass(String path){
        writeTextFile(new File(path+"/speechRecognizer.xml"),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "   <LabelClass classname=\"speechRecognizer\">" +
                        "       <entity>" +
                        "           <name>accepted</name>" +
                        "           <color>-10027162</color>" +
                        "           <continuousColor>-12566464</continuousColor>" +
                        "           <id>0</id>" +
                        "       </entity>" +
                        "       <entity>" +
                        "           <name>ignored</name>" +
                        "           <color>-26368</color>" +
                        "           <continuousColor>-12566464</continuousColor>" +
                        "           <id>1</id>" +
                        "       </entity>" +
                        "   </LabelClass>"
        );
    }

    private void initTaskMaxTimes() {
        for(int i=0;i<taskMaxTimes.length;i++)taskMaxTimes[i]=7500;
    }

}

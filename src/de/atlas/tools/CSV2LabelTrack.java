package de.atlas.tools; /**
 * Created by smeudt on 15.10.14.
 */
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;


public class CSV2LabelTrack {
    private static String csvSplitRegex_ = "[,;\t]";

    public static void main( String[] args){
        if(args.length<1){
            System.out.println("no input file\n");
            printUsage();
            return;
        }
        if (args[0].equalsIgnoreCase("--help")){
            printUsage();
            return;
        }
        String trackName=args[0].substring(0,args[0].length()-4);
        String className = "generic";
        String isContinous = "false";
        String interpolationType = "1";
        String externalChange = "0";

        for(int i=1;i<args.length;i++){
            String[] arg = args[i].split("=");
            if(arg[0].equalsIgnoreCase("-name"))trackName = arg[1];
            if(arg[0].equalsIgnoreCase("-labelClass"))className = arg[1];
            if(arg[0].equalsIgnoreCase("-isContinuous"))isContinous = arg[1];
            if(arg[0].equalsIgnoreCase("-interpolationType"))interpolationType = arg[1];
            if(arg[0].equalsIgnoreCase("-externalChange"))externalChange = arg[1];
        }

        Element root = new Element("LabelTrack");
        root.setAttribute("name", trackName);
        root.setAttribute("externalchange", externalChange);
        root.setAttribute("classname", className);
        root.setAttribute("isContinuous", isContinous);
        root.setAttribute("interpolationType", interpolationType);

        RandomAccessFile in;
        try {
            in = new RandomAccessFile(args[0],"r");
            int startIDX = -1;
            int endIDX = -1;
            int timeStampIDX = -1;
            int valueIDX = -1;
            int comentIDX = -1;
            int typeIDX = -1;
            int textIDX = -1;
            int entityIDX = -1;
            int contCount = 0;
            int[] xIDX = null;
            int[] yIDX = null;

            String[] header = in.readLine().split(csvSplitRegex_);
            for(int i=0;i<header.length;i++){
                if(header[i].equalsIgnoreCase("start"))startIDX=i;
                if(header[i].equalsIgnoreCase("end"))endIDX=i;
                if(header[i].equalsIgnoreCase("timeStamp"))timeStampIDX=i;
                if(header[i].equalsIgnoreCase("value"))valueIDX=i;
                if(header[i].equalsIgnoreCase("comment"))comentIDX=i;
                if(header[i].equalsIgnoreCase("type"))typeIDX=i;
                if(header[i].equalsIgnoreCase("text"))textIDX=i;
                if(header[i].equalsIgnoreCase("entity"))entityIDX=i;
                if(header[i].startsWith("x"))contCount++;
            }
            if(contCount>0){
                xIDX = new int[contCount];
                yIDX = new int[contCount];
            }
            for(int i=0;i<header.length;i++){
                if(header[i].startsWith("x")){
                    xIDX[Integer.parseInt(header[i].substring(1))-1]=i;
                }
                if(header[i].startsWith("y")){
                    yIDX[Integer.parseInt(header[i].substring(1))-1]=i;
                }
            }

            String line;
            // create labels in Track
            while ((line = in.readLine()) != null) {
                String[] segments = line.split(csvSplitRegex_);

                Element label = new Element("label");

                Element starttime = new Element("starttime");
                starttime.setText(segments[startIDX]);
                label.addContent(starttime);

                Element endtime = new Element("endtime");
                endtime.setText(segments[endIDX]);
                label.addContent(endtime);

                Element timestamp = new Element("timestamp");
                timestamp.setText(timeStampIDX < 0 ? String.valueOf(System.currentTimeMillis()) : segments[timeStampIDX]);
                label.addContent(timestamp);

                Element value = new Element("value");
                value.setText(valueIDX<0?"0":segments[valueIDX]);
                label.addContent(value);

                Element comment = new Element("comment");
                comment.setText(comentIDX<0?"":segments[comentIDX]);
                label.addContent(comment);

                Element type = new Element("type");
                type.setText(typeIDX<0?"MANUAL":segments[typeIDX]);
                label.addContent(type);

                Element text = new Element("text");
                text.setText(textIDX<0?"":segments[textIDX]);
                label.addContent(text);

                Element classentity = new Element("classentity");
                classentity.setText(entityIDX<0?"none":segments[entityIDX]);
                label.addContent(classentity);

                if(contCount>0) {
                    Element samplingPoints = new Element("continuousSamplingPoints");
                    for(int i=0;i<contCount;i++) {
                        Element esp = new Element("samplePoint");
                        esp.setAttribute("t",segments[xIDX[i]]);
                        esp.setAttribute("y",segments[yIDX[i]]);
                        samplingPoints.addContent(esp);
                    }
                    label.addContent(samplingPoints);
                }
                root.addContent(label);
            }

            Document doc = new Document(root);
            FileOutputStream out = new FileOutputStream(trackName+".xml");
            XMLOutputter serializer = new XMLOutputter();
            serializer.setFormat(Format.getPrettyFormat());
            serializer.output(doc, out);

            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found\n");
            printUsage();
            //e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Something went wrong\n");
            e.printStackTrace();
        }
    }
    private static void printUsage(){
        System.out.println("Usage: java -jar CSV2LabelTrack.jar INFILE [OPTIONS]");
        System.out.println("");
        System.out.println("OPTIONS:");
        System.out.println("-name=NAME                 sets the LabelTrackName.");
        System.out.println("                           Default: INFILE without extension");
        System.out.println("-labelClass=LABELCLASS     sets the LabelClass of the Track.");
        System.out.println("                           Default: 'generic'");
        System.out.println("-isContinuous=true/false   sets the continuous annotation flag.");
        System.out.println("                           Default: false");
        System.out.println("-interpolationType=1/2/3/4 sets the interpolation type in case of ");
        System.out.println("                           continuous annotation. 1: Linear (default)");
        System.out.println("                           2: B-Spline, 3: SoftEdge, 4: Stairs");
        System.out.println("-externalChange=TIMESTAMP  sets the externalChange time.");
        System.out.println("                           Default: 0");
        System.out.println("");
        System.out.println("INFILE:");
        System.out.println("csv file with ',' ';' or 'tabulator' as delimiter. Decimal delimiter '.' !!!");
        System.out.println("Headline indicates used columns.");
        System.out.println("start       : Starttime of label in milliseconds (obligatory)");
        System.out.println("end         : Endtime of label in milliseconds (obligatory)");
        System.out.println("timestamp   : Timestamp of label creation time in milliseconds (optional) ");
        System.out.println("              default: systemtime at time of csv conversion");
        System.out.println("value       : Value of label (optional) default: 0");
        System.out.println("comment     : Comment of label (optional)");
        System.out.println("type        : Type of label (optional) default 'MANUAL'");
        System.out.println("              For more information see ATLAS helpfiles");
        System.out.println("text        : Text of label (optional)");
        System.out.println("entity      : ClassEntity from LABELCLASS attached to the label (optional)");
        System.out.println("              default: 'none'");
        System.out.println("xN          : x value in milliseconds for continuous labels (optional)");
        System.out.println("              x>=0, x<=length of label");
        System.out.println("yN          : y value for continuous labels (optional) y>=0, y<=1");
        System.out.println("              N indicates the number of the value.");
        System.out.println("              At least 3 (x,y) pairs must be given");
        System.out.println("              x1 should be set to 0, xLAST should be set to labelLength.");
        System.out.println("");
        System.out.println("Crisp INFILE Example containing 2 labels:");
        System.out.println("start,end,value,text");
        System.out.println("200,500,0.6,hello");
        System.out.println("600,900,1.5,world");
        System.out.println("");
        System.out.println("continuous INFILE Example containing 2 labels of class 'generic':");
        System.out.println("start,end,value,text,entity,type,x1,y1,x2,y2,x3,y3,x4,y4");
        System.out.println("200,500,0.6,hello,labeled,MANUAL,0,0.1,150,0.8,260,0.7,300,0.3");
        System.out.println("600,900,1.5,world,none,AUTOMATIC,0,0.2,100,0.1,200,0.6,900,0.5");
        System.out.println("");
    }
}

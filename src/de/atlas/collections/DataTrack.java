package de.atlas.collections;

import de.atlas.colormap.*;
import de.atlas.data.Project;
import de.atlas.data.VectorSample;
import de.atlas.messagesystem.*;
import de.atlas.misc.HelperFunctions;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.InputSource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.DoubleBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataTrack extends JComponent {

    private static final long serialVersionUID = 1L;

    public static final int STYLE_STACKED = 1;
    public static final int STYLE_MATRIX = 2;
    public static final int STYLE_LINES = 3;
    public static final int STYLE_POINTS = 4;
    public static final int STYLE_LINES_POINTS = 5;


    private ArrayList<VectorSample> data;
    private double min,max;
    private BufferedImage img;
    private int start;
    //private double zoom=1.0;
    private File file;
    private String name;
    private int dimension;
    private double sampleRate = 0;
    private RandomAccessFile raf;
    private FileChannel fChannel;
    private DoubleBuffer dataBuffer;
    private ColorMap colorMap;
    private JCheckBoxMenuItem colorful;
    private JCheckBoxMenuItem fire;
    private JCheckBoxMenuItem gray;
    private JCheckBoxMenuItem islands;
    private JCheckBoxMenuItem ocean;
    private JCheckBoxMenuItem sun;
    private JCheckBoxMenuItem linesStyleMenu;
    private JCheckBoxMenuItem linesPointsStyleMenu;
    private JCheckBoxMenuItem matrixStyleMenu;
    private JCheckBoxMenuItem stackStyleMenu;
    private JCheckBoxMenuItem pointsStyleMenu;
    private boolean showLegend = false;
    private int style = DataTrack.STYLE_LINES;

    public DataTrack(int length, String file, String name,ColorMap cm,double min,double max,int dimension, double sampleRate){
        this.setBounds(0, 0, 1, ObjectLine.MEDIUM);
        //this.setMinimumSize(new Dimension(0,0));
        this.file = new File(file);
        this.name=name;
        this.colorMap=cm;
        this.min = min;
        this.max = max;
        this.dimension = dimension;
        this.sampleRate = sampleRate;
        MessageManager.getInstance().addSlotChangedListener(new SlotChangedListener(){
            @Override
            public void slotChanged(SlotEvent e) {
                updateSlot(e.getStart());
            }
        });
        MessageManager.getInstance().addZoomChangedListener(new ZoomChangedListener(){
            @Override
            public void zoomChanged(ZoomEvent e) {
                drawIMG(start,e.getZoom());
                repaint();
            }
        });
        MessageManager.getInstance().addMinMaxChangedListener(new MinMaxChangedListener(){

            @Override
            public void minMaxChanged(MinMaxChangedEvent e) {
                maxMinChanged(e.getObjectLine());

            }

        });

        this.addComponentListener(new ComponentListener(){

            @Override
            public void componentHidden(ComponentEvent e) {

            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
                drawIMG(start, Project.getInstance().getZoom());
                repaint();
            }

            @Override
            public void componentShown(ComponentEvent e) {

            }});

        //this.generateRandomData();
        if(this.file.getName().endsWith(".raw")){
            try {
                raf = new RandomAccessFile(file,"r");
                fChannel = raf.getChannel();
                dataBuffer = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size()).asDoubleBuffer();

                double time=dataBuffer.limit()*1000.0/sampleRate;
                if(sampleRate==0) {
                    while (dataBuffer.hasRemaining()) {
                        time = dataBuffer.get();//time
                        for (int i = 0; i < this.dimension; i++) {
                            dataBuffer.get();
                        }
                    }
                }
                Project.getInstance().setProjectLength(time);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        drawIMG(start,Project.getInstance().getZoom());
    }

    public int getDimension() {
        return dimension;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getPath(){
        return file.getPath();
    }

    public String getFileName(){
        return file.getName();
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    private void updateSlot(int start){
        this.start=start;
        drawIMG(start, Project.getInstance().getZoom());
        this.repaint();
    }


    public ColorMap getColorMap() {
        return colorMap;
    }

    public void setColorMap(ColorMap colorMap) {
        this.colorMap = colorMap;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double[][] getDataWithTime(){
            dataBuffer.rewind();

            ArrayList<Double> dataArrayList = new ArrayList<>();
            while (dataBuffer.hasRemaining()) {
                dataArrayList.add(dataBuffer.get());
            }
            Iterator<Double> iterator = dataArrayList.iterator();
            double[][] outData = new double[dataArrayList.size() / (dimension + 1)][dimension + 1];
            for (int i = 0; i < dataArrayList.size() / (dimension + 1); i++) {
                if(sampleRate==0) {
                    outData[i][0] = iterator.next();
                }else{
                    outData[i][0] = i*1000.0/sampleRate;
                }
                for (int d = 1; d < dimension + 1; d++) {
                    outData[i][d] = iterator.next();
                }
            }
            return outData;
    }

    public double[][] getData(long from, long to) {
        if (from > to) return null;
        if (to < from) return null;

        dataBuffer.rewind();
        if (sampleRate == 0) {
            while (dataBuffer.hasRemaining()) {
                if (dataBuffer.get() >= from) break; //time
                for (int i = 0; i < this.dimension; i++) dataBuffer.get();//data
            }
            if (!dataBuffer.hasRemaining()) return null;
            dataBuffer.position(dataBuffer.position() - 1 < 0 ? 0 : dataBuffer.position() - 1);
            ArrayList<Double[]> data = new ArrayList<Double[]>();
            while (dataBuffer.hasRemaining() && dataBuffer.get() <= to) {
                Double[] tmp = new Double[this.dimension];
                for (int i = 0; i < this.dimension; i++) tmp[i] = dataBuffer.get();//data
                data.add(tmp);
            }
            double[][] ret = new double[data.size()][this.dimension];
            Iterator<Double[]> iD = data.iterator();
            int i = 0;
            while (iD.hasNext()) {
                Double[] tmp = iD.next();
                for (int d = 0; d < this.dimension; d++) ret[i][d] = tmp[d];
                i++;
            }
            return ret;
        } else {
            long firstSample = (long) (from / 1000.0 * sampleRate);
            long lastSample = (long) (to / 1000.0 * sampleRate);
            double[][] ret = new double[(int) (lastSample - firstSample)][dimension];
            dataBuffer.position((int) firstSample * dimension);
            for (long i = 0; i < lastSample - firstSample; i++) {
                for (int d = 0; d < dimension; d++) {
                    ret[(int) i][d] = dataBuffer.get();
                }
                if (!dataBuffer.hasRemaining()) break;
            }
            return ret;
        }
    }

    private void drawIMG(int start, double zoom){
        if(this.getParent()==null)return;
        if(!this.getParent().isVisible())return;

        img = new BufferedImage( this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.white);
        g2.fillRect(0, 0, img.getWidth(), img.getHeight());

        if(sampleRate!=0){
            switch (style) {
                case DataTrack.STYLE_LINES:
                    drawSampledLineIMG(start, zoom, g2);
                    break;
                case DataTrack.STYLE_LINES_POINTS:
                    drawSampledLinePointsIMG(start, zoom, g2);
                    break;
                case DataTrack.STYLE_MATRIX:
                    drawSampledMatrixIMG(start, zoom, g2);
                    break;
                case DataTrack.STYLE_POINTS:
                    drawSampledPointsIMG(start, zoom, g2);
                    break;
                case DataTrack.STYLE_STACKED:
                    drawSampledStackIMG(start, zoom, g2);
                    break;
            }
        }else {
            switch (style) {
                case DataTrack.STYLE_LINES:
                    drawUnSampledLineIMG(start, zoom, g2);
                    break;
                case DataTrack.STYLE_LINES_POINTS:
                    drawUnSampledLinePointsIMG(start, zoom, g2);
                    break;
                case DataTrack.STYLE_MATRIX:
                    drawUnSampledMatrixIMG(start, zoom, g2);
                    break;
                case DataTrack.STYLE_POINTS:
                    drawUnSampledPointsIMG(start, zoom, g2);
                    break;
                case DataTrack.STYLE_STACKED:
                    drawUnSampledStackIMG(start, zoom, g2);
                    break;
            }
        }
    }

    private void drawUnSampledStackIMG(int start, double zoom, Graphics2D g2) {
        double scalarScaling= this.getHeight()/(this.max-this.min);
        double rate = ((dataBuffer.limit() / (this.dimension + 1)) / dataBuffer.get((int) (dataBuffer.limit() - (this.dimension + 1)))) * 1000.0;
        double time = 0;

        dataBuffer.position(getUnsampledDrawStart());
        if (!dataBuffer.hasRemaining()) {
            return;
        }

        int old_x = -100;
        int xstart = 0;
        int shift = (int) ((1.0 / zoom) * (rate / 1000.0)) * (this.dimension + 1);
        if (shift < (this.dimension + 1)) {
            shift = (this.dimension + 1);
        }

        int old_y[] = new int[this.dimension];

        while ((time < (start + this.getWidth() / zoom)) && dataBuffer.hasRemaining()) {
            if (dataBuffer.position() >= dataBuffer.limit()) {
                break;
            }
            int pos = dataBuffer.position();

            time = dataBuffer.get(pos);
            xstart = (int) (((time) - start) * zoom);
            if (old_x == xstart) {//rechts
                while (pos < (dataBuffer.limit() - (this.dimension + 1)) && old_x == xstart) {
                    pos = pos + (this.dimension + 1);
                    time = dataBuffer.get(pos);
                    xstart = (int) (((time) - start) * zoom);
                }
            } else if (old_x < xstart) {//links
                boolean test = false;
                while (pos - (this.dimension + 1) > 0 && old_x < xstart) {
                    pos = pos - (this.dimension + 1);
                    time = dataBuffer.get(pos);
                    xstart = (int) (((time) - start) * zoom);
                    test = true;
                }
                if (test) {
                    pos = pos + (this.dimension + 1);
                }
                time = dataBuffer.get(pos);
                dataBuffer.position(pos);
                xstart = (int) (((time) - start) * zoom);
            }

            dataBuffer.get();

            int yOffset = 0;
            int[] tmp_old_y = new int[dimension];
            for (int d = 0; d < this.dimension && dataBuffer.hasRemaining(); d++) {
                g2.setColor(colorMap.getColor((double) d / (double) this.dimension));
                int yDiff = (int) ((dataBuffer.get() - 0) * scalarScaling);
                int new_y = this.getHeight() - yDiff - yOffset;
                Polygon p = new Polygon();
                p.addPoint(old_x, old_y[d]);
                p.addPoint(xstart, new_y);
                p.addPoint(xstart, new_y + yDiff);
                p.addPoint(old_x, d == 0 ? this.getHeight() : old_y[d - 1]);
                g2.fillPolygon(p);
                tmp_old_y[d] = new_y;
                yOffset += yDiff;
            }
            old_y = tmp_old_y;

            old_x = xstart;
            if (dataBuffer.limit() >= pos + shift) {
                dataBuffer.position(pos + shift);
            }
        }
    }

    private void drawUnSampledPointsIMG(int start, double zoom, Graphics2D g2) {
        double scalarScaling= this.getHeight()/(this.max-this.min);
        double rate = ((dataBuffer.limit() / (this.dimension + 1)) / dataBuffer.get((int) (dataBuffer.limit() - (this.dimension + 1)))) * 1000.0;
        double time = 0;

        dataBuffer.position(getUnsampledDrawStart());

        int old_x = -100;
        int xstart = 0;
        int shift = (int) ((1.0 / zoom) * (rate / 1000.0)) * (this.dimension + 1);
        if (shift < (this.dimension + 1)) {
            shift = (this.dimension + 1);
        }

        int old_y[] = new int[this.dimension];

        while ((time < (start + this.getWidth() / zoom)) && dataBuffer.hasRemaining()) {
            if (dataBuffer.position() >= dataBuffer.limit()) break;
            int pos = dataBuffer.position();

            time = dataBuffer.get(pos);
            xstart = (int) (((time) - start) * zoom);
            if (old_x == xstart) {//rechts
                while (pos < (dataBuffer.limit() - (this.dimension + 1)) && old_x == xstart) {
                    pos = pos + (this.dimension + 1);
                    time = dataBuffer.get(pos);
                    xstart = (int) (((time) - start) * zoom);
                }
            } else if (old_x < xstart) {//links
                boolean test = false;
                while (pos - (this.dimension + 1) > 0 && old_x < xstart) {
                    pos = pos - (this.dimension + 1);
                    time = dataBuffer.get(pos);
                    xstart = (int) (((time) - start) * zoom);
                    test = true;
                }
                if (test) {
                    pos = pos + (this.dimension + 1);
                }
                time = dataBuffer.get(pos);
                dataBuffer.position(pos);
                xstart = (int) (((time) - start) * zoom);
            }

            dataBuffer.get();

            for (int y = 0; y < this.dimension && dataBuffer.hasRemaining(); y++) {
                g2.setColor(colorMap.getColor((double) y / (double) this.dimension));
                int ystart = (int) (this.getHeight() - ((dataBuffer.get() - this.min) * scalarScaling));
                g2.drawOval(xstart-2, ystart-2,4,4);
                old_y[y] = ystart;
            }
            old_x = xstart;
            if (dataBuffer.limit() >= pos + shift) {
                dataBuffer.position(pos + shift);
            }
        }
    }

    private void drawUnSampledMatrixIMG(int start, double zoom, Graphics2D g2) {
        double scalarScaling= this.getHeight()/(this.max-this.min);
        double dimScaling = this.getHeight() / (double) this.dimension;
        double colorScale = 1.0 / (this.max - this.min);
        double rate = ((dataBuffer.limit() / (this.dimension + 1)) / dataBuffer.get((int) (dataBuffer.limit() - (this.dimension + 1)))) * 1000.0;
        double time = 0;

        dataBuffer.position(getUnsampledDrawStart());
        if (!dataBuffer.hasRemaining()) {
            return;
        }

        int old_x = -100;
        int xstart = 0;
        int shift = (int) ((1.0 / zoom) * (rate / 1000.0)) * (this.dimension + 1);
        if (shift < (this.dimension + 1)) {
            shift = (this.dimension + 1);
        }

        int old_y[] = new int[this.dimension];

        while ((time < (start + this.getWidth() / zoom)) && dataBuffer.hasRemaining()) {
            if (dataBuffer.position() >= dataBuffer.limit()) {
                break;
            }
            int pos = dataBuffer.position();

            time = dataBuffer.get(pos);
            xstart = (int) (((time) - start) * zoom);
            if (old_x == xstart) {//rechts
                while (pos < (dataBuffer.limit() - (this.dimension + 1)) && old_x == xstart) {
                    pos = pos + (this.dimension + 1);
                    time = dataBuffer.get(pos);
                    xstart = (int) (((time) - start) * zoom);
                }
            } else if (old_x < xstart) {//links
                boolean test = false;
                while (pos - (this.dimension + 1) > 0 && old_x < xstart) {
                    pos = pos - (this.dimension + 1);
                    time = dataBuffer.get(pos);
                    xstart = (int) (((time) - start) * zoom);
                    test = true;
                }
                if (test) {
                    pos = pos + (this.dimension + 1);
                }
                time = dataBuffer.get(pos);
                dataBuffer.position(pos);
                xstart = (int) (((time) - start) * zoom);
            }

            dataBuffer.get();

            for (int y = 0; y < this.dimension && dataBuffer.hasRemaining(); y++) {
                g2.setColor(colorMap.getColor((dataBuffer.get() - min) * colorScale));
                int ystart = (int) (y * dimScaling);
                int swidth = xstart - old_x + 1;
                int sheight = (int) (dimScaling + 1.0);
                g2.fillRect(xstart, ystart, swidth, sheight);
            }

            old_x = xstart;
            if (dataBuffer.limit() >= pos + shift) {
                dataBuffer.position(pos + shift);
            }
        }

        for (int i = 0; i < this.getHeight() && this.showLegend; i++) {
            g2.setColor(colorMap.getColor(1.0 - ((double) i / (double) this.getHeight())));
            g2.fillRect(0, i, 10, 1);
        }
    }

    private void drawUnSampledLinePointsIMG(int start, double zoom, Graphics2D g2) {
        double scalarScaling= this.getHeight()/(this.max-this.min);
        double rate = ((dataBuffer.limit() / (this.dimension + 1)) / dataBuffer.get((int) (dataBuffer.limit() - (this.dimension + 1)))) * 1000.0;
        double time = 0;

        dataBuffer.position(getUnsampledDrawStart());

        int old_x = -100;
        int xstart = 0;
        int shift = (int) ((1.0 / zoom) * (rate / 1000.0)) * (this.dimension + 1);
        if (shift < (this.dimension + 1)) {
            shift = (this.dimension + 1);
        }

        int old_y[] = new int[this.dimension];

        while ((time < (start + this.getWidth() / zoom)) && dataBuffer.hasRemaining()) {
            if (dataBuffer.position() >= dataBuffer.limit()) break;
            int pos = dataBuffer.position();

            time = dataBuffer.get(pos);
            xstart = (int) (((time) - start) * zoom);
            if (old_x == xstart) {//rechts
                while (pos < (dataBuffer.limit() - (this.dimension + 1)) && old_x == xstart) {
                    pos = pos + (this.dimension + 1);
                    time = dataBuffer.get(pos);
                    xstart = (int) (((time) - start) * zoom);
                }
            } else if (old_x < xstart) {//links
                boolean test = false;
                while (pos - (this.dimension + 1) > 0 && old_x < xstart) {
                    pos = pos - (this.dimension + 1);
                    time = dataBuffer.get(pos);
                    xstart = (int) (((time) - start) * zoom);
                    test = true;
                }
                if (test) {
                    pos = pos + (this.dimension + 1);
                }
                time = dataBuffer.get(pos);
                dataBuffer.position(pos);
                xstart = (int) (((time) - start) * zoom);
            }

            dataBuffer.get();

            for (int y = 0; y < this.dimension && dataBuffer.hasRemaining(); y++) {
                g2.setColor(colorMap.getColor((double) y / (double) this.dimension));
                int ystart = (int) (this.getHeight() - ((dataBuffer.get() - this.min) * scalarScaling));
                g2.drawLine(xstart, ystart, old_x, old_y[y]);
                g2.drawOval(xstart-2, ystart-2,4,4);
                old_y[y] = ystart;
            }
            old_x = xstart;
            if (dataBuffer.limit() >= pos + shift) {
                dataBuffer.position(pos + shift);
            }
        }
    }

    private void drawUnSampledLineIMG(int start, double zoom, Graphics2D g2) {
        double scalarScaling= this.getHeight()/(this.max-this.min);
        double rate = ((dataBuffer.limit() / (this.dimension + 1)) / dataBuffer.get((int) (dataBuffer.limit() - (this.dimension + 1)))) * 1000.0;
        double time = 0;

        dataBuffer.position(getUnsampledDrawStart());

        int old_x = -100;
        int xstart = 0;
        int shift = (int) ((1.0 / zoom) * (rate / 1000.0)) * (this.dimension + 1);
        if (shift < (this.dimension + 1)) {
            shift = (this.dimension + 1);
        }

        int old_y[] = new int[this.dimension];

        while ((time < (start + this.getWidth() / zoom)) && dataBuffer.hasRemaining()) {
            if (dataBuffer.position() >= dataBuffer.limit()) break;
            int pos = dataBuffer.position();

            time = dataBuffer.get(pos);
            xstart = (int) (((time) - start) * zoom);
            if (old_x == xstart) {//rechts
                while (pos < (dataBuffer.limit() - (this.dimension + 1)) && old_x == xstart) {
                    pos = pos + (this.dimension + 1);
                    time = dataBuffer.get(pos);
                    xstart = (int) (((time) - start) * zoom);
                }
            } else if (old_x < xstart) {//links
                boolean test = false;
                while (pos - (this.dimension + 1) > 0 && old_x < xstart) {
                    pos = pos - (this.dimension + 1);
                    time = dataBuffer.get(pos);
                    xstart = (int) (((time) - start) * zoom);
                    test = true;
                }
                if (test) {
                    pos = pos + (this.dimension + 1);
                }
                time = dataBuffer.get(pos);
                dataBuffer.position(pos);
                xstart = (int) (((time) - start) * zoom);
            }

            dataBuffer.get();

            for (int y = 0; y < this.dimension && dataBuffer.hasRemaining(); y++) {
                g2.setColor(colorMap.getColor((double) y / (double) this.dimension));
                int ystart = (int) (this.getHeight() - ((dataBuffer.get() - this.min) * scalarScaling));
                g2.drawLine(xstart, ystart, old_x, old_y[y]);
                old_y[y] = ystart;
            }
            old_x = xstart;
            if (dataBuffer.limit() >= pos + shift) {
                dataBuffer.position(pos + shift);
            }
        }

    }

    private void drawSampledLinePointsIMG(int start, double zoom, Graphics2D g2) {
        drawSampledLineIMG(start, zoom, g2);
        drawSampledPointsIMG(start, zoom, g2);
    }

    private void drawSampledPointsIMG(int start, double zoom, Graphics2D g2) {
        double startSec = start/1000.0;
        double timeSec;
        double zoomSec = zoom*1000;

        double scalarScaling= this.getHeight()/(this.max-this.min);

        if(zoomSec>sampleRate){
            // up sampling, just paint (up sampling is done by line renderer)
            if(startSec*sampleRate*dimension>dataBuffer.limit())return;
            dataBuffer.position((int)(startSec*sampleRate)*dimension);
            timeSec = (dataBuffer.position()/dimension)/sampleRate;
            while( timeSec < (startSec + this.getWidth() / zoomSec) && dataBuffer.hasRemaining()){
                for (int d = 0; d < this.dimension && dataBuffer.hasRemaining(); d++) {
                    g2.setColor(colorMap.getColor((double) d / (double) this.dimension));
                    int y = (int) (this.getHeight() - ((dataBuffer.get() - this.min) * scalarScaling));
                    int x = (int) (((timeSec) - startSec) * zoomSec);
                    g2.drawOval(x-2, y-2, 4, 4);
                }
                timeSec += 1.0/sampleRate;
            }
        }else{
            // down sample
            dataBuffer.rewind();
            for(int i=0;i<this.getWidth()&& dataBuffer.hasRemaining();i++) {
                int newPosition = (int)(startSec*sampleRate*dimension)+(int)(((i/zoomSec)*sampleRate)*dimension);
                //if(i==0)System.out.println(start + " : " + i + " : " + newPosition);
                //if(i%100==0)System.out.println(start + " : " + i + " : " + newPosition);
                //if(i==this.getWidth()-1)System.out.println(start + " : " + i + " : " + newPosition);
                if(newPosition > dataBuffer.limit())break;
                dataBuffer.position(newPosition);
                for (int d = 0; d < this.dimension && dataBuffer.hasRemaining(); d++) {
                    g2.setColor(colorMap.getColor((double) d / (double) this.dimension));
                    int y = (int) (this.getHeight() - ((dataBuffer.get() - this.min) * scalarScaling));
                    g2.drawOval(i-2, y-2, 4, 4);
                }
            }
        }
    }

    private void drawSampledLineIMG(int start, double zoom, Graphics2D g2){

        double startSec = start/1000.0;
        double timeSec;
        double zoomSec = zoom*1000;

        double scalarScaling= this.getHeight()/(this.max-this.min);
        int[] oldX = new int[dimension];
        int[] oldY = new int[dimension];

        if(zoomSec>sampleRate){
            // up sampling, just paint (up sampling is done by line renderer)
            if(startSec*sampleRate*dimension>dataBuffer.limit())return;
            dataBuffer.position((int)(startSec*sampleRate)*dimension);
            timeSec = (dataBuffer.position()/dimension)/sampleRate;
            //System.out.println("init -- time: " + timeSec + " end: " + (startSec + this.getWidth() / zoomSec));
            while( timeSec < (startSec + this.getWidth() / zoomSec) && dataBuffer.hasRemaining()){
                for (int d = 0; d < this.dimension && dataBuffer.hasRemaining(); d++) {
                    g2.setColor(colorMap.getColor((double) d / (double) this.dimension));
                    int y = (int) (this.getHeight() - ((dataBuffer.get() - this.min) * scalarScaling));
                    int x = (int) (((timeSec) - startSec) * zoomSec);
                    g2.drawLine(x, y, oldX[d], oldY[d]);
                    oldX[d] = x;
                    oldY[d] = y;
                }
                timeSec += 1.0/sampleRate;
            }
            //g2.setColor(Color.darkGray);
            //g2.setFont(new Font("Monospaced.bold",1,20));
            //g2.drawString("up sampling," + " startSec: " + startSec + " startPos: " + (int)(startSec*sampleRate)*dimension + " zoomSec: " + zoomSec, 10, 20);
        }else{
            // down sample
            dataBuffer.rewind();
            for(int i=0;i<this.getWidth()&& dataBuffer.hasRemaining();i++) {
                int newPosition = (int)(startSec*sampleRate*dimension)+(int)(((i/zoomSec)*sampleRate)*dimension);
                if(newPosition > dataBuffer.limit())break;
                dataBuffer.position(newPosition);
                for (int d = 0; d < this.dimension && dataBuffer.hasRemaining(); d++) {
                    g2.setColor(colorMap.getColor((double) d / (double) this.dimension));
                    int y = (int) (this.getHeight() - ((dataBuffer.get() - this.min) * scalarScaling));
                    g2.drawLine(i, y, i-1, oldY[d]);
                    oldY[d] = y;
                }
            }
            //g2.setColor(Color.darkGray);
            //g2.setFont(new Font("Monospaced.bold",1,20));
            //g2.drawString("down sampling," + " startSec: " + startSec + " startPos: " + (int)(startSec*sampleRate)*dimension + " zoomSec: " + zoomSec, 10, 20);
        }
    }

    private void drawSampledStackIMG(int start, double zoom, @NotNull Graphics2D g2){
        g2.setColor(Color.darkGray);
        g2.setFont(new Font("Monospaced.bold",1,30));
        g2.drawString("Sampled stacked renderer not jet implemented", 10, 50);
    }

    private void drawSampledMatrixIMG(int start, double zoom, @NotNull Graphics2D g2){

        double startSec = start/1000.0;
        double timeSec;
        double zoomSec = zoom*1000;
        double dimScaling = this.getHeight() / (double) this.dimension;
        double colorScale = 1.0 / (this.max - this.min);

        double scalarScaling= this.getHeight()/(this.max-this.min);
        int[] oldX = new int[dimension];

        if(zoomSec>sampleRate){
            // up sampling, just paint (up sampling is done by line renderer)
            if(startSec*sampleRate*dimension>dataBuffer.limit())return;
            dataBuffer.position((int)(startSec*sampleRate)*dimension);
            timeSec = (dataBuffer.position()/dimension)/sampleRate;
            //System.out.println("init -- time: " + timeSec + " end: " + (startSec + this.getWidth() / zoomSec));
            while( timeSec < (startSec + this.getWidth() / zoomSec) && dataBuffer.hasRemaining()){
                for (int d = 0; d < this.dimension && dataBuffer.hasRemaining(); d++) {
                    g2.setColor(colorMap.getColor((dataBuffer.get() - min) * colorScale));
                    int x = (int) (((timeSec) - startSec) * zoomSec);
                    g2.fillRect(x, (int)(d*dimScaling), x-oldX[d]+1, (int)(dimScaling+1));
                    oldX[d] = x;
                }
                timeSec += 1.0/sampleRate;
            }
            //g2.setColor(Color.darkGray);
            //g2.setFont(new Font("Monospaced.bold",1,20));
            //g2.drawString("up sampling," + " startSec: " + startSec + " startPos: " + (int)(startSec*sampleRate)*dimension + " zoomSec: " + zoomSec, 10, 20);
        }else{
            // down sample
            dataBuffer.rewind();
            for(int i=0;i<this.getWidth()&& dataBuffer.hasRemaining();i++) {
                int newPosition = (int)(startSec*sampleRate*dimension)+(int)(((i/zoomSec)*sampleRate)*dimension);
                if(newPosition > dataBuffer.limit())break;
                dataBuffer.position(newPosition);
                for (int d = 0; d < this.dimension && dataBuffer.hasRemaining(); d++) {
                    g2.setColor(colorMap.getColor((dataBuffer.get() - min) * colorScale));
                    g2.drawLine(i, (int)(d * dimScaling), i, (int)((d+1)*dimScaling));
                }
            }
            //g2.setColor(Color.darkGray);
            //g2.setFont(new Font("Monospaced.bold",1,20));
            //g2.drawString("down sampling," + " startSec: " + startSec + " startPos: " + (int)(startSec*sampleRate)*dimension + " zoomSec: " + zoomSec, 10, 20);
        }

        for (int i = 0; i < this.getHeight() && this.showLegend; i++) {
            g2.setColor(colorMap.getColor(1.0 - ((double) i / (double) this.getHeight())));
            g2.fillRect(0, i, 10, 1);
        }
    }

    private int getUnsampledDrawStart(){
        int startPosition = 0;
        double time = Double.MAX_VALUE;

        dataBuffer.rewind();
        double rate = ((dataBuffer.limit() / (this.dimension + 1)) / dataBuffer.get((int) (dataBuffer.limit() - (this.dimension + 1)))) * 1000.0;

        startPosition = (int) ((this.start / 1000.0) * rate) * (this.dimension + 1) - (this.dimension + 1);
        if (startPosition < 0) startPosition = 0;
        if (startPosition >= (int) (dataBuffer.limit() - (this.dimension + 1))) startPosition = (int) (dataBuffer.limit() - (this.dimension + 1));

        while (dataBuffer.get(startPosition) > this.start && startPosition > 0) {
            startPosition = startPosition - (this.dimension + 1) * (int) ((dataBuffer.limit() / ((this.dimension + 1))) / 1000);
            if (startPosition < 0) startPosition = 0;
        }

        while (dataBuffer.hasRemaining()) {
            time = dataBuffer.get();//time
            if (time >= this.start) {
                break;
            }
            for (int i = 0; i < this.dimension; i++) {
                dataBuffer.get();
            }
        }

        return dataBuffer.position()-1;
    }

    public void mousePressed(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON3){
            showContextMenu(e);
        }
    }

    private void contextHide(){
        sun.setSelected(false);
        ocean.setSelected(false);
        islands.setSelected(false);
        gray.setSelected(false);
        fire.setSelected(false);
        colorful.setSelected(false);
    }

    private void styleHide(){
        matrixStyleMenu.setSelected(false);
        linesPointsStyleMenu.setSelected(false);
        linesStyleMenu.setSelected(false);
        pointsStyleMenu.setSelected(false);
        stackStyleMenu.setSelected(false);
    }

    private void showContextMenu(MouseEvent evt){
        JPopupMenu menu = new JPopupMenu();

        JMenu colorMapMenu = new JMenu("Colormap");
        colorful = new JCheckBoxMenuItem("Colorful");
        fire = new JCheckBoxMenuItem("fire");
        gray = new JCheckBoxMenuItem("Gray");
        islands = new JCheckBoxMenuItem("Islands");
        ocean = new JCheckBoxMenuItem("Ocean");
        sun = new JCheckBoxMenuItem("Sun");
        contextHide();
        if(this.colorMap.getName().equalsIgnoreCase("Sun"))sun.setSelected(true);
        if(this.colorMap.getName().equalsIgnoreCase("Ocean"))ocean.setSelected(true);
        if(this.colorMap.getName().equalsIgnoreCase("Islands"))islands.setSelected(true);
        if(this.colorMap.getName().equalsIgnoreCase("Gray"))gray.setSelected(true);
        if(this.colorMap.getName().equalsIgnoreCase("Fire"))fire.setSelected(true);
        if(this.colorMap.getName().equalsIgnoreCase("Colorful"))colorful.setSelected(true);
        colorMapMenu.add(colorful);
        colorMapMenu.add(fire);
        colorMapMenu.add(gray);
        colorMapMenu.add(islands);
        colorMapMenu.add(ocean);
        colorMapMenu.add(sun);
        menu.add(colorMapMenu);

        JCheckBoxMenuItem legendMenu = new JCheckBoxMenuItem("Show Legend");
        legendMenu.setSelected(showLegend);
        menu.add(legendMenu);


        JMenu styleMenu = new JMenu("Draw style");
        linesStyleMenu = new JCheckBoxMenuItem("Lines");
        linesPointsStyleMenu = new JCheckBoxMenuItem("Lines + Points");
        pointsStyleMenu = new JCheckBoxMenuItem("Points");
        matrixStyleMenu = new JCheckBoxMenuItem("Matrix");
        stackStyleMenu = new JCheckBoxMenuItem("Stacked");
        linesStyleMenu.setSelected(getStyle()==DataTrack.STYLE_LINES);
        linesPointsStyleMenu.setSelected(getStyle()==DataTrack.STYLE_LINES_POINTS);
        pointsStyleMenu.setSelected(getStyle()==DataTrack.STYLE_POINTS);
        matrixStyleMenu.setSelected(getStyle()==DataTrack.STYLE_MATRIX);
        stackStyleMenu.setSelected(getStyle()==DataTrack.STYLE_STACKED);
        styleMenu.add(linesStyleMenu);
        styleMenu.add(linesPointsStyleMenu);
        styleMenu.add(pointsStyleMenu);
        styleMenu.add(stackStyleMenu);
        styleMenu.add(matrixStyleMenu);
        menu.add(styleMenu);


        colorful.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DataTrack.this.colorMap = new ColorMap_Colorful();
                DataTrack.this.drawIMG(DataTrack.this.start,Project.getInstance().getZoom());
                MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
            }
        });
        fire.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DataTrack.this.colorMap = new ColorMap_Fire();
                DataTrack.this.drawIMG(DataTrack.this.start,Project.getInstance().getZoom());
                MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
            }
        });
        gray.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DataTrack.this.colorMap = new ColorMap_Gray();
                DataTrack.this.drawIMG(DataTrack.this.start,Project.getInstance().getZoom());
                MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
            }
        });
        islands.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DataTrack.this.colorMap = new ColorMap_Islands();
                DataTrack.this.drawIMG(DataTrack.this.start,Project.getInstance().getZoom());
                MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
            }
        });
        ocean.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DataTrack.this.colorMap = new ColorMap_Ocean();
                DataTrack.this.drawIMG(DataTrack.this.start,Project.getInstance().getZoom());
                MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
            }
        });
        sun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DataTrack.this.colorMap = new ColorMap_Sun();
                DataTrack.this.drawIMG(DataTrack.this.start,Project.getInstance().getZoom());
                MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
            }
        });
        legendMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLegend = !showLegend;
                DataTrack.this.drawIMG(DataTrack.this.start, Project.getInstance().getZoom());
                MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
            }
        });
        linesStyleMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                style = DataTrack.STYLE_LINES;
                DataTrack.this.drawIMG(DataTrack.this.start, Project.getInstance().getZoom());
                MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
            }
        });
        linesPointsStyleMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                style = DataTrack.STYLE_LINES_POINTS;
                DataTrack.this.drawIMG(DataTrack.this.start, Project.getInstance().getZoom());
                MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
            }
        });
        pointsStyleMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                style = DataTrack.STYLE_POINTS;
                DataTrack.this.drawIMG(DataTrack.this.start, Project.getInstance().getZoom());
                MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
            }
        });
        matrixStyleMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                style = DataTrack.STYLE_MATRIX;
                DataTrack.this.drawIMG(DataTrack.this.start, Project.getInstance().getZoom());
                MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
            }
        });
        stackStyleMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                style = DataTrack.STYLE_STACKED;
                DataTrack.this.drawIMG(DataTrack.this.start, Project.getInstance().getZoom());
                MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
            }
        });

        menu.show(this, evt.getX(), evt.getY());
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        if ( img != null )
            g2.drawImage( img, 0, 0, this );

    }

    public double[] getDataTimePoints(long from, long to) {
        dataBuffer.rewind();
        if(sampleRate==0) {
            double time;
            while (dataBuffer.hasRemaining()) {
                if ((time = dataBuffer.get()) >= from) break; //time
                for (int i = 0; i < this.dimension; i++) dataBuffer.get();//data
            }
            if (!dataBuffer.hasRemaining()) return null;
            dataBuffer.position(dataBuffer.position() - 1 < 0 ? 0 : dataBuffer.position() - 1);
            ArrayList<Double> data = new ArrayList<Double>();
            while (dataBuffer.hasRemaining() && (time = dataBuffer.get()) <= to) {
                data.add(time);
                for (int i = 0; i < this.dimension; i++) dataBuffer.get();//data
            }
            double[] ret = new double[data.size()];
            Iterator<Double> iD = data.iterator();
            int i = 0;
            while (iD.hasNext()) {
                ret[i++] = iD.next();
            }
            return ret;
        }else {
            long firstSample = (long) (from / 1000.0 * sampleRate);
            long lastSample = (long) (to / 1000.0 * sampleRate);
            double[] ret = new double[(int)(lastSample-firstSample)];
            double time = firstSample*1000.0/sampleRate;
            for (int i = 0;i<ret.length;i++){
                ret[i]=time;
                time+=1000.0/sampleRate;
            }
            return ret;
        }
    }

    public void maxMinChanged(ObjectLine ol){
        if(ol.getTrack().equals(this)){
            drawIMG(start, Project.getInstance().getZoom());
            repaint();

        }
    }

    public void updateData(double[][] newData) {
        try{
            raf.close();
            double[] mm = HelperFunctions.toRawFile(newData, sampleRate!=0?true:false, this.getPath());
            this.min=mm[0];
            this.max=mm[1];
            file = new File(this.getPath());
            raf = new RandomAccessFile(file,"r");
            fChannel = raf.getChannel();
            dataBuffer = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size()).asDoubleBuffer();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    public double getSampleRate() {
        return this.sampleRate;
    }

    public String getStyleAsString() {
        switch (style) {
            case DataTrack.STYLE_LINES:
                return ("lines");
            case DataTrack.STYLE_LINES_POINTS:
                return ("linesPoints");
            case DataTrack.STYLE_MATRIX:
                return ("matrix");
            case DataTrack.STYLE_POINTS:
                return ("points");
            case DataTrack.STYLE_STACKED:
                return ("stack");
        }
        return null;
    }

    public void setStyle(String style) {
        if(style.equalsIgnoreCase("lines"))this.setStyle(DataTrack.STYLE_LINES);
        if(style.equalsIgnoreCase("points"))this.setStyle(DataTrack.STYLE_POINTS);
        if(style.equalsIgnoreCase("linesPoints"))this.setStyle(DataTrack.STYLE_LINES_POINTS);
        if(style.equalsIgnoreCase("matrix"))this.setStyle(DataTrack.STYLE_MATRIX);
        if(style.equalsIgnoreCase("stacked"))this.setStyle(DataTrack.STYLE_STACKED);
    }
}

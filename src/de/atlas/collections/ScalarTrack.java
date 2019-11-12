package de.atlas.collections;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JCheckBoxMenuItem;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import de.atlas.data.Project;
import de.atlas.data.ScalarSample;
import de.atlas.gui.LiveData;
import de.atlas.messagesystem.*;
import de.atlas.misc.HelperFunctions;

public class ScalarTrack extends JPanel{

	private static final long serialVersionUID = 1L;
	private double min,max;
	private boolean showPoints=true;
	private boolean showLine=true;
	private int start;
	private BufferedImage img;
	private File file;
	private String name;
	private RandomAccessFile raf;
	private FileChannel fChannel;
	private JCheckBoxMenuItem linesItem;
	private JCheckBoxMenuItem pointsItem;
	private DoubleBuffer ramData;
	private double maxTime = 0;

	public ScalarTrack(int length, String file,String name){		
		this.setBounds(0, 0, length, ObjectLine.MEDIUM);
		this.file = new File(file);
		this.name=name;
		MessageManager.getInstance().addSlotChangedListener(new SlotChangedListener(){
			@Override
			public void slotChanged(SlotEvent e) {
				updateSlot(e.getStart());

			}			 
		});
		MessageManager.getInstance().addZoomChangedListener(new ZoomChangedListener(){
			@Override
			public void zoomChanged(ZoomEvent e) {
				drawIMG(start, e.getZoom());
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

				drawIMG(start,Project.getInstance().getZoom());
				repaint();						
			}

			@Override
			public void componentShown(ComponentEvent e) {

			}});
		
		if(this.file.getName().endsWith(".raw")){
			try {
				buildRamData();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.drawIMG(start,Project.getInstance().getZoom());

		MessageManager.getInstance().addRepaintListener(new RepaintListener() {
			@Override
			public void repaintRequested(RepaintEvent e) {
//				System.out.println(e.getSource().getClass().toString());
				if(e.getSource().getClass().toString().contains("LiveData")){
					drawIMG(start, Project.getInstance().getZoom());
				}
			}
		});
	}

	public void appendDataUnsafe(double frameRate, ArrayList<Double> val){
        try {
            if(raf!=null)raf.close();
            raf = new RandomAccessFile(file,"rw");
            fChannel = raf.getChannel();
            DoubleBuffer iB = fChannel.map(FileChannel.MapMode.READ_WRITE, 0, fChannel.size()+2*val.size()*Double.SIZE/8).asDoubleBuffer();
            iB.position(iB.limit()-2*val.size());
            for(int i=0;i<val.size();i++){
                iB.put(maxTime+((double)(i+1)*1000/frameRate));
                iB.put(val.get(i));
            }
            maxTime=maxTime+((double)val.size()*1000/frameRate);
			Project.getInstance().setProjectLength(maxTime+1);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

	public void appendDataRelative(ArrayList<Double> time, ArrayList<Double> val){
        for(int i=0;i<time.size();i++)time.set(i,time.get(i)+this.maxTime);
        appendDataAbsolute(time,val);
    }

	public void appendDataAbsolute(ArrayList<Double> time, ArrayList<Double> val){
		try {
			//consistency check
		    if(time.size()!=val.size())return;
            ArrayList<ScalarSample> samples = new ArrayList<>();
            for(int i=0;i< time.size();i++)samples.add(new ScalarSample(val.get(i),time.get(i)));
            Collections.sort(samples);
            if(samples.get(0).getTime()<=this.maxTime)return;

            //append data
            if(raf!=null)raf.close();
			raf = new RandomAccessFile(file,"rw");
			fChannel = raf.getChannel();
			DoubleBuffer iB = fChannel.map(FileChannel.MapMode.READ_WRITE, 0, fChannel.size()+2*time.size()*Double.SIZE/8).asDoubleBuffer();
			iB.position(iB.limit()-2*time.size());
			for(int i=0;i<time.size();i++){
				iB.put(samples.get(i).getTime());
				iB.put(samples.get(i).getVal());
			}
			buildRamData();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void refresh(){
		try {
			buildRamData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void buildRamData() throws IOException {
		ArrayList<Double> data = new ArrayList<Double>();
		if(raf!=null)raf.close();
		raf = new RandomAccessFile(file,"r");
		fChannel = raf.getChannel();
		DoubleBuffer iB = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size()).asDoubleBuffer();

		double time=0;
		double val=0;
		long pixelCounter=1;

		double pixelDuration = (1.0/100.0)*1000.0; //in in ms
		double min = Double.MAX_VALUE;
		double max = -1*Double.MAX_VALUE;
		double min_time = 0;
		double max_time = 0;
		while(iB.hasRemaining()){
			time = iB.get();//time
			val = iB.get();//value

			if((pixelCounter*pixelDuration)<time || !iB.hasRemaining()){
				//sprung in ein neues pixel
				//schreibe min und max und zeit
				if(min_time<max_time){
					data.add(min_time);
					data.add(min);
					data.add(max_time);
					data.add(max);
				}else{
					data.add(max_time);
					data.add(max);
					data.add(min_time);
					data.add(min);
				}
				pixelCounter++;
				min = max = val;
				min_time = max_time=time;
			}else{
				if(min>val){
					min=val;
					min_time = time;
				}
				if(max<val){
					max=val;
					max_time = time;
				}
			}
		}
		ramData = DoubleBuffer.allocate(data.size());
		for( Double f : data){
			ramData.put(f);
		}
		this.maxTime = time;
		Project.getInstance().setProjectLength(time+1);

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

	public void generateRandomData(){
		try {
			RandomAccessFile raf = new RandomAccessFile(file,"rw");

			this.fChannel = raf.getChannel();

			long numValues = 1000l*1800l;
			DoubleBuffer dB = this.fChannel.map(FileChannel.MapMode.READ_WRITE, 0, numValues*2*(Double.SIZE/8)).asDoubleBuffer();
			for(long i=0; i< numValues;i+=1){
				dB.put(i);
				dB.put(i%(50));

			}			
			raf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updateSlot(int start){
		this.start=start;
		this.drawIMG(start,Project.getInstance().getZoom());
		this.repaint();
	}

	public boolean isShowPoints() {
		return showPoints;
	}

	public void setShowPoints(boolean showPoints) {
		this.showPoints = showPoints;
	}

	public boolean isShowLine() {
		return showLine;
	}

	public void setShowLine(boolean showLine) {
		this.showLine = showLine;
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
		try {
			DoubleBuffer buf = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size()).asDoubleBuffer();
			ArrayList<Double> dataArrayList = new ArrayList<>();
			while(buf.hasRemaining()){
				dataArrayList.add(buf.get());
			}
			Iterator<Double> iterator = dataArrayList.iterator();
			double[][] outData = new double[dataArrayList.size()/2][2];
			for(int i=0;i<dataArrayList.size()/2;i++){
				outData[i][0] = iterator.next();
				outData[i][1] = iterator.next();
			}
			return outData;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public double[][] getData(long from, long to){
		DoubleBuffer iB;
		try {
			iB = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size()).asDoubleBuffer();
			while(iB.get()<from){
				iB.get();
			}
			ArrayList<Double> data = new ArrayList<Double>();
			data.add(iB.get());
			while(iB.hasRemaining()&&iB.get()<=to){
				data.add(iB.get());
			}
			double[][] ret = new double[data.size()][1];
			Iterator<Double> iD = data.iterator();
			int i=0;
			while(iD.hasNext()){
				ret[i++][0]=iD.next().doubleValue();
			}
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		}			 
		return null;
	}

	private void drawIMG(int start, double zoom){

		if(this.getParent()==null)return;  //no parent
		if(!this.getParent().isVisible())return;   //invisible

		img = new BufferedImage( this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
		double scaling= this.getHeight()/(this.max-this.min);
		Graphics2D g2 = img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.white);
		g2.fillRect(0, 0, img.getWidth(), img.getHeight());

		try {
			if(fChannel.size()==0)return; //no data
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			DoubleBuffer iB;
			if(Project.getInstance().getZoom()<0.1){
				iB = ramData;
			}else{
				iB = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size()).asDoubleBuffer();
			}

			int startPosition=0;

			double rate = ((iB.limit()/2.0) / iB.get((int)(iB.limit()-2)))*1000.0;

			startPosition = (int)((this.start/1000.0)*rate)*2 -2;


			if(startPosition<0){
				startPosition=0;
			}
			if(startPosition>=(int)(iB.limit()-2)){
				startPosition=(int)(iB.limit()-2);
			}

			while(iB.get(startPosition)>this.start && startPosition>0){
//					System.out.println(startPosition + " "+ iB.limit());
				startPosition=startPosition- (2*(int)(iB.limit()<1000?1:iB.limit()/1000));
				if(startPosition<0){
					startPosition=0;
				}
			}
//				System.out.println("end draw");
			iB.position(startPosition);

			double preVal=iB.get(1);
			double time=0;
			double val=0;


			while(iB.hasRemaining()){
				time = iB.get();//time
				val = iB.get();//value
				if(time>=this.start){
					break;
				}
				preVal=val;
			}
			int old_x = 0;
			int old_x2 = 0;
			int old_y = (int)(this.getHeight()-((preVal-this.min)*scaling));


			int min=0,max=0;
			//int count = 0;
			while((time<(start+this.getWidth()/zoom)) && iB.hasRemaining()){

				if(showLine){
					g2.setColor(Color.red);
					int x_right = (int)(((time)-start)*zoom);
					if(old_x<x_right){
						if(min!=max && min!=0){
							g2.drawLine( old_x,
									min,
									old_x,
									max);
						}
						int y_right =(int)(this.getHeight()-((val-this.min)*scaling));
						min=max=y_right;

						g2.drawLine( old_x,
								old_y,
								x_right,
								y_right);

						old_x=x_right;
						old_y = y_right;
					}
					if(old_x==x_right){
						int y_right =(int)(this.getHeight()-((val-this.min)*scaling));
						if(min > y_right){
							min=y_right;
						}if(max < y_right){
							max=y_right;
						}
					}
				}
				if(showPoints){
					g2.setColor(Color.blue);

					int x =(int)(((time)-start)*zoom-2);
					int y = (int)(this.getHeight()-((val-this.min)*scaling)-2);
					if(x > old_x2){
						g2.drawOval(x,y,4,4);
						old_x2 = x;
					}
				}
				time=iB.get();
				val=iB.get();
			}



		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		//g2.setColor(Color.darkGray);
		//g2.setFont(new Font("Monospaced.bold",1,20));
		//g2.drawString("zoom: " + zoom + " this.width " + this.getWidth() + " lcoll.width " + Project.getInstance().getLcoll().getWidth(), 10, 20);

	}

	public void paint(Graphics g) {

		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);			
		if ( img != null )
			g2.drawImage( img, 0, 0, this );
	}

	public void mousePressed(MouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON3){
			showContextMenu(e);
		}		
	}

	public void showContextMenu(MouseEvent evt){
		JPopupMenu menu = new JPopupMenu();
		pointsItem = new JCheckBoxMenuItem("show Points");
		linesItem = new JCheckBoxMenuItem("show Lines");
		pointsItem.setSelected(this.showPoints);
		linesItem.setSelected(this.showLine);



		pointsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showPoints =pointsItem.isSelected();		
				ScalarTrack.this.drawIMG(ScalarTrack.this.start,Project.getInstance().getZoom());
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});
		linesItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showLine=linesItem.isSelected();
				ScalarTrack.this.drawIMG(ScalarTrack.this.start,Project.getInstance().getZoom());
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});

		menu.add(pointsItem);
		menu.add(linesItem);

		menu.show(this, evt.getX(), evt.getY()); 
	}

	public double[] getDataTimePoints(long from, long to) {
		DoubleBuffer iB;
		try {
			iB = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size()).asDoubleBuffer();
			double time = 0;
			while((time = iB.get())<from){
				iB.get();
			}
			ArrayList<Double> data = new ArrayList<Double>();
			data.add(time);
			iB.get();
			while(iB.hasRemaining()&&(time=iB.get())<=to){
				data.add(time);
				iB.get();
			}
			double[] ret = new double[data.size()];
			Iterator<Double> iD = data.iterator();
			int i=0;
			while(iD.hasNext()){
				ret[i++]=iD.next().doubleValue();
			}
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		}			 
		return null;
	}

	public void maxMinChanged(ObjectLine ol){
		if(ol.getTrack().equals(this)){
			drawIMG(start, Project.getInstance().getZoom());
			repaint();		
			
		}
	}

	public void updateData(double[][] newData) {
		try {
			raf.close();
			HelperFunctions.toRawFile(newData, true, this.getPath());
			file = new File(this.getPath());
			buildRamData();
/*			raf = new RandomAccessFile(file,"r");
			fChannel = raf.getChannel();
			DoubleBuffer iB = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size()).asDoubleBuffer();

			ArrayList<Double> data = new ArrayList<Double>();

			double time=0;
			double val=0;
			long pixelCounter=1;


			double pixelDuration = (1.0/100.0)*1000.0; //in in ms
			double min = Double.MAX_VALUE;
			double max = -1*Double.MAX_VALUE;
			double min_time = 0;
			double max_time = 0;
			while(iB.hasRemaining()){
				time = iB.get();//time
				val = iB.get();//value

				if((pixelCounter*pixelDuration)<time || !iB.hasRemaining()){
					//sprung in ein neues pixel
					//schreibe min und max und zeit
					if(min_time<max_time){
						data.add(min_time);
						data.add(min);
						data.add(max_time);
						data.add(max);
					}else{
						data.add(max_time);
						data.add(max);
						data.add(min_time);
						data.add(min);
					}
					pixelCounter++;
					min = max = val;
					min_time = max_time=time;
				}else{
					if(min>val){
						min=val;
						min_time = time;
					}
					if(max<val){
						max=val;
						max_time = time;
					}
				}
				//if(pixelCounter==10)
				//System.exit(0);
			}
			ramData = DoubleBuffer.allocate(data.size());
			for( Double f : data){
				ramData.put(f);
			}
			Project.getInstance().setProjectLength(time);*/
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void appendDataPointUnsafe(long time, double val) {
		try {
			//consistency check
			if(time<=this.maxTime)this.maxTime=time;

			//append data
			if(raf!=null)raf.close();
			raf = new RandomAccessFile(file,"rw");
			fChannel = raf.getChannel();
			DoubleBuffer iB = fChannel.map(FileChannel.MapMode.READ_WRITE, 0, fChannel.size()+2*Double.SIZE/8).asDoubleBuffer();
			iB.position(iB.limit() - 2);
			iB.put(time);
			iB.put(val);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void clearData() {
		updateData(new double[0][0]);
	}
}

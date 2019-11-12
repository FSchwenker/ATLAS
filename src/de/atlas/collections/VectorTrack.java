package de.atlas.collections;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.DoubleBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import de.atlas.colormap.*;
import de.atlas.misc.HelperFunctions;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import de.atlas.data.Project;
import de.atlas.data.VectorSample;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.MinMaxChangedEvent;
import de.atlas.messagesystem.MinMaxChangedListener;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.SlotChangedListener;
import de.atlas.messagesystem.SlotEvent;
import de.atlas.messagesystem.ZoomChangedListener;
import de.atlas.messagesystem.ZoomEvent;

public class VectorTrack extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<VectorSample> data;
	private double min,max;
	private BufferedImage img;
	private int start;
	//private double zoom=1.0;
	private File file;
	private String name;
	private int dimension;
	private RandomAccessFile raf;
	private FileChannel fChannel;
	private ColorMap colorMap;
	private JCheckBoxMenuItem colorful;
	private JCheckBoxMenuItem fire;
	private JCheckBoxMenuItem gray;
	private JCheckBoxMenuItem islands;
	private JCheckBoxMenuItem ocean;
	private JCheckBoxMenuItem sun;
	private JCheckBoxMenuItem legendMenu;
	private JCheckBoxMenuItem scalarMenu;
	private JCheckBoxMenuItem stackMenu;
	private boolean showLegend = false;
	private boolean asScalar = false;
	private boolean stackScalar = false;


	public VectorTrack(int length, String file, String name,ColorMap cm,double min,double max,int dimension){
		this.setBounds(0, 0, length, ObjectLine.MEDIUM);
		this.file = new File(file);
		this.name=name;
		this.colorMap=cm;
		this.min = min;
		this.max = max;
		this.dimension = dimension;
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
				drawIMG(start,Project.getInstance().getZoom());
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
				DoubleBuffer iB = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size()).asDoubleBuffer();	

				double time=0;

				while(iB.hasRemaining()){
					time = iB.get();//time				
					for(int i=0;i<this.dimension;i++){
						iB.get();
					}
				}			
				Project.getInstance().setProjectLength(time);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}catch (IOException e) {
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

	public boolean isAsScalar() {
		return asScalar;
	}

	public boolean isStacked() {
		return stackScalar;
	}

	public void setAsScalar(boolean asScalar) {
		this.asScalar = asScalar;
	}

	public void setStacked(boolean isStacked){
		this.stackScalar = isStacked;
	}

	private void updateSlot(int start){
		this.start=start;
		drawIMG(start, Project.getInstance().getZoom());
		this.repaint();
	}

	public void generateRandomData(){
		try {
			RandomAccessFile raf = new RandomAccessFile(file,"rw");

			this.fChannel = raf.getChannel();

			long numValues = 1000l*1800l;
			long numDimensions = 50;
			DoubleBuffer dB = this.fChannel.map(FileChannel.MapMode.READ_WRITE, 0, numValues*(numDimensions+1)*(Double.SIZE/8)).asDoubleBuffer();
			for(int i =0; i<numValues;i+=1){			
				dB.put(i);
				for(int j=0; j<numDimensions;j++){				
					dB.put( ( (j/(double)numDimensions)+(i%10)/10.0 )/2.0 );								
				}				
			}
			raf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ColorMap getColorMap() {
		return colorMap;
	}

	public void setColorMap(ColorMap colorMap) {
		this.colorMap = colorMap;
	}

	public void readMemMappedRaw(long starttime, long numValues){

		data = new ArrayList<VectorSample>();
		try {			
			RandomAccessFile raf = new RandomAccessFile(file,"r");

			FileChannel fChannel = raf.getChannel();
			MappedByteBuffer iB = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, 1024);

			while(iB.hasRemaining()){
				iB.get();
			}			
			fChannel.close();
			raf.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readRAW(){
		data = new ArrayList<VectorSample>();
		try {
			RandomAccessFile raf = new RandomAccessFile(file,"r");
			this.name = raf.readLine();					
			this.min = raf.readDouble();
			this.max = raf.readDouble();
			long dim = raf.readLong();
			try{
				while(true){					
					VectorSample tmp = new VectorSample();
					tmp.setTime(raf.readLong());
					ArrayList<Double> val =  new ArrayList<Double>();
					for(int i=0; i<dim;i++){
						val.add(raf.readDouble());
					}					
					tmp.setVal(val); 
					data.add(tmp);								
				}
			}catch(EOFException e){
				raf.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadData(){
		data = new ArrayList<VectorSample>();
		if (file.getPath().endsWith(".xml")) {

			SAXBuilder sxbuild = new SAXBuilder();
			InputSource is=null;
			try {
				is = new InputSource(new FileInputStream(file.getPath()));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			Document doc;
			try {

				doc = sxbuild.build(is);
				Element root = doc.getRootElement();
				this.name = root.getAttribute("name").getValue();
				this.min = root.getAttribute("min").getDoubleValue();
				this.max = root.getAttribute("max").getDoubleValue();


				@SuppressWarnings("unchecked")
				List<Element>  vectorList =((List<Element>)root.getChildren("vector"));				
				for(Element vector : vectorList){									
					VectorSample tmp = new VectorSample();
					tmp.setTime(vector.getAttribute("time").getLongValue());
					@SuppressWarnings("unchecked")
					List<Element>  valueList =((List<Element>)vector.getChildren("value"));
					ArrayList<Double> val =  new ArrayList<Double>(); 
					for(Element value : valueList){
						val.add(Double.parseDouble(value.getValue()));
					}
					tmp.setVal(val);
					this.data.add(tmp);
				}													
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}		    
		}						
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
			double[][] outData = new double[dataArrayList.size()/(dimension+1)][dimension+1];
			for(int i=0;i<dataArrayList.size()/(dimension+1);i++){
				outData[i][0] = iterator.next();
				for(int d=1;d<dimension+1;d++){
					outData[i][d] = iterator.next();
				}
			}
			return outData;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public double[][] getData(long from, long to){
		if(from>to)return null;
		if(to<from)return null;
		DoubleBuffer iB;
		try {
			iB = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size()).asDoubleBuffer();
			while(iB.hasRemaining()){
				if(iB.get()>=from)break; //time				
				for(int i=0;i<this.dimension;i++)iB.get();//data
			}	
			if(!iB.hasRemaining())return null;
			iB.position(iB.position()-1<0?0:iB.position()-1);
			ArrayList<Double[]> data = new ArrayList<Double[]>();
			while(iB.hasRemaining()&&iB.get()<=to){
				Double[] tmp = new Double[this.dimension];
				for(int i=0;i<this.dimension;i++)tmp[i]=iB.get();//data
				data.add(tmp);
			}
			double[][] ret = new double[data.size()][this.dimension];
			Iterator<Double[]> iD = data.iterator();
			int i=0;
			while(iD.hasNext()){
				Double[] tmp = iD.next();
				for(int d=0;d<this.dimension;d++)ret[i][d]=tmp[d];
				i++;
			}
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
		}			 
		return null;
	}

	private void drawIMG(int start, double zoom){
		if(this.getParent()==null){ 		
			return;
		}if(this.getParent().isVisible()){

			img = new BufferedImage( this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);	
			double scalarScaling= this.getHeight()/(this.max-this.min);
			Graphics2D g2 = img.createGraphics();				
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);	
			g2.setColor(Color.white);
			g2.fillRect(0, 0, img.getWidth(), img.getHeight());


			try {			
				DoubleBuffer iB = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size()).asDoubleBuffer();	

				int startPosition=0;

				double rate = ((iB.limit()/(this.dimension+1)) / iB.get((int)(iB.limit()-(this.dimension+1))))*1000.0;





				startPosition = (int)((this.start/1000.0)*rate)*(this.dimension+1)-(this.dimension+1);						
				if(startPosition<0){
					startPosition=0;
				}
				if(startPosition>=(int)(iB.limit()-(this.dimension+1))){
					startPosition=(int)(iB.limit()-(this.dimension+1));
				}


				while(iB.get(startPosition)>this.start && startPosition>0){
					startPosition=startPosition - (this.dimension+1)*(int)((iB.limit()/((this.dimension+1)))/1000);
					if(startPosition<0){
						startPosition=0;
					}		

				}

				iB.position(startPosition);

				double dimScale = this.getHeight()/(double)this.dimension;
				double colorScale =1.0/(this.max-this.min);
				double time=Double.MAX_VALUE;

				while(iB.hasRemaining()){
					time = iB.get();//time
					if(time>=this.start){					
						break;
					}				
					for(int i=0;i<this.dimension;i++){
						iB.get();
					}					
				}	
				if(!iB.hasRemaining()){
					return;
				}
				iB.position(iB.position()-(1));
				int old_x = -100;
				int xstart=0;
				int shift = (int)( (1.0/zoom) * (rate/1000.0))*(this.dimension+1);
				if(shift<(this.dimension+1)){
					shift = (this.dimension+1);
				}


				int old_y[] = new int[this.dimension];
				
				while((time<(start+this.getWidth()/zoom)) && iB.hasRemaining()){								
					if(iB.position()>=iB.limit()){
						break;
					}
					int pos = iB.position();											
					//long drawstarttimeval = System.nanoTime();
					time=iB.get(pos);
					xstart = (int)( ( (time)-start )*zoom  );
					if(old_x==xstart){//rechts
						while(pos<(iB.limit()-(this.dimension+1)) && old_x==xstart){
							pos = pos+(this.dimension+1);
							time=iB.get(pos);
							xstart = (int)( ( (time)-start )*zoom  );

						}	
					}else if(old_x<xstart){//links
						boolean test =false;
						while(pos-(this.dimension+1)>0 && old_x<xstart){
							pos=pos-(this.dimension+1);								
							time=iB.get(pos);
							xstart = (int)( ( (time)-start )*zoom  );
							test = true;
						}
						if(test){
							pos = pos+(this.dimension+1);
						}


						time=iB.get(pos);
						iB.position(pos);
						xstart = (int)( ( (time)-start )*zoom  );							

					}

					iB.get();
					if(asScalar){
						if(stackScalar) {
							//ToDo: stack plot
							int yOffset = 0;
							int[] tmp_old_y=new int[dimension];
							for (int d = 0; d < this.dimension; d++) {
								g2.setColor(colorMap.getColor((double) d / (double) this.dimension));
//								int ystart = (int) (this.getHeight() - ((iB.get() - this.min) * scalarScaling));
								int yDiff = (int) ((iB.get() - 0) * scalarScaling);
								int new_y = this.getHeight()-yDiff-yOffset;

								Polygon p = new Polygon();
								p.addPoint(old_x, old_y[d]);
								p.addPoint(xstart, new_y);
								p.addPoint(xstart, new_y+yDiff);
								p.addPoint(old_x, d==0?this.getHeight():old_y[d-1]);
								g2.fillPolygon(p);

								tmp_old_y[d] = new_y;
								yOffset+=yDiff;
							}
							old_y = tmp_old_y;

						}else{
							for (int y = 0; y < this.dimension; y++) {
//						for(int y=0; y<1;y++){
								g2.setColor(colorMap.getColor((double) y / (double) this.dimension));
								int ystart = (int) (this.getHeight() - ((iB.get() - this.min) * scalarScaling));
								g2.drawLine(xstart, ystart, old_x, old_y[y]);
								old_y[y] = ystart;
							}
						}
					}else{
						for(int y=0; y<this.dimension;y++){	
							g2.setColor(colorMap.getColor((iB.get()-min)*colorScale));
							int ystart = (int)(y*dimScale);					
							int swidth = xstart-old_x+1;
							int sheight = (int)(dimScale+1.0); 

							g2.fillRect(xstart, ystart,	swidth,	sheight);
						}
					}
					old_x = xstart;
					if(iB.limit()>=pos+shift){					
						iB.position(pos+shift);
					}


				}
				for(int i=0;i<this.getHeight()&&this.showLegend;i++){
					g2.setColor(colorMap.getColor(1.0-((double)i/(double)this.getHeight())));
					g2.fillRect(0, i, 10, 1);
				}


			}catch (FileNotFoundException e) {
				e.printStackTrace();
			}catch (IOException e) {
				e.printStackTrace();
			}
		}

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

	public void showContextMenu(MouseEvent evt){
		JPopupMenu menu = new JPopupMenu();
		colorful = new JCheckBoxMenuItem("Colorful");
		fire = new JCheckBoxMenuItem("fire");
		gray = new JCheckBoxMenuItem("Gray");
		islands = new JCheckBoxMenuItem("Islands");
		ocean = new JCheckBoxMenuItem("Ocean");
		sun = new JCheckBoxMenuItem("Sun");
		legendMenu = new JCheckBoxMenuItem("Show Legend");
		scalarMenu = new JCheckBoxMenuItem("Show as Scalars");
		stackMenu = new JCheckBoxMenuItem("Stack Scalars");

		legendMenu.setSelected(showLegend);
		scalarMenu.setSelected(asScalar);
		stackMenu.setSelected(stackScalar);

		if(this.colorMap.getName().equalsIgnoreCase("Sun")){
			contextHide();
			sun.setSelected(true);
		}else if(this.colorMap.getName().equalsIgnoreCase("Ocean")){
			contextHide();
			ocean.setSelected(true);
		}else if(this.colorMap.getName().equalsIgnoreCase("Islands")){
			contextHide();			
			islands.setSelected(true);			
		}else if(this.colorMap.getName().equalsIgnoreCase("Gray")){
			contextHide();			
			gray.setSelected(true);			
		}else if(this.colorMap.getName().equalsIgnoreCase("Fire")){
			contextHide();			
			fire.setSelected(true);
		}else if(this.colorMap.getName().equalsIgnoreCase("Colorful")){
			contextHide();
			colorful.setSelected(true);
		}


		colorful.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {							
				VectorTrack.this.colorMap = new ColorMap_Colorful();
				VectorTrack.this.drawIMG(VectorTrack.this.start,Project.getInstance().getZoom());
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});
		fire.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {							
				VectorTrack.this.colorMap = new ColorMap_Fire();
				VectorTrack.this.drawIMG(VectorTrack.this.start,Project.getInstance().getZoom());
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});
		gray.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {							
				VectorTrack.this.colorMap = new ColorMap_Gray();
				VectorTrack.this.drawIMG(VectorTrack.this.start,Project.getInstance().getZoom());
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});
		islands.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {							
				VectorTrack.this.colorMap = new ColorMap_Islands();
				VectorTrack.this.drawIMG(VectorTrack.this.start,Project.getInstance().getZoom());
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});
		ocean.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {							
				VectorTrack.this.colorMap = new ColorMap_Ocean();
				VectorTrack.this.drawIMG(VectorTrack.this.start,Project.getInstance().getZoom());
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});
		sun.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {							
				VectorTrack.this.colorMap = new ColorMap_Sun();
				VectorTrack.this.drawIMG(VectorTrack.this.start,Project.getInstance().getZoom());
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});
		legendMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showLegend = !showLegend;
				VectorTrack.this.drawIMG(VectorTrack.this.start, Project.getInstance().getZoom());
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});
		scalarMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				asScalar = !asScalar;
				VectorTrack.this.drawIMG(VectorTrack.this.start, Project.getInstance().getZoom());
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});
		stackMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stackScalar = !stackScalar;
				VectorTrack.this.drawIMG(VectorTrack.this.start, Project.getInstance().getZoom());
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});

		menu.add(colorful);
		menu.add(fire);
		menu.add(gray);
		menu.add(islands);
		menu.add(ocean);
		menu.add(sun);
		menu.add(legendMenu);
		menu.add(scalarMenu);
		menu.add(stackMenu);
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
		DoubleBuffer iB;
		try {
			iB = fChannel.map(FileChannel.MapMode.READ_ONLY, 0, fChannel.size()).asDoubleBuffer();
			double time;
			while(iB.hasRemaining()){
				if((time=iB.get())>=from)break; //time				
				for(int i=0;i<this.dimension;i++)iB.get();//data
			}	
			if(!iB.hasRemaining())return null;
			iB.position(iB.position()-1<0?0:iB.position()-1);
			ArrayList<Double> data = new ArrayList<Double>();
			while(iB.hasRemaining()&&(time = iB.get())<=to){
				data.add(time);
				for(int i=0;i<this.dimension;i++)iB.get();//data
			}
			double[] ret = new double[data.size()];
			Iterator<Double> iD = data.iterator();
			int i=0;
			while(iD.hasNext()){
				ret[i++] = iD.next();
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
			raf = new RandomAccessFile(file,"r");
			fChannel = raf.getChannel();








		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
}


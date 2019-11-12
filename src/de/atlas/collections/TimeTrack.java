package de.atlas.collections;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import de.atlas.messagesystem.*;
import de.atlas.data.Project;
import de.atlas.data.TimeType;
import de.atlas.misc.AtlasProperties;

public class TimeTrack extends JPanel implements MouseMotionListener, MouseListener, MouseWheelListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Font f;
	private FontMetrics fm;
	private int smallRaster, bigRaster,smallFrameRaster, bigFrameRaster;
	private BufferedImage timelineimg;
	private double zoom=1.0;
	private int start;
	public boolean showMarkerTime = false;
	public boolean useLoopArea = false;
	public double loopStart;
	public double loopEnd;
	private int mousesensitivityspace = AtlasProperties.getInstance()
	.getMouseSensitivitySpace();	
	private TimeType time_type = TimeType.MILLIS;
	private long currentTime_ = 0;

	private boolean inLoopResizingArea = false;

		public TimeTrack(int time){
		this.setBounds(0, 0, time, 30);
		this.setMinimumSize(new Dimension(200,30));
		this.setMaximumSize(new Dimension(5000,30));
		this.smallRaster=AtlasProperties.getInstance().getTimeTrackSmallRaster();
		this.bigRaster = AtlasProperties.getInstance().getTimeTrackBigRaster();
		this.smallFrameRaster=AtlasProperties.getInstance().getTimeTrackSmallFrameRaster();
		this.bigFrameRaster=AtlasProperties.getInstance().getTimeTrackBigFrameRaster();
		this.f =AtlasProperties.getInstance().getTimeTrackFont();
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.addMouseWheelListener(this);
		drawIMG(0);
		MessageManager.getInstance().addZoomChangedListener(new ZoomChangedListener(){
			@Override
			public void zoomChanged(ZoomEvent e) {
				adjustZoom(e.getZoom());
			}			 
		});
		MessageManager.getInstance().addSlotChangedListener(new SlotChangedListener(){
			@Override
			public void slotChanged(SlotEvent e) {
				updateSlot(e.getStart());
			}			 
		});


	}
	private void updateSlot(int start){
		//drawIMG(start);
		this.start=start;
		this.repaint();
	}
	private void adjustZoom(double zoom){
		this.zoom=zoom;

		if(this.zoom>2.5){
			this.bigRaster = 20;
		}else if(this.zoom >0.55){
			this.bigRaster=100;
		}else if(this.zoom>0.1){
			this.bigRaster=500;
		}else if(this.zoom>0.02){
			this.bigRaster=2500;
		}else if(this.zoom>0.004){
			this.bigRaster=12500;
		}else if(this.zoom>0.0008){
			this.bigRaster=62500;
		}else{
			this.bigRaster=312500;
		}
		this.smallRaster=this.bigRaster/5;


		if(this.zoom>1.25){
			this.bigFrameRaster=(int) (1000.0/Project.getInstance().getProjectFPS());
		}else if(this.zoom>0.25){
			this.bigFrameRaster=(int) (1000.0/Project.getInstance().getProjectFPS()*5);
		}else if(this.zoom>0.05){
			this.bigFrameRaster=(int) (1000.0/Project.getInstance().getProjectFPS()*25);
		}else if(this.zoom>0.01){
			this.bigFrameRaster=(int) (1000.0/Project.getInstance().getProjectFPS()*125);
		}else if(this.zoom>0.002){
			this.bigFrameRaster=(int) (1000.0/Project.getInstance().getProjectFPS()*125*5);
		}else if(this.zoom>0.0003){
			this.bigFrameRaster=(int) (1000.0/Project.getInstance().getProjectFPS()*25*125);
		}else{
			this.bigFrameRaster=(int) (1000.0/Project.getInstance().getProjectFPS()*125*125);
		}

		if(this.bigFrameRaster<1000.0/Project.getInstance().getProjectFPS()){
			this.bigFrameRaster=(int) (1000.0/Project.getInstance().getProjectFPS());
		}
		this.smallFrameRaster = this.bigFrameRaster/5;
		if(this.smallFrameRaster<1000.0/Project.getInstance().getProjectFPS()){
			this.smallFrameRaster=(int) (1000.0/Project.getInstance().getProjectFPS());
		}


		this.repaint();

	}

	private void drawIMG(int start){
		timelineimg = new BufferedImage( this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = timelineimg.createGraphics();				
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);	
		g2.setFont(f);
		if (fm == null) {
			fm = g2.getFontMetrics();						
		}		
		g2.setColor(Color.lightGray);
		g2.fill3DRect(0, 0, this.getWidth(), this.getHeight(), true);
		g2.setColor(Color.black);
		int r, s, offset;
		NumberFormat formatter2 = new DecimalFormat("#00");
		NumberFormat formatter3 = new DecimalFormat("#000");
		int frameDuration = 0;		
		switch (time_type) {
		case MILLIS:
			r = (int)(((this.getWidth())/(this.bigRaster*this.zoom)))+1;
			s = (int)(((start/this.bigRaster)*this.bigRaster + this.bigRaster));
			offset= s-start;
			for(int i = 0; i<r;i++)
			{	

				if(Project.getInstance()!=null && (s+i*this.bigRaster)<=Project.getInstance().getProjectLength()){
					g2.drawLine((int)((offset+i*this.bigRaster)*this.zoom), 
							2, 
							(int)((offset+i*this.bigRaster)*this.zoom), 
							this.getHeight()-2);

					g2.drawString(String.valueOf(s+i*this.bigRaster), 
							(int)((offset+i*this.bigRaster+2)*this.zoom), 
							this.f.getSize()+2);
				}
			}

			r = (int)((this.getWidth())/(this.smallRaster*this.zoom))+1;
			s = (start/this.smallRaster)*this.smallRaster + this.smallRaster;
			offset= s-start;
			for(int i = 0; i<r;i++)
			{			
				if(Project.getInstance()!=null && (s+i*this.smallRaster)<=Project.getInstance().getProjectLength()){
					g2.drawLine((int)((offset+i*this.smallRaster)*this.zoom), 
							(int) (this.getHeight()*(2/3.0)), 
							(int)((offset+i*this.smallRaster)*this.zoom), 
							this.getHeight());						
				}
			}

			break;
		case HHmmssSS:
			r = (int)(((this.getWidth())/(this.bigRaster*this.zoom)))+1;
			s = (int)(((start/this.bigRaster)*this.bigRaster + this.bigRaster));			
			offset= s-start;
			for(int i = 0; i<r;i++)
			{			
				if(Project.getInstance()!=null && (s+i*this.bigRaster)<=Project.getInstance().getProjectLength()){
					g2.drawLine((int)((offset+i*this.bigRaster)*this.zoom), 
							2, 
							(int)((offset+i*this.bigRaster)*this.zoom), 
							this.getHeight()-2);

					long hour = ((s+i*this.bigRaster) % (24*60*60*1000))/(60 * 60 * 1000);
					long min = ((s+i*this.bigRaster) % (60 * 60 * 1000))/(60*1000);
					long sec = ((s+i*this.bigRaster) % (60 * 1000))/(1000);
					long ms = ((s+i*this.bigRaster) % (1000));
					g2.drawString(formatter2.format(hour)+":"+formatter2.format(min)+":"+formatter2.format(sec)+":"+formatter3.format(ms), 
							(int)((offset+i*this.bigRaster+2)*this.zoom), 
							this.f.getSize()+2);
				}
			}

			r = (int)((this.getWidth())/(this.smallRaster*this.zoom))+1;
			s = (start/this.smallRaster)*this.smallRaster + this.smallRaster;
			offset= s-start;
			for(int i = 0; i<r;i++)
			{			
				if(Project.getInstance()!=null && (s+i*this.smallRaster)<=Project.getInstance().getProjectLength()){
					g2.drawLine((int)((offset+i*this.smallRaster)*this.zoom), 
							(int) (this.getHeight()*(2/3.0)), 
							(int)((offset+i*this.smallRaster)*this.zoom), 
							this.getHeight());	
				}
			}
			break;
		case HHmmssff:			
			frameDuration = (int) ((1000.0/Project.getInstance().getProjectFPS()));
			r = (int)(((this.getWidth())/(this.bigFrameRaster*this.zoom)))+1;
			s = (int)(((start/this.bigFrameRaster)*this.bigFrameRaster + this.bigFrameRaster));			
			offset= s-start-frameDuration;
			for(int i = 0; i<r;i++)
			{			
				if(Project.getInstance()!=null && (s+i*this.bigFrameRaster)<=Project.getInstance().getProjectLength()){
					g2.drawLine((int)((offset+i*this.bigFrameRaster)*this.zoom), 
							2, 
							(int)((offset+i*this.bigFrameRaster)*this.zoom), 
							this.getHeight()-2);

					long hour = ((s+i*this.bigFrameRaster) % (24*60*60*1000))/(60 * 60 * 1000);
					long min = ((s+i*this.bigFrameRaster) % (60 * 60 * 1000))/(60*1000);
					long sec = ((s+i*this.bigFrameRaster) % (60 * 1000))/(1000);
					long ms = (long) (((s+i*this.bigFrameRaster) % (1000))/(1000.0/Project.getInstance().getProjectFPS()));
					g2.drawString(formatter2.format(hour)+":"+formatter2.format(min)+":"+formatter2.format(sec)+":"+formatter2.format(ms), 
							(int)((offset+i*this.bigFrameRaster+2)*this.zoom), 
							this.f.getSize()+2);
				}
			}

			r = (int)((this.getWidth())/(this.smallFrameRaster*this.zoom))+1;
			s = (start/this.smallFrameRaster)*this.smallFrameRaster + this.smallFrameRaster;
			offset= s-start-frameDuration;
			for(int i = 0; i<r;i++)
			{			
				if(Project.getInstance()!=null && (s+i*this.smallFrameRaster)<=Project.getInstance().getProjectLength()){
					g2.drawLine((int)((offset+i*this.smallFrameRaster)*this.zoom), 
							(int) (this.getHeight()*(2/3.0)), 
							(int)((offset+i*this.smallFrameRaster)*this.zoom), 
							this.getHeight());						
				}
			}
			break;
		case FRAMES:		
			frameDuration = (int) ((1000.0/Project.getInstance().getProjectFPS()));
			r = (int)(((this.getWidth())/(this.bigFrameRaster*this.zoom)))+1;
			s = (int)(((start/this.bigFrameRaster)*this.bigFrameRaster + this.bigFrameRaster));			
			offset= s-start-frameDuration;
			for(int i = 0; i<r;i++)				
			{			
				if(Project.getInstance()!=null && (s+i*this.bigFrameRaster)<=Project.getInstance().getProjectLength()){
					g2.drawLine((int)((offset+i*this.bigFrameRaster)*this.zoom), 
							2, 
							(int)((offset+i*this.bigFrameRaster)*this.zoom), 
							this.getHeight()-2);

					g2.drawString(String.valueOf((s+i*this.bigFrameRaster)/frameDuration), 
							(int)((offset+i*this.bigFrameRaster+2)*this.zoom), 
							this.f.getSize()+2);
				}
			}

			r = (int)((this.getWidth())/(this.smallFrameRaster*this.zoom))+1;
			s = (start/this.smallFrameRaster)*this.smallFrameRaster + this.smallFrameRaster;
			offset= s-start-frameDuration;
			for(int i = 0; i<r;i++)
			{			
				if(Project.getInstance()!=null && (s+i*this.smallFrameRaster)<=Project.getInstance().getProjectLength()){
					g2.drawLine((int)((offset+i*this.smallFrameRaster)*this.zoom), 
							(int) (this.getHeight()*(2/3.0)), 
							(int)((offset+i*this.smallFrameRaster)*this.zoom), 
							this.getHeight());						
				}
			}
			break;
		}
		//draw loop
		if(this.useLoopArea){
			g2.setColor(Color.cyan);
			g2.fillRect((int)((loopStart-start)*this.zoom), this.getHeight()/2, (int)((loopEnd-loopStart)*this.zoom), this.getHeight()/2);
		}
	}

	public void paint(Graphics g) {
		this.drawIMG(start);		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);			
		if ( timelineimg != null )
			g2.drawImage( timelineimg, 0, 0, this );

	}
	public void mouseReleased(MouseEvent e) {

	}
	public void showContextMenu(MouseEvent evt){
		JPopupMenu menu = new JPopupMenu();
		//{FRAMES, MILLIS, HHmmssSS, HHmmssff}
		JRadioButtonMenuItem millisItem = new JRadioButtonMenuItem("Milliseconds");
		JRadioButtonMenuItem absolutItem = new JRadioButtonMenuItem("Time (HHmmssSS)");
		JRadioButtonMenuItem frameItem = new JRadioButtonMenuItem("Frames");
		JRadioButtonMenuItem milliframeItem = new JRadioButtonMenuItem("Time:Frames (HHmmssff)");
		JCheckBoxMenuItem showMarkerTimesItem = new JCheckBoxMenuItem("show Marker Time",showMarkerTime);
		JCheckBoxMenuItem useLoopAreaItem = new JCheckBoxMenuItem("use Loop Area",useLoopArea);
		final int mouseX=evt.getX();


		showMarkerTimesItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showMarkerTime = !showMarkerTime;
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});
		millisItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				time_type = TimeType.MILLIS;
				MessageManager.getInstance().timeTypeChanged(new TimeTypeEvent(this,time_type));
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});
		absolutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				time_type = TimeType.HHmmssSS;
				MessageManager.getInstance().timeTypeChanged(new TimeTypeEvent(this,time_type));
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});
		frameItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				time_type = TimeType.FRAMES;
				MessageManager.getInstance().timeTypeChanged(new TimeTypeEvent(this,time_type));
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});
		milliframeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				time_type = TimeType.HHmmssff;
				MessageManager.getInstance().timeTypeChanged(new TimeTypeEvent(this,time_type));
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			}
		});

		useLoopAreaItem.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				useLoopArea = !useLoopArea;
				MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
				loopStart = (long)((double)(mouseX-100)/zoom)+start;
				loopEnd = (long)((double)(mouseX+100)/zoom)+start;
			}
		});

		switch (time_type) {
		case MILLIS:
			millisItem.setSelected(true);
			break;
		case HHmmssSS:
			absolutItem.setSelected(true);
			break;
		case HHmmssff:			
			milliframeItem.setSelected(true);
			break;
		case FRAMES:		
			frameItem.setSelected(true);
			break;
		}

		menu.add(millisItem);
		menu.add(absolutItem);
		menu.add(frameItem);
		menu.add(milliframeItem);
		menu.add(showMarkerTimesItem);
		menu.add(useLoopAreaItem);
		menu.show(this, evt.getX(), evt.getY()); 
	}
	public void mouseClicked(MouseEvent arg0) {
		
	}
	public void mouseEntered(MouseEvent arg0) {

	}
	public void mouseExited(MouseEvent arg0) {

	}
	public void mousePressed(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON3){
			showContextMenu(e);
		}else if (e.getButton() == MouseEvent.BUTTON1 && this.getCursor().getType()==Cursor.DEFAULT_CURSOR) {

			currentTime_ = (int) ((((e.getX() / Project.getInstance().getZoom()) + Project.getInstance().getLcoll().timeScrollBar_.getValue())));

			MessageManager.getInstance().timeChanged(
					new TimeEvent(this, currentTime_));
			this.repaint();
		}


    }
	public void mouseMoved(MouseEvent e) {
		if(!this.useLoopArea||e.getY()<=this.getHeight()/2){
			inLoopResizingArea  = false;
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			return;
		}
		if((e.getX() / Project.getInstance().getZoom()) + start > getLoopStart() && (e.getX() / Project.getInstance().getZoom()) + start < getLoopStart() + mousesensitivityspace	/ Project.getInstance().getZoom()) {
			setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
			inLoopResizingArea  = true;
		} else if ((e.getX() / Project.getInstance().getZoom()) + start < getLoopEnd()	&& (e.getX() / Project.getInstance().getZoom())	+ start > getLoopEnd() - mousesensitivityspace	/ Project.getInstance().getZoom()) {
			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
			inLoopResizingArea  = true;
		} else{
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			inLoopResizingArea  = false;
		}

	}
	public void mouseDragged(MouseEvent e){
		if (this.getCursor().getType()==Cursor.DEFAULT_CURSOR) {
			currentTime_ = (int) ((((e.getX() / Project.getInstance().getZoom()) + Project.getInstance().getLcoll().timeScrollBar_.getValue())));

			MessageManager.getInstance().timeChanged(
					new TimeEvent(this, currentTime_));
			this.repaint();
			return;
		}
		if (!this.useLoopArea) return;
		if (this.getCursor().getType() == (Cursor.E_RESIZE_CURSOR)) {
			if (start + (e.getX() / Project.getInstance().getZoom()) >= loopStart+ 10 / Project.getInstance().getZoom()){
				loopEnd = start + (e.getX() / Project.getInstance().getZoom());
			}
		}else if (this.getCursor().getType() == (Cursor.W_RESIZE_CURSOR)) {// VERKLEINERN/VERGROESSERN
			if (start + (e.getX() / Project.getInstance().getZoom()) <= loopEnd - 10 / Project.getInstance().getZoom()){
				loopStart = start + (e.getX() / Project.getInstance().getZoom());
			}
		}
		this.getParent().getParent().getParent().repaint();		
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getWheelRotation() == -1) {
			Project.getInstance().getLcoll().getTimeTrack_().zoomIn();
		} else {
			Project.getInstance().getLcoll().getTimeTrack_().zoomOut();
		}

	}


	public boolean isUseLoopArea() {
		return useLoopArea;
	}
	public long getLoopStart() {
		return (long)loopStart;
	}
	public long getLoopEnd() {
		return (long)loopEnd;
	}

	public boolean isInLoopResizingArea() {
		return inLoopResizingArea;
	}
}

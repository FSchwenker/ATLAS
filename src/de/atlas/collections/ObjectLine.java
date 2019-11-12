package de.atlas.collections;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.atlas.data.Project;
import de.atlas.messagesystem.ClassChangedEvent;
import de.atlas.messagesystem.ClassChangedListener;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.MinMaxChangedEvent;
import de.atlas.messagesystem.MinMaxChangedListener;
import de.atlas.messagesystem.ZoomEvent;

public class ObjectLine extends JPanel implements Comparable<ObjectLine>{
	/**
	 * 
	 */

	private static final long serialVersionUID = 1L;
	public static final int SMALL = 50;
	public static final int MEDIUM = 100;
	public static final int LARGE = 200;
	
	private final float minMaxFontFactor = 0.75f;

	protected JPanel nameP;	
	private JLabel minLabel, maxLabel;
	private LabelTrack labeltrackP;
	private TimeTrack timetrackP;
	private ScalarTrack scalartrackP;
	private VectorTrack vectortrackP;
	private DataTrack dataTrackP;
	private JButton zoominBtn;
	private JButton zoomoutBtn;			
	private JLabel nameLabel;
	private int order;
	private boolean active=true;
	private boolean learnable=false;
	private boolean sendToMatlab = false;

	private int trackHeight = ObjectLine.MEDIUM;
	
	public ObjectLine(LabelTrack lt){
		this.labeltrackP=lt;
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setBounds(0,0,100+lt.getWidth(),trackHeight);
		this.setPreferredSize(new Dimension(lt.getWidth(),trackHeight));
		this.setBorder(BorderFactory.createLineBorder(Color.black));		

		this.nameP = new JPanel();		
		this.nameP.setLayout(new GridLayout(1, 0, 0, 0));
		this.nameP.setMinimumSize(new Dimension(Project.getInstance().getLcoll().getNameLabelWidth(),54));
		this.nameP.setPreferredSize(new Dimension(Project.getInstance().getLcoll().getNameLabelWidth(),54));
		this.nameP.setMaximumSize(new Dimension(Project.getInstance().getLcoll().getNameLabelWidth(),500));


		this.nameLabel=new JLabel("<HTML><BODY>"+this.labeltrackP.getName()+"<BR><font color= \"6e6e6e\">"+this.labeltrackP.getLabelClass().toString()+"</BODY></HTML>");
		this.nameLabel.setHorizontalAlignment(JLabel.LEFT);		
		this.nameP.add(this.nameLabel);
		this.nameP.setBounds(0, 0, 100, trackHeight);
		this.nameP.setBackground(Color.lightGray);
		this.add(nameP);


		this.add(labeltrackP);
		this.labeltrackP.setBounds(Project.getInstance().getLcoll().getNameLabelWidth(), this.getY(), lt.getWidth(), lt.getHeight());	
		MessageManager.getInstance().addClassChangedListener(new ClassChangedListener(){

			public void classChanged(ClassChangedEvent e) {
				ObjectLine.this.nameLabel.setText("<HTML><BODY>"+ObjectLine.this.labeltrackP.getName()+"<BR><font color= \"6e6e6e\">"+ObjectLine.this.labeltrackP.getLabelClass().toString()+"</BODY></HTML>");							
			}
		});
	}

	public ObjectLine(ScalarTrack st){
		this.scalartrackP=st;

		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setBounds(0,0,100+st.getWidth(),trackHeight);
		this.setPreferredSize(new Dimension(st.getWidth(),trackHeight));
		this.setBorder(BorderFactory.createLineBorder(Color.black));

		this.nameP = new JPanel();
		this.nameP.setLayout(new GridLayout(3, 1, 0, 0));
		
	    
		this.nameP.setMinimumSize(new Dimension(Project.getInstance().getLcoll().getNameLabelWidth(),54));
		this.nameP.setPreferredSize(new Dimension(Project.getInstance().getLcoll().getNameLabelWidth(),54));
		this.nameP.setMaximumSize(new Dimension(Project.getInstance().getLcoll().getNameLabelWidth(),500));
		this.nameLabel=new JLabel("<HTML><BODY>"+this.scalartrackP.getName()+"</BODY></HTML>");
		this.minLabel = new JLabel(String.valueOf(st.getMin()));
		this.maxLabel = new JLabel(String.valueOf(st.getMax()));
		this.minLabel.setHorizontalAlignment(JLabel.RIGHT);
		this.maxLabel.setHorizontalAlignment(JLabel.RIGHT);
		this.minLabel.setVerticalAlignment(JLabel.BOTTOM);
		this.maxLabel.setVerticalAlignment(JLabel.TOP);
		this.minLabel.setFont(nameLabel.getFont().deriveFont(minMaxFontFactor*(float)nameLabel.getFont().getSize()));
		this.maxLabel.setFont(nameLabel.getFont().deriveFont(minMaxFontFactor*(float)nameLabel.getFont().getSize()));
		this.nameP.add(this.maxLabel);
		this.nameP.add(this.nameLabel);
		this.nameP.add(this.minLabel);
		this.nameP.setBounds(0,0,100,trackHeight);
		this.nameP.setBackground(Color.lightGray);
		this.add(this.nameP);
		this.add(this.scalartrackP);
		this.scalartrackP.setBounds(Project.getInstance().getLcoll().getNameLabelWidth(), this.getY(), st.getWidth(), st.getHeight());
		MessageManager.getInstance().addMinMaxChangedListener(new MinMaxChangedListener(){

			@Override
			public void minMaxChanged(MinMaxChangedEvent e) {
				if(e.getObjectLine().getTrack().equals(scalartrackP)){
					minLabel.setText(String.valueOf(scalartrackP.getMin()));
					maxLabel.setText(String.valueOf(scalartrackP.getMax()));
				}
				
			}
			
		});
	}

	public ObjectLine(VectorTrack vt){
		this.vectortrackP=vt;
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setBounds(0,0,100+vt.getWidth(),trackHeight);
		this.setPreferredSize(new Dimension(vt.getWidth(),trackHeight));
		this.setBorder(BorderFactory.createLineBorder(Color.black));

		this.nameP = new JPanel();
		this.nameP.setLayout(new GridLayout(3, 1, 0, 0));

		this.nameP.setMinimumSize(new Dimension(Project.getInstance().getLcoll().getNameLabelWidth(),54));
		this.nameP.setPreferredSize(new Dimension(Project.getInstance().getLcoll().getNameLabelWidth(),54));
		this.nameP.setMaximumSize(new Dimension(Project.getInstance().getLcoll().getNameLabelWidth(),500));
		this.nameLabel=new JLabel("<HTML><BODY>"+this.vectortrackP.getName()+"</BODY></HTML>");
		this.minLabel = new JLabel(String.valueOf(vt.getMin()));
		this.maxLabel = new JLabel(String.valueOf(vt.getMax()));
		this.minLabel.setHorizontalAlignment(JLabel.RIGHT);
		this.maxLabel.setHorizontalAlignment(JLabel.RIGHT);
		this.minLabel.setVerticalAlignment(JLabel.BOTTOM);
		this.maxLabel.setVerticalAlignment(JLabel.TOP);
		this.minLabel.setFont(nameLabel.getFont().deriveFont(minMaxFontFactor*(float)nameLabel.getFont().getSize()));
		this.maxLabel.setFont(nameLabel.getFont().deriveFont(minMaxFontFactor*(float)nameLabel.getFont().getSize()));
		this.nameP.add(this.maxLabel);
		this.nameP.add(this.nameLabel);
		this.nameP.add(this.minLabel);
		this.nameP.setBounds(0,0,100,trackHeight);
		this.nameP.setBackground(Color.lightGray);
		this.add(this.nameP);
		this.add(this.vectortrackP);
		this.vectortrackP.setBounds(Project.getInstance().getLcoll().getNameLabelWidth(), this.getY(), vt.getWidth(), vt.getHeight());
		MessageManager.getInstance().addMinMaxChangedListener(new MinMaxChangedListener(){

			@Override
			public void minMaxChanged(MinMaxChangedEvent e) {
				if(e.getObjectLine().getTrack().equals(vectortrackP)){
					minLabel.setText(String.valueOf(vectortrackP.getMin()));
					maxLabel.setText(String.valueOf(vectortrackP.getMax()));
				}

			}

		});

	}

	public ObjectLine(DataTrack dt){
		this.dataTrackP=dt;
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setBounds(0,0,100+dt.getWidth(),trackHeight);
		//this.setPreferredSize(new Dimension(dt.getWidth(),trackHeight));
		this.setBorder(BorderFactory.createLineBorder(Color.black));

		this.nameP = new JPanel();
		this.nameP.setLayout(new GridLayout(3, 1, 0, 0));

		this.nameP.setMinimumSize(new Dimension(Project.getInstance().getLcoll().getNameLabelWidth(),54));
		this.nameP.setPreferredSize(new Dimension(Project.getInstance().getLcoll().getNameLabelWidth(),54));
		this.nameP.setMaximumSize(new Dimension(Project.getInstance().getLcoll().getNameLabelWidth(),500));
		this.nameLabel=new JLabel("<HTML><BODY>"+this.dataTrackP.getName()+"</BODY></HTML>");
		this.minLabel = new JLabel(String.valueOf(dt.getMin()));
		this.maxLabel = new JLabel(String.valueOf(dt.getMax()));
		this.minLabel.setHorizontalAlignment(JLabel.RIGHT);
		this.maxLabel.setHorizontalAlignment(JLabel.RIGHT);
		this.minLabel.setVerticalAlignment(JLabel.BOTTOM);
		this.maxLabel.setVerticalAlignment(JLabel.TOP);
		this.minLabel.setFont(nameLabel.getFont().deriveFont(minMaxFontFactor*(float)nameLabel.getFont().getSize()));
		this.maxLabel.setFont(nameLabel.getFont().deriveFont(minMaxFontFactor*(float)nameLabel.getFont().getSize()));
		this.nameP.add(this.maxLabel);
		this.nameP.add(this.nameLabel);
		this.nameP.add(this.minLabel);
		this.nameP.setBounds(0,0,100,trackHeight);
		this.nameP.setBackground(Color.lightGray);
		this.add(this.nameP);
		this.add(this.dataTrackP);
		this.dataTrackP.setBounds(Project.getInstance().getLcoll().getNameLabelWidth(), this.getY(), dt.getWidth(), dt.getHeight());
		MessageManager.getInstance().addMinMaxChangedListener(new MinMaxChangedListener(){

			@Override
			public void minMaxChanged(MinMaxChangedEvent e) {
				if(e.getObjectLine().getTrack().equals(dataTrackP)){
					minLabel.setText(String.valueOf(dataTrackP.getMin()));
					maxLabel.setText(String.valueOf(dataTrackP.getMax()));
				}

			}

		});

	}

	public ObjectLine(TimeTrack tt){
		this.timetrackP=tt;	
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setBounds(0,0,100+tt.getWidth(),tt.getHeight()+2);
		this.setPreferredSize(new Dimension(tt.getWidth(),tt.getHeight()+2));
		this.setBorder(BorderFactory.createLineBorder(Color.black));

		this.nameP = new JPanel();		

		this.nameP.setLayout(new GridLayout(1,2,0,0));
		this.nameP.setMinimumSize(new Dimension(100,30));
		this.nameP.setPreferredSize(new Dimension(100,30));
		this.nameP.setMaximumSize(new Dimension(100,30));
		//		this.nameLabel=new JLabel("");
		//this.nameP.add(this.nameLabel);
		this.nameP.setBounds(1, 1, 99, tt.getHeight()-1);
		this.nameP.setBackground(Color.lightGray);		
		this.add(nameP);


		this.add(timetrackP);
		this.timetrackP.setBounds(100, this.getY(), tt.getWidth(), tt.getHeight());

		zoominBtn = new JButton("+");		
		//zoominBtn.setMinimumSize(new Dimension(45,30));
		zoominBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				zoomIn();
			}
		});
		zoomoutBtn = new JButton("-");
		//zoomoutBtn.setMinimumSize(new Dimension(45,30));

		zoomoutBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				zoomOut();
			}
		});
	/*	MessageManager.getInstance().addLabelSelectionListener(new LabelSelectionListener(){
			@Override
			public void selectionChanged(LabelSelectionEvent e) {				
				ObjectLine.this.activeLabel = e.getLabelObject();
				ObjectLine.this.preLabel = e.getPrevObject();
				ObjectLine.this.postLabel = e.getPostObject();
			}			 
		});*/



		this.nameP.add(zoominBtn);
		this.nameP.add(zoomoutBtn);

		zoominBtn.setInputMap(WHEN_FOCUSED,new InputMap());
		zoomoutBtn.setInputMap(WHEN_FOCUSED,new InputMap());

		zoominBtn.setFocusable(false);
		zoomoutBtn.setFocusable(false); 

		//zoominBtn.addKeyListener(AnnotationKeyListener.getInstance());
		/* {
			@Override
			public void keyPressed(KeyEvent e) {
				MessageManager.getInstance().keyTyped(e);
				if(e.getKeyCode()==KeyEvent.VK_SPACE){
					MessageManager.getInstance().playPause(new PlayPauseEvent(this,false));
				}else if(e.getKeyCode()==KeyEvent.VK_L){
					MessageManager.getInstance().playPause(new PlayPauseEvent(this,true));					
				}else if(e.getKeyCode()==KeyEvent.VK_CONTROL){
					MessageManager.getInstance().unlockLabels(new UnlockLabelsEvent(this,true));
				}
			}
			public void keyReleased(KeyEvent e){
				if(e.getKeyCode()==KeyEvent.VK_CONTROL){
					MessageManager.getInstance().unlockLabels(new UnlockLabelsEvent(this,false));
				}
			}
		});*/
		//zoomoutBtn.addKeyListener(AnnotationKeyListener.getInstance());
		/* {
			@Override
			public void keyPressed(KeyEvent e) {
				MessageManager.getInstance().keyTyped(e);
				if(e.getKeyCode()==KeyEvent.VK_SPACE){
					MessageManager.getInstance().playPause(new PlayPauseEvent(this,false));
				}else if(e.getKeyCode()==KeyEvent.VK_L){
					MessageManager.getInstance().playPause(new PlayPauseEvent(this,true));					
				}else if(e.getKeyCode()==KeyEvent.VK_CONTROL){
					MessageManager.getInstance().unlockLabels(new UnlockLabelsEvent(this,true));
				}
			}
			public void keyReleased(KeyEvent e){
				if(e.getKeyCode()==KeyEvent.VK_CONTROL){
					MessageManager.getInstance().unlockLabels(new UnlockLabelsEvent(this,false));
				}
			}
		});*/				
	}

	public boolean isSendToMatlab() {
		return sendToMatlab;
	}

	public void setSendToMatlab(boolean sendToMatlab) {
		this.sendToMatlab = sendToMatlab;
	}

	public boolean isActive() {
		return active;
	}

	public void writeXML(){
		if(this.labeltrackP!=null){
			this.labeltrackP.writeXML();
		}
	}

	public void setActive(boolean active) {
		this.active = active;
		this.setVisible(this.active);
	}

	public void setName(String str){
		if(this.labeltrackP!=null){
			this.labeltrackP.setName(str);
			this.nameLabel.setText("<HTML><BODY>"+this.labeltrackP.getName()+"<BR><font color= \"6e6e6e\">"+this.labeltrackP.getLabelClass().toString()+"</BODY></HTML>");
		}else if(this.scalartrackP!=null){
			this.scalartrackP.setName(str);
			this.nameLabel.setText("<HTML><BODY>"+this.scalartrackP.getName()+"</BODY></HTML>");
		}else if(this.vectortrackP!=null){
			this.vectortrackP.setName(str);
			this.nameLabel.setText("<HTML><BODY>"+this.vectortrackP.getName()+"</BODY></HTML>");
		}else if(this.dataTrackP!=null) {
			this.dataTrackP.setName(str);
			this.nameLabel.setText("<HTML><BODY>" + this.dataTrackP.getName() + "</BODY></HTML>");
		}


	}

	public int getDataDimesion(){
		if(this.labeltrackP!=null){
			return 1;
		}else if(this.scalartrackP!=null){
			return 1;
		}else if(this.vectortrackP!=null){
			return this.vectortrackP.getDimension();
		}else if(this.dataTrackP!=null) {
			return this.dataTrackP.getDimension();
		}
		return 0;
	}

	public double[][] getData(long from, long to){
		if(this.labeltrackP!=null){
			return labeltrackP.getData(from,to);
		}else if(this.scalartrackP!=null){
			return scalartrackP.getData(from,to);
		}else if(this.vectortrackP!=null){
			return this.vectortrackP.getData(from,to);
		}else if(this.dataTrackP!=null){
			return this.dataTrackP.getData(from,to);
		}
		return null;
	}

	public double[] getDataTimePoints(long from, long to) {
		if(this.labeltrackP!=null){
			return labeltrackP.getDataTimePoints(from,to);
		}else if(this.scalartrackP!=null){
			return scalartrackP.getDataTimePoints(from,to);
		}else if(this.vectortrackP!=null){
			return this.vectortrackP.getDataTimePoints(from,to);
		}else if(this.dataTrackP!=null){
			return this.dataTrackP.getDataTimePoints(from,to);
		}
		return null;
	}

	public String getName(){

		if(this.labeltrackP!=null){
			return this.labeltrackP.getName();
		}else if(this.scalartrackP!=null){
			return 	this.scalartrackP.getName();
		}else if(this.vectortrackP!=null){
			return this.vectortrackP.getName();
		}else if(this.dataTrackP!=null){
			return this.dataTrackP.getName();
		}
		return null;
	}

	public Object getTrack(){

		if(this.labeltrackP!=null){
			return this.labeltrackP;
		}else if(this.scalartrackP!=null){
			return 	this.scalartrackP;
		}else if(this.vectortrackP!=null){
			return this.vectortrackP;
		}else if(this.dataTrackP!=null){
			return this.dataTrackP;
		}
		return null;
	}

	public String getType(){
		if(this.labeltrackP!=null){
			return "LabelTrack";
		}else if(this.scalartrackP!=null){
			return 	"ScalarTrack";
		}else if(this.vectortrackP!=null){
			return "VectorTrack";
		}else if(this.dataTrackP!=null){
			return "DataTrack";
		}
		return null;
	}

	public String getPath(){
		if(this.labeltrackP!=null){
			return this.labeltrackP.getPath();
		}else if(this.scalartrackP!=null){
			return 	this.scalartrackP.getPath();
		}else if(this.vectortrackP!=null){
			return 	this.vectortrackP.getPath();
		}else if(this.dataTrackP!=null){
			return 	this.dataTrackP.getPath();
		}
		return null;
	}

	public String toString(){
		if(this.active){			
			if(this.labeltrackP!=null){
				return "<HTML><BODY><font color= \"006e00\">"+this.labeltrackP.getName()+"</font><BR><font color= \"6e6e6e\">"+this.labeltrackP.getLabelClass().toString()+"</BODY></HTML>";
			}else{
				return "<HTML><BODY><font color= \"006e00\">"+this.getName()+"</font></BODY></HTML>";
			}
		}else{
			if(this.labeltrackP!=null){
				return "<HTML><BODY><font color= \"6e0000\">"+this.labeltrackP.getName()+"</font><BR><font color= \"6e6e6e\">"+this.labeltrackP.getLabelClass().toString()+"</BODY></HTML>";
			}else{
				return "<HTML><BODY><font color= \"6e0000\">"+this.getName()+"</font></BODY></HTML>";
			}
		}		
	}

	public void zoomIn(){
		if(Project.getInstance().getZoom()<10){			
			this.zoomoutBtn.setEnabled(true);
			MessageManager.getInstance().zoomChanged(new ZoomEvent(this,Project.getInstance().getZoom()*1.5));
			repaint();
		}else{
			this.zoominBtn.setEnabled(false);
		}
	}

	public void zoomOut(){
		if(Project.getInstance().getZoom()>0.0003){		
			this.zoominBtn.setEnabled(true);
			MessageManager.getInstance().zoomChanged(new ZoomEvent(this,Project.getInstance().getZoom()/1.5));
			repaint();
		}else{
			this.zoomoutBtn.setEnabled(false);
		}
	}

	public TimeTrack getTimetrackP() {
		return timetrackP;
	}

	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order){
		this.order = order;
	}

	public boolean isLearnable() {
		return learnable;
	}

	public void setLearnable(boolean learnable) {
		this.learnable = learnable;
	}

	@Override
	public int compareTo(ObjectLine o) {
		return ((Integer)order).compareTo((Integer)o.getOrder());
	}

	public JButton getZoominBtn() {
		return zoominBtn;
	}

	void addComponent( Container cont,GridBagLayout gbl,Component c,int x, int y,int width, int height,double weightx, double weighty ){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x; gbc.gridy = y;
		gbc.gridwidth = width; gbc.gridheight = height;
		gbc.weightx = weightx; gbc.weighty = weighty;
		gbl.setConstraints( c, gbc );
		cont.add( c );
	}

	public void setTrackHeight(int height) {
		this.trackHeight = height;
		this.nameP.setBounds(0, 0, nameP.getWidth(), height);
		if(labeltrackP!=null){
			this.setBounds(0,0,nameP.getWidth()+labeltrackP.getWidth(),height);
			this.setPreferredSize(new Dimension(nameP.getWidth()+labeltrackP.getWidth(),height));
			labeltrackP.setBounds(1,1,labeltrackP.getWidth(),height);
			labeltrackP.setPreferredSize(new Dimension(labeltrackP.getWidth(),height));
			
		}else if(scalartrackP!=null){
			this.setBounds(0,0,nameP.getWidth()+scalartrackP.getWidth(),height);
			this.setPreferredSize(new Dimension(nameP.getWidth()+scalartrackP.getWidth(),height));
			scalartrackP.setBounds(1,1,scalartrackP.getWidth(),height);
			scalartrackP.setPreferredSize(new Dimension(scalartrackP.getWidth(),height));
			
		}else if(vectortrackP!=null){
			this.setBounds(0,0,nameP.getWidth()+vectortrackP.getWidth(),height);
			this.setPreferredSize(new Dimension(nameP.getWidth()+vectortrackP.getWidth(),height));
			vectortrackP.setBounds(1,1,vectortrackP.getWidth(),height);
			vectortrackP.setPreferredSize(new Dimension(vectortrackP.getWidth(),height));

		}else if(dataTrackP!=null){
			this.setBounds(0,0,nameP.getWidth()+dataTrackP.getWidth(),height);
			this.setPreferredSize(new Dimension(nameP.getWidth()+dataTrackP.getWidth(),height));
			dataTrackP.setBounds(1,1,dataTrackP.getWidth(),height);
			//dataTrackP.setPreferredSize(new Dimension(dataTrackP.getWidth(),height));

		}
	}
	
	public int getTrackHeight(){
		return this.trackHeight;
	}

	public JPanel getNameP() {
		return nameP;
	}

	public void setAsScalar(boolean asScalar) {
		// TODO Auto-generated method stub
		if(vectortrackP!=null)vectortrackP.setAsScalar(asScalar);
	}

	public void setStacked(boolean isStacked) {
		if(vectortrackP!=null)vectortrackP.setStacked(isStacked);
	}

	public void setStyle(String style) {
		if(dataTrackP!=null)dataTrackP.setStyle(style);
	}
}

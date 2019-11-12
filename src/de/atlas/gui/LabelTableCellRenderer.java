package de.atlas.gui;


import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.TimeTypeChangedListener;
import de.atlas.messagesystem.TimeTypeEvent;
import de.atlas.misc.AtlasProperties;
import de.atlas.data.LabelClass;
import de.atlas.data.LabelClassEntity;
import de.atlas.data.LabelType;
import de.atlas.data.Project;
import de.atlas.data.TimeType;

public class LabelTableCellRenderer extends DefaultTableCellRenderer{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Format format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS");
	private Color unselectedForeground = Color.BLACK;
	private Color unselectedBackground = Color.WHITE;
	private TimeType timeType=TimeType.MILLIS;
	private NumberFormat formatter2 = new DecimalFormat("#00");
	private NumberFormat formatter3 = new DecimalFormat("#000");
	public LabelTableCellRenderer(){
		MessageManager.getInstance().addTimeTypeChangedListener(new TimeTypeChangedListener(){

			@Override
			public void timeTypeChanged(TimeTypeEvent e) {
				timeType = e.getTimeType();				
			}});
	}
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		long hour=0;
		long min=0;
		long sec=0;
		long ms=0;
		if (isSelected) {
			super.setForeground(table.getSelectionForeground());
			super.setBackground(table.getSelectionBackground());
		}
		else {
			super.setForeground((unselectedForeground != null) ? unselectedForeground : table.getForeground());
			super.setBackground((unselectedBackground != null) ? unselectedBackground : table.getBackground());
		}			      
		setFont(table.getFont());
		this.setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
		
		if(value instanceof LabelClassEntity){
			setBackground(((LabelClassEntity)value).getColor());			
			setText(((LabelClassEntity)value).getName());
			this.setToolTipText(((LabelClassEntity)value).getName());
		}if(value instanceof LabelClass){
			setText(((LabelClass)value).getName());
			this.setToolTipText(((LabelClass)value).getName());
		}else if(value instanceof Date){			
			setText(format.format(((Date)value)));
			this.setToolTipText(format.format(((Date)value)));
		}else if (value instanceof LabelType){
			switch(((LabelType)value)){
			case MANUAL:
				setForeground(AtlasProperties.getInstance().getManualColor());
				break;
			case AUTOMATIC:
				setForeground(AtlasProperties.getInstance().getAutoColor());
				break;
			case AUTO_ACCEPTED:
				setForeground(AtlasProperties.getInstance().getAcceptColor());	
				break;
			case AUTO_REJECTED:
				setForeground(AtlasProperties.getInstance().getRejectColor());	
				break;
			}
			setText(((LabelType)value).toString());
			this.setToolTipText(((LabelType)value).toString());
		}else if (value instanceof Double){
			setText(((Double)value).toString());
			this.setToolTipText(((Double)value).toString());
		}else if (value instanceof Long){
			this.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
			switch(timeType){
			case MILLIS:				
				setText(((Long)value).toString());
				this.setToolTipText(((Long)value).toString());
				break;
			case HHmmssSS:
				hour = (((Long)value) % (24*60*60*1000))/(60 * 60 * 1000);
				min =  (((Long)value) % (60 * 60 * 1000))/(60*1000);
				sec =  (((Long)value) % (60 * 1000))/(1000);
				ms = (((Long)value) % (1000));								
				setText(formatter2.format(hour)+":"+formatter2.format(min)+":"+formatter2.format(sec)+":"+formatter3.format(ms));
				this.setToolTipText(formatter2.format(hour)+":"+formatter2.format(min)+":"+formatter2.format(sec)+":"+formatter3.format(ms));
				break;
			case HHmmssff:
				long tmp =((Long)value); 
					tmp+=(1000.0/ Project.getInstance().getProjectFPS());
				hour = (tmp % (24*60*60*1000))/(60 * 60 * 1000);
				min = (tmp % (60 * 60 * 1000))/(60*1000);
				sec = (tmp % (60 * 1000))/(1000);
				ms = (long) ((tmp % (1000))/(1000.0/Project.getInstance().getProjectFPS()));
				setText(formatter2.format(hour)+":"+formatter2.format(min)+":"+formatter2.format(sec)+":"+formatter2.format(ms));
				this.setToolTipText(formatter2.format(hour)+":"+formatter2.format(min)+":"+formatter2.format(sec)+":"+formatter2.format(ms));
				break;
			case FRAMES:
				setText(String.valueOf((int) (((Long)value)/(1000.0/Project.getInstance().getProjectFPS()))+1) );
				this.setToolTipText(String.valueOf((int)(((Long)value)/(1000.0/Project.getInstance().getProjectFPS()))+1));
				break;
			}
			
		}else if (value instanceof String){
			setText(((String)value).toString());
			String tmp = "";
			int j = 1;
			for(int i =0; i<((String)value).toString().length();i++ ){
				if(i >= 37*j && ((String)value).toString().charAt(i) == ' ' ){
					tmp = tmp + ((String)value).toString().charAt(i) + "<BR>";
					j++;
				}else{
					tmp = tmp + ((String)value).toString().charAt(i);
				}
			}
			this.setToolTipText("<html>"+tmp+"</html>");
		}
		return this;
	}
}

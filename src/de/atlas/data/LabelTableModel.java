package de.atlas.data;



import java.util.Date;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class LabelTableModel extends AbstractTableModel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String[] columnNames; 
    private Vector<LabelObject> data = new Vector<LabelObject>();
    
    
    public LabelTableModel(String[] colNames){
    	this.columnNames=colNames;
    }
	@Override
	public int getColumnCount() {
		return this.columnNames.length;
	}

	@Override
	public int getRowCount() {
		return this.data.size();
	}
	
	@Override
	public Object getValueAt(int row, int col) {		
		Object ret = null;
		switch(col){		
		case 0:
			ret = data.get(row).getStart();
			break;
		case 1:
			ret = data.get(row).getEnd();
			break;
		case 2:
			ret = data.get(row).getComment();
			break;
		case 3:
			ret = data.get(row).getText();
			break;
		case 4:
			ret = data.get(row).getLabelClass();
			break;
		case 5:
			ret = data.get(row).getLabelClassEntity();
			break;
		case 6:
			ret = data.get(row).getValue();
			break;
		case 7:
			ret = data.get(row).getLabelType();
			break;
		case 8:
			ret = new Date(data.get(row).getTimestamp());
			break;					
		}
		
		return ret;
	}
	public LabelObject getRow(int row){
		if(row>=0 && row< this.data.size()){
			return data.get(row);
		}else{
			return null;
		}
		
	}
	 public String getColumnName(int col) {
	        return columnNames[col].toString();
	    }
	 public void setValueAt(LabelObject value, int row) {
	        data.set(row, value);
	        fireTableDataChanged();
	    }
	 public void addRow(LabelObject lo){		 
			 this.data.add(lo);
			 fireTableDataChanged();			 		
	 }
	 public void deleteRow(LabelObject lo){
		 this.data.remove(lo);
		 fireTableDataChanged();
	 }
}

package de.atlas.gui;

import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.atlas.collections.LabelTrack;
import de.atlas.collections.ObjectLine;
import de.atlas.data.LabelObject;
import de.atlas.data.LabelTableModel;
import de.atlas.data.Project;
import de.atlas.misc.AtlasProperties;
import de.atlas.messagesystem.LabelSelectionEvent;
import de.atlas.messagesystem.LabelSelectionListener;
import de.atlas.messagesystem.MessageManager;
import de.atlas.messagesystem.RepaintEvent;
import de.atlas.messagesystem.RepaintListener;
import de.atlas.messagesystem.SlotEvent;
import de.atlas.messagesystem.TimeTypeChangedListener;
import de.atlas.messagesystem.TimeTypeEvent;
import de.atlas.messagesystem.UpdateTracksEvent;
import de.atlas.messagesystem.UpdateTracksListener;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.ListSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LabelTrackTabular extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTable table;
	private JList list;
	private String[] columnNames = new String[] {"Start", "End", "Comment", "Text", "Class", "Entity", "Value", "State", "Date"};
	private JPanel panel;
	private JPanel panel_1;
	private Font f = new Font("monospaced", Font.BOLD, 15);
	

	/**
	 * Create the frame.
	 */
	public LabelTrackTabular() {
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 937, 512);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setMaximumSize(new Dimension(200,5000));

		JLabel lblLabeltracks = new JLabel("LabelTracks");
		panel.add(lblLabeltracks);

		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane);

		list = new JList();		
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				trackSelectionChanged();
			}
		});
		scrollPane.setViewportView(list);

		panel_1 = new JPanel();
		panel_1.setBorder(null);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

		JScrollPane scrollPane_1 = new JScrollPane();
		panel_1.add(scrollPane_1);

		table = new JTable();

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				labelSelectionChanged();
			}
		});
		table.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		table.setFillsViewportHeight(true);
		table.setModel(new DefaultTableModel(columnNames,0));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(Object.class, new LabelTableCellRenderer());
		table.setFont(f);
		table.setRowHeight(20);
		scrollPane_1.setViewportView(table);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		contentPane.add(panel);
		contentPane.add(Box.createRigidArea(new Dimension(5,0)));
		contentPane.add(panel_1);


		MessageManager.getInstance().addUpdateTracksListener(new UpdateTracksListener(){
			@Override
			public void updateTracks(UpdateTracksEvent e) {
				buildTrackList();
				trackSelectionChanged();
			}			 
		});		
		MessageManager.getInstance().addLabelSelectionListener(new LabelSelectionListener(){
			@Override
			public void selectionChanged(LabelSelectionEvent e) {
				if(!(e.getSource().getClass().toString().equals(LabelTrackTabular.class.toString()))) {
					table.clearSelection();
					
				}
			}			 
		});
		MessageManager.getInstance().addRepaintListener(new RepaintListener(){
			@Override
			public void repaintRequested(RepaintEvent e) {
				repaintChangedData();
			}					
		});
		MessageManager.getInstance().addTimeTypeChangedListener(new TimeTypeChangedListener(){

			@Override
			public void timeTypeChanged(TimeTypeEvent e) {
				repaintChangedData();
			}});
		AtlasProperties.getInstance().addJFrameBoundsWatcher("labTraTab",
				this, true, true);
	}
	private void labelSelectionChanged(){
		if(this.table.getModel().getRowCount()>0 && ((LabelTableModel)this.table.getModel()).getRow(this.table.getSelectedRow())!=null){

			double shift = (Project.getInstance().getLcoll().getWidth()/2.0)/Project.getInstance().getZoom() -
			((((LabelTableModel)this.table.getModel()).getRow(this.table.getSelectedRow()).getEnd() - 
					((LabelTableModel)this.table.getModel()).getRow(this.table.getSelectedRow()).getStart())/2.0);
			
			
			if((((LabelTableModel)this.table.getModel()).getRow(this.table.getSelectedRow()).getStart()-(shift)>=0)){
				MessageManager.getInstance().slotChanged(new SlotEvent(this,(int) (((LabelTableModel)this.table.getModel()).getRow(this.table.getSelectedRow()).getStart()-(shift))));
			}else{
				MessageManager.getInstance().slotChanged(new SlotEvent(this,0));
			}
			int i = this.table.getSelectedRow();
			LabelObject prev = null;
			LabelObject post = null;
			if(i>0){
				prev = ((LabelTableModel)this.table.getModel()).getRow(this.table.getSelectedRow()-1);						
			}			
			if(i<((LabelTableModel)this.table.getModel()).getRowCount()-1){
				post = ((LabelTableModel)this.table.getModel()).getRow(this.table.getSelectedRow()+1);
			}
			MessageManager.getInstance().selectionChanged(new LabelSelectionEvent(this,prev, ((LabelTableModel)this.table.getModel()).getRow(this.table.getSelectedRow()),post));
			
		}
	}

private void buildTrackList(){
	int index = this.list.getSelectedIndex();
	DefaultListModel listModel = new DefaultListModel();		
	Iterator<ObjectLine> iT = Project.getInstance().getLcoll().getList().iterator();
	while(iT.hasNext()){
		ObjectLine tmp = iT.next();		
		if(tmp.getType().equals("LabelTrack")){
			listModel.addElement(tmp);
		}
	}		
	list.setModel(listModel);
	list.setSelectedIndex(index);
}

private void repaintChangedData(){		
	int row = this.table.getSelectedRow();
	if(this.list.getSelectedValue()!=null) {
		LabelTrack tmp = (LabelTrack) ((ObjectLine)this.list.getSelectedValue()).getTrack();
		LabelTableModel tblMod = new LabelTableModel(columnNames);		
		Iterator<LabelObject> iL = tmp.getLabels().iterator();

		while(iL.hasNext()){						
			LabelObject lo = iL.next();	
			tblMod.addRow(lo);								
		}		
		this.table = this.autoResizeColWidth(this.table, tblMod);		
		this.table.getSelectionModel().setSelectionInterval(row, row);		
	}
}

private void trackSelectionChanged(){		

	if(this.list.getSelectedValue()!=null) {
		LabelTrack tmp = (LabelTrack) ((ObjectLine)this.list.getSelectedValue()).getTrack();
		LabelTableModel tblMod = new LabelTableModel(columnNames);		
		Iterator<LabelObject> iL = tmp.getLabels().iterator();

		while(iL.hasNext()){						
			LabelObject lo = iL.next();	
			tblMod.addRow(lo);								
		}
		//this.table.setModel(tblMod);
		this.table = this.autoResizeColWidth(this.table, tblMod);
	}
}
public JTable autoResizeColWidth(JTable table, LabelTableModel tblMod) {
	table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	table.setModel(tblMod);

	int margin = 5;

	for (int i = 0; i < table.getColumnCount(); i++) {
		int                     vColIndex = i;
		DefaultTableColumnModel colModel  = (DefaultTableColumnModel) table.getColumnModel();
		TableColumn             col       = colModel.getColumn(vColIndex);
		int                     width     = 0;

		// Get width of column header
		TableCellRenderer renderer = col.getHeaderRenderer();

		if (renderer == null) {
			renderer = table.getTableHeader().getDefaultRenderer();
		}

		Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);

		width = comp.getPreferredSize().width;

		// Get maximum width of column data
		for (int r = 0; r < table.getRowCount(); r++) {
			renderer = table.getCellRenderer(r, vColIndex);
			comp     = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false,
					r, vColIndex);
			width = Math.max(width, comp.getPreferredSize().width);
		}
		if(width>200){
			width=200;
		}

		// Add margin
		width += 2 * margin;

		// Set the width
		col.setPreferredWidth(width);
	}

	((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(
			SwingConstants.LEFT);

	// table.setAutoCreateRowSorter(true);
	table.getTableHeader().setReorderingAllowed(false);

	return table;
}



}

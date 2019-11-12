package de.atlas.gui;

import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JTree;

import de.atlas.data.LabelClasses;
import de.atlas.messagesystem.ClassChangedEvent;
import de.atlas.messagesystem.ClassChangedListener;
import de.atlas.messagesystem.MessageManager;
import de.atlas.misc.AtlasProperties;

public class LabelLegend extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTree tree;
	//private DefaultMutableTreeNode root;
	
	/**
	 * Create the frame.
	 */
	public LabelLegend() {
		super("Class Legend");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 266, 430);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		JPanel panel = new JPanel();
		contentPane.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane);
	
		
		DefaultTreeModel model = new DefaultTreeModel(LabelClasses.getInstance());
		Font f = new Font("Monospaced", Font.BOLD, 16);
		tree = new JTree();
		tree.setShowsRootHandles(true);
		tree.setFont(f);
		LabelClassEntityTreeCellRenderer renderer = new LabelClassEntityTreeCellRenderer();
		tree.setCellRenderer(renderer);
		tree.setModel(model);
		scrollPane.setViewportView(tree);
		
		
		MessageManager.getInstance().addClassChangedListener(new ClassChangedListener(){
			public void classChanged(ClassChangedEvent e) {
				DefaultTreeModel model = new DefaultTreeModel(LabelClasses.getInstance());
				tree.setModel(model);
				tree.expandRow(0);
				tree.setRootVisible(false);
				repaint();
			}

		});
		AtlasProperties.getInstance().addJFrameBoundsWatcher("labLeg",
				this, true, true);
	}
}

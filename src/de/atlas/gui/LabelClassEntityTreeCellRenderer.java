package de.atlas.gui;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.atlas.data.LabelClass;
import de.atlas.data.LabelClassEntity;

public class LabelClassEntityTreeCellRenderer extends DefaultTreeCellRenderer{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus ) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        
        if(leaf && value instanceof LabelClassEntity){
        
        	BufferedImage img = new BufferedImage( 25, 25, BufferedImage.TYPE_INT_RGB);
    		Graphics2D g2 = img.createGraphics();				
    		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    				RenderingHints.VALUE_ANTIALIAS_ON);	
    		//g2.setColor(((LabelClassEntity)value).getColor());
    		g2.setColor(this.getBackground());
    		g2.fillRect(0, 0, img.getWidth(), img.getHeight());
    		g2.setColor(((LabelClassEntity)value).getColor());
    		g2.fillOval((int)(img.getWidth()/6.0), (int)(img.getHeight()/6.0),(int)((img.getWidth()/6.0)*4), (int)((img.getHeight()/6.0))*4);
    		ImageIcon leafIcon = new ImageIcon(img);
    		if (leafIcon != null) {    		    
    			setIcon( leafIcon);
    		    //setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, ((LabelClassEntity)value).getColor()));    		
    		}
        }else if(value instanceof LabelClass){
        	setIcon( null);
        	this.setOpenIcon(null);
        	this.setClosedIcon(null);
        }
        
        
        return this;
    }
}

package de.atlas.data;

import java.awt.Color;
import java.util.Enumeration;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class LabelClassEntity implements MutableTreeNode, TreeNode{
	private String name;
	private int id;
    private Color color;
    private Color continuousColor;
	private LabelClass parent;
	public LabelClassEntity(LabelClass parent, String name, int id, Color col, Color col2){
		this.name=name;
		this.id=id;
        this.color=col;
        this.continuousColor=col2;
		this.parent = parent;
	}
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    public Color getContinuousColor() {
        return continuousColor;
    }

    public void setContinuousColor(Color color) {
        this.continuousColor = color;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	public String toString(){
		return name;
	}
	@Override
	public Enumeration<?> children() {
		return null;
	}
	@Override
	public boolean getAllowsChildren() {
		return false;
	}
	@Override
	public TreeNode getChildAt(int childIndex) {
		return null;
	}
	@Override
	public int getChildCount() {
		return 0;
	}
	@Override
	public int getIndex(TreeNode node) {
		return 0;
	}
	@Override
	public TreeNode getParent() {		
		return parent;
	}
	@Override
	public boolean isLeaf() {
		return true;
	}
	@Override
	public void insert(MutableTreeNode arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void remove(int arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void remove(MutableTreeNode arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void removeFromParent() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setParent(MutableTreeNode arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setUserObject(Object o) {
		this.name=(String)o;
		
	}
	
	
}

package de.atlas.data;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;

public class LabelClasses extends DefaultListModel implements ComboBoxModel,
		TreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static LabelClasses instance = new LabelClasses();

	private LabelClass selectedItem;

	public LabelClasses() {
	}

	public void addClass(LabelClass lc) {
		if (null == lc) {
			System.err.println("labelclass to add is null");
		} else {
			boolean exists = false;
			for (int i = 0; i < this.getSize(); i++) {
				if (((LabelClass) this.getElementAt(i)).getName()
						.equalsIgnoreCase(lc.getName())) {
					exists = true;
				}
			}
			if (!exists) {
				this.addElement(lc);
			} else {
				Object[] options = { "yes", "no" };
				int n = JOptionPane
						.showOptionDialog(
								null,
								"LabelClass already exists. Do you want to overwrite?",
								"LabelClass already exists!",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[0]);
				if (n == 0) {
					this.removeElement(this.getClassByName(lc.getName()));
					this.addElement(lc);
				}
			}
		}
	}

	public void removeClass(LabelClass lc) {
		this.removeElement(lc);
	}

	@Override
	public Object getSelectedItem() {
		return selectedItem;
	}

	@Override
	public void setSelectedItem(Object o) {
		this.selectedItem = (LabelClass) o;

	}

	public static LabelClasses getInstance() {
		return instance;
	}

	public void readClassFromXML(File filename) {
		//System.out.println("filename: " + filename);
		this.addClass(new LabelClass(filename));
	}

	public void writeXML() {
		for (int i = 0; i < this.getSize(); i++) {
			((LabelClass) this.getElementAt(i)).writeXML();
		}
	}

	public LabelClass getClassByName(String name) {
		for (int i = 0; i < this.getSize(); i++) {
			if (((LabelClass) this.getElementAt(i)).getName().equalsIgnoreCase(
					name)) {
				return (LabelClass) this.getElementAt(i);
			}
		}
		return null;
	}

	public LabelClass getClassAt(int index) {
		if (0 <= index && this.getSize() > index) {
			return (LabelClass) this.getElementAt(index);
		}
		// index out of bounds
		return null;
	}

	public void deleteAll() {
		this.clear();
	}

	public void addGenericClass() {
		LabelClass l = new LabelClass("generic");
		l.addEntity(new LabelClassEntity(l, "labeled", 0, Color.lightGray, Color.darkGray));
		this.addClass(l);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<LabelClass> children() {
		return (Enumeration<LabelClass>) this.elements();
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return (TreeNode) this.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return this.size();
	}

	@Override
	public int getIndex(TreeNode node) {
		return this.indexOf(node);
	}

	@Override
	public TreeNode getParent() {
		return this;
	}

	@Override
	public boolean isLeaf() {
		if (this.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	public String toString() {
		return "LabelClasses";
	}

	public ArrayList<LabelClass> getClasses() {
		ArrayList<LabelClass> arlist = new ArrayList<LabelClass>();
		for (int i = 0; i < this.getSize(); i++) {
			arlist.add(this.getClassAt(i));
		}
		return arlist;
	}

}

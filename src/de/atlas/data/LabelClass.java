package de.atlas.data;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

public class LabelClass implements ComboBoxModel, MutableTreeNode, TreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name_;
	private LabelClassEntity selectedItem_;
	private ArrayList<LabelClassEntity> entities_ = new ArrayList<LabelClassEntity>();

	public LabelClass(String name) {
		this.name_ = name;
	}

	public LabelClass(File file) {
		this.readXML(file.getPath());
	}

	public String getName() {
		return name_;
	}

	public void setName(String name) {
		this.name_ = name;
	}

	public void addEntity(LabelClassEntity cl) {
		this.entities_.add(cl);
	}

	public void removeEntity(LabelClassEntity cl) {
		this.entities_.remove(cl);
	}

	public ArrayList<LabelClassEntity> getLabelClassEntities() {
		return entities_;
	}

	public String toString() {
		return name_;
	}

	public Object getSelectedItem() {
		return this.selectedItem_;
	}

	public void setSelectedItem(Object o) {
		this.selectedItem_ = (LabelClassEntity) o;
	}

	public void readXML(String file) {

		if (file.endsWith(".xml")) {
			SAXBuilder sxbuild = new SAXBuilder();
			InputSource is = null;
			try {
				is = new InputSource(new FileInputStream(file));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			Document doc;
			try {

				doc = sxbuild.build(is);
				Element root = doc.getRootElement();

				this.name_ = root.getAttribute("classname").getValue();
				// READ ENTITIES
				@SuppressWarnings("unchecked")
				List<Element> entityList = ((List<Element>) root
						.getChildren("entity"));
				for (Element entity : entityList) {
					try {
						String toAddName = entity.getChild("name").getValue();
						Color toAddColor = new Color(Integer.parseInt(entity.getChild("color").getValue()));
						int toAddId = Integer.parseInt(entity.getChild("id").getValue());
                        Color toAddColor2 = entity.getChild("continuousColor")==null?new Color(Color.darkGray.getRGB()):new Color(Integer.parseInt(entity.getChild("continuousColor").getValue()));
						LabelClassEntity entityToAdd = new LabelClassEntity(
								this, toAddName, toAddId, toAddColor, toAddColor2);
						this.addEntity(entityToAdd);
					} catch (Exception e) {
					System.err.println("error while trying to add entity");
					}
				}

			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void writeXML() {
		Element root = new Element("LabelClass");
		root.setAttribute("classname", this.getName());
		for (int i = 0; i < this.entities_.size(); i++) {
			LabelClassEntity tmp = (LabelClassEntity) this.getElementAt(i);

			Element entity = new Element("entity");

			Element name = new Element("name");
			name.setText(String.valueOf(tmp.getName()));
			entity.addContent(name);
            Element color = new Element("color");
            color.setText(String.valueOf(tmp.getColor().getRGB()));
            entity.addContent(color);
            Element color2 = new Element("continuousColor");
            color2.setText(String.valueOf(tmp.getContinuousColor().getRGB()));
            entity.addContent(color2);
			Element id = new Element("id");
			id.setText(String.valueOf(tmp.getId()));
			entity.addContent(id);
			root.addContent(entity);
		}
		Document doc = new Document(root);

		try {
			FileOutputStream out = new FileOutputStream(Project.getInstance()
					.getProjectPath() + "classes/" + name_ + ".xml");
			XMLOutputter serializer = new XMLOutputter();
			serializer.setFormat(Format.getPrettyFormat());
			serializer.output(doc, out);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public LabelClassEntity getEntityByName(String name) {
		for (int i = 0; i < this.getSize(); i++) {
			if (((LabelClassEntity) this.getElementAt(i)).getName()
					.equalsIgnoreCase(name)) {
				return (LabelClassEntity) this.getElementAt(i);
			}
		}
		return null;
	}

	public LabelClassEntity getEntityAt(int index) {
		return (LabelClassEntity) this.getElementAt(index);
	}

	@Override
	public Enumeration<LabelClassEntity> children() {
		return (Enumeration<LabelClassEntity>) Collections
				.enumeration(this.entities_);
	}

	@Override
	public boolean getAllowsChildren() {
		return true;
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return (TreeNode) this.entities_.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return this.entities_.size();
	}

	@Override
	public int getIndex(TreeNode node) {
		return this.entities_.indexOf(node);
	}

	@Override
	public TreeNode getParent() {
		return null;
	}

	@Override
	public boolean isLeaf() {
		if (this.entities_.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getElementAt(int index) {

		return this.entities_.get(index);
	}

	@Override
	public int getSize() {
		return this.entities_.size();
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void insert(MutableTreeNode child, int index) {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(int index) {
		entities_.remove(index);

	}

	@Override
	public void remove(MutableTreeNode node) {
		entities_.remove(node);

	}

	@Override
	public void removeFromParent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setParent(MutableTreeNode newParent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUserObject(Object o) {
		this.name_ = (String) o;

	}

	public LabelClassEntity getEntityByID(int ID) {
		for (int i = 0; i < this.getSize(); i++) {
			if (((LabelClassEntity) this.getElementAt(i)).getId() == ID) {
				return (LabelClassEntity) this.getElementAt(i);
			}
		}
		return null;
	}

	public int getEntityCount() {
		// TODO Auto-generated method stub
		return entities_.size();
	}
}

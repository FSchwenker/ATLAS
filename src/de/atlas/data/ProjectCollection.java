package de.atlas.data;

import de.atlas.misc.HelperFunctions;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.InputSource;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by smeudt on 13.02.2015.
 */
public class ProjectCollection {
    private File collectionFile;
    private ArrayList<ProjectCollectionEntity> projects = new ArrayList<>();

    public ProjectCollection(File collectionFile){
            setCollectionFile(collectionFile);
    }

    public ArrayList<ProjectCollectionEntity> getProjects(){
        return projects;
    }
    public void addProject(ProjectCollectionEntity p){
        projects.add(p);
        Collections.sort(projects);
        writeXML();
    }
    public void delProject(ProjectCollectionEntity p){
        projects.remove(p);
        writeXML();
    }
    public void delProject(int i){
        if(i>0&&i<projects.size())projects.remove(i);
        writeXML();
    }

    public File getCollectionFile() {
        return collectionFile;
    }

    public void setCollectionFile(File collectionFile) {
        if (HelperFunctions.fileExists(collectionFile.getAbsolutePath())){
            this.collectionFile = collectionFile;
            projects.clear();
            loadXML();
        }else{
            this.collectionFile = collectionFile;
            writeXML();
        }
    }
    public void loadXML(){
        this.projects.clear();
        // READ PROJECTCOLLECTION.XML
        if (this.collectionFile.getPath().endsWith(".xml")) {
            SAXBuilder sxbuild = new SAXBuilder();
            InputSource is = null;
            try {
                is = new InputSource(new FileInputStream(collectionFile));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            Document doc;
            try {
                doc = sxbuild.build(is);
                Element root = doc.getRootElement();
                List<Element> projectList = ((List<Element>) root.getChildren("Project"));
                for (Element p : projectList) {
                    projects.add(new ProjectCollectionEntity(p.getValue(), p.getAttribute("name").getValue()));
                }
                Collections.sort(projects);
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeXML(){
        Element root = new Element("AnnotationProjectCollection");

        for (int i = 0; i < this.projects.size(); i++) {
            ProjectCollectionEntity tmp = projects.get(i);
            Element e = new Element("Project");
            e.setAttribute("name", tmp.getName());
            e.setText(tmp.getPath());
            root.addContent(e);
        }

        Document doc = new Document(root);
        try {
            FileOutputStream out = new FileOutputStream(this.collectionFile);
            XMLOutputter serializer = new XMLOutputter();
            serializer.setFormat(Format.getPrettyFormat());
            serializer.output(doc, out);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public ProjectCollectionEntity getProject(int index) {
        return this.projects.get(index);
    }
}

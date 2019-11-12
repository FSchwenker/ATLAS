package de.atlas.gui;

import de.atlas.data.LabelClasses;
import de.atlas.data.Project;
import de.atlas.data.ProjectCollection;
import de.atlas.data.ProjectCollectionEntity;
import de.atlas.messagesystem.*;
import de.atlas.misc.AtlasProperties;
import eu.c_bauer.userinputverifier.UserInputVerifier;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

public class ProjectCollectionEditor extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final Atlas atlas;
	private JPanel contentPane;
	private JList projectList;
	private int lastSelectedProject = -1;
	private JButton buttonAdd;
	private JButton buttonDel;
	private JButton buttonNew;
	private JButton buttonLoad;
	private boolean lastSelectionFrozen = false;

	private ProjectCollection projectCollection;

    /**
	 * Create the frame.
	 */
	public ProjectCollectionEditor(Atlas atlas) {
		this.atlas = atlas;
		setResizable(false);
		setTitle("ProjectCollectionEditor");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(0, 0, 350, 695);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(0, 55, 350, 600);
		contentPane.add(panel);
		panel.setLayout(null);

		JLabel lblProjects = new JLabel("Projects:");
		lblProjects.setBounds(12, 12, 325, 15);
		panel.add(lblProjects);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 39, 325, 550);
		panel.add(scrollPane);

		projectList = new JList();
		projectList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				selectionChanged();
			}
		});
		projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPane.setViewportView(projectList);

		this.buildProjectList();

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(0, 10, 350, 55);
		contentPane.add(panel_1);
		panel_1.setLayout(null);

		buttonAdd = new JButton("Add");
		buttonAdd.setBounds(12, 12, 70, 30);
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addProject();
			}
		});
		panel_1.add(buttonAdd);
		buttonAdd.setEnabled(false);

		buttonDel = new JButton("Remove");
		buttonDel.setBounds(94, 12, 70, 30);
		buttonDel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				delProject();
			}
		});
		panel_1.add(buttonDel);
		buttonDel.setEnabled(false);

		buttonNew = new JButton("New");
		buttonNew.setBounds(176, 12, 70, 30);
		buttonNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				newProjectCollection();
			}
		});
		panel_1.add(buttonNew);

		buttonLoad = new JButton("Load");
		buttonLoad.setBounds(258, 12, 70, 30);
		buttonLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadProjectCollection();
			}
		});
		panel_1.add(buttonLoad);

		MessageManager.getInstance().addProjectLoadedListener(
				new ProjectLoadedListener() {
					@Override
					public void projectLoaded(ProjectLoadedEvent e) {

					}
				});
		AtlasProperties.getInstance().addJFrameBoundsWatcher("pCEd", this,
				true, false);

		// init gui with no selected items
		selectionChanged();
		final UserInputVerifier uiv = new UserInputVerifier();
	}

	private void loadProjectCollection() {
		JFileChooser fc = new JFileChooser();
		FileFilter ff = new FileNameExtensionFilter("ProjectCollection", "xml");

		if (ff != null) {
			fc.setFileFilter(ff);
		}

		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			this.projectCollection = new ProjectCollection(fc.getSelectedFile());
			//this.projectCollection.loadXML();
			buildProjectList();
			buttonDel.setEnabled(true);
			buttonAdd.setEnabled(true);
		}
	}

	private void newProjectCollection() {
		JFileChooser fc = new JFileChooser();
		FileFilter ff = new FileNameExtensionFilter("ProjectCollection", "xml");

		if (ff != null) {
			fc.setFileFilter(ff);
		}

		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			this.projectCollection = new ProjectCollection(fc.getSelectedFile());
			buildProjectList();
			buttonDel.setEnabled(true);
			buttonAdd.setEnabled(true);
		}
	}

	private void delProject() {
		projectCollection.delProject(projectList.getSelectedIndex());
		buildProjectList();
	}

	private void addProject() {
		JFileChooser fc = new JFileChooser();
		FileFilter ff = new FileNameExtensionFilter("Project", "xml");

		if (ff != null) {
			fc.setFileFilter(ff);
		}

		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			projectCollection.addProject(new ProjectCollectionEntity(fc.getSelectedFile().getAbsolutePath(), fc.getSelectedFile().getName()));
			buildProjectList();
		}

	}

	private void buildProjectList() {
		if(projectCollection==null)return;
		DefaultListModel listModel = new DefaultListModel();
		Iterator<ProjectCollectionEntity> iPCE = projectCollection.getProjects().iterator();
		while (iPCE.hasNext()) {
			listModel.addElement(iPCE.next());
		}
		projectList.setModel(listModel);
	}

	private void selectionChanged() {
		if (lastSelectedProject!=this.projectList.getSelectedIndex()&&this.projectList.getSelectedIndex()>=0) {
			atlas.stopPlayback();

			if (Project.getInstance().getProjectPath().length() > 0) {
				Object[] options = { "yes", "no", "cancel" };
				int n = JOptionPane.showOptionDialog(this,
						"Do you want to save the current project?",
						"Save Project?", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n == 2) {// cancel
					this.projectList.setSelectedIndex(lastSelectedProject);
					return;
				} else if (n == 0) {// yes
					Project.getInstance().saveProject();
				}
			}

			Project.getInstance().getLcoll().reset();
			Project.getInstance().getMcoll().reset();
			LabelClasses.getInstance().deleteAll();
			MessageManager.getInstance().requestTrackUpdate(new UpdateTracksEvent(this));
			MessageManager.getInstance().requestRepaint(new RepaintEvent(this));
			Project.getInstance().loadProject(projectCollection.getProject(this.projectList.getSelectedIndex()).getPath(),true);

			//System.out.println(projectCollection.getProject(this.projectList.getSelectedIndex()).getName());
		}
		lastSelectedProject = this.projectList.getSelectedIndex();
	}

}
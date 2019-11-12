package de.atlas.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.atlas.data.LabelClass;
import de.atlas.data.LabelClassEntity;
import de.atlas.data.LabelClasses;
import de.atlas.messagesystem.ClassChangedEvent;
import de.atlas.messagesystem.ClassChangedListener;
import de.atlas.messagesystem.MessageManager;
import de.atlas.misc.AtlasProperties;
import eu.c_bauer.userinputverifier.UserInputVerifier;

import javax.swing.JSeparator;

public class ClassEditor extends JFrame {

	/**
	 * getTreePath and treePathFind are from
	 * http://www.exampledepot.com/egs/javax.swing.tree/FindNode.html
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JFormattedTextField tfId_;
	private Color color = Color.lightGray;
    private JButton btnClassEntityColor_;
    private JButton btnClassEntityCColor_;
	private JButton btnAddClass;
	private JButton btnDelete;
	private JButton btnAddEntity;
	private JTextField tfName_;
	private JTree tree;
	private LabelClassEntity selectedClassEntity_ = null;
	private LabelClass selectedClass_ = null;
	private UserInputVerifier uivNameAndId_ = null;

	private LabelClass beforeUpdateSelectedClass_ = null;
	private LabelClassEntity beforeUpdateSelectedEntity_ = null;
	private JTextField beforeUpdateTextField_ = null;
	private int beforeUpdateCaretPos_ = -1;
	private ArrayList<String> beforeUpdateClasses_ = new ArrayList<String>();
	private ArrayList<String> beforeUpdateEntities_ = new ArrayList<String>();
    private Color beforeUpdateColor = null;
    private Color beforeUpdateCColor = null;

	public ClassEditor() {
		setTitle("ClassEditor");
		setResizable(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 449, 320);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(12, 12, 254, 255);
		contentPane.add(panel);
		panel.setLayout(null);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(0, 0, 254, 255);

		panel.add(scrollPane_1);

		// Font f = new Font("Monospaced", Font.BOLD, 16);
		tree = new JTree();

		tree.setEditable(false);
		tree.setShowsRootHandles(true);
		tree.setFont(new Font("Monospaced", Font.BOLD, 12));
		LabelClassEntityTreeCellRenderer renderer = new LabelClassEntityTreeCellRenderer();
		tree.setCellRenderer(renderer);
		scrollPane_1.setViewportView(tree);
		scrollPane_1.setBorder(new TitledBorder("Label Classes"));

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
				if (tree.getLastSelectedPathComponent() instanceof LabelClass) {

					// clear user input verifiers
					uivNameAndId_.removeAll();
					// uivId_.removeAll();
					selectedClassEntity_ = null;
					selectedClass_ = ((LabelClass) tree
							.getLastSelectedPathComponent());

					tfName_.setText(selectedClass_.getName());
					tfName_.setEnabled(false);
					tfId_.setText("");
					tfId_.setEnabled(false);
                    btnClassEntityColor_.setBackground(ClassEditor.this.getBackground());
                    btnClassEntityColor_.setEnabled(false);
                    btnClassEntityCColor_.setBackground(ClassEditor.this.getBackground());
                    btnClassEntityCColor_.setEnabled(false);

					btnDelete.setEnabled(true);
					btnAddEntity.setEnabled(true);

					uivNameAndId_.setTextFieldRestriction_NonEmpty(tfName_,
							"tfName_", true, false);
					uivNameAndId_.setTextFieldRestriction_WindowsFilename(
							tfName_, "tfName_", false, true, false, false);
				} else if (tree.getLastSelectedPathComponent() instanceof LabelClassEntity) {
					// clear user input verifiers
					uivNameAndId_.removeAll();
					// uivId_.removeAll();
					selectedClassEntity_ = ((LabelClassEntity) tree
							.getLastSelectedPathComponent());
					selectedClass_ = (LabelClass) tree.getSelectionPath()
							.getPathComponent(1);
					btnDelete.setEnabled(true);
					tfId_.setEnabled(true);
                    btnClassEntityColor_.setEnabled(true);
                    btnClassEntityCColor_.setEnabled(true);
					tfName_.setEnabled(true);
					tfName_.setText(selectedClassEntity_.getName());
					tfId_.setText(String.valueOf(selectedClassEntity_.getId()));
                    btnClassEntityColor_.setBackground(selectedClassEntity_.getColor());
                    btnClassEntityCColor_.setBackground(selectedClassEntity_.getContinuousColor());
					btnDelete.setEnabled(true);
					btnAddEntity.setEnabled(true);

					// set input verifiers
					uivNameAndId_.setTextFieldRestriction_NonEmpty(tfName_,
							"tfName_", true, false);
					uivNameAndId_.setTextFieldRestriction_WindowsFilename(tfName_, "tfName_", false, true, false, false);

					ArrayList<LabelClassEntity> entities = selectedClass_.getLabelClassEntities();
					if (1 < entities.size()) {
						LinkedList<String> forbiddenList = new LinkedList<String>();
						boolean found = false;
						for (int i = 0; i < entities.size(); i++) {
							String entityName = entities.get(i).getName();
							if (!entityName.equals(selectedClassEntity_.getName())) {
								forbiddenList.add(entityName);
							}
						}
						String[] forbiddenNames = new String[forbiddenList
								.size()];
						forbiddenList.toArray(forbiddenNames);
						uivNameAndId_.setTextFieldRestriction_ForbiddenStrings(tfName_, "tfName_", forbiddenNames, true, false, false);
					}
					uivNameAndId_.setTextFieldRestriction_NonEmpty(tfId_, "tfId_", true, false);
					uivNameAndId_.setTextFieldRestriction_Int(tfId_, "tfId_", true, false, false);
				} else {
					// clear user input verifiers
					uivNameAndId_.removeAll();
					uivNameAndId_.removeAll();
					btnDelete.setEnabled(false);
					btnAddEntity.setEnabled(false);
					tfName_.setText("");
					tfName_.setEnabled(false);
					tfId_.setText("");
					tfId_.setEnabled(false);
                    btnClassEntityColor_.setBackground(ClassEditor.this.getBackground());
                    btnClassEntityColor_.setEnabled(false);
                    btnClassEntityCColor_.setBackground(ClassEditor.this.getBackground());
                    btnClassEntityCColor_.setEnabled(false);
				}
			}

		});

		JPanel panelRight = new JPanel();
		panelRight.setBounds(276, 11, 157, 270);
		contentPane.add(panelRight);

		btnAddClass = new JButton("add class");
		btnAddClass.setBounds(0, 146, 157, 25);
		btnAddClass.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addClass();
			}
		});
		panelRight.setLayout(null);
		panelRight.add(btnAddClass);

		btnAddEntity = new JButton("add entity");
		btnAddEntity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addEntity();
			}
		});
		btnAddEntity.setBounds(0, 183, 157, 25);
		panelRight.add(btnAddEntity);

		JLabel classEntityIdLabel = new JLabel("id");
		classEntityIdLabel.setBounds(0, 43, 39, 15);
		panelRight.add(classEntityIdLabel);

		tfId_ = new JFormattedTextField();
		tfId_.setBounds(43, 40, 114, 19);
		panelRight.add(tfId_);
		tfId_.setColumns(10);

		JLabel classEntityNameLabel = new JLabel("name");
		classEntityNameLabel.setBounds(0, 14, 39, 15);
		panelRight.add(classEntityNameLabel);

		tfName_ = new JTextField();
		tfName_.setBounds(43, 11, 114, 19);
		panelRight.add(tfName_);
		tfName_.setColumns(10);

		JLabel classEntityColorLabel = new JLabel("color");
		classEntityColorLabel.setBounds(0, 77, 39, 15);
		panelRight.add(classEntityColorLabel);

		btnClassEntityColor_ = new JButton("  ");
		btnClassEntityColor_.setBounds(43, 70, 114, 25);
		panelRight.add(btnClassEntityColor_);

        JLabel classEntityCColorLabel = new JLabel("c.col.");
        classEntityCColorLabel.setBounds(0, 108, 39, 15);
        panelRight.add(classEntityCColorLabel);

        btnClassEntityCColor_ = new JButton("  ");
        btnClassEntityCColor_.setBounds(43, 100, 114, 25);
        panelRight.add(btnClassEntityCColor_);

		JSeparator separator = new JSeparator();
		separator.setBounds(0, 133, 157, 2);
		panelRight.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(0, 219, 157, 2);
		panelRight.add(separator_1);
		
        btnDelete = new JButton("delete");
        btnDelete.setBounds(0, 230, 157, 25);
        panelRight.add(btnDelete);
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                deleteClassOrEntity();
            }
        });
        // will be enabled or disabled with (de)selection of labelclasses and
        // entities
        btnDelete.setEnabled(false);
        btnClassEntityColor_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                color = JColorChooser.showDialog(ClassEditor.this, "Choose a color", Color.lightGray);

                if (color != null) {
                    btnClassEntityColor_.setBackground(color);
                    saveEntity(null);
                }

            }
        });
        btnClassEntityCColor_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                color = JColorChooser.showDialog(ClassEditor.this, "Choose a color", Color.darkGray);

                if (color != null) {
                    btnClassEntityCColor_.setBackground(color);
                    saveEntity(null);
                }

            }
        });
		MessageManager.getInstance().addClassChangedListener(
				new ClassChangedListener() {
					public void classChanged(ClassChangedEvent e) {
						DefaultTreeModel model = new DefaultTreeModel(
								LabelClasses.getInstance());
						ArrayList<LabelClass> classes = LabelClasses
								.getInstance().getClasses();

						// class added
						if (beforeUpdateClasses_.size() < classes.size()) {
							for (int i = 0; i < classes.size(); i++) {
								if (!beforeUpdateClasses_.contains(classes.get(i).getName())) {
									beforeUpdateSelectedClass_ = classes.get(i);
									break;
								}
							}

							try {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										setTreeSelection(beforeUpdateSelectedClass_, null, false, false);
									}
								});
							} catch (Exception ex) {
								System.err.println("class added - exception trying to select tree path");
							}

						}

						// class deleted - select next in line or - if last -
						// one above
						else if (beforeUpdateClasses_.size() > classes.size()) {
							ArrayList<String> curNames = new ArrayList<String>();
							for (int i = 0; i < classes.size(); i++) {
								curNames.add(classes.get(i).getName());
							}
							int indexToSelect;
							for (indexToSelect = 0; indexToSelect < classes.size(); indexToSelect++) {
								if (!beforeUpdateClasses_.get(indexToSelect).equals(classes.get(indexToSelect).getName())) {
                                    break;
                                }
							}

							try {
								beforeUpdateSelectedClass_ = classes.get(indexToSelect);
							} catch (Exception e1) {
								try {
									beforeUpdateSelectedClass_ = classes.get(indexToSelect - 1);
								} catch (Exception e2) {
									beforeUpdateSelectedClass_ = null;
								}
							}
							if (null != beforeUpdateSelectedClass_) {
								try {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											setTreeSelection(beforeUpdateSelectedClass_, null, false, false);
										}
									});
								} catch (Exception ex) {
									System.err.println("class deleted - exception trying to select tree path");
								}
							}
						}

						// entity added - select new entity
						else if (null != beforeUpdateSelectedClass_ && beforeUpdateSelectedClass_.getSize() > beforeUpdateEntities_.size()) {
							ArrayList<LabelClassEntity> entities = beforeUpdateSelectedClass_.getLabelClassEntities();
							tfName_.setEnabled(false);
							tfId_.setEnabled(false);
							for (int i = 0; i < entities.size(); i++) {
								if (!beforeUpdateEntities_.contains(entities.get(i).getName())) {
									beforeUpdateSelectedEntity_ = entities.get(i);
                                    break;
								}
							}
							try {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										setTreeSelection(beforeUpdateSelectedClass_, beforeUpdateSelectedEntity_, true, true);
									}
								});
							} catch (Exception ex) {
								System.err.println("entity added - exception trying to select tree path");
							}
						}

						// entity deleted
						else if (null != beforeUpdateSelectedClass_ && beforeUpdateSelectedClass_.getSize() < beforeUpdateEntities_.size()) {
							tfName_.setEnabled(false);
							tfId_.setEnabled(false);

							ArrayList<LabelClassEntity> entities = beforeUpdateSelectedClass_.getLabelClassEntities();
							ArrayList<String> curNames = new ArrayList<String>();
							for (int i = 0; i < entities.size(); i++) {
								curNames.add(entities.get(i).getName());
							}
							int indexToSelect;
							for (indexToSelect = 0; indexToSelect < entities.size(); indexToSelect++) {
								if (!beforeUpdateClasses_.get(indexToSelect).equals(entities.get(indexToSelect).getName())) {
                                    break;
                                }
							}

							try {
								beforeUpdateSelectedEntity_ = entities.get(indexToSelect);
							} catch (Exception e1) {
								try {
									beforeUpdateSelectedEntity_ = entities.get(indexToSelect - 1);
								} catch (Exception e2) {
									beforeUpdateSelectedEntity_ = null;
								}
							}

							try {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										setTreeSelection(beforeUpdateSelectedClass_, beforeUpdateSelectedEntity_, true, true);
									}
								});
							} catch (Exception ex) {
								System.err.println("entity name changed - exception trying to select tree path");
							}
						}

						// entity name or id changed
						else if (null != beforeUpdateSelectedClass_ // unnecessary
								&& null != beforeUpdateSelectedEntity_ // unnecessary
								&& (tfName_ == beforeUpdateTextField_ || tfId_ == beforeUpdateTextField_)) {

							// disable textfields
							tfName_.setEnabled(false);
							tfId_.setEnabled(false);

							try {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										setTreeSelection(beforeUpdateSelectedClass_, beforeUpdateSelectedEntity_, true, true);
									}
								});
							} catch (Exception ex) {
								System.err
										.println("entity name changed - exception trying to select tree path");
							}
						}

                        // entity color changed
                        else if (null != beforeUpdateSelectedClass_ // unnecessary
                                && null != beforeUpdateSelectedEntity_ // unnecessary
                                && null == beforeUpdateTextField_
                                && btnClassEntityColor_.getBackground() !=beforeUpdateColor) {

                            // disable textfields
                            tfName_.setEnabled(false);
                            tfId_.setEnabled(false);

                            try {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        setTreeSelection(beforeUpdateSelectedClass_, beforeUpdateSelectedEntity_, true, true);
                                    }
                                });
                            } catch (Exception ex) {
                                System.err
                                        .println("entity color changed - exception trying to select tree path");
                            }
                        }
                        // entity color2 changed
                        else if (null != beforeUpdateSelectedClass_ // unnecessary
                                && null != beforeUpdateSelectedEntity_ // unnecessary
                                && null == beforeUpdateTextField_
                                && btnClassEntityCColor_.getBackground() !=beforeUpdateCColor) {

                            // disable textfields
                            tfName_.setEnabled(false);
                            tfId_.setEnabled(false);

                            try {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        setTreeSelection(beforeUpdateSelectedClass_, beforeUpdateSelectedEntity_, true, true);
                                    }
                                });
                            } catch (Exception ex) {
                                System.err.println("entity continuousColor changed - exception trying to select tree path");
                            }
                        }

						tree.setModel(model);
						tree.expandRow(-1);
						tree.setRootVisible(false);
						repaint();
					}

				});
		DefaultTreeModel model = new DefaultTreeModel(LabelClasses.getInstance());
		tree.setModel(model);
		AtlasProperties.getInstance().addJFrameBoundsWatcher("claEd", this, true, false);

		uivNameAndId_ = new UserInputVerifier();
		PropertyChangeListener uivChangeListenerName = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				try {
					Runnable r = new Runnable() {
						@Override
						public void run() {
							if (tfName_.isEnabled() && tfId_.isEnabled()) {
								if (null != selectedClassEntity_) {
									if (!selectedClassEntity_.getName().equals(
											tfName_.getText())) {
										saveEntity(tfName_);
									}
								} else if (null != selectedClass_) {
									if (!selectedClass_.getName().equals(
											tfName_.getText())) {
										if (uivNameAndId_.isAllInputsOk()) {
											// saveClass
											// NAME CHANGE NOT SUPPRTED YET -
											// maybe later
										}
									}
								}
							}
						}
					};
					SwingUtilities.invokeLater(r);
				} catch (Exception ex) {
				}
			}
		};
		uivNameAndId_.addPropertyChangeListener(uivChangeListenerName);

		PropertyChangeListener uivChangeListenerId = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				try {
					Runnable r = new Runnable() {
						@Override
						public void run() {
							if (tfId_.isEnabled()) {
								if (null != selectedClassEntity_) {
									try {
										if (selectedClassEntity_.getId() != Integer
												.parseInt(tfId_.getText())) {
											saveEntity(tfId_);
										}
									} catch (NumberFormatException nfe) {
										System.err
												.println("id not parsable as integer");
									}
								}
							}
						}
					};
					SwingUtilities.invokeLater(r);
				} catch (Exception ex) {
				}
			}
		};
		uivNameAndId_.addPropertyChangeListener(uivChangeListenerId);
	}

	private void deleteClassOrEntity() {
		if (selectedClassEntity_ != null) {
			rememberStatesBeforeUpdate(null);
			selectedClass_.removeEntity(selectedClassEntity_);
			MessageManager.getInstance().requestClassChanged(
					new ClassChangedEvent(this));
		} else if (selectedClass_ != null) {
			rememberStatesBeforeUpdate(null);
			LabelClasses.getInstance().removeClass(selectedClass_);
			MessageManager.getInstance().requestClassChanged(
					new ClassChangedEvent(this));
		}
	}

	private void addClass() {
		rememberStatesBeforeUpdate(null);
		// get existing class names to disallow
		ArrayList<LabelClass> classes = LabelClasses.getInstance().getClasses();
		String[] forbiddenNames = new String[classes.size()];
		for (int i = 0; i < classes.size(); i++) {
			forbiddenNames[i] = classes.get(i).getName();
		}
		ClassEditor_newClassDialog.showDialog(this, "Add LabelClass",
				forbiddenNames);
	}

	private void addEntity() {
		rememberStatesBeforeUpdate(null);
		// get existing entity names to disallow
		ArrayList<LabelClassEntity> entities = beforeUpdateSelectedClass_
				.getLabelClassEntities();
		String[] forbiddenNames = new String[entities.size()];
		for (int i = 0; i < entities.size(); i++) {
			forbiddenNames[i] = entities.get(i).getName();
		}
		if (null != selectedClass_) {
			ClassEditor_newEntityDialog.showDialog(this, selectedClass_, "Add Entity to " + selectedClass_.getName(), forbiddenNames);
		}
	}

	/**
	 * Saves the currently selected entity if all inputs (id and name) are ok by
	 * the UserInputVerifier.
	 * 
	 * @param triggeringTextField
	 *            textfield triggering the update, name or id textfield or null
	 *            (color button)
	 */
	private void saveEntity(JTextField triggeringTextField) {
		if (null != selectedClassEntity_ && uivNameAndId_.isAllInputsOk()) {
			rememberStatesBeforeUpdate(triggeringTextField);
            selectedClassEntity_.setColor(btnClassEntityColor_.getBackground());
            selectedClassEntity_.setContinuousColor(btnClassEntityCColor_.getBackground());
			if (tfId_.getText().length() > 0) {
				selectedClassEntity_.setId(Integer.parseInt(tfId_.getText()));
			} else {
				selectedClassEntity_.setId(0);
			}
			selectedClassEntity_.setName(tfName_.getText());
			MessageManager.getInstance().requestClassChanged(
					new ClassChangedEvent(this));
		}
	}

	/**
	 * Remember relevant values to restore view and positioning of gui for user
	 * after new model was applied.
	 * 
	 * @param updateTextField
	 *            textfield that triggered the update - set to null if update
	 *            was triggered by something else
	 */
	private void rememberStatesBeforeUpdate(JTextField updateTextField) {
		beforeUpdateSelectedClass_ = selectedClass_;
		beforeUpdateSelectedEntity_ = selectedClassEntity_;

		ArrayList<LabelClass> classes = LabelClasses.getInstance().getClasses();
		beforeUpdateClasses_ = new ArrayList<String>();
		for (int i = 0; i < classes.size(); i++) {
			beforeUpdateClasses_.add(classes.get(i).getName());
		}

		ArrayList<LabelClassEntity> entities = selectedClass_
				.getLabelClassEntities();
		beforeUpdateEntities_ = new ArrayList<String>();
		for (int i = 0; i < entities.size(); i++) {
			beforeUpdateEntities_.add(entities.get(i).getName());
		}

		beforeUpdateTextField_ = updateTextField;
		if (null == updateTextField) {
			beforeUpdateCaretPos_ = -1;
		} else {
			beforeUpdateCaretPos_ = updateTextField.getCaretPosition();
		}
		if (null != beforeUpdateSelectedEntity_) {
			beforeUpdateColor = beforeUpdateSelectedEntity_.getColor();
		}
	}

	/**
	 * Selects the tree's specified branch.
	 * 
	 * @param labelClass
	 *            LabelClass to select
	 * @param entity
	 *            entity to select
	 * @param enableTfName
	 *            if set to true, textfield for name will be set to enabled
	 *            after updates, else nothing is done
	 * @param enableTfId
	 *            if set to true, textfield for id will be set to enabled after
	 *            updates, else nothing is done
	 */
	private void setTreeSelection(LabelClass labelClass,
			LabelClassEntity entity, boolean enableTfName, boolean enableTfId) {
		TreePath path = null;
		if (null != labelClass && null != entity) {
			Object[] nodes = new Object[3];
			nodes[0] = (TreeNode) tree.getModel().getRoot();
			nodes[1] = (Object) labelClass;
			nodes[2] = (Object) entity;
			path = getTreePath(nodes);
		}

		else if (null != labelClass) {
			Object[] nodes = new Object[2];
			nodes[0] = (TreeNode) tree.getModel().getRoot();
			nodes[1] = (Object) labelClass;
			path = getTreePath(nodes);

		}
		restoreClassTreeSelection(path, enableTfName, enableTfId);
	}

	/**
	 * Restores the old selection in the class tree, the textfields and caret.
	 * 
	 * @param path
	 *            TreePath to set
	 * @param enableTfName
	 *            if set to true, textfield for name will be set to enabled
	 *            after updates, else nothing is done
	 * @param enableTfId
	 *            if set to true, textfield for id will be set to enabled after
	 *            updates, else nothing is done
	 */
	private void restoreClassTreeSelection(TreePath path,
			final boolean enableTfName, final boolean enableTfId) {
		if (null == path) {
			if (enableTfName) {
				tfName_.setEnabled(true);
			}
			if (enableTfId) {
				tfId_.setEnabled(true);
			}
			return;
		}
		final JTextField tfToSelect = beforeUpdateTextField_;
		final int caretPos = beforeUpdateCaretPos_;
		tree.setSelectionPath(path);

		try {
			// SwingUtilities just to be sure that settings still
			// to apply don't change it again - should work directly
			// without runnable
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (null != tfToSelect) {
						try {
							tfToSelect.setCaretPosition(caretPos);
						} catch (Exception e) {
							try {
								tfToSelect.setCaretPosition(caretPos - 1);
							} catch (Exception e2) {
							}
						}
						tfToSelect.requestFocusInWindow();
						if (enableTfName) {
							tfName_.setEnabled(true);
						}
						if (enableTfId) {
							tfId_.setEnabled(true);
						}
					}
				}
			});
		} catch (Exception ex) {
			System.err.println("error setting caret position in textfield");
		}

	}

	/**
	 * Finds the path in tree as specified by the node array. The node array is
	 * 
	 * a sequence of nodes where nodes[0] is the root and nodes[i] is a child of
	 * nodes[i-1]. Comparison is done using Object.equals(). Returns null if not
	 * found.
	 * 
	 * @param tree
	 *            Jtree used
	 * @param nodes
	 *            array of nodes as described above
	 */
	private TreePath getTreePath(Object[] nodes) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();
		return treePathFind(tree, new TreePath(root), nodes, 0, false);
	}

	private TreePath treePathFind(JTree tree, TreePath parent, Object[] nodes,
			int depth, boolean byName) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		Object o = node;

		// If by name, convert node to a string
		if (byName) {
			o = o.toString();
		}

		// If equal, go down the branch
		if (o.equals(nodes[depth])) {
			// If at end, return match
			if (depth == nodes.length - 1) {
				return parent;
			}

			// Traverse children
			if (node.getChildCount() >= 0) {
				for (@SuppressWarnings("unchecked")
				Enumeration<TreeNode> e = node.children(); e.hasMoreElements();) {
					TreeNode n = e.nextElement();
					TreePath path = parent.pathByAddingChild(n);
					TreePath result = treePathFind(tree, path, nodes, depth + 1, byName);
					// Found a match
					if (result != null) {
						return result;
					}
				}
			}
		}
		// No match at this branch
		return null;
	}
}

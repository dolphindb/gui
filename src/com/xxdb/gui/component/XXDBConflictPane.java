package com.xxdb.gui.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultTreeModel;

import com.xxdb.gui.data.ProjectNode;

public class XXDBConflictPane extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private XXDBJDialog conflictDialog = null;
	private boolean importDone = true;

	public XXDBConflictPane(ProjectNode curNode, ProjectNode nodeToInsert,
							DefaultTreeModel treeModel, XXDBFileTree tree,
							String oldFolderPath, String newFolderPath,
							ProjectNode oldParent, ProjectNode newParent,
							int oldIndex, int newIndex) {
		super();

		String parentFolderPath = new File(newFolderPath).getParent();

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel promptLabel = new JLabel("Enter a new name for " + curNode.getName());
		JTextField conflictField = new JTextField(30);
		conflictField.setText(curNode.getName());
		JLabel conflictLabel = new JLabel(curNode.getName() + " already exists");

		JButton confirmButton = new JButton("OK");
		confirmButton.setEnabled(false);
		confirmButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String newName = conflictField.getText();
				treeModel.insertNodeInto(curNode, newParent, newIndex);
				tree.renameNodeFromTo(curNode,
									  oldFolderPath,
									  parentFolderPath + File.separator + newName);
				conflictDialog.dispose();
				importDone = true;
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				treeModel.insertNodeInto(curNode, oldParent, oldIndex);
				conflictDialog.dispose();
				importDone = false;
			}
		});
		
		conflictField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				textChanged();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				textChanged();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {}
			
			private void textChanged() {
				String newName = conflictField.getText();
				String oldPath = curNode.getDirPath();

				String newFilePath = new File(oldPath).getParent() + File.separator + newName;
				if (newName.trim().isEmpty()) {
					conflictLabel.setText(" ");
					confirmButton.setEnabled(false);
				} else if (new File(newFilePath).exists()) {
					conflictLabel.setText(newName + " already exists");
					confirmButton.setEnabled(false);
				} else {
					conflictLabel.setText(" ");
					confirmButton.setEnabled(true);
				}
			}
		});
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(promptLabel)
				.addComponent(conflictField)
				.addComponent(conflictLabel)
				.addGroup(layout.createSequentialGroup()
					.addComponent(confirmButton)
					.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(cancelButton))));
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(promptLabel)
			.addComponent(conflictField)
			.addComponent(conflictLabel)
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(confirmButton)
				.addComponent(cancelButton)));
		
		layout.linkSize(SwingConstants.HORIZONTAL, confirmButton, cancelButton);
		
		conflictDialog = new XXDBJDialog(tree.getEditor(), "DolphinDB name conflict", this);
		conflictDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		conflictDialog.setApplicationModal();
	}
	
	public boolean getImportDone() {
		return importDone;
	}
}

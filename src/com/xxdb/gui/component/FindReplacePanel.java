package com.xxdb.gui.component;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

public class FindReplacePanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static final int FIND = 0;
	private static final int REPLACE = 1;
	private static final int REPLACEALL = 2;
	private static final boolean FORWARD = true;
	private static final boolean BACKWARD = false;

	private RSyntaxTextArea searchArea;
	private JTextField searchField;
	private JTextField replaceField;
	private JCheckBox regexCB;
	private JCheckBox matchCaseCB;

	public FindReplacePanel(XXDBRSyntaxTextArea textArea) {this.setLayout(new BorderLayout());
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		this.searchArea = textArea;
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		this.searchArea = textArea;
	
		searchField = new JTextField(30);
		setDefaultSearchText();
		replaceField = new JTextField(30);
	    
		final JButton nextButton = new JButton("Next");
	    nextButton.setActionCommand("FindNext");
	    nextButton.addActionListener(this);
	    searchField.addActionListener(new ActionListener() {
	       public void actionPerformed(ActionEvent e) {
	          nextButton.doClick(0);
	       }
	    });
		
	    final JButton prevButton = new JButton("Prev");
	    prevButton.setActionCommand("FindPrev");
	    prevButton.addActionListener(this);
		
		final JButton replaceButton = new JButton("Replace");
	    replaceButton.setActionCommand("ReplaceNext");
	    replaceButton.addActionListener(this);
		
		final JButton replaceAllButton = new JButton("Replace All");
	    replaceAllButton.setActionCommand("ReplaceAll");
	    replaceAllButton.addActionListener(this);
		
		JLabel findLabel = new JLabel("Find");
		JLabel replaceLabel = new JLabel("Replace with");
		
	    regexCB = new JCheckBox("Regex");
	    matchCaseCB = new JCheckBox("Match Case");
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(findLabel)
				.addComponent(replaceLabel))
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(searchField)
				.addComponent(replaceField)
				.addGroup(layout.createSequentialGroup()
					.addComponent(regexCB)
					.addComponent(matchCaseCB)))
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(nextButton)
				.addComponent(replaceButton))
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(prevButton)
				.addComponent(replaceAllButton))		
		);
				
		layout.linkSize(SwingConstants.HORIZONTAL, nextButton, prevButton, replaceButton, replaceAllButton);
			
		layout.setVerticalGroup(
			layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(findLabel)
					.addComponent(searchField)
					.addComponent(nextButton)
					.addComponent(prevButton))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(replaceLabel)
					.addComponent(replaceField)
					.addComponent(replaceButton)
					.addComponent(replaceAllButton))
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(regexCB)
					.addComponent(matchCaseCB))
			);
	}
	
	public void actionPerformed(ActionEvent e) {
	    String command = e.getActionCommand();
	    if (command.equalsIgnoreCase("FindNext"))
	    	findReplace(FIND, FORWARD);
	    else if (command.equalsIgnoreCase("FindPrev"))
	    	findReplace(FIND, BACKWARD);
	    else if (command.equalsIgnoreCase("ReplaceNext"))
	    	findReplace(REPLACE, FORWARD);
	    else if (command.equalsIgnoreCase("ReplacePrev"))
	    	findReplace(REPLACE, BACKWARD);
	    else if (command.equalsIgnoreCase("ReplaceAll"))
	    	findReplace(REPLACEALL, FORWARD);
	}
	
	private void findReplace(int operation, boolean forward) {
	    boolean found;
	    int occurrence = 0;
	    
		SearchContext context = new SearchContext();
	    String text = searchField.getText();
	    if (text.length() == 0) {
	    	return;
	    }
	    context.setSearchFor(text);
	    context.setMatchCase(matchCaseCB.isSelected());
	    context.setRegularExpression(regexCB.isSelected());
	    context.setSearchForward(forward);
	    context.setWholeWord(false);
	    context.setReplaceWith(replaceField.getText());

	    switch (operation) {
	    case FIND:
	    	found = SearchEngine.find(searchArea, context).wasFound();
	    	if (!found)
		    	JOptionPane.showMessageDialog(this, "Text not found");
	    	break;
	    case REPLACE:
	    	found = SearchEngine.replace(searchArea, context).wasFound();
	    	if (!found)
		    	JOptionPane.showMessageDialog(this, "Text not found");
	    	break;
	    case REPLACEALL:
	    	occurrence = SearchEngine.replaceAll(searchArea, context).getCount();
	    	JOptionPane.showMessageDialog(this, "Replace count: " + occurrence);
	    	break;
	    }

	    
	}
	
	public RSyntaxTextArea getTextArea(){
		return searchArea;
	}
	
	public void setTextArea(RSyntaxTextArea area){
		searchArea = area;
	}

	public JTextField getSearchField() {
		return searchField;
	}

	public void setSearchField(JTextField searchField) {
		this.searchField = searchField;
	}
	
	public void setDefaultSearchText() {
		searchField.setText(searchArea.getSelectedText());
	}
}

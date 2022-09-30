package com.xxdb.gui.component;

import java.util.ArrayList;
import java.util.TreeMap;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

public class XXDBStatusBar extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private ArrayList<JLabel> list;
	private TreeMap<String, JLabel> map;
	private JPanel statusPanel;
	private GridBagLayout gbl;
	private GridBagConstraints gbc;
	private JLabel lastLabel;
	private int height;
	
	public XXDBStatusBar(){
		this(20);
	}
	
	public XXDBStatusBar(int height){
		Box box;
		
		lastLabel = null;
		this.height=height;
		setLayout(new BorderLayout());
		list=new ArrayList<>();
		map=new TreeMap<>();
		
		box=Box.createVerticalBox();
		statusPanel=new JPanel();
		
		box.add(statusPanel,BorderLayout.WEST);
		add(box,BorderLayout.SOUTH);
		
		gbl = new GridBagLayout();
		statusPanel.setLayout(gbl);	
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		gbc = new GridBagConstraints();
	}
	
	private void addLabel(JLabel lb, int pad){
		if (lastLabel !=null){
			statusPanel.remove(lastLabel);
		}
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0,0,0,pad);
		gbl.setConstraints(lb,gbc);
		statusPanel.add(lb);
		
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1;
		gbc.weighty = 1;
		lastLabel = new JLabel(" ");
		lastLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		gbl.setConstraints(lastLabel, gbc);
		statusPanel.add(lastLabel);
	}
	
	public void addJcomponent(JComponent control, int pad){
		if (lastLabel !=null){
			statusPanel.remove(lastLabel);
		}
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0,0,0,pad);
		gbl.setConstraints(control,gbc);
		statusPanel.add(control);
		
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1;
		gbc.weighty = 1;
		lastLabel = new JLabel(" ");
		lastLabel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		gbl.setConstraints(lastLabel, gbc);
		statusPanel.add(lastLabel);
	}
	
	public boolean addSection(String text, int width){
		return addSection(null,text,width);
	}
	
	public boolean addSection(String key, String text, int width){
		JLabel label;
		
		if(key==null)
			key=String.valueOf(list.size()+1);
		if(map.containsKey(key))
			return false;
		label=new JLabel(text);
		label.setName("label");
		label.setPreferredSize(new Dimension(width,height));
		label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		map.put(key,label);
		list.add(label);
		addLabel(label,2);
		return true;
	}
	
	public int getSectionCount(){
		return list.size();
	}
	
	public void setSectionText(int index, String text){
		if(index>=list.size())
			return;
		((JLabel)list.get(index)).setText(text);
	}
	
	public void setSectionText(String key, String text){
		JLabel label;
		
		label=(JLabel)map.get(key);
		if(label!=null)
			label.setText(text);
	}
}

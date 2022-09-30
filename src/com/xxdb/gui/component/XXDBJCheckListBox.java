package com.xxdb.gui.component;
import com.xxdb.gui.common.ListItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class XXDBJCheckListBox<E> extends JList<E> implements ActionListener {
	private static final long serialVersionUID = 1L;

	private CheckListBoxModel dataModal = null;
	private	JPopupMenu jPopupMenu;
	private JMenuItem jMenuItem;

    private class CheckListBoxModel extends AbstractListModel<E> {
		private static final long serialVersionUID = 1L;
		private LinkedHashSet<E> items = null;

        CheckListBoxModel(LinkedHashSet<E> items) {
            this.items = items;
        }

        public void addItem(E item){
        	this.items.add(item);
        }
        
        public int getSize() {
            return items.size();
        }
        
        protected void fireCheckChanged(Object source, int index) {
            fireContentsChanged(source, index, index);
        }
        
        protected void fireContentChanged(Object source, int index) {
            fireContentsChanged(source, index, index);
        }

		@Override
		public E getElementAt(int index) {
			E reItem = null;
			int i = 0;
			Iterator<E> iterator  = items.iterator();
			do{
				E item  = iterator.next();
				if(i==index){
					reItem = item;
					break;
				}
				i++;
			}while(iterator.hasNext());
			
			return reItem;
		}
        
    }

    public XXDBJCheckListBox(LinkedHashSet<E> items) {
    	dataModal = new CheckListBoxModel(items);
        setModel(dataModal);
        init();
    }
    
    public XXDBJCheckListBox() {
    	dataModal = new CheckListBoxModel(new LinkedHashSet<E>());
        setModel(dataModal);
        init();
    }
    
    protected void init() {
    	jPopupMenu = new JPopupMenu();
    	jMenuItem = new JMenuItem("remove selected item");
    	jMenuItem.addActionListener(this);
    	jPopupMenu.add(jMenuItem);
    	
    	addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				 if(e.getButton() == 3){
					 jPopupMenu.show((JList<?>)e.getSource(),e.getX(),e.getY());
				 }
			}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}
		});

        class MyCellRenderer extends JCheckBox implements ListCellRenderer<Object> {

            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public MyCellRenderer() {
                setOpaque(true);
            }

            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                setComponentOrientation(list.getComponentOrientation());
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }

                if (value instanceof Icon) {
                    setIcon((Icon) value);
                    setText("");
                } else {
                    setIcon(null);
                    setText((value == null) ? "" : value.toString());
                }
                setEnabled(list.isEnabled());
                setFont(list.getFont());

                this.setSelected(isChecked(index));
                return this;
            }
        }

        this.setCellRenderer(new MyCellRenderer());
        
        class CheckBoxListener extends MouseAdapter {

            @Override
            public void mouseClicked(MouseEvent e) {
            	if(e.getButton() == MouseEvent.BUTTON1){
	                int index = locationToIndex(e.getPoint());
	                if(index>=0) invertChecked(index);
            	}
            }
        }

        this.addMouseListener(new CheckBoxListener());
        
    }
    
    
    public void actionPerformed(ActionEvent e) {
    	Object obj = e.getSource();
        if(obj instanceof JMenuItem) {
            JMenuItem menuItem = (JMenuItem)obj;
            String str = menuItem.getText();
            if(str.equals("remove all")){
            	removeAll();
            }else if(str.equals("remove selected item")){
            	removeSelectedItem();
            }
        }
    }
    
    public void removeAllItems(){
        this.dataModal.items.removeAll(dataModal.items);
    	this.validate();
    	this.repaint();
    }
    
    public void removeSelectedItem(){
    	E sItem = (E)this.getSelectedValue();
    	
    	if(sItem!=null){
    		Iterator<E> iter= this.dataModal.items.iterator();
    		do {
				if(iter.next().equals(sItem)){
					iter.remove();
				}
			} while (iter.hasNext());
    	}
    	this.validate();
    	this.repaint();
    }
    public void addItem(E item){
    	if(this.getForeground()==Color.gray){
    		this.removeAllItems();
    	}
    	if(!this.dataModal.items.contains(item)){
        	this.dataModal.addItem(item);
        	CheckListBoxModel model = (CheckListBoxModel) getModel();
            model.fireContentChanged(this, this.dataModal.getSize()-1);
            this.repaint();    		
    	}
    }

    public void invertChecked(int index) {
    	ListItem item = (ListItem)this.dataModal.getElementAt(index);
    	if(item!=null){
    		item.reverseChecked();

            CheckListBoxModel model = (CheckListBoxModel) getModel();
            model.fireCheckChanged(this, index);
            this.repaint();
     	}
    }

    public boolean isChecked(int index) {
    	ListItem item = (ListItem)this.dataModal.getElementAt(index);
    	if(item!=null&&index>=0){
    		return item.getSelected();
    	}else{
    		return false;
    	}
    	
    }

    public int getCheckedCount() {
        int result = 0;
        for (int i = 0; i < this.dataModal.items.size(); i++) {
            ListItem item = (ListItem)this.dataModal.getElementAt(i);
            if(item!=null){
            	if (item.getSelected()) {
                    result++;
                }	
            }
            
        }
        return result;
    }

    public List<E> getCheckedItems() {
        List<E> result = new ArrayList<E>();
        for (int i = 0; i < this.dataModal.items.size(); i++) {
        	ListItem item = (ListItem)this.dataModal.getElementAt(i);
        	if(item!=null){
        		if (item.getSelected()) {
                    result.add(this.dataModal.getElementAt(i));
                }	
        	}
        }
        return result;
    }
}
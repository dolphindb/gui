package com.xxdb.gui.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.tree.TreePath;

public class XXDBJTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = 1L;
	
	private Map<String, XXDBRTextScrollPane> workbooks = new HashMap<String, XXDBRTextScrollPane>();
	private XXDBEditor parent;
	
	// Draggable tabs variables
	private boolean dragging = false;
	private int draggedTabIndex = 0;
	private Rectangle bounds;
	private Point currentMouseLocation = null;
	
	public XXDBJTabbedPane(XXDBEditor parent) {
		super();
		this.parent = parent;
		
		// Draggable tabs
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				if (!dragging) {
					int tabNum = getUI().tabForCoordinate(XXDBJTabbedPane.this, e.getX(), e.getY());

					if (tabNum >= 0) {
						draggedTabIndex = tabNum;
						bounds = getUI().getTabBounds(XXDBJTabbedPane.this, tabNum);
						
						dragging = true;
						repaint();
					}
				} else {
					currentMouseLocation = e.getPoint();
					repaint();
				}
			}
		});
		
		addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (dragging) {
					double mouseX = e.getX();
					int tabNum = getUI().tabForCoordinate(XXDBJTabbedPane.this, (int) mouseX, 10);

					if (tabNum < 0) {    /* Move to leftmost or rightmost */
						double leftBound = getUI().getTabBounds(XXDBJTabbedPane.this, 0).getMinX();
						if (mouseX < leftBound)
							tabNum = 0;
						else
							tabNum = XXDBJTabbedPane.this.getTabCount() - 1;
					}
					
					if (tabNum != draggedTabIndex) {
						String title = getTitleAt(draggedTabIndex);
						Component comp = getComponentAt(draggedTabIndex);
						Component tabComp = getTabComponentAt(draggedTabIndex);
						
						removeTabAt(draggedTabIndex);
						insertTab(title, comp, tabComp, tabNum);
						setSelectedIndex(tabNum);
					}
					repaint();
					
					dragging = false;
					currentMouseLocation = null;
				}
			}
		});
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (dragging && currentMouseLocation != null) {
			Graphics2D g2 = (Graphics2D) g;
			float thickness = 2;
			Stroke oldStorke = g2.getStroke();
			g2.setStroke(new BasicStroke(thickness));
			g2.drawRect(currentMouseLocation.x, bounds.y, bounds.width, bounds.height);
			g2.setStroke(oldStorke);
		}
	}

	private void locateInFileTree(XXDBRTextScrollPane tab) {
		TreePath pathOfSelectedNode = tab.getNodePath();
		XXDBFileTree tree = parent.getExplorerTree();
		tree.makeVisible(pathOfSelectedNode);
		tree.scrollPathToVisible(pathOfSelectedNode);
		tree.setSelectionPath(pathOfSelectedNode);
	}

	public XXDBRTextScrollPane addWorkBook(String name, XXDBRTextScrollPane editor) {
		String path = editor.getTextArea().getOpenFilePath();
		if(path == null){
			add(name, editor);
			return editor;
		}
		else if(!workbooks.containsKey(path)) {
			workbooks.put(path, editor);
			add(name, editor);
		}
		return workbooks.get(path);
	}
	
	public boolean renameWorkbook(String oldPath, String newPath) {
		XXDBRTextScrollPane panel = workbooks.get(oldPath);
		if (panel != null) {
			try {
				workbooks.remove(oldPath);
				workbooks.put(newPath, panel);
				
				int index = indexOfComponent(panel);
				String newFilename = new File(newPath).getName();
				setTitleAt(index, newFilename);
				panel.getTextArea().setOpenFilePath(newPath);    // The absolute path will be converted to the relative one.
			}
			catch (Exception ex) {
				return false;
			}
		}
		return true;
	}
	
	public Collection<XXDBRTextScrollPane> getAllWorkbooks() {
		return workbooks.values();
	}
	
	public XXDBRTextScrollPane isAlreadyOpen(String fileName) {
		return workbooks.get(fileName);
	}
		
	public boolean removeWorkBook(String filenName) {
		workbooks.remove(filenName);
		return true;
	}
	
	public void removeAllWorkBook(){
		workbooks.clear();
	}
	
    @Override
    public void addTab(String title, Icon icon, Component component, String tip) {
        super.addTab(title, icon, component, tip);
        int count = this.getTabCount() - 1;

    	CloseButtonTab tab = new CloseButtonTab(this, (XXDBRTextScrollPane)component, title, icon);
        setTabComponentAt(count, tab);
    	tab.setTitle(title);
    }

    @Override
    public void addTab(String title, Icon icon, Component component) {
        addTab(title, icon, component, null);
    }

    @Override
    public void addTab(String title, Component component) {
        addTab(title, null, component);
    }
    
    public void insertTab(String title, Component component, Component tabComponent, int index) {
    	super.insertTab(title, null, component, null, index);
    	setTabComponentAt(index, tabComponent);
    }
    
    public void addTabNoExit(String title, Icon icon, Component component, String tip) {
        super.addTab(title, icon, component, tip);
    }

    public void addTabNoExit(String title, Icon icon, Component component) {
        addTabNoExit(title, icon, component, null);
    }

    public void addTabNoExit(String title, Component component) {
        addTabNoExit(title, null, component);
    }
    
    @Override
    public void setTitleAt(int index, String title){
    	super.setTitleAt(index, title);
    	
    	CloseButtonTab tabComp = (CloseButtonTab) getTabComponentAt(index);
    	if (tabComp != null)
    		tabComp.setTitle(title);
    }
    
	private void removeTab(XXDBRTextScrollPane tab){
		XXDBRSyntaxTextArea current = (XXDBRSyntaxTextArea) (tab.getTextArea());
		if(!current.isSaved()) {
			int selection = JOptionPane.showConfirmDialog(this, 
	            "Do you want to save : " + current.getName() + "?", 
	            "DolphinDB Workspace", 
	            JOptionPane.YES_NO_OPTION,
	            JOptionPane.QUESTION_MESSAGE);
			if (selection == JOptionPane.YES_OPTION){
	        	try {
	        		XXDBMenuBar.saveFile(null, current);
	        	} catch (Exception e) {
	        	}
	        }
		}
		removeWorkBook(tab.getTextArea().getOpenFilePath());
		
		int index = indexOfComponent(tab);
		remove(index);
        
        checkAllTabsClosed();
	}
	
	protected void checkAllTabsClosed() {
		if(getSelectedIndex() < 0)
        	parent.resetWorkbook(null);
	}

    public class CloseButtonTab extends JPanel {
		private static final long serialVersionUID = 1L;
		private JLabel titleLabel;

        public CloseButtonTab(final XXDBJTabbedPane parent, final XXDBRTextScrollPane tab, String title, Icon icon) {
            setOpaque(false);
            FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER, 3, 3);
            setLayout(flowLayout);
            titleLabel = new JLabel(title);
            titleLabel.setIcon(icon);
            add(titleLabel);

            JButton button = new JButton(MetalIconFactory.getInternalFrameCloseIcon(16));
            button.setMargin(new Insets(0, 0, 0, 0));
            button.addMouseListener(new CloseListener(tab));
            add(button);
            
            ActionListener actionListener = new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent event) {
					String cmd = event.getActionCommand();
					if(cmd.equalsIgnoreCase("Close")){
				       removeTab(tab);    
					}
					else if(cmd.equalsIgnoreCase("CloseOthers")){
						for(XXDBRTextScrollPane comp : parent.getAllWorkbooks().toArray(new XXDBRTextScrollPane[]{})){
							if(comp != tab)
								removeTab(comp);
						}
					}
					else if(cmd.equalsIgnoreCase("CloseAll")){
						for(XXDBRTextScrollPane comp : parent.getAllWorkbooks().toArray(new XXDBRTextScrollPane[]{})){
							removeTab(comp);
						}
					}
					else if (cmd.equalsIgnoreCase("Locate")) {
						locateInFileTree(tab);
					}
				}
            };
            
            final JPopupMenu menu=new JPopupMenu();
    	    final JMenuItem mnuClose = new JMenuItem("Close");    
    	    mnuClose.addActionListener(actionListener);
    	    mnuClose.setActionCommand("Close");
    	    menu.add(mnuClose);
    	    
    	    final JMenuItem mnuCloseOthers = new JMenuItem("Close Others");    
    	    mnuCloseOthers.addActionListener(actionListener);
    	    mnuCloseOthers.setActionCommand("CloseOthers");
    	    menu.add(mnuCloseOthers);
    	    menu.addSeparator();
    	    
    	    final JMenuItem mnuCloseAll = new JMenuItem("Close All");    
    	    mnuCloseAll.addActionListener(actionListener);
    	    mnuCloseAll.setActionCommand("CloseAll");
    	    menu.add(mnuCloseAll);
    	    menu.addSeparator();
    	    
    	    final JMenuItem mnuLocate = new JMenuItem("Locate");
    	    mnuLocate.addActionListener(actionListener);
    	    mnuLocate.setActionCommand("Locate");
    	    menu.add(mnuLocate);
    	    
    	    addMouseMotionListener(new MouseAdapter() {
    	    	public void mouseDragged(MouseEvent e) {
    	    		/* Redispatch to parent */
    	    		Component source = (Component) e.getSource();
    	    		MouseEvent parentEvent = SwingUtilities.convertMouseEvent(source, e, parent);
    	    		parent.dispatchEvent(parentEvent);
    	    	}
    	    });
    	    
    	    addMouseListener(new MouseAdapter(){
    			public void mousePressed(MouseEvent e) {
    				if((System.getProperty("os.name").equalsIgnoreCase("linux")
							|| System.getProperty("os.name").toLowerCase().contains("mac")) && e.isPopupTrigger()){
						menu.show(e.getComponent(), e.getX(), e.getY());
					}else {
						onPressOrRelease(e);
					}
    			} 
    			
    	    	public void mouseReleased(MouseEvent e) {
    	    		onPressOrRelease(e);
    			}
    	    	
    	    	private void onPressOrRelease(MouseEvent e){
    				if (e.isPopupTrigger()) {
    					menu.show(e.getComponent(), e.getX(), e.getY());
    				}
    				else{
    					CloseButtonTab tab = (CloseButtonTab)e.getSource();
    					int selectedIndex = parent.getSelectedIndex();
    					if(selectedIndex < 0 || parent.getTabComponentAt(parent.getSelectedIndex()) != tab){
    						int newSelectedIndex = getTabIndex(tab);
    						parent.setSelectedIndex(newSelectedIndex);
    					}
    				}
    	            
    				/* Redispatch to parent */
    	            Component source = (Component) e.getSource();
    	            MouseEvent parentEvent = SwingUtilities.convertMouseEvent(source, e, parent);
    	            parent.dispatchEvent(parentEvent);
    	    	}
    	    	
    	    	private int getTabIndex(CloseButtonTab tab) {
    	    		int tabCount = parent.getTabCount();
					for(int i=0; i<tabCount; ++i){
						if(parent.getTabComponentAt(i) == tab){
							return i;
						}
					}
					return -1;
    	    	}
    	    });
        }
        
        public void setTitle(String title){
        	titleLabel.setText(title);
        }
    }

    private class CloseListener implements MouseListener {
        private XXDBRTextScrollPane tab;
        private Border oldBorder = null;

        public CloseListener(XXDBRTextScrollPane tab){
            this.tab=tab;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getSource() instanceof JButton){
                removeTab(tab);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {
            if(e.getSource() instanceof JButton){
                JButton clickedButton = (JButton) e.getSource();
                oldBorder = oldBorder == null ? clickedButton.getBorder() : oldBorder;
                clickedButton.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY,3));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if(e.getSource() instanceof JButton){
                JButton clickedButton = (JButton) e.getSource();
                clickedButton.setBorder(oldBorder);
            }
        }
    }
}



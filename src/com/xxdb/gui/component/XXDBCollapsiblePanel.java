package com.xxdb.gui.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

public class XXDBCollapsiblePanel extends JPanel {
 
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String title = "Title";
	TitledBorder border;
    public XXDBCollapsiblePanel() {
        Border b1 = BorderFactory.createEmptyBorder(10, 20, 10, 20);
		border = BorderFactory.createTitledBorder(title);
		CompoundBorder b2 = BorderFactory.createCompoundBorder(b1,border);
        setBorder(b2);
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);
        addMouseListener(mouseListener);
    }
 
    MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            toggleVisibility();
        }
    };
 
    ComponentListener contentComponentListener = new ComponentAdapter() {
        @Override
        public void componentShown(ComponentEvent e) {
            updateBorderTitle();
        }
        @Override
        public void componentHidden(ComponentEvent e) {
            updateBorderTitle();
        }
    };
 
    public String getTitle() {
        return title;
    }
 
    public void setTitle(String title) {
        firePropertyChange("title", this.title, this.title = title);
    }
 
    @Override
    public Component add(Component comp) {
        comp.addComponentListener(contentComponentListener);
        Component r = super.add(comp);
        updateBorderTitle();
        return r;
    }
 
    @Override
    public Component add(String name, Component comp) {
        comp.addComponentListener(contentComponentListener);
        Component r = super.add(name, comp);
        updateBorderTitle();
        return r;
    }
 
    @Override
    public Component add(Component comp, int index) {
        comp.addComponentListener(contentComponentListener);
        Component r = super.add(comp, index);
        updateBorderTitle();
        return r;
    }
 
    @Override
    public void add(Component comp, Object constraints) {
        comp.addComponentListener(contentComponentListener);
        super.add(comp, constraints);
        updateBorderTitle();
    }
 
    @Override
    public void add(Component comp, Object constraints, int index) {
        comp.addComponentListener(contentComponentListener);
        super.add(comp, constraints, index);
        updateBorderTitle();
    }
 
    @Override
    public void remove(int index) {
        Component comp = getComponent(index);
        comp.removeComponentListener(contentComponentListener);
        super.remove(index);
    }
 
    @Override
    public void remove(Component comp) {
        comp.removeComponentListener(contentComponentListener);
        super.remove(comp);
    }
 
    @Override
    public void removeAll() {
        for (Component c : getComponents()) {
            c.removeComponentListener(contentComponentListener);
        }
        super.removeAll();
    }
 
    protected void toggleVisibility() {
        toggleVisibility(hasInvisibleComponent());
    }
 
    protected void toggleVisibility(boolean visible) {
        for (Component c : getComponents()) {
            c.setVisible(visible);
        }
        updateBorderTitle();
    }
 
    protected void updateBorderTitle() {
        char arrow = (char)31; 
        if (getComponentCount() > 0) {
            arrow = (hasInvisibleComponent()?(char)31:(char)30);
        }
        border.setTitle(title +" "+ arrow);
        repaint();
    }
 
    protected final boolean hasInvisibleComponent() {
        for (Component c : getComponents()) {
            if (!c.isVisible()) {
                return true;
            }
        }
        return false;
    }
 
}
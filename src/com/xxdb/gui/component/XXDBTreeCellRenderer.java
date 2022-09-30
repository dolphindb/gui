package com.xxdb.gui.component;

import java.awt.Component;
import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import com.xxdb.gui.common.Utility;

public class XXDBTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 1L;

	double adjrate = Utility.getAdjustRate();

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		setText(value.toString());

		if (sel) {
			setForeground(getTextSelectionColor());
		} else {
			setForeground(getTextNonSelectionColor());
		}

		this.setIcon(getScaleIcon(this.getIcon()));

		return this;
	}

	private ImageIcon getScaleIcon(Icon icon) {
		ImageIcon imgIcon = (ImageIcon) icon;
		Image img = imgIcon.getImage();
		int w = (int) (icon.getIconWidth() * adjrate);
		int h = (int) (icon.getIconHeight() * adjrate);
		Image scaleImage = img.getScaledInstance(w, h, Image.SCALE_DEFAULT);
		return new ImageIcon(scaleImage);
	}

}

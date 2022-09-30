package com.xxdb.gui.component;

import javax.swing.border.Border;
import java.awt.*;

public class BasicStyleBorder implements Border {
    private int thickness;
    private Insets insets;
    private Dimension lastComponentSize;
    private Color color;
    private Color color2;

    public BasicStyleBorder(Color color, int thickness) {
        this.color = color;
        if (color == null) {
            this.color = color = Color.gray;
        }
        color2 = new Color(210, 210, 210, 0);
        this.thickness = thickness;
    }

    public Insets getBorderInsets(Component c) {
        Dimension currentComponent = c.getSize();

        if (currentComponent.equals(lastComponentSize)) {
            return insets;
        }

        insets = new Insets(thickness, thickness, thickness, thickness);
        lastComponentSize = currentComponent;
        return insets;
    }

    public boolean isBorderOpaque() {
        return true;
    }
    public void paintBorder(Component c, Graphics g, int x, int y, int width,
                            int height) {
        Graphics2D g2d = (Graphics2D) g.create();

        GradientPaint gp = new GradientPaint(x, y, color, x, y + thickness,
                color2);
        g2d.setPaint(gp);
        g2d.fillRect(x, y, width, thickness);


        gp = new GradientPaint(x, y + height - thickness - 1, color2, x, y
                + height, color);
        g2d.setPaint(gp);
        g2d.fillRect(x, y + height - thickness - 1, width, thickness);
        gp = new GradientPaint(x, y, color, x + thickness, y, color2);
        g2d.setPaint(gp);
        g2d.fillRect(x, y, thickness, height);
        gp = new GradientPaint(x + width - thickness - 1, y, color2, x + width,
                y, color);
        g2d.setPaint(gp);
        g2d.fillRect(x + width - thickness - 1, y, thickness, height);
        g2d.setPaint(color);
        g2d.drawRect(x, y, width - 1, height - 1);
        g2d.dispose();
    }
}
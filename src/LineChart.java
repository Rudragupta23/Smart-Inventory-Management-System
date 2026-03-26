import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class LineChart extends JPanel {
    private Map<String, Integer> data;
    private Color lineColor = new Color(231, 76, 60); // Red line
    private Color textColor = Color.BLACK;

    public void setData(Map<String, Integer> data) {
        this.data = data;
        repaint(); 
    }

    public void setDarkMode(boolean isDark) {
        this.textColor = isDark ? Color.WHITE : Color.BLACK;
        this.lineColor = isDark ? new Color(241, 196, 15) : new Color(231, 76, 60); // Yellow line in dark mode
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth(); 
        int height = getHeight();
        int padding = 55; 
        int labelPadding = 25;

        int maxQty = 1;
        for (int qty : data.values()) { if (qty > maxQty) maxQty = qty; }

        g2d.setColor(textColor);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(padding, height - padding, padding, padding); 
        g2d.drawLine(padding, height - padding, width - padding, height - padding); 

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.drawString("Stock Trend", padding - 30, padding - 15);
        g2d.drawString("Categories", width / 2 - 30, height - 10);

        int pointSpacing = (width - (2 * padding)) / data.size();
        int xOffset = padding + (pointSpacing / 2);

        int prevX = -1;
        int prevY = -1;

        // Draw the line connecting the points
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int pointHeight = (int) (((double) entry.getValue() / maxQty) * (height - 2 * padding - labelPadding));
            int currentX = xOffset;
            int currentY = height - padding - pointHeight;

            if (prevX != -1 && prevY != -1) {
                g2d.setColor(lineColor);
                g2d.setStroke(new BasicStroke(3f)); 
                g2d.drawLine(prevX, prevY, currentX, currentY);
            }

            prevX = currentX;
            prevY = currentY;
            xOffset += pointSpacing;
        }

        // Draw the dots and text
        xOffset = padding + (pointSpacing / 2);
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int pointHeight = (int) (((double) entry.getValue() / maxQty) * (height - 2 * padding - labelPadding));
            int currentX = xOffset;
            int currentY = height - padding - pointHeight;

            g2d.setColor(new Color(41, 128, 185)); 
            g2d.fillOval(currentX - 6, currentY - 6, 12, 12);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2f));
            g2d.drawOval(currentX - 6, currentY - 6, 12, 12);

            g2d.setColor(textColor);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            String valueLabel = entry.getValue() + " Units";
            g2d.drawString(valueLabel, currentX - (g2d.getFontMetrics().stringWidth(valueLabel) / 2), currentY - 15);
            g2d.drawString(entry.getKey(), currentX - (g2d.getFontMetrics().stringWidth(entry.getKey()) / 2), height - padding + 20);

            xOffset += pointSpacing;
        }
    }
}
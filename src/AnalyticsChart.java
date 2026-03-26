import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class AnalyticsChart extends JPanel {
    private Map<String, Integer> data;
    private Color barColor = new Color(46, 204, 113);
    private Color textColor = Color.BLACK;

    public void setData(Map<String, Integer> data) {
        this.data = data;
        repaint(); 
    }

    public void setDarkMode(boolean isDark) {
        this.textColor = isDark ? Color.WHITE : Color.BLACK;
        this.barColor = isDark ? new Color(52, 152, 219) : new Color(46, 204, 113);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth(); int height = getHeight();
        int padding = 55; int labelPadding = 25;

        int maxQty = 1;
        for (int qty : data.values()) { if (qty > maxQty) maxQty = qty; }

        g2d.setColor(textColor);
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(padding, height - padding, padding, padding); 
        g2d.drawLine(padding, height - padding, width - padding, height - padding); 

        // Clearly state what the graph represents
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.drawString("Units in Stock", padding - 30, padding - 15);
        g2d.drawString("Categories", width / 2 - 30, height - 10);

        int barWidth = (width - (2 * padding)) / data.size() - 20;
        int xOffset = padding + 10;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int barHeight = (int) (((double) entry.getValue() / maxQty) * (height - 2 * padding - labelPadding));
            
            g2d.setColor(barColor);
            g2d.fillRect(xOffset, height - padding - barHeight, barWidth, barHeight);
            
            g2d.setColor(textColor);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            // Added explicit "Units" text
            String valText = entry.getValue() + " Units";
            g2d.drawString(valText, xOffset + (barWidth / 2) - (g2d.getFontMetrics().stringWidth(valText)/2), height - padding - barHeight - 5);
            g2d.drawString(entry.getKey(), xOffset + (barWidth / 2) - (g2d.getFontMetrics().stringWidth(entry.getKey())/2), height - padding + 20);

            xOffset += barWidth + 20;
        }
    }
}
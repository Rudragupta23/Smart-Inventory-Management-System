import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class PieChart extends JPanel {
    private Map<String, Integer> data;
    private boolean isDark = false;
    private final Color[] colors = {
        new Color(46, 204, 113), new Color(52, 152, 219), 
        new Color(155, 89, 182), new Color(241, 196, 15), 
        new Color(230, 126, 34), new Color(231, 76, 60)
    };

    public void setData(Map<String, Integer> data) {
        this.data = data;
        repaint();
    }

    public void setDarkMode(boolean isDark) {
        this.isDark = isDark;
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
        
        // Reduced size to fit inside the new, smaller 3-chart layout
        int minSize = Math.min(width, height) - 100; 
        
        // Centered horizontally (removed the +50) and shifted down slightly to clear the text legend
        int x = (width - minSize) / 2; 
        int y = (height - minSize) / 2 + 35;

        int total = 0;
        for (int val : data.values()) total += val;

        int startAngle = 0;
        int colorIdx = 0;

        // Draw Pie Slices
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int arcAngle = (int) Math.round((double) entry.getValue() / total * 360);
            g2d.setColor(colors[colorIdx % colors.length]);
            g2d.fillArc(x, y, minSize, minSize, startAngle, arcAngle);
            startAngle += arcAngle;
            colorIdx++;
        }
        
        // Draw Legend with explicitly clarified labels
        int legendY = 20;
        colorIdx = 0;
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            g2d.setColor(colors[colorIdx % colors.length]);
            g2d.fillRect(10, legendY, 15, 15);
            
            g2d.setColor(isDark ? Color.WHITE : Color.BLACK);
            int percentage = (int) Math.round((double) entry.getValue() / total * 100);
            
            // Added clear text explaining what the percentage represents
            g2d.drawString(entry.getKey() + " (" + percentage + "% of Total Inventory)", 35, legendY + 12);
            
            legendY += 25;
            colorIdx++;
        }
    }
}
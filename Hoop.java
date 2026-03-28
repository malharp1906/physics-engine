import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Hoop {
    private double x, y, width, height;
    private boolean ballInside = false;
    private Vector2D previousPos;

    // --- VERLET SOFT-BODY NET VARIABLES ---
    private class NetNode {
        double x, y, oldX, oldY;
        boolean fixed;
        
        public NetNode(double x, double y, boolean fixed) {
            this.x = x; this.y = y;
            this.oldX = x; this.oldY = y;
            this.fixed = fixed;
        }
    }

    private List<NetNode> leftNodes = new ArrayList<>();
    private List<NetNode> rightNodes = new ArrayList<>();
    private int segments = 6; 
    private double linkLen = 14.0; 

    public Hoop(double x, double y, double width, double height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.previousPos = new Vector2D(x, y);

        for (int i = 0; i <= segments; i++) {
            leftNodes.add(new NetNode(x, y + i * linkLen, i == 0));
            rightNodes.add(new NetNode(x + width, y + i * linkLen, i == 0));
        }
    }

    public void simulateNet(Ball b) {
        for (int i = 0; i <= segments; i++) {
            updateNode(leftNodes.get(i), b);
            updateNode(rightNodes.get(i), b);
        }

        for (int iter = 0; iter < 5; iter++) {
            for (int i = 0; i < segments; i++) {
                applyConstraint(leftNodes.get(i), leftNodes.get(i + 1), linkLen);
                applyConstraint(rightNodes.get(i), rightNodes.get(i + 1), linkLen);
                
                double targetWidth = width - (i * 5.0);
                applyConstraint(leftNodes.get(i), rightNodes.get(i), targetWidth);
            }
            applyConstraint(leftNodes.get(segments), rightNodes.get(segments), width - (segments * 5.0));
        }
    }

    private void updateNode(NetNode n, Ball b) {
        if (n.fixed) return; 

        double vx = (n.x - n.oldX) * 0.92; 
        double vy = (n.y - n.oldY) * 0.92;

        n.oldX = n.x;
        n.oldY = n.y;

        n.x += vx;
        n.y += vy + 0.5; 

        if (b != null) {
            double dx = n.x - b.pos.x;
            double dy = n.y - b.pos.y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            if (dist < b.radius) {
                double push = (b.radius - dist);
                n.x += (dx / dist) * push;
                n.y += (dy / dist) * push;
            }
        }
    }

    private void applyConstraint(NetNode n1, NetNode n2, double targetDist) {
        double dx = n2.x - n1.x;
        double dy = n2.y - n1.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) return;

        double diff = (dist - targetDist) / dist;
        double offsetX = dx * 0.5 * diff;
        double offsetY = dy * 0.5 * diff;

        if (!n1.fixed) { n1.x += offsetX; n1.y += offsetY; }
        if (!n2.fixed) { n2.x -= offsetX; n2.y -= offsetY; }
    }

    public boolean checkScore(Ball b) {
        if (b == null) return false;

        double hoopMinX = x;
        double hoopMaxX = x + width;
        double hoopMinY = y;
        double hoopMaxY = y + height;

        boolean currentlyInside = (b.pos.x > hoopMinX && b.pos.x < hoopMaxX &&
                                   b.pos.y > hoopMinY && b.pos.y < hoopMaxY);
        boolean scored = false;

        if (currentlyInside) {
            if (!ballInside && b.pos.y > previousPos.y) ballInside = true;
        } else {
            if (ballInside && b.pos.y > y + height) { 
                scored = true;
                ballInside = false;
            } else if (ballInside) {
                ballInside = false; 
            }
        }

        previousPos.set(b.pos.x, b.pos.y);
        return scored;
    }

    public void draw(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(3)); 

        for (int i = 0; i < segments; i++) {
            NetNode l1 = leftNodes.get(i);
            NetNode l2 = leftNodes.get(i + 1);
            NetNode r1 = rightNodes.get(i);
            NetNode r2 = rightNodes.get(i + 1);

            // Silver Net Strings
            g2d.setColor(new Color(160, 160, 170)); 
            
            // Vertical bounding strings
            g2d.drawLine((int)l1.x, (int)l1.y, (int)l2.x, (int)l2.y);
            g2d.drawLine((int)r1.x, (int)r1.y, (int)r2.x, (int)r2.y);
            
            // The Zig-Zag Diamond Pattern
            if (i < segments) {
                g2d.drawLine((int)l1.x, (int)l1.y, (int)r2.x, (int)r2.y);
                g2d.drawLine((int)r1.x, (int)r1.y, (int)l2.x, (int)l2.y);
            }
            
            // Metal Connector Joints (Tiny dark gray circles at intersections)
            g2d.setColor(new Color(100, 100, 110));
            g2d.fillOval((int)l1.x - 2, (int)l1.y - 2, 4, 4);
            g2d.fillOval((int)r1.x - 2, (int)r1.y - 2, 4, 4);
        }
        
        // Draw the Solid Red Rim over the top of the net
        g2d.setColor(new Color(220, 30, 30));
        g2d.setStroke(new BasicStroke(6));
        g2d.drawLine((int)x - 5, (int)y, (int)(x + width) + 5, (int)y); 
        g2d.setStroke(new BasicStroke(1));
    }
}
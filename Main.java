import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.RadialGradientPaint;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private EnginePanel enginePanel;

    public Main() {
        setTitle("Java 2D Physics Engine");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null); // Center window on screen

        // Initialize the CardLayout Manager
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Instantiate our screens
        enginePanel = new EnginePanel(this); 
        MainMenuPanel menuPanel = new MainMenuPanel(this);

        // Add them to the deck
        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(enginePanel, "ENGINE");

        add(mainContainer);
        
        // Show the menu first
        cardLayout.show(mainContainer, "MENU");
    }

    // Public routers called by the Menu buttons
    public void startGame(String gameMode) {
        if (gameMode.equals("BILLIARDS")) {
            enginePanel.loadBilliardsScene();
        } else {
            enginePanel.loadBasketballScene();
        }
        cardLayout.show(mainContainer, "ENGINE");
    }

    public void showMenu() {
        cardLayout.show(mainContainer, "MENU");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}

// ==========================================
// HIGH-END MAIN MENU GRAPHICS
// ==========================================
class MainMenuPanel extends JPanel implements Runnable {
    private Main parent;
    private Rectangle btnBilliards = new Rectangle(300, 400, 180, 60);
    private Rectangle btnBasketball = new Rectangle(520, 400, 180, 60);
    
    private boolean hoverBilliards = false;
    private boolean hoverBasketball = false;

    // Background floating particles
    private List<MenuParticle> particles = new ArrayList<>();

    public MainMenuPanel(Main parent) {
        this.parent = parent;
        
        // Generate floating background orbs
        for (int i = 0; i < 20; i++) {
            particles.add(new MenuParticle(
                Math.random() * 1000, Math.random() * 700, 
                (Math.random() - 0.5) * 4, (Math.random() - 0.5) * 4, 
                Math.random() * 30 + 10
            ));
        }

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverBilliards = btnBilliards.contains(e.getPoint());
                hoverBasketball = btnBasketball.contains(e.getPoint());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (hoverBilliards) parent.startGame("BILLIARDS");
                if (hoverBasketball) parent.startGame("BASKETBALL");
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        new Thread(this).start(); // Start menu animation loop
    }

    @Override
    public void run() {
        while (true) {
            // Update background particles
            for (MenuParticle p : particles) {
                p.x += p.vx;
                p.y += p.vy;
                if (p.x < 0 || p.x > 1000) p.vx *= -1;
                if (p.y < 0 || p.y > 700) p.vy *= -1;
            }
            repaint();
            try { Thread.sleep(16); } catch (Exception e) {}
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Deep Space Radial Gradient Background
        Point2D center = new Point2D.Float(500, 350);
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {new Color(30, 35, 55), new Color(10, 12, 18)};
        RadialGradientPaint gradient = new RadialGradientPaint(center, 700, dist, colors);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // 2. Draw Floating Glowing Particles
        for (MenuParticle p : particles) {
            g2d.setColor(new Color(100, 200, 255, 30)); // Highly transparent Cyan
            g2d.fillOval((int)(p.x - p.r), (int)(p.y - p.r), (int)p.r*2, (int)p.r*2);
            g2d.setColor(new Color(100, 200, 255, 80)); // Inner core
            g2d.fillOval((int)(p.x - p.r/2), (int)(p.y - p.r/2), (int)p.r, (int)p.r);
        }

        // 3. Title Text
        g2d.setFont(new Font("SansSerif", Font.BOLD, 64));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "PHYSICS ENGINE";
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        
        // Drop shadow for title
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString(title, titleX + 4, 254);
        // Main title color
        g2d.setColor(Color.WHITE);
        g2d.drawString(title, titleX, 250);

        // Subtitle
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 24));
        g2d.setColor(new Color(180, 180, 200));
        g2d.drawString("SELECT SIMULATION", 380, 300);

        // 4. Draw Interactive Buttons
        drawMenuButton(g2d, btnBilliards, "🎱 Billiards", hoverBilliards, new Color(46, 139, 87));
        drawMenuButton(g2d, btnBasketball, "🏀 Basketball", hoverBasketball, new Color(220, 80, 30));
    }

    private void drawMenuButton(Graphics2D g2d, Rectangle rect, String text, boolean isHovered, Color baseColor) {
        // Drop Shadow
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRoundRect(rect.x + 5, rect.y + 5, rect.width, rect.height, 20, 20);

        // Hover Effect Color Lerping
        if (isHovered) {
            g2d.setColor(baseColor.brighter());
        } else {
            g2d.setColor(baseColor);
        }
        g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);

        // Button Outline
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);

        // Button Text
        g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int textY = rect.y + ((rect.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }
}

// Simple struct for the background menu effect
class MenuParticle {
    double x, y, vx, vy, r;
    public MenuParticle(double x, double y, double vx, double vy, double r) {
        this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.r = r;
    }
}
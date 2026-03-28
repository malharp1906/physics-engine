import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EnginePanel extends JPanel implements Runnable {
    public enum Scene { BILLIARDS, BASKETBALL }
    private Scene currentScene = Scene.BASKETBALL;

    private List<Ball> balls = new ArrayList<>();
    private List<Wall> walls = new ArrayList<>();
    private List<Vector2D> pockets = new ArrayList<>();
    private Vector2D gravity = new Vector2D(0, 0);
    private Hoop basketballHoop; 

    private final int POCKET_RADIUS = 32; 

    private Ball activeBall = null;
    private boolean isAiming = false;
    private double aimX, aimY;
    private double power = 0.0;
    private double angle = 0.0;
    private int score = 0;

    private Rectangle btnBilliards = new Rectangle(800, 20, 150, 40);
    private Rectangle btnBasketball = new Rectangle(800, 70, 150, 40);
    private Rectangle btnReset = new Rectangle(800, 120, 150, 40);
    private Rectangle btnMenu = new Rectangle(800, 170, 150, 40);

    private Main parentFrame;

    public EnginePanel(Main parent) {
        this.parentFrame = parent;
        loadBasketballScene();

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (btnBilliards.contains(e.getPoint())) loadBilliardsScene();
                else if (btnBasketball.contains(e.getPoint())) loadBasketballScene();
                else if (btnReset.contains(e.getPoint())) {
                    if (currentScene == Scene.BILLIARDS) loadBilliardsScene();
                    else loadBasketballScene();
                }
                else if (btnMenu.contains(e.getPoint())) {
                    parentFrame.showMenu(); 
                }

                if (activeBall != null) {
                    double dx = e.getX() - activeBall.pos.x;
                    double dy = e.getY() - activeBall.pos.y;
                    if (dx * dx + dy * dy <= 40 * 40) { 
                        isAiming = true;
                        aimX = e.getX();
                        aimY = e.getY();
                        activeBall.vel.set(0, 0); 
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isAiming) {
                    aimX = e.getX();
                    aimY = e.getY();
                    
                    double dx = activeBall.pos.x - aimX;
                    double dy = activeBall.pos.y - aimY;
                    power = Math.min(Math.sqrt(dx * dx + dy * dy) * 0.15, 30.0); 
                    angle = Math.toDegrees(Math.atan2(-dy, dx));
                    if (angle < 0) angle += 360;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isAiming) {
                    isAiming = false;
                    double dx = activeBall.pos.x - aimX;
                    double dy = activeBall.pos.y - aimY;
                    
                    Vector2D launchVel = new Vector2D(dx * 0.15, dy * 0.15);
                    if (launchVel.mag() > 30) launchVel.mult(30 / launchVel.mag());
                    
                    activeBall.vel.set(launchVel.x, launchVel.y);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        new Thread(this).start();
    }

    public void loadBilliardsScene() {
        currentScene = Scene.BILLIARDS;
        balls.clear(); walls.clear(); pockets.clear();
        gravity.set(0, 0);
        score = 0;

        walls.add(new Wall(100, 100, 800, 50, new Color(139, 69, 19))); 
        walls.add(new Wall(100, 550, 800, 50, new Color(139, 69, 19))); 
        walls.add(new Wall(100, 150, 50, 400, new Color(139, 69, 19))); 
        walls.add(new Wall(850, 150, 50, 400, new Color(139, 69, 19))); 

        pockets.add(new Vector2D(150, 150)); 
        pockets.add(new Vector2D(500, 140)); 
        pockets.add(new Vector2D(850, 150)); 
        pockets.add(new Vector2D(150, 550)); 
        pockets.add(new Vector2D(500, 560)); 
        pockets.add(new Vector2D(850, 550)); 

        Color green = new Color(34, 139, 34);
        walls.add(new Wall(190, 150, 270, 15, green)); 
        walls.add(new Wall(540, 150, 270, 15, green)); 
        walls.add(new Wall(190, 535, 270, 15, green)); 
        walls.add(new Wall(540, 535, 270, 15, green)); 
        walls.add(new Wall(150, 190, 15, 320, green)); 
        walls.add(new Wall(835, 190, 15, 320, green)); 

        activeBall = new Ball(300, 350, 12, 1.0, Color.WHITE);
        balls.add(activeBall);

        int startX = 650, startY = 350, r = 12;
        Color[] colors = {Color.YELLOW, Color.BLUE, Color.RED, new Color(128, 0, 128), new Color(255, 140, 0), Color.GREEN, new Color(139, 0, 0), Color.BLACK};
        int colorIdx = 0;
        
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col <= row; col++) {
                double x = startX + (row * r * 1.75);
                double y = startY - (row * r) + (col * r * 2);
                balls.add(new Ball(x, y, r, 1.0, colors[colorIdx++ % colors.length]));
            }
        }
    }

    public void loadBasketballScene() {
        currentScene = Scene.BASKETBALL;
        balls.clear(); walls.clear(); pockets.clear();
        gravity.set(0, 0.4); 
        score = 0;

        // Static Environment
        walls.add(new Wall(0, 600, 1000, 100, new Color(210, 180, 140))); 
        walls.add(new Wall(800, 250, 10, 350, new Color(50, 50, 50))); 
        walls.add(new Wall(790, 200, 10, 150, Color.BLACK)); 

        // The Graphics and Scoring Logic Hoop
        basketballHoop = new Hoop(720, 300, 70, 10);

        // THE FIX: Set the physics colliders to 100% transparent (0,0,0,0)
        balls.add(new Ball(715, 290, 6, Double.POSITIVE_INFINITY, new Color(0, 0, 0, 0))); 
        balls.add(new Ball(795, 290, 6, Double.POSITIVE_INFINITY, new Color(0, 0, 0, 0))); 

        activeBall = new Ball(200, 500, 20, 1.5, new Color(255, 140, 0));
        balls.add(activeBall);
    }

    @Override
    public void run() {
        while (true) {
            updatePhysics();
            repaint();
            try { Thread.sleep(16); } catch (Exception e) {} 
        }
    }

    private void updatePhysics() {
        // ALWAYS run the cloth simulation so the net sways even while aiming
        if (currentScene == Scene.BASKETBALL && basketballHoop != null) {
            basketballHoop.simulateNet(activeBall);
        }

        Iterator<Ball> iter = balls.iterator();
        while (iter.hasNext()) {
            Ball b = iter.next();
            if (isAiming && b == activeBall) continue; 
            
            if (b.mass != Double.POSITIVE_INFINITY) {
                b.vel.add(gravity);
                b.pos.add(b.vel);
                
                if (currentScene == Scene.BILLIARDS) b.vel.mult(0.985); 
                else b.vel.mult(0.995); 

                if (b.vel.mag() > 35) b.vel.mult(35 / b.vel.mag());
            }

            if (currentScene == Scene.BILLIARDS && b.mass != Double.POSITIVE_INFINITY) {
                boolean pocketed = false;
                for (Vector2D p : pockets) {
                    if (Vector2D.sub(b.pos, p).mag() < POCKET_RADIUS) { 
                        if (b == activeBall) {
                            b.pos.set(300, 350);
                            b.vel.set(0, 0);
                        } else {
                            iter.remove(); 
                            score += 10;
                        }
                        pocketed = true;
                        break;
                    }
                }
                if (pocketed) continue; 
            }

            if (currentScene == Scene.BASKETBALL && b == activeBall) {
                if (basketballHoop.checkScore(b)) {
                    score += 1; 
                    b.pos.set(200, 500); 
                    b.vel.set(0, 0);
                }
            }

            int pad = (int)b.radius;
            if (currentScene == Scene.BILLIARDS && b.mass != Double.POSITIVE_INFINITY) {
                boolean nearPocket = false;
                for (Vector2D p : pockets) {
                    if (Vector2D.sub(b.pos, p).mag() < POCKET_RADIUS + 30) {
                        nearPocket = true; break;
                    }
                }
                if (!nearPocket) {
                    int minX = 150 + pad, maxX = 850 - pad; 
                    int minY = 150 + pad, maxY = 550 - pad;
                    if (b.pos.x < minX) { b.pos.x = minX; b.vel.x *= -0.8; }
                    if (b.pos.x > maxX) { b.pos.x = maxX; b.vel.x *= -0.8; }
                    if (b.pos.y < minY) { b.pos.y = minY; b.vel.y *= -0.8; }
                    if (b.pos.y > maxY) { b.pos.y = maxY; b.vel.y *= -0.8; }
                }
            } else if (currentScene == Scene.BASKETBALL) {
                if (b.pos.x < pad) { b.pos.x = pad; b.vel.x *= -0.8; }
                if (b.pos.x > 1000 - pad) { b.pos.x = 1000 - pad; b.vel.x *= -0.8; }
                if (b.pos.y < pad) { b.pos.y = pad; b.vel.y *= -0.8; }
                if (b.pos.y > 700 - pad) { b.pos.y = 700 - pad; b.vel.y *= -0.8; }
            }
        }

        for (Ball b : balls) {
            if (b.mass == Double.POSITIVE_INFINITY) continue;
            for (Wall w : walls) {
                double closestX = Math.max(w.x, Math.min(b.pos.x, w.x + w.width));
                double closestY = Math.max(w.y, Math.min(b.pos.y, w.y + w.height));

                double dx = b.pos.x - closestX;
                double dy = b.pos.y - closestY;
                double distanceSq = dx * dx + dy * dy;

                if (distanceSq < b.radius * b.radius) {
                    double distance = Math.sqrt(distanceSq);
                    if (distance == 0) continue;
                    
                    Vector2D normal = new Vector2D(dx / distance, dy / distance);
                    double overlap = b.radius - distance;
                    
                    b.pos.x += normal.x * overlap;
                    b.pos.y += normal.y * overlap;

                    double dotProduct = (b.vel.x * normal.x) + (b.vel.y * normal.y);
                    if (dotProduct < 0) {
                        double restitution = (currentScene == Scene.BILLIARDS) ? 1.6 : 1.75;
                        b.vel.x -= restitution * dotProduct * normal.x;
                        b.vel.y -= restitution * dotProduct * normal.y;
                    }
                }
            }
        }

        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball b1 = balls.get(i);
                Ball b2 = balls.get(j);

                Vector2D delta = Vector2D.sub(b1.pos, b2.pos);
                double dist = delta.mag();
                double min_dist = b1.radius + b2.radius;

                if (dist < min_dist && dist > 0) {
                    Vector2D normal = new Vector2D(delta.x / dist, delta.y / dist);
                    double overlap = 0.5 * (min_dist - dist);
                    
                    if (b1.mass != Double.POSITIVE_INFINITY) { b1.pos.x += normal.x * overlap; b1.pos.y += normal.y * overlap; }
                    if (b2.mass != Double.POSITIVE_INFINITY) { b2.pos.x -= normal.x * overlap; b2.pos.y -= normal.y * overlap; }

                    Vector2D relVel = Vector2D.sub(b1.vel, b2.vel);
                    double velAlongNormal = (relVel.x * normal.x) + (relVel.y * normal.y);
                    
                    if (velAlongNormal > 0) continue;

                    double restitution = 0.95; 
                    double jImpulse = -(1 + restitution) * velAlongNormal;
                    jImpulse /= (1.0 / b1.mass) + (1.0 / b2.mass);

                    Vector2D impulse = new Vector2D(normal.x * jImpulse, normal.y * jImpulse);
                    
                    if (b1.mass != Double.POSITIVE_INFINITY) { b1.vel.x += impulse.x / b1.mass; b1.vel.y += impulse.y / b1.mass; }
                    if (b2.mass != Double.POSITIVE_INFINITY) { b2.vel.x -= impulse.x / b2.mass; b2.vel.y -= impulse.y / b2.mass; }
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        if (currentScene == Scene.BILLIARDS) {
            g2d.setColor(new Color(46, 139, 87)); 
            g2d.fillRect(150, 150, 700, 400);

            g2d.setColor(Color.BLACK);
            for (Vector2D p : pockets) {
                g2d.fillOval((int)p.x - POCKET_RADIUS, (int)p.y - POCKET_RADIUS, POCKET_RADIUS * 2, POCKET_RADIUS * 2);
            }
        } else if (currentScene == Scene.BASKETBALL && basketballHoop != null) {
            basketballHoop.draw(g2d); 
        }

        for (Wall w : walls) {
            g2d.setColor(w.color);
            g2d.fillRect((int)w.x, (int)w.y, (int)w.width, (int)w.height);
            g2d.setColor(Color.BLACK);
            g2d.drawRect((int)w.x, (int)w.y, (int)w.width, (int)w.height); 
        }

        for (Ball b : balls) {
            g2d.setColor(b.color);
            g2d.fillOval((int)(b.pos.x - b.radius), (int)(b.pos.y - b.radius), (int)b.radius * 2, (int)b.radius * 2);
            
            if (b.mass != Double.POSITIVE_INFINITY) {
                g2d.setColor(Color.BLACK);
                g2d.drawOval((int)(b.pos.x - b.radius), (int)(b.pos.y - b.radius), (int)b.radius * 2, (int)b.radius * 2);
                
                if (currentScene == Scene.BILLIARDS && b.color != Color.WHITE) {
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval((int)(b.pos.x - b.radius/2), (int)(b.pos.y - b.radius/2), 6, 6);
                }
            }
        }

        if (isAiming && activeBall != null) {
            double dx = activeBall.pos.x - aimX;
            double dy = activeBall.pos.y - aimY;

            Vector2D launchVel = new Vector2D(dx * 0.15, dy * 0.15);
            if (launchVel.mag() > 30) launchVel.mult(30 / launchVel.mag());

            if (currentScene == Scene.BASKETBALL) {
                g2d.setColor(new Color(200, 50, 50, 150));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine((int)activeBall.pos.x, (int)activeBall.pos.y, (int)aimX, (int)aimY);
                
                g2d.setColor(new Color(100, 100, 100, 150));
                double simX = activeBall.pos.x;
                double simY = activeBall.pos.y;
                Vector2D simVel = new Vector2D(launchVel.x, launchVel.y);
                
                for (int i = 0; i < 120; i++) { 
                    simVel.add(gravity);
                    simX += simVel.x;
                    simY += simVel.y;
                    
                    simVel.mult(0.995); 
                    if (simVel.mag() > 35) simVel.mult(35 / simVel.mag()); 
                    
                    if (i % 3 == 0) g2d.fillOval((int)simX - 3, (int)simY - 3, 6, 6); 
                    if (simY > 600) break; 
                }

            } else if (currentScene == Scene.BILLIARDS) {
                g2d.setColor(new Color(139, 69, 19));
                g2d.setStroke(new BasicStroke(6));
                g2d.drawLine((int)activeBall.pos.x - (int)(dx * 0.2), (int)activeBall.pos.y - (int)(dy * 0.2), 
                             (int)aimX, (int)aimY);
                
                g2d.setStroke(new BasicStroke(1));
                if (launchVel.mag() > 0.5) {
                    Vector2D stepVel = new Vector2D(launchVel.x, launchVel.y);
                    stepVel.mult(1.0 / stepVel.mag()); 
                    
                    Vector2D ghostPos = new Vector2D(activeBall.pos.x, activeBall.pos.y);
                    Ball hitBall = null;
                    
                    for (int k = 0; k < 600; k++) {
                        ghostPos.add(stepVel);
                        if (ghostPos.x <= 165 || ghostPos.x >= 835 || ghostPos.y <= 165 || ghostPos.y >= 535) break;

                        for (Ball b : balls) {
                            if (b == activeBall || b.mass == Double.POSITIVE_INFINITY) continue; 
                            if (Vector2D.sub(ghostPos, b.pos).mag() <= activeBall.radius + b.radius) {
                                hitBall = b;
                                break;
                            }
                        }
                        if (hitBall != null) break;
                    }
                    
                    g2d.setColor(Color.WHITE);
                    g2d.drawLine((int)activeBall.pos.x, (int)activeBall.pos.y, (int)ghostPos.x, (int)ghostPos.y);
                    
                    if (hitBall != null) {
                        g2d.drawOval((int)ghostPos.x - (int)activeBall.radius, (int)ghostPos.y - (int)activeBall.radius, 
                                     (int)activeBall.radius*2, (int)activeBall.radius*2);
                        
                        Vector2D targetDir = Vector2D.sub(hitBall.pos, ghostPos);
                        targetDir.mult(1.0 / targetDir.mag());
                        g2d.setColor(Color.RED);
                        g2d.drawLine((int)hitBall.pos.x, (int)hitBall.pos.y, 
                                     (int)(hitBall.pos.x + targetDir.x * 60), (int)(hitBall.pos.y + targetDir.y * 60));
                        
                        Vector2D cueDir = new Vector2D(-targetDir.y, targetDir.x);
                        if (cueDir.x * stepVel.x + cueDir.y * stepVel.y < 0) cueDir.set(targetDir.y, -targetDir.x); 
                        
                        g2d.setColor(Color.WHITE);
                        g2d.drawLine((int)ghostPos.x, (int)ghostPos.y, 
                                     (int)(ghostPos.x + cueDir.x * 60), (int)(ghostPos.y + cueDir.y * 60));
                    }
                }
            }
        }

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 24));
        g2d.drawString("Score: " + score, 20, 40);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g2d.drawString(String.format("Power: %.2f", power), 20, 70);
        g2d.drawString(String.format("Angle: %.2f", angle), 20, 90);

        g2d.setColor(new Color(220, 220, 220));
        g2d.fillRect(btnBilliards.x, btnBilliards.y, btnBilliards.width, btnBilliards.height);
        g2d.fillRect(btnBasketball.x, btnBasketball.y, btnBasketball.width, btnBasketball.height);
        g2d.fillRect(btnReset.x, btnReset.y, btnReset.width, btnReset.height); 
        g2d.fillRect(btnMenu.x, btnMenu.y, btnMenu.width, btnMenu.height); 

        g2d.setColor(Color.BLACK);
        g2d.drawRect(btnBilliards.x, btnBilliards.y, btnBilliards.width, btnBilliards.height);
        g2d.drawRect(btnBasketball.x, btnBasketball.y, btnBasketball.width, btnBasketball.height);
        g2d.drawRect(btnReset.x, btnReset.y, btnReset.width, btnReset.height); 
        g2d.drawRect(btnMenu.x, btnMenu.y, btnMenu.width, btnMenu.height); 
        
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.drawString("Billiards", btnBilliards.x + 45, btnBilliards.y + 25);
        g2d.drawString("Basketball", btnBasketball.x + 35, btnBasketball.y + 25);
        g2d.drawString("Restart Scene", btnReset.x + 25, btnReset.y + 25); 
        g2d.drawString("Main Menu", btnMenu.x + 35, btnMenu.y + 25); 
    }
}
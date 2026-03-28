import java.awt.Color;

public class Ball {
    public Vector2D pos, vel;
    public double radius, mass;
    public Color color;

    public Ball(double x, double y, double r, double m, Color c) {
        this.pos = new Vector2D(x, y);
        this.vel = new Vector2D(0, 0);
        this.radius = r;
        this.mass = m;
        this.color = c;
    }
}
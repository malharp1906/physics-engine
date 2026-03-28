import java.awt.Color;

public class Wall {
    public double x, y, width, height;
    public Color color;

    public Wall(double x, double y, double w, double h, Color c) {
        this.x = x; 
        this.y = y; 
        this.width = w; 
        this.height = h;
        this.color = c;
    }
}
public class Vector2D {
    public double x, y;

    public Vector2D(double x, double y) { 
        this.x = x; 
        this.y = y; 
    }

    public void set(double x, double y) { 
        this.x = x; 
        this.y = y; 
    }

    public void add(Vector2D v) { 
        this.x += v.x; 
        this.y += v.y; 
    }

    public void mult(double scalar) { 
        this.x *= scalar; 
        this.y *= scalar; 
    }

    public double mag() { 
        return Math.sqrt(x * x + y * y); 
    }

    public static Vector2D sub(Vector2D v1, Vector2D v2) { 
        return new Vector2D(v1.x - v2.x, v1.y - v2.y); 
    }
}
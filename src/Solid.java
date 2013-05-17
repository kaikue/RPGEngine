import java.awt.*;
import java.awt.Rectangle;


public abstract class Solid {
    
    int x, y;
    Rectangle boundingBox = new Rectangle();
    Image image;
    
    public Solid(int x, int y, Rectangle boundingBox) {
        this.x = x;
        this.y = y;
        this.boundingBox = boundingBox;
    }
    
    public void draw(Graphics g) {
        g.drawImage(image, x, y, null);
    }
    
    public void update() {
        ;
    }

}

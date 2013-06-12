import java.awt.*;


public class Overlay {
    
    int x, y;
    Image image;
    
    public Overlay(Image image, int x, int y) {
        this.image = image;
        this.x = x;
        this.y = y;
    }
    
    public void draw(Graphics g, int viewX, int viewY) {    
        g.drawImage(image, x + viewX, y + viewY, null);
    }

}

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;


public class Enemy extends Actor {

    Image image;
    
    public Enemy(Image image, int x, int y, Rectangle boundingBox, int health) {
        super(x, y, boundingBox, health);
        this.image = image;
    }
    public void draw(Graphics g) {
        
        g.drawImage(image, x, y, null);
        
    }
}

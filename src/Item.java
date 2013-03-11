import java.awt.*;


public class Item extends Solid {
    
    Image image;
    boolean isWeapon = false;
    
    public Item(Image image, int x, int y, Rectangle boundingBox) {
        super(x, y, boundingBox);
        this.image = image;
    }
    public void draw(Graphics g) {
        
        g.drawImage(image, x, y, null);
        
    }
    
}

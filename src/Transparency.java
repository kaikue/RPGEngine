import java.awt.Graphics;
import java.awt.Rectangle;

public class Transparency extends Scenery {
    
    Rectangle drawSpace;
    
    public Transparency(Rectangle drawSpace, Rectangle boundingBox) {
        super(null, drawSpace.x, drawSpace.y, boundingBox);
        this.drawSpace = drawSpace;
    }
    
    public void draw(Graphics g, RPG rpg) {
        //move the capture to -pos.x, -pos.y, then draw a clipped rectangle
        int realX = rpg.pos.x + x;
        int realY = rpg.pos.y + y;
        g.drawImage(rpg.capture, x, y, x + drawSpace.width, y + drawSpace.height, realX, realY, realX + drawSpace.width, realY + drawSpace.height, null);
    }

}
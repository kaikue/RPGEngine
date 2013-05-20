import java.awt.Rectangle;

public class Transparency extends Scenery {
    
    Rectangle drawSpace;
    
    public Transparency(Rectangle drawSpace, Rectangle boundingBox) {
        super(null, drawSpace.x, drawSpace.y, boundingBox);
        this.drawSpace = drawSpace;
    }
}
import java.awt.*;

public class Scenery extends Solid {
    
    public Scenery(Image image, int x, int y, Rectangle boundingBox) {
        super(x, y, boundingBox);
        this.image = image;
    }

}

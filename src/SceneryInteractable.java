import java.awt.Image;
import java.awt.Rectangle;


public class SceneryInteractable extends Scenery {
    
    String message;
    MessageOverlay overlay;
    
    public SceneryInteractable(Image image, int x, int y, Rectangle boundingBox, MessageOverlay overlay) {
        super(image, x, y, boundingBox);
        this.overlay = overlay;
    }

}

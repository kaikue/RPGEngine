import java.awt.Image;
import java.awt.Rectangle;


public class LevelWarper extends Scenery {
    
    String effect;
    boolean keepInventory;
    
    public LevelWarper(Image image, int x, int y, Rectangle boundingBox, String effect, boolean keepInventory) {
        super(image, x, y, boundingBox);
        this.effect = effect;
        this.keepInventory = keepInventory;
    }
    
    public void warp(RPG rpg) {
        if(effect.equals("nextLevel")) {
            rpg.nextLevel(keepInventory);
        }
        else if(effect.equals("previousLevel")) {
            rpg.previousLevel(keepInventory);
        }
    }
}

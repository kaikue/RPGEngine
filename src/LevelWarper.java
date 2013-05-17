import java.awt.Image;
import java.awt.Rectangle;


public class LevelWarper extends Scenery {
    
    String effect;
    
    public LevelWarper(Image image, int x, int y, Rectangle boundingBox, String effect) {
        super(image, x, y, boundingBox);
        this.effect = effect;
    }
    
    public void warp(RPG rpg) {
        if(effect.equals("nextLevel")) {
            rpg.nextLevel();
        }
        else if(effect.equals("previousLevel")) {
            rpg.previousLevel();
        }
    }
}

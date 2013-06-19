import java.awt.Image;
import java.awt.Rectangle;

public class LevelWarper extends Scenery {
    
    String effect;
    boolean keepInventory;
    int spawnX, spawnY;
    
    public LevelWarper(Image image, int x, int y, Rectangle boundingBox, String effect, boolean keepInventory) {
        super(image, x, y, boundingBox);
        this.effect = effect;
        this.keepInventory = keepInventory;
        this.spawnX = -1;
        this.spawnY = -1;
    }
    
    public LevelWarper(Image image, int x, int y, Rectangle boundingBox, String effect, boolean keepInventory, int spawnX, int spawnY) {
        this(image, x, y, boundingBox, effect, keepInventory);
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }
    
    public void warp(RPG rpg) {
        if(effect.equals("nextLevel")) {
            rpg.nextLevel(keepInventory);
            if(spawnX != -1 && spawnY != -1) {
                rpg.player.boundingBox.x = spawnX;
                rpg.player.boundingBox.y = spawnY;
                rpg.player.x = rpg.player.boundingBox.x + rpg.player.boundingBoxOffsetX;
                rpg.player.y = rpg.player.boundingBox.y + rpg.player.boundingBoxOffsetY;
            }
        }
        else if(effect.equals("previousLevel")) {
            rpg.previousLevel(keepInventory);
            if(spawnX != -1 && spawnY != -1) {
                rpg.player.x = spawnX;
                rpg.player.y = spawnY;
                rpg.player.boundingBox.x = rpg.player.x + rpg.player.boundingBoxOffsetX;
                rpg.player.boundingBox.y = rpg.player.y + rpg.player.boundingBoxOffsetY;
            }
        }
    }
}

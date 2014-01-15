import java.awt.*;
import java.util.ArrayList;
import javax.swing.ImageIcon;


public class Player extends Actor {
    
    int width, height;
    boolean up, down, left, right;
    Image imageSheet;
    Image imgUp, imgDown, imgLeft, imgRight;
    Image animUp1, animUp2, animDown1, animDown2, animLeft1, animLeft2, animRight1, animRight2;
    int imageSheetOffsetX, imageSheetOffsetY, imageSheetRows, imageSheetColumns;
    boolean animated, frame;
    ArrayList<Item> inventory = new ArrayList<Item>();
    int inventorySlot;
    Item inventoryItem;
    boolean canShoot;
    
    public Player(Image imageSheet, int x, int y, int width, int height, int imageSheetOffsetX, int imageSheetOffsetY, int health, int speed) {
        super(null, x, y, new Rectangle(x, y + height * 3 / 4, width, height / 4), health, speed);
        this.width = width;
        this.height = height;
        this.up = false;
        this.down = false;
        this.left = false;
        this.right = false;
        this.imageSheet = imageSheet;
        this.imageSheetOffsetX = imageSheetOffsetX;
        this.imageSheetOffsetY = imageSheetOffsetY;
        ImageIcon i = new ImageIcon(this.imageSheet);
        this.imageSheetRows = i.getIconWidth() / (this.width + this.imageSheetOffsetX);
        this.imageSheetColumns = i.getIconHeight() / (this.height + this.imageSheetOffsetY);
        
        //get images from spritesheet
        this.imgUp = splitImage(1);
        this.imgDown = splitImage(7);
        this.imgLeft = splitImage(10);
        this.imgRight = splitImage(4);
        this.animUp1 = splitImage(0);
        this.animUp2 = splitImage(2);
        this.animDown1 = splitImage(6);
        this.animDown2 = splitImage(8);
        this.animLeft1 = splitImage(9);
        this.animLeft2 = splitImage(11);
        this.animRight1 = splitImage(3);
        this.animRight2 = splitImage(5);
        this.image = null;
        
        this.animated = false;
        this.frame = false;
        this.inventorySlot = 0;
        this.inventoryItem = null;
        this.canShoot = true;
    }
    
    @Override
    public void move(RPG rpg) {
        //find the player's next position
        int nextX = boundingBox.x;
        int nextY = boundingBox.y;
        if(up) {
            animated = true;
            nextY -= speed;
        }
        if(down) {
            animated = true;
            nextY += speed;
        }
        if(left) {
            animated = true;
            nextX -= speed;
        }
        if(right) {
            animated = true;
            nextX += speed;
        }
        if(!up && !down && !left && !right) {
            animated = false;
        }
        checkCollisions(nextX, nextY, rpg, true);
    }
    
    @Override
    public int collide(int next, int current, RPG rpg) {
        for(Solid solid : rpg.currentLevel.allSolids) {
            if(solid.equals(this)) {
                continue;
            }
            if(boundingBox.intersects(solid.boundingBox)) {
                if(solid instanceof Scenery) {
                    if(solid instanceof LevelWarper) {
                        ((LevelWarper)solid).warp(rpg);
                        break;
                    }
                    return current;
                }
                else if(solid instanceof Item) {
                    collect((Item)solid, rpg);
                    break;
                }
            }
        }
        return next;
    }
    
    public void collect(Item item, RPG rpg) {
        inventory.add(item);
        rpg.currentLevel.allSolids.remove(item);
        updateHeldItem();
        //maybe play a sound?
    }
    
    public void updateHeldItem() {
        if(inventorySlot < inventory.size()) {
            inventoryItem = inventory.get(inventorySlot);
        }
        else {
            inventoryItem = null;
        }
    }
    
    public Image splitImage(int imgIndex) {
        return RPGUtils.splitImage(imageSheet, imageSheetRows, imageSheetColumns, width + imageSheetOffsetX, height + imageSheetOffsetY, imgIndex);
    }
    
    @Override
    public void kill(RPG rpg) {
        rpg.quit();
    }
}

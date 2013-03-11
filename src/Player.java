import java.awt.*;
import java.util.ArrayList;

import javax.swing.ImageIcon;


public class Player extends Actor {

    int nextX, nextY;
    int width, height;
    int boundingBoxOffsetX, boundingBoxOffsetY;
    boolean up, down, left, right;
    int speed;
    Image imageSheet;
    Image imgUp, imgDown, imgLeft, imgRight;
    Image animUp1, animUp2, animDown1, animDown2, animLeft1, animLeft2, animRight1, animRight2;
    int imageSheetOffsetX, imageSheetOffsetY, imageSheetRows, imageSheetColumns;
    boolean animated, frame;
    ArrayList<Item> inventory = new ArrayList<Item>();
    int inventorySlot;
    Item inventoryItem;
    boolean canShoot;
    long lastShot;
    
    public Player(Image imageSheet, int x, int y, int width, int height, int speed, int imageSheetOffsetX, int imageSheetOffsetY, int health) {
        super(x, y, new Rectangle(x, y + height * 3 / 4, width, height / 4), health);
        this.width = width;
        this.height = height; 
        this.boundingBoxOffsetX = boundingBox.x - x;
        this.boundingBoxOffsetY = boundingBox.y - y;
        this.up = false;
        this.down = false;
        this.left = false;
        this.right = false;
        this.speed = speed;
        this.dir = "down";
        this.imageSheet = imageSheet;
        this.imageSheetOffsetX = imageSheetOffsetX;
        this.imageSheetOffsetY = imageSheetOffsetY;
        ImageIcon i = new ImageIcon(this.imageSheet);
        this.imageSheetRows = i.getIconWidth() / (this.width + this.imageSheetOffsetX);
        this.imageSheetColumns = i.getIconHeight() / (this.height + this.imageSheetOffsetY);
        this.attack = null;
        
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
        this.lastShot = 0;
    }
    
    public void move(ArrayList<Solid> solids, RPG rpg) {
        //find the player's next position
        nextX = boundingBox.x;
        nextY = boundingBox.y;
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
        
        //set the player's direction based on x and y change (x takes precedence)
        if(nextX - this.boundingBox.x > 0) {
            dir = "right";
        }
        else if(nextX - this.boundingBox.x < 0) {
            dir = "left";
        }
        else if(nextY - this.boundingBox.y > 0) {
            dir = "down";
        }
        else if(nextY - this.boundingBox.y < 0) {
            dir = "up";
        }
        
        //check if there are any collisions that will happen
        int currentX = boundingBox.x;
        int currentY = boundingBox.y;
        boundingBox.setLocation(nextX, currentY);
        for(Solid solid : solids) {
            if(boundingBox.intersects(solid.boundingBox)) {
                if(solid instanceof Scenery) {
                    if(solid instanceof LevelWarper) {
                        ((LevelWarper)solid).warp(rpg);
                        break;
                    }
                    nextX = currentX;
                    break;
                }
                else if(solid instanceof Item) {
                    collect((Item)solid);
                    break;
                }
            }
        }
        boundingBox.setLocation(currentX, nextY);
        for(Solid solid : solids) {
            if(boundingBox.intersects(solid.boundingBox)) {
                if(solid instanceof Scenery) {
                    if(solid instanceof LevelWarper) {
                        ((LevelWarper)solid).warp(rpg);
                        break;
                    }
                    nextY = currentY;
                    break;
                }
                else if(solid instanceof Item) {
                    collect((Item)solid);
                    break;
                }
            }
        }
        
        //move the player's bounding box
        boundingBox.setLocation(nextX, nextY);
        
        //move the player
        x = boundingBox.x - boundingBoxOffsetX;
        y = boundingBox.y - boundingBoxOffsetY;
        
        //select the inventory item, kind of a hack to put in here but whatever #YOLO
        try {
            inventoryItem = inventory.get(inventorySlot);
        }
        catch(IndexOutOfBoundsException e) {
            inventoryItem = null;
        }
        if(inventoryItem instanceof Weapon) {
            attack = ((Weapon)inventoryItem).attack;
            attack.creator = this;
        }
        else {
            attack = null;
        }
    }
    
    public void collect(Item item) {
        inventory.add(item);
        RPG.currentLevel.allSolids.remove(item);
        //maybe play a sound?
    }
    
    public Image splitImage(int imgIndex) {
        return RPG.splitImage(imageSheet, imageSheetRows, imageSheetColumns, width + imageSheetOffsetX, height + imageSheetOffsetY, imgIndex);
    }
    
    public Image getAnim(String dir, RPG rpg) {
        frame = rpg.animate(frame, 5); //every 5 steps, advance the animation
        if(dir.equals("up")) {
            if(frame) {
                return animUp1;
            }
            return animUp2;
        }
        if(dir.equals("down")) {
            if(frame) {
                return animDown1;
            }
            return animDown2;
        }
        if(dir.equals("left")) {
            if(frame) {
                return animLeft1;
            }
            return animLeft2;
        }
        if(dir.equals("right")) {
            if(frame) {
                return animRight1;
            }
            return animRight2;
        }
        return null;
    }
    
    public void draw(Graphics g, RPG rpg) {
        
        if(dir.equals("up")) {
            if(up) {
                //animated! (animPlayerUp)
                image = getAnim(dir, rpg);
            }
            else {
                //not animated. (imgPlayerUp)
                image = imgUp;
            }
        }
        else if(dir.equals("down")) {
            if(down) {
                //animated! (animPlayerUp)
                image = getAnim(dir, rpg);
            }
            else {
                //not animated. (imgPlayerUp)
                image = imgDown;
            }
        }
        else if(dir.equals("left")) {
            if(left) {
                //animated! (animPlayerUp)
                image = getAnim(dir, rpg);
            }
            else {
                //not animated. (imgPlayerUp)
                image = imgLeft;
            }
        }
        else if(dir.equals("right")) {
            if(right) {
                //animated! (animPlayerUp)
                image = getAnim(dir, rpg);
            }
            else {
                //not animated. (imgPlayerUp)
                image = imgRight;
            }
        }
        
        //g.drawString(dir, 0, 10);
        g.drawImage(image, x, y, null);
        
    }

}
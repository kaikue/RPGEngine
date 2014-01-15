import java.awt.Image;
import java.awt.Rectangle;


public class Actor extends Scenery {

    int maxHealth, health;
    int boundingBoxOffsetX, boundingBoxOffsetY;
    int speed;
    String dir;
    long lastShot;
    int[] knockback;
    
    public Actor(Image image, int x, int y, Rectangle boundingBox, int health, int speed) {
        super(image, x, y, boundingBox);
        this.maxHealth = health;
        this.health = health;
        this.speed = speed;
        boundingBoxOffsetX = boundingBox.x - x;
        boundingBoxOffsetY = boundingBox.y - y;
        dir = "down";
        lastShot = 0;
        knockback = new int[2];
    }
    
    public double distanceTo(Solid s) {
        //the distance from the solid's center to the player's center
        int sX = s.boundingBox.x + s.boundingBox.width / 2;
        int sY = s.boundingBox.y + s.boundingBox.height / 2;
        int pX = boundingBox.x + boundingBox.width / 2;
        int pY = boundingBox.y + boundingBox.height / 2;
        
        return Math.sqrt(Math.pow(sX - pX, 2) + Math.pow(sY - pY, 2));
    }
    
    public boolean killMe() {
        return health <= 0;
    }
    
    public void move(RPG rpg) {
        
    }
    
    public void checkCollisions(int nextX, int nextY, RPG rpg, boolean setDir) {
        if(setDir) {
            //set the direction based on x and y change (x takes precedence)
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
        }
        //apply knockback
        if(knockback[0] > 0) {
            nextX += knockback[0]--;
        }
        else if(knockback[0] < 0) {
            nextX += knockback[0]++;
        }
        if(knockback[1] > 0) {
            nextY += knockback[1]--;
        }
        else if(knockback[1] < 0) {
            nextY += knockback[1]++;
        }
        
        //check if there are any collisions that will happen, and if so, reset the x or y
        int currentX = boundingBox.x;
        int currentY = boundingBox.y;
        boundingBox.setLocation(nextX, currentY);
        nextX = collide(nextX, currentX, rpg);
        
        boundingBox.setLocation(currentX, nextY);
        nextY = collide(nextY, currentY, rpg);
        
        //move the player's bounding box
        boundingBox.setLocation(nextX, nextY);
        
        //move the player
        x = boundingBox.x - boundingBoxOffsetX;
        y = boundingBox.y - boundingBoxOffsetY;
    }
    
    public int collide(int next, int current, RPG rpg) {
        for(Solid solid : rpg.currentLevel.allSolids) {
            if(solid.equals(this)) {
                continue;
            }
            if(boundingBox.intersects(solid.boundingBox)) {
                return current;
            }
        }
        return next;
    }
    
    public void attack(RPG rpg) {
        //use the player's current item (only usable items are weapons right now)
        Weapon weapon = null;
        if(this.equals(rpg.player)) {
            if(rpg.player.inventoryItem instanceof Weapon) {
                weapon = (Weapon)rpg.player.inventoryItem;
            }
            else if(rpg.player.inventoryItem != null) {
                rpg.player.inventoryItem.use(rpg.player);
                return;
            }
            else {
                return;
            }
        }
        else if(this instanceof Enemy) {
            weapon = ((Enemy)this).weapon;
        }
        if(rpg.time - lastShot > weapon.rate) {
            Attack attack = weapon.fire(this);
            rpg.currentLevel.allSolids.add(attack);
            attack.checkCollision(rpg.currentLevel.allSolids);
            lastShot = rpg.time;
        }
    }
    
    public void update(RPG rpg) {
        move(rpg);
    }
    
}

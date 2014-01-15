import java.awt.Image;
import java.awt.Rectangle;


public class Enemy extends Actor {
    
    String type;
    Weapon weapon;
    //double angle; //only for collide-spiral- that should really be its own class
    
    public Enemy(Image image, int x, int y, Rectangle boundingBox, int health, int speed, String type, Weapon weapon) {
        super(image, x, y, boundingBox, health, speed);
        this.type = type;
        this.weapon = weapon;
        //replace 50 with player dimensions
        
        //attackDistance = Math.max(boundingBox.width / 2, boundingBox.height / 2) + 50;
        //angle = 0;
    }
    
    @Override
    public void move(RPG rpg) {
        //only move if on screen (or close to it)
        int activationDistance = (rpg.appWidth + rpg.appHeight) / 2;
        if(distanceTo(rpg.player) > activationDistance) {
            System.out.println("returning");
            return;
        }
        
        if(type.equals("collide-linear")) {
            weapon.attack.position(this);
            Rectangle collider = new Rectangle(weapon.attack.boundingBox);
            if(collider.intersects(rpg.player.boundingBox)) {
                attack(rpg);
            }
            else {
                //move straight towards player
                //thanks to risingstar64 for code help
                int eX = boundingBox.x + boundingBox.width / 2;
                int eY = boundingBox.y + boundingBox.height / 2;
                int pX = rpg.player.boundingBox.x + rpg.player.boundingBox.width / 2;
                int pY = rpg.player.boundingBox.y + rpg.player.boundingBox.height / 2;
                double angle = Math.atan2(-(eY - pY), eX - pX);
                int nextX = (int)(x - Math.cos(angle) * speed);
                int nextY = (int)(y + Math.sin(angle) * speed);
                checkCollisions(nextX, nextY, rpg, true);
            }
        }
        /*else if(type.equals("collide-spiral")) {
            //this doesn't work right
            if(distanceTo(rpg.player) > 10) {
                //spiral towards player
                int pX = rpg.player.boundingBox.x + rpg.player.boundingBox.width / 2;
                int pY = rpg.player.boundingBox.y + rpg.player.boundingBox.height / 2;
                //if(rad > 1) {
                //    rad -= speed / 4;
                //}
                double rad = distanceTo(rpg.player);
                System.out.println(rad);
                int nextX = (int)(pX + Math.sin(angle) * rad) - boundingBox.width / 2;
                int nextY = (int)(pY - Math.cos(angle) * rad) - boundingBox.height / 2;
                checkCollisions(nextX, nextY, rpg);
                if(x == nextX && y == nextY) {
                    angle += 0.01 * speed;
                }
            }
            else {
                //attack
                
            }
        }*/
        else if(type.equals("shoot-axis")) {
            //move towards closest axis then shoot
            //maybe approach after finding axis?
            int eX = boundingBox.x + boundingBox.width / 2;
            int eY = boundingBox.y + boundingBox.height / 2;
            int pX = rpg.player.boundingBox.x + rpg.player.boundingBox.width / 2;
            int pY = rpg.player.boundingBox.y + rpg.player.boundingBox.height / 2;
            int xDiff = pX - eX;
            int yDiff = pY - eY;
            int nextX = x;
            int nextY = y;
            if(Math.abs(xDiff) < speed || Math.abs(yDiff) < speed) {
                //within some tolerance
                attack(rpg);
                System.out.println("attack");
            }
            if(Math.abs(xDiff) < Math.abs(yDiff)) {
                //move towards player x
                if(xDiff < 0) {
                    //enemy x is bigger- move left
                    nextX -= speed;
                }
                else {
                    nextX += speed;
                }
                if(yDiff < 0) {
                    dir = "up";
                }
                else {
                    dir = "down";
                }
            }
            else if(Math.abs(yDiff) < Math.abs(xDiff)) {
                //move towards player y
                if(yDiff < 0) {
                    nextY -= speed;
                }
                else {
                    nextY += speed;
                }
                if(xDiff < 0) {
                    dir = "left";
                }
                else {
                    dir = "right";
                }
            }
            checkCollisions(nextX, nextY, rpg, false);
        }
    }
}

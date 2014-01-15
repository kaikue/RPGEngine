import java.awt.Image;
import java.awt.Rectangle;

public class Weapon extends Item {
    
    Attack attack;
    int rate; //how many frames must pass before the weapon can fire again
    //boolean rotated = false; //whether the attack has obtained rotated images
    
    public Weapon(Image image, int x, int y, Rectangle boundingBox, Attack attack, int rate) {
        super(image, x, y, boundingBox);
        this.attack = attack;
        this.rate = rate;
    }
    
    public Attack fire(Actor actor) {
        
        if(actor.dir.equals("left")) {
            attack.velocity = Attack.LEFT;
        }
        else if(actor.dir.equals("right")) {
            attack.velocity = Attack.RIGHT;
        }
        else if(actor.dir.equals("up")) {
            attack.velocity = Attack.UP;
        }
        else if(actor.dir.equals("down")) {
            attack.velocity = Attack.DOWN;
        }
        
        attack.position(actor);
        attack.age = attack.maxAge;
        attack.creator = actor;
        return attack;
        //actor.attack = attack;
        //Attack newA = new Attack(attack.image, attack.x, attack.y, new Rectangle(attack.boundingBox.x, attack.boundingBox.y, attack.boundingBox.width, attack.boundingBox.height), attackVelocity, attack.speed, attack.age, attack.damage, actor);
        //newA.rotateImage();
        //return newA;
    }
}

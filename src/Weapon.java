import java.awt.Image;
import java.awt.Rectangle;


public class Weapon extends Item {
    
    Attack attack;
    int rate; //how many frames must pass before the weapon can fire again
    
    public Weapon(Image image, int x, int y, Rectangle boundingBox, Attack attack, int rate) {
        super(image, x, y, boundingBox);
        this.attack = attack;
        this.rate = rate;
        this.isWeapon = true;
    }
    
    public void fire(Actor actor) {
        int[] attackVelocity = new int[2];
        if(actor.dir.equals("left")) {
            attackVelocity[0] = -1;
            attackVelocity[1] = 0;
        }
        else if(actor.dir.equals("right")) {
            attackVelocity[0] = 1;
            attackVelocity[1] = 0;
        }
        else if(actor.dir.equals("up")) {
            attackVelocity[0] = 0;
            attackVelocity[1] = -1;
        }
        else if(actor.dir.equals("down")) {
            attackVelocity[0] = 0;
            attackVelocity[1] = 1;
        }
        actor.attack = new Attack(this.attack.image, actor.x, actor.y, new Rectangle(this.attack.boundingBox.x + actor.x, this.attack.boundingBox.y + actor.y, this.attack.boundingBox.width, this.attack.boundingBox.height), attackVelocity, this.attack.speed, this.attack.damage, actor);
    }

}

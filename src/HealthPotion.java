import java.awt.Image;
import java.awt.Rectangle;


public class HealthPotion extends Item {
    
    int health;
    
    public HealthPotion(Image image, int x, int y, Rectangle boundingBox, int health) {
        super(image, x, y, boundingBox);
        this.health = health;
    }
    
    @Override
    public void use(Actor actor) {
        actor.health += health;
        if(actor.health > actor.maxHealth) {
            actor.health = actor.maxHealth;
        }
        if(actor instanceof Player) {
            ((Player)actor).inventory.remove(this);
            ((Player)actor).updateHeldItem();
        }
    }
}

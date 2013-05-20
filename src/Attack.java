import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;

public class Attack extends Solid {
    
    static ArrayList<Attack> allAttacks = new ArrayList<Attack>();
    int[] velocity;
    int speed;
    int age;
    int damage;
    Solid creator;
    
    public Attack(Image image, int x, int y, Rectangle boundingBox, int[] velocity, int speed, int age, int damage, Actor creator) {
        super(x, y, boundingBox);
        this.image = image;
        this.velocity = velocity;
        this.speed = speed;
        this.age = age;
        this.damage = damage;
        this.creator = creator;
        Attack.allAttacks.add(this);
    }
    
    public void update() {
        if(age == 0) {
            Attack.allAttacks.remove(this);
            RPG.currentLevel.allSolids.remove(this);
        }
        if(age > 0) {
            age--;
        }
        x += velocity[0] * speed;
        y += velocity[1] * speed;
        boundingBox.x += velocity[0] * speed;
        boundingBox.y += velocity[1] * speed;
        if(speed == 0) {
            //move with player
        }
    }

}

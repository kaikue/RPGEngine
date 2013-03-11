import java.awt.Rectangle;


public class Actor extends Solid{

    int maxHealth, health;
    String dir;
    Attack attack;
    
    public Actor(int x, int y, Rectangle boundingBox, int health) {
        super(x, y, boundingBox);
        this.maxHealth = health;
        this.health = health;
        this.attack = null;
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
    
}

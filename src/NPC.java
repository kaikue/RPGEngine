import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.ImageIcon;


public class NPC extends SceneryInteractable {

    public NPC(Image image, int x, int y, MessageOverlay overlay) {
        super(image, x, y, new Rectangle(x, y + new ImageIcon(image).getIconHeight() * 3 / 4, new ImageIcon(image).getIconWidth(), new ImageIcon(image).getIconHeight() / 4), overlay);
        //add more things if necessary (more dialogue, movement)
    }

}

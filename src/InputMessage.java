import java.awt.Image;


public class InputMessage extends MessageOverlay {

    String code;
    String effect;
    
    public InputMessage(Image overlayImage, Image speakerImage, int x, int y, String message, String code, String effect) {
        super(overlayImage, speakerImage, x, y, message, null);
        this.code = code;
        this.effect = effect;
    }
    
    public void advance(RPG rpg) {
        rpg.getInput(code, effect);
    }

}

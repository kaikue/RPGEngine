import java.applet.Applet;


public class Launcher extends Applet {
    private static final long serialVersionUID = 1L;
    private RPG rpg;

    public void init(){
        rpg = new RPG();
        rpg.setSize(800, 600);
        //rpg.setResizable(false);
        //rpg.setLocationRelativeTo(null);
    }
    
    public void start(){
        rpg.setVisible(true);
    }

    public void stop(){
        rpg.setVisible(false);
    }

    public void destroy(){
        //rpg.dispose();
    }
    
}

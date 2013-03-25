import java.util.Comparator;

public class CustomComparator implements Comparator<Solid> {

    public int compare(Solid s1, Solid s2) {
        //int x1 = s1.boundingBox.x;
        int y1 = s1.boundingBox.y;
        //int x2 = s2.boundingBox.x;
        int y2 = s2.boundingBox.y;
        //return (y1>y2 ? -1 : (y1==y2 ? 0 : (x1>x2 ? -1 : (x1==x2 ? 0 : 1))));
        return (y1>y2 ? -1 : (y1==y2 ? 0 : 1));
    }
}

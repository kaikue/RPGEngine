import java.util.Comparator;

public class SolidComparator implements Comparator<Solid> {

    public int compare(Solid s1, Solid s2) {
        //sorts by y, then x
        //array must be reversed after sorting
        int x1 = s1.boundingBox.x;
        int y1 = s1.boundingBox.y;
        int x2 = s2.boundingBox.x;
        int y2 = s2.boundingBox.y;
        if(y1 > y2) {
            return -1;
        }
        else if(y1 < y2) {
            return 1;
        }
        else {
            if(x1 > x2) {
                return -1;
            }
            else if(x1 < x2) {
                return 1;
            }
            else {
                return 0;
            }
        }
        //old version (only sorts by y):
        //return (y1>y2 ? -1 : (y1==y2 ? 0 : 1));
    }
}

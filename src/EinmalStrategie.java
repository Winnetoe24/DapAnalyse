import java.util.HashSet;
import java.util.Set;

public class EinmalStrategie implements AnalyseStrategie{
    @Override
    public Set<int[][]> supply(int[] data, int[] arg) {
        HashSet<int[][]> set = new HashSet<>();
        set.add(new int[][]{data,arg});
        return set;
    }
}

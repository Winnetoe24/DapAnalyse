import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NMalStrategie implements AnalyseStrategie{
    @Override
    public Set<int[][]> supply(int[] data, int[] k) {
        HashSet<int[][]> set = new HashSet<>();
        int[] arg = Arrays.copyOfRange(k, 1, k.length);
        int n = k[0];
        for (int i = 0; i < n; i++) {
            int[] datan = new int[data.length+1];
            datan[0] = i;
            System.arraycopy(data, 0,datan, 1, data.length);
            set.add(new int[][]{datan,arg});
        }
        return set;
    }
}

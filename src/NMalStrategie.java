import java.util.*;

public class NMalStrategie implements AnalyseStrategie{
    @Override
    public List<int[][]> supply(int[] data, int[] k) {
        List<int[][]> list = new ArrayList<>();
        int[] arg = Arrays.copyOfRange(k, 1, k.length);
        int n = k[0];
        for (int i = 0; i < n; i++) {
            int[] datan = new int[data.length+1];
            datan[0] = i;
            System.arraycopy(data, 0,datan, 1, data.length);
            list.add(new int[][]{datan,arg});
        }
        return list;
    }
}

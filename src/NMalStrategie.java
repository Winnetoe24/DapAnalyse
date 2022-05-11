import java.util.*;

public class NMalStrategie implements AnalyseStrategie{
    @Override
    public List<int[][]> supply(int[] data, int[] k) {
        List<int[][]> list = new ArrayList<>();
        int[] arg = Arrays.copyOfRange(k, 0, k.length-1);
        int n = k[k.length-1];
        for (int i = 0; i < n; i++) {
            int[] datan = new int[data.length+1];
            datan[data.length] = i;
            System.arraycopy(data, 0,datan, 0, data.length);
            list.add(new int[][]{datan,arg});
        }
        return list;
    }
}

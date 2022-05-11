import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NMalGroeßerStrategie implements AnalyseStrategie{
    @Override
    public List<int[][]> supply(int[] data, int[] k) {
        List<int[][]> list = new ArrayList<>();
        int[] arg = Arrays.copyOfRange(k, 0, k.length-1);
        int n = k[k.length-1];
        int schrittgroeße = k[k.length-2];
        for (int i = 1; i <= n; i++) {

            int groeße = schrittgroeße * i;
            int[] datan = new int[groeße];
            datan[datan.length-1] = i;
            System.arraycopy(data, 0,datan, 0, groeße -1);
            for (int i1 = 0; i1 < arg.length; i1++) {
                while (arg[i1] >= groeße) {
                    arg[i1] /= 2;
                    if (arg[i1] <= 0) {
                        arg[i1] = 1;
                        break;
                    }
                }
            }
            list.add(new int[][]{datan,arg});
        }
        return list;    }
}

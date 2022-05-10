import java.util.ArrayList;
import java.util.List;

public class EinmalStrategie implements AnalyseStrategie {
    @Override
    public List<int[][]> supply(int[] data, int[] arg) {
        List<int[][]> list = new ArrayList<>();
        list.add(new int[][]{data, arg});
        return list;
    }
}

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Analyse {

    public static void main(String[] args) throws Exception {
        //args lesen
        List<Integer> argumente = new ArrayList<>();
        Set<String> strategiePfade = new HashSet<>();
        Set<String> klassenPfade = new HashSet<>();
        int n = 0;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equalsIgnoreCase("-help") || arg.equalsIgnoreCase("--help")) {
                printHelp();
                return;
            }
            if (n > 0) {
                try {
                    int e = Integer.parseInt(arg);
                    argumente.add(e);
                } catch (NumberFormatException e) {
                    System.err.println("Konnte Argument nicht lesen:" + arg);
                }
                n--;
                continue;
            }
            if (arg.startsWith("-arg=") && arg.length() > 5) {
                try {
                    n = Integer.parseInt(arg.substring(5));
                } catch (NumberFormatException e) {
                    throw new Exception("Konnte die Anzahl der Argumente nicht lesen", e);
                }
                continue;
            }
            if (arg.startsWith("-prg=") && arg.length() > 5) {
                klassenPfade.add(arg.substring(5));
                continue;
            }
            if (arg.startsWith("-strg=") && arg.length() > 6) {
                strategiePfade.add(arg.substring(6));
                continue;
            }
            System.err.println("Argument konnte nicht gelesen werden");
            return;
        }
        System.out.println("Argumente gelesen");
        System.out.println("Beginne Daten einzulesen");
        //Einlesen
        int[] data = new BufferedReader(new InputStreamReader(System.in))
                .lines()
                .map(Integer::parseInt)
                .mapToInt(Integer::intValue)
                .toArray();

        System.out.println("Daten eingelesen");

        int[] argumenteArr = argumente.stream().mapToInt(Integer::intValue).toArray();

        Set<AnalyseStrategie> analyseStrategien = new HashSet<AnalyseStrategie>();

        for (String strategiePfad : strategiePfade) {
            Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(strategiePfad);
            Object noArgInstance = getNoArgInstance(aClass);
            if (noArgInstance instanceof AnalyseStrategie) {
                analyseStrategien.add((AnalyseStrategie) noArgInstance);
                System.out.println("Strategie hinzugefügt:" + strategiePfad);
            }
        }

        System.out.println("Generiere Run Sets");
        Set<Set<int[][]>> runSets = new HashSet<>();
        for (AnalyseStrategie analyseStrategie : analyseStrategien) {
            Set<int[][]> supply = analyseStrategie.supply(data, argumenteArr);
            runSets.add(supply);
        }


        for (String klassenPfad : klassenPfade) {

            try {
                Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(klassenPfad);
                Object o = getNoArgInstance(aClass);
                if (o instanceof Analysierbar) {
                    Analysierbar analysierbar = (Analysierbar) o;
                    System.out.println();
                    System.out.println("Führe " + klassenPfad + " aus");
                    for (Set<int[][]> runSet : runSets) {
                        System.out.println();
                        System.out.println("Set:" + runSet);
                        for (int[][] ints : runSet) {
                            System.out.println();
                            System.out.println("Run:" + ints);
                            System.out.println();
                            Instant now = Instant.now();
                            analysierbar.run(ints[0], ints[1]);
                            Instant now2 = Instant.now();
                            System.out.println();
                            System.out.println("Duration: " + Duration.between(now, now2).toMillis() + " ms");
                            System.out.println();
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


    }

    public static Object getNoArgInstance(Class<?> klasse) {
        for (Constructor<?> constructor : klasse.getConstructors()) {
            try {
                Object o = constructor.newInstance();
                return o;
            } catch (InstantiationException e) {

            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {

            }
        }
        return null;
    }

    public static void printHelp() {
        System.out.println("Hilfe zur Analyse:");
        System.out.println("'-help' ruft diese Hilfe auf");
        System.out.println("'-arg=<n>' liest die nächsten n Argumente als Argumente für die zu analysierenden Programme");
        System.out.println("'-prg=<Pfad>' versucht die Klasse unter dem Pfad zu laden und als Analysierbar aufzurufen");
        System.out.println("'-strg=<Pfad>' versucht die Klasse unter dem Pfad zu laden und als Strategie zu benutzen");
    }
}

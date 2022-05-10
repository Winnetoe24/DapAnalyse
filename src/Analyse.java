import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLOutput;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public class Analyse {

    private static Map<List<int[][]>, String> runListToName = new HashMap<>();
    /**
     * Map<Programm,Map<Strategie, Map<run, Duration>>>
     */
    private static Map<String, Map<String, Map<Integer, Duration>>> zeiten = new HashMap<>();

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

        Map<AnalyseStrategie, String> analyseStrategien = new HashMap<>();

        for (String strategiePfad : strategiePfade) {
            Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(strategiePfad);
            Object noArgInstance = getNoArgInstance(aClass);
            if (noArgInstance instanceof AnalyseStrategie) {
                analyseStrategien.put((AnalyseStrategie) noArgInstance, strategiePfad);
                System.out.println("Strategie hinzugefügt:" + strategiePfad);
            }
        }

        System.out.println("Generiere Run Sets");
        Set<List<int[][]>> runSets = new HashSet<>();
        for (AnalyseStrategie analyseStrategie : analyseStrategien.keySet()) {
            List<int[][]> supply = analyseStrategie.supply(data, argumenteArr);
            runSets.add(supply);
            runListToName.put(supply, analyseStrategien.get(analyseStrategie));
        }


        for (String klassenPfad : klassenPfade) {

            try {
                Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(klassenPfad);
                Object o = getNoArgInstance(aClass);
                if (o instanceof Analysierbar) {
                    Analysierbar analysierbar = (Analysierbar) o;
                    runAnalyse(runSets, klassenPfad, ints -> analysierbar.run(ints[0], ints[1]));
                } else {
                    Method method = aClass.getMethod("run", int[].class, int[].class);
                    runAnalyse(runSets, klassenPfad, ints -> {
                        try {
                            method.invoke(o, ints[0], ints[1]);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        standardAusgabe();

    }

    public static void standardAusgabe() {
        System.out.println("Ausgabe:");
        zeiten.forEach((s, stringMapMap) -> {
            System.out.println("Daten des Programms:"+s);
            stringMapMap.forEach((s1, integerDurationMap) -> {
                System.out.println("Strategie "+ s1+":");
                integerDurationMap.forEach((integer, duration) -> {
                    System.out.println("Run "+integer+": "+duration.toMillis()+" ms");
                });
            });
        });
    }

    private static void runAnalyse(Set<List<int[][]>> runSets, String klassenPfad, Consumer<int[][]> consumer) {
        System.out.println();
        System.out.println("Führe " + klassenPfad + " aus");
        for (List<int[][]> runSet : runSets) {
            System.out.println();
            String name = runListToName.get(runSet);
            System.out.println("Set:" + name);
            for (int i = 0; i < runSet.size(); i++) {
                int[][] ints = runSet.get(i);
                System.out.println();
                System.out.println("Run:" + i);
                System.out.println();
                Instant now = Instant.now();
                consumer.accept(ints);
                Instant now2 = Instant.now();
                System.out.println();
                Duration between = Duration.between(now, now2);
                zeiten.putIfAbsent(klassenPfad, new HashMap<>());
                Map<String, Map<Integer, Duration>> stringMapMap = zeiten.get(klassenPfad);
                stringMapMap.putIfAbsent(name, new HashMap<>());
                stringMapMap.get(name).put(i, between);
                System.out.println("Duration: " + between.toMillis() + " ms");
                System.out.println();
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

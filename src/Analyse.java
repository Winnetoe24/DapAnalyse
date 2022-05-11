import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        boolean percent = false;
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
            if (arg.equalsIgnoreCase("-percent")){
                percent = true;
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
        diagrammAusgabe(percent);
    }

    public static void diagrammAusgabe(boolean percentAusgabe) {
        zeiten.forEach((s, stringMapMap) -> {
            System.out.println("Daten des Programms: " + s);
            stringMapMap.forEach((s1, integerDurationMap) -> {
                List<Integer> integers = new ArrayList<>(integerDurationMap.keySet());
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < integers.size(); i++) {
                    line.append("-");
                }
                System.out.println(line);
                System.out.println("Diagramm von: "+s1);
                List<Long> collect = integerDurationMap.values().stream()
                        .map(Duration::toMillis).collect(Collectors.toList());
                long min = getMin(collect);
                System.out.println("Minimaler Wert:"+min);
                long max = getMax(collect);
                System.out.println("Maximaler Wert:"+max);
                max -= min;
                int[] points = new int[integers.size()];
                for (int i = 0; i < points.length; i++) {
                    Integer integer = integers.get(i);
                    long value = collect.get(i);
                    value -= min;
                    double percent = (value + 0d) / max;
                    if (percentAusgabe) {
                        System.out.println("Prozente:");
                        System.out.println(integer.intValue() + ":" + percent);
                    }
                    percent *= 10d;
                    points[i] = (int) percent;
                }

                for (int i = 9; i >= 0; i--) {
                    StringBuilder bob = new StringBuilder();
                    for (int point : points) {
                        if (point == i || i == 9 && point > 9) {
                            bob.append('*');
                        } else {
                            bob.append(' ');
                        }
                    }
                    System.out.println(bob);
                }
                System.out.println(line);
            });
        });
        String[] diagramm = new String[10];

    }

    /**
     * Bestimmt das kleinste Element des Arrays.
     *
     * @param data Ein Array der mindestens 1 Element enthält.
     * @return das kleinste Element im Array
     */
    public static long getMin(List<Long> data) {
        return data.stream().min(Comparator.naturalOrder()).orElse(0L);
    }

    /**
     * Bestimmt das größte Element des Arrays.
     *
     * @param data Ein Array der mindestens 1 Element enthält.
     * @return das größte Element des Arrays.
     */
    public static long getMax(List<Long> data) {
        return data.stream().max(Comparator.naturalOrder()).get();
    }

    public static void standardAusgabe() {
        System.out.println("Ausgabe:");
        zeiten.forEach((s, stringMapMap) -> {
            System.out.println("Daten des Programms:" + s);
            stringMapMap.forEach((s1, integerDurationMap) -> {
                System.out.println("Strategie " + s1 + ":");
                integerDurationMap.forEach((integer, duration) -> {
                    System.out.println("Run " + integer + ": " + duration.toMillis() + " ms");
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
                System.out.println("Length: "+ints[0].length);
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

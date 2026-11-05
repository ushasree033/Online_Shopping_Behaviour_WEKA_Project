
import java.io.*;
import java.util.*;

public class ShoppingCubeOperations {

    static class Record {
        String category, visitor, device, purchase;

        Record(String[] v, int c, int vi, int d, int p) {
            category = clean(v[c]);
            visitor = clean(v[vi]);
            device = clean(v[d]);
            purchase = clean(v[p]);
        }

        static String clean(String s) {
            s = s.trim();
            if (s.length() >= 2 &&
                ((s.startsWith("'") && s.endsWith("'")) ||
                 (s.startsWith("\"") && s.endsWith("\"")))) {
                return s.substring(1, s.length() - 1);
            }
            return s;
        }
    }

    static List<Record> records = new ArrayList<>();

    static void loadData() throws Exception {
        String file = "online_shopping_behaviour.arff";
        int c = -1, v = -1, d = -1, p = -1;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int index = 0;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.toLowerCase().startsWith("@attribute")) {
                    String lower = line.toLowerCase();

                    if (lower.contains("product_category")) c = index;
                    if (lower.contains("visitor_type")) v = index;
                    if (lower.contains("device")) d = index;
                    if (lower.contains("purchase")) p = index;

                    index++;
                }

                if (line.equalsIgnoreCase("@data")) break;
            }

            if (c < 0 || v < 0 || d < 0 || p < 0)
                throw new Exception("Required attributes not found.");

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("%")) continue;

                String[] values = line.split(",", -1);

                if (values.length > Math.max(Math.max(c, v), Math.max(d, p)))
                    records.add(new Record(values, c, v, d, p));
            }
        }

        System.out.println("Records loaded: " + records.size());
    }

    static Map<String, int[]> summarize(
            List<Record> data,
            java.util.function.Function<Record, String> keyFunction) {

        Map<String, int[]> result = new TreeMap<>();

        for (Record r : data) {
            String key = keyFunction.apply(r);
            int[] count = result.computeIfAbsent(key, k -> new int[2]);
            count[0]++;
            if (r.purchase.equalsIgnoreCase("yes")) count[1]++;
        }

        return result;
    }

    static void writeCsv(String fileName, Map<String, int[]> result)
            throws IOException {

        try (PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                    new FileOutputStream(fileName), "UTF-8"))) {

            out.println("Dimension,Records,Purchases,Purchase_Percentage");

            for (Map.Entry<String, int[]> entry : result.entrySet()) {
                int total = entry.getValue()[0];
                int purchases = entry.getValue()[1];
                double percentage = total == 0 ? 0 :
                        purchases * 100.0 / total;

                out.printf(Locale.US, "\"%s\",%d,%d,%.2f%%%n",
                        entry.getKey().replace("\"", "\"\""),
                        total, purchases, percentage);
            }
        }

        System.out.println("Created: " + fileName);
    }

    // 1. ROLL-UP
    static void rollUp() throws IOException {
        Map<String, int[]> result =
                summarize(records, r -> r.category);
        writeCsv("Rollup_Result.csv", result);
    }

    // 2. DRILL-DOWN
    static void drillDown() throws IOException {
        Map<String, int[]> result = summarize(records,
                r -> r.category + " | " + r.visitor + " | " + r.device);
        writeCsv("Drilldown_Result.csv", result);
    }

    // 3. SLICE: Mobile devices only
    static void slice() throws IOException {
        List<Record> selected = new ArrayList<>();

        for (Record r : records)
            if (r.device.equalsIgnoreCase("Mobile"))
                selected.add(r);

        Map<String, int[]> result = summarize(selected,
                r -> r.category + " | " + r.visitor);
        writeCsv("Slice_Result.csv", result);
    }

    // 4. DICE: Beauty and Clothing on Mobile and Tablet
    static void dice() throws IOException {
        List<Record> selected = new ArrayList<>();

        for (Record r : records) {
            boolean category =
                    r.category.equalsIgnoreCase("Beauty") ||
                    r.category.equalsIgnoreCase("Clothing");

            boolean device =
                    r.device.equalsIgnoreCase("Mobile") ||
                    r.device.equalsIgnoreCase("Tablet");

            if (category && device) selected.add(r);
        }

        Map<String, int[]> result = summarize(selected,
                r -> r.category + " | " + r.visitor + " | " + r.device);
        writeCsv("Dice_Result.csv", result);
    }

    // 5. PIVOT: Categories as rows and devices as columns
    static void pivot() throws IOException {
        String[] devices = {"Desktop", "Mobile", "Tablet"};

        Map<String, Map<String, int[]>> table = new TreeMap<>();

        for (Record r : records) {
            table.putIfAbsent(r.category, new TreeMap<>());
            Map<String, int[]> row = table.get(r.category);

            int[] count = row.computeIfAbsent(r.device, k -> new int[2]);
            count[0]++;
            if (r.purchase.equalsIgnoreCase("yes")) count[1]++;
        }

        try (PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                    new FileOutputStream("Pivot_Result.csv"), "UTF-8"))) {

            out.print("Product_Category");

            for (String device : devices) {
                out.print("," + device + "_Records");
                out.print("," + device + "_Purchases");
            }

            out.println();

            for (String category : table.keySet()) {
                out.print("\"" + category + "\"");

                for (String device : devices) {
                    int[] count = table.get(category)
                            .getOrDefault(device, new int[2]);

                    out.print("," + count[0] + "," + count[1]);
                }

                out.println();
            }
        }

        System.out.println("Created: Pivot_Result.csv");
    }

    public static void main(String[] args) throws Exception {
        loadData();

        rollUp();
        drillDown();
        slice();
        dice();
        pivot();

        System.out.println("\nAll five data cube CSV files generated!");
    }
}

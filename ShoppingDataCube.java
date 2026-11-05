
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.TreeMap;

public class ShoppingDataCube {

    static class CubeResult {
        int totalRecords = 0;
        int totalPurchases = 0;
    }

    public static void main(String[] args) throws Exception {

        String fileName = "online_shopping_behaviour.arff";

        Map<String, CubeResult> cube = new TreeMap<>();

        boolean dataStarted = false;
        int categoryIndex = -1;
        int visitorIndex = -1;
        int deviceIndex = -1;
        int purchaseIndex = -1;

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(fileName))) {

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty() || line.startsWith("%")) {
                    continue;
                }

                if (!dataStarted && line.toLowerCase().startsWith("@attribute")) {

                    String lower = line.toLowerCase();

                    if (lower.contains("product_category"))
                        categoryIndex = countAttributes(reader, line, "product_category");

                    // Attribute positions are detected below from the header.
                }

                if (line.equalsIgnoreCase("@data")) {
                    dataStarted = true;
                    break;
                }
            }

            // Read the header again to identify attribute positions.
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(fileName))) {

            String line;
            int index = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.toLowerCase().startsWith("@attribute")) {
                    String lower = line.toLowerCase();

                    if (lower.contains("product_category"))
                        categoryIndex = index;
                    if (lower.contains("visitor_type"))
                        visitorIndex = index;
                    if (lower.contains("device"))
                        deviceIndex = index;
                    if (lower.contains("purchase"))
                        purchaseIndex = index;

                    index++;
                }

                if (line.equalsIgnoreCase("@data"))
                    break;
            }

            if (categoryIndex < 0 || visitorIndex < 0 ||
                deviceIndex < 0 || purchaseIndex < 0) {
                throw new Exception("Required attributes were not found.");
            }

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty() || line.startsWith("%"))
                    continue;

                String[] values = line.split(",");

                String category = values[categoryIndex].trim();
                String visitor = values[visitorIndex].trim();
                String device = values[deviceIndex].trim();
                String purchase = values[purchaseIndex].trim();

                String key = category + " | " + visitor + " | " + device;

                CubeResult result = cube.computeIfAbsent(
                    key, k -> new CubeResult()
                );

                result.totalRecords++;

                if (purchase.equalsIgnoreCase("yes"))
                    result.totalPurchases++;
            }
        }

        System.out.println("\nONLINE SHOPPING BEHAVIOUR DATA CUBE");
        System.out.println("---------------------------------------------------------------");
        System.out.printf("%-42s %10s %12s %12s%n",
                "Category | Visitor | Device",
                "Records", "Purchases", "Purchase %");
        System.out.println("---------------------------------------------------------------");

        for (Map.Entry<String, CubeResult> entry : cube.entrySet()) {

            CubeResult result = entry.getValue();

            double percentage =
                result.totalRecords == 0 ? 0 :
                (result.totalPurchases * 100.0 / result.totalRecords);

            System.out.printf("%-42s %10d %12d %11.2f%%%n",
                    entry.getKey(),
                    result.totalRecords,
                    result.totalPurchases,
                    percentage);
        }

        try (FileWriter writer = new FileWriter("DataCube_Result.csv")) {
    writer.write("Category,Visitor,Device,Records,Purchases,Purchase_Percentage\n");

    for (Map.Entry<String, CubeResult> entry : cube.entrySet()) {
        CubeResult result = entry.getValue();

        double percentage = result.totalRecords == 0 ? 0 :
                (result.totalPurchases * 100.0 / result.totalRecords);

        String[] dimensions = entry.getKey().split(" \\| ");

        writer.write(dimensions[0] + "," + dimensions[1] + "," + dimensions[2] + ","
                + result.totalRecords + "," + result.totalPurchases + ","
                + String.format(java.util.Locale.US, "%.2f", percentage) + "\n");
    }
}

System.out.println("DataCube_Result.csv created successfully!");

        System.out.println("---------------------------------------------------------------");
        System.out.println("Total combinations: " + cube.size());
    }

    // Helper retained for compatibility with the header-reading section.
    static int countAttributes(BufferedReader reader, String line, String name) {
        return -1;
    }
}

package lookupCanId;

import java.util.HashMap;
import java.util.Map;

public class HexValueLookup {
    /***
     * Retrieves custom hex value tables based on the chosen mode.
     *
     * @param chooseMode An integer representing the selection:
     *                   0: Hex value table for tset.
     *                   1: Hex value table for Telltail.
     *                   2: Hex value table for Vehicle Status.
     *                   3: Hex value table for Telltail and Vehicle Status.
     * @return A 2D array containing hex value table.
     ***/
    public static String[][] gethexValueTable(int chooseMode) {
        if (chooseMode == 0) { /* For tset hexValueTable */
            String[][] hexValueTable = {
                    { // Test A status
                            "0x3=Switch ON123",
                            "0xFE=AAA",
                            "0xFF=BBB",
                    },
                    { // Test B status
                            "0x3=Switch Fault",
                            "0x217=Switch Stuck ON Detected",
                            "0x1=Switch Pressed",
                            "0x0=Switch Released",
                    },
            };
            return hexValueTable;
        }
        if (chooseMode == 1) { /* Telltail hexValueTable */
            String[][] hexValueTable = {
                    { // Telltail Status: Only have on/off status
                            "0x1=On", "0x0=Off"
                    },
            };
            return hexValueTable;
        }
        if (chooseMode == 2) { /* Vehicle Status hexValueTable */
            String[][] hexValueTable = {
                    { // Vehicle Status: PRND status
                            "0x3=Switch Fault", "0x2=Switch Stuck ON Detected",
                            "0x1=Switch Pressed", "0x0=Switch Released",
                    },
                    { // Vehicle Status: Outdoor temperature status
                            "0xFF=FF - Signal not available", "0xFE=Init",
                    },
                    { // Vehicle Status: Speed status
                            "0x1FFF=Error", "0x1FFE=Init",
                    },
                    { // Vehicle Status: Battery level SOC status
                            "0xFF=Error", "0xFE=Init",
                    },
                    { // Vehicle Status: Driving mileage status
                            "0x3FF=Error", "0x3FE=Init",
                    }
            };
            return hexValueTable;
        }
        if (chooseMode == 3) {/* Telltail and Vehicle Status hexValueTable */
            String[][] hexValueTable = {
                    { // Telltail Status: Only have on/off status
                            "0x1=On", "0x0=Off"
                    },
                    { // Vehicle Status: PRND status
                            "0x3=Switch Fault", "0x2=Switch Stuck ON Detected",
                            "0x1=Switch Pressed", "0x0=Switch Released",
                    },
                    { // Vehicle Status: Outdoor temperature status
                            "0xFF=FF - Signal not available", "0xFE=Init",
                    },
                    { // Vehicle Status: Speed status
                            "0x1FFF=Error", "0x1FFE=Init",
                    },
                    { // Vehicle Status: Battery level SOC status
                            "0xFF=Error", "0xFE=Init",
                    },
                    { // Vehicle Status: Driving mileage status
                            "0x3FF=Error", "0x3FE=Init",
                    }
            };
            return hexValueTable;
        }
        return new String[0][0]; // Return empty array
    }

    /***
     * Retrieves a custom corresponding table by mapping indexes from
     * canIdSignalsTable to hexValueTables based on the chosen mode.
     *
     * @param canIdSignalsTable An array containing signal indexes.
     * @param hexValueTable     A 2D array containing hex value tables.
     * @param chooseMode        An integer representing the selection:
     *                          0: Choose correspondingTable for test.
     *                          1: Choose correspondingTable for Telltail.
     *                          2: Choose correspondingTable for Vehicle Status.
     *                          3: Choose correspondingTable for Telltail and
     *                          Vehicle Status.
     * @return A map containing the corresponding table.
     ***/
    public static Map<String, String[]> getCorrespondingTable(String[] canIdSignalsTable, String[][] hexValueTable, int chooseMode) {
        Map<String, String[]> correspondingTable = new HashMap<>();
        switch (chooseMode) {
            case 0: /* Custom correspondingTable for test. */
                correspondingTable.put(canIdSignalsTable[0], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[1], hexValueTable[1]);
                correspondingTable.put(canIdSignalsTable[2], hexValueTable[1]);
                correspondingTable.put(canIdSignalsTable[3], hexValueTable[0]);
                break;
            case 1: /* Custom correspondingTable for Telltail. */
                correspondingTable.put(canIdSignalsTable[0], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[1], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[2], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[3], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[4], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[5], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[6], hexValueTable[0]);
                break;
            case 2: /* Custom correspondingTable for Vehicle Status. */
                correspondingTable.put(canIdSignalsTable[0], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[1], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[2], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[3], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[4], hexValueTable[1]);
                correspondingTable.put(canIdSignalsTable[5], hexValueTable[2]);
                correspondingTable.put(canIdSignalsTable[6], hexValueTable[3]);
                correspondingTable.put(canIdSignalsTable[7], hexValueTable[4]);
                break;
            case 3: /* Custom correspondingTable for Telltail and Vehicle Status. */
                correspondingTable.put(canIdSignalsTable[0], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[1], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[2], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[3], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[4], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[5], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[6], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[7], hexValueTable[1]);
                correspondingTable.put(canIdSignalsTable[8], hexValueTable[1]);
                correspondingTable.put(canIdSignalsTable[9], hexValueTable[1]);
                correspondingTable.put(canIdSignalsTable[10], hexValueTable[1]);
                correspondingTable.put(canIdSignalsTable[11], hexValueTable[2]);
                correspondingTable.put(canIdSignalsTable[12], hexValueTable[3]);
                correspondingTable.put(canIdSignalsTable[13], hexValueTable[4]);
                correspondingTable.put(canIdSignalsTable[14], hexValueTable[5]);
                break;
        }
        return correspondingTable;
    }

    /***
     * Retrieves the status information corresponding to a specific ID and
     * hexadecimal value from the provided corresponding table.
     *
     * @param specificID         The specific ID to search for in the corresponding table.
     * @param hexValue           The hexadecimal value to match in the corresponding table entry.
     * @param correspondingTable The mapping table containing IDs and their associated hex value arrays.
     * @return The status information associated with the specific ID and hexadecimal value, or
     * "Not found" if no matching entry is found in the corresponding table.
     ***/
    public static String getHexValueStatus(String specificID, String hexValue, Map<String, String[]> correspondingTable) {
        for (Map.Entry<String, String[]> entry : correspondingTable.entrySet()) {
            if (specificID.equals(entry.getKey())) {
                String[] hexValueTable = entry.getValue();
                for (String entryValue : hexValueTable) {
                    String[] parts = entryValue.split("=");
                    if (parts.length == 2 && parts[0].equals(hexValue)) {
                        return entryValue;
                    }
                }
            }
        }
        return "Not found";
    }

    private static void testGetHexValueStatus() {  /* Test using a specificID to find the HexValueStatus. */
        int selectMode = 0;
        String[][] hexValueTable = gethexValueTable(selectMode);
        String[] canIdSignalsTable = SpecificCanIdDataset.getSpecificCanIdDatasets(selectMode);
        Map<String, String[]> correspondingTable = getCorrespondingTable(canIdSignalsTable, hexValueTable, selectMode);
        String specificID = "0x199, 26, 3";
        String hexValue = "0x3";
        String hexValueStatus = getHexValueStatus(specificID, hexValue, correspondingTable);
        System.out.println("specificID: " + specificID + "\nStatus: " + hexValueStatus);
    }

    public static void main(String[] args) {
        //testGetHexValueStatus();
    }
}
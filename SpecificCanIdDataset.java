package lookupCanId;

public class SpecificCanIdDataset {
    /**
     * Retrieves custom specific CAN ID signal datasets based on the chosen mode.
     *
     * @param chooseMode An integer representing the selection:
     *                   0: Signal Specific CAN ID for test.
     *                   1: Specific CAN ID for Telltail.
     *                   2: Specific CAN ID for Vehicle Status.
     *                   3: Specific CAN ID for Telltail and Vehicle Status.
     * @return A String array containing signal specific CAN ID.
     **/
    public static String[] getSpecificCanIdDatasets(int chooseMode) {
        if (chooseMode == 0) { /* For tset CAN ID */
            String[] canIdSignals = { "0x199, 26, 3", "0x199, 13, 2", "0x199, 17, 2", "0x199, 19, 2" };
            return canIdSignals;
        }
        if (chooseMode == 1) { /* Telltail CAN ID */
            String[] canIdSignals = {
                    "0x217, 18, 1", // Low beam (on/off)
                    "0x217, 17, 1", // High beam (on/off)
                    "0x217, 43, 1", // Front fog lamp (on/off)
                    "0x217, 50, 1", // Left direction light (on/off), head and tail lights are the same CAN ID
                    "0x217, 49, 1", // Right direction light (on/off), head and tail lights are the same CAN ID
                    "0x217, 41, 1", // Rear fog light (on/off)
                    "0x500, 48, 1", // Warning light HAZARD (on/off)
            };
            return canIdSignals;
        }
        if (chooseMode == 2) { /* Vehicle Status CAN ID */
            String[] canIdSignals = {
                    "0x199, 13, 2", // PRND_P Park light
                    "0x199, 15, 2", // PRND_R Reverse light
                    "0x199, 17, 2", // PRND_N Neutral light
                    "0x199, 19, 2", // PRND_D Drive light
                    "0x40A, 31, 8", // Outdoor temperature
                    "0x217, 39, 13", // Speed
                    "0x403, 23, 8", // Battery level SOC
                    "0x403. 25, 10", // Driving mileage
            };
            return canIdSignals;
        }
        if (chooseMode == 3) {/* Telltail and Vehicle Status CAN ID */
            String[] canIdSignals = {
                    "0x217, 18, 1", // Low beam (on/off)
                    "0x217, 17, 1", // High beam (on/off)
                    "0x217, 43, 1", // Front fog lamp (on/off)
                    "0x217, 50, 1", // Left direction light (on/off), head and tail lights are the same CAN ID
                    "0x217, 49, 1", // Right direction light (on/off), head and tail lights are the same CAN ID
                    "0x217, 41, 1", // Rear fog light (on/off)
                    "0x500, 48, 1", // Warning light HAZARD (on/off)
                    "0x199, 13, 2", // PRND_P Park light
                    "0x199, 15, 2", // PRND_R Reverse light
                    "0x199, 17, 2", // PRND_N Neutral light
                    "0x199, 19, 2", // PRND_D Drive light
                    "0x40A, 31, 8", // Outdoor temperature
                    "0x217, 39, 13", // Speed
                    "0x403, 23, 8", // Battery level SOC
                    "0x403. 25, 10", // Driving mileage
            };
            return canIdSignals;
        }
        return new String[0]; // Return empty array
    }

    /** Test get a Specific CAN ID dataset. */
    private static void testGetSpecificCanIdDataSet() { 
        int selectMode = 0;
        String[] SpecificCanIds = getSpecificCanIdDatasets(selectMode);
        for (String canIDSet : SpecificCanIds) {
            System.out.println("SpecificCanId: " + canIDSet + "\n");
        }
    }

    public static void main(String[] args) {
        //testGetSpecificCanIdDataSet();
    }
}

package lookupCanId;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Queue;

public class SignalCANInfo {
    /**
     * Parses the dataSet and extracts signal information.
     *
     * @param dataSet A string containing signal data separated by ';'.
     * @return An array containing signal information:
     *         [0]: Signal data in its original format.
     *         [1]: The formatted can ID.
     *         [2]: The extracted can data.
     */
    public static String[] parseSignalData(String dataSet) {
        // Get signalInfo
        String[] data = dataSet.split(" ");
        String signalInfo = dataSet;

        // Remove header (first group)
        String[] canIDTmpArray = new String[3];
        System.arraycopy(data, 1, canIDTmpArray, 0, 3);
        String canIDTmp = String.join(" ", canIDTmpArray);

        // Get canData
        String[] canDataArray = new String[8];
        System.arraycopy(data, 4, canDataArray, 0, 8);
        String canData = String.join(" ", canDataArray);

        // Process canIDTmp
        String[] canIDTmpValues = canIDTmp.split(" ");
        StringBuilder processedCanIDTmp = new StringBuilder();

        for (String value : canIDTmpValues) {
            int intValue = Integer.parseInt(value, 16);
            processedCanIDTmp.append(Integer.toHexString(intValue));
        }

        // Create canID
        String canID = "0x" + processedCanIDTmp.toString();

        // Return values in an array
        return new String[] { signalInfo, canID, canData };
    }

    /**
     * Calculates the hexadecimal value based on specificID and signalData.
     *
     * @param specificID A string containing specific ID, start bit, and length.
     * @param signalData An array containing parsed signal information.
     * @return The calculated hexadecimal value.
     */
    public static String getHexValue(String specificID, String[] signalData) {
        String[] specificIDParts = specificID.split(", ");
        String myCanID = specificIDParts[0];
        String myStartBit = specificIDParts[1];
        String myLength = specificIDParts[2];

        String signalInfo = signalData[0];
        String canID = signalData[1];
        // String canData = signalData[2];

        String[] stringArray = signalInfo.split(" ");

        int[] intArray = new int[stringArray.length];
        for (int j = 0; j < stringArray.length; j++) {
            intArray[j] = Integer.decode("0x" + stringArray[j]);
        }
        if (stringArray.length >= 12) {
            for (int i = 0; i < stringArray.length; i++) {
                if (canID.equals(myCanID)) {
                    int trueStartBit = Integer.parseInt(myStartBit) + 1 - Integer.parseInt(myLength);
                    int canValue = (intArray[4 + trueStartBit / 8] >> trueStartBit % 8)
                            & ((1 << Integer.parseInt(myLength)) - 1);

                    // System.out.println("\nmyCanID: " + myCanID + "\nmyStartBit: " + myStartBit +
                    // "\nmyLength: " + myLength + "\n");
                    // System.out.println("\ncanID: " + canID + "\nsignalInfo: " + signalInfo+"\n");

                    // Return Hex Value
                    return "0x" + Integer.toHexString(canValue);
                }
            }
        }

        return ""; // or handle the case when specificID is not found in signalData
    }

    /**
     * Finds the Hex Value Status using specific CAN IDs within received data.
     * 
     * @param receivedData A string containing CAN signal data formatted according
     *                     to standard.
     * @param chooseMode   An integer representing the selection:
     *                     0: Hex value status for tset.
     *                     1: Hex value status for Telltail.
     *                     2: Hex value status for Vehicle Status.
     *                     3: Hex value status for Telltail and Vehicle Status.
     */
    public static void findSpecificIDsForHexValueStatus(String receivedData, int selectMode) {
        String[] canIdSignalsTable = SpecificCanIdDataset.getSpecificCanIdDatasets(selectMode);
        String[][] hexValueTable = HexValueLookup.gethexValueTable(selectMode);
        Map<String, String[]> correspondingTable = HexValueLookup.getCorrespondingTable(canIdSignalsTable,
                hexValueTable, selectMode);

        String[] dataSets = receivedData.split(";");
        // Record the specific ID that was processed
        Set<String> processedIDs = new HashSet<>();

        for (String dataSet : dataSets) {
            String[] signalData = parseSignalData(dataSet);
            for (String specificID : canIdSignalsTable) {
                // Check if a specific ID has already been processed
                if (!processedIDs.contains(specificID)) {
                    String hexValue = getHexValue(specificID, signalData);
                    if (!hexValue.isEmpty()) {
                        String result = HexValueLookup.getHexValueStatus(specificID, hexValue, correspondingTable);
                        System.out.println("Hex Value: " + hexValue + "\n");
                        System.out.println("specificID: " + specificID + "\nStatus: " + result + "\n-----\n");
                    }
                    // Add processed specific ID to collection
                    processedIDs.add(specificID);
                }
            }
        }
    }

    /** Test using a specificID to find the HexValue. */
    private static void testGetHexValue() { 
        String receivedData = "53 01 09 09 00 90 67 1b 00 00 00 00;53 01 09 09 00 90 67 1b 00 00 00 00;53 01 0F 0C 00 90 67 1b 00 00 00 00;53 01 09 09 00 90 57 1b 00 00 00 00;53 01 0F 0C 00 90 67 1b 00 00 00 00;";
        String[] specificID2 = SpecificCanIdDataset.getSpecificCanIdDatasets(0);
        String myTestInput = specificID2[0]; // "0x199, 26, 3"
        String[] dataSets = receivedData.split(";");
        for (String dataSet : dataSets) {
            String[] signalData = parseSignalData(dataSet);
            String hexValue = getHexValue(myTestInput, signalData);
            if (!hexValue.isEmpty()) {
                System.out.println("Hex Value: " + hexValue + "\n-----\n");
            }
        }
    }

    /** Test using a specificID to find the HexValueStatus. */
    private static void testGetHexValueStatus() { 
        int selectMode = 0;
        String[][] hexValueTable = HexValueLookup.gethexValueTable(selectMode);
        String[] canIdSignalsTable = SpecificCanIdDataset.getSpecificCanIdDatasets(selectMode);
        Map<String, String[]> correspondingTable = HexValueLookup.getCorrespondingTable(canIdSignalsTable, hexValueTable, selectMode);
        String specificID = "0x199, 26, 3";
        String hexValue = "0x3";
        String hexValueStatus = HexValueLookup.getHexValueStatus(specificID, hexValue, correspondingTable);
        System.out.println("specificID: " + specificID + "\nStatus: " + hexValueStatus);
    }

    /** Test using multiple specificIDs to find the hexValue status on receivedDataSets. */
    private static void testSpecificIDsForHexValueStatus() { 
        String receivedData = "53 01 09 09 00 90 67 1b 00 00 00 00;53 01 09 09 00 90 67 1b 00 00 00 00;53 01 0F 0C 00 90 67 1b 00 00 00 00;53 01 09 09 00 90 57 1b 00 00 00 00;53 01 0F 0C 00 90 67 1b 00 00 00 00;";
        int selectMode = 0;
        findSpecificIDsForHexValueStatus(receivedData, selectMode);
    }

    /**
     * Test receiving simulated CAN signals to find Hex Value status with queue length.
     * This test aims to simulate the reception of CAN signals, organize them into a
     * queue,
     * and find the Hex Value status based on the queued data. It verifies the
     * functionality
     * of parsing signals, managing the queue, and extracting Hex Value status based
     * on
     * predefined IDs from the received data.
     */
    private static void testSignalQueueHexValueStatus() {
        int count = 255;
        int selectMode = 0;
        String inputSimulateCanSignals = SignalReceiver.simulateCanSignals(count);
        Queue<String> tmpSignalQueue = SignalReceiver.getSignalQueue(inputSimulateCanSignals);
        String aString = SignalReceiver.getQueueToString(tmpSignalQueue);
        System.out.println("aString: " + aString);
        SignalCANInfo.findSpecificIDsForHexValueStatus(aString, selectMode);
    }

    public static void main(String[] args) {
        //testSpecificIDsForHexValueStatus();
        testSignalQueueHexValueStatus();
    }
}

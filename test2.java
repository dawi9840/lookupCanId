import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.HashMap;


public class SignalCANInfo {
    private static int selectMode = 3; // [3]: telltale, vehicle and DMS status.
    private static String[] canIdSignalsTable_;
    private static String[][] hexValueTable_;
    private static Map<String, String[]> correspondingTable_;
    private static String idLowBeam;
    private static String idHighBeam;
    private static String idFrontFog;
    private static String idLeftDirection;
    private static String idRightDirection;
    private static String idRearFog;
    private static String idHazard;
    private static String idGearP;
    private static String idGearR;
    private static String idGearN;
    private static String idGearD;         
    private static String idOduTemp;
    private static String idSpeed;
    private static String idSoc;
    private static String idDrivingMileage;
    private static String idYaw;
    private static String idPhoneCall;

    private static String statusLightOn;
    private static String statusLightOff;
    private static String statusGearOn;
    private static String statusDmsOn;
    private static String statusDmsOff;

    // SignalCANInfo======================================================
    /***
     * Parses the dataSet and extracts signal information.
     *
     * @param dataSet A string containing signal data separated by ';'.
     * @return An array containing signal information:
     *         [0]: Signal data in its original format.
     *         [1]: The formatted can ID.
     *         [2]: The extracted can data.
     ***/
    public static String[] SignalCANInfo_parseSignalData(String dataSet) {
        // System.out.println("dawi_parseSignalData");

        // Get signalInfo
        String[] data = dataSet.split(" ");
        String signalInfo = dataSet;

        // Remove header (first group)
        String[] canIDTmpArray = new String[3];
        System.arraycopy(data, 1, canIDTmpArray, 0, 3);
        String canIDTmp = String.join(" ", canIDTmpArray);
        // System.out.println("canIDTmp_Remove header: " + canIDTmp);

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
        String canID = "0x" + processedCanIDTmp.toString().toUpperCase();

        // System.out.println("signalInfo: " + signalInfo);
        // System.out.println("canID: " + canID);
        // System.out.println("canData: " + canData + "\n----------\n");

        return new String[] { signalInfo, canID, canData };
    }

    /***
     * Calculates the hexadecimal value based on specificID and signalData.
     *
     * @param specificID A string containing specific ID, start bit, and length.
     * @param signalData An array containing parsed signal information.
     * @return The calculated hexadecimal value.
     ***/
    public static String SignalCANInfo_getHexValue(String specificID, String[] signalData) {
        // System.out.println("dawi_getHexValue");

        String[] specificIDParts = specificID.split(", ");
        if (specificIDParts.length < 3) {
                return "";
        }
        String myCanID = specificIDParts[0];
        String myStartBit = specificIDParts[1];
        String myLength = specificIDParts[2];

        // System.out.println("=====\nmyCanID: " + myCanID);
        // System.out.println("myStartBit: " + myStartBit);
        // System.out.println("myLength: " + myLength + "\n=====\n");

        String signalInfo = signalData[0];
        String canID = signalData[1];
        // String canData = signalData[2];
        // System.out.println("canID: " + canID);
        // System.out.println("signalInfo: " + signalInfo + "\n--------------------\n");

        String[] stringArray = signalInfo.split(" ");

        int[] intArray = new int[stringArray.length];
        for (int j = 0; j < stringArray.length; j++) {
            intArray[j] = Integer.decode("0x" + stringArray[j]);
        }
        if (stringArray.length >= 12) {
            for (int i = 0; i < stringArray.length; i++) {
                if (canID.equals(myCanID)) {
                    int trueStartBit = Integer.parseInt(myStartBit) + 1 - Integer.parseInt(myLength);
                    int canValue = (intArray[4 + trueStartBit / 8] >> trueStartBit % 8) & ((1 << Integer.parseInt(myLength)) - 1);
                    String HexValue = "0x" + Integer.toHexString(canValue);
                    // System.out.println("HexValue: "+ HexValue + "\n----------\n");
                    return HexValue;
                }
            }
        }

        return ""; // or handle the case when specificID is not found in signalData
    }

    /***
     * Finds the Hex Value Status using specific CAN IDs within received data.
     *
     * @param receivedData A string containing CAN signal data formatted according to standard.
     * @param selectMode An integer representing the selection:
     *                   0: Hex value status for tset.
     *                   1: Hex value status for Telltail.
     *                   2: Hex value status for Vehicle Status.
     *                   3: Hex value status for Telltail, Vehicle Status and DMS status.
     *                   4: Hex value status for DMS Status.
     ***/
    public static void SignalCANInfo_findSpecificIDsForHexValueStatus(String receivedData, int selectMode) {
        // System.out.println("dawi_findSpecificIDsForHexValueStatus");

        String[] canIdSignalsTable = SpecificCanIdDataset_getSpecificCanIdDatasets(selectMode);
        String[][] hexValueTable = HexValueLookup_gethexValueTable(selectMode);
        Map<String, String[]> correspondingTable = HexValueLookup_getCorrespondingTable(canIdSignalsTable, hexValueTable, selectMode);
        String[] dataSets = receivedData.split(";");

        // Record the specific ID that was processed
        Set<String> processedIDs = new HashSet<>();

        for (String dataSet : dataSets) {
            String[] signalData = SignalCANInfo_parseSignalData(dataSet);
            for (String specificID : canIdSignalsTable) {
                // Check if a specific ID has already been processed
                if (!processedIDs.contains(specificID)) {
                    String hexValue = SignalCANInfo_getHexValue(specificID, signalData);
                    if (!hexValue.isEmpty()) {
                        String result = HexValueLookup_getHexValueStatus(specificID, hexValue, correspondingTable);
                        // System.out.println("hexValue: " + hexValue);
                        // System.out.println("specificID: " + specificID);
                        // System.out.println("hexValueStatus: " + result + "\n----------\n");
                    }
                    // Add processed specific ID to collection
                    processedIDs.add(specificID);
                }
            }
        }
    }

    public static int SignalCANInfo_convertHexValueToDecimalValue(String hexValue){
        // System.out.println( "dawi_convertHexValueToDecimalValue");
        String trimmedString = hexValue.substring(2);
        int DecimalValue = Integer.parseInt(trimmedString, 16);
        // System.out.println( "DecimalValue: " + DecimalValue + "\n----------\n");
        return DecimalValue;
    }

    // HexValueLookup======================================================
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
    public static String[][] HexValueLookup_gethexValueTable(int chooseMode) {
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
                            "0x1=On", "0x0=Off",
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
                    { // 0.Telltail Status: Only have on/off status
                            "0x1=On", "0x0=Off",
                    },
                    { // 1.Vehicle Status: PRND status
                            "0x3=Switch Fault", "0x2=Switch Stuck ON Detected",
                            "0x1=Switch Pressed", "0x0=Switch Released",
                    },
                    { // 2.Vehicle Status: Outdoor temperature status
                            "0xFF=FF - Signal not available", "0xFE=Init",
                    },
                    { // 3.Vehicle Status: Speed status
                            "0x1FFF=Error", "0x1FFE=Init",
                    },
                    { // 4.Vehicle Status: Battery level SOC status
                            "0xFF=Error", "0xFE=Init",
                    },
                    { // 5.Vehicle Status: Driving mileage status
                            "0x3FF=Error", "0x3FE=Init",
                    },
                    { // 6.DMS Status: Only have Alarm/NotAlarm status
                            "0x1=Alarm", "0x0=NotAlarm",
                    },
            };
            return hexValueTable;
        }
        if (chooseMode == 4) {/* DMS Status hexValueTable */
            String[][] hexValueTable = {
                    { // DMS Status: Only have Alarm/NotAlarm status
                            "0x1=Alarm", "0x0=NotAlarm",
                    },
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
     *                          3: Choose correspondingTable for
     *                             Telltail, Vehicle Status, and DMS.
     *                          4: Choose correspondingTable for DMS.
     * @return A map containing the corresponding table.
     ***/
    public static Map<String, String[]> HexValueLookup_getCorrespondingTable(String[] canIdSignalsTable, String[][] hexValueTable, int chooseMode) {
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
            case 3: /* Custom correspondingTable for Telltail, Vehicle Status and DMS. */
                // Ref Telltail status : case1 hexValueTable
                correspondingTable.put(canIdSignalsTable[0], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[1], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[2], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[3], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[4], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[5], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[6], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[7], hexValueTable[1]);
                // Ref Vehicle status: case2 hexValueTable
                correspondingTable.put(canIdSignalsTable[8], hexValueTable[1]);
                correspondingTable.put(canIdSignalsTable[9], hexValueTable[1]);
                correspondingTable.put(canIdSignalsTable[10], hexValueTable[1]);
                correspondingTable.put(canIdSignalsTable[11], hexValueTable[2]);
                correspondingTable.put(canIdSignalsTable[12], hexValueTable[3]);
                correspondingTable.put(canIdSignalsTable[13], hexValueTable[4]);
                correspondingTable.put(canIdSignalsTable[14], hexValueTable[5]);
                // Ref DMS: case4 hexValueTable
                correspondingTable.put(canIdSignalsTable[15], hexValueTable[6]);
                correspondingTable.put(canIdSignalsTable[16], hexValueTable[6]);
                break;
            case 4: /* Custom correspondingTable for DMS Status. */
                correspondingTable.put(canIdSignalsTable[0], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[1], hexValueTable[1]);
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
    public static String HexValueLookup_getHexValueStatus(String specificID, String hexValue, Map<String, String[]> correspondingTable) {
        // System.out.println( "dawi_getHexValueStatus");
        for (Map.Entry<String, String[]> entry : correspondingTable.entrySet()) {
            // Check whether a specific ID matches the Key of the current Map entry
            if (specificID.equals(entry.getKey())) {
                String[] hexValueTable = entry.getValue();
                // Check whether the corresponding value array is null
                if (hexValueTable != null) {
                    for (String entryValue : hexValueTable) {
                        // Check if each entryValue is null
                        if (entryValue != null) {
                            String[] parts = entryValue.split("=");
                            // Check length of parts array to avoid ArrayIndexOutOfBoundsException
                            if (parts.length >= 2 && parts[0].equals(hexValue)) {
                                // System.out.println("entryValue" + entryValue + "\n----------\n");
                                return entryValue;
                            }
                        }
                    }
                }
            }
        }
        return "Not found";
    }

    // SpecificCanIdDataset======================================================
    /***
     * Retrieves custom specific CAN ID signal datasets based on the chosen mode.
     *
     * @param chooseMode An integer representing the selection:
     *                   0: Signal Specific CAN ID for test.
     *                   1: Specific CAN ID for Telltail.
     *                   2: Specific CAN ID for Vehicle Status.
     *                   3: Specific CAN ID for Telltail, Vehicle Status and DMS.
     *                   4: Specific CAN ID for DMS.
     * @return A String array containing signal specific CAN ID.
     ***/
    public static String[] SpecificCanIdDataset_getSpecificCanIdDatasets(int chooseMode) {
        if (chooseMode == 0) { /* For tset CAN ID (ID, startBit, length) */
            String[] canIdSignals = {
                    "0x199, 26, 3",
                    "0x199, 13, 2",
                    "0x199, 17, 2",
                    "0x199, 19, 2",
            };
            return canIdSignals;
        }
        if (chooseMode == 1) { /* Telltail CAN ID (ID, startBit, length) */
            String[] canIdSignals = {
                    "0x217, 18, 1", // Low beam (on/off)
                    "0x217, 17, 1", // High beam (on/off)
                    "0x215, 43, 1", // Front fog lamp (on/off), head and tail lights are the same CAN ID
                    "0x217, 50, 1", // Left direction light (on/off), head and tail lights are the same CAN ID
                    "0x217, 49, 1", // Right direction light (on/off), head and tail lights are the same CAN ID
                    "0x217, 41, 1", // Rear fog light (on/off)
                    "0x500, 48, 1", // Warning light HAZARD (on/off)
            };
            return canIdSignals;
        }
        if (chooseMode == 2) { /* Vehicle Status CAN ID (ID, startBit, length) */
            String[] canIdSignals = {
                    "0x199, 13, 2",  // PRND_P Park light
                    "0x199, 15, 2",  // PRND_R Reverse light
                    "0x199, 17, 2",  // PRND_N Neutral light
                    "0x199, 19, 2",  // PRND_D Drive light
                    "0x40A, 31, 8",  // Outdoor temperature
                    "0x217, 39, 13", // Speed
                    "0x403, 23, 8",  // Battery level SOC
                    "0x403, 25, 10", // Driving mileage
            };
            return canIdSignals;
        }
        if (chooseMode == 3) {/* Telltail and Vehicle Status and DMS CAN ID (ID, startBit, length) */
            String[] canIdSignals = {
                    "0x217, 18, 1",  // 0.Low beam (on/off)
                    "0x217, 17, 1",  // 1.High beam (on/off)
                    "0x215, 43, 1",  // 2.Front fog lamp (on/off), head and tail lights are the same CAN ID
                    "0x217, 50, 1",  // 3.Left direction light (on/off), head and tail lights are the same CAN ID
                    "0x217, 49, 1",  // 4.Right direction light (on/off), head and tail lights are the same CAN ID
                    "0x217, 41, 1",  // 5.Rear fog light (on/off)
                    "0x500, 48, 1",  // 6.Warning light HAZARD (on/off)
                    "0x199, 13, 2",  // 7.PRND_P Park light
                    "0x199, 15, 2",  // 8.PRND_R Reverse light
                    "0x199, 17, 2",  // 9.PRND_N Neutral light
                    "0x199, 19, 2",  // 10.PRND_D Drive light
                    "0x40A, 31, 8",  // 11.Outdoor temperature
                    "0x217, 39, 13", // 12.Speed
                    "0x403, 23, 8",  // 13.Battery level SOC
                    "0x403, 25, 10", // 14.Driving mileage
                    "0x700, 18, 1",  // 15.Yaw
                    "0x700, 19, 1",  // 16.PhoneCall
            };
            return canIdSignals;
        }
        if (chooseMode == 4) {/* DMS CAN ID (ID, startBit, length) */
            String[] canIdSignals = {
                    "0x700, 18, 1", // Yaw
                    "0x700, 19, 1", // PhoneCall
            };
            return canIdSignals;
        }
        return new String[0]; // Return empty array
    }

    private static void SpecificCanIdDataset_testGetSpecificCanIdDataSet() { /* Test get a Specific CAN ID dataset. */
        int selectMode = 0;
        String[] SpecificCanIds = SpecificCanIdDataset_getSpecificCanIdDatasets(selectMode);
        for (String canIDSet : SpecificCanIds) {
            System.out.println("specificCanID: " + canIDSet);
        }
    }

    // SpecificSignalDatasetForQML======================================================
    public static String[] SpecificSignalDatasetForQML_getSpecificSignals(int chooseMode) {
        if (chooseMode == 0) {/* Telltail signals */
            String[] signalsStrArr = {       // (on/off)
                    "Low_Beam_Light",        // "0x217, 18, 1"
                    "High_Beam_Light",       // "0x217, 17, 1"
                    "Fog_Light_Front",       // "0x215, 43, 1", head and tail lights are the same signal name
                    "Direction_Light_Left",  // "0x217, 50, 1", head and tail lights are the same signal name
                    "Direction_Light_Right", // "0x217, 49, 1", head and tail lights are the same signal name
                    "Fog_Light_Rear",        // "0x217, 41, 1"
                    "Warning_Light_HAZARD",  // "0x500, 48, 1"
            };
            return signalsStrArr;
        }
        if (chooseMode == 1) {/* Vehicle Status signals */
            String[] signalsStrArr = {
                    "gear_p",                // "0x199, 13, 2", Park light
                    "gear_r",                // "0x199, 15, 2", Reverse lights
                    "gear_n",                // "0x199, 17, 2", Neutral light
                    "gear_d",                // "0x199, 19, 2", Drive light
                    "Outdoor_temperature",   // "0x40A, 31, 8", Outdoor temperature
                    "Speed",                 // "0x217, 39, 13", Speed
                    "Battery_Level_SOC",     // "0x403, 23, 8", Battery level SOC
                    "Driving_Mileage",       // "0x403. 25, 10", Driving mileage
            };
            return signalsStrArr;
        }
        if (chooseMode == 2) {/* DMS signals*/
            String[] signalsStrArr = {
                "Time_For_A_Brake",          // "0x700, 18, 1", Yaw
                "Focus_On_Driving",          // "0x700, 19, 1", PhoneCall
            };
            return signalsStrArr;
        }
        return new String[0]; // Return empty array
    }

    public static String[] SpecificSignalDatasetForQML_getLightStatus(){
        /* Telltail signals */
        String[] statusArr = {"Opened", "Closed"};
        return statusArr;
    }

    public static String[] SpecificSignalDatasetForQML_getDmsYawStatus(){
        /* DMS signals */
        String[] statusArr = {"Yaw_Alarm", "Yaw_NotAlarm"};
        return statusArr;
    }

    public static String[] SpecificSignalDatasetForQML_getDmsPhoneCallStatus(){
        /* DMS signals */
        String[] statusArr = {"PhoneCall_Alarm", "PhoneCall_NotAlarm"};
        return statusArr;
    }

    public static String SpecificSignalDatasetForQML_testGetSpecificSignal(int chooseMode, int index){
        return SpecificSignalDatasetForQML_getSpecificSignals(chooseMode)[index];
    }

    public static String SpecificSignalDatasetForQML_testGetLightStatus(int index){
        return SpecificSignalDatasetForQML_getLightStatus()[index];
    }

    public static String SpecificSignalDatasetForQML_testGetDmsYawStatus(int index){
        return SpecificSignalDatasetForQML_getDmsYawStatus()[index];
    }

    public static String SpecificSignalDatasetForQML_testGetDmsPhoneCallStatus(int index){
        return SpecificSignalDatasetForQML_getDmsPhoneCallStatus()[index];
    }

    // =================================================================================
    public static void initSpecificCanSeriesTable(){
        canIdSignalsTable_ = SpecificCanIdDataset_getSpecificCanIdDatasets(selectMode);
        hexValueTable_ = HexValueLookup_gethexValueTable(selectMode);
        correspondingTable_ = HexValueLookup_getCorrespondingTable(canIdSignalsTable_, hexValueTable_, selectMode);

        idLowBeam = canIdSignalsTable_[0];         // "0x217, 18, 1",  // 0.Low beam (on/off)
        idHighBeam = canIdSignalsTable_[1];        // "0x217, 17, 1",  // 1.High beam (on/off)
        idFrontFog = canIdSignalsTable_[2];        // "0x215, 43, 1",  // 2.Front fog lamp (on/off)
        idLeftDirection = canIdSignalsTable_[3];   // "0x217, 50, 1",  // 3.Left direction light (on/off)
        idRightDirection = canIdSignalsTable_[4];  // "0x217, 49, 1",  // 4.Right direction light (on/off)
        idRearFog = canIdSignalsTable_[5];         // "0x217, 41, 1",  // 5.Rear fog light (on/off)
        idHazard = canIdSignalsTable_[6];          // "0x500, 48, 1",  // 6.Warning light HAZARD (on/off)
        idGearP = canIdSignalsTable_[7];           // "0x199, 13, 2",  // 7.PRND_P Park light
        idGearR = canIdSignalsTable_[8];           // "0x199, 15, 2",  // 8.PRND_R Reverse light
        idGearN = canIdSignalsTable_[9];           // "0x199, 17, 2",  // 9.PRND_N Neutral light
        idGearD = canIdSignalsTable_[10];          // "0x199, 19, 2",  // 10.PRND_D Drive light
        idOduTemp = canIdSignalsTable_[11];        // "0x40A, 31, 8",  // 11.Outdoor temperature
        idSpeed = canIdSignalsTable_[12];          // "0x217, 39, 13", // 12.Speed
        idSoc = canIdSignalsTable_[13];            // "0x403, 23, 8",  // 13.Battery level SOC
        idDrivingMileage = canIdSignalsTable_[14]; // "0x403, 25, 10", // 14.Driving mileage
        idYaw = canIdSignalsTable_[15];            // "0x700, 18, 1",  // 15.Yaw
        idPhoneCall = canIdSignalsTable_[16];      // "0x700, 19, 1",  // 16.PhoneCall

        statusLightOn = hexValueTable_[0][0];      // 0x1=On, Telltail Status: on status
        statusLightOff = hexValueTable_[0][1];     // 0x0=Off, Telltail Status: off status
        statusGearOn = hexValueTable_[1][2];       // 0x1=Switch Pressed
        statusDmsOn = hexValueTable_[6][0];        // 0x1=Alarm
        statusDmsOff = hexValueTable_[6][1];       // 0x0=NotAlarm
    }

    private static void dawi_test(){
        String receivedData = "54 02 01 05 00 00 00 00 00 00 00 00;"; // front Fog off
        // String receivedData = "54 04 00 0A 00 00 00 3E 00 00 00 00;"; // ODU: 62 degC
        // String receivedData = "54 02 01 07 00 00 00 00 6F 20 00 00;"; // low beam:off; high beam:off;speed: 0 km/h;
        // String receivedData = "54 02 01 07 00 00 00 00 C2 70 00 00;"; // speed: 0 km/h;high beam:off;LD:off; RD:off; rear fog:off; 
        // String receivedData = "54 04 00 03 00 00 32 02 08 00 00 00;"; // DM: 520 km/h; soc:20%; DM:50 km;

        // String receivedData = "54 02 01 07 00 00 04 00 00 00 00 00;"; // low beam on
        // String receivedData = "54 02 01 07 00 00 00 00 00 00 00 00;"; // low beam off; high beam off;;Speed: 0 km/h;
        // String receivedData = "54 01 09 09 00 10 00 00 00 00 00 00;"; // gear_P:0x1=Switch Pressed; (gear_R, gera_N, gear_D):0x0=Switch Released; 
        // String receivedData = "54 01 09 09 00 40 00 00 00 00 00 00;"; // gear_R:0x1=Switch Pressed; (gear_P, gera_N, gear_D):0x0=Switch Released;
        // String receivedData = "54 01 09 09 00 00 01 00 00 00 00 00;"; // gera_N:0x1=Switch Pressed; (gear_P,gear_R, gear_D):0x0=Switch Released; 
        // String receivedData = "54 01 09 09 00 00 04 00 00 00 00 00;"; // gear_D: 0x1=Switch Pressed; (gear_P, gear_R, gera_N):0x0=Switch Released;
        
        // String receivedData = "54 04 00 03 00 00 A7 00 00 00 00 00;"; // Soc: 67%; DM: 167 km;
        // String receivedData = "54 04 00 03 00 00 C8 00 00 00 00 00;"; // SOC: 80 %, DM: 200 km;
        // String receivedData = "54 04 00 03 00 00 00 02 A6 00 00 00;"; // DM: 0 km; soc: 0 %;
        // String receivedData = "54 04 00 03 00 00 00 00 C8 00 00 00;"; // DM: 0 km/h; Soc:0 %;
        // String receivedData = "54 02 01 07 00 00 00 00 6F 20 00;"; // data length less 35 Unicode characters

        // Record the specific ID that was processed
        // Set<String> processedIDs = new HashSet<>();

        String[] dataSets = receivedData.split(";");

        for (String dataSet : dataSets) {
            // System.out.println("length: " + receivedData.length());
            // System.out.println("dataSet: " + dataSet);
            if(dataSet.length() >= 35){
                String[] signalData = SignalCANInfo_parseSignalData(dataSet);
                // System.out.println("signalData[1]: " + signalData[1]);
                // System.out.println("signalData[2]: " + signalData[2]);
    
                for(int i=0; i<canIdSignalsTable_.length; i++){    
                    String[] specificIDParts = canIdSignalsTable_[i].split(", ");
                    String myCanID = specificIDParts[0];
                    String myStartBit = specificIDParts[1];
                    String myLength = specificIDParts[2];
                    // System.out.println(i + ", myCanID: " + myCanID + "\n");
        
                    if(signalData[1].equals(myCanID)){
                        // if (!processedIDs.contains(myCanID)) {
                            System.out.println("\nsignalData[1]: " + signalData[1]);
                            System.out.println("signalData[2]: " + signalData[2]);
                            System.out.println(i + ", (myCanID, myStartBit, myLength): (" + myCanID + ", " + myStartBit + ", " + myLength + ")\n");
                            // System.out.println(i + ", canIdSignalsTable_[i]: " + canIdSignalsTable_[i]);
                            String hexValue = SignalCANInfo_getHexValue(canIdSignalsTable_[i], signalData);
                            String hexValueStatus = HexValueLookup_getHexValueStatus(canIdSignalsTable_[i], hexValue, correspondingTable_);
                            if(!hexValue.isEmpty()){
                                System.out.println("hexValue: " + hexValue);
                                System.out.println("hexValueStatus: " + hexValueStatus + "\n");
                                separateTelltailSignals(canIdSignalsTable_[i], hexValueStatus);
                                separateGearSignals(canIdSignalsTable_[i], hexValueStatus);
                                separateVehicleStatusSignals(canIdSignalsTable_[i], hexValue, hexValueStatus);
                                separateDmsSignals(canIdSignalsTable_[i], hexValueStatus);
                            }
                        //     // Add the processed specific ID to the collection
                        //     processedIDs.add(myCanID);
                        // }
                    }
                }
            }else{
                System.out.println("Input data length less than 35 Unicode characters!");
                System.out.println("dataSet: " + dataSet);
                System.out.println("dataSet.lenght(): " + dataSet.length());
            }
        }
    }

    private static void separateTelltailSignals(String specificID, String hexValueStatus){
        String telltaleLight = "";
        String telltaleStatus = "";

        System.out.println("\ndawi_separateTelltailSignals-------------");
        // System.out.println("Input specificID: " + specificID);
        // System.out.println("Input hexValueStatus: " + hexValueStatus);

        // 0.Low beam (on/off)
        if(specificID.equals(idLowBeam) && hexValueStatus.equals(statusLightOn)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 0);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(0);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n\n");
        }
        if(specificID.equals(idLowBeam) && hexValueStatus.equals(statusLightOff)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 0);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(1);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n\n");
        }

        // 1.High beam (on/off)
        if(specificID.equals(idHighBeam) && hexValueStatus.equals(statusLightOn)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 1);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(0);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n");
        }
        if(specificID.equals(idHighBeam) && hexValueStatus.equals(statusLightOff)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 1);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(1);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n");
        }

        // 2.Front fog lamp (on/off), head and tail lights are the same CAN ID
        if(specificID.equals(idFrontFog) && hexValueStatus.equals(statusLightOn)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 2);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(0);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n");
        }
        if(specificID.equals(idFrontFog) && hexValueStatus.equals(statusLightOff)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 2);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(1);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n");
        }

        // 3.Left direction light (on/off), head and tail lights are the same CAN ID
        if(specificID.equals(idLeftDirection) && hexValueStatus.equals(statusLightOn)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 3);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(0);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n");
        }
        if(specificID.equals(idLeftDirection) && hexValueStatus.equals(statusLightOff)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 3);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(1);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n");
        }
        
        // 4.Right direction light (on/off), head and tail lights are the same CAN ID
        if(specificID.equals(idRightDirection) && hexValueStatus.equals(statusLightOn)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 4);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(0);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n");
        }
        if(specificID.equals(idRightDirection) && hexValueStatus.equals(statusLightOff)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 4);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(1);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n");
        }

        // 5.Rear fog light (on/off)
        if(specificID.equals(idRearFog) && hexValueStatus.equals(statusLightOn)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 5);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(0);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n");
        }
        if(specificID.equals(idRearFog) && hexValueStatus.equals(statusLightOff)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 5);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(1);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n");
        }

        // 6.Warning light HAZARD (on/off)
        if(specificID.equals(idHazard) && hexValueStatus.equals(statusLightOn)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 6);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(0);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n");
        }
        if(specificID.equals(idHazard) && hexValueStatus.equals(statusLightOff)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 6);
            telltaleStatus = SpecificSignalDatasetForQML_testGetLightStatus(1);
            System.out.println("Light: " + telltaleLight + ", Status: " + telltaleStatus + "\n");
        }

        if(!telltaleLight.equals("") && !telltaleStatus.equals("")){
            System.out.println("Input specificID: " + specificID);
        }
        System.out.println("\n-----------------------------------------------------\n");
    }

    private static void separateGearSignals(String specificID, String hexValueStatus){
        String gearValue = "";

        System.out.println("dawi_separateGearSignals-------------\n");
        // System.out.println("Input specificID: "+ specificID);
        // System.out.println("Input hexValueStatus: "+ hexValueStatus + "\n\n");

        if(specificID.equals(idGearP) && hexValueStatus.equals(statusGearOn)){/* 7.PRND_P Park light */ 
            gearValue = SpecificSignalDatasetForQML_testGetSpecificSignal(1, 0); //{1:0-3}
            System.out.println("gearValue: " + gearValue + "\n");
        }else if(specificID.equals(idGearR) && hexValueStatus.equals(statusGearOn)){/* 8.PRND_R Reverse light */
            gearValue = SpecificSignalDatasetForQML_testGetSpecificSignal(1, 1); //{1:0-3}
            System.out.println("gearValue: " + gearValue + "\n");
        }else if(specificID.equals(idGearN) && hexValueStatus.equals(statusGearOn)){/* 9.PRND_N Neutral light */
            gearValue = SpecificSignalDatasetForQML_testGetSpecificSignal(1, 2); //{1:0-3}
            System.out.println("gearValue: " + gearValue + "\n");
        }else if(specificID.equals(idGearD) && hexValueStatus.equals(statusGearOn)){/* 10.PRND_D Drive light */
            gearValue = SpecificSignalDatasetForQML_testGetSpecificSignal(1, 3); //{1:0-3}
            System.out.println("gearValue: " + gearValue + "\n");
        }else if(!gearValue.equals("")){
            System.out.println("Input specificID: "+ specificID);
        }
        System.out.println("\n-----------------------------------------------------\n");
    }

    private static void separateVehicleStatusSignals(String specificID, String hexValue, String hexValueStatus){
        String strOduValues = "";
        String strSpeedValues = "";
        String strSocValues = "";
        String strDmValues = "";
        double factor = 0;
        int maximum = 0;
        int minimum = 0;
        String unit = "";

        System.out.println("\ndawi_separateVehicleStatusSignals-------------");
        // System.out.println("Input specificID: " + specificID);
        // System.out.println("Input hexValue: " + hexValue);
        // System.out.println("Input hexValueStatus: " + hexValueStatus);

        /* 11.Outdoor temperature */
        if(specificID.equals(idOduTemp) && 
           !hexValueStatus.equals(statusLightOn) && !hexValueStatus.equals(statusLightOff) &&
           !hexValueStatus.equals(hexValueTable_[1][0]) && !hexValueStatus.equals(hexValueTable_[1][1]) &&
           !hexValueStatus.equals(hexValueTable_[2][0]) && !hexValueStatus.equals(hexValueTable_[2][1]) &&
           !hexValueStatus.equals(hexValueTable_[3][0]) && !hexValueStatus.equals(hexValueTable_[3][1]) && 
           !hexValueStatus.equals(hexValueTable_[4][0]) && !hexValueStatus.equals(hexValueTable_[4][1]) &&
           !hexValueStatus.equals(hexValueTable_[5][0]) && !hexValueStatus.equals(hexValueTable_[5][1]) &&
           !hexValueStatus.equals(statusDmsOn) && !hexValueStatus.equals(statusDmsOff)
           ){
            factor = 1;
            maximum = 213;
            minimum = -40;
            unit = " degC";
            int oduValues = (int)Math.round(SignalCANInfo_convertHexValueToDecimalValue(hexValue)*factor);
            if(oduValues>=minimum && oduValues<=maximum){
                strOduValues = String.valueOf(oduValues);
                System.out.println("ODU: " + strOduValues + unit + "\n\n");
            }
        }

        /* 12.Speed */
        if(specificID.equals(idSpeed) && 
           !hexValueStatus.equals(statusLightOn) && !hexValueStatus.equals(statusLightOff) &&
           !hexValueStatus.equals(hexValueTable_[1][0]) && !hexValueStatus.equals(hexValueTable_[1][1]) &&
           !hexValueStatus.equals(hexValueTable_[2][0]) && !hexValueStatus.equals(hexValueTable_[2][1]) &&
           !hexValueStatus.equals(hexValueTable_[3][0]) && !hexValueStatus.equals(hexValueTable_[3][1]) &&
           !hexValueStatus.equals(hexValueTable_[4][0]) && !hexValueStatus.equals(hexValueTable_[4][1]) &&
           !hexValueStatus.equals(hexValueTable_[5][0]) && !hexValueStatus.equals(hexValueTable_[5][1]) &&
           !hexValueStatus.equals(statusDmsOn) && !hexValueStatus.equals(statusDmsOff)
           ){
            factor = 0.05625;
            maximum = 360;
            minimum = -100;
            unit = " km/h";
            int speedValues = (int)Math.round(SignalCANInfo_convertHexValueToDecimalValue(hexValue)*factor);
            if(speedValues>=minimum && speedValues<=maximum){
                strSpeedValues = String.valueOf(speedValues);
                System.out.println("Speed: " + strSpeedValues + unit + "\n\n");
            }
        }

        /* 13.Battery level SOC */
        if(specificID.equals(idSoc) &&
           !hexValueStatus.equals(statusLightOn) && !hexValueStatus.equals(statusLightOff) &&
           !hexValueStatus.equals(hexValueTable_[1][0]) && !hexValueStatus.equals(hexValueTable_[1][1]) &&
           !hexValueStatus.equals(hexValueTable_[2][0]) && !hexValueStatus.equals(hexValueTable_[2][1]) &&
           !hexValueStatus.equals(hexValueTable_[3][0]) && !hexValueStatus.equals(hexValueTable_[3][1]) && 
           !hexValueStatus.equals(hexValueTable_[4][0]) && !hexValueStatus.equals(hexValueTable_[4][1]) &&
           !hexValueStatus.equals(hexValueTable_[5][0]) && !hexValueStatus.equals(hexValueTable_[5][1]) &&
           !hexValueStatus.equals(statusDmsOn) && !hexValueStatus.equals(statusDmsOff)
           ){
            factor = 0.4;
            maximum = 100;
            minimum = 0;
            unit = " %";
            int socValues = (int)Math.round(SignalCANInfo_convertHexValueToDecimalValue(hexValue)*factor);
            if(socValues>=minimum && socValues<=maximum){
                strSocValues = String.valueOf(socValues);
                System.out.println("SOC: " + strSocValues + unit + "\n\n");
            }
        }

        /* 14.Driving mileage */
        if(specificID.equals(idDrivingMileage) &&
           !hexValueStatus.equals(statusLightOn) && !hexValueStatus.equals(statusLightOff) &&
           !hexValueStatus.equals(hexValueTable_[1][0]) && !hexValueStatus.equals(hexValueTable_[1][1]) &&
           !hexValueStatus.equals(hexValueTable_[2][0]) && !hexValueStatus.equals(hexValueTable_[2][1]) &&
           !hexValueStatus.equals(hexValueTable_[3][0]) && !hexValueStatus.equals(hexValueTable_[3][1]) && 
           !hexValueStatus.equals(hexValueTable_[4][0]) && !hexValueStatus.equals(hexValueTable_[4][1]) &&
           !hexValueStatus.equals(hexValueTable_[5][0]) && !hexValueStatus.equals(hexValueTable_[5][1]) &&
           !hexValueStatus.equals(statusDmsOn) && !hexValueStatus.equals(statusDmsOff)
        ){
            factor = 1;
            maximum = 1023;
            minimum = 0;
            unit = " km";
            int dmValues = (int)Math.round(SignalCANInfo_convertHexValueToDecimalValue(hexValue)*factor);
            if(dmValues>=minimum && dmValues<=maximum){
                strDmValues = String.valueOf(dmValues);
                System.out.println("Driving mileage: " + strDmValues + unit + "\n\n");
            }
        }

        if(!strOduValues.equals("") && !strSpeedValues.equals("") &&
           !strSocValues.equals("") && !strDmValues.equals("")){
            System.out.println("Input specificID: " + specificID);
        }
        System.out.println("\n-----------------------------------------------------\n");
    }

    private static void separateDmsSignals(String specificID, String hexValueStatus){
        String dmsAleart = "";
        String dmsStatus = "";
        System.out.println("\ndawi_separateDmsSignals-------------");
        // System.out.println("Input specificID: " + specificID);
        // System.out.println("Input hexValueStatus: " + hexValueStatus);

        /* 15.Yaw */
        if(specificID.equals(idYaw) && hexValueStatus.equals(statusDmsOn)){
            dmsAleart = SpecificSignalDatasetForQML_testGetSpecificSignal(2, 0); // "Time_For_A_Brake",// "0x700, 18, 1"
            dmsStatus = SpecificSignalDatasetForQML_testGetDmsYawStatus(0);// "Yaw_Alarm" 
            System.out.println("dmsAleart: " + dmsAleart + ", Status: " + dmsStatus + "\n\n");
        }
        if(specificID.equals(idYaw) && hexValueStatus.equals(statusDmsOff)){
            dmsAleart = SpecificSignalDatasetForQML_testGetSpecificSignal(2, 0);
            dmsStatus = SpecificSignalDatasetForQML_testGetDmsYawStatus(1);// "Yaw_NotAlarm"
            System.out.println("dmsAleart: " + dmsAleart + ", Status: " + dmsStatus + "\n\n");
        }

        /* 16.PhoneCall */
        if(specificID.equals(idPhoneCall) && hexValueStatus.equals(statusDmsOn)){
            dmsAleart = SpecificSignalDatasetForQML_testGetSpecificSignal(2, 1); // "Focus_On_Driving",// "0x700, 19, 1"
            dmsStatus = SpecificSignalDatasetForQML_testGetDmsPhoneCallStatus(0);// "PhoneCall_Alarm" 
            System.out.println("dmsAleart: " + dmsAleart + ", Status: " + dmsStatus + "\n\n");
        }
        if(specificID.equals(idPhoneCall) && hexValueStatus.equals(statusDmsOff)){
            dmsAleart = SpecificSignalDatasetForQML_testGetSpecificSignal(2, 1);
            dmsStatus = SpecificSignalDatasetForQML_testGetDmsPhoneCallStatus(1);// "PhoneCall_NotAlarm"
            System.out.println("dmsAleart: " + dmsAleart + ", Status: " + dmsStatus + "\n\n");
        }

        if(!dmsAleart.equals("") && !dmsStatus.equals("")){
            System.out.println("Input specificID: " + specificID);
        }
        System.out.println("\n-----------------------------------------------------\n");
    }

    private static void test(){
        System.out.println("test-------------------------\n");
        // System.out.println("canIdSignalsTable_[12]: " + canIdSignalsTable_[12] + "\n");
        // System.out.println("hexValueTable_[3][0]: " + hexValueTable_[3][0] + "\n");
        // System.out.println("hexValueTable_[3][1]: " + hexValueTable_[3][1] + "\n");
        // System.out.println("length: " + canIdSignalsTable_.length + "\n");

        // for(int i=0; i<canIdSignalsTable_.length; i++){    
        //     String[] specificIDParts = canIdSignalsTable_[i].split(", ");
        //     String myCanID = specificIDParts[0];
        //     String myStartBit = specificIDParts[1];
        //     String myLength = specificIDParts[2];
        //     System.out.println(i + ", myCanID: " + myCanID + "\n");
        // }
        
        String hexValue = "0xa7";
        int oduValues = (int)Math.round(SignalCANInfo_convertHexValueToDecimalValue(hexValue)*0.4);
        String strOduValues = String.valueOf(oduValues);
        System.out.println("ODU: " + strOduValues + "\n\n");
        
        
    }

    public static void main(String[] args) {
        initSpecificCanSeriesTable();
        dawi_test();
        // test();
    }
}

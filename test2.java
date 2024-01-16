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

    private static String canID217;
    private static String startBitLowBeam;
    private static String startBitHighBeam;

    private static String canID427;
    private static String startBitLeftDirection;
    private static String startBitRightDirection;
    private static String startBitHazard;

    private static String canID403;
    private static String startBitSoc;
    private static String startBitDrivingMileage;
    private static String lengthSoc;
    private static String lengthDrivingMileage;
    private static String strSocValues;
    private static String strDmValues;
    private static String unit;
    private static double factor;
    private static double maximum;
    private static double minimum;
    private static int offset;

    private static String statusLightOn;
    private static String statusLightOff;
    private static String statusGearOn;
    private static String statusDmsOn;
    private static String statusDmsOff;
    private static String statusLedOn;
    private static String statusLedOff;

    private static String qmlStatusLightOn;
    private static String qmlStatusLightOff;
    private static String qmlLowBeamLight;
    private static String qmlHighBeamLight;
    private static String qmlLeftDirectionLight;
    private static String qmlRightDirectionLight;
    private static String qmlHazardLight;
    
    public static int[] canBuffer = {0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0}; // initial value 0

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

        // System.out.println("Input specificID: " + specificID);
        //Log.d(TAG, "myCanID: " + myCanID);
        //Log.d(TAG, "myStartBit: " + myStartBit);
        //Log.d(TAG, "myLength: " + myLength + "\n=====\n");
        //Log.d(TAG, "InputSourceCanInfo: " + signalData[0]);
        //Log.d(TAG, "InputcanID: " + signalData[1]);
        //Log.d(TAG, "InputcanData: " + signalData[2] + "\n--------------------\n");

        return getCanValue(myCanID, myStartBit, myLength, signalData); 
    }

    /**
     * Calculates the hexadecimal value based on CAN ID, start bit, length, and signalData.
     *
     * @param canId      A string representing the CAN ID.
     * @param startBit   A string representing the starting bit.
     * @param length     A string representing the length.
     * @param signalData An array containing parsed signal information.
     * @return The calculated hexadecimal value.
     ***/
    public static String getCanValue(String canId, String startBit, String length, String[] signalData) {
        // kuma version
        String hexValue = "";
 
        canId = canId.contains("0x") ? canId.replace("0x", "") : canId;
        String[] strSignalData = signalData[0].split(" ");

        if(strSignalData != null) {
            int[] arr = new int[strSignalData.length];
            for(int i=0; i<strSignalData.length; i++) { 
                arr[i] = Integer.decode("0x" + strSignalData[i]);
            }
            if(strSignalData.length >= 12){
                //Log.d("kuma", "strSignalData= " + " " +  
                //              strSignalData[0] + " " + strSignalData[1] + " " + strSignalData[2] + " " + strSignalData[3] + " " + 
                //              strSignalData[4] + " " + strSignalData[5] + " " + strSignalData[6] + " " + strSignalData[7] + " " + 
                //              strSignalData[8] + " " + strSignalData[9] + " " + strSignalData[10] + " " + strSignalData[11]);
                System.arraycopy(arr, 4, canBuffer, 0, arr.length-4);
                if(canId.contains("403") && startBit.contains("25")) {
                    // CDI_PTHVbattStatus1 - ptHMIAvaDrvRange_CDI (里程)
                    hexValue = hexValue + "0x" + intToHex(canBuffer[3]) + intToHex(canBuffer[4]);
                }else if(canId.contains("322") && startBit.contains("39")) {
                    // FBCM_LampCmd - headWHLSpd_FBCM (車速)
                    byte originalValue = (byte) canBuffer[5];
                    byte mask = (byte) 0xF8; // 掩碼 11111000
                    byte result = (byte) (originalValue & mask); // 將第5位到第7位替換為0
                    hexValue = hexValue + "0x" + intToHex(canBuffer[4]) + byteToHex(result);
                }else{
                    // 實際起始位置跟excel檔案描述方式不同
                    int trueStartBit = Integer.valueOf((Integer.valueOf(startBit) + 1 - (Integer.valueOf(length))));
                    int canValue = (canBuffer[trueStartBit / 8] >>
                            trueStartBit % 8 &
                            Integer.valueOf((int) Math.pow(2, Integer.valueOf(length))) - 1);
                    hexValue = hexValue + "0x" + intToHex(canValue);
                }
            }
        }
        hexValue = hexValue.replace("0x0","0x");
        return hexValue;
    }

    /**
     * Converts a byte value to its hexadecimal representation.
     * Convert byte to hexadecimal string using Java built-in method.
     *
     * @param value The byte value to convert.
     * @return The hexadecimal representation of the byte value.
     ***/
    public static String byteToHex(byte value) {
        return String.format("%02X", value & 0xFF);
    }

    /**
     * Converts an integer value to its hexadecimal representation.
     *
     * @param value The integer value to convert.
     * @return The hexadecimal representation of the integer value.
     ***/
    public static String intToHex(int value) {
        return String.format("%02X", value);
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
                    { // 7.LED light Status: for direction light(Left/Right) and hazard light
                            "0x3=Reserved", "0x2=LED Fail",
                            "0x1=LED On","0x0=LED Off",
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
        if (chooseMode == 5) {/* 0x427 Status hexValueTable */
            String[][] hexValueTable = {
                    { // LED light Status: for direction light(Left/Right) and hazard light
                            "0x3=Reserved", "0x2=LED Fail",
                            "0x1=LED On","0x0=LED Off",
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
                correspondingTable.put(canIdSignalsTable[3], hexValueTable[7]);  //Direction_Light_Left
                correspondingTable.put(canIdSignalsTable[4], hexValueTable[7]);  //Direction_Light_Right
                correspondingTable.put(canIdSignalsTable[5], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[6], hexValueTable[7]);  //Warning_Light_HAZARD
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
                correspondingTable.put(canIdSignalsTable[3], hexValueTable[7]);  //Direction_Light_Left
                correspondingTable.put(canIdSignalsTable[4], hexValueTable[7]);  //Direction_Light_Right
                correspondingTable.put(canIdSignalsTable[5], hexValueTable[0]);
                correspondingTable.put(canIdSignalsTable[6], hexValueTable[7]);  //Warning_Light_HAZARD
                // Ref Vehicle status: case2 hexValueTable
                correspondingTable.put(canIdSignalsTable[7], hexValueTable[1]);  //P
                correspondingTable.put(canIdSignalsTable[8], hexValueTable[1]);  //R
                correspondingTable.put(canIdSignalsTable[9], hexValueTable[1]);  //N
                correspondingTable.put(canIdSignalsTable[10], hexValueTable[1]); //D
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
                    "0x427, 13, 2", // Left direction light (on/off), head and tail lights are the same CAN ID
                    "0x427, 11, 2", // Right direction light (on/off), head and tail lights are the same CAN ID
                    "0x217, 41, 1", // Rear fog light (on/off)
                    "0x427, 42, 2", // Warning light HAZARD (on/off)
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
                    "0x322, 39, 13", // Speed
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
                    "0x427, 13, 2",  // 3.Left direction light (on/off), head and tail lights are the same CAN ID
                    "0x427, 11, 2",  // 4.Right direction light (on/off), head and tail lights are the same CAN ID
                    "0x217, 41, 1",  // 5.Rear fog light (on/off)
                    "0x427, 42, 2",  // 6.Warning light HAZARD (on/off)
                    "0x199, 13, 2",  // 7.PRND_P Park light
                    "0x199, 15, 2",  // 8.PRND_R Reverse light
                    "0x199, 17, 2",  // 9.PRND_N Neutral light
                    "0x199, 19, 2",  // 10.PRND_D Drive light
                    "0x40A, 31, 8",  // 11.Outdoor temperature
                    "0x322, 39, 13", // 12.Speed
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
                    "Direction_Light_Left",  // "0x427, 13, 2", head and tail lights are the same signal name
                    "Direction_Light_Right", // "0x427, 11, 2", head and tail lights are the same signal name
                    "Fog_Light_Rear",        // "0x217, 41, 1"
                    "Warning_Light_HAZARD",  // "0x427, 42, 2"
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
                    "Speed",                 // "0x322, 39, 13", Speed
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
        return  SpecificSignalDatasetForQML_getLightStatus()[index];
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
        String[] lowBeamSpecificIDParts = idLowBeam.split(", ");
        String[] highBeamSpecificIDParts = idHighBeam.split(", ");
        canID217= "0x217";
        startBitLowBeam = lowBeamSpecificIDParts[1];   // 18
        startBitHighBeam = highBeamSpecificIDParts[1]; // 17

        idLeftDirection = canIdSignalsTable_[3];   // "0x427, 13, 2",  // 3.Left direction light (on/off)
        idRightDirection = canIdSignalsTable_[4];  // "0x427, 11, 2",  // 4.Right direction light (on/off)
        idHazard = canIdSignalsTable_[6];          // "0x427, 42, 2",  // 6.Warning light HAZARD (on/off)
        canID427 = "0x427";
        String[] leftDirectionSpecificIDParts = idLeftDirection.split(", ");
        String[] higRighDirectionSpecificIDParts = idRightDirection.split(", ");
        String[] hazardSpecificIDParts = idHazard.split(", ");
        startBitLeftDirection = leftDirectionSpecificIDParts[1];     // 13
        startBitRightDirection = higRighDirectionSpecificIDParts[1]; // 11
        startBitHazard = hazardSpecificIDParts[1];                   // 42

        idSoc = canIdSignalsTable_[13];            // "0x403, 23, 8",  // 13.Battery level SOC
        idDrivingMileage = canIdSignalsTable_[14]; // "0x403, 25, 10", // 14.Driving mileage
        canID403 = "0x403";
        String[] socSpecificIDParts = idSoc.split(", ");           
        String[] dmSpecificIDParts = idDrivingMileage.split(", ");
        startBitSoc = socSpecificIDParts[1];           // 23
        startBitDrivingMileage = dmSpecificIDParts[1]; // 25
        lengthSoc = socSpecificIDParts[2];             // 8
        lengthDrivingMileage = dmSpecificIDParts[2];   // 10

        strSocValues = "";
        strDmValues = "";
        unit = "";
        factor = 0;
        maximum = 0;
        minimum = 0;
        offset = 0;

        idFrontFog = canIdSignalsTable_[2];        // "0x215, 43, 1",  // 2.Front fog lamp (on/off)
        idRearFog = canIdSignalsTable_[5];         // "0x217, 41, 1",  // 5.Rear fog light (on/off)
        idGearP = canIdSignalsTable_[7];           // "0x199, 13, 2",  // 7.PRND_P Park light
        idGearR = canIdSignalsTable_[8];           // "0x199, 15, 2",  // 8.PRND_R Reverse light
        idGearN = canIdSignalsTable_[9];           // "0x199, 17, 2",  // 9.PRND_N Neutral light
        idGearD = canIdSignalsTable_[10];          // "0x199, 19, 2",  // 10.PRND_D Drive light
        idOduTemp = canIdSignalsTable_[11];        // "0x40A, 31, 8",  // 11.Outdoor temperature
        idSpeed = canIdSignalsTable_[12];          // "0x322, 39, 13", // 12.Speed
        idYaw = canIdSignalsTable_[15];            // "0x700, 18, 1",  // 15.Yaw
        idPhoneCall = canIdSignalsTable_[16];      // "0x700, 19, 1",  // 16.PhoneCall

        statusLightOn = hexValueTable_[0][0];      // 0x1=On, Telltale Status: on status
        statusLightOff = hexValueTable_[0][1];     // 0x0=Off, Telltale Status: off status
        statusLedOn = hexValueTable_[7][2];        // 0x1=LED On, For 0x427 light on status
        statusLedOff = hexValueTable_[7][3];       // 0x0=LED Off, For 0x427 light off status
        statusGearOn = hexValueTable_[1][2];       // 0x1=Switch Pressed, For Gear on status
        statusDmsOn = hexValueTable_[6][0];        // 0x1=Alarm
        statusDmsOff = hexValueTable_[6][1];       // 0x0=NotAlarm

        // send the string enumerator for QML
        qmlLowBeamLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 0);         // Low_Beam_Light
        qmlHighBeamLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 1);        // High_Beam_Light
        qmlLeftDirectionLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 3);   // Direction_Light_Left
        qmlRightDirectionLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 4);  // Direction_Light_Right
        qmlHazardLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 6);          // Warning_Light_HAZARD
        qmlStatusLightOn = SpecificSignalDatasetForQML_testGetLightStatus(0);                         // Opened
        qmlStatusLightOff = SpecificSignalDatasetForQML_testGetLightStatus(1);                        // Closed
    }

    private static void dawi_test(String receivedData){
        // System.out.println("test processReceivedCanSignals() function");

        System.out.println("Input string: " + receivedData + "\n");

        String lowBeanLightStatus = "";
        String highBeanLightStatus = "";
        String leftDirectionLightStatus = "";
        String rightDirectionLightStatus = "";
        String hazardLightStatus = "";
        String socStatus = "";
        String drivingMileageStatus = "";
        String[] dataSets = receivedData.split(";");

        for (String dataSet : dataSets) {
            // System.out.println("length: " + receivedData.length());
            // System.out.println("dataSet: " + dataSet);
            if(dataSet.length() >= 35){
                String[] signalData = SignalCANInfo_parseSignalData(dataSet);
                System.out.println("InputCanID: " + signalData[1]);
                System.out.println("InputCanData: " + signalData[2]);

                for(int i=0; i<canIdSignalsTable_.length; i++){    
                    String[] specificIDParts = canIdSignalsTable_[i].split(", ");
                    String myCanID = specificIDParts[0];
                    String myStartBit = specificIDParts[1];
                    String myLength = specificIDParts[2];
                    System.out.println(i + ", myCanID: " + myCanID + "\n");
                    // System.out.println("InputCanID: " + signalData[1] + "\n");

                    String hexValue = SignalCANInfo_getHexValue(canIdSignalsTable_[i], signalData);

                    if(socAndtDrivingMileageCondition(signalData, myCanID, myStartBit)){
                        // System.out.println( i + "0x403_(myCanID, myStartBit): (" + myCanID + ", " + myStartBit + ")");

                        // Caculate hexValueStatus of low/hight beam light status.
                        if(myStartBit.equals(startBitSoc) && myLength.equals(lengthSoc)){
                            socStatus = HexValueLookup_getHexValueStatus(canIdSignalsTable_[i], hexValue, correspondingTable_);
                            System.out.println( "socStatus: " + socStatus + "\n");
                            if(!socStatus.equals(hexValueTable_[4][0]) && !socStatus.equals(hexValueTable_[4][1])) {
                                factor = 0.4;
                                maximum = 100;
                                minimum = 0;
                                unit = " %";
                                int socValues = (int)Math.round(SignalCANInfo_convertHexValueToDecimalValue(hexValue)*factor);
                                if(socValues>minimum && socValues<=maximum){
                                    strSocValues = String.valueOf(socValues);
                                    //invokeSendingSocValueToQt(strSocValues);
                                    System.out.println("SOC: " + strSocValues + unit + "\n\n");
                                }
                            }
                        }else if(myStartBit.equals(startBitDrivingMileage) && myLength.equals(lengthDrivingMileage)){
                            drivingMileageStatus = HexValueLookup_getHexValueStatus(canIdSignalsTable_[i], hexValue, correspondingTable_);
                            System.out.println("drivingMileageStatus: " + drivingMileageStatus + "\n");
                            if(!drivingMileageStatus.equals(hexValueTable_[5][0]) && !drivingMileageStatus.equals(hexValueTable_[5][1])) {
                                factor = 1;
                                maximum = 1023;
                                minimum = 0;
                                unit = " km";
                                int dmValues = (int)Math.round(SignalCANInfo_convertHexValueToDecimalValue(hexValue)*factor);
                                if(dmValues>minimum && dmValues<=maximum){
                                    strDmValues = String.valueOf(dmValues);
                                    //invokeSendingdrivingMileageValueToQt(strDmValues);
                                    System.out.println("Driving mileage: " + strDmValues + unit + "\n\n");
                                }
                            }
                        }else{//exception
                            System.out.println("SOC and DM status exception");
                        }

                    }else if(LeftAndRightDirectionAndHazardCondition(signalData, myCanID, myStartBit)){
                        // System.out.println( i + "0x427_(myCanID, myStartBit): (" + myCanID + ", " + myStartBit + ")");

                        // Caculate hexValueStatus of low/hight direction, and hazard light status.
                        if(myStartBit.equals(startBitLeftDirection)){
                            leftDirectionLightStatus = HexValueLookup_getHexValueStatus(canIdSignalsTable_[i], hexValue, correspondingTable_);
                            System.out.println( "leftDirectionLightStatus: " + leftDirectionLightStatus + "\n");
                        }else if(myStartBit.equals(startBitRightDirection)){
                            rightDirectionLightStatus = HexValueLookup_getHexValueStatus(canIdSignalsTable_[i], hexValue, correspondingTable_);
                            System.out.println("rightDirectionLightStatus: " + rightDirectionLightStatus + "\n");
                        }else if(myStartBit.equals(startBitHazard)){
                            hazardLightStatus = HexValueLookup_getHexValueStatus(canIdSignalsTable_[i], hexValue, correspondingTable_);
                            System.out.println("hazardLightStatus: " + hazardLightStatus + "\n");
                        }else{//exception
                            System.out.println("left/right and hazard status exception");
                        }
                        sendLRDirectionAndHazardLightsToQML(leftDirectionLightStatus, rightDirectionLightStatus, hazardLightStatus);

                    }else if(lowBeamAndHighBeamLightsCondition(signalData, myCanID, myStartBit)){
                        //System.out.println( i + "0x217_(myCanID, myStartBit): (" + myCanID + ", " + myStartBit + ")");
                        
                        // Caculate hexValueStatus of low/hight beam light status.
                        if(myStartBit.equals(startBitLowBeam)){// low beam
                            lowBeanLightStatus = HexValueLookup_getHexValueStatus(canIdSignalsTable_[i], hexValue, correspondingTable_);
                            System.out.println( "lowBeanLightStatus: " + lowBeanLightStatus + "\n");
                        }else if(myStartBit.equals(startBitHighBeam)){// high beam
                            highBeanLightStatus = HexValueLookup_getHexValueStatus(canIdSignalsTable_[i], hexValue, correspondingTable_);
                            System.out.println("highBeanLightStatus: " + highBeanLightStatus + "\n");
                        }else{//exception
                            System.out.println("low/high beam status exception");
                        }
                        sendLowAndHighBeamLightsToQML(lowBeanLightStatus, highBeanLightStatus);

                    }else if(signalData[1].equals(myCanID)){
                        // System.out.println("dawi test other canID block");
                        System.out.println("\nInputCanID: " + signalData[1]);
                        System.out.println("InputCanData: " + signalData[2]);
                        System.out.println(i + " , (myCanID, myStartBit, myLength): (" + myCanID + ", " + myStartBit + ", " + myLength + ")\n");

                        String hexValueStatus = HexValueLookup_getHexValueStatus(canIdSignalsTable_[i], hexValue, correspondingTable_);

                        if(!hexValue.isEmpty()){
                            System.out.println("hexValue: " + hexValue);
                            System.out.println("hexValueStatus: " + hexValueStatus + "\n");
                            separateFogLampSignals(canIdSignalsTable_[i], hexValueStatus);
                            separateGearSignals(canIdSignalsTable_[i], hexValueStatus);
                            separateVehicleStatusSignals(canIdSignalsTable_[i], hexValue, hexValueStatus);
                            separateDmsSignals(canIdSignalsTable_[i], hexValueStatus);
                        }
                    }
                }
            }else{
                System.out.println("Input data length less than 35 Unicode characters!");
                System.out.println("dataSet: " + dataSet);
                System.out.println("dataSet.lenght(): " + dataSet.length());
            }
        }
    }

    private static boolean socAndtDrivingMileageCondition(String[] signalData, String myCanID, String myStartBit){
        return signalData[1].equals(canID403) && 
                ((myCanID.equals(canID403) && myStartBit.equals(startBitSoc)) || 
                (myCanID.equals(canID403) && myStartBit.equals(startBitDrivingMileage)));
    }

    private static boolean lowBeamAndHighBeamLightsCondition(String[] signalData, String myCanID, String myStartBit){
        return signalData[1].equals(canID217) && 
                ((myCanID.equals(canID217) && myStartBit.equals(startBitLowBeam)) || 
                (myCanID.equals(canID217) && myStartBit.equals(startBitHighBeam)));
    }

    private static boolean LeftAndRightDirectionAndHazardCondition(String[] signalData, String myCanID, String myStartBit){
        return signalData[1].equals(canID427) && 
                ((myCanID.equals(canID427) && myStartBit.equals(startBitLeftDirection)) || 
                (myCanID.equals(canID427) && myStartBit.equals(startBitRightDirection)) ||
                (myCanID.equals(canID427) && myStartBit.equals(startBitHazard)));
    }

    private static void sendLRDirectionAndHazardLightsToQML(String leftDirectionLightStatus, String rightDirectionLightStatus, String hazardLightStatus){
        // Using hexValueStatus to send command status to QML.
        if(leftDirectionLightStatus.equals(statusLedOn)){
            // invokeSendToQtSignalChange(qmlLeftDirectionLight, qmlStatusLightOn);
            System.out.println( qmlLeftDirectionLight + " is " + qmlStatusLightOn + "!\n");
        }else if(rightDirectionLightStatus.equals(statusLedOn)){
            // invokeSendToQtSignalChange(qmlRightDirectionLight, qmlStatusLightOn);
            System.out.println( qmlRightDirectionLight + " is " + qmlStatusLightOn + "!\n");
        }else if(hazardLightStatus.equals(statusLedOn)){
            // invokeSendToQtSignalChange(qmlHazardLight, qmlStatusLightOn);
            System.out.println( qmlHazardLight + " is " + qmlStatusLightOn + "!\n");
        }else{
            // invokeSendToQtSignalChange(qmlHazardLight, qmlStatusLightOff);
            System.out.println( "All " + qmlLowBeamLight + " ," + 
            qmlHighBeamLight + " ,and " +qmlHazardLight+ " are " + qmlStatusLightOff + "!\n");
        }
    }

    private static void sendLowAndHighBeamLightsToQML(String lowBeanLightStatus, String highBeanLightStatus){
        if(lowBeanLightStatus.equals(statusLightOn)) {
            // invokeSendToQtSignalChange(qmlLowBeamLight, qmlStatusLightOn);
            System.out.println( qmlLowBeamLight + " is " + qmlStatusLightOn + "!\n");
        }else if(highBeanLightStatus.equals(statusLightOn)) {
            // invokeSendToQtSignalChange(qmlHighBeamLight, qmlStatusLightOn);
            System.out.println( qmlHighBeamLight + " is " + qmlStatusLightOn + "!\n");
        }else{
            // invokeSendToQtSignalChange(qmlHighBeamLight, qmlStatusLightOff);
            System.out.println( "Both " + qmlLowBeamLight + " and " + qmlHighBeamLight + " are " + qmlStatusLightOff + "!\n");
        }
    }

    private static void separateFogLampSignals(String specificID, String hexValueStatus){
        String telltaleLight = "";

        System.out.println("\ndawi_separateFogLampSignals-------------");
        // System.out.println("Input specificID: " + specificID);
        // System.out.println("Input hexValueStatus: " + hexValueStatus);

        // 2.Front fog lamp (on/off), head and tail lights are the same CAN ID
        if(specificID.equals(idFrontFog) && hexValueStatus.equals(statusLightOn)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 2);
            // invokeSendToQtSignalChange(telltaleLight, qmlStatusLightOn);
            System.out.println("Light: " + telltaleLight + ", Status: " + qmlStatusLightOn + "\n");
        }
        if(specificID.equals(idFrontFog) && hexValueStatus.equals(statusLightOff)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 2);
            // invokeSendToQtSignalChange(telltaleLight, qmlStatusLightOff);
            System.out.println("Light: " + telltaleLight + ", Status: " + qmlStatusLightOff + "\n");
        }

        // 5.Rear fog light (on/off)
        if(specificID.equals(idRearFog) && hexValueStatus.equals(statusLightOn)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 5);
            // invokeSendToQtSignalChange(telltaleLight, qmlStatusLightOn);
            System.out.println("Light: " + telltaleLight + ", Status: " + qmlStatusLightOn + "\n");
        }
        if(specificID.equals(idRearFog) && hexValueStatus.equals(statusLightOff)){
            telltaleLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 5);
            // invokeSendToQtSignalChange(telltaleLight, qmlStatusLightOff);
            System.out.println("Light: " + telltaleLight + ", Status: " + qmlStatusLightOff + "\n");
        }

        if(!telltaleLight.equals("")){
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
        int initValue = 0;

/*
        String strSocValues = "";
        String strDmValues = "";
        
        String unit = "";
        double factor = 0;
        double maximum = 0;
        double minimum = 0;
        int offset = 0;
*/        
        
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
            offset = -40;
            unit = " degC";
            int oduValues = (int)Math.round(SignalCANInfo_convertHexValueToDecimalValue(hexValue)*factor + offset);
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
            maximum = 240;   //360
            minimum = 0;     //-100
            initValue = -100;
            unit = " km/h";
            int speedValues = (int)Math.round(SignalCANInfo_convertHexValueToDecimalValue(hexValue) * factor + initValue);
            if(speedValues>=minimum && speedValues<=maximum){
                strSpeedValues = String.valueOf(speedValues);
                System.out.println("Speed: " + strSpeedValues + unit + "\n\n");
            }
        }
/*
        // 13.Battery level SOC
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

        // 14.Driving mileage
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
*/
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
        
        // String hexValue = "0xa7";
        // int oduValues = (int)Math.round(SignalCANInfo_convertHexValueToDecimalValue(hexValue)*0.4);
        // String strOduValues = String.valueOf(oduValues);
        // System.out.println("ODU: " + strOduValues + "\n\n");

        // String hexValue = "0x6F20";//"0xDE3";
        // int initValue = -100;
        // int aa = SignalCANInfo_convertHexValueToDecimalValue(hexValue);
        // System.out.println("aa: " + aa + "\n\n");

        // double factor = 0.05625;
        // int speed = (int)(Math.round(aa)*factor + initValue);
        // System.out.println("speed: " + speed + "\n\n");
        
        // int aa = 23;
        // String bb = intToHex(aa);
        // System.out.println("bb: " + bb);

        // System.out.println("canID217: " + canID217 + "\n"); // 0x217
        // System.out.println("startBitLowBeam: " + startBitLowBeam + "\n"); //18
        // System.out.println("startBitHighBeam: " + startBitHighBeam + "\n"); //17

        // String lowBeamLight = SpecificSignalDatasetForQML_testGetSpecificSignal(0, 0);
        // System.out.println("lowBeamLight: " + lowBeamLight + "\n"); //Low_Beam_Light
        // String tt1 = SpecificSignalDatasetForQML_testGetLightStatus(0);
        // String tt2 = SpecificSignalDatasetForQML_testGetLightStatus(1);
        // System.out.println("tt1: " + tt1 + "\n"); //Opened
        // System.out.println("tt2: " + tt2 + "\n"); //Closed

    }


    public static void main(String[] args) {
        initSpecificCanSeriesTable();
        //54 04 02 07 00 00 40 00 00 00 00 00
        String receivedData = "54 04 00 03 00 00 00 02 8A 00 00 00;54 04 00 03 00 00 C5 00 00 00 00 00;";
        
        dawi_test(receivedData);
        //V2
        // test();
    }
}

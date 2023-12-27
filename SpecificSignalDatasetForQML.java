package lookupCanId;

public class SpecificSignalDatasetForQML {
    public static String[] getSpecificSignals(int chooseMode) {
        if (chooseMode == 0) {/* Telltail signals */
            String[] signalsStrArr = {       // (on/off)
                    "Low_Beam_Light",        // "0x217, 18, 1"
                    "High_Beam_Light",       // "0x217, 17, 1"
                    "Fog_Light_Front",       // "0x217, 43, 1", head and tail lights are the same signal name
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

    public static String[] getLightStatus(){
        /* Telltail signals */
        String[] statusArr = {"Opened", "Closed"};
        return statusArr;
    }

    public static String[] getDmsStatus(){
        /* DMS signals */
        String[] statusArr = {"Alarm", "NotAlarm"};
        return statusArr;
    }

    public static String testGetSpecificSignal(int chooseMode, int index){
        return getSpecificSignals(chooseMode)[index];
    }

    public static String testGetLightStatus(int index){
        return getLightStatus()[index];
    }

    public static String testGetDmsStatus(int index){
        return getLightStatus()[index];
    }

    private static void testCase1ToGetSignalAndLightStatus(){
        String testSignal = testGetSpecificSignal(0, 0);
        String testLightStatus = testGetLightStatus(0);
        System.out.println("Signal: " + testSignal + "\n"); 
        System.out.println("Light status: " + testLightStatus + "\n");
    }

    private static void testCase2ControlLightStatus(){
        String testSig;
        String testStatus;
        int mode = 0;

        if(mode == 0){
            testSig = testGetSpecificSignal(0, 0);
            testStatus = testGetLightStatus(0);
            System.out.println("mode: " + mode);
            System.out.println("Signal: " + testSig); 
            System.out.println("Light status: " + testStatus + "\n");
        }

        if(mode == 1){
            testSig = testGetSpecificSignal(0, 1);
            testStatus = testGetLightStatus(1);
            System.out.println("mode: " + mode);
            System.out.println("Signal: " + testSig); 
            System.out.println("Light status: " + testStatus + "\n");
        }
    }

    public static void main(String[] args) {
        testCase1ToGetSignalAndLightStatus();
        // testCase2ControlLightStatus();
    }
}

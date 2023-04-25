package bluetooth.chat.application.connect;

import java.util.UUID;

public class Constant {
    public static final String CONNECTTION_UUID ="00001101-0000-1000-8000-00805F9B34FB";
//    public static final String CONNECTTION_UUID ="00001101-0000-1000-8000-00805F9B34FB";

    public static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");


    public static final int  MSG_START_LISTENING =1;
    public static final int  MSG_FINISH_LISTENING =2;
    public static final int  MSG_GOT_A_CLINET =3;
    public static final int  MSG_CONNECTED_TO_SERVER =4;
    public static final int  MSG_GOT_DATA =5;
    public static final int  MSG_ERROR =6;
    public static final int  MSG_SEND = 7;
}

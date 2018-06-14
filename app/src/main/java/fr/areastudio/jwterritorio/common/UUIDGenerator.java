package fr.areastudio.jwterritorio.common;


import android.util.Base64;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by Julien on 17/03/2018.
 */

public class UUIDGenerator {

    public static String uuidToBase64() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return android.util.Base64.encodeToString(bb.array(), Base64.URL_SAFE).replace("==","");


    }
}

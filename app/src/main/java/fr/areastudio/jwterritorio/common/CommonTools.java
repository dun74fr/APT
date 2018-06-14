package fr.areastudio.jwterritorio.common;

import android.content.Context;

/**
 * Created by Julien on 06/04/2018.
 */

public class CommonTools {
    static public int getPositioninArray(Context context, int arrayId, String value){
        String[] values = context.getResources().getStringArray(arrayId);
        for (int i = 0; i < values.length; i++){
            if (values[i].equals(value)){
                return i;
            }
        }
        return 0;
    }
}

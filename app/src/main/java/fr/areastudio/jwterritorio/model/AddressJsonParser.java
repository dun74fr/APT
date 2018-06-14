package fr.areastudio.jwterritorio.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Modifier;
import java.util.List;

public class AddressJsonParser {


    public List<Address> readJson(String json) {
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                .serializeNulls()
                .create();
        try {
            return gson.fromJson(json, new TypeToken<List<Address>>() {
            }.getType());
        }catch (JsonSyntaxException e){
            e.printStackTrace();
        }
        return null;
    }

    public void writeJson(List<Territory> territories) {
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                .serializeNulls()
                .create();
        try {
            String json = gson.toJson(territories);
            Log.d("JSON",json);

        }catch (JsonSyntaxException e){
            e.printStackTrace();
        }
    }

}
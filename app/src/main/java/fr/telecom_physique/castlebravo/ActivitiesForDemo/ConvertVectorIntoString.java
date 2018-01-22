package fr.telecom_physique.castlebravo.ActivitiesForDemo;

import java.util.Vector;

/**
 * Created by Guillaumee on 28/04/2016.
 */
public class ConvertVectorIntoString {

    public String convertToString(Vector<Integer> aVectorToConvert) {

        String aString = new String();

        for (int i = 0; i < aVectorToConvert.size(); i++) {

            aString = aString + Integer.toString(aVectorToConvert.get(i));

        }

        return aString;

    }

}

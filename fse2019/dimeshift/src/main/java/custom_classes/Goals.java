package custom_classes;

import po_utils.TestData;
import java.util.Random;

public class Goals implements TestData {

    // public static final Goals SKI = new Goals("ski");
    // public static final Goals MUSEUM = new Goals("museum");
    // public static final Goals HOUSE = new Goals("house");
    // public static final Goals CAR = new Goals("car");
    public final String value;

    private static final String[] placeholders = {
        "ski",
        "museum",
        "house",
        "car"
    };

    private Goals(String goalString) {
        this.value = goalString;
    }

    public static Goals fromString(String s) {
        return new Goals(s);
    }

    public String value() {
        return this.value;
    }
}
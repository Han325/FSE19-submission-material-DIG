package custom_classes;

import po_utils.TestData;

public class PetTypes implements TestData {

    public final String value;

    public static final String[] examples = {
        "bird",
        "cat",
        "dog",
        "hamster",
        "lizard",
        "snake"
    };

    private PetTypes(String petTypeString) {
        this.value = petTypeString;
    }

    public static PetTypes fromString(String s) {
        return new PetTypes(s);
    }

    public String value() {
        return this.value;
    }
}
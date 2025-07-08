package custom_classes;

import po_utils.TestData;

public class PetNames implements TestData {

    public final String value;

    public static final String[] examples = {
        "Leo",
        "Basil",
        "Rosy",
        "Jewel"
    };

    private PetNames(String petNameString) {
        this.value = petNameString;
    }

    public static PetNames fromString(String s) {
        return new PetNames(s);
    }

    public String value() {
        return this.value;
    }
}
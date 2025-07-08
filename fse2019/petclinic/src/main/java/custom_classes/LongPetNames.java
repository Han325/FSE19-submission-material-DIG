package custom_classes;

import po_utils.TestData;

public class LongPetNames implements TestData {

    public final String value;

    public static final String[] examples = {
        "Leo",
        "Basil",
        "Rosy",
        "JewelJewelJewelJewelJewelJewelJewelJewelJewelJewelJewel"
    };

    private LongPetNames(String petNameString) {
        this.value = petNameString;
    }

    public static LongPetNames fromString(String s) {
        return new LongPetNames(s);
    }

    public String value() {
        return this.value;
    }
}
package custom_classes;

import po_utils.TestData;

public class PeopleNames implements TestData {
    public final String value;

    public static final String[] examples = {
        "John",
        "Mike",
        "Mark"
    };


    private PeopleNames(String name){
        this.value = name;
    }

    public static PeopleNames fromString(String s) {
        return new PeopleNames(s);
    }

    public String value(){
        return this.value;
    }
}

package custom_classes;

import po_utils.TestData;

public class BoardNames implements TestData {

    public final String value;

    public static final String[] examples = {
        "My Retrospective",
        "Work",
        "Project X",
        "Project Y"
    };

    private BoardNames(String boardName){
        this.value = boardName;
    }

    public static BoardNames fromString(String s) {
        return new BoardNames(s);
    }

    public String value(){
        return this.value;
    }
}

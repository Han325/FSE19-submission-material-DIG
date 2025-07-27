package custom_classes;

import po_utils.TestData;

public class IdeasPosts implements TestData {

    public final String value;

    public static final String[] examples = {
        "Probe further with the client on requirements",
        "Plan for fewer storypoints",
        "Have a fridaynight drink with the team",
        "Less meetings",
        "Discuss with supplier",
        "Ask for edit rights at devops"
    };

    private IdeasPosts(String ideaPost){
        this.value = ideaPost;
    }

    public static IdeasPosts fromString(String s) {
        return new IdeasPosts(s);
    }

    public String value(){
        return this.value;
    }
}

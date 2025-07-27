package custom_classes;

import po_utils.TestData;

public class NotWentWellPosts implements TestData {

    public final String value;

    public static final String[] examples = {
        "Clarity on requirements",
        "Late work nights",
        "Build takes too long",
        "Coffee tastes terrible",
        "Too many meetings",
        "Dev time consumed in next release estimation"
    };

    private NotWentWellPosts(String notWentWellPost){
        this.value = notWentWellPost;
    }

    public static NotWentWellPosts fromString(String s) {
        return new NotWentWellPosts(s);
    }

    public String value(){
        return this.value;
    }
}

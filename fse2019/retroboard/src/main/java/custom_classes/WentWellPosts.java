package custom_classes;

import po_utils.TestData;

public class WentWellPosts implements TestData {

    public final String value;

    public static final String[] examples = {
        "Team bonding",
        "Delivering on time",
        "The client is reviewing work delivery quickly, usually within 24 hours",
        "We're on track to solve Problem X within the budget",
        "The team is sending over work that needs few revisions",
        "I think the design is going to really delight the users of the website"
    };

    private WentWellPosts(String wentWellPostsString){
        this.value = wentWellPostsString;
    }

    public static WentWellPosts fromString(String s) {
        return new WentWellPosts(s);
    }

    public String value(){
        return this.value;
    }
}

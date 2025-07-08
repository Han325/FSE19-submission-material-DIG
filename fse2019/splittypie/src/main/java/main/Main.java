package main;

import custom_classes.*;

public class Main {

    public static void main(String[] args) {

        ClassUnderTestApogen classUnderTestApogen0 = new ClassUnderTestApogen();
        TripNames tripNames0 = TripNames.fromString("Mirabilandia");
        classUnderTestApogen0.goToNewEventHomeIndexPage(tripNames0);
        classUnderTestApogen0.cancelNewEventPage();
        classUnderTestApogen0.goToNewEventHomeIndexPage(tripNames0);
        Currencies currencies0 = Currencies.fromString("Czech koruna (CZK)");
        Participants participants0 = Participants.fromString("Mark");
        Participants participants1 = Participants.fromString("Luke");
        classUnderTestApogen0.formNewEventPage(currencies0, participants0, participants1);
        classUnderTestApogen0.goToTransactionsHomePage();

    }
}

package org.semesteroppgave.models.filehandlers.fileSave;

import org.junit.jupiter.api.Test;
import org.semesteroppgave.ApplicationData;
import org.semesteroppgave.Main;
import org.semesteroppgave.models.data.components.*;
import org.semesteroppgave.models.data.customizations.Autopilot;
import org.semesteroppgave.models.data.customizations.Gps;
import org.semesteroppgave.models.data.customizations.Sunroof;
import org.semesteroppgave.models.data.customizations.Towbar;
import org.semesteroppgave.models.data.productmodels.*;

import java.io.IOException;
import java.nio.file.Paths;

class FileSaverCsvTest {

    @Test
    void save() {
        Product testProduct = new Electric.Builder("Elektrisk", 1_200_000)
                .selectedMotor(new Motor("V8", 2000.0, "Denne V8 motoren er billig og økonomisk"))
                .selectedRim(new Rim("Rund", 1000.0, "Disse felgene er runde"))
                .selectedSeatcover(new SeatCover("Mykt", 123, "Dette setetrekket er veldig komfortabelt under lange kjøreturer"))
                .selectedSpoiler(new Spoiler("Høy", 1000, "Denne spoileren gir ekstra fart ettersom den er meget høy"))
                .selectedTires(new Tires("Sommer", 1500.0, "Disse sommerdekkene er veldig slitesterke"))
                .selectedBattery(new Battery("Li-ion", 10000.0, "Li-ion batteri er miljøvennlige og har lang rekkevidde"))
                .withGps(new Gps())
                .withSunroof(new Sunroof())
                .withTowbar(new Towbar())
                .withAutopilot(new Autopilot())
                .build();

        ApplicationData.getInstance().getRegisterProduct().setUserProductList(testProduct);

        FileSaver fileSaver = new FileSaverCsv();

        try {
            fileSaver.save(Paths.get("src/main/resources/org/semesteroppgave/files/testFiles/testWriteCsv.csv"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
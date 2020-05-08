package org.semesteroppgave.models.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.semesteroppgave.ApplicationData;
import org.semesteroppgave.Main;
import org.semesteroppgave.models.data.components.*;
import org.semesteroppgave.models.exceptions.*;
import org.semesteroppgave.models.utilities.alerts.Dialogs;
import org.semesteroppgave.models.utilities.converters.ComponentConverter;
import org.semesteroppgave.models.utilities.converters.DoubleConverter;
import org.semesteroppgave.models.utilities.inputhandler.InputValidation;
import org.semesteroppgave.models.utilities.search.ComponentSearch;

import java.io.IOException;

/**
 * Klasse for opprettelse av komponenter og redigering i tableview
 * Modell til AdminComponentController
 */

public class AdminCreateComponent {

    private final ObservableList<Component> createComponentList = FXCollections.observableArrayList(); //Liste over nye komponenter
    private final ObservableList<String> componentChoice = FXCollections.observableArrayList(); //Liste over komponenter i comboboksen
    private final ComponentSearch componentSearch;

    public AdminCreateComponent(ComponentSearch componentSearch) {
        this.componentSearch = componentSearch;
    }

    public ObservableList<Component> getCreateComponentList() {
        return this.createComponentList;
    }

    //Metode som oppretter komponent fra input og legger det inn i tableview. Tester for duplikater
    public void addComponent(TableView<Component> tableViewAddedConfig, TextField version, TextField price,
                             TextArea description, ComboBox<String> cbCreate) throws IllegalArgumentException {

        Component newComponent;
        switch (cbCreate.getValue()) {
            case "Motor":
                newComponent = new Motor(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Felg":
                newComponent = new Rim(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Setetrekk":
                newComponent = new SeatCover(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Spoiler":
                newComponent = new Spoiler(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Dekk":
                newComponent = new Tires(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Batteri":
                newComponent = new Battery(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Tank":
                newComponent = new FuelContainer(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Girboks":
                newComponent = new Gearbox(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            default:
                throw new InvalidComponentException("Fant ikke komponenten");
        }

        duplicateComponent(newComponent);

        createComponentList.add(newComponent);
        tableViewAddedConfig.setItems(createComponentList);
        version.clear();
        price.clear();
        description.clear();
    }

    //Teser om komponenter finnes fra før i de ulike listene
    private void duplicateComponent(Component component) throws IllegalArgumentException {

        for (Component createComponent : createComponentList) {
            if (createComponent.equals(component)) {
                throw new DuplicateException("Komponenten finnes allerede is opprettele av komponent listen");
            }
        }

        for (Component searchComponent : componentSearch.getSearchResult()) {
            if (searchComponent.equals(component)) {
                throw new DuplicateException("Komponenten finnes allerede i søkelisten");
            }
        }

        for (Component listComponent : ApplicationData.getInstance().getRegisterComponent().getComponentList()) {
            if (listComponent.equals(component)) {
                throw new DuplicateException("Komponenten finnes allerede i komponentlisten");
            }
        }
    }

    public void completeComponent() throws IOException {

        for (Component component : createComponentList) {
            ApplicationData.getInstance().getRegisterComponent().setComponentList(component);
        }
        createComponentList.clear();
        Main.setRoot("admincomponent");
    }

    //Ved endring av komponentnavn i tableview må komponenten konverteres til sin nye komponent-klasse
    private void editComponent(Component component) throws InvalidComponentException {
        Component newComponent = ComponentConverter.convert(component);
        int index = ApplicationData.getInstance().getRegisterComponent().getComponentList().indexOf(component);
        ApplicationData.getInstance().getRegisterComponent().getComponentList().remove(component);
        //Her plasseres det nye objektet på den tidligere plassen til det gamle objektet
        ApplicationData.getInstance().getRegisterComponent().getComponentList().add(index, newComponent);
    }

    /**
     * Metoder for redigering i tableview, som tester input. Avvik er også håndtert her
     *
     * @param event              får tak i verdien fra kolonne i tableviw
     * @param doubleStrConverter objekt i konverteringsklasse for double
     */

    public void editPriceColumn(TableColumn.CellEditEvent<Component, Double> event,
                                DoubleConverter.DoubleStringConverter doubleStrConverter, TableView<Component> tableViewAddedConfig) {
        try {
            if (doubleStrConverter.wasSuccessful()) {
                event.getRowValue().setPrice(event.getNewValue());
            }
        } catch (NullPointerException e) {
            Dialogs.showErrorDialog("Feil", "Feil i pris", "Du må fylle inn prisen");
        } catch (IllegalArgumentException e) {
            Dialogs.showErrorDialog("Feil", "Ugyldig pris: ", e.getMessage());
        }
        tableViewAddedConfig.refresh();
    }

    public void editComponentColumn(TableColumn.CellEditEvent<Component, String> event, TableView<Component> tableViewComponents) {
        try {
            InputValidation.testComponentCount(tableViewComponents, "endre");
            event.getRowValue().setComponent(InputValidation.testValidComponent(event.getNewValue()));
            editComponent(event.getTableView().getSelectionModel().getSelectedItem());
        } catch (InvalidComponentException e) {
            Dialogs.showErrorDialog("Redigeringsfeil", "Ugyldig komponent!", e.getMessage());
        } catch (InvalidDeleteException e) {
            Dialogs.showErrorDialog("Feil", "Du kan ikke endre komponenten!", e.getMessage());
        }
        tableViewComponents.refresh();
    }

    public void editVersionColumn(TableColumn.CellEditEvent<Component, String> event, TableView<Component> tableViewComponents) {
        try {
            String newValue = event.getNewValue();
            if (checkUniquenessVersion(InputValidation.testValidVersion(newValue))) {
                event.getRowValue().setVersion(newValue);
            } else {
                event.getRowValue().setVersion(event.getOldValue());
                Dialogs.showErrorDialog("Redigeringsfeil", "Duplisering av komponent",
                        newValue + " finnes fra før");
            }
            //JavaFX bug omvei
            event.getTableColumn().setVisible(false);
            event.getTableColumn().setVisible(true);

        } catch (InvalidVersionException | DuplicateException e) {
            Dialogs.showErrorDialog("Redigeringsfeil", "Ugyldig versjon!", e.getMessage());
        }
        tableViewComponents.refresh();
    }

    private boolean checkUniquenessVersion(String value) {
        return ApplicationData.getInstance().getRegisterComponent().getComponentList()
                .stream().noneMatch(item -> item.getVersion().equals(value));
    }

    //Sletting av komponenter fra tableview
    public void deleteRow(TableView<Component> tableViewComponents,
                          ObservableList<Component> list, boolean state) throws IllegalArgumentException {

        if (tableViewComponents.getSelectionModel().getSelectedItem() != null) {
            if (state) {
                //Hvis state er true er det adminComponentController som endres på
                //Her finner vi ut om det er mulig å slette en komponent
                //Det må være minst én av hver komponent
                InputValidation.testComponentCount(tableViewComponents, "slette flere av");
            }
            Dialogs.showConfirmationDialog("Du har valgt komponenten: "
                            + tableViewComponents.getSelectionModel().getSelectedItem().getComponent()
                            + ", versjon: " + tableViewComponents.getSelectionModel().getSelectedItem().getVersion(),
                    "Ønsker du virkelig å slette denne komponenten?",
                    response -> {
                        if (response == ButtonType.OK) {
                            list.remove(tableViewComponents.getSelectionModel().getSelectedItem());
                            //Tester om vi finner det samme objektet i søkerListen. Hvis så, sletter vi det også
                            for (Component component : componentSearch.getSearchResult()) {
                                if (component.equals(tableViewComponents.getSelectionModel().getSelectedItem())) {
                                    componentSearch.getSearchResult().remove(component);
                                    break;
                                }
                            }
                        }
                    });
        } else {
            Dialogs.showErrorDialog("Feil", "Du har ikke valgt en komponent", "Velg en komponent og prøv på nytt");
        }
    }

    //Legger inn verdier i combokox for valg av komponent
    public void loadChoice(ComboBox<String> cbCreate) {
        componentChoice.removeAll();
        componentChoice.addAll("Motor", "Felg", "Setetrekk", "Spoiler", "Dekk", "Batteri", "Tank", "Girboks");
        cbCreate.getItems().addAll(componentChoice);
        cbCreate.setValue(componentChoice.get(0));
    }
}
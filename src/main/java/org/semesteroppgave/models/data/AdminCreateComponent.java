package org.semesteroppgave.models.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.semesteroppgave.ApplicationData;
import org.semesteroppgave.Main;
import org.semesteroppgave.models.data.productcomponents.*;
import org.semesteroppgave.models.exceptions.*;
import org.semesteroppgave.models.utilities.alerts.Dialogs;
import org.semesteroppgave.models.utilities.inputhandler.InputValidation;

import java.io.IOException;

public class AdminCreateComponent {
    //Liste over alle komponentene
    private final ObservableList<Component> createComponentList = FXCollections.observableArrayList();
    //Liste over komponenter i comboboksen
    private final ObservableList<String> componentChoice = FXCollections.observableArrayList();
    private final ComponentSearch componentSearch;

    public AdminCreateComponent(ComponentSearch componentSearch) {
        this.componentSearch = componentSearch;
    }

    public ObservableList<Component> getCreateComponentList() {
        return this.createComponentList;
    }

    public void addComponent(Label lblMessage, TableView<Component> tableViewAddedConfig, TextField version, TextField price, TextArea description, ComboBox<String> cbCreate) throws IllegalArgumentException {
        lblMessage.setText("");
        Component newComponent = null;
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
                Dialogs.showErrorDialog("Legg til komponent", "Fant ikke komponenten", "Prøv igjen");
        }

        duplicateComponent(newComponent);

        if (newComponent != null) {
            createComponentList.add(newComponent);
            tableViewAddedConfig.setItems(createComponentList);
            lblMessage.setText("Komponenten er lagt til");
            version.clear();
            price.clear();
            description.clear();
        } else {
            Dialogs.showErrorDialog("Oups!", "Feil i oppretting av komponent", "Denne komponenten finnes fra før");
        }
    }

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

    private void convert(Component component) {
        Component newComponent;
        switch (component.getComponent()) {
            case "Motor":
                newComponent = new Motor(component.getVersion(), component.getPrice(), component.getDescription());
                break;
            case "Felg":
                newComponent = new Rim(component.getVersion(), component.getPrice(), component.getDescription());
                break;
            case "Setetrekk":
                newComponent = new SeatCover(component.getVersion(), component.getPrice(), component.getDescription());
                break;
            case "Spoiler":
                newComponent = new Spoiler(component.getVersion(), component.getPrice(), component.getDescription());
                break;
            case "Dekk":
                newComponent = new Tires(component.getVersion(), component.getPrice(), component.getDescription());
                break;
            case "Batteri":
                newComponent = new Battery(component.getVersion(), component.getPrice(), component.getDescription());
                break;
            case "Tank":
                newComponent = new FuelContainer(component.getVersion(), component.getPrice(), component.getDescription());
                break;
            case "Girboks":
                newComponent = new Gearbox(component.getVersion(), component.getPrice(), component.getDescription());
                break;
            default:
                throw new InvalidComponentException("Fant ikke komponenten");
        }
        int index = ApplicationData.getInstance().getRegisterComponent().getComponentList().indexOf(component);
        ApplicationData.getInstance().getRegisterComponent().getComponentList().remove(component);
        //Her plasserer jeg det nye objektet på den tidligere plassen til det gamle objektet
        ApplicationData.getInstance().getRegisterComponent().getComponentList().add(index, newComponent);
    }

    public void editPriceColumn(TableColumn.CellEditEvent<Component, Double> event, InputValidation.DoubleStringConverter doubleStrConverter, TableView<Component> tableViewAddedConfig) {
        try {
            if (doubleStrConverter.wasSuccessful()) {
                event.getRowValue().setPrice(event.getNewValue());
            }
        } catch (NumberFormatException e) {
            Dialogs.showErrorDialog("Feil,", "Feil i pris", "Du må skrive inn et positivt tall");
        } catch (IllegalArgumentException e) {
            Dialogs.showErrorDialog("Feil", "Ugyldig pris: ", e.getMessage());
        }
        tableViewAddedConfig.refresh();
    }

    public void editComponentColumn(TableColumn.CellEditEvent<Component, String> event, TableView<Component> tableViewComponents) {
        try {
            InputValidation.testComponentCount(tableViewComponents, "endre");
            event.getRowValue().setComponent(InputValidation.testValidComponent(event.getNewValue()));
            convert(event.getTableView().getSelectionModel().getSelectedItem());
        } catch (InvalidComponentException e) {
            Dialogs.showErrorDialog("Redigeringsfeil", "Ugyldig komponent!", e.getMessage());
            tableViewComponents.refresh();
        } catch (InvalidDeleteException e) {
            Dialogs.showErrorDialog("Feil", "Du kan ikke endre komponent!", e.getMessage());
        }
    }

    public void editVersionColumn(TableColumn.CellEditEvent<Component, String> event, TableView<Component> tableViewComponents) {
        try {
            String newValue = event.getNewValue();
            if (checkUniquenessVersion(InputValidation.testValidVersion(newValue))) {
                event.getRowValue().setVersion(newValue);
            } else {
                event.getRowValue().setVersion(event.getOldValue());
                Dialogs.showErrorDialog("Redigeringsfeil", "Duplisering av komponent", newValue + " finnes fra før");
            }
            //JavaFX bug omvei
            event.getTableColumn().setVisible(false);
            event.getTableColumn().setVisible(true);

        } catch (InvalidVersionException | DuplicateException e) {
            Dialogs.showErrorDialog("Redigeringsfeil", "Ugyldig versjon!", e.getMessage());
            tableViewComponents.refresh();
        }
    }

    private boolean checkUniquenessVersion(String value) {
        return ApplicationData.getInstance().getRegisterComponent().getComponentList().stream().noneMatch(item -> item.getVersion().equals(value));
    }

    public void deleteColumn(TableView<Component> tableViewComponents, ObservableList<Component> list, boolean state) throws IllegalArgumentException {

        if (tableViewComponents.getSelectionModel().getSelectedItem() != null) {
            if (state) {
                //Hvis state er true er det adminComponentController som endres på
                //Her finner vi ut om det er mulig å slette en komponent
                //Det må være minst én av hver komponent
                InputValidation.testComponentCount(tableViewComponents, "slette flere av");
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Bekreft");
            alert.setHeaderText("Du har valgt komponenten: " + tableViewComponents.getSelectionModel().getSelectedItem().getComponent()
                    + ", versjon: " + tableViewComponents.getSelectionModel().getSelectedItem().getVersion());
            alert.setContentText("Ønsker du virkerlig å slette denne komponenten?");
            alert.showAndWait().ifPresent(response -> {
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

    public void loadChoice(ComboBox<String> cbCreate) {
        componentChoice.removeAll();
        componentChoice.addAll("Motor", "Felg", "Setetrekk", "Spoiler", "Dekk", "Batteri", "Tank", "Girboks");
        cbCreate.getItems().addAll(componentChoice);
        cbCreate.setValue(componentChoice.get(0));

    }
}

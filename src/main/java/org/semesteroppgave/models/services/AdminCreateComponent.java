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
 * Class for component creation and tableview editing
 * Model for AdminComponentController
 */

public class AdminCreateComponent {

    private final ObservableList<Component> createComponentList = FXCollections.observableArrayList(); //List of new components
    private final ObservableList<String> componentChoice = FXCollections.observableArrayList(); //List of components in the combobox
    private final ComponentSearch componentSearch;

    public AdminCreateComponent(ComponentSearch componentSearch) {
        this.componentSearch = componentSearch;
    }

    public ObservableList<Component> getCreateComponentList() {
        return this.createComponentList;
    }

    //Method that creates component from input and inserts it into tableview. Tests for duplicates
    public void addComponent(TableView<Component> tableViewAddedConfig, TextField version, TextField price,
                             TextArea description, ComboBox<String> cbCreate) throws IllegalArgumentException {

        Component newComponent;
        switch (cbCreate.getValue()) {
            case "Motor":
                newComponent = new Motor(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Rim":
                newComponent = new Rim(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Seat cover":
                newComponent = new SeatCover(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Spoiler":
                newComponent = new Spoiler(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Tires":
                newComponent = new Tires(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Battery":
                newComponent = new Battery(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Fuel container":
                newComponent = new FuelContainer(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            case "Gearbox":
                newComponent = new Gearbox(version.getText(), Double.parseDouble(price.getText()), description.getText());
                break;
            default:
                throw new InvalidComponentException("Could not find component");
        }

        duplicateComponent(newComponent);

        createComponentList.add(newComponent);
        tableViewAddedConfig.setItems(createComponentList);
        version.clear();
        price.clear();
        description.clear();
    }

    //Tests if components are already included in the various lists
    private void duplicateComponent(Component component) throws IllegalArgumentException {

        for (Component createComponent : createComponentList) {
            if (createComponent.equals(component)) {
                throw new DuplicateException("The component is already in the creation of component list");
            }
        }

        for (Component searchComponent : componentSearch.getSearchResult()) {
            if (searchComponent.equals(component)) {
                throw new DuplicateException("The component is already in the search list");
            }
        }

        for (Component listComponent : ApplicationData.getInstance().getRegisterComponent().getComponentList()) {
            if (listComponent.equals(component)) {
                throw new DuplicateException("The component is already in the component list");
            }
        }
    }

    public void completeComponent() throws IOException {

        for (Component component : createComponentList) {
            ApplicationData.getInstance().getRegisterComponent().getComponentList().add(0, component);
        }
        createComponentList.clear();
        Main.setRoot("admincomponent");
    }

    //When changing the component name in tableview, the component must be converted to its new component class
    private void editComponent(Component component) throws InvalidComponentException {
        Component newComponent = ComponentConverter.convert(component);
        int index = ApplicationData.getInstance().getRegisterComponent().getComponentList().indexOf(component);
        ApplicationData.getInstance().getRegisterComponent().getComponentList().remove(component);
        //Here, the new object is placed in the previous location of the old object
        ApplicationData.getInstance().getRegisterComponent().getComponentList().add(index, newComponent);
    }

    /**
     * Methods for editing in tableview, which tests input. Exceptions are also handled here
     *
     * @param event              get value from column in tableviw
     * @param doubleStrConverter object in conversion class for double
     */

    public void editPriceColumn(TableColumn.CellEditEvent<Component, Double> event,
                                DoubleConverter.DoubleStringConverter doubleStrConverter, TableView<Component> tableViewAddedConfig) {
        try {
            if (doubleStrConverter.wasSuccessful()) {
                event.getRowValue().setPrice(event.getNewValue());
            }
        } catch (NullPointerException e) {
            Dialogs.showErrorDialog("Error", "Price error", "You must fill in the price");
        } catch (IllegalArgumentException e) {
            Dialogs.showErrorDialog("Error", "Invalid price: ", e.getMessage());
        }
        tableViewAddedConfig.refresh();
    }

    public void editComponentColumn(TableColumn.CellEditEvent<Component, String> event, TableView<Component> tableViewComponents) {
        try {
            InputValidation.testComponentCount(tableViewComponents, "change");
            event.getRowValue().setComponent(InputValidation.testValidComponent(event.getNewValue()));
            editComponent(event.getTableView().getSelectionModel().getSelectedItem());
        } catch (InvalidComponentException e) {
            Dialogs.showErrorDialog("Editing error", "Invalid component!", e.getMessage());
        } catch (InvalidDeleteException e) {
            Dialogs.showErrorDialog("Error", "You cannot change the component!", e.getMessage());
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
                Dialogs.showErrorDialog("Editing error", "Duplication of component",
                        newValue + " exists from before");
            }
            //JavaFX bug detour
            event.getTableColumn().setVisible(false);
            event.getTableColumn().setVisible(true);

        } catch (InvalidVersionException | DuplicateException e) {
            Dialogs.showErrorDialog("Editing error", "Invalid version!", e.getMessage());
        }
        tableViewComponents.refresh();
    }

    private boolean checkUniquenessVersion(String value) {
        return ApplicationData.getInstance().getRegisterComponent().getComponentList()
                .stream().noneMatch(item -> item.getVersion().equals(value));
    }

    //Deleting components from tableview
    public void deleteRow(TableView<Component> tableViewComponents,
                          ObservableList<Component> list, boolean state) throws IllegalArgumentException {

        if (tableViewComponents.getSelectionModel().getSelectedItem() != null) {
            if (state) {
                //If state is true, then it is adminComponentController that changes on
                //Here we find out if it is possible to delete a component
                //There must be at least one of each component
                InputValidation.testComponentCount(tableViewComponents, "delete more of");
            }
            Dialogs.showConfirmationDialog("You have selected the component: "
                            + tableViewComponents.getSelectionModel().getSelectedItem().getComponent()
                            + ", version: " + tableViewComponents.getSelectionModel().getSelectedItem().getVersion(),
                    "Do you really want to delete this component?",
                    response -> {
                        if (response == ButtonType.OK) {
                            list.remove(tableViewComponents.getSelectionModel().getSelectedItem());
                            //Tests whether we find the same object in the Search List. If so, we also delete it
                            for (Component component : componentSearch.getSearchResult()) {
                                if (component.equals(tableViewComponents.getSelectionModel().getSelectedItem())) {
                                    componentSearch.getSearchResult().remove(component);
                                    break;
                                }
                            }
                        }
                    });
        } else {
            Dialogs.showErrorDialog("Error", "You have not selected a component",
                    "Please select a component and try again");
        }
    }

    //Inserts values into combobox for component selection
    public void loadChoice(ComboBox<String> cbCreate) {
        componentChoice.removeAll();
        componentChoice.addAll("Motor", "Rim", "Seat cover", "Spoiler", "Tires", "Battery", "Fuel container", "Gearbox");
        cbCreate.getItems().addAll(componentChoice);
        cbCreate.setValue(componentChoice.get(0));
    }
}

package org.semesteroppgave.models.data.carcomponents;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import org.semesteroppgave.models.utilities.inputhandler.InputValidation;

import java.io.Serializable;
import java.util.Arrays;

public abstract class Component implements Serializable {

    private static final long serialVersionUID = 1;

    private transient SimpleStringProperty version;
    private transient SimpleDoubleProperty price;
    private transient SimpleStringProperty description;

    public Component(String version, double price, String desciption) {
        if (version.isBlank()) throw new NullPointerException("Du har glemt å fylle inn versjonen");
        if (desciption.isBlank()) throw new NullPointerException("Du har glemt å fylle inn beskrivelsen");
        this.version = new SimpleStringProperty(InputValidation.testValidVersion(version));
        this.description = new SimpleStringProperty(InputValidation.testValidDescription(desciption));
        this.price = new SimpleDoubleProperty(price);
    }

    public abstract String [] getModel();
    public abstract void setModel(String [] model);
    public abstract String getComponent();
    public abstract void setComponent(String component);

    public String getVersion(){
        return version.getValue();
    }

    public void setVersion(String version) {
        InputValidation.testValidVersion(version);
        this.version.set(version);
    }

    public void setVersion() {
        this.version = new SimpleStringProperty();
    }

    public double getPrice() {
        return price.getValue();
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public void setPrice() {
        this.price = new SimpleDoubleProperty();
    }

    public String getDescription(){
        return description.getValue();
    }

    public void setDescription(String description){
        this.description.set(description);
    }

    public void setDescription(){
        this.description = new SimpleStringProperty();
    }

    public String toFile(){
        return getVersion() + ";"+ getPrice() + ";" + getDescription();
    }

    @Override
    public boolean equals(Object obj) {
        //Bestemmer hva som skal til for at to objekter er like
        //Description kan være lik
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof Component) {
            Component component = (Component) obj;
            return (Arrays.equals(component.getModel(), getModel()) && component.getVersion().equals(version.getValue())
                    && component.getPrice() == price.doubleValue());
        }
        return false;
    }
}
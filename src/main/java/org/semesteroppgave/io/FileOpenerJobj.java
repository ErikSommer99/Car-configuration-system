package org.semesteroppgave.io;

import org.semesteroppgave.RegisterComponent;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileOpenerJobj implements FileOpener{

    @Override
    public void open(RegisterComponent registerComponent, Path filePath) throws IOException {
        try (InputStream fin = Files.newInputStream(filePath);
             ObjectInputStream oin = new ObjectInputStream(fin)) {

            RegisterComponent register = (RegisterComponent) oin.readObject();
            registerComponent.removeAll();
            register.getComponentsList().forEach(registerComponent::setComponentsList);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
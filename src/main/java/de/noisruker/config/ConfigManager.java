/*
 * ConfigManager
 * ConfigManager.java
 * Copyright © 2021 Fabius Mettner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.noisruker.config;

import de.noisruker.config.event.ConfigChangeAllowedEvent;
import de.noisruker.config.event.ConfigEntryChangeEvent;
import de.noisruker.event.EventManager;
import de.noisruker.logger.Settings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.controlsfx.control.ToggleSwitch;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;

import static de.noisruker.logger.Logger.LOGGER;

/**
 * Hier werden alle {@link ConfigElement Konfigurations Elemente} gespeichert
 * und verwaltet. Diese Klasse ermöglicht das Laden der initialisierten Werte,
 * einer Konfigurations-Datei, sowie das Schreiben einer Konfigurations-Datei.
 *
 * @author Fabius Mettner
 * @version 1.0
 * @category Config
 */
public class ConfigManager {

    /**
     * Die Instanz des {@link ConfigManager Konfigurations Managers}.
     */
    private static ConfigManager instance;

    /**
     * @return Die {@link #instance aktive Instanz} des Konfigurations-Managers.
     */
    public static ConfigManager getInstance() {
        return instance == null ? instance = new ConfigManager() : instance;
    }

    /**
     * Alle {@link ConfigElement Konfigurations-Elemente}, die registriert wurden.
     */
    private final ArrayList<Field> fields = new ArrayList<>();

    /**
     * Gibt das erste {@code Field} aus {@link #fields der Liste aller
     * Konfigurations-Elemente} aus, dessen Name mit dem mitgegebenen {@link String}
     * übereinstimmt zurück.
     *
     * @param name Die Benennung des {@link ConfigElement Konfigurations-Element}
     * @return Das gleichnamige {@link ConfigElement Konfigurations-Element}, oder
     * {@code null}, falls keines Vorhanden.
     */
    public Field getField(String name) {
        for (Field f : fields) {
            if (f.getName().equals(name))
                return f;
        }
        return null;
    }

    private Field getFieldByTextName(String fieldName) {
        for(Field f: fields) {
            ConfigElement element = f.getAnnotation(ConfigElement.class);
            if(element.name().equals(fieldName))
                return f;
        }
        return null;
    }


    /**
     * Fügt das zu registrierende {@link ConfigElement} in {@link #fields die Liste
     * aller Konfigurations-Elemente} ein, wenn es über die Annotation
     * {@link ConfigElement} verfügt.
     *
     * @param configElement Das zu registrierende {@link ConfigElement}
     * @throws IOException Sollte die Datei, die zu registrieren versucht wird,
     *                     nicht die Annotation {@link ConfigElement} besitzen, wird
     *                     eine IOException mit der Nachricht: "Not the right
     *                     annotation argument.", ausgegeben.
     */
    public void register(Field configElement) throws IOException {
        if (configElement.getAnnotation(ConfigElement.class) == null)
            throw new IOException("Not the right annotation argument.");
        fields.add(configElement);
    }

    /**
     * Registriert alle {@link Field Felder} der {@link Class Klasse}, die über die
     * Annotation {@link ConfigElement} verfügen über die Methode
     * {@link #register(Field)}.
     *
     * @param c Die {@link Class Klasse}, deren {@link Field Felder}, welche die
     *          Annotation {@link ConfigElement} tragen, registriert werden sollen.
     * @throws IOException Sollte ein Fehler beim Registrieren der Felder auftreten.
     */
    public void register(Class c) throws IOException {
        for (Field f : c.getFields()) {

            if (f.getAnnotation(ConfigElement.class) != null)
                this.register(f);

        }
    }

    /**
     * Lädt die unter {@link ConfigElement#defaultValue() dem Standartwert}
     * mitgegebenen Werte in die jeweiligen Felder, sollten diese nicht
     * standardmäßig über einen Wert verfügen.
     *
     * @implNote Diese Methode funktioniert nur bedingt. Daher ist es ratsam, die
     * Felder direkt zu initialisieren.
     */
    public void loadDefault() {
        this.fields.forEach(r -> {
            boolean a = r.isAccessible();
            r.setAccessible(true);
            try {
                if (r.get(this) == null) {
                    ConfigElement e = r.getAnnotation(ConfigElement.class);
                    String dv = e.defaultValue();

                    if (e.type().equals("check"))
                        r.set(this, Boolean.parseBoolean(dv));
                    else if (e.type().equals("count"))
                        r.set(this, Integer.parseInt(dv));
                    else if (e.type().equals("text"))
                        r.set(this, dv);
                    else
                        r.set(this, dv);

                    this.onConfigChanged(e.name(), dv);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            r.setAccessible(a);
        });

        String saveFolder = Paths.get(Settings.HOME_FOLDER, Settings.PROGRAMM_FOLDER).toString();

        if (!Files.exists(FileSystems.getDefault().getPath(saveFolder), LinkOption.NOFOLLOW_LINKS))
            new File(saveFolder).mkdir();


    }

    /**
     * <p>
     * Lädt eine Konfigurations Datei ein. Dabei werden die {@link #fields Elemente
     * aus der Liste aller Konfigurations-Elemente} auf den für sie vermerkten Wert
     * gesetzt.
     * </p>
     *
     * <p>
     * Die Konfigurationsdatei ist in {@code XML} zu schreiben. Dabei umschließt der
     * Parameter {@code config} die gesammte Konfigurationsdatei. Unter dem
     * Parameter {@code fields} können mithilfe des Parameters {@code field} und dem
     * folgenden Parameter {@code parameter} die einzelnen Konfigurationswerte
     * gesetzt werden. Dabei ist {@code name} der Name des zu setztenden Feldes.
     * {@code value} gibt den Wert an, auf den es gesetzt wird. Unter
     * {@code default} kann der Standartwert als orientierung angegeben werden und
     * unter {@code type} ist die Klasse vermerkt, in welche das {@code value}
     * konvertiert wird.
     * </p>
     *
     * @param input Der Pfad zu der einzulesenden Datei.
     * @throws SAXException Sollte ein Fehler in der Struktur der Datei vorliegen.
     * @throws IOException Sollte ein Fehler beim Einlesen der Datei auftreten
     */
    public void load(String input) throws SAXException, IOException {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        InputSource inputSource = new InputSource(new FileReader(input));

        xmlReader.setContentHandler(new FieldHandler());
        xmlReader.parse(inputSource);

    }

    /**
     * Exportiert die Daten der {@link #fields Elemente aus der Liste aller
     * Konfigurations-Elemente} in eine Datei nach dem unter {@link #load(String)}
     * erklärten Aufbau. Diese Datei ist von der Methode {@link #load(String)}
     * wieder einlesbar.
     *
     * @param output Der Pfad zu dem Exportiert wird.
     * @throws IOException Sollte es nicht möglich sein an den angegebenen Pfad zu
     *                     Schreiben, oder sollte output gleich {@code null} sein
     */
    public void save(File output) throws IOException {
        if (output == null)
            throw new IOException("No file to write to!");

        FileWriter fw;
        BufferedWriter bw;

        fw = new FileWriter(output);
        bw = new BufferedWriter(fw);

        bw.append("<config>");
        bw.newLine();
        bw.append(" <fields>");
        bw.newLine();

        fields.forEach(e -> {
            try {
                bw.append("  <field>");
                bw.newLine();

                bw.append("   <parameter>");
                bw.newLine();

                bw.append("    <name>").append(e.getName()).append("</name>");
                bw.newLine();
                bw.append("    <value>").append(String.valueOf(e.get(this))).append("</value>");
                bw.newLine();
                bw.append("    <default>").append(e.getAnnotation(ConfigElement.class).defaultValue()).append("</default>");
                bw.newLine();
                bw.append("    <type>").append(e.getAnnotation(ConfigElement.class).type().getTypeName()).append("</type>");
                bw.newLine();

                bw.append("   </parameter>");
                bw.newLine();

                bw.append("  </field>");
                bw.newLine();
            } catch (IllegalArgumentException | IOException | IllegalAccessException e1) {
                LOGGER.log(Level.SEVERE, "Fehler beim Erstellen der Config datei!", e1);
            }
        });

        bw.append(" </fields>");
        bw.newLine();

        bw.append("</config>");

        bw.close();

    }

    /**
     * Diese Methode baut einen Konfigurations-Baum auf und speichert diesen im mitgegebenen {@link TreeView}.
     * Anschließend verwaltet er auch das in der mitgegebenen {@link VBox} alle Werte,
     * die auf das im Tree ausgewählte Element passen angezeigt werden und verwaltet dort auftretenden Änderungen und
     * übernimmt diese in die Konfigurationseinstellungen.
     * Die mitgegebene {@link PropertyResourceBundle Sprachdatei} dient zur Übersetzung der Werte und muss, wenn angegeben <strong>alle</strong> keys der Konfigurationswerte enthalten.
     *
     * @param tree Der Baum in dem die Konfigurationsliste angezeigt wird.
     * @param configurations Die Box, in der die Konfigurationen zum ausgewählten Thema bearbeitet werden können.
     * @param language Die Sprachdatei mit den Übersetzungen der Werte oder {@code null}, wenn die Werte nicht übersetzt werden sollen.
     */
    public void createMenuTree(final TreeView<String> tree, final VBox configurations, final PropertyResourceBundle language) {
        TreeItem<String> root = new TreeItem<>(language != null ? language.getString("config.location.config") : "Settings");

        root.setExpanded(true);

        tree.setRoot(root);

        // Build tree
        for (Field f : this.fields) {
            if (f.getAnnotation(ConfigElement.class) == null)
                continue;
            ConfigElement e = f.getAnnotation(ConfigElement.class);

            TreeItem<String> actual = null;

            for (String s : e.location().split("\\.")) {
                if (actual == null) {
                    actual = root;
                    continue;
                }
                boolean found = false;
                for (TreeItem<String> ti : actual.getChildren()) {
                    if ((ti).getValue()
                            .equalsIgnoreCase(language != null ? language.getString("config.location." + s) : s)) {
                        actual = ti;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    TreeItem<String> nti = new TreeItem<>(language != null ? language.getString("config.location." + s) : s);

                    nti.setExpanded(true);
                    actual.getChildren().add(0, nti);
                    actual = nti;
                }
            }

        }

        // Build Config Elements when Tree Item is selected
        tree.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> this.displayConfigValues(tree, configurations, language)));
        tree.getSelectionModel().select(0);
    }

    private void displayConfigValues(final TreeView<String> tree, final VBox configurations, final PropertyResourceBundle language) {
        TreeItem<String> selected = tree.getSelectionModel().getSelectedItem();

        if (selected == null)
            return;
        StringBuilder location = new StringBuilder(selected.getValue());

        TreeItem<String> actual = selected;

        // Gets the config path position
        while (actual != null) {

            actual = actual.getParent();

            if (actual != null)
                location.insert(0, actual.getValue() + ".");
        }
        configurations.getChildren().clear();

        VBox checks = new VBox();
        checks.setPadding(new Insets(20, 0, 0, 0));
        Label checksLabel = new Label((language != null ? language.getString("config.booleans") : "Further Configurations") + ":");
        checksLabel.setWrapText(true);
        checks.getChildren().add(checksLabel);
        checks.setSpacing(20);

        for (Field f : this.fields) {

            // Checks if field is a config element
            if (f.getAnnotation(ConfigElement.class) == null)
                continue;
            ConfigElement e = f.getAnnotation(ConfigElement.class);

            // Checks if field should show up
            if (!e.visible())
                continue;

            // Builds the path of the Element
            StringBuilder fieldlocation = new StringBuilder();

            for (String s : e.location().split("\\."))
                if (!s.isEmpty())
                    fieldlocation.append(fieldlocation.toString().equals("") ? "" : ".").append(language != null ? language.getString("config.location." + s) : s);

            // Checks if this element is part of the configs for the selected tree item
            if (!fieldlocation.toString().equalsIgnoreCase(location.toString()))
                continue;


            if (e.type() == ConfigElementType.CHECK) {
                HBox check = new HBox();
                check.setAlignment(Pos.CENTER_LEFT);
                check.setSpacing(20);

                ToggleSwitch toggleSwitch = new ToggleSwitch();

                try {
                    toggleSwitch.setSelected(f.getBoolean(null));
                } catch (IllegalAccessException ignored) {
                }

                toggleSwitch.selectedProperty().addListener((o, oldValue, newValue) -> {
                    if (oldValue != newValue) {
                        Object message = EventManager.getInstance().triggerEvent(new ConfigChangeAllowedEvent(e.name(), oldValue.toString(), newValue.toString()));
                        if(message instanceof String) {
                            LOGGER.log(Level.WARNING, (String) message,
                                    new Exception(language == null ? "Config change denied" : "warning.config_change_denied"));
                            toggleSwitch.setSelected(oldValue);
                            return;
                        }
                        try {
                            f.set(null, newValue);
                        } catch (IllegalAccessException ignored) { }
                        this.onConfigChanged(e.name(), String.valueOf(newValue));
                    }
                });

                this.listeners.add(new ChangeEntry(e.name(), () -> {
                    try {
                        toggleSwitch.setSelected((Boolean) f.get(null));
                    } catch (IllegalArgumentException | IllegalAccessException ignored) {
                    }
                }));

                toggleSwitch.setPrefWidth(27.0);
                Tooltip t = new Tooltip(language != null ? language.getString("config." + e.description()): e.description());
                toggleSwitch.setTooltip(t);
                Label l = new Label(language != null ? language.getString("config." + e.name()) : e.name());
                l.setAlignment(Pos.CENTER);
                l.setPrefHeight(18);
                l.setWrapText(true);
                l.setTooltip(t);

                check.getChildren().addAll(toggleSwitch, l);

                checks.getChildren().addAll(check);
            } else if (e.type() == ConfigElementType.COUNT) {
                Spinner<Integer> cb = new Spinner<>();
                cb.setTooltip(new Tooltip(language != null ? language.getString("config." + e.description()) : e.description()));
                cb.setEditable(true);
                cb.setMaxWidth(Double.MAX_VALUE);
                this.listeners.add(new ChangeEntry(e.name(), () -> {
                    try {
                        cb.getValueFactory().setValue(f.getInt(null));
                    } catch (IllegalArgumentException | IllegalAccessException ignored) {
                    }
                }));
                try {
                    cb.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(this.minCounting.getOrDefault(e.name(), 0),
                            this.maxCounting.getOrDefault(e.name(), Integer.MAX_VALUE), f.getInt(null)));
                    cb.getValueFactory().valueProperty().addListener((o, oldValue, newValue) -> {
                        try {
                            if (!Objects.equals(oldValue, newValue)) {
                                Object message = EventManager.getInstance().triggerEvent(new ConfigChangeAllowedEvent(e.name(), oldValue.toString(), newValue.toString()));
                                if(message instanceof String) {
                                    LOGGER.log(Level.WARNING, (String) message,
                                            new Exception(language == null ? "Config change denied" : "warning.config_change_denied"));
                                    cb.getValueFactory().setValue(oldValue);
                                    return;
                                }
                                f.set(null, newValue);
                                this.onConfigChanged(e.name(), String.valueOf(newValue));
                            }

                        } catch (IllegalArgumentException | IllegalAccessException e1) {
                            e1.printStackTrace();
                        }
                    });
                } catch (IllegalArgumentException | IllegalAccessException e3) {
                    e3.printStackTrace();
                }

                Label l = new Label(((language != null ? language.getString("config." + e.name()) : e.name()) + ":"));
                l.setWrapText(true);
                l.autosize();
                configurations.getChildren().addAll(l, cb);
            } else if (e.type() == ConfigElementType.TEXT) {

                TextField cb = new TextField();
                cb.setTooltip(new Tooltip(language != null ? language.getString("config." + e.description()) : e.description()));
                cb.setMaxWidth(Double.MAX_VALUE);

                try {
                    cb.setText((String) f.get(null));
                } catch (IllegalArgumentException | IllegalAccessException e2) {
                    e2.printStackTrace();
                }

                this.listeners.add(new ChangeEntry(e.name(), () -> {
                    try {
                        cb.setText((String) f.get(null));
                    } catch (IllegalArgumentException | IllegalAccessException ignored) {

                    }
                }));

                cb.addEventHandler(KeyEvent.KEY_RELEASED, events -> {
                    try {
                        Object message = EventManager.getInstance().triggerEvent(new ConfigChangeAllowedEvent(e.name(), cb.getText(), cb.getText()));
                        if(message instanceof String) {
                            LOGGER.log(Level.WARNING, (String) message,
                                    new Exception(language == null ? "Config change denied" : "warning.config_change_denied"));
                            try {
                                cb.setText((String) f.get(null));
                            } catch (IllegalArgumentException | IllegalAccessException ignored) { }
                            return;
                        }

                        f.set(null, cb.getText());
                        this.onConfigChanged(e.name(), cb.getText());
                    } catch (IllegalArgumentException | IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                });
                Label l = new Label((language != null ? language.getString("config." + e.name()) : e.name() + ":"));
                l.setWrapText(true);
                l.autosize();

                configurations.getChildren().addAll(l, cb);
            } else if (e.type() == ConfigElementType.CHOOSE && this.options.containsKey(e.name())) {

                ComboBox<String> cb = new ComboBox<>();
                cb.setTooltip(new Tooltip(language != null ? language.getString("config." + e.description()) : e.description()));

                cb.setItems(FXCollections.observableArrayList(this.options.get(e.name())));

                cb.setMaxWidth(Double.MAX_VALUE);

                cb.setConverter(new StringConverter<>() {
                    @Override
                    public String toString(String s) {
                        if (language != null && language.containsKey(e.name() + "." + s))
                            return language.getString(e.name() + "." + s);
                        return s;
                    }

                    @Override
                    public String fromString(String s) {
                        if (language == null)
                            return s;
                        for (String string : language.keySet()) {
                            if (s.equals(language.getString(e.name() + "." + string)))
                                return string;
                        }
                        return s;
                    }
                });

                try {
                    cb.setValue((String) f.get(null));
                } catch (IllegalArgumentException | IllegalAccessException e2) {
                    e2.printStackTrace();
                }

                this.listeners.add(new ChangeEntry(e.name(), () -> {
                    try {
                        cb.setValue((String) f.get(null));
                    } catch (IllegalArgumentException | IllegalAccessException ignored) {

                    }
                }));

                cb.addEventHandler(ActionEvent.ANY, events -> {
                    try {
                        Object message = EventManager.getInstance().triggerEvent(new ConfigChangeAllowedEvent(e.name(), cb.getValue(), cb.getValue()));
                        if(message instanceof String) {
                            LOGGER.log(Level.WARNING, (String) message,
                                    new Exception(language == null ? "Config change denied" : "warning.config_change_denied"));
                            try {
                                cb.setValue((String) f.get(null));
                            } catch (IllegalArgumentException | IllegalAccessException ignored) { }
                            return;
                        }
                        f.set(null, cb.getValue());
                        this.onConfigChanged(e.name(), cb.getValue());
                    } catch (IllegalArgumentException | IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                });
                Label l = new Label((language != null ? language.getString("config." + e.name()) : e.name() + ":"));
                l.setWrapText(true);
                l.autosize();

                configurations.getChildren().addAll(l, cb);
            }
        }
        if (checks.getChildren().size() > 1)
            configurations.getChildren().add(checks);
    }

    private final HashMap<String, Integer> maxCounting = new HashMap<>();
    private final HashMap<String, Integer> minCounting = new HashMap<>();
    private final HashMap<String, String[]> options = new HashMap<>();

    /**
     * Setzt den maximalen und den minimalen Zahlenwert, den das Element mit dem Namen {@code name} haben kann.
     * @param name Der Name des Elements
     * @param min Der minimale Wert, den das Element haben darf
     * @param max Der maximale Wert, den das Element haben darf
     */
    public void registerIntegerRange(String name, int min, int max) {
        this.registerIntegerMin(name, min);
        this.registerIntegerMax(name, max);
    }

    /**
     * Setzt den maximalen Wert, den das Element {@code name} haben kann.
     * @param name Der Name des Elements
     * @param max Der maximale Wert, den das Element haben darf
     */
    public void registerIntegerMax(String name, int max) {
        this.maxCounting.put(name, max);
    }

    /**
     * Setzt den minimalen Wert, den das Element {@code name} haben kann.
     * @param name Der Name des Elements
     * @param min Der minimale Wert, den das Element haben darf
     */
    public void registerIntegerMin(String name, int min) {
        this.minCounting.put(name, min);
    }

    /**
     * Gibt die registrierten Werteigenschaften für das Konfigurationselement zurück. Diese werden nur angewandt, wenn das Element als type {@link ConfigElementType#CHOOSE} angegeben hat.
     * @param name Der Name des Elements
     * @return Die möglichen Werteigenschaften
     */
    public String[] getRegisteredOptions(String name) {
        if (this.options.containsKey(name))
            return this.options.get(name);
        return null;
    }

    /**
     * Setzt die Werteigenschaften für ein Element. Diese werden nur angewandt, wenn das Element als type {@link ConfigElementType#CHOOSE} angegeben hat.
     * @param name Der Name des Elements
     * @param options Die möglichen Werte, die der Benutzer auswählen darf
     */
    public void registerOptionParameters(String name, String... options) {
        this.options.put(name, options);
    }

    /**
     * Wird ausgeführt, wenn sich der Wert eines Konfigurationselements ändert
     * @param fieldName Der Name des Elements, das sich geändert hat
     * @param value Der neue Wert des Elements
     */
    public void onConfigChanged(String fieldName, String value) {
        for (ChangeEntry e : listeners)
            if (e.getForValue().equals(fieldName))
                e.getListener().onChange();

        // FOR THE EVENT MANAGER

        EventManager.getInstance().triggerEvent(new ConfigEntryChangeEvent(fieldName, value));
    }

    private final ArrayList<ChangeEntry> listeners = new ArrayList<>();

    private static class ChangeEntry {
        private final String s;
        private final ActionListener l;

        public ChangeEntry(String s, ActionListener l) {
            this.s = s;
            this.l = l;
        }

        public String getForValue() {
            return s;
        }

        public ActionListener getListener() {
            return l;
        }
    }

    /**
     * Löst für jedes Konfigurationselement {@link ConfigManager#onConfigChanged(String, String)} aus.
     */
    public void onConfigChangedGeneral() {
        for (Field f : this.fields) {
            if (f.getAnnotation(ConfigElement.class) == null)
                continue;
            ConfigElement e = f.getAnnotation(ConfigElement.class);
            try {
                this.onConfigChanged(e.name(), String.valueOf(f.get(null)));
            } catch (IllegalAccessException | IllegalArgumentException | NullPointerException ignored) { }
        }
    }

    private interface ActionListener {
        void onChange();
    }

}

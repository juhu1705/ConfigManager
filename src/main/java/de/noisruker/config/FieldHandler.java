/*
 * ConfigManager
 * FieldHandler.java
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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;

import java.lang.reflect.Field;

/**
 * Diese Klasse organisiert das Einlesen der Config Datei.
 *
 * @author Fabius Mettner
 * @version 1.0
 * @see ContentHandler
 * @see ConfigManager#load(String)
 */
class FieldHandler implements ContentHandler {

    /**
     * Zwischenspeicherung für die einzulesenden Daten.
     */
    private String value, defaultValue, type, name, currentValue;

    /**
     * Speichert die eingelesenden Werte zur Bearbeitung.
     */
    @Override
    public void characters(char[] arg0, int arg1, int arg2) {
        currentValue = new String(arg0, arg1, arg2);
    }

    @Override
    public void endDocument() {

    }

    /**
     * Lädt die Daten in das Programm.
     */
    @Override
    public void endElement(String arg0, String arg1, String arg2) {
        if (arg1.equals("name"))
            name = currentValue;
        if (arg1.equals("value"))
            value = currentValue;
        if (arg1.equals("default"))
            defaultValue = currentValue;
        if (arg1.equals("type"))
            type = currentValue;

        if (arg1.equals("field")) {
            Field f = ConfigManager.getInstance().getField(name);
            if (f == null)
                return;

            boolean a = f.isAccessible();
            f.setAccessible(true);
            try {

                ConfigElement e = f.getAnnotation(ConfigElement.class);

                if (e.type().equals(ConfigElementType.COUNT))
                    f.set(this, Integer.parseInt(value));
                else if (e.type().equals(ConfigElementType.CHECK))
                    f.set(this, Boolean.parseBoolean(value));
                else
                    f.set(this, value);

                ConfigManager.getInstance().onConfigChanged(e.name(), value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            f.setAccessible(a);
        }

    }

    @Override
    public void endPrefixMapping(String arg0) {

    }

    @Override
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2) {

    }

    @Override
    public void processingInstruction(String arg0, String arg1) {

    }

    @Override
    public void setDocumentLocator(Locator arg0) {

    }

    @Override
    public void skippedEntity(String arg0) {

    }

    @Override
    public void startDocument() {

    }

    @Override
    public void startElement(String arg0, String arg1, String arg2, Attributes arg3) {

    }

    @Override
    public void startPrefixMapping(String arg0, String arg1) {

    }

}

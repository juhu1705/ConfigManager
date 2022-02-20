/*
 * ConfigManager
 * ConfigElementType.java
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

import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;

import java.util.PropertyResourceBundle;

/**
 * Die möglichen Typen die ein Konfigurationselement haben kann.
 *
 * @author Juhu1705
 * @version 1.0
 */
public enum ConfigElementType {
    /**
     * Für Eigenschaften mit einer Liste aus möglichen Werten
     * <p>
     * Die angesprochene Liste muss via {@link ConfigManager#registerOptionParameters(String, String...)} gesetzt werden, ansonsten wird dieser Wert nicht im über {@link ConfigManager#createMenuTree(TreeView, VBox, PropertyResourceBundle)} generierten GUI angezeigt.
     */
    CHOOSE("choose"),
    /**
     * Für wahr/falsch Eigenschaften
     */
    CHECK("check"),
    /**
     * Für integer Zahlenwerte
     * <p>
     * Über die Methode {@link ConfigManager#registerIntegerRange(String, int, int)} oder die Methoden: {@link ConfigManager#registerIntegerMax(String, int)}, {@link ConfigManager#registerIntegerMin(String, int)}, können die maximale und minimale Größe des Wertes gesetzt werden.
     */
    COUNT("count"),
    /**
     * Für einfachen Text
     */
    TEXT("text");

    private final String typeName;

    ConfigElementType(String typeName) {
        this.typeName = typeName;
    }

    /**
     * @return The name of the ElementType
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * @return {@link ConfigElementType#getTypeName()}
     */
    @Override
    public String toString() {
        return this.getTypeName();
    }
}

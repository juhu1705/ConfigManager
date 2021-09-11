/*
 * ConfigManager
 * ConfigEntryChangeEvent.java
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

package de.noisruker.config.event;

import de.noisruker.event.events.Event;

/**
 * Dieses Event wird immer dann ausgelöst, wenn ein Konfigurationswert geändert wird. Dabei entspricht das entryValue dem neuen Wert und der entryName dem Namen der Eigenschaft die aktualisiert wurde.
 *
 * @author Juhu1705
 * @version 1.0
 * @category Config
 */
public class ConfigEntryChangeEvent extends Event {

    private final String entryName, entryValue;

    public ConfigEntryChangeEvent(String entryName, String entryValueAsString) {
        super("ConfigEntryChange");
        this.entryName = entryName;
        this.entryValue = entryValueAsString;
    }

    /**
     * @return Der Name des Konfigurationswertes
     */
    public String getEntryName() {
        return entryName;
    }

    /**
     * @return Der neue Wert des Konfigurationswertes
     */
    public String getEntryValue() {
        return entryValue;
    }

}

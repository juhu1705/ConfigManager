/*
 * ConfigManager
 * ConfigChangeAllowedEvent.java
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
 * Dieses Event wird immer dann ausgelöst, wenn ein Konfigurationswert geändert werden soll. Ist der Wert nicht valide, kann er über aufruf der Methode {@link ConfigChangeAllowedEvent#denyChange(String)} verboten werden!
 *
 * @author Juhu1705
 * @version 1.0
 */
public class ConfigChangeAllowedEvent extends Event<String> {

    private final String entryName, entryValue, newEntryValueAsString;

    /**
     * Dient als Event zur Überprüfung von Konfigurationseigenschaftsänderungen
     *
     * @param entryName Der Name des zu ändernden Eigenschaft
     * @param entryValueAsString Der jetzige Wert der Eigenschaft
     * @param newEntryValueAsString Der neu zu setzende Wert der Eigenschaft
     */
    public ConfigChangeAllowedEvent(String entryName, String entryValueAsString, String newEntryValueAsString) {
        super("ConfigEntryChange");
        this.entryName = entryName;
        this.entryValue = entryValueAsString;
        this.newEntryValueAsString = newEntryValueAsString;
    }

    /**
     * @return Den Namen der zu änderten Konfigurationseigenschaft
     */
    public String getEntryName() {
        return entryName;
    }

    /**
     * @return Den Wert zu dem die Eigenschaft geändert werden soll
     */
    public String getNewEntryValue() {
        return this.newEntryValueAsString;
    }

    /**
     * @return Der jetzige Wert der Konfigurationseigenschaft
     */
    public String getEntryValue() {
        return this.entryValue;
    }

    /**
     * Verbietet die Änderung des Wertes
     * @param description Eine kurze Beschreibung wieso dieser Wert verboten wurde.
     */
    public void denyChange(String description) {
        super.setResult(description);
    }

    /**
     * @return Ob die änderung momentan erlaubt ist
     */
    public boolean isAllowed() {
        return super.getResult() == null;
    }

    /**
     * Erlaubt die Änderung des Konfigurationswertes
     */
    public void allowChange() {
        super.setResult(null);
    }

}

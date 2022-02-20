/*
 * ConfigManager
 * ConfigElement.java
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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Diese Annotation verifiziert ein Attribut, als Konfigurationsattribut. Dabei
 * muss das Attribut {@code public static}, also öffentlich und statisch sein.
 * Das Config Elements wird über die Klasse {@link ConfigManager Konfigurations
 * Manager} registriert. Konfigurationsattribute, die ein {@code String},
 * {@code Integer}, oder {@code Boolean} als Wert aufweisen, werden automatisch
 * im GUI unter dem Reiter Einstellungen zu finden sein. Dabei ist
 * {@link #description() die Beschreibung} als Hovertext und {@link #name() der
 * Name} als Benennung eingefügt. Hierbei werden diese beiden eingegebenen
 * Strings durch den String aus der verwendeten Sprachdatei ersetzt. Alle
 * registrierten Konfigurationselemente werden in der Config-Datei unter
 * "%localappdata%/CaRP/config.cfg" gespeichert.
 *
 * @author Juhu1705
 * @version 1.0
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface ConfigElement {

    /**
     * @return Den Standardmäßig gesetzte Initialwert.
     * <p>
     * Nur Strings werden automatisch richtig initialisiert. Bitte den
     * gewünschten Wert standardmäßig einprogrammieren. Dieser wird
     * überschrieben, sobald die Konfigurationsdatei geladen wird.
     */
    String defaultValue();

    /**
     * @return Den Objekttypen dieser Klasse. Wenn primäre Datentypen wie int
     * benutzt werden, dann kann hier die Klasse Integer.class verwendet
     * werden.
     */
    ConfigElementType type();

    /**
     * @return Den in den Sprachdateien hinterlegten Übersetzungsstring für die
     * Beschreibung der Konfiguration
     * <p>
     * Der hinterlegte String muss, damit dass Programm läuft, in den
     * Sprachdateien hinterlegt sein, sollten diese genutzt werden.
     */
    String description();

    /**
     * @return Den in den Sprachdateien hinterlegten Key zum Übersetzten des Namens.
     * <p>
     * Der hinterlegte String muss, damit dass Programm läuft in den
     * Sprachdateien hinterlegt sein, sollten diese genutzt werden.
     */
    String name();

    /**
     * @return Die Position im Baumsystem, unter der die Config zu finden ist.
     * Der hinterlegte String muss, damit dass Programm läuft in den
     * Sprachdateien hinterlegt sein, sollten diese genutzt werden. Ein "." trennt die Strings. Jeder
     * Einzelstring wird mit "String".location gesucht.
     */
    String location();

    /**
     * @return Ob die Einstellung in den Einstellungen sichtbar ist
     */
    boolean visible();

}
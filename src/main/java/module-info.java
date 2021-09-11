module ConfigManager.config {
    requires java.base;
    requires org.controlsfx.controls;
    requires de.noisruker.event;
    requires de.noisruker.logger;
    requires java.xml;
    requires java.logging;

    exports de.noisruker.config;
    exports de.noisruker.config.event;
}
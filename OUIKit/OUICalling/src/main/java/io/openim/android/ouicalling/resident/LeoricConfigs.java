

package io.openim.android.ouicalling.resident;

public class LeoricConfigs {

    public final LeoricConfig PERSISTENT_CONFIG;
    public final LeoricConfig DAEMON_ASSISTANT_CONFIG;

    public LeoricConfigs(LeoricConfig persistentConfig, LeoricConfig daemonAssistantConfig) {
        this.PERSISTENT_CONFIG = persistentConfig;
        this.DAEMON_ASSISTANT_CONFIG = daemonAssistantConfig;
    }

    public static class LeoricConfig {

        final String processName;
        final String serviceName;

        public LeoricConfig(String processName, String serviceName) {
            this.processName = processName;
            this.serviceName = serviceName;
        }
    }
}

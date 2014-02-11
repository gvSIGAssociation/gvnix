package org.gvnix.gva.security.providers;

public class SecurityProviderId {

    private String name;
    private String description;
    private String className;

    public SecurityProviderId(SecurityProvider provider) {
        this.name = provider.getName();
        this.description = provider.getDescription();
        this.className = provider.getClass().getCanonicalName();
    }

    public String getId() {
        return this.name;
    }

    public String getDescription() {
        return description;
    }

    public boolean is(SecurityProvider provider) {
        return name.equals(provider.getName())
                && className.equals(provider.getClass().getCanonicalName());
    }

}

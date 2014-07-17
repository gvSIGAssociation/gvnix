package org.gvnix.addon.jpa.geo.providers;

public class GeoProviderId {

    private String name;
    private String description;
    private String className;

    public GeoProviderId(GeoProvider provider) {
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

    public boolean is(GeoProvider provider) {
        return name.equals(provider.getName())
                && className.equals(provider.getClass().getCanonicalName());
    }

}

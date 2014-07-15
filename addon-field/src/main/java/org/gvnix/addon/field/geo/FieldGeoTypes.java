package org.gvnix.addon.field.geo;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Example of an enum used for tab-completion of properties.
 * 
 * @since 1.1.1
 */
public enum FieldGeoTypes {
    AUSTRALIA("Australia"), UNITED_STATES("United States"), GERMANY("Germany"), NOT_SPECIFIED(
            "None of your business!");

    private String propertyName;

    private FieldGeoTypes(String propertyName) {
        Validate.notBlank(propertyName, "Property name required");
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("propertyName", propertyName);
        return builder.toString();
    }
}
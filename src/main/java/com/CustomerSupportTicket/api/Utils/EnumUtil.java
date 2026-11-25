package com.CustomerSupportTicket.api.Utils;

public enum EnumUtil {

    // DB Operations
    ADD("add"),
    EDIT("edit"),
    DELETE("delete"),
    EXISTS("exists"),

    // Response Status
    ERROR("error"),
    SUCCESS("success");

    private final String value;

    EnumUtil(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
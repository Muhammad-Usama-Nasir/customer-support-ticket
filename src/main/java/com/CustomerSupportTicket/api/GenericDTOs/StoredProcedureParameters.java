package com.CustomerSupportTicket.api.GenericDTOs;

import jakarta.persistence.ParameterMode;

public record StoredProcedureParameters(String paramName, Object paramValue, Class<?> paramType,
        ParameterMode parameterMode) {

    public StoredProcedureParameters(String paramName, Object paramValue, Class<?> paramType) {
        this(paramName, paramValue, paramType, ParameterMode.IN);
    }
}
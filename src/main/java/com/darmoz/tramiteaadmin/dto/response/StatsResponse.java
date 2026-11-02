package com.darmoz.tramiteaadmin.dto.response;

/** Tarjetas del dashboard. */
public record StatsResponse(long procedures, long categories, long institutions, long suggestions,
                            long registeredUsers) {
}

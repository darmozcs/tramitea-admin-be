package com.darmoz.tramiteaadmin.service;

final class Specs {

    private Specs() {
    }

    /** Para usar en LIKE con '\' como escape. */
    static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}

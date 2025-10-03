package com.lyttledev.lyttleessentials.utils.SelectorUtil;

import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result wrapper for selector resolution with status and optional message.
 * Avoids conflating "no permission", "invalid selector", and "no matches".
 */
final class SelectorResult {

    enum Status {
        OK,
        NO_PERMISSION,
        INVALID_INPUT,
        INVALID_SELECTOR,
        INVALID_CONTEXT,
        NOT_ON_MAIN_THREAD,
        NO_MATCHES,
        ERROR
    }

    private final Status status;
    private final String message;
    private final List<Entity> entities;

    private SelectorResult(Status status, String message, List<Entity> entities) {
        this.status = status;
        this.message = message;
        this.entities = entities == null ? Collections.emptyList() : Collections.unmodifiableList(entities);
    }

    static SelectorResult ok(List<? extends Entity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new SelectorResult(Status.NO_MATCHES, "No entities matched the selector.", Collections.emptyList());
        }
        return new SelectorResult(Status.OK, "", new ArrayList<>(entities));
    }

    static SelectorResult noPermission(String message) {
        return new SelectorResult(Status.NO_PERMISSION, message, Collections.emptyList());
    }

    static SelectorResult invalidInput(String message) {
        return new SelectorResult(Status.INVALID_INPUT, message, Collections.emptyList());
    }

    static SelectorResult invalidSelector(String message) {
        return new SelectorResult(Status.INVALID_SELECTOR, message, Collections.emptyList());
    }

    static SelectorResult invalidContext(String message) {
        return new SelectorResult(Status.INVALID_CONTEXT, message, Collections.emptyList());
    }

    static SelectorResult notOnMainThread() {
        return new SelectorResult(Status.NOT_ON_MAIN_THREAD, "Selector resolution must run on the main server thread.", Collections.emptyList());
    }

    static SelectorResult noMatches() {
        return new SelectorResult(Status.NO_MATCHES, "No entities matched the selector.", Collections.emptyList());
    }

    static SelectorResult error(String message) {
        return new SelectorResult(Status.ERROR, message, Collections.emptyList());
    }

    Status getStatus() {
        return status;
    }

    String getMessage() {
        return message;
    }

    List<Entity> getEntities() {
        return entities;
    }

    boolean isOk() {
        return status == Status.OK || status == Status.NO_MATCHES;
    }
}

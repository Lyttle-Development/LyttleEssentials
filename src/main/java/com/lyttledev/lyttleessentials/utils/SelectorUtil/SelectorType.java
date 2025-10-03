package com.lyttledev.lyttleessentials.utils.SelectorUtil;

/**
 * High-level selector/alias kinds used for permission enforcement.
 */
enum SelectorType {
    ALL,            // @a or *
    RANDOM,         // @r
    ENTITIES,       // @e
    NEAREST,        // @p
    SELF,           // @s
    EXCEPT_SELF,    // ** (custom alias)
    DIRECT_NAME,    // Exact player name
    UUID,           // Entity UUID
    UNKNOWN;

    static SelectorType fromInput(String input) {
        if (input == null) return UNKNOWN;
        String s = input.trim();
        if (s.equals("*") || s.equalsIgnoreCase("@a")) return ALL;
        if (s.equals("**")) return EXCEPT_SELF;
        if (s.equalsIgnoreCase("@r")) return RANDOM;
        if (s.equalsIgnoreCase("@p")) return NEAREST;
        if (s.equalsIgnoreCase("@s")) return SELF;
        if (s.startsWith("@e")) return ENTITIES;
        if (s.startsWith("@a")) return ALL;
        if (s.startsWith("@r")) return RANDOM;
        if (s.startsWith("@p")) return NEAREST;
        if (s.startsWith("@s")) return SELF;
        return UNKNOWN;
    }
}

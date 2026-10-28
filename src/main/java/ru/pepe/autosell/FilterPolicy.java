package ru.pepe.autosell;

/** Side-effect-free filter policy, shared by configuration and regression tests. */
final class FilterPolicy {
    static boolean allowed(boolean allowlist, boolean listed, boolean protectSpecial, boolean special) {
        return allowlist == listed && (!protectSpecial || !special);
    }
}

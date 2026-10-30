package ru.pepe.autosell;

import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FabricDistributionTest {
    @Test void metadataTargetsRequestedLoaderAndClientOnly() throws Exception {
        try (var stream = getClass().getResourceAsStream("/fabric.mod.json")) {
            assertNotNull(stream);
            var json = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("Hype auto sell", json.get("name").getAsString());
            assertEquals("pepe", json.get("id").getAsString()); // Preserve upgrade/config compatibility.
            assertEquals("client", json.get("environment").getAsString());
            assertEquals("26.3", json.getAsJsonObject("depends").get("minecraft").getAsString());
            assertEquals(">=0.19.5", json.getAsJsonObject("depends").get("fabricloader").getAsString());
            assertEquals("1.1.0", json.get("version").getAsString());
        }
        assertNull(getClass().getResource("/META-INF/neoforge.mods.toml"));
    }
    @Test void noNeoForgeReferencesLeakIntoFabricClasses() throws Exception {
        try (var files = Files.walk(Path.of("build/classes/java/main"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".class")).toList()) {
                String pool = new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1);
                assertFalse(pool.contains("net/neoforged/"), "Loader dependency leaked: " + file);
            }
        }
    }
    @Test void allSoundsAndFlybySpriteAreBundled() throws Exception {
        try (var stream = getClass().getResourceAsStream("/assets/pepe/sounds.json")) {
            assertNotNull(stream);
            var sounds = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            for (var entry : sounds.entrySet()) {
                for (var sound : entry.getValue().getAsJsonObject().getAsJsonArray("sounds")) {
                    String name = sound.isJsonPrimitive() ? sound.getAsString() : sound.getAsJsonObject().get("name").getAsString();
                    assertNotNull(getClass().getResource("/assets/" + name.replace(":", "/sounds/") + ".ogg"), name);
                }
            }
        }
        assertNotNull(getClass().getResource("/assets/pepe/textures/gui/villager_flyby.png"));
    }
}

package ru.pepe.autosell;

import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArcadeUpgradeTest {
    @Test void impactCameraIsNeutralAtRestAndResetsImmediately() {
        var zoom = new ImpactZoom();
        assertEquals(1, zoom.multiplier());
        zoom.trigger(true);
        float minimum = 1;
        for (int i=0;i<22;i++) { minimum=Math.min(minimum,zoom.multiplier()); zoom.tick(); }
        assertTrue(minimum >= 0.9649f && minimum < 0.98f);
        assertEquals(1, zoom.multiplier());
        zoom.trigger(false);zoom.tick();zoom.clear();
        assertEquals(1, zoom.multiplier());
    }
    @Test void allEighteenPortraitsAndAnimationFramesAreBundled() throws Exception {
        assertEquals(18, PortraitArt.NAMES.length);
        for (String name : PortraitArt.NAMES) {
            try(var stream=getClass().getResourceAsStream("/assets/pepe/textures/gui/face_"+name+".png")) {
                assertNotNull(stream,name);
                var image=ImageIO.read(stream);
                assertEquals(128,image.getWidth());assertEquals(128,image.getHeight());
            }
        }
        for(int i=0;i<8;i++) {
            String name=i==0?"villager_flyby.png":"villager_flyby_"+i+".png";
            try(var stream=getClass().getResourceAsStream("/assets/pepe/textures/gui/"+name)) {
                assertNotNull(stream,name);
                var image=ImageIO.read(stream);
                assertEquals(128,image.getWidth());assertEquals(256,image.getHeight());
                assertEquals(0,image.getRGB(0,0)>>>24);
            }
        }
    }
    @Test void animatedAlphabetUsesLegalLowercaseResourcePaths() throws Exception {
        for(char c : "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray()) for(int i=0;i<6;i++) {
            String path="/assets/pepe/textures/gui/type/"+c+"_"+i+".png";
            try(var stream=getClass().getResourceAsStream(path)) { assertNotNull(stream,path); }
        }
    }
    @Test void portraitSelectionAlwaysResolvesToAnExistingAssetName() {
        var names=java.util.List.of(PortraitArt.NAMES);
        for(var emotion:SaleFeedback.Emotion.values()) for(int tick=0;tick<500;tick++)
            assertTrue(names.contains(PortraitArt.name(emotion,tick,tick/5)));
    }
    @Test void jackpotReplacesOldSoundEvent() throws Exception {
        try(var stream=getClass().getResourceAsStream("/assets/pepe/sounds.json")) {
            assertNotNull(stream);
            String json=new String(stream.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
            assertTrue(json.contains("slotmachinejackpot"));assertFalse(json.contains("musor_drop"));
        }
        assertNotNull(getClass().getResource("/assets/pepe/sounds/slotmachinejackpot.ogg"));
    }
}

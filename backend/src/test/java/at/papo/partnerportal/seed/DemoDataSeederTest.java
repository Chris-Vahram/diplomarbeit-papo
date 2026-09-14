package at.papo.partnerportal.seed;

import io.quarkus.runtime.LaunchMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoDataSeederTest {

    @Test
    void normalModeNeedsOptInAndExistingDataAlwaysBlocksSeeding() {
        assertFalse(DemoDataSeeder.shouldSeed(LaunchMode.NORMAL, 0));
        assertTrue(DemoDataSeeder.shouldSeed(LaunchMode.NORMAL, 0, true));
        assertFalse(DemoDataSeeder.shouldSeed(LaunchMode.NORMAL, 1, true));
        assertFalse(DemoDataSeeder.shouldSeed(LaunchMode.DEVELOPMENT, 1));
        assertFalse(DemoDataSeeder.shouldSeed(LaunchMode.TEST, 1));
        assertTrue(DemoDataSeeder.shouldSeed(LaunchMode.DEVELOPMENT, 0));
        assertTrue(DemoDataSeeder.shouldSeed(LaunchMode.TEST, 0));
        assertTrue(DemoDataSeeder.shouldSeedCompanyProfile(0));
        assertFalse(DemoDataSeeder.shouldSeedCompanyProfile(1));
    }
}

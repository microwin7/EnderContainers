package fr.utarwyn.endercontainers.dependency;

import fr.utarwyn.endercontainers.mock.DependencyMock;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DependencyResolverTest {

    @Mock
    private PluginManager pluginManager;

    @Mock
    private Plugin plugin;

    @Before
    public void setUp() {
        // Create a fake description file for the plugin
        PluginDescriptionFile descriptionFile = mock(PluginDescriptionFile.class);
        when(descriptionFile.getVersion()).thenReturn("1.12.5");

        // Plugin manager stubs
        when(this.plugin.getDescription()).thenReturn(descriptionFile);
        when(this.pluginManager.isPluginEnabled("Plugin")).thenReturn(true);
        when(this.pluginManager.getPlugin("Plugin")).thenReturn(this.plugin);
    }

    @Test
    public void resolveFailure() {
        DependencyResolver resolver = new DependencyResolver(this.pluginManager);

        // Empty name
        assertThatNullPointerException().isThrownBy(resolver::resolve)
                .withNoCause().withMessageContaining("name");

        // Empty patterns
        resolver.name("NoPlugin");
        assertThatNullPointerException().isThrownBy(resolver::resolve)
                .withNoCause().withMessageContaining("matcher");

        // Disabled plugin
        resolver.use(Dependency.class);
        assertThat(resolver.resolve()).isEmpty();

        // Wrong dependency class with enabled plugin
        resolver.name("Plugin");
        assertThat(resolver.resolve()).isEmpty();
    }

    @Test
    public void resolveWithUse() {
        DependencyResolver resolver = new DependencyResolver(this.pluginManager)
                .name("Plugin").use(DependencyMock.class);

        Optional<Dependency> dependency = resolver.resolve();

        assertThat(dependency).isNotEmpty();
        assertThat(dependency.get().getPlugin())
                .isNotNull().isEqualTo(this.plugin);

        Dependency dep = dependency.get();
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> dep.validateBlockChestOpening(null, null))
                .withNoCause().withMessage(null);
    }

    @Test
    public void resolveWithMatcher() {
        DependencyResolver resolver = new DependencyResolver(this.pluginManager)
                .name("Plugin").matchVersion("^2.*", Dependency.class);

        // Unknown version
        assertThat(resolver.resolve()).isEmpty();

        // Matched version
        resolver.matchVersion("^1.*", DependencyMock.class);
        assertThat(resolver.resolve()).isNotEmpty();
    }

}

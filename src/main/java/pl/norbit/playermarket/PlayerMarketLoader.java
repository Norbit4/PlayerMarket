package pl.norbit.playermarket;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.lang.reflect.Field;

public class PlayerMarketLoader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        addMavenRepository(resolver);

        resolver.addDependency(new Dependency(
                        new DefaultArtifact("mysql:mysql-connector-java:8.0.33"),
                        null)
        );
        resolver.addDependency(new Dependency(
                        new DefaultArtifact("commons-dbutils:commons-dbutils:1.7"),
                        null)
        );
        resolver.addDependency(new Dependency(
                        new DefaultArtifact("com.h2database:h2:2.2.220"),
                        null)
        );
        resolver.addDependency(new Dependency(
                        new DefaultArtifact("org.yaml:snakeyaml:2.0"),
                        null)
        );
        classpathBuilder.addLibrary(resolver);
    }

    private void addMavenRepository(MavenLibraryResolver resolver) {
        try {
            MavenLibraryResolver.class.getField("MAVEN_CENTRAL_DEFAULT_MIRROR");

            resolver.addRepository(new RemoteRepository.Builder(
                            "central",
                            "default",
                            MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR
                    ).build()
            );
        } catch (Exception ignored) {
            resolver.addRepository(
                    new RemoteRepository.Builder(
                            "central",
                            "default",
                            "https://repo.maven.apache.org/maven2/"
                    ).build()
            );
        }
    }
}

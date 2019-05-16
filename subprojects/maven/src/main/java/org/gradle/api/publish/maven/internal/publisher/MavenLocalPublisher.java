/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.publish.maven.internal.publisher;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.artifacts.mvnsettings.LocalMavenRepositoryLocator;
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransport;
import org.gradle.api.internal.artifacts.repositories.transport.RepositoryTransportFactory;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.internal.Factory;
import org.gradle.internal.resource.ExternalResourceName;
import org.gradle.internal.resource.ExternalResourceRepository;

import java.io.File;
import java.net.URI;
import java.util.Collections;

public class MavenLocalPublisher extends AbstractMavenPublisher {
    private final LocalMavenRepositoryLocator mavenRepositoryLocator;

    public MavenLocalPublisher(Factory<File> temporaryDirFactory, RepositoryTransportFactory repositoryTransportFactory, LocalMavenRepositoryLocator mavenRepositoryLocator) {
        super(temporaryDirFactory, repositoryTransportFactory);
        this.mavenRepositoryLocator = mavenRepositoryLocator;
    }

    @Override
    public void publish(MavenNormalizedPublication publication, MavenArtifactRepository artifactRepository) {
        URI rootUri = mavenRepositoryLocator.getLocalMavenRepository().toURI();
        String protocol = rootUri.getScheme().toLowerCase();
        RepositoryTransport transport = repositoryTransportFactory.createTransport(protocol, "mavenLocal", Collections.emptyList());
        ExternalResourceRepository repository = transport.getRepository();

        publish(publication, repository, rootUri, true);
    }

    @Override
    protected Metadata createSnapshotMetadata(MavenNormalizedPublication publication, String groupId, String artifactId, String version, ExternalResourceRepository repository, ExternalResourceName metadataResource) {
        Metadata metadata = new Metadata();
        metadata.setModelVersion("1.1.0");
        metadata.setGroupId(groupId);
        metadata.setArtifactId(artifactId);
        metadata.setVersion(version);

        Snapshot snapshot = new Snapshot();
        snapshot.setLocalCopy(true);
        Versioning versioning = new Versioning();
        versioning.updateTimestamp();
        versioning.setSnapshot(snapshot);

        for (MavenArtifact artifact : publication.getAllArtifacts()) {
            SnapshotVersion sv = new SnapshotVersion();
            sv.setClassifier(artifact.getClassifier());
            sv.setExtension(artifact.getExtension());
            sv.setVersion(version);
            sv.setUpdated(versioning.getLastUpdated());

            versioning.getSnapshotVersions().add(sv);
        }

        metadata.setVersioning(versioning);

        return metadata;
    }
}

package com.gridpadel.domain.port;

import com.gridpadel.domain.model.Tournament;

import java.util.List;

public interface BracketPublishPort {
    /**
     * Publishes tournament brackets as HTML to a remote hosting service.
     * @param tournaments the tournaments to publish
     * @param repoUrl the git remote URL of the repository
     * @return the public URL where brackets are accessible
     */
    String publish(List<Tournament> tournaments, String repoUrl);
}

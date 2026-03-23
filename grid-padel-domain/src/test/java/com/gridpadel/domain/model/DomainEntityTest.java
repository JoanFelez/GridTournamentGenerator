package com.gridpadel.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEntityTest {

    @Test
    void domainEntityInterfaceShouldBeAccessible() {
        DomainEntity entity = new DomainEntity() {};
        assertThat(entity).isNotNull();
    }
}

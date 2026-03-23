package com.gridpadel.application.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationServiceTest {

    @Test
    void applicationServiceInterfaceShouldBeAccessible() {
        ApplicationService service = new ApplicationService() {};
        assertThat(service).isNotNull();
    }
}

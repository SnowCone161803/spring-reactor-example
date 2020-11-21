package com.gdm.animalsorter.animalfilter;

import com.gdm.animalsorter.animalsource.AnimalsService;
import com.gdm.animalsorter.animalsource.AllAnimalsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class AnimalFilterConfiguration {

    private final AllAnimalsService allAnimalsService;
    private final FilteredAnimalsService filteredAnimalsService;

    @Value("${animal.isFilteringEnabled}")
    private boolean isFilteringEnabled;

    AnimalFilterConfiguration(
        AllAnimalsService allAnimalsService,
        FilteredAnimalsService filteredAnimalsService
    ) {
        this.allAnimalsService = allAnimalsService;
        this.filteredAnimalsService = filteredAnimalsService;
    }

    @Bean
    @Primary // Spring can't tell the difference between subclasses and @Bean annotated things
    AnimalsService animalsServiceImpl() {
        log.debug("animal filtering is: " + (isFilteringEnabled ? "enabled" : "disabled"));
        return isFilteringEnabled
            ? this.filteredAnimalsService
            : this.allAnimalsService;
    }
}

package com.gdm.animalsorter.descriptions;

import reactor.core.publisher.Flux;

public interface AnimalDescriptionService {
    Flux<String> getDescriptions();
}

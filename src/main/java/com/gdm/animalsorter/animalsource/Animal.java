package com.gdm.animalsorter.animalsource;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Animal {
    final String name;
    final AnimalType type;
}

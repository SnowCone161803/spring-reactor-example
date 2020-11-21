package com.gdm.animalsorter;

import com.gdm.animalsorter.animalsource.Animal;
import org.junit.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import static org.junit.Assert.assertEquals;

public class TestEmittingFromSink {
    private Sinks.Many<Publisher<Animal>> animalSink = Sinks.many().multicast().onBackpressureBuffer();

    @Test
    public void emittingFromABasicSink() throws Exception {
        final Sinks.Many<Publisher<String>> animalSink = Sinks.many().multicast().onBackpressureBuffer();
        var emitResult = animalSink.tryEmitNext(Mono.just("emittingFromABasicSink"));
        animalSink.asFlux().flatMap(f -> f).subscribe(System.out::println);
        assertEquals(emitResult, Sinks.EmitResult.OK);
    }
}

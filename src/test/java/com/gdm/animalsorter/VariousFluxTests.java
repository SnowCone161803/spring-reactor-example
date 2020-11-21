package com.gdm.animalsorter;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
public class VariousFluxTests {

    @Ignore("uses real time")
    @Test
    public void intervalFlux_isHot() throws Exception {
        final var first = Flux.interval(Duration.ofMillis(100));
        first.skip(2).subscribe(System.out::println);
        first.subscribe(System.out::println);
        Thread.sleep(1000);
    }

    @Ignore("uses real time")
    @Test
    public void unicastEmitterCannotBeSubscribedToTwice() throws Exception {
        final Sinks.Many<String> unicastSink = Sinks.many().unicast().onBackpressureBuffer();
        unicastSink
            .asFlux()
            .subscribe(System.out::println);

        unicastSink.asFlux()
            .subscribe(
                System.out::println,
                (e) -> System.out.println("Could not subscribe twice")
            );

        Flux.interval(Duration.ofMillis(100))
                .take(4)
                .subscribe((i) -> unicastSink.tryEmitNext("boop" + i));

        Thread.sleep(1000);
    }

    @Ignore("uses real time")
    @Test
    public void multicastEmitter_canHaveMultipleSubscribers() throws Exception {
        final Sinks.Many<String> multicastSink = Sinks.many().multicast().onBackpressureBuffer();
        multicastSink.asFlux()
                .subscribe(i -> System.out.println("first: " + i));

        multicastSink.asFlux()
                .subscribe(i -> System.out.println("second: " + i));

        final Disposable subscription = Flux.interval(Duration.ofMillis(100))
                .take(4)
                .subscribe((i) -> multicastSink.tryEmitNext("boop" + i));

        Thread.sleep(1000);
        subscription.dispose();

        multicastSink.asFlux()
                .subscribe(i -> System.out.println("afterwards: " + i));
    }

    @Test
    public void multicastEmitter_isItHot() throws Exception {
        final Sinks.Many<String> multicastSink = Sinks.many().multicast().onBackpressureBuffer();

        Flux.range(1, 10)
            .subscribe((i) -> multicastSink.tryEmitNext("boop " + i));

        // complete subscription
        var disposable = multicastSink.asFlux()
            .subscribe(i -> {});

        // hot flux gives only latest elements on second subscription
        StepVerifier.create(multicastSink.asFlux().count())
                .expectNext(0L)
                .expectComplete();
    }

    @Test
    public void range_isMulticast() throws Exception {
        final var rangeFlux = Flux.range(1,10);
        rangeFlux.subscribe((i) -> {});
        rangeFlux.subscribe((i) -> {});
        // expect no exception
    }

    @Test
    public void merge_returnsMulticast() throws Exception {
        final var first = Flux.range(1,10);
        final var second = Flux.range(100,10);

        final var merged = Flux.merge(first, second);

        merged.subscribe((i) -> {});
        merged.subscribe((i) -> {});
        // expect no exception
    }

    @Test()
    public void unicast_onlysubscribeOnce() throws Exception {
        var unicast = createUnicastFlux();
        unicast.subscribe((i) -> {});
        try {
            // second subscription
            unicast.subscribe(
                this::expectNoNext,
                this::expectedIllegalStateException);
        } catch (IllegalStateException e) {
            // expected exception
        }
    }

    @Test
    public void mergingUnicast_createsUnsubscribableMerged() throws Exception {
        final var unicast = createUnicastFlux();

        final var merged = Flux.merge(unicast);

        // throws exception
        merged.subscribe(
            this::expectNoNext,
            this::expectedIllegalStateException);
    }

    @Test
    public void replayUnicast_createsMultiCast() throws Exception {
        final var unicastReplay = createUnicastFlux().replay();

        unicastReplay.subscribe(System.out::println);
        unicastReplay.subscribe(System.out::println);
    }

    @Test
    public void groupedBy_isMulticast() throws Exception {
        final var numbers = Flux.range(0,10);
        final var grouped = numbers.groupBy((n) -> n % 2);

        grouped.subscribe((i) -> {});
        grouped.subscribe((i) -> {});
    }

    @Test
    public void groupedBy_isColdFlux() throws Exception {
        final var numberOfGroups = 10000;
        final var streamLength = numberOfGroups * 4;
        final var numbers = Flux.range(0,streamLength);
        final var grouped = numbers.groupBy((n) -> n % numberOfGroups);

        grouped.subscribe((i) -> {});
        grouped.count().subscribe((count) -> {
            if (count != numberOfGroups) {
                throw new RuntimeException("incorrect count");
            }
        });
    }

    @Test
    public void showHowCheckpointWorks() throws Exception {
        final var unicastFlux = Flux.range(1,100);
        final var checkpointFlux = unicastFlux.checkpoint("unicast flux checkpoint", true);
        checkpointFlux.subscribe((i) -> System.out.println("unicast flux result: " + i));

        Thread.sleep(1000);
    }

    @Test
    public void groupedBy_allGroupsMustBeConsumedOrTheyAllBlock() throws Exception {
        final var numberOfGroups = 100;
        final var numberOfItemsPerGroup = 10;

        final var streamLength = numberOfGroups * numberOfItemsPerGroup;
        assert (long) numberOfGroups * (long) numberOfItemsPerGroup == (long) streamLength : "stream too large";
        final var numbers = Flux.range(0, streamLength);
        final var grouped = numbers
                .groupBy((n) -> n % numberOfGroups)
                // needed to ensure that all groups are parsed
                .replay();

        final var groupZeroCount = grouped
                .filter((g) -> g.key() == 0)
                .flatMap(f -> f)
                .count();

        final var otherGroupsCount = grouped
                .filter((g) -> g.key() != 0)
                .flatMap(f -> f)
                .count();

        AtomicBoolean zeroCountComplete = new AtomicBoolean(false);
        // consume group zero; otherwise stream blocks and does not complete
        groupZeroCount.subscribe((zeroCount) -> {
            zeroCountComplete.set(true);
            //System.out.println("zeroCount " + zeroCount);
            if (zeroCount != 10) {
                throw new RuntimeException("Expected a different zeroCount " + zeroCount);
            }
        });

        AtomicBoolean otherCountComplete = new AtomicBoolean(false);
        // consume the rest of the groups; otherwise stream blocks and does not complete
        otherGroupsCount.subscribe((otherCount) -> {
            otherCountComplete.set(true);
            //System.out.println("otherCount " + otherCount);
            if (otherCount != streamLength - numberOfItemsPerGroup) {
                final String[] message = {
                        "expected an otherCount of ",
                        Integer.valueOf(streamLength - numberOfItemsPerGroup).toString(),
                        " but instead got ",
                        Long.valueOf(otherCount).toString()
                };

                throw new RuntimeException(String.join("", message));
            }
        });

        // start streams after call to replay()
        grouped.connect();

        // this works because Flux.range(0, 10); is synchronous
        // blockLast() would be needed otherwise
        final var streamIsComplete = zeroCountComplete.get() && otherCountComplete.get();
        assert streamIsComplete : "did not complete streams";
    }

    private Flux<String> createUnicastFlux() {
        final Sinks.Many<String> unicastSink = Sinks.many().unicast().onBackpressureBuffer();
        final Disposable subscription = Flux.interval(Duration.ofMillis(100))
            .take(10)
            .subscribe((i) -> unicastSink.tryEmitNext("boop" + i));
        subscription.dispose();
        return unicastSink.asFlux();
    }

    private void expectNoNext(Object item) {
        throw new RuntimeException("Unexpected item");
    }

    private void expectedIllegalStateException(Throwable e) {
        if (e.getClass().equals(IllegalStateException.class)) {
            // expected exception
            return;
        }
        System.out.println("Expected " + IllegalStateException.class.getName() + " but got " + e.getClass().getName());
    }
}

package net.devotopia.observer.ex01;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reactive Streams - Operators
 * Publisher -> Data -> Subscriber
 * Publisher -> Data1 -> Operators1 -> Data2 -> Operators2 -> data3 -> Subscriber
 * <p>
 * [data] -------> data가 위에서 아래로 흐르는것을 down stream
 * pub -> Data1 -> mapPub -> data2 -> logSub
 * <- subscribe(logsub())
 * -> onSubscribe(s)
 * -> onNext
 * -> onNext
 * -> onNext
 * -> onComplete
 * <------- [data] data가 아래에서 위로 흐르는것을 up stream
 * <p>
 * 1. map (d1 -> f -> d2)
 */
public class PubSub12 {
    private static final Logger logger = LoggerFactory.getLogger(PubSub12.class.getName());

    public static void main(String[] args) {
        Iterable<Integer> iter = Stream.iterate(1, a -> a + 1).limit(10)
                .collect(Collectors.toList());

        Publisher<Integer> pub = iterPub(iter);

        Publisher<StringBuilder> reducePub = reducePub(pub, new StringBuilder(), (a, b) -> a.append(" , "+ b));

        reducePub.subscribe(logSub());
    }

    //                                                                          arg1, arg2, result
    private static <T, R> Publisher<R> reducePub(Publisher<T> pub, R i, BiFunction<R, T, R> bf) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                pub.subscribe(new DelegateSub3<T, R>(sub) {
                    R result = i;

                    @Override
                    public void onNext(T integer) {
                        result = bf.apply(result, integer);
                    }

                    @Override
                    public void onComplete() {
                        sub.onNext(result);
                        sub.onComplete();
                    }
                });
            }
        };
    }

    // 1, 2, 3, 4, 5
    // 0 -> (0, 1) -> 0 + 1 = 1
    // 1 -> (1, 2) -> 1 + 2 = 3
    // 3 -> (3, 3) -> 3 + 3 = 6
    // 6 -> (6, 4) -> 6 + 4 = 10

    private static Publisher<Integer> sumPub(Publisher<Integer> pub) {
        return sub -> pub.subscribe(new DelegateSub1(sub) {
            int sum = 0;
            @Override
            public void onNext(Integer integer) {
                sum += integer;
            }

            @Override
            public void onComplete() {
                sub.onNext(sum);
                sub.onComplete();
            }
        });
    }

    // Source에 해당하는 타임은 T이고 무언가를 감싸서 새로 만들어 넘겨주는 타임은 R이다
    // func은 T를 넣으주고 R이 return되게 해준다
    private static <T, R> Publisher<R> mapPub(Publisher<T> pub, Function<T, R> f) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) { // 받는 쪽에서 원하는 타임은 R
                pub.subscribe(new DelegateSub3<T, R>(sub) {
                    @Override
                    public void onNext(T i) {
                        sub.onNext(f.apply(i));
                    }
                });
            }
        };
    }

    private static <T> Subscriber<T> logSub() {
        return new Subscriber<T>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                logger.debug("onComplete");
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(T i) {
                logger.debug("onNext: {}", i);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.debug("onError: {}", throwable.getMessage());
            }

            @Override
            public void onComplete() {
                logger.debug("onComplete");
            }
        };
    }

    private static Publisher<Integer> iterPub(Iterable<Integer> iter) {
        return sub -> sub.onSubscribe(new Subscription() {
            @Override
            public void request(long l) {
                try {
                    iter.forEach(s -> sub.onNext(s));
                    sub.onComplete();
                } catch (Throwable t) {
                    sub.onError(t);
                }
            }

            @Override
            public void cancel() {

            }
        });
    }
}

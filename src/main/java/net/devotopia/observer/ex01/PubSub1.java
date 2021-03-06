package net.devotopia.observer.ex01;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Arrays;
import java.util.Iterator;

public class PubSub1 {
    // Publisher <-- Observable
    // Subscriber <-- Observer
    public static void main(String[] args) {
        Iterable<Integer> iter = Arrays.asList(1, 2, 3, 4, 5);

        Publisher p = new Publisher() {
            @Override
            public void subscribe(Subscriber subscriber) {
                Iterator<Integer> it = iter.iterator();

                subscriber.onSubscribe(new Subscription() {
                    @Override
                    public void request(long l) {
                        try {
//                        while (true) {
                            while (l-- > 0) {
                                if (it.hasNext()) {
                                    subscriber.onNext(it.next());
                                } else {
                                    subscriber.onComplete();
                                    break;
                                }
                            }
                        } catch (RuntimeException e) {
                            subscriber.onError(e);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        Subscriber<Integer> s = new Subscriber<Integer>() {
            private Subscription subscription;
            private int bufferSize = 2;

            @Override
            public void onSubscribe(Subscription subscription) {
                // 받는 데이터 설정 처리

                System.out.println("onSubscribe");
//                subscription.request(Long.MAX_VALUE);
                this.subscription = subscription;

                // this.subscription.request(1);
                this.subscription.request(2);
            }

            @Override
            public void onNext(Integer integer) {
                System.out.println("onNext " + integer);
                // this.subscription.request(1);

                if (--this.bufferSize <= 0) {
                    this.bufferSize = 2;
                    this.subscription.request(2);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("onError");
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };

        p.subscribe(s);
    }

    /**
     * 1. 퍼블리셔에 서브스크라이버를 등록 -> subscribe
     * 2. 서브스크라이버는 서브스크립션으로 등록
     * 3. 서브스크립션은 백프레셔 역할을
     *
     */
}

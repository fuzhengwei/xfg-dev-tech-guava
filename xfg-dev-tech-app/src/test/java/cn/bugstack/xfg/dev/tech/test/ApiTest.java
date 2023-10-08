package cn.bugstack.xfg.dev.tech.test;

import cn.bugstack.xfg.dev.tech.test.entity.UserEntity;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.google.common.net.InternetDomainName;
import com.google.common.reflect.Invokable;
import com.google.common.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Guava https://github.com/google/guava/wiki
 */
@Slf4j
public class ApiTest {

    /**
     * 功能：基本工具
     * 文档：
     */
    @Test
    public void test_ObjectCommonMethods() {
        UserEntity userEntity = UserEntity.builder()
                .amount(100D)
                .userName("xfg")
                .password("abc000")
                .createTime(new Date())
                .build();

        log.info("测试结果 isEqual: {}", Objects.equal(userEntity, null));

        Optional<Integer> possible = Optional.of(5);
        log.info("测试结果 isPresent: {} get: {}", possible.isPresent(), possible.get());
    }

    /**
     * 功能：不可变集合
     * 文档：https://github.com/google/guava/wiki/ImmutableCollectionsExplained
     */
    @Test
    public void test_immutable() {
        ImmutableList<String> list = ImmutableList.of("a", "b", "c");
    }

    /**
     * 功能：缓存
     * 文档：<a href="https://github.com/google/guava/wiki/CachesExplained">CachesExplained</a>
     */
    @Test
    public void test_cache() {
        Cache<String, String> cache = CacheBuilder.newBuilder()
                // 最大存储条数，缓存将尝试逐出最近或不经常使用的条目
                .maximumSize(10000)
                // 可以设定删除时候的权重判断
                .weigher((Weigher<String, String>) (x, y) -> x.length() - y.length())
                // 有效时间
                .expireAfterWrite(3, TimeUnit.SECONDS)
                // 记录次数
                .recordStats()
                .build();

        cache.put("xfg", "bugstack.cn");
        log.info("测试结果：{}", cache.getIfPresent("xfg"));

        cache.invalidate("xfg"); // cache.invalidateAll(); 也可以全部删除

        log.info("测试结果：{}", cache.getIfPresent("xfg"));
        log.info("测试结果：{}", cache.stats());
    }

    /**
     * 功能：并发回调
     * 文档：https://github.com/google/guava/wiki/ListenableFutureExplained
     */
    @Test
    public void test_ListenableFuture() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
        ListenableFuture<String> explosion = executorService.submit(() -> "finished");

        ExecutorService callBackService = Executors.newFixedThreadPool(1);
        Futures.addCallback(explosion, new FutureCallback<String>() {
            public void onSuccess(String explosion) {
                System.out.println("onSuccess");
                countDownLatch.countDown();
            }

            public void onFailure(Throwable thrown) {
                System.out.println("onFailure");
                countDownLatch.countDown();
            }
        }, callBackService);

        countDownLatch.await();
    }

    /**
     * 功能：字符串
     * 文档：https://github.com/google/guava/wiki/StringsExplained
     */
    @Test
    public void test_StringsExplained() {
        Joiner joiner = Joiner.on("; ").skipNulls();
        log.info("测试结果：{}", joiner.join("Harry", null, "Ron", "Hermione"));

        log.info("测试结果：{}", Joiner.on(",").join(Arrays.asList(1, 5, 7)));
    }

    /**
     * 功能：域名截取
     * 文档：https://github.com/google/guava/wiki/InternetDomainNameExplained
     */
    @Test
    public void test_InternetDomainName() {
        InternetDomainName owner = InternetDomainName.from("mail.google.com").topPrivateDomain();
        log.info("测试结果：{}", owner.topPrivateDomain());
    }

    /**
     * 功能：布隆过滤器
     * 文档：https://github.com/google/guava/wiki/HashingExplained#bloomfilter
     */
    @Test
    public void test_BloomFilter() {
        BloomFilter<String> bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()),
                1000,
                0.01);

        // 向布隆过滤器中添加元素
        bloomFilter.put("apple");
        bloomFilter.put("banana");
        bloomFilter.put("orange");

        // 检查元素是否存在于布隆过滤器中
        System.out.println(bloomFilter.mightContain("apple"));   // true
        System.out.println(bloomFilter.mightContain("banana"));  // true
        System.out.println(bloomFilter.mightContain("orange"));  // true
        System.out.println(bloomFilter.mightContain("grape"));   // false

        // 输出布隆过滤器的统计信息
        System.out.println("Expected FPP: " + bloomFilter.expectedFpp());
        System.out.println("Number of Inserted Elements: " + bloomFilter.approximateElementCount());
    }

    /**
     * 功能：反射
     * 文档：https://github.com/google/guava/wiki/ReflectionExplained
     */
    @Test
    public void test_Invokable() throws NoSuchMethodException {
        Method method = UserEntity.class.getMethod("getUserName");
        Invokable<?, ?> invokable = Invokable.from(method);
        log.info("测试结果 - 方法名称：{}", invokable.getName());
        log.info("测试结果 - 参数类型：{}", JSON.toJSONString(invokable.getTypeParameters()));
        log.info("测试结果 - 静态判断：{}", invokable.isStatic());
        // !(Modifier.isFinal(method.getModifiers()) || Modifiers.isPrivate(method.getModifiers()) || Modifiers.isStatic(method.getModifiers()) || Modifiers.isFinal(method.getDeclaringClass().getModifiers()))
        log.info("测试结果 - isOverridable：{}", invokable.isOverridable());
    }

}

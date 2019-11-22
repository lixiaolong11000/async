package com.example.asyn;

import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AsynApplicationTests {

    @Test
    void contextLoads() {
    }


    @Test
    public void asynExposeProxy() throws InterruptedException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfigure.class);
        AsyncTransactionalTestBean asyncTransactionalTestBean = ctx.getBean(AsyncTransactionalTestBean.class);
        AsyncTestBean asyncTestBean = ctx.getBean(AsyncTestBean.class);

        assertThat(AopUtils.isAopProxy(asyncTransactionalTestBean)).as("asyncTransactionalTestBean is not a proxy").isTrue();
        assertThat(AopUtils.isAopProxy(asyncTestBean)).as("asyncTestBean is not a proxy").isTrue();

        asyncTransactionalTestBean.testTransToAsync();

        asyncTransactionalTestBean.testAsyncToAsync();

        asyncTestBean.testAsyncToAsyncTOAsync();

        ctx.close();
    }


    @Service
    public static class AsyncTransactionalTestBean {

        @Transactional
        public Collection<?> testTransToAsync() {
            System.out.println("testTransToAsync " + Thread.currentThread().getName());
            ((AsyncTransactionalTestBean) AopContext.currentProxy()).testAsyncToAsync();
            return null;
        }


        @Async
        public void testAsyncToAsync() {
            System.out.println("testAsyncToAsync " + Thread.currentThread().getName());
            ((AsyncTransactionalTestBean) AopContext.currentProxy()).testAsync();
        }

        @Async
        public void testAsync() {
            System.out.println("testAsync " + Thread.currentThread().getName());
        }
    }


    @Service
    public static class AsyncTestBean {

        @Async
        public Collection<?> testAsyncToAsyncTOAsync() {
            System.out.println("testAsyncToAsyncTOAsync " + Thread.currentThread().getName());
            ((AsyncTransactionalTestBean) AopContext.currentProxy()).testAsyncToAsync();
            return null;
        }

        @Async
        public void testAsyncToAsync() {
            System.out.println("testAsyncToAsync " + Thread.currentThread().getName());
            ((AsyncTransactionalTestBean) AopContext.currentProxy()).testAsync();
        }

        @Async
        public void testAsync() {
            System.out.println("testAsync " + Thread.currentThread().getName());
        }
    }


    @EnableAsync
    @EnableAspectJAutoProxy(exposeProxy = true)
    @Configuration
    @EnableTransactionManagement
    static class AppConfigure  {

        @Bean
        public AsyncTransactionalTestBean asyncTransactionalTestBean() {
            return new AsyncTransactionalTestBean();
        }

        @Bean
        public AsyncTestBean asyncTestBean() {
            return new AsyncTestBean();
        }

        @Bean
        public PlatformTransactionManager txManager() {
            return new MockTransactionManager();
        }
    }
}

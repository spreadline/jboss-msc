/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.msc.util;

import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Service listener that allow certain behaviors to be expected.  Like services starting, stopping, etc.  The expect
 * calls return a {@code java.util.concurrent.Future} that can be used to retrieve the controller once the expected behavior has occurred.
 *
 * @author John E. Bailey
 */
public class TestServiceListener extends AbstractServiceListener<Object> {
    private final Map<ServiceName, ServiceFuture> expectedStarts = new HashMap<ServiceName, ServiceFuture>();
    private final Map<ServiceName, ServiceFuture> expectedStops = new HashMap<ServiceName, ServiceFuture>();
    private final Map<ServiceName, ServiceFuture> expectedStoppings = new HashMap<ServiceName, ServiceFuture>();
    private final Map<ServiceName, ServiceFutureWithValidation> expectedStopsOnly = new HashMap<ServiceName, ServiceFutureWithValidation>();
    private final Map<ServiceName, ServiceFuture> expectedRemovals = new HashMap<ServiceName, ServiceFuture>();
    private final Map<ServiceName, ServiceFailureFuture> expectedFailures = new HashMap<ServiceName, ServiceFailureFuture>();
    private final Map<ServiceName, ServiceFuture> expectedDependencyFailures = new HashMap<ServiceName, ServiceFuture>();
    private final Map<ServiceName, ServiceFuture> expectedDependencyRetryings = new HashMap<ServiceName, ServiceFuture>();
    private final Map<ServiceName, ServiceFuture> expectedDependencyUninstalls = new HashMap<ServiceName, ServiceFuture>();
    private final Map<ServiceName, ServiceFuture> expectedDependencyInstalls = new HashMap<ServiceName, ServiceFuture>();
    private final Map<ServiceName, ServiceFuture> expectedListenerAddeds = new HashMap<ServiceName, ServiceFuture>();

    public void listenerAdded(final ServiceController<? extends Object> serviceController) {
        final ServiceFuture future = expectedListenerAddeds.remove(serviceController.getName());
        if(future != null) {
            future.setServiceController(serviceController);
        }
    }
    
    public void serviceStarted(final ServiceController<? extends Object> serviceController) {
        final ServiceFuture future = expectedStarts.remove(serviceController.getName());
        if(future != null) {
            future.setServiceController(serviceController);
        }
    }

    public void serviceStopped(ServiceController<? extends Object> serviceController) {
        final ServiceFuture future = expectedStops.remove(serviceController.getName());
        expectedStopsOnly.remove(serviceController.getName());
        if(future != null) {
            future.setServiceController(serviceController);
        }
    }

    public void serviceStopping(ServiceController<? extends Object> serviceController) {
        final ServiceFutureWithValidation invalidFuture = expectedStopsOnly.remove(serviceController.getName());
        if(invalidFuture != null) {
            invalidFuture.invalidate();
        }
        else {
            final ServiceFuture future = expectedStoppings.remove(serviceController.getName());
            if (future != null) {
                future.setServiceController(serviceController);
            }
        }
    }

    public void serviceRemoved(ServiceController<? extends Object> serviceController) {
        final ServiceFuture future = expectedRemovals.remove(serviceController.getName());
        if(future != null) {
            future.setServiceController(serviceController);
        }
    }

    public void serviceFailed(ServiceController<? extends Object> serviceController, StartException reason) {
        final ServiceFailureFuture future = expectedFailures.remove(serviceController.getName());
        if(future != null) {
            future.setStartException(reason);
        }
    }

    public void dependencyFailed(final ServiceController<? extends Object> serviceController) {
        final ServiceFuture future = expectedDependencyFailures.remove(serviceController.getName());
        if(future != null) {
            future.setServiceController(serviceController);
        }
    }

    public void dependencyFailureCleared(final ServiceController<? extends Object> serviceController) {
        final ServiceFuture future = expectedDependencyRetryings.remove(serviceController.getName());
        if(future != null) {
            future.setServiceController(serviceController);
        }
    }

    public void dependencyInstalled(ServiceController<? extends Object> serviceController) {
        final ServiceFuture future = expectedDependencyInstalls.remove(serviceController.getName());
        if(future != null) {
            future.setServiceController(serviceController);
        }
    }

    public void dependencyUninstalled(ServiceController<? extends Object> serviceController) {
        final ServiceFuture future = expectedDependencyUninstalls.remove(serviceController.getName());
        if(future != null) {
            future.setServiceController(serviceController);
        }
    }

    public Future<ServiceController<?>> expectListenerAdded(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture();
        expectedListenerAddeds.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectNoListenerAdded(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture(200);
        expectedListenerAddeds.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectServiceStart(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture();
        expectedStarts.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectNoServiceStart(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture(200);
        expectedStarts.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectServiceStop(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture();
        expectedStops.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectNoServiceStop(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture(200);
        expectedStops.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectServiceStoppedOnly(final ServiceName serviceName) {
        final ServiceFutureWithValidation future = new ServiceFutureWithValidation();
        expectedStops.put(serviceName, future);
        expectedStopsOnly.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectServiceStopping(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture();
        expectedStoppings.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectNoServiceStopping(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture(200);
        expectedStoppings.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectServiceRemoval(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture();
        expectedRemovals.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectNoServiceRemoval(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture(200);
        expectedRemovals.put(serviceName, future);
        return future;
    }

    public Future<StartException> expectServiceFailure(final ServiceName serviceName) {
        final ServiceFailureFuture future = new ServiceFailureFuture();
        expectedFailures.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectDependencyFailure(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture();
        expectedDependencyFailures.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectNoDependencyFailure(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture(200);
        expectedDependencyFailures.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectDependencyFailureCleared(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture();
        expectedDependencyRetryings.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectDependencyInstall(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture();
        expectedDependencyInstalls.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectDependencyUninstall(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture();
        expectedDependencyUninstalls.put(serviceName, future);
        return future;
    }

    public Future<ServiceController<?>> expectNoDependencyUninstall(final ServiceName serviceName) {
        final ServiceFuture future = new ServiceFuture(200);
        expectedDependencyUninstalls.put(serviceName, future);
        return future;
    }

    private static class ServiceFuture implements Future<ServiceController<?>> {
        private ServiceController<?> serviceController;
        private final CountDownLatch countDownLatch = new CountDownLatch(1);
        private final long delay;
        private final TimeUnit delayTimeUnit;

        ServiceFuture() {
            delay = 30L;
            delayTimeUnit = TimeUnit.SECONDS;
        }

        ServiceFuture(long timeInMs) {
            delay = timeInMs;
            delayTimeUnit = TimeUnit.MILLISECONDS;
        }
        
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return serviceController != null;
        }

        @Override
        public ServiceController<?> get() throws InterruptedException, ExecutionException {
            try {
                return get(delay, delayTimeUnit);
            } catch (TimeoutException e) {
                throw new RuntimeException("Could not get start exception in " + delay + " " + delayTimeUnit + " timeout.");
            }
        }

        @Override
        public ServiceController<?> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            countDownLatch.await(timeout, unit);
            return serviceController;
        }

        private void setServiceController(final ServiceController<?> serviceController) {
            this.serviceController = serviceController;
            countDownLatch.countDown();
        }
    }

    private static class ServiceFutureWithValidation extends ServiceFuture {
        private boolean valid = true;

        ServiceFutureWithValidation() {
            super();
        }

        public void invalidate() {
            valid = false;
        }

        @Override
        public ServiceController<?> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            ServiceController<?> result = super.get(timeout, unit);
            return valid? result: null;
        }
    }

    private static class ServiceFailureFuture implements Future<StartException> {
        private StartException startException;
        private final CountDownLatch countDownLatch = new CountDownLatch(1);

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return startException != null;
        }

        @Override
        public StartException get() throws InterruptedException, ExecutionException {
            try {
                return get(30L, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new RuntimeException("Could not get start exception in 30 second timeout.");
            }
        }

        @Override
        public StartException get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            countDownLatch.await(timeout, unit);
            return startException;
        }

        public void setStartException(StartException startException) {
            this.startException = startException;
            countDownLatch.countDown();
        }
    }
}

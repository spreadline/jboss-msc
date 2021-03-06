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

package org.jboss.msc.value;

import static org.jboss.msc.value.ErrorMessage.noSuchMethod;

import java.lang.reflect.Method;
import java.util.List;

/**
 * A value which looks up a public method by name and parameters from a class.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public final class LookupMethodValue implements Value<Method> {
    private final Value<Class<?>> target;
    private final String methodName;
    private final List<? extends Value<Class<?>>> parameterTypes;
    private final int paramCount;

    /**
     * Construct a new instance.
     *
     * @param target the class in which to look for the method
     * @param methodName the name of the method
     * @param parameterTypes the method parameter types
     */
    public LookupMethodValue(final Value<Class<?>> target, final String methodName, final List<? extends Value<Class<?>>> parameterTypes) {
        if (target == null) {
            throw new IllegalArgumentException("target is null");
        }
        if (methodName == null) {
            throw new IllegalArgumentException("methodName is null");
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException("parameterTypes is null");
        }
        this.target = target;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        paramCount = parameterTypes.size();
    }

    public LookupMethodValue(final Value<Class<?>> target, final String methodName, final int paramCount) {
        if (target == null) {
            throw new IllegalArgumentException("target is null");
        }
        if (methodName == null) {
            throw new IllegalArgumentException("methodName is null");
        }
        this.target = target;
        this.methodName = methodName;
        parameterTypes = null;
        this.paramCount = paramCount;
    }

    /** {@inheritDoc} */
    public Method getValue() throws IllegalStateException {
        final Class<?> targetClass = target.getValue();
        final List<? extends Value<Class<?>>> parameterTypes = this.parameterTypes;
        final String methodName = this.methodName;
        if (parameterTypes != null) {
            Class<?>[] types = new Class[parameterTypes.size()];
            int i = 0;
            for (Value<Class<?>> type : parameterTypes) {
                types[i++] = type.getValue();
            }
            try {
                return targetClass.getMethod(methodName, types);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(noSuchMethod(targetClass, methodName, parameterTypes));
            }
        } else {
            final int paramCount = this.paramCount;
            for (Method method : targetClass.getMethods()) {
                if (method.getName().equals(methodName) && method.getParameterTypes().length == paramCount) {
                    return method;
                }
            }
            throw new IllegalStateException("No such method '" + methodName + "' found on " + targetClass);
        }
    }
}
